import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.zip.CRC32;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Validates and stages the exact safe subset of nano-impl-0.1.325.jar, then
 * verifies the deterministic derivative produced by scripts/DeterministicJar.java.
 * No class from either archive is defined or executed.
 */
public final class SanitizeNanoImpl {
    private static final HexFormat HEX = HexFormat.of();
    private static final String INPUT_SHA =
            "fbde8184da356bd3a9995a9f05f01493efe4baa87c4d48377cda3e53331807cd";
    private static final long INPUT_BYTES = 25_293;
    private static final String POLICY_SHA =
            "ff8bffd9a2d653750308ab9455509f02a613d30adf5bd11ecae767fadaf4a5f1";
    private static final String OUTPUT_SHA =
            "08f9699d6e35b9e052ec85ee15e0896b3a685c74e6e226d8cd89d9c61dbf8d5f";
    private static final long OUTPUT_BYTES = 81_218;
    private static final LocalDateTime OUTPUT_TIME =
            LocalDateTime.of(1980, 1, 1, 0, 0);
    private static final long OUTPUT_EPOCH_MILLIS = 315_532_800_000L;
    private static final String OUTPUT_EXTRA_HEX = "555405000100a6ce12";
    private static final Set<String> FORBIDDEN_HASHES = Set.of(
            "f3627f52580b84fe8f536423643fb46999fd2458498989307f2820d778eb73a1",
            "ca64f839d051d909974a624b4345d83cc739a0b90edbe54e7c3605a2fd8fc13b");

    private record Policy(int ordinal, String entry, String kind,
                          String treatment, long bytes, String sha256) {}

    private record Checked(Policy policy, String actualSha, String status) {}

    private static void check(boolean condition, String message) {
        if (!condition) throw new IllegalStateException(message);
    }

    private static String sha256(Path path) throws Exception {
        try (InputStream in = Files.newInputStream(path)) {
            return sha256(in);
        }
    }

    private static String sha256(InputStream input) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] buffer = new byte[64 * 1024];
        for (int count; (count = input.read(buffer)) != -1; ) {
            if (count > 0) digest.update(buffer, 0, count);
        }
        return HEX.formatHex(digest.digest());
    }

    private static String sha256(byte[] bytes) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        return HEX.formatHex(digest.digest(bytes));
    }

    private static void requireSafeEntry(String entry) {
        check(!entry.isEmpty(), "empty archive entry name");
        check(!entry.startsWith("/"), "absolute archive entry: " + entry);
        check(!entry.contains("\\"), "backslash archive entry: " + entry);
        check(!entry.contains("\0"), "NUL archive entry: " + entry);
        for (String part : entry.split("/", -1)) {
            check(!"..".equals(part), "parent-traversing archive entry: " + entry);
        }
    }

    private static List<Policy> readPolicy(Path path) throws Exception {
        check(Files.isRegularFile(path), "missing entry policy: " + path);
        String policySha = sha256(path);
        check(POLICY_SHA.equals(policySha),
                "unexpected entry-policy SHA-256: " + policySha);
        List<Policy> rows = new ArrayList<>();
        Set<String> names = new HashSet<>();
        try (BufferedReader reader = Files.newBufferedReader(
                path, StandardCharsets.UTF_8)) {
            String header = reader.readLine();
            check("ordinal\tentry\tkind\ttreatment\tbytes\tsha256".equals(header),
                    "unexpected entry-policy header: " + header);
            for (String line; (line = reader.readLine()) != null; ) {
                String[] fields = line.split("\t", -1);
                check(fields.length == 6, "malformed entry-policy row: " + line);
                int ordinal = Integer.parseInt(fields[0]);
                long bytes = Long.parseLong(fields[4]);
                Policy row = new Policy(ordinal, fields[1], fields[2],
                        fields[3], bytes, fields[5]);
                check(ordinal == rows.size() + 1,
                        "non-contiguous policy ordinal: " + ordinal);
                requireSafeEntry(row.entry());
                check(names.add(row.entry()), "duplicate policy entry: " + row.entry());
                check(Set.of("directory", "class", "resource").contains(row.kind()),
                        "invalid policy kind: " + row.kind());
                check(Set.of("drop-directory", "retain", "remove-forbidden")
                                .contains(row.treatment()),
                        "invalid policy treatment: " + row.treatment());
                if ("directory".equals(row.kind())) {
                    check(row.entry().endsWith("/"),
                            "directory entry lacks trailing slash: " + row.entry());
                    check("drop-directory".equals(row.treatment()),
                            "directory must be dropped: " + row.entry());
                    check(row.bytes() == 0 && "-".equals(row.sha256()),
                            "directory must have zero bytes and no hash: " + row.entry());
                } else {
                    check(!row.entry().endsWith("/"),
                            "file entry has trailing slash: " + row.entry());
                    check(!"drop-directory".equals(row.treatment()),
                            "file cannot use directory treatment: " + row.entry());
                    check(row.bytes() >= 0, "negative entry size: " + row.entry());
                    check(row.sha256().matches("[0-9a-f]{64}"),
                            "invalid entry hash: " + row.entry());
                }
                rows.add(row);
            }
        }
        check(rows.size() == 21, "expected 21 policy rows, got " + rows.size());
        check(count(rows, p -> "drop-directory".equals(p.treatment())) == 8,
                "expected eight directory rows");
        check(count(rows, p -> "retain".equals(p.treatment())) == 11,
                "expected eleven retained rows");
        check(count(rows, p -> "remove-forbidden".equals(p.treatment())) == 2,
                "expected two forbidden rows");
        Set<String> policyForbidden = new TreeSet<>();
        for (Policy row : rows) {
            if ("remove-forbidden".equals(row.treatment())) {
                policyForbidden.add(row.sha256());
            }
        }
        check(policyForbidden.equals(FORBIDDEN_HASHES),
                "policy forbidden hashes differ from the hard-coded boundary");
        return rows;
    }

    private static long count(List<Policy> rows, Function<Policy, Boolean> pred) {
        return rows.stream().filter(p -> pred.apply(p)).count();
    }

    private static void requireEmptyDirectory(Path path, String label)
            throws IOException {
        if (Files.exists(path)) {
            check(Files.isDirectory(path), label + " is not a directory: " + path);
            check(!Files.isSymbolicLink(path), label + " may not be a symlink: " + path);
            try (var files = Files.list(path)) {
                check(files.findAny().isEmpty(),
                        "refusing non-empty " + label + ": " + path);
            }
        } else {
            Files.createDirectories(path);
        }
    }

    private static void prepare(Path input, Path policyPath, Path staging,
                                Path evidence) throws Exception {
        check(Files.isRegularFile(input), "missing input JAR: " + input);
        check(Files.size(input) == INPUT_BYTES,
                "unexpected input JAR size: " + Files.size(input));
        String inputSha = sha256(input);
        check(INPUT_SHA.equals(inputSha), "unexpected input JAR SHA-256: " + inputSha);
        List<Policy> policy = readPolicy(policyPath);
        requireEmptyDirectory(staging, "staging directory");
        requireEmptyDirectory(evidence, "prepare evidence directory");

        List<Checked> checked = new ArrayList<>();
        long retainedBytes = 0;
        int ordinal = 0;
        try (ZipFile zip = new ZipFile(input.toFile())) {
            check(zip.getComment() == null, "input archive comment is not empty");
            Enumeration<? extends ZipEntry> entries = zip.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                ordinal++;
                check(ordinal <= policy.size(),
                        "unexpected input archive entry: " + entry.getName());
                Policy expected = policy.get(ordinal - 1);
                requireSafeEntry(entry.getName());
                check(expected.ordinal() == ordinal,
                        "policy ordinal mismatch at " + ordinal);
                check(expected.entry().equals(entry.getName()),
                        "input entry mismatch at " + ordinal + ": expected "
                                + expected.entry() + " got " + entry.getName());
                check(("directory".equals(expected.kind())) == entry.isDirectory(),
                        "entry kind mismatch: " + entry.getName());
                check(entry.getSize() == expected.bytes(),
                        "entry size mismatch: " + entry.getName());
                if (entry.isDirectory()) {
                    checked.add(new Checked(expected, "-", "drop"));
                    continue;
                }
                byte[] bytes;
                try (InputStream in = zip.getInputStream(entry)) {
                    bytes = in.readAllBytes();
                }
                check(bytes.length == expected.bytes(),
                        "read size mismatch: " + entry.getName());
                String actualSha = sha256(bytes);
                check(actualSha.equals(expected.sha256()),
                        "entry SHA-256 mismatch: " + entry.getName());
                if ("remove-forbidden".equals(expected.treatment())) {
                    check(FORBIDDEN_HASHES.contains(actualSha),
                            "removal entry lacks a forbidden hash: " + entry.getName());
                    checked.add(new Checked(expected, actualSha, "remove"));
                } else {
                    check(!FORBIDDEN_HASHES.contains(actualSha),
                            "forbidden hash marked for retention: " + entry.getName());
                    Path destination = staging.resolve(entry.getName()).normalize();
                    check(destination.startsWith(staging.toAbsolutePath().normalize()),
                            "staged entry escaped root: " + entry.getName());
                    Files.createDirectories(destination.getParent());
                    Files.write(destination, bytes);
                    retainedBytes += bytes.length;
                    checked.add(new Checked(expected, actualSha, "retain"));
                }
            }
        }
        check(ordinal == policy.size(),
                "input archive ended at " + ordinal + " entries; expected " + policy.size());
        check(retainedBytes == 79_362,
                "unexpected retained byte count: " + retainedBytes);
        verifyStaging(staging, policy);

        List<String> validation = new ArrayList<>();
        validation.add("ordinal\tentry\tkind\ttreatment\tbytes\texpected_sha256"
                + "\tactual_sha256\tstatus");
        for (Checked row : checked) {
            Policy p = row.policy();
            validation.add(tsv(p.ordinal(), p.entry(), p.kind(), p.treatment(),
                    p.bytes(), p.sha256(), row.actualSha(), row.status()));
        }
        writeLines(evidence.resolve("original-entry-validation.tsv"), validation);

        List<String> retained = new ArrayList<>();
        retained.add("entry\tkind\tbytes\tsha256");
        policy.stream().filter(p -> "retain".equals(p.treatment()))
                .sorted(Comparator.comparing(Policy::entry))
                .forEach(p -> retained.add(tsv(p.entry(), p.kind(), p.bytes(), p.sha256())));
        writeLines(evidence.resolve("retained-entries.tsv"), retained);

        List<String> removed = new ArrayList<>();
        removed.add("entry\treason\tbytes\tsha256");
        policy.stream().filter(p -> "remove-forbidden".equals(p.treatment()))
                .forEach(p -> removed.add(tsv(p.entry(), "forbidden-content-hash",
                        p.bytes(), p.sha256())));
        writeLines(evidence.resolve("removed-entries.tsv"), removed);

        writeLines(evidence.resolve("prepare-summary.tsv"), List.of(
                "metric\tvalue",
                "input.jar.sha256\t" + INPUT_SHA,
                "input.jar.bytes\t" + INPUT_BYTES,
                "input.entries.total\t21",
                "input.entries.directories\t8",
                "input.entries.files\t13",
                "retained.entries\t11",
                "retained.bytes\t79362",
                "removed.entries\t2",
                "removed.bytes\t2030",
                "removed.forbidden_hashes\t2"));
    }

    private static void verifyStaging(Path staging, List<Policy> policy)
            throws Exception {
        Map<String, Policy> expected = new LinkedHashMap<>();
        policy.stream().filter(p -> "retain".equals(p.treatment()))
                .sorted(Comparator.comparing(Policy::entry))
                .forEach(p -> expected.put(p.entry(), p));
        List<Path> files;
        try (var paths = Files.walk(staging)) {
            files = paths.filter(Files::isRegularFile)
                    .sorted(Comparator.comparing(p -> logical(staging, p))).toList();
        }
        try (var paths = Files.walk(staging)) {
            check(paths.noneMatch(Files::isSymbolicLink),
                    "staging tree contains a symbolic link");
        }
        check(files.size() == expected.size(),
                "staging file count mismatch: " + files.size());
        for (Path file : files) {
            String name = logical(staging, file);
            Policy row = expected.remove(name);
            check(row != null, "unexpected staged file: " + name);
            check(Files.size(file) == row.bytes(), "staged size mismatch: " + name);
            check(sha256(file).equals(row.sha256()), "staged hash mismatch: " + name);
        }
        check(expected.isEmpty(), "missing staged files: " + expected.keySet());
    }

    private static void verify(Path archive, Path policyPath, Path evidence)
            throws Exception {
        check(Files.isRegularFile(archive), "missing derivative JAR: " + archive);
        List<Policy> retained = readPolicy(policyPath).stream()
                .filter(p -> "retain".equals(p.treatment()))
                .sorted(Comparator.comparing(Policy::entry)).toList();
        requireEmptyDirectory(evidence, "verify evidence directory");
        List<String> validation = new ArrayList<>();
        validation.add("ordinal\tentry\tkind\tmethod\tbytes\tcompressed_bytes"
                + "\ttimestamp_local\ttimestamp_epoch_millis\textra_hex"
                + "\texpected_sha256\tactual_sha256\tforbidden_hash\tstatus");
        List<String> hits = new ArrayList<>();
        hits.add("entry\tbytes\tsha256\trule");
        long payloadBytes = 0;
        int classes = 0;
        int resources = 0;
        int ordinal = 0;
        try (ZipFile zip = new ZipFile(archive.toFile())) {
            check(zip.getComment() == null, "derivative archive comment is not empty");
            Enumeration<? extends ZipEntry> entries = zip.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                ordinal++;
                check(ordinal <= retained.size(),
                        "unexpected derivative entry: " + entry.getName());
                Policy expected = retained.get(ordinal - 1);
                requireSafeEntry(entry.getName());
                check(!entry.isDirectory(),
                        "derivative must be file-only: " + entry.getName());
                check(entry.getName().equals(expected.entry()),
                        "derivative entry mismatch at " + ordinal + ": expected "
                                + expected.entry() + " got " + entry.getName());
                check(entry.getMethod() == ZipEntry.STORED,
                        "derivative entry is not STORED: " + entry.getName());
                check(entry.getSize() == expected.bytes(),
                        "derivative size mismatch: " + entry.getName());
                check(entry.getCompressedSize() == expected.bytes(),
                        "derivative compressed size mismatch: " + entry.getName());
                check(entry.getComment() == null,
                        "derivative entry comment is present: " + entry.getName());
                String extraHex = entry.getExtra() == null
                        ? "" : HEX.formatHex(entry.getExtra());
                check(OUTPUT_EXTRA_HEX.equals(extraHex),
                        "derivative entry extra data mismatch: " + entry.getName());
                check(OUTPUT_TIME.equals(entry.getTimeLocal()),
                        "derivative timestamp mismatch: " + entry.getName());
                check(entry.getTime() == OUTPUT_EPOCH_MILLIS,
                        "derivative epoch timestamp mismatch: " + entry.getName());
                byte[] bytes;
                try (InputStream in = zip.getInputStream(entry)) {
                    bytes = in.readAllBytes();
                }
                String actualSha = sha256(bytes);
                check(actualSha.equals(expected.sha256()),
                        "derivative content hash mismatch: " + entry.getName());
                CRC32 crc = new CRC32();
                crc.update(bytes);
                check(entry.getCrc() == crc.getValue(),
                        "derivative CRC mismatch: " + entry.getName());
                boolean forbidden = FORBIDDEN_HASHES.contains(actualSha);
                if (forbidden) {
                    hits.add(tsv(entry.getName(), bytes.length, actualSha,
                            "vendor-jks-content-hash"));
                }
                payloadBytes += bytes.length;
                if ("class".equals(expected.kind())) classes++;
                else resources++;
                validation.add(tsv(ordinal, entry.getName(), expected.kind(), "stored",
                        bytes.length, entry.getCompressedSize(), entry.getTimeLocal(),
                        entry.getTime(), extraHex, expected.sha256(), actualSha,
                        forbidden, "pass"));
            }
        }
        check(ordinal == retained.size(),
                "derivative ended at " + ordinal + " entries; expected " + retained.size());
        check(payloadBytes == 79_362, "unexpected derivative payload bytes: " + payloadBytes);
        check(classes == 2 && resources == 9,
                "unexpected derivative kind counts: classes=" + classes
                        + " resources=" + resources);
        check(hits.size() == 1, "forbidden content remains in derivative");
        long archiveBytes = Files.size(archive);
        String archiveSha = sha256(archive);
        check(archiveBytes == OUTPUT_BYTES,
                "unexpected derivative JAR size: " + archiveBytes);
        check(archiveSha.equals(OUTPUT_SHA),
                "unexpected derivative JAR SHA-256: " + archiveSha);

        writeLines(evidence.resolve("sanitized-entry-validation.tsv"), validation);
        writeLines(evidence.resolve("forbidden-hits.tsv"), hits);
        writeLines(evidence.resolve("sanitized-summary.tsv"), List.of(
                "metric\tvalue",
                "derivative.jar.sha256\t" + archiveSha,
                "derivative.jar.bytes\t" + archiveBytes,
                "derivative.entries.total\t" + ordinal,
                "derivative.entries.directories\t0",
                "derivative.entries.classes\t" + classes,
                "derivative.entries.resources\t" + resources,
                "derivative.entries.stored\t" + ordinal,
                "derivative.entries.deflated\t0",
                "derivative.timestamp.local\t" + OUTPUT_TIME,
                "derivative.timestamp.epoch_millis\t" + OUTPUT_EPOCH_MILLIS,
                "derivative.entry.extra_hex\t" + OUTPUT_EXTRA_HEX,
                "derivative.payload.bytes\t" + payloadBytes,
                "forbidden.hash.hits\t0"));
    }

    private static String logical(Path root, Path file) {
        return root.toAbsolutePath().normalize()
                .relativize(file.toAbsolutePath().normalize()).toString()
                .replace(file.getFileSystem().getSeparator(), "/");
    }

    private static String tsv(Object... fields) {
        List<String> values = new ArrayList<>();
        for (Object field : fields) {
            String value = String.valueOf(field);
            check(!value.contains("\t") && !value.contains("\n")
                            && !value.contains("\r"),
                    "unsafe TSV field");
            values.add(value);
        }
        return String.join("\t", values);
    }

    private static void writeLines(Path file, List<String> lines)
            throws IOException {
        Files.createDirectories(file.getParent());
        Files.writeString(file, String.join("\n", lines) + "\n",
                StandardCharsets.UTF_8);
    }

    public static void main(String[] args) throws Exception {
        if (args.length != 5 && args.length != 4) {
            throw new IllegalArgumentException(
                    "usage: SanitizeNanoImpl prepare INPUT_JAR POLICY_TSV "
                            + "STAGING_DIRECTORY EVIDENCE_DIRECTORY\n"
                            + "   or: SanitizeNanoImpl verify DERIVATIVE_JAR "
                            + "POLICY_TSV EVIDENCE_DIRECTORY");
        }
        switch (args[0]) {
            case "prepare" -> {
                check(args.length == 5, "prepare requires four arguments");
                prepare(Path.of(args[1]).toAbsolutePath().normalize(),
                        Path.of(args[2]).toAbsolutePath().normalize(),
                        Path.of(args[3]).toAbsolutePath().normalize(),
                        Path.of(args[4]).toAbsolutePath().normalize());
                System.out.println("validated exact nano-impl input and staged 11 retained entries");
            }
            case "verify" -> {
                check(args.length == 4, "verify requires three arguments");
                verify(Path.of(args[1]).toAbsolutePath().normalize(),
                        Path.of(args[2]).toAbsolutePath().normalize(),
                        Path.of(args[3]).toAbsolutePath().normalize());
                System.out.println("verified deterministic nano-impl derivative with zero forbidden hashes");
            }
            default -> throw new IllegalArgumentException("unknown mode: " + args[0]);
        }
    }
}
