import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HexFormat;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

/**
 * Read-only, non-class-loading inventory of a JAR's physical entry order and
 * ZIP metadata. This deliberately records evidence; it does not reproduce or
 * execute any archived implementation.
 */
public final class ArchiveInventory {
    private static final HexFormat HEX = HexFormat.of();

    private static String sha256(Path path) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        try (InputStream in = Files.newInputStream(path)) {
            update(digest, in);
        }
        return HEX.formatHex(digest.digest());
    }

    private static String sha256(InputStream in) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        update(digest, in);
        return HEX.formatHex(digest.digest());
    }

    private static String sha256(byte[] bytes) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        return HEX.formatHex(digest.digest(bytes));
    }

    private static void update(MessageDigest digest, InputStream in)
            throws IOException {
        byte[] buffer = new byte[64 * 1024];
        int count;
        while ((count = in.read(buffer)) != -1) {
            digest.update(buffer, 0, count);
        }
    }

    private static String kind(JarEntry entry) {
        if (entry.isDirectory()) return "directory";
        if (entry.getName().endsWith(".class")) return "class";
        return "resource";
    }

    private static String method(JarEntry entry) {
        return switch (entry.getMethod()) {
            case ZipEntry.STORED -> "stored";
            case ZipEntry.DEFLATED -> "deflated";
            default -> Integer.toString(entry.getMethod());
        };
    }

    private static String clean(String value) {
        if (value == null) return "";
        return value.replace("\\", "\\\\")
                .replace("\t", "\\t")
                .replace("\r", "\\r")
                .replace("\n", "\\n");
    }

    public static void main(String[] args) throws Exception {
        if (args.length != 2) {
            throw new IllegalArgumentException(
                    "usage: ArchiveInventory INPUT_JAR OUTPUT_DIRECTORY");
        }

        Path input = Path.of(args[0]).toAbsolutePath().normalize();
        Path output = Path.of(args[1]).toAbsolutePath().normalize();
        if (!Files.isRegularFile(input)) {
            throw new IllegalArgumentException("input JAR not found: " + input);
        }
        Files.createDirectories(output);

        List<String> entries = new ArrayList<>();
        entries.add(String.join("\t",
                "ordinal", "entry", "kind", "method", "size",
                "compressed_size", "crc32", "time_millis", "extra_sha256",
                "comment", "content_sha256"));

        long directories = 0;
        long classes = 0;
        long resources = 0;
        long uncompressed = 0;
        long compressed = 0;
        long stored = 0;
        long deflated = 0;
        int ordinal = 0;
        String archiveComment;

        try (JarFile jar = new JarFile(input.toFile(), false)) {
            archiveComment = jar.getComment();
            Enumeration<JarEntry> enumeration = jar.entries();
            while (enumeration.hasMoreElements()) {
                JarEntry entry = enumeration.nextElement();
                ordinal += 1;
                String entryKind = kind(entry);
                switch (entryKind) {
                    case "directory" -> directories += 1;
                    case "class" -> classes += 1;
                    case "resource" -> resources += 1;
                    default -> throw new IllegalStateException(entryKind);
                }
                if (entry.getSize() >= 0) uncompressed += entry.getSize();
                if (entry.getCompressedSize() >= 0) {
                    compressed += entry.getCompressedSize();
                }
                if (entry.getMethod() == ZipEntry.STORED) stored += 1;
                if (entry.getMethod() == ZipEntry.DEFLATED) deflated += 1;

                byte[] extra = entry.getExtra();
                String extraHash = extra == null ? "" : sha256(extra);
                String contentHash = "";
                if (!entry.isDirectory()) {
                    try (InputStream in = jar.getInputStream(entry)) {
                        contentHash = sha256(in);
                    }
                }

                entries.add(String.join("\t",
                        Integer.toString(ordinal),
                        clean(entry.getName()),
                        entryKind,
                        method(entry),
                        Long.toString(entry.getSize()),
                        Long.toString(entry.getCompressedSize()),
                        Long.toHexString(entry.getCrc()),
                        Long.toString(entry.getTime()),
                        extraHash,
                        clean(entry.getComment()),
                        contentHash));
            }
        }

        List<String> summary = List.of(
                "metric\tvalue",
                "archive.file\t" + input.getFileName(),
                "archive.bytes\t" + Files.size(input),
                "archive.sha256\t" + sha256(input),
                "archive.comment\t" + clean(archiveComment),
                "entries.total\t" + ordinal,
                "entries.directories\t" + directories,
                "entries.classes\t" + classes,
                "entries.resources\t" + resources,
                "entries.stored\t" + stored,
                "entries.deflated\t" + deflated,
                "entries.uncompressed_bytes\t" + uncompressed,
                "entries.compressed_bytes\t" + compressed);

        Files.write(output.resolve("archive-entries.tsv"), entries,
                StandardCharsets.UTF_8);
        Files.write(output.resolve("archive-summary.tsv"), summary,
                StandardCharsets.UTF_8);
    }
}
