import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Exhaustively maps one Transactor namespace scope to exact Clojure source
 * entries in the hash-bound shipped dependency JAR set.  The default scope is
 * the 85 bundled-library namespaces; the optional "datomic" scope audits all
 * 162 Datomic namespaces, including the ones with no shipped source.  The
 * scanner reads ZIPs directly and never loads classes from them.
 */
public final class MapBundledSources {
    private record Namespace(String name, String initEntry, String initSha,
                             String sourceClj, String sourceCljc) {}
    private record JarSpec(String distributionPath, String sha) {}
    private record Match(String jar, String jarSha, String entry,
                         String sourceSha) {}

    private static String sha256(Path path) throws IOException {
        try (InputStream in = Files.newInputStream(path)) {
            return sha256(in);
        }
    }

    private static String sha256(InputStream in) throws IOException {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] buffer = new byte[1 << 16];
            for (int count; (count = in.read(buffer)) >= 0; ) {
                if (count > 0) digest.update(buffer, 0, count);
            }
            return hex(digest.digest());
        } catch (NoSuchAlgorithmException impossible) {
            throw new AssertionError(impossible);
        }
    }

    private static String hex(byte[] bytes) {
        StringBuilder out = new StringBuilder(bytes.length * 2);
        for (byte value : bytes) {
            out.append(String.format("%02x", value & 0xff));
        }
        return out.toString();
    }

    private static List<Namespace> readNamespaces(Path tsv, String scope)
            throws IOException {
        List<Namespace> result = new ArrayList<>();
        try (BufferedReader reader = Files.newBufferedReader(
                tsv, StandardCharsets.UTF_8)) {
            String header = reader.readLine();
            if (!"namespace\tentry\tscope\tsha256".equals(header)) {
                throw new IOException(
                        "unexpected namespace TSV header: " + header);
            }
            for (String line; (line = reader.readLine()) != null; ) {
                String[] fields = line.split("\\t", -1);
                if (fields.length != 4) {
                    throw new IOException("bad namespace row: " + line);
                }
                if (!scope.equals(fields[2])) continue;
                if (!fields[1].endsWith("__init.class")) {
                    throw new IOException(
                            "unexpected init entry: " + fields[1]);
                }
                String base = fields[1].substring(
                        0, fields[1].length() - "__init.class".length());
                result.add(new Namespace(fields[0], fields[1], fields[3],
                        base + ".clj", base + ".cljc"));
            }
        }
        result.sort(Comparator.comparing(Namespace::name));
        int expected = switch (scope) {
            case "bundled" -> 85;
            case "datomic" -> 162;
            default -> throw new IOException(
                    "unsupported namespace scope: " + scope);
        };
        if (result.size() != expected) {
            throw new IOException(
                    "expected " + expected + " " + scope
                    + " namespaces, got " + result.size());
        }
        return result;
    }

    private static List<JarSpec> readJars(Path tsv) throws IOException {
        List<JarSpec> result = new ArrayList<>();
        try (BufferedReader reader = Files.newBufferedReader(
                tsv, StandardCharsets.UTF_8)) {
            String header = reader.readLine();
            if (header == null
                    || !header.startsWith(
                            "jar\tdistribution_path\tsha256\t")) {
                throw new IOException(
                        "unexpected distribution TSV header: " + header);
            }
            for (String line; (line = reader.readLine()) != null; ) {
                String[] fields = line.split("\\t", -1);
                if (fields.length != 12) {
                    throw new IOException("bad distribution row: " + line);
                }
                if (!"dependency".equals(fields[11])) continue;
                result.add(new JarSpec(fields[1], fields[2]));
            }
        }
        result.sort(Comparator.comparing(JarSpec::distributionPath));
        if (result.size() != 533) {
            throw new IOException(
                    "expected 533 dependency JARs, got " + result.size());
        }
        return result;
    }

    private static String join(
            List<Match> matches,
            java.util.function.Function<Match, String> field) {
        return String.join(";", matches.stream().map(field).toList());
    }

    public static void main(String[] args) throws Exception {
        if (args.length < 5 || args.length > 6) {
            throw new IllegalArgumentException(
                    "usage: MapBundledSources NAMESPACE_TSV JAR_TSV "
                    + "DIST_ROOT OUTPUT_TSV REPORT_MD [bundled|datomic]");
        }
        Path namespaceTsv = Path.of(args[0]);
        Path jarTsv = Path.of(args[1]);
        Path distRoot = Path.of(args[2]);
        Path outputTsv = Path.of(args[3]);
        Path reportMd = Path.of(args[4]);
        String scope = args.length == 6 ? args[5] : "bundled";
        List<Namespace> namespaces = readNamespaces(namespaceTsv, scope);
        List<JarSpec> jars = readJars(jarTsv);

        Map<String, Namespace> wanted = new LinkedHashMap<>();
        Map<String, List<Match>> matches = new LinkedHashMap<>();
        for (Namespace ns : namespaces) {
            wanted.put(ns.sourceClj(), ns);
            wanted.put(ns.sourceCljc(), ns);
            matches.put(ns.name(), new ArrayList<>());
        }

        int verifiedJars = 0;
        long scannedEntries = 0;
        for (JarSpec jar : jars) {
            Path path = distRoot.resolve(jar.distributionPath());
            if (!Files.isRegularFile(path)) {
                throw new IOException("missing dependency JAR: " + path);
            }
            String actualJarSha = sha256(path);
            if (!actualJarSha.equals(jar.sha())) {
                throw new IOException(
                        "dependency JAR hash mismatch: " + path
                        + " expected " + jar.sha()
                        + " got " + actualJarSha);
            }
            verifiedJars++;
            try (ZipFile zip = new ZipFile(path.toFile())) {
                var entries = zip.entries();
                while (entries.hasMoreElements()) {
                    ZipEntry entry = entries.nextElement();
                    scannedEntries++;
                    if (entry.isDirectory()) continue;
                    Namespace ns = wanted.get(entry.getName());
                    if (ns == null) continue;
                    try (InputStream in = zip.getInputStream(entry)) {
                        matches.get(ns.name()).add(new Match(
                                jar.distributionPath(), jar.sha(),
                                entry.getName(), sha256(in)));
                    }
                }
            }
        }
        for (List<Match> nsMatches : matches.values()) {
            nsMatches.sort(Comparator.comparing(Match::jar)
                    .thenComparing(Match::entry)
                    .thenComparing(Match::sourceSha));
        }

        List<String> output = new ArrayList<>();
        output.add("namespace\tinit_entry\tinit_sha256\tstatus\tmatch_count"
                + "\towner_jars\towner_jar_sha256s\tsource_entries"
                + "\tsource_sha256s");
        Map<String, Integer> statusCounts = new LinkedHashMap<>();
        Map<String, Integer> ownerCounts = new LinkedHashMap<>();
        List<String> missing = new ArrayList<>();
        List<String> ambiguous = new ArrayList<>();
        for (Namespace ns : namespaces) {
            List<Match> nsMatches = matches.get(ns.name());
            Set<String> distinctHashes = new TreeSet<>();
            for (Match match : nsMatches) {
                distinctHashes.add(match.sourceSha());
            }
            String status;
            if (nsMatches.isEmpty()) {
                status = "missing";
            } else if (nsMatches.size() == 1) {
                status = "single-exact-path";
            } else if (distinctHashes.size() == 1) {
                status = "ambiguous-identical";
            } else {
                status = "ambiguous-different";
            }
            statusCounts.merge(status, 1, Integer::sum);
            if (status.equals("missing")) missing.add(ns.name());
            if (status.startsWith("ambiguous")) {
                ambiguous.add(ns.name() + " (" + status + ", "
                        + nsMatches.size() + " matches)");
            }
            for (Match match : nsMatches) {
                ownerCounts.merge(match.jar(), 1, Integer::sum);
            }
            output.add(String.join("\t", Arrays.asList(
                    ns.name(), ns.initEntry(), ns.initSha(), status,
                    Integer.toString(nsMatches.size()),
                    join(nsMatches, Match::jar),
                    join(nsMatches, Match::jarSha),
                    join(nsMatches, Match::entry),
                    join(nsMatches, Match::sourceSha))));
        }
        Files.write(outputTsv, output, StandardCharsets.UTF_8);

        List<String> report = new ArrayList<>();
        report.add(scope.equals("bundled")
                ? "# Bundled Transactor namespace source ownership"
                : "# Datomic Transactor dependency-source ownership");
        report.add("");
        if (scope.equals("bundled")) {
            report.add("This read-only scan checked all **" + verifiedJars
                    + "** hash-bound shipped dependency JARs ("
                    + scannedEntries + " physical ZIP entries) for exact "
                    + "`.clj`/`.cljc` paths derived from the 85 "
                    + "non-`datomic.*` Transactor initializer class paths.");
        } else {
            report.add("This read-only scan checked all **" + verifiedJars
                    + "** hash-bound shipped dependency JARs ("
                    + scannedEntries + " physical ZIP entries) for exact "
                    + "`.clj`/`.cljc` paths derived from all "
                    + namespaces.size() + " Datomic Transactor initializer "
                    + "class paths.");
        }
        report.add("");
        report.add("## Result");
        report.add("");
        for (Map.Entry<String, Integer> count : statusCounts.entrySet()) {
            report.add("- `" + count.getKey() + "`: " + count.getValue());
        }
        report.add("");
        report.add("## Owner JARs");
        report.add("");
        ownerCounts.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(entry -> report.add("- `" + entry.getKey()
                        + "`: " + entry.getValue()
                        + " namespace source entries"));
        report.add("");
        report.add("## Missing exact source paths");
        report.add("");
        if (missing.isEmpty()) {
            report.add("None.");
        } else {
            for (String name : missing) report.add("- `" + name + "`");
        }
        report.add("");
        report.add("## Ambiguous exact paths");
        report.add("");
        if (ambiguous.isEmpty()) {
            report.add("None.");
        } else {
            for (String name : ambiguous) report.add("- `" + name + "`");
        }
        report.add("");
        report.add("## Interpretation boundary");
        report.add("");
        report.add("A single exact path in the distribution establishes a "
                + "concrete shipped source candidate and ownership/version "
                + "lead. It does not alone prove that the embedded AOT class "
                + "was compiled from byte-for-byte that source; AOT surface "
                + "or recompilation evidence is a separate validation gate. "
                + "Missing paths remain decompiler recovery inputs.");
        Files.write(reportMd, report, StandardCharsets.UTF_8);

        System.out.println(scope + "_namespaces=" + namespaces.size());
        System.out.println("verified_dependency_jars=" + verifiedJars);
        System.out.println("scanned_zip_entries=" + scannedEntries);
        statusCounts.forEach((status, count) ->
                System.out.println("status_" + status + "=" + count));
        System.out.println("output_tsv_sha256=" + sha256(outputTsv));
        System.out.println("report_md_sha256=" + sha256(reportMd));
    }
}
