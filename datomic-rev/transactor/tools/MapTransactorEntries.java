import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Deterministically accounts for every Transactor class and resource entry.
 *
 * This program never loads a class from the licensed artifact. It reads compact
 * checked-in inventories, streams ZIP entries for byte/hash verification, and
 * extracts original resources only beneath licensed-evidence/ in the requested
 * /tmp output. No original classfile is extracted.
 */
public final class MapTransactorEntries {
    private static final String EXPECTED_JAR_SHA =
            "d90819b57e2138085ddc26c93314f953c2cd776d1d1aa8b43a6bec1e475d3692";
    private static final String EXPECTED_PEER_SHA =
            "cb55c9d01e155f9965e3332cd8ef70be42cdefacb50c072076e9ecf22b9a23ba";

    record ClassRow(String entry, String internalName, String sourceFile,
                    String sourceKind, int major, long bytes, String sha256) {}
    record NsOwner(String namespace, String entry, String scope, String sha256,
                   String prefix, String expectedSourceStem) {}
    record JavaOwner(String sourcePath, String relationship, String outerName) {}
    record ResourceRow(String entry, long size, long compressedSize, String crc32,
                       String method, String sha256) {}
    record Candidate(NsOwner owner, String matchKind, boolean sourceCompatible) {}
    record ResourcePolicy(String category, String phase, String evidenceDisposition,
                          String candidateAction, String candidateSource) {}

    public static void main(String[] args) throws Exception {
        if (args.length != 4) {
            throw new IllegalArgumentException(
                    "usage: MapTransactorEntries BASELINE_DIR TRANSACTOR_JAR PEER_JAR OUTPUT_DIR");
        }
        Path baseline = Path.of(args[0]).toAbsolutePath().normalize();
        Path jar = Path.of(args[1]).toAbsolutePath().normalize();
        Path peer = Path.of(args[2]).toAbsolutePath().normalize();
        Path out = Path.of(args[3]).toAbsolutePath().normalize();
        requireEmptyOutput(out);
        Files.createDirectories(out);

        check(EXPECTED_JAR_SHA.equals(sha256(jar)), "unexpected Transactor JAR hash");
        check(EXPECTED_PEER_SHA.equals(sha256(peer)), "unexpected Peer JAR hash");

        List<ClassRow> classes = readClasses(baseline.resolve("transactor-classes.tsv"));
        List<NsOwner> namespaces = readNamespaces(
                baseline.resolve("transactor-namespace-inits.tsv"));
        List<JavaOwner> javaOwners = readJavaOwners(
                baseline.resolve("java-source-candidates.tsv"));
        List<ResourceRow> resources = readResources(
                baseline.resolve("transactor-resources.tsv"));

        check(classes.size() == 9_682, "expected 9,682 classes");
        check(namespaces.size() == 247, "expected 247 namespace initializers");
        check(javaOwners.size() == 46, "expected 46 Java source candidates");
        check(resources.size() == 11, "expected 11 resources");

        Map<String, ClassRow> classesByEntry = uniqueMap(classes, ClassRow::entry, "class entry");
        Map<String, JavaOwner> javaBySource = uniqueMap(
                javaOwners, JavaOwner::sourcePath, "Java source path");
        Map<String, ResourceRow> resourcesByEntry = uniqueMap(
                resources, ResourceRow::entry, "resource entry");

        List<String> ownership = new ArrayList<>();
        ownership.add("entry\tsource_kind\trole\towner_kind\towner_id\towner_scope"
                + "\tresolution\tstructural_candidate_count\tsource_compatible_candidate_count"
                + "\tstructural_candidates\tsource_file\tbytes\tclass_sha256");
        List<String> orphans = new ArrayList<>();
        orphans.add("entry\tsource_kind\treason\tinternal_name\tsource_file\tclass_sha256");
        List<String> ambiguities = new ArrayList<>();
        ambiguities.add("entry\tstatus\tselected_namespace\treason\tcandidates"
                + "\tinternal_name\tsource_file\tclass_sha256");
        List<String> unresolved = new ArrayList<>();
        unresolved.add("entry\tsource_kind\treason\tcandidates\tinternal_name"
                + "\tsource_file\tclass_sha256");
        List<String> resolvedOverlaps = new ArrayList<>();
        resolvedOverlaps.add("entry\tselected_namespace\tresolution\tstructural_candidates"
                + "\tsource_compatible_candidates\tsource_file\tclass_sha256");

        TreeMap<String, Integer> ownerCounts = new TreeMap<>();
        TreeMap<String, Integer> roleCounts = new TreeMap<>();
        int clojureClasses = 0;
        int javaClasses = 0;
        int structurallyOverlapping = 0;
        int overlapResolvedByUniqueSource = 0;
        int conventionOnlyAmbiguities = 0;
        int sourceCompatibleOverlapping = 0;
        int noSourceFileSupport = 0;

        for (ClassRow c : classes) {
            String role;
            String ownerKind;
            String ownerId;
            String ownerScope;
            String resolution;
            int candidateCount;
            int sourceCandidateCount;
            String renderedCandidates;

            if (c.sourceKind().equals("clojure-aot")) {
                clojureClasses++;
                List<Candidate> candidates = namespaceCandidates(c, namespaces);
                List<Candidate> sourceCandidates = candidates.stream()
                        .filter(Candidate::sourceCompatible).toList();
                candidateCount = candidates.size();
                sourceCandidateCount = sourceCandidates.size();
                renderedCandidates = renderCandidates(candidates);

                if (candidates.isEmpty()) {
                    orphans.add(tsv(c.entry(), c.sourceKind(), "no initializer-prefix candidate",
                            c.internalName(), c.sourceFile(), c.sha256()));
                    continue;
                }

                Candidate selected = selectNamespaceCandidate(c, candidates, sourceCandidates);
                if (selected == null) {
                    unresolved.add(tsv(c.entry(), c.sourceKind(),
                            "multiple equally strong initializer candidates", renderedCandidates,
                            c.internalName(), c.sourceFile(), c.sha256()));
                    continue;
                }

                role = selected.matchKind().equals("initializer-exact")
                        ? "namespace-init"
                        : (c.internalName().contains("$")
                           ? "aot-function-or-generated" : "aot-type-or-interface");
                ownerKind = "clojure-namespace";
                ownerId = selected.owner().namespace();
                ownerScope = selected.owner().scope();
                resolution = resolution(candidates, sourceCandidates, selected);
                if (!selected.sourceCompatible()) noSourceFileSupport++;
                if (candidateCount > 1) {
                    structurallyOverlapping++;
                    if (sourceCandidateCount == 1) overlapResolvedByUniqueSource++;
                    else if (sourceCandidateCount > 1) sourceCompatibleOverlapping++;
                    else {
                        conventionOnlyAmbiguities++;
                        ambiguities.add(tsv(c.entry(), "resolved-conventionally", ownerId,
                                "no SourceFile evidence; selected longest initializer prefix",
                                renderedCandidates, c.internalName(),
                                c.sourceFile().isEmpty() ? "<empty>" : c.sourceFile(), c.sha256()));
                    }
                    resolvedOverlaps.add(tsv(c.entry(), ownerId, resolution, renderedCandidates,
                            renderCandidates(sourceCandidates), c.sourceFile(), c.sha256()));
                }
            } else if (c.sourceKind().equals("java")) {
                javaClasses++;
                String sourcePath = javaSourcePath(c);
                JavaOwner selected = javaBySource.get(sourcePath);
                if (selected == null) {
                    orphans.add(tsv(c.entry(), c.sourceKind(), "no Java source candidate",
                            c.internalName(), c.sourceFile(), c.sha256()));
                    continue;
                }
                role = c.internalName().contains("$") ? "java-nested" : "java-top-level";
                ownerKind = "java-source";
                ownerId = selected.sourcePath();
                ownerScope = selected.relationship();
                resolution = "package+SourceFile-exact";
                candidateCount = 1;
                sourceCandidateCount = 1;
                renderedCandidates = selected.sourcePath();
            } else {
                orphans.add(tsv(c.entry(), c.sourceKind(), "unknown source kind",
                        c.internalName(), c.sourceFile(), c.sha256()));
                continue;
            }

            ownership.add(tsv(c.entry(), c.sourceKind(), role, ownerKind, ownerId, ownerScope,
                    resolution, candidateCount, sourceCandidateCount, renderedCandidates,
                    c.sourceFile(), c.bytes(), c.sha256()));
            ownerCounts.merge(ownerKind + ":" + ownerId, 1, Integer::sum);
            roleCounts.merge(role, 1, Integer::sum);
        }

        verifyZip(jar, classesByEntry, resourcesByEntry);
        ResourceAccounting resourceAccounting = recoverAndClassifyResources(
                jar, peer, resources, out);

        writeLines(out.resolve("class-ownership.tsv"), ownership);
        writeLines(out.resolve("orphan-classes.tsv"), orphans);
        writeLines(out.resolve("ambiguous-classes.tsv"), ambiguities);
        writeLines(out.resolve("unresolved-classes.tsv"), unresolved);
        writeLines(out.resolve("resolved-prefix-overlaps.tsv"), resolvedOverlaps);

        List<String> ownerSummary = new ArrayList<>();
        ownerSummary.add("owner_kind\towner_id\tclass_count");
        ownerCounts.forEach((key, count) -> {
            int colon = key.indexOf(':');
            ownerSummary.add(tsv(key.substring(0, colon), key.substring(colon + 1), count));
        });
        writeLines(out.resolve("owner-class-counts.tsv"), ownerSummary);

        List<String> summary = new ArrayList<>();
        summary.add("metric\tvalue");
        metric(summary, "input.transactor.sha256", EXPECTED_JAR_SHA);
        metric(summary, "input.peer.sha256", EXPECTED_PEER_SHA);
        metric(summary, "classes.total", classes.size());
        metric(summary, "classes.accounted", ownership.size() - 1);
        metric(summary, "classes.clojure_aot", clojureClasses);
        metric(summary, "classes.java", javaClasses);
        metric(summary, "classes.orphan", orphans.size() - 1);
        metric(summary, "classes.resolved_convention_only_ambiguous", conventionOnlyAmbiguities);
        metric(summary, "classes.unresolved_ambiguous", unresolved.size() - 1);
        metric(summary, "classes.resolved_structural_prefix_overlap", structurallyOverlapping);
        metric(summary, "classes.prefix_overlap_resolved_by_unique_SourceFile",
                overlapResolvedByUniqueSource);
        metric(summary, "classes.resolved_source_compatible_overlap", sourceCompatibleOverlapping);
        metric(summary, "classes.selected_without_SourceFile_support", noSourceFileSupport);
        metric(summary, "owners.clojure_namespaces", namespaces.size());
        metric(summary, "owners.java_sources", javaOwners.size());
        metric(summary, "roles.namespace_init", roleCounts.getOrDefault("namespace-init", 0));
        metric(summary, "roles.aot_function_or_generated",
                roleCounts.getOrDefault("aot-function-or-generated", 0));
        metric(summary, "roles.aot_type_or_interface",
                roleCounts.getOrDefault("aot-type-or-interface", 0));
        metric(summary, "roles.java_top_level", roleCounts.getOrDefault("java-top-level", 0));
        metric(summary, "roles.java_nested", roleCounts.getOrDefault("java-nested", 0));
        metric(summary, "resources.total", resources.size());
        metric(summary, "resources.shared_byte_identical_with_peer", resourceAccounting.sharedWithPeer());
        metric(summary, "resources.transactor_only", resources.size() - resourceAccounting.sharedWithPeer());
        metric(summary, "original_classfiles_extracted", 0);
        metric(summary, "original_resources_extracted_to_licensed_evidence", resources.size());
        writeLines(out.resolve("summary.tsv"), summary);

        check(ownership.size() - 1 == 9_682, "not every class was accounted");
        check(orphans.size() == 1, "orphan classes remain");
        check(unresolved.size() == 1, "unresolved ambiguous classes remain");
        check(ownerCounts.size() == 293, "expected all 247 + 46 owners to have classes");
        check(roleCounts.getOrDefault("namespace-init", 0) == 247,
                "namespace initializer role mismatch");
        check(roleCounts.getOrDefault("java-top-level", 0) == 46,
                "Java top-level role mismatch");

        writeManifest(out);
        System.out.println("entry closure accounting passed: " + out);
    }

    private static List<Candidate> namespaceCandidates(ClassRow c, List<NsOwner> owners) {
        List<Candidate> result = new ArrayList<>();
        for (NsOwner o : owners) {
            String kind = null;
            if (c.internalName().equals(o.prefix() + "__init")) kind = "initializer-exact";
            else if (c.internalName().startsWith(o.prefix() + "$")) kind = "function-prefix";
            else if (c.internalName().startsWith(o.prefix() + "/")) kind = "type-prefix";
            if (kind != null) {
                boolean sourceCompatible = kind.equals("initializer-exact")
                        || c.sourceFile().equals(o.expectedSourceStem() + ".clj")
                        || c.sourceFile().equals(o.expectedSourceStem() + ".cljc");
                result.add(new Candidate(o, kind, sourceCompatible));
            }
        }
        result.sort(Comparator.comparingInt((Candidate x) -> x.owner().prefix().length())
                .reversed().thenComparing(x -> x.owner().namespace()));
        return result;
    }

    private static Candidate selectNamespaceCandidate(ClassRow c, List<Candidate> candidates,
                                                       List<Candidate> sourceCandidates) {
        if (c.internalName().endsWith("__init")) {
            List<Candidate> exact = candidates.stream()
                    .filter(x -> x.matchKind().equals("initializer-exact")).toList();
            return exact.size() == 1 ? exact.get(0) : null;
        }
        List<Candidate> pool = sourceCandidates.isEmpty() ? candidates : sourceCandidates;
        int bestLength = pool.get(0).owner().prefix().length();
        List<Candidate> best = pool.stream()
                .filter(x -> x.owner().prefix().length() == bestLength).toList();
        return best.size() == 1 ? best.get(0) : null;
    }

    private static String resolution(List<Candidate> candidates, List<Candidate> sourceCandidates,
                                     Candidate selected) {
        if (selected.matchKind().equals("initializer-exact")) return "initializer-exact";
        if (candidates.size() == 1 && selected.sourceCompatible()) {
            return selected.matchKind() + "+SourceFile-exact";
        }
        if (sourceCandidates.size() == 1) return "SourceFile-unique-among-prefixes";
        if (sourceCandidates.size() > 1) return "longest-prefix-among-SourceFile-matches";
        if (candidates.size() > 1) return "longest-prefix-convention-no-SourceFile";
        return selected.matchKind() + "-unique-no-SourceFile";
    }

    private static String renderCandidates(List<Candidate> candidates) {
        List<String> values = new ArrayList<>();
        for (Candidate c : candidates) {
            values.add(c.owner().namespace() + "[" + c.matchKind()
                    + (c.sourceCompatible() ? ",source-match" : ",source-mismatch") + "]");
        }
        return String.join(";", values);
    }

    private static String javaSourcePath(ClassRow c) {
        int slash = c.internalName().lastIndexOf('/');
        return (slash < 0 ? "" : c.internalName().substring(0, slash + 1)) + c.sourceFile();
    }

    record ResourceAccounting(int sharedWithPeer) {}

    private static ResourceAccounting recoverAndClassifyResources(
            Path jar, Path peer, List<ResourceRow> resources, Path out) throws Exception {
        Path evidenceRoot = out.resolve("licensed-evidence/resources");
        Files.createDirectories(evidenceRoot);
        Map<String, ResourcePolicy> policies = resourcePolicies();
        List<String> classification = new ArrayList<>();
        classification.add("entry\tcategory\tphase\tshared_byte_identical_with_peer"
                + "\tevidence_disposition\tcandidate_action\tcandidate_source\tbytes\tsha256");
        List<String> evidenceManifest = new ArrayList<>();
        evidenceManifest.add("entry\tbytes\tsha256");
        int shared = 0;

        try (ZipFile transactorZip = new ZipFile(jar.toFile());
             ZipFile peerZip = new ZipFile(peer.toFile())) {
            for (ResourceRow r : resources) {
                ZipEntry entry = transactorZip.getEntry(r.entry());
                check(entry != null, "missing resource in Transactor ZIP: " + r.entry());
                Path target = evidenceRoot.resolve(r.entry()).normalize();
                check(target.startsWith(evidenceRoot), "unsafe resource path: " + r.entry());
                Files.createDirectories(target.getParent());
                try (InputStream in = transactorZip.getInputStream(entry)) {
                    Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
                }
                check(Files.size(target) == r.size(), "resource size mismatch: " + r.entry());
                check(sha256(target).equals(r.sha256()), "resource hash mismatch: " + r.entry());
                if (r.entry().endsWith(".jks")) {
                    target.toFile().setReadable(false, false);
                    target.toFile().setWritable(false, false);
                    target.toFile().setExecutable(false, false);
                    target.toFile().setReadable(true, true);
                    target.toFile().setWritable(true, true);
                }

                ZipEntry peerEntry = peerZip.getEntry(r.entry());
                boolean samePeer = peerEntry != null
                        && sha256(peerZip.getInputStream(peerEntry)).equals(r.sha256());
                if (samePeer) shared++;
                ResourcePolicy p = policies.get(r.entry());
                check(p != null, "resource lacks policy: " + r.entry());
                classification.add(tsv(r.entry(), p.category(), p.phase(), samePeer,
                        p.evidenceDisposition(), p.candidateAction(), p.candidateSource(),
                        r.size(), r.sha256()));
                evidenceManifest.add(tsv(r.entry(), r.size(), r.sha256()));
            }
        }
        writeLines(out.resolve("resource-classification.tsv"), classification);
        writeLines(out.resolve("licensed-evidence/resources-manifest.tsv"), evidenceManifest);
        return new ResourceAccounting(shared);
    }

    private static Map<String, ResourcePolicy> resourcePolicies() {
        Map<String, ResourcePolicy> m = new LinkedHashMap<>();
        m.put("META-INF/MANIFEST.MF", policy("build-metadata", "core-packaging",
                "licensed-evidence-only", "regenerate",
                "candidate build tool; do not copy original Created-By metadata"));
        m.put("META-INF/maven/com.datomic/datomic-transactor-pro/pom.xml",
                policy("dependency-and-provenance-metadata", "core-build",
                        "licensed-evidence-only", "reconstruct",
                        "verified dependency inventory; generate candidate coordinates/POM"));
        m.put("copyright", policy("legal-notice", "core-packaging",
                "licensed-evidence-only-pending-license-review", "author-candidate-notice",
                "candidate copyright plus required third-party notices"));
        m.put("css/bootstrap.min.css", policy("third-party-ui-asset-bootstrap-2.1.1",
                "optional-interface", "licensed-evidence-only", "source-from-upstream",
                "official Bootstrap 2.1.1 source/release with its Apache-2.0 notice"));
        m.put("data_readers.clj", policy("clojure-runtime-registration", "core-runtime",
                "semantic-reference-shared-with-peer", "reconstruct-and-test",
                "candidate source registering db/id, db/fn, and base64 readers"));
        m.put("datomic/VERSION", policy("version-metadata", "core-packaging",
                "semantic-reference-shared-with-peer", "generate",
                "candidate build version; retain 1.0.7277 only as compatibility target"));
        m.put("datomic/aws/instance-arch.edn", policy("aws-deployment-metadata",
                "alternative-backend-later", "licensed-evidence-only-shared-with-peer",
                "reconstruct-or-replace", "tested current AWS instance architecture discovery"));
        m.put("datomic/aws/region-arch-ami.edn", policy("aws-ami-catalog",
                "alternative-backend-later", "licensed-evidence-only-shared-with-peer",
                "replace-with-current-discovery", "AWS API/config; never ship stale AMI IDs blindly"));
        m.put("datomic/transactor-key.jks", policy("tls-private-key-keystore",
                "transport-security", "secret-bearing-licensed-evidence-never-runtime",
                "generate-per-environment", "new candidate-owned keypair/certificate"));
        m.put("datomic/transactor-trust.jks", policy("tls-truststore",
                "transport-security", "licensed-evidence-never-runtime",
                "generate-per-environment", "candidate trust anchors paired with generated identity"));
        m.put("js/bootstrap.min.js", policy("third-party-ui-asset-bootstrap-5.3.3",
                "optional-interface", "licensed-evidence-only", "source-from-upstream",
                "official Bootstrap 5.3.3 release with its MIT notice"));
        return m;
    }

    private static ResourcePolicy policy(String category, String phase, String evidence,
                                         String action, String source) {
        return new ResourcePolicy(category, phase, evidence, action, source);
    }

    private static void verifyZip(Path jar, Map<String, ClassRow> classes,
                                  Map<String, ResourceRow> resources) throws Exception {
        TreeSet<String> seenClasses = new TreeSet<>();
        TreeSet<String> seenResources = new TreeSet<>();
        try (ZipFile zip = new ZipFile(jar.toFile())) {
            var entries = zip.entries();
            while (entries.hasMoreElements()) {
                ZipEntry e = entries.nextElement();
                if (e.isDirectory()) continue;
                if (e.getName().endsWith(".class")) {
                    ClassRow c = classes.get(e.getName());
                    check(c != null, "class absent from baseline: " + e.getName());
                    check(e.getSize() == c.bytes(), "class size mismatch: " + e.getName());
                    check(sha256(zip.getInputStream(e)).equals(c.sha256()),
                            "class hash mismatch: " + e.getName());
                    check(seenClasses.add(e.getName()), "duplicate class ZIP entry: " + e.getName());
                } else {
                    ResourceRow r = resources.get(e.getName());
                    check(r != null, "resource absent from baseline: " + e.getName());
                    check(e.getSize() == r.size(), "resource size mismatch: " + e.getName());
                    check(sha256(zip.getInputStream(e)).equals(r.sha256()),
                            "resource hash mismatch: " + e.getName());
                    check(seenResources.add(e.getName()),
                            "duplicate resource ZIP entry: " + e.getName());
                }
            }
        }
        check(seenClasses.equals(classes.keySet()), "class ZIP/baseline entry-set mismatch");
        check(seenResources.equals(resources.keySet()), "resource ZIP/baseline entry-set mismatch");
    }

    private static List<ClassRow> readClasses(Path file) throws IOException {
        return readRows(file, 7, p -> new ClassRow(p[0], p[1], p[2], p[3],
                Integer.parseInt(p[4]), Long.parseLong(p[5]), p[6]));
    }

    private static List<NsOwner> readNamespaces(Path file) throws IOException {
        return readRows(file, 4, p -> {
            check(p[1].endsWith("__init.class"), "bad initializer entry: " + p[1]);
            String prefix = p[1].substring(0, p[1].length() - "__init.class".length());
            int slash = prefix.lastIndexOf('/');
            String expectedSourceStem = prefix.substring(slash + 1);
            return new NsOwner(p[0], p[1], p[2], p[3], prefix, expectedSourceStem);
        });
    }

    private static List<JavaOwner> readJavaOwners(Path file) throws IOException {
        return readRows(file, 2, p -> new JavaOwner(p[0], p[1],
                p[0].substring(0, p[0].length() - ".java".length())));
    }

    private static List<ResourceRow> readResources(Path file) throws IOException {
        return readRows(file, 6, p -> new ResourceRow(p[0], Long.parseLong(p[1]),
                Long.parseLong(p[2]), p[3], p[4], p[5]));
    }

    private static <T> List<T> readRows(Path file, int columns, Function<String[], T> parser)
            throws IOException {
        List<String> lines = Files.readAllLines(file, StandardCharsets.UTF_8);
        check(!lines.isEmpty(), "empty TSV: " + file);
        List<T> rows = new ArrayList<>();
        for (int i = 1; i < lines.size(); i++) {
            String[] p = lines.get(i).split("\\t", -1);
            check(p.length == columns,
                    "unexpected column count at " + file + ":" + (i + 1));
            rows.add(parser.apply(p));
        }
        return rows;
    }

    private static <T> Map<String, T> uniqueMap(List<T> values, Function<T, String> keyFn,
                                                 String label) {
        Map<String, T> result = new TreeMap<>();
        for (T value : values) {
            String key = keyFn.apply(value);
            check(result.put(key, value) == null, "duplicate " + label + ": " + key);
        }
        return result;
    }

    private static void requireEmptyOutput(Path out) throws IOException {
        if (!Files.exists(out)) return;
        try (var stream = Files.list(out)) {
            check(stream.findAny().isEmpty(), "refusing non-empty output: " + out);
        }
    }

    private static void writeLines(Path file, List<String> lines) throws IOException {
        Files.createDirectories(file.getParent());
        Files.writeString(file, String.join("\n", lines) + "\n", StandardCharsets.UTF_8);
    }

    private static void writeManifest(Path out) throws Exception {
        List<Path> files;
        try (var stream = Files.walk(out)) {
            files = stream.filter(Files::isRegularFile)
                    .filter(p -> !p.equals(out.resolve("manifest.sha256")))
                    .sorted(Comparator.comparing(p -> out.relativize(p).toString()))
                    .toList();
        }
        List<String> lines = new ArrayList<>();
        for (Path file : files) {
            lines.add(sha256(file) + "  " + out.relativize(file));
        }
        writeLines(out.resolve("manifest.sha256"), lines);
    }

    private static void metric(List<String> rows, String key, Object value) {
        rows.add(tsv(key, value));
    }

    private static String tsv(Object... fields) {
        List<String> values = new ArrayList<>();
        for (Object f : fields) {
            String s = String.valueOf(f);
            check(!s.contains("\t") && !s.contains("\n") && !s.contains("\r"),
                    "unsafe TSV value");
            values.add(s);
        }
        return String.join("\t", values);
    }

    private static String sha256(Path file) throws Exception {
        try (InputStream in = Files.newInputStream(file)) {
            return sha256(in);
        }
    }

    private static String sha256(InputStream input) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        try (InputStream in = new BufferedInputStream(input)) {
            byte[] buffer = new byte[64 * 1024];
            for (int n; (n = in.read(buffer)) >= 0; ) digest.update(buffer, 0, n);
        }
        StringBuilder out = new StringBuilder(64);
        for (byte b : digest.digest()) out.append(String.format(Locale.ROOT, "%02x", b));
        return out.toString();
    }

    private static void check(boolean condition, String message) {
        if (!condition) throw new IllegalStateException(message);
    }
}
