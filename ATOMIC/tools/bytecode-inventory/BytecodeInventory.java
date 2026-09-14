import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.security.MessageDigest;
import java.util.*;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;

import org.objectweb.asm.*;
import org.w3c.dom.*;

/**
 * Deterministic, read-only bytecode inventory for a primary JAR and a directory
 * of companion JARs. Output contains no timestamps and all records are sorted.
 *
 * Counting rules:
 *   - class/method/field counts are class-file definitions;
 *   - instruction_count is one for every ASM visit*Insn callback (debug
 *     labels, frames, line numbers, local-variable tables and annotations are
 *     deliberately excluded);
 *   - call_count is METHOD_INSN + INVOKEDYNAMIC;
 *   - literal_instruction_count includes ACONST_NULL, xCONST_n, BIPUSH,
 *     SIPUSH and LDC; literal_field_count is ConstantValue attributes;
 *   - field accesses are GETSTATIC/PUTSTATIC/GETFIELD/PUTFIELD instructions.
 */
public final class BytecodeInventory {
    private static final int ASM_API = Opcodes.ASM9;

    record Config(Path primary, Path companionDir, Path pom, Path cljRoot,
                  Path javaRoot, Path out, boolean detailPomDeps,
                  List<Path> detailJars) {}

    static final class JarInfo {
        final Path path;
        final String id;
        long bytes;
        String sha256;
        int entries;
        int directories;
        int classes;
        int resources;
        boolean primary;
        boolean pomDependency;
        boolean detail;
        final List<ResourceData> resourceData = new ArrayList<>();
        JarInfo(Path p) {
            path = p.toAbsolutePath().normalize();
            id = path.getFileName().toString();
        }
    }

    record ResourceData(String jar, String entry, long size, long compressedSize,
                        long crc, int method, String sha256) {}

    static final class PomDep {
        int ordinal;
        String group;
        String artifact;
        String version;
        String scope;
        String optional;
        String type;
        String classifier;
        String exclusions;
        String resolution;
        List<Path> jars = new ArrayList<>();
    }

    record SourceInfo(String kind, String namespace, String internalPrefix,
                      String relativePath) {}

    static final class SourceCatalog {
        final TreeMap<String, SourceInfo> cljByPrefix = new TreeMap<>();
        final TreeMap<String, SourceInfo> javaByClass = new TreeMap<>();
        final TreeMap<String, List<SourceInfo>> byBasename = new TreeMap<>();
    }

    static final class RefCount {
        long total;
        long calls;
        long fields;
        long types;
        final TreeMap<String, Long> kinds = new TreeMap<>();
        void add(String kind) {
            total++;
            kinds.merge(kind, 1L, Long::sum);
            if (kind.equals("method-owner") || kind.equals("indy-bootstrap")) calls++;
            else if (kind.equals("field-owner")) fields++;
            else types++;
        }
        String kindsText() {
            StringJoiner j = new StringJoiner(",");
            kinds.forEach((k, v) -> j.add(k + "=" + v));
            return j.toString();
        }
    }

    static final class ClassData {
        String jar;
        String entry;
        String name;
        int version;
        int access;
        String signature;
        String superName;
        List<String> interfaces = List.of();
        String sourceFile;
        String sourceDebug;
        String sourceKind;
        String sourcePath;
        String namespace;
        String role;
        final List<FieldData> fields = new ArrayList<>();
        final List<MethodData> methods = new ArrayList<>();
        final TreeMap<String, RefCount> refs = new TreeMap<>();
        long instructions;
        long calls;
        long indyCalls;
        long fieldAccesses;
        long literalInstructions;
        long literalFields;
        long classBytes;
        String sha256;
        void ref(String owner, String kind) {
            if (owner == null || owner.isEmpty() || owner.equals(name)) return;
            refs.computeIfAbsent(owner, ignored -> new RefCount()).add(kind);
        }
    }

    record FieldData(String name, String desc, String signature, int access,
                     Object value) {}

    static final class MethodData {
        String name;
        String desc;
        String signature;
        int access;
        List<String> exceptions;
        boolean hasCode;
        long instructions;
        long calls;
        long indyCalls;
        long fieldAccesses;
        long literals;
        long insnIndex;
        final TreeMap<Integer, Long> opcodes = new TreeMap<>();
        final List<CallData> callData = new ArrayList<>();
        final List<FieldAccessData> fieldData = new ArrayList<>();
        final List<LiteralData> literalData = new ArrayList<>();
    }

    record CallData(long instructionIndex, int opcode, String owner, String name,
                    String desc, boolean itf, String bootstrap, String args) {}
    record FieldAccessData(long instructionIndex, int opcode, String owner,
                           String name, String desc) {}
    record LiteralData(long instructionIndex, int opcode, String kind,
                       String value) {}

    static final class Edge {
        final String callerNamespace;
        final String targetKind;
        final String target;
        long references;
        long calls;
        long fields;
        long types;
        final TreeSet<String> owners = new TreeSet<>();
        Edge(String c, String k, String t) {
            callerNamespace = c;
            targetKind = k;
            target = t;
        }
    }

    static final class Inventory {
        Config config;
        final TreeMap<String, JarInfo> jars = new TreeMap<>();
        final List<PomDep> pomDeps = new ArrayList<>();
        final TreeMap<String, TreeSet<String>> definitions = new TreeMap<>();
        final List<ClassData> classes = new ArrayList<>();
        final TreeMap<String, ClassData> detailedByJarAndName = new TreeMap<>();
        final List<String> warnings = new ArrayList<>();
        SourceCatalog sources;
    }

    public static void main(String[] argv) throws Exception {
        Config cfg = parseArgs(argv);
        Inventory inv = new Inventory();
        inv.config = cfg;
        Files.createDirectories(cfg.out());
        inv.sources = scanSources(cfg.cljRoot(), cfg.javaRoot());
        if (cfg.pom() != null) inv.pomDeps.addAll(parsePom(cfg.pom()));
        scanJars(inv);
        resolvePomDependencies(inv);
        chooseDetailedJars(inv);
        parseDetailedJars(inv);
        mapSourcesAndNamespaces(inv);
        writeOutputs(inv);
    }

    private static Config parseArgs(String[] argv) {
        Path primary = null, companion = null, pom = null, clj = null,
             java = null, out = null;
        boolean detailPom = false;
        List<Path> detail = new ArrayList<>();
        for (int i = 0; i < argv.length; i++) {
            String a = argv[i];
            switch (a) {
                case "--primary" -> primary = Path.of(requireValue(argv, ++i, a));
                case "--companion-dir" -> companion = Path.of(requireValue(argv, ++i, a));
                case "--pom" -> pom = Path.of(requireValue(argv, ++i, a));
                case "--clj-source-root" -> clj = Path.of(requireValue(argv, ++i, a));
                case "--java-source-root" -> java = Path.of(requireValue(argv, ++i, a));
                case "--out" -> out = Path.of(requireValue(argv, ++i, a));
                case "--detail-pom-deps" -> detailPom = true;
                case "--detail-jar" -> detail.add(Path.of(requireValue(argv, ++i, a)));
                case "--help", "-h" -> usageAndExit(0);
                default -> throw new IllegalArgumentException("Unknown argument: " + a);
            }
        }
        if (primary == null || out == null) usageAndExit(2);
        return new Config(normalizeExisting(primary, "primary JAR"),
                companion == null ? null : normalizeExisting(companion, "companion directory"),
                pom == null ? null : normalizeExisting(pom, "POM"),
                clj == null ? null : normalizeExisting(clj, "Clojure source root"),
                java == null ? null : normalizeExisting(java, "Java source root"),
                out.toAbsolutePath().normalize(), detailPom,
                detail.stream().map(p -> normalizeExisting(p, "detail JAR")).toList());
    }

    private static String requireValue(String[] a, int i, String flag) {
        if (i >= a.length) throw new IllegalArgumentException("Missing value for " + flag);
        return a[i];
    }

    private static Path normalizeExisting(Path p, String what) {
        Path n = p.toAbsolutePath().normalize();
        if (!Files.exists(n)) throw new IllegalArgumentException(what + " does not exist: " + n);
        return n;
    }

    private static void usageAndExit(int status) {
        System.err.println("Usage: java -cp asm-9.2.jar BytecodeInventory.java " +
                "--primary JAR --out DIR [--companion-dir DIR] [--pom POM] " +
                "[--clj-source-root DIR] [--java-source-root DIR] " +
                "[--detail-pom-deps] [--detail-jar JAR ...]");
        System.exit(status);
    }

    private static SourceCatalog scanSources(Path cljRoot, Path javaRoot) throws IOException {
        SourceCatalog out = new SourceCatalog();
        if (cljRoot != null) {
            for (Path p : regularFiles(cljRoot, ".clj")) {
                String rel = unix(cljRoot.relativize(p));
                String text = Files.readString(p, StandardCharsets.UTF_8);
                String ns = parseNamespace(text);
                String prefix = ns == null ? stripSuffix(rel, ".clj") : mungeNamespace(ns);
                SourceInfo s = new SourceInfo("clojure", ns == null ? dotted(prefix) : ns,
                        prefix, rel);
                out.cljByPrefix.put(prefix, s);
                out.byBasename.computeIfAbsent(p.getFileName().toString(), x -> new ArrayList<>()).add(s);
            }
        }
        if (javaRoot != null) {
            for (Path p : regularFiles(javaRoot, ".java")) {
                String rel = unix(javaRoot.relativize(p));
                String base = stripSuffix(rel, ".java");
                SourceInfo s = new SourceInfo("java", null, base, rel);
                out.javaByClass.put(base, s);
                out.byBasename.computeIfAbsent(p.getFileName().toString(), x -> new ArrayList<>()).add(s);
            }
        }
        out.byBasename.values().forEach(v -> v.sort(Comparator.comparing(SourceInfo::relativePath)));
        return out;
    }

    private static List<Path> regularFiles(Path root, String suffix) throws IOException {
        try (var stream = Files.walk(root)) {
            return stream.filter(Files::isRegularFile)
                    .filter(p -> p.getFileName().toString().endsWith(suffix))
                    .sorted(Comparator.comparing(p -> unix(root.relativize(p))))
                    .toList();
        }
    }

    private static String parseNamespace(String text) {
        Matcher m = Pattern.compile("(?m)\\(ns\\s+(?:\\^\\{[^}]*}\\s*)?([^\\s()]+)").matcher(text);
        if (m.find()) return m.group(1);
        // tools.decompiler reconstructs AOT namespace setup as in-ns rather
        // than an ns macro, sometimes wrapping the symbol in .withMeta.
        m = Pattern.compile("\\((?:clojure\\.core/)?in-ns\\s+(?:\\(\\.withMeta\\s+)?'([^\\s()\\[\\]{}]+)").matcher(text);
        return m.find() ? m.group(1) : null;
    }

    private static String mungeNamespace(String ns) {
        return ns.replace('.', '/').replace('-', '_');
    }

    private static List<PomDep> parsePom(Path pom) throws Exception {
        DocumentBuilderFactory f = DocumentBuilderFactory.newInstance();
        f.setNamespaceAware(true);
        f.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        f.setFeature("http://xml.org/sax/features/external-general-entities", false);
        f.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
        f.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
        f.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
        Document doc = f.newDocumentBuilder().parse(pom.toFile());
        Element project = doc.getDocumentElement();
        Element deps = directChild(project, "dependencies");
        List<PomDep> out = new ArrayList<>();
        if (deps == null) return out;
        int ordinal = 0;
        for (Element d : directChildren(deps, "dependency")) {
            PomDep x = new PomDep();
            x.ordinal = ++ordinal;
            x.group = childText(d, "groupId", "");
            x.artifact = childText(d, "artifactId", "");
            x.version = childText(d, "version", "");
            x.scope = childText(d, "scope", "compile");
            x.optional = childText(d, "optional", "false");
            x.type = childText(d, "type", "jar");
            x.classifier = childText(d, "classifier", "");
            Element exclusions = directChild(d, "exclusions");
            List<String> es = new ArrayList<>();
            if (exclusions != null) {
                for (Element e : directChildren(exclusions, "exclusion")) {
                    es.add(childText(e, "groupId", "") + ":" + childText(e, "artifactId", ""));
                }
            }
            Collections.sort(es);
            x.exclusions = String.join(",", es);
            out.add(x);
        }
        return out;
    }

    private static Element directChild(Element p, String local) {
        for (Node n = p.getFirstChild(); n != null; n = n.getNextSibling()) {
            if (n instanceof Element e && local.equals(e.getLocalName() == null ? e.getTagName() : e.getLocalName())) return e;
        }
        return null;
    }

    private static List<Element> directChildren(Element p, String local) {
        List<Element> out = new ArrayList<>();
        for (Node n = p.getFirstChild(); n != null; n = n.getNextSibling()) {
            if (n instanceof Element e && local.equals(e.getLocalName() == null ? e.getTagName() : e.getLocalName())) out.add(e);
        }
        return out;
    }

    private static String childText(Element p, String local, String fallback) {
        Element e = directChild(p, local);
        return e == null ? fallback : e.getTextContent().trim();
    }

    private static void scanJars(Inventory inv) throws Exception {
        TreeSet<Path> paths = new TreeSet<>(Comparator.comparing(Path::toString));
        paths.add(inv.config.primary());
        if (inv.config.companionDir() != null) {
            try (var stream = Files.list(inv.config.companionDir())) {
                paths.addAll(stream.filter(Files::isRegularFile)
                        .filter(p -> p.getFileName().toString().endsWith(".jar"))
                        .map(p -> p.toAbsolutePath().normalize()).toList());
            }
        }
        paths.addAll(inv.config.detailJars());
        for (Path p : paths) {
            JarInfo j = scanJar(p, inv.definitions);
            j.primary = p.equals(inv.config.primary());
            inv.jars.put(j.id, j);
        }
    }

    private static JarInfo scanJar(Path path, TreeMap<String, TreeSet<String>> definitions) throws Exception {
        JarInfo out = new JarInfo(path);
        out.bytes = Files.size(path);
        out.sha256 = sha256(path);
        try (ZipFile zip = new ZipFile(path.toFile())) {
            List<? extends ZipEntry> entries = Collections.list(zip.entries()).stream()
                    .sorted(Comparator.comparing(ZipEntry::getName)).toList();
            for (ZipEntry e : entries) {
                out.entries++;
                if (e.isDirectory()) {
                    out.directories++;
                } else if (e.getName().endsWith(".class")) {
                    out.classes++;
                    String n = entryClassName(e.getName());
                    definitions.computeIfAbsent(n, ignored -> new TreeSet<>()).add(out.id);
                } else {
                    out.resources++;
                    String digest;
                    try (InputStream in = zip.getInputStream(e)) { digest = sha256(in); }
                    out.resourceData.add(new ResourceData(out.id, e.getName(), e.getSize(),
                            e.getCompressedSize(), e.getCrc(), e.getMethod(), digest));
                }
            }
        }
        return out;
    }

    private static String entryClassName(String entry) {
        String n = entry.substring(0, entry.length() - ".class".length());
        Matcher m = Pattern.compile("^META-INF/versions/[0-9]+/(.*)$").matcher(n);
        return m.matches() ? m.group(1) : n;
    }

    private static void resolvePomDependencies(Inventory inv) {
        List<JarInfo> all = new ArrayList<>(inv.jars.values());
        for (PomDep d : inv.pomDeps) {
            String suffix = d.classifier.isEmpty() ? "" : "-" + d.classifier;
            String exact = d.artifact + "-" + d.version + suffix + ".jar";
            List<JarInfo> found = all.stream().filter(j -> j.id.equals(exact)).toList();
            if (!found.isEmpty()) {
                d.resolution = "exact";
            } else {
                String prefix = d.artifact + "-";
                found = all.stream().filter(j -> j.id.startsWith(prefix) && j.id.endsWith(".jar"))
                        .sorted(Comparator.comparing(j -> j.id)).toList();
                d.resolution = found.isEmpty() ? "missing" : "artifact-fallback";
            }
            for (JarInfo j : found) {
                j.pomDependency = true;
                d.jars.add(j.path);
            }
        }
    }

    private static void chooseDetailedJars(Inventory inv) {
        JarInfo primary = inv.jars.get(inv.config.primary().getFileName().toString());
        primary.detail = true;
        if (inv.config.detailPomDeps()) {
            for (PomDep d : inv.pomDeps) for (Path p : d.jars) {
                JarInfo j = inv.jars.get(p.getFileName().toString());
                if (j != null) j.detail = true;
            }
        }
        for (Path p : inv.config.detailJars()) {
            JarInfo j = inv.jars.get(p.getFileName().toString());
            if (j != null) j.detail = true;
        }
        // Always scan companions that overlap the primary. Their per-class
        // hashes establish whether an apparent namespace closure is copied,
        // divergent, or merely a classpath collision.
        String primaryId = primary.id;
        for (TreeSet<String> definingJars : inv.definitions.values()) {
            if (!definingJars.contains(primaryId)) continue;
            for (String jarId : definingJars) {
                JarInfo j = inv.jars.get(jarId);
                if (j != null) j.detail = true;
            }
        }
    }

    private static void parseDetailedJars(Inventory inv) throws IOException {
        for (JarInfo jar : inv.jars.values()) {
            if (!jar.detail) continue;
            try (ZipFile zip = new ZipFile(jar.path.toFile())) {
                List<? extends ZipEntry> entries = Collections.list(zip.entries()).stream()
                        .filter(e -> !e.isDirectory() && e.getName().endsWith(".class"))
                        .sorted(Comparator.comparing(ZipEntry::getName)).toList();
                for (ZipEntry e : entries) {
                    try (InputStream in = zip.getInputStream(e)) {
                        byte[] bytes = in.readAllBytes();
                        ClassReader cr = new ClassReader(bytes);
                        ClassData c = new ClassData();
                        c.jar = jar.id;
                        c.entry = e.getName();
                        c.classBytes = bytes.length;
                        c.sha256 = sha256(bytes);
                        cr.accept(new CountingClassVisitor(c), 0);
                        inv.classes.add(c);
                        inv.detailedByJarAndName.put(c.jar + "\u0000" + c.name, c);
                    } catch (Exception ex) {
                        inv.warnings.add(jar.id + "\t" + e.getName() + "\t" + ex.getClass().getName() + "\t" + ex.getMessage());
                    }
                }
            }
        }
        inv.classes.sort(Comparator.comparing((ClassData c) -> c.jar).thenComparing(c -> c.name).thenComparing(c -> c.entry));
        Collections.sort(inv.warnings);
    }

    static final class CountingClassVisitor extends ClassVisitor {
        final ClassData c;
        CountingClassVisitor(ClassData c) { super(ASM_API); this.c = c; }

        @Override public void visit(int version, int access, String name, String signature,
                                    String superName, String[] interfaces) {
            c.version = version;
            c.access = access;
            c.name = name;
            c.signature = signature;
            c.superName = superName;
            c.interfaces = interfaces == null ? List.of() : List.of(interfaces.clone());
            c.ref(superName, "super");
            if (interfaces != null) for (String x : interfaces) c.ref(x, "interface");
        }

        @Override public void visitSource(String source, String debug) {
            c.sourceFile = source;
            c.sourceDebug = debug;
        }

        @Override public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
            addDescriptorRefs(c, desc, "class-annotation");
            return null;
        }

        @Override public FieldVisitor visitField(int access, String name, String desc,
                                                 String signature, Object value) {
            c.fields.add(new FieldData(name, desc, signature, access, value));
            addDescriptorRefs(c, desc, "field-descriptor");
            if (value != null) c.literalFields++;
            return new FieldVisitor(ASM_API) {
                @Override public AnnotationVisitor visitAnnotation(String d, boolean visible) {
                    addDescriptorRefs(c, d, "field-annotation");
                    return null;
                }
            };
        }

        @Override public MethodVisitor visitMethod(int access, String name, String desc,
                                                   String signature, String[] exceptions) {
            MethodData m = new MethodData();
            m.access = access;
            m.name = name;
            m.desc = desc;
            m.signature = signature;
            m.exceptions = exceptions == null ? List.of() : List.of(exceptions.clone());
            c.methods.add(m);
            addDescriptorRefs(c, desc, "method-descriptor");
            if (exceptions != null) for (String x : exceptions) c.ref(x, "declared-exception");
            return new CountingMethodVisitor(c, m);
        }
    }

    static final class CountingMethodVisitor extends MethodVisitor {
        final ClassData c;
        final MethodData m;
        CountingMethodVisitor(ClassData c, MethodData m) { super(ASM_API); this.c = c; this.m = m; }

        @Override public void visitCode() { m.hasCode = true; }

        private long insn(int opcode) {
            m.instructions++;
            c.instructions++;
            m.opcodes.merge(opcode, 1L, Long::sum);
            return m.insnIndex++;
        }

        private void literal(long index, int opcode, Object value) {
            m.literals++;
            c.literalInstructions++;
            m.literalData.add(new LiteralData(index, opcode, literalKind(value), renderConstant(value)));
        }

        @Override public void visitInsn(int opcode) {
            long i = insn(opcode);
            Object value = immediateConstant(opcode);
            if (value != NO_LITERAL) literal(i, opcode, value);
        }

        @Override public void visitIntInsn(int opcode, int operand) {
            long i = insn(opcode);
            if (opcode == Opcodes.BIPUSH || opcode == Opcodes.SIPUSH) literal(i, opcode, operand);
        }

        @Override public void visitVarInsn(int opcode, int varIndex) { insn(opcode); }

        @Override public void visitTypeInsn(int opcode, String type) {
            insn(opcode);
            c.ref(type, "type-instruction");
        }

        @Override public void visitFieldInsn(int opcode, String owner, String name, String desc) {
            long i = insn(opcode);
            m.fieldAccesses++;
            c.fieldAccesses++;
            c.ref(owner, "field-owner");
            addDescriptorRefs(c, desc, "field-instruction-descriptor");
            m.fieldData.add(new FieldAccessData(i, opcode, owner, name, desc));
        }

        @Override public void visitMethodInsn(int opcode, String owner, String name,
                                              String desc, boolean itf) {
            long i = insn(opcode);
            m.calls++;
            c.calls++;
            c.ref(owner, "method-owner");
            addDescriptorRefs(c, desc, "call-descriptor");
            m.callData.add(new CallData(i, opcode, owner, name, desc, itf, "", ""));
        }

        @Override public void visitInvokeDynamicInsn(String name, String desc, Handle bootstrap,
                                                     Object... args) {
            long i = insn(Opcodes.INVOKEDYNAMIC);
            m.calls++;
            m.indyCalls++;
            c.calls++;
            c.indyCalls++;
            addDescriptorRefs(c, desc, "indy-descriptor");
            c.ref(bootstrap.getOwner(), "indy-bootstrap");
            addDescriptorRefs(c, bootstrap.getDesc(), "indy-bootstrap-descriptor");
            for (Object x : args) addConstantRefs(c, x, "indy-argument");
            m.callData.add(new CallData(i, Opcodes.INVOKEDYNAMIC, "", name, desc, false,
                    renderConstant(bootstrap), renderConstants(args)));
        }

        @Override public void visitJumpInsn(int opcode, Label label) { insn(opcode); }

        @Override public void visitLdcInsn(Object value) {
            long i = insn(Opcodes.LDC);
            literal(i, Opcodes.LDC, value);
            addConstantRefs(c, value, "ldc");
        }

        @Override public void visitIincInsn(int varIndex, int increment) { insn(Opcodes.IINC); }

        @Override public void visitTableSwitchInsn(int min, int max, Label dflt, Label... labels) {
            insn(Opcodes.TABLESWITCH);
        }

        @Override public void visitLookupSwitchInsn(Label dflt, int[] keys, Label[] labels) {
            insn(Opcodes.LOOKUPSWITCH);
        }

        @Override public void visitMultiANewArrayInsn(String desc, int dims) {
            insn(Opcodes.MULTIANEWARRAY);
            addDescriptorRefs(c, desc, "multiarray-descriptor");
        }

        @Override public void visitTryCatchBlock(Label start, Label end, Label handler, String type) {
            c.ref(type, "catch-type");
        }

        @Override public void visitLocalVariable(String name, String desc, String signature,
                                                 Label start, Label end, int index) {
            addDescriptorRefs(c, desc, "local-descriptor");
        }

        @Override public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
            addDescriptorRefs(c, desc, "method-annotation");
            return null;
        }
    }

    private static final Object NO_LITERAL = new Object();

    private static Object immediateConstant(int opcode) {
        return switch (opcode) {
            case Opcodes.ACONST_NULL -> null;
            case Opcodes.ICONST_M1 -> -1;
            case Opcodes.ICONST_0 -> 0;
            case Opcodes.ICONST_1 -> 1;
            case Opcodes.ICONST_2 -> 2;
            case Opcodes.ICONST_3 -> 3;
            case Opcodes.ICONST_4 -> 4;
            case Opcodes.ICONST_5 -> 5;
            case Opcodes.LCONST_0 -> 0L;
            case Opcodes.LCONST_1 -> 1L;
            case Opcodes.FCONST_0 -> 0.0f;
            case Opcodes.FCONST_1 -> 1.0f;
            case Opcodes.FCONST_2 -> 2.0f;
            case Opcodes.DCONST_0 -> 0.0d;
            case Opcodes.DCONST_1 -> 1.0d;
            default -> NO_LITERAL;
        };
    }

    private static void addDescriptorRefs(ClassData c, String desc, String kind) {
        if (desc == null || desc.isEmpty()) return;
        try {
            Type t = desc.charAt(0) == '(' ? Type.getMethodType(desc) : Type.getType(desc);
            addTypeRefs(c, t, kind);
        } catch (IllegalArgumentException ignored) {
            // Generic signatures are intentionally not treated as descriptors.
        }
    }

    private static void addTypeRefs(ClassData c, Type t, String kind) {
        switch (t.getSort()) {
            case Type.OBJECT -> c.ref(t.getInternalName(), kind);
            case Type.ARRAY -> addTypeRefs(c, t.getElementType(), kind);
            case Type.METHOD -> {
                for (Type x : t.getArgumentTypes()) addTypeRefs(c, x, kind);
                addTypeRefs(c, t.getReturnType(), kind);
            }
        }
    }

    private static void addConstantRefs(ClassData c, Object x, String kind) {
        if (x instanceof Type t) addTypeRefs(c, t, kind);
        else if (x instanceof Handle h) {
            c.ref(h.getOwner(), kind);
            addDescriptorRefs(c, h.getDesc(), kind + "-descriptor");
        } else if (x instanceof ConstantDynamic d) {
            addDescriptorRefs(c, d.getDescriptor(), kind + "-descriptor");
            addConstantRefs(c, d.getBootstrapMethod(), kind + "-bootstrap");
            for (int i = 0; i < d.getBootstrapMethodArgumentCount(); i++)
                addConstantRefs(c, d.getBootstrapMethodArgument(i), kind + "-argument");
        }
    }

    private static void mapSourcesAndNamespaces(Inventory inv) {
        Map<String, List<String>> initPrefixesByJar = new TreeMap<>();
        for (ClassData c : inv.classes) {
            if (c.name.endsWith("__init")) {
                initPrefixesByJar.computeIfAbsent(c.jar, ignored -> new ArrayList<>())
                        .add(c.name.substring(0, c.name.length() - "__init".length()));
            }
        }
        initPrefixesByJar.values().forEach(v -> v.sort(Comparator.comparingInt(String::length).reversed().thenComparing(x -> x)));
        for (ClassData c : inv.classes) {
            if (c.sourceFile != null && c.sourceFile.endsWith(".clj")) c.sourceKind = "clojure-aot";
            else if (c.sourceFile != null && c.sourceFile.endsWith(".java")) c.sourceKind = "java";
            else c.sourceKind = "unknown";

            String prefix = matchingInitPrefix(c.name, initPrefixesByJar.getOrDefault(c.jar, List.of()));
            SourceInfo source = null;
            if (prefix != null) source = inv.sources.cljByPrefix.get(prefix);
            if (source == null && c.sourceFile != null) source = bestSourceCandidate(c, inv.sources.byBasename.get(c.sourceFile));
            if (source == null && "java".equals(c.sourceKind)) source = inv.sources.javaByClass.get(outerClass(c.name));
            if (source != null) {
                c.sourcePath = source.relativePath();
                if (source.namespace() != null) c.namespace = source.namespace();
                if (source.kind().equals("clojure")) c.sourceKind = "clojure-aot";
                else if (source.kind().equals("java")) c.sourceKind = "java";
            }
            // Clojure namespace initializer classes frequently omit SourceFile.
            // An initializer-derived prefix is nevertheless direct AOT evidence.
            if (prefix != null && c.sourceKind.equals("unknown")) c.sourceKind = "clojure-aot";
            if (c.namespace == null && prefix != null) c.namespace = dotted(prefix);
            if (c.namespace == null && "java".equals(c.sourceKind)) c.namespace = javaPackage(c.name);
            if (c.namespace == null) c.namespace = bytecodePackage(c.name);

            if (c.name.endsWith("__init")) c.role = "namespace-init";
            else if (c.sourceKind.equals("clojure-aot") && c.name.contains("$")) c.role = "aot-function-or-generated";
            else if (c.sourceKind.equals("clojure-aot")) c.role = "aot-type-or-interface";
            else if (c.sourceKind.equals("java") && c.name.contains("$")) c.role = "java-nested";
            else if (c.sourceKind.equals("java")) c.role = "java-top-level";
            else c.role = "unknown";
        }
    }

    private static String matchingInitPrefix(String name, List<String> prefixes) {
        for (String p : prefixes) {
            if (name.equals(p + "__init") || name.startsWith(p + "$") || name.startsWith(p + "/")) return p;
        }
        return null;
    }

    private static SourceInfo bestSourceCandidate(ClassData c, List<SourceInfo> candidates) {
        if (candidates == null || candidates.isEmpty()) return null;
        return candidates.stream().max(Comparator
                .comparingInt((SourceInfo s) -> sourceScore(c, s))
                .thenComparing(SourceInfo::relativePath, Comparator.reverseOrder())).orElse(null);
    }

    private static int sourceScore(ClassData c, SourceInfo s) {
        String p = s.internalPrefix();
        int score = 0;
        if (c.name.equals(p) || c.name.startsWith(p + "$") || c.name.startsWith(p + "/")) score += 1000 + p.length();
        if (outerClass(c.name).equals(p)) score += 800;
        if (bytecodePackage(c.name).equals(bytecodePackage(p))) score += 100;
        return score;
    }

    private static void writeOutputs(Inventory inv) throws Exception {
        writeJars(inv);
        writeResources(inv);
        writePomDeps(inv);
        writeClasses(inv);
        writeFields(inv);
        writeMethods(inv);
        writeOpcodes(inv);
        writeCalls(inv);
        writeFieldAccesses(inv);
        writeLiterals(inv);
        writeNamespaceClasses(inv);
        writeNamespaceSummaries(inv);
        writeJarBytecodeSummaries(inv);
        writeExternalOwnersAndEdges(inv);
        writeDuplicates(inv);
        writeSummary(inv);
        writeArchitecture(inv);
        writeLines(inv.config.out().resolve("warnings.tsv"), List.of("jar\tentry\texception\tmessage"), inv.warnings);
    }

    private static void writeJars(Inventory inv) throws IOException {
        List<String> rows = new ArrayList<>();
        for (JarInfo j : inv.jars.values()) rows.add(row(j.id, j.path, j.sha256, j.bytes,
                j.entries, j.directories, j.classes, j.resources, j.primary,
                j.pomDependency, j.detail));
        writeTable(inv, "jars.tsv", "jar\tpath\tsha256\tbytes\tentries\tdirectories\tclass_entries\tresource_entries\tprimary\tpom_dependency\tdetail_scanned", rows);
    }

    private static void writeResources(Inventory inv) throws IOException {
        List<String> rows = new ArrayList<>();
        for (JarInfo j : inv.jars.values()) for (ResourceData r : j.resourceData)
            rows.add(row(r.jar(), r.entry(), r.size(), r.compressedSize(), unsigned(r.crc()),
                    r.method() == ZipEntry.STORED ? "stored" : r.method() == ZipEntry.DEFLATED ? "deflated" : r.method(),
                    r.sha256()));
        writeTable(inv, "resources.tsv", "jar\tentry\tsize\tcompressed_size\tcrc32\tmethod\tsha256", rows);
    }

    private static void writePomDeps(Inventory inv) throws IOException {
        List<String> rows = new ArrayList<>();
        for (PomDep d : inv.pomDeps) rows.add(row(d.ordinal, d.group, d.artifact, d.version,
                d.scope, d.optional, d.type, d.classifier, d.exclusions, d.resolution,
                d.jars.stream().map(p -> p.getFileName().toString()).sorted().toList()));
        writeTable(inv, "pom-dependencies.tsv", "ordinal\tgroup_id\tartifact_id\tdeclared_version\tscope\toptional\ttype\tclassifier\texclusions\tresolution\tshipped_jars", rows);
    }

    private static void writeClasses(Inventory inv) throws IOException {
        List<String> rows = new ArrayList<>();
        for (ClassData c : inv.classes) rows.add(row(c.jar, c.entry, c.name, dotted(c.name),
                c.version & 0xFFFF, c.version >>> 16, accessHex(c.access), c.sourceFile,
                c.sourceKind, c.sourcePath, c.namespace, c.role, c.superName, c.interfaces,
                c.fields.size(), c.methods.size(), c.instructions, c.calls, c.indyCalls,
                c.fieldAccesses, c.literalInstructions, c.literalFields, c.refs.size()));
        // Append byte provenance so the established count-column positions
        // remain stable for simple validators.
        for (int i = 0; i < rows.size(); i++) {
            ClassData c = inv.classes.get(i);
            rows.set(i, rows.get(i) + "\t" + c.classBytes + "\t" + c.sha256);
        }
        writeTable(inv, "classes.tsv", "jar\tentry\tinternal_name\tbinary_name\tmajor\tminor\taccess\tsource_file\tsource_kind\tsource_path\tnamespace\trole\tsuper\tinterfaces\tfield_count\tmethod_count\tinstruction_count\tcall_count\tinvokedynamic_count\tfield_access_count\tliteral_instruction_count\tliteral_field_count\treferenced_owner_count\tclass_bytes\tsha256", rows);
    }

    private static void writeFields(Inventory inv) throws IOException {
        List<String> rows = new ArrayList<>();
        for (ClassData c : inv.classes) {
            List<FieldData> fields = new ArrayList<>(c.fields);
            fields.sort(Comparator.comparing(FieldData::name).thenComparing(FieldData::desc));
            for (FieldData f : fields) rows.add(row(c.jar, c.name, f.name(), f.desc(), f.signature(),
                    accessHex(f.access()), f.value() == null ? "" : literalKind(f.value()),
                    f.value() == null ? "" : renderConstant(f.value())));
        }
        writeTable(inv, "fields.tsv", "jar\tclass\tname\tdescriptor\tsignature\taccess\tconstant_kind\tconstant_value", rows);
    }

    private static void writeMethods(Inventory inv) throws IOException {
        List<String> rows = new ArrayList<>();
        for (ClassData c : inv.classes) {
            List<MethodData> methods = new ArrayList<>(c.methods);
            methods.sort(methodComparator());
            for (MethodData m : methods) rows.add(row(c.jar, c.name, m.name, m.desc, m.signature,
                    accessHex(m.access), m.exceptions, m.hasCode, m.instructions, m.calls,
                    m.indyCalls, m.fieldAccesses, m.literals));
        }
        writeTable(inv, "methods.tsv", "jar\tclass\tname\tdescriptor\tsignature\taccess\texceptions\thas_code\tinstruction_count\tcall_count\tinvokedynamic_count\tfield_access_count\tliteral_count", rows);
    }

    private static void writeOpcodes(Inventory inv) throws IOException {
        List<String> rows = new ArrayList<>();
        for (ClassData c : inv.classes) {
            List<MethodData> methods = new ArrayList<>(c.methods);
            methods.sort(methodComparator());
            for (MethodData m : methods) for (var e : m.opcodes.entrySet())
                rows.add(row(c.jar, c.name, m.name, m.desc, e.getKey(), opcodeName(e.getKey()), e.getValue()));
        }
        writeTable(inv, "opcodes.tsv", "jar\tclass\tmethod\tdescriptor\topcode\topcode_name\tcount", rows);
    }

    private static void writeCalls(Inventory inv) throws IOException {
        List<String> rows = new ArrayList<>();
        for (ClassData c : inv.classes) {
            List<MethodData> methods = new ArrayList<>(c.methods);
            methods.sort(methodComparator());
            for (MethodData m : methods) for (CallData x : m.callData) {
                String resolution = x.owner().isEmpty() ? "dynamic" : resolution(inv, x.owner());
                rows.add(row(c.jar, c.name, m.name, m.desc, x.instructionIndex(), x.opcode(),
                        opcodeName(x.opcode()), x.owner(), x.name(), x.desc(), x.itf(),
                        x.bootstrap(), x.args(), resolution));
            }
        }
        writeTable(inv, "calls.tsv", "jar\tcaller_class\tcaller_method\tcaller_descriptor\tinstruction_index\topcode\topcode_name\ttarget_owner\ttarget_name\ttarget_descriptor\tinterface\tbootstrap\tbootstrap_args\tresolution", rows);
    }

    private static void writeFieldAccesses(Inventory inv) throws IOException {
        List<String> rows = new ArrayList<>();
        for (ClassData c : inv.classes) {
            List<MethodData> methods = new ArrayList<>(c.methods);
            methods.sort(methodComparator());
            for (MethodData m : methods) for (FieldAccessData x : m.fieldData)
                rows.add(row(c.jar, c.name, m.name, m.desc, x.instructionIndex(), x.opcode(),
                        opcodeName(x.opcode()), x.owner(), x.name(), x.desc(), resolution(inv, x.owner())));
        }
        writeTable(inv, "field-accesses.tsv", "jar\tcaller_class\tcaller_method\tcaller_descriptor\tinstruction_index\topcode\topcode_name\ttarget_owner\ttarget_name\ttarget_descriptor\tresolution", rows);
    }

    private static void writeLiterals(Inventory inv) throws IOException {
        List<String> rows = new ArrayList<>();
        for (ClassData c : inv.classes) {
            List<FieldData> fields = new ArrayList<>(c.fields);
            fields.sort(Comparator.comparing(FieldData::name).thenComparing(FieldData::desc));
            for (FieldData f : fields) if (f.value() != null)
                rows.add(row(c.jar, c.name, "field", f.name(), f.desc(), "", "", "ConstantValue",
                        literalKind(f.value()), renderConstant(f.value())));
            List<MethodData> methods = new ArrayList<>(c.methods);
            methods.sort(methodComparator());
            for (MethodData m : methods) for (LiteralData x : m.literalData)
                rows.add(row(c.jar, c.name, "instruction", m.name, m.desc, x.instructionIndex(),
                        x.opcode(), opcodeName(x.opcode()), x.kind(), x.value()));
        }
        writeTable(inv, "literals.tsv", "jar\tclass\torigin\tmember\tdescriptor\tinstruction_index\topcode\topcode_name\tkind\tvalue", rows);
    }

    private static void writeNamespaceClasses(Inventory inv) throws IOException {
        List<String> rows = new ArrayList<>();
        for (ClassData c : inv.classes) rows.add(row(c.jar, c.namespace, c.sourceKind, c.sourcePath,
                c.role, c.name, c.fields.size(), c.methods.size(), c.instructions, c.calls,
                c.fieldAccesses, c.literalInstructions + c.literalFields));
        writeTable(inv, "namespace-classes.tsv", "jar\tnamespace\tsource_kind\tsource_path\trole\tclass\tfields\tmethods\tinstructions\tcalls\tfield_accesses\tliterals", rows);
    }

    private static void writeNamespaceSummaries(Inventory inv) throws IOException {
        record NSKey(String jar, String ns) {}
        class Totals { long classes, fields, methods, instructions, calls, indy, accesses, literals; boolean init; TreeSet<String> sources = new TreeSet<>(); TreeMap<String,Long> kinds = new TreeMap<>(); }
        TreeMap<NSKey, Totals> totals = new TreeMap<>(Comparator.comparing(NSKey::jar).thenComparing(NSKey::ns));
        for (ClassData c : inv.classes) {
            Totals t = totals.computeIfAbsent(new NSKey(c.jar, nz(c.namespace)), ignored -> new Totals());
            t.classes++; t.fields += c.fields.size(); t.methods += c.methods.size();
            t.instructions += c.instructions; t.calls += c.calls; t.indy += c.indyCalls;
            t.accesses += c.fieldAccesses; t.literals += c.literalInstructions + c.literalFields;
            t.init |= c.role.equals("namespace-init");
            if (c.sourcePath != null) t.sources.add(c.sourcePath);
            t.kinds.merge(c.sourceKind, 1L, Long::sum);
        }
        List<String> rows = new ArrayList<>();
        totals.forEach((k,t) -> rows.add(row(k.jar(), k.ns(), t.sources, mapText(t.kinds), t.init,
                t.classes, t.fields, t.methods, t.instructions, t.calls, t.indy, t.accesses, t.literals)));
        writeTable(inv, "namespace-summary.tsv", "jar\tnamespace\tsource_paths\tsource_kind_class_counts\thas_init_class\tclasses\tfields\tmethods\tinstructions\tcalls\tinvokedynamic\tfield_accesses\tliterals", rows);
    }

    private static void writeJarBytecodeSummaries(Inventory inv) throws IOException {
        List<String> rows = new ArrayList<>();
        for (JarInfo j : inv.jars.values()) {
            if (!j.detail) continue;
            List<ClassData> cs = inv.classes.stream().filter(c -> c.jar.equals(j.id)).toList();
            rows.add(row(j.id, j.classes, cs.size(),
                    cs.stream().mapToLong(c -> c.fields.size()).sum(),
                    cs.stream().mapToLong(c -> c.methods.size()).sum(),
                    cs.stream().mapToLong(c -> c.instructions).sum(),
                    cs.stream().mapToLong(c -> c.calls).sum(),
                    cs.stream().mapToLong(c -> c.indyCalls).sum(),
                    cs.stream().mapToLong(c -> c.fieldAccesses).sum(),
                    cs.stream().mapToLong(c -> c.literalInstructions).sum(),
                    cs.stream().mapToLong(c -> c.literalFields).sum(),
                    cs.stream().filter(c -> c.sourceKind.equals("clojure-aot")).count(),
                    cs.stream().filter(c -> c.sourceKind.equals("java")).count(),
                    cs.stream().filter(c -> c.sourceKind.equals("unknown")).count(),
                    cs.stream().filter(c -> c.role.equals("namespace-init")).count(),
                    cs.stream().map(c -> c.namespace).filter(Objects::nonNull).distinct().count(),
                    j.resources));
        }
        writeTable(inv, "jar-bytecode-summary.tsv", "jar\tclass_entries\tparsed_classes\tfields\tmethods\tinstructions\tcalls\tinvokedynamic\tfield_accesses\tliteral_instructions\tliteral_fields\tclojure_aot_classes\tjava_classes\tunknown_source_classes\tnamespace_init_classes\tdistinct_namespaces\tresource_entries", rows);
    }

    private static void writeExternalOwnersAndEdges(Inventory inv) throws IOException {
        String primaryJar = inv.config.primary().getFileName().toString();
        TreeMap<String, RefCount> external = new TreeMap<>();
        TreeMap<String, Edge> edges = new TreeMap<>();
        for (ClassData c : inv.classes) {
            if (!c.jar.equals(primaryJar)) continue;
            for (var r : c.refs.entrySet()) {
                String owner = r.getKey();
                RefCount rc = r.getValue();
                TreeSet<String> defs = inv.definitions.get(owner);
                boolean primaryDef = defs != null && defs.contains(primaryJar);
                if (!primaryDef) mergeRef(external.computeIfAbsent(owner, ignored -> new RefCount()), rc);
                Target target = targetFor(inv, primaryJar, owner);
                String caller = nz(c.namespace);
                String key = caller + "\u0000" + target.kind + "\u0000" + target.name;
                Edge e = edges.computeIfAbsent(key, ignored -> new Edge(caller, target.kind, target.name));
                e.references += rc.total; e.calls += rc.calls; e.fields += rc.fields; e.types += rc.types; e.owners.add(owner);
            }
        }
        List<String> ownerRows = new ArrayList<>();
        external.forEach((owner, rc) -> ownerRows.add(row(owner, rc.total, rc.calls, rc.fields,
                rc.types, rc.kindsText(), resolution(inv, owner), inv.definitions.getOrDefault(owner, new TreeSet<>()))));
        writeTable(inv, "external-owners.tsv", "owner\treferences\tcall_owner_references\tfield_owner_references\ttype_references\treference_kinds\tresolution\tdefining_jars", ownerRows);
        List<String> edgeRows = new ArrayList<>();
        edges.values().stream().sorted(Comparator.comparing((Edge e) -> e.callerNamespace)
                .thenComparing(e -> e.targetKind).thenComparing(e -> e.target)).forEach(e ->
                edgeRows.add(row(e.callerNamespace, e.targetKind, e.target, e.references,
                        e.calls, e.fields, e.types, e.owners.size(), e.owners)));
        writeTable(inv, "namespace-dependencies.tsv", "caller_namespace\ttarget_kind\ttarget\treferences\tcall_owner_references\tfield_owner_references\ttype_references\tdistinct_owners\towners", edgeRows);
    }

    record Target(String kind, String name) {}
    private static Target targetFor(Inventory inv, String primaryJar, String owner) {
        String definitionOwner = arrayElementOwner(owner);
        if (definitionOwner == null && owner.startsWith("[")) return new Target("jdk", "array");
        if (definitionOwner == null) definitionOwner = owner;
        ClassData target = inv.detailedByJarAndName.get(primaryJar + "\u0000" + definitionOwner);
        if (target != null) return new Target("primary-namespace", nz(target.namespace));
        TreeSet<String> defs = inv.definitions.get(definitionOwner);
        if (defs != null && !defs.isEmpty()) return new Target("companion-jar", String.join(",", defs));
        if (isJdk(definitionOwner)) return new Target("jdk", jdkModuleFamily(definitionOwner));
        return new Target("unresolved", bytecodePackage(definitionOwner));
    }

    private static void mergeRef(RefCount into, RefCount from) {
        into.total += from.total; into.calls += from.calls; into.fields += from.fields; into.types += from.types;
        from.kinds.forEach((k,v) -> into.kinds.merge(k, v, Long::sum));
    }

    private static void writeDuplicates(Inventory inv) throws IOException {
        List<String> rows = new ArrayList<>();
        inv.definitions.forEach((name, jars) -> {
            if (jars.size() <= 1) return;
            TreeMap<String, String> hashes = new TreeMap<>();
            for (String jar : jars) {
                ClassData c = inv.detailedByJarAndName.get(jar + "\u0000" + name);
                if (c != null) hashes.put(jar, c.sha256);
            }
            boolean allDetailed = hashes.size() == jars.size();
            long distinctHashes = hashes.values().stream().distinct().count();
            StringJoiner byJar = new StringJoiner(",");
            hashes.forEach((j,h) -> byJar.add(j + "=" + h));
            rows.add(row(name, jars.size(), jars, allDetailed, allDetailed ? distinctHashes : "",
                    allDetailed ? distinctHashes == 1 : "", byJar));
        });
        writeTable(inv, "duplicate-classes.tsv", "class\tjar_count\tjars\tall_definitions_detailed\tdistinct_sha256\tbyte_identical\tsha256_by_jar", rows);
    }

    private static void writeSummary(Inventory inv) throws IOException {
        String primary = inv.config.primary().getFileName().toString();
        List<ClassData> pc = inv.classes.stream().filter(c -> c.jar.equals(primary)).toList();
        List<String> rows = new ArrayList<>();
        metric(rows, "primary.jar", primary);
        metric(rows, "primary.sha256", inv.jars.get(primary).sha256);
        metric(rows, "primary.class_entries", inv.jars.get(primary).classes);
        metric(rows, "primary.parsed_classes", pc.size());
        metric(rows, "primary.fields", pc.stream().mapToLong(c -> c.fields.size()).sum());
        metric(rows, "primary.methods", pc.stream().mapToLong(c -> c.methods.size()).sum());
        metric(rows, "primary.instructions", pc.stream().mapToLong(c -> c.instructions).sum());
        metric(rows, "primary.calls", pc.stream().mapToLong(c -> c.calls).sum());
        metric(rows, "primary.invokedynamic", pc.stream().mapToLong(c -> c.indyCalls).sum());
        metric(rows, "primary.field_accesses", pc.stream().mapToLong(c -> c.fieldAccesses).sum());
        metric(rows, "primary.literal_instructions", pc.stream().mapToLong(c -> c.literalInstructions).sum());
        metric(rows, "primary.literal_fields", pc.stream().mapToLong(c -> c.literalFields).sum());
        metric(rows, "primary.clojure_aot_classes", pc.stream().filter(c -> c.sourceKind.equals("clojure-aot")).count());
        metric(rows, "primary.java_classes", pc.stream().filter(c -> c.sourceKind.equals("java")).count());
        metric(rows, "primary.unknown_source_classes", pc.stream().filter(c -> c.sourceKind.equals("unknown")).count());
        metric(rows, "primary.source_mapped_classes", pc.stream().filter(c -> c.sourcePath != null).count());
        metric(rows, "primary.distinct_clojure_source_paths", pc.stream().filter(c -> c.sourceKind.equals("clojure-aot")).map(c -> c.sourcePath).filter(Objects::nonNull).distinct().count());
        metric(rows, "primary.distinct_java_source_paths", pc.stream().filter(c -> c.sourceKind.equals("java")).map(c -> c.sourcePath).filter(Objects::nonNull).distinct().count());
        metric(rows, "primary.namespace_init_classes", pc.stream().filter(c -> c.role.equals("namespace-init")).count());
        metric(rows, "primary.distinct_namespaces", pc.stream().map(c -> c.namespace).filter(Objects::nonNull).distinct().count());
        metric(rows, "primary.resource_entries", inv.jars.get(primary).resources);
        if (inv.config.pom() != null) {
            String pomHash;
            try { pomHash = sha256(inv.config.pom()); }
            catch (Exception e) { throw new IOException(e); }
            metric(rows, "pom.input_path", inv.config.pom());
            metric(rows, "pom.input_sha256", pomHash);
            Optional<ResourceData> embedded = inv.jars.get(primary).resourceData.stream()
                    .filter(r -> r.entry().matches("META-INF/maven/[^/]+/[^/]+/pom\\.xml"))
                    .findFirst();
            metric(rows, "pom.embedded_entry", embedded.map(ResourceData::entry).orElse(""));
            metric(rows, "pom.embedded_sha256", embedded.map(ResourceData::sha256).orElse(""));
            metric(rows, "pom.embedded_matches_input", embedded.map(r -> r.sha256().equals(pomHash)).orElse(false));
        }
        metric(rows, "distribution.companion_jars", inv.jars.size() - 1L);
        metric(rows, "distribution.indexed_class_names", inv.definitions.size());
        metric(rows, "distribution.duplicate_class_names", inv.definitions.values().stream().filter(s -> s.size() > 1).count());
        metric(rows, "pom.direct_dependencies", inv.pomDeps.size());
        metric(rows, "pom.exactly_resolved", inv.pomDeps.stream().filter(d -> d.resolution.equals("exact")).count());
        metric(rows, "pom.fallback_resolved", inv.pomDeps.stream().filter(d -> d.resolution.equals("artifact-fallback")).count());
        metric(rows, "pom.missing", inv.pomDeps.stream().filter(d -> d.resolution.equals("missing")).count());
        metric(rows, "detail.jars", inv.jars.values().stream().filter(j -> j.detail).count());
        metric(rows, "detail.classes", inv.classes.size());
        metric(rows, "parse.warnings", inv.warnings.size());
        writeTable(inv, "summary.tsv", "metric\tvalue", rows);
    }

    private static void writeArchitecture(Inventory inv) throws IOException {
        String primary = inv.config.primary().getFileName().toString();
        List<ClassData> pc = inv.classes.stream().filter(c -> c.jar.equals(primary)).toList();
        long fields = pc.stream().mapToLong(c -> c.fields.size()).sum();
        long methods = pc.stream().mapToLong(c -> c.methods.size()).sum();
        long insns = pc.stream().mapToLong(c -> c.instructions).sum();
        long calls = pc.stream().mapToLong(c -> c.calls).sum();
        long literals = pc.stream().mapToLong(c -> c.literalInstructions + c.literalFields).sum();
        record N(String name, long classes, long methods, long instructions, long calls) {}
        TreeMap<String,long[]> nt = new TreeMap<>();
        for (ClassData c : pc) {
            long[] x = nt.computeIfAbsent(nz(c.namespace), ignored -> new long[4]);
            x[0]++; x[1] += c.methods.size(); x[2] += c.instructions; x[3] += c.calls;
        }
        List<N> top = nt.entrySet().stream().map(e -> new N(e.getKey(), e.getValue()[0], e.getValue()[1], e.getValue()[2], e.getValue()[3]))
                .sorted(Comparator.comparingLong(N::instructions).reversed().thenComparing(N::name)).limit(25).toList();
        List<String> lines = new ArrayList<>();
        lines.add("# Bytecode architecture inventory");
        lines.add("");
        lines.add("Primary artifact: `" + primary + "` (`" + inv.jars.get(primary).sha256 + "`).");
        lines.add("");
        lines.add("## Exact primary counts");
        lines.add("");
        lines.add("| Classes | Fields | Methods | Instructions | Calls | Literals |");
        lines.add("|---:|---:|---:|---:|---:|---:|");
        lines.add("| " + pc.size() + " | " + fields + " | " + methods + " | " + insns + " | " + calls + " | " + literals + " |");
        lines.add("");
        lines.add("Calls include direct method instructions and `invokedynamic`. Literals follow the rules documented in `BytecodeInventory.java` and `README.md`.");
        lines.add("");
        lines.add("## Source provenance");
        lines.add("");
        lines.add("| Kind | Classes |");
        lines.add("|---|---:|");
        for (String kind : List.of("clojure-aot", "java", "unknown"))
            lines.add("| " + kind + " | " + pc.stream().filter(c -> c.sourceKind.equals(kind)).count() + " |");
        lines.add("");
        long cljPaths = pc.stream().filter(c -> c.sourceKind.equals("clojure-aot")).map(c -> c.sourcePath).filter(Objects::nonNull).distinct().count();
        long javaPaths = pc.stream().filter(c -> c.sourceKind.equals("java")).map(c -> c.sourcePath).filter(Objects::nonNull).distinct().count();
        lines.add("The " + pc.stream().filter(c -> c.sourceKind.equals("clojure-aot")).count() + " AOT class files map to " + cljPaths + " reconstructed Clojure source paths; the " + pc.stream().filter(c -> c.sourceKind.equals("java")).count() + " Java class files map to " + javaPaths + " top-level Java source paths. Namespace init classes: " + pc.stream().filter(c -> c.role.equals("namespace-init")).count() + ". Detailed closure is in `namespace-classes.tsv` and `namespace-dependencies.tsv`.");
        if (inv.config.javaRoot() != null) {
            long catalogFiles = inv.sources.javaByClass.size();
            lines.add("");
            lines.add("The provided Java source catalog contains " + catalogFiles + " files versus " + pc.size() + " primary class entries. For this artifact, the difference is the nested handwritten classes that decompilers coalesce into their enclosing Java source; `classes.tsv` remains the class-file authority.");
        }
        lines.add("");
        lines.add("### Class-file versions");
        lines.add("");
        lines.add("| Major | Class entries |");
        lines.add("|---:|---:|");
        TreeMap<Integer, Long> versions = new TreeMap<>();
        for (ClassData c : pc) versions.merge(c.version & 0xffff, 1L, Long::sum);
        versions.forEach((v,n) -> lines.add("| " + v + " | " + n + " |"));
        lines.add("");
        lines.add("## Largest primary namespaces by instruction count");
        lines.add("");
        lines.add("| Namespace | Classes | Methods | Instructions | Calls |");
        lines.add("|---|---:|---:|---:|---:|");
        for (N n : top) lines.add("| `" + n.name + "` | " + n.classes + " | " + n.methods + " | " + n.instructions + " | " + n.calls + " |");
        lines.add("");
        lines.add("## Distribution and dependency evidence");
        lines.add("");
        lines.add("The companion index covers " + (inv.jars.size() - 1) + " JARs and " + inv.definitions.size() + " distinct class names. The distribution POM declares " + inv.pomDeps.size() + " direct dependencies: " + inv.pomDeps.stream().filter(d -> d.resolution.equals("exact")).count() + " exact filename matches, " + inv.pomDeps.stream().filter(d -> d.resolution.equals("artifact-fallback")).count() + " artifact-only fallbacks, and " + inv.pomDeps.stream().filter(d -> d.resolution.equals("missing")).count() + " missing artifacts.");
        if (inv.config.pom() != null) {
            String inputPomHash;
            try { inputPomHash = sha256(inv.config.pom()); }
            catch (Exception e) { throw new IOException(e); }
            Optional<ResourceData> embeddedPom = inv.jars.get(primary).resourceData.stream()
                    .filter(r -> r.entry().matches("META-INF/maven/[^/]+/[^/]+/pom\\.xml")).findFirst();
            lines.add("");
            lines.add("The embedded Maven POM " + (embeddedPom.isPresent() && embeddedPom.get().sha256().equals(inputPomHash) ? "is byte-identical to" : "differs from") + " the distribution POM (`" + inputPomHash + "`).");
        }
        lines.add("");
        lines.add("| Coordinate | Scope | Resolution | Shipped JARs |");
        lines.add("|---|---|---|---|");
        for (PomDep d : inv.pomDeps) if (!d.resolution.equals("exact"))
            lines.add("| `" + d.group + ":" + d.artifact + ":" + d.version + "` | " + d.scope + " | " + d.resolution + " | " + (d.jars.isEmpty() ? "" : d.jars.stream().map(p -> p.getFileName().toString()).sorted().collect(java.util.stream.Collectors.joining(", "))) + " |");
        lines.add("");
        long primaryOverlaps = inv.definitions.values().stream().filter(js -> js.contains(primary) && js.size() > 1).count();
        lines.add("There are " + inv.definitions.values().stream().filter(js -> js.size() > 1).count() + " duplicate class names across the indexed distribution, including " + primaryOverlaps + " primary/companion overlaps. Resolution consumers must retain classpath order; `duplicate-classes.tsv` records every ambiguity.");
        TreeMap<String, long[]> overlapByJar = new TreeMap<>();
        inv.definitions.forEach((name, js) -> {
            if (!js.contains(primary) || js.size() <= 1) return;
            ClassData p = inv.detailedByJarAndName.get(primary + "\u0000" + name);
            for (String jar : js) if (!jar.equals(primary)) {
                long[] counts = overlapByJar.computeIfAbsent(jar, ignored -> new long[3]);
                counts[0]++;
                ClassData other = inv.detailedByJarAndName.get(jar + "\u0000" + name);
                if (p != null && other != null) {
                    if (p.sha256.equals(other.sha256)) counts[1]++; else counts[2]++;
                }
            }
        });
        if (!overlapByJar.isEmpty()) {
            lines.add("");
            lines.add("| Overlap companion | Shared classes | Companion classes | Complete class closure | Byte-identical | Different bytes |");
            lines.add("|---|---:|---:|---|---:|---:|");
            overlapByJar.forEach((jar,n) -> {
                JarInfo companion = inv.jars.get(jar);
                long companionClasses = companion == null ? -1 : companion.classes;
                lines.add("| `" + jar + "` | " + n[0] + " | " + (companionClasses < 0 ? "" : companionClasses) + " | " + (companionClasses >= 0 && n[0] == companionClasses) + " | " + n[1] + " | " + n[2] + " |");
            });
        }
        lines.add("");
        lines.add("Use `external-owners.tsv` for every non-primary owner referenced by primary bytecode, `pom-dependencies.tsv` for declared-vs-shipped dependency evidence, `resources.tsv` for the ZIP resource catalog, and `duplicate-classes.tsv` before assigning any external owner to a unique companion.");
        lines.add("");
        lines.add("### Primary resources");
        lines.add("");
        lines.add("| Entry | Bytes | SHA-256 |");
        lines.add("|---|---:|---|");
        for (ResourceData r : inv.jars.get(primary).resourceData)
            lines.add("| `" + r.entry() + "` | " + r.size() + " | `" + r.sha256() + "` |");
        lines.add("");
        lines.add("## Reproducibility boundary");
        lines.add("");
        lines.add("ZIP timestamps are intentionally excluded. Rows, sets, and maps are sorted; paths identify inputs; JAR SHA-256 values bind the run to exact bytes. No class loading or code execution from an analyzed JAR occurs.");
        writeLines(inv.config.out().resolve("architecture.md"), lines, List.of());
    }

    private static void metric(List<String> rows, String key, Object value) { rows.add(row(key, value)); }

    private static void writeTable(Inventory inv, String file, String header, List<String> rows) throws IOException {
        writeLines(inv.config.out().resolve(file), List.of(header), rows);
    }

    private static void writeLines(Path file, List<String> first, List<String> rest) throws IOException {
        try (BufferedWriter w = Files.newBufferedWriter(file, StandardCharsets.UTF_8)) {
            for (String s : first) { w.write(s); w.write('\n'); }
            for (String s : rest) { w.write(s); w.write('\n'); }
        }
    }

    private static Comparator<MethodData> methodComparator() {
        return Comparator.comparing((MethodData m) -> m.name).thenComparing(m -> m.desc);
    }

    private static String row(Object... values) {
        StringJoiner j = new StringJoiner("\t");
        for (Object v : values) j.add(tsv(v));
        return j.toString();
    }

    private static String tsv(Object v) {
        if (v == null) return "";
        String s;
        if (v instanceof Collection<?> c) s = c.stream().map(BytecodeInventory::plain).collect(java.util.stream.Collectors.joining(","));
        else s = plain(v);
        return escapeTsv(s);
    }

    // Class-file modified UTF-8 permits isolated UTF-16 surrogate code units.
    // Standard UTF-8 writers reject them, so retain their exact code unit as a
    // deterministic ASCII escape. Escape all C0 controls for one-record-per-line TSV.
    private static String escapeTsv(String s) {
        StringBuilder b = new StringBuilder(s.length() + 16);
        for (int i = 0; i < s.length(); i++) {
            char ch = s.charAt(i);
            if (ch == '\\') b.append("\\\\");
            else if (ch == '\t') b.append("\\t");
            else if (ch == '\r') b.append("\\r");
            else if (ch == '\n') b.append("\\n");
            else if (ch < 0x20 || ch == 0x7f) b.append(String.format(Locale.ROOT, "\\u%04x", (int) ch));
            else if (Character.isHighSurrogate(ch)) {
                if (i + 1 < s.length() && Character.isLowSurrogate(s.charAt(i + 1))) {
                    b.append(ch).append(s.charAt(++i));
                } else b.append(String.format(Locale.ROOT, "\\u%04x", (int) ch));
            } else if (Character.isLowSurrogate(ch)) {
                b.append(String.format(Locale.ROOT, "\\u%04x", (int) ch));
            } else b.append(ch);
        }
        return b.toString();
    }

    private static String plain(Object v) { return v == null ? "" : String.valueOf(v); }
    private static String nz(String s) { return s == null ? "" : s; }
    private static String unix(Path p) { return p.toString().replace(File.separatorChar, '/'); }
    private static String stripSuffix(String s, String suffix) { return s.substring(0, s.length() - suffix.length()); }
    private static String dotted(String internal) { return internal == null ? "" : internal.replace('/', '.'); }
    private static String outerClass(String internal) { int i = internal.indexOf('$'); return i < 0 ? internal : internal.substring(0, i); }
    private static String bytecodePackage(String internal) { int i = internal.lastIndexOf('/'); return i < 0 ? "" : dotted(internal.substring(0, i)); }
    private static String javaPackage(String internal) { return bytecodePackage(outerClass(internal)); }
    private static String accessHex(int access) { return String.format(Locale.ROOT, "0x%04x", access); }
    private static String unsigned(long crc) { return crc < 0 ? "" : String.format(Locale.ROOT, "%08x", crc); }

    private static String mapText(Map<String, ? extends Number> m) {
        StringJoiner j = new StringJoiner(",");
        m.forEach((k,v) -> j.add(k + "=" + v));
        return j.toString();
    }

    private static String resolution(Inventory inv, String owner) {
        String definitionOwner = arrayElementOwner(owner);
        if (definitionOwner == null && owner.startsWith("[")) return "jdk:array";
        if (definitionOwner == null) definitionOwner = owner;
        TreeSet<String> jars = inv.definitions.get(definitionOwner);
        if (jars != null && !jars.isEmpty()) return "jar:" + String.join(",", jars);
        if (isJdk(definitionOwner)) return "jdk:" + jdkModuleFamily(definitionOwner);
        return "unresolved";
    }

    // ASM may surface an array descriptor as the owner of clone() or as the
    // operand of CHECKCAST/INSTANCEOF. Resolve object arrays through their
    // element class; a null return denotes a primitive array JVM type.
    private static String arrayElementOwner(String owner) {
        if (owner == null || !owner.startsWith("[")) return owner;
        try {
            Type element = Type.getType(owner).getElementType();
            return element.getSort() == Type.OBJECT ? element.getInternalName() : null;
        } catch (IllegalArgumentException e) {
            return owner;
        }
    }

    private static boolean isJdk(String owner) {
        return owner.startsWith("java/") || owner.startsWith("javax/") || owner.startsWith("jdk/") || owner.startsWith("sun/") || owner.startsWith("com/sun/");
    }

    private static String jdkModuleFamily(String owner) {
        int slash = owner.indexOf('/');
        if (slash < 0) return owner;
        int second = owner.indexOf('/', slash + 1);
        return dotted(second < 0 ? owner : owner.substring(0, second));
    }

    private static String sha256(Path p) throws Exception {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        try (InputStream in = Files.newInputStream(p)) {
            return sha256(in, md);
        }
    }

    private static String sha256(InputStream in) throws Exception {
        return sha256(in, MessageDigest.getInstance("SHA-256"));
    }

    private static String sha256(byte[] bytes) throws Exception {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        return HexFormat.of().formatHex(md.digest(bytes));
    }

    private static String sha256(InputStream in, MessageDigest md) throws IOException {
        byte[] buf = new byte[1 << 20];
        for (int n; (n = in.read(buf)) >= 0;) if (n > 0) md.update(buf, 0, n);
        return HexFormat.of().formatHex(md.digest());
    }

    private static String literalKind(Object v) {
        if (v == null) return "null";
        if (v instanceof String) return "string";
        if (v instanceof Integer) return "int";
        if (v instanceof Long) return "long";
        if (v instanceof Float) return "float";
        if (v instanceof Double) return "double";
        if (v instanceof Type) return "type";
        if (v instanceof Handle) return "handle";
        if (v instanceof ConstantDynamic) return "constant-dynamic";
        return v.getClass().getName();
    }

    private static String renderConstants(Object[] values) {
        StringJoiner j = new StringJoiner(",", "[", "]");
        for (Object v : values) j.add(renderConstant(v));
        return j.toString();
    }

    private static String renderConstant(Object v) {
        if (v == null) return "null";
        if (v instanceof String s) return "string:" + s;
        if (v instanceof Float f) return "float:0x" + Integer.toHexString(Float.floatToRawIntBits(f));
        if (v instanceof Double d) return "double:0x" + Long.toHexString(Double.doubleToRawLongBits(d));
        if (v instanceof Integer i) return "int:" + i;
        if (v instanceof Long l) return "long:" + l;
        if (v instanceof Type t) return "type:" + t.getDescriptor();
        if (v instanceof Handle h) return "handle:" + h.getTag() + ":" + h.getOwner() + "." + h.getName() + h.getDesc() + ":itf=" + h.isInterface();
        if (v instanceof ConstantDynamic d) {
            List<Object> args = new ArrayList<>();
            for (int i = 0; i < d.getBootstrapMethodArgumentCount(); i++) args.add(d.getBootstrapMethodArgument(i));
            return "condy:" + d.getName() + ":" + d.getDescriptor() + ":" + renderConstant(d.getBootstrapMethod()) + ":" + renderConstants(args.toArray());
        }
        return v.getClass().getName() + ":" + v;
    }

    private static final String[] OPCODE_NAMES = buildOpcodeNames();
    private static String opcodeName(int opcode) {
        return opcode >= 0 && opcode < OPCODE_NAMES.length && OPCODE_NAMES[opcode] != null ? OPCODE_NAMES[opcode] : "opcode_" + opcode;
    }

    private static String[] buildOpcodeNames() {
        String text = "NOP ACONST_NULL ICONST_M1 ICONST_0 ICONST_1 ICONST_2 ICONST_3 ICONST_4 ICONST_5 LCONST_0 LCONST_1 FCONST_0 FCONST_1 FCONST_2 DCONST_0 DCONST_1 BIPUSH SIPUSH LDC LDC_W LDC2_W ILOAD LLOAD FLOAD DLOAD ALOAD ILOAD_0 ILOAD_1 ILOAD_2 ILOAD_3 LLOAD_0 LLOAD_1 LLOAD_2 LLOAD_3 FLOAD_0 FLOAD_1 FLOAD_2 FLOAD_3 DLOAD_0 DLOAD_1 DLOAD_2 DLOAD_3 ALOAD_0 ALOAD_1 ALOAD_2 ALOAD_3 IALOAD LALOAD FALOAD DALOAD AALOAD BALOAD CALOAD SALOAD ISTORE LSTORE FSTORE DSTORE ASTORE ISTORE_0 ISTORE_1 ISTORE_2 ISTORE_3 LSTORE_0 LSTORE_1 LSTORE_2 LSTORE_3 FSTORE_0 FSTORE_1 FSTORE_2 FSTORE_3 DSTORE_0 DSTORE_1 DSTORE_2 DSTORE_3 ASTORE_0 ASTORE_1 ASTORE_2 ASTORE_3 IASTORE LASTORE FASTORE DASTORE AASTORE BASTORE CASTORE SASTORE POP POP2 DUP DUP_X1 DUP_X2 DUP2 DUP2_X1 DUP2_X2 SWAP IADD LADD FADD DADD ISUB LSUB FSUB DSUB IMUL LMUL FMUL DMUL IDIV LDIV FDIV DDIV IREM LREM FREM DREM INEG LNEG FNEG DNEG ISHL LSHL ISHR LSHR IUSHR LUSHR IAND LAND IOR LOR IXOR LXOR IINC I2L I2F I2D L2I L2F L2D F2I F2L F2D D2I D2L D2F I2B I2C I2S LCMP FCMPL FCMPG DCMPL DCMPG IFEQ IFNE IFLT IFGE IFGT IFLE IF_ICMPEQ IF_ICMPNE IF_ICMPLT IF_ICMPGE IF_ICMPGT IF_ICMPLE IF_ACMPEQ IF_ACMPNE GOTO JSR RET TABLESWITCH LOOKUPSWITCH IRETURN LRETURN FRETURN DRETURN ARETURN RETURN GETSTATIC PUTSTATIC GETFIELD PUTFIELD INVOKEVIRTUAL INVOKESPECIAL INVOKESTATIC INVOKEINTERFACE INVOKEDYNAMIC NEW NEWARRAY ANEWARRAY ARRAYLENGTH ATHROW CHECKCAST INSTANCEOF MONITORENTER MONITOREXIT WIDE MULTIANEWARRAY IFNULL IFNONNULL GOTO_W JSR_W";
        return text.split(" ");
    }
}
