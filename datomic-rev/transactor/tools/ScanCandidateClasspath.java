import java.io.BufferedReader;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

/**
 * Exhaustively scans an ordered candidate classpath without defining a class.
 *
 * <p>The scanner binds each top-level element to an expected SHA-256, walks
 * directory elements without following links, streams every JAR entry, and
 * compares whole files and entry payloads with a content-hash deny policy.
 * It deliberately does not deny individual original class hashes: exact
 * recompilation from recovered source can legitimately reproduce them.</p>
 */
public final class ScanCandidateClasspath {
    private static final Pattern SHA256 = Pattern.compile("[0-9a-f]{64}");
    private static final int MAX_NESTED_ARCHIVE_DEPTH = 4;
    private static final int MAX_NESTED_ARCHIVE_BYTES = 64 * 1024 * 1024;

    private static final class RootSpec {
        final String name;
        final Path path;

        RootSpec(String name, Path path) {
            this.name = name;
            this.path = path;
        }
    }

    private static final class Element {
        final int position;
        final String role;
        final String root;
        final String relativePath;
        final String kind;
        final String expectedSha;

        Element(int position, String role, String root, String relativePath,
                String kind, String expectedSha) {
            this.position = position;
            this.role = role;
            this.root = root;
            this.relativePath = relativePath;
            this.kind = kind;
            this.expectedSha = expectedSha;
        }

        String logicalPath() {
            return root + ":" + relativePath;
        }
    }

    private static final class Forbidden {
        final String rule;
        final String sha;
        final String reason;

        Forbidden(String rule, String sha, String reason) {
            this.rule = rule;
            this.sha = sha;
            this.reason = reason;
        }
    }

    private static final class ElementResult {
        final Element element;
        String actualSha = "";
        long bytes;
        long files;
        long archiveEntries;
        long archiveBytes;
        long nestedArchiveEntries;
        long nestedArchiveBytes;
        int hits;
        int violations;

        ElementResult(Element element) {
            this.element = element;
        }
    }

    private final Map<String, RootSpec> roots;
    private final Map<String, List<Forbidden>> forbiddenBySha;
    private final List<String> hitRows = new ArrayList<>();
    private final List<String> violationRows = new ArrayList<>();

    private ScanCandidateClasspath(Map<String, RootSpec> roots,
                                   List<Forbidden> forbidden) {
        this.roots = roots;
        this.forbiddenBySha = new HashMap<>();
        for (Forbidden rule : forbidden) {
            forbiddenBySha.computeIfAbsent(rule.sha,
                    ignored -> new ArrayList<>()).add(rule);
        }
    }

    private static String hex(byte[] bytes) {
        StringBuilder result = new StringBuilder(bytes.length * 2);
        for (byte value : bytes) {
            result.append(String.format("%02x", value & 0xff));
        }
        return result.toString();
    }

    private static MessageDigest sha256Digest() {
        try {
            return MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException impossible) {
            throw new AssertionError(impossible);
        }
    }

    private static String sha256(InputStream input) throws IOException {
        MessageDigest digest = sha256Digest();
        byte[] buffer = new byte[1 << 16];
        for (int count; (count = input.read(buffer)) >= 0; ) {
            if (count > 0) digest.update(buffer, 0, count);
        }
        return hex(digest.digest());
    }

    private static String sha256(Path path) throws IOException {
        try (InputStream input = Files.newInputStream(path)) {
            return sha256(input);
        }
    }

    private static String escape(String value) {
        return value.replace("\\", "\\\\")
                .replace("\t", "\\t")
                .replace("\r", "\\r")
                .replace("\n", "\\n");
    }

    private static void requireSafeField(String label, String value)
            throws IOException {
        if (value.isEmpty() || value.indexOf('\t') >= 0
                || value.indexOf('\r') >= 0 || value.indexOf('\n') >= 0
                || value.indexOf('\0') >= 0) {
            throw new IOException("unsafe " + label + ": " + escape(value));
        }
    }

    private static boolean safeRelativePath(String value) {
        if (value.isEmpty() || value.indexOf('\0') >= 0
                || value.indexOf('\\') >= 0 || value.startsWith("/")) {
            return false;
        }
        Path path = Path.of(value);
        if (path.isAbsolute() || !path.normalize().toString().replace('\\', '/')
                .equals(value)) {
            return false;
        }
        for (String part : value.split("/", -1)) {
            if (part.isEmpty() || part.equals(".") || part.equals("..")) {
                return false;
            }
        }
        return true;
    }

    private static boolean safeZipName(String value, boolean directory) {
        if (value.isEmpty() || value.indexOf('\0') >= 0
                || value.indexOf('\\') >= 0 || value.startsWith("/")) {
            return false;
        }
        String normalized = directory && value.endsWith("/")
                ? value.substring(0, value.length() - 1) : value;
        if (normalized.isEmpty()) return false;
        for (String part : normalized.split("/", -1)) {
            if (part.isEmpty() || part.equals(".") || part.equals("..")) {
                return false;
            }
        }
        return true;
    }

    private static List<Element> readElements(Path path) throws IOException {
        List<Element> result = new ArrayList<>();
        Set<String> logicalPaths = new HashSet<>();
        try (BufferedReader reader = Files.newBufferedReader(
                path, StandardCharsets.UTF_8)) {
            String header = reader.readLine();
            if (!"position\trole\troot\trelative_path\tkind\texpected_sha256"
                    .equals(header)) {
                throw new IOException("unexpected classpath header: " + header);
            }
            int expectedPosition = 1;
            for (String line; (line = reader.readLine()) != null; ) {
                String[] fields = line.split("\t", -1);
                if (fields.length != 6) {
                    throw new IOException("malformed classpath row: " + line);
                }
                int position;
                try {
                    position = Integer.parseInt(fields[0]);
                } catch (NumberFormatException bad) {
                    throw new IOException("bad classpath position: " + fields[0]);
                }
                if (position != expectedPosition++) {
                    throw new IOException("classpath positions are not contiguous at "
                            + position);
                }
                requireSafeField("role", fields[1]);
                requireSafeField("root", fields[2]);
                if (!safeRelativePath(fields[3])) {
                    throw new IOException("unsafe relative classpath path: "
                            + escape(fields[3]));
                }
                if (!Set.of("file", "jar", "directory").contains(fields[4])) {
                    throw new IOException("unsupported classpath kind: " + fields[4]);
                }
                if (!SHA256.matcher(fields[5]).matches()) {
                    throw new IOException("invalid expected SHA-256 at position "
                            + position);
                }
                Element element = new Element(position, fields[1], fields[2],
                        fields[3], fields[4], fields[5]);
                if (!logicalPaths.add(element.logicalPath())) {
                    throw new IOException("duplicate logical classpath path: "
                            + element.logicalPath());
                }
                result.add(element);
            }
        }
        if (result.isEmpty()) throw new IOException("candidate classpath is empty");
        return result;
    }

    private static List<Forbidden> readForbidden(Path path) throws IOException {
        List<Forbidden> result = new ArrayList<>();
        Set<String> rules = new HashSet<>();
        try (BufferedReader reader = Files.newBufferedReader(
                path, StandardCharsets.UTF_8)) {
            String header = reader.readLine();
            if (!"rule_id\tsha256\treason".equals(header)) {
                throw new IOException("unexpected forbidden-policy header: " + header);
            }
            for (String line; (line = reader.readLine()) != null; ) {
                String[] fields = line.split("\t", -1);
                if (fields.length != 3) {
                    throw new IOException("malformed forbidden-policy row: " + line);
                }
                requireSafeField("rule id", fields[0]);
                requireSafeField("reason", fields[2]);
                if (!SHA256.matcher(fields[1]).matches()) {
                    throw new IOException("invalid forbidden SHA-256: " + fields[1]);
                }
                if (!rules.add(fields[0])) {
                    throw new IOException("duplicate forbidden rule: " + fields[0]);
                }
                result.add(new Forbidden(fields[0], fields[1], fields[2]));
            }
        }
        if (result.isEmpty()) throw new IOException("forbidden policy is empty");
        return result;
    }

    private static Map<String, RootSpec> readRoots(String[] args, int start)
            throws IOException {
        Map<String, RootSpec> result = new LinkedHashMap<>();
        for (int index = start; index < args.length; index++) {
            int separator = args[index].indexOf('=');
            if (separator <= 0 || separator == args[index].length() - 1) {
                throw new IOException("root must be NAME=PATH: " + args[index]);
            }
            String name = args[index].substring(0, separator);
            requireSafeField("root name", name);
            Path path = Path.of(args[index].substring(separator + 1))
                    .toAbsolutePath().normalize();
            if (!Files.isDirectory(path, LinkOption.NOFOLLOW_LINKS)
                    || Files.isSymbolicLink(path)) {
                throw new IOException("root is not a real directory: " + path);
            }
            Path real = path.toRealPath(LinkOption.NOFOLLOW_LINKS);
            if (!real.equals(path)) {
                throw new IOException("root contains unresolved aliases: " + path);
            }
            if (result.put(name, new RootSpec(name, path)) != null) {
                throw new IOException("duplicate root name: " + name);
            }
        }
        if (result.isEmpty()) throw new IOException("no logical roots supplied");
        return result;
    }

    private void violation(ElementResult result, String scope, String path,
                           String reason) {
        result.violations++;
        violationRows.add(result.element.position + "\t"
                + escape(result.element.logicalPath()) + "\t" + scope + "\t"
                + escape(path) + "\t" + escape(reason));
    }

    private void matchForbidden(ElementResult result, String scope,
                                String nestedPath, long bytes, String sha) {
        List<Forbidden> rules = forbiddenBySha.getOrDefault(
                sha, Collections.emptyList());
        for (Forbidden rule : rules) {
            result.hits++;
            hitRows.add(rule.rule + "\t" + result.element.position + "\t"
                    + escape(result.element.logicalPath()) + "\t" + scope + "\t"
                    + escape(nestedPath) + "\t" + bytes + "\t" + sha + "\t"
                    + escape(rule.reason));
        }
    }

    private static boolean nestedArchive(String name) {
        String lower = name.toLowerCase(Locale.ROOT);
        return lower.endsWith(".jar") || lower.endsWith(".zip");
    }

    private static boolean zipMagic(byte[] prefix, int length) {
        return length >= 4
                && prefix[0] == 0x50 && prefix[1] == 0x4b
                && ((prefix[2] == 0x03 && prefix[3] == 0x04)
                    || (prefix[2] == 0x05 && prefix[3] == 0x06));
    }

    private static int unsignedShortLittleEndian(byte[] bytes, int offset) {
        return (bytes[offset] & 0xff) | ((bytes[offset + 1] & 0xff) << 8);
    }

    private static long unsignedIntLittleEndian(byte[] bytes, int offset) {
        return (bytes[offset] & 0xffL)
                | ((bytes[offset + 1] & 0xffL) << 8)
                | ((bytes[offset + 2] & 0xffL) << 16)
                | ((bytes[offset + 3] & 0xffL) << 24);
    }

    /**
     * Validates the ordinary single-disk ZIP envelope consumed by the nested
     * stream scan. In particular, the EOCD must account for the physical end
     * of the byte array; otherwise a leading empty EOCD could hide a second,
     * unparsed archive in trailing bytes.
     */
    private static String zipEnvelopeViolation(byte[] archive,
                                                int observedEntries) {
        if (archive.length < 22) {
            return "ZIP is shorter than an end-of-central-directory record";
        }
        int minimum = Math.max(0, archive.length - 22 - 65_535);
        int eocd = -1;
        int commentLength = -1;
        for (int offset = archive.length - 22; offset >= minimum; offset--) {
            if (archive[offset] == 0x50 && archive[offset + 1] == 0x4b
                    && archive[offset + 2] == 0x05
                    && archive[offset + 3] == 0x06) {
                int candidateComment = unsignedShortLittleEndian(
                        archive, offset + 20);
                if ((long) offset + 22 + candidateComment == archive.length) {
                    eocd = offset;
                    commentLength = candidateComment;
                    break;
                }
            }
        }
        if (eocd < 0) {
            return "ZIP end record is missing or leaves unparsed trailing bytes";
        }
        if (commentLength != 0) {
            return "ZIP archive comment is not allowed in a nested payload";
        }
        int disk = unsignedShortLittleEndian(archive, eocd + 4);
        int centralDisk = unsignedShortLittleEndian(archive, eocd + 6);
        int diskEntries = unsignedShortLittleEndian(archive, eocd + 8);
        int totalEntries = unsignedShortLittleEndian(archive, eocd + 10);
        long centralBytes = unsignedIntLittleEndian(archive, eocd + 12);
        long centralOffset = unsignedIntLittleEndian(archive, eocd + 16);
        if (disk != 0 || centralDisk != 0 || diskEntries != totalEntries) {
            return "multi-disk ZIP envelopes are not supported";
        }
        if (totalEntries == 0xffff || centralBytes == 0xffff_ffffL
                || centralOffset == 0xffff_ffffL) {
            return "ZIP64 nested envelopes are not supported";
        }
        if (totalEntries != observedEntries) {
            return "ZIP central-directory entry count differs from streamed entries";
        }
        if (centralOffset + centralBytes != eocd) {
            return "ZIP central-directory bounds are inconsistent";
        }
        if (totalEntries == 0) {
            if (eocd != 0 || centralOffset != 0 || centralBytes != 0) {
                return "empty ZIP contains unparsed bytes before its end record";
            }
        } else {
            if (centralOffset + 4 > archive.length
                    || archive[(int) centralOffset] != 0x50
                    || archive[(int) centralOffset + 1] != 0x4b
                    || archive[(int) centralOffset + 2] != 0x01
                    || archive[(int) centralOffset + 3] != 0x02) {
                return "ZIP central directory does not begin at its declared offset";
            }
        }
        return null;
    }

    private void scanNestedArchive(ElementResult result, byte[] archive,
                                   String parent, int depth) {
        if (depth > MAX_NESTED_ARCHIVE_DEPTH) {
            violation(result, "nested-archive", parent,
                    "nested archive depth exceeds " + MAX_NESTED_ARCHIVE_DEPTH);
            return;
        }
        Set<String> names = new HashSet<>();
        int observedEntries = 0;
        try (ZipInputStream zip = new ZipInputStream(
                new ByteArrayInputStream(archive))) {
            for (ZipEntry entry; (entry = zip.getNextEntry()) != null; ) {
                observedEntries++;
                String name = entry.getName();
                String logical = parent + "!/" + name;
                if (!names.add(name)) {
                    violation(result, "nested-archive", logical,
                            "duplicate ZIP entry name");
                }
                if (!safeZipName(name, entry.isDirectory())) {
                    violation(result, "nested-archive", logical,
                            "unsafe ZIP entry name");
                }
                if (entry.isDirectory()) continue;
                result.nestedArchiveEntries++;
                boolean namedArchive = nestedArchive(name);
                MessageDigest digest = sha256Digest();
                byte[] prefix = zip.readNBytes(4);
                int prefixLength = prefix.length;
                digest.update(prefix);
                long bytes = prefixLength;
                ByteArrayOutputStream captured = zipMagic(prefix, prefixLength)
                        ? new ByteArrayOutputStream() : null;
                if (captured != null) captured.write(prefix);
                byte[] buffer = new byte[1 << 16];
                for (int count; (count = zip.read(buffer)) >= 0; ) {
                    if (count == 0) continue;
                    digest.update(buffer, 0, count);
                    bytes += count;
                    if (captured != null && bytes <= MAX_NESTED_ARCHIVE_BYTES) {
                        captured.write(buffer, 0, count);
                    }
                }
                result.nestedArchiveBytes += bytes;
                String sha = hex(digest.digest());
                matchForbidden(result, "nested-archive-entry", logical, bytes, sha);
                boolean magicArchive = zipMagic(prefix, prefixLength);
                if (namedArchive && !magicArchive) {
                    violation(result, "nested-archive", logical,
                            "archive-named entry lacks a ZIP signature");
                }
                if (magicArchive) {
                    if (bytes > MAX_NESTED_ARCHIVE_BYTES) {
                        violation(result, "nested-archive", logical,
                                "nested archive exceeds byte limit "
                                        + MAX_NESTED_ARCHIVE_BYTES);
                    } else {
                        scanNestedArchive(result, captured.toByteArray(),
                                logical, depth + 1);
                    }
                }
            }
            String envelopeViolation = zipEnvelopeViolation(
                    archive, observedEntries);
            if (envelopeViolation != null) {
                violation(result, "nested-archive", parent, envelopeViolation);
            }
        } catch (IOException failure) {
            violation(result, "nested-archive", parent,
                    "nested ZIP scan failure: " + failure.getMessage());
        }
    }

    private void scanArchive(ElementResult result, Path path, String scopePrefix)
            throws IOException {
        Set<String> names = new HashSet<>();
        try (ZipFile zip = new ZipFile(path.toFile())) {
            Enumeration<? extends ZipEntry> entries = zip.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                String name = entry.getName();
                if (!names.add(name)) {
                    violation(result, scopePrefix, name, "duplicate ZIP entry name");
                }
                if (!safeZipName(name, entry.isDirectory())) {
                    violation(result, scopePrefix, name, "unsafe ZIP entry name");
                }
                if (entry.isDirectory()) continue;
                result.archiveEntries++;
                long declaredSize = entry.getSize();
                MessageDigest digest = sha256Digest();
                boolean namedArchive = nestedArchive(name);
                try (BufferedInputStream input = new BufferedInputStream(
                        zip.getInputStream(entry))) {
                    byte[] prefix = input.readNBytes(4);
                    int prefixLength = prefix.length;
                    digest.update(prefix);
                    long bytes = prefixLength;
                    ByteArrayOutputStream captured = zipMagic(prefix, prefixLength)
                            ? new ByteArrayOutputStream() : null;
                    if (captured != null) captured.write(prefix);
                    byte[] buffer = new byte[1 << 16];
                    for (int count; (count = input.read(buffer)) >= 0; ) {
                        if (count > 0) {
                            digest.update(buffer, 0, count);
                            bytes += count;
                            if (captured != null
                                    && bytes <= MAX_NESTED_ARCHIVE_BYTES) {
                                captured.write(buffer, 0, count);
                            }
                        }
                    }
                    result.archiveBytes += bytes;
                    if (declaredSize >= 0 && declaredSize != bytes) {
                        violation(result, scopePrefix, name,
                                "declared ZIP size differs from streamed bytes");
                    }
                    String sha = hex(digest.digest());
                    matchForbidden(result, "archive-entry", name, bytes, sha);
                    boolean magicArchive = zipMagic(prefix, prefixLength);
                    if (namedArchive && !magicArchive) {
                        violation(result, scopePrefix, name,
                                "archive-named entry lacks a ZIP signature");
                    }
                    if (magicArchive) {
                        if (bytes > MAX_NESTED_ARCHIVE_BYTES) {
                            violation(result, scopePrefix, name,
                                    "nested archive exceeds byte limit "
                                            + MAX_NESTED_ARCHIVE_BYTES);
                        } else {
                            scanNestedArchive(result, captured.toByteArray(), name, 1);
                        }
                    }
                }
            }
        }
    }

    private static void requireNoSymlinkComponents(Path root, Path path)
            throws IOException {
        Path current = root;
        if (Files.isSymbolicLink(current)) {
            throw new IOException("logical root is a symbolic link: " + root);
        }
        Path relative = root.relativize(path);
        for (Path part : relative) {
            current = current.resolve(part);
            if (Files.isSymbolicLink(current)) {
                throw new IOException("symbolic link in classpath path: " + current);
            }
        }
    }

    private String scanDirectory(ElementResult result, Path directory)
            throws IOException {
        List<Path> files;
        try (Stream<Path> paths = Files.walk(directory)) {
            files = paths.filter(path -> !path.equals(directory))
                    .sorted(Comparator.comparing(path -> directory.relativize(path)
                            .toString().replace('\\', '/')))
                    .collect(Collectors.toList());
        }
        MessageDigest manifestDigest = sha256Digest();
        for (Path path : files) {
            String relative = directory.relativize(path).toString()
                    .replace('\\', '/');
            if (Files.isSymbolicLink(path)) {
                violation(result, "directory", relative,
                        "symbolic link in directory classpath element");
                continue;
            }
            if (Files.isDirectory(path, LinkOption.NOFOLLOW_LINKS)) continue;
            if (!Files.isRegularFile(path, LinkOption.NOFOLLOW_LINKS)) {
                violation(result, "directory", relative,
                        "non-regular directory entry");
                continue;
            }
            long bytes = Files.size(path);
            String sha = sha256(path);
            result.files++;
            result.bytes += bytes;
            matchForbidden(result, "directory-file", relative, bytes, sha);
            String manifestLine = sha + "\t" + bytes + "\t" + relative + "\n";
            manifestDigest.update(manifestLine.getBytes(StandardCharsets.UTF_8));
            byte[] prefix = new byte[4];
            int prefixLength;
            try (InputStream input = Files.newInputStream(path)) {
                prefixLength = input.read(prefix);
                if (prefixLength < 0) prefixLength = 0;
            }
            boolean namedArchive = nestedArchive(relative);
            boolean magicArchive = zipMagic(prefix, prefixLength);
            if (namedArchive && !magicArchive) {
                violation(result, "directory", relative,
                        "archive-named file lacks a ZIP signature");
            }
            if (magicArchive) {
                if (bytes > MAX_NESTED_ARCHIVE_BYTES) {
                    violation(result, "directory", relative,
                            "nested archive exceeds byte limit "
                                    + MAX_NESTED_ARCHIVE_BYTES);
                } else {
                    scanNestedArchive(result, Files.readAllBytes(path), relative, 1);
                }
            }
        }
        return hex(manifestDigest.digest());
    }

    private ElementResult scan(Element element) {
        ElementResult result = new ElementResult(element);
        RootSpec root = roots.get(element.root);
        if (root == null) {
            violation(result, "element", element.relativePath,
                    "unknown logical root: " + element.root);
            return result;
        }
        Path path = root.path.resolve(element.relativePath).normalize();
        if (!path.startsWith(root.path)) {
            violation(result, "element", element.relativePath,
                    "resolved path leaves logical root");
            return result;
        }
        try {
            requireNoSymlinkComponents(root.path, path);
            if (element.kind.equals("directory")) {
                if (!Files.isDirectory(path, LinkOption.NOFOLLOW_LINKS)) {
                    violation(result, "element", element.relativePath,
                            "expected directory is missing or has wrong kind");
                    return result;
                }
                result.actualSha = scanDirectory(result, path);
            } else {
                if (!Files.isRegularFile(path, LinkOption.NOFOLLOW_LINKS)) {
                    violation(result, "element", element.relativePath,
                            "expected file is missing or has wrong kind");
                    return result;
                }
                result.bytes = Files.size(path);
                result.files = 1;
                result.actualSha = sha256(path);
                matchForbidden(result, "top-level-file", "", result.bytes,
                        result.actualSha);
                if (element.kind.equals("jar")) {
                    scanArchive(result, path, "archive");
                }
                String postScanSha = sha256(path);
                if (!postScanSha.equals(result.actualSha)) {
                    violation(result, "element", element.relativePath,
                            "top-level file changed during scan: started "
                                    + result.actualSha + " ended " + postScanSha);
                }
            }
            if (!element.expectedSha.equals(result.actualSha)) {
                violation(result, "element", element.relativePath,
                        "top-level SHA-256 mismatch: expected "
                                + element.expectedSha + " got " + result.actualSha);
            }
        } catch (IOException failure) {
            violation(result, "element", element.relativePath,
                    "scan failure: " + failure.getMessage());
        }
        return result;
    }

    private static void writeManifest(Path output, List<String> names)
            throws IOException {
        List<String> rows = new ArrayList<>();
        for (String name : names) {
            rows.add(sha256(output.resolve(name)) + "  " + name);
        }
        Files.write(output.resolve("manifest.sha256"), rows,
                StandardCharsets.UTF_8);
    }

    public static void main(String[] args) throws Exception {
        if (args.length < 4) {
            throw new IllegalArgumentException(
                    "usage: ScanCandidateClasspath CLASSPATH_TSV "
                            + "FORBIDDEN_TSV OUTPUT_DIR ROOT=PATH [ROOT=PATH ...]");
        }
        Path classpathTsv = Path.of(args[0]);
        Path forbiddenTsv = Path.of(args[1]);
        Path output = Path.of(args[2]).toAbsolutePath().normalize();
        if (Files.exists(output)) {
            try (Stream<Path> children = Files.list(output)) {
                if (children.findAny().isPresent()) {
                    throw new IOException("refusing non-empty output directory: "
                            + output);
                }
            }
        }
        Files.createDirectories(output);

        List<Element> elements = readElements(classpathTsv);
        List<Forbidden> forbidden = readForbidden(forbiddenTsv);
        Map<String, RootSpec> roots = readRoots(args, 3);
        for (Element element : elements) {
            if (!roots.containsKey(element.root)) {
                throw new IOException("classpath uses undeclared root: "
                        + element.root);
            }
        }
        ScanCandidateClasspath scanner = new ScanCandidateClasspath(
                roots, forbidden);

        List<ElementResult> results = new ArrayList<>();
        for (Element element : elements) results.add(scanner.scan(element));

        List<String> elementRows = new ArrayList<>();
        elementRows.add("position\trole\tkind\tlogical_path\texpected_sha256"
                + "\tactual_sha256\tbytes\tfiles\tarchive_entries"
                + "\tarchive_bytes\tnested_archive_entries"
                + "\tnested_archive_bytes\tforbidden_hits\tviolations");
        long totalBytes = 0;
        long totalFiles = 0;
        long totalArchiveEntries = 0;
        long totalArchiveBytes = 0;
        long totalNestedArchiveEntries = 0;
        long totalNestedArchiveBytes = 0;
        int totalHits = 0;
        int totalViolations = 0;
        for (ElementResult result : results) {
            totalBytes += result.bytes;
            totalFiles += result.files;
            totalArchiveEntries += result.archiveEntries;
            totalArchiveBytes += result.archiveBytes;
            totalNestedArchiveEntries += result.nestedArchiveEntries;
            totalNestedArchiveBytes += result.nestedArchiveBytes;
            totalHits += result.hits;
            totalViolations += result.violations;
            Element element = result.element;
            elementRows.add(element.position + "\t" + escape(element.role)
                    + "\t" + element.kind + "\t"
                    + escape(element.logicalPath()) + "\t"
                    + element.expectedSha + "\t" + result.actualSha + "\t"
                    + result.bytes + "\t" + result.files + "\t"
                    + result.archiveEntries + "\t" + result.archiveBytes + "\t"
                    + result.nestedArchiveEntries + "\t"
                    + result.nestedArchiveBytes + "\t" + result.hits + "\t"
                    + result.violations);
        }
        Files.write(output.resolve("classpath-elements.tsv"), elementRows,
                StandardCharsets.UTF_8);

        List<String> hits = new ArrayList<>();
        hits.add("rule_id\tposition\telement\tscope\tnested_path\tbytes"
                + "\tsha256\treason");
        hits.addAll(scanner.hitRows);
        Files.write(output.resolve("forbidden-hits.tsv"), hits,
                StandardCharsets.UTF_8);

        List<String> violations = new ArrayList<>();
        violations.add("position\telement\tscope\tpath\treason");
        violations.addAll(scanner.violationRows);
        Files.write(output.resolve("violations.tsv"), violations,
                StandardCharsets.UTF_8);

        List<String> summary = List.of(
                "metric\tvalue",
                "classpath.elements\t" + elements.size(),
                "logical.roots\t" + roots.size(),
                "forbidden.rules\t" + forbidden.size(),
                "top-level.files\t" + totalFiles,
                "top-level.bytes\t" + totalBytes,
                "archive.entries\t" + totalArchiveEntries,
                "archive.entry.bytes\t" + totalArchiveBytes,
                "nested.archive.entries\t" + totalNestedArchiveEntries,
                "nested.archive.entry.bytes\t" + totalNestedArchiveBytes,
                "forbidden.hits\t" + totalHits,
                "violations\t" + totalViolations,
                "status\t" + (totalHits == 0 && totalViolations == 0
                        ? "PASS" : "FAIL"));
        Files.write(output.resolve("summary.tsv"), summary,
                StandardCharsets.UTF_8);
        writeManifest(output, List.of("classpath-elements.tsv",
                "forbidden-hits.tsv", "violations.tsv", "summary.tsv"));

        System.out.println("classpath_elements=" + elements.size());
        System.out.println("archive_entries=" + totalArchiveEntries);
        System.out.println("forbidden_hits=" + totalHits);
        System.out.println("violations=" + totalViolations);
        System.out.println("evidence=" + output.resolve("manifest.sha256"));
        if (totalHits != 0 || totalViolations != 0) System.exit(2);
    }
}
