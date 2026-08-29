import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
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
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Attribute;
import org.objectweb.asm.ByteVector;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.ConstantDynamic;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Handle;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.ModuleVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.RecordComponentVisitor;
import org.objectweb.asm.Type;
import org.objectweb.asm.TypePath;
import org.objectweb.asm.signature.SignatureReader;
import org.objectweb.asm.signature.SignatureVisitor;

/**
 * Fail-closed inventory of the complete exact-source AOT runtime boundary.
 *
 * <p>This scanner never defines a candidate class.  It accounts for every
 * filesystem node and archive entry, binds canonical origins and content
 * hashes, parses every class with ASM, records every typed reference-bearing
 * callback, and closes those references independently for candidate A and B.
 * Candidate resources require an exact allow-list row.  Runtime resources are
 * owned by their ordered runtime element.  Unknown, unreadable, aliased,
 * duplicate, or unaccounted data is a violation.  Generated candidate output
 * may not contain nested archives, and candidate-lane content may not contain
 * keystores or pinned original payloads.  Hash-ledgered runtime archives are
 * recursively inventoried in either lane.  Oracle content is admitted according
 * to its separately hash-anchored runtime ledger and role/identity policy.
 * Candidate effective-class collisions require an exact ordered position,
 * role, entry, release, and class-hash policy row; runtime elements are never
 * deduplicated.</p>
 *
 * <p>The scanner is deliberately separate from {@code
 * ScanCandidateClasspath}.  It is not a JVM verifier and does not claim AOT
 * equivalence or Stage 1 completion.</p>
 */
public final class ScanExactAotBoundary {
    private static final int ASM = Opcodes.ASM9;
    private static final int JAVA_FEATURE = 11;
    private static final long MAX_ENTRY_BYTES = 256L * 1024L * 1024L;
    private static final int MAX_NESTED_DEPTH = 4;
    private static final Pattern SHA256 = Pattern.compile("[0-9a-f]{64}");
    private static final Pattern FORBIDDEN_ORIGINAL_ARCHIVE = Pattern.compile(
            "(?i)(^|/)(datomic-transactor-pro|peer|core2|nano-impl)(-[^/]*)?\\.(jar|zip)$");
    private static final String CANDIDATE_COLLISION_HEADER =
            "lane\tinternal_name\tfirst_position\tfirst_role\tfirst_entry"
            + "\tfirst_release\tfirst_sha256\tlater_position\tlater_role"
            + "\tlater_entry\tlater_release\tlater_sha256\tbyte_relation";
    private static final List<String> RELATIONS = List.of(
            "candidate-a--candidate-b", "candidate-a--original",
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
        final Path peerJar;
        final Path cohort;
        final Path ownership;
        final Path classMappings;
        final Path runtimePolicy;
        final Path candidateRuntime;
        final Path oracleRuntime;
        final Path candidateCollisions;
        final Path allowedResources;
        final Path forbiddenPayloads;
        final Path output;
        final boolean requireProductionShape;

        Config(Path candidateA, Path candidateB, Path originalJar,
               Path peerJar, Path cohort, Path ownership, Path classMappings,
               Path runtimePolicy, Path candidateRuntime, Path oracleRuntime,
               Path candidateCollisions, Path allowedResources,
               Path forbiddenPayloads, Path output) {
            this(candidateA, candidateB, originalJar, peerJar, cohort,
                    ownership, classMappings, runtimePolicy, candidateRuntime,
                    oracleRuntime, candidateCollisions, allowedResources,
                    forbiddenPayloads, output, true);
        }

        Config(Path candidateA, Path candidateB, Path originalJar,
               Path peerJar, Path cohort, Path ownership, Path classMappings,
               Path runtimePolicy, Path candidateRuntime, Path oracleRuntime,
               Path candidateCollisions, Path allowedResources,
               Path forbiddenPayloads, Path output,
               boolean requireProductionShape) {
            this.candidateA = absolute(candidateA);
            this.candidateB = absolute(candidateB);
            this.originalJar = absolute(originalJar);
            this.peerJar = absolute(peerJar);
            this.cohort = absolute(cohort);
            this.ownership = absolute(ownership);
            this.classMappings = absolute(classMappings);
            this.runtimePolicy = absolute(runtimePolicy);
            this.candidateRuntime = absolute(candidateRuntime);
            this.oracleRuntime = absolute(oracleRuntime);
            this.candidateCollisions = absolute(candidateCollisions);
            this.allowedResources = absolute(allowedResources);
            this.forbiddenPayloads = absolute(forbiddenPayloads);
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

    private static final class RuntimeElement {
        final String lane;
        final int position;
        final String role;
        final String kind;
        final Path path;
        final String expectedSha;
        String actualSha = "";
        String postSha = "";
        long discovered;
        long recorded;
        long classes;
        long resources;
        long directories;
        long other;
        long bytes;
        long errors;
        long forbiddenHits;
        long collisions;
        boolean scanned;

        RuntimeElement(String lane, int position, String role,
                       String kind, Path path, String expectedSha) {
            this.lane = lane;
            this.position = position;
            this.role = role;
            this.kind = kind;
            this.path = path;
            this.expectedSha = expectedSha;
        }

        String key() { return lane + ":" + position + ":" + role; }

        String owner() { return "runtime:" + lane + ":" + position + ":" + role; }
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

    private static final class MappingRow {
        final String comparison;
        final String namespace;
        final String left;
        final String right;
        final boolean identity;

        MappingRow(String comparison, String namespace, String left,
                   String right, boolean identity) {
            this.comparison = comparison;
            this.namespace = namespace;
            this.left = left;
            this.right = right;
            this.identity = identity;
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

    private static final class RuntimeShadow {
        final String lane;
        final int position;
        final String role;
        final String internalName;
        final String origin;
        final String sha;
        final String candidateADisposition;
        final String candidateBDisposition;
        final String oracleDisposition;

        RuntimeShadow(RuntimeElement element, String internalName,
                String origin, String sha, String candidateADisposition,
                String candidateBDisposition, String oracleDisposition) {
            this.lane = element.lane;
            this.position = element.position;
            this.role = element.role;
            this.internalName = internalName;
            this.origin = origin;
            this.sha = sha;
            this.candidateADisposition = candidateADisposition;
            this.candidateBDisposition = candidateBDisposition;
            this.oracleDisposition = oracleDisposition;
        }
    }

    private static final class AllowedResource {
        final String boundary;
        final String path;
        String owner;
        final String sha;
        boolean seen;

        AllowedResource(String boundary, String path, String owner, String sha) {
            this.boundary = boundary;
            this.path = path;
            this.owner = owner;
            this.sha = sha;
        }

        String key() { return boundary + "\u0000" + path; }
    }

    private static final class ForbiddenPayload {
        final String rule;
        final String sha;
        final String reason;
        ForbiddenPayload(String rule, String sha, String reason) {
            this.rule = rule;
            this.sha = sha;
            this.reason = reason;
        }
    }

    private static final class EntryRecord {
        final String id;
        final String boundary;
        final int elementPosition;
        String owner;
        final String origin;
        String kind;
        final String logicalPath;
        final int occurrence;
        String internalName = "";
        long bytes;
        String sha = "";
        String status = "PASS";

        EntryRecord(String boundary, int elementPosition, String owner,
                    String origin, String kind, String logicalPath,
                    int occurrence) {
            this.boundary = boundary;
            this.elementPosition = elementPosition;
            this.owner = owner;
            this.origin = origin;
            this.kind = kind;
            this.logicalPath = logicalPath;
            this.occurrence = occurrence;
            this.id = boundary + "\u0000" + elementPosition + "\u0000"
                    + logicalPath + "\u0000" + occurrence;
        }
    }

    private static final class ClassRecord {
        final String boundary;
        final String lane;
        final int elementPosition;
        final String owner;
        final String logicalPath;
        final String internalName;
        final int release;
        final boolean activeRuntimeEntry;
        final List<ReferenceRecord> references = new ArrayList<>();
        final Set<String> declaredMembers = new HashSet<>();
        String superName = "";
        final List<String> interfaces = new ArrayList<>();
        int referenceSequence;

        ClassRecord(String boundary, String lane, int elementPosition,
                    String owner, String logicalPath, String internalName,
                    int release,
                    boolean activeRuntimeEntry) {
            this.boundary = boundary;
            this.lane = lane;
            this.elementPosition = elementPosition;
            this.owner = owner;
            this.logicalPath = logicalPath;
            this.internalName = internalName;
            this.release = release;
            this.activeRuntimeEntry = activeRuntimeEntry;
        }
    }

    private static final class ReferenceRecord {
        final String boundary;
        final String lane;
        final int elementPosition;
        final String sourceClass;
        final String sourcePath;
        final int sequence;
        final String location;
        final String form;
        final String targetKind;
        final String targetOwner;
        final String targetName;
        final String targetDescriptor;
        final String valueSha;
        final boolean sourceActive;
        String resolution = "not-applicable";

        ReferenceRecord(ClassRecord source, String location, String form,
                        String targetKind, String targetOwner,
                        String targetName, String targetDescriptor,
                        String valueSha) {
            this.boundary = source.boundary;
            this.lane = source.lane;
            this.elementPosition = source.elementPosition;
            this.sourceClass = source.internalName;
            this.sourcePath = source.logicalPath;
            this.sequence = ++source.referenceSequence;
            this.location = location;
            this.form = form;
            this.targetKind = targetKind;
            this.targetOwner = targetOwner;
            this.targetName = targetName;
            this.targetDescriptor = targetDescriptor;
            this.valueSha = valueSha;
            this.sourceActive = source.activeRuntimeEntry;
        }
    }

    private static final class Violation {
        final String boundary;
        final String runtimeLane;
        final int elementPosition;
        final String scope;
        final String path;
        final String rule;
        final String detail;
        final boolean forbiddenHit;

        Violation(String boundary, String scope, String path,
                  String rule, String detail) {
            this(boundary, "", -1, scope, path, rule, detail, false);
        }

        Violation(String boundary, String runtimeLane, int elementPosition,
                  String scope, String path, String rule, String detail,
                  boolean forbiddenHit) {
            this.boundary = boundary;
            this.runtimeLane = runtimeLane;
            this.elementPosition = elementPosition;
            this.scope = scope;
            this.path = path;
            this.rule = rule;
            this.detail = detail;
            this.forbiddenHit = forbiddenHit;
        }
    }

    private static final class Collision {
        final String lane;
        final String kind;
        final String logicalName;
        final int firstPosition;
        final String firstRole;
        final String firstOwner;
        final String firstOrigin;
        final String firstEntry;
        final int firstRelease;
        final String firstSha;
        final int laterPosition;
        final String laterRole;
        final String laterOwner;
        final String laterOrigin;
        final String laterEntry;
        final int laterRelease;
        final String laterSha;
        final String byteRelation;
        final String disposition;

        Collision(String lane, String kind, String logicalName,
                  Claim first, Claim later, String disposition) {
            this.lane = lane;
            this.kind = kind;
            this.logicalName = logicalName;
            this.firstPosition = first.position;
            this.firstRole = first.role;
            this.firstOwner = first.owner;
            this.firstOrigin = first.origin;
            this.firstEntry = first.entry;
            this.firstRelease = first.release;
            this.firstSha = first.sha;
            this.laterPosition = later.position;
            this.laterRole = later.role;
            this.laterOwner = later.owner;
            this.laterOrigin = later.origin;
            this.laterEntry = later.entry;
            this.laterRelease = later.release;
            this.laterSha = later.sha;
            this.byteRelation = first.sha.equals(later.sha)
                    ? "BYTE_IDENTICAL" : "BYTE_DIFFERENT";
            this.disposition = disposition;
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

        boolean matches(Collision collision) {
            return lane.equals(collision.lane)
                    && internalName.equals(collision.logicalName)
                    && firstPosition == collision.firstPosition
                    && firstRole.equals(collision.firstRole)
                    && firstEntry.equals(collision.firstEntry)
                    && firstRelease == collision.firstRelease
                    && firstSha.equals(collision.firstSha)
                    && laterPosition == collision.laterPosition
                    && laterRole.equals(collision.laterRole)
                    && laterEntry.equals(collision.laterEntry)
                    && laterRelease == collision.laterRelease
                    && laterSha.equals(collision.laterSha)
                    && byteRelation.equals(collision.byteRelation);
        }
    }

    private static final class Claim {
        final int position;
        final String role;
        final String owner;
        final String origin;
        final String entry;
        final int release;
        final String sha;

        Claim(int position, String role, String owner, String origin,
              String entry, int release, String sha) {
            this.position = position;
            this.role = role;
            this.owner = owner;
            this.origin = origin;
            this.entry = entry;
            this.release = release;
            this.sha = sha;
        }
    }

    private static final class ScanState {
        final Map<String, CohortRow> cohort = new TreeMap<>();
        final Map<String, String> originalOwnership = new TreeMap<>();
        final Map<String, Integer> originalOwnershipCounts = new TreeMap<>();
        final Map<String, List<MappingRow>> mappings = new LinkedHashMap<>();
        final Map<String, String> expectedCandidateA = new TreeMap<>();
        final Map<String, String> expectedCandidateB = new TreeMap<>();
        final Map<String, OverlapProof> overlapProofs = new TreeMap<>();
        final Map<String, ArtifactPolicy> runtimePolicy = new LinkedHashMap<>();
        final Map<String, CollisionPolicyRow> candidateCollisionPolicy =
                new LinkedHashMap<>();
        final Map<String, AllowedResource> allowed = new TreeMap<>();
        final Map<String, List<ForbiddenPayload>> forbiddenBySha = new HashMap<>();
        final List<RuntimeElement> runtimeElements = new ArrayList<>();
        RuntimeElement originalElement;
        RuntimeElement peerElement;
        final List<EntryRecord> entries = new ArrayList<>();
        final List<ClassRecord> classes = new ArrayList<>();
        final List<ReferenceRecord> references = new ArrayList<>();
        final List<Violation> violations = new ArrayList<>();
        final List<Collision> collisions = new ArrayList<>();
        final List<RuntimeShadow> shadows = new ArrayList<>();
        final Map<String, Claim> claims = new HashMap<>();
        final Map<String, String> physicalOrigins = new HashMap<>();
        final Map<String, String> inputPreHashes = new LinkedHashMap<>();
        final Map<String, String> inputPostHashes = new LinkedHashMap<>();
        final Map<String, String> inputOrigins = new LinkedHashMap<>();
        final Map<String, Integer> candidateCountsA = new TreeMap<>();
        final Map<String, Integer> candidateCountsB = new TreeMap<>();
        final Set<String> actualCandidateA = new TreeSet<>();
        final Set<String> actualCandidateB = new TreeSet<>();
        int expectedCandidateRuntimeElements = -1;
        int expectedOracleRuntimeElements = -1;
        String expectedCandidateLedgerSha = "";
        String expectedOracleLedgerSha = "";
        long discoveredEntries;
        long recordedEntries;
        long discoveredInputs;
        long recordedInputs;
        String candidateAPre = "";
        String candidateAPost = "";
        String candidateBPre = "";
        String candidateBPost = "";
        boolean setupComplete;
        boolean ledgerComplete;

        ScanState() {
            for (String relation : RELATIONS) {
                mappings.put(relation, new ArrayList<>());
            }
        }

        void violation(String boundary, String scope, String path,
                       String rule, String detail) {
            violations.add(new Violation(boundary, scope, path, rule,
                    clean(detail)));
        }

        void violation(RuntimeElement element, String scope, String path,
                       String rule, String detail) {
            element.errors++;
            violations.add(new Violation("runtime-" + element.lane,
                    element.lane, element.position, scope, path, rule,
                    clean(detail), false));
        }

        void forbiddenViolation(RuntimeElement element, String scope,
                                String path, String rule, String detail) {
            element.errors++;
            element.forbiddenHits++;
            violations.add(new Violation("runtime-" + element.lane,
                    element.lane, element.position, scope, path, rule,
                    clean(detail), true));
        }

        void record(EntryRecord entry) {
            entries.add(entry);
            recordedEntries++;
        }

        void claim(String lane, String kind, String logicalName, int position,
                   String role, String owner, String origin, String entry,
                   int release, String sha, RuntimeElement element) {
            String key = lane + "\u0000" + kind + "\u0000" + logicalName;
            Claim current = new Claim(position, role, owner, origin, entry,
                    release, sha);
            Claim previous = claims.putIfAbsent(key, current);
            if (previous != null) {
                Collision collision = new Collision(lane, kind, logicalName,
                        previous, current, "UNCLASSIFIED");
                String disposition = "FORBIDDEN_DUPLICATE";
                if (lane.equals("oracle")) {
                    disposition = "ORDERED_FIRST_ORIGIN_WINS";
                } else if (lane.equals("candidate")
                        && kind.equals("class")) {
                    CollisionPolicyRow policy = candidateCollisionPolicy.get(
                            logicalName + "\u0000" + position);
                    if (policy != null && policy.matches(collision)) {
                        policy.seen = true;
                        disposition = "POLICY_BOUND_ORDERED_FIRST_ORIGIN_WINS";
                    } else {
                        violation(element, "collision", logicalName,
                                policy == null
                                        ? "missing-candidate-collision-policy"
                                        : "candidate-collision-policy-mismatch",
                                policy == null
                                        ? "candidate runtime class collision has no exact policy row"
                                        : "candidate runtime class collision differs from its policy row");
                    }
                }
                collision = new Collision(lane, kind, logicalName, previous,
                        current, disposition);
                collisions.add(collision);
                if (element != null) element.collisions++;
                if (!lane.equals("oracle")
                        && !disposition.equals(
                                "POLICY_BOUND_ORDERED_FIRST_ORIGIN_WINS")) {
                    if (element == null) {
                        violation(lane, "collision", logicalName,
                                "duplicate-logical-entry",
                                "logical " + kind + " is owned by both "
                                        + previous.owner + " and " + owner);
                    } else {
                        violation(element, "collision", logicalName,
                                "duplicate-logical-entry",
                                "logical " + kind + " is owned by both "
                                        + previous.owner + " and " + owner);
                    }
                }
            }
        }

        void claimCandidate(String boundary, String kind, String logicalName,
                            String owner, String origin, String sha) {
            claim(boundary, kind, logicalName, 0, "generated-output", owner,
                    origin, logicalName, 0, sha, null);
        }

        RuntimeElement element(String lane, int position) {
            if (lane == null || lane.isEmpty()) return null;
            if (originalElement != null && originalElement.lane.equals(lane)
                    && originalElement.position == position) {
                return originalElement;
            }
            if (peerElement != null && peerElement.lane.equals(lane)
                    && peerElement.position == position) return peerElement;
            for (RuntimeElement element : runtimeElements) {
                if (element.lane.equals(lane)
                        && element.position == position) return element;
            }
            return null;
        }
    }

    private static Path absolute(Path path) {
        return path.toAbsolutePath().normalize();
    }

    private static String clean(String value) {
        if (value == null) return "";
        return value.replace("\\", "\\\\").replace("\t", "\\t")
                .replace("\r", "\\r").replace("\n", "\\n");
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new Failure(message);
    }

    private static String hex(byte[] bytes) {
        StringBuilder result = new StringBuilder(bytes.length * 2);
        for (byte value : bytes) {
            result.append(String.format(Locale.ROOT, "%02x", value & 0xff));
        }
        return result.toString();
    }

    private static MessageDigest digest() {
        try {
            return MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException impossible) {
            throw new AssertionError(impossible);
        }
    }

    private static String sha256(byte[] bytes) {
        return hex(digest().digest(bytes));
    }

    private static String sha256(String value) {
        return sha256(value.getBytes(StandardCharsets.UTF_8));
    }

    private static String sha256(Path path) throws IOException {
        MessageDigest digest = digest();
        try (InputStream input = Files.newInputStream(path)) {
            byte[] buffer = new byte[1 << 16];
            for (int count; (count = input.read(buffer)) >= 0; ) {
                if (count > 0) digest.update(buffer, 0, count);
            }
        }
        return hex(digest.digest());
    }

    private static void safeField(String label, String value) {
        require(value != null && !value.isEmpty()
                        && value.indexOf('\t') < 0 && value.indexOf('\r') < 0
                        && value.indexOf('\n') < 0 && value.indexOf('\0') < 0,
                "unsafe " + label + ": " + clean(value));
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

    private static boolean safeZipName(String value, boolean directory) {
        if (value == null || value.isEmpty() || value.startsWith("/")
                || value.indexOf('\\') >= 0 || value.indexOf('\0') >= 0) {
            return false;
        }
        String normalized = directory && value.endsWith("/")
                ? value.substring(0, value.length() - 1) : value;
        return !normalized.isEmpty() && safeRelative(normalized);
    }

    private static String[] header(String line, String path) {
        require(line != null, "empty TSV: " + path);
        return line.split("\t", -1);
    }

    private static int column(String[] header, String name) {
        for (int index = 0; index < header.length; index++) {
            if (header[index].equals(name)) return index;
        }
        throw new Failure("TSV column missing: " + name);
    }

    private static String[] row(String line, int width, String label,
                                int number) {
        String[] fields = line.split("\t", -1);
        require(fields.length == width,
                "malformed " + label + " row " + number);
        return fields;
    }

    private static Path canonicalFile(Path path) throws IOException {
        Path absolute = absolute(path);
        require(Files.isRegularFile(absolute, LinkOption.NOFOLLOW_LINKS),
                "not a regular file: " + absolute);
        require(!Files.isSymbolicLink(absolute),
                "symbolic-link file is forbidden: " + absolute);
        Path real = absolute.toRealPath(LinkOption.NOFOLLOW_LINKS);
        require(real.equals(absolute),
                "file origin is not canonical: " + absolute + " -> " + real);
        return real;
    }

    private static Path canonicalDirectory(Path path) throws IOException {
        Path absolute = absolute(path);
        require(Files.isDirectory(absolute, LinkOption.NOFOLLOW_LINKS),
                "not a directory: " + absolute);
        require(!Files.isSymbolicLink(absolute),
                "symbolic-link directory is forbidden: " + absolute);
        Path real = absolute.toRealPath(LinkOption.NOFOLLOW_LINKS);
        require(real.equals(absolute),
                "directory origin is not canonical: " + absolute + " -> " + real);
        return real;
    }

    private static boolean overlaps(Path left, Path right) {
        return left.equals(right) || left.startsWith(right)
                || right.startsWith(left);
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
            if (Files.isSymbolicLink(path)) {
                line = "L\t-\t0\t" + relative + "\n";
            } else if (Files.isDirectory(path, LinkOption.NOFOLLOW_LINKS)) {
                line = "D\t-\t0\t" + relative + "\n";
            } else if (Files.isRegularFile(path, LinkOption.NOFOLLOW_LINKS)) {
                line = "F\t" + sha256(path) + "\t" + Files.size(path)
                        + "\t" + relative + "\n";
            } else {
                line = "S\t-\t0\t" + relative + "\n";
            }
            digest.update(line.getBytes(StandardCharsets.UTF_8));
        }
        return hex(digest.digest());
    }

    private static void readCohort(Path path, ScanState state)
            throws IOException {
        try (BufferedReader reader = Files.newBufferedReader(path,
                StandardCharsets.UTF_8)) {
            String[] columns = header(reader.readLine(), path.toString());
            int namespace = column(columns, "namespace");
            int expected = column(columns, "expected_classes");
            int number = 1;
            for (String line; (line = reader.readLine()) != null; ) {
                number++;
                String[] fields = row(line, columns.length, "cohort", number);
                safeField("namespace", fields[namespace]);
                int count;
                try { count = Integer.parseInt(fields[expected]); }
                catch (NumberFormatException bad) {
                    throw new Failure("invalid expected class count in cohort row "
                            + number);
                }
                require(count > 0, "nonpositive expected class count in cohort row "
                        + number);
                require(state.cohort.put(fields[namespace],
                        new CohortRow(fields[namespace], count)) == null,
                        "duplicate cohort namespace: " + fields[namespace]);
            }
        }
        require(!state.cohort.isEmpty(), "empty exact-AOT cohort");
    }

    private static void readOwnership(Path path, ScanState state)
            throws IOException {
        try (BufferedReader reader = Files.newBufferedReader(path,
                StandardCharsets.UTF_8)) {
            String[] columns = header(reader.readLine(), path.toString());
            int entry = column(columns, "entry");
            int owner = column(columns, "owner_id");
            Set<String> ownedEntries = new HashSet<>();
            int number = 1;
            for (String line; (line = reader.readLine()) != null; ) {
                number++;
                String[] fields = row(line, columns.length, "ownership", number);
                if (!state.cohort.containsKey(fields[owner])) continue;
                require(fields[entry].endsWith(".class")
                                && safeRelative(fields[entry]),
                        "unsafe owned class entry in row " + number + ": "
                                + clean(fields[entry]));
                require(ownedEntries.add(fields[entry]),
                        "duplicate owned class entry: " + fields[entry]);
                String internal = fields[entry].substring(0,
                        fields[entry].length() - 6);
                require(state.originalOwnership.put(internal,
                                fields[owner]) == null,
                        "duplicate owned original class: " + internal);
                state.originalOwnershipCounts.merge(fields[owner], 1,
                        Integer::sum);
            }
        }
        for (CohortRow cohort : state.cohort.values()) {
            int actual = state.originalOwnershipCounts.getOrDefault(
                    cohort.namespace, 0);
            require(actual == cohort.expectedClasses,
                    "cohort/ownership count differs for " + cohort.namespace
                            + ": " + actual + " vs " + cohort.expectedClasses);
        }
    }

    private static void readMappings(Path path, ScanState state)
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
                require(safeRelative(fields[2] + ".class")
                                && safeRelative(fields[3] + ".class"),
                        "unsafe mapped class in row " + number);
                require(Set.of("true", "false").contains(fields[4])
                                && Boolean.parseBoolean(fields[4])
                                        == fields[2].equals(fields[3]),
                        "mapping identity bit differs from class names in row "
                                + number);
                require(exactRows.add(line),
                        "duplicate class-mapping row " + number);
                state.mappings.get(fields[0]).add(new MappingRow(fields[0],
                        fields[1], fields[2], fields[3],
                        Boolean.parseBoolean(fields[4])));
            }
        }
        for (String relation : RELATIONS) {
            require(!state.mappings.get(relation).isEmpty(),
                    "mapping relation is empty: " + relation);
        }
        deriveExactCandidateMembership(state);
    }

    private static Map<String, String> exactMapping(String relation,
            ScanState state) {
        Map<String, String> result = new TreeMap<>();
        Set<String> right = new TreeSet<>();
        for (MappingRow mapping : state.mappings.get(relation)) {
            require(result.put(mapping.left, mapping.right) == null,
                    "duplicate mapping left class in " + relation + ": "
                            + mapping.left);
            require(right.add(mapping.right),
                    "duplicate mapping right class in " + relation + ": "
                            + mapping.right);
        }
        require(result.size() == state.originalOwnership.size(),
                "mapping cardinality differs from exact ownership in "
                        + relation + ": " + result.size() + " vs "
                        + state.originalOwnership.size());
        return result;
    }

    private static void deriveExactCandidateMembership(ScanState state) {
        Map<String, String> ao = exactMapping("candidate-a--original", state);
        Map<String, String> bo = exactMapping("candidate-b--original", state);
        Map<String, String> ab = exactMapping("candidate-a--candidate-b", state);
        Map<String, String> originalToA = new TreeMap<>();
        Map<String, String> originalToB = new TreeMap<>();
        for (Map.Entry<String, String> mapping : ao.entrySet()) {
            require(state.originalOwnership.containsKey(mapping.getValue()),
                    "candidate-a mapping targets an unowned original class: "
                            + mapping.getValue());
            require(originalToA.put(mapping.getValue(), mapping.getKey()) == null,
                    "candidate-a mapping is not invertible at "
                            + mapping.getValue());
            state.expectedCandidateA.put(mapping.getKey(),
                    state.originalOwnership.get(mapping.getValue()));
        }
        for (Map.Entry<String, String> mapping : bo.entrySet()) {
            require(state.originalOwnership.containsKey(mapping.getValue()),
                    "candidate-b mapping targets an unowned original class: "
                            + mapping.getValue());
            require(originalToB.put(mapping.getValue(), mapping.getKey()) == null,
                    "candidate-b mapping is not invertible at "
                            + mapping.getValue());
            state.expectedCandidateB.put(mapping.getKey(),
                    state.originalOwnership.get(mapping.getValue()));
        }
        require(originalToA.keySet().equals(state.originalOwnership.keySet())
                        && originalToB.keySet().equals(
                                state.originalOwnership.keySet()),
                "candidate/original mappings do not exactly cover ownership");
        require(ab.keySet().equals(state.expectedCandidateA.keySet())
                        && new TreeSet<>(ab.values()).equals(
                                state.expectedCandidateB.keySet()),
                "candidate-a/candidate-b mapping does not exactly cover both candidates");
        for (String original : state.originalOwnership.keySet()) {
            String candidateA = originalToA.get(original);
            String candidateB = originalToB.get(original);
            require(candidateB.equals(ab.get(candidateA)),
                    "three-way mapping is inconsistent at original class "
                            + original);
            state.overlapProofs.put(original, new OverlapProof(original,
                    candidateA, candidateB));
        }
    }

    private static void readAllowedResources(Path path, ScanState state)
            throws IOException {
        try (BufferedReader reader = Files.newBufferedReader(path,
                StandardCharsets.UTF_8)) {
            String expectedHeader = "candidate\tpath\towner\tsha256";
            require(expectedHeader.equals(reader.readLine()),
                    "unexpected allowed-resource header");
            int number = 1;
            for (String line; (line = reader.readLine()) != null; ) {
                number++;
                String[] fields = row(line, 4, "allowed-resource", number);
                require(Set.of("candidate-a", "candidate-b", "both")
                                .contains(fields[0]),
                        "invalid allowed-resource candidate in row " + number);
                require(safeRelative(fields[1])
                                && !fields[1].endsWith(".class"),
                        "unsafe or class-valued allowed resource in row " + number);
                safeField("allowed-resource owner", fields[2]);
                require(SHA256.matcher(fields[3]).matches(),
                        "invalid allowed-resource SHA-256 in row " + number);
                List<String> boundaries = fields[0].equals("both")
                        ? List.of("candidate-a", "candidate-b")
                        : List.of(fields[0]);
                for (String boundary : boundaries) {
                    AllowedResource resource = new AllowedResource(boundary,
                            fields[1], fields[2], fields[3]);
                    require(state.allowed.put(resource.key(), resource) == null,
                            "duplicate allowed resource: " + boundary + ":"
                                    + fields[1]);
                }
            }
        }
    }

    private static void readForbiddenPayloads(Path path, ScanState state)
            throws IOException {
        Set<String> rules = new HashSet<>();
        try (BufferedReader reader = Files.newBufferedReader(path,
                StandardCharsets.UTF_8)) {
            require("rule_id\tsha256\treason".equals(reader.readLine()),
                    "unexpected forbidden-payload header");
            int number = 1;
            for (String line; (line = reader.readLine()) != null; ) {
                number++;
                String[] fields = row(line, 3, "forbidden-payload", number);
                safeField("forbidden rule", fields[0]);
                safeField("forbidden reason", fields[2]);
                require(rules.add(fields[0]),
                        "duplicate forbidden-payload rule: " + fields[0]);
                require(SHA256.matcher(fields[1]).matches(),
                        "invalid forbidden-payload SHA-256 in row " + number);
                ForbiddenPayload rule = new ForbiddenPayload(fields[0],
                        fields[1], fields[2]);
                state.forbiddenBySha.computeIfAbsent(rule.sha,
                        ignored -> new ArrayList<>()).add(rule);
            }
        }
        require(!rules.isEmpty(), "forbidden-payload policy is empty");
    }

    private static void readRuntime(Path path, String lane, ScanState state)
            throws IOException {
        try (BufferedReader reader = Files.newBufferedReader(path,
                StandardCharsets.UTF_8)) {
            String expectedHeader = "position\trole\tkind\tpath"
                    + "\texpected_sha256";
            require(expectedHeader.equals(reader.readLine()),
                    "unexpected runtime-boundary header");
            int expectedPosition = 1;
            int number = 1;
            for (String line; (line = reader.readLine()) != null; ) {
                number++;
                String[] fields = row(line, 5, "runtime-boundary", number);
                int position;
                try { position = Integer.parseInt(fields[0]); }
                catch (NumberFormatException bad) {
                    throw new Failure("invalid runtime position in row " + number);
                }
                require(position == expectedPosition++,
                        "runtime positions are not contiguous at " + position);
                safeField("runtime role", fields[1]);
                require(Set.of("jar", "directory").contains(fields[2]),
                        "unsupported runtime kind: " + fields[2]);
                require(SHA256.matcher(fields[4]).matches(),
                        "invalid runtime SHA-256 in row " + number);
                state.runtimeElements.add(new RuntimeElement(lane, position,
                        fields[1], fields[2], absolute(Path.of(fields[3])),
                        fields[4]));
            }
        }
        require(state.runtimeElements.stream().anyMatch(
                        element -> element.lane.equals(lane)),
                lane + " runtime boundary is empty");
    }

    private static void readCandidateCollisionPolicy(Path path,
            ScanState state) throws IOException {
        try (BufferedReader reader = Files.newBufferedReader(path,
                StandardCharsets.UTF_8)) {
            require(CANDIDATE_COLLISION_HEADER.equals(reader.readLine()),
                    "unexpected candidate-collision-policy header");
            int number = 1;
            for (String line; (line = reader.readLine()) != null;) {
                number++;
                String[] fields = row(line, 13, "candidate-collision-policy",
                        number);
                require(fields[0].equals("candidate"),
                        "collision policy may describe only candidate lane rows");
                require(!fields[1].endsWith(".class")
                                && safeRelative(fields[1] + ".class"),
                        "unsafe collision internal name in row " + number);
                safeField("collision first role", fields[3]);
                safeField("collision later role", fields[8]);
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

    private static void readRuntimePolicy(Path path, ScanState state)
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
                    "unexpected runtime-policy header");
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
                require(fields[2].equals("REQUIRE") == !fields[3].isEmpty()
                                && fields[4].equals("REQUIRE")
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
        require(state.runtimePolicy.values().stream().map(policy -> policy.sha)
                        .collect(Collectors.toSet()).size()
                        == state.runtimePolicy.size(),
                "runtime policy aliases two pinned identities");
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
        require(slash > 0, "malformed multi-release class path: " + name);
        String version = rest.substring(0, slash);
        require(version.matches("[0-9]+"),
                "nonnumeric multi-release class version: " + name);
        int release;
        try { release = Integer.parseInt(version); }
        catch (NumberFormatException bad) {
            throw new Failure("oversized multi-release class version: " + name);
        }
        require(release >= 9, "multi-release class version is below 9: " + name);
        String internalPath = rest.substring(slash + 1);
        require(safeRelative(internalPath),
                "unsafe multi-release class path: " + name);
        return new ClassPathIdentity(internalPath, release);
    }

    private static byte[] readLimited(InputStream input, long declared,
                                      String label) throws IOException {
        require(declared < 0 || declared <= MAX_ENTRY_BYTES,
                label + " exceeds byte limit " + MAX_ENTRY_BYTES);
        ByteArrayOutputStream output = new ByteArrayOutputStream(
                declared > 0 && declared < Integer.MAX_VALUE
                        ? (int) declared : 8192);
        byte[] buffer = new byte[1 << 16];
        long total = 0;
        for (int count; (count = input.read(buffer)) >= 0; ) {
            if (count == 0) continue;
            total += count;
            require(total <= MAX_ENTRY_BYTES,
                    label + " exceeds byte limit " + MAX_ENTRY_BYTES);
            output.write(buffer, 0, count);
        }
        require(declared < 0 || declared == total,
                label + " declared size differs from streamed bytes: "
                        + declared + " vs " + total);
        return output.toByteArray();
    }

    private static boolean archiveMagic(byte[] bytes) {
        return bytes.length >= 4 && bytes[0] == 0x50 && bytes[1] == 0x4b
                && ((bytes[2] == 0x03 && bytes[3] == 0x04)
                    || (bytes[2] == 0x05 && bytes[3] == 0x06)
                    || (bytes[2] == 0x07 && bytes[3] == 0x08));
    }

    private static boolean archiveName(String name) {
        String lower = name.toLowerCase(Locale.ROOT);
        return lower.endsWith(".jar") || lower.endsWith(".zip")
                || lower.endsWith(".war") || lower.endsWith(".ear");
    }

    private static boolean keystoreName(String name) {
        String lower = name.toLowerCase(Locale.ROOT);
        return lower.endsWith(".jks") || lower.endsWith(".jceks")
                || lower.endsWith(".p12") || lower.endsWith(".pfx")
                || lower.endsWith(".keystore") || lower.endsWith(".truststore");
    }

    private static boolean keystoreMagic(byte[] bytes) {
        if (bytes.length < 4) return false;
        long magic = ((bytes[0] & 0xffL) << 24)
                | ((bytes[1] & 0xffL) << 16)
                | ((bytes[2] & 0xffL) << 8) | (bytes[3] & 0xffL);
        return magic == 0xfeedfeedL || magic == 0xcecececeL;
    }

    private static void checkLogicalName(ScanState state, String boundary,
                                         String scope, String path) {
        String slash = path.replace('\\', '/');
        if (FORBIDDEN_ORIGINAL_ARCHIVE.matcher(slash).find()) {
            state.violation(boundary, scope, path,
                    "forbidden-original-logical-name",
                    "logical name matches a licensed Datomic/Nano artifact name");
        }
    }

    private static void checkPayload(ScanState state, EntryRecord entry,
                                     byte[] bytes, boolean topLevelJar) {
        checkPayload(state, entry, bytes, topLevelJar, null);
    }

    private static void checkPayload(ScanState state, EntryRecord entry,
                                     byte[] bytes, boolean topLevelJar,
                                     RuntimeElement element) {
        int violationsBefore = state.violations.size();
        int forbiddenBefore = 0;
        boolean candidateContent = element == null
                || element.lane.equals("candidate");
        boolean archive = archiveMagic(bytes);
        boolean archiveNamed = archiveName(entry.logicalPath);
        if (!topLevelJar && element != null && archiveNamed && !archive) {
            entry.status = "FAIL";
            state.violation(entry.boundary, "payload", entry.logicalPath,
                    "archive-named-non-zip",
                    "runtime resource has an archive name without ZIP magic");
        }
        if (candidateContent) {
            for (ForbiddenPayload rule : state.forbiddenBySha.getOrDefault(
                    entry.sha, Collections.emptyList())) {
                forbiddenBefore++;
                entry.status = "FAIL";
                state.violation(entry.boundary, "payload", entry.logicalPath,
                        rule.rule,
                        "forbidden SHA-256 payload: " + rule.reason);
            }
            checkLogicalName(state, entry.boundary, "entry-name",
                    entry.logicalPath);
            if (!topLevelJar && element == null
                    && (archive || archiveNamed)) {
                entry.status = "FAIL";
                state.violation(entry.boundary, "payload", entry.logicalPath,
                        "raw-nested-archive",
                        archive ? "resource payload has ZIP magic"
                                : "archive-named resource lacks ZIP magic");
            }
            if (keystoreName(entry.logicalPath) || keystoreMagic(bytes)) {
                entry.status = "FAIL";
                state.violation(entry.boundary, "payload", entry.logicalPath,
                        "raw-keystore-resource",
                        "keystore name or magic is forbidden at the candidate boundary");
            }
        }
        if (element != null) {
            element.errors += state.violations.size() - violationsBefore;
            element.forbiddenHits += forbiddenBefore;
        }
    }

    private static void checkCanonicalNode(ScanState state, String boundary,
                                           Path path, String relative) {
        try {
            Path absolute = absolute(path);
            Path real = absolute.toRealPath(LinkOption.NOFOLLOW_LINKS);
            if (!absolute.equals(real)) {
                state.violation(boundary, "origin", relative,
                        "noncanonical-origin", absolute + " -> " + real);
            }
            String physical = physicalKey(absolute);
            String previous = state.physicalOrigins.putIfAbsent(physical,
                    boundary + ":" + relative);
            if (previous != null) {
                state.violation(boundary, "origin", relative,
                        "physical-origin-collision",
                        "filesystem node aliases " + previous);
            }
        } catch (IOException failure) {
            state.violation(boundary, "origin", relative,
                    "unresolved-origin", failure.getMessage());
        }
    }

    private static void scanCandidate(String boundary, Path root,
                                      ScanState state) throws IOException {
        Map<String, Integer> counts = boundary.equals("candidate-a")
                ? state.candidateCountsA : state.candidateCountsB;
        for (Path path : tree(root)) {
            String relative = root.relativize(path).toString()
                    .replace('\\', '/');
            state.discoveredEntries++;
            EntryRecord entry = new EntryRecord(boundary, 0, "unresolved",
                    path.toString(), "filesystem-node", relative, 1);
            try {
                if (Files.isSymbolicLink(path)) {
                    entry.status = "FAIL";
                    entry.kind = "symbolic-link";
                    state.violation(boundary, "filesystem", relative,
                            "symbolic-link", "candidate tree contains a symbolic link");
                } else if (Files.isDirectory(path, LinkOption.NOFOLLOW_LINKS)) {
                    entry.kind = "directory";
                    checkCanonicalNode(state, boundary, path, relative);
                } else if (!Files.isRegularFile(path,
                        LinkOption.NOFOLLOW_LINKS)) {
                    entry.status = "FAIL";
                    entry.kind = "special-file";
                    state.violation(boundary, "filesystem", relative,
                            "special-file", "candidate tree entry is not regular");
                } else {
                    checkCanonicalNode(state, boundary, path, relative);
                    byte[] bytes;
                    try (InputStream input = Files.newInputStream(path)) {
                        bytes = readLimited(input, Files.size(path),
                                boundary + ":" + relative);
                    }
                    entry.bytes = bytes.length;
                    entry.sha = sha256(bytes);
                    if (relative.endsWith(".class")) {
                        entry.kind = "class";
                        Path logical = Path.of(relative);
                        require(logical.getNameCount() >= 2,
                                "candidate class lacks namespace container: "
                                        + relative);
                        String namespace = logical.getName(0).toString();
                        require(state.cohort.containsKey(namespace),
                                "candidate class has unknown namespace container: "
                                        + namespace);
                        String internalPath = logical.subpath(1,
                                logical.getNameCount()).toString()
                                .replace('\\', '/');
                        require(internalPath.endsWith(".class"),
                                "candidate class path is malformed: " + relative);
                        String expectedInternal = internalPath.substring(0,
                                internalPath.length() - 6);
                        Map<String, String> expectedMembership = boundary
                                .equals("candidate-a")
                                ? state.expectedCandidateA
                                : state.expectedCandidateB;
                        require(expectedMembership.containsKey(expectedInternal),
                                "candidate class is not an exact mapped member: "
                                        + boundary + ":" + expectedInternal);
                        require(expectedMembership.get(expectedInternal)
                                        .equals(namespace),
                                "candidate class is in the wrong namespace container: "
                                        + relative);
                        entry.internalName = scanClass(state, boundary,
                                boundary, 0, namespace, relative,
                                expectedInternal, 0, true, bytes);
                        entry.owner = namespace;
                        counts.merge(namespace, 1, Integer::sum);
                        (boundary.equals("candidate-a")
                                ? state.actualCandidateA
                                : state.actualCandidateB).add(entry.internalName);
                        state.claimCandidate(boundary, "class",
                                entry.internalName,
                                namespace + ":" + relative, path.toString(),
                                entry.sha);
                    } else {
                        entry.kind = "resource";
                        AllowedResource allowed = state.allowed.get(boundary
                                + "\u0000" + relative);
                        require(allowed != null,
                                "unaccounted candidate resource: " + boundary
                                        + ":" + relative);
                        require(allowed.sha.equals(entry.sha),
                                "allowed candidate resource hash differs: "
                                        + boundary + ":" + relative);
                        require(!allowed.seen,
                                "allowed candidate resource seen twice: "
                                        + boundary + ":" + relative);
                        allowed.seen = true;
                        entry.owner = allowed.owner;
                        checkPayload(state, entry, bytes, false);
                        if (archiveMagic(bytes)) {
                            scanNestedArchive(state, boundary, 0,
                                    allowed.owner, relative, bytes, 1);
                        }
                        state.claimCandidate(boundary, "resource", relative,
                                allowed.owner, path.toString(), entry.sha);
                    }
                }
            } catch (Throwable failure) {
                entry.status = "FAIL";
                state.violation(boundary, "entry", relative,
                        "scan-failure", failure.getMessage());
            } finally {
                state.record(entry);
            }
        }
    }

    private static void scanRuntimeDirectory(RuntimeElement element,
                                             ScanState state)
            throws IOException {
        String boundary = "runtime-" + element.lane;
        for (Path path : tree(element.path)) {
            String relative = element.path.relativize(path).toString()
                    .replace('\\', '/');
            String owner = element.owner();
            state.discoveredEntries++;
            element.discovered++;
            EntryRecord entry = new EntryRecord(boundary, element.position,
                    owner, path.toString(), "filesystem-node", relative, 1);
            try {
                if (Files.isSymbolicLink(path)) {
                    entry.status = "FAIL";
                    entry.kind = "symbolic-link";
                    element.other++;
                    state.violation(element, "filesystem", relative,
                            "symbolic-link", "runtime tree contains a symbolic link");
                } else if (Files.isDirectory(path, LinkOption.NOFOLLOW_LINKS)) {
                    entry.kind = "directory";
                    element.directories++;
                    int before = state.violations.size();
                    checkCanonicalNode(state, boundary, path, relative);
                    element.errors += state.violations.size() - before;
                } else if (!Files.isRegularFile(path,
                        LinkOption.NOFOLLOW_LINKS)) {
                    entry.status = "FAIL";
                    entry.kind = "special-file";
                    element.other++;
                    state.violation(element, "filesystem", relative,
                            "special-file", "runtime tree entry is not regular");
                } else {
                    int before = state.violations.size();
                    checkCanonicalNode(state, boundary, path, relative);
                    element.errors += state.violations.size() - before;
                    byte[] bytes;
                    try (InputStream input = Files.newInputStream(path)) {
                        bytes = readLimited(input, Files.size(path),
                                element.role + ":" + relative);
                    }
                    scanRuntimePayload(state, element, entry, relative, bytes,
                            false, 0, !element.lane.equals("inventory"));
                }
            } catch (Throwable failure) {
                entry.status = "FAIL";
                element.other++;
                state.violation(element, "entry", relative,
                        "scan-failure", failure.getMessage());
            } finally {
                state.record(entry);
                element.recorded++;
            }
        }
        element.scanned = true;
    }

    private static void scanRuntimePayload(ScanState state,
                                           RuntimeElement element,
                                           EntryRecord entry,
                                           String logicalPath, byte[] bytes,
                                           boolean topLevelJar, int release,
                                           boolean active) {
        entry.bytes = bytes.length;
        entry.sha = sha256(bytes);
        element.bytes += bytes.length;
        if (element.position != 0 && !element.lane.equals("inventory")) {
            checkPayload(state, entry, bytes, topLevelJar, element);
        }
        if (logicalPath.endsWith(".class")) {
            entry.kind = "class";
            ClassPathIdentity identity = archiveClassPath(logicalPath);
            require(identity.internalPath.endsWith(".class"),
                    "malformed runtime class path: " + logicalPath);
            String expectedInternal = identity.internalPath.substring(0,
                    identity.internalPath.length() - 6);
            int actualRelease = release == 0 ? identity.release : release;
            entry.internalName = scanClass(state, "runtime-" + element.lane,
                    element.lane, element.position, entry.owner, logicalPath,
                    expectedInternal, actualRelease, active, bytes);
            element.classes++;
            if (active && !entry.internalName.equals("module-info")) {
                state.claim(element.lane, "class", entry.internalName,
                        element.position, element.role, entry.owner,
                        entry.origin, logicalPath, actualRelease, entry.sha,
                        element);
                recordRuntimeShadow(state, element, entry);
            }
        } else {
            entry.kind = "resource";
            element.resources++;
            if (!topLevelJar && archiveMagic(bytes)) {
                scanNestedArchive(state, element, entry.owner, logicalPath,
                        bytes, 1);
            }
        }
    }

    private static void recordRuntimeShadow(ScanState state,
                                            RuntimeElement element,
                                            EntryRecord entry) {
        if (element.position == 0 || element.lane.equals("inventory")) return;
        boolean ownedName = state.originalOwnership.containsKey(
                entry.internalName)
                || state.expectedCandidateA.containsKey(entry.internalName)
                || state.expectedCandidateB.containsKey(entry.internalName);
        if (!ownedName) return;
        OverlapProof proof = state.overlapProofs.get(entry.internalName);
        if (proof == null) {
            state.violation(element, "shadow", entry.internalName,
                    "undeclared-owned-shadow",
                    "runtime class matches a candidate name without an original-right mapping proof");
            return;
        }
        String candidateA = element.lane.equals("candidate")
                ? proof.candidateADisposition() : "NOT_APPLICABLE";
        String candidateB = element.lane.equals("candidate")
                ? proof.candidateBDisposition() : "NOT_APPLICABLE";
        String oracle = element.lane.equals("oracle")
                ? "CHILD_FIRST_OWNED" : "NOT_APPLICABLE";
        state.shadows.add(new RuntimeShadow(element, entry.internalName,
                entry.origin, entry.sha, candidateA, candidateB, oracle));
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

    private static void scanRuntimeJar(RuntimeElement element,
                                       ScanState state) throws IOException {
        String boundary = "runtime-" + element.lane;
        String owner = element.owner();
        Set<String> names = new HashSet<>();
        Set<String> classReleases = new HashSet<>();
        Map<String, Integer> occurrences = new HashMap<>();
        int observed = 0;
        try (ZipFile zip = new ZipFile(element.path.toFile())) {
            List<ZipEntry> entries = new ArrayList<>();
            Enumeration<? extends ZipEntry> enumeration = zip.entries();
            while (enumeration.hasMoreElements()) entries.add(enumeration.nextElement());
            entries.sort(Comparator.comparing(ZipEntry::getName));
            boolean multiRelease = multiRelease(zip);
            Map<String, Integer> effectiveRelease = new HashMap<>();
            for (ZipEntry zipEntry : entries) {
                if (zipEntry.isDirectory()
                        || !zipEntry.getName().endsWith(".class")) continue;
                ClassPathIdentity identity = archiveClassPath(zipEntry.getName());
                if (identity.release == 0
                        || (multiRelease && identity.release <= JAVA_FEATURE)) {
                    String internal = identity.internalPath.substring(0,
                            identity.internalPath.length() - 6);
                    effectiveRelease.merge(internal, identity.release,
                            Math::max);
                }
            }
            for (ZipEntry zipEntry : entries) {
                observed++;
                String name = zipEntry.getName();
                int occurrence = occurrences.merge(name, 1, Integer::sum);
                state.discoveredEntries++;
                element.discovered++;
                EntryRecord entry = new EntryRecord(boundary, element.position,
                        owner, element.path + "!/" + name, "archive-entry",
                        name, occurrence);
                try {
                    if (!names.add(name)) {
                        entry.status = "FAIL";
                        state.violation(element, "archive", name,
                                "duplicate-archive-entry",
                                "archive contains a duplicate logical name");
                    }
                    require(safeZipName(name, zipEntry.isDirectory()),
                            "unsafe archive entry name: " + clean(name));
                    if (!zipEntry.isDirectory()) {
                        byte[] bytes;
                        try (InputStream input = zip.getInputStream(zipEntry)) {
                            bytes = readLimited(input, zipEntry.getSize(),
                                    element.role + "!/" + name);
                        }
                        ClassPathIdentity identity = name.endsWith(".class")
                                ? archiveClassPath(name) : null;
                        if (identity != null) {
                            String internal = identity.internalPath.substring(0,
                                    identity.internalPath.length() - 6);
                            require(classReleases.add(internal + "@"
                                            + identity.release),
                                    "duplicate runtime class/release " + internal
                                            + "@" + identity.release);
                        }
                        boolean active = identity != null
                                && !element.lane.equals("inventory")
                                && effectiveRelease.getOrDefault(
                                        identity.internalPath.substring(0,
                                                identity.internalPath.length() - 6),
                                        -1) == identity.release;
                        scanRuntimePayload(state, element, entry, name, bytes,
                                false, identity == null ? 0 : identity.release,
                                active);
                    } else {
                        entry.kind = "directory";
                        element.directories++;
                    }
                } catch (Throwable failure) {
                    entry.status = "FAIL";
                    element.other++;
                    state.violation(element, "archive-entry", name,
                            "scan-failure", failure.getMessage());
                } finally {
                    state.record(entry);
                    element.recorded++;
                }
            }
        }
        try {
            byte[] archive;
            try (InputStream input = Files.newInputStream(element.path)) {
                archive = readLimited(input, Files.size(element.path),
                        element.role + " archive envelope");
            }
            String envelope = zipEnvelopeViolation(archive, observed, true);
            if (envelope != null) {
                state.violation(element, "archive", element.role,
                        "archive-envelope", envelope);
            }
        } catch (Throwable failure) {
            state.violation(element, "archive", element.role,
                    "archive-envelope-scan", failure.getMessage());
        }
        element.scanned = true;
    }

    private static void scanNestedArchive(ScanState state,
                                          RuntimeElement element,
                                          String owner, String parent,
                                          byte[] archive, int depth) {
        String boundary = "runtime-" + element.lane;
        if (depth > MAX_NESTED_DEPTH) {
            state.violation(element, "nested-archive", parent,
                    "nested-archive-depth",
                    "nested archive depth exceeds " + MAX_NESTED_DEPTH);
            return;
        }
        Set<String> names = new HashSet<>();
        Map<String, Integer> occurrences = new HashMap<>();
        int observed = 0;
        try (ZipInputStream zip = new ZipInputStream(
                new ByteArrayInputStream(archive))) {
            for (ZipEntry zipEntry; (zipEntry = zip.getNextEntry()) != null; ) {
                observed++;
                String name = zipEntry.getName();
                String logical = parent + "!/" + name;
                int occurrence = occurrences.merge(name, 1, Integer::sum);
                state.discoveredEntries++;
                element.discovered++;
                EntryRecord entry = new EntryRecord(boundary, element.position,
                        owner, logical, "nested-archive-entry", logical,
                        occurrence);
                try {
                    if (!names.add(name)) {
                        entry.status = "FAIL";
                        state.violation(element, "nested-archive", logical,
                                "duplicate-archive-entry",
                                "nested archive contains duplicate logical name");
                    }
                    require(safeZipName(name, zipEntry.isDirectory()),
                            "unsafe nested archive entry name: " + clean(name));
                    if (!zipEntry.isDirectory()) {
                        byte[] bytes = readLimited(zip, zipEntry.getSize(), logical);
                        entry.bytes = bytes.length;
                        entry.sha = sha256(bytes);
                        element.bytes += bytes.length;
                        checkPayload(state, entry, bytes, false, element);
                        if (name.endsWith(".class")) {
                            entry.kind = "nested-class";
                            element.classes++;
                            ClassPathIdentity identity = archiveClassPath(name);
                            String internalPath = identity.internalPath;
                            require(internalPath.endsWith(".class"),
                                    "malformed nested class path: " + logical);
                            entry.internalName = scanClass(state,
                                    "runtime-nested", element.lane,
                                    element.position, owner, logical,
                                    internalPath.substring(0,
                                            internalPath.length() - 6),
                                    identity.release, false, bytes);
                        }
                        if (archiveMagic(bytes)) {
                            scanNestedArchive(state, element, owner, logical,
                                    bytes, depth + 1);
                        }
                    } else {
                        entry.kind = "nested-directory";
                        element.directories++;
                    }
                } catch (Throwable failure) {
                    entry.status = "FAIL";
                    element.other++;
                    state.violation(element, "nested-archive", logical,
                            "scan-failure", failure.getMessage());
                } finally {
                    state.record(entry);
                    element.recorded++;
                }
            }
            String envelope = zipEnvelopeViolation(archive, observed, false);
            if (envelope != null) {
                state.violation(element, "nested-archive", parent,
                        "archive-envelope", envelope);
            }
        } catch (Throwable failure) {
            state.violation(element, "nested-archive", parent,
                    "nested-archive-scan", failure.getMessage());
        }
    }

    /* Generated candidate-output nested archives are forbidden, but every
     * reachable entry is still inventoried so the failing ledger is complete. */
    private static void scanNestedArchive(ScanState state, String boundary,
                                          int elementPosition, String owner,
                                          String parent, byte[] archive,
                                          int depth) {
        if (depth > MAX_NESTED_DEPTH) {
            state.violation(boundary, "nested-archive", parent,
                    "nested-archive-depth",
                    "nested archive depth exceeds " + MAX_NESTED_DEPTH);
            return;
        }
        Set<String> names = new HashSet<>();
        Map<String, Integer> occurrences = new HashMap<>();
        int observed = 0;
        try (ZipInputStream zip = new ZipInputStream(
                new ByteArrayInputStream(archive))) {
            for (ZipEntry zipEntry; (zipEntry = zip.getNextEntry()) != null;) {
                observed++;
                String name = zipEntry.getName();
                String logical = parent + "!/" + name;
                int occurrence = occurrences.merge(name, 1, Integer::sum);
                state.discoveredEntries++;
                EntryRecord entry = new EntryRecord(boundary, elementPosition,
                        owner, logical, "nested-archive-entry", logical,
                        occurrence);
                try {
                    require(names.add(name),
                            "nested archive contains duplicate entry " + name);
                    require(safeZipName(name, zipEntry.isDirectory()),
                            "unsafe nested archive entry name: " + clean(name));
                    if (zipEntry.isDirectory()) {
                        entry.kind = "nested-directory";
                    } else {
                        byte[] bytes = readLimited(zip, zipEntry.getSize(),
                                logical);
                        entry.bytes = bytes.length;
                        entry.sha = sha256(bytes);
                        checkPayload(state, entry, bytes, false);
                        if (name.endsWith(".class")) {
                            entry.kind = "nested-class";
                            ClassPathIdentity identity = archiveClassPath(name);
                            entry.internalName = scanClass(state,
                                    "candidate-nested", "", 0, owner, logical,
                                    identity.internalPath.substring(0,
                                            identity.internalPath.length() - 6),
                                    identity.release, false, bytes);
                        }
                        if (archiveMagic(bytes)) {
                            scanNestedArchive(state, boundary, elementPosition,
                                    owner, logical, bytes, depth + 1);
                        }
                    }
                } catch (Throwable failure) {
                    entry.status = "FAIL";
                    state.violation(boundary, "nested-archive", logical,
                            "scan-failure", failure.getMessage());
                } finally {
                    state.record(entry);
                }
            }
            String envelope = zipEnvelopeViolation(archive, observed, false);
            if (envelope != null) {
                state.violation(boundary, "nested-archive", parent,
                        "archive-envelope", envelope);
            }
        } catch (Throwable failure) {
            state.violation(boundary, "nested-archive", parent,
                    "nested-archive-scan", failure.getMessage());
        }
    }

    private static int u2le(byte[] bytes, int offset) {
        return (bytes[offset] & 0xff) | ((bytes[offset + 1] & 0xff) << 8);
    }

    private static long u4le(byte[] bytes, int offset) {
        return (bytes[offset] & 0xffL)
                | ((bytes[offset + 1] & 0xffL) << 8)
                | ((bytes[offset + 2] & 0xffL) << 16)
                | ((bytes[offset + 3] & 0xffL) << 24);
    }

    private static String zipEnvelopeViolation(byte[] archive,
                                               int observedEntries,
                                               boolean allowComment) {
        if (archive.length < 22) return "ZIP is shorter than its end record";
        int minimum = Math.max(0, archive.length - 22 - 65_535);
        int eocd = -1;
        int commentLength = -1;
        for (int offset = archive.length - 22; offset >= minimum; offset--) {
            if (archive[offset] == 0x50 && archive[offset + 1] == 0x4b
                    && archive[offset + 2] == 0x05
                    && archive[offset + 3] == 0x06) {
                int candidate = u2le(archive, offset + 20);
                if ((long) offset + 22 + candidate == archive.length) {
                    eocd = offset;
                    commentLength = candidate;
                    break;
                }
            }
        }
        if (eocd < 0) {
            return "ZIP end record is missing or leaves trailing bytes";
        }
        if (!allowComment && commentLength != 0) {
            return "nested ZIP comments are not allowed";
        }
        int disk = u2le(archive, eocd + 4);
        int centralDisk = u2le(archive, eocd + 6);
        int diskEntries = u2le(archive, eocd + 8);
        int totalEntries = u2le(archive, eocd + 10);
        long centralBytes = u4le(archive, eocd + 12);
        long centralOffset = u4le(archive, eocd + 16);
        if (disk != 0 || centralDisk != 0 || diskEntries != totalEntries) {
            return "multi-disk ZIP envelopes are unsupported";
        }
        if (totalEntries == 0xffff || centralBytes == 0xffff_ffffL
                || centralOffset == 0xffff_ffffL) {
            return "ZIP64 envelopes are unsupported";
        }
        if (totalEntries != observedEntries) {
            return "central-directory count differs from streamed entries";
        }
        if (centralOffset + centralBytes != eocd) {
            return "central-directory bounds are inconsistent";
        }
        if (totalEntries == 0) {
            if (eocd != 0 || centralOffset != 0 || centralBytes != 0) {
                return "empty ZIP has unparsed bytes before its end record";
            }
        } else if (centralOffset + 4 > archive.length
                || archive[(int) centralOffset] != 0x50
                || archive[(int) centralOffset + 1] != 0x4b
                || archive[(int) centralOffset + 2] != 0x01
                || archive[(int) centralOffset + 3] != 0x02) {
            return "central directory does not begin at its declared offset";
        }
        return null;
    }

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
        cursor.skip((long) cursor.u2() * 2);
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

    private static String scanClass(ScanState state, String boundary,
                                    String lane, int elementPosition,
                                    String owner, String logicalPath,
                                    String expectedInternal, int release,
                                    boolean activeRuntimeEntry,
                                    byte[] bytes) {
        validateClassEnvelope(bytes);
        ClassReader reader;
        try { reader = new ClassReader(bytes); }
        catch (RuntimeException malformed) {
            throw new Failure("ASM rejected class " + logicalPath, malformed);
        }
        require(reader.getClassName().equals(expectedInternal),
                "class path/internal-name mismatch: " + logicalPath + " -> "
                        + reader.getClassName() + " expected " + expectedInternal);
        ClassRecord record = new ClassRecord(boundary, lane, elementPosition,
                owner, logicalPath, reader.getClassName(), release,
                activeRuntimeEntry);
        state.classes.add(record);
        ReferenceScanner scanner = new ReferenceScanner(state, record);
        try { reader.accept(scanner, 0); }
        catch (Failure failure) { throw failure; }
        catch (RuntimeException malformed) {
            throw new Failure("ASM callback scan failed for " + logicalPath,
                    malformed);
        }
        require(scanner.visitedHeader && scanner.visitedEnd,
                "incomplete ASM callback ledger for " + logicalPath);
        return reader.getClassName();
    }

    private static final class ReferenceScanner extends ClassVisitor {
        final ScanState state;
        final ClassRecord source;
        boolean visitedHeader;
        boolean visitedEnd;

        ReferenceScanner(ScanState state, ClassRecord source) {
            super(ASM);
            this.state = state;
            this.source = source;
        }

        void violation(String location, String rule, String detail) {
            RuntimeElement element = state.element(source.lane,
                    source.elementPosition);
            if (element == null) {
                state.violation(source.boundary, "classfile:" + location,
                        source.logicalPath, rule, detail);
            } else {
                state.violation(element, "classfile:" + location,
                        source.logicalPath, rule, detail);
            }
        }

        void add(String location, String form, String kind, String owner,
                 String name, String descriptor, String valueSha) {
            ReferenceRecord reference = new ReferenceRecord(source, location,
                    form, kind, owner == null ? "" : owner,
                    name == null ? "" : name,
                    descriptor == null ? "" : descriptor,
                    valueSha == null ? "" : valueSha);
            source.references.add(reference);
            state.references.add(reference);
        }

        void classType(String location, String form, String internal) {
            if (internal == null) return;
            if (internal.startsWith("[")) {
                descriptor(location, form, internal);
                return;
            }
            require(!internal.isEmpty() && internal.indexOf('.') < 0,
                    "invalid internal class name in " + source.internalName
                            + ": " + internal);
            add(location, form, "class", internal, "", "", "");
        }

        void type(String location, String form, Type type) {
            switch (type.getSort()) {
                case Type.OBJECT:
                    classType(location, form, type.getInternalName());
                    break;
                case Type.ARRAY:
                    type(location, form + ":array", type.getElementType());
                    break;
                case Type.METHOD:
                    for (Type argument : type.getArgumentTypes()) {
                        type(location, form + ":argument", argument);
                    }
                    type(location, form + ":return", type.getReturnType());
                    break;
                case Type.VOID: case Type.BOOLEAN: case Type.CHAR:
                case Type.BYTE: case Type.SHORT: case Type.INT:
                case Type.FLOAT: case Type.LONG: case Type.DOUBLE:
                    break;
                default:
                    throw new Failure("unknown ASM Type sort " + type.getSort());
            }
        }

        void descriptor(String location, String form, String descriptor) {
            if (descriptor == null) return;
            try {
                Type type = descriptor.startsWith("(")
                        ? Type.getMethodType(descriptor)
                        : Type.getType(descriptor);
                type(location, form, type);
            } catch (RuntimeException malformed) {
                throw new Failure("malformed descriptor in "
                        + source.internalName + ": " + descriptor, malformed);
            }
        }

        void signature(String location, String form, String signature,
                       boolean typeOnly) {
            if (signature == null) return;
            try {
                SignatureReader reader = new SignatureReader(signature);
                SignatureScanVisitor visitor = new SignatureScanVisitor(this,
                        location, form);
                if (typeOnly) reader.acceptType(visitor);
                else reader.accept(visitor);
            } catch (RuntimeException malformed) {
                throw new Failure("malformed generic signature in "
                        + source.internalName + ": " + signature, malformed);
            }
        }

        void member(String location, String form, String owner, String name,
                    String descriptor, boolean method) {
            classType(location, form + ":owner", owner);
            descriptor(location, form + ":descriptor", descriptor);
            add(location, form, method ? "method" : "field", owner, name,
                    descriptor, "");
        }

        void handle(String location, String form, Handle handle, int depth) {
            require(depth <= 32, "constant/handle nesting exceeds 32");
            int tag = handle.getTag();
            require(tag >= Opcodes.H_GETFIELD && tag <= Opcodes.H_INVOKEINTERFACE,
                    "unknown method-handle tag " + tag);
            boolean field = tag <= Opcodes.H_PUTSTATIC;
            member(location, form + ":handle-" + tag, handle.getOwner(),
                    handle.getName(), handle.getDesc(), !field);
        }

        void constant(String location, String form, Object value, int depth) {
            require(depth <= 32, "constant nesting exceeds 32");
            if (value == null) {
                add(location, form, "constant", "", "null", "",
                        sha256("null"));
            } else if (value instanceof String || value instanceof Integer
                    || value instanceof Long || value instanceof Float
                    || value instanceof Double || value instanceof Byte
                    || value instanceof Short || value instanceof Character
                    || value instanceof Boolean) {
                String rendered = value.getClass().getName() + ":"
                        + String.valueOf(value);
                add(location, form, "constant", "", value.getClass().getName(),
                        "", sha256(rendered));
            } else if (value instanceof Type) {
                Type type = (Type) value;
                type(location, form + ":type-constant", type);
                add(location, form, "constant-type", "", "",
                        type.getDescriptor(), sha256(type.getDescriptor()));
            } else if (value instanceof Handle) {
                handle(location, form, (Handle) value, depth + 1);
            } else if (value instanceof ConstantDynamic) {
                ConstantDynamic dynamic = (ConstantDynamic) value;
                descriptor(location, form + ":condy-descriptor",
                        dynamic.getDescriptor());
                add(location, form, "constant-dynamic", "", dynamic.getName(),
                        dynamic.getDescriptor(), "");
                handle(location, form + ":condy-bootstrap",
                        dynamic.getBootstrapMethod(), depth + 1);
                for (int index = 0;
                     index < dynamic.getBootstrapMethodArgumentCount(); index++) {
                    constant(location, form + ":condy-argument-" + index,
                            dynamic.getBootstrapMethodArgument(index), depth + 1);
                }
            } else if (value.getClass().isArray()) {
                int length = Array.getLength(value);
                for (int index = 0; index < length; index++) {
                    constant(location, form + ":array-" + index,
                            Array.get(value, index), depth + 1);
                }
            } else {
                throw new Failure("unknown constant form "
                        + value.getClass().getName());
            }
        }

        AnnotationVisitor annotation(String location, String form,
                                     String annotationDescriptor) {
            descriptor(location, form + ":annotation-type",
                    annotationDescriptor);
            String annotationOwner;
            try { annotationOwner = Type.getType(annotationDescriptor)
                    .getInternalName(); }
            catch (RuntimeException malformed) {
                throw new Failure("malformed annotation descriptor: "
                        + annotationDescriptor, malformed);
            }
            return annotationValues(location, form, annotationOwner);
        }

        AnnotationVisitor annotationValues(String location, String form,
                                           String annotationOwner) {
            return new AnnotationVisitor(ASM) {
                @Override public void visit(String name, Object value) {
                    if (!annotationOwner.isEmpty() && name != null) {
                        add(location, form + ":element", "annotation-element",
                                annotationOwner, name, "", "");
                    }
                    constant(location, form + ":value", value, 0);
                }

                @Override public void visitEnum(String name, String descriptor,
                                                String value) {
                    ReferenceScanner.this.descriptor(location,
                            form + ":enum-type", descriptor);
                    String enumOwner = Type.getType(descriptor).getInternalName();
                    add(location, form + ":enum", "field", enumOwner, value,
                            descriptor, "");
                }

                @Override public AnnotationVisitor visitAnnotation(
                        String name, String descriptor) {
                    return annotation(location, form + ":nested", descriptor);
                }

                @Override public AnnotationVisitor visitArray(String name) {
                    return annotationValues(location, form + ":array",
                            annotationOwner);
                }
            };
        }

        void frameValue(String location, Object value) {
            if (value instanceof String) {
                classType(location, "frame-type", (String) value);
            } else if (value instanceof Label) {
                add(location, "frame-uninitialized", "frame", "", "label",
                        "", "");
            } else if (value instanceof Integer) {
                int code = (Integer) value;
                require(Set.of(Opcodes.TOP, Opcodes.INTEGER, Opcodes.FLOAT,
                        Opcodes.DOUBLE, Opcodes.LONG, Opcodes.NULL,
                        Opcodes.UNINITIALIZED_THIS).contains(code),
                        "unknown expanded-frame value " + code);
            } else {
                throw new Failure("unknown expanded-frame form: "
                        + (value == null ? "null" : value.getClass().getName()));
            }
        }

        @Override public void visit(int version, int access, String name,
                                    String signature, String superName,
                                    String[] interfaces) {
            visitedHeader = true;
            source.superName = superName == null ? "" : superName;
            source.interfaces.clear();
            if (interfaces != null) {
                source.interfaces.addAll(Arrays.asList(interfaces));
            }
            require(name.equals(source.internalName),
                    "ASM header differs from ClassReader name");
            int major = version & 0xffff;
            if (source.activeRuntimeEntry && major > Opcodes.V11) {
                violation("<class>", "unsupported-active-class-version",
                        "class major " + major + " exceeds Java 11");
            }
            signature("<class>", "class-signature", signature, false);
            classType("<class>", "super", superName);
            if (interfaces != null) {
                for (String iface : interfaces) {
                    classType("<class>", "interface", iface);
                }
            }
        }

        @Override public void visitSource(String sourceFile, String debug) {
            if (sourceFile != null) constant("<class>", "source-file",
                    sourceFile, 0);
            if (debug != null) constant("<class>", "source-debug", debug, 0);
        }

        @Override public ModuleVisitor visitModule(String name, int access,
                                                   String version) {
            add("<module>", "module-declaration", "module", "", name, "",
                    sha256(String.valueOf(version)));
            return new ModuleVisitor(ASM) {
                @Override public void visitMainClass(String mainClass) {
                    classType("<module>", "module-main-class", mainClass);
                }
                @Override public void visitPackage(String packaze) {
                    add("<module>", "module-package", "package", "", packaze,
                            "", "");
                }
                @Override public void visitRequire(String module, int access,
                                                   String version) {
                    add("<module>", "module-require", "module", "", module,
                            "", sha256(String.valueOf(version)));
                }
                @Override public void visitExport(String packaze, int access,
                                                  String... modules) {
                    add("<module>", "module-export", "package", "", packaze,
                            "", "");
                    if (modules != null) for (String module : modules) {
                        add("<module>", "module-export-target", "module", "",
                                module, "", "");
                    }
                }
                @Override public void visitOpen(String packaze, int access,
                                                String... modules) {
                    add("<module>", "module-open", "package", "", packaze,
                            "", "");
                    if (modules != null) for (String module : modules) {
                        add("<module>", "module-open-target", "module", "",
                                module, "", "");
                    }
                }
                @Override public void visitUse(String service) {
                    classType("<module>", "module-use", service);
                }
                @Override public void visitProvide(String service,
                                                   String... providers) {
                    classType("<module>", "module-provide-service", service);
                    for (String provider : providers) {
                        classType("<module>", "module-provider", provider);
                    }
                }
            };
        }

        @Override public void visitNestHost(String nestHost) {
            classType("<class>", "nest-host", nestHost);
        }

        @Override public void visitOuterClass(String owner, String name,
                                              String descriptor) {
            if (name == null) classType("<class>", "outer-owner", owner);
            else member("<class>", "outer-method", owner, name, descriptor,
                    true);
        }

        @Override public AnnotationVisitor visitAnnotation(String descriptor,
                                                           boolean visible) {
            return annotation("<class>", visible ? "annotation-visible"
                    : "annotation-invisible", descriptor);
        }

        @Override public AnnotationVisitor visitTypeAnnotation(int typeRef,
                TypePath typePath, String descriptor, boolean visible) {
            return annotation("<class>", "type-annotation-" + typeRef + "-"
                    + String.valueOf(typePath), descriptor);
        }

        @Override public void visitAttribute(Attribute attribute) {
            violation("<class>", "unknown-attribute",
                    "unknown class attribute " + attribute.type);
        }

        @Override public void visitNestMember(String nestMember) {
            classType("<class>", "nest-member", nestMember);
        }

        @Override public void visitPermittedSubclass(String permittedSubclass) {
            classType("<class>", "permitted-subclass", permittedSubclass);
        }

        @Override public void visitInnerClass(String name, String outerName,
                                             String innerName, int access) {
            classType("<class>", "inner-class", name);
            classType("<class>", "inner-outer", outerName);
            if (innerName != null) constant("<class>", "inner-simple-name",
                    innerName, 0);
        }

        @Override public RecordComponentVisitor visitRecordComponent(
                String name, String descriptor, String signature) {
            String location = "record:" + name;
            descriptor(location, "record-descriptor", descriptor);
            signature(location, "record-signature", signature, true);
            return new RecordComponentVisitor(ASM) {
                @Override public AnnotationVisitor visitAnnotation(
                        String descriptor, boolean visible) {
                    return annotation(location, "record-annotation", descriptor);
                }
                @Override public AnnotationVisitor visitTypeAnnotation(int ref,
                        TypePath path, String descriptor, boolean visible) {
                    return annotation(location, "record-type-annotation-" + ref,
                            descriptor);
                }
                @Override public void visitAttribute(Attribute attribute) {
                    violation(location, "unknown-attribute",
                            "unknown record attribute " + attribute.type);
                }
            };
        }

        @Override public FieldVisitor visitField(int access, String name,
                String descriptor, String signature, Object value) {
            String location = "field:" + name + descriptor;
            if (!source.declaredMembers.add("F\u0000" + name + "\u0000"
                    + descriptor)) {
                violation(location, "duplicate-declared-member",
                        "duplicate field name and descriptor");
            }
            descriptor(location, "field-descriptor", descriptor);
            signature(location, "field-signature", signature, true);
            if (value != null) constant(location, "field-constant", value, 0);
            return new FieldVisitor(ASM) {
                @Override public AnnotationVisitor visitAnnotation(
                        String descriptor, boolean visible) {
                    return annotation(location, "field-annotation", descriptor);
                }
                @Override public AnnotationVisitor visitTypeAnnotation(int ref,
                        TypePath path, String descriptor, boolean visible) {
                    return annotation(location, "field-type-annotation-" + ref,
                            descriptor);
                }
                @Override public void visitAttribute(Attribute attribute) {
                    violation(location, "unknown-attribute",
                            "unknown field attribute " + attribute.type);
                }
            };
        }

        @Override public MethodVisitor visitMethod(int access, String name,
                String descriptor, String signature, String[] exceptions) {
            String location = "method:" + name + descriptor;
            if (!source.declaredMembers.add("M\u0000" + name + "\u0000"
                    + descriptor)) {
                violation(location, "duplicate-declared-member",
                        "duplicate method name and descriptor");
            }
            descriptor(location, "method-descriptor", descriptor);
            signature(location, "method-signature", signature, false);
            if (exceptions != null) for (String exception : exceptions) {
                classType(location, "declared-exception", exception);
            }
            return new MethodVisitor(ASM) {
                @Override public void visitParameter(String parameter,
                                                     int access) {
                    if (parameter != null) constant(location,
                            "method-parameter-name", parameter, 0);
                }

                @Override public AnnotationVisitor visitAnnotationDefault() {
                    return annotationValues(location, "annotation-default", "");
                }

                @Override public AnnotationVisitor visitAnnotation(
                        String descriptor, boolean visible) {
                    return annotation(location, "method-annotation", descriptor);
                }

                @Override public AnnotationVisitor visitTypeAnnotation(int ref,
                        TypePath path, String descriptor, boolean visible) {
                    return annotation(location, "method-type-annotation-" + ref,
                            descriptor);
                }

                @Override public AnnotationVisitor visitParameterAnnotation(
                        int parameter, String descriptor, boolean visible) {
                    return annotation(location, "parameter-annotation-"
                            + parameter, descriptor);
                }

                @Override public void visitAttribute(Attribute attribute) {
                    violation(location, "unknown-attribute",
                            "unknown method/code attribute " + attribute.type);
                }

                @Override public void visitFrame(int type, int numLocal,
                        Object[] local, int numStack, Object[] stack) {
                    if (local != null) for (int index = 0; index < numLocal;
                                            index++) {
                        frameValue(location + ":local-" + index, local[index]);
                    }
                    if (stack != null) for (int index = 0; index < numStack;
                                            index++) {
                        frameValue(location + ":stack-" + index, stack[index]);
                    }
                }

                @Override public void visitTypeInsn(int opcode, String type) {
                    classType(location, "type-insn-" + opcode, type);
                }

                @Override public void visitFieldInsn(int opcode, String owner,
                        String name, String descriptor) {
                    member(location, "field-insn-" + opcode, owner, name,
                            descriptor, false);
                }

                @Override public void visitMethodInsn(int opcode, String owner,
                        String name, String descriptor, boolean isInterface) {
                    member(location, "method-insn-" + opcode, owner, name,
                            descriptor, true);
                }

                @Override public void visitInvokeDynamicInsn(String name,
                        String descriptor, Handle bootstrap,
                        Object... bootstrapArguments) {
                    ReferenceScanner.this.descriptor(location,
                            "invokedynamic-descriptor", descriptor);
                    add(location, "invokedynamic-callsite", "invokedynamic",
                            "", name, descriptor, "");
                    handle(location, "invokedynamic-bootstrap", bootstrap, 0);
                    for (int index = 0; index < bootstrapArguments.length;
                         index++) {
                        constant(location, "invokedynamic-argument-" + index,
                                bootstrapArguments[index], 0);
                    }
                }

                @Override public void visitLdcInsn(Object value) {
                    constant(location, "ldc", value, 0);
                }

                @Override public void visitMultiANewArrayInsn(
                        String descriptor, int dimensions) {
                    ReferenceScanner.this.descriptor(location,
                            "multi-anewarray-" + dimensions, descriptor);
                }

                @Override public AnnotationVisitor visitInsnAnnotation(int ref,
                        TypePath path, String descriptor, boolean visible) {
                    return annotation(location, "insn-type-annotation-" + ref,
                            descriptor);
                }

                @Override public void visitTryCatchBlock(Label start, Label end,
                        Label handler, String type) {
                    classType(location, "try-catch-type", type);
                }

                @Override public AnnotationVisitor visitTryCatchAnnotation(
                        int ref, TypePath path, String descriptor,
                        boolean visible) {
                    return annotation(location, "try-catch-annotation-" + ref,
                            descriptor);
                }

                @Override public void visitLocalVariable(String name,
                        String descriptor, String signature, Label start,
                        Label end, int index) {
                    ReferenceScanner.this.descriptor(location,
                            "local-descriptor-" + index, descriptor);
                    ReferenceScanner.this.signature(location,
                            "local-signature-" + index, signature, true);
                }

                @Override public AnnotationVisitor visitLocalVariableAnnotation(
                        int ref, TypePath path, Label[] start, Label[] end,
                        int[] index, String descriptor, boolean visible) {
                    return annotation(location,
                            "local-variable-annotation-" + ref, descriptor);
                }
            };
        }

        @Override public void visitEnd() { visitedEnd = true; }
    }

    private static final class SignatureScanVisitor extends SignatureVisitor {
        final ReferenceScanner scanner;
        final String location;
        final String form;
        String currentClass;

        SignatureScanVisitor(ReferenceScanner scanner, String location,
                             String form) {
            super(ASM);
            this.scanner = scanner;
            this.location = location;
            this.form = form;
        }

        @Override public void visitClassType(String name) {
            currentClass = name;
            scanner.classType(location, form + ":class", name);
        }

        @Override public void visitInnerClassType(String name) {
            if (currentClass == null) currentClass = name;
            else currentClass = currentClass + "$" + name;
            scanner.classType(location, form + ":inner", currentClass);
        }

        @Override public SignatureVisitor visitClassBound() { return this; }
        @Override public SignatureVisitor visitInterfaceBound() { return this; }
        @Override public SignatureVisitor visitSuperclass() { return this; }
        @Override public SignatureVisitor visitInterface() { return this; }
        @Override public SignatureVisitor visitParameterType() { return this; }
        @Override public SignatureVisitor visitReturnType() { return this; }
        @Override public SignatureVisitor visitExceptionType() { return this; }
        @Override public SignatureVisitor visitArrayType() { return this; }
        @Override public SignatureVisitor visitTypeArgument(char wildcard) {
            return this;
        }
        @Override public void visitEnd() { currentClass = null; }
    }

    private static void bindInput(ScanState state, String name, Path path,
                                  boolean directory) throws IOException {
        state.discoveredInputs++;
        Path canonical = directory ? canonicalDirectory(path)
                : canonicalFile(path);
        String hash = directory ? directoryManifest(canonical)
                : sha256(canonical);
        require(state.inputPreHashes.put(name, hash) == null,
                "duplicate input ledger name: " + name);
        state.inputOrigins.put(name, canonical.toString());
        state.recordedInputs++;
    }

    private static String physicalKey(Path path) throws IOException {
        BasicFileAttributes attributes = Files.readAttributes(path,
                BasicFileAttributes.class, LinkOption.NOFOLLOW_LINKS);
        Object key = attributes.fileKey();
        return key == null ? path.toString()
                : attributes.getClass().getName() + ":" + key;
    }

    private static List<RuntimeElement> allRuntimeElements(ScanState state) {
        List<RuntimeElement> result = new ArrayList<>();
        if (state.originalElement != null) result.add(state.originalElement);
        result.addAll(state.runtimeElements);
        if (state.peerElement != null) result.add(state.peerElement);
        return result;
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
                "prospective output has a symbolic-link leaf/ancestor: "
                        + cursor);
        Path result = cursor.toRealPath();
        for (int index = missing.size() - 1; index >= 0; index--) {
            result = result.resolve(missing.get(index));
        }
        return result.normalize();
    }

    private static void preflightOutput(Config config, ScanState state)
            throws IOException {
        Path output = prospectiveCanonical(config.output);
        for (String origin : state.inputOrigins.values()) {
            Path lexical = absolute(Path.of(origin));
            require(!overlaps(output, lexical),
                    "prospective output overlaps input: output=" + output
                            + " input=" + lexical);
            Path real = lexical.toRealPath(LinkOption.NOFOLLOW_LINKS);
            require(!overlaps(output, real),
                    "prospective output overlaps canonical input: output="
                            + output + " input=" + real);
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
                        "refusing non-empty scanner output: " + absolute);
            }
            canonicalDirectory(absolute);
        } else {
            Files.createDirectories(absolute);
            canonicalDirectory(absolute);
        }
    }

    private static void bindRuntimeInputs(ScanState state)
            throws IOException {
        for (RuntimeElement element : allRuntimeElements(state)) {
            bindInput(state, "runtime:" + element.key(), element.path,
                    element.kind.equals("directory"));
            element.actualSha = state.inputPreHashes.get("runtime:"
                    + element.key());
        }
    }

    private static void validateOrigins(Config config, ScanState state,
                                        Path candidateA, Path candidateB)
            throws IOException {
        require(!overlaps(candidateA, candidateB),
                "candidate A/B roots overlap");
        require(!config.candidateRuntime.equals(config.oracleRuntime),
                "candidate and oracle runtime ledgers must be distinct inputs");
        require(!overlaps(candidateA, config.originalJar)
                        && !overlaps(candidateB, config.originalJar)
                        && !overlaps(candidateA, config.peerJar)
                        && !overlaps(candidateB, config.peerJar),
                "candidate root overlaps a licensed inventory artifact");
        require(!config.originalJar.equals(config.peerJar),
                "Transactor and Peer inventory paths must be distinct");
        state.physicalOrigins.put(physicalKey(candidateA),
                "candidate-a:<root>");
        require(state.physicalOrigins.putIfAbsent(physicalKey(candidateB),
                        "candidate-b:<root>") == null,
                "candidate roots alias the same physical origin");
        Map<String, Set<Path>> lanePaths = new HashMap<>();
        lanePaths.put("candidate", new HashSet<>());
        lanePaths.put("oracle", new HashSet<>());
        for (RuntimeElement element : state.runtimeElements) {
            Path canonical = element.kind.equals("directory")
                    ? canonicalDirectory(element.path)
                    : canonicalFile(element.path);
            require(canonical.equals(element.path),
                    "runtime path is not canonical: " + element.path);
            require(lanePaths.get(element.lane).add(canonical),
                    "duplicate path inside " + element.lane
                            + " runtime lane: " + canonical);
            require(!overlaps(canonical, candidateA)
                            && !overlaps(canonical, candidateB)
                            && !overlaps(canonical, config.originalJar)
                            && !overlaps(canonical, config.peerJar),
                    "runtime element overlaps an owned/inventory input: "
                            + canonical);
        }
        preflightOutput(config, state);
    }

    private static void policyViolation(ScanState state,
            RuntimeElement element, ArtifactPolicy policy, String detail) {
        element.forbiddenHits++;
        state.violation(element, "runtime-policy", element.path.toString(),
                policy.ruleId, detail);
    }

    private static void validateRuntimePolicy(Config config, ScanState state)
            throws IOException {
        long candidateCount = state.runtimeElements.stream()
                .filter(element -> element.lane.equals("candidate")).count();
        long oracleCount = state.runtimeElements.stream()
                .filter(element -> element.lane.equals("oracle")).count();
        if (candidateCount != state.expectedCandidateRuntimeElements
                || oracleCount != state.expectedOracleRuntimeElements) {
            state.violation("runtime-policy", "lane-cardinality", "<ledgers>",
                    "runtime-lane-cardinality",
                    "candidate=" + candidateCount + "/"
                            + state.expectedCandidateRuntimeElements
                            + " oracle=" + oracleCount + "/"
                            + state.expectedOracleRuntimeElements);
        }
        if (config.requireProductionShape
                && (state.expectedCandidateRuntimeElements != 532
                        || state.expectedOracleRuntimeElements != 533)) {
            state.violation("runtime-policy", "production-shape", "<ledgers>",
                    "production-runtime-shape",
                    "normal mode requires candidate=532 and oracle=533");
        }
        if (!sha256(config.candidateRuntime).equals(
                        state.expectedCandidateLedgerSha)
                || !sha256(config.oracleRuntime).equals(
                        state.expectedOracleLedgerSha)) {
            state.violation("runtime-policy", "ledger-anchor", "<ledgers>",
                    "runtime-ledger-anchor",
                    "ordered runtime ledger bytes differ from policy anchors");
        }
        ArtifactPolicy transactor = state.runtimePolicy.get(
                "transactor-original");
        ArtifactPolicy peer = state.runtimePolicy.get("peer-original");
        if (!state.originalElement.actualSha.equals(transactor.sha)) {
            state.violation("original", "identity", config.originalJar.toString(),
                    "transactor-original",
                    "owned Transactor hash differs from pinned policy");
        }
        if (!state.peerElement.actualSha.equals(peer.sha)) {
            state.violation("peer-inventory", "identity",
                    config.peerJar.toString(), "peer-original",
                    "inventory-only Peer hash differs from pinned policy");
        }
        for (ArtifactPolicy policy : state.runtimePolicy.values()) {
            List<RuntimeElement> candidate = state.runtimeElements.stream()
                    .filter(element -> element.lane.equals("candidate")
                            && element.actualSha.equals(policy.sha))
                    .collect(Collectors.toList());
            List<RuntimeElement> oracle = state.runtimeElements.stream()
                    .filter(element -> element.lane.equals("oracle")
                            && element.actualSha.equals(policy.sha))
                    .collect(Collectors.toList());
            if (policy.candidateDisposition.equals("FORBID")) {
                for (RuntimeElement element : candidate) {
                    policyViolation(state, element, policy,
                            "forbidden identity is present in candidate lane");
                }
            } else if (policy.candidateDisposition.equals("REQUIRE")) {
                if (candidate.size() != 1
                        || !candidate.get(0).role.equals(policy.candidateRole)) {
                    state.violation("runtime-policy", "candidate-requirement",
                            policy.ruleId, "required-role-hash",
                            "required identity must occur exactly once at role "
                                    + policy.candidateRole);
                    for (RuntimeElement element : candidate) {
                        if (!element.role.equals(policy.candidateRole)) {
                            policyViolation(state, element, policy,
                                    "right hash appears at the wrong role");
                        }
                    }
                }
            } else if (!policy.ruleId.equals("transactor-original")
                    || !candidate.isEmpty()) {
                state.violation("runtime-policy", "candidate-owned",
                        policy.ruleId, "unexpected-owned-disposition",
                        "candidate lane cannot own a licensed original");
            }
            if (policy.oracleDisposition.equals("FORBID")) {
                for (RuntimeElement element : oracle) {
                    policyViolation(state, element, policy,
                            "forbidden identity is present in oracle lane");
                }
            } else if (policy.oracleDisposition.equals("REQUIRE")) {
                if (oracle.size() != 1
                        || !oracle.get(0).role.equals(policy.oracleRole)) {
                    state.violation("runtime-policy", "oracle-requirement",
                            policy.ruleId, "required-role-hash",
                            "required identity must occur exactly once at role "
                                    + policy.oracleRole);
                    for (RuntimeElement element : oracle) {
                        if (!element.role.equals(policy.oracleRole)) {
                            policyViolation(state, element, policy,
                                    "right hash appears at the wrong role");
                        }
                    }
                }
            } else if (!policy.ruleId.equals("transactor-original")
                    || !oracle.isEmpty()) {
                state.violation("runtime-policy", "oracle-owned",
                        policy.ruleId, "unexpected-owned-disposition",
                        "owned Transactor is separate position zero, not a fallback element");
            }
        }
        for (CollisionPolicyRow collision
                : state.candidateCollisionPolicy.values()) {
            RuntimeElement first = state.element("candidate",
                    collision.firstPosition);
            RuntimeElement later = state.element("candidate",
                    collision.laterPosition);
            if (first == null || later == null
                    || !first.role.equals(collision.firstRole)
                    || !later.role.equals(collision.laterRole)) {
                state.violation("candidate-collision-policy", "runtime-order",
                        collision.internalName, "collision-role-position-spoof",
                        "collision row does not bind existing ordered candidate roles");
            }
        }
        if (config.requireProductionShape) {
            long identical = state.candidateCollisionPolicy.values().stream()
                    .filter(value -> value.byteRelation.equals(
                            "BYTE_IDENTICAL")).count();
            long different = state.candidateCollisionPolicy.values().stream()
                    .filter(value -> value.byteRelation.equals(
                            "BYTE_DIFFERENT")).count();
            boolean allJline = state.candidateCollisionPolicy.values().stream()
                    .allMatch(value -> value.internalName.startsWith("jline/"));
            if (state.candidateCollisionPolicy.size() != 33
                    || identical != 14 || different != 19 || !allJline) {
                state.violation("candidate-collision-policy",
                        "production-shape", "<policy>",
                        "production-candidate-collision-shape",
                        "normal mode requires 33 jline rows: 14 byte-identical and 19 byte-different");
            }
        }
    }

    private static void reconcileCandidateCollisionPolicy(ScanState state) {
        long actual = state.collisions.stream()
                .filter(value -> value.lane.equals("candidate")
                        && value.kind.equals("class"))
                .count();
        for (CollisionPolicyRow policy
                : state.candidateCollisionPolicy.values()) {
            if (!policy.seen) {
                state.violation("candidate-collision-policy", "reconciliation",
                        policy.internalName,
                        "extra-or-unobserved-candidate-collision-policy",
                        "policy row did not match an observed candidate collision");
            }
        }
        long seen = state.candidateCollisionPolicy.values().stream()
                .filter(value -> value.seen).count();
        if (actual != seen || seen != state.candidateCollisionPolicy.size()) {
            state.violation("candidate-collision-policy", "reconciliation",
                    "<ledger>", "incomplete-candidate-collision-ledger",
                    "observed=" + actual + " matched=" + seen
                            + " policy="
                            + state.candidateCollisionPolicy.size());
        }
    }

    private static void checkTopLevelPayload(ScanState state,
                                             RuntimeElement element) {
        if (!element.lane.equals("candidate")
                || element.kind.equals("directory")) return;
        for (ForbiddenPayload rule : state.forbiddenBySha.getOrDefault(
                element.actualSha, Collections.emptyList())) {
            element.forbiddenHits++;
            state.violation(element, "top-level-payload", element.role,
                    rule.rule, "forbidden whole-file SHA-256: " + rule.reason);
        }
    }

    private static void scanRuntimeElement(RuntimeElement element,
                                           ScanState state) {
        if (!element.expectedSha.equals(element.actualSha)) {
            state.violation(element, "input", element.role,
                    "input-hash-mismatch", "expected " + element.expectedSha
                            + " got " + element.actualSha);
        }
        checkTopLevelPayload(state, element);
        try {
            if (element.kind.equals("directory")) {
                scanRuntimeDirectory(element, state);
            } else {
                scanRuntimeJar(element, state);
            }
        } catch (Throwable failure) {
            element.scanned = true;
            state.violation(element, "driver", element.path.toString(),
                    "runtime-element-scan-failure",
                    failure.getClass().getName() + ": " + failure.getMessage());
        }
    }

    private static void checkCandidateMembership(ScanState state) {
        if (!state.actualCandidateA.equals(state.expectedCandidateA.keySet())) {
            Set<String> missing = new TreeSet<>(state.expectedCandidateA.keySet());
            missing.removeAll(state.actualCandidateA);
            Set<String> extra = new TreeSet<>(state.actualCandidateA);
            extra.removeAll(state.expectedCandidateA.keySet());
            state.violation("candidate-a", "ownership", "<membership>",
                    "exact-class-membership",
                    "missing=" + missing + " extra=" + extra);
        }
        if (!state.actualCandidateB.equals(state.expectedCandidateB.keySet())) {
            Set<String> missing = new TreeSet<>(state.expectedCandidateB.keySet());
            missing.removeAll(state.actualCandidateB);
            Set<String> extra = new TreeSet<>(state.actualCandidateB);
            extra.removeAll(state.expectedCandidateB.keySet());
            state.violation("candidate-b", "ownership", "<membership>",
                    "exact-class-membership",
                    "missing=" + missing + " extra=" + extra);
        }
        for (AllowedResource resource : state.allowed.values()) {
            if (!resource.seen) {
                state.violation(resource.boundary, "ownership", resource.path,
                        "missing-allowed-resource",
                        "allow-listed resource was not present");
            }
        }
    }

    private static boolean platformClass(String internal,
                                         Map<String, Boolean> cache) {
        if (internal == null || internal.isEmpty()) return false;
        Boolean existing = cache.get(internal);
        if (existing != null) return existing;
        boolean result;
        try {
            result = ClassLoader.getPlatformClassLoader()
                    .getResource(internal + ".class") != null;
        } catch (RuntimeException failure) {
            result = false;
        }
        cache.put(internal, result);
        return result;
    }

    private static boolean typedMemberReference(ReferenceRecord reference) {
        return reference.targetKind.equals("field")
                || reference.targetKind.equals("method")
                || reference.targetKind.equals("annotation-element");
    }

    private static boolean declaredMember(ClassRecord record,
                                          ReferenceRecord reference) {
        if (reference.targetKind.equals("field")) {
            return record.declaredMembers.contains("F\u0000"
                    + reference.targetName + "\u0000"
                    + reference.targetDescriptor);
        }
        if (reference.targetKind.equals("method")) {
            return record.declaredMembers.contains("M\u0000"
                    + reference.targetName + "\u0000"
                    + reference.targetDescriptor);
        }
        String prefix = "M\u0000" + reference.targetName + "\u0000";
        return record.declaredMembers.stream().anyMatch(
                member -> member.startsWith(prefix));
    }

    private static boolean scannedMember(Map<String, ClassRecord> universe,
            String owner, ReferenceRecord reference, Set<String> visited) {
        if (!visited.add(owner)) return false;
        ClassRecord record = universe.get(owner);
        if (record == null) return false;
        if (declaredMember(record, reference)) return true;
        if (reference.targetKind.equals("method")
                && reference.targetName.equals("<init>")) return false;
        if (!record.superName.isEmpty()
                && scannedMember(universe, record.superName, reference,
                        visited)) return true;
        for (String iface : record.interfaces) {
            if (scannedMember(universe, iface, reference, visited)) return true;
        }
        return false;
    }

    private static boolean platformMember(String owner,
            ReferenceRecord reference, Set<Class<?>> visited) {
        try {
            Class<?> type = Class.forName(owner.replace('/', '.'), false,
                    ClassLoader.getPlatformClassLoader());
            return platformMember(type, reference, visited);
        } catch (ClassNotFoundException | LinkageError failure) {
            return false;
        }
    }

    private static boolean platformMember(Class<?> type,
            ReferenceRecord reference, Set<Class<?>> visited) {
        if (type == null || !visited.add(type)) return false;
        try {
            if (reference.targetKind.equals("field")) {
                for (Field field : type.getDeclaredFields()) {
                    if (field.getName().equals(reference.targetName)
                            && Type.getDescriptor(field.getType()).equals(
                                    reference.targetDescriptor)) return true;
                }
            } else {
                if (reference.targetName.equals("<init>")) {
                    for (Constructor<?> constructor
                            : type.getDeclaredConstructors()) {
                        if (Type.getConstructorDescriptor(constructor).equals(
                                reference.targetDescriptor)) return true;
                    }
                    return false;
                }
                for (Method method : type.getDeclaredMethods()) {
                    if (!method.getName().equals(reference.targetName)) continue;
                    if (reference.targetKind.equals("annotation-element")
                            || Type.getMethodDescriptor(method).equals(
                                    reference.targetDescriptor)) return true;
                }
            }
        } catch (RuntimeException | LinkageError failure) {
            return false;
        }
        if (platformMember(type.getSuperclass(), reference, visited)) return true;
        for (Class<?> iface : type.getInterfaces()) {
            if (platformMember(iface, reference, visited)) return true;
        }
        return false;
    }

    private static void putFirst(Map<String, ClassRecord> target,
                                 ClassRecord record) {
        if (record.activeRuntimeEntry && !record.internalName.equals("module-info")) {
            target.putIfAbsent(record.internalName, record);
        }
    }

    private static void referenceViolation(ScanState state,
            ReferenceRecord reference, String rule, String detail) {
        RuntimeElement element = state.element(reference.lane,
                reference.elementPosition);
        if (element == null) {
            state.violation(reference.boundary, "reference",
                    reference.sourceClass + ":" + reference.location,
                    rule, detail);
        } else {
            state.violation(element, "reference",
                    reference.sourceClass + ":" + reference.location,
                    rule, detail);
        }
    }

    private static boolean resolveIn(Map<String, ClassRecord> universe,
            ReferenceRecord reference, Map<String, Boolean> platformCache) {
        if (platformClass(reference.targetOwner, platformCache)) {
            return !typedMemberReference(reference)
                    || platformMember(reference.targetOwner, reference,
                            new HashSet<>());
        }
        if (!universe.containsKey(reference.targetOwner)) return false;
        return !typedMemberReference(reference)
                || scannedMember(universe, reference.targetOwner, reference,
                        new HashSet<>());
    }

    private static void resolveReferences(ScanState state) {
        Map<String, ClassRecord> candidateA = new LinkedHashMap<>();
        Map<String, ClassRecord> candidateB = new LinkedHashMap<>();
        Map<String, ClassRecord> oracle = new LinkedHashMap<>();
        for (ClassRecord record : state.classes) {
            if (record.boundary.equals("candidate-a")) {
                putFirst(candidateA, record);
            } else if (record.boundary.equals("candidate-b")) {
                putFirst(candidateB, record);
            }
        }
        Set<String> blockedA = state.overlapProofs.values().stream()
                .filter(proof -> !proof.originalName.equals(
                        proof.candidateAName))
                .map(proof -> proof.originalName).collect(Collectors.toSet());
        Set<String> blockedB = state.overlapProofs.values().stream()
                .filter(proof -> !proof.originalName.equals(
                        proof.candidateBName))
                .map(proof -> proof.originalName).collect(Collectors.toSet());
        List<ClassRecord> activeRuntime = state.classes.stream()
                .filter(record -> record.activeRuntimeEntry
                        && record.lane != null && !record.lane.isEmpty())
                .sorted(Comparator.comparing((ClassRecord record) -> record.lane)
                        .thenComparingInt(record -> record.elementPosition)
                        .thenComparing(record -> record.internalName))
                .collect(Collectors.toList());
        for (ClassRecord record : activeRuntime) {
            if (record.lane.equals("candidate")) {
                if (!blockedA.contains(record.internalName)) {
                    putFirst(candidateA, record);
                }
                if (!blockedB.contains(record.internalName)) {
                    putFirst(candidateB, record);
                }
            } else if (record.lane.equals("oracle")) {
                putFirst(oracle, record);
            }
        }
        Map<String, Boolean> platformCache = new HashMap<>();
        for (ReferenceRecord reference : state.references) {
            if (reference.targetOwner.isEmpty()) continue;
            if (!reference.sourceActive
                    || reference.boundary.equals("runtime-nested")) {
                reference.resolution = "inactive-entry-inventoried";
                continue;
            }
            boolean resolved = false;
            if (reference.boundary.equals("candidate-a")) {
                resolved = resolveIn(candidateA, reference, platformCache);
                reference.resolution = resolved ? "candidate-a-exact"
                        : "UNRESOLVED";
            } else if (reference.boundary.equals("candidate-b")) {
                resolved = resolveIn(candidateB, reference, platformCache);
                reference.resolution = resolved ? "candidate-b-exact"
                        : "UNRESOLVED";
            } else if (reference.lane.equals("candidate")) {
                boolean a = resolveIn(candidateA, reference, platformCache);
                boolean b = resolveIn(candidateB, reference, platformCache);
                resolved = a && b;
                reference.resolution = resolved
                        ? "candidate-runtime-both-exact" : "UNRESOLVED";
            } else if (reference.lane.equals("oracle")) {
                resolved = resolveIn(oracle, reference, platformCache);
                reference.resolution = resolved ? "oracle-exact"
                        : "UNRESOLVED";
            }
            if (!resolved) {
                referenceViolation(state, reference,
                        typedMemberReference(reference) ? "member-escape"
                                : "reference-escape",
                        reference.form + " -> " + reference.targetOwner
                                + (typedMemberReference(reference)
                                        ? "." + reference.targetName
                                                + reference.targetDescriptor
                                        : ""));
            }
        }
    }

    private static void sealPostInputs(ScanState state) {
        for (Map.Entry<String, String> input : state.inputOrigins.entrySet()) {
            String post = "";
            try {
                Path path = Path.of(input.getValue());
                post = Files.isDirectory(path, LinkOption.NOFOLLOW_LINKS)
                        ? directoryManifest(path) : sha256(path);
            } catch (Throwable failure) {
                state.violation("input", "mutation", input.getKey(),
                        "post-hash-failure", failure.toString());
            }
            state.inputPostHashes.put(input.getKey(), post);
            if (!post.equals(state.inputPreHashes.get(input.getKey()))) {
                state.violation("input", "mutation", input.getKey(),
                        "input-mutated", "input changed during scan");
            }
        }
        state.candidateAPost = state.inputPostHashes.getOrDefault(
                "owned:candidate-a", "");
        state.candidateBPost = state.inputPostHashes.getOrDefault(
                "owned:candidate-b", "");
        for (RuntimeElement element : allRuntimeElements(state)) {
            element.postSha = state.inputPostHashes.getOrDefault(
                    "runtime:" + element.key(), "");
        }
    }

    private static void reconcileRuntimeElements(ScanState state) {
        for (RuntimeElement element : allRuntimeElements(state)) {
            element.classes = 0;
            element.resources = 0;
            element.directories = 0;
            element.other = 0;
            element.bytes = 0;
            String boundary = "runtime-" + element.lane;
            for (EntryRecord entry : state.entries) {
                if (!entry.boundary.equals(boundary)
                        || entry.elementPosition != element.position
                        || !entry.owner.equals(element.owner())) continue;
                element.bytes += entry.bytes;
                if (entry.kind.equals("class")
                        || entry.kind.equals("nested-class")) {
                    element.classes++;
                } else if (entry.kind.equals("resource")) {
                    element.resources++;
                } else if (entry.kind.contains("directory")) {
                    element.directories++;
                } else {
                    element.other++;
                }
            }
        }
    }

    private static boolean ledgerComplete(ScanState state) {
        if (!state.setupComplete
                || state.discoveredEntries != state.recordedEntries
                || state.discoveredInputs != state.recordedInputs
                || state.inputPreHashes.size() != state.inputPostHashes.size()) {
            return false;
        }
        Set<String> ids = new HashSet<>();
        for (EntryRecord entry : state.entries) {
            if (!ids.add(entry.id)
                    || !(entry.status.equals("PASS")
                         || entry.status.equals("FAIL"))) return false;
        }
        for (RuntimeElement element : allRuntimeElements(state)) {
            if (!element.scanned || element.discovered != element.recorded
                    || element.classes + element.resources
                            + element.directories + element.other
                                    != element.recorded) return false;
        }
        return true;
    }

    private static int runScan(Config config) throws IOException {
        ScanState state = new ScanState();
        bindInput(state, "policy:cohort", config.cohort, false);
        bindInput(state, "policy:ownership", config.ownership, false);
        bindInput(state, "policy:class-mappings", config.classMappings, false);
        bindInput(state, "policy:runtime-policy", config.runtimePolicy, false);
        bindInput(state, "policy:candidate-runtime-ledger",
                config.candidateRuntime, false);
        bindInput(state, "policy:oracle-runtime-ledger",
                config.oracleRuntime, false);
        bindInput(state, "policy:candidate-collisions",
                config.candidateCollisions, false);
        bindInput(state, "policy:allowed-resources", config.allowedResources,
                false);
        bindInput(state, "policy:forbidden-payloads",
                config.forbiddenPayloads, false);
        readCohort(config.cohort, state);
        readOwnership(config.ownership, state);
        readMappings(config.classMappings, state);
        readAllowedResources(config.allowedResources, state);
        readForbiddenPayloads(config.forbiddenPayloads, state);
        readCandidateCollisionPolicy(config.candidateCollisions, state);
        readRuntimePolicy(config.runtimePolicy, state);
        readRuntime(config.candidateRuntime, "candidate", state);
        readRuntime(config.oracleRuntime, "oracle", state);
        ArtifactPolicy transactor = state.runtimePolicy.get(
                "transactor-original");
        ArtifactPolicy peer = state.runtimePolicy.get("peer-original");
        state.originalElement = new RuntimeElement("oracle", 0,
                "owned-transactor-root", "jar", config.originalJar,
                transactor.sha);
        state.peerElement = new RuntimeElement("inventory", 0,
                "peer-inventory-only", "jar", config.peerJar, peer.sha);
        bindInput(state, "owned:candidate-a", config.candidateA, true);
        bindInput(state, "owned:candidate-b", config.candidateB, true);
        bindInput(state, "owned:transactor-original", config.originalJar,
                false);
        bindInput(state, "inventory:peer-original", config.peerJar, false);
        bindRuntimeInputs(state);
        Path candidateA = canonicalDirectory(config.candidateA);
        Path candidateB = canonicalDirectory(config.candidateB);
        state.candidateAPre = state.inputPreHashes.get("owned:candidate-a");
        state.candidateBPre = state.inputPreHashes.get("owned:candidate-b");
        validateOrigins(config, state, candidateA, candidateB);
        prepareOutput(config.output);
        try {
            validateRuntimePolicy(config, state);
            scanCandidate("candidate-a", candidateA, state);
            scanCandidate("candidate-b", candidateB, state);
            scanRuntimeElement(state.originalElement, state);
            for (RuntimeElement element : state.runtimeElements.stream()
                    .filter(value -> value.lane.equals("oracle"))
                    .sorted(Comparator.comparingInt(value -> value.position))
                    .collect(Collectors.toList())) {
                scanRuntimeElement(element, state);
            }
            for (RuntimeElement element : state.runtimeElements.stream()
                    .filter(value -> value.lane.equals("candidate"))
                    .sorted(Comparator.comparingInt(value -> value.position))
                    .collect(Collectors.toList())) {
                scanRuntimeElement(element, state);
            }
            reconcileCandidateCollisionPolicy(state);
            scanRuntimeElement(state.peerElement, state);
            checkCandidateMembership(state);
            resolveReferences(state);
            state.setupComplete = true;
        } catch (Throwable failure) {
            state.violation("scanner", "setup", "<setup>",
                    "setup-or-driver-failure",
                    failure.getClass().getName() + ": " + failure.getMessage());
        }
        sealPostInputs(state);
        reconcileRuntimeElements(state);
        state.ledgerComplete = ledgerComplete(state);
        if (!state.ledgerComplete) {
            state.violation("scanner", "ledger", "<ledger>",
                    "incomplete-ledger", "discovered entries="
                            + state.discoveredEntries + " recorded="
                            + state.recordedEntries + " discovered inputs="
                            + state.discoveredInputs + " recorded inputs="
                            + state.recordedInputs);
        }
        writeEvidence(config.output, state);
        return state.violations.isEmpty() && state.ledgerComplete ? 0 : 2;
    }

    private static void writeLines(Path path, List<String> lines)
            throws IOException {
        Files.write(path, lines, StandardCharsets.UTF_8,
                StandardOpenOption.CREATE_NEW);
    }

    private static void writeEvidence(Path output, ScanState state)
            throws IOException {
        List<String> inputRows = new ArrayList<>();
        inputRows.add("name\torigin\texpected_sha256\tpre_sha256"
                + "\tpost_sha256\tstatus");
        List<String> inputNames = new ArrayList<>(state.inputPreHashes.keySet());
        Collections.sort(inputNames);
        Map<String, String> runtimeExpected = new HashMap<>();
        for (RuntimeElement element : allRuntimeElements(state)) {
            runtimeExpected.put("runtime:" + element.key(),
                    element.expectedSha);
        }
        for (String name : inputNames) {
            String pre = state.inputPreHashes.getOrDefault(name, "");
            String post = state.inputPostHashes.getOrDefault(name, "");
            String expected = runtimeExpected.getOrDefault(name, pre);
            String status = !pre.isEmpty() && pre.equals(post)
                    && pre.equals(expected) ? "PASS" : "FAIL";
            inputRows.add(clean(name) + "\t"
                    + clean(state.inputOrigins.getOrDefault(name, "")) + "\t"
                    + expected + "\t" + pre + "\t" + post + "\t" + status);
        }
        writeLines(output.resolve("inputs.tsv"), inputRows);

        List<RuntimeElement> runtimeElements = allRuntimeElements(state);
        runtimeElements.sort(Comparator
                .comparing((RuntimeElement element) -> element.lane)
                .thenComparingInt(element -> element.position)
                .thenComparing(element -> element.role));
        List<String> runtimeRows = new ArrayList<>();
        runtimeRows.add("lane\tposition\trole\tkind\tpath"
                + "\texpected_sha256\tpre_sha256\tpost_sha256"
                + "\tentries_discovered\tentries_recorded\tclasses"
                + "\tresources\tdirectories\tother\tledger_bytes\terrors"
                + "\tforbidden_hits\tcollisions\tscanned\tstatus");
        for (RuntimeElement element : runtimeElements) {
            boolean reconciled = element.scanned
                    && element.discovered == element.recorded
                    && element.classes + element.resources
                            + element.directories + element.other
                                    == element.recorded
                    && element.actualSha.equals(element.postSha)
                    && element.actualSha.equals(element.expectedSha);
            runtimeRows.add(element.lane + "\t" + element.position + "\t"
                    + clean(element.role) + "\t" + element.kind + "\t"
                    + clean(element.path.toString()) + "\t"
                    + element.expectedSha + "\t" + element.actualSha + "\t"
                    + element.postSha + "\t" + element.discovered + "\t"
                    + element.recorded + "\t" + element.classes + "\t"
                    + element.resources + "\t" + element.directories + "\t"
                    + element.other + "\t" + element.bytes + "\t"
                    + element.errors + "\t" + element.forbiddenHits + "\t"
                    + element.collisions + "\t" + element.scanned + "\t"
                    + (reconciled ? "PASS" : "FAIL"));
        }
        writeLines(output.resolve("runtime-elements.tsv"), runtimeRows);

        List<EntryRecord> entries = new ArrayList<>(state.entries);
        entries.sort(Comparator.comparing((EntryRecord row) -> row.boundary)
                .thenComparingInt(row -> row.elementPosition)
                .thenComparing(row -> row.logicalPath)
                .thenComparingInt(row -> row.occurrence));
        List<String> entryRows = new ArrayList<>();
        entryRows.add("ordinal\tboundary\telement_position\towner\torigin"
                + "\tkind\tlogical_path\toccurrence\tinternal_name\tbytes"
                + "\tsha256\tstatus");
        int ordinal = 0;
        for (EntryRecord entry : entries) {
            entryRows.add(++ordinal + "\t" + clean(entry.boundary) + "\t"
                    + entry.elementPosition + "\t" + clean(entry.owner) + "\t"
                    + clean(entry.origin) + "\t" + clean(entry.kind) + "\t"
                    + clean(entry.logicalPath) + "\t" + entry.occurrence + "\t"
                    + clean(entry.internalName) + "\t" + entry.bytes + "\t"
                    + entry.sha + "\t" + entry.status);
        }
        writeLines(output.resolve("entries.tsv"), entryRows);

        List<ReferenceRecord> references = new ArrayList<>(state.references);
        references.sort(Comparator
                .comparing((ReferenceRecord row) -> row.boundary)
                .thenComparing(row -> row.sourcePath)
                .thenComparingInt(row -> row.sequence));
        List<String> referenceRows = new ArrayList<>();
        referenceRows.add("ordinal\tboundary\tsource_class\tsource_path"
                + "\tlane\telement_position\tcallback_sequence\tlocation"
                + "\tform\ttarget_kind"
                + "\ttarget_owner\ttarget_name\ttarget_descriptor"
                + "\tvalue_sha256\tresolution");
        ordinal = 0;
        for (ReferenceRecord reference : references) {
            referenceRows.add(++ordinal + "\t" + clean(reference.boundary)
                    + "\t" + clean(reference.sourceClass) + "\t"
                    + clean(reference.sourcePath) + "\t"
                    + clean(reference.lane) + "\t" + reference.elementPosition
                    + "\t" + reference.sequence
                    + "\t" + clean(reference.location) + "\t"
                    + clean(reference.form) + "\t"
                    + clean(reference.targetKind) + "\t"
                    + clean(reference.targetOwner) + "\t"
                    + clean(reference.targetName) + "\t"
                    + clean(reference.targetDescriptor) + "\t"
                    + reference.valueSha + "\t" + reference.resolution);
        }
        writeLines(output.resolve("references.tsv"), referenceRows);

        List<Collision> collisions = new ArrayList<>(state.collisions);
        collisions.sort(Comparator.comparing((Collision row) -> row.lane)
                .thenComparing(row -> row.kind)
                .thenComparing(row -> row.logicalName)
                .thenComparingInt(row -> row.firstPosition)
                .thenComparingInt(row -> row.laterPosition)
                .thenComparing(row -> row.laterOwner));
        List<String> collisionRows = new ArrayList<>();
        collisionRows.add("lane\tkind\tlogical_name\tfirst_position"
                + "\tfirst_role\tfirst_owner\tfirst_origin\tfirst_entry"
                + "\tfirst_release\tfirst_sha256\tlater_position\tlater_role"
                + "\tlater_owner\tlater_origin\tlater_entry\tlater_release"
                + "\tlater_sha256\tbyte_relation\tdisposition");
        for (Collision collision : collisions) {
            collisionRows.add(clean(collision.lane) + "\t"
                    + clean(collision.kind) + "\t"
                    + clean(collision.logicalName) + "\t"
                    + collision.firstPosition + "\t"
                    + clean(collision.firstRole) + "\t"
                    + clean(collision.firstOwner) + "\t"
                    + clean(collision.firstOrigin) + "\t"
                    + clean(collision.firstEntry) + "\t"
                    + collision.firstRelease + "\t"
                    + collision.firstSha + "\t"
                    + collision.laterPosition + "\t"
                    + clean(collision.laterRole) + "\t"
                    + clean(collision.laterOwner) + "\t"
                    + clean(collision.laterOrigin) + "\t"
                    + clean(collision.laterEntry) + "\t"
                    + collision.laterRelease + "\t"
                    + collision.laterSha + "\t"
                    + collision.byteRelation + "\t"
                    + collision.disposition);
        }
        writeLines(output.resolve("collisions.tsv"), collisionRows);

        List<RuntimeShadow> shadows = new ArrayList<>(state.shadows);
        shadows.sort(Comparator
                .comparing((RuntimeShadow shadow) -> shadow.lane)
                .thenComparingInt(shadow -> shadow.position)
                .thenComparing(shadow -> shadow.internalName));
        List<String> shadowRows = new ArrayList<>();
        shadowRows.add("lane\tposition\trole\tinternal_name\torigin"
                + "\tclass_sha256\tcandidate_a_disposition"
                + "\tcandidate_b_disposition\toracle_disposition");
        for (RuntimeShadow shadow : shadows) {
            shadowRows.add(shadow.lane + "\t" + shadow.position + "\t"
                    + clean(shadow.role) + "\t"
                    + clean(shadow.internalName) + "\t"
                    + clean(shadow.origin) + "\t" + shadow.sha + "\t"
                    + shadow.candidateADisposition + "\t"
                    + shadow.candidateBDisposition + "\t"
                    + shadow.oracleDisposition);
        }
        writeLines(output.resolve("runtime-shadows.tsv"), shadowRows);

        List<Violation> violations = new ArrayList<>(state.violations);
        violations.sort(Comparator.comparing((Violation row) -> row.boundary)
                .thenComparing(row -> row.scope)
                .thenComparing(row -> row.path)
                .thenComparing(row -> row.rule)
                .thenComparing(row -> row.detail));
        List<String> violationRows = new ArrayList<>();
        violationRows.add("boundary\tscope\tpath\trule\tdetail");
        for (Violation violation : violations) {
            violationRows.add(clean(violation.boundary) + "\t"
                    + clean(violation.scope) + "\t" + clean(violation.path)
                    + "\t" + clean(violation.rule) + "\t"
                    + clean(violation.detail));
        }
        writeLines(output.resolve("violations.tsv"), violationRows);

        long candidateAClasses = state.classes.stream()
                .filter(row -> row.boundary.equals("candidate-a")).count();
        long candidateBClasses = state.classes.stream()
                .filter(row -> row.boundary.equals("candidate-b")).count();
        long runtimeClasses = state.classes.stream()
                .filter(row -> row.boundary.startsWith("runtime-")).count();
        long unresolved = state.references.stream()
                .filter(row -> row.resolution.equals("UNRESOLVED")).count();
        long memberReferences = state.references.stream()
                .filter(ScanExactAotBoundary::typedMemberReference).count();
        long oracleCollisions = state.collisions.stream()
                .filter(collision -> collision.lane.equals("oracle")).count();
        long candidateCollisions = state.collisions.stream()
                .filter(collision -> collision.lane.equals("candidate")
                        && collision.kind.equals("class")).count();
        long candidateIdentical = state.collisions.stream()
                .filter(collision -> collision.lane.equals("candidate")
                        && collision.kind.equals("class")
                        && collision.byteRelation.equals("BYTE_IDENTICAL"))
                .count();
        List<String> summary = List.of(
                "metric\tvalue",
                "status\t" + (state.violations.isEmpty()
                        && state.ledgerComplete ? "PASS" : "FAIL"),
                "scope\texact-AOT candidate/original/peer/two-lane runtime boundary",
                "stage.1.pass.claimed\tfalse",
                "jvm.verifier.run\tNOT_RUN",
                "wrapper.integration.run\tNOT_RUN",
                "cohort.namespaces\t" + state.cohort.size(),
                "expected.candidate.classes\t"
                        + state.originalOwnership.size(),
                "candidate.a.classes\t" + candidateAClasses,
                "candidate.b.classes\t" + candidateBClasses,
                "candidate.runtime.elements\t"
                        + state.runtimeElements.stream().filter(element ->
                                element.lane.equals("candidate")).count(),
                "oracle.fallback.elements\t"
                        + state.runtimeElements.stream().filter(element ->
                                element.lane.equals("oracle")).count(),
                "oracle.transactor.position\t0",
                "peer.inventory.only\ttrue",
                "runtime.classes\t" + runtimeClasses,
                "entries.discovered\t" + state.discoveredEntries,
                "entries.recorded\t" + state.recordedEntries,
                "inputs.discovered\t" + state.discoveredInputs,
                "inputs.recorded\t" + state.recordedInputs,
                "references.recorded\t" + state.references.size(),
                "member.references.recorded\t" + memberReferences,
                "member.resolution.policy\tEXACT_DECLARATION_OR_INHERITED_TYPED_MEMBER",
                "references.unresolved\t" + unresolved,
                "collisions\t" + state.collisions.size(),
                "candidate.ordered.class.collisions\t"
                        + candidateCollisions,
                "candidate.ordered.class.collisions.byte-identical\t"
                        + candidateIdentical,
                "candidate.ordered.class.collisions.byte-different\t"
                        + (candidateCollisions - candidateIdentical),
                "candidate.collision.policy.rows\t"
                        + state.candidateCollisionPolicy.size(),
                "candidate.collision.policy\tPOLICY_BOUND_ORDERED_FIRST_ORIGIN_WINS",
                "oracle.ordered.collisions\t" + oracleCollisions,
                "oracle.collision.policy\tORDERED_FIRST_ORIGIN_WINS",
                "violations\t" + state.violations.size(),
                "entry.ledger.complete\t" + state.ledgerComplete,
                "unknown.attribute.policy\tFAIL",
                "generated.output.nested.archive.policy\tFAIL_AND_INVENTORY",
                "runtime.nested.archive.policy\tBOUNDED_RECURSIVE_INVENTORY",
                "keystore.policy\tFAIL",
                "licensed.artifact.policy\tFIVE_EXACT_ROLE_SHA_IDENTITIES",
                "original.nano.candidate.policy\tFORBID",
                "sanitized.nano.candidate.policy\tREQUIRE_EXACT_ROLE_AND_SHA");
        writeLines(output.resolve("summary.tsv"), summary);
        writeSelfExcludingManifest(output, List.of("collisions.tsv",
                "entries.tsv", "inputs.tsv", "references.tsv",
                "runtime-elements.tsv", "runtime-shadows.tsv", "summary.tsv",
                "violations.tsv"));
    }

    private static void writeSelfExcludingManifest(Path output,
                                                    List<String> names)
            throws IOException {
        List<String> sorted = new ArrayList<>(names);
        Collections.sort(sorted);
        List<String> rows = new ArrayList<>();
        for (String name : sorted) {
            rows.add(sha256(output.resolve(name)) + "  " + name);
        }
        writeLines(output.resolve("manifest.sha256"), rows);
    }

    @FunctionalInterface
    private interface CheckedRunnable { void run() throws Exception; }

    private static void selfTest(List<String> rows, String name,
                                 CheckedRunnable body) {
        try {
            body.run();
            rows.add(name + "\tPASS");
        } catch (Throwable failure) {
            rows.add(name + "\tFAIL:" + clean(failure.getClass().getName()
                    + ":" + failure.getMessage()));
        }
    }

    private static final class RuntimeFixture {
        final String role;
        final String kind;
        final Path path;
        RuntimeFixture(String role, String kind, Path path) {
            this.role = role;
            this.kind = kind;
            this.path = path;
        }
    }

    private static final class Fixture {
        final Path root;
        final Path candidateA;
        final Path candidateB;
        final Path candidateDependency;
        final Path oracleDependency;
        final Path originalJar;
        final Path peerJar;
        final Path core2Jar;
        final Path nanoOriginalJar;
        final Path nanoSanitizedJar;
        final byte[] originalNanoJks;
        final Path cohort;
        final Path ownership;
        final Path mappings;
        final Path runtimePolicy;
        final Path candidateLedger;
        final Path oracleLedger;
        final Path candidateCollisions;
        final Path allowed;
        final Path forbidden;
        final List<RuntimeFixture> candidateRuntime = new ArrayList<>();
        final List<RuntimeFixture> oracleRuntime = new ArrayList<>();
        final List<String> allowedRows = new ArrayList<>();
        final List<String> forbiddenRows = new ArrayList<>();
        String candidateAName = "fixture/core";
        String candidateBName = "fixture/core";
        int scanNumber;

        Fixture() throws Exception {
            root = Files.createTempDirectory("scan-exact-aot-selftest.")
                    .toRealPath();
            candidateA = Files.createDirectories(root.resolve("candidate-a"));
            candidateB = Files.createDirectories(root.resolve("candidate-b"));
            candidateDependency = Files.createDirectories(
                    root.resolve("candidate-dependency"));
            oracleDependency = Files.createDirectories(
                    root.resolve("oracle-dependency"));
            originalJar = root.resolve("transactor-original.jar");
            peerJar = root.resolve("peer-original.jar");
            core2Jar = root.resolve("core2-original.jar");
            nanoOriginalJar = root.resolve("nano-original.jar");
            nanoSanitizedJar = root.resolve("nano-sanitized.jar");
            cohort = root.resolve("cohort.tsv");
            ownership = root.resolve("ownership.tsv");
            mappings = root.resolve("class-mappings.tsv");
            runtimePolicy = root.resolve("runtime-policy.tsv");
            candidateLedger = root.resolve("candidate-runtime.tsv");
            oracleLedger = root.resolve("oracle-runtime.tsv");
            candidateCollisions = root.resolve("candidate-collisions.tsv");
            allowed = root.resolve("allowed.tsv");
            forbidden = root.resolve("forbidden.tsv");
            writeText(candidateCollisions, CANDIDATE_COLLISION_HEADER + "\n");
            writeText(cohort, "namespace\texpected_classes\nfixture.core\t1\n");
            writeText(ownership,
                    "entry\towner_id\nfixture/core.class\tfixture.core\n");
            byte[] basic = fixtureClass("fixture/core", null, false, false);
            writeCandidate(candidateA, "fixture/core.class", basic);
            writeCandidate(candidateB, "fixture/core.class", basic);
            writeJar(originalJar, Map.of("fixture/core.class", basic,
                    "transactor.edn", "transactor\n".getBytes(
                            StandardCharsets.UTF_8)));
            writeJar(peerJar, Map.of("peer.edn",
                    "peer\n".getBytes(StandardCharsets.UTF_8)));
            writeJar(core2Jar, Map.of("core2.edn",
                    "core2\n".getBytes(StandardCharsets.UTF_8)));
            originalNanoJks = new byte[] {(byte) 0xfe, (byte) 0xed,
                    (byte) 0xfe, (byte) 0xed, 0, 0, 0, 2};
            writeJar(nanoOriginalJar, Map.of(
                    "nano.edn", "nano-original\n".getBytes(
                            StandardCharsets.UTF_8),
                    "nano_impl/transactor-key.jks", originalNanoJks));
            writeJar(nanoSanitizedJar, Map.of("nano.edn",
                    "nano-sanitized\n".getBytes(StandardCharsets.UTF_8)));
            candidateRuntime.add(new RuntimeFixture("nano-sanitized", "jar",
                    nanoSanitizedJar));
            candidateRuntime.add(new RuntimeFixture("candidate-dependency",
                    "directory", candidateDependency));
            oracleRuntime.add(new RuntimeFixture("core2-original", "jar",
                    core2Jar));
            oracleRuntime.add(new RuntimeFixture("nano-original", "jar",
                    nanoOriginalJar));
            oracleRuntime.add(new RuntimeFixture("oracle-dependency",
                    "directory", oracleDependency));
            forbiddenRows.add("core2-original-content\t" + sha256(core2Jar)
                    + "\toriginal core2 fixture");
            forbiddenRows.add("nano-original-content\t"
                    + sha256(nanoOriginalJar) + "\toriginal Nano fixture");
            forbiddenRows.add("nano-original-jks-content\t"
                    + sha256(originalNanoJks)
                    + "\toriginal Nano keystore fixture");
            forbiddenRows.add("never-match\t"
                    + "0000000000000000000000000000000000000000000000000000000000000000"
                    + "\tsynthetic nonmatching control");
        }

        void addCandidateRuntime(String role, String kind, Path path) {
            candidateRuntime.add(new RuntimeFixture(role, kind, path));
        }

        void addOracleRuntime(String role, String kind, Path path) {
            oracleRuntime.add(new RuntimeFixture(role, kind, path));
        }

        void setCandidateCollisionPolicy(String internalName,
                int firstIndex, String firstEntry, int firstRelease,
                String firstSha, int laterIndex, String laterEntry,
                int laterRelease, String laterSha) throws IOException {
            RuntimeFixture first = candidateRuntime.get(firstIndex);
            RuntimeFixture later = candidateRuntime.get(laterIndex);
            String relation = firstSha.equals(laterSha)
                    ? "BYTE_IDENTICAL" : "BYTE_DIFFERENT";
            writeText(candidateCollisions, CANDIDATE_COLLISION_HEADER + "\n"
                    + "candidate\t" + internalName + "\t"
                    + (firstIndex + 1) + "\t" + first.role + "\t"
                    + firstEntry + "\t" + firstRelease + "\t" + firstSha
                    + "\t" + (laterIndex + 1) + "\t" + later.role + "\t"
                    + laterEntry + "\t" + laterRelease + "\t" + laterSha
                    + "\t" + relation + "\n");
        }

        void renameCandidates(String candidateAInternal,
                              String candidateBInternal) throws IOException {
            Files.delete(candidateA.resolve("fixture.core/" + candidateAName
                    + ".class"));
            Files.delete(candidateB.resolve("fixture.core/" + candidateBName
                    + ".class"));
            candidateAName = candidateAInternal;
            candidateBName = candidateBInternal;
            writeCandidate(candidateA, candidateAName + ".class",
                    fixtureClass(candidateAName, null, false, false));
            writeCandidate(candidateB, candidateBName + ".class",
                    fixtureClass(candidateBName, null, false, false));
        }

        private void writeLedger(Path path, List<RuntimeFixture> runtime)
                throws IOException {
            StringBuilder runtimeText = new StringBuilder(
                    "position\trole\tkind\tpath\texpected_sha256\n");
            int position = 0;
            for (RuntimeFixture element : runtime) {
                String hash = element.kind.equals("directory")
                        ? directoryManifest(element.path) : sha256(element.path);
                runtimeText.append(++position).append('\t').append(element.role)
                        .append('\t').append(element.kind).append('\t')
                        .append(element.path)
                        .append('\t').append(hash).append('\n');
            }
            writeText(path, runtimeText.toString());
        }

        Config config() throws Exception {
            return config(root.resolve("scan-output-" + (++scanNumber)));
        }

        Config config(Path output) throws Exception {
            writeText(mappings,
                    "comparison\tnamespace\tleft_class\tright_class\tidentity\n"
                    + "candidate-a--candidate-b\tfixture.core\t"
                    + candidateAName + "\t" + candidateBName + "\t"
                    + candidateAName.equals(candidateBName) + "\n"
                    + "candidate-a--original\tfixture.core\t"
                    + candidateAName + "\tfixture/core\t"
                    + candidateAName.equals("fixture/core") + "\n"
                    + "candidate-b--original\tfixture.core\t"
                    + candidateBName + "\tfixture/core\t"
                    + candidateBName.equals("fixture/core") + "\n");
            writeLedger(candidateLedger, candidateRuntime);
            writeLedger(oracleLedger, oracleRuntime);
            String policyHeader = "rule_id\tsha256\tcandidate_disposition"
                    + "\tcandidate_role\toracle_disposition\toracle_role"
                    + "\tcandidate_elements\toracle_elements"
                    + "\tcandidate_ledger_sha256\toracle_ledger_sha256\n";
            String anchors = "\t" + candidateRuntime.size() + "\t"
                    + oracleRuntime.size() + "\t" + sha256(candidateLedger)
                    + "\t" + sha256(oracleLedger) + "\n";
            writeText(runtimePolicy, policyHeader
                    + "transactor-original\t" + sha256(originalJar)
                    + "\tFORBID\t\tOWNED\t" + anchors
                    + "peer-original\t" + sha256(peerJar)
                    + "\tFORBID\t\tFORBID\t" + anchors
                    + "core2-original\t" + sha256(core2Jar)
                    + "\tFORBID\t\tREQUIRE\tcore2-original" + anchors
                    + "nano-original\t" + sha256(nanoOriginalJar)
                    + "\tFORBID\t\tREQUIRE\tnano-original" + anchors
                    + "nano-sanitized\t" + sha256(nanoSanitizedJar)
                    + "\tREQUIRE\tnano-sanitized\tFORBID\t" + anchors);
            StringBuilder allowedText = new StringBuilder(
                    "candidate\tpath\towner\tsha256\n");
            for (String row : allowedRows) allowedText.append(row).append('\n');
            writeText(allowed, allowedText.toString());
            StringBuilder forbiddenText = new StringBuilder(
                    "rule_id\tsha256\treason\n");
            for (String row : forbiddenRows) forbiddenText.append(row).append('\n');
            writeText(forbidden, forbiddenText.toString());
            return new Config(candidateA, candidateB, originalJar, peerJar,
                    cohort, ownership, mappings, runtimePolicy,
                    candidateLedger, oracleLedger, candidateCollisions,
                    allowed, forbidden, output, false);
        }
    }

    private static void writeText(Path path, String text) throws IOException {
        Files.write(path, text.getBytes(StandardCharsets.UTF_8));
    }

    private static void writeCandidate(Path root, String internalPath,
                                       byte[] bytes) throws IOException {
        Path target = root.resolve("fixture.core").resolve(internalPath);
        Files.createDirectories(target.getParent());
        Files.write(target, bytes);
    }

    private static byte[] fixtureClass(String internal, String escapeType,
                                       boolean handleEscape,
                                       boolean unknownAttribute) {
        ClassWriter writer = new ClassWriter(0);
        writer.visit(Opcodes.V11, Opcodes.ACC_PUBLIC | Opcodes.ACC_SUPER,
                internal, null, "java/lang/Object", null);
        writer.visitAnnotation("Ljava/lang/Deprecated;", true).visitEnd();
        writer.visitField(Opcodes.ACC_PUBLIC, "text",
                escapeType == null ? "Ljava/lang/String;"
                        : "L" + escapeType + ";", null, null).visitEnd();
        MethodVisitor constructor = writer.visitMethod(Opcodes.ACC_PUBLIC,
                "<init>", "()V", null, null);
        constructor.visitCode();
        constructor.visitVarInsn(Opcodes.ALOAD, 0);
        constructor.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/Object",
                "<init>", "()V", false);
        constructor.visitInsn(Opcodes.RETURN);
        constructor.visitMaxs(1, 1);
        constructor.visitEnd();
        MethodVisitor constants = writer.visitMethod(Opcodes.ACC_PUBLIC
                | Opcodes.ACC_STATIC, "constants", "()V", null, null);
        constants.visitCode();
        constants.visitLdcInsn(Type.getObjectType("java/lang/String"));
        constants.visitInsn(Opcodes.POP);
        if (handleEscape) {
            constants.visitLdcInsn(new Handle(Opcodes.H_INVOKESTATIC,
                    "missing/Bootstrap", "call", "()V", false));
            constants.visitInsn(Opcodes.POP);
        }
        constants.visitInsn(Opcodes.RETURN);
        constants.visitMaxs(1, 0);
        constants.visitEnd();
        if (unknownAttribute) writer.visitAttribute(new Attribute(
                "FixtureUnknownAttribute") {
            @Override protected ByteVector write(ClassWriter classWriter,
                    byte[] code, int codeLength, int maxStack, int maxLocals) {
                return new ByteVector().putByte(1);
            }
        });
        writer.visitEnd();
        return writer.toByteArray();
    }

    private static byte[] fixtureCaller(String internal, String targetOwner,
                                        String targetMethod) {
        ClassWriter writer = new ClassWriter(0);
        writer.visit(Opcodes.V11, Opcodes.ACC_PUBLIC | Opcodes.ACC_SUPER,
                internal, null, "java/lang/Object", null);
        MethodVisitor constructor = writer.visitMethod(Opcodes.ACC_PUBLIC,
                "<init>", "()V", null, null);
        constructor.visitCode();
        constructor.visitVarInsn(Opcodes.ALOAD, 0);
        constructor.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/Object",
                "<init>", "()V", false);
        constructor.visitInsn(Opcodes.RETURN);
        constructor.visitMaxs(1, 1);
        constructor.visitEnd();
        MethodVisitor call = writer.visitMethod(Opcodes.ACC_PUBLIC
                | Opcodes.ACC_STATIC, "call", "()V", null, null);
        call.visitCode();
        call.visitMethodInsn(Opcodes.INVOKESTATIC, targetOwner, targetMethod,
                "()V", false);
        call.visitInsn(Opcodes.RETURN);
        call.visitMaxs(0, 0);
        call.visitEnd();
        writer.visitEnd();
        return writer.toByteArray();
    }

    private static void writeJar(Path path, Map<String, byte[]> values)
            throws IOException {
        List<String> names = new ArrayList<>(values.keySet());
        Collections.sort(names);
        try (ZipOutputStream zip = new ZipOutputStream(
                Files.newOutputStream(path))) {
            for (String name : names) {
                ZipEntry entry = new ZipEntry(name);
                entry.setTime(0L);
                zip.putNextEntry(entry);
                zip.write(values.get(name));
                zip.closeEntry();
            }
        }
    }

    private static byte[] zipPayload(String name, byte[] payload)
            throws IOException {
        return zipPayload(Map.of(name, payload));
    }

    private static byte[] zipPayload(Map<String, byte[]> values)
            throws IOException {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (ZipOutputStream zip = new ZipOutputStream(bytes)) {
            List<String> names = new ArrayList<>(values.keySet());
            Collections.sort(names);
            for (String name : names) {
                ZipEntry entry = new ZipEntry(name);
                entry.setTime(0L);
                zip.putNextEntry(entry);
                zip.write(values.get(name));
                zip.closeEntry();
            }
        }
        return bytes.toByteArray();
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

    private static byte[] corruptZipEntryCount(byte[] archive) {
        byte[] result = archive.clone();
        int eocd = result.length - 22;
        require(eocd >= 0 && result[eocd] == 0x50
                        && result[eocd + 1] == 0x4b
                        && result[eocd + 2] == 0x05
                        && result[eocd + 3] == 0x06
                        && u2le(result, eocd + 20) == 0,
                "fixture ZIP lacks an un-commented end record");
        int corrupted = u2le(result, eocd + 10) + 1;
        require(corrupted <= 0xffff,
                "fixture ZIP entry count cannot be incremented");
        result[eocd + 8] = (byte) corrupted;
        result[eocd + 9] = (byte) (corrupted >>> 8);
        result[eocd + 10] = (byte) corrupted;
        result[eocd + 11] = (byte) (corrupted >>> 8);
        return result;
    }

    private static void requireStatus(int actual, int expected, String label) {
        require(actual == expected, label + " status " + actual
                + " expected " + expected);
    }

    private static void requireViolation(Path output, String fragment)
            throws IOException {
        String text = Files.readString(output.resolve("violations.tsv"),
                StandardCharsets.UTF_8);
        require(text.contains(fragment),
                "expected violation fragment absent: " + fragment);
    }

    private static void requireNoViolation(Path output, String fragment)
            throws IOException {
        String text = Files.readString(output.resolve("violations.tsv"),
                StandardCharsets.UTF_8);
        require(!text.contains(fragment),
                "unexpected violation fragment present: " + fragment);
    }

    private static void requireEvidence(Path output, String file,
                                        String fragment) throws IOException {
        String text = Files.readString(output.resolve(file),
                StandardCharsets.UTF_8);
        require(text.contains(fragment), "expected " + file
                + " fragment absent: " + fragment);
    }

    private static int runSelfTests(Path output) throws Exception {
        output = absolute(output);
        if (Files.exists(output)) {
            require(Files.isDirectory(output, LinkOption.NOFOLLOW_LINKS)
                            && !Files.isSymbolicLink(output),
                    "self-test output is not a real directory: " + output);
            try (Stream<Path> children = Files.list(output)) {
                require(!children.findAny().isPresent(),
                        "refusing non-empty self-test output: " + output);
            }
        }
        Files.createDirectories(output);
        List<String> rows = new ArrayList<>();
        rows.add("test\tstatus");

        selfTest(rows, "positive-complete-two-lane-boundary", () -> {
            Fixture fixture = new Fixture();
            Config config = fixture.config();
            requireStatus(runScan(config), 0, "positive boundary");
            requireEvidence(config.output, "runtime-elements.tsv",
                    "oracle\t0\towned-transactor-root");
            requireEvidence(config.output, "runtime-elements.tsv",
                    "inventory\t0\tpeer-inventory-only");
            requireEvidence(config.output, "runtime-elements.tsv",
                    "oracle\t1\tcore2-original\tjar");
            requireEvidence(config.output, "runtime-elements.tsv",
                    "oracle\t2\tnano-original\tjar");
            requireEvidence(config.output, "entries.tsv",
                    "nano_impl/transactor-key.jks");
            requireEvidence(config.output, "summary.tsv",
                    "member.resolution.policy\tEXACT_DECLARATION_OR_INHERITED_TYPED_MEMBER");
        });

        selfTest(rows, "same-cardinality-wrong-membership-rejected", () -> {
            Fixture fixture = new Fixture();
            Files.delete(fixture.candidateA.resolve(
                    "fixture.core/fixture/core.class"));
            writeCandidate(fixture.candidateA, "fixture/other.class",
                    fixtureClass("fixture/other", null, false, false));
            Config config = fixture.config();
            requireStatus(runScan(config), 2, "wrong candidate membership");
            requireViolation(config.output, "exact-class-membership");
        });

        selfTest(rows, "allowed-generated-resource-exact", () -> {
            Fixture fixture = new Fixture();
            byte[] resource = "generated-resource\n"
                    .getBytes(StandardCharsets.UTF_8);
            String relative = "fixture.core/fixture/generated.edn";
            Files.createDirectories(fixture.candidateA.resolve(
                    "fixture.core/fixture"));
            Files.createDirectories(fixture.candidateB.resolve(
                    "fixture.core/fixture"));
            Files.write(fixture.candidateA.resolve(relative), resource);
            Files.write(fixture.candidateB.resolve(relative), resource);
            fixture.allowedRows.add("both\t" + relative
                    + "\tfixture.core.generated\t" + sha256(resource));
            Config config = fixture.config();
            requireStatus(runScan(config), 0, "allowed resource");
        });

        selfTest(rows, "renamed-forbidden-payload-rejected-and-ledgered", () -> {
            Fixture fixture = new Fixture();
            Path renamed = fixture.root.resolve("harmless-dependency.jar");
            writeJar(renamed, Map.of("payload.txt", "licensed-original"
                    .getBytes(StandardCharsets.UTF_8)));
            fixture.addCandidateRuntime("renamed-forbidden", "jar", renamed);
            fixture.forbiddenRows.add("licensed-original-content\t"
                    + sha256(renamed) + "\traw original fixture");
            Config config = fixture.config();
            requireStatus(runScan(config), 2, "renamed payload");
            requireViolation(config.output, "licensed-original-content");
            requireEvidence(config.output, "runtime-elements.tsv",
                    "renamed-forbidden\tjar");
        });

        selfTest(rows, "original-core2-renamed-candidate-rejected", () -> {
            Fixture fixture = new Fixture();
            Path renamed = fixture.root.resolve("harmless-core-library.jar");
            Files.copy(fixture.core2Jar, renamed);
            fixture.addCandidateRuntime("harmless-core-library", "jar",
                    renamed);
            Config config = fixture.config();
            requireStatus(runScan(config), 2,
                    "renamed original core2 candidate");
            requireViolation(config.output, "core2-original");
            requireViolation(config.output, "core2-original-content");
        });

        selfTest(rows, "original-jks-content-rejected-in-candidate", () -> {
            Fixture fixture = new Fixture();
            Path renamed = fixture.candidateDependency.resolve(
                    "harmless-resource.bin");
            Files.write(renamed, fixture.originalNanoJks);
            Config config = fixture.config();
            requireStatus(runScan(config), 2,
                    "renamed original JKS candidate content");
            requireViolation(config.output, "nano-original-jks-content");
            requireViolation(config.output, "raw-keystore-resource");
        });

        selfTest(rows, "oracle-original-core2-wrong-role-rejected", () -> {
            Fixture fixture = new Fixture();
            fixture.oracleRuntime.set(0, new RuntimeFixture(
                    "core2-role-spoof", "jar", fixture.core2Jar));
            Config config = fixture.config();
            requireStatus(runScan(config), 2, "oracle core2 role spoof");
            requireViolation(config.output, "required-role-hash");
        });

        selfTest(rows, "oracle-original-core2-hash-spoof-rejected", () -> {
            Fixture fixture = new Fixture();
            Path impostor = fixture.root.resolve("core2-impostor.jar");
            writeJar(impostor, Map.of("core2.edn",
                    "impostor\n".getBytes(StandardCharsets.UTF_8)));
            fixture.oracleRuntime.set(0, new RuntimeFixture(
                    "core2-original", "jar", impostor));
            Config config = fixture.config();
            requireStatus(runScan(config), 2, "oracle core2 hash spoof");
            requireViolation(config.output, "required-role-hash");
        });

        selfTest(rows, "oracle-original-core2-path-tamper-rejected", () -> {
            Fixture fixture = new Fixture();
            Path rebound = fixture.root.resolve("core2-rebound.jar");
            Files.copy(fixture.core2Jar, rebound);
            Config config = fixture.config();
            String ledger = Files.readString(fixture.oracleLedger,
                    StandardCharsets.UTF_8);
            require(ledger.contains(fixture.core2Jar.toString()),
                    "oracle fixture ledger lacks core2 path");
            writeText(fixture.oracleLedger, ledger.replace(
                    fixture.core2Jar.toString(), rebound.toString()));
            requireStatus(runScan(config), 2,
                    "post-binding oracle core2 path tamper");
            requireViolation(config.output, "runtime-ledger-anchor");
        });

        selfTest(rows, "original-and-sanitized-nano-lane-swap-rejected", () -> {
            Fixture fixture = new Fixture();
            fixture.candidateRuntime.set(0, new RuntimeFixture(
                    "nano-original", "jar", fixture.nanoOriginalJar));
            fixture.oracleRuntime.set(1, new RuntimeFixture(
                    "nano-sanitized", "jar", fixture.nanoSanitizedJar));
            Config config = fixture.config();
            requireStatus(runScan(config), 2, "Nano lane swap");
            requireViolation(config.output, "nano-original");
            requireViolation(config.output, "nano-sanitized");
        });

        selfTest(rows, "sanitized-nano-name-allowed-only-by-role-sha", () -> {
            Fixture fixture = new Fixture();
            Path originalLooking = fixture.root.resolve("nano-impl-1.0.jar");
            Files.copy(fixture.nanoSanitizedJar, originalLooking);
            fixture.candidateRuntime.set(0, new RuntimeFixture(
                    "nano-sanitized", "jar", originalLooking));
            Config config = fixture.config();
            requireStatus(runScan(config), 0, "sanitized Nano exact exception");
        });

        selfTest(rows, "original-nano-renamed-candidate-rejected", () -> {
            Fixture fixture = new Fixture();
            Path renamed = fixture.root.resolve("safe-library.jar");
            Files.copy(fixture.nanoOriginalJar, renamed);
            fixture.addCandidateRuntime("safe-library", "jar", renamed);
            Config config = fixture.config();
            requireStatus(runScan(config), 2, "original Nano candidate");
            requireViolation(config.output, "nano-original");
        });

        selfTest(rows, "sanitized-nano-wrong-role-rejected", () -> {
            Fixture fixture = new Fixture();
            fixture.candidateRuntime.set(0, new RuntimeFixture(
                    "wrong-nano-role", "jar", fixture.nanoSanitizedJar));
            Config config = fixture.config();
            requireStatus(runScan(config), 2, "sanitized Nano wrong role");
            requireViolation(config.output, "required-role-hash");
        });

        selfTest(rows, "peer-identity-forbidden-in-candidate", () -> {
            Fixture fixture = new Fixture();
            Path copy = fixture.root.resolve("renamed-peer-candidate.jar");
            Files.copy(fixture.peerJar, copy);
            fixture.addCandidateRuntime("peer-copy", "jar", copy);
            Config config = fixture.config();
            requireStatus(runScan(config), 2, "Peer candidate contamination");
            requireViolation(config.output, "peer-original");
        });

        selfTest(rows, "peer-identity-forbidden-in-oracle", () -> {
            Fixture fixture = new Fixture();
            Path copy = fixture.root.resolve("renamed-peer-oracle.jar");
            Files.copy(fixture.peerJar, copy);
            fixture.addOracleRuntime("peer-copy", "jar", copy);
            Config config = fixture.config();
            requireStatus(runScan(config), 2, "Peer oracle contamination");
            requireViolation(config.output, "peer-original");
        });

        selfTest(rows, "transactor-renamed-runtime-copy-rejected", () -> {
            Fixture fixture = new Fixture();
            Path copy = fixture.root.resolve("renamed-root.jar");
            Files.copy(fixture.originalJar, copy);
            fixture.addCandidateRuntime("renamed-root", "jar", copy);
            Config config = fixture.config();
            requireStatus(runScan(config), 2, "Transactor candidate copy");
            requireViolation(config.output, "transactor-original");
        });

        selfTest(rows, "same-dependency-scanned-in-disjoint-lanes", () -> {
            Fixture fixture = new Fixture();
            Path shared = fixture.root.resolve("shared.jar");
            writeJar(shared, Map.of("dep/Shared.class",
                    fixtureClass("dep/Shared", null, false, false)));
            fixture.addCandidateRuntime("shared-candidate", "jar", shared);
            fixture.addOracleRuntime("shared-oracle", "jar", shared);
            Config config = fixture.config();
            requireStatus(runScan(config), 0, "disjoint runtime lanes");
            requireEvidence(config.output, "entries.tsv", "runtime-candidate");
            requireEvidence(config.output, "entries.tsv", "runtime-oracle");
        });

        selfTest(rows, "candidate-runtime-duplicate-class-rejected", () -> {
            Fixture fixture = new Fixture();
            byte[] duplicate = fixtureClass("dep/Dup", null, false, false);
            Path one = fixture.root.resolve("dep-one.jar");
            Path two = fixture.root.resolve("dep-two.jar");
            writeJar(one, Map.of("dep/Dup.class", duplicate));
            writeJar(two, Map.of("dep/Dup.class", duplicate));
            fixture.addCandidateRuntime("dep-one", "jar", one);
            fixture.addCandidateRuntime("dep-two", "jar", two);
            Config config = fixture.config();
            requireStatus(runScan(config), 2, "candidate duplicate class");
            requireViolation(config.output,
                    "missing-candidate-collision-policy");
            requireViolation(config.output, "duplicate-logical-entry");
        });

        selfTest(rows, "candidate-runtime-policy-bound-collision-passes",
                () -> {
            Fixture fixture = new Fixture();
            String internal = "jline/console/Fixture";
            String entry = internal + ".class";
            byte[] first = fixtureClass(internal, null, false, false);
            byte[] later = fixtureClass(internal, "java/lang/Object", false,
                    false);
            Path firstJar = fixture.root.resolve("jline-first.jar");
            Path laterJar = fixture.root.resolve("jline-later.jar");
            writeJar(firstJar, Map.of(entry, first));
            writeJar(laterJar, Map.of(entry, later));
            fixture.addCandidateRuntime("jline-first", "jar", firstJar);
            fixture.addCandidateRuntime("jline-later", "jar", laterJar);
            fixture.setCandidateCollisionPolicy(internal, 2, entry, 0,
                    sha256(first), 3, entry, 0, sha256(later));
            Config config = fixture.config();
            requireStatus(runScan(config), 0,
                    "policy-bound candidate collision");
            requireEvidence(config.output, "collisions.tsv",
                    sha256(first) + "\t4\tjline-later");
            requireEvidence(config.output, "collisions.tsv",
                    sha256(later) + "\tBYTE_DIFFERENT"
                            + "\tPOLICY_BOUND_ORDERED_FIRST_ORIGIN_WINS");
        });

        selfTest(rows, "candidate-runtime-identical-collision-recorded",
                () -> {
            Fixture fixture = new Fixture();
            String internal = "jline/console/Identical";
            String entry = internal + ".class";
            byte[] bytes = fixtureClass(internal, null, false, false);
            Path firstJar = fixture.root.resolve("identical-first.jar");
            Path laterJar = fixture.root.resolve("identical-later.jar");
            writeJar(firstJar, Map.of(entry, bytes));
            writeJar(laterJar, Map.of(entry, bytes));
            fixture.addCandidateRuntime("identical-first", "jar", firstJar);
            fixture.addCandidateRuntime("identical-later", "jar", laterJar);
            fixture.setCandidateCollisionPolicy(internal, 2, entry, 0,
                    sha256(bytes), 3, entry, 0, sha256(bytes));
            Config config = fixture.config();
            requireStatus(runScan(config), 0,
                    "byte-identical candidate collision");
            requireEvidence(config.output, "collisions.tsv",
                    "BYTE_IDENTICAL\tPOLICY_BOUND_ORDERED_FIRST_ORIGIN_WINS");
        });

        selfTest(rows, "candidate-collision-order-reversal-rejected", () -> {
            Fixture fixture = new Fixture();
            String internal = "jline/console/Reordered";
            String entry = internal + ".class";
            byte[] first = fixtureClass(internal, null, false, false);
            byte[] later = fixtureClass(internal, "java/lang/Object", false,
                    false);
            Path firstJar = fixture.root.resolve("order-first.jar");
            Path laterJar = fixture.root.resolve("order-later.jar");
            writeJar(firstJar, Map.of(entry, first));
            writeJar(laterJar, Map.of(entry, later));
            fixture.addCandidateRuntime("order-first", "jar", firstJar);
            fixture.addCandidateRuntime("order-later", "jar", laterJar);
            fixture.setCandidateCollisionPolicy(internal, 2, entry, 0,
                    sha256(first), 3, entry, 0, sha256(later));
            Collections.swap(fixture.candidateRuntime, 2, 3);
            Config config = fixture.config();
            requireStatus(runScan(config), 2,
                    "candidate collision order reversal");
            requireViolation(config.output, "collision-role-position-spoof");
            requireViolation(config.output,
                    "candidate-collision-policy-mismatch");
        });

        selfTest(rows, "extra-candidate-collision-policy-row-rejected", () -> {
            Fixture fixture = new Fixture();
            String internal = "jline/console/Ghost";
            String entry = internal + ".class";
            byte[] bytes = fixtureClass(internal, null, false, false);
            Path firstJar = fixture.root.resolve("ghost-first.jar");
            Path laterJar = fixture.root.resolve("ghost-later.jar");
            writeJar(firstJar, Map.of("jline/console/First.class",
                    fixtureClass("jline/console/First", null, false, false)));
            writeJar(laterJar, Map.of("jline/console/Later.class",
                    fixtureClass("jline/console/Later", null, false, false)));
            fixture.addCandidateRuntime("ghost-first", "jar", firstJar);
            fixture.addCandidateRuntime("ghost-later", "jar", laterJar);
            fixture.setCandidateCollisionPolicy(internal, 2, entry, 0,
                    sha256(bytes), 3, entry, 0, sha256(bytes));
            Config config = fixture.config();
            requireStatus(runScan(config), 2,
                    "extra candidate collision policy row");
            requireViolation(config.output,
                    "extra-or-unobserved-candidate-collision-policy");
            requireViolation(config.output,
                    "incomplete-candidate-collision-ledger");
        });

        selfTest(rows, "candidate-collision-lane-spoof-rejected", () -> {
            Fixture fixture = new Fixture();
            Config config = fixture.config();
            writeText(fixture.candidateCollisions,
                    CANDIDATE_COLLISION_HEADER + "\n"
                    + "oracle\tjline/Spoof\t1\tnano-sanitized"
                    + "\tjline/Spoof.class\t0\t"
                    + "0000000000000000000000000000000000000000000000000000000000000000"
                    + "\t2\tcandidate-dependency\tjline/Spoof.class\t0\t"
                    + "0000000000000000000000000000000000000000000000000000000000000000"
                    + "\tBYTE_IDENTICAL\n");
            boolean rejected = false;
            try { runScan(config); }
            catch (Failure expected) { rejected = true; }
            require(rejected, "oracle lane spoof entered candidate policy");
        });

        selfTest(rows, "candidate-collision-effective-release-spoof-rejected",
                () -> {
            Fixture fixture = new Fixture();
            String internal = "jline/console/Versioned";
            String baseEntry = internal + ".class";
            String versionedEntry = "META-INF/versions/11/" + baseEntry;
            byte[] base = fixtureClass(internal, null, false, false);
            byte[] versioned = fixtureClass(internal, "java/lang/Object",
                    false, false);
            byte[] later = fixtureClass(internal, "java/lang/Number", false,
                    false);
            Path firstJar = fixture.root.resolve("versioned-first.jar");
            Path laterJar = fixture.root.resolve("versioned-later.jar");
            writeJar(firstJar, Map.of(
                    "META-INF/MANIFEST.MF",
                    "Manifest-Version: 1.0\r\nMulti-Release: true\r\n\r\n"
                            .getBytes(StandardCharsets.UTF_8),
                    baseEntry, base, versionedEntry, versioned));
            writeJar(laterJar, Map.of(baseEntry, later));
            fixture.addCandidateRuntime("versioned-first", "jar", firstJar);
            fixture.addCandidateRuntime("versioned-later", "jar", laterJar);
            fixture.setCandidateCollisionPolicy(internal, 2, baseEntry, 0,
                    sha256(base), 3, baseEntry, 0, sha256(later));
            Config config = fixture.config();
            requireStatus(runScan(config), 2,
                    "candidate collision effective-release spoof");
            requireViolation(config.output,
                    "candidate-collision-policy-mismatch");
        });

        selfTest(rows, "candidate-runtime-duplicate-release-path-rejected",
                () -> {
            Fixture fixture = new Fixture();
            byte[] archive = zipPayload(Map.of(
                    "jline/A.class", fixtureClass("jline/A", null, false,
                            false),
                    "jline/B.class", fixtureClass("jline/B", null, false,
                            false)));
            archive = renameZipEntry(archive, "jline/B.class",
                    "jline/A.class");
            Path duplicate = fixture.root.resolve("duplicate-path.jar");
            Files.write(duplicate, archive);
            fixture.addCandidateRuntime("duplicate-path", "jar", duplicate);
            Config config = fixture.config();
            requireStatus(runScan(config), 2,
                    "candidate runtime duplicate release/path");
            requireViolation(config.output, "duplicate-archive-entry");
        });

        selfTest(rows, "oracle-transactor-position-zero-first-origin", () -> {
            Fixture fixture = new Fixture();
            Path collision = fixture.root.resolve("oracle-shadow.jar");
            writeJar(collision, Map.of("fixture/core.class",
                    fixtureClass("fixture/core", null, false, false)));
            fixture.addOracleRuntime("oracle-shadow", "jar", collision);
            Config config = fixture.config();
            requireStatus(runScan(config), 0, "oracle root collision");
            requireEvidence(config.output, "collisions.tsv",
                    "oracle\tclass\tfixture/core\t0\t");
            requireEvidence(config.output, "collisions.tsv",
                    "ORDERED_FIRST_ORIGIN_WINS");
        });

        selfTest(rows, "oracle-fallback-order-first-origin", () -> {
            Fixture fixture = new Fixture();
            Path one = fixture.root.resolve("oracle-one.jar");
            Path two = fixture.root.resolve("oracle-two.jar");
            byte[] duplicate = fixtureClass("dep/Ordered", null, false, false);
            writeJar(one, Map.of("dep/Ordered.class", duplicate));
            writeJar(two, Map.of("dep/Ordered.class", duplicate));
            fixture.addOracleRuntime("oracle-one", "jar", one);
            fixture.addOracleRuntime("oracle-two", "jar", two);
            Config config = fixture.config();
            requireStatus(runScan(config), 0, "oracle fallback collision");
            requireEvidence(config.output, "collisions.tsv",
                    "dep/Ordered\t4\toracle-one"
                            + "\truntime:oracle:4:oracle-one");
        });

        selfTest(rows, "mapping-derived-identity-shadow", () -> {
            Fixture fixture = new Fixture();
            Path shadow = fixture.root.resolve("identity-shadow.jar");
            writeJar(shadow, Map.of("fixture/core.class",
                    fixtureClass("fixture/core", null, false, false)));
            fixture.addCandidateRuntime("identity-shadow", "jar", shadow);
            Config config = fixture.config();
            requireStatus(runScan(config), 0, "identity shadow");
            requireEvidence(config.output, "runtime-shadows.tsv",
                    "CHILD_FIRST_OWNED\tCHILD_FIRST_OWNED\tNOT_APPLICABLE");
        });

        selfTest(rows, "mapping-derived-nonidentity-original-alias", () -> {
            Fixture fixture = new Fixture();
            fixture.renameCandidates("fixture/candidate_a",
                    "fixture/candidate_b");
            Path alias = fixture.root.resolve("original-alias.jar");
            writeJar(alias, Map.of("fixture/core.class",
                    fixtureClass("fixture/core", null, false, false)));
            fixture.addCandidateRuntime("original-alias", "jar", alias);
            Config config = fixture.config();
            requireStatus(runScan(config), 0, "nonidentity alias");
            requireEvidence(config.output, "runtime-shadows.tsv",
                    "BLOCKED_ORIGINAL_ALIAS\tBLOCKED_ORIGINAL_ALIAS");
        });

        selfTest(rows, "typed-member-escape-rejected", () -> {
            Fixture fixture = new Fixture();
            byte[] caller = fixtureCaller("fixture/core", "dep/Target",
                    "missing");
            writeCandidate(fixture.candidateA, "fixture/core.class", caller);
            writeCandidate(fixture.candidateB, "fixture/core.class", caller);
            Path target = fixture.root.resolve("target.jar");
            writeJar(target, Map.of("dep/Target.class",
                    fixtureClass("dep/Target", null, false, false)));
            fixture.addCandidateRuntime("target", "jar", target);
            Config config = fixture.config();
            requireStatus(runScan(config), 2, "typed member escape");
            requireViolation(config.output, "member-escape");
        });

        selfTest(rows, "descriptor-reference-escape-rejected", () -> {
            Fixture fixture = new Fixture();
            byte[] escaping = fixtureClass("fixture/core", "missing/Type",
                    false, false);
            writeCandidate(fixture.candidateA, "fixture/core.class", escaping);
            writeCandidate(fixture.candidateB, "fixture/core.class", escaping);
            Config config = fixture.config();
            requireStatus(runScan(config), 2, "descriptor escape");
            requireViolation(config.output, "reference-escape");
        });

        selfTest(rows, "handle-reference-escape-rejected", () -> {
            Fixture fixture = new Fixture();
            byte[] escaping = fixtureClass("fixture/core", null, true, false);
            writeCandidate(fixture.candidateA, "fixture/core.class", escaping);
            writeCandidate(fixture.candidateB, "fixture/core.class", escaping);
            Config config = fixture.config();
            requireStatus(runScan(config), 2, "handle escape");
            requireViolation(config.output, "missing/Bootstrap");
        });

        selfTest(rows, "candidate-runtime-h2-data-zip-inventoried", () -> {
            Fixture fixture = new Fixture();
            byte[] dataZip = zipPayload(Map.of(
                    "org/h2/util/messages.properties",
                    "message=value\n".getBytes(StandardCharsets.UTF_8),
                    "nested/Retained.class",
                    fixtureClass("nested/Retained", null, false, false)));
            Path h2 = fixture.root.resolve("h2-retained.jar");
            writeJar(h2, Map.of("org/h2/util/data.zip", dataZip));
            fixture.addCandidateRuntime("h2-retained", "jar", h2);
            Config config = fixture.config();
            requireStatus(runScan(config), 0,
                    "candidate runtime H2 data.zip");
            requireEvidence(config.output, "entries.tsv",
                    "runtime-candidate");
            requireEvidence(config.output, "entries.tsv",
                    "org/h2/util/data.zip!/org/h2/util/messages.properties");
            requireEvidence(config.output, "entries.tsv",
                    "org/h2/util/data.zip!/nested/Retained.class");
        });

        selfTest(rows, "oracle-runtime-nested-archive-inventoried", () -> {
            Fixture fixture = new Fixture();
            byte[] dataZip = zipPayload(Map.of("oracle/data.txt",
                    "oracle nested data\n".getBytes(StandardCharsets.UTF_8)));
            Path dependency = fixture.root.resolve("oracle-nested.jar");
            writeJar(dependency, Map.of("oracle/data.zip", dataZip));
            fixture.addOracleRuntime("oracle-nested", "jar", dependency);
            Config config = fixture.config();
            requireStatus(runScan(config), 0, "oracle runtime nested ZIP");
            requireEvidence(config.output, "entries.tsv",
                    "runtime-oracle");
            requireEvidence(config.output, "entries.tsv",
                    "oracle/data.zip!/oracle/data.txt");
        });

        selfTest(rows, "candidate-output-nested-archive-rejected", () -> {
            Fixture fixture = new Fixture();
            byte[] nested = zipPayload("inside.txt", "inside".getBytes(
                    StandardCharsets.UTF_8));
            String relative = "fixture.core/fixture/generated-data.bin";
            Path candidateAResource = fixture.candidateA.resolve(relative);
            Path candidateBResource = fixture.candidateB.resolve(relative);
            Files.createDirectories(candidateAResource.getParent());
            Files.createDirectories(candidateBResource.getParent());
            Files.write(candidateAResource, nested);
            Files.write(candidateBResource, nested);
            fixture.allowedRows.add("both\t" + relative
                    + "\tfixture.core.generated\t" + sha256(nested));
            Config config = fixture.config();
            requireStatus(runScan(config), 2,
                    "generated candidate output nested archive");
            requireViolation(config.output, "raw-nested-archive");
            requireEvidence(config.output, "entries.tsv", "inside.txt");
        });

        selfTest(rows, "candidate-runtime-nested-forbidden-jks-rejected",
                () -> {
            Fixture fixture = new Fixture();
            byte[] nested = zipPayload("safe-resource.bin",
                    fixture.originalNanoJks);
            Path dependency = fixture.root.resolve("nested-jks.jar");
            writeJar(dependency, Map.of("retained/data.zip", nested));
            fixture.addCandidateRuntime("nested-jks", "jar", dependency);
            Config config = fixture.config();
            requireStatus(runScan(config), 2,
                    "candidate runtime nested forbidden JKS");
            requireViolation(config.output, "nano-original-jks-content");
            requireViolation(config.output, "raw-keystore-resource");
            requireNoViolation(config.output, "raw-nested-archive");
            requireEvidence(config.output, "entries.tsv",
                    "retained/data.zip!/safe-resource.bin");
        });

        selfTest(rows, "runtime-archive-named-non-zip-rejected", () -> {
            for (String lane : List.of("candidate", "oracle")) {
                Fixture fixture = new Fixture();
                Path dependency = fixture.root.resolve(lane
                        + "-archive-name.jar");
                writeJar(dependency, Map.of("retained/data.zip",
                        "not a ZIP\n".getBytes(StandardCharsets.UTF_8)));
                if (lane.equals("candidate")) {
                    fixture.addCandidateRuntime("archive-name", "jar",
                            dependency);
                } else {
                    fixture.addOracleRuntime("archive-name", "jar",
                            dependency);
                }
                Config config = fixture.config();
                requireStatus(runScan(config), 2,
                        lane + " archive-named non-ZIP");
                requireViolation(config.output, "archive-named-non-zip");
            }
        });

        selfTest(rows, "runtime-nested-depth-overflow-rejected", () -> {
            Fixture fixture = new Fixture();
            byte[] nested = zipPayload("leaf.txt", "leaf\n".getBytes(
                    StandardCharsets.UTF_8));
            for (int depth = 0; depth < MAX_NESTED_DEPTH; depth++) {
                nested = zipPayload("level-" + depth + ".zip", nested);
            }
            Path dependency = fixture.root.resolve("too-deep.jar");
            writeJar(dependency, Map.of("retained/data.zip", nested));
            fixture.addCandidateRuntime("too-deep", "jar", dependency);
            Config config = fixture.config();
            requireStatus(runScan(config), 2, "nested ZIP depth overflow");
            requireViolation(config.output, "nested-archive-depth");
        });

        selfTest(rows, "runtime-nested-duplicate-entry-rejected", () -> {
            Fixture fixture = new Fixture();
            byte[] nested = zipPayload(Map.of(
                    "same-a.txt", "first\n".getBytes(StandardCharsets.UTF_8),
                    "same-b.txt", "second\n".getBytes(StandardCharsets.UTF_8)));
            nested = renameZipEntry(nested, "same-b.txt", "same-a.txt");
            Path dependency = fixture.root.resolve("duplicate-nested.jar");
            writeJar(dependency, Map.of("retained/data.zip", nested));
            fixture.addCandidateRuntime("duplicate-nested", "jar",
                    dependency);
            Config config = fixture.config();
            requireStatus(runScan(config), 2, "nested duplicate entry");
            requireViolation(config.output, "duplicate-archive-entry");
        });

        selfTest(rows, "runtime-nested-unsafe-path-rejected", () -> {
            Fixture fixture = new Fixture();
            byte[] nested = zipPayload("../escape.txt", "escape\n".getBytes(
                    StandardCharsets.UTF_8));
            Path dependency = fixture.root.resolve("unsafe-nested.jar");
            writeJar(dependency, Map.of("retained/data.zip", nested));
            fixture.addCandidateRuntime("unsafe-nested", "jar", dependency);
            Config config = fixture.config();
            requireStatus(runScan(config), 2, "nested unsafe path");
            requireViolation(config.output,
                    "unsafe nested archive entry name");
        });

        selfTest(rows, "runtime-nested-truncated-zip-rejected", () -> {
            Fixture fixture = new Fixture();
            byte[] complete = zipPayload("inside.txt", "inside\n".getBytes(
                    StandardCharsets.UTF_8));
            byte[] truncated = Arrays.copyOf(complete, complete.length - 5);
            Path dependency = fixture.root.resolve("truncated-nested.jar");
            writeJar(dependency, Map.of("retained/data.zip", truncated));
            fixture.addCandidateRuntime("truncated-nested", "jar",
                    dependency);
            Config config = fixture.config();
            requireStatus(runScan(config), 2, "truncated nested ZIP");
            requireViolation(config.output, "archive-envelope");
            requireViolation(config.output, "ZIP end record is missing");
        });

        selfTest(rows, "runtime-nested-trailing-bytes-rejected", () -> {
            Fixture fixture = new Fixture();
            byte[] complete = zipPayload("inside.txt", "inside\n".getBytes(
                    StandardCharsets.UTF_8));
            byte[] trailing = Arrays.copyOf(complete, complete.length + 4);
            Arrays.fill(trailing, complete.length, trailing.length,
                    (byte) 0x7f);
            Path dependency = fixture.root.resolve("trailing-nested.jar");
            writeJar(dependency, Map.of("retained/data.zip", trailing));
            fixture.addCandidateRuntime("trailing-nested", "jar",
                    dependency);
            Config config = fixture.config();
            requireStatus(runScan(config), 2, "nested ZIP trailing bytes");
            requireViolation(config.output, "archive-envelope");
            requireViolation(config.output, "trailing bytes");
        });

        selfTest(rows, "runtime-nested-envelope-count-rejected", () -> {
            Fixture fixture = new Fixture();
            byte[] nested = corruptZipEntryCount(zipPayload("inside.txt",
                    "inside\n".getBytes(StandardCharsets.UTF_8)));
            Path dependency = fixture.root.resolve("bad-envelope-nested.jar");
            writeJar(dependency, Map.of("retained/data.zip", nested));
            fixture.addCandidateRuntime("bad-envelope-nested", "jar",
                    dependency);
            Config config = fixture.config();
            requireStatus(runScan(config), 2, "nested ZIP envelope count");
            requireViolation(config.output,
                    "central-directory count differs from streamed entries");
        });

        selfTest(rows, "runtime-nested-malformed-class-rejected", () -> {
            Fixture fixture = new Fixture();
            byte[] nested = zipPayload("nested/Bad.class",
                    new byte[] {0, 1, 2, 3});
            Path dependency = fixture.root.resolve("bad-class-nested.jar");
            writeJar(dependency, Map.of("retained/data.zip", nested));
            fixture.addCandidateRuntime("bad-class-nested", "jar",
                    dependency);
            Config config = fixture.config();
            requireStatus(runScan(config), 2, "nested malformed class");
            requireViolation(config.output, "scan-failure");
            requireEvidence(config.output, "entries.tsv",
                    "retained/data.zip!/nested/Bad.class");
        });

        selfTest(rows, "symbolic-link-rejected", () -> {
            Fixture fixture = new Fixture();
            Path target = fixture.root.resolve("link-target.bin");
            Files.write(target, new byte[] {1, 2, 3});
            Files.createSymbolicLink(fixture.candidateDependency.resolve(
                    "link.bin"), target);
            Config config = fixture.config();
            requireStatus(runScan(config), 2, "symbolic link");
            requireViolation(config.output, "symbolic-link");
        });

        selfTest(rows, "special-file-rejected", () -> {
            Fixture fixture = new Fixture();
            Path fifo = fixture.candidateDependency.resolve("special.fifo");
            Process process = new ProcessBuilder("/usr/bin/mkfifo",
                    fifo.toString()).redirectErrorStream(true).start();
            require(process.waitFor() == 0, "mkfifo fixture failed");
            Config config = fixture.config();
            requireStatus(runScan(config), 2, "special file");
            requireViolation(config.output, "special-file");
        });

        selfTest(rows, "forbidden-keystore-resource-rejected", () -> {
            Fixture fixture = new Fixture();
            byte[] keystore = new byte[] {(byte) 0xfe, (byte) 0xed,
                    (byte) 0xfe, (byte) 0xed, 0, 0, 0, 2};
            String relative = "fixture.core/fixture/renamed-resource.jks";
            Files.createDirectories(fixture.candidateA.resolve(
                    "fixture.core/fixture"));
            Files.createDirectories(fixture.candidateB.resolve(
                    "fixture.core/fixture"));
            Files.write(fixture.candidateA.resolve(relative), keystore);
            Files.write(fixture.candidateB.resolve(relative), keystore);
            fixture.allowedRows.add("both\t" + relative
                    + "\tfixture.core.generated\t" + sha256(keystore));
            Config config = fixture.config();
            requireStatus(runScan(config), 2, "keystore resource");
            requireViolation(config.output, "raw-keystore-resource");
        });

        selfTest(rows, "unknown-class-attribute-rejected", () -> {
            Fixture fixture = new Fixture();
            byte[] unknown = fixtureClass("fixture/core", null, false, true);
            writeCandidate(fixture.candidateA, "fixture/core.class", unknown);
            writeCandidate(fixture.candidateB, "fixture/core.class", unknown);
            Config config = fixture.config();
            requireStatus(runScan(config), 2, "unknown attribute");
            requireViolation(config.output, "unknown-attribute");
        });

        selfTest(rows, "first-runtime-error-does-not-suppress-later-ledger", () -> {
            Fixture fixture = new Fixture();
            Path malformed = fixture.root.resolve("malformed.jar");
            Files.write(malformed, new byte[] {1, 2, 3, 4});
            fixture.candidateRuntime.add(0, new RuntimeFixture(
                    "malformed-first", "jar", malformed));
            Config config = fixture.config();
            requireStatus(runScan(config), 2, "malformed first element");
            requireEvidence(config.output, "runtime-elements.tsv",
                    "nano-sanitized\tjar");
            requireEvidence(config.output, "runtime-elements.tsv",
                    "malformed-first\tjar");
        });

        selfTest(rows, "peer-inventory-reference-escape-is-inactive", () -> {
            Fixture fixture = new Fixture();
            writeJar(fixture.peerJar, Map.of("peer/Inventory.class",
                    fixtureClass("peer/Inventory", "missing/PeerType", false,
                            false)));
            Config config = fixture.config();
            requireStatus(runScan(config), 0, "Peer inventory inactivity");
            requireEvidence(config.output, "references.tsv",
                    "inactive-entry-inventoried");
        });

        selfTest(rows, "invalid-mapping-refuses-before-output", () -> {
            Fixture fixture = new Fixture();
            Path prospective = fixture.root.resolve("mapping-output");
            Config config = fixture.config(prospective);
            writeText(fixture.mappings,
                    "comparison\tnamespace\tleft_class\tright_class\tidentity\n"
                    + "candidate-a--original\tfixture.core\tfixture/core"
                    + "\tfixture/core\ttrue\n");
            boolean rejected = false;
            try { runScan(config); }
            catch (Failure expected) { rejected = true; }
            require(rejected, "invalid mapping was accepted");
            require(!Files.exists(prospective),
                    "preflight failure created output");
        });

        selfTest(rows, "candidate-nested-output-preflight-no-mutation", () -> {
            Fixture fixture = new Fixture();
            Path nested = fixture.candidateA.resolve("would-be-output");
            Config config = fixture.config(nested);
            String before = directoryManifest(fixture.candidateA);
            boolean rejected = false;
            try { runScan(config); }
            catch (Failure expected) { rejected = true; }
            require(rejected, "candidate-nested output was accepted");
            require(before.equals(directoryManifest(fixture.candidateA))
                            && !Files.exists(nested),
                    "candidate input mutated before overlap rejection");
        });

        selfTest(rows, "runtime-nested-output-preflight-no-mutation", () -> {
            Fixture fixture = new Fixture();
            Path nested = fixture.candidateDependency.resolve(
                    "would-be-output");
            Config config = fixture.config(nested);
            String before = directoryManifest(fixture.candidateDependency);
            boolean rejected = false;
            try { runScan(config); }
            catch (Failure expected) { rejected = true; }
            require(rejected, "runtime-nested output was accepted");
            require(before.equals(directoryManifest(
                                    fixture.candidateDependency))
                            && !Files.exists(nested),
                    "runtime input mutated before overlap rejection");
        });

        selfTest(rows, "nonempty-output-refused-without-mutation", () -> {
            Fixture fixture = new Fixture();
            Path occupied = Files.createDirectories(
                    fixture.root.resolve("occupied-output"));
            Path marker = occupied.resolve("marker.txt");
            Files.writeString(marker, "preserve\n", StandardCharsets.UTF_8);
            Config config = fixture.config(occupied);
            boolean rejected = false;
            try { runScan(config); }
            catch (Failure expected) { rejected = true; }
            require(rejected, "nonempty output was accepted");
            require(Files.readString(marker, StandardCharsets.UTF_8)
                            .equals("preserve\n"),
                    "nonempty output marker was changed");
            try (Stream<Path> contents = Files.list(occupied)) {
                require(contents.count() == 1,
                        "nonempty output gained scanner files");
            }
        });

        selfTest(rows, "incomplete-ledger-rejected", () -> {
            ScanState state = new ScanState();
            state.setupComplete = true;
            state.discoveredEntries = 2;
            EntryRecord one = new EntryRecord("fixture", 0, "fixture",
                    "fixture", "resource", "one", 1);
            state.record(one);
            require(!ledgerComplete(state),
                    "incomplete synthetic ledger was accepted");
        });

        int passed = 0;
        for (int index = 1; index < rows.size(); index++) {
            if (rows.get(index).endsWith("\tPASS")) passed++;
        }
        writeLines(output.resolve("self-test.tsv"), rows);
        List<String> summary = List.of(
                "metric\tvalue",
                "tests\t" + (rows.size() - 1),
                "passed\t" + passed,
                "failed\t" + (rows.size() - 1 - passed),
                "status\t" + (passed == rows.size() - 1 ? "PASS" : "FAIL"),
                "production.scanner.run\tNOT_RUN",
                "jvm.verifier.run\tNOT_RUN",
                "wrapper.integration.run\tNOT_RUN",
                "stage.1.pass.claimed\tfalse");
        writeLines(output.resolve("summary.tsv"), summary);
        writeSelfExcludingManifest(output,
                List.of("self-test.tsv", "summary.tsv"));
        return passed == rows.size() - 1 ? 0 : 2;
    }

    private static void usage() {
        System.err.println("usage:");
        System.err.println("  ScanExactAotBoundary --scan CANDIDATE_A "
                + "CANDIDATE_B TRANSactor_ORIGINAL_JAR PEER_INVENTORY_JAR "
                + "COHORT_TSV OWNERSHIP_TSV CLASS_MAPPINGS_TSV "
                + "RUNTIME_POLICY_TSV CANDIDATE_RUNTIME_TSV "
                + "ORACLE_RUNTIME_TSV CANDIDATE_COLLISIONS_TSV "
                + "ALLOWED_RESOURCES_TSV "
                + "FORBIDDEN_PAYLOADS_TSV OUTPUT_DIR");
        System.err.println("  ScanExactAotBoundary --self-test OUTPUT_DIR");
    }

    public static void main(String[] args) throws Exception {
        if (args.length == 2 && args[0].equals("--self-test")) {
            int status = runSelfTests(Path.of(args[1]));
            if (status != 0) System.exit(status);
            return;
        }
        if (args.length == 15 && args[0].equals("--scan")) {
            Config config = new Config(Path.of(args[1]), Path.of(args[2]),
                    Path.of(args[3]), Path.of(args[4]), Path.of(args[5]),
                    Path.of(args[6]), Path.of(args[7]), Path.of(args[8]),
                    Path.of(args[9]), Path.of(args[10]), Path.of(args[11]),
                    Path.of(args[12]), Path.of(args[13]), Path.of(args[14]));
            int status = runScan(config);
            System.out.println("status=" + (status == 0 ? "PASS" : "FAIL"));
            System.out.println("evidence="
                    + config.output.resolve("manifest.sha256"));
            if (status != 0) System.exit(status);
            return;
        }
        usage();
        System.exit(64);
    }
}
