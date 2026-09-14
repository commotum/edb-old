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
        if (args.length != 3) {
            System.err.println("usage: CompileSources SOURCE_ROOT OUTPUT_DIR CLASSPATH");
            System.exit(2);
        }
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        if (compiler == null) {
            throw new IllegalStateException("system Java compiler is unavailable");
        }
        List<File> sources = new ArrayList<>();
        try (Stream<Path> paths = Files.walk(Path.of(args[0]))) {
            paths.filter(path -> path.toString().endsWith(".java"))
                 .sorted()
                 .map(Path::toFile)
                 .forEach(sources::add);
        }
        Files.createDirectories(Path.of(args[1]));
        try (StandardJavaFileManager files = compiler.getStandardFileManager(null, null, null)) {
            boolean ok = compiler.getTask(
                null,
                files,
                null,
                Arrays.asList("-source", "11", "-target", "11",
                              "-classpath", expandClasspath(args[2]), "-d", args[1]),
                null,
                files.getJavaFileObjectsFromFiles(sources)
            ).call();
            if (!ok) System.exit(1);
        }
        System.out.println("compiled " + sources.size() + " Java sources");
    }
}
