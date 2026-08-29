import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.management.ManagementFactory;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.security.CodeSource;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.ProtectionDomain;
import java.security.cert.Certificate;
import java.util.ArrayList;
import java.util.Arrays;
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
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import org.objectweb.asm.Attribute;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.RecordComponentVisitor;
import org.objectweb.asm.Type;

/**
 * Strict JVM verification, method-maxima, and expanded-frame gate for the
 * exact-source AOT cohort.
 *
 * <p>Normal mode must itself run under {@code -Xverify:all}.  Candidate A,
 * candidate B, and the licensed original are loaded by three isolated loaders.
 * Owned bytes are child-first, while every non-owned dependency comes only
 * from the explicit ordered, hash-bound lane ledger over the platform loader.
 * Candidate loaders never receive oracle URLs; the original loader alone gets
 * the separately bound oracle ledger and may use the exact original JAR.
 * Candidate effective-class collisions must match the separately sealed
 * collision policy and resolve under the real ordered URLs to the declared
 * first code source; no runtime element is deduplicated.</p>
 *
 * <p>The current relation is intentionally strict: mapped classes must expose
 * an unambiguous exact method name plus class-mapped descriptor, access, code
 * presence, declared max stack/locals, and every expanded-frame boundary and
 * value.  A narrower quotient requires later evidence; this gate does not
 * invent one.</p>
 */
public final class VerifyExactAotRuntime {
    private static final int ASM = Opcodes.ASM9;
    private static final int JAVA_FEATURE = 11;
    private static final long MAX_CLASS_BYTES = 32L * 1024L * 1024L;
    private static final Pattern SHA256 = Pattern.compile("[0-9a-f]{64}");
    private static final String CANDIDATE_COLLISION_HEADER =
            "lane\tinternal_name\tfirst_position\tfirst_role\tfirst_entry"
            + "\tfirst_release\tfirst_sha256\tlater_position\tlater_role"
            + "\tlater_entry\tlater_release\tlater_sha256\tbyte_relation";
    private static final List<String> RELATIONS = List.of(
            "candidate-a--candidate-b",
            "candidate-a--original",
            "candidate-b--original");

    private static final class Failure extends RuntimeException {
        private static final long serialVersionUID = 1L;
        Failure(String message) { super(message); }
        Failure(String message, Throwable cause) { super(message, cause); }
    }

    private static final class Config {
        final Path candidateA;
        final Path candidateB;
        final Path originalJar;
        final Path cohort;
        final Path ownership;
        final Path classMappings;
        final Path runtimePolicy;
        final Path candidateRuntimeLedger;
        final Path oracleRuntimeLedger;
        final Path candidateCollisionPolicy;
        final Path output;
        final boolean requireProductionShape;

        Config(Path candidateA, Path candidateB, Path originalJar,
               Path cohort, Path ownership, Path classMappings,
               Path runtimePolicy, Path candidateRuntimeLedger,
               Path oracleRuntimeLedger, Path candidateCollisionPolicy,
               Path output) {
            this(candidateA, candidateB, originalJar, cohort, ownership,
                    classMappings, runtimePolicy, candidateRuntimeLedger,
                    oracleRuntimeLedger, candidateCollisionPolicy, output,
                    true);
        }

        Config(Path candidateA, Path candidateB, Path originalJar,
               Path cohort, Path ownership, Path classMappings,
               Path runtimePolicy, Path candidateRuntimeLedger,
               Path oracleRuntimeLedger, Path candidateCollisionPolicy,
               Path output,
               boolean requireProductionShape) {
            this.candidateA = absolute(candidateA);
            this.candidateB = absolute(candidateB);
            this.originalJar = absolute(originalJar);
            this.cohort = absolute(cohort);
            this.ownership = absolute(ownership);
            this.classMappings = absolute(classMappings);
            this.runtimePolicy = absolute(runtimePolicy);
            this.candidateRuntimeLedger = absolute(candidateRuntimeLedger);
            this.oracleRuntimeLedger = absolute(oracleRuntimeLedger);
            this.candidateCollisionPolicy = absolute(
                    candidateCollisionPolicy);
            this.output = absolute(output);
            this.requireProductionShape = requireProductionShape;
        }
    }

    private static final class CohortRow {
        final String namespace;
        final int expectedClasses;
        CohortRow(String namespace, int expectedClasses) {
            this.namespace = namespace;
            this.expectedClasses = expectedClasses;
        }
    }

    private static final class ClassBytes {
        final String universe;
        final String namespace;
        final String internalName;
        final String logicalPath;
        final String origin;
        final byte[] bytes;
        final String sha;
        ClassModel model;

        ClassBytes(String universe, String namespace, String internalName,
                   String logicalPath, String origin, byte[] bytes) {
            this.universe = universe;
            this.namespace = namespace;
            this.internalName = internalName;
            this.logicalPath = logicalPath;
            this.origin = origin;
            this.bytes = bytes;
            this.sha = sha256(bytes);
        }
    }

    private static final class RuntimeElement {
        final String lane;
        final int position;
        final String role;
        final String kind;
        final Path path;
        final String expectedSha;
        String preSha = "";
        String postSha = "";
        long classes;
        long ownedShadows;
        long collisions;
        boolean scanned;
        RuntimeElement(String lane, int position, String role, String kind,
                       Path path, String expectedSha) {
            this.lane = lane;
            this.position = position;
            this.role = role;
            this.kind = kind;
            this.path = path;
            this.expectedSha = expectedSha;
        }
    }

    private static final class ArtifactPolicy {
        final String ruleId;
        final String sha;
        final String candidateDisposition;
        final String candidateRole;
        final String oracleDisposition;
        final String oracleRole;

        ArtifactPolicy(String ruleId, String sha,
                String candidateDisposition, String candidateRole,
                String oracleDisposition, String oracleRole) {
            this.ruleId = ruleId;
            this.sha = sha;
            this.candidateDisposition = candidateDisposition;
            this.candidateRole = candidateRole;
            this.oracleDisposition = oracleDisposition;
            this.oracleRole = oracleRole;
        }
    }

    private static final class RuntimeShadow {
        final String lane;
        final int position;
        final String role;
        final String internalName;
        final String entry;
        final String element;
        final String sha;
        final String candidateADisposition;
        final String candidateBDisposition;
        final String oracleDisposition;

        RuntimeShadow(String lane, int position, String role,
                String internalName, String entry, String element,
                String sha, String candidateADisposition,
                String candidateBDisposition, String oracleDisposition) {
            this.lane = lane;
            this.position = position;
            this.role = role;
            this.internalName = internalName;
            this.entry = entry;
            this.element = element;
            this.sha = sha;
            this.candidateADisposition = candidateADisposition;
            this.candidateBDisposition = candidateBDisposition;
            this.oracleDisposition = oracleDisposition;
        }
    }

    private static final class OverlapProof {
        final String originalName;
        final String candidateAName;
        final String candidateBName;

        OverlapProof(String originalName, String candidateAName,
                String candidateBName) {
            this.originalName = originalName;
            this.candidateAName = candidateAName;
            this.candidateBName = candidateBName;
        }

        String candidateADisposition() {
            return originalName.equals(candidateAName)
                    ? "CHILD_FIRST_OWNED" : "BLOCKED_ORIGINAL_ALIAS";
        }

        String candidateBDisposition() {
            return originalName.equals(candidateBName)
                    ? "CHILD_FIRST_OWNED" : "BLOCKED_ORIGINAL_ALIAS";
        }
    }

    private static final class AliasBlockResult {
        final String universe;
        final String internalName;
        String status = "FAIL";
        String detail = "not examined";

        AliasBlockResult(String universe, String internalName) {
            this.universe = universe;
            this.internalName = internalName;
        }
    }

    private static final class RuntimeClassOrigin {
        final int position;
        final String role;
        final Path element;
        final String entry;
        final int release;
        final String sha;
        final String codeSource;

        RuntimeClassOrigin(int position, String role, Path element,
                String entry, int release, String sha) throws IOException {
            this.position = position;
            this.role = role;
            this.element = element;
            this.entry = entry;
            this.release = release;
            this.sha = sha;
            this.codeSource = element.toUri().toURL().toExternalForm();
        }

        String render() {
            return position + ":" + element + (entry.isEmpty()
                    ? "" : "!/" + entry);
        }
    }

    private static final class RuntimeCollision {
        final String lane;
        final String internalName;
        final RuntimeClassOrigin first;
        final RuntimeClassOrigin later;
        final String byteRelation;
        boolean policyMatched;
        String status = "FAIL";
        String detail = "not examined";

        RuntimeCollision(String lane, String internalName,
                RuntimeClassOrigin first, RuntimeClassOrigin later) {
            this.lane = lane;
            this.internalName = internalName;
            this.first = first;
            this.later = later;
            this.byteRelation = first.sha.equals(later.sha)
                    ? "BYTE_IDENTICAL" : "BYTE_DIFFERENT";
        }
    }

    private static final class CollisionPolicyRow {
        final String lane;
        final String internalName;
        final int firstPosition;
        final String firstRole;
        final String firstEntry;
        final int firstRelease;
        final String firstSha;
        final int laterPosition;
        final String laterRole;
        final String laterEntry;
        final int laterRelease;
        final String laterSha;
        final String byteRelation;
        boolean seen;

        CollisionPolicyRow(String lane, String internalName,
                int firstPosition, String firstRole, String firstEntry,
                int firstRelease, String firstSha, int laterPosition,
                String laterRole, String laterEntry, int laterRelease,
                String laterSha, String byteRelation) {
            this.lane = lane;
            this.internalName = internalName;
            this.firstPosition = firstPosition;
            this.firstRole = firstRole;
            this.firstEntry = firstEntry;
            this.firstRelease = firstRelease;
            this.firstSha = firstSha;
            this.laterPosition = laterPosition;
            this.laterRole = laterRole;
            this.laterEntry = laterEntry;
            this.laterRelease = laterRelease;
            this.laterSha = laterSha;
            this.byteRelation = byteRelation;
        }

        String key() {
            return internalName + "\u0000" + laterPosition;
        }

        boolean matches(RuntimeCollision collision) {
            return lane.equals(collision.lane)
                    && internalName.equals(collision.internalName)
                    && firstPosition == collision.first.position
                    && firstRole.equals(collision.first.role)
                    && firstEntry.equals(collision.first.entry)
                    && firstRelease == collision.first.release
                    && firstSha.equals(collision.first.sha)
                    && laterPosition == collision.later.position
                    && laterRole.equals(collision.later.role)
                    && laterEntry.equals(collision.later.entry)
                    && laterRelease == collision.later.release
                    && laterSha.equals(collision.later.sha)
                    && byteRelation.equals(collision.byteRelation);
        }
    }

    private static final class FrameModel {
        final int ordinal;
        final int offset;
        final List<Object> locals;
        final List<Object> stack;
        FrameModel(int ordinal, int offset, List<Object> locals,
                   List<Object> stack) {
            this.ordinal = ordinal;
            this.offset = offset;
            this.locals = locals;
            this.stack = stack;
        }
    }

    private static final class MethodModel {
        final String name;
        final String descriptor;
        final int access;
        final List<FrameModel> frames = new ArrayList<>();
        boolean codeVisited;
        boolean maxsVisited;
        boolean endVisited;
        int maxStack = -1;
        int maxLocals = -1;
        int currentOffset = -1;

        MethodModel(String name, String descriptor, int access) {
            this.name = name;
            this.descriptor = descriptor;
            this.access = access;
        }

        String key() { return name + "\u0000" + descriptor; }
        boolean abstractOrNative() {
            return (access & (Opcodes.ACC_ABSTRACT | Opcodes.ACC_NATIVE)) != 0;
        }
    }

    private static final class ClassModel {
        final String internalName;
        final Map<String, MethodModel> methods = new TreeMap<>();
        boolean headerVisited;
        boolean endVisited;
        ClassModel(String internalName) { this.internalName = internalName; }
    }

    private static final class MappingRow {
        final String comparison;
        final String namespace;
        final String left;
        final String right;
        final String identity;
        MappingRow(String comparison, String namespace, String left,
                   String right, String identity) {
            this.comparison = comparison;
            this.namespace = namespace;
            this.left = left;
            this.right = right;
            this.identity = identity;
        }
    }

    private static final class RelationResult {
        final String comparison;
        final String namespace;
        final String leftClass;
        final String rightClass;
        final String leftMethod;
        final String leftDescriptor;
        String rightMethod = "";
        String rightDescriptor = "";
        int leftMaxStack = -1;
        int rightMaxStack = -1;
        int leftMaxLocals = -1;
        int rightMaxLocals = -1;
        int leftFrames = -1;
        int rightFrames = -1;
        String status = "FAIL";
        String detail = "not examined";

        RelationResult(String comparison, String namespace, String leftClass,
                       String rightClass, String leftMethod,
                       String leftDescriptor) {
            this.comparison = comparison;
            this.namespace = namespace;
            this.leftClass = leftClass;
            this.rightClass = rightClass;
            this.leftMethod = leftMethod;
            this.leftDescriptor = leftDescriptor;
        }
    }

    private static final class VerificationResult {
        final String universe;
        final String internalName;
        final String expectedLoader;
        String actualLoader = "";
        String expectedOrigin;
        String actualOrigin = "";
        String initialized = "false";
        String status = "FAIL";
        String detail = "not examined";

        VerificationResult(String universe, String internalName,
                           String expectedLoader, String expectedOrigin) {
            this.universe = universe;
            this.internalName = internalName;
            this.expectedLoader = expectedLoader;
            this.expectedOrigin = expectedOrigin;
        }
    }

    private static final class Violation {
        final String scope;
        final String universe;
        final String path;
        final String rule;
        final String detail;
        Violation(String scope, String universe, String path, String rule,
                  String detail) {
            this.scope = scope;
            this.universe = universe;
            this.path = path;
            this.rule = rule;
            this.detail = detail;
        }
    }

    private static final class State {
        final Map<String, CohortRow> cohort = new TreeMap<>();
        final Map<String, String> originalNamespace = new TreeMap<>();
        final Map<String, ClassBytes> candidateA = new TreeMap<>();
        final Map<String, ClassBytes> candidateB = new TreeMap<>();
        final Map<String, ClassBytes> original = new TreeMap<>();
        final List<RuntimeElement> runtime = new ArrayList<>();
        final Map<String, ArtifactPolicy> runtimePolicy = new TreeMap<>();
        final Map<String, CollisionPolicyRow> candidateCollisionPolicy =
                new LinkedHashMap<>();
        final List<RuntimeShadow> runtimeShadows = new ArrayList<>();
        final List<AliasBlockResult> aliasBlocks = new ArrayList<>();
        final Map<String, RuntimeClassOrigin> oracleRootClasses = new TreeMap<>();
        final List<RuntimeCollision> runtimeCollisions = new ArrayList<>();
        final Map<String, List<MappingRow>> mappings = new LinkedHashMap<>();
        final List<RelationResult> relations = new ArrayList<>();
        final List<VerificationResult> verifications = new ArrayList<>();
        final List<Violation> violations = new ArrayList<>();
        final Map<String, String> inputOrigins = new LinkedHashMap<>();
        final Map<String, String> inputPre = new LinkedHashMap<>();
        final Map<String, String> inputPost = new LinkedHashMap<>();
        long methodsExpected;
        long methodsRecorded;
        long framesExpected;
        long framesRecorded;
        long verificationExpected;
        long verificationRecorded;
        long relationExpected;
        long relationRecorded;
        long aliasBlockExpected;
        long aliasBlockRecorded;
        long collisionExpected;
        long collisionRecorded;
        int expectedCandidateRuntimeElements = -1;
        int expectedOracleRuntimeElements = -1;
        String expectedCandidateLedgerSha = "";
        String expectedOracleLedgerSha = "";
        boolean setupComplete;
        boolean ledgerComplete;
        boolean productionShapeRequired;

        State() {
            for (String relation : RELATIONS) mappings.put(relation,
                    new ArrayList<>());
        }

        void violation(String scope, String universe, String path,
                       String rule, String detail) {
            violations.add(new Violation(scope, universe, path, rule,
                    clean(detail)));
        }
    }

    private static Path absolute(Path path) {
        return path.toAbsolutePath().normalize();
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new Failure(message);
    }

    private static String clean(String value) {
        if (value == null) return "";
        return value.replace("\\", "\\\\").replace("\t", "\\t")
                .replace("\r", "\\r").replace("\n", "\\n");
    }

    private static String hex(byte[] bytes) {
        StringBuilder result = new StringBuilder(bytes.length * 2);
        for (byte value : bytes) result.append(String.format(Locale.ROOT,
                "%02x", value & 0xff));
        return result.toString();
    }

    private static MessageDigest digest() {
        try { return MessageDigest.getInstance("SHA-256"); }
        catch (NoSuchAlgorithmException impossible) {
            throw new AssertionError(impossible);
        }
    }

    private static String sha256(byte[] bytes) {
        return hex(digest().digest(bytes));
    }

    private static String sha256(Path path) throws IOException {
        MessageDigest digest = digest();
        try (InputStream input = Files.newInputStream(path)) {
            byte[] buffer = new byte[1 << 16];
            for (int count; (count = input.read(buffer)) >= 0;) {
                if (count > 0) digest.update(buffer, 0, count);
            }
        }
        return hex(digest.digest());
    }

    private static Path canonicalFile(Path path) throws IOException {
        Path absolute = absolute(path);
        require(Files.isRegularFile(absolute, LinkOption.NOFOLLOW_LINKS)
                        && !Files.isSymbolicLink(absolute),
                "not a regular non-symlink file: " + absolute);
        Path real = absolute.toRealPath(LinkOption.NOFOLLOW_LINKS);
        require(real.equals(absolute), "noncanonical file origin: " + absolute
                + " -> " + real);
        return real;
    }

    private static Path canonicalDirectory(Path path) throws IOException {
        Path absolute = absolute(path);
        require(Files.isDirectory(absolute, LinkOption.NOFOLLOW_LINKS)
                        && !Files.isSymbolicLink(absolute),
                "not a real non-symlink directory: " + absolute);
        Path real = absolute.toRealPath(LinkOption.NOFOLLOW_LINKS);
        require(real.equals(absolute), "noncanonical directory origin: "
                + absolute + " -> " + real);
        return real;
    }

    private static boolean overlaps(Path left, Path right) {
        return left.equals(right) || left.startsWith(right)
                || right.startsWith(left);
    }

    private static boolean safeRelative(String value) {
        if (value == null || value.isEmpty() || value.startsWith("/")
                || value.indexOf('\\') >= 0 || value.indexOf('\0') >= 0) {
            return false;
        }
        Path path = Path.of(value);
        if (path.isAbsolute() || !path.normalize().toString()
                .replace('\\', '/').equals(value)) return false;
        for (String part : value.split("/", -1)) {
            if (part.isEmpty() || part.equals(".") || part.equals("..")) {
                return false;
            }
        }
        return true;
    }

    private static String[] row(String line, int width, String label,
                                int number) {
        String[] fields = line.split("\t", -1);
        require(fields.length == width, "malformed " + label + " row "
                + number);
        return fields;
    }

    private static int column(String[] header, String name) {
        for (int index = 0; index < header.length; index++) {
            if (header[index].equals(name)) return index;
        }
        throw new Failure("TSV column missing: " + name);
    }

    private static List<Path> tree(Path root) throws IOException {
        try (Stream<Path> paths = Files.walk(root)) {
            return paths.filter(path -> !path.equals(root))
                    .sorted(Comparator.comparing(path -> root.relativize(path)
                            .toString().replace('\\', '/')))
                    .collect(Collectors.toList());
        }
    }

    private static String directoryManifest(Path root) throws IOException {
        MessageDigest digest = digest();
        for (Path path : tree(root)) {
            String relative = root.relativize(path).toString()
                    .replace('\\', '/');
            String line;
            if (Files.isSymbolicLink(path)) line = "L\t-\t0\t" + relative + "\n";
            else if (Files.isDirectory(path, LinkOption.NOFOLLOW_LINKS))
                line = "D\t-\t0\t" + relative + "\n";
            else if (Files.isRegularFile(path, LinkOption.NOFOLLOW_LINKS))
                line = "F\t" + sha256(path) + "\t" + Files.size(path)
                        + "\t" + relative + "\n";
            else line = "S\t-\t0\t" + relative + "\n";
            digest.update(line.getBytes(StandardCharsets.UTF_8));
        }
        return hex(digest.digest());
    }

    private static byte[] readClassBytes(InputStream input, long declared,
                                         String label) throws IOException {
        require(declared < 0 || declared <= MAX_CLASS_BYTES,
                "oversized class " + label);
        ByteArrayOutputStream output = new ByteArrayOutputStream(
                declared > 0 && declared < Integer.MAX_VALUE
                        ? (int) declared : 8192);
        byte[] buffer = new byte[1 << 16];
        long total = 0;
        for (int count; (count = input.read(buffer)) >= 0;) {
            if (count == 0) continue;
            total += count;
            require(total <= MAX_CLASS_BYTES, "oversized class " + label);
            output.write(buffer, 0, count);
        }
        require(declared < 0 || total == declared,
                "class size differs while reading " + label);
        return output.toByteArray();
    }

    private static void readCohort(Path path, State state) throws IOException {
        try (BufferedReader reader = Files.newBufferedReader(path,
                StandardCharsets.UTF_8)) {
            String first = reader.readLine();
            require(first != null, "empty cohort TSV");
            String[] header = first.split("\t", -1);
            int namespace = column(header, "namespace");
            int expected = column(header, "expected_classes");
            int number = 1;
            for (String line; (line = reader.readLine()) != null;) {
                number++;
                String[] fields = row(line, header.length, "cohort", number);
                require(!fields[namespace].isEmpty(),
                        "empty cohort namespace in row " + number);
                int count;
                try { count = Integer.parseInt(fields[expected]); }
                catch (NumberFormatException bad) {
                    throw new Failure("invalid expected class count in row "
                            + number);
                }
                require(count > 0, "nonpositive expected class count");
                require(state.cohort.put(fields[namespace],
                        new CohortRow(fields[namespace], count)) == null,
                        "duplicate cohort namespace: " + fields[namespace]);
            }
        }
        require(!state.cohort.isEmpty(), "empty exact-AOT cohort");
    }

    private static void readOwnership(Path path, State state)
            throws IOException {
        Map<String, Integer> counts = new HashMap<>();
        try (BufferedReader reader = Files.newBufferedReader(path,
                StandardCharsets.UTF_8)) {
            String first = reader.readLine();
            require(first != null, "empty ownership TSV");
            String[] header = first.split("\t", -1);
            int entry = column(header, "entry");
            int owner = column(header, "owner_id");
            int number = 1;
            for (String line; (line = reader.readLine()) != null;) {
                number++;
                String[] fields = row(line, header.length, "ownership", number);
                if (!state.cohort.containsKey(fields[owner])) continue;
                require(fields[entry].endsWith(".class")
                                && safeRelative(fields[entry]),
                        "invalid owned class path in row " + number);
                String internal = fields[entry].substring(0,
                        fields[entry].length() - 6);
                require(state.originalNamespace.put(internal,
                        fields[owner]) == null,
                        "duplicate owned original class: " + internal);
                counts.merge(fields[owner], 1, Integer::sum);
            }
        }
        for (CohortRow row : state.cohort.values()) {
            require(counts.getOrDefault(row.namespace, 0)
                            == row.expectedClasses,
                    "cohort/ownership cardinality differs for " + row.namespace);
        }
    }

    private static void readRuntimeLedger(Path path, String lane, State state)
            throws IOException {
        try (BufferedReader reader = Files.newBufferedReader(path,
                StandardCharsets.UTF_8)) {
            require("position\trole\tkind\tpath\texpected_sha256"
                            .equals(reader.readLine()),
                    "unexpected runtime ledger header");
            int expectedPosition = 1;
            int number = 1;
            for (String line; (line = reader.readLine()) != null;) {
                number++;
                String[] fields = row(line, 5, "runtime", number);
                int position;
                try { position = Integer.parseInt(fields[0]); }
                catch (NumberFormatException bad) {
                    throw new Failure("invalid runtime position in row " + number);
                }
                require(position == expectedPosition++,
                        "runtime positions are not contiguous at " + position);
                require(!fields[1].isEmpty(), "empty runtime role in row "
                        + number);
                require(Set.of("jar", "directory").contains(fields[2]),
                        "unsupported runtime kind: " + fields[2]);
                require(SHA256.matcher(fields[4]).matches(),
                        "invalid runtime SHA-256 in row " + number);
                state.runtime.add(new RuntimeElement(lane, position, fields[1],
                        fields[2], absolute(Path.of(fields[3])), fields[4]));
            }
        }
        require(state.runtime.stream().anyMatch(value -> value.lane.equals(lane)),
                lane + " runtime dependency ledger is empty");
    }

    private static void readCandidateCollisionPolicy(Path path, State state)
            throws IOException {
        try (BufferedReader reader = Files.newBufferedReader(path,
                StandardCharsets.UTF_8)) {
            require(CANDIDATE_COLLISION_HEADER.equals(reader.readLine()),
                    "unexpected candidate collision policy header");
            int number = 1;
            for (String line; (line = reader.readLine()) != null;) {
                number++;
                String[] fields = row(line, 13,
                        "candidate-collision-policy", number);
                require(fields[0].equals("candidate"),
                        "collision policy may describe only candidate lane rows");
                require(!fields[1].endsWith(".class")
                                && safeRelative(fields[1] + ".class"),
                        "unsafe collision internal name in row " + number);
                require(!fields[3].isEmpty() && !fields[8].isEmpty()
                                && fields[3].indexOf('\0') < 0
                                && fields[8].indexOf('\0') < 0,
                        "unsafe collision role in row " + number);
                require(safeRelative(fields[4])
                                && fields[4].endsWith(".class")
                                && safeRelative(fields[9])
                                && fields[9].endsWith(".class"),
                        "unsafe collision entry in row " + number);
                int firstPosition;
                int firstRelease;
                int laterPosition;
                int laterRelease;
                try {
                    firstPosition = Integer.parseInt(fields[2]);
                    firstRelease = Integer.parseInt(fields[5]);
                    laterPosition = Integer.parseInt(fields[7]);
                    laterRelease = Integer.parseInt(fields[10]);
                } catch (NumberFormatException bad) {
                    throw new Failure("invalid collision integer in row "
                            + number);
                }
                require(firstPosition > 0 && firstPosition < laterPosition
                                && firstRelease >= 0
                                && firstRelease <= JAVA_FEATURE
                                && laterRelease >= 0
                                && laterRelease <= JAVA_FEATURE,
                        "invalid collision order/release in row " + number);
                ClassPathIdentity firstPath = archiveClassPath(fields[4]);
                ClassPathIdentity laterPath = archiveClassPath(fields[9]);
                require(firstPath.internalPath.equals(fields[1] + ".class")
                                && laterPath.internalPath.equals(
                                        fields[1] + ".class")
                                && firstPath.release == firstRelease
                                && laterPath.release == laterRelease,
                        "collision entry/internal/release mismatch in row "
                                + number);
                require(SHA256.matcher(fields[6]).matches()
                                && SHA256.matcher(fields[11]).matches(),
                        "invalid collision class SHA-256 in row " + number);
                require(Set.of("BYTE_IDENTICAL", "BYTE_DIFFERENT")
                                .contains(fields[12])
                                && fields[12].equals(fields[6].equals(fields[11])
                                        ? "BYTE_IDENTICAL" : "BYTE_DIFFERENT"),
                        "collision byte relation disagrees with hashes in row "
                                + number);
                CollisionPolicyRow policy = new CollisionPolicyRow(fields[0],
                        fields[1], firstPosition, fields[3], fields[4],
                        firstRelease, fields[6], laterPosition, fields[8],
                        fields[9], laterRelease, fields[11], fields[12]);
                require(state.candidateCollisionPolicy.put(policy.key(),
                                policy) == null,
                        "duplicate candidate collision policy row " + number);
            }
        }
    }

    private static void readRuntimePolicy(Path path, State state)
            throws IOException {
        Set<String> requiredIds = Set.of("transactor-original",
                "peer-original", "core2-original", "nano-original",
                "nano-sanitized");
        try (BufferedReader reader = Files.newBufferedReader(path,
                StandardCharsets.UTF_8)) {
            require(("rule_id\tsha256\tcandidate_disposition\tcandidate_role"
                    + "\toracle_disposition\toracle_role"
                    + "\tcandidate_elements\toracle_elements"
                    + "\tcandidate_ledger_sha256\toracle_ledger_sha256")
                            .equals(reader.readLine()),
                    "unexpected runtime policy header");
            int number = 1;
            for (String line; (line = reader.readLine()) != null;) {
                number++;
                String[] fields = row(line, 10, "runtime-policy", number);
                require(requiredIds.contains(fields[0]),
                        "unknown runtime-policy rule " + fields[0]);
                require(SHA256.matcher(fields[1]).matches(),
                        "invalid runtime-policy SHA-256 in row " + number);
                require(Set.of("FORBID", "REQUIRE", "OWNED")
                                .contains(fields[2])
                                && Set.of("FORBID", "REQUIRE", "OWNED")
                                        .contains(fields[4]),
                        "invalid runtime-policy disposition in row " + number);
                require((fields[2].equals("REQUIRE")) == !fields[3].isEmpty()
                                && (fields[4].equals("REQUIRE"))
                                        == !fields[5].isEmpty(),
                        "runtime-policy roles must exist exactly for REQUIRE rows");
                int candidateElements;
                int oracleElements;
                try {
                    candidateElements = Integer.parseInt(fields[6]);
                    oracleElements = Integer.parseInt(fields[7]);
                } catch (NumberFormatException bad) {
                    throw new Failure("invalid runtime-policy element count in row "
                            + number);
                }
                require(candidateElements > 0 && oracleElements > 0
                                && SHA256.matcher(fields[8]).matches()
                                && SHA256.matcher(fields[9]).matches(),
                        "invalid runtime-policy ledger anchor in row " + number);
                if (state.expectedCandidateRuntimeElements < 0) {
                    state.expectedCandidateRuntimeElements = candidateElements;
                    state.expectedOracleRuntimeElements = oracleElements;
                    state.expectedCandidateLedgerSha = fields[8];
                    state.expectedOracleLedgerSha = fields[9];
                } else {
                    require(state.expectedCandidateRuntimeElements
                                    == candidateElements
                                    && state.expectedOracleRuntimeElements
                                            == oracleElements
                                    && state.expectedCandidateLedgerSha
                                            .equals(fields[8])
                                    && state.expectedOracleLedgerSha
                                            .equals(fields[9]),
                            "runtime-policy ledger anchors differ between rows");
                }
                require(state.runtimePolicy.put(fields[0], new ArtifactPolicy(
                        fields[0], fields[1], fields[2], fields[3], fields[4],
                        fields[5])) == null,
                        "duplicate runtime-policy rule " + fields[0]);
            }
        }
        require(state.runtimePolicy.keySet().equals(requiredIds),
                "runtime policy must contain exactly the five pinned identities");
        require(state.runtimePolicy.get("transactor-original")
                        .candidateDisposition.equals("FORBID")
                        && state.runtimePolicy.get("transactor-original")
                                .oracleDisposition.equals("OWNED")
                        && state.runtimePolicy.get("peer-original")
                                .candidateDisposition.equals("FORBID")
                        && state.runtimePolicy.get("peer-original")
                                .oracleDisposition.equals("FORBID")
                        && state.runtimePolicy.get("core2-original")
                                .candidateDisposition.equals("FORBID")
                        && state.runtimePolicy.get("core2-original")
                                .oracleDisposition.equals("REQUIRE")
                        && state.runtimePolicy.get("nano-original")
                                .candidateDisposition.equals("FORBID")
                        && state.runtimePolicy.get("nano-original")
                                .oracleDisposition.equals("REQUIRE")
                        && state.runtimePolicy.get("nano-sanitized")
                                .candidateDisposition.equals("REQUIRE")
                        && state.runtimePolicy.get("nano-sanitized")
                                .oracleDisposition.equals("FORBID"),
                "runtime policy dispositions do not describe the exact Datomic lanes");
        Set<String> hashes = state.runtimePolicy.values().stream()
                .map(value -> value.sha).collect(Collectors.toSet());
        require(hashes.size() == state.runtimePolicy.size(),
                "runtime policy aliases two licensed/sanitized identities");
    }

    private static void readMappings(Path path, State state)
            throws IOException {
        Set<String> exactRows = new HashSet<>();
        try (BufferedReader reader = Files.newBufferedReader(path,
                StandardCharsets.UTF_8)) {
            require("comparison\tnamespace\tleft_class\tright_class\tidentity"
                            .equals(reader.readLine()),
                    "unexpected class-mapping header");
            int number = 1;
            for (String line; (line = reader.readLine()) != null;) {
                number++;
                String[] fields = row(line, 5, "class-mapping", number);
                require(RELATIONS.contains(fields[0]),
                        "unknown comparison in mapping row " + number);
                require(state.cohort.containsKey(fields[1]),
                        "mapping namespace outside cohort in row " + number);
                require(!fields[2].isEmpty() && !fields[3].isEmpty(),
                        "empty mapped class in row " + number);
                require(exactRows.add(line),
                        "duplicate class-mapping row " + number);
                state.mappings.get(fields[0]).add(new MappingRow(fields[0],
                        fields[1], fields[2], fields[3], fields[4]));
            }
        }
        for (String relation : RELATIONS) {
            require(!state.mappings.get(relation).isEmpty(),
                    "mapping relation is empty: " + relation);
        }
    }

    private static Map<String, ClassBytes> indexCandidate(Path root,
            String universe, State state) throws IOException {
        Map<String, ClassBytes> result = new TreeMap<>();
        Map<String, Integer> counts = new HashMap<>();
        Set<Object> physical = new HashSet<>();
        for (Path path : tree(root)) {
            String relative = root.relativize(path).toString()
                    .replace('\\', '/');
            require(!Files.isSymbolicLink(path),
                    universe + " contains a symbolic link: " + relative);
            Path real = absolute(path).toRealPath(LinkOption.NOFOLLOW_LINKS);
            require(real.equals(absolute(path)), universe
                    + " contains a noncanonical entry: " + relative);
            if (Files.isDirectory(path, LinkOption.NOFOLLOW_LINKS)) continue;
            require(Files.isRegularFile(path, LinkOption.NOFOLLOW_LINKS),
                    universe + " contains a special file: " + relative);
            Object key = Files.readAttributes(path,
                    java.nio.file.attribute.BasicFileAttributes.class,
                    LinkOption.NOFOLLOW_LINKS).fileKey();
            if (key != null) require(physical.add(key),
                    universe + " contains hard-linked class entries");
            require(relative.endsWith(".class"),
                    universe + " contains an extra non-class file: " + relative);
            Path logical = Path.of(relative);
            require(logical.getNameCount() >= 2,
                    universe + " class lacks namespace container: " + relative);
            String namespace = logical.getName(0).toString();
            require(state.cohort.containsKey(namespace),
                    universe + " has extra namespace container: " + namespace);
            String internalPath = logical.subpath(1, logical.getNameCount())
                    .toString().replace('\\', '/');
            String expectedInternal = internalPath.substring(0,
                    internalPath.length() - 6);
            byte[] bytes;
            try (InputStream input = Files.newInputStream(path)) {
                bytes = readClassBytes(input, Files.size(path), relative);
            }
            ClassReader reader = new ClassReader(bytes);
            require(reader.getClassName().equals(expectedInternal), universe
                    + " path/internal mismatch: " + relative + " -> "
                    + reader.getClassName());
            ClassBytes classBytes = new ClassBytes(universe, namespace,
                    reader.getClassName(), relative, path.toString(), bytes);
            require(result.put(classBytes.internalName, classBytes) == null,
                    universe + " duplicate class " + classBytes.internalName);
            counts.merge(namespace, 1, Integer::sum);
        }
        for (CohortRow row : state.cohort.values()) {
            int actual = counts.getOrDefault(row.namespace, 0);
            require(actual == row.expectedClasses, universe
                    + " class count differs for " + row.namespace + ": "
                    + actual + " vs " + row.expectedClasses);
        }
        return result;
    }

    private static Map<String, ClassBytes> indexOriginal(Path jarPath,
            State state) throws IOException {
        Map<String, ClassBytes> result = new TreeMap<>();
        Set<String> names = new HashSet<>();
        try (ZipFile zip = new ZipFile(jarPath.toFile())) {
            Enumeration<? extends ZipEntry> entries = zip.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                require(names.add(entry.getName()),
                        "original JAR has duplicate entry " + entry.getName());
                if (entry.isDirectory() || !entry.getName().endsWith(".class"))
                    continue;
                String logical = multiReleaseClassPath(entry.getName());
                require(safeRelative(logical),
                        "unsafe original class path " + entry.getName());
                String internal = logical.substring(0, logical.length() - 6);
                byte[] bytes;
                try (InputStream input = zip.getInputStream(entry)) {
                    bytes = readClassBytes(input, entry.getSize(),
                            entry.getName());
                }
                ClassReader reader = new ClassReader(bytes);
                require(reader.getClassName().equals(internal),
                        "original path/internal mismatch: " + entry.getName());
                if (!internal.equals("module-info")) {
                    state.oracleRootClasses.putIfAbsent(internal,
                            new RuntimeClassOrigin(0, "owned-transactor-root",
                                    jarPath, entry.getName(),
                                    archiveClassPath(entry.getName()).release,
                                    sha256(bytes)));
                }
                if (!entry.getName().equals(logical)) continue;
                String namespace = state.originalNamespace.get(internal);
                if (namespace == null) continue;
                ClassBytes classBytes = new ClassBytes("original", namespace,
                        internal, entry.getName(), jarPath + "!/"
                                + entry.getName(), bytes);
                require(result.put(internal, classBytes) == null,
                        "duplicate owned original class " + internal);
            }
        }
        require(result.keySet().equals(state.originalNamespace.keySet()),
                "owned original class membership is missing or extra: missing="
                        + difference(state.originalNamespace.keySet(),
                                result.keySet()) + " extra="
                        + difference(result.keySet(),
                                state.originalNamespace.keySet()));
        return result;
    }

    private static <T> Set<T> difference(Set<T> left, Set<T> right) {
        Set<T> result = new TreeSet<>();
        result.addAll(left);
        result.removeAll(right);
        return result;
    }

    private static String multiReleaseClassPath(String name) {
        String prefix = "META-INF/versions/";
        if (!name.startsWith(prefix)) return name;
        String rest = name.substring(prefix.length());
        int slash = rest.indexOf('/');
        require(slash > 0 && rest.substring(0, slash).matches("[0-9]+"),
                "malformed multi-release class path: " + name);
        return rest.substring(slash + 1);
    }

    private static final class ClassPathIdentity {
        final String internalPath;
        final int release;
        ClassPathIdentity(String internalPath, int release) {
            this.internalPath = internalPath;
            this.release = release;
        }
    }

    private static ClassPathIdentity archiveClassPath(String name) {
        String prefix = "META-INF/versions/";
        if (!name.startsWith(prefix)) return new ClassPathIdentity(name, 0);
        String rest = name.substring(prefix.length());
        int slash = rest.indexOf('/');
        require(slash > 0 && rest.substring(0, slash).matches("[0-9]+"),
                "malformed multi-release class path: " + name);
        int release;
        try { release = Integer.parseInt(rest.substring(0, slash)); }
        catch (NumberFormatException bad) {
            throw new Failure("oversized multi-release class version: "
                    + name);
        }
        require(release >= 9,
                "multi-release class version is below 9: " + name);
        String internalPath = rest.substring(slash + 1);
        require(safeRelative(internalPath),
                "unsafe multi-release class path: " + name);
        return new ClassPathIdentity(internalPath, release);
    }

    private static boolean multiRelease(ZipFile zip) throws IOException {
        ZipEntry manifest = zip.getEntry("META-INF/MANIFEST.MF");
        if (manifest == null) return false;
        try (InputStream input = zip.getInputStream(manifest)) {
            java.util.jar.Manifest value = new java.util.jar.Manifest(input);
            return "true".equalsIgnoreCase(value.getMainAttributes()
                    .getValue("Multi-Release"));
        }
    }

    private static void indexRuntimeClasses(RuntimeElement element,
            State state, Map<String, RuntimeClassOrigin> runtimeOwners,
            Set<String> ownedNames, Map<String, OverlapProof> overlapProofs)
            throws IOException {
        String scanSha = element.kind.equals("directory")
                ? directoryManifest(element.path) : sha256(element.path);
        if (element.preSha.isEmpty()) element.preSha = scanSha;
        if (!element.preSha.equals(scanSha)) {
            state.violation("input", "runtime", element.path.toString(),
                    "mutated-before-index", "pre=" + element.preSha
                            + " index=" + scanSha);
        }
        if (!element.preSha.equals(element.expectedSha)) {
            state.violation("input", "runtime", element.path.toString(),
                    "hash-mismatch", "expected " + element.expectedSha
                            + " got " + element.preSha);
        }
        if (element.kind.equals("directory")) {
            for (Path path : tree(element.path)) {
                String relative = element.path.relativize(path).toString()
                        .replace('\\', '/');
                require(!Files.isSymbolicLink(path),
                        "runtime dependency contains symlink: " + relative);
                if (Files.isDirectory(path, LinkOption.NOFOLLOW_LINKS)) continue;
                require(Files.isRegularFile(path, LinkOption.NOFOLLOW_LINKS),
                        "runtime dependency contains special file: " + relative);
                if (!relative.endsWith(".class")) continue;
                byte[] bytes;
                try (InputStream input = Files.newInputStream(path)) {
                    bytes = readClassBytes(input, Files.size(path), relative);
                }
                ClassReader reader = new ClassReader(bytes);
                String expected = relative.substring(0, relative.length() - 6);
                require(reader.getClassName().equals(expected),
                        "runtime path/internal mismatch: " + relative);
                String classSha = sha256(bytes);
                registerRuntimeClass(element, state, runtimeOwners, expected,
                        new RuntimeClassOrigin(element.position, element.role,
                                element.path, relative, 0, classSha));
                recordRuntimeShadow(element, state, expected, relative,
                        path.toString(), classSha, ownedNames,
                        overlapProofs);
                element.classes++;
            }
        } else {
            Set<String> entryNames = new HashSet<>();
            Set<String> releaseClasses = new HashSet<>();
            Map<String, RuntimeClassOrigin> effectiveClasses = new TreeMap<>();
            try (ZipFile zip = new ZipFile(element.path.toFile())) {
                boolean multiRelease = multiRelease(zip);
                Enumeration<? extends ZipEntry> entries = zip.entries();
                while (entries.hasMoreElements()) {
                    ZipEntry entry = entries.nextElement();
                    require(entryNames.add(entry.getName()),
                            "runtime JAR duplicate entry " + entry.getName());
                    if (entry.isDirectory() || !entry.getName().endsWith(".class"))
                        continue;
                    ClassPathIdentity identity = archiveClassPath(
                            entry.getName());
                    String logical = identity.internalPath;
                    require(logical.endsWith(".class") && safeRelative(logical),
                            "unsafe runtime class path " + entry.getName());
                    byte[] bytes;
                    try (InputStream input = zip.getInputStream(entry)) {
                        bytes = readClassBytes(input, entry.getSize(),
                                entry.getName());
                    }
                    ClassReader reader = new ClassReader(bytes);
                    String expected = logical.substring(0, logical.length() - 6);
                    require(reader.getClassName().equals(expected),
                            "runtime path/internal mismatch: " + entry.getName());
                    String collisionKey = expected + "@" + identity.release;
                    require(releaseClasses.add(collisionKey),
                            "duplicate runtime class/release " + collisionKey
                                    + " in " + element.path);
                    if (identity.release == 0
                            || (multiRelease
                                    && identity.release <= JAVA_FEATURE)) {
                        RuntimeClassOrigin current = effectiveClasses.get(
                                expected);
                        if (current == null
                                || identity.release > current.release) {
                            effectiveClasses.put(expected,
                                    new RuntimeClassOrigin(element.position,
                                            element.role, element.path,
                                            entry.getName(), identity.release,
                                            sha256(bytes)));
                        }
                    }
                    element.classes++;
                }
            }
            for (Map.Entry<String, RuntimeClassOrigin> value
                    : effectiveClasses.entrySet()) {
                RuntimeClassOrigin origin = value.getValue();
                registerRuntimeClass(element, state, runtimeOwners,
                        value.getKey(), origin);
                recordRuntimeShadow(element, state, value.getKey(),
                        origin.entry, element.path + "!/" + origin.entry,
                        origin.sha, ownedNames, overlapProofs);
            }
        }
        element.scanned = true;
    }

    private static void registerRuntimeClass(RuntimeElement element,
            State state, Map<String, RuntimeClassOrigin> runtimeOwners,
            String internalName, RuntimeClassOrigin origin) {
        if (internalName.equals("module-info")) return;
        RuntimeClassOrigin first = runtimeOwners.putIfAbsent(internalName,
                origin);
        if (first == null) return;
        element.collisions++;
        RuntimeCollision collision = new RuntimeCollision(element.lane,
                internalName, first, origin);
        state.runtimeCollisions.add(collision);
        if (element.lane.equals("candidate")) {
            CollisionPolicyRow policy = state.candidateCollisionPolicy.get(
                    internalName + "\u0000" + element.position);
            if (policy != null && policy.matches(collision)) {
                policy.seen = true;
                collision.policyMatched = true;
            } else {
                state.violation("runtime-collision-policy", "candidate",
                        internalName,
                        policy == null
                                ? "missing-candidate-collision-policy"
                                : "candidate-collision-policy-mismatch",
                        policy == null
                                ? "candidate collision has no exact policy row"
                                : "candidate collision differs from its policy row");
            }
        }
    }

    private static void recordRuntimeShadow(RuntimeElement element,
            State state, String internalName, String entry, String origin,
            String sha, Set<String> ownedNames,
            Map<String, OverlapProof> overlapProofs) {
        if (!ownedNames.contains(internalName)) return;
        OverlapProof proof = overlapProofs.get(internalName);
        require(proof != null,
                "runtime dependency has an undeclared/unproved owned shadow "
                        + internalName + " in " + element.path);
        element.ownedShadows++;
        String candidateA = element.lane.equals("candidate")
                ? proof.candidateADisposition() : "NOT_APPLICABLE";
        String candidateB = element.lane.equals("candidate")
                ? proof.candidateBDisposition() : "NOT_APPLICABLE";
        String oracle = element.lane.equals("oracle")
                ? "CHILD_FIRST_OWNED" : "NOT_APPLICABLE";
        state.runtimeShadows.add(new RuntimeShadow(element.lane,
                element.position, element.role, internalName, entry, origin,
                sha, candidateA, candidateB, oracle));
    }

    /* A separate envelope walk makes ASM's otherwise permissive parser reject
     * unknown constant-pool tags, truncation, and trailing payloads. */
    private static final class ClassCursor {
        final byte[] data;
        int position;
        ClassCursor(byte[] data) { this.data = data; }
        int u1() {
            require(position < data.length, "truncated class file");
            return data[position++] & 0xff;
        }
        int u2() { return (u1() << 8) | u1(); }
        long u4() { return ((long) u2() << 16) | u2(); }
        void skip(long count) {
            require(count >= 0 && count <= Integer.MAX_VALUE
                            && position <= data.length - (int) count,
                    "truncated or oversized class-file structure");
            position += (int) count;
        }
    }

    private static void skipAttributes(ClassCursor cursor) {
        int count = cursor.u2();
        for (int index = 0; index < count; index++) {
            cursor.u2();
            cursor.skip(cursor.u4());
        }
    }

    private static void validateClassEnvelope(byte[] bytes) {
        ClassCursor cursor = new ClassCursor(bytes);
        require(cursor.u4() == 0xcafebabeL, "invalid class-file magic");
        cursor.u2();
        cursor.u2();
        int constants = cursor.u2();
        for (int index = 1; index < constants; index++) {
            int tag = cursor.u1();
            switch (tag) {
                case 1: cursor.skip(cursor.u2()); break;
                case 3: case 4: cursor.skip(4); break;
                case 5: case 6: cursor.skip(8); index++; break;
                case 7: case 8: case 16: case 19: case 20:
                    cursor.skip(2); break;
                case 9: case 10: case 11: case 12: case 17: case 18:
                    cursor.skip(4); break;
                case 15: cursor.skip(3); break;
                default: throw new Failure("unknown constant-pool tag " + tag);
            }
        }
        cursor.skip(6);
        cursor.skip((long) cursor.u2() * 2L);
        int fields = cursor.u2();
        for (int index = 0; index < fields; index++) {
            cursor.skip(6);
            skipAttributes(cursor);
        }
        int methods = cursor.u2();
        for (int index = 0; index < methods; index++) {
            cursor.skip(6);
            skipAttributes(cursor);
        }
        skipAttributes(cursor);
        require(cursor.position == bytes.length,
                "class file contains trailing bytes: parsed=" + cursor.position
                        + " size=" + bytes.length);
    }

    private static List<Object> frameValues(Object[] values, int count) {
        List<Object> result = new ArrayList<>();
        for (int index = 0; index < count; index++) {
            Object value = values[index];
            require(value instanceof Integer || value instanceof String
                            || value instanceof Label,
                    "unknown expanded-frame value "
                            + (value == null ? "null"
                                    : value.getClass().getName()));
            result.add(value);
        }
        return result;
    }

    private static final class OffsetClassReader extends ClassReader {
        OffsetClassReader(byte[] bytes) { super(bytes); }

        @Override protected Label readLabel(int bytecodeOffset,
                Label[] labels) {
            Label label = super.readLabel(bytecodeOffset, labels);
            if (label.info == null) label.info = Integer.valueOf(bytecodeOffset);
            else require(label.info.equals(Integer.valueOf(bytecodeOffset)),
                    "one ASM label was assigned two bytecode offsets");
            return label;
        }
    }

    private static int labelOffset(Label label) {
        if (label.info instanceof Integer) return (Integer) label.info;
        try { return label.getOffset(); }
        catch (IllegalStateException unresolved) {
            throw new Failure("unresolved frame label", unresolved);
        }
    }

    private static void validateMethodCallbacks(ClassBytes owner,
            MethodModel method, State state) {
        boolean complete;
        if (method.abstractOrNative()) {
            complete = !method.codeVisited && !method.maxsVisited
                    && method.endVisited && method.frames.isEmpty();
        } else {
            complete = method.codeVisited && method.maxsVisited
                    && method.endVisited && method.maxStack >= 0
                    && method.maxLocals >= 0;
        }
        require(complete, "incomplete method callback ledger for "
                + owner.universe + ":" + owner.internalName + "."
                + method.name + method.descriptor);
        state.methodsRecorded++;
    }

    private static void parseClass(ClassBytes owner, State state) {
        validateClassEnvelope(owner.bytes);
        ClassModel model = new ClassModel(owner.internalName);
        ClassVisitor visitor = new ClassVisitor(ASM) {
            @Override public void visit(int version, int access, String name,
                    String signature, String superName, String[] interfaces) {
                require(!model.headerVisited, "duplicate class header callback");
                require(owner.internalName.equals(name),
                        "class header identity changed while parsing "
                                + owner.internalName);
                model.headerVisited = true;
            }

            @Override public void visitAttribute(Attribute attribute) {
                throw new Failure("unknown class attribute " + attribute.type
                        + " in " + owner.internalName);
            }

            @Override public FieldVisitor visitField(int access, String name,
                    String descriptor, String signature, Object value) {
                return new FieldVisitor(ASM) {
                    @Override public void visitAttribute(Attribute attribute) {
                        throw new Failure("unknown field attribute "
                                + attribute.type + " in " + owner.internalName
                                + "." + name + descriptor);
                    }
                };
            }

            @Override public RecordComponentVisitor visitRecordComponent(
                    String name, String descriptor, String signature) {
                return new RecordComponentVisitor(ASM) {
                    @Override public void visitAttribute(Attribute attribute) {
                        throw new Failure("unknown record-component attribute "
                                + attribute.type + " in " + owner.internalName
                                + "." + name + descriptor);
                    }
                };
            }

            @Override public MethodVisitor visitMethod(int access, String name,
                    String descriptor, String signature, String[] exceptions) {
                MethodModel method = new MethodModel(name, descriptor, access);
                require(model.methods.put(method.key(), method) == null,
                        "duplicate method identity " + owner.internalName + "."
                                + name + descriptor);
                state.methodsExpected++;
                return new MethodVisitor(ASM) {
                    @Override public void visitCode() {
                        require(!method.codeVisited,
                                "duplicate visitCode callback for " + name
                                        + descriptor);
                        method.codeVisited = true;
                    }

                    @Override public void visitLabel(Label label) {
                        method.currentOffset = labelOffset(label);
                    }

                    @Override public void visitFrame(int type, int numLocal,
                            Object[] local, int numStack, Object[] stack) {
                        require(type == Opcodes.F_NEW,
                                "non-expanded frame callback for " + name
                                        + descriptor + ": " + type);
                        require(method.codeVisited && method.currentOffset >= 0,
                                "frame lacks a resolved code boundary for "
                                        + name + descriptor);
                        state.framesExpected++;
                        method.frames.add(new FrameModel(method.frames.size(),
                                method.currentOffset,
                                frameValues(local, numLocal),
                                frameValues(stack, numStack)));
                        state.framesRecorded++;
                    }

                    @Override public void visitMaxs(int maxStack,
                            int maxLocals) {
                        require(!method.maxsVisited,
                                "duplicate visitMaxs callback for " + name
                                        + descriptor);
                        method.maxsVisited = true;
                        method.maxStack = maxStack;
                        method.maxLocals = maxLocals;
                    }

                    @Override public void visitAttribute(Attribute attribute) {
                        throw new Failure("unknown method/code attribute "
                                + attribute.type + " in " + owner.internalName
                                + "." + name + descriptor);
                    }

                    @Override public void visitEnd() {
                        require(!method.endVisited,
                                "duplicate method end callback for " + name
                                        + descriptor);
                        method.endVisited = true;
                    }
                };
            }

            @Override public void visitEnd() {
                require(!model.endVisited, "duplicate class end callback");
                model.endVisited = true;
            }
        };
        try {
            new OffsetClassReader(owner.bytes).accept(visitor,
                    ClassReader.EXPAND_FRAMES);
        } catch (Failure failure) {
            throw failure;
        } catch (Throwable failure) {
            throw new Failure("ASM parse failed for " + owner.universe + ":"
                    + owner.internalName + ": " + failure, failure);
        }
        require(model.headerVisited && model.endVisited,
                "incomplete class callback ledger for " + owner.internalName);
        for (MethodModel method : model.methods.values()) {
            validateMethodCallbacks(owner, method, state);
        }
        owner.model = model;
    }

    private static void parseClasses(Map<String, ClassBytes> classes,
            State state) {
        for (ClassBytes bytes : classes.values()) parseClass(bytes, state);
    }

    private static Map<String, ClassBytes> relationLeft(String relation,
            State state) {
        if (relation.equals("candidate-a--candidate-b")
                || relation.equals("candidate-a--original")) {
            return state.candidateA;
        }
        if (relation.equals("candidate-b--original")) return state.candidateB;
        throw new Failure("unknown relation " + relation);
    }

    private static Map<String, ClassBytes> relationRight(String relation,
            State state) {
        if (relation.equals("candidate-a--candidate-b")) {
            return state.candidateB;
        }
        if (relation.equals("candidate-a--original")
                || relation.equals("candidate-b--original")) {
            return state.original;
        }
        throw new Failure("unknown relation " + relation);
    }

    private static Map<String, String> validatedClassMap(String relation,
            State state) {
        Map<String, ClassBytes> leftClasses = relationLeft(relation, state);
        Map<String, ClassBytes> rightClasses = relationRight(relation, state);
        Map<String, String> result = new TreeMap<>();
        Set<String> rightSeen = new HashSet<>();
        for (MappingRow row : state.mappings.get(relation)) {
            require(leftClasses.containsKey(row.left), "mapping left class is "
                    + "absent in " + relation + ": " + row.left);
            require(rightClasses.containsKey(row.right), "mapping right class is "
                    + "absent in " + relation + ": " + row.right);
            require(leftClasses.get(row.left).namespace.equals(row.namespace)
                            && rightClasses.get(row.right).namespace
                                    .equals(row.namespace),
                    "mapping namespace disagrees with class ownership in "
                            + relation + ": " + row.left + " -> " + row.right);
            require(row.identity.equals("true") || row.identity.equals("false"),
                    "mapping identity is not boolean in " + relation);
            require(Boolean.parseBoolean(row.identity)
                            == row.left.equals(row.right),
                    "mapping identity flag is false evidence in " + relation
                            + ": " + row.left + " -> " + row.right);
            require(result.put(row.left, row.right) == null,
                    "duplicate mapping left class in " + relation + ": "
                            + row.left);
            require(rightSeen.add(row.right),
                    "duplicate mapping right class in " + relation + ": "
                            + row.right);
        }
        require(result.keySet().equals(leftClasses.keySet()),
                "mapping left coverage differs in " + relation + ": missing="
                        + difference(leftClasses.keySet(), result.keySet())
                        + " extra=" + difference(result.keySet(),
                                leftClasses.keySet()));
        require(rightSeen.equals(rightClasses.keySet()),
                "mapping right coverage differs in " + relation + ": missing="
                        + difference(rightClasses.keySet(), rightSeen)
                        + " extra=" + difference(rightSeen,
                                rightClasses.keySet()));
        return result;
    }

    private static Map<String, OverlapProof> runtimeOverlapProofs(State state) {
        validatedClassMap("candidate-a--candidate-b", state);
        Map<String, String> ao = validatedClassMap(
                "candidate-a--original", state);
        Map<String, String> bo = validatedClassMap(
                "candidate-b--original", state);
        Map<String, String> originalToA = new TreeMap<>();
        for (Map.Entry<String, String> entry : ao.entrySet()) {
            require(originalToA.put(entry.getValue(), entry.getKey()) == null,
                    "candidate-a/original mapping is not invertible");
        }
        Map<String, String> originalToB = new TreeMap<>();
        for (Map.Entry<String, String> entry : bo.entrySet()) {
            require(originalToB.put(entry.getValue(), entry.getKey()) == null,
                    "candidate-b/original mapping is not invertible");
        }
        Map<String, OverlapProof> result = new TreeMap<>();
        for (String original : state.original.keySet()) {
            require(originalToA.containsKey(original)
                            && originalToB.containsKey(original),
                    "original overlap lacks complete candidate mappings: "
                            + original);
            result.put(original, new OverlapProof(original,
                    originalToA.get(original), originalToB.get(original)));
        }
        return result;
    }

    private static Type mappedType(Type type, Map<String, String> classMap) {
        switch (type.getSort()) {
            case Type.OBJECT:
                return Type.getObjectType(classMap.getOrDefault(
                        type.getInternalName(), type.getInternalName()));
            case Type.ARRAY: {
                Type element = mappedType(type.getElementType(), classMap);
                StringBuilder descriptor = new StringBuilder();
                for (int index = 0; index < type.getDimensions(); index++) {
                    descriptor.append('[');
                }
                descriptor.append(element.getDescriptor());
                return Type.getType(descriptor.toString());
            }
            case Type.METHOD: {
                Type[] arguments = type.getArgumentTypes();
                for (int index = 0; index < arguments.length; index++) {
                    arguments[index] = mappedType(arguments[index], classMap);
                }
                return Type.getMethodType(mappedType(type.getReturnType(),
                        classMap), arguments);
            }
            default: return type;
        }
    }

    private static String mappedDescriptor(String descriptor,
            Map<String, String> classMap) {
        try { return mappedType(Type.getType(descriptor), classMap)
                .getDescriptor(); }
        catch (IllegalArgumentException bad) {
            throw new Failure("invalid method descriptor " + descriptor, bad);
        }
    }

    private static String frameValue(Object value,
            Map<String, String> classMap) {
        if (value instanceof Integer) return "I:" + value;
        if (value instanceof Label) return "U@" + labelOffset((Label) value);
        require(value instanceof String, "unknown frame value");
        String name = (String) value;
        if (name.startsWith("[")) {
            return "T:" + mappedDescriptor(name, classMap);
        }
        return "T:" + classMap.getOrDefault(name, name);
    }

    private static String frameSide(FrameModel frame,
            Map<String, String> classMap) {
        List<String> locals = new ArrayList<>();
        for (Object value : frame.locals) locals.add(frameValue(value, classMap));
        List<String> stack = new ArrayList<>();
        for (Object value : frame.stack) stack.add(frameValue(value, classMap));
        return frame.offset + "|L=" + String.join(",", locals)
                + "|S=" + String.join(",", stack);
    }

    private static void compareRelations(State state) {
        state.relationExpected = 0;
        for (String relation : RELATIONS) {
            for (ClassBytes bytes : relationLeft(relation, state).values()) {
                state.relationExpected += bytes.model.methods.size();
            }
        }
        for (String relation : RELATIONS) {
            Map<String, ClassBytes> leftClasses = relationLeft(relation, state);
            Map<String, ClassBytes> rightClasses = relationRight(relation, state);
            Map<String, String> classMap = validatedClassMap(relation, state);
            for (Map.Entry<String, String> classPair : classMap.entrySet()) {
                ClassBytes leftClass = leftClasses.get(classPair.getKey());
                ClassBytes rightClass = rightClasses.get(classPair.getValue());
                Set<String> rightCovered = new HashSet<>();
                for (MethodModel left : leftClass.model.methods.values()) {
                    RelationResult result = new RelationResult(relation,
                            leftClass.namespace, leftClass.internalName,
                            rightClass.internalName, left.name,
                            left.descriptor);
                    state.relations.add(result);
                    state.relationRecorded++;
                    String rightDescriptor = mappedDescriptor(left.descriptor,
                            classMap);
                    result.rightMethod = left.name;
                    result.rightDescriptor = rightDescriptor;
                    result.leftMaxStack = left.maxStack;
                    result.leftMaxLocals = left.maxLocals;
                    result.leftFrames = left.frames.size();
                    MethodModel right = rightClass.model.methods.get(left.name
                            + "\u0000" + rightDescriptor);
                    if (right == null) {
                        result.detail = "mapped method is absent";
                        state.violation("relation", relation,
                                leftClass.internalName + "." + left.name
                                        + left.descriptor,
                                "unproved-method-identity", result.detail);
                        continue;
                    }
                    rightCovered.add(right.key());
                    result.rightMaxStack = right.maxStack;
                    result.rightMaxLocals = right.maxLocals;
                    result.rightFrames = right.frames.size();
                    List<String> differences = new ArrayList<>();
                    if (left.access != right.access) differences.add("access");
                    if (left.codeVisited != right.codeVisited)
                        differences.add("code-presence");
                    if (left.maxStack != right.maxStack)
                        differences.add("max-stack");
                    if (left.maxLocals != right.maxLocals)
                        differences.add("max-locals");
                    if (left.frames.size() != right.frames.size()) {
                        differences.add("frame-count");
                    } else {
                        for (int index = 0; index < left.frames.size(); index++) {
                            String leftFrame = frameSide(left.frames.get(index),
                                    classMap);
                            String rightFrame = frameSide(
                                    right.frames.get(index),
                                    Collections.emptyMap());
                            if (!leftFrame.equals(rightFrame)) {
                                differences.add("frame-" + index);
                            }
                        }
                    }
                    if (differences.isEmpty()) {
                        result.status = "PASS";
                        result.detail = "strict exact maxima/frame relation";
                    } else {
                        result.detail = "strict differences: "
                                + String.join(",", differences);
                        state.violation("relation", relation,
                                leftClass.internalName + "." + left.name
                                        + left.descriptor,
                                "maxima-frame-difference", result.detail);
                    }
                }
                Set<String> extras = difference(
                        rightClass.model.methods.keySet(), rightCovered);
                for (String extra : extras) {
                    state.violation("relation", relation,
                            rightClass.internalName + "." + extra,
                            "unproved-method-identity",
                            "right method has no mapped left method");
                }
            }
        }
    }

    private static final class OwnedLoader extends URLClassLoader {
        private final String label;
        private final Map<String, byte[]> owned;
        private final Set<String> blocked;
        private final ProtectionDomain ownedDomain;

        OwnedLoader(String label, URL[] runtime, Map<String, ClassBytes> classes,
                    URL ownedOrigin, Set<String> blockedInternalNames) {
            super(runtime, ClassLoader.getPlatformClassLoader());
            this.label = label;
            this.owned = new HashMap<>();
            for (ClassBytes bytes : classes.values()) {
                this.owned.put(bytes.internalName.replace('/', '.'), bytes.bytes);
            }
            this.blocked = blockedInternalNames.stream()
                    .map(value -> value.replace('/', '.'))
                    .collect(Collectors.toUnmodifiableSet());
            this.ownedDomain = new ProtectionDomain(new CodeSource(ownedOrigin,
                    (Certificate[]) null), null, this, null);
        }

        String evidenceName() { return "owned-loader:" + label; }

        @Override protected Class<?> loadClass(String name, boolean resolve)
                throws ClassNotFoundException {
            synchronized (getClassLoadingLock(name)) {
                Class<?> loaded = findLoadedClass(name);
                if (loaded == null) {
                    byte[] bytes = owned.get(name);
                    if (bytes != null) {
                        loaded = defineClass(name, bytes, 0, bytes.length,
                                ownedDomain);
                    } else if (blocked.contains(name)) {
                        throw new ClassNotFoundException(
                                "blocked mapped original alias " + name);
                    } else {
                        loaded = super.loadClass(name, false);
                    }
                }
                if (resolve) resolveClass(loaded);
                return loaded;
            }
        }

        Class<?> loadAndResolve(String binaryName)
                throws ClassNotFoundException {
            return loadClass(binaryName, true);
        }
    }

    private static boolean verifyAllRequested() {
        boolean requested = false;
        for (String argument : ManagementFactory.getRuntimeMXBean()
                .getInputArguments()) {
            if (argument.equals("-Xverify:all")) requested = true;
            if (argument.equals("-noverify")
                    || argument.equals("-Xverify:none")
                    || argument.equals("-Xverify:remote")
                    || argument.equals("-XX:-BytecodeVerificationLocal")
                    || argument.equals("-XX:-BytecodeVerificationRemote")) {
                return false;
            }
        }
        return requested;
    }

    private static URL[] runtimeUrls(State state, String lane)
            throws IOException {
        List<RuntimeElement> elements = state.runtime.stream()
                .filter(value -> value.lane.equals(lane))
                .sorted(Comparator.comparingInt(value -> value.position))
                .collect(Collectors.toList());
        URL[] result = new URL[elements.size()];
        for (int index = 0; index < result.length; index++) {
            result[index] = elements.get(index).path.toUri().toURL();
        }
        return result;
    }

    private static String actualLoader(Class<?> loaded) {
        ClassLoader loader = loaded.getClassLoader();
        if (loader instanceof OwnedLoader) {
            return ((OwnedLoader) loader).evidenceName();
        }
        if (loader == null) return "bootstrap";
        return loader.getClass().getName();
    }

    private static String actualOrigin(Class<?> loaded) {
        ProtectionDomain domain = loaded.getProtectionDomain();
        if (domain == null || domain.getCodeSource() == null
                || domain.getCodeSource().getLocation() == null) return "";
        return domain.getCodeSource().getLocation().toExternalForm();
    }

    private static void verifyUniverse(String universe,
            Map<String, ClassBytes> classes, URL[] runtime, URL ownedOrigin,
            Set<String> blockedAliases, Set<String> presentBlockedAliases,
            List<RuntimeCollision> collisions, State state) {
        String expectedLoader = "owned-loader:" + universe;
        String expectedOrigin = ownedOrigin.toExternalForm();
        try (OwnedLoader loader = new OwnedLoader(universe, runtime, classes,
                ownedOrigin, blockedAliases)) {
            for (ClassBytes bytes : classes.values()) {
                VerificationResult result = new VerificationResult(universe,
                        bytes.internalName, expectedLoader, expectedOrigin);
                state.verifications.add(result);
                state.verificationRecorded++;
                try {
                    Class<?> loaded = loader.loadAndResolve(
                            bytes.internalName.replace('/', '.'));
                    /* Descriptor resolution is deliberately forced without
                     * initialization; it closes a common lazy-verification
                     * escape hatch while preserving Class.forName(false)
                     * semantics. */
                    loaded.getDeclaredConstructors();
                    loaded.getDeclaredMethods();
                    loaded.getDeclaredFields();
                    result.actualLoader = actualLoader(loaded);
                    result.actualOrigin = actualOrigin(loaded);
                    require(result.expectedLoader.equals(result.actualLoader),
                            "owned class escaped its isolated loader");
                    require(result.expectedOrigin.equals(result.actualOrigin),
                            "owned class code-source origin differs");
                    result.status = "PASS";
                    result.detail = "loaded, resolved, and reflected without initialization";
                } catch (Throwable failure) {
                    result.actualLoader = result.actualLoader.isEmpty()
                            ? expectedLoader : result.actualLoader;
                    result.detail = failure.getClass().getName() + ": "
                            + clean(failure.getMessage());
                    state.violation("verification", universe,
                            bytes.internalName, "jvm-verification-or-resolution",
                            result.detail);
                }
            }
            for (String alias : new TreeSet<>(presentBlockedAliases)) {
                AliasBlockResult result = new AliasBlockResult(universe, alias);
                state.aliasBlocks.add(result);
                state.aliasBlockRecorded++;
                try {
                    Class<?> escaped = loader.loadAndResolve(
                            alias.replace('/', '.'));
                    result.detail = "blocked alias loaded via "
                            + actualLoader(escaped) + " from "
                            + actualOrigin(escaped);
                    state.violation("blocked-alias", universe, alias,
                            "original-alias-loaded", result.detail);
                } catch (ClassNotFoundException expected) {
                    result.status = "PASS";
                    result.detail = "blocked before parent/runtime URL lookup";
                } catch (Throwable failure) {
                    result.detail = failure.getClass().getName() + ": "
                            + clean(failure.getMessage());
                    state.violation("blocked-alias", universe, alias,
                            "alias-block-unproved", result.detail);
                }
            }
            for (RuntimeCollision collision : collisions) {
                state.collisionRecorded++;
                try {
                    require(!collision.lane.equals("candidate")
                                    || collision.policyMatched,
                            "candidate collision is not policy-bound");
                    Class<?> selected = loader.loadAndResolve(
                            collision.internalName.replace('/', '.'));
                    selected.getDeclaredConstructors();
                    selected.getDeclaredMethods();
                    selected.getDeclaredFields();
                    String actual = actualOrigin(selected);
                    require(actualLoader(selected).equals(expectedLoader),
                            "ordered collision escaped its isolated loader");
                    require(actual.equals(collision.first.codeSource),
                            "ordered collision selected " + actual
                                    + " instead of "
                                    + collision.first.codeSource);
                    collision.status = "PASS";
                    collision.detail = "resolved without initialization to first origin";
                } catch (Throwable failure) {
                    collision.detail = failure.getClass().getName() + ": "
                            + clean(failure.getMessage());
                    state.violation("runtime-collision", collision.lane,
                            collision.internalName, "first-wins-unproved",
                            collision.detail);
                }
            }
        } catch (IOException closeFailure) {
            state.violation("verification", universe, "<loader>",
                    "loader-close", closeFailure.toString());
        }
    }

    private static void verifyOwnedClasses(Config config, State state)
            throws IOException {
        require(verifyAllRequested(),
                "normal verifier mode requires the exact JVM option -Xverify:all");
        URL[] candidateRuntime = runtimeUrls(state, "candidate");
        URL[] oracleDependencies = runtimeUrls(state, "oracle");
        URL[] oracleRuntime = new URL[oracleDependencies.length + 1];
        oracleRuntime[0] = config.originalJar.toUri().toURL();
        System.arraycopy(oracleDependencies, 0, oracleRuntime, 1,
                oracleDependencies.length);
        state.verificationExpected = (long) state.candidateA.size()
                + state.candidateB.size() + state.original.size();
        state.collisionExpected = state.runtimeCollisions.size();
        Map<String, OverlapProof> proofs = runtimeOverlapProofs(state);
        Set<String> blockedA = proofs.values().stream()
                .filter(value -> !value.originalName.equals(value.candidateAName))
                .map(value -> value.originalName).collect(Collectors.toSet());
        Set<String> blockedB = proofs.values().stream()
                .filter(value -> !value.originalName.equals(value.candidateBName))
                .map(value -> value.originalName).collect(Collectors.toSet());
        Set<String> presentBlockedA = state.runtimeShadows.stream()
                .filter(value -> value.lane.equals("candidate")
                        && value.candidateADisposition
                                .equals("BLOCKED_ORIGINAL_ALIAS"))
                .map(value -> value.internalName).collect(Collectors.toSet());
        Set<String> presentBlockedB = state.runtimeShadows.stream()
                .filter(value -> value.lane.equals("candidate")
                        && value.candidateBDisposition
                                .equals("BLOCKED_ORIGINAL_ALIAS"))
                .map(value -> value.internalName).collect(Collectors.toSet());
        state.aliasBlockExpected = (long) presentBlockedA.size()
                + presentBlockedB.size();
        List<RuntimeCollision> candidateCollisions = state.runtimeCollisions
                .stream().filter(value -> value.lane.equals("candidate"))
                .collect(Collectors.toList());
        List<RuntimeCollision> oracleCollisions = state.runtimeCollisions
                .stream().filter(value -> value.lane.equals("oracle"))
                .collect(Collectors.toList());
        verifyUniverse("candidate-a", state.candidateA, candidateRuntime,
                config.candidateA.toUri().toURL(), blockedA, presentBlockedA,
                candidateCollisions, state);
        verifyUniverse("candidate-b", state.candidateB, candidateRuntime,
                config.candidateB.toUri().toURL(), blockedB, presentBlockedB,
                Collections.emptyList(), state);
        verifyUniverse("original", state.original, oracleRuntime,
                config.originalJar.toUri().toURL(), Collections.emptySet(),
                Collections.emptySet(), oracleCollisions, state);
    }

    private static boolean ownedSelectionProved(String universe,
            String internalName, State state) {
        return state.verifications.stream().anyMatch(value ->
                value.universe.equals(universe)
                && value.internalName.equals(internalName)
                && value.status.equals("PASS")
                && value.expectedLoader.equals(value.actualLoader)
                && value.expectedOrigin.equals(value.actualOrigin));
    }

    private static boolean aliasBlockProved(String universe,
            String internalName, State state) {
        return state.aliasBlocks.stream().anyMatch(value ->
                value.universe.equals(universe)
                && value.internalName.equals(internalName)
                && value.status.equals("PASS"));
    }

    private static boolean dispositionProved(String universe,
            String disposition, String internalName, State state) {
        if (disposition.equals("NOT_APPLICABLE")) return true;
        if (disposition.equals("CHILD_FIRST_OWNED")) {
            return ownedSelectionProved(universe, internalName, state);
        }
        if (disposition.equals("BLOCKED_ORIGINAL_ALIAS")) {
            return aliasBlockProved(universe, internalName, state);
        }
        return false;
    }

    private static boolean shadowSelectionProved(RuntimeShadow shadow,
            State state) {
        return dispositionProved("candidate-a",
                        shadow.candidateADisposition, shadow.internalName, state)
                && dispositionProved("candidate-b",
                        shadow.candidateBDisposition, shadow.internalName, state)
                && dispositionProved("original", shadow.oracleDisposition,
                        shadow.internalName, state);
    }

    private static void validateShadowSelections(State state) {
        for (RuntimeShadow shadow : state.runtimeShadows) {
            if (!shadowSelectionProved(shadow, state)) {
                state.violation("runtime-shadow", shadow.lane,
                        shadow.element, "child-first-selection-unproved",
                        shadow.internalName);
            }
        }
    }

    private static void prepareOutput(Path output) throws IOException {
        Path absolute = absolute(output);
        if (Files.exists(absolute, LinkOption.NOFOLLOW_LINKS)) {
            require(!Files.isSymbolicLink(absolute)
                            && Files.isDirectory(absolute,
                                    LinkOption.NOFOLLOW_LINKS),
                    "output exists but is not a real directory: " + absolute);
            try (Stream<Path> contents = Files.list(absolute)) {
                require(!contents.findAny().isPresent(),
                        "refusing to overwrite nonempty output: " + absolute);
            }
            canonicalDirectory(absolute);
        } else {
            Files.createDirectories(absolute);
            canonicalDirectory(absolute);
        }
    }

    private static Path prospectiveCanonical(Path path) throws IOException {
        Path absolute = absolute(path);
        List<String> missing = new ArrayList<>();
        Path cursor = absolute;
        while (!Files.exists(cursor, LinkOption.NOFOLLOW_LINKS)) {
            require(cursor.getFileName() != null,
                    "cannot resolve prospective output path " + absolute);
            missing.add(cursor.getFileName().toString());
            cursor = cursor.getParent();
        }
        require(!Files.isSymbolicLink(cursor),
                "prospective output has a symlink leaf/ancestor " + cursor);
        Path result = cursor.toRealPath();
        for (int index = missing.size() - 1; index >= 0; index--) {
            result = result.resolve(missing.get(index));
        }
        return result.normalize();
    }

    private static void preflightOutput(Config config, State state)
            throws IOException {
        Path output = prospectiveCanonical(config.output);
        List<Path> inputs = new ArrayList<>(List.of(config.candidateA,
                config.candidateB, config.originalJar, config.cohort,
                config.ownership, config.classMappings, config.runtimePolicy,
                config.candidateRuntimeLedger, config.oracleRuntimeLedger,
                config.candidateCollisionPolicy));
        for (RuntimeElement element : state.runtime) inputs.add(element.path);
        for (Path input : inputs) {
            Path lexical = absolute(input);
            require(!overlaps(output, lexical),
                    "prospective output overlaps input: output=" + output
                            + " input=" + lexical);
            require(Files.exists(lexical, LinkOption.NOFOLLOW_LINKS),
                    "preflight input is absent: " + lexical);
            Path real = lexical.toRealPath();
            require(!overlaps(output, real),
                    "prospective output overlaps canonical input: output="
                            + output + " input=" + real);
        }
    }

    private static void bindInput(State state, String name, Path origin,
            String hash) {
        require(state.inputOrigins.put(name, origin.toString()) == null,
                "duplicate bound input name " + name);
        state.inputPre.put(name, hash);
    }

    private static void bindPolicyAndOwnedInputs(Config config, State state)
            throws IOException {
        bindInput(state, "policy:cohort", config.cohort,
                sha256(config.cohort));
        bindInput(state, "policy:ownership", config.ownership,
                sha256(config.ownership));
        bindInput(state, "policy:class-mappings", config.classMappings,
                sha256(config.classMappings));
        bindInput(state, "policy:runtime-policy", config.runtimePolicy,
                sha256(config.runtimePolicy));
        bindInput(state, "policy:candidate-runtime-ledger",
                config.candidateRuntimeLedger,
                sha256(config.candidateRuntimeLedger));
        bindInput(state, "policy:oracle-runtime-ledger",
                config.oracleRuntimeLedger, sha256(config.oracleRuntimeLedger));
        bindInput(state, "policy:candidate-collision-policy",
                config.candidateCollisionPolicy,
                sha256(config.candidateCollisionPolicy));
        bindInput(state, "owned:candidate-a", config.candidateA,
                directoryManifest(config.candidateA));
        bindInput(state, "owned:candidate-b", config.candidateB,
                directoryManifest(config.candidateB));
        bindInput(state, "owned:original", config.originalJar,
                sha256(config.originalJar));
    }

    private static void bindRuntimeInputs(State state) throws IOException {
        for (RuntimeElement element : state.runtime) {
            bindInput(state, "runtime:" + element.lane + ":"
                    + element.position + ":" + element.role, element.path,
                    element.preSha);
        }
    }

    private static String currentHash(Path path) throws IOException {
        return Files.isDirectory(path, LinkOption.NOFOLLOW_LINKS)
                ? directoryManifest(path) : sha256(path);
    }

    private static void capturePostHashes(State state) {
        for (Map.Entry<String, String> input : state.inputOrigins.entrySet()) {
            try {
                state.inputPost.put(input.getKey(),
                        currentHash(Path.of(input.getValue())));
            } catch (Throwable failure) {
                state.inputPost.put(input.getKey(), "");
                state.violation("input", "postflight", input.getValue(),
                        "post-hash-failed", failure.toString());
            }
        }
        for (RuntimeElement element : state.runtime) {
            element.postSha = state.inputPost.getOrDefault("runtime:"
                    + element.lane + ":" + element.position + ":"
                    + element.role, "");
        }
        for (String name : state.inputPre.keySet()) {
            String pre = state.inputPre.get(name);
            String post = state.inputPost.getOrDefault(name, "");
            if (!pre.equals(post)) {
                state.violation("input", "postflight",
                        state.inputOrigins.get(name), "input-mutated",
                        "pre=" + pre + " post=" + post);
            }
        }
    }

    private static void validateOrigins(Config config, State state)
            throws IOException {
        canonicalDirectory(config.candidateA);
        canonicalDirectory(config.candidateB);
        canonicalFile(config.originalJar);
        canonicalFile(config.cohort);
        canonicalFile(config.ownership);
        canonicalFile(config.classMappings);
        canonicalFile(config.runtimePolicy);
        canonicalFile(config.candidateRuntimeLedger);
        canonicalFile(config.oracleRuntimeLedger);
        canonicalFile(config.candidateCollisionPolicy);
        require(!config.candidateRuntimeLedger.equals(config.oracleRuntimeLedger),
                "candidate and oracle runtime ledgers must be distinct inputs");
        require(!overlaps(config.candidateA, config.candidateB),
                "candidate roots overlap");
        require(!overlaps(config.candidateA, config.originalJar)
                        && !overlaps(config.candidateB, config.originalJar),
                "candidate root overlaps the licensed original");
        String originalHash = sha256(config.originalJar);
        Map<String, Set<Path>> runtimePaths = new HashMap<>();
        runtimePaths.put("candidate", new HashSet<>());
        runtimePaths.put("oracle", new HashSet<>());
        /* Hash and bind the entire ordered boundary before enforcing any
         * per-element policy, so a first-element failure cannot suppress the
         * rest of the input ledger. */
        for (RuntimeElement element : state.runtime) {
            if (element.kind.equals("directory")) {
                canonicalDirectory(element.path);
            } else {
                canonicalFile(element.path);
            }
            element.preSha = currentHash(element.path);
        }
        bindRuntimeInputs(state);
        for (RuntimeElement element : state.runtime) {
            require(runtimePaths.get(element.lane).add(element.path),
                    "duplicate " + element.lane + " runtime dependency path "
                            + element.path);
            require(!overlaps(element.path, config.candidateA)
                            && !overlaps(element.path, config.candidateB)
                            && !overlaps(element.path, config.output),
                    "runtime dependency overlaps owned input/output: "
                            + element.path);
            String actual = element.preSha;
            require(!overlaps(element.path, config.originalJar),
                    "runtime ledger redundantly contains the separately owned original: "
                            + element.path);
            require(!actual.equals(originalHash),
                    "runtime ledger contains a renamed copy of the separately owned original: "
                            + element.path);
        }
    }

    private static void validateRuntimePolicy(Config config, State state)
            throws IOException {
        state.productionShapeRequired = config.requireProductionShape;
        long candidateElements = state.runtime.stream()
                .filter(value -> value.lane.equals("candidate")).count();
        long oracleElements = state.runtime.stream()
                .filter(value -> value.lane.equals("oracle")).count();
        require(candidateElements == state.expectedCandidateRuntimeElements
                        && oracleElements
                                == state.expectedOracleRuntimeElements,
                "runtime lane cardinality differs from policy: candidate="
                        + candidateElements + "/"
                        + state.expectedCandidateRuntimeElements + " oracle="
                        + oracleElements + "/"
                        + state.expectedOracleRuntimeElements);
        if (config.requireProductionShape) {
            require(state.expectedCandidateRuntimeElements == 532
                            && state.expectedOracleRuntimeElements == 533,
                    "normal mode requires exactly 532 candidate and 533 oracle runtime elements");
        }
        require(sha256(config.candidateRuntimeLedger)
                        .equals(state.expectedCandidateLedgerSha)
                        && sha256(config.oracleRuntimeLedger)
                                .equals(state.expectedOracleLedgerSha),
                "ordered runtime ledger bytes differ from policy anchors");
        ArtifactPolicy transactor = state.runtimePolicy.get(
                "transactor-original");
        require(sha256(config.originalJar).equals(transactor.sha),
                "owned original does not match transactor-original policy hash");
        for (ArtifactPolicy policy : state.runtimePolicy.values()) {
            int candidateHashCount = 0;
            int candidateExactCount = 0;
            int oracleHashCount = 0;
            int oracleExactCount = 0;
            for (RuntimeElement element : state.runtime) {
                if (!element.preSha.equals(policy.sha)) continue;
                if (element.lane.equals("candidate")) {
                    candidateHashCount++;
                    if (element.role.equals(policy.candidateRole))
                        candidateExactCount++;
                } else {
                    oracleHashCount++;
                    if (element.role.equals(policy.oracleRole))
                        oracleExactCount++;
                }
            }
            if (policy.candidateDisposition.equals("FORBID")) {
                require(candidateHashCount == 0,
                        "candidate lane contains forbidden identity "
                                + policy.ruleId);
            } else if (policy.candidateDisposition.equals("REQUIRE")) {
                require(candidateHashCount == 1 && candidateExactCount == 1,
                        "candidate lane does not contain exactly one role/hash-bound "
                                + policy.ruleId);
            } else {
                require(policy.ruleId.equals("transactor-original")
                                && candidateHashCount == 0,
                        "unexpected candidate OWNED policy " + policy.ruleId);
            }
            if (policy.oracleDisposition.equals("FORBID")) {
                require(oracleHashCount == 0,
                        "oracle lane contains forbidden identity "
                                + policy.ruleId);
            } else if (policy.oracleDisposition.equals("REQUIRE")) {
                require(oracleHashCount == 1 && oracleExactCount == 1,
                        "oracle lane does not contain exactly one role/hash-bound "
                                + policy.ruleId);
            } else {
                require(policy.ruleId.equals("transactor-original")
                                && oracleHashCount == 0,
                        "unexpected oracle OWNED policy " + policy.ruleId);
            }
        }
        for (CollisionPolicyRow collision
                : state.candidateCollisionPolicy.values()) {
            RuntimeElement first = state.runtime.stream()
                    .filter(value -> value.lane.equals("candidate")
                            && value.position == collision.firstPosition)
                    .findFirst().orElse(null);
            RuntimeElement later = state.runtime.stream()
                    .filter(value -> value.lane.equals("candidate")
                            && value.position == collision.laterPosition)
                    .findFirst().orElse(null);
            require(first != null && later != null
                            && first.role.equals(collision.firstRole)
                            && later.role.equals(collision.laterRole),
                    "collision role/position policy spoof for "
                            + collision.internalName);
        }
        if (config.requireProductionShape) {
            long identical = state.candidateCollisionPolicy.values().stream()
                    .filter(value -> value.byteRelation.equals(
                            "BYTE_IDENTICAL")).count();
            long different = state.candidateCollisionPolicy.values().stream()
                    .filter(value -> value.byteRelation.equals(
                            "BYTE_DIFFERENT")).count();
            require(state.candidateCollisionPolicy.size() == 33
                            && identical == 14 && different == 19
                            && state.candidateCollisionPolicy.values().stream()
                                    .allMatch(value -> value.internalName
                                            .startsWith("jline/")),
                    "normal mode requires 33 jline collision rows: 14 byte-identical and 19 byte-different");
        }
    }

    private static void reconcileCandidateCollisionPolicy(State state) {
        long actual = state.runtimeCollisions.stream()
                .filter(value -> value.lane.equals("candidate")).count();
        for (CollisionPolicyRow policy
                : state.candidateCollisionPolicy.values()) {
            if (!policy.seen) {
                state.violation("runtime-collision-policy", "candidate",
                        policy.internalName,
                        "extra-or-unobserved-candidate-collision-policy",
                        "policy row did not match an observed candidate collision");
            }
        }
        long seen = state.candidateCollisionPolicy.values().stream()
                .filter(value -> value.seen).count();
        if (actual != seen || seen != state.candidateCollisionPolicy.size()) {
            state.violation("runtime-collision-policy", "candidate",
                    "<ledger>", "incomplete-candidate-collision-ledger",
                    "observed=" + actual + " matched=" + seen
                            + " policy="
                            + state.candidateCollisionPolicy.size());
        }
    }

    private static void setupAndRun(Config config, State state)
            throws IOException {
        bindPolicyAndOwnedInputs(config, state);
        readCohort(config.cohort, state);
        readOwnership(config.ownership, state);
        readMappings(config.classMappings, state);
        readCandidateCollisionPolicy(config.candidateCollisionPolicy, state);
        readRuntimePolicy(config.runtimePolicy, state);
        validateOrigins(config, state);
        validateRuntimePolicy(config, state);
        state.candidateA.putAll(indexCandidate(config.candidateA,
                "candidate-a", state));
        state.candidateB.putAll(indexCandidate(config.candidateB,
                "candidate-b", state));
        state.original.putAll(indexOriginal(config.originalJar, state));
        Set<String> forbiddenOwned = new HashSet<>();
        forbiddenOwned.addAll(state.candidateA.keySet());
        forbiddenOwned.addAll(state.candidateB.keySet());
        forbiddenOwned.addAll(state.original.keySet());
        Map<String, OverlapProof> overlapProofs = runtimeOverlapProofs(state);
        Map<String, RuntimeClassOrigin> candidateOwners = new HashMap<>();
        Map<String, RuntimeClassOrigin> oracleOwners = new HashMap<>(
                state.oracleRootClasses);
        for (RuntimeElement element : state.runtime) {
            Map<String, RuntimeClassOrigin> owners = element.lane.equals("candidate")
                    ? candidateOwners : oracleOwners;
            indexRuntimeClasses(element, state, owners, forbiddenOwned,
                    overlapProofs);
        }
        reconcileCandidateCollisionPolicy(state);
        parseClasses(state.candidateA, state);
        parseClasses(state.candidateB, state);
        parseClasses(state.original, state);
        compareRelations(state);
        verifyOwnedClasses(config, state);
        validateShadowSelections(state);
        state.setupComplete = true;
    }

    private static void writeLines(Path path, List<String> lines)
            throws IOException {
        String payload = String.join("\n", lines) + "\n";
        Files.writeString(path, payload, StandardCharsets.UTF_8,
                StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING,
                StandardOpenOption.WRITE);
    }

    private static List<ClassBytes> allClasses(State state) {
        List<ClassBytes> result = new ArrayList<>();
        result.addAll(state.candidateA.values());
        result.addAll(state.candidateB.values());
        result.addAll(state.original.values());
        result.sort(Comparator.comparing((ClassBytes value) -> value.universe)
                .thenComparing(value -> value.internalName));
        return result;
    }

    private static void writeSelfExcludingManifest(Path output,
            List<String> names) throws IOException {
        List<String> sorted = new ArrayList<>(names);
        Collections.sort(sorted);
        List<String> rows = new ArrayList<>();
        for (String name : sorted) {
            rows.add(sha256(output.resolve(name)) + "  " + name);
        }
        writeLines(output.resolve("manifest.sha256"), rows);
    }

    private static void writeEvidence(Path output, State state)
            throws IOException {
        List<String> inputs = new ArrayList<>();
        inputs.add("name\torigin\texpected_sha256\tpre_sha256"
                + "\tpost_sha256\tstatus");
        Map<String, String> runtimeExpected = new HashMap<>();
        for (RuntimeElement element : state.runtime) {
            runtimeExpected.put("runtime:" + element.lane + ":"
                    + element.position + ":" + element.role,
                    element.expectedSha);
        }
        List<String> inputNames = new ArrayList<>(state.inputPre.keySet());
        Collections.sort(inputNames);
        for (String name : inputNames) {
            String pre = state.inputPre.getOrDefault(name, "");
            String post = state.inputPost.getOrDefault(name, "");
            String expected = runtimeExpected.getOrDefault(name, pre);
            String status = !pre.isEmpty() && pre.equals(post)
                    && pre.equals(expected) ? "PASS" : "FAIL";
            inputs.add(clean(name) + "\t"
                    + clean(state.inputOrigins.getOrDefault(name, "")) + "\t"
                    + expected + "\t" + pre + "\t" + post + "\t" + status);
        }
        writeLines(output.resolve("inputs.tsv"), inputs);

        List<String> runtimeRows = new ArrayList<>();
        runtimeRows.add("lane\tposition\trole\tkind\tpath\texpected_sha256"
                + "\tpre_sha256\tpost_sha256\tclasses\towned_shadows"
                + "\tcollisions\tscanned\tstatus");
        List<RuntimeElement> runtimeElements = new ArrayList<>(state.runtime);
        runtimeElements.sort(Comparator
                .comparing((RuntimeElement value) -> value.lane)
                .thenComparingInt(value -> value.position));
        for (RuntimeElement element : runtimeElements) {
            String status = element.expectedSha.equals(element.preSha)
                    && element.preSha.equals(element.postSha) && element.scanned
                    ? "PASS" : "FAIL";
            runtimeRows.add(element.lane + "\t" + element.position + "\t"
                    + clean(element.role)
                    + "\t" + element.kind + "\t" + clean(element.path.toString())
                    + "\t" + element.expectedSha + "\t" + element.preSha
                    + "\t" + element.postSha + "\t" + element.classes
                    + "\t" + element.ownedShadows + "\t"
                    + element.collisions + "\t" + element.scanned + "\t"
                    + status);
        }
        writeLines(output.resolve("runtime-elements.tsv"), runtimeRows);

        List<RuntimeShadow> shadows = new ArrayList<>(state.runtimeShadows);
        shadows.sort(Comparator
                .comparing((RuntimeShadow value) -> value.lane)
                .thenComparingInt(value -> value.position)
                .thenComparing(value -> value.internalName)
                .thenComparing(value -> value.entry));
        List<String> shadowRows = new ArrayList<>();
        shadowRows.add("lane\tposition\trole\tinternal_name\tentry\torigin"
                + "\tclass_sha256\tcandidate_a_disposition"
                + "\tcandidate_b_disposition\toracle_disposition"
                + "\truntime_copy_selected\tstatus");
        for (RuntimeShadow shadow : shadows) {
            boolean proved = shadowSelectionProved(shadow, state);
            shadowRows.add(shadow.lane + "\t" + shadow.position + "\t"
                    + clean(shadow.role) + "\t" + clean(shadow.internalName)
                    + "\t" + clean(shadow.entry) + "\t"
                    + clean(shadow.element) + "\t" + shadow.sha
                    + "\t" + shadow.candidateADisposition + "\t"
                    + shadow.candidateBDisposition + "\t"
                    + shadow.oracleDisposition + "\tfalse\t"
                    + (proved ? "PASS" : "FAIL"));
        }
        writeLines(output.resolve("runtime-shadows.tsv"), shadowRows);

        List<AliasBlockResult> blockResults = new ArrayList<>(state.aliasBlocks);
        blockResults.sort(Comparator
                .comparing((AliasBlockResult value) -> value.universe)
                .thenComparing(value -> value.internalName));
        List<String> aliasRows = new ArrayList<>();
        aliasRows.add("universe\tinternal_name\tdisposition\tstatus\tdetail");
        for (AliasBlockResult result : blockResults) {
            aliasRows.add(result.universe + "\t" + clean(result.internalName)
                    + "\tBLOCKED_BEFORE_PARENT_OR_URL\t" + result.status
                    + "\t" + clean(result.detail));
        }
        writeLines(output.resolve("blocked-aliases.tsv"), aliasRows);

        List<RuntimeCollision> collisionResults = new ArrayList<>(
                state.runtimeCollisions);
        collisionResults.sort(Comparator
                .comparing((RuntimeCollision value) -> value.internalName)
                .thenComparingInt(value -> value.later.position)
                .thenComparing(value -> value.later.entry));
        List<String> collisionRows = new ArrayList<>();
        collisionRows.add("lane\tinternal_name\tfirst_position\tfirst_role"
                + "\tfirst_origin\tfirst_entry\tfirst_release\tfirst_sha256"
                + "\tfirst_code_source\tlater_position\tlater_role"
                + "\tlater_origin\tlater_entry\tlater_release"
                + "\tlater_sha256\tbyte_relation\tcandidate_policy\tstatus"
                + "\tdetail");
        for (RuntimeCollision collision : collisionResults) {
            collisionRows.add(collision.lane + "\t"
                    + clean(collision.internalName) + "\t"
                    + collision.first.position + "\t"
                    + clean(collision.first.role) + "\t"
                    + clean(collision.first.render()) + "\t"
                    + clean(collision.first.entry) + "\t"
                    + collision.first.release + "\t"
                    + collision.first.sha + "\t"
                    + clean(collision.first.codeSource) + "\t"
                    + collision.later.position + "\t"
                    + clean(collision.later.role) + "\t"
                    + clean(collision.later.render()) + "\t"
                    + clean(collision.later.entry) + "\t"
                    + collision.later.release + "\t"
                    + collision.later.sha + "\t"
                    + collision.byteRelation + "\t"
                    + (collision.lane.equals("candidate")
                            ? Boolean.toString(collision.policyMatched)
                            : "NOT_APPLICABLE") + "\t"
                    + collision.status + "\t" + clean(collision.detail));
        }
        writeLines(output.resolve("runtime-collisions.tsv"), collisionRows);

        List<String> classRows = new ArrayList<>();
        classRows.add("universe\tnamespace\tinternal_name\tlogical_path"
                + "\torigin\tbytes\tsha256\tmethods\tframes\tstatus");
        for (ClassBytes bytes : allClasses(state)) {
            int methods = bytes.model == null ? -1 : bytes.model.methods.size();
            int frames = bytes.model == null ? -1 : bytes.model.methods.values()
                    .stream().mapToInt(value -> value.frames.size()).sum();
            classRows.add(bytes.universe + "\t" + clean(bytes.namespace) + "\t"
                    + clean(bytes.internalName) + "\t"
                    + clean(bytes.logicalPath) + "\t" + clean(bytes.origin)
                    + "\t" + bytes.bytes.length + "\t" + bytes.sha + "\t"
                    + methods + "\t" + frames + "\t"
                    + (bytes.model == null ? "FAIL" : "PASS"));
        }
        writeLines(output.resolve("class-index.tsv"), classRows);

        List<String> maxima = new ArrayList<>();
        maxima.add("universe\tnamespace\tclass\tmethod\tdescriptor\taccess"
                + "\tcode\tmax_stack\tmax_locals\tframes\tstatus");
        List<String> frames = new ArrayList<>();
        frames.add("universe\tnamespace\tclass\tmethod\tdescriptor\tordinal"
                + "\toffset\tlocals\tstack\tstatus");
        for (ClassBytes bytes : allClasses(state)) {
            if (bytes.model == null) continue;
            for (MethodModel method : bytes.model.methods.values()) {
                maxima.add(bytes.universe + "\t" + clean(bytes.namespace) + "\t"
                        + clean(bytes.internalName) + "\t" + clean(method.name)
                        + "\t" + clean(method.descriptor) + "\t" + method.access
                        + "\t" + method.codeVisited + "\t" + method.maxStack
                        + "\t" + method.maxLocals + "\t" + method.frames.size()
                        + "\tPASS");
                for (FrameModel frame : method.frames) {
                    List<String> localValues = new ArrayList<>();
                    for (Object value : frame.locals) localValues.add(frameValue(
                            value, Collections.emptyMap()));
                    List<String> stackValues = new ArrayList<>();
                    for (Object value : frame.stack) stackValues.add(frameValue(
                            value, Collections.emptyMap()));
                    frames.add(bytes.universe + "\t" + clean(bytes.namespace)
                            + "\t" + clean(bytes.internalName) + "\t"
                            + clean(method.name) + "\t" + clean(method.descriptor)
                            + "\t" + frame.ordinal + "\t" + frame.offset + "\t"
                            + clean(String.join(",", localValues)) + "\t"
                            + clean(String.join(",", stackValues)) + "\tPASS");
                }
            }
        }
        writeLines(output.resolve("maxima.tsv"), maxima);
        writeLines(output.resolve("frames.tsv"), frames);

        List<RelationResult> relationResults = new ArrayList<>(state.relations);
        relationResults.sort(Comparator
                .comparing((RelationResult value) -> value.comparison)
                .thenComparing(value -> value.leftClass)
                .thenComparing(value -> value.leftMethod)
                .thenComparing(value -> value.leftDescriptor));
        List<String> relations = new ArrayList<>();
        relations.add("comparison\tnamespace\tleft_class\tright_class"
                + "\tleft_method\tleft_descriptor\tright_method"
                + "\tright_descriptor\tleft_max_stack\tright_max_stack"
                + "\tleft_max_locals\tright_max_locals\tleft_frames"
                + "\tright_frames\tstatus\tdetail");
        for (RelationResult result : relationResults) {
            relations.add(result.comparison + "\t" + clean(result.namespace)
                    + "\t" + clean(result.leftClass) + "\t"
                    + clean(result.rightClass) + "\t" + clean(result.leftMethod)
                    + "\t" + clean(result.leftDescriptor) + "\t"
                    + clean(result.rightMethod) + "\t"
                    + clean(result.rightDescriptor) + "\t"
                    + result.leftMaxStack + "\t" + result.rightMaxStack + "\t"
                    + result.leftMaxLocals + "\t" + result.rightMaxLocals
                    + "\t" + result.leftFrames + "\t" + result.rightFrames
                    + "\t" + result.status + "\t" + clean(result.detail));
        }
        writeLines(output.resolve("relations.tsv"), relations);

        List<VerificationResult> verificationResults = new ArrayList<>(
                state.verifications);
        verificationResults.sort(Comparator
                .comparing((VerificationResult value) -> value.universe)
                .thenComparing(value -> value.internalName));
        List<String> verifications = new ArrayList<>();
        verifications.add("universe\tinternal_name\texpected_loader"
                + "\tactual_loader\texpected_origin\tactual_origin"
                + "\tinitialized\tstatus\tdetail");
        for (VerificationResult result : verificationResults) {
            verifications.add(result.universe + "\t"
                    + clean(result.internalName) + "\t"
                    + clean(result.expectedLoader) + "\t"
                    + clean(result.actualLoader) + "\t"
                    + clean(result.expectedOrigin) + "\t"
                    + clean(result.actualOrigin) + "\t" + result.initialized
                    + "\t" + result.status + "\t" + clean(result.detail));
        }
        writeLines(output.resolve("verification.tsv"), verifications);

        List<Violation> violationsSorted = new ArrayList<>(state.violations);
        violationsSorted.sort(Comparator
                .comparing((Violation value) -> value.scope)
                .thenComparing(value -> value.universe)
                .thenComparing(value -> value.path)
                .thenComparing(value -> value.rule)
                .thenComparing(value -> value.detail));
        List<String> violations = new ArrayList<>();
        violations.add("scope\tuniverse\tpath\trule\tdetail");
        for (Violation violation : violationsSorted) {
            violations.add(clean(violation.scope) + "\t"
                    + clean(violation.universe) + "\t" + clean(violation.path)
                    + "\t" + clean(violation.rule) + "\t"
                    + clean(violation.detail));
        }
        writeLines(output.resolve("violations.tsv"), violations);

        boolean status = state.setupComplete && state.ledgerComplete
                && state.violations.isEmpty();
        List<String> summary = List.of(
                "metric\tvalue",
                "status\t" + (status ? "PASS" : "FAIL"),
                "scope\tstandalone exact-AOT JVM/maxima/frame gate",
                "stage.1.pass.claimed\tfalse",
                "production.cohort.run\tNOT_RUN",
                "wrapper.integration\tNOT_RUN",
                "jvm.verify.all.requested\t" + verifyAllRequested(),
                "loader.parent\tplatform",
                "loader.owned.policy\tchild-first owned names only",
                "loader.dependencies\tdisjoint candidate/oracle ordered hash-bound ledgers",
                "relation.policy\tstrict exact mapped method/access/code/maxima/expanded-frames",
                "cohort.namespaces\t" + state.cohort.size(),
                "candidate.a.classes\t" + state.candidateA.size(),
                "candidate.b.classes\t" + state.candidateB.size(),
                "original.classes\t" + state.original.size(),
                "oracle.root.classes.indexed\t"
                        + state.oracleRootClasses.size(),
                "runtime.elements\t" + state.runtime.size(),
                "runtime.candidate.elements\t" + state.runtime.stream()
                        .filter(value -> value.lane.equals("candidate")).count(),
                "runtime.oracle.elements\t" + state.runtime.stream()
                        .filter(value -> value.lane.equals("oracle")).count(),
                "runtime.production.shape.required\t"
                        + state.productionShapeRequired,
                "runtime.policy.candidate.elements\t"
                        + state.expectedCandidateRuntimeElements,
                "runtime.policy.oracle.elements\t"
                        + state.expectedOracleRuntimeElements,
                "runtime.policy.candidate.ledger.sha256\t"
                        + state.expectedCandidateLedgerSha,
                "runtime.policy.oracle.ledger.sha256\t"
                        + state.expectedOracleLedgerSha,
                "runtime.elements.scanned\t" + state.runtime.stream()
                        .filter(value -> value.scanned).count(),
                "runtime.owned.shadows\t" + state.runtimeShadows.size(),
                "blocked.aliases.expected\t" + state.aliasBlockExpected,
                "blocked.aliases.recorded\t" + state.aliasBlockRecorded,
                "runtime.collisions.expected\t" + state.collisionExpected,
                "runtime.collisions.recorded\t" + state.collisionRecorded,
                "runtime.candidate.collisions\t"
                        + state.runtimeCollisions.stream().filter(value ->
                                value.lane.equals("candidate")).count(),
                "runtime.candidate.collisions.byte-identical\t"
                        + state.runtimeCollisions.stream().filter(value ->
                                value.lane.equals("candidate")
                                && value.byteRelation.equals(
                                        "BYTE_IDENTICAL")).count(),
                "runtime.candidate.collisions.byte-different\t"
                        + state.runtimeCollisions.stream().filter(value ->
                                value.lane.equals("candidate")
                                && value.byteRelation.equals(
                                        "BYTE_DIFFERENT")).count(),
                "runtime.candidate.collision.policy.rows\t"
                        + state.candidateCollisionPolicy.size(),
                "runtime.candidate.collision.policy\tPOLICY_BOUND_ORDERED_FIRST_ORIGIN_WINS",
                "methods.expected\t" + state.methodsExpected,
                "methods.recorded\t" + state.methodsRecorded,
                "frames.expected\t" + state.framesExpected,
                "frames.recorded\t" + state.framesRecorded,
                "verification.expected\t" + state.verificationExpected,
                "verification.recorded\t" + state.verificationRecorded,
                "relations.expected\t" + state.relationExpected,
                "relations.recorded\t" + state.relationRecorded,
                "setup.complete\t" + state.setupComplete,
                "ledgers.complete\t" + state.ledgerComplete,
                "violations\t" + state.violations.size(),
                "unknown.attribute.policy\tFAIL",
                "unproved.method.identity.policy\tFAIL",
                "original.runtime.contamination.policy\tFAIL");
        writeLines(output.resolve("summary.tsv"), summary);

        writeSelfExcludingManifest(output, List.of("blocked-aliases.tsv",
                "class-index.tsv",
                "frames.tsv", "inputs.tsv", "maxima.tsv", "relations.tsv",
                "runtime-collisions.tsv", "runtime-elements.tsv",
                "runtime-shadows.tsv", "summary.tsv", "verification.tsv",
                "violations.tsv"));
    }

    private static int runGate(Config config) throws IOException {
        State state = new State();
        readRuntimeLedger(config.candidateRuntimeLedger, "candidate", state);
        readRuntimeLedger(config.oracleRuntimeLedger, "oracle", state);
        preflightOutput(config, state);
        prepareOutput(config.output);
        try {
            setupAndRun(config, state);
        } catch (Throwable failure) {
            state.violation("gate", "driver", "<setup-or-run>", "fatal",
                    failure.getClass().getName() + ": "
                            + clean(failure.getMessage()));
        }
        capturePostHashes(state);
        state.ledgerComplete = state.methodsExpected == state.methodsRecorded
                && state.framesExpected == state.framesRecorded
                && state.verificationExpected == state.verificationRecorded
                && state.relationExpected == state.relationRecorded
                && state.aliasBlockExpected == state.aliasBlockRecorded
                && state.collisionExpected == state.collisionRecorded
                && state.runtime.stream().allMatch(value -> value.scanned);
        writeEvidence(config.output, state);
        return state.setupComplete && state.ledgerComplete
                && state.violations.isEmpty() ? 0 : 2;
    }

    private static byte[] fixtureClass(String internalName, int constructorStack,
            int constructorLocals, int probeStack, boolean malformedFrame,
            boolean initializer, boolean extraMethod,
            boolean externalDescriptor) {
        ClassWriter writer = new ClassWriter(0);
        writer.visit(Opcodes.V11, Opcodes.ACC_PUBLIC | Opcodes.ACC_FINAL,
                internalName, null, "java/lang/Object", null);
        MethodVisitor method = writer.visitMethod(Opcodes.ACC_PUBLIC,
                "<init>", "()V", null, null);
        method.visitCode();
        method.visitVarInsn(Opcodes.ALOAD, 0);
        method.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/Object",
                "<init>", "()V", false);
        method.visitInsn(Opcodes.RETURN);
        method.visitMaxs(constructorStack, constructorLocals);
        method.visitEnd();

        method = writer.visitMethod(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC,
                "probe", "()I", null, null);
        method.visitCode();
        method.visitInsn(Opcodes.ICONST_1);
        method.visitInsn(Opcodes.IRETURN);
        method.visitMaxs(probeStack, 0);
        method.visitEnd();

        if (malformedFrame) {
            method = writer.visitMethod(Opcodes.ACC_PUBLIC
                    | Opcodes.ACC_STATIC, "branch", "(I)I", null, null);
            method.visitCode();
            Label zero = new Label();
            method.visitVarInsn(Opcodes.ILOAD, 0);
            method.visitJumpInsn(Opcodes.IFEQ, zero);
            method.visitInsn(Opcodes.ICONST_1);
            method.visitInsn(Opcodes.IRETURN);
            method.visitLabel(zero);
            method.visitFrame(Opcodes.F_FULL, 0, new Object[0], 1,
                    new Object[] {Opcodes.INTEGER});
            method.visitInsn(Opcodes.ICONST_0);
            method.visitInsn(Opcodes.IRETURN);
            method.visitMaxs(1, 1);
            method.visitEnd();
        }

        if (extraMethod) {
            method = writer.visitMethod(Opcodes.ACC_PUBLIC
                    | Opcodes.ACC_STATIC, "extra", "()V", null, null);
            method.visitCode();
            method.visitInsn(Opcodes.RETURN);
            method.visitMaxs(0, 0);
            method.visitEnd();
        }

        if (externalDescriptor) {
            method = writer.visitMethod(Opcodes.ACC_PUBLIC
                    | Opcodes.ACC_STATIC, "external",
                    "()Lfixture/OracleOnly;", null, null);
            method.visitCode();
            method.visitInsn(Opcodes.ACONST_NULL);
            method.visitInsn(Opcodes.ARETURN);
            method.visitMaxs(1, 0);
            method.visitEnd();
        }

        if (initializer) {
            method = writer.visitMethod(Opcodes.ACC_STATIC, "<clinit>",
                    "()V", null, null);
            method.visitCode();
            method.visitLdcInsn("verify.exact.aot.initialized");
            method.visitLdcInsn("yes");
            method.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/System",
                    "setProperty", "(Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;",
                    false);
            method.visitInsn(Opcodes.POP);
            method.visitInsn(Opcodes.RETURN);
            method.visitMaxs(2, 0);
            method.visitEnd();
        }
        writer.visitEnd();
        return writer.toByteArray();
    }

    private static void writeJar(Path path, Map<String, byte[]> entries)
            throws IOException {
        try (OutputStream raw = Files.newOutputStream(path,
                StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING,
                StandardOpenOption.WRITE);
             ZipOutputStream zip = new ZipOutputStream(raw)) {
            for (Map.Entry<String, byte[]> value : new TreeMap<>(entries)
                    .entrySet()) {
                ZipEntry entry = new ZipEntry(value.getKey());
                entry.setTime(0L);
                zip.putNextEntry(entry);
                zip.write(value.getValue());
                zip.closeEntry();
            }
        }
    }

    private static byte[] renameZipEntry(byte[] archive, String from,
            String to) {
        byte[] source = from.getBytes(StandardCharsets.UTF_8);
        byte[] target = to.getBytes(StandardCharsets.UTF_8);
        require(source.length == target.length,
                "ZIP entry rename must preserve byte length");
        byte[] result = archive.clone();
        int replacements = 0;
        for (int offset = 0; offset <= result.length - source.length;
                offset++) {
            boolean match = true;
            for (int index = 0; index < source.length; index++) {
                if (result[offset + index] != source[index]) {
                    match = false;
                    break;
                }
            }
            if (!match) continue;
            System.arraycopy(target, 0, result, offset, target.length);
            replacements++;
            offset += source.length - 1;
        }
        require(replacements == 2,
                "expected one local and one central ZIP entry name");
        return result;
    }

    private static void writeRuntimeLedger(Path ledger,
            List<String[]> rows) throws IOException {
        List<String> lines = new ArrayList<>();
        lines.add("position\trole\tkind\tpath\texpected_sha256");
        int position = 0;
        for (String[] row : rows) {
            require(row.length == 4, "bad synthetic runtime row");
            lines.add(++position + "\t" + row[0] + "\t" + row[1] + "\t"
                    + row[2] + "\t" + row[3]);
        }
        writeLines(ledger, lines);
    }

    private static final class Fixture {
        final Path root;
        final Path candidateA;
        final Path candidateB;
        final Path original;
        final Path cohort;
        final Path ownership;
        final Path mappings;
        final Path runtimePolicy;
        final Path candidateLedger;
        final Path oracleLedger;
        final Path candidateCollisions;
        final Path candidateDeps;
        final Path sanitizedNano;
        final Path peerOriginal;
        final Path core2Original;
        final Path nanoOriginal;

        Fixture(Path root, Path candidateA, Path candidateB, Path original,
                Path cohort, Path ownership, Path mappings,
                Path runtimePolicy, Path candidateLedger, Path oracleLedger,
                Path candidateCollisions, Path candidateDeps,
                Path sanitizedNano, Path peerOriginal, Path core2Original,
                Path nanoOriginal) {
            this.root = root;
            this.candidateA = candidateA;
            this.candidateB = candidateB;
            this.original = original;
            this.cohort = cohort;
            this.ownership = ownership;
            this.mappings = mappings;
            this.runtimePolicy = runtimePolicy;
            this.candidateLedger = candidateLedger;
            this.oracleLedger = oracleLedger;
            this.candidateCollisions = candidateCollisions;
            this.candidateDeps = candidateDeps;
            this.sanitizedNano = sanitizedNano;
            this.peerOriginal = peerOriginal;
            this.core2Original = core2Original;
            this.nanoOriginal = nanoOriginal;
        }

        Config config(Path output) {
            return new Config(candidateA, candidateB, original, cohort,
                    ownership, mappings, runtimePolicy, candidateLedger,
                    oracleLedger, candidateCollisions, output, false);
        }

        void writeCandidateCollision(String internalName,
                int firstPosition, String firstRole, String firstEntry,
                int firstRelease, String firstSha, int laterPosition,
                String laterRole, String laterEntry, int laterRelease,
                String laterSha) throws IOException {
            String relation = firstSha.equals(laterSha)
                    ? "BYTE_IDENTICAL" : "BYTE_DIFFERENT";
            writeLines(candidateCollisions, List.of(
                    CANDIDATE_COLLISION_HEADER,
                    "candidate\t" + internalName + "\t" + firstPosition
                            + "\t" + firstRole + "\t" + firstEntry + "\t"
                            + firstRelease + "\t" + firstSha + "\t"
                            + laterPosition + "\t" + laterRole + "\t"
                            + laterEntry + "\t" + laterRelease + "\t"
                            + laterSha + "\t" + relation));
        }
    }

    private static Fixture fixture(Path root, byte[] candidateABytes,
            byte[] candidateBBytes, byte[] originalBytes) throws IOException {
        Files.createDirectories(root);
        Path candidateA = root.resolve("candidate-a");
        Path candidateB = root.resolve("candidate-b");
        Path candidateAClass = candidateA.resolve(
                "fixture.core/fixture/Core.class");
        Path candidateBClass = candidateB.resolve(
                "fixture.core/fixture/Core.class");
        Files.createDirectories(candidateAClass.getParent());
        Files.createDirectories(candidateBClass.getParent());
        Files.write(candidateAClass, candidateABytes);
        Files.write(candidateBClass, candidateBBytes);
        Path original = root.resolve("original.jar");
        writeJar(original, Map.of("fixture/Core.class", originalBytes));
        Path cohort = root.resolve("cohort.tsv");
        writeLines(cohort, List.of("namespace\texpected_classes",
                "fixture.core\t1"));
        Path ownership = root.resolve("ownership.tsv");
        writeLines(ownership, List.of("entry\towner_id",
                "fixture/Core.class\tfixture.core"));
        Path mappings = root.resolve("class-mappings.tsv");
        writeLines(mappings, List.of(
                "comparison\tnamespace\tleft_class\tright_class\tidentity",
                "candidate-a--candidate-b\tfixture.core\tfixture/Core\tfixture/Core\ttrue",
                "candidate-a--original\tfixture.core\tfixture/Core\tfixture/Core\ttrue",
                "candidate-b--original\tfixture.core\tfixture/Core\tfixture/Core\ttrue"));
        Path candidateDeps = root.resolve("candidate-deps");
        Files.createDirectories(candidateDeps);
        Path sanitizedNano = root.resolve("nano-sanitized.jar");
        Path peerOriginal = root.resolve("peer-original.jar");
        Path core2Original = root.resolve("core2-original.jar");
        Path nanoOriginal = root.resolve("nano-original.jar");
        writeJar(sanitizedNano, Map.of("fixture/sanitized.txt",
                "sanitized\n".getBytes(StandardCharsets.UTF_8)));
        writeJar(peerOriginal, Map.of(
                "fixture/peer.txt",
                "peer\n".getBytes(StandardCharsets.UTF_8)));
        writeJar(core2Original, Map.of(
                "fixture/core2.txt",
                "core2\n".getBytes(StandardCharsets.UTF_8),
                "fixture/Core.class",
                fixtureClass("fixture/Core", 0, 0, 0, false, false,
                        false, false)));
        writeJar(nanoOriginal, Map.of("fixture/nano.txt",
                "nano-original\n".getBytes(StandardCharsets.UTF_8)));
        Path runtimePolicy = root.resolve("runtime-policy.tsv");
        Path candidateLedger = root.resolve("candidate-runtime.tsv");
        writeRuntimeLedger(candidateLedger, List.of(
                new String[] {"candidate-deps", "directory",
                        candidateDeps.toString(),
                        directoryManifest(candidateDeps)},
                new String[] {"sanitized-datomic-dependency", "jar",
                        sanitizedNano.toString(), sha256(sanitizedNano)}));
        Path oracleLedger = root.resolve("oracle-runtime.tsv");
        writeRuntimeLedger(oracleLedger, List.of(
                new String[] {"licensed-core2", "jar",
                        core2Original.toString(), sha256(core2Original)},
                new String[] {"original-nano", "jar",
                        nanoOriginal.toString(), sha256(nanoOriginal)}));
        Path candidateCollisions = root.resolve("candidate-collisions.tsv");
        writeLines(candidateCollisions, List.of(CANDIDATE_COLLISION_HEADER));
        Fixture result = new Fixture(root, candidateA, candidateB, original,
                cohort,
                ownership, mappings, runtimePolicy, candidateLedger,
                oracleLedger, candidateCollisions, candidateDeps,
                sanitizedNano, peerOriginal, core2Original, nanoOriginal);
        writeFixturePolicy(result);
        return result;
    }

    private static void writeFixturePolicy(Fixture fixture) throws IOException {
        int candidateElements = Files.readAllLines(fixture.candidateLedger,
                StandardCharsets.UTF_8).size() - 1;
        int oracleElements = Files.readAllLines(fixture.oracleLedger,
                StandardCharsets.UTF_8).size() - 1;
        String suffix = "\t" + candidateElements + "\t" + oracleElements
                + "\t" + sha256(fixture.candidateLedger) + "\t"
                + sha256(fixture.oracleLedger);
        writeLines(fixture.runtimePolicy, List.of(
                "rule_id\tsha256\tcandidate_disposition\tcandidate_role"
                        + "\toracle_disposition\toracle_role"
                        + "\tcandidate_elements\toracle_elements"
                        + "\tcandidate_ledger_sha256\toracle_ledger_sha256",
                "transactor-original\t" + sha256(fixture.original)
                        + "\tFORBID\t\tOWNED\t" + suffix,
                "peer-original\t" + sha256(fixture.peerOriginal)
                        + "\tFORBID\t\tFORBID\t" + suffix,
                "core2-original\t" + sha256(fixture.core2Original)
                        + "\tFORBID\t\tREQUIRE\tlicensed-core2" + suffix,
                "nano-original\t" + sha256(fixture.nanoOriginal)
                        + "\tFORBID\t\tREQUIRE\toriginal-nano" + suffix,
                "nano-sanitized\t" + sha256(fixture.sanitizedNano)
                        + "\tREQUIRE\tsanitized-datomic-dependency\tFORBID\t"
                        + suffix));
    }

    private interface CheckedAction { void run() throws Exception; }

    private static void selfTest(List<String> rows, String name,
            CheckedAction action) {
        try {
            action.run();
            rows.add(name + "\tPASS\tok");
        } catch (Throwable failure) {
            rows.add(name + "\tFAIL\t" + failure.getClass().getName() + ": "
                    + clean(failure.getMessage()));
        }
    }

    private static String evidence(Path output, String file)
            throws IOException {
        return Files.readString(output.resolve(file), StandardCharsets.UTF_8);
    }

    private static void expectGate(Fixture fixture, Path output,
            boolean shouldPass, String evidenceNeedle) throws IOException {
        int status = runGate(fixture.config(output));
        require((status == 0) == shouldPass,
                "unexpected gate status " + status + " for " + output);
        if (evidenceNeedle != null) {
            String violations = evidence(output, "violations.tsv");
            require(violations.contains(evidenceNeedle),
                    "expected violation evidence is absent: "
                            + evidenceNeedle);
        }
        String summary = evidence(output, "summary.tsv");
        require(summary.contains("status\t" + (shouldPass ? "PASS" : "FAIL")),
                "summary status disagrees with gate status");
    }

    private static byte[] normalFixtureClass() {
        return fixtureClass("fixture/Core", 1, 1, 1, false, false,
                false, false);
    }

    private static int runSelfTests(Path output) throws IOException {
        require(verifyAllRequested(),
                "self-tests require the exact JVM option -Xverify:all");
        prepareOutput(output);
        Path scratch = Files.createTempDirectory("verify-exact-aot-selftest.");
        List<String> rows = new ArrayList<>();
        rows.add("test\tstatus\tdetail");

        selfTest(rows, "valid-resolution-and-no-initialization", () -> {
            System.clearProperty("verify.exact.aot.initialized");
            byte[] bytes = fixtureClass("fixture/Core", 1, 1, 1, false,
                    true, false, false);
            Fixture value = fixture(scratch.resolve("valid"), bytes, bytes,
                    bytes);
            expectGate(value, value.root.resolve("out"), true, null);
            require(System.getProperty("verify.exact.aot.initialized") == null,
                    "Class.forName(false)/resolution initialized fixture");
        });

        selfTest(rows, "policy-bound-candidate-collision-first-wins", () -> {
            byte[] owned = normalFixtureClass();
            Fixture value = fixture(scratch.resolve("candidate-collision"),
                    owned, owned, owned);
            String internal = "jline/console/Fixture";
            String entry = internal + ".class";
            byte[] first = fixtureClass(internal, 1, 1, 1, false, false,
                    false, false);
            byte[] later = fixtureClass(internal, 1, 1, 1, false, false,
                    true, false);
            Path firstJar = value.root.resolve("jline-first.jar");
            Path laterJar = value.root.resolve("jline-later.jar");
            writeJar(firstJar, Map.of(entry, first));
            writeJar(laterJar, Map.of(entry, later));
            writeRuntimeLedger(value.candidateLedger, List.of(
                    new String[] {"candidate-deps", "directory",
                            value.candidateDeps.toString(),
                            directoryManifest(value.candidateDeps)},
                    new String[] {"sanitized-datomic-dependency", "jar",
                            value.sanitizedNano.toString(),
                            sha256(value.sanitizedNano)},
                    new String[] {"jline-first", "jar", firstJar.toString(),
                            sha256(firstJar)},
                    new String[] {"jline-later", "jar", laterJar.toString(),
                            sha256(laterJar)}));
            writeFixturePolicy(value);
            value.writeCandidateCollision(internal, 3, "jline-first", entry,
                    0, sha256(first), 4, "jline-later", entry, 0,
                    sha256(later));
            Path out = value.root.resolve("out");
            expectGate(value, out, true, null);
            String collisions = evidence(out, "runtime-collisions.tsv");
            require(collisions.contains(sha256(first))
                            && collisions.contains(sha256(later))
                            && collisions.contains("BYTE_DIFFERENT\ttrue\tPASS")
                            && collisions.contains(firstJar.toUri().toURL()
                                    .toExternalForm()),
                    "candidate first-wins evidence is incomplete");
        });

        selfTest(rows, "policy-bound-identical-candidate-collision", () -> {
            byte[] owned = normalFixtureClass();
            Fixture value = fixture(scratch.resolve("identical-collision"),
                    owned, owned, owned);
            String internal = "jline/console/Identical";
            String entry = internal + ".class";
            byte[] bytes = fixtureClass(internal, 1, 1, 1, false, false,
                    false, false);
            Path firstJar = value.root.resolve("identical-first.jar");
            Path laterJar = value.root.resolve("identical-later.jar");
            writeJar(firstJar, Map.of(entry, bytes));
            writeJar(laterJar, Map.of(entry, bytes));
            writeRuntimeLedger(value.candidateLedger, List.of(
                    new String[] {"candidate-deps", "directory",
                            value.candidateDeps.toString(),
                            directoryManifest(value.candidateDeps)},
                    new String[] {"sanitized-datomic-dependency", "jar",
                            value.sanitizedNano.toString(),
                            sha256(value.sanitizedNano)},
                    new String[] {"identical-first", "jar",
                            firstJar.toString(), sha256(firstJar)},
                    new String[] {"identical-later", "jar",
                            laterJar.toString(), sha256(laterJar)}));
            writeFixturePolicy(value);
            value.writeCandidateCollision(internal, 3, "identical-first",
                    entry, 0, sha256(bytes), 4, "identical-later", entry, 0,
                    sha256(bytes));
            Path out = value.root.resolve("out");
            expectGate(value, out, true, null);
            require(evidence(out, "runtime-collisions.tsv").contains(
                            "BYTE_IDENTICAL\ttrue\tPASS"),
                    "byte-identical collision relation was not proved");
        });

        selfTest(rows, "missing-candidate-collision-policy-rejected", () -> {
            byte[] owned = normalFixtureClass();
            Fixture value = fixture(scratch.resolve("missing-collision-row"),
                    owned, owned, owned);
            String internal = "jline/MissingPolicy";
            String entry = internal + ".class";
            byte[] bytes = fixtureClass(internal, 1, 1, 1, false, false,
                    false, false);
            Path firstJar = value.root.resolve("missing-first.jar");
            Path laterJar = value.root.resolve("missing-later.jar");
            writeJar(firstJar, Map.of(entry, bytes));
            writeJar(laterJar, Map.of(entry, bytes));
            writeRuntimeLedger(value.candidateLedger, List.of(
                    new String[] {"candidate-deps", "directory",
                            value.candidateDeps.toString(),
                            directoryManifest(value.candidateDeps)},
                    new String[] {"sanitized-datomic-dependency", "jar",
                            value.sanitizedNano.toString(),
                            sha256(value.sanitizedNano)},
                    new String[] {"missing-first", "jar", firstJar.toString(),
                            sha256(firstJar)},
                    new String[] {"missing-later", "jar", laterJar.toString(),
                            sha256(laterJar)}));
            writeFixturePolicy(value);
            expectGate(value, value.root.resolve("out"), false,
                    "missing-candidate-collision-policy");
        });

        selfTest(rows, "extra-candidate-collision-policy-rejected", () -> {
            byte[] owned = normalFixtureClass();
            Fixture value = fixture(scratch.resolve("extra-collision-row"),
                    owned, owned, owned);
            String internal = "jline/Ghost";
            String entry = internal + ".class";
            String zero = "0000000000000000000000000000000000000000000000000000000000000000";
            value.writeCandidateCollision(internal, 1, "candidate-deps",
                    entry, 0, zero, 2, "sanitized-datomic-dependency", entry,
                    0, zero);
            expectGate(value, value.root.resolve("out"), false,
                    "extra-or-unobserved-candidate-collision-policy");
        });

        selfTest(rows, "candidate-collision-order-reversal-rejected", () -> {
            byte[] owned = normalFixtureClass();
            Fixture value = fixture(scratch.resolve("collision-reorder"),
                    owned, owned, owned);
            String internal = "jline/Reordered";
            String entry = internal + ".class";
            byte[] first = fixtureClass(internal, 1, 1, 1, false, false,
                    false, false);
            byte[] later = fixtureClass(internal, 1, 1, 1, false, false,
                    true, false);
            Path firstJar = value.root.resolve("reorder-first.jar");
            Path laterJar = value.root.resolve("reorder-later.jar");
            writeJar(firstJar, Map.of(entry, first));
            writeJar(laterJar, Map.of(entry, later));
            writeRuntimeLedger(value.candidateLedger, List.of(
                    new String[] {"candidate-deps", "directory",
                            value.candidateDeps.toString(),
                            directoryManifest(value.candidateDeps)},
                    new String[] {"sanitized-datomic-dependency", "jar",
                            value.sanitizedNano.toString(),
                            sha256(value.sanitizedNano)},
                    new String[] {"reorder-later", "jar", laterJar.toString(),
                            sha256(laterJar)},
                    new String[] {"reorder-first", "jar", firstJar.toString(),
                            sha256(firstJar)}));
            writeFixturePolicy(value);
            value.writeCandidateCollision(internal, 3, "reorder-first", entry,
                    0, sha256(first), 4, "reorder-later", entry, 0,
                    sha256(later));
            expectGate(value, value.root.resolve("out"), false,
                    "collision role/position policy spoof");
        });

        selfTest(rows, "candidate-collision-lane-spoof-rejected", () -> {
            byte[] owned = normalFixtureClass();
            Fixture value = fixture(scratch.resolve("collision-lane-spoof"),
                    owned, owned, owned);
            String zero = "0000000000000000000000000000000000000000000000000000000000000000";
            writeLines(value.candidateCollisions, List.of(
                    CANDIDATE_COLLISION_HEADER,
                    "oracle\tjline/Spoof\t1\tcandidate-deps"
                            + "\tjline/Spoof.class\t0\t" + zero
                            + "\t2\tsanitized-datomic-dependency"
                            + "\tjline/Spoof.class\t0\t" + zero
                            + "\tBYTE_IDENTICAL"));
            expectGate(value, value.root.resolve("out"), false,
                    "collision policy may describe only candidate lane");
        });

        selfTest(rows, "candidate-collision-unresolved-class-rejected", () -> {
            byte[] owned = normalFixtureClass();
            Fixture value = fixture(scratch.resolve("collision-unresolved"),
                    owned, owned, owned);
            String internal = "jline/Unresolved";
            String entry = internal + ".class";
            byte[] bytes = fixtureClass(internal, 1, 1, 1, false, false,
                    false, true);
            Path firstJar = value.root.resolve("unresolved-first.jar");
            Path laterJar = value.root.resolve("unresolved-later.jar");
            writeJar(firstJar, Map.of(entry, bytes));
            writeJar(laterJar, Map.of(entry, bytes));
            writeRuntimeLedger(value.candidateLedger, List.of(
                    new String[] {"candidate-deps", "directory",
                            value.candidateDeps.toString(),
                            directoryManifest(value.candidateDeps)},
                    new String[] {"sanitized-datomic-dependency", "jar",
                            value.sanitizedNano.toString(),
                            sha256(value.sanitizedNano)},
                    new String[] {"unresolved-first", "jar",
                            firstJar.toString(), sha256(firstJar)},
                    new String[] {"unresolved-later", "jar",
                            laterJar.toString(), sha256(laterJar)}));
            writeFixturePolicy(value);
            value.writeCandidateCollision(internal, 3, "unresolved-first",
                    entry, 0, sha256(bytes), 4, "unresolved-later", entry, 0,
                    sha256(bytes));
            expectGate(value, value.root.resolve("out"), false,
                    "first-wins-unproved");
        });

        selfTest(rows, "candidate-collision-wrong-code-source-rejected", () -> {
            Path root = scratch.resolve("wrong-collision-origin");
            Files.createDirectories(root);
            String internal = "jline/WrongOrigin";
            String entry = internal + ".class";
            byte[] firstBytes = fixtureClass(internal, 1, 1, 1, false,
                    false, false, false);
            byte[] laterBytes = fixtureClass(internal, 1, 1, 1, false,
                    false, true, false);
            Path firstJar = root.resolve("first.jar");
            Path laterJar = root.resolve("later.jar");
            writeJar(firstJar, Map.of(entry, firstBytes));
            writeJar(laterJar, Map.of(entry, laterBytes));
            RuntimeClassOrigin declaredFirst = new RuntimeClassOrigin(1,
                    "declared-first", firstJar, entry, 0,
                    sha256(firstBytes));
            RuntimeClassOrigin declaredLater = new RuntimeClassOrigin(2,
                    "declared-later", laterJar, entry, 0,
                    sha256(laterBytes));
            RuntimeCollision collision = new RuntimeCollision("candidate",
                    internal, declaredFirst, declaredLater);
            collision.policyMatched = true;
            State state = new State();
            verifyUniverse("candidate-a", Collections.emptyMap(),
                    new URL[] {laterJar.toUri().toURL(),
                            firstJar.toUri().toURL()}, root.toUri().toURL(),
                    Collections.emptySet(), Collections.emptySet(),
                    List.of(collision), state);
            String laterUrl = laterJar.toUri().toURL().toExternalForm();
            require(state.violations.stream().anyMatch(value ->
                            value.rule.equals("first-wins-unproved")
                            && value.detail.contains(laterUrl)),
                    "wrong collision code source was not rejected");
        });

        selfTest(rows, "candidate-collision-effective-release-spoof-rejected",
                () -> {
            byte[] owned = normalFixtureClass();
            Fixture value = fixture(scratch.resolve("collision-release"),
                    owned, owned, owned);
            String internal = "jline/Versioned";
            String baseEntry = internal + ".class";
            String versionedEntry = "META-INF/versions/11/" + baseEntry;
            byte[] base = fixtureClass(internal, 1, 1, 1, false, false,
                    false, false);
            byte[] versioned = fixtureClass(internal, 1, 1, 1, false, false,
                    true, false);
            byte[] later = fixtureClass(internal, 1, 1, 2, false, false,
                    false, false);
            Path firstJar = value.root.resolve("release-first.jar");
            Path laterJar = value.root.resolve("release-later.jar");
            writeJar(firstJar, Map.of("META-INF/MANIFEST.MF",
                    "Manifest-Version: 1.0\r\nMulti-Release: true\r\n\r\n"
                            .getBytes(StandardCharsets.UTF_8),
                    baseEntry, base, versionedEntry, versioned));
            writeJar(laterJar, Map.of(baseEntry, later));
            writeRuntimeLedger(value.candidateLedger, List.of(
                    new String[] {"candidate-deps", "directory",
                            value.candidateDeps.toString(),
                            directoryManifest(value.candidateDeps)},
                    new String[] {"sanitized-datomic-dependency", "jar",
                            value.sanitizedNano.toString(),
                            sha256(value.sanitizedNano)},
                    new String[] {"release-first", "jar", firstJar.toString(),
                            sha256(firstJar)},
                    new String[] {"release-later", "jar", laterJar.toString(),
                            sha256(laterJar)}));
            writeFixturePolicy(value);
            value.writeCandidateCollision(internal, 3, "release-first",
                    baseEntry, 0, sha256(base), 4, "release-later", baseEntry,
                    0, sha256(later));
            expectGate(value, value.root.resolve("out"), false,
                    "candidate-collision-policy-mismatch");
        });

        selfTest(rows, "candidate-runtime-duplicate-release-rejected", () -> {
            byte[] owned = normalFixtureClass();
            Fixture value = fixture(scratch.resolve("duplicate-release"),
                    owned, owned, owned);
            String internal = "jline/DuplicateRelease";
            byte[] bytes = fixtureClass(internal, 1, 1, 1, false, false,
                    false, false);
            Path duplicate = value.root.resolve("duplicate-release.jar");
            writeJar(duplicate, Map.of(
                    "META-INF/MANIFEST.MF",
                    "Manifest-Version: 1.0\r\nMulti-Release: true\r\n\r\n"
                            .getBytes(StandardCharsets.UTF_8),
                    "META-INF/versions/11/" + internal + ".class", bytes,
                    "META-INF/versions/011/" + internal + ".class", bytes));
            writeRuntimeLedger(value.candidateLedger, List.of(
                    new String[] {"candidate-deps", "directory",
                            value.candidateDeps.toString(),
                            directoryManifest(value.candidateDeps)},
                    new String[] {"sanitized-datomic-dependency", "jar",
                            value.sanitizedNano.toString(),
                            sha256(value.sanitizedNano)},
                    new String[] {"duplicate-release", "jar",
                            duplicate.toString(), sha256(duplicate)}));
            writeFixturePolicy(value);
            expectGate(value, value.root.resolve("out"), false,
                    "duplicate runtime class/release");
        });

        selfTest(rows, "candidate-runtime-duplicate-entry-path-rejected",
                () -> {
            byte[] owned = normalFixtureClass();
            Fixture value = fixture(scratch.resolve("duplicate-entry-path"),
                    owned, owned, owned);
            Path normal = value.root.resolve("normal-path.jar");
            writeJar(normal, Map.of(
                    "jline/A.class", fixtureClass("jline/A", 1, 1, 1,
                            false, false, false, false),
                    "jline/B.class", fixtureClass("jline/B", 1, 1, 1,
                            false, false, false, false)));
            byte[] duplicateBytes = renameZipEntry(Files.readAllBytes(normal),
                    "jline/B.class", "jline/A.class");
            Path duplicate = value.root.resolve("duplicate-path.jar");
            Files.write(duplicate, duplicateBytes);
            writeRuntimeLedger(value.candidateLedger, List.of(
                    new String[] {"candidate-deps", "directory",
                            value.candidateDeps.toString(),
                            directoryManifest(value.candidateDeps)},
                    new String[] {"sanitized-datomic-dependency", "jar",
                            value.sanitizedNano.toString(),
                            sha256(value.sanitizedNano)},
                    new String[] {"duplicate-path", "jar",
                            duplicate.toString(), sha256(duplicate)}));
            writeFixturePolicy(value);
            expectGate(value, value.root.resolve("out"), false,
                    "runtime JAR duplicate entry");
        });

        selfTest(rows, "too-small-max-stack-rejected-by-jvm", () -> {
            byte[] bytes = fixtureClass("fixture/Core", 0, 1, 1, false,
                    false, false, false);
            Fixture value = fixture(scratch.resolve("small-stack"), bytes,
                    bytes, bytes);
            expectGate(value, value.root.resolve("out"), false,
                    "jvm-verification-or-resolution");
        });

        selfTest(rows, "too-small-max-locals-rejected-by-jvm", () -> {
            byte[] bytes = fixtureClass("fixture/Core", 1, 0, 1, false,
                    false, false, false);
            Fixture value = fixture(scratch.resolve("small-locals"), bytes,
                    bytes, bytes);
            expectGate(value, value.root.resolve("out"), false,
                    "jvm-verification-or-resolution");
        });

        selfTest(rows, "malformed-expanded-frame-rejected-by-jvm", () -> {
            byte[] bytes = fixtureClass("fixture/Core", 1, 1, 1, true,
                    false, false, false);
            Fixture value = fixture(scratch.resolve("bad-frame"), bytes,
                    bytes, bytes);
            expectGate(value, value.root.resolve("out"), false,
                    "jvm-verification-or-resolution");
        });

        selfTest(rows, "inflated-maxima-rejected-by-relation", () -> {
            byte[] normal = normalFixtureClass();
            byte[] inflated = fixtureClass("fixture/Core", 1, 1, 2, false,
                    false, false, false);
            Fixture value = fixture(scratch.resolve("inflated"), normal,
                    inflated, normal);
            expectGate(value, value.root.resolve("out"), false,
                    "maxima-frame-difference");
        });

        selfTest(rows, "missing-class-rejected", () -> {
            byte[] bytes = normalFixtureClass();
            Fixture value = fixture(scratch.resolve("missing-class"), bytes,
                    bytes, bytes);
            Files.delete(value.candidateA.resolve(
                    "fixture.core/fixture/Core.class"));
            expectGate(value, value.root.resolve("out"), false,
                    "fatal");
        });

        selfTest(rows, "extra-class-rejected", () -> {
            byte[] bytes = normalFixtureClass();
            Fixture value = fixture(scratch.resolve("extra-class"), bytes,
                    bytes, bytes);
            Files.write(value.candidateA.resolve(
                    "fixture.core/fixture/Extra.class"),
                    fixtureClass("fixture/Extra", 1, 1, 1, false, false,
                            false, false));
            expectGate(value, value.root.resolve("out"), false,
                    "fatal");
        });

        selfTest(rows, "hard-linked-duplicate-class-rejected", () -> {
            byte[] bytes = normalFixtureClass();
            Fixture value = fixture(scratch.resolve("duplicate-class"), bytes,
                    bytes, bytes);
            Files.createLink(value.candidateA.resolve(
                    "fixture.core/fixture/Duplicate.class"),
                    value.candidateA.resolve("fixture.core/fixture/Core.class"));
            expectGate(value, value.root.resolve("out"), false,
                    "fatal");
        });

        selfTest(rows, "missing-mapping-row-rejected", () -> {
            byte[] bytes = normalFixtureClass();
            Fixture value = fixture(scratch.resolve("missing-map"), bytes,
                    bytes, bytes);
            writeLines(value.mappings, List.of(
                    "comparison\tnamespace\tleft_class\tright_class\tidentity",
                    "candidate-a--candidate-b\tfixture.core\tfixture/Core\tfixture/Core\ttrue",
                    "candidate-a--original\tfixture.core\tfixture/Core\tfixture/Core\ttrue"));
            expectGate(value, value.root.resolve("out"), false, "fatal");
        });

        selfTest(rows, "duplicate-mapping-row-rejected", () -> {
            byte[] bytes = normalFixtureClass();
            Fixture value = fixture(scratch.resolve("duplicate-map"), bytes,
                    bytes, bytes);
            Files.writeString(value.mappings,
                    "candidate-a--candidate-b\tfixture.core\tfixture/Core\tfixture/Core\ttrue\n",
                    StandardCharsets.UTF_8, StandardOpenOption.APPEND);
            expectGate(value, value.root.resolve("out"), false, "fatal");
        });

        selfTest(rows, "wrong-path-internal-name-rejected", () -> {
            byte[] bytes = normalFixtureClass();
            Fixture value = fixture(scratch.resolve("wrong-path"), bytes,
                    bytes, bytes);
            Path correct = value.candidateA.resolve(
                    "fixture.core/fixture/Core.class");
            Path wrong = value.candidateA.resolve(
                    "fixture.core/fixture/Wrong.class");
            Files.delete(correct);
            Files.write(wrong, bytes);
            expectGate(value, value.root.resolve("out"), false, "fatal");
        });

        selfTest(rows, "proved-poisoned-shadow-is-child-first-unselected", () -> {
            byte[] bytes = normalFixtureClass();
            Fixture value = fixture(scratch.resolve("shadow"), bytes, bytes,
                    bytes);
            Path shadow = value.candidateDeps.resolve("fixture/Core.class");
            Files.createDirectories(shadow.getParent());
            byte[] poison = fixtureClass("fixture/Core", 0, 0, 0, false,
                    false, false, false);
            Files.write(shadow, poison);
            writeRuntimeLedger(value.candidateLedger, List.of(
                    new String[] {"candidate-deps", "directory",
                            value.candidateDeps.toString(),
                            directoryManifest(value.candidateDeps)},
                    new String[] {"sanitized-datomic-dependency", "jar",
                            value.sanitizedNano.toString(),
                            sha256(value.sanitizedNano)}));
            writeFixturePolicy(value);
            Path out = value.root.resolve("out");
            expectGate(value, out, true, null);
            String shadows = evidence(out, "runtime-shadows.tsv");
            require(shadows.contains("fixture/Core")
                            && shadows.contains(sha256(poison))
                            && shadows.contains("CHILD_FIRST_OWNED")
                            && shadows.contains("\tfalse\tPASS"),
                    "proved poisoned shadow lacks selection evidence");
        });

        selfTest(rows, "unproved-owned-shadow-rejected", () -> {
            byte[] originalBytes = normalFixtureClass();
            Fixture value = fixture(scratch.resolve("unproved-shadow"),
                    originalBytes, originalBytes, originalBytes);
            Files.delete(value.candidateA.resolve(
                    "fixture.core/fixture/Core.class"));
            Files.delete(value.candidateB.resolve(
                    "fixture.core/fixture/Core.class"));
            byte[] renamed = fixtureClass("fixture/Renamed", 1, 1, 1,
                    false, false, false, false);
            Files.write(value.candidateA.resolve(
                    "fixture.core/fixture/Renamed.class"), renamed);
            Files.write(value.candidateB.resolve(
                    "fixture.core/fixture/Renamed.class"), renamed);
            writeLines(value.mappings, List.of(
                    "comparison\tnamespace\tleft_class\tright_class\tidentity",
                    "candidate-a--candidate-b\tfixture.core\tfixture/Renamed\tfixture/Renamed\ttrue",
                    "candidate-a--original\tfixture.core\tfixture/Renamed\tfixture/Core\tfalse",
                    "candidate-b--original\tfixture.core\tfixture/Renamed\tfixture/Core\tfalse"));
            Path shadow = value.candidateDeps.resolve(
                    "fixture/Renamed.class");
            Files.createDirectories(shadow.getParent());
            Files.write(shadow, renamed);
            writeRuntimeLedger(value.candidateLedger, List.of(
                    new String[] {"candidate-deps", "directory",
                            value.candidateDeps.toString(),
                            directoryManifest(value.candidateDeps)},
                    new String[] {"sanitized-datomic-dependency", "jar",
                            value.sanitizedNano.toString(),
                            sha256(value.sanitizedNano)}));
            writeFixturePolicy(value);
            expectGate(value, value.root.resolve("out"), false,
                    "undeclared/unproved owned shadow");
        });

        selfTest(rows, "poisoned-nonidentity-original-alias-blocked", () -> {
            byte[] originalBytes = normalFixtureClass();
            Fixture value = fixture(scratch.resolve("blocked-alias"),
                    originalBytes, originalBytes, originalBytes);
            Files.delete(value.candidateA.resolve(
                    "fixture.core/fixture/Core.class"));
            Files.delete(value.candidateB.resolve(
                    "fixture.core/fixture/Core.class"));
            byte[] renamed = fixtureClass("fixture/Renamed", 1, 1, 1,
                    false, false, false, false);
            Files.write(value.candidateA.resolve(
                    "fixture.core/fixture/Renamed.class"), renamed);
            Files.write(value.candidateB.resolve(
                    "fixture.core/fixture/Renamed.class"), renamed);
            writeLines(value.mappings, List.of(
                    "comparison\tnamespace\tleft_class\tright_class\tidentity",
                    "candidate-a--candidate-b\tfixture.core\tfixture/Renamed\tfixture/Renamed\ttrue",
                    "candidate-a--original\tfixture.core\tfixture/Renamed\tfixture/Core\tfalse",
                    "candidate-b--original\tfixture.core\tfixture/Renamed\tfixture/Core\tfalse"));
            byte[] poison = fixtureClass("fixture/Core", 0, 0, 0, false,
                    false, false, false);
            Path alias = value.candidateDeps.resolve("fixture/Core.class");
            Files.createDirectories(alias.getParent());
            Files.write(alias, poison);
            writeRuntimeLedger(value.candidateLedger, List.of(
                    new String[] {"candidate-deps", "directory",
                            value.candidateDeps.toString(),
                            directoryManifest(value.candidateDeps)},
                    new String[] {"sanitized-datomic-dependency", "jar",
                            value.sanitizedNano.toString(),
                            sha256(value.sanitizedNano)}));
            writeFixturePolicy(value);
            Path out = value.root.resolve("out");
            expectGate(value, out, true, null);
            String blocks = evidence(out, "blocked-aliases.tsv");
            require(blocks.contains("candidate-a\tfixture/Core")
                            && blocks.contains("candidate-b\tfixture/Core")
                            && blocks.contains("BLOCKED_BEFORE_PARENT_OR_URL\tPASS"),
                    "nonidentity alias block was not proved in both loaders");
        });

        selfTest(rows, "candidate-original-contamination-rejected", () -> {
            byte[] bytes = normalFixtureClass();
            Fixture value = fixture(scratch.resolve("original-contamination"),
                    bytes, bytes, bytes);
            Path renamed = value.root.resolve("innocuous-dependency.jar");
            Files.copy(value.original, renamed);
            writeRuntimeLedger(value.candidateLedger, List.of(
                    new String[] {"candidate-deps", "directory",
                            value.candidateDeps.toString(),
                            directoryManifest(value.candidateDeps)},
                    new String[] {"sanitized-datomic-dependency", "jar",
                            value.sanitizedNano.toString(),
                            sha256(value.sanitizedNano)},
                    new String[] {"renamed-role", "jar", renamed.toString(),
                            sha256(renamed)}));
            writeFixturePolicy(value);
            expectGate(value, value.root.resolve("out"), false, "fatal");
        });

        selfTest(rows, "candidate-pinned-peer-identity-rejected", () -> {
            byte[] bytes = normalFixtureClass();
            Fixture value = fixture(scratch.resolve("peer-contamination"),
                    bytes, bytes, bytes);
            writeRuntimeLedger(value.candidateLedger, List.of(
                    new String[] {"candidate-deps", "directory",
                            value.candidateDeps.toString(),
                            directoryManifest(value.candidateDeps)},
                    new String[] {"sanitized-datomic-dependency", "jar",
                            value.sanitizedNano.toString(),
                            sha256(value.sanitizedNano)},
                    new String[] {"misleading", "jar",
                            value.peerOriginal.toString(),
                            sha256(value.peerOriginal)}));
            writeFixturePolicy(value);
            expectGate(value, value.root.resolve("out"), false,
                    "peer-original");
        });

        selfTest(rows, "sanitized-nano-wrong-role-rejected", () -> {
            byte[] bytes = normalFixtureClass();
            Fixture value = fixture(scratch.resolve("nano-wrong-role"), bytes,
                    bytes, bytes);
            writeRuntimeLedger(value.candidateLedger, List.of(
                    new String[] {"candidate-deps", "directory",
                            value.candidateDeps.toString(),
                            directoryManifest(value.candidateDeps)},
                    new String[] {"wrong-role", "jar",
                            value.sanitizedNano.toString(),
                            sha256(value.sanitizedNano)}));
            writeFixturePolicy(value);
            expectGate(value, value.root.resolve("out"), false,
                    "nano-sanitized");
        });

        selfTest(rows, "sanitized-nano-wrong-hash-rejected", () -> {
            byte[] bytes = normalFixtureClass();
            Fixture value = fixture(scratch.resolve("nano-wrong-hash"), bytes,
                    bytes, bytes);
            Path changed = value.root.resolve("changed-sanitized.jar");
            writeJar(changed, Map.of("fixture/sanitized.txt",
                    "changed\n".getBytes(StandardCharsets.UTF_8)));
            writeRuntimeLedger(value.candidateLedger, List.of(
                    new String[] {"candidate-deps", "directory",
                            value.candidateDeps.toString(),
                            directoryManifest(value.candidateDeps)},
                    new String[] {"sanitized-datomic-dependency", "jar",
                            changed.toString(), sha256(changed)}));
            writeFixturePolicy(value);
            expectGate(value, value.root.resolve("out"), false,
                    "nano-sanitized");
        });

        selfTest(rows, "oracle-required-core2-identity-missing", () -> {
            byte[] bytes = normalFixtureClass();
            Fixture value = fixture(scratch.resolve("oracle-missing-core2"),
                    bytes, bytes, bytes);
            writeRuntimeLedger(value.oracleLedger, List.<String[]>of(
                    new String[] {"original-nano", "jar",
                            value.nanoOriginal.toString(),
                            sha256(value.nanoOriginal)}));
            writeFixturePolicy(value);
            expectGate(value, value.root.resolve("out"), false,
                    "core2-original");
        });

        selfTest(rows, "ordered-runtime-ledger-anchor-rejects-reorder", () -> {
            byte[] bytes = normalFixtureClass();
            Fixture value = fixture(scratch.resolve("ledger-reorder"), bytes,
                    bytes, bytes);
            /* Deliberately do not rewrite the policy anchor. */
            writeRuntimeLedger(value.oracleLedger, List.of(
                    new String[] {"original-nano", "jar",
                            value.nanoOriginal.toString(),
                            sha256(value.nanoOriginal)},
                    new String[] {"licensed-core2", "jar",
                            value.core2Original.toString(),
                            sha256(value.core2Original)}));
            expectGate(value, value.root.resolve("out"), false,
                    "ordered runtime ledger bytes differ");
        });

        selfTest(rows, "normal-mode-requires-532-and-533-elements", () -> {
            byte[] bytes = normalFixtureClass();
            Fixture value = fixture(scratch.resolve("production-cardinality"),
                    bytes, bytes, bytes);
            Config production = new Config(value.candidateA, value.candidateB,
                    value.original, value.cohort, value.ownership,
                    value.mappings, value.runtimePolicy,
                    value.candidateLedger, value.oracleLedger,
                    value.candidateCollisions, value.root.resolve("out"));
            require(runGate(production) == 2,
                    "small fixture passed normal production mode");
            require(evidence(value.root.resolve("out"), "violations.tsv")
                            .contains("exactly 532 candidate and 533 oracle"),
                    "production cardinality rejection lacks evidence");
        });

        selfTest(rows, "oracle-lane-not-visible-to-candidates", () -> {
            byte[] bytes = fixtureClass("fixture/Core", 1, 1, 1, false,
                    false, false, true);
            Fixture value = fixture(scratch.resolve("cross-lane"), bytes,
                    bytes, bytes);
            Path oracleDeps = value.root.resolve("oracle-deps");
            Path dependency = oracleDeps.resolve("fixture/OracleOnly.class");
            Files.createDirectories(dependency.getParent());
            Files.write(dependency, fixtureClass("fixture/OracleOnly", 1, 1,
                    1, false, false, false, false));
            writeRuntimeLedger(value.oracleLedger, List.of(
                    new String[] {"licensed-core2", "jar",
                            value.core2Original.toString(),
                            sha256(value.core2Original)},
                    new String[] {"original-nano", "jar",
                            value.nanoOriginal.toString(),
                            sha256(value.nanoOriginal)},
                    new String[] {"oracle-only", "directory",
                            oracleDeps.toString(), directoryManifest(oracleDeps)}));
            writeFixturePolicy(value);
            expectGate(value, value.root.resolve("out"), false,
                    "jvm-verification-or-resolution");
            String verification = evidence(value.root.resolve("out"),
                    "verification.tsv");
            require(verification.contains("candidate-a\tfixture/Core")
                            && verification.contains("original\tfixture/Core"),
                    "cross-lane verification ledger is incomplete");
        });

        selfTest(rows, "incomplete-callback-ledger-rejected", () -> {
            State state = new State();
            ClassBytes owner = new ClassBytes("synthetic", "fixture.core",
                    "fixture/Core", "fixture/Core.class", "synthetic",
                    new byte[] {0});
            MethodModel method = new MethodModel("broken", "()V",
                    Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC);
            method.codeVisited = true;
            state.methodsExpected = 1;
            boolean rejected = false;
            try { validateMethodCallbacks(owner, method, state); }
            catch (Failure expected) { rejected = true; }
            require(rejected && state.methodsRecorded == 0,
                    "incomplete synthetic callback was accepted");
        });

        selfTest(rows, "unproved-method-identity-rejected", () -> {
            byte[] withExtra = fixtureClass("fixture/Core", 1, 1, 1, false,
                    false, true, false);
            byte[] withoutExtra = normalFixtureClass();
            Fixture value = fixture(scratch.resolve("missing-method"),
                    withExtra, withoutExtra, withExtra);
            expectGate(value, value.root.resolve("out"), false,
                    "unproved-method-identity");
        });

        selfTest(rows, "verify-all-absence-probe-rejected", () -> {
            Path java = Path.of(System.getProperty("java.home"), "bin", "java");
            ProcessBuilder builder = new ProcessBuilder(java.toString(), "-cp",
                    System.getProperty("java.class.path"),
                    VerifyExactAotRuntime.class.getName(),
                    "--require-verify-all-probe");
            builder.redirectErrorStream(true);
            builder.environment().remove("JAVA_TOOL_OPTIONS");
            builder.environment().remove("_JAVA_OPTIONS");
            builder.environment().remove("JDK_JAVA_OPTIONS");
            Process process = builder.start();
            try (InputStream input = process.getInputStream()) {
                while (input.read() >= 0) { /* drain */ }
            }
            int status = process.waitFor();
            require(status != 0,
                    "probe without -Xverify:all was incorrectly accepted");
        });

        selfTest(rows, "nonempty-output-overwrite-refused", () -> {
            byte[] bytes = normalFixtureClass();
            Fixture value = fixture(scratch.resolve("overwrite"), bytes, bytes,
                    bytes);
            Path out = value.root.resolve("out");
            Files.createDirectories(out);
            Path marker = out.resolve("marker.txt");
            Files.writeString(marker, "preserve\n", StandardCharsets.UTF_8);
            boolean rejected = false;
            try { runGate(value.config(out)); }
            catch (Failure expected) { rejected = true; }
            require(rejected, "nonempty output was accepted");
            require(Files.readString(marker, StandardCharsets.UTF_8)
                            .equals("preserve\n"),
                    "output refusal changed preexisting marker");
            try (Stream<Path> paths = Files.list(out)) {
                require(paths.count() == 1,
                        "output refusal created additional files");
            }
        });

        selfTest(rows, "nested-output-preflight-does-not-mutate-input", () -> {
            byte[] bytes = normalFixtureClass();
            Fixture candidateCase = fixture(
                    scratch.resolve("nested-output-candidate"), bytes, bytes,
                    bytes);
            String candidateBefore = directoryManifest(candidateCase.candidateA);
            Path candidateNested = candidateCase.candidateA.resolve(
                    "would-be-output");
            boolean candidateRejected = false;
            try { runGate(candidateCase.config(candidateNested)); }
            catch (Failure expected) { candidateRejected = true; }
            require(candidateRejected && !Files.exists(candidateNested)
                            && candidateBefore.equals(directoryManifest(
                                    candidateCase.candidateA)),
                    "candidate-nested output mutated input before rejection");

            Fixture runtimeCase = fixture(
                    scratch.resolve("nested-output-runtime"), bytes, bytes,
                    bytes);
            String runtimeBefore = directoryManifest(runtimeCase.candidateDeps);
            Path runtimeNested = runtimeCase.candidateDeps.resolve(
                    "would-be-output");
            boolean runtimeRejected = false;
            try { runGate(runtimeCase.config(runtimeNested)); }
            catch (Failure expected) { runtimeRejected = true; }
            require(runtimeRejected && !Files.exists(runtimeNested)
                            && runtimeBefore.equals(directoryManifest(
                                    runtimeCase.candidateDeps)),
                    "runtime-nested output mutated input before rejection");
        });

        selfTest(rows, "failure-evidence-sealed", () -> {
            byte[] bytes = normalFixtureClass();
            Fixture value = fixture(scratch.resolve("failure-sealing"), bytes,
                    bytes, bytes);
            Files.delete(value.candidateA.resolve(
                    "fixture.core/fixture/Core.class"));
            Path out = value.root.resolve("out");
            require(runGate(value.config(out)) == 2,
                    "failure fixture unexpectedly passed");
            for (String file : List.of("class-index.tsv", "frames.tsv",
                    "blocked-aliases.tsv", "inputs.tsv", "manifest.sha256", "maxima.tsv",
                    "relations.tsv", "runtime-collisions.tsv",
                    "runtime-elements.tsv", "summary.tsv",
                    "runtime-shadows.tsv", "verification.tsv",
                    "violations.tsv")) {
                require(Files.isRegularFile(out.resolve(file)),
                        "failure evidence file absent: " + file);
            }
            require(evidence(out, "summary.tsv").contains("status\tFAIL"),
                    "sealed failure summary is not FAIL");
        });

        long failures = rows.stream().skip(1)
                .filter(value -> value.contains("\tFAIL\t")).count();
        writeLines(output.resolve("self-test.tsv"), rows);
        writeLines(output.resolve("summary.tsv"), List.of("metric\tvalue",
                "status\t" + (failures == 0 ? "PASS" : "FAIL"),
                "tests\t" + (rows.size() - 1),
                "failures\t" + failures,
                "jvm.verify.all.requested\t" + verifyAllRequested(),
                "production.cohort.run\tNOT_RUN",
                "wrapper.integration\tNOT_RUN",
                "stage.1.pass.claimed\tfalse"));
        writeSelfExcludingManifest(output,
                List.of("self-test.tsv", "summary.tsv"));
        return failures == 0 ? 0 : 2;
    }

    private static void usage() {
        System.err.println("usage:\n"
                + "  VerifyExactAotRuntime --verify CANDIDATE_A CANDIDATE_B"
                + " ORIGINAL_JAR COHORT_TSV OWNERSHIP_TSV CLASS_MAPPINGS_TSV"
                + " RUNTIME_POLICY_TSV CANDIDATE_RUNTIME_TSV"
                + " ORACLE_RUNTIME_TSV CANDIDATE_COLLISIONS_TSV OUTPUT\n"
                + "  VerifyExactAotRuntime --self-test OUTPUT\n"
                + "  VerifyExactAotRuntime --require-verify-all-probe");
    }

    public static void main(String[] arguments) {
        int status = 2;
        try {
            if (arguments.length == 1
                    && arguments[0].equals("--require-verify-all-probe")) {
                require(verifyAllRequested(), "-Xverify:all is absent");
                status = 0;
            } else if (arguments.length == 2
                    && arguments[0].equals("--self-test")) {
                status = runSelfTests(absolute(Path.of(arguments[1])));
            } else if (arguments.length == 12
                    && arguments[0].equals("--verify")) {
                status = runGate(new Config(Path.of(arguments[1]),
                        Path.of(arguments[2]), Path.of(arguments[3]),
                        Path.of(arguments[4]), Path.of(arguments[5]),
                        Path.of(arguments[6]), Path.of(arguments[7]),
                        Path.of(arguments[8]), Path.of(arguments[9]),
                        Path.of(arguments[10]), Path.of(arguments[11])));
            } else {
                usage();
            }
        } catch (Throwable failure) {
            System.err.println(failure.getClass().getName() + ": "
                    + clean(failure.getMessage()));
            status = 2;
        }
        if (status != 0) System.exit(status);
    }
}
