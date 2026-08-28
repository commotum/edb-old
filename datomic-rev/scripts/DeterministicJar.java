import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.zip.CRC32;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/** Creates a file-only, sorted, fixed-timestamp, stored-entry JAR. */
public final class DeterministicJar {
    private static final LocalDateTime ENTRY_TIME = LocalDateTime.of(1980, 1, 1, 0, 0);

    private static void fail(String message) {
        System.err.println(message);
        System.exit(2);
    }

    public static void main(String[] args) throws Exception {
        if (args.length != 2) {
            fail("usage: DeterministicJar STAGING_ROOT OUTPUT_JAR");
        }

        Path stagingRoot = Path.of(args[0]).toAbsolutePath().normalize();
        Path outputJar = Path.of(args[1]).toAbsolutePath().normalize();
        if (!Files.isDirectory(stagingRoot)) {
            fail("staging root is not a directory: " + stagingRoot);
        }
        if (outputJar.startsWith(stagingRoot)) {
            fail("output JAR must be outside the staging root");
        }
        if (Files.exists(outputJar)) {
            fail("refusing to overwrite output JAR: " + outputJar);
        }

        List<Path> files = new ArrayList<>();
        try (var paths = Files.walk(stagingRoot)) {
            paths.filter(Files::isRegularFile).forEach(files::add);
        }
        files.sort(Comparator.comparing(path -> entryName(stagingRoot, path)));
        if (files.isEmpty()) {
            fail("staging root contains no files");
        }

        Path parent = outputJar.getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
        try (OutputStream raw = Files.newOutputStream(outputJar);
             ZipOutputStream zip = new ZipOutputStream(new BufferedOutputStream(raw))) {
            for (Path file : files) {
                addStoredEntry(zip, entryName(stagingRoot, file), file);
            }
        }
        System.out.println("packaged " + files.size() + " deterministic JAR entries");
    }

    private static String entryName(Path root, Path file) {
        String name = root.relativize(file.toAbsolutePath().normalize())
                          .toString()
                          .replace(file.getFileSystem().getSeparator(), "/");
        if (name.isEmpty() || name.startsWith("/") || name.contains("../")) {
            throw new IllegalArgumentException("unsafe JAR entry name: " + name);
        }
        return name;
    }

    private static void addStoredEntry(ZipOutputStream zip, String name, Path file)
            throws IOException {
        byte[] bytes = Files.readAllBytes(file);
        CRC32 crc = new CRC32();
        crc.update(bytes);

        ZipEntry entry = new ZipEntry(name);
        entry.setMethod(ZipEntry.STORED);
        entry.setSize(bytes.length);
        entry.setCompressedSize(bytes.length);
        entry.setCrc(crc.getValue());
        entry.setTimeLocal(ENTRY_TIME);
        entry.setComment(null);
        entry.setExtra(null);
        zip.putNextEntry(entry);
        zip.write(bytes);
        zip.closeEntry();
    }
}
