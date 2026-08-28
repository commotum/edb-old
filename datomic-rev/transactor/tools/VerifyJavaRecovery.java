import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ConstantDynamic;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Handle;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

/**
 * Compares recovered Java-origin classes with their licensed archive entries.
 *
 * <p>The original classes are streamed from the archive and are never extracted.
 * Debug tables, stack-map frames, and compiler-computed max stack/local values are
 * intentionally excluded. The comparison covers class hierarchy/access, field and
 * method ABI, and executable JVM instructions including constants, control flow,
 * calls, and try/catch tables.</p>
 */
public final class VerifyJavaRecovery {
  private static final class Snapshot {
    final String abi;
    final String code;
    final int fields;
    final int methods;

    Snapshot(String abi, String code, int fields, int methods) {
      this.abi = abi;
      this.code = code;
      this.fields = fields;
      this.methods = methods;
    }
  }

  private static final class MethodCode extends MethodVisitor {
    private final StringBuilder output = new StringBuilder();
    private final IdentityHashMap<Label, Integer> labels = new IdentityHashMap<>();
    private int nextLabel;

    MethodCode() {
      super(Opcodes.ASM9);
    }

    String result() {
      return output.toString();
    }

    private int label(Label label) {
      Integer id = labels.get(label);
      if (id == null) {
        id = nextLabel++;
        labels.put(label, id);
      }
      return id;
    }

    private void line(String kind, Object... values) {
      output.append(kind);
      for (Object value : values) {
        output.append('\t').append(value);
      }
      output.append('\n');
    }

    @Override public void visitCode() {
      line("CODE");
    }

    @Override public void visitLabel(Label label) {
      line("LABEL", label(label));
    }

    @Override public void visitInsn(int opcode) {
      line("INSN", opcode);
    }

    @Override public void visitIntInsn(int opcode, int operand) {
      line("INT", opcode, operand);
    }

    @Override public void visitVarInsn(int opcode, int var) {
      line("VAR", opcode, var);
    }

    @Override public void visitTypeInsn(int opcode, String type) {
      line("TYPE", opcode, type);
    }

    @Override public void visitFieldInsn(
        int opcode, String owner, String name, String descriptor) {
      line("FIELD", opcode, owner, name, descriptor);
    }

    @Override public void visitMethodInsn(
        int opcode, String owner, String name, String descriptor, boolean isInterface) {
      line("CALL", opcode, owner, name, descriptor, isInterface);
    }

    @Override public void visitInvokeDynamicInsn(
        String name, String descriptor, Handle bootstrap, Object... arguments) {
      output.append("INDY\t").append(name).append('\t').append(descriptor)
          .append('\t').append(value(bootstrap));
      for (Object argument : arguments) {
        output.append('\t').append(value(argument));
      }
      output.append('\n');
    }

    @Override public void visitJumpInsn(int opcode, Label target) {
      line("JUMP", opcode, label(target));
    }

    @Override public void visitLdcInsn(Object constant) {
      line("LDC", value(constant));
    }

    @Override public void visitIincInsn(int var, int increment) {
      line("IINC", var, increment);
    }

    @Override public void visitTableSwitchInsn(
        int min, int max, Label defaultLabel, Label... targets) {
      output.append("TABLE\t").append(min).append('\t').append(max)
          .append('\t').append(label(defaultLabel));
      for (Label target : targets) {
        output.append('\t').append(label(target));
      }
      output.append('\n');
    }

    @Override public void visitLookupSwitchInsn(
        Label defaultLabel, int[] keys, Label[] targets) {
      output.append("LOOKUP\t").append(label(defaultLabel));
      for (int index = 0; index < keys.length; index++) {
        output.append('\t').append(keys[index]).append(':').append(label(targets[index]));
      }
      output.append('\n');
    }

    @Override public void visitMultiANewArrayInsn(String descriptor, int dimensions) {
      line("MULTI", descriptor, dimensions);
    }

    @Override public void visitTryCatchBlock(
        Label start, Label end, Label handler, String type) {
      line("TRY", label(start), label(end), label(handler), type);
    }

    @Override public void visitEnd() {
      line("END");
    }
  }

  private VerifyJavaRecovery() {}

  private static String nullable(String value) {
    return value == null ? "-" : value;
  }

  private static String value(Object item) {
    if (item == null) {
      return "null";
    }
    if (item instanceof Type) {
      return "type:" + ((Type) item).getDescriptor();
    }
    if (item instanceof Handle) {
      Handle handle = (Handle) item;
      return "handle:" + handle.getTag() + ':' + handle.getOwner() + ':'
          + handle.getName() + ':' + handle.getDesc() + ':' + handle.isInterface();
    }
    if (item instanceof ConstantDynamic) {
      ConstantDynamic dynamic = (ConstantDynamic) item;
      StringBuilder output = new StringBuilder("dynamic:")
          .append(dynamic.getName()).append(':').append(dynamic.getDescriptor())
          .append(':').append(value(dynamic.getBootstrapMethod()));
      for (int index = 0; index < dynamic.getBootstrapMethodArgumentCount(); index++) {
        output.append(':').append(value(dynamic.getBootstrapMethodArgument(index)));
      }
      return output.toString();
    }
    return item.getClass().getName() + ':' + item;
  }

  private static Snapshot snapshot(byte[] classBytes) {
    List<String> classRows = new ArrayList<>();
    List<String> fieldRows = new ArrayList<>();
    List<String> methodRows = new ArrayList<>();
    List<String> relationRows = new ArrayList<>();
    Map<String, MethodCode> methodCode = new TreeMap<>();

    new ClassReader(classBytes).accept(new ClassVisitor(Opcodes.ASM9) {
      @Override public void visit(
          int version, int access, String name, String signature,
          String superName, String[] interfaces) {
        classRows.add("CLASS\t" + access + '\t' + name + '\t' + nullable(signature)
            + '\t' + nullable(superName) + '\t' + String.join(",", interfaces));
      }

      @Override public void visitOuterClass(String owner, String name, String descriptor) {
        relationRows.add("OUTER\t" + owner + '\t' + nullable(name) + '\t'
            + nullable(descriptor));
      }

      @Override public void visitNestHost(String nestHost) {
        relationRows.add("NEST_HOST\t" + nestHost);
      }

      @Override public void visitNestMember(String nestMember) {
        relationRows.add("NEST_MEMBER\t" + nestMember);
      }

      @Override public void visitInnerClass(
          String name, String outerName, String innerName, int access) {
        relationRows.add("INNER\t" + name + '\t' + nullable(outerName) + '\t'
            + nullable(innerName) + '\t' + access);
      }

      @Override public FieldVisitor visitField(
          int access, String name, String descriptor, String signature, Object fieldValue) {
        fieldRows.add("FIELD\t" + access + '\t' + name + '\t' + descriptor + '\t'
            + nullable(signature) + '\t' + value(fieldValue));
        return null;
      }

      @Override public MethodVisitor visitMethod(
          int access, String name, String descriptor, String signature, String[] exceptions) {
        List<String> sortedExceptions = exceptions == null
            ? Collections.emptyList() : new ArrayList<>(Arrays.asList(exceptions));
        Collections.sort(sortedExceptions);
        String key = name + '\t' + descriptor;
        methodRows.add("METHOD\t" + access + '\t' + key + '\t' + nullable(signature)
            + '\t' + String.join(",", sortedExceptions));
        MethodCode visitor = new MethodCode();
        methodCode.put(key, visitor);
        return visitor;
      }
    }, ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);

    Collections.sort(fieldRows);
    Collections.sort(methodRows);
    Collections.sort(relationRows);
    StringBuilder abi = new StringBuilder();
    appendRows(abi, classRows);
    appendRows(abi, relationRows);
    appendRows(abi, fieldRows);
    appendRows(abi, methodRows);

    StringBuilder code = new StringBuilder();
    for (Map.Entry<String, MethodCode> entry : methodCode.entrySet()) {
      code.append("METHOD\t").append(entry.getKey()).append('\n')
          .append(entry.getValue().result());
    }
    return new Snapshot(abi.toString(), code.toString(), fieldRows.size(), methodRows.size());
  }

  private static void appendRows(StringBuilder output, List<String> rows) {
    for (String row : rows) {
      output.append(row).append('\n');
    }
  }

  private static String sha256(byte[] bytes) {
    try {
      byte[] digest = MessageDigest.getInstance("SHA-256").digest(bytes);
      StringBuilder output = new StringBuilder(digest.length * 2);
      for (byte value : digest) {
        output.append(String.format("%02x", value & 0xff));
      }
      return output.toString();
    } catch (NoSuchAlgorithmException error) {
      throw new IllegalStateException(error);
    }
  }

  private static String sha256(String value) {
    return sha256(value.getBytes(StandardCharsets.UTF_8));
  }

  private static List<String> readClassList(Path classList) throws IOException {
    List<String> entries = Files.readAllLines(classList, StandardCharsets.UTF_8).stream()
        .map(String::trim)
        .filter(line -> !line.isEmpty() && !line.startsWith("#"))
        .collect(Collectors.toList());
    List<String> sorted = new ArrayList<>(entries);
    Collections.sort(sorted);
    if (!entries.equals(sorted) || entries.size() != new TreeSet<>(entries).size()) {
      throw new IllegalArgumentException("class list must be sorted and unique: " + classList);
    }
    return entries;
  }

  private static List<String> candidateClasses(Path candidateRoot) throws IOException {
    try (Stream<Path> paths = Files.walk(candidateRoot)) {
      return paths.filter(path -> path.toString().endsWith(".class"))
          .map(candidateRoot::relativize)
          .map(path -> path.toString().replace('\\', '/'))
          .sorted()
          .collect(Collectors.toList());
    }
  }

  public static void main(String[] arguments) throws Exception {
    if (arguments.length != 4) {
      System.err.println(
          "usage: VerifyJavaRecovery CANDIDATE_CLASSES ORIGINAL_JAR CLASS_LIST OUTPUT_DIR");
      System.exit(2);
    }
    Path candidateRoot = Path.of(arguments[0]).toAbsolutePath().normalize();
    Path originalJar = Path.of(arguments[1]).toAbsolutePath().normalize();
    Path classList = Path.of(arguments[2]).toAbsolutePath().normalize();
    Path outputDirectory = Path.of(arguments[3]).toAbsolutePath().normalize();
    List<String> expected = readClassList(classList);
    List<String> actual = candidateClasses(candidateRoot);
    if (!actual.equals(expected)) {
      TreeSet<String> missing = new TreeSet<>(expected);
      missing.removeAll(actual);
      TreeSet<String> unexpected = new TreeSet<>(actual);
      unexpected.removeAll(expected);
      throw new IllegalStateException(
          "candidate class closure differs; missing=" + missing + ", unexpected=" + unexpected);
    }

    Files.createDirectories(outputDirectory);
    StringBuilder relation = new StringBuilder(
        "class_path\tabi_status\tcandidate_abi_sha256\toriginal_abi_sha256"
        + "\tcode_status\tcandidate_code_sha256\toriginal_code_sha256"
        + "\tcandidate_raw_sha256\toriginal_raw_sha256\n");
    int abiMatches = 0;
    int codeMatches = 0;
    int candidateFields = 0;
    int originalFields = 0;
    int candidateMethods = 0;
    int originalMethods = 0;
    try (ZipFile archive = new ZipFile(originalJar.toFile())) {
      for (String classPath : expected) {
        byte[] candidate = Files.readAllBytes(candidateRoot.resolve(classPath));
        ZipEntry entry = archive.getEntry(classPath);
        if (entry == null || entry.isDirectory()) {
          throw new IllegalStateException("original class entry missing: " + classPath);
        }
        byte[] original;
        try (java.io.InputStream input = archive.getInputStream(entry)) {
          original = input.readAllBytes();
        }
        Snapshot candidateSnapshot = snapshot(candidate);
        Snapshot originalSnapshot = snapshot(original);
        candidateFields += candidateSnapshot.fields;
        originalFields += originalSnapshot.fields;
        candidateMethods += candidateSnapshot.methods;
        originalMethods += originalSnapshot.methods;
        String candidateAbi = sha256(candidateSnapshot.abi);
        String originalAbi = sha256(originalSnapshot.abi);
        String candidateCode = sha256(candidateSnapshot.code);
        String originalCode = sha256(originalSnapshot.code);
        boolean abiMatch = candidateAbi.equals(originalAbi);
        boolean codeMatch = candidateCode.equals(originalCode);
        if (abiMatch) abiMatches++;
        if (codeMatch) codeMatches++;
        relation.append(classPath).append('\t')
            .append(abiMatch ? "MATCH" : "DIFF").append('\t')
            .append(candidateAbi).append('\t').append(originalAbi).append('\t')
            .append(codeMatch ? "MATCH" : "DIFF").append('\t')
            .append(candidateCode).append('\t').append(originalCode).append('\t')
            .append(sha256(candidate)).append('\t').append(sha256(original)).append('\n');
      }
    }

    Path relationPath = outputDirectory.resolve("java-class-relation.tsv");
    Files.writeString(relationPath, relation.toString(), StandardCharsets.UTF_8);
    String summary = "metric\tvalue\n"
        + "classes.expected\t" + expected.size() + "\n"
        + "classes.candidate\t" + actual.size() + "\n"
        + "fields.candidate\t" + candidateFields + "\n"
        + "fields.original\t" + originalFields + "\n"
        + "methods.candidate\t" + candidateMethods + "\n"
        + "methods.original\t" + originalMethods + "\n"
        + "abi.matches\t" + abiMatches + "\n"
        + "abi.differences\t" + (expected.size() - abiMatches) + "\n"
        + "code.matches\t" + codeMatches + "\n"
        + "code.differences\t" + (expected.size() - codeMatches) + "\n";
    Files.writeString(outputDirectory.resolve("summary.tsv"), summary, StandardCharsets.UTF_8);
    if (abiMatches != expected.size() || codeMatches != expected.size()) {
      throw new IllegalStateException(
          "Java recovery mismatch: ABI " + abiMatches + '/' + expected.size()
              + ", code " + codeMatches + '/' + expected.size()
              + "; inspect " + relationPath);
    }
    System.out.println(
        "Java recovery exact: " + expected.size() + " classes; relation=" + relationPath);
  }
}
