import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Comparator;
import java.util.stream.Stream;
import javax.tools.JavaCompiler;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;

public final class CompileSources {
    private static String expandClasspath(String value) throws Exception {
        List<String> entries = new ArrayList<>();
        for (String entry : value.split(java.util.regex.Pattern.quote(File.pathSeparator))) {
            if (entry.endsWith("*")) {
                Path directory = Path.of(entry.substring(0, entry.length() - 1));
                try (Stream<Path> paths = Files.list(directory)) {
                    paths.filter(path -> path.toString().endsWith(".jar"))
                         .sorted(Comparator.naturalOrder())
                         .map(Path::toString)
                         .forEach(entries::add);
                }
            } else {
                entries.add(entry);
            }
        }
        return String.join(File.pathSeparator, entries);
    }

    public static void main(String[] args) throws Exception {
        if (args.length != 3 && args.length != 4) {
            System.err.println(
                "usage: CompileSources SOURCE_ROOT OUTPUT_DIR CLASSPATH [SOURCE_LIST]");
            System.exit(2);
        }
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        if (compiler == null) {
            throw new IllegalStateException("system Java compiler is unavailable");
        }
        List<File> sources = new ArrayList<>();
        Path sourceRoot = Path.of(args[0]);
        if (args.length == 4) {
            for (String rawLine : Files.readAllLines(Path.of(args[3]))) {
                String line = rawLine.strip();
                if (!line.isEmpty() && !line.startsWith("#")) {
                    Path source = sourceRoot.resolve(line).normalize();
                    if (!source.startsWith(sourceRoot.normalize()) || !Files.isRegularFile(source)) {
                        throw new IllegalArgumentException("missing or invalid source: " + line);
                    }
                    sources.add(source.toFile());
                }
            }
            sources.sort(Comparator.naturalOrder());
        } else {
            try (Stream<Path> paths = Files.walk(sourceRoot)) {
                paths.filter(path -> path.toString().endsWith(".java"))
                     .sorted()
                     .map(Path::toFile)
                     .forEach(sources::add);
            }
        }
        Files.createDirectories(Path.of(args[1]));
        try (StandardJavaFileManager files = compiler.getStandardFileManager(null, null, null)) {
            boolean ok = compiler.getTask(
                null,
                files,
                null,
                Arrays.asList("-source", "11", "-target", "11",
                              "-encoding", "UTF-8", "-proc:none",
                              "-classpath", expandClasspath(args[2]), "-d", args[1]),
                null,
                files.getJavaFileObjectsFromFiles(sources)
            ).call();
            if (!ok) System.exit(1);
        }
        System.out.println("compiled " + sources.size() + " Java sources");
    }
}
