import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

/** Verifies the physical ordering and metadata emitted by DeterministicJar. */
public final class InspectJarMetadata {
    private static final LocalDateTime ENTRY_TIME =
            LocalDateTime.of(1980, 1, 1, 0, 0);
    private static final byte[] ENTRY_EXTRA = new byte[] {
        0x55, 0x54, 0x05, 0x00, 0x01, 0x00, (byte) 0xa6, (byte) 0xce, 0x12
    };

    private static void fail(String message) {
        throw new IllegalStateException(message);
    }

    public static void main(String[] args) throws Exception {
        if (args.length != 2) {
            fail("usage: InspectJarMetadata ARTIFACT_JAR EXPECTED_ENTRIES");
        }

        List<String> expected = Files.readAllLines(Path.of(args[1]));
        try (JarFile jar = new JarFile(args[0])) {
            if (jar.getComment() != null) {
                fail("archive comment is present");
            }
            Enumeration<JarEntry> entries = jar.entries();
            int index = 0;
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                if (index >= expected.size()) {
                    fail("archive contains more entries than expected");
                }
                String expectedName = expected.get(index);
                if (!expectedName.equals(entry.getName())) {
                    fail("entry order differs at position " + (index + 1)
                         + ": expected " + expectedName + ", found "
                         + entry.getName());
                }
                if (entry.isDirectory()) {
                    fail("directory entry is present: " + entry.getName());
                }
                if (entry.getMethod() != ZipEntry.STORED) {
                    fail("entry is not STORED: " + entry.getName());
                }
                if (entry.getSize() != entry.getCompressedSize()) {
                    fail("STORED entry sizes differ: " + entry.getName());
                }
                if (!ENTRY_TIME.equals(entry.getTimeLocal())) {
                    fail("entry timestamp differs: " + entry.getName());
                }
                if (entry.getComment() != null) {
                    fail("entry comment is present: " + entry.getName());
                }
                if (!Arrays.equals(ENTRY_EXTRA, entry.getExtra())) {
                    fail("entry extra field differs: " + entry.getName());
                }
                index += 1;
            }
            if (index != expected.size()) {
                fail("archive contains fewer entries than expected");
            }
        }
        System.out.println("deterministic JAR ordering and metadata passed");
    }
}
