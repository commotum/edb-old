import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/** Writes the JVM-expanded classpath to a dedicated, one-entry-per-line file. */
public final class PrintRuntimeClasspath {
    private PrintRuntimeClasspath() {}

    public static void main(String[] args) throws Exception {
        if (args.length != 1) {
            throw new IllegalArgumentException(
                "usage: PrintRuntimeClasspath.java OUTPUT_FILE");
        }
        String classpath = System.getProperty("java.class.path");
        if (classpath == null || classpath.isEmpty()) {
            throw new IllegalStateException("java.class.path is empty");
        }
        String[] entries = classpath.split(
            java.util.regex.Pattern.quote(java.io.File.pathSeparator), -1);
        StringBuilder output = new StringBuilder();
        for (String entry : entries) {
            if (entry.isEmpty() || entry.indexOf('\n') >= 0 || entry.indexOf('\r') >= 0) {
                throw new IllegalStateException("invalid runtime classpath entry");
            }
            output.append(Path.of(entry).toAbsolutePath().normalize())
                  .append('\n');
        }
        Files.writeString(Path.of(args[0]), output.toString(),
                          StandardCharsets.UTF_8);
    }
}
