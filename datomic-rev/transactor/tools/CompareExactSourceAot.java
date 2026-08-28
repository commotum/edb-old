import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.function.Consumer;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Attribute;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
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

/**
 * Fail-closed semantic comparator for the exact-source AOT cohort.
 *
 * The comparator deliberately uses only ASM's core visitor API.  It records
 * every non-debug class-file callback, including expanded stack-map frames,
 * and refuses unknown attributes.  Compiler-generated differences are
 * quotiented only through typed graph mappings or source-, owner-, shape-, and
 * count-sealed witnesses.  Everything else is compared exactly.  Max-stack and
 * max-local values remain outside this semantic model and therefore require the
 * validator's separate whole-cohort verifier gate before acceptance.
 */
public final class CompareExactSourceAot {
  private static final int ASM = Opcodes.ASM9;
  private static final int READ_FLAGS = ClassReader.EXPAND_FRAMES;
  private static final Pattern DOUBLE_ID = Pattern.compile("__(\\d+)");
  private static final Pattern EVAL_ID = Pattern.compile("\\$eval(\\d+)");
  private static final Pattern INST_ID = Pattern.compile("\\$inst_(\\d+)__");
  private static final Pattern STRUCTURAL_GENSYM_SYMBOL = Pattern.compile("[A-Za-z_$][A-Za-z0-9_$-]*__\\d+__auto__");
  private static final Pattern STATE_MACHINE_OWNER = Pattern.compile("^(.*\\$state_machine__\\d+__auto____\\d+)(?:\\$.*)?$");
  private static final Pattern CORE_ASYNC_IOC_INVOKE_OWNER = Pattern.compile(
      "^clojure/core/async\\$.*\\$state_machine__\\d+__auto____\\d+\\$fn__\\d+(?:\\$.*)?$");
  private static final String CORE_ASYNC_NAMESPACE = "clojure.core.async";
  private static final String CORE_ASYNC_SOURCE_ENTRY = "clojure/core/async.clj";
  private static final String CORE_ASYNC_SOURCE_SHA256 =
      "f205f3eb5be1b05c7191d514eedd1b968fde53e4e207f14782e6374bbf271f58";
  private static final String CORE_ASYNC_OWNER_JAR = "lib/core.async-1.6.681.jar";
  private static final String CORE_ASYNC_OWNER_SHA256 =
      "2e9160118c381d418ab1c4d330b6323ff76b7947e6e2d9b1cd1be950fd269d9f";
  private static final Pattern GENERATED_LOCAL_NAME = Pattern.compile(
      "(?:(?:inst|statearr|state|seq|i|count|chunk)_\\d+|" +
      "[A-Za-z_$][A-Za-z0-9_$]*__\\d+(?:__auto__(?:\\d+)?)?|p\\d+__\\d+_SHARP_|" +
      "(?:tmp|fn|c|a|msg|ex|f|x_amz_content_sha)\\d+)");
  private static final Pattern CORE_ASYNC_ALTS_MAP_LOCAL = Pattern.compile("^map__(\\d+)$");
  private static final Pattern CORE_ASYNC_ALTS_PARAMETER_LOCAL = Pattern.compile("^p__(\\d+)$");
  private static final Pattern AUTO_LOCAL_TERMINAL_UNIQUIFIER = Pattern.compile(
      "^([A-Za-z_$][A-Za-z0-9_$]*__\\d+__auto__)(\\d+)$");
  private static final Pattern SHARP_LOCAL_UNIQUIFIER = Pattern.compile("^(p\\d+)__(\\d+)_SHARP_$");
  private static final Pattern CORE_ASYNC_DO_ALT_VEC_LOCAL = Pattern.compile("^vec__(\\d+)$");
  /* Four independently compiled copies of the exact async.clj identify these
   * complete hygienic-macro bases as byte-stable while only the terminal AOT
   * context uniquifier changes.  The base is never alpha-renamed by this
   * quotient. */
  private static final Set<String> CORE_ASYNC_AUTO_LOCAL_STABLE_BASES =
      Collections.unmodifiableSet(new LinkedHashSet<String>(Arrays.asList(
          "and__5579__auto__", "c__6065__auto__", "cache__8034__auto__",
          "f__8035__auto__", "f__8037__auto__", "n__6088__auto__",
          "or__5581__auto__", "temp__5802__auto__", "temp__5804__auto__",
          "v__6812__auto__")));
  private static final int CORE_ASYNC_AUTO_LOCAL_WITNESS_ROWS = 76;
  private static final Map<String, Integer> CORE_ASYNC_AUTO_LOCAL_WITNESS_SHAPE_COUNTS =
      coreAsyncAutoLocalWitnessShapeCounts();
  private static final int CORE_ASYNC_SHARP_LOCAL_WITNESS_ROWS = 7;
  private static final Map<String, Integer> CORE_ASYNC_SHARP_LOCAL_WITNESS_SHAPE_COUNTS =
      coreAsyncSharpLocalWitnessShapeCounts();
  private static final int CORE_ASYNC_DO_ALT_VEC_LOCAL_WITNESS_ROWS = 1;
  private static final String CORE_ASYNC_DO_ALT_VEC_LOCAL_WITNESS_SHAPE_SHA256 =
      "70a41200b61f380b691adb79b22c42fc37d79ef3adfa7c7f74e2280fbfa51098";
  private static final Pattern SINGLE_LOCAL_ID = Pattern.compile(
      "^((?:(?:inst|statearr|state|seq|i|count|chunk)_|" +
      "(?:tmp|fn|c|a|msg|ex|f|x_amz_content_sha)))(\\d+)$");
  private static final Pattern FIXED_MEMBER_SLOT = Pattern.compile("(?:const__\\d+|__cached_class__\\d+|__site__\\d+|__thunk__\\d+)");

  private static class Failure extends RuntimeException {
    Failure(String message) { super(message); }
  }

  private static final class IncompleteMapping extends Failure {
    IncompleteMapping(String message) { super(message); }
  }

  private static void require(boolean condition, String message) {
    if (!condition) throw new Failure(message);
  }

  private static Map<String, Integer> coreAsyncAutoLocalWitnessShapeCounts() {
    Map<String, Integer> result = new TreeMap<String, Integer>();
    String[] rows = {
      "04c31bda1b001dfeda726a0e924a8d2480693c1e62ef957e330ad7c45061d780:1",
      "115a0ed2dd84a6c33065a5fe5778c960ee7038342f965b65abc49ce3d26bcfa6:1",
      "13a050bf2522a26965a842472c72724216c050ec4b6c701335822c0d2ce6d270:1",
      "1547c82c8f266bde527dcead990d6d91a1fd3a2dd3326dafc4fc9319c232ef86:1",
      "181ec384710227798e796bfe37d852f77c334e77c8bfd588e735999ddd2bad2b:1",
      "1d0f8e02183376513eb360926d9a478ba712e05269e87b8e3348b1112defe302:12",
      "1ebe2e41102336c1fd06e7859620a77a9184ad807024f39eee142efd9d6d36a7:1",
      "2e2a688981c20aa2a4844a82fff2cdca96ea204ecc3acc4324000a949e34cd46:1",
      "2e76babea9b28a10fbb7df7c624f87d76d38c6ec3bea62484cfad7ccb3d87f7b:1",
      "40e5ca6c41080183284336a4649f9c4014920a65cc60b8edbb30da9650151505:1",
      "4511ffd5378c5b223d25533dd3cc9223a2675feacb24cabb6980c61f8e25c5e3:1",
      "55971922a847ca2d29744db368043e681c07f0dbab47e0295c0cd620e5226377:1",
      "55d4517c30fd8fd21b544c2f45d6df6d90f75f09b087e917f2b06e2ea5703e7c:1",
      "58d441a352a7e23d331a3b84846a3d7d12d037003e8625c80ab9b33d1bba2ad3:1",
      "58dd4a4ef1d40c96c21620a2387b22390860a7bf312351e3633085c1d5c5dab5:2",
      "58e7000597d9873d1d3a99f997da2b118bb973de572ba837a61c42928df8ac85:1",
      "5bc6de08f8e6e8a8f7770aaa25c455613beb8d7e9c7710838d8f80cddf45797a:1",
      "5cf01440b3ccf2c4bb3a8e012f04be1166323b63327295b12c5fb85abea1aa87:6",
      "701cfa0dbc7235773ec3e6ea09fd2aa61cdacb5210d5982858abe1cc15a696e3:1",
      "82860cf7df56b99055f60e7b9c91262018a404aa5d56f0fa232dd368c921d683:1",
      "86712861275836e6432b4419b97bb3bd8297a7a6f114720036755cce771e2ec4:6",
      "879762c266b90e683bf57b75101fe574d83006c11f5f1a33b34ff45d199b797f:4",
      "88304d4dd6a2f1c07992646f5cf89c2bebe1cf200105dc151c54d03650f072b6:1",
      "8a683ac2f6f9efd5f14aee633f4421505fc55018d509ca3e8ab4277c735efdf7:1",
      "93fc8e8183abac1e9393d67fb2abd48c5811f53747e2653fb2b0db088aff2cdc:1",
      "946fd195e49cfc616248ebbb46b14ef3790b58c4390ae586046c66f5d2918e08:1",
      "9da4227138acd74e76e492cb89e1cffae4d9608b422c986a19a444a1e66a849f:2",
      "9e4cbcdf0d6b93f9223c029d62c550f69d02eb0a809cb003f01fa394d06328ed:1",
      "9f8acd77e65d06e5d1d775ed45dfe1e7cc80a30b58e9f56cb2285b1643f56490:1",
      "a8c98bcf8f9a17be65e6943ae6080e9ae6e2f9e5c32f157eb0a7709be87b17e8:1",
      "aa359ae7b5f6ff3b6725da6ab89dd74a0a2a8d8cfd9a99b257cc697b0a6673a3:1",
      "acfe6bffdff88e7c1a2392e494c9ad566a57a5e28b225eed48ebe9ee4d7d5249:1",
      "b486826c7976be73dca452f56420d8cf30e99c0bf7f5ceb98ae3bc38724bb3d7:1",
      "b4c614b5b57e1b957617a3cf9c783bfdeffdb234f13b1a5993538b4cbc6d87fc:1",
      "b6b67d007efb627557f5f60280342f5fa0ea50308a59f3c79ca7ea8fdf632a81:1",
      "bdb1eb753ce48e964ebf58e919c13ee63b02d4bcdfa3c67e8ef4187d950bce02:1",
      "cab5227f59ff614beb98fafa5ccfc56a409a0e90316481e555fdc523e52010f3:1",
      "d5c2c681d146908e5da6998e9990c27b4f579ded484c0c1f708d984792bb47a9:6",
      "e267bce8e0fd28d5d66ecff20dc61250ba39119ca08ab08d976a8adb42d51d89:4",
      "e8449ad6a7027e2e9cda73d594aa9d6b59af5a3078430a405b59bcd7e6fde7bc:1",
      "eeb1d05352fd6adfb70e2b5ee4aeb618c710140ada8875517439bd56aaa1f203:1",
      "f93f66c2e3adff151730fc380140de3cb6a229e89064e3d4431fd2d12ac88610:1"
    };
    int total = 0;
    for (String row : rows) {
      int separator = row.lastIndexOf(':');
      String hash = row.substring(0, separator);
      int count = Integer.parseInt(row.substring(separator + 1));
      require(hash.matches("[0-9a-f]{64}") && count > 0,
          "invalid core.async auto-local witness-shape row");
      require(result.put(hash, count) == null,
          "duplicate core.async auto-local witness-shape hash: " + hash);
      total += count;
    }
    require(total == CORE_ASYNC_AUTO_LOCAL_WITNESS_ROWS,
        "core.async auto-local witness-shape counts do not close");
    return Collections.unmodifiableMap(result);
  }

  private static String sha256(String value) {
    try {
      byte[] digest = MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));
      StringBuilder result = new StringBuilder(64);
      for (byte item : digest) result.append(String.format(Locale.ROOT, "%02x", item & 0xff));
      return result.toString();
    } catch (NoSuchAlgorithmException impossible) {
      throw new Failure("SHA-256 is unavailable: " + impossible.getMessage());
    }
  }

  private static Map<String, Integer> coreAsyncSharpLocalWitnessShapeCounts() {
    Map<String, Integer> result = new TreeMap<String, Integer>();
    result.put("3aa0705632253eae7850616413cb3ee59b9a9e7795cf4e6e42d3c959b82f2653", 1);
    result.put("b644e3fede03f213b958687c0490548a1e703114fc914fed26c50814988b35fb", 1);
    result.put("c171a7153dda7b8da2515b309daae5085a5cc0b029c94118b4e0f8942dda1271", 1);
    result.put("d4a39ef692e23aeaa2225bb2901cde7112a643ca9174655bc048b74585b9224a", 1);
    result.put("dd440ce44509c18bf2430f1dff8b58c2308082ad65cb467142d09506f39e9c6b", 1);
    result.put("e62247fe62038e7034fa78ecebc642f31b2d2ab7eb6a0b2c3c571c5bb6bc6f12", 2);
    int total = result.values().stream().mapToInt(Integer::intValue).sum();
    require(total == CORE_ASYNC_SHARP_LOCAL_WITNESS_ROWS,
        "core.async sharp-local witness-shape counts do not close");
    return Collections.unmodifiableMap(result);
  }

  private static String q(String value) {
    if (value == null) return "-";
    String encoded = Base64.getEncoder().encodeToString(value.getBytes(StandardCharsets.UTF_8));
    return value.length() + ":" + encoded;
  }

  private static String join(Collection<String> values) {
    return String.join(",", values);
  }

  private static String typePath(TypePath path) {
    return path == null ? "-" : path.toString();
  }

  private static final class AnnotationNode extends AnnotationVisitor {
    final String prefix;
    final List<String> values = new ArrayList<String>();
    final Consumer<String> sink;

    AnnotationNode(String prefix, Consumer<String> sink) {
      super(ASM);
      this.prefix = prefix;
      this.sink = sink;
    }

    @Override public void visit(String name, Object value) {
      values.add("v(" + q(name) + "," + rawValue(value) + ")");
    }

    @Override public void visitEnum(String name, String descriptor, String value) {
      values.add("e(" + q(name) + "," + q(descriptor) + "," + q(value) + ")");
    }

    @Override public AnnotationVisitor visitAnnotation(String name, String descriptor) {
      final int index = values.size();
      values.add(null);
      return new AnnotationNode("a(" + q(name) + "," + q(descriptor) + ")", rendered -> values.set(index, rendered));
    }

    @Override public AnnotationVisitor visitArray(String name) {
      final int index = values.size();
      values.add(null);
      return new AnnotationNode("r(" + q(name) + ")", rendered -> values.set(index, rendered));
    }

    @Override public void visitEnd() {
      require(!values.contains(null), "annotation visitor ended with an incomplete nested value");
      sink.accept(prefix + "[" + join(values) + "]");
    }
  }

  private static String rawValue(Object value) {
    if (value == null) return "null";
    if (value instanceof Float) return "java.lang.Float:raw=0x" + Integer.toHexString(Float.floatToRawIntBits((Float) value));
    if (value instanceof Double) return "java.lang.Double:raw=0x" + Long.toHexString(Double.doubleToRawLongBits((Double) value));
    if (value instanceof Type) return "type:" + q(((Type) value).getDescriptor());
    if (value instanceof Handle) {
      Handle h = (Handle) value;
      return "handle:" + h.getTag() + ":" + q(h.getOwner()) + ":" + q(h.getName()) + ":" + q(h.getDesc()) + ":" + h.isInterface();
    }
    if (value instanceof ConstantDynamic) {
      ConstantDynamic c = (ConstantDynamic) value;
      List<String> args = new ArrayList<String>();
      for (int i = 0; i < c.getBootstrapMethodArgumentCount(); i++) args.add(rawValue(c.getBootstrapMethodArgument(i)));
      return "condy:" + q(c.getName()) + ":" + q(c.getDescriptor()) + ":" + rawValue(c.getBootstrapMethod()) + ":[" + join(args) + "]";
    }
    Class<?> c = value.getClass();
    if (c.isArray()) {
      List<String> items = new ArrayList<String>();
      for (int i = 0; i < Array.getLength(value); i++) items.add(rawValue(Array.get(value, i)));
      return "array:" + c.getComponentType().getName() + ":[" + join(items) + "]";
    }
    return c.getName() + ":" + q(String.valueOf(value));
  }

  private static final class Insn {
    final String kind;
    final int opcode;
    final List<Object> args;

    Insn(String kind, int opcode, Object... args) {
      this.kind = kind;
      this.opcode = opcode;
      this.args = Arrays.asList(args);
    }

    boolean is(String expectedKind, int expectedOpcode) {
      return kind.equals(expectedKind) && opcode == expectedOpcode;
    }

    String owner() { return (String) args.get(0); }
    String memberName() { return (String) args.get(1); }
    String descriptor() { return (String) args.get(2); }

    String render(Normalizer n, String namespace) {
      List<String> rendered = new ArrayList<String>();
      for (int i = 0; i < args.size(); i++) {
        Object arg = args.get(i);
        if (arg instanceof LabelRef) rendered.add("L" + ((LabelRef) arg).id);
        else if (arg instanceof int[]) rendered.add(Arrays.toString((int[]) arg));
        else if (arg instanceof LabelRef[]) {
          List<String> labels = new ArrayList<String>();
          for (LabelRef label : (LabelRef[]) arg) labels.add("L" + label.id);
          rendered.add("[" + join(labels) + "]");
        } else if ((kind.equals("VAR") || kind.equals("IINC")) && i == 0) {
          rendered.add(n.localInstruction(this, (Integer) arg));
        } else if (kind.equals("FIELD") || kind.equals("METHOD")) {
          if (i == 0) rendered.add(q(n.internal((String) arg, namespace)));
          else if (i == 1) rendered.add(q(n.memberName((String) args.get(0), kind.equals("FIELD") ? "F" : "M",
              (String) arg, (String) args.get(2), namespace)));
          else if (i == 2) rendered.add(q(n.descriptor((String) arg, namespace)));
          else rendered.add(String.valueOf(arg));
        } else if (kind.equals("TYPE")) {
          rendered.add(q(n.typeOperand((String) arg, namespace)));
        } else if (kind.equals("MULTIANEWARRAY")) {
          if (i == 0) rendered.add(q(n.descriptor((String) arg, namespace)));
          else rendered.add(String.valueOf(arg));
        } else if (kind.equals("FRAME")) {
          if (arg instanceof Object[]) rendered.add(n.frameValues((Object[]) arg, namespace));
          else rendered.add(String.valueOf(arg));
        } else if (kind.equals("LDC")) {
          rendered.add(n.value(arg, namespace));
        } else if (kind.equals("INDY")) {
          if (i == 0) rendered.add(q((String) arg));
          else if (i == 1) rendered.add(q(n.descriptor((String) arg, namespace)));
          else rendered.add(n.value(arg, namespace));
        } else if (arg instanceof String) {
          rendered.add(q(n.known((String) arg, namespace)));
        } else {
          rendered.add(String.valueOf(arg));
        }
      }
      return kind + "(" + opcode + (rendered.isEmpty() ? "" : "," + join(rendered)) + ")";
    }
  }

  private static final class LabelRef {
    final int id;
    LabelRef(int id) { this.id = id; }
  }

  private static final class FieldModel {
    int access;
    String name;
    String descriptor;
    String signature;
    Object value;
    final List<String> events = new ArrayList<String>();

    String render(Normalizer n, String namespace, String owner) {
      List<String> normalizedEvents = new ArrayList<String>();
      for (String event : events) normalizedEvents.add(n.known(event, namespace));
      return "FIELD{" + access + "," + q(n.memberName(owner, "F", name, descriptor, namespace)) + "," + q(n.descriptor(descriptor, namespace)) + "," +
          q(n.signature(signature, namespace)) + "," + n.value(value, namespace) + ",[" + join(normalizedEvents) + "]}";
    }
  }

  private static final class MethodModel {
    int access;
    String name;
    String descriptor;
    String signature;
    List<String> exceptions = new ArrayList<String>();
    final List<String> events = new ArrayList<String>();
    final List<String> codeEvents = new ArrayList<String>();
    final List<Insn> insns = new ArrayList<Insn>();
    final List<TryCatchModel> tryCatches = new ArrayList<TryCatchModel>();
    final List<LocalVariableModel> localVariables = new ArrayList<LocalVariableModel>();
    int maxStack = -1;
    int maxLocals = -1;

    String key() { return name + "\u0000" + descriptor; }
  }

  private static final class LocalVariableModel {
    final String name;
    final String descriptor;
    final String signature;
    final LabelRef start;
    final LabelRef end;
    final int index;
    LocalVariableModel(String name, String descriptor, String signature, LabelRef start, LabelRef end, int index) {
      this.name = name; this.descriptor = descriptor; this.signature = signature;
      this.start = start; this.end = end; this.index = index;
    }
  }

  private static final class TryCatchModel {
    final LabelRef start;
    final LabelRef end;
    final LabelRef handler;
    final String type;
    TryCatchModel(LabelRef start, LabelRef end, LabelRef handler, String type) {
      this.start = start; this.end = end; this.handler = handler; this.type = type;
    }
  }

  private static int controlRegionLabelPosition(MethodModel method, LabelRef label) {
    Set<Integer> controlLabels = stateControlLabels(method);
    int region = 0;
    for (Insn instruction : method.insns) {
      if (!instruction.kind.equals("LABEL")) continue;
      int current = ((LabelRef) instruction.args.get(0)).id;
      if (controlLabels.contains(current)) region++;
      if (current == label.id) return region;
    }
    throw new Failure("debug/control label is absent from its method instruction stream: L" + label.id);
  }

  private static final class ClassModel {
    String namespace;
    String sourceEntry;
    String sourceSha256;
    String sourceOwnerJar;
    String sourceOwnerSha256;
    int version;
    int access;
    String name;
    String signature;
    String superName;
    List<String> interfaces = new ArrayList<String>();
    final List<String> events = new ArrayList<String>();
    final List<FieldModel> fields = new ArrayList<FieldModel>();
    final List<MethodModel> methods = new ArrayList<MethodModel>();
    final List<String> unsupported = new ArrayList<String>();
    Map<String, String> varBindings;
  }

  private static final class StateSlotKey implements Comparable<StateSlotKey> {
    final String machineOwner;
    final long index;
    StateSlotKey(String machineOwner, long index) { this.machineOwner = machineOwner; this.index = index; }
    @Override public boolean equals(Object other) {
      return other instanceof StateSlotKey && machineOwner.equals(((StateSlotKey) other).machineOwner) && index == ((StateSlotKey) other).index;
    }
    @Override public int hashCode() { return Objects.hash(machineOwner, index); }
    @Override public int compareTo(StateSlotKey other) {
      int c = machineOwner.compareTo(other.machineOwner); return c != 0 ? c : Long.compare(index, other.index);
    }
    @Override public String toString() { return machineOwner + "#" + index; }
  }

  private static final String IOC_MACROS_NS = "clojure.core.async.impl.ioc-macros";
  private static final String AGET_OBJECT = IOC_MACROS_NS + "/aget-object";
  private static final String ASET_OBJECT = IOC_MACROS_NS + "/aset-object";

  private static final class StateSlotAccess {
    final StateSlotKey key;
    final String kind;
    final ClassModel owner;
    final MethodModel method;
    final Insn slotInsn;
    final MemberKey stateField;

    StateSlotAccess(StateSlotKey key, String kind, ClassModel owner, MethodModel method,
                    Insn slotInsn, MemberKey stateField) {
      this.key = key; this.kind = kind; this.owner = owner; this.method = method;
      this.slotInsn = slotInsn; this.stateField = stateField;
    }
  }

  private static final class StateSlotUniverse {
    final Set<StateSlotKey> nodes = new TreeSet<StateSlotKey>();
    final Map<Insn, StateSlotAccess> byInstruction = new IdentityHashMap<Insn, StateSlotAccess>();
    final Map<StateSlotKey, List<StateSlotAccess>> byNode = new TreeMap<StateSlotKey, List<StateSlotAccess>>();

    void add(StateSlotAccess access) {
      require(byInstruction.put(access.slotInsn, access) == null,
          "one instruction was classified as more than one state-slot access in " + access.owner.name);
      nodes.add(access.key);
      byNode.computeIfAbsent(access.key, ignored -> new ArrayList<StateSlotAccess>()).add(access);
    }

    StateSlotKey key(Insn instruction) {
      StateSlotAccess access = byInstruction.get(instruction);
      return access == null ? null : access.key;
    }
  }

  private static final class LocalNodeKey implements Comparable<LocalNodeKey> {
    final String owner;
    final String methodName;
    final String methodDescriptor;
    final int ordinal;
    final String category;
    LocalNodeKey(String owner, String methodName, String methodDescriptor, int ordinal, String category) {
      this.owner = owner; this.methodName = methodName; this.methodDescriptor = methodDescriptor;
      this.ordinal = ordinal; this.category = category;
    }
    @Override public boolean equals(Object other) {
      if (!(other instanceof LocalNodeKey)) return false;
      LocalNodeKey key = (LocalNodeKey) other;
      return owner.equals(key.owner) && methodName.equals(key.methodName) &&
          methodDescriptor.equals(key.methodDescriptor) && ordinal == key.ordinal && category.equals(key.category);
    }
    @Override public int hashCode() { return Objects.hash(owner, methodName, methodDescriptor, ordinal, category); }
    @Override public int compareTo(LocalNodeKey other) {
      int c = owner.compareTo(other.owner); if (c != 0) return c;
      c = methodName.compareTo(other.methodName); if (c != 0) return c;
      c = methodDescriptor.compareTo(other.methodDescriptor); if (c != 0) return c;
      c = Integer.compare(ordinal, other.ordinal); if (c != 0) return c;
      return category.compareTo(other.category);
    }
    @Override public String toString() {
      return owner + "." + methodName + methodDescriptor + "#local-" + ordinal + ":" + category;
    }
  }

  private static final class LocalNodeInfo {
    final LocalNodeKey key;
    final ClassModel owner;
    final MethodModel method;
    final boolean boundary;
    final Set<Integer> physicalSlots;
    int instructionOccurrences;
    int frameOccurrences;
    int lvtOccurrences;
    LocalNodeInfo(LocalNodeKey key, ClassModel owner, MethodModel method,
                  boolean boundary, Set<Integer> physicalSlots) {
      this.key = key; this.owner = owner; this.method = method;
      this.boundary = boundary; this.physicalSlots = physicalSlots;
    }
  }

  private static final class LocalUniverse {
    final Set<LocalNodeKey> generated = new TreeSet<LocalNodeKey>();
    final Map<LocalNodeKey, LocalNodeInfo> info = new TreeMap<LocalNodeKey, LocalNodeInfo>();
    final Map<Insn, LocalNodeKey> instructionNodes = new IdentityHashMap<Insn, LocalNodeKey>();
    final Map<Insn, Map<Integer, LocalNodeKey>> frameNodes = new IdentityHashMap<Insn, Map<Integer, LocalNodeKey>>();
    final Map<LocalVariableModel, LocalNodeKey> variableNodes = new IdentityHashMap<LocalVariableModel, LocalNodeKey>();
    final Map<MethodModel, Set<LocalNodeKey>> methodNodes = new IdentityHashMap<MethodModel, Set<LocalNodeKey>>();
    final Set<String> nonLvtCompilerIds = new TreeSet<String>(numericComparator());
    final Map<String, Integer> autoLocalSuffixOccurrences = new TreeMap<String, Integer>(numericComparator());
    final Map<String, Integer> lvtCompilerIdOccurrences = new TreeMap<String, Integer>(numericComparator());
    boolean coreAsyncAutoLocalInventoryClosed;
    boolean coreAsyncSharpLocalInventoryClosed;
    boolean coreAsyncDoAltVecLocalInventoryClosed;

    LocalNodeKey instruction(Insn instruction) { return instructionNodes.get(instruction); }

    static LocalUniverse of(Map<String, ClassModel> classes, StateSlotUniverse slots) {
      LocalUniverse result = new LocalUniverse();
      for (ClassModel owner : classes.values()) for (MethodModel method : owner.methods) {
        analyzeLocalMethod(owner, method, result);
      }
      result.nonLvtCompilerIds.addAll(nonLvtCompilerIds(classes.values()));
      for (ClassModel owner : classes.values()) for (MethodModel method : owner.methods)
        for (LocalVariableModel variable : method.localVariables) {
          for (String id : ids(variable.name))
            result.lvtCompilerIdOccurrences.merge(id, 1, Integer::sum);
          Matcher matcher = AUTO_LOCAL_TERMINAL_UNIQUIFIER.matcher(variable.name);
          if (matcher.matches())
            result.autoLocalSuffixOccurrences.merge(matcher.group(2), 1, Integer::sum);
        }
      result.coreAsyncAutoLocalInventoryClosed =
          coreAsyncAutoLocalInventoryClosed(result, classes.values());
      result.coreAsyncSharpLocalInventoryClosed =
          coreAsyncSharpLocalInventoryClosed(result, classes.values());
      result.coreAsyncDoAltVecLocalInventoryClosed =
          coreAsyncDoAltVecLocalInventoryClosed(result, classes.values());
      return result;
    }
  }

  private static final class LocalDef {
    final int id;
    final int slot;
    final String category;
    final boolean boundary;
    LocalDef(int id, int slot, String category, boolean boundary) {
      this.id = id; this.slot = slot; this.category = category; this.boundary = boundary;
    }
  }

  private static final class LocalUnionFind {
    final List<Integer> parent = new ArrayList<Integer>();
    int add() { int id = parent.size(); parent.add(id); return id; }
    int find(int value) {
      int root = value;
      while (parent.get(root) != root) root = parent.get(root);
      while (parent.get(value) != value) { int next = parent.get(value); parent.set(value, root); value = next; }
      return root;
    }
    void union(int a, int b) {
      int x = find(a), y = find(b); if (x == y) return;
      if (x < y) parent.set(y, x); else parent.set(x, y);
    }
  }

  private static String localCategoryForOpcode(int opcode) {
    if (opcode == Opcodes.ALOAD || opcode == Opcodes.ASTORE) return "A";
    if (opcode == Opcodes.ILOAD || opcode == Opcodes.ISTORE || opcode == Opcodes.IINC) return "I";
    if (opcode == Opcodes.FLOAD || opcode == Opcodes.FSTORE) return "F";
    if (opcode == Opcodes.LLOAD || opcode == Opcodes.LSTORE) return "J";
    if (opcode == Opcodes.DLOAD || opcode == Opcodes.DSTORE) return "D";
    return null;
  }

  private static String localCategory(Type type) {
    if (type.getSort() == Type.OBJECT || type.getSort() == Type.ARRAY) return "A";
    if (type.getSort() == Type.LONG) return "J";
    if (type.getSort() == Type.DOUBLE) return "D";
    if (type.getSort() == Type.FLOAT) return "F";
    return "I";
  }

  private static boolean localStoreInsn(Insn instruction) {
    return instruction.kind.equals("VAR") && instruction.opcode >= Opcodes.ISTORE &&
        instruction.opcode <= Opcodes.ASTORE;
  }

  private static boolean localLoadInsn(Insn instruction) {
    return instruction.kind.equals("VAR") && instruction.opcode >= Opcodes.ILOAD &&
        instruction.opcode <= Opcodes.ALOAD;
  }

  private static Map<Integer, Set<Integer>> copyLocalState(Map<Integer, Set<Integer>> source) {
    Map<Integer, Set<Integer>> result = new TreeMap<Integer, Set<Integer>>();
    for (Map.Entry<Integer, Set<Integer>> entry : source.entrySet())
      result.put(entry.getKey(), new TreeSet<Integer>(entry.getValue()));
    return result;
  }

  private static void mergeLocalState(Map<Integer, Set<Integer>> target,
                                      Map<Integer, Set<Integer>> source) {
    for (Map.Entry<Integer, Set<Integer>> entry : source.entrySet())
      target.computeIfAbsent(entry.getKey(), ignored -> new TreeSet<Integer>()).addAll(entry.getValue());
  }

  private static String frameCategory(Object value) {
    if (value instanceof String) return "A";
    if (value == Opcodes.LONG) return "J";
    if (value == Opcodes.DOUBLE) return "D";
    if (value == Opcodes.FLOAT) return "F";
    if (value == Opcodes.INTEGER) return "I";
    if (value instanceof LabelRef) return "A";
    return null;
  }

  private static int frameWidth(Object value) {
    return value == Opcodes.LONG || value == Opcodes.DOUBLE ? 2 : 1;
  }

  private static void analyzeLocalMethod(ClassModel owner, MethodModel method, LocalUniverse target) {
    /* statePredecessors includes conservative protected-range-to-handler
     * edges.  That may merge same-category webs and thereby make the quotient
     * stricter, but cannot invent a missing reaching definition.  Local
     * variable type annotations remain exact until their index arrays are
     * represented structurally. */
    for (String event : method.codeEvents) if (event.startsWith("LVANNOT(")) return;
    List<Insn> code = method.insns;
    if (code.isEmpty()) return;
    LocalUnionFind unions = new LocalUnionFind();
    List<LocalDef> defs = new ArrayList<LocalDef>();
    Map<Integer, Integer> entryDefs = new TreeMap<Integer, Integer>();
    Map<Insn, Integer> storeDefs = new IdentityHashMap<Insn, Integer>();
    Map<Insn, Set<Integer>> occurrenceDefs = new IdentityHashMap<Insn, Set<Integer>>();
    Consumer<LocalDef> addDef = def -> {
      require(def.id == defs.size() && def.id == unions.add(), "local definition numbering diverged");
      defs.add(def);
    };

    int parameterSlot = 0;
    if ((method.access & Opcodes.ACC_STATIC) == 0) {
      LocalDef self = new LocalDef(defs.size(), 0, "A", true); addDef.accept(self); entryDefs.put(0, self.id);
      parameterSlot = 1;
    }
    for (Type type : Type.getArgumentTypes(method.descriptor)) {
      LocalDef parameter = new LocalDef(defs.size(), parameterSlot, localCategory(type), true);
      addDef.accept(parameter); entryDefs.put(parameterSlot, parameter.id); parameterSlot += type.getSize();
    }
    for (Insn instruction : code) {
      if (localStoreInsn(instruction) || instruction.kind.equals("IINC")) {
        int slot = (Integer) instruction.args.get(0);
        String category = localCategoryForOpcode(instruction.opcode);
        if (category == null) return;
        LocalDef def = new LocalDef(defs.size(), slot, category, false); addDef.accept(def);
        storeDefs.put(instruction, def.id);
      } else if (instruction.kind.equals("VAR") && !localLoadInsn(instruction)) {
        return;
      }
    }

    List<Set<Integer>> predecessors = statePredecessors(method);
    List<Map<Integer, Set<Integer>>> incoming = new ArrayList<Map<Integer, Set<Integer>>>();
    List<Map<Integer, Set<Integer>>> outgoing = new ArrayList<Map<Integer, Set<Integer>>>();
    for (int i = 0; i < code.size(); i++) { incoming.add(new TreeMap<Integer, Set<Integer>>()); outgoing.add(new TreeMap<Integer, Set<Integer>>()); }
    int iterations = 0;
    boolean changed;
    do {
      require(++iterations <= Math.max(1000, code.size() * 20), "local reaching-definition analysis did not converge in " + owner.name + "." + method.key());
      changed = false;
      for (int i = 0; i < code.size(); i++) {
        Map<Integer, Set<Integer>> nextIn = new TreeMap<Integer, Set<Integer>>();
        if (i == 0) for (Map.Entry<Integer, Integer> entry : entryDefs.entrySet())
          nextIn.put(entry.getKey(), new TreeSet<Integer>(Collections.singleton(entry.getValue())));
        for (int predecessor : predecessors.get(i)) mergeLocalState(nextIn, outgoing.get(predecessor));
        Map<Integer, Set<Integer>> nextOut = copyLocalState(nextIn);
        Insn instruction = code.get(i);
        Integer def = storeDefs.get(instruction);
        if (def != null) nextOut.put((Integer) instruction.args.get(0),
            new TreeSet<Integer>(Collections.singleton(def)));
        if (!nextIn.equals(incoming.get(i))) { incoming.set(i, nextIn); changed = true; }
        if (!nextOut.equals(outgoing.get(i))) { outgoing.set(i, nextOut); changed = true; }
      }
    } while (changed);

    boolean unsafe = false;
    for (int i = 0; i < code.size(); i++) {
      Insn instruction = code.get(i);
      if (localLoadInsn(instruction) || instruction.kind.equals("IINC")) {
        int slot = (Integer) instruction.args.get(0);
        Set<Integer> reaching = incoming.get(i).get(slot);
        if (reaching == null || reaching.isEmpty()) { unsafe = true; break; }
        String expected = localCategoryForOpcode(instruction.opcode);
        for (int def : reaching) {
          if (!defs.get(def).category.equals(expected)) { unsafe = true; break; }
          unions.union(reaching.iterator().next(), def);
        }
        if (unsafe) break;
        Set<Integer> associated = new TreeSet<Integer>(reaching);
        Integer ownDef = storeDefs.get(instruction);
        if (ownDef != null) { for (int def : reaching) unions.union(ownDef, def); associated.add(ownDef); }
        occurrenceDefs.put(instruction, associated);
      } else if (localStoreInsn(instruction)) {
        occurrenceDefs.put(instruction, new TreeSet<Integer>(Collections.singleton(storeDefs.get(instruction))));
      }
    }
    if (unsafe) return;

    Map<Integer, Integer> labelIndex = new HashMap<Integer, Integer>();
    for (int i = 0; i < code.size(); i++) if (code.get(i).kind.equals("LABEL"))
      labelIndex.put(((LabelRef) code.get(i).args.get(0)).id, i);
    Map<LocalVariableModel, Integer> variableDef = new IdentityHashMap<LocalVariableModel, Integer>();
    for (LocalVariableModel variable : method.localVariables) {
      Integer start = labelIndex.get(variable.start.id), end = labelIndex.get(variable.end.id);
      if (start == null || end == null || start > end) return;
      String category = localCategory(Type.getType(variable.descriptor));
      Set<Integer> linked = new TreeSet<Integer>();
      for (int i = start; i < end; i++) {
        Insn instruction = code.get(i);
        if ((localLoadInsn(instruction) || localStoreInsn(instruction) || instruction.kind.equals("IINC")) &&
            instruction.args.get(0).equals(variable.index)) linked.addAll(occurrenceDefs.get(instruction));
      }
      /* Actual typed occurrences are the authoritative live-range anchors.
       * A metadata-only LVT row has no executable use to bind.  Give it a
       * typed synthetic node rather than importing unrelated definitions that
       * a state-machine loop can carry through the range's start label. */
      if (linked.isEmpty()) {
        LocalDef synthetic = new LocalDef(defs.size(), variable.index, category, false); addDef.accept(synthetic);
        linked.add(synthetic.id);
      }
      int first = linked.iterator().next();
      for (int def : linked) {
        if (!defs.get(def).category.equals(category)) return;
        unions.union(first, def);
      }
      variableDef.put(variable, first);
    }

    Map<Insn, Map<Integer, Integer>> frameDefs = new IdentityHashMap<Insn, Map<Integer, Integer>>();
    for (int i = 0; i < code.size(); i++) {
      Insn instruction = code.get(i);
      if (!instruction.kind.equals("FRAME")) continue;
      Object[] locals = (Object[]) instruction.args.get(1);
      int physical = 0;
      Map<Integer, Integer> cells = new TreeMap<Integer, Integer>();
      for (Object value : locals) {
        String category = frameCategory(value);
        if (category != null) {
          Set<Integer> linked = incoming.get(i).get(physical);
          int representative;
          if (linked == null || linked.isEmpty()) {
            LocalDef synthetic = new LocalDef(defs.size(), physical, category, false); addDef.accept(synthetic);
            representative = synthetic.id;
          } else {
            representative = linked.iterator().next();
            for (int def : linked) {
              if (!defs.get(def).category.equals(category)) return;
              unions.union(representative, def);
            }
          }
          cells.put(physical, representative);
        }
        physical += frameWidth(value);
      }
      frameDefs.put(instruction, cells);
    }

    Map<Integer, List<LocalDef>> groups = new TreeMap<Integer, List<LocalDef>>();
    for (LocalDef def : defs) groups.computeIfAbsent(unions.find(def.id), ignored -> new ArrayList<LocalDef>()).add(def);
    List<Integer> roots = new ArrayList<Integer>(groups.keySet());
    Collections.sort(roots, (a, b) -> Integer.compare(
        groups.get(a).stream().mapToInt(def -> def.id).min().orElse(Integer.MAX_VALUE),
        groups.get(b).stream().mapToInt(def -> def.id).min().orElse(Integer.MAX_VALUE)));
    Map<Integer, LocalNodeKey> keyByRoot = new HashMap<Integer, LocalNodeKey>();
    int ordinal = 0;
    for (int root : roots) {
      List<LocalDef> group = groups.get(root);
      String category = group.get(0).category;
      boolean boundary = false;
      Set<Integer> physical = new TreeSet<Integer>();
      for (LocalDef def : group) {
        require(def.category.equals(category), "local web combines incompatible categories in " + owner.name + "." + method.key());
        boundary |= def.boundary; physical.add(def.slot);
      }
      LocalNodeKey key = new LocalNodeKey(owner.name, method.name, method.descriptor, ordinal++, category);
      keyByRoot.put(root, key);
      LocalNodeInfo info = new LocalNodeInfo(key, owner, method, boundary, physical);
      target.info.put(key, info);
      target.methodNodes.computeIfAbsent(method, ignored -> new TreeSet<LocalNodeKey>()).add(key);
      if (!boundary) target.generated.add(key);
    }
    for (Map.Entry<Insn, Set<Integer>> entry : occurrenceDefs.entrySet()) {
      int root = unions.find(entry.getValue().iterator().next());
      for (int def : entry.getValue()) require(unions.find(def) == root, "local occurrence spans more than one web");
      LocalNodeKey key = keyByRoot.get(root);
      target.instructionNodes.put(entry.getKey(), key);
      target.info.get(key).instructionOccurrences++;
    }
    for (Map.Entry<LocalVariableModel, Integer> entry : variableDef.entrySet()) {
      LocalNodeKey key = keyByRoot.get(unions.find(entry.getValue()));
      target.variableNodes.put(entry.getKey(), key);
      target.info.get(key).lvtOccurrences++;
    }
    for (Map.Entry<Insn, Map<Integer, Integer>> entry : frameDefs.entrySet()) {
      Map<Integer, LocalNodeKey> cells = new TreeMap<Integer, LocalNodeKey>();
      for (Map.Entry<Integer, Integer> cell : entry.getValue().entrySet()) {
        LocalNodeKey key = keyByRoot.get(unions.find(cell.getValue()));
        cells.put(cell.getKey(), key);
        target.info.get(key).frameOccurrences++;
      }
      target.frameNodes.put(entry.getKey(), cells);
    }
  }

  private static final class MemberKey implements Comparable<MemberKey> {
    final String owner;
    final String kind;
    final String name;
    final String descriptor;
    MemberKey(String owner, String kind, String name, String descriptor) {
      this.owner = owner; this.kind = kind; this.name = name; this.descriptor = descriptor;
    }
    @Override public boolean equals(Object other) {
      if (!(other instanceof MemberKey)) return false;
      MemberKey key = (MemberKey) other;
      return owner.equals(key.owner) && kind.equals(key.kind) && name.equals(key.name) && descriptor.equals(key.descriptor);
    }
    @Override public int hashCode() { return Objects.hash(owner, kind, name, descriptor); }
    @Override public int compareTo(MemberKey other) {
      int c = owner.compareTo(other.owner); if (c != 0) return c;
      c = kind.compareTo(other.kind); if (c != 0) return c;
      c = name.compareTo(other.name); if (c != 0) return c;
      return descriptor.compareTo(other.descriptor);
    }
    @Override public String toString() { return owner + "." + kind + "." + name + descriptor; }
  }

  private static final class MemberUniverse {
    final Set<MemberKey> generated = new TreeSet<MemberKey>();
    static MemberUniverse of(Map<String, ClassModel> classes) {
      MemberUniverse universe = new MemberUniverse();
      /* Spelling is not evidence.  A node is eligible only when its bytecode
       * proves the closed captured-field role: a trivial constructor assigns
       * every argument exactly once to every instance field.  Public/protected
       * fields remain boundary-visible and exact. */
      for (CaptureSpec spec : captureSpecs(classes).values()) for (FieldModel field : spec.fieldsByArgument)
        if (eligibleGeneratedMember(field.access, field.name))
          universe.generated.add(new MemberKey(spec.owner.name, "F", field.name, field.descriptor));
      return universe;
    }
    boolean contains(String owner, String kind, String name, String descriptor) {
      return generated.contains(new MemberKey(owner, kind, name, descriptor));
    }
  }

  private static boolean eligibleGeneratedMember(int access, String name) {
    return (access & (Opcodes.ACC_PUBLIC | Opcodes.ACC_PROTECTED)) == 0 &&
        !FIXED_MEMBER_SLOT.matcher(name).matches();
  }

  private static String generatedMemberSkeleton(String name) {
    return FIXED_MEMBER_SLOT.matcher(name).matches() ? name : "<CAPTURE_MEMBER>";
  }

  private static String stateMachineOwner(String className) {
    Matcher matcher = STATE_MACHINE_OWNER.matcher(className);
    return matcher.matches() ? matcher.group(1) : null;
  }

  private static Long pushedLong(Insn instruction) {
    if (instruction.is("INSN", Opcodes.LCONST_0)) return 0L;
    if (instruction.is("INSN", Opcodes.LCONST_1)) return 1L;
    if (instruction.kind.equals("LDC") && instruction.args.get(0) instanceof Long)
      return (Long) instruction.args.get(0);
    return null;
  }

  private static Map<MemberKey, String> varBindings(Map<String, ClassModel> classes) {
    Map<MemberKey, String> result = new TreeMap<MemberKey, String>();
    for (ClassModel owner : classes.values()) {
      Map<String, String> local = new TreeMap<String, String>();
      for (MethodModel method : owner.methods) {
        if (!method.name.equals("<clinit>") || !method.descriptor.equals("()V")) continue;
        List<Insn> code = method.insns;
        for (int i = 0; i + 4 < code.size(); i++) {
          Insn namespace = code.get(i), name = code.get(i + 1), call = code.get(i + 2);
          Insn cast = code.get(i + 3), store = code.get(i + 4);
          if (!namespace.kind.equals("LDC") || !(namespace.args.get(0) instanceof String) ||
              !name.kind.equals("LDC") || !(name.args.get(0) instanceof String) ||
              !call.is("METHOD", Opcodes.INVOKESTATIC) || !call.owner().equals("clojure/lang/RT") ||
              !call.memberName().equals("var") ||
              !call.descriptor().equals("(Ljava/lang/String;Ljava/lang/String;)Lclojure/lang/Var;") ||
              !cast.is("TYPE", Opcodes.CHECKCAST) || !cast.args.get(0).equals("clojure/lang/Var") ||
              !store.is("FIELD", Opcodes.PUTSTATIC) || !store.owner().equals(owner.name) ||
              !store.descriptor().equals("Lclojure/lang/Var;")) continue;
          FieldModel declared = null;
          for (FieldModel candidate : owner.fields)
            if (candidate.name.equals(store.memberName()) && candidate.descriptor.equals(store.descriptor()))
              declared = candidate;
          require(declared != null &&
                  (declared.access & (Opcodes.ACC_STATIC | Opcodes.ACC_FINAL)) ==
                      (Opcodes.ACC_STATIC | Opcodes.ACC_FINAL) &&
                  (declared.access & Opcodes.ACC_VOLATILE) == 0 && declared.value == null,
              "constant Var binding target is not an own static-final nonvolatile field: " +
              owner.name + "." + store.memberName());
          String fieldKey = store.memberName() + "\u0000" + store.descriptor();
          String target = namespace.args.get(0) + "/" + name.args.get(0);
          String old = local.put(fieldKey, target);
          require(old == null || old.equals(target), "conflicting constant Var binding in " + owner.name + "." + store.memberName());
          MemberKey key = new MemberKey(owner.name, "F", store.memberName(), store.descriptor());
          String globalOld = result.put(key, target);
          require(globalOld == null || globalOld.equals(target), "conflicting Var field binding: " + key);
        }
      }
      owner.varBindings = Collections.unmodifiableMap(local);
    }
    return result;
  }

  private static boolean stateControlBoundary(Insn instruction) {
    return instruction.kind.equals("LABEL") || instruction.kind.equals("FRAME") ||
        instruction.kind.equals("JUMP") || instruction.kind.equals("TABLESWITCH") ||
        instruction.kind.equals("LOOKUPSWITCH");
  }

  private static Set<Integer> stateControlLabels(MethodModel method) {
    Set<Integer> result = new HashSet<Integer>();
    for (Insn instruction : method.insns) {
      if (instruction.kind.equals("JUMP"))
        result.add(((LabelRef) instruction.args.get(0)).id);
      else if (instruction.kind.equals("TABLESWITCH")) {
        result.add(((LabelRef) instruction.args.get(2)).id);
        for (LabelRef label : (LabelRef[]) instruction.args.get(3)) result.add(label.id);
      } else if (instruction.kind.equals("LOOKUPSWITCH")) {
        result.add(((LabelRef) instruction.args.get(0)).id);
        for (LabelRef label : (LabelRef[]) instruction.args.get(2)) result.add(label.id);
      } else if (instruction.kind.equals("FRAME")) {
        for (Object value : (Object[]) instruction.args.get(1))
          if (value instanceof LabelRef) result.add(((LabelRef) value).id);
        for (Object value : (Object[]) instruction.args.get(2))
          if (value instanceof LabelRef) result.add(((LabelRef) value).id);
      }
    }
    for (TryCatchModel block : method.tryCatches) {
      result.add(block.start.id); result.add(block.end.id);
      if (block.handler != null) result.add(block.handler.id);
    }
    /* Local-variable type annotations carry structured label ranges in a
     * code attribute.  Until those ranges have their own typed quotient, every
     * label in such a method is a semantic anchor and must remain exact. */
    boolean typedLocalRange = false;
    for (String event : method.codeEvents) if (event.startsWith("LVANNOT(")) typedLocalRange = true;
    if (typedLocalRange) for (Insn instruction : method.insns)
      if (instruction.kind.equals("LABEL")) result.add(((LabelRef) instruction.args.get(0)).id);
    return result;
  }

  private static int previousStateInstruction(MethodModel method, int before,
                                              Set<Integer> controlLabels) {
    for (int i = before - 1; i >= 0; i--) {
      Insn instruction = method.insns.get(i);
      if (instruction.kind.equals("LABEL") &&
          !controlLabels.contains(((LabelRef) instruction.args.get(0)).id)) continue;
      return i;
    }
    return -1;
  }

  private static int nextStateInstruction(MethodModel method, int after,
                                          Set<Integer> controlLabels) {
    for (int i = after + 1; i < method.insns.size(); i++) {
      Insn instruction = method.insns.get(i);
      if (instruction.kind.equals("LABEL") &&
          !controlLabels.contains(((LabelRef) instruction.args.get(0)).id)) continue;
      return i;
    }
    return -1;
  }

  private static MemberKey stateFieldStoredBefore(ClassModel owner, MethodModel method, int store,
                                                  Set<Integer> controlLabels) {
    if ((method.access & Opcodes.ACC_STATIC) != 0) return null;
    int fieldAt = previousStateInstruction(method, store, controlLabels);
    int receiverAt = previousStateInstruction(method, fieldAt, controlLabels);
    if (receiverAt < 0 || !method.insns.get(receiverAt).is("VAR", Opcodes.ALOAD) ||
        !method.insns.get(receiverAt).args.get(0).equals(0) ||
        !method.insns.get(fieldAt).is("FIELD", Opcodes.GETFIELD)) return null;
    Insn fieldInsn = method.insns.get(fieldAt);
    FieldModel field = declaredOwnInstanceField(owner, fieldInsn);
    if (field == null || !reference(field.descriptor)) return null;
    return new MemberKey(owner.name, "F", field.name, field.descriptor);
  }

  /* Return the exact ALOAD occurrence and slot copied into an ASTORE.  The
   * compiler's load-null-store clear idiom is one expression; non-control
   * debug labels may delimit its line/local-variable metadata. */
  private static int[] stateAliasStoredBefore(MethodModel method, int store,
                                              Set<Integer> controlLabels) {
    int previous = previousStateInstruction(method, store, controlLabels);
    if (previous < 0) return null;
    Insn direct = method.insns.get(previous);
    if (direct.is("VAR", Opcodes.ALOAD))
      return new int[] {previous, (Integer) direct.args.get(0)};
    if (!direct.is("VAR", Opcodes.ASTORE)) return null;
    int clearNull = previousStateInstruction(method, previous, controlLabels);
    int sourceAt = previousStateInstruction(method, clearNull, controlLabels);
    if (sourceAt < 0 || !method.insns.get(clearNull).is("INSN", Opcodes.ACONST_NULL) ||
        !method.insns.get(sourceAt).is("VAR", Opcodes.ALOAD) ||
        !method.insns.get(sourceAt).args.get(0).equals(direct.args.get(0))) return null;
    return new int[] {sourceAt, (Integer) method.insns.get(sourceAt).args.get(0)};
  }

  private static MemberKey directStateField(ClassModel owner, MethodModel method, List<Insn> code, int at) {
    if ((method.access & Opcodes.ACC_STATIC) != 0 || at + 1 >= code.size() ||
        !code.get(at).is("VAR", Opcodes.ALOAD) || !code.get(at).args.get(0).equals(0) ||
        !code.get(at + 1).is("FIELD", Opcodes.GETFIELD)) return null;
    Insn fieldInsn = code.get(at + 1);
    FieldModel field = declaredOwnInstanceField(owner, fieldInsn);
    if (field == null || !reference(field.descriptor)) return null;
    return new MemberKey(owner.name, "F", field.name, field.descriptor);
  }

  private static final class StateObjectOperand {
    final MemberKey field;
    final int next;
    StateObjectOperand(MemberKey field, int next) { this.field = field; this.next = next; }
  }

  private static final class ReachingStores {
    final Set<Integer> stores = new TreeSet<Integer>();
    boolean reachesUnknown;
  }

  private static void addEdge(List<Set<Integer>> predecessors, int from, int to) {
    if (to >= 0 && to < predecessors.size()) predecessors.get(to).add(from);
  }

  private static boolean terminatesFlow(int opcode) {
    return (opcode >= Opcodes.IRETURN && opcode <= Opcodes.RETURN) || opcode == Opcodes.ATHROW ||
        opcode == Opcodes.RET;
  }

  private static List<Set<Integer>> statePredecessors(MethodModel method) {
    List<Insn> code = method.insns;
    List<Set<Integer>> result = new ArrayList<Set<Integer>>();
    for (int i = 0; i < code.size(); i++) result.add(new TreeSet<Integer>());
    Map<Integer, Integer> labels = new HashMap<Integer, Integer>();
    for (int i = 0; i < code.size(); i++)
      if (code.get(i).kind.equals("LABEL")) labels.put(((LabelRef) code.get(i).args.get(0)).id, i);
    for (int i = 0; i < code.size(); i++) {
      Insn instruction = code.get(i);
      if (instruction.kind.equals("JUMP")) {
        Integer target = labels.get(((LabelRef) instruction.args.get(0)).id);
        require(target != null, "state-flow jump target is absent");
        addEdge(result, i, target);
        if (instruction.opcode != Opcodes.GOTO && instruction.opcode != Opcodes.JSR) addEdge(result, i, i + 1);
      } else if (instruction.kind.equals("TABLESWITCH")) {
        Integer target = labels.get(((LabelRef) instruction.args.get(2)).id);
        require(target != null, "state-flow tableswitch default is absent"); addEdge(result, i, target);
        for (LabelRef label : (LabelRef[]) instruction.args.get(3)) {
          target = labels.get(label.id); require(target != null, "state-flow tableswitch target is absent");
          addEdge(result, i, target);
        }
      } else if (instruction.kind.equals("LOOKUPSWITCH")) {
        Integer target = labels.get(((LabelRef) instruction.args.get(0)).id);
        require(target != null, "state-flow lookupswitch default is absent"); addEdge(result, i, target);
        for (LabelRef label : (LabelRef[]) instruction.args.get(2)) {
          target = labels.get(label.id); require(target != null, "state-flow lookupswitch target is absent");
          addEdge(result, i, target);
        }
      } else if (!terminatesFlow(instruction.opcode)) addEdge(result, i, i + 1);
    }
    for (TryCatchModel block : method.tryCatches) {
      Integer start = labels.get(block.start.id), end = labels.get(block.end.id);
      Integer handler = block.handler == null ? null : labels.get(block.handler.id);
      require(start != null && end != null && handler != null, "state-flow try/catch label is absent");
      for (int i = start; i < end; i++) addEdge(result, i, handler);
    }
    return result;
  }

  private static ReachingStores reachingStores(MethodModel method, int useAt, int slot) {
    List<Set<Integer>> predecessors = statePredecessors(method);
    ReachingStores result = new ReachingStores();
    Deque<Integer> pending = new ArrayDeque<Integer>();
    Set<Integer> visited = new HashSet<Integer>();
    if (useAt < 0 || useAt >= predecessors.size()) { result.reachesUnknown = true; return result; }
    pending.addAll(predecessors.get(useAt));
    if (pending.isEmpty()) result.reachesUnknown = true;
    while (!pending.isEmpty()) {
      int at = pending.removeFirst();
      if (!visited.add(at)) continue;
      Insn instruction = method.insns.get(at);
      if ((instruction.kind.equals("VAR") && instruction.args.get(0).equals(slot) &&
           instruction.opcode >= Opcodes.ISTORE && instruction.opcode <= Opcodes.ASTORE) ||
          (instruction.kind.equals("IINC") && instruction.args.get(0).equals(slot))) {
        if (instruction.is("VAR", Opcodes.ASTORE)) result.stores.add(at);
        else result.reachesUnknown = true;
        continue;
      }
      if (predecessors.get(at).isEmpty()) result.reachesUnknown = true;
      else pending.addAll(predecessors.get(at));
    }
    return result;
  }

  private static boolean exactConstructionStoredAt(MethodModel method, int store, String type,
                                                    String constructorDescriptor) {
    List<Insn> code = method.insns;
    if (store < 1 || !code.get(store).is("VAR", Opcodes.ASTORE) ||
        !code.get(store - 1).is("METHOD", Opcodes.INVOKESPECIAL) ||
        !code.get(store - 1).owner().equals(type) || !code.get(store - 1).memberName().equals("<init>") ||
        !code.get(store - 1).descriptor().equals(constructorDescriptor)) return false;
    for (int i = store - 2; i >= 0; i--) {
      if (stateControlBoundary(code.get(i))) return false;
      if (code.get(i).is("TYPE", Opcodes.NEW) && code.get(i).args.get(0).equals(type))
        return i + 1 < code.size() && code.get(i + 1).is("INSN", Opcodes.DUP);
    }
    return false;
  }

  private static boolean localIsMachineObject(MethodModel method, int useAt, int slot, String machine,
                                              Set<String> visited, int depth) {
    if (depth > 48 || !visited.add("machine:" + useAt + ":" + slot)) return false;
    ReachingStores reaching = reachingStores(method, useAt, slot);
    if (reaching.reachesUnknown || reaching.stores.isEmpty()) return false;
    for (int store : reaching.stores) {
      Insn init = store > 0 ? method.insns.get(store - 1) : null;
      if (init != null && init.is("METHOD", Opcodes.INVOKESPECIAL) && init.owner().equals(machine) &&
          init.memberName().equals("<init>")) {
        if (!exactConstructionStoredAt(method, store, machine, init.descriptor())) return false;
        continue;
      }
      int[] source = stateAliasStoredBefore(method, store, stateControlLabels(method));
      if (source == null || !localIsMachineObject(method, source[0], source[1], machine, visited, depth + 1)) return false;
    }
    return true;
  }

  private static boolean stateResultStoredAt(MethodModel method, int store, String machine,
                                             Set<String> visited, int depth) {
    List<Insn> code = method.insns;
    Set<Integer> controlLabels = stateControlLabels(method);
    int invokeAt = previousStateInstruction(method, store, controlLabels);
    int castAt = previousStateInstruction(method, invokeAt, controlLabels);
    if (castAt < 0) return false;
    Insn invoke = code.get(invokeAt);
    if (!invoke.is("METHOD", Opcodes.INVOKEINTERFACE) || !invoke.owner().equals("clojure/lang/IFn") ||
        !invoke.memberName().equals("invoke") || !invoke.descriptor().equals("()Ljava/lang/Object;") ||
        !Boolean.TRUE.equals(invoke.args.get(3)) || !code.get(castAt).is("TYPE", Opcodes.CHECKCAST) ||
        !code.get(castAt).args.get(0).equals("clojure/lang/IFn")) return false;
    int receiverAt, machineLocal;
    int receiverOrClear = previousStateInstruction(method, castAt, controlLabels);
    if (receiverOrClear >= 0 && code.get(receiverOrClear).is("VAR", Opcodes.ALOAD)) {
      receiverAt = receiverOrClear; machineLocal = (Integer) code.get(receiverAt).args.get(0);
    } else {
      int clearNull = previousStateInstruction(method, receiverOrClear, controlLabels);
      int receiver = previousStateInstruction(method, clearNull, controlLabels);
      if (receiver < 0 || !code.get(receiver).is("VAR", Opcodes.ALOAD) ||
          !code.get(clearNull).is("INSN", Opcodes.ACONST_NULL) ||
          !code.get(receiverOrClear).is("VAR", Opcodes.ASTORE) ||
          !code.get(receiver).args.get(0).equals(code.get(receiverOrClear).args.get(0))) return false;
      receiverAt = receiver; machineLocal = (Integer) code.get(receiverAt).args.get(0);
    }
    return localIsMachineObject(method, receiverAt, machineLocal, machine, visited, depth + 1);
  }

  private static final class StateOrigin {
    final boolean valid;
    final MemberKey field;
    StateOrigin(boolean valid, MemberKey field) { this.valid = valid; this.field = field; }
  }

  private static StateOrigin stateOriginFromLocal(ClassModel owner, MethodModel method, int useAt, int slot,
                                                  String machine, Set<MemberKey> knownStateFields,
                                                  Set<String> visited, int depth) {
    if (depth > 48 || !visited.add("state:" + useAt + ":" + slot)) return new StateOrigin(false, null);
    ReachingStores reaching = reachingStores(method, useAt, slot);
    if (reaching.reachesUnknown || reaching.stores.isEmpty()) return new StateOrigin(false, null);
    MemberKey commonField = null;
    boolean sawNonField = false;
    List<Insn> code = method.insns;
    Set<Integer> controlLabels = stateControlLabels(method);
    for (int store : reaching.stores) {
      MemberKey field = stateFieldStoredBefore(owner, method, store, controlLabels);
      if (field != null && knownStateFields.contains(field)) {
        if (commonField == null) commonField = field;
        else if (!commonField.equals(field)) sawNonField = true;
        continue;
      }
      if (exactConstructionStoredAt(method, store, "java/util/concurrent/atomic/AtomicReferenceArray", "(I)V") ||
          stateResultStoredAt(method, store, machine, visited, depth)) {
        sawNonField = true; continue;
      }
      int[] source = stateAliasStoredBefore(method, store, controlLabels);
      if (source == null) return new StateOrigin(false, null);
      StateOrigin alias = stateOriginFromLocal(owner, method, source[0], source[1], machine,
          knownStateFields, visited, depth + 1);
      if (!alias.valid) return alias;
      if (alias.field == null) sawNonField = true;
      else if (commonField == null) commonField = alias.field;
      else if (!commonField.equals(alias.field)) sawNonField = true;
    }
    return new StateOrigin(true, sawNonField ? null : commonField);
  }

  private static StateObjectOperand stateObjectOperand(ClassModel owner, MethodModel method, List<Insn> code,
                                                        int at, Set<MemberKey> knownStateFields,
                                                        boolean directOnly, String machine) {
    MemberKey direct = directStateField(owner, method, code, at);
    if (direct != null) {
      int next = at + 2;
      /* Exact load-and-clear idiom: GETFIELD leaves the old state object on
       * the stack while the following own-field PUTFIELD writes null. */
      if (next + 2 < code.size() && code.get(next).is("VAR", Opcodes.ALOAD) &&
          code.get(next).args.get(0).equals(0) && code.get(next + 1).is("INSN", Opcodes.ACONST_NULL) &&
          code.get(next + 2).is("FIELD", Opcodes.PUTFIELD)) {
        Insn clear = code.get(next + 2);
        if (clear.owner().equals(direct.owner) && clear.memberName().equals(direct.name) &&
            clear.descriptor().equals(direct.descriptor) && declaredOwnInstanceField(owner, clear) != null)
          next += 3;
      }
      return new StateObjectOperand(direct, next);
    }
    if (!directOnly && at < code.size() && code.get(at).is("VAR", Opcodes.ALOAD)) {
      int slot = (Integer) code.get(at).args.get(0);
      StateOrigin origin = stateOriginFromLocal(owner, method, at, slot, machine,
          knownStateFields, new HashSet<String>(), 0);
      if (origin.valid) {
        int next = at + 1;
        if (next + 1 < code.size() && code.get(next).is("INSN", Opcodes.ACONST_NULL) &&
            code.get(next + 1).is("VAR", Opcodes.ASTORE) && code.get(next + 1).args.get(0).equals(slot))
          next += 2;
        return new StateObjectOperand(origin.field, next);
      }
    }
    return null;
  }

  private static boolean exactInvoke(Insn instruction, String owner, String descriptor) {
    return instruction.is("METHOD", Opcodes.INVOKEINTERFACE) && instruction.owner().equals(owner) &&
        instruction.memberName().equals("invokePrim") && instruction.descriptor().equals(descriptor) &&
        instruction.args.size() == 4 && Boolean.TRUE.equals(instruction.args.get(3));
  }

  private static final class StackValue {
    final int size;
    final boolean reference;
    StackValue(int size, boolean reference) { this.size = size; this.reference = reference; }
  }

  private static StackValue stackType(Type type) {
    return new StackValue(type.getSize(), type.getSort() == Type.OBJECT || type.getSort() == Type.ARRAY);
  }

  private static boolean popStack(List<StackValue> stack, Type expected) {
    if (stack.isEmpty()) return false;
    StackValue value = stack.remove(stack.size() - 1);
    boolean expectedReference = expected.getSort() == Type.OBJECT || expected.getSort() == Type.ARRAY;
    return value.size == expected.getSize() && (!expectedReference || value.reference);
  }

  /* Prove that a straight-line value expression is stack-isolated: starting
   * from an empty abstract stack it never underflows and leaves exactly one
   * reference.  Calls and reads remain byte-for-byte in the final model; this
   * proof only establishes that they cannot consume or replace the state-slot
   * long below the expression. */
  private static boolean straightLineStateValueRegion(MethodModel method, int start, int end) {
    if (start <= 0 || end <= start || end > method.insns.size()) return false;
    List<Set<Integer>> predecessors = statePredecessors(method);
    for (int i = start; i < end; i++) {
      Insn instruction = method.insns.get(i);
      if (instruction.kind.equals("JUMP") || instruction.kind.equals("TABLESWITCH") ||
          instruction.kind.equals("LOOKUPSWITCH") || terminatesFlow(instruction.opcode)) return false;
      if (!predecessors.get(i).equals(Collections.singleton(i - 1))) return false;
    }
    return true;
  }

  private static boolean isolatedStateValueBlock(List<Insn> code, int start, int end,
                                                 ClassModel owner, MethodModel method) {
    /* Labels and expanded frames are verifier/debug metadata, not stack
     * operations.  They are safe to ignore only after proving that no jump or
     * exception-handler edge enters the candidate value region: each modeled
     * event must have the immediately preceding event as its sole predecessor. */
    if (!straightLineStateValueRegion(method, start, end)) return false;
    List<StackValue> stack = new ArrayList<StackValue>();
    for (int i = start; i < end; i++) {
      Insn instruction = code.get(i);
      if (instruction.kind.equals("LABEL") || instruction.kind.equals("FRAME")) continue;
      if (instruction.kind.equals("VAR")) {
        switch (instruction.opcode) {
          case Opcodes.ALOAD: stack.add(new StackValue(1, true)); break;
          case Opcodes.ILOAD: case Opcodes.FLOAD: stack.add(new StackValue(1, false)); break;
          case Opcodes.LLOAD: case Opcodes.DLOAD: stack.add(new StackValue(2, false)); break;
          case Opcodes.ASTORE:
            if (!popStack(stack, Type.getType(Object.class))) return false; break;
          case Opcodes.ISTORE: case Opcodes.FSTORE:
            if (stack.isEmpty() || stack.remove(stack.size() - 1).size != 1) return false; break;
          case Opcodes.LSTORE: case Opcodes.DSTORE:
            if (stack.isEmpty() || stack.remove(stack.size() - 1).size != 2) return false; break;
          default: return false;
        }
      } else if (instruction.kind.equals("LDC")) {
        Object value = instruction.args.get(0);
        if (value instanceof Long || value instanceof Double) stack.add(new StackValue(2, false));
        else if (value instanceof Type) stack.add(new StackValue(1, true));
        else stack.add(new StackValue(1, value instanceof String || value instanceof Handle || value instanceof ConstantDynamic));
      } else if (instruction.kind.equals("INT")) {
        if (instruction.opcode == Opcodes.BIPUSH || instruction.opcode == Opcodes.SIPUSH)
          stack.add(new StackValue(1, false));
        else if (instruction.opcode == Opcodes.NEWARRAY) {
          if (stack.isEmpty() || stack.remove(stack.size() - 1).size != 1) return false;
          stack.add(new StackValue(1, true));
        } else return false;
      } else if (instruction.kind.equals("TYPE")) {
        if (instruction.opcode == Opcodes.NEW) stack.add(new StackValue(1, true));
        else if (instruction.opcode == Opcodes.CHECKCAST) {
          if (!popStack(stack, Type.getType(Object.class))) return false;
          stack.add(new StackValue(1, true));
        } else if (instruction.opcode == Opcodes.INSTANCEOF) {
          if (!popStack(stack, Type.getType(Object.class))) return false;
          stack.add(new StackValue(1, false));
        } else if (instruction.opcode == Opcodes.ANEWARRAY) {
          if (stack.isEmpty() || stack.remove(stack.size() - 1).size != 1) return false;
          stack.add(new StackValue(1, true));
        } else return false;
      } else if (instruction.kind.equals("FIELD")) {
        Type fieldType = Type.getType(instruction.descriptor());
        if (instruction.opcode == Opcodes.GETSTATIC) {
          if (instruction.owner().equals(owner.name)) {
            FieldModel declared = null;
            for (FieldModel candidate : owner.fields)
              if (candidate.name.equals(instruction.memberName()) &&
                  candidate.descriptor.equals(instruction.descriptor())) declared = candidate;
            if (declared == null ||
                (declared.access & (Opcodes.ACC_STATIC | Opcodes.ACC_FINAL)) !=
                    (Opcodes.ACC_STATIC | Opcodes.ACC_FINAL) ||
                (declared.access & Opcodes.ACC_VOLATILE) != 0) return false;
          } else return false;
          stack.add(stackType(fieldType));
        } else if (instruction.opcode == Opcodes.GETFIELD) {
          if (!popStack(stack, Type.getType(Object.class))) return false;
          stack.add(stackType(fieldType));
        } else if (instruction.opcode == Opcodes.PUTSTATIC) {
          if (!popStack(stack, fieldType)) return false;
        } else if (instruction.opcode == Opcodes.PUTFIELD) {
          if (!popStack(stack, fieldType) || !popStack(stack, Type.getType(Object.class))) return false;
        } else return false;
      } else if (instruction.kind.equals("METHOD") || instruction.kind.equals("INDY")) {
        String descriptor = instruction.kind.equals("METHOD") ? instruction.descriptor() : (String) instruction.args.get(1);
        Type methodType = Type.getMethodType(descriptor);
        Type[] arguments = methodType.getArgumentTypes();
        for (int j = arguments.length - 1; j >= 0; j--) if (!popStack(stack, arguments[j])) return false;
        if (instruction.kind.equals("METHOD") && instruction.opcode != Opcodes.INVOKESTATIC &&
            !popStack(stack, Type.getType(Object.class))) return false;
        if (methodType.getReturnType().getSort() != Type.VOID) stack.add(stackType(methodType.getReturnType()));
      } else if (instruction.kind.equals("MULTIANEWARRAY")) {
        int dimensions = (Integer) instruction.args.get(1);
        for (int j = 0; j < dimensions; j++)
          if (stack.isEmpty() || stack.remove(stack.size() - 1).size != 1) return false;
        stack.add(new StackValue(1, true));
      } else if (instruction.kind.equals("IINC")) {
        /* no stack effect */
      } else if (instruction.kind.equals("INSN")) {
        switch (instruction.opcode) {
          case Opcodes.NOP: break;
          case Opcodes.ACONST_NULL: stack.add(new StackValue(1, true)); break;
          case Opcodes.ICONST_M1: case Opcodes.ICONST_0: case Opcodes.ICONST_1:
          case Opcodes.ICONST_2: case Opcodes.ICONST_3: case Opcodes.ICONST_4: case Opcodes.ICONST_5:
          case Opcodes.FCONST_0: case Opcodes.FCONST_1: case Opcodes.FCONST_2:
            stack.add(new StackValue(1, false)); break;
          case Opcodes.LCONST_0: case Opcodes.LCONST_1: case Opcodes.DCONST_0: case Opcodes.DCONST_1:
            stack.add(new StackValue(2, false)); break;
          case Opcodes.POP:
            if (stack.isEmpty() || stack.remove(stack.size() - 1).size != 1) return false; break;
          case Opcodes.POP2:
            if (stack.isEmpty()) return false;
            StackValue top = stack.remove(stack.size() - 1);
            if (top.size == 1 && (stack.isEmpty() || stack.remove(stack.size() - 1).size != 1)) return false;
            break;
          case Opcodes.DUP:
            if (stack.isEmpty() || stack.get(stack.size() - 1).size != 1) return false;
            stack.add(stack.get(stack.size() - 1)); break;
          default: return false;
        }
      } else return false;
    }
    return stack.size() == 1 && stack.get(0).size == 1 && stack.get(0).reference;
  }

  private static boolean safeStateValueBlock(List<Insn> code, int start, int end,
                                             ClassModel owner, MethodModel method) {
    Normalizer identity = new Normalizer(Collections.emptyMap(), Collections.emptyMap());
    String simple = safeBlock(code, start, end, "Ljava/lang/Object;", identity, owner.namespace, owner,
        (method.access & Opcodes.ACC_STATIC) == 0);
    if (simple != null) return true;
    boolean isolated = isolatedStateValueBlock(code, start, end, owner, method);
    if (isolated) return true;
    if (end == start + 9 && code.get(start).is("VAR", Opcodes.ILOAD) &&
        code.get(start + 1).kind.equals("JUMP") &&
        (code.get(start + 1).opcode == Opcodes.IFEQ || code.get(start + 1).opcode == Opcodes.IFNE) &&
        booleanConstant(code.get(start + 2)) != null && code.get(start + 3).is("JUMP", Opcodes.GOTO) &&
        code.get(start + 4).kind.equals("LABEL") && code.get(start + 5).kind.equals("FRAME") &&
        booleanConstant(code.get(start + 6)) != null && code.get(start + 7).kind.equals("LABEL") &&
        code.get(start + 8).kind.equals("FRAME") &&
        !booleanConstant(code.get(start + 2)).equals(booleanConstant(code.get(start + 6))) &&
        ((LabelRef) code.get(start + 1).args.get(0)).id == ((LabelRef) code.get(start + 4).args.get(0)).id &&
        ((LabelRef) code.get(start + 3).args.get(0)).id == ((LabelRef) code.get(start + 7).args.get(0)).id)
      return true;
    if (straightLineStateValueRegion(method, start, end)) {
      List<Insn> semantic = new ArrayList<Insn>();
      for (int i = start; i < end; i++)
        if (!code.get(i).kind.equals("LABEL") && !code.get(i).kind.equals("FRAME")) semantic.add(code.get(i));
      if (semantic.size() == 1 && booleanConstant(semantic.get(0)) != null) return true;
    }
    if (end != start + 1 || !code.get(start).is("FIELD", Opcodes.GETSTATIC) ||
        !code.get(start).owner().equals(owner.name)) return false;
    Insn get = code.get(start);
    for (FieldModel field : owner.fields)
      if (field.name.equals(get.memberName()) && field.descriptor.equals(get.descriptor()) &&
          reference(field.descriptor) &&
          (field.access & (Opcodes.ACC_STATIC | Opcodes.ACC_FINAL)) ==
              (Opcodes.ACC_STATIC | Opcodes.ACC_FINAL) &&
          (field.access & Opcodes.ACC_VOLATILE) == 0)
        return true;
    return false;
  }

  private static String booleanConstant(Insn instruction) {
    if (!instruction.is("FIELD", Opcodes.GETSTATIC) || !instruction.owner().equals("java/lang/Boolean") ||
        !instruction.descriptor().equals("Ljava/lang/Boolean;")) return null;
    return instruction.memberName().equals("TRUE") || instruction.memberName().equals("FALSE")
        ? instruction.memberName() : null;
  }

  private static StateSlotAccess stateAccessAt(ClassModel owner, MethodModel method, int start,
                                               String machine,
                                               String target, Set<MemberKey> knownStateFields,
                                               boolean directOnly) {
    List<Insn> code = method.insns;
    if (start + 5 >= code.size()) return null;
    Insn field = code.get(start), root = code.get(start + 1), cast = code.get(start + 2);
    String kind, ifnOwner, descriptor;
    if (target.equals(AGET_OBJECT)) {
      kind = "read"; ifnOwner = "clojure/lang/IFn$OLO";
      descriptor = "(Ljava/lang/Object;J)Ljava/lang/Object;";
    } else if (target.equals(ASET_OBJECT)) {
      kind = "write"; ifnOwner = "clojure/lang/IFn$OLOO";
      descriptor = "(Ljava/lang/Object;JLjava/lang/Object;)Ljava/lang/Object;";
    } else return null;
    if (!field.is("FIELD", Opcodes.GETSTATIC) ||
        !root.is("METHOD", Opcodes.INVOKEVIRTUAL) || !root.owner().equals("clojure/lang/Var") ||
        !root.memberName().equals("getRawRoot") || !root.descriptor().equals("()Ljava/lang/Object;") ||
        !cast.is("TYPE", Opcodes.CHECKCAST) || !cast.args.get(0).equals(ifnOwner)) return null;
    StateObjectOperand state = stateObjectOperand(owner, method, code, start + 3, knownStateFields, directOnly, machine);
    if (state == null || state.next >= code.size()) return null;
    Insn slotInstruction = code.get(state.next);
    Long slot = pushedLong(slotInstruction);
    if (slot == null || slot.longValue() < 0) return null;
    int invokeAt;
    if (kind.equals("read")) {
      invokeAt = nextStateInstruction(method, state.next, stateControlLabels(method));
      if (invokeAt < 0 || !exactInvoke(code.get(invokeAt), ifnOwner, descriptor)) return null;
    } else {
      List<Integer> candidates = new ArrayList<Integer>();
      for (int i = state.next + 2; i < code.size() && i <= state.next + 256; i++) {
        if (terminatesFlow(code.get(i).opcode) || code.get(i).kind.equals("TABLESWITCH") ||
            code.get(i).kind.equals("LOOKUPSWITCH")) break;
        if (!exactInvoke(code.get(i), ifnOwner, descriptor)) continue;
        if (safeStateValueBlock(code, state.next + 1, i, owner, method)) candidates.add(i);
      }
      if (candidates.size() != 1) return null;
      invokeAt = candidates.get(0);
    }
    if (machine == null) return null;
    return new StateSlotAccess(new StateSlotKey(machine, slot.longValue()), kind, owner, method,
        slotInstruction, state.field);
  }

  private static boolean constructsClass(ClassModel owner, String target) {
    boolean allocated = false, initialized = false;
    for (MethodModel method : owner.methods) for (Insn instruction : method.insns) {
      if (instruction.is("TYPE", Opcodes.NEW) && instruction.args.get(0).equals(target)) allocated = true;
      if (instruction.is("METHOD", Opcodes.INVOKESPECIAL) && instruction.owner().equals(target) &&
          instruction.memberName().equals("<init>")) initialized = true;
    }
    return allocated && initialized;
  }

  private static Map<String, String> stateMachineFamilies(Map<String, ClassModel> classes) {
    Set<String> roots = new TreeSet<String>();
    for (String name : classes.keySet()) {
      String root = stateMachineOwner(name);
      if (root != null && classes.containsKey(root)) roots.add(root);
    }
    Map<String, String> result = new TreeMap<String, String>();
    for (ClassModel owner : classes.values()) {
      String direct = stateMachineOwner(owner.name);
      if (direct != null) {
        require(roots.contains(direct), "state-machine family root is absent from cohort: " + owner.name + " -> " + direct);
        result.put(owner.name, direct);
        continue;
      }
      List<String> candidates = new ArrayList<String>();
      for (String root : roots)
        if (root.startsWith(owner.name + "$state_machine__") && constructsClass(owner, root)) candidates.add(root);
      require(candidates.size() <= 1,
          "class constructs multiple suffix-related state-machine roots: " + owner.name + " -> " + candidates);
      if (candidates.size() == 1) result.put(owner.name, candidates.get(0));
    }
    return result;
  }

  private static StateSlotUniverse stateSlotUniverse(Map<String, ClassModel> classes) {
    Map<MemberKey, String> bindings = varBindings(classes);
    Map<String, String> families = stateMachineFamilies(classes);
    Set<MemberKey> stateFields = new TreeSet<MemberKey>();
    /* First discover state-object fields only from the exact direct read shape.
     * This prevents an arbitrary local or captured Object from becoming an
     * anchor merely because it is passed to an IFn with a long argument. */
    for (ClassModel owner : classes.values()) for (MethodModel method : owner.methods) {
      String machine = families.get(owner.name);
      if (machine == null) continue;
      for (int i = 0; i < method.insns.size(); i++) {
        Insn instruction = method.insns.get(i);
        if (!instruction.is("FIELD", Opcodes.GETSTATIC)) continue;
        String target = bindings.get(new MemberKey(instruction.owner(), "F", instruction.memberName(), instruction.descriptor()));
        if (!AGET_OBJECT.equals(target)) continue;
        StateSlotAccess access = stateAccessAt(owner, method, i, machine, target, Collections.emptySet(), true);
        if (access != null) stateFields.add(access.stateField);
      }
    }
    StateSlotUniverse result = new StateSlotUniverse();
    for (ClassModel owner : classes.values()) for (MethodModel method : owner.methods) {
      for (int i = 0; i < method.insns.size(); i++) {
        Insn instruction = method.insns.get(i);
        if (!instruction.is("FIELD", Opcodes.GETSTATIC)) continue;
        String target = bindings.get(new MemberKey(instruction.owner(), "F", instruction.memberName(), instruction.descriptor()));
        if (!AGET_OBJECT.equals(target) && !ASET_OBJECT.equals(target)) continue;
        String machine = families.get(owner.name);
        /* Runtime helpers such as ioc-alts! also call these Vars, but their
         * protocol indices are not compiler-renumbered state-machine locals.
         * Keep those instructions entirely exact by excluding them from this
         * narrowly generated-family quotient. */
        if (machine == null) continue;
        StateSlotAccess access = stateAccessAt(owner, method, i, machine, target, stateFields, false);
        require(access != null, "unproved " + target + " bytecode shape in " + owner.name + "." + method.key() +
            " at instruction " + i + " window=" + instructionWindow(method.insns, i, owner.namespace));
        require(access.stateField == null || stateFields.contains(access.stateField),
            "ioc state-array access does not use a direct-read-proved state field or a proved constructed state result in " +
            owner.name + "." + method.key());
        result.add(access);
      }
    }
    for (List<StateSlotAccess> accesses : result.byNode.values())
      Collections.sort(accesses, (a, b) -> {
        int c = a.owner.name.compareTo(b.owner.name); if (c != 0) return c;
        c = a.method.key().compareTo(b.method.key()); if (c != 0) return c;
        return Integer.compare(a.method.insns.indexOf(a.slotInsn), b.method.insns.indexOf(b.slotInsn));
      });
    return result;
  }

  private static String instructionWindow(List<Insn> code, int center, String namespace) {
    Normalizer identity = new Normalizer(Collections.emptyMap(), Collections.emptyMap());
    List<String> result = new ArrayList<String>();
    for (int i = Math.max(0, center - 5); i <= Math.min(code.size() - 1, center + 16); i++)
      result.add((i - center) + ":" + code.get(i).render(identity, namespace));
    return "[" + join(result) + "]";
  }

  private static final class RecordingClassVisitor extends ClassVisitor {
    final ClassModel model;

    RecordingClassVisitor(String namespace) {
      super(ASM);
      model = new ClassModel();
      model.namespace = namespace;
    }

    @Override public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
      model.version = version;
      model.access = access;
      model.name = name;
      model.signature = signature;
      model.superName = superName;
      if (interfaces != null) model.interfaces.addAll(Arrays.asList(interfaces));
    }

    @Override public void visitSource(String source, String debug) {
      model.events.add("SOURCE(" + q(source) + "," + q(debug) + ")");
    }

    @Override public ModuleVisitor visitModule(String name, int access, String version) {
      model.unsupported.add("module attribute");
      return null;
    }

    @Override public void visitNestHost(String nestHost) { model.events.add("NESTHOST(" + q(nestHost) + ")"); }
    @Override public void visitOuterClass(String owner, String name, String descriptor) { model.events.add("OUTER(" + q(owner) + "," + q(name) + "," + q(descriptor) + ")"); }
    @Override public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) { return annotation(model.events, "ANNOT(" + q(descriptor) + "," + visible + ")"); }
    @Override public AnnotationVisitor visitTypeAnnotation(int typeRef, TypePath typePath, String descriptor, boolean visible) { return annotation(model.events, "TANNOT(" + typeRef + "," + q(typePath(typePath)) + "," + q(descriptor) + "," + visible + ")"); }
    @Override public void visitAttribute(Attribute attribute) { model.unsupported.add("class attribute " + attribute.type); }
    @Override public void visitNestMember(String nestMember) { model.events.add("NESTMEMBER(" + q(nestMember) + ")"); }
    @Override public void visitPermittedSubclass(String permittedSubclass) { model.events.add("PERMITTED(" + q(permittedSubclass) + ")"); }
    @Override public void visitInnerClass(String name, String outerName, String innerName, int access) { model.events.add("INNER(" + q(name) + "," + q(outerName) + "," + q(innerName) + "," + access + ")"); }

    @Override public RecordComponentVisitor visitRecordComponent(String name, String descriptor, String signature) {
      model.unsupported.add("record component " + name);
      return null;
    }

    @Override public FieldVisitor visitField(int access, String name, String descriptor, String signature, Object value) {
      final FieldModel field = new FieldModel();
      field.access = access; field.name = name; field.descriptor = descriptor; field.signature = signature; field.value = value;
      model.fields.add(field);
      return new FieldVisitor(ASM) {
        @Override public AnnotationVisitor visitAnnotation(String desc, boolean visible) { return annotation(field.events, "ANNOT(" + q(desc) + "," + visible + ")"); }
        @Override public AnnotationVisitor visitTypeAnnotation(int ref, TypePath path, String desc, boolean visible) { return annotation(field.events, "TANNOT(" + ref + "," + q(typePath(path)) + "," + q(desc) + "," + visible + ")"); }
        @Override public void visitAttribute(Attribute attribute) { model.unsupported.add("field " + name + " attribute " + attribute.type); }
      };
    }

    @Override public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
      final MethodModel method = new MethodModel();
      method.access = access; method.name = name; method.descriptor = descriptor; method.signature = signature;
      if (exceptions != null) method.exceptions.addAll(Arrays.asList(exceptions));
      model.methods.add(method);
      return new RecordingMethodVisitor(model, method);
    }
  }

  private static final class RecordingMethodVisitor extends MethodVisitor {
    final ClassModel owner;
    final MethodModel method;
    final Map<Label, LabelRef> labels = new HashMap<Label, LabelRef>();

    RecordingMethodVisitor(ClassModel owner, MethodModel method) { super(ASM); this.owner = owner; this.method = method; }
    LabelRef label(Label label) { return labels.computeIfAbsent(label, ignored -> new LabelRef(labels.size())); }
    LabelRef[] labels(Label[] source) { LabelRef[] result = new LabelRef[source.length]; for (int i = 0; i < source.length; i++) result[i] = label(source[i]); return result; }
    Object[] frameValues(int count, Object[] source) {
      Object[] result = new Object[count];
      for (int i = 0; i < count; i++) result[i] = source[i] instanceof Label ? label((Label) source[i]) : source[i];
      return result;
    }
    String labelIds(Label[] source) { List<String> result = new ArrayList<String>(); for (Label item : source) result.add(String.valueOf(label(item).id)); return "[" + join(result) + "]"; }

    @Override public void visitParameter(String name, int access) { method.events.add("PARAM(" + q(name) + "," + access + ")"); }
    @Override public AnnotationVisitor visitAnnotationDefault() { return annotation(method.events, "DEFAULT"); }
    @Override public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) { return annotation(method.events, "ANNOT(" + q(descriptor) + "," + visible + ")"); }
    @Override public AnnotationVisitor visitTypeAnnotation(int ref, TypePath path, String descriptor, boolean visible) { return annotation(method.events, "TANNOT(" + ref + "," + q(typePath(path)) + "," + q(descriptor) + "," + visible + ")"); }
    @Override public void visitAnnotableParameterCount(int parameterCount, boolean visible) { method.events.add("APCOUNT(" + parameterCount + "," + visible + ")"); }
    @Override public AnnotationVisitor visitParameterAnnotation(int parameter, String descriptor, boolean visible) { return annotation(method.events, "PANNOT(" + parameter + "," + q(descriptor) + "," + visible + ")"); }
    @Override public void visitAttribute(Attribute attribute) { owner.unsupported.add("method " + method.key() + " attribute " + attribute.type); }
    @Override public void visitCode() { method.codeEvents.add("CODE"); }
    @Override public void visitFrame(int type, int numLocal, Object[] local, int numStack, Object[] stack) {
      method.insns.add(new Insn("FRAME", -1, type, frameValues(numLocal, local), frameValues(numStack, stack)));
    }
    @Override public void visitInsn(int opcode) { method.insns.add(new Insn("INSN", opcode)); }
    @Override public void visitIntInsn(int opcode, int operand) { method.insns.add(new Insn("INT", opcode, operand)); }
    @Override public void visitVarInsn(int opcode, int varIndex) { method.insns.add(new Insn("VAR", opcode, varIndex)); }
    @Override public void visitTypeInsn(int opcode, String type) { method.insns.add(new Insn("TYPE", opcode, type)); }
    @Override public void visitFieldInsn(int opcode, String ownerName, String name, String descriptor) { method.insns.add(new Insn("FIELD", opcode, ownerName, name, descriptor)); }
    @Override public void visitMethodInsn(int opcode, String ownerName, String name, String descriptor, boolean isInterface) { method.insns.add(new Insn("METHOD", opcode, ownerName, name, descriptor, isInterface)); }
    @Override public void visitInvokeDynamicInsn(String name, String descriptor, Handle bootstrapMethodHandle, Object... bootstrapMethodArguments) { method.insns.add(new Insn("INDY", Opcodes.INVOKEDYNAMIC, name, descriptor, bootstrapMethodHandle, bootstrapMethodArguments)); }
    @Override public void visitJumpInsn(int opcode, Label label) { method.insns.add(new Insn("JUMP", opcode, label(label))); }
    @Override public void visitLabel(Label label) { method.insns.add(new Insn("LABEL", -1, label(label))); }
    @Override public void visitLdcInsn(Object value) { method.insns.add(new Insn("LDC", Opcodes.LDC, value)); }
    @Override public void visitIincInsn(int varIndex, int increment) { method.insns.add(new Insn("IINC", Opcodes.IINC, varIndex, increment)); }
    @Override public void visitTableSwitchInsn(int min, int max, Label dflt, Label... labels) { method.insns.add(new Insn("TABLESWITCH", Opcodes.TABLESWITCH, min, max, label(dflt), labels(labels))); }
    @Override public void visitLookupSwitchInsn(Label dflt, int[] keys, Label[] labels) { method.insns.add(new Insn("LOOKUPSWITCH", Opcodes.LOOKUPSWITCH, label(dflt), keys, labels(labels))); }
    @Override public void visitMultiANewArrayInsn(String descriptor, int numDimensions) { method.insns.add(new Insn("MULTIANEWARRAY", Opcodes.MULTIANEWARRAY, descriptor, numDimensions)); }
    @Override public AnnotationVisitor visitInsnAnnotation(int typeRef, TypePath typePath, String descriptor, boolean visible) { return annotation(method.codeEvents, "IANNOT(" + method.insns.size() + "," + typeRef + "," + q(typePath(typePath)) + "," + q(descriptor) + "," + visible + ")"); }
    @Override public void visitTryCatchBlock(Label start, Label end, Label handler, String type) {
      LabelRef startRef = label(start), endRef = label(end), handlerRef = handler == null ? null : label(handler);
      method.tryCatches.add(new TryCatchModel(startRef, endRef, handlerRef, type));
      method.codeEvents.add("TRY(" + startRef.id + "," + endRef.id + "," + (handlerRef == null ? -1 : handlerRef.id) + "," + q(type) + ")");
    }
    @Override public AnnotationVisitor visitTryCatchAnnotation(int typeRef, TypePath typePath, String descriptor, boolean visible) { return annotation(method.codeEvents, "TCANNOT(" + typeRef + "," + q(typePath(typePath)) + "," + q(descriptor) + "," + visible + ")"); }
    @Override public void visitLocalVariable(String name, String descriptor, String signature, Label start, Label end, int index) {
      method.localVariables.add(new LocalVariableModel(name, descriptor, signature, label(start), label(end), index));
    }
    @Override public AnnotationVisitor visitLocalVariableAnnotation(int typeRef, TypePath typePath, Label[] start, Label[] end, int[] index, String descriptor, boolean visible) { return annotation(method.codeEvents, "LVANNOT(" + typeRef + "," + q(typePath(typePath)) + "," + labelIds(start) + "," + labelIds(end) + "," + Arrays.toString(index) + "," + q(descriptor) + "," + visible + ")"); }
    @Override public void visitLineNumber(int line, Label start) { method.codeEvents.add("LINE(" + line + "," + label(start).id + ")"); }
    @Override public void visitMaxs(int maxStack, int maxLocals) { method.maxStack = maxStack; method.maxLocals = maxLocals; }
  }

  private static AnnotationVisitor annotation(List<String> events, String prefix) {
    final int index = events.size();
    events.add(null);
    return new AnnotationNode(prefix, rendered -> events.set(index, rendered));
  }

  private static String rawStrings(String[] values) { if (values == null) return "-"; List<String> out = new ArrayList<String>(); for (String v : values) out.add(q(v)); return "[" + join(out) + "]"; }

  private static final class IdMap {
    final String namespace;
    final Map<String, String> mapping = new TreeMap<String, String>((a, b) -> Long.compare(Long.parseLong(a), Long.parseLong(b)));
    final Set<String> leftIds;
    final Set<String> rightIds;

    IdMap(String namespace, Set<String> leftIds, Set<String> rightIds, Map<String, String> provedMapping) {
      this.namespace = namespace;
      this.leftIds = new TreeSet<String>(numericComparator()); this.leftIds.addAll(leftIds);
      this.rightIds = new TreeSet<String>(numericComparator()); this.rightIds.addAll(rightIds);
      mapping.putAll(provedMapping);
      require(new HashSet<String>(mapping.values()).size() == mapping.size(), "compiler-ID mapping is not injective for " + namespace);
      require(mapping.keySet().equals(this.leftIds), "compiler-ID mapping is incomplete for " + namespace);
      require(new HashSet<String>(mapping.values()).equals(this.rightIds), "compiler-ID mapping is not surjective for " + namespace);
    }
  }

  private static Comparator<String> numericComparator() { return (a, b) -> { int c = Long.compare(Long.parseLong(a), Long.parseLong(b)); return c != 0 ? c : a.compareTo(b); }; }

  private static Set<String> ids(String value) {
    Set<String> result = new TreeSet<String>(numericComparator());
    if (value == null) return result;
    for (IdToken token : idTokens(value)) result.add(token.number);
    return result;
  }

  private static String replaceIds(String value, Map<String, String> mapping, boolean requireKnown) {
    if (value == null) return null;
    String result = replaceIdPattern(value, INST_ID, "$inst_", "__", mapping, requireKnown);
    result = replaceIdPattern(result, EVAL_ID, "$eval", "", mapping, requireKnown);
    result = replaceIdPattern(result, DOUBLE_ID, "__", "", mapping, requireKnown);
    Matcher single = SINGLE_LOCAL_ID.matcher(result);
    if (!single.matches()) return result;
    String mapped = mapping.get(single.group(2));
    if (mapped == null) {
      if (requireKnown) throw new Failure("unmapped compiler ID " + single.group(2) + " in " + value);
      mapped = single.group(2);
    }
    return single.group(1) + mapped;
  }

  private static String replaceIdPattern(String value, Pattern pattern, String prefix, String suffix,
                                         Map<String, String> mapping, boolean requireKnown) {
    Matcher matcher = pattern.matcher(value);
    StringBuffer out = new StringBuffer();
    while (matcher.find()) {
      String oldId = matcher.group(1);
      String newId = mapping.get(oldId);
      if (newId == null) {
        if (requireKnown) throw new Failure("unmapped compiler ID " + oldId + " in " + value);
        newId = oldId;
      }
      String replacement = prefix + newId + suffix;
      matcher.appendReplacement(out, Matcher.quoteReplacement(replacement));
    }
    matcher.appendTail(out);
    return out.toString();
  }

  private static final class Normalizer {
    final Map<String, IdMap> maps;
    final Map<String, String> classNamespace;
    final Map<String, String> classMapping;
    final MemberUniverse members;
    final Map<MemberKey, MemberKey> memberMapping;
    final StateSlotUniverse stateSlots;
    final Map<StateSlotKey, StateSlotKey> stateSlotMapping;
    final StateSlotKey highlightedStateSlot;
    final LocalUniverse locals;
    final Map<LocalNodeKey, LocalNodeKey> localMapping;
    final LocalNodeKey highlightedLocal;
    final boolean erase;
    final boolean eraseMembers;
    final boolean eraseStateSlots;
    final boolean eraseLocals;

    Normalizer(Map<String, IdMap> maps, Map<String, String> classNamespace) { this(maps, classNamespace, Collections.emptyMap(), false, new MemberUniverse(), Collections.emptyMap()); }
    Normalizer(Map<String, IdMap> maps, Map<String, String> classNamespace, boolean erase) { this(maps, classNamespace, Collections.emptyMap(), erase, new MemberUniverse(), Collections.emptyMap()); }
    Normalizer(Map<String, IdMap> maps, Map<String, String> classNamespace, Map<String, String> classMapping, boolean erase) {
      this(maps, classNamespace, classMapping, erase, new MemberUniverse(), Collections.emptyMap());
    }
    Normalizer(Map<String, IdMap> maps, Map<String, String> classNamespace, Map<String, String> classMapping,
               boolean erase, MemberUniverse members, Map<MemberKey, MemberKey> memberMapping) {
      this(maps, classNamespace, classMapping, erase, erase, members, memberMapping);
    }
    Normalizer(Map<String, IdMap> maps, Map<String, String> classNamespace, Map<String, String> classMapping,
               boolean erase, boolean eraseMembers, MemberUniverse members, Map<MemberKey, MemberKey> memberMapping) {
      this(maps, classNamespace, classMapping, erase, eraseMembers, members, memberMapping,
          erase, new StateSlotUniverse(), Collections.emptyMap(), null,
          erase, new LocalUniverse(), Collections.emptyMap(), null);
    }
    Normalizer(Map<String, IdMap> maps, Map<String, String> classNamespace, Map<String, String> classMapping,
               boolean erase, boolean eraseMembers, MemberUniverse members, Map<MemberKey, MemberKey> memberMapping,
               boolean eraseStateSlots, StateSlotUniverse stateSlots,
               Map<StateSlotKey, StateSlotKey> stateSlotMapping, StateSlotKey highlightedStateSlot) {
      this(maps, classNamespace, classMapping, erase, eraseMembers, members, memberMapping,
          eraseStateSlots, stateSlots, stateSlotMapping, highlightedStateSlot,
          erase, new LocalUniverse(), Collections.emptyMap(), null);
    }
    Normalizer(Map<String, IdMap> maps, Map<String, String> classNamespace, Map<String, String> classMapping,
               boolean erase, boolean eraseMembers, MemberUniverse members, Map<MemberKey, MemberKey> memberMapping,
               boolean eraseStateSlots, StateSlotUniverse stateSlots,
               Map<StateSlotKey, StateSlotKey> stateSlotMapping, StateSlotKey highlightedStateSlot,
               boolean eraseLocals, LocalUniverse locals,
               Map<LocalNodeKey, LocalNodeKey> localMapping, LocalNodeKey highlightedLocal) {
      this.maps = maps; this.classNamespace = classNamespace; this.classMapping = classMapping; this.erase = erase;
      this.eraseMembers = eraseMembers; this.members = members; this.memberMapping = memberMapping;
      this.eraseStateSlots = eraseStateSlots; this.stateSlots = stateSlots;
      this.stateSlotMapping = stateSlotMapping; this.highlightedStateSlot = highlightedStateSlot;
      this.eraseLocals = eraseLocals; this.locals = locals; this.localMapping = localMapping;
      this.highlightedLocal = highlightedLocal;
    }
    Map<String, String> mapping(String namespace) { IdMap map = maps.get(namespace); return map == null ? Collections.emptyMap() : map.mapping; }
    /* Member names and arbitrary metadata are never compiler-ID normalized.
     * Only typed internal class names (and descriptors/handles that contain an
     * exact owned internal name) may use the proved alpha-renaming map. */
    String known(String value, String namespace) { return value; }
    String internal(String value, String namespace) {
      if (value == null) return null;
      String selected = classNamespace.get(value);
      if (erase) {
        if (selected == null) return value;
        String mapped = classMapping.get(value);
        return eraseIds(mapped == null ? value : mapped);
      }
      String mappedClass = classMapping.get(value);
      if (mappedClass != null) return mappedClass;
      /* Only an exact owned-class hit supplies a compile-unit scope.  An ID in
       * an external class name is intentionally left byte-for-byte intact; it
       * will consequently compare exactly instead of being normalized under a
       * guessed namespace. */
      if (selected != null) throw new Failure("owned class reference lacks a proved class-node mapping: " + value);
      return value;
    }
    String descriptor(String value, String namespace) {
      if (value == null) return null;
      StringBuilder out = new StringBuilder();
      for (int i = 0; i < value.length();) {
        char c = value.charAt(i);
        if (c == 'L') {
          int end = value.indexOf(';', i);
          if (end < 0) return known(value, namespace);
          String internal = value.substring(i + 1, end);
          out.append('L').append(internal(internal, namespace)).append(';');
          i = end + 1;
        } else { out.append(c); i++; }
      }
      return out.toString();
    }
    String signature(String value, String namespace) { return known(value, namespace); }
    String structuralSymbol(String value, String namespace) {
      require(STRUCTURAL_GENSYM_SYMBOL.matcher(value).matches(), "not a proved structural gensym symbol: " + value);
      return erase ? eraseIds(value) : replaceIds(value, mapping(namespace), true);
    }
    String typeOperand(String value, String namespace) {
      return value != null && value.startsWith("[") ? descriptor(value, namespace) : internal(value, namespace);
    }
    String frameValues(Object[] values, String namespace) {
      List<String> rendered = new ArrayList<String>();
      for (Object value : values) {
        if (value instanceof String) rendered.add("TYPE:" + q(typeOperand((String) value, namespace)));
        else if (value instanceof LabelRef) rendered.add("UNINIT:L" + ((LabelRef) value).id);
        else rendered.add("TAG:" + String.valueOf(value));
      }
      return "[" + join(rendered) + "]";
    }
    String memberName(String owner, String kind, String name, String descriptor, String namespace) {
      MemberKey key = new MemberKey(owner, kind, name, descriptor);
      if (eraseMembers && members.generated.contains(key)) return generatedMemberSkeleton(name);
      MemberKey mapped = memberMapping.get(key);
      if (mapped != null) return mapped.name;
      if (!erase && members.generated.contains(key))
        throw new IncompleteMapping("generated member lacks a proved graph-node mapping: " + key);
      return name;
    }
    String stateSlot(StateSlotKey key, String namespace) {
      require(stateSlots.nodes.contains(key), "state-slot instruction is absent from its typed universe: " + key);
      String normalizedOwner = internal(key.machineOwner, namespace);
      if (eraseStateSlots) {
        String marker = key.equals(highlightedStateSlot) ? "SELF" : "SLOT";
        return "STATE_SLOT{" + q(normalizedOwner) + "," + marker + "}";
      }
      StateSlotKey mapped = stateSlotMapping.get(key);
      if (mapped == null) throw new IncompleteMapping("state slot lacks a proved graph-node mapping: " + key);
      require(mapped.machineOwner.equals(normalizedOwner),
          "state-slot witness crosses its mapped state-machine owner: " + key + " -> " + mapped +
          " expected-owner=" + normalizedOwner);
      return "STATE_SLOT{" + q(mapped.machineOwner) + "," + mapped.index + "}";
    }
    String localNode(LocalNodeKey key) {
      LocalNodeInfo info = locals.info.get(key);
      require(info != null, "local node is absent from its typed universe: " + key);
      if (info.boundary) return "FIXED_LOCAL";
      if (eraseLocals) return key.equals(highlightedLocal) ? "LOCAL{SELF:" + key.category + "}" :
          "LOCAL{NODE:" + key.category + "}";
      LocalNodeKey mapped = localMapping.get(key);
      if (mapped == null) throw new IncompleteMapping("local def-use node lacks a proved mapping: " + key);
      return "LOCAL{" + q(mapped.toString()) + "}";
    }
    String localInstruction(Insn instruction, int rawIndex) {
      LocalNodeKey key = locals.instruction(instruction);
      if (key == null || locals.info.get(key).boundary) return String.valueOf(rawIndex);
      return localNode(key);
    }
    String frameValue(Object value, String namespace) {
      if (value instanceof String) return "TYPE:" + q(typeOperand((String) value, namespace));
      if (value instanceof LabelRef) return "UNINIT:L" + ((LabelRef) value).id;
      return "TAG:" + String.valueOf(value);
    }
    String frame(Insn instruction, String namespace) {
      Object[] localValues = (Object[]) instruction.args.get(1);
      Object[] stackValues = (Object[]) instruction.args.get(2);
      Map<Integer, LocalNodeKey> cells = locals.frameNodes.get(instruction);
      List<String> fixed = new ArrayList<String>(), generated = new ArrayList<String>();
      int physical = 0;
      for (Object value : localValues) {
        LocalNodeKey key = cells == null ? null : cells.get(physical);
        String renderedValue = frameValue(value, namespace);
        if (key != null && !locals.info.get(key).boundary)
          generated.add(localNode(key) + "=" + renderedValue);
        else fixed.add(physical + "=" + renderedValue);
        physical += frameWidth(value);
      }
      Collections.sort(generated);
      List<String> stack = new ArrayList<String>();
      for (Object value : stackValues) stack.add(frameValue(value, namespace));
      return "FRAME(" + instruction.opcode + "," + instruction.args.get(0) + ",fixed=[" +
          join(fixed) + "],locals=[" + join(generated) + "],stack=[" + join(stack) + "])";
    }
    String localVariable(MethodModel method, LocalVariableModel variable, String namespace) {
      LocalNodeKey key = locals.variableNodes.get(variable);
      String index = key == null || locals.info.get(key).boundary ? String.valueOf(variable.index) : localNode(key);
      String name = variable.name;
      if (coreAsyncIocMetadataOnlyLvt(locals, method, variable)) {
        name = "CORE_ASYNC_IOC_METADATA_ONLY_LVT_NAME";
      } else if (coreAsyncAltsGeneratedLvtSpelling(locals, method, variable)) {
        name = eraseIds(name);
      } else if (coreAsyncAutoLocalTerminalSpelling(locals, method, variable)) {
        name = coreAsyncAutoLocalTerminalBase(variable);
      } else if (coreAsyncSharpLocalSpelling(locals, method, variable)) {
        name = coreAsyncSharpLocalPrefix(variable);
      } else if (coreAsyncDoAltVecLocalSpelling(locals, method, variable)) {
        name = coreAsyncDoAltVecLocalPrefix(variable);
      } else if (GENERATED_LOCAL_NAME.matcher(name).matches()) {
        name = eraseLocals ? eraseIds(name) : replaceIds(name, mapping(namespace), true);
      }
      return "LOCAL(" + q(name) + "," + q(descriptor(variable.descriptor, namespace)) + "," +
          q(signature(variable.signature, namespace)) + "," + controlRegionLabelPosition(method, variable.start) + "," +
          controlRegionLabelPosition(method, variable.end) + "," + index + ")";
    }
    String value(Object value, String namespace) {
      if (value == null) return "null";
      if (value instanceof Float) return "java.lang.Float:raw=0x" + Integer.toHexString(Float.floatToRawIntBits((Float) value));
      if (value instanceof Double) return "java.lang.Double:raw=0x" + Long.toHexString(Double.doubleToRawLongBits((Double) value));
      /* String constants are semantic data, not structural identifiers. */
      if (value instanceof String) return "java.lang.String:" + q((String) value);
      if (value instanceof Type) return "type:" + q(descriptor(((Type) value).getDescriptor(), namespace));
      if (value instanceof Handle) {
        Handle h = (Handle) value;
        boolean field = h.getTag() == Opcodes.H_GETFIELD || h.getTag() == Opcodes.H_GETSTATIC ||
            h.getTag() == Opcodes.H_PUTFIELD || h.getTag() == Opcodes.H_PUTSTATIC;
        return "handle:" + h.getTag() + ":" + q(internal(h.getOwner(), namespace)) + ":" +
            q(memberName(h.getOwner(), field ? "F" : "M", h.getName(), h.getDesc(), namespace)) + ":" +
            q(descriptor(h.getDesc(), namespace)) + ":" + h.isInterface();
      }
      if (value instanceof ConstantDynamic) {
        ConstantDynamic c = (ConstantDynamic) value;
        List<String> args = new ArrayList<String>();
        for (int i = 0; i < c.getBootstrapMethodArgumentCount(); i++) args.add(value(c.getBootstrapMethodArgument(i), namespace));
        return "condy:" + q(c.getName()) + ":" + q(descriptor(c.getDescriptor(), namespace)) + ":" + value(c.getBootstrapMethod(), namespace) + ":[" + join(args) + "]";
      }
      Class<?> c = value.getClass();
      if (c.isArray()) {
        List<String> items = new ArrayList<String>();
        for (int i = 0; i < Array.getLength(value); i++) items.add(value(Array.get(value, i), namespace));
        return "array:" + c.getComponentType().getName() + ":[" + join(items) + "]";
      }
      return c.getName() + ":" + q(String.valueOf(value));
    }
  }

  private static String eraseIds(String value) {
    if (value == null) return null;
    String result = INST_ID.matcher(value).replaceAll(Matcher.quoteReplacement("$inst_0__"));
    result = EVAL_ID.matcher(result).replaceAll(Matcher.quoteReplacement("$eval0"));
    result = DOUBLE_ID.matcher(result).replaceAll("__0");
    Matcher single = SINGLE_LOCAL_ID.matcher(result);
    return single.matches() ? single.group(1) + "0" : result;
  }

  private static final class CaptureSpec {
    final ClassModel owner;
    final MethodModel constructor;
    final List<FieldModel> fieldsByArgument;
    CaptureSpec(ClassModel owner, MethodModel constructor, List<FieldModel> fieldsByArgument) { this.owner = owner; this.constructor = constructor; this.fieldsByArgument = fieldsByArgument; }
    String key() { return owner.name + "\u0000" + constructor.descriptor; }
  }

  private static CaptureSpec captureSpec(ClassModel owner, MethodModel method) {
    if (!method.name.equals("<init>")) return null;
    List<Insn> code = new ArrayList<Insn>();
    for (Insn insn : method.insns) {
      if (insn.kind.equals("FRAME")) return null;
      if (!insn.kind.equals("LABEL") && !insn.is("INSN", Opcodes.NOP)) code.add(insn);
    }
    Type[] args = Type.getArgumentTypes(method.descriptor);
    if (args.length == 0 || code.size() != 3 + args.length * 3) return null;
    if (!code.get(0).is("VAR", Opcodes.ALOAD) || ((Integer) code.get(0).args.get(0)) != 0) return null;
    Insn superCall = code.get(1);
    if (!superCall.is("METHOD", Opcodes.INVOKESPECIAL) || !superCall.memberName().equals("<init>") || !superCall.descriptor().equals("()V") || !superCall.owner().equals(owner.superName)) return null;
    if (!code.get(code.size() - 1).is("INSN", Opcodes.RETURN)) return null;
    Map<String, FieldModel> instanceFields = new HashMap<String, FieldModel>();
    for (FieldModel field : owner.fields) if ((field.access & Opcodes.ACC_STATIC) == 0) instanceFields.put(field.name + "\u0000" + field.descriptor, field);
    if (instanceFields.size() != args.length) return null;
    List<FieldModel> byArg = new ArrayList<FieldModel>();
    Set<FieldModel> assigned = new HashSet<FieldModel>();
    int slot = 1;
    for (int i = 0; i < args.length; i++) {
      Insn self = code.get(2 + i * 3);
      Insn load = code.get(3 + i * 3);
      Insn put = code.get(4 + i * 3);
      if (!self.is("VAR", Opcodes.ALOAD) || ((Integer) self.args.get(0)) != 0) return null;
      if (!load.is("VAR", args[i].getOpcode(Opcodes.ILOAD)) || ((Integer) load.args.get(0)) != slot) return null;
      if (!put.is("FIELD", Opcodes.PUTFIELD) || !put.owner().equals(owner.name) || !put.descriptor().equals(args[i].getDescriptor())) return null;
      FieldModel field = instanceFields.get(put.memberName() + "\u0000" + put.descriptor());
      if (field == null || !assigned.add(field)) return null;
      byArg.add(field);
      slot += args[i].getSize();
    }
    if (assigned.size() != instanceFields.size()) return null;
    if (method.codeEvents.isEmpty() || !method.codeEvents.get(0).equals("CODE") ||
        !method.tryCatches.isEmpty()) return null;
    for (int i = 1; i < method.codeEvents.size(); i++)
      if (!method.codeEvents.get(i).startsWith("LINE(")) return null;
    return new CaptureSpec(owner, method, byArg);
  }

  private static Map<String, CaptureSpec> captureSpecs(Map<String, ClassModel> classes) {
    Map<String, CaptureSpec> result = new HashMap<String, CaptureSpec>();
    for (ClassModel model : classes.values()) for (MethodModel method : model.methods) {
      CaptureSpec spec = captureSpec(model, method);
      if (spec != null) {
        CaptureSpec old = result.put(spec.key(), spec);
        require(old == null, "duplicate captured constructor key " + spec.key());
      }
    }
    return result;
  }

  private static final class ActiveCapturePair {
    final Map<String, CaptureSpec> left = new HashMap<String, CaptureSpec>();
    final Map<String, CaptureSpec> right = new HashMap<String, CaptureSpec>();
  }

  private static String capturePairKey(CaptureSpec spec) {
    List<String> fields = new ArrayList<String>();
    for (FieldModel field : spec.fieldsByArgument)
      fields.add(captureFieldSkeleton(field) + ":" + eraseIds(field.descriptor));
    Collections.sort(fields);
    return eraseIds(spec.owner.name) + "\u0000" + join(fields);
  }

  private static List<String> captureArgumentOrder(CaptureSpec spec) {
    List<String> result = new ArrayList<String>();
    for (FieldModel field : spec.fieldsByArgument)
      result.add(captureFieldSkeleton(field) + ":" + eraseIds(field.descriptor));
    return result;
  }

  private static String captureFieldSkeleton(FieldModel field) {
    return eligibleGeneratedMember(field.access, field.name) ? generatedMemberSkeleton(field.name) : field.name;
  }

  private static ActiveCapturePair activeCapturePairs(Map<String, ClassModel> left,
                                                       Map<String, ClassModel> right) {
    Map<String, CaptureSpec> allLeft = captureSpecs(left), allRight = captureSpecs(right);
    Map<String, List<CaptureSpec>> keyedLeft = new HashMap<String, List<CaptureSpec>>(), keyedRight = new HashMap<String, List<CaptureSpec>>();
    for (CaptureSpec spec : allLeft.values()) keyedLeft.computeIfAbsent(capturePairKey(spec), ignored -> new ArrayList<CaptureSpec>()).add(spec);
    for (CaptureSpec spec : allRight.values()) keyedRight.computeIfAbsent(capturePairKey(spec), ignored -> new ArrayList<CaptureSpec>()).add(spec);
    require(keyedLeft.keySet().equals(keyedRight.keySet()),
        "trivial all-field constructor sets differ between comparison sides: left=" + keyedLeft.size() +
        " right=" + keyedRight.size() + " left-only=" + firstValues(difference(keyedLeft.keySet(), keyedRight.keySet()), 5) +
        " right-only=" + firstValues(difference(keyedRight.keySet(), keyedLeft.keySet()), 5));
    ActiveCapturePair result = new ActiveCapturePair();
    for (String key : keyedLeft.keySet()) {
      List<CaptureSpec> leftGroup = keyedLeft.get(key), rightGroup = keyedRight.get(key);
      require(leftGroup.size() == rightGroup.size(), "captured-constructor pairing multiplicity differs for " + key);
      List<String> leftOrders = new ArrayList<String>(), rightOrders = new ArrayList<String>();
      for (CaptureSpec spec : leftGroup) leftOrders.add(join(captureArgumentOrder(spec)));
      for (CaptureSpec spec : rightGroup) rightOrders.add(join(captureArgumentOrder(spec)));
      Collections.sort(leftOrders); Collections.sort(rightOrders);
      boolean ownsGeneratedNode = false;
      for (CaptureSpec spec : leftGroup) for (FieldModel field : spec.fieldsByArgument)
        if (spec.fieldsByArgument.size() > 1 && eligibleGeneratedMember(field.access, field.name)) ownsGeneratedNode = true;
      if (leftOrders.equals(rightOrders) && !ownsGeneratedNode) continue;
      /* If an erased class/member shape has several instances, activate the
       * whole shape group.  This does not choose a pairing or assert equality:
       * every constructor and every use site must independently satisfy the
       * semantics-preserving normalization, and the later class/member graph
       * isomorphism still has to pair and compare the complete corpus. */
      for (CaptureSpec a : leftGroup) result.left.put(a.key(), a);
      for (CaptureSpec b : rightGroup) result.right.put(b.key(), b);
    }
    return result;
  }

  private static final class AbstractValue {
    final String descriptor;
    AbstractValue(String descriptor) { this.descriptor = descriptor; }
  }

  private static boolean reference(String descriptor) { return descriptor.equals("NULL") || descriptor.startsWith("L") || descriptor.startsWith("[") || descriptor.equals("A"); }
  private static boolean compatible(String actual, String expected) {
    if (reference(expected)) return reference(actual);
    if ("ZBCSI".contains(expected) && actual.equals("I")) return true;
    return actual.equals(expected);
  }

  private static FieldModel declaredOwnInstanceField(ClassModel owner, Insn insn) {
    if (owner == null || !insn.owner().equals(owner.name)) return null;
    for (FieldModel field : owner.fields)
      if (field.name.equals(insn.memberName()) && field.descriptor.equals(insn.descriptor()) &&
          (field.access & (Opcodes.ACC_STATIC | Opcodes.ACC_VOLATILE)) == 0) return field;
    return null;
  }

  private static String safeBlock(List<Insn> code, int start, int end, String expected, Normalizer normalizer,
                                  String namespace, ClassModel currentClass, boolean receiverIsThis) {
    List<AbstractValue> stack = new ArrayList<AbstractValue>();
    Set<Integer> loadedReferences = new HashSet<Integer>();
    Set<String> loadedFields = new HashSet<String>();
    for (int i = start; i < end; i++) {
      Insn insn = code.get(i);
      if (insn.kind.equals("VAR")) {
        int slot = (Integer) insn.args.get(0);
        switch (insn.opcode) {
          case Opcodes.ALOAD: stack.add(new AbstractValue("A")); loadedReferences.add(slot); break;
          case Opcodes.ILOAD: stack.add(new AbstractValue("I")); break;
          case Opcodes.LLOAD: stack.add(new AbstractValue("J")); break;
          case Opcodes.FLOAD: stack.add(new AbstractValue("F")); break;
          case Opcodes.DLOAD: stack.add(new AbstractValue("D")); break;
          case Opcodes.ASTORE:
            if (stack.isEmpty() || !reference(stack.get(stack.size() - 1).descriptor) || i == start || !code.get(i - 1).is("INSN", Opcodes.ACONST_NULL) || !loadedReferences.contains(slot)) return null;
            stack.remove(stack.size() - 1); break;
          default: return null;
        }
      } else if (insn.kind.equals("INSN")) {
        switch (insn.opcode) {
          case Opcodes.ACONST_NULL: stack.add(new AbstractValue("NULL")); break;
          case Opcodes.ICONST_M1: case Opcodes.ICONST_0: case Opcodes.ICONST_1: case Opcodes.ICONST_2: case Opcodes.ICONST_3: case Opcodes.ICONST_4: case Opcodes.ICONST_5: stack.add(new AbstractValue("I")); break;
          case Opcodes.LCONST_0: case Opcodes.LCONST_1: stack.add(new AbstractValue("J")); break;
          case Opcodes.FCONST_0: case Opcodes.FCONST_1: case Opcodes.FCONST_2: stack.add(new AbstractValue("F")); break;
          case Opcodes.DCONST_0: case Opcodes.DCONST_1: stack.add(new AbstractValue("D")); break;
          default: return null;
        }
      } else if (insn.kind.equals("INT") && (insn.opcode == Opcodes.BIPUSH || insn.opcode == Opcodes.SIPUSH)) {
        stack.add(new AbstractValue("I"));
      } else if (insn.kind.equals("LDC")) {
        Object value = insn.args.get(0);
        if (value instanceof Long) stack.add(new AbstractValue("J"));
        else if (value instanceof Double) stack.add(new AbstractValue("D"));
        else if (value instanceof Float) stack.add(new AbstractValue("F"));
        else if (value instanceof Integer) stack.add(new AbstractValue("I"));
        else if (value instanceof String) stack.add(new AbstractValue("A"));
        else return null;
      } else if (insn.kind.equals("FIELD") && insn.opcode == Opcodes.GETFIELD) {
        if (!receiverIsThis || declaredOwnInstanceField(currentClass, insn) == null || i == start || !code.get(i - 1).is("VAR", Opcodes.ALOAD) ||
            ((Integer) code.get(i - 1).args.get(0)) != 0 || stack.isEmpty() || !reference(stack.get(stack.size() - 1).descriptor)) return null;
        stack.remove(stack.size() - 1);
        stack.add(new AbstractValue(insn.descriptor()));
        loadedFields.add(insn.owner() + "\u0000" + insn.memberName() + "\u0000" + insn.descriptor());
      } else if (insn.kind.equals("FIELD") && insn.opcode == Opcodes.PUTFIELD) {
        String fieldKey = insn.owner() + "\u0000" + insn.memberName() + "\u0000" + insn.descriptor();
        if (!receiverIsThis || declaredOwnInstanceField(currentClass, insn) == null || i < start + 2 || !code.get(i - 1).is("INSN", Opcodes.ACONST_NULL) ||
            !code.get(i - 2).is("VAR", Opcodes.ALOAD) || ((Integer) code.get(i - 2).args.get(0)) != 0 ||
            !loadedFields.contains(fieldKey) || stack.size() < 2 ||
            !reference(stack.get(stack.size() - 1).descriptor) || !reference(stack.get(stack.size() - 2).descriptor)) return null;
        stack.remove(stack.size() - 1);
        stack.remove(stack.size() - 1);
      } else return null;
    }
    if (stack.size() != 1 || !compatible(stack.get(0).descriptor, expected)) return null;
    List<String> tokens = new ArrayList<String>();
    for (int i = start; i < end; i++) tokens.add(code.get(i).render(normalizer, namespace));
    return "[" + join(tokens) + "]";
  }

  private static final class Partition {
    final List<String> blocks = new ArrayList<String>();
  }

  private static int[] uniquePartitionBoundaries(List<Insn> code, int start, int end, Type[] types,
                                                  Normalizer n, String namespace, ClassModel currentClass,
                                                  boolean receiverIsThis) {
    List<int[]> results = new ArrayList<int[]>();
    partitionBoundaries(code, start, end, types, 0, new int[types.length + 1], results, n, namespace, currentClass, receiverIsThis);
    require(results.size() == 1, "partition boundaries are incomplete or ambiguous");
    validatePartitionEffects(code, results.get(0));
    return results.get(0);
  }

  private static void validatePartitionEffects(List<Insn> code, int[] bounds) {
    List<Set<String>> fieldReads = new ArrayList<Set<String>>();
    List<Set<String>> fieldWrites = new ArrayList<Set<String>>();
    List<Set<Integer>> localReads = new ArrayList<Set<Integer>>();
    List<Set<Integer>> localWrites = new ArrayList<Set<Integer>>();
    for (int block = 0; block + 1 < bounds.length; block++) {
      Set<String> reads = new HashSet<String>(), writes = new HashSet<String>();
      Set<Integer> loads = new HashSet<Integer>(), stores = new HashSet<Integer>();
      for (int i = bounds[block]; i < bounds[block + 1]; i++) {
        Insn insn = code.get(i);
        if (insn.kind.equals("FIELD")) {
          String key = insn.owner() + "\u0000" + insn.memberName() + "\u0000" + insn.descriptor();
          if (insn.opcode == Opcodes.GETFIELD) reads.add(key);
          else if (insn.opcode == Opcodes.PUTFIELD) writes.add(key);
        } else if (insn.kind.equals("VAR")) {
          int slot = (Integer) insn.args.get(0);
          if (isLoad(insn.opcode)) loads.add(slot);
          else if (insn.opcode == Opcodes.ASTORE) stores.add(slot);
        }
      }
      require(reads.containsAll(writes), "capture argument clears a field it did not first read");
      require(loads.containsAll(stores), "capture argument clears a local it did not first read");
      fieldReads.add(reads); fieldWrites.add(writes); localReads.add(loads); localWrites.add(stores);
    }
    for (int i = 0; i < fieldReads.size(); i++) for (int j = i + 1; j < fieldReads.size(); j++) {
      Set<String> accessedJ = new HashSet<String>(fieldReads.get(j)); accessedJ.addAll(fieldWrites.get(j));
      Set<String> accessedI = new HashSet<String>(fieldReads.get(i)); accessedI.addAll(fieldWrites.get(i));
      require(Collections.disjoint(fieldWrites.get(i), accessedJ) && Collections.disjoint(fieldWrites.get(j), accessedI),
          "capture argument field effects are not independent across reordered blocks");
      require(Collections.disjoint(localWrites.get(i), localReads.get(j)) && Collections.disjoint(localWrites.get(j), localReads.get(i)) &&
          Collections.disjoint(localWrites.get(i), localWrites.get(j)),
          "capture argument local effects are not independent across reordered blocks");
    }
  }

  private static void partitions(List<Insn> code, int position, int end, Type[] types, int arg, Normalizer n,
                                 String namespace, ClassModel currentClass, boolean receiverIsThis,
                                 List<String> blocks, List<Partition> results) {
    if (results.size() > 1) return;
    if (arg == types.length) {
      if (position == end) { Partition p = new Partition(); p.blocks.addAll(blocks); results.add(p); }
      return;
    }
    int remaining = types.length - arg - 1;
    for (int next = position + 1; next <= end - remaining; next++) {
      String block = safeBlock(code, position, next, types[arg].getDescriptor(), n, namespace, currentClass, receiverIsThis);
      if (block != null) {
        blocks.add(block);
        partitions(code, next, end, types, arg + 1, n, namespace, currentClass, receiverIsThis, blocks, results);
        blocks.remove(blocks.size() - 1);
      }
    }
  }

  private static final class RenderedMethod {
    final String text;
    final int collapsedSites;
    final int sourceSetLiterals;
    final int discardedLocalNoops;
    final int stateSlotAccesses;
    RenderedMethod(String text, int collapsedSites, int sourceSetLiterals, int discardedLocalNoops,
                   int stateSlotAccesses) {
      this.text = text; this.collapsedSites = collapsedSites; this.sourceSetLiterals = sourceSetLiterals;
      this.discardedLocalNoops = discardedLocalNoops; this.stateSlotAccesses = stateSlotAccesses;
    }
  }

  private static final class DelegateCapture {
    final CaptureSpec target;
    final List<String> assignments;
    final List<String> parameters;
    DelegateCapture(CaptureSpec target, List<String> assignments, List<String> parameters) {
      this.target = target;
      this.assignments = assignments;
      this.parameters = parameters;
    }
  }

  /* Recognize the auxiliary constructor emitted for deftype/reify classes.  It
   * must do exactly one thing: pass all values (and possibly fixed defaults such
   * as null metadata) to the proved full captured-field constructor. */
  private static DelegateCapture delegateCapture(ClassModel owner, MethodModel method,
                                                   Normalizer n, Map<String, CaptureSpec> captures) {
    if (!method.name.equals("<init>")) return null;
    List<Insn> code = new ArrayList<Insn>();
    for (Insn insn : method.insns) {
      if (insn.kind.equals("FRAME")) return null;
      if (!insn.kind.equals("LABEL") && !insn.is("INSN", Opcodes.NOP)) code.add(insn);
    }
    if (code.size() < 3 || !code.get(0).is("VAR", Opcodes.ALOAD) || ((Integer) code.get(0).args.get(0)) != 0 ||
        !code.get(code.size() - 1).is("INSN", Opcodes.RETURN)) return null;
    Insn call = code.get(code.size() - 2);
    if (!call.is("METHOD", Opcodes.INVOKESPECIAL) || !call.owner().equals(owner.name) || !call.memberName().equals("<init>")) return null;
    CaptureSpec target = captures.get(owner.name + "\u0000" + call.descriptor());
    if (target == null) return null;
    Type[] targetTypes = Type.getArgumentTypes(target.constructor.descriptor);
    List<Partition> ps = new ArrayList<Partition>();
    partitions(code, 1, code.size() - 2, targetTypes, 0, n, owner.namespace, owner, true, new ArrayList<String>(), ps);
    require(ps.size() == 1, "auxiliary captured constructor partition is incomplete or ambiguous in " + owner.name + method.descriptor);
    uniquePartitionBoundaries(code, 1, code.size() - 2, targetTypes, n, owner.namespace, owner, true);

    Type[] ownTypes = Type.getArgumentTypes(method.descriptor);
    Map<Integer, Type> ownSlots = new HashMap<Integer, Type>();
    int slot = 1;
    for (Type type : ownTypes) { ownSlots.put(slot, type); slot += type.getSize(); }
    Set<Integer> consumedSlots = new HashSet<Integer>();
    List<String> assignments = new ArrayList<String>();
    List<String> parameters = new ArrayList<String>();
    for (int i = 0; i < targetTypes.length; i++) {
      FieldModel targetField = target.fieldsByArgument.get(i);
      String field = n.memberName(target.owner.name, "F", targetField.name, targetField.descriptor, target.owner.namespace);
      String fieldDesc = n.descriptor(target.fieldsByArgument.get(i).descriptor, target.owner.namespace);
      String block = ps.get(0).blocks.get(i);
      List<Insn> rawBlock = blockSliceForPartition(code, 1, code.size() - 2, targetTypes, i, n, owner.namespace, owner, true);
      if (rawBlock.size() == 1 && rawBlock.get(0).kind.equals("VAR") && isLoad(rawBlock.get(0).opcode)) {
        int parameterSlot = (Integer) rawBlock.get(0).args.get(0);
        Type ownType = ownSlots.get(parameterSlot);
        require(ownType != null && compatible(loadDescriptor(rawBlock.get(0).opcode), ownType.getDescriptor()) &&
            compatible(ownType.getDescriptor(), targetTypes[i].getDescriptor()),
            "auxiliary captured constructor forwards an incompatible local in " + owner.name + method.descriptor);
        require(consumedSlots.add(parameterSlot), "auxiliary captured constructor reuses a parameter in " + owner.name + method.descriptor);
        assignments.add(q(field) + "=PARAM");
        parameters.add(q(field) + ":field=" + q(fieldDesc) + ":parameter=" +
            q(n.descriptor(ownType.getDescriptor(), owner.namespace)));
      } else {
        assignments.add(q(field) + "=DEFAULT" + block);
      }
    }
    require(consumedSlots.equals(ownSlots.keySet()), "auxiliary captured constructor does not forward each declared parameter exactly once in " + owner.name + method.descriptor);
    Collections.sort(assignments);
    Collections.sort(parameters);
    return new DelegateCapture(target, assignments, parameters);
  }

  private static boolean isLoad(int opcode) {
    return opcode == Opcodes.ALOAD || opcode == Opcodes.ILOAD || opcode == Opcodes.LLOAD || opcode == Opcodes.FLOAD || opcode == Opcodes.DLOAD;
  }

  private static String loadDescriptor(int opcode) {
    if (opcode == Opcodes.ALOAD) return "A";
    if (opcode == Opcodes.ILOAD) return "I";
    if (opcode == Opcodes.LLOAD) return "J";
    if (opcode == Opcodes.FLOAD) return "F";
    if (opcode == Opcodes.DLOAD) return "D";
    throw new Failure("not a load opcode: " + opcode);
  }

  /* Re-run the unique partition and return one raw block.  This intentionally
   * favors auditability over clever state sharing; auxiliary constructors are
   * rare and tiny. */
  private static List<Insn> blockSliceForPartition(List<Insn> code, int start, int end, Type[] types,
                                                   int wanted, Normalizer n, String namespace, ClassModel currentClass,
                                                   boolean receiverIsThis) {
    int[] bounds = uniquePartitionBoundaries(code, start, end, types, n, namespace, currentClass, receiverIsThis);
    return new ArrayList<Insn>(code.subList(bounds[wanted], bounds[wanted + 1]));
  }

  private static final Set<String> MEMOIZE_FIFO_CLASS_SET = Collections.unmodifiableSet(new HashSet<String>(Arrays.asList(
      "clojure.lang.IFn", "clojure.lang.AFn", "java.lang.Runnable", "java.util.concurrent.Callable")));

  private static String structuralGensymAt(List<Insn> code, int start) {
    if (start + 2 >= code.size() || !code.get(start).is("INSN", Opcodes.ACONST_NULL) ||
        !code.get(start + 1).kind.equals("LDC") || !(code.get(start + 1).args.get(0) instanceof String)) return null;
    String value = (String) code.get(start + 1).args.get(0);
    Insn call = code.get(start + 2);
    if (!STRUCTURAL_GENSYM_SYMBOL.matcher(value).matches() || !call.is("METHOD", Opcodes.INVOKESTATIC) ||
        !call.owner().equals("clojure/lang/Symbol") || !call.memberName().equals("intern") ||
        !call.descriptor().equals("(Ljava/lang/String;Ljava/lang/String;)Lclojure/lang/Symbol;")) return null;
    return value;
  }

  private static List<String> structuralGensyms(ClassModel model) {
    List<String> result = new ArrayList<String>();
    for (MethodModel method : model.methods) for (int i = 0; i < method.insns.size(); i++) {
      String value = structuralGensymAt(method.insns, i);
      if (value != null) { result.add(value); i += 2; }
    }
    return result;
  }

  private static Integer pushedInt(Insn insn) {
    if (insn.kind.equals("INSN") && insn.opcode >= Opcodes.ICONST_M1 && insn.opcode <= Opcodes.ICONST_5)
      return insn.opcode - Opcodes.ICONST_0;
    if (insn.kind.equals("INT") && (insn.opcode == Opcodes.BIPUSH || insn.opcode == Opcodes.SIPUSH))
      return (Integer) insn.args.get(0);
    if (insn.kind.equals("LDC") && insn.args.get(0) instanceof Integer) return (Integer) insn.args.get(0);
    return null;
  }

  private static boolean pureLocalDiscard(List<Insn> code, int start) {
    if (start + 1 >= code.size() || !code.get(start).kind.equals("VAR") || !code.get(start + 1).kind.equals("INSN")) return false;
    int load = code.get(start).opcode, discard = code.get(start + 1).opcode;
    return ((load == Opcodes.ALOAD || load == Opcodes.ILOAD || load == Opcodes.FLOAD) && discard == Opcodes.POP) ||
        ((load == Opcodes.LLOAD || load == Opcodes.DLOAD) && discard == Opcodes.POP2);
  }

  private static final class SourceSetLiteral {
    final int end;
    final String rendered;
    SourceSetLiteral(int end, String rendered) { this.end = end; this.rendered = rendered; }
  }

  /* Exact-source bounded quotient for memoize.clj:335.  Clojure 1.11 emits
   * this one source set in different hash-iteration orders across fresh JVMs.
   * No arbitrary array, LDC sequence, or set construction enters the rule. */
  private static SourceSetLiteral memoizeFifoSourceSet(ClassModel owner, MethodModel method, int start) {
    if (!owner.name.startsWith("clojure/core/memoize$") || !method.name.equals("<clinit>") ||
        !method.descriptor.equals("()V")) return null;
    List<Insn> code = method.insns;
    if (start + 2 >= code.size() || !Objects.equals(pushedInt(code.get(start)), 4) ||
        !code.get(start + 1).is("TYPE", Opcodes.ANEWARRAY) ||
        !code.get(start + 1).args.get(0).equals("java/lang/Object")) return null;
    int at = start + 2;
    String kind = null;
    List<String> values = new ArrayList<String>();
    for (int index = 0; index < 4; index++) {
      if (at + 4 >= code.size() || !code.get(at).is("INSN", Opcodes.DUP) ||
          !Objects.equals(pushedInt(code.get(at + 1)), index)) return null;
      at += 2;
      String value;
      String currentKind;
      if (at + 2 < code.size() && code.get(at).kind.equals("LDC") && code.get(at).args.get(0) instanceof String &&
          code.get(at + 1).is("METHOD", Opcodes.INVOKESTATIC) && code.get(at + 1).owner().equals("clojure/lang/RT") &&
          code.get(at + 1).memberName().equals("classForName") &&
          code.get(at + 1).descriptor().equals("(Ljava/lang/String;)Ljava/lang/Class;")) {
        value = (String) code.get(at).args.get(0); currentKind = "CLASS"; at += 2;
      } else if (at + 3 < code.size() && code.get(at).is("INSN", Opcodes.ACONST_NULL) &&
          code.get(at + 1).kind.equals("LDC") && code.get(at + 1).args.get(0) instanceof String &&
          code.get(at + 2).is("METHOD", Opcodes.INVOKESTATIC) && code.get(at + 2).owner().equals("clojure/lang/Symbol") &&
          code.get(at + 2).memberName().equals("intern") &&
          code.get(at + 2).descriptor().equals("(Ljava/lang/String;Ljava/lang/String;)Lclojure/lang/Symbol;")) {
        value = (String) code.get(at + 1).args.get(0); currentKind = "SYMBOL"; at += 3;
      } else return null;
      if (!MEMOIZE_FIFO_CLASS_SET.contains(value) || (kind != null && !kind.equals(currentKind))) return null;
      kind = currentKind; values.add(value);
      if (at >= code.size() || !code.get(at).is("INSN", Opcodes.AASTORE)) return null;
      at++;
    }
    if (new HashSet<String>(values).size() != 4 || at >= code.size()) return null;
    Insn create = code.get(at);
    if (!create.is("METHOD", Opcodes.INVOKESTATIC) || !create.owner().equals("clojure/lang/PersistentHashSet") ||
        !create.memberName().equals("create") ||
        !create.descriptor().equals("([Ljava/lang/Object;)Lclojure/lang/PersistentHashSet;")) return null;
    Collections.sort(values);
    List<String> rendered = new ArrayList<String>(); for (String value : values) rendered.add(q(value));
    return new SourceSetLiteral(at + 1, "SOURCE_SET{memoize.clj:335," + kind + ",[" + join(rendered) + "]}");
  }

  private static void partitionBoundaries(List<Insn> code, int position, int end, Type[] types, int arg,
                                          int[] boundaries, List<int[]> results, Normalizer n, String namespace,
                                          ClassModel currentClass, boolean receiverIsThis) {
    if (results.size() > 1) return;
    boundaries[arg] = position;
    if (arg == types.length) {
      if (position == end) results.add(Arrays.copyOf(boundaries, boundaries.length));
      return;
    }
    int remaining = types.length - arg - 1;
    for (int next = position + 1; next <= end - remaining; next++) {
      if (safeBlock(code, position, next, types[arg].getDescriptor(), n, namespace, currentClass, receiverIsThis) != null)
        partitionBoundaries(code, next, end, types, arg + 1, boundaries, results, n, namespace, currentClass, receiverIsThis);
    }
  }

  private static RenderedMethod renderMethod(ClassModel owner, MethodModel method, Normalizer n, Map<String, CaptureSpec> captures) {
    CaptureSpec ownCapture = captures.get(owner.name + "\u0000" + method.descriptor);
    DelegateCapture delegate = ownCapture == null ? delegateCapture(owner, method, n, captures) : null;
    List<String> events = new ArrayList<String>(); for (String e : method.events) events.add(n.known(e, owner.namespace));
    List<String> codeEvents = new ArrayList<String>(); for (String e : method.codeEvents) codeEvents.add(n.known(e, owner.namespace));
    List<String> localVariableEvents = new ArrayList<String>();
    for (LocalVariableModel variable : method.localVariables)
      localVariableEvents.add(n.localVariable(method, variable, owner.namespace));
    Collections.sort(localVariableEvents);
    codeEvents.addAll(localVariableEvents);
    List<String> exceptions = new ArrayList<String>(); for (String e : method.exceptions) exceptions.add(n.internal(e, owner.namespace));
    String desc;
    List<String> code = new ArrayList<String>();
    int collapsed = 0;
    int sourceSetLiterals = 0;
    int discardedLocalNoops = 0;
    int stateSlotAccesses = 0;
    if (ownCapture != null) {
      require(events.isEmpty(), "captured constructor has method annotations/parameters: " + owner.name + method.descriptor);
      List<String> assignments = new ArrayList<String>();
      for (FieldModel field : ownCapture.fieldsByArgument) assignments.add(q(n.memberName(owner.name, "F", field.name, field.descriptor, owner.namespace)) + ":" + q(n.descriptor(field.descriptor, owner.namespace)));
      Collections.sort(assignments);
      desc = "CAPTURE{" + join(assignments) + "}";
      code.add("CAPTURE_ASSIGN{" + join(assignments) + "}");
    } else if (delegate != null) {
      require(events.isEmpty(), "auxiliary captured constructor has method annotations/parameters: " + owner.name + method.descriptor);
      desc = "CAPTURE_DELEGATE{fields=" + join(delegate.parameters) + "}";
      code.add("CAPTURE_DELEGATE_ASSIGN{" + join(delegate.assignments) + "}");
    } else {
      desc = n.descriptor(method.descriptor, owner.namespace);
      Set<Integer> controlLabels = stateControlLabels(method);
      for (int i = 0; i < method.insns.size();) {
        if (method.insns.get(i).kind.equals("LABEL") &&
            !controlLabels.contains(((LabelRef) method.insns.get(i).args.get(0)).id)) {
          /* Debug-only labels may move within one control-flow region between
           * equivalent AOTs. LVT ranges are compared by control region, while
           * jump/switch/try/frame-uninitialized/type-annotation anchors remain. */
          i++; continue;
        }
        if (method.insns.get(i).kind.equals("FRAME")) {
          code.add(n.frame(method.insns.get(i), owner.namespace)); i++; continue;
        }
        StateSlotKey stateSlot = n.stateSlots.key(method.insns.get(i));
        if (stateSlot != null) {
          code.add(n.stateSlot(stateSlot, owner.namespace)); stateSlotAccesses++; i++; continue;
        }
        if (pureLocalDiscard(method.insns, i)) { discardedLocalNoops++; i += 2; continue; }
        String structuralSymbol = structuralGensymAt(method.insns, i);
        if (structuralSymbol != null) {
          code.add("STRUCTURAL_GENSYM_SYMBOL{" + q(n.structuralSymbol(structuralSymbol, owner.namespace)) + "}");
          i += 3; continue;
        }
        SourceSetLiteral setLiteral = memoizeFifoSourceSet(owner, method, i);
        if (setLiteral != null) {
          code.add(setLiteral.rendered); sourceSetLiterals++; i = setLiteral.end; continue;
        }
        Insn insn = method.insns.get(i);
        if (insn.is("TYPE", Opcodes.NEW)) {
          String constructedOwner = (String) insn.args.get(0);
          List<Integer> candidates = new ArrayList<Integer>();
          for (int j = i + 2; j < method.insns.size(); j++) {
            Insn possible = method.insns.get(j);
            if (possible.kind.equals("LABEL") || possible.kind.equals("JUMP") || possible.kind.equals("TABLESWITCH") || possible.kind.equals("LOOKUPSWITCH")) break;
            if (possible.is("METHOD", Opcodes.INVOKESPECIAL) && possible.owner().equals(constructedOwner) && possible.memberName().equals("<init>") && captures.containsKey(constructedOwner + "\u0000" + possible.descriptor())) candidates.add(j);
          }
          if (!candidates.isEmpty()) {
            if (i + 1 >= method.insns.size() || !method.insns.get(i + 1).is("INSN", Opcodes.DUP)) {
              code.add(insn.render(n, owner.namespace)); i++; continue;
            }
            List<String> successful = new ArrayList<String>();
            int successfulEnd = -1;
            for (Integer end : candidates) {
              CaptureSpec spec = captures.get(constructedOwner + "\u0000" + method.insns.get(end).descriptor());
              Type[] types = Type.getArgumentTypes(spec.constructor.descriptor);
              List<Partition> ps = new ArrayList<Partition>();
              boolean receiverIsThis = (method.access & Opcodes.ACC_STATIC) == 0;
              partitions(method.insns, i + 2, end, types, 0, n, owner.namespace, owner, receiverIsThis, new ArrayList<String>(), ps);
              if (ps.size() == 1) {
                try {
                  uniquePartitionBoundaries(method.insns, i + 2, end, types, n, owner.namespace, owner, receiverIsThis);
                  List<String> assignments = new ArrayList<String>();
                  for (int k = 0; k < types.length; k++) {
                    FieldModel field = spec.fieldsByArgument.get(k);
                    assignments.add(q(n.memberName(spec.owner.name, "F", field.name, field.descriptor, spec.owner.namespace)) + "=" + ps.get(0).blocks.get(k));
                  }
                  Collections.sort(assignments);
                  successful.add("CONSTRUCT{" + q(n.internal(constructedOwner, owner.namespace)) + "," + join(assignments) + "}");
                  successfulEnd = end;
                } catch (Failure unsafeEffects) {
                  if (unsafeEffects instanceof IncompleteMapping) throw unsafeEffects;
                  /* Leave this construction exact below. */
                }
              }
            }
            if (successful.size() == 1) {
              code.add(successful.get(0));
              collapsed++;
              i = successfulEnd + 1;
              continue;
            }
          }
        }
        code.add(insn.render(n, owner.namespace));
        i++;
      }
    }
    /* max_stack/max_locals are derived verifier metadata.  They are deliberately
     * omitted from the semantic model because a proved argument permutation can
     * change the optimal max_stack.  The gate separately defines and verifies
     * every candidate class with -Xverify:all before accepting this quotient. */
    String text = "METHOD{" + method.access + "," + q(n.memberName(owner.name, "M", method.name, method.descriptor, owner.namespace)) + "," + q(desc) + "," + q(n.signature(method.signature, owner.namespace)) +
        ",[" + join(exceptions) + "],[" + join(events) + "],[" + join(codeEvents) + "],[" + join(code) + "]}";
    return new RenderedMethod(text, collapsed, sourceSetLiterals, discardedLocalNoops, stateSlotAccesses);
  }

  private static final class RenderedClass {
    final String name;
    final String text;
    final int collapsedSites;
    final int sourceSetLiterals;
    final int discardedLocalNoops;
    final int stateSlotAccesses;
    RenderedClass(String name, String text, int collapsedSites, int sourceSetLiterals,
                  int discardedLocalNoops, int stateSlotAccesses) {
      this.name = name; this.text = text; this.collapsedSites = collapsedSites; this.sourceSetLiterals = sourceSetLiterals;
      this.discardedLocalNoops = discardedLocalNoops; this.stateSlotAccesses = stateSlotAccesses;
    }
  }

  private static RenderedClass renderClass(ClassModel model, Normalizer n, Map<String, CaptureSpec> captures) {
    require(model.unsupported.isEmpty(), "unsupported class-file features in " + model.name + ": " + model.unsupported);
    List<String> interfaces = new ArrayList<String>(); for (String i : model.interfaces) interfaces.add(n.internal(i, model.namespace));
    List<String> events = new ArrayList<String>(); for (String e : model.events) events.add(n.known(e, model.namespace));
    List<String> fields = renderFields(model, n, captures);
    List<String> methods = new ArrayList<String>(); int collapsed = 0, sourceSets = 0, localNoops = 0, stateAccesses = 0;
    for (MethodModel m : model.methods) { RenderedMethod rendered = renderMethod(model, m, n, captures); methods.add(rendered.text); collapsed += rendered.collapsedSites; sourceSets += rendered.sourceSetLiterals; localNoops += rendered.discardedLocalNoops; stateAccesses += rendered.stateSlotAccesses; }
    String name = n.internal(model.name, model.namespace);
    String text = "CLASS{" + model.version + "," + model.access + "," + q(name) + "," + q(n.signature(model.signature, model.namespace)) + "," + q(n.internal(model.superName, model.namespace)) +
        ",[" + join(interfaces) + "],[" + join(events) + "],[" + join(fields) + "],[" + join(methods) + "]}";
    return new RenderedClass(name, text, collapsed, sourceSets, localNoops, stateAccesses);
  }

  private static List<String> renderFields(ClassModel model, Normalizer n, Map<String, CaptureSpec> captures) {
    Set<String> reorderable = new HashSet<String>();
    for (CaptureSpec spec : captures.values()) if (spec.owner == model)
      for (FieldModel field : spec.fieldsByArgument) reorderable.add(field.name + "\u0000" + field.descriptor);
    if (reorderable.isEmpty()) {
      List<String> exact = new ArrayList<String>();
      for (FieldModel field : model.fields) exact.add(field.render(n, model.namespace, model.name));
      return exact;
    }
    int first = Integer.MAX_VALUE, last = -1, count = 0;
    for (int i = 0; i < model.fields.size(); i++) {
      FieldModel field = model.fields.get(i);
      if (reorderable.contains(field.name + "\u0000" + field.descriptor)) {
        first = Math.min(first, i); last = i; count++;
      }
    }
    require(count == reorderable.size(), "proved captured-field set is incomplete in " + model.name);
    require(last - first + 1 == count, "proved captured fields are not a contiguous class-file block in " + model.name);
    List<String> block = new ArrayList<String>();
    for (int i = first; i <= last; i++) block.add(model.fields.get(i).render(n, model.namespace, model.name));
    Collections.sort(block);
    List<String> result = new ArrayList<String>();
    for (int i = 0; i < model.fields.size(); i++) {
      if (i == first) result.addAll(block);
      if (i < first || i > last) result.add(model.fields.get(i).render(n, model.namespace, model.name));
    }
    return result;
  }

  private static final class ClassCursor {
    final byte[] data;
    int position;
    ClassCursor(byte[] data) { this.data = data; }
    int u1() { require(position < data.length, "truncated class file"); return data[position++] & 0xff; }
    int u2() { return (u1() << 8) | u1(); }
    long u4() { return ((long) u2() << 16) | u2(); }
    void skip(long count) {
      require(count >= 0 && count <= Integer.MAX_VALUE && position <= data.length - (int) count, "truncated or oversized class-file structure");
      position += (int) count;
    }
  }

  private static void skipAttributes(ClassCursor cursor) {
    int count = cursor.u2();
    for (int i = 0; i < count; i++) { cursor.u2(); cursor.skip(cursor.u4()); }
  }

  private static void validateClassEnvelope(byte[] bytes) {
    ClassCursor cursor = new ClassCursor(bytes);
    require(cursor.u4() == 0xcafebabeL, "invalid class-file magic");
    cursor.u2(); cursor.u2();
    int constants = cursor.u2();
    for (int i = 1; i < constants; i++) {
      int tag = cursor.u1();
      switch (tag) {
        case 1: cursor.skip(cursor.u2()); break;
        case 3: case 4: cursor.skip(4); break;
        case 5: case 6: cursor.skip(8); i++; break;
        case 7: case 8: case 16: case 19: case 20: cursor.skip(2); break;
        case 9: case 10: case 11: case 12: case 17: case 18: cursor.skip(4); break;
        case 15: cursor.skip(3); break;
        default: throw new Failure("unknown constant-pool tag " + tag);
      }
    }
    cursor.skip(6);
    cursor.skip((long) cursor.u2() * 2);
    int fields = cursor.u2();
    for (int i = 0; i < fields; i++) { cursor.skip(6); skipAttributes(cursor); }
    int methods = cursor.u2();
    for (int i = 0; i < methods; i++) { cursor.skip(6); skipAttributes(cursor); }
    skipAttributes(cursor);
    require(cursor.position == bytes.length, "class file contains trailing bytes: parsed=" + cursor.position + " size=" + bytes.length);
  }

  private static ClassModel readClass(InputStream stream, String namespace) throws IOException {
    byte[] bytes = stream.readAllBytes();
    validateClassEnvelope(bytes);
    RecordingClassVisitor visitor = new RecordingClassVisitor(namespace);
    new ClassReader(bytes).accept(visitor, READ_FLAGS);
    require(visitor.model.name != null, "class reader did not visit a class header");
    return visitor.model;
  }

  private static Map<String, ClassModel> readCandidate(Path root, Map<String, String> expectedNamespaceByClass) throws IOException {
    require(Files.isDirectory(root), "candidate class root is not a directory: " + root);
    Map<String, ClassModel> result = new TreeMap<String, ClassModel>();
    try (java.util.stream.Stream<Path> paths = Files.walk(root)) {
      for (Path path : (Iterable<Path>) paths::iterator) {
        if (path.equals(root)) continue;
        require(!Files.isSymbolicLink(path), "candidate class tree contains a symbolic link: " + path);
        if (Files.isDirectory(path, java.nio.file.LinkOption.NOFOLLOW_LINKS)) continue;
        require(Files.isRegularFile(path, java.nio.file.LinkOption.NOFOLLOW_LINKS), "candidate class tree contains a non-regular entry: " + path);
        require(path.toString().endsWith(".class"), "candidate class tree contains a non-class file: " + path);
        ClassModel model;
        try (InputStream in = Files.newInputStream(path)) { model = readClass(in, null); }
        String namespace = expectedNamespaceByClass.get(model.name);
        if (namespace == null) namespace = namespaceFromContainer(root, path);
        model.namespace = namespace;
        Path expectedRelative = Path.of(namespace).resolve(model.name + ".class");
        require(root.relativize(path).equals(expectedRelative), "candidate class path/internal-name mismatch: " +
            root.relativize(path) + " vs " + expectedRelative);
        require(result.put(model.name, model) == null, "duplicate candidate class " + model.name + " under " + root);
      }
    }
    return result;
  }

  private static String namespaceFromContainer(Path root, Path classFile) {
    Path relative = root.relativize(classFile);
    require(relative.getNameCount() >= 2, "candidate class lacks namespace container: " + classFile);
    return relative.getName(0).toString();
  }

  private static Map<String, ClassModel> readOriginal(Path jarPath, Map<String, String> namespaceByClass) throws IOException {
    Map<String, ClassModel> result = new TreeMap<String, ClassModel>();
    try (JarFile jar = new JarFile(jarPath.toFile())) {
      for (Map.Entry<String, String> expected : namespaceByClass.entrySet()) {
        JarEntry entry = jar.getJarEntry(expected.getKey() + ".class");
        require(entry != null, "owned original class missing from jar: " + expected.getKey());
        ClassModel model;
        try (InputStream in = jar.getInputStream(entry)) { model = readClass(in, expected.getValue()); }
        require(model.name.equals(expected.getKey()), "original class internal name disagrees with entry: " + expected.getKey());
        result.put(model.name, model);
      }
    }
    return result;
  }

  private static List<String> externalOriginalUses(Path jarPath, Set<String> ownedClasses,
                                                   Set<String> targetOwners) throws IOException {
    List<String> hits = new ArrayList<String>();
    try (JarFile jar = new JarFile(jarPath.toFile())) {
      java.util.Enumeration<JarEntry> entries = jar.entries();
      while (entries.hasMoreElements()) {
        JarEntry entry = entries.nextElement();
        if (entry.isDirectory() || !entry.getName().endsWith(".class")) continue;
        String entryClass = entry.getName().substring(0, entry.getName().length() - 6);
        if (ownedClasses.contains(entryClass)) continue;
        try (InputStream in = jar.getInputStream(entry)) {
          new ClassReader(in).accept(new ClassVisitor(ASM) {
            String caller;
            void check(String location, String opcode, Object... values) {
              for (Object value : values) for (String target : referencedTargets(value, targetOwners))
                hits.add(caller + "\t" + location + "\t" + opcode + "\t" + target);
            }
            AnnotationVisitor annotation(String location, String opcode) {
              return new AnnotationVisitor(ASM) {
                @Override public void visit(String name, Object value) { check(location, opcode + "_VALUE", value); }
                @Override public void visitEnum(String name, String descriptor, String value) { check(location, opcode + "_ENUM", descriptor); }
                @Override public AnnotationVisitor visitAnnotation(String name, String descriptor) { check(location, opcode + "_NESTED", descriptor); return annotation(location, opcode); }
                @Override public AnnotationVisitor visitArray(String name) { return annotation(location, opcode); }
              };
            }
            @Override public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
              caller = name; check("<class>", "HEADER", superName, signature, interfaces);
            }
            @Override public void visitOuterClass(String owner, String name, String descriptor) { check("<class>", "OUTER", owner, descriptor); }
            @Override public void visitNestHost(String nestHost) { check("<class>", "NESTHOST", nestHost); }
            @Override public void visitNestMember(String nestMember) { check("<class>", "NESTMEMBER", nestMember); }
            @Override public void visitPermittedSubclass(String permittedSubclass) { check("<class>", "PERMITTED", permittedSubclass); }
            @Override public void visitInnerClass(String name, String outerName, String innerName, int access) { check("<class>", "INNER", name, outerName); }
            @Override public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) { check("<class>", "ANNOTATION", descriptor); return annotation("<class>", "ANNOTATION"); }
            @Override public AnnotationVisitor visitTypeAnnotation(int typeRef, TypePath typePath, String descriptor, boolean visible) { check("<class>", "TYPE_ANNOTATION", descriptor); return annotation("<class>", "TYPE_ANNOTATION"); }
            @Override public void visitAttribute(Attribute attribute) { hits.add(caller + "\t<class>\tUNSCANNED_ATTRIBUTE\t" + attribute.type); }
            @Override public FieldVisitor visitField(int access, String name, String descriptor, String signature, Object value) {
              check(name, "FIELD_DECL", descriptor, signature, value);
              return new FieldVisitor(ASM) {
                @Override public AnnotationVisitor visitAnnotation(String desc, boolean visible) { check(name, "FIELD_ANNOTATION", desc); return annotation(name, "FIELD_ANNOTATION"); }
                @Override public AnnotationVisitor visitTypeAnnotation(int ref, TypePath path, String desc, boolean visible) { check(name, "FIELD_TYPE_ANNOTATION", desc); return annotation(name, "FIELD_TYPE_ANNOTATION"); }
                @Override public void visitAttribute(Attribute attribute) { hits.add(caller + "\t" + name + "\tUNSCANNED_ATTRIBUTE\t" + attribute.type); }
              };
            }
            @Override public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
              check(name + descriptor, "METHOD_DECL", descriptor, signature, exceptions);
              return new MethodVisitor(ASM) {
                @Override public AnnotationVisitor visitAnnotationDefault() { return annotation(name + descriptor, "ANNOTATION_DEFAULT"); }
                @Override public AnnotationVisitor visitAnnotation(String desc, boolean visible) { check(name + descriptor, "METHOD_ANNOTATION", desc); return annotation(name + descriptor, "METHOD_ANNOTATION"); }
                @Override public AnnotationVisitor visitTypeAnnotation(int ref, TypePath path, String desc, boolean visible) { check(name + descriptor, "METHOD_TYPE_ANNOTATION", desc); return annotation(name + descriptor, "METHOD_TYPE_ANNOTATION"); }
                @Override public AnnotationVisitor visitParameterAnnotation(int parameter, String desc, boolean visible) { check(name + descriptor, "PARAMETER_ANNOTATION", desc); return annotation(name + descriptor, "PARAMETER_ANNOTATION"); }
                @Override public void visitAttribute(Attribute attribute) { hits.add(caller + "\t" + name + descriptor + "\tUNSCANNED_ATTRIBUTE\t" + attribute.type); }
                @Override public void visitTypeInsn(int opcode, String type) {
                  check(name + descriptor, "TYPE_INSN_" + opcode, type);
                }
                @Override public void visitMethodInsn(int opcode, String owner, String member, String descriptor, boolean isInterface) {
                  check(name + descriptor, "METHOD_INSN_" + opcode, owner, descriptor);
                }
                @Override public void visitFieldInsn(int opcode, String owner, String member, String descriptor) { check(name + descriptor, "FIELD_INSN_" + opcode, owner, descriptor); }
                @Override public void visitInvokeDynamicInsn(String member, String indyDescriptor, Handle bootstrap, Object... arguments) { check(name + descriptor, "INVOKEDYNAMIC", indyDescriptor, bootstrap, arguments); }
                @Override public void visitLdcInsn(Object value) { check(name + descriptor, "LDC_STRUCTURAL", value); }
                @Override public void visitMultiANewArrayInsn(String arrayDescriptor, int dimensions) { check(name + descriptor, "MULTIANEWARRAY", arrayDescriptor); }
                @Override public void visitTryCatchBlock(Label start, Label end, Label handler, String type) { check(name + descriptor, "TRY_CATCH", type); }
                @Override public void visitLocalVariable(String local, String localDescriptor, String localSignature, Label start, Label end, int index) { check(name + descriptor, "LOCAL", localDescriptor, localSignature); }
              };
            }
          }, READ_FLAGS);
        }
      }
    }
    Collections.sort(hits);
    return hits;
  }

  private static Set<String> referencedTargets(Object value, Set<String> targets) {
    Set<String> result = new HashSet<String>();
    if (value == null) return result;
    if (value instanceof String) {
      String string = (String) value;
      if (targets.contains(string)) result.add(string);
      Matcher matcher = Pattern.compile("L([^;<]+)").matcher(string);
      while (matcher.find()) if (targets.contains(matcher.group(1))) result.add(matcher.group(1));
    } else if (value instanceof Type) {
      result.addAll(referencedTargets(((Type) value).getDescriptor(), targets));
    } else if (value instanceof Handle) {
      Handle handle = (Handle) value;
      result.addAll(referencedTargets(handle.getOwner(), targets)); result.addAll(referencedTargets(handle.getDesc(), targets));
    } else if (value instanceof ConstantDynamic) {
      ConstantDynamic dynamic = (ConstantDynamic) value;
      result.addAll(referencedTargets(dynamic.getDescriptor(), targets)); result.addAll(referencedTargets(dynamic.getBootstrapMethod(), targets));
      for (int i = 0; i < dynamic.getBootstrapMethodArgumentCount(); i++) result.addAll(referencedTargets(dynamic.getBootstrapMethodArgument(i), targets));
    } else if (value instanceof Object[]) {
      for (Object item : (Object[]) value) result.addAll(referencedTargets(item, targets));
    }
    return result;
  }

  private static void rejectExternalModelUses(Collection<ClassModel> models, Set<String> ownedClasses,
                                              Map<String, CaptureSpec> captures) {
    Set<String> targetOwners = new HashSet<String>();
    for (CaptureSpec spec : captures.values()) targetOwners.add(spec.owner.name);
    for (ClassModel model : models) {
      if (ownedClasses.contains(model.name)) continue;
      for (MethodModel method : model.methods) for (Insn insn : method.insns) {
        if (insn.is("TYPE", Opcodes.NEW) && targetOwners.contains(insn.args.get(0)))
          throw new Failure("external class constructs captured class: " + model.name + " -> " + insn.args.get(0));
        if (insn.is("METHOD", Opcodes.INVOKESPECIAL) && insn.memberName().equals("<init>") && captures.containsKey(insn.owner() + "\u0000" + insn.descriptor()))
          throw new Failure("external class invokes captured constructor: " + model.name + " -> " + insn.owner());
      }
    }
  }

  private static final class SourceIdentity {
    final String sourceEntry;
    final String sourceSha256;
    final String ownerJar;
    final String ownerSha256;
    SourceIdentity(String sourceEntry, String sourceSha256, String ownerJar, String ownerSha256) {
      this.sourceEntry = sourceEntry; this.sourceSha256 = sourceSha256;
      this.ownerJar = ownerJar; this.ownerSha256 = ownerSha256;
    }
  }

  private static Map<String, SourceIdentity> readSourceIdentities(Path cohort) throws IOException {
    List<String> lines = Files.readAllLines(cohort, StandardCharsets.UTF_8);
    require(!lines.isEmpty(), "empty cohort TSV");
    String[] header = lines.get(0).split("\\t", -1);
    int namespaceColumn = column(header, "namespace");
    int sourceEntryColumn = column(header, "source_entry");
    int sourceShaColumn = column(header, "source_sha256");
    int ownerJarColumn = column(header, "owner_jar");
    int ownerShaColumn = column(header, "owner_jar_sha256");
    Map<String, SourceIdentity> result = new TreeMap<String, SourceIdentity>();
    for (int i = 1; i < lines.size(); i++) {
      String[] fields = lines.get(i).split("\\t", -1);
      int required = Math.max(namespaceColumn, Math.max(sourceEntryColumn,
          Math.max(sourceShaColumn, Math.max(ownerJarColumn, ownerShaColumn))));
      require(fields.length > required, "short cohort TSV row " + (i + 1));
      require(fields[sourceShaColumn].matches("[0-9a-f]{64}"),
          "invalid source SHA-256 in cohort row " + (i + 1));
      require(fields[ownerShaColumn].matches("[0-9a-f]{64}"),
          "invalid owner SHA-256 in cohort row " + (i + 1));
      SourceIdentity identity = new SourceIdentity(fields[sourceEntryColumn], fields[sourceShaColumn],
          fields[ownerJarColumn], fields[ownerShaColumn]);
      require(result.put(fields[namespaceColumn], identity) == null,
          "duplicate source identity for namespace " + fields[namespaceColumn]);
    }
    return result;
  }

  private static void stampSourceIdentities(Map<String, ClassModel> classes,
                                            Map<String, SourceIdentity> identities) {
    for (ClassModel model : classes.values()) {
      SourceIdentity identity = identities.get(model.namespace);
      require(identity != null, "class namespace has no cohort source identity: " +
          model.name + " -> " + model.namespace);
      model.sourceEntry = identity.sourceEntry;
      model.sourceSha256 = identity.sourceSha256;
      model.sourceOwnerJar = identity.ownerJar;
      model.sourceOwnerSha256 = identity.ownerSha256;
    }
  }

  private static Map<String, String> readOwnership(Path cohort, Path ownership) throws IOException {
    Set<String> namespaces = new HashSet<String>();
    List<String> cohortLines = Files.readAllLines(cohort, StandardCharsets.UTF_8);
    for (int i = 1; i < cohortLines.size(); i++) namespaces.add(cohortLines.get(i).split("\\t", -1)[0]);
    Map<String, String> result = new TreeMap<String, String>();
    List<String> lines = Files.readAllLines(ownership, StandardCharsets.UTF_8);
    require(!lines.isEmpty(), "empty class ownership TSV");
    String[] header = lines.get(0).split("\\t", -1);
    int entryColumn = column(header, "entry"); int ownerColumn = column(header, "owner_id");
    for (int i = 1; i < lines.size(); i++) {
      String[] fields = lines.get(i).split("\\t", -1);
      if (!namespaces.contains(fields[ownerColumn])) continue;
      require(fields[entryColumn].endsWith(".class"), "ownership entry is not a class: " + fields[entryColumn]);
      String internal = fields[entryColumn].substring(0, fields[entryColumn].length() - 6);
      require(result.put(internal, fields[ownerColumn]) == null, "duplicate owned class: " + internal);
    }
    return result;
  }

  private static int column(String[] header, String name) { for (int i = 0; i < header.length; i++) if (header[i].equals(name)) return i; throw new Failure("TSV column missing: " + name); }

  private static Set<String> structuralIds(Collection<ClassModel> classes) {
    Set<String> result = new TreeSet<String>(numericComparator());
    for (ClassModel c : classes) {
      collect(result, c.name, c.signature, c.superName); collect(result, c.interfaces);
      for (FieldModel f : c.fields) collect(result, f.name, f.descriptor, f.signature);
      for (MethodModel m : c.methods) {
        collect(result, m.name, m.descriptor, m.signature); collect(result, m.exceptions);
        for (Insn insn : m.insns) {
          if (insn.kind.equals("TYPE")) collect(result, (String) insn.args.get(0));
          else if (insn.kind.equals("FIELD") || insn.kind.equals("METHOD")) collect(result, (String) insn.args.get(0), (String) insn.args.get(1), (String) insn.args.get(2));
          else if (insn.kind.equals("INDY")) collect(result, (String) insn.args.get(0), (String) insn.args.get(1));
          else if (insn.kind.equals("MULTIANEWARRAY")) collect(result, (String) insn.args.get(0));
        }
      }
    }
    return result;
  }

  private static void collectNonLvtCompilerIds(Set<String> target, Object value) {
    if (value == null || value instanceof LabelRef) return;
    if (value instanceof String) target.addAll(ids((String) value));
    else if (value instanceof Type) target.addAll(ids(((Type) value).getDescriptor()));
    else if (value instanceof Handle) {
      Handle handle = (Handle) value;
      target.addAll(ids(handle.getOwner())); target.addAll(ids(handle.getName()));
      target.addAll(ids(handle.getDesc()));
    } else if (value instanceof ConstantDynamic) {
      ConstantDynamic dynamic = (ConstantDynamic) value;
      target.addAll(ids(dynamic.getName())); target.addAll(ids(dynamic.getDescriptor()));
      collectNonLvtCompilerIds(target, dynamic.getBootstrapMethod());
      for (int i = 0; i < dynamic.getBootstrapMethodArgumentCount(); i++)
        collectNonLvtCompilerIds(target, dynamic.getBootstrapMethodArgument(i));
    } else if (value instanceof Collection) {
      for (Object item : (Collection<?>) value) collectNonLvtCompilerIds(target, item);
    } else if (value.getClass().isArray()) {
      for (int i = 0; i < Array.getLength(value); i++)
        collectNonLvtCompilerIds(target, Array.get(value, i));
    }
  }

  private static Set<String> nonLvtCompilerIds(Collection<ClassModel> classes) {
    Set<String> result = new TreeSet<String>(numericComparator());
    for (ClassModel model : classes) {
      collectNonLvtCompilerIds(result, Arrays.asList(model.name, model.signature, model.superName));
      collectNonLvtCompilerIds(result, model.interfaces); collectNonLvtCompilerIds(result, model.events);
      for (FieldModel field : model.fields) {
        collectNonLvtCompilerIds(result, Arrays.asList(field.name, field.descriptor, field.signature));
        collectNonLvtCompilerIds(result, field.value); collectNonLvtCompilerIds(result, field.events);
      }
      for (MethodModel method : model.methods) {
        collectNonLvtCompilerIds(result, Arrays.asList(method.name, method.descriptor, method.signature));
        collectNonLvtCompilerIds(result, method.exceptions); collectNonLvtCompilerIds(result, method.events);
        collectNonLvtCompilerIds(result, method.codeEvents);
        for (Insn instruction : method.insns) collectNonLvtCompilerIds(result, instruction.args);
        for (TryCatchModel block : method.tryCatches) collectNonLvtCompilerIds(result, block.type);
        for (LocalVariableModel variable : method.localVariables)
          collectNonLvtCompilerIds(result, Arrays.asList(variable.descriptor, variable.signature));
      }
    }
    return result;
  }

  private static Set<String> ownedClassIds(Collection<ClassModel> classes) {
    Set<String> result = new TreeSet<String>(numericComparator());
    for (ClassModel model : classes) result.addAll(ids(model.name));
    return result;
  }

  private static Set<String> compilerNodeIds(Collection<ClassModel> classes, LocalUniverse locals) {
    Set<String> result = ownedClassIds(classes);
    for (ClassModel model : classes) {
      for (String symbol : structuralGensyms(model)) result.addAll(ids(symbol));
      for (MethodModel method : model.methods) for (LocalVariableModel variable : method.localVariables)
        if (!coreAsyncIocMetadataOnlyLvt(locals, method, variable) &&
            !coreAsyncAltsGeneratedLvtSpelling(locals, method, variable) &&
            !coreAsyncAutoLocalTerminalSpelling(locals, method, variable) &&
            !coreAsyncSharpLocalSpelling(locals, method, variable) &&
            !coreAsyncDoAltVecLocalSpelling(locals, method, variable) &&
            GENERATED_LOCAL_NAME.matcher(variable.name).matches()) result.addAll(ids(variable.name));
    }
    return result;
  }

  private static String generatedLocalMethodKey(MethodModel method) {
    return method.access + "\u0000" + eraseIds(method.name) + "\u0000" + eraseIds(method.descriptor) +
        "\u0000" + eraseIds(method.signature) + "\u0000" + join(method.exceptions);
  }

  private static boolean hasGeneratedLocalName(MethodModel method, LocalUniverse locals) {
    for (LocalVariableModel variable : method.localVariables)
      if (!coreAsyncIocMetadataOnlyLvt(locals, method, variable) &&
          !coreAsyncAltsGeneratedLvtSpelling(locals, method, variable) &&
          !coreAsyncAutoLocalTerminalSpelling(locals, method, variable) &&
          !coreAsyncSharpLocalSpelling(locals, method, variable) &&
          !coreAsyncDoAltVecLocalSpelling(locals, method, variable) &&
          GENERATED_LOCAL_NAME.matcher(variable.name).matches()) return true;
    return false;
  }

  private static boolean entryBoundarySlot(MethodModel method, int slot) {
    int next = (method.access & Opcodes.ACC_STATIC) == 0 ? 1 : 0;
    if ((method.access & Opcodes.ACC_STATIC) == 0 && slot == 0) return true;
    for (Type type : Type.getArgumentTypes(method.descriptor)) {
      if (slot >= next && slot < next + type.getSize()) return true;
      next += type.getSize();
    }
    return false;
  }

  private static boolean exactCoreAsyncSourceIdentity(ClassModel owner) {
    if (!CORE_ASYNC_NAMESPACE.equals(owner.namespace) ||
        !CORE_ASYNC_SOURCE_ENTRY.equals(owner.sourceEntry) ||
        !CORE_ASYNC_SOURCE_SHA256.equals(owner.sourceSha256) ||
        !CORE_ASYNC_OWNER_JAR.equals(owner.sourceOwnerJar) ||
        !CORE_ASYNC_OWNER_SHA256.equals(owner.sourceOwnerSha256)) return false;
    int sourceEvents = 0;
    for (String event : owner.events) if (event.startsWith("SOURCE(")) {
      sourceEvents++;
      /* The compiler emits a SourceDebugExtension SMAP for this namespace.
       * Its exact payload remains in the ordinary class-event comparison; the
       * source-name half of SourceFile is the identity anchor used here. */
      if (!event.startsWith("SOURCE(" + q("async.clj") + ",") || !event.endsWith(")")) return false;
    }
    return sourceEvents == 1;
  }

  /* Clojure's IOC lowering can attach different authored/generated names to
   * a debug-only LocalVariableTable row when the exact same async.clj is AOTed
   * under a different dependency-load context.  This is the complete and
   * deliberately fail-closed acceptance predicate for that one quotient. */
  private static boolean coreAsyncIocMetadataOnlyLvt(LocalUniverse locals,
                                                     MethodModel method,
                                                     LocalVariableModel variable) {
    LocalNodeKey key = locals.variableNodes.get(variable);
    LocalNodeInfo info = key == null ? null : locals.info.get(key);
    return info != null && info.method == method &&
        exactCoreAsyncSourceIdentity(info.owner) &&
        CORE_ASYNC_IOC_INVOKE_OWNER.matcher(info.owner.name).matches() &&
        method.name.equals("invoke") && method.descriptor.equals("()Ljava/lang/Object;") &&
        (method.access & Opcodes.ACC_STATIC) == 0 &&
        variable.descriptor.equals("Ljava/lang/Object;") && variable.signature == null &&
        !info.boundary && info.physicalSlots.size() == 1 &&
        info.physicalSlots.contains(variable.index) &&
        info.instructionOccurrences == 0 && info.frameOccurrences == 0 &&
        info.lvtOccurrences == 1;
  }

  private static boolean exactAltsLocalShape(LocalUniverse locals,
                                             MethodModel method,
                                             LocalVariableModel variable,
                                             int slot, String descriptor,
                                             int startLabel, int endLabel,
                                             int startRegion, int endRegion,
                                             boolean boundary,
                                             int instructionOccurrences,
                                             int frameOccurrences) {
    LocalNodeKey key = locals.variableNodes.get(variable);
    LocalNodeInfo info = key == null ? null : locals.info.get(key);
    return info != null && info.method == method && key.category.equals("A") &&
        variable.index == slot && variable.descriptor.equals(descriptor) && variable.signature == null &&
        variable.start.id == startLabel && variable.end.id == endLabel &&
        controlRegionLabelPosition(method, variable.start) == startRegion &&
        controlRegionLabelPosition(method, variable.end) == endRegion &&
        info.boundary == boundary && info.physicalSlots.equals(Collections.singleton(slot)) &&
        info.instructionOccurrences == instructionOccurrences &&
        info.frameOccurrences == frameOccurrences && info.lvtOccurrences == 1;
  }

  private static boolean coreAsyncAltsGeneratedLvtFamily(LocalUniverse locals,
                                                         MethodModel method) {
    Set<LocalNodeKey> methodNodes = locals.methodNodes.get(method);
    if (methodNodes == null || method.localVariables.size() != 5) return false;
    ClassModel owner = null;
    for (LocalNodeKey key : methodNodes) {
      LocalNodeInfo info = locals.info.get(key);
      if (info == null || info.method != method) return false;
      if (owner == null) owner = info.owner;
      else if (owner != info.owner) return false;
    }
    if (owner == null || !exactCoreAsyncSourceIdentity(owner) ||
        !owner.name.equals("clojure/core/async$alts_BANG_") ||
        owner.version != Opcodes.V1_8 ||
        owner.access != (Opcodes.ACC_PUBLIC | Opcodes.ACC_FINAL | Opcodes.ACC_SUPER) ||
        !owner.superName.equals("clojure/lang/RestFn") ||
        !method.name.equals("invokeStatic") ||
        !method.descriptor.equals("(Ljava/lang/Object;Lclojure/lang/ISeq;)Ljava/lang/Object;") ||
        method.access != (Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC)) return false;
    List<LocalVariableModel> maps = new ArrayList<LocalVariableModel>();
    List<LocalVariableModel> parameters = new ArrayList<LocalVariableModel>();
    String mapId = null, parameterId = null;
    for (LocalVariableModel variable : method.localVariables) {
      Matcher map = CORE_ASYNC_ALTS_MAP_LOCAL.matcher(variable.name);
      Matcher parameter = CORE_ASYNC_ALTS_PARAMETER_LOCAL.matcher(variable.name);
      if (map.matches()) {
        maps.add(variable);
        if (mapId == null) mapId = map.group(1);
        else if (!mapId.equals(map.group(1))) return false;
      } else if (parameter.matches()) {
        parameters.add(variable);
        if (parameterId == null) parameterId = parameter.group(1);
        else if (!parameterId.equals(parameter.group(1))) return false;
      }
    }
    if (maps.size() != 2 || parameters.size() != 1 || mapId == null || parameterId == null) return false;
    try {
      if (Long.parseLong(parameterId) == Long.MAX_VALUE ||
          Long.parseLong(parameterId) + 1 != Long.parseLong(mapId)) return false;
    } catch (NumberFormatException invalidId) { return false; }
    LocalVariableModel mapSlot2 = null, mapSlot3 = null;
    for (LocalVariableModel variable : maps) {
      if (variable.index == 2) mapSlot2 = variable;
      else if (variable.index == 3) mapSlot3 = variable;
      else return false;
    }
    return mapSlot2 != null && mapSlot3 != null &&
        exactAltsLocalShape(locals, method, mapSlot2, 2, "Ljava/lang/Object;",
            1, 26, 0, 11, false, 10, 11) &&
        exactAltsLocalShape(locals, method, mapSlot3, 3, "Ljava/lang/Object;",
            22, 26, 8, 11, false, 3, 0) &&
        exactAltsLocalShape(locals, method, parameters.get(0), 1, "Lclojure/lang/ISeq;",
            0, 26, 0, 11, true, 2, 0);
  }

  private static boolean coreAsyncAltsGeneratedLvtSpelling(LocalUniverse locals,
                                                           MethodModel method,
                                                           LocalVariableModel variable) {
    return coreAsyncAltsGeneratedLvtFamily(locals, method) &&
        (CORE_ASYNC_ALTS_MAP_LOCAL.matcher(variable.name).matches() ||
         CORE_ASYNC_ALTS_PARAMETER_LOCAL.matcher(variable.name).matches());
  }

  private static String coreAsyncAutoLocalWitnessShape(LocalUniverse locals,
                                                       MethodModel method,
                                                       LocalVariableModel variable) {
    Matcher matcher = AUTO_LOCAL_TERMINAL_UNIQUIFIER.matcher(variable.name);
    LocalNodeKey key = locals.variableNodes.get(variable);
    LocalNodeInfo info = key == null ? null : locals.info.get(key);
    if (!matcher.matches() || info == null || info.method != method) return null;
    String suffix = matcher.group(2);
    Integer suffixOccurrences = locals.autoLocalSuffixOccurrences.get(suffix);
    return String.join("\t", Arrays.asList(
        eraseIds(info.owner.name), method.name, method.descriptor, matcher.group(1),
        variable.descriptor, String.valueOf(variable.signature), String.valueOf(variable.index),
        String.valueOf(variable.start.id), String.valueOf(variable.end.id),
        String.valueOf(controlRegionLabelPosition(method, variable.start)),
        String.valueOf(controlRegionLabelPosition(method, variable.end)), eraseIds(key.toString()),
        String.valueOf(info.boundary), info.physicalSlots.toString(),
        String.valueOf(info.instructionOccurrences), String.valueOf(info.frameOccurrences),
        String.valueOf(info.lvtOccurrences),
        String.valueOf(locals.nonLvtCompilerIds.contains(suffix)),
        String.valueOf(suffixOccurrences == null ? 0 : suffixOccurrences), info.owner.sourceSha256));
  }

  /* Return the exact witnessed row hash, or null.  This lower-level predicate
   * is intentionally separate from the complete-inventory seal so its every
   * negative can be exercised without synthesizing all 76 production rows. */
  private static String coreAsyncAutoLocalWitnessHash(LocalUniverse locals,
                                                      MethodModel method,
                                                      LocalVariableModel variable) {
    Matcher matcher = AUTO_LOCAL_TERMINAL_UNIQUIFIER.matcher(variable.name);
    if (!matcher.matches() || !CORE_ASYNC_AUTO_LOCAL_STABLE_BASES.contains(matcher.group(1)))
      return null;
    LocalNodeKey key = locals.variableNodes.get(variable);
    LocalNodeInfo info = key == null ? null : locals.info.get(key);
    String suffix = matcher.group(2);
    if (info == null || info.method != method || !exactCoreAsyncSourceIdentity(info.owner) ||
        info.boundary || info.physicalSlots.size() != 1 ||
        !info.physicalSlots.contains(variable.index) || info.lvtOccurrences != 1 ||
        locals.nonLvtCompilerIds.contains(suffix) ||
        !Integer.valueOf(1).equals(locals.autoLocalSuffixOccurrences.get(suffix))) return null;
    String hash = sha256(coreAsyncAutoLocalWitnessShape(locals, method, variable));
    return CORE_ASYNC_AUTO_LOCAL_WITNESS_SHAPE_COUNTS.containsKey(hash) ? hash : null;
  }

  private static boolean coreAsyncAutoLocalInventoryClosed(LocalUniverse locals,
                                                           Collection<ClassModel> classes) {
    Map<String, Integer> actual = new TreeMap<String, Integer>();
    int rows = 0;
    for (ClassModel owner : classes) {
      if (!CORE_ASYNC_NAMESPACE.equals(owner.namespace)) continue;
      for (MethodModel method : owner.methods) for (LocalVariableModel variable : method.localVariables) {
        Matcher matcher = AUTO_LOCAL_TERMINAL_UNIQUIFIER.matcher(variable.name);
        if (!matcher.matches() || !CORE_ASYNC_AUTO_LOCAL_STABLE_BASES.contains(matcher.group(1)))
          continue;
        rows++;
        String hash = coreAsyncAutoLocalWitnessHash(locals, method, variable);
        if (hash == null) return false;
        actual.merge(hash, 1, Integer::sum);
      }
    }
    return rows == CORE_ASYNC_AUTO_LOCAL_WITNESS_ROWS &&
        actual.equals(CORE_ASYNC_AUTO_LOCAL_WITNESS_SHAPE_COUNTS);
  }

  private static boolean coreAsyncAutoLocalTerminalSpelling(LocalUniverse locals,
                                                            MethodModel method,
                                                            LocalVariableModel variable) {
    return locals.coreAsyncAutoLocalInventoryClosed &&
        coreAsyncAutoLocalWitnessHash(locals, method, variable) != null;
  }

  private static String coreAsyncAutoLocalTerminalBase(LocalVariableModel variable) {
    Matcher matcher = AUTO_LOCAL_TERMINAL_UNIQUIFIER.matcher(variable.name);
    require(matcher.matches(), "auto-local terminal base requested for a nonmatching LVT name");
    return matcher.group(1);
  }

  private static String coreAsyncSharpLocalWitnessShape(LocalUniverse locals,
                                                        MethodModel method,
                                                        LocalVariableModel variable) {
    Matcher matcher = SHARP_LOCAL_UNIQUIFIER.matcher(variable.name);
    LocalNodeKey key = locals.variableNodes.get(variable);
    LocalNodeInfo info = key == null ? null : locals.info.get(key);
    if (!matcher.matches() || info == null || info.method != method) return null;
    String id = matcher.group(2);
    Integer idOccurrences = locals.lvtCompilerIdOccurrences.get(id);
    return String.join("\t", Arrays.asList(
        eraseIds(info.owner.name), method.name, method.descriptor, matcher.group(1),
        variable.descriptor, String.valueOf(variable.signature), String.valueOf(variable.index),
        String.valueOf(variable.start.id), String.valueOf(variable.end.id),
        String.valueOf(controlRegionLabelPosition(method, variable.start)),
        String.valueOf(controlRegionLabelPosition(method, variable.end)), eraseIds(key.toString()),
        String.valueOf(info.boundary), info.physicalSlots.toString(),
        String.valueOf(info.instructionOccurrences), String.valueOf(info.frameOccurrences),
        String.valueOf(info.lvtOccurrences), String.valueOf(locals.nonLvtCompilerIds.contains(id)),
        String.valueOf(idOccurrences == null ? 0 : idOccurrences), info.owner.sourceSha256));
  }

  private static String coreAsyncSharpLocalWitnessHash(LocalUniverse locals,
                                                       MethodModel method,
                                                       LocalVariableModel variable) {
    Matcher matcher = SHARP_LOCAL_UNIQUIFIER.matcher(variable.name);
    if (!matcher.matches() || !matcher.group(1).equals("p1")) return null;
    LocalNodeKey key = locals.variableNodes.get(variable);
    LocalNodeInfo info = key == null ? null : locals.info.get(key);
    String id = matcher.group(2);
    if (info == null || info.method != method || !exactCoreAsyncSourceIdentity(info.owner) ||
        info.owner.version != Opcodes.V1_8 ||
        info.owner.access != (Opcodes.ACC_PUBLIC | Opcodes.ACC_FINAL | Opcodes.ACC_SUPER) ||
        info.owner.signature != null || !info.owner.superName.equals("clojure/lang/AFunction") ||
        !info.owner.interfaces.isEmpty() || method.access != Opcodes.ACC_PUBLIC ||
        !info.boundary || !entryBoundarySlot(method, variable.index) ||
        info.physicalSlots.size() != 1 || !info.physicalSlots.contains(variable.index) ||
        info.lvtOccurrences != 1 || locals.nonLvtCompilerIds.contains(id) ||
        !Integer.valueOf(1).equals(locals.lvtCompilerIdOccurrences.get(id))) return null;
    String hash = sha256(coreAsyncSharpLocalWitnessShape(locals, method, variable));
    return CORE_ASYNC_SHARP_LOCAL_WITNESS_SHAPE_COUNTS.containsKey(hash) ? hash : null;
  }

  private static boolean coreAsyncSharpLocalInventoryClosed(LocalUniverse locals,
                                                            Collection<ClassModel> classes) {
    Map<String, Integer> actual = new TreeMap<String, Integer>();
    int rows = 0;
    for (ClassModel owner : classes) {
      if (!CORE_ASYNC_NAMESPACE.equals(owner.namespace)) continue;
      for (MethodModel method : owner.methods) for (LocalVariableModel variable : method.localVariables) {
        if (!SHARP_LOCAL_UNIQUIFIER.matcher(variable.name).matches()) continue;
        rows++;
        String hash = coreAsyncSharpLocalWitnessHash(locals, method, variable);
        if (hash == null) return false;
        actual.merge(hash, 1, Integer::sum);
      }
    }
    return coreAsyncSharpLocalShapeCountsClosed(rows, actual);
  }

  private static boolean coreAsyncSharpLocalShapeCountsClosed(int rows,
                                                              Map<String, Integer> actual) {
    return rows == CORE_ASYNC_SHARP_LOCAL_WITNESS_ROWS &&
        actual.equals(CORE_ASYNC_SHARP_LOCAL_WITNESS_SHAPE_COUNTS);
  }

  private static boolean coreAsyncSharpLocalSpelling(LocalUniverse locals,
                                                     MethodModel method,
                                                     LocalVariableModel variable) {
    return locals.coreAsyncSharpLocalInventoryClosed &&
        coreAsyncSharpLocalWitnessHash(locals, method, variable) != null;
  }

  private static String coreAsyncSharpLocalPrefix(LocalVariableModel variable) {
    Matcher matcher = SHARP_LOCAL_UNIQUIFIER.matcher(variable.name);
    require(matcher.matches(), "sharp-local prefix requested for a nonmatching LVT name");
    return matcher.group(1) + "__SHARP_";
  }

  private static String coreAsyncDoAltVecLocalInstructionGraph(LocalUniverse locals,
                                                               MethodModel method,
                                                               LocalNodeKey key) {
    List<String> result = new ArrayList<String>();
    for (int i = 0; i < method.insns.size(); i++) {
      Insn instruction = method.insns.get(i);
      if (!key.equals(locals.instructionNodes.get(instruction))) continue;
      result.add(i + ":" + instruction.kind + ":" + instruction.opcode + ":" + instruction.args);
    }
    return join(result);
  }

  private static String coreAsyncDoAltVecLocalFrameGraph(LocalUniverse locals,
                                                         MethodModel method,
                                                         LocalNodeKey key) {
    List<String> result = new ArrayList<String>();
    for (int i = 0; i < method.insns.size(); i++) {
      Map<Integer, LocalNodeKey> cells = locals.frameNodes.get(method.insns.get(i));
      if (cells == null) continue;
      List<Integer> physicalSlots = new ArrayList<Integer>();
      for (Map.Entry<Integer, LocalNodeKey> cell : cells.entrySet())
        if (key.equals(cell.getValue())) physicalSlots.add(cell.getKey());
      if (!physicalSlots.isEmpty()) result.add(i + ":" + physicalSlots);
    }
    return join(result);
  }

  private static String coreAsyncDoAltVecLocalAliasGraph(LocalUniverse locals,
                                                         MethodModel method,
                                                         LocalNodeKey key) {
    List<String> result = new ArrayList<String>();
    for (LocalVariableModel alias : method.localVariables) {
      if (!key.equals(locals.variableNodes.get(alias))) continue;
      result.add(eraseIds(alias.name) + ":" + alias.descriptor + ":" + clean(alias.signature) +
          ":slot=" + alias.index + ":labels=" + alias.start.id + "-" + alias.end.id +
          ":regions=" + controlRegionLabelPosition(method, alias.start) + "-" +
          controlRegionLabelPosition(method, alias.end));
    }
    return join(result);
  }

  private static String coreAsyncDoAltVecLocalWitnessShape(LocalUniverse locals,
                                                           MethodModel method,
                                                           LocalVariableModel variable) {
    LocalNodeKey key = locals.variableNodes.get(variable);
    LocalNodeInfo info = key == null ? null : locals.info.get(key);
    if (info == null || info.method != method) return null;
    return String.join("\t", Arrays.asList(
        info.owner.name, method.name, method.descriptor, eraseIds(variable.name),
        variable.descriptor, clean(variable.signature), String.valueOf(variable.index),
        String.valueOf(variable.start.id), String.valueOf(variable.end.id),
        String.valueOf(controlRegionLabelPosition(method, variable.start)),
        String.valueOf(controlRegionLabelPosition(method, variable.end)), eraseIds(key.toString()),
        String.valueOf(info.boundary), info.physicalSlots.toString(),
        String.valueOf(info.instructionOccurrences), String.valueOf(info.frameOccurrences),
        String.valueOf(info.lvtOccurrences),
        coreAsyncDoAltVecLocalInstructionGraph(locals, method, key),
        coreAsyncDoAltVecLocalFrameGraph(locals, method, key),
        coreAsyncDoAltVecLocalAliasGraph(locals, method, key),
        info.owner.sourceEntry, info.owner.sourceSha256, info.owner.sourceOwnerJar,
        info.owner.sourceOwnerSha256));
  }

  /* async.clj's authored destructuring local in this one exact method receives
   * the AOT compilation context's next compiler ID even though that ID has no
   * structural occurrence.  Four independent outputs contain exactly one row
   * with this complete typed graph.  This predicate does not cover any other
   * vec__ local or any other compiler-number spelling. */
  private static String coreAsyncDoAltVecLocalWitnessHash(LocalUniverse locals,
                                                          MethodModel method,
                                                          LocalVariableModel variable) {
    Matcher matcher = CORE_ASYNC_DO_ALT_VEC_LOCAL.matcher(variable.name);
    LocalNodeKey key = locals.variableNodes.get(variable);
    LocalNodeInfo info = key == null ? null : locals.info.get(key);
    if (!matcher.matches() || info == null || info.method != method ||
        !exactCoreAsyncSourceIdentity(info.owner) ||
        !info.owner.name.equals("clojure/core/async$do_alt") ||
        info.owner.version != Opcodes.V1_8 ||
        info.owner.access != (Opcodes.ACC_PUBLIC | Opcodes.ACC_FINAL | Opcodes.ACC_SUPER) ||
        info.owner.signature != null || !info.owner.superName.equals("clojure/lang/AFunction") ||
        !info.owner.interfaces.isEmpty() || method.access != (Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC) ||
        !method.name.equals("invokeStatic") ||
        !method.descriptor.equals("(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;") ||
        method.signature != null || !method.exceptions.isEmpty() || method.localVariables.size() != 11 ||
        !variable.descriptor.equals("Ljava/lang/Object;") || variable.signature != null ||
        variable.index != 6 || info.boundary || !key.category.equals("A") ||
        info.physicalSlots.size() != 1 || !info.physicalSlots.contains(variable.index) ||
        info.instructionOccurrences != 4 || info.frameOccurrences != 0 || info.lvtOccurrences != 1 ||
        locals.nonLvtCompilerIds.contains(matcher.group(1)) ||
        !Integer.valueOf(1).equals(locals.lvtCompilerIdOccurrences.get(matcher.group(1)))) return null;
    String hash = sha256(coreAsyncDoAltVecLocalWitnessShape(locals, method, variable));
    return CORE_ASYNC_DO_ALT_VEC_LOCAL_WITNESS_SHAPE_SHA256.equals(hash) ? hash : null;
  }

  private static boolean coreAsyncDoAltVecLocalInventoryClosed(LocalUniverse locals,
                                                               Collection<ClassModel> classes) {
    Map<String, Integer> actual = new TreeMap<String, Integer>();
    int owners = 0, methods = 0, rows = 0;
    for (ClassModel owner : classes) {
      if (!CORE_ASYNC_NAMESPACE.equals(owner.namespace) ||
          !owner.name.equals("clojure/core/async$do_alt")) continue;
      owners++;
      for (MethodModel method : owner.methods) {
        if (!method.name.equals("invokeStatic") ||
            !method.descriptor.equals("(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;")) continue;
        methods++;
        for (LocalVariableModel variable : method.localVariables) {
          if (!CORE_ASYNC_DO_ALT_VEC_LOCAL.matcher(variable.name).matches()) continue;
          rows++;
          String hash = coreAsyncDoAltVecLocalWitnessHash(locals, method, variable);
          if (hash == null) return false;
          actual.merge(hash, 1, Integer::sum);
        }
      }
    }
    return owners == 1 && methods == 1 && coreAsyncDoAltVecLocalShapeCountsClosed(rows, actual);
  }

  private static boolean coreAsyncDoAltVecLocalShapeCountsClosed(int rows,
                                                                 Map<String, Integer> actual) {
    return rows == CORE_ASYNC_DO_ALT_VEC_LOCAL_WITNESS_ROWS && actual.size() == 1 &&
        Integer.valueOf(1).equals(actual.get(CORE_ASYNC_DO_ALT_VEC_LOCAL_WITNESS_SHAPE_SHA256));
  }

  private static boolean coreAsyncDoAltVecLocalSpelling(LocalUniverse locals,
                                                        MethodModel method,
                                                        LocalVariableModel variable) {
    return locals.coreAsyncDoAltVecLocalInventoryClosed &&
        coreAsyncDoAltVecLocalWitnessHash(locals, method, variable) != null;
  }

  private static String coreAsyncDoAltVecLocalPrefix(LocalVariableModel variable) {
    require(CORE_ASYNC_DO_ALT_VEC_LOCAL.matcher(variable.name).matches(),
        "do-alt vec-local prefix requested for a nonmatching LVT name");
    return "vec__";
  }

  private static String localVariableSkeleton(MethodModel method, LocalVariableModel variable,
                                              LocalUniverse locals) {
    String name;
    if (coreAsyncIocMetadataOnlyLvt(locals, method, variable)) name = "CORE_ASYNC_IOC_METADATA_ONLY_LVT_NAME";
    else if (coreAsyncAltsGeneratedLvtSpelling(locals, method, variable)) name = eraseIds(variable.name);
    else if (coreAsyncAutoLocalTerminalSpelling(locals, method, variable))
      name = coreAsyncAutoLocalTerminalBase(variable);
    else if (coreAsyncSharpLocalSpelling(locals, method, variable))
      name = coreAsyncSharpLocalPrefix(variable);
    else if (coreAsyncDoAltVecLocalSpelling(locals, method, variable))
      name = coreAsyncDoAltVecLocalPrefix(variable);
    else name = GENERATED_LOCAL_NAME.matcher(variable.name).matches() ? eraseIds(variable.name) : variable.name;
    return name + "\u0000" + eraseIds(variable.descriptor) + "\u0000" + eraseIds(variable.signature) +
        "\u0000" + controlRegionLabelPosition(method, variable.start) + "\u0000" +
        controlRegionLabelPosition(method, variable.end) +
        (entryBoundarySlot(method, variable.index) ? "\u0000fixed=" + variable.index : "");
  }

  private static void constrainGeneratedLocalNames(String namespace,
                                                   Map<String, ClassModel> left,
                                                   Map<String, ClassModel> right,
                                                   Map<String, String> classPairs,
                                                   LocalUniverse leftLocals,
                                                   LocalUniverse rightLocals,
                                                   Map<String, String> mapping,
                                                   Map<String, String> reverse) {
    for (ClassModel leftClass : left.values()) {
      if (!leftClass.namespace.equals(namespace)) continue;
      ClassModel rightClass = right.get(classPairs.get(leftClass.name));
      require(rightClass != null, "generated-local owner lacks a paired class: " + leftClass.name);
      Map<String, MethodModel> rightMethods = new HashMap<String, MethodModel>();
      for (MethodModel method : rightClass.methods) {
        if (!hasGeneratedLocalName(method, rightLocals)) continue;
        String key = generatedLocalMethodKey(method);
        require(rightMethods.put(key, method) == null,
            "generated-local method key is ambiguous in " + rightClass.name + ": " + key);
      }
      Set<String> used = new HashSet<String>();
      for (MethodModel leftMethod : leftClass.methods) {
        if (!hasGeneratedLocalName(leftMethod, leftLocals)) continue;
        String key = generatedLocalMethodKey(leftMethod);
        MethodModel rightMethod = rightMethods.get(key);
        require(rightMethod != null, "generated-local method lacks a paired ABI context: " +
            leftClass.name + "." + leftMethod.key());
        used.add(key);
        Map<String, List<LocalVariableModel>> leftRows = new TreeMap<String, List<LocalVariableModel>>();
        Map<String, List<LocalVariableModel>> rightRows = new TreeMap<String, List<LocalVariableModel>>();
        for (LocalVariableModel variable : leftMethod.localVariables) {
          if (coreAsyncIocMetadataOnlyLvt(leftLocals, leftMethod, variable) ||
              coreAsyncAltsGeneratedLvtSpelling(leftLocals, leftMethod, variable) ||
              coreAsyncAutoLocalTerminalSpelling(leftLocals, leftMethod, variable) ||
              coreAsyncSharpLocalSpelling(leftLocals, leftMethod, variable) ||
              coreAsyncDoAltVecLocalSpelling(leftLocals, leftMethod, variable)) continue;
          leftRows.computeIfAbsent(localVariableSkeleton(leftMethod, variable, leftLocals),
              ignored -> new ArrayList<LocalVariableModel>()).add(variable);
        }
        for (LocalVariableModel variable : rightMethod.localVariables) {
          if (coreAsyncIocMetadataOnlyLvt(rightLocals, rightMethod, variable) ||
              coreAsyncAltsGeneratedLvtSpelling(rightLocals, rightMethod, variable) ||
              coreAsyncAutoLocalTerminalSpelling(rightLocals, rightMethod, variable) ||
              coreAsyncSharpLocalSpelling(rightLocals, rightMethod, variable) ||
              coreAsyncDoAltVecLocalSpelling(rightLocals, rightMethod, variable)) continue;
          rightRows.computeIfAbsent(localVariableSkeleton(rightMethod, variable, rightLocals),
              ignored -> new ArrayList<LocalVariableModel>()).add(variable);
        }
        require(leftRows.keySet().equals(rightRows.keySet()),
            "local-variable row-shape sets differ in paired generated-local method " + leftClass.name + "." + leftMethod.key());
        for (String row : leftRows.keySet()) {
          List<LocalVariableModel> aRows = leftRows.get(row), bRows = rightRows.get(row);
          require(aRows.size() == bRows.size(), "local-variable row-shape multiplicity differs in " +
              leftClass.name + "." + leftMethod.key());
          List<String> aNames = new ArrayList<String>(), bNames = new ArrayList<String>();
          for (LocalVariableModel variable : aRows)
            if (GENERATED_LOCAL_NAME.matcher(variable.name).matches()) aNames.add(variable.name);
          for (LocalVariableModel variable : bRows)
            if (GENERATED_LOCAL_NAME.matcher(variable.name).matches()) bNames.add(variable.name);
          require((aNames.isEmpty()) == (bNames.isEmpty()), "generated-local classification differs in row shape " +
              row + " in " + leftClass.name + "." + leftMethod.key());
          if (aNames.isEmpty()) continue;
          require(aNames.size() == bNames.size(), "generated-local occurrence multiplicity differs in row shape " +
              row + " in " + leftClass.name + "." + leftMethod.key());
          for (int i = 0; i < aNames.size(); i++) {
            String aName = aNames.get(i), bName = bNames.get(i);
            for (String id : ids(aName))
              require(mapping.containsKey(id), "generated-local occurrence contains an ID not established by the " +
                  "pre-proved compilation-unit bijection in " + leftClass.name + "." + leftMethod.key() + ": " + aName);
            for (String id : ids(bName))
              require(reverse.containsKey(id), "right generated-local occurrence contains an ID not established by the " +
                  "pre-proved compilation-unit bijection in " + rightClass.name + "." + rightMethod.key() + ": " + bName);
            require(replaceIds(aName, mapping, true).equals(bName),
                "generated-local occurrence conflicts with the pre-proved compilation-unit ID bijection in " +
                leftClass.name + "." + leftMethod.key() + ": " + aName + " -> " +
                replaceIds(aName, mapping, true) + " vs " + bName);
          }
        }
      }
      require(used.equals(rightMethods.keySet()),
          "right side has unmatched generated-local method contexts in " + rightClass.name);
    }
  }

  private static void constrainStructuralGensyms(String namespace,
                                                  Map<String, ClassModel> left,
                                                  Map<String, ClassModel> right,
                                                  Map<String, String> classPairs,
                                                  Map<String, String> mapping,
                                                  Map<String, String> reverse) {
    for (ClassModel leftClass : left.values()) {
      if (!leftClass.namespace.equals(namespace)) continue;
      String rightName = classPairs.get(leftClass.name);
      require(rightName != null, "structural-symbol owner lacks class pairing: " + leftClass.name);
      ClassModel rightClass = right.get(rightName);
      require(rightClass != null, "paired structural-symbol owner is absent: " + rightName);
      List<String> a = structuralGensyms(leftClass), b = structuralGensyms(rightClass);
      require(a.size() == b.size(), "typed structural-symbol count differs in paired class " + leftClass.name + " -> " + rightName);
      for (int i = 0; i < a.size(); i++) constrainIds(namespace, a.get(i), b.get(i), mapping, reverse);
    }
  }

  private static void collect(Set<String> target, String... values) { for (String value : values) target.addAll(ids(value)); }
  private static void collect(Set<String> target, Collection<String> values) { for (String value : values) target.addAll(ids(value)); }

  private static Map<String, List<ClassModel>> byNamespace(Map<String, ClassModel> classes) {
    Map<String, List<ClassModel>> result = new TreeMap<String, List<ClassModel>>();
    for (ClassModel c : classes.values()) result.computeIfAbsent(c.namespace, ignored -> new ArrayList<ClassModel>()).add(c);
    return result;
  }

  private static final class IdToken {
    final int form;
    final String number;
    final int start;
    IdToken(int form, String number, int start) { this.form = form; this.number = number; this.start = start; }
  }

  private static List<IdToken> idTokens(String value) {
    List<IdToken> result = new ArrayList<IdToken>();
    if (value == null) return result;
    collectIdTokens(result, DOUBLE_ID, 1, value);
    collectIdTokens(result, EVAL_ID, 2, value);
    collectIdTokens(result, INST_ID, 3, value);
    Matcher single = SINGLE_LOCAL_ID.matcher(value);
    if (single.matches()) result.add(new IdToken(4, single.group(2), single.start(2)));
    Collections.sort(result, (a, b) -> { int c = Integer.compare(a.start, b.start); return c != 0 ? c : Integer.compare(a.form, b.form); });
    return result;
  }

  private static void collectIdTokens(List<IdToken> target, Pattern pattern, int form, String value) {
    Matcher matcher = pattern.matcher(value);
    while (matcher.find()) target.add(new IdToken(form, matcher.group(1), matcher.start()));
  }

  private static void constrainIds(String namespace, String left, String right,
                                   Map<String, String> mapping, Map<String, String> reverse) {
    require(eraseIds(left).equals(eraseIds(right)), "ID-erased class names differ for a paired skeleton in " + namespace + ": " + left + " vs " + right);
    List<IdToken> a = idTokens(left), b = idTokens(right);
    require(a.size() == b.size(), "compiler-ID token count differs in paired class names for " + namespace);
    for (int i = 0; i < a.size(); i++) {
      require(a.get(i).form == b.get(i).form, "compiler-ID token forms differ in paired class names for " + namespace);
      String oldRight = mapping.putIfAbsent(a.get(i).number, b.get(i).number);
      require(oldRight == null || oldRight.equals(b.get(i).number), "conflicting compiler-ID constraint in " + namespace + " for " + a.get(i).number);
      String oldLeft = reverse.putIfAbsent(b.get(i).number, a.get(i).number);
      require(oldLeft == null || oldLeft.equals(a.get(i).number), "non-injective compiler-ID constraint in " + namespace + " for " + b.get(i).number);
    }
  }

  private static Map<String, List<ClassModel>> skeletonGroups(String namespace,
                                                               List<ClassModel> classes,
                                                               Normalizer eraser,
                                                               Map<String, CaptureSpec> activeCaptures) {
    Map<String, List<ClassModel>> result = new HashMap<String, List<ClassModel>>();
    for (ClassModel model : classes) {
      RenderedClass rendered = renderClass(model, eraser, activeCaptures);
      result.computeIfAbsent(rendered.text, ignored -> new ArrayList<ClassModel>()).add(model);
    }
    return result;
  }

  private static Map<String, ClassModel> indexClasses(Collection<ClassModel> classes) {
    Map<String, ClassModel> result = new HashMap<String, ClassModel>();
    for (ClassModel model : classes) result.put(model.name, model);
    return result;
  }

  private static boolean compatibleConstraint(String left, String right,
                                              Map<String, String> mapping,
                                              Map<String, String> reverse) {
    if (!eraseIds(left).equals(eraseIds(right))) return false;
    List<IdToken> a = idTokens(left), b = idTokens(right);
    if (a.size() != b.size()) return false;
    for (int i = 0; i < a.size(); i++) {
      if (a.get(i).form != b.get(i).form) return false;
      String knownRight = mapping.get(a.get(i).number);
      if (knownRight != null && !knownRight.equals(b.get(i).number)) return false;
      String knownLeft = reverse.get(b.get(i).number);
      if (knownLeft != null && !knownLeft.equals(a.get(i).number)) return false;
    }
    return true;
  }

  private static final class PairingSearch {
    final List<ClassModel> left;
    final Map<ClassModel, List<ClassModel>> candidates;
    final Set<String> allLeftIds;
    final Set<String> allRightIds;
    int solutions;
    Map<String, String> solution;
    Map<String, String> solutionClasses;
    Map<String, String> secondSolution;
    Map<String, String> secondSolutionClasses;

    PairingSearch(List<ClassModel> left, Map<ClassModel, List<ClassModel>> candidates,
                  Set<String> allLeftIds, Set<String> allRightIds) {
      this.left = left; this.candidates = candidates;
      this.allLeftIds = allLeftIds; this.allRightIds = allRightIds;
    }

    void search(Set<ClassModel> assignedLeft, Set<ClassModel> assignedRight,
                Map<String, String> mapping, Map<String, String> reverse,
                Map<String, String> classMapping) {
      if (solutions > 1) return;
      if (assignedLeft.size() == left.size()) {
        if (!mapping.keySet().equals(allLeftIds) || !new HashSet<String>(mapping.values()).equals(allRightIds)) return;
        solutions++;
        if (solutions == 1) {
          solution = new TreeMap<String, String>(mapping);
          solutionClasses = new TreeMap<String, String>(classMapping);
        } else if (solutions == 2) {
          secondSolution = new TreeMap<String, String>(mapping);
          secondSolutionClasses = new TreeMap<String, String>(classMapping);
        }
        return;
      }
      ClassModel selected = null;
      List<ClassModel> viableSelected = null;
      for (ClassModel candidateLeft : left) {
        if (assignedLeft.contains(candidateLeft)) continue;
        List<ClassModel> viable = new ArrayList<ClassModel>();
        for (ClassModel candidateRight : candidates.get(candidateLeft))
          if (!assignedRight.contains(candidateRight) && compatibleConstraint(candidateLeft.name, candidateRight.name, mapping, reverse)) viable.add(candidateRight);
        if (viable.isEmpty()) return;
        if (viableSelected == null || viable.size() < viableSelected.size()) { selected = candidateLeft; viableSelected = viable; }
      }
      for (ClassModel right : viableSelected) {
        Map<String, String> next = new HashMap<String, String>(mapping);
        Map<String, String> nextReverse = new HashMap<String, String>(reverse);
        constrainIds(selected.namespace, selected.name, right.name, next, nextReverse);
        assignedLeft.add(selected); assignedRight.add(right);
        classMapping.put(selected.name, right.name);
        search(assignedLeft, assignedRight, next, nextReverse, classMapping);
        classMapping.remove(selected.name);
        assignedLeft.remove(selected); assignedRight.remove(right);
        if (solutions > 1) return;
      }
    }
  }

  private static String pairingAmbiguityDiagnostic(PairingSearch search) {
    if (search.solutions < 2 || search.solution == null || search.secondSolution == null) return "-";
    List<String> classes = new ArrayList<String>();
    Set<String> classKeys = new TreeSet<String>();
    classKeys.addAll(search.solutionClasses.keySet()); classKeys.addAll(search.secondSolutionClasses.keySet());
    for (String key : classKeys) {
      String first = search.solutionClasses.get(key), second = search.secondSolutionClasses.get(key);
      if (!Objects.equals(first, second)) classes.add(key + "->{" + first + "|" + second + "}");
    }
    List<String> ids = new ArrayList<String>();
    Set<String> idKeys = new TreeSet<String>(numericComparator());
    idKeys.addAll(search.solution.keySet()); idKeys.addAll(search.secondSolution.keySet());
    for (String key : idKeys) {
      String first = search.solution.get(key), second = search.secondSolution.get(key);
      if (!Objects.equals(first, second)) ids.add(key + "->{" + first + "|" + second + "}");
    }
    return "class-alternatives=" + classes + " id-alternatives=" + ids;
  }

  private static Map<String, List<MethodModel>> methodsByErasedAbi(ClassModel model) {
    Map<String, List<MethodModel>> result = new TreeMap<String, List<MethodModel>>();
    for (MethodModel method : model.methods)
      result.computeIfAbsent(generatedLocalMethodKey(method), ignored -> new ArrayList<MethodModel>()).add(method);
    return result;
  }

  private static List<String> constructedOwnedTargets(MethodModel method) {
    List<String> result = new ArrayList<String>();
    for (Insn instruction : method.insns)
      if (instruction.is("TYPE", Opcodes.NEW)) result.add((String) instruction.args.get(0));
    return result;
  }

  /* A singleton full-class skeleton pair is already independently anchored.
   * Preserve the ordered typed NEW edges in that owner to constrain otherwise
   * identical generated child classes.  This is graph evidence, not a sort or
   * arbitrary tie-break: a genuinely symmetric, unreferenced pair remains
   * ambiguous and is rejected by PairingSearch. */
  private static void constrainOrderedConstructionGraph(String namespace,
                                                        Map<String, ClassModel> left,
                                                        Map<String, ClassModel> right,
                                                        Map<String, String> anchoredClassPairs,
                                                        Map<String, String> mapping,
                                                        Map<String, String> reverse) {
    for (Map.Entry<String, String> pair : anchoredClassPairs.entrySet()) {
      ClassModel leftOwner = left.get(pair.getKey()), rightOwner = right.get(pair.getValue());
      require(leftOwner != null && rightOwner != null,
          "anchored class pair disappeared while deriving construction graph: " + pair);
      Map<String, List<MethodModel>> leftMethods = methodsByErasedAbi(leftOwner);
      Map<String, List<MethodModel>> rightMethods = methodsByErasedAbi(rightOwner);
      for (String methodKey : leftMethods.keySet()) {
        List<MethodModel> aMethods = leftMethods.get(methodKey), bMethods = rightMethods.get(methodKey);
        if (aMethods.size() != 1 || bMethods == null || bMethods.size() != 1) continue;
        List<String> aTargets = constructedOwnedTargets(aMethods.get(0));
        List<String> bTargets = constructedOwnedTargets(bMethods.get(0));
        require(aTargets.size() == bTargets.size(),
            "ordered construction-edge count differs in anchored class " + leftOwner.name +
            "." + aMethods.get(0).key());
        for (int i = 0; i < aTargets.size(); i++) {
          String aTarget = aTargets.get(i), bTarget = bTargets.get(i);
          boolean aOwned = left.containsKey(aTarget), bOwned = right.containsKey(bTarget);
          require(aOwned == bOwned, "ordered construction edge crosses exact-source ownership boundary in " +
              leftOwner.name + "." + aMethods.get(0).key() + " at NEW ordinal " + i);
          if (!aOwned) continue;
          require(eraseIds(aTarget).equals(eraseIds(bTarget)),
              "ordered construction-edge target shapes differ in " + leftOwner.name +
              "." + aMethods.get(0).key() + " at NEW ordinal " + i + ": " +
              aTarget + " vs " + bTarget);
          constrainIds(namespace, aTarget, bTarget, mapping, reverse);
        }
      }
    }
  }

  private static Map<String, IdMap> idMaps(String comparison, Map<String, ClassModel> left, Map<String, ClassModel> right,
                                           Map<String, CaptureSpec> activeLeft, Map<String, CaptureSpec> activeRight,
                                           MemberUniverse leftMembers, MemberUniverse rightMembers,
                                           StateSlotUniverse leftSlots, StateSlotUniverse rightSlots,
                                           LocalUniverse leftLocals, LocalUniverse rightLocals,
                                           List<String> mappingRows, Map<String, String> classPairs) {
    Map<String, List<ClassModel>> l = byNamespace(left), r = byNamespace(right);
    require(l.keySet().equals(r.keySet()), "namespace sets differ: " + l.keySet() + " vs " + r.keySet());
    Map<String, IdMap> result = new TreeMap<String, IdMap>();
    for (String namespace : l.keySet()) {
      Normalizer leftEraser = new Normalizer(Collections.emptyMap(), classNamespaces(left), Collections.emptyMap(), true, true,
          leftMembers, Collections.emptyMap(), true, leftSlots, Collections.emptyMap(), null,
          true, leftLocals, Collections.emptyMap(), null);
      Normalizer rightEraser = new Normalizer(Collections.emptyMap(), classNamespaces(right), Collections.emptyMap(), true, true,
          rightMembers, Collections.emptyMap(), true, rightSlots, Collections.emptyMap(), null,
          true, rightLocals, Collections.emptyMap(), null);
      Map<String, List<ClassModel>> leftSkeletons = skeletonGroups(namespace, l.get(namespace), leftEraser, activeLeft);
      Map<String, List<ClassModel>> rightSkeletons = skeletonGroups(namespace, r.get(namespace), rightEraser, activeRight);
      String skeletonDiagnostic = skeletonDifferenceDiagnostic(leftSkeletons, rightSkeletons, rightEraser, activeRight);
      require(leftSkeletons.keySet().equals(rightSkeletons.keySet()),
          "ID-placeholder full-class skeleton sets differ for " + namespace + ": left-only=" +
          leftSkeletons.keySet().stream().filter(k -> !rightSkeletons.containsKey(k)).count() +
          " right-only=" + rightSkeletons.keySet().stream().filter(k -> !leftSkeletons.containsKey(k)).count() +
          " left-examples=" + unmatchedSkeletonClasses(leftSkeletons, rightSkeletons) +
          " right-examples=" + unmatchedSkeletonClasses(rightSkeletons, leftSkeletons) +
          " diagnostic=" + skeletonDiagnostic);
      List<ClassModel> searchLeft = new ArrayList<ClassModel>();
      Map<ClassModel, List<ClassModel>> candidates = new HashMap<ClassModel, List<ClassModel>>();
      Map<String, String> initial = new HashMap<String, String>();
      Map<String, String> initialReverse = new HashMap<String, String>();
      Map<String, String> initialClasses = new HashMap<String, String>();
      for (String skeleton : leftSkeletons.keySet()) {
        List<ClassModel> leftGroup = leftSkeletons.get(skeleton), rightGroup = rightSkeletons.get(skeleton);
        require(leftGroup.size() == rightGroup.size(), "ID-placeholder skeleton multiplicity differs for " + namespace + ": " + leftGroup.size() + " vs " + rightGroup.size());
        if (leftGroup.size() == 1) {
          constrainIds(namespace, leftGroup.get(0).name, rightGroup.get(0).name, initial, initialReverse);
          initialClasses.put(leftGroup.get(0).name, rightGroup.get(0).name);
        } else {
          for (ClassModel leftClass : leftGroup) { searchLeft.add(leftClass); candidates.put(leftClass, rightGroup); }
        }
      }
      Set<String> leftClassIds = ownedClassIds(l.get(namespace)), rightClassIds = ownedClassIds(r.get(namespace));
      Map<String, ClassModel> namespaceLeft = indexClasses(l.get(namespace));
      Map<String, ClassModel> namespaceRight = indexClasses(r.get(namespace));
      constrainOrderedConstructionGraph(namespace, namespaceLeft, namespaceRight, initialClasses,
          initial, initialReverse);
      PairingSearch search = new PairingSearch(searchLeft, candidates, leftClassIds, rightClassIds);
      search.search(new HashSet<ClassModel>(), new HashSet<ClassModel>(), initial, initialReverse, initialClasses);
      require(search.solutions == 1, "compiler-ID full-cohort pairing is " +
          (search.solutions == 0 ? "incomplete/conflicting" : "ambiguous") + " for " + namespace +
          ": solutions-observed=" + search.solutions + " " + pairingAmbiguityDiagnostic(search));
      Map<String, String> proved = new TreeMap<String, String>(search.solution);
      Map<String, String> provedReverse = new HashMap<String, String>();
      for (Map.Entry<String, String> entry : proved.entrySet()) provedReverse.put(entry.getValue(), entry.getKey());
      classPairs.putAll(search.solutionClasses);
      constrainStructuralGensyms(namespace, left, right, classPairs, proved, provedReverse);
      constrainGeneratedLocalNames(namespace, left, right, classPairs, leftLocals, rightLocals,
          proved, provedReverse);
      Set<String> leftIds = compilerNodeIds(l.get(namespace), leftLocals);
      Set<String> rightIds = compilerNodeIds(r.get(namespace), rightLocals);
      IdMap map = new IdMap(namespace, leftIds, rightIds, proved);
      result.put(namespace, map);
      for (Map.Entry<String, String> entry : map.mapping.entrySet()) mappingRows.add(comparison + "\t" + namespace + "\t" + entry.getKey() + "\t" + entry.getValue() + "\t" + entry.getKey().equals(entry.getValue()));
    }
    return result;
  }

  private static Map<String, IdMap> exactPathIdMaps(String comparison, Map<String, ClassModel> left,
                                                    Map<String, ClassModel> right, List<String> mappingRows,
                                                    Map<String, String> classPairs,
                                                    LocalUniverse leftLocals,
                                                    LocalUniverse rightLocals) {
    require(left.keySet().equals(right.keySet()), "exact-path witness requested but class paths differ in " + comparison);
    Map<String, List<ClassModel>> l = byNamespace(left), r = byNamespace(right);
    require(l.keySet().equals(r.keySet()), "exact-path witness namespace sets differ in " + comparison);
    Map<String, IdMap> result = new TreeMap<String, IdMap>();
    for (String namespace : l.keySet()) {
      Map<String, String> proof = new HashMap<String, String>(), reverse = new HashMap<String, String>();
      for (ClassModel model : l.get(namespace)) {
        constrainIds(namespace, model.name, right.get(model.name).name, proof, reverse);
        classPairs.put(model.name, model.name);
      }
      Set<String> leftIds = compilerNodeIds(l.get(namespace), leftLocals);
      Set<String> rightIds = compilerNodeIds(r.get(namespace), rightLocals);
      require(leftIds.equals(rightIds), "exact-path comparison has different complete compiler-ID sets in " +
          namespace + ": left-only=" + difference(leftIds, rightIds) + " right-only=" + difference(rightIds, leftIds));
      for (String id : leftIds) {
        String old = proof.putIfAbsent(id, id), oldReverse = reverse.putIfAbsent(id, id);
        require((old == null || old.equals(id)) && (oldReverse == null || oldReverse.equals(id)),
            "exact-path compiler-ID identity conflicts with a class constraint in " + namespace + ": " + id);
      }
      constrainStructuralGensyms(namespace, left, right, classPairs, proof, reverse);
      constrainGeneratedLocalNames(namespace, left, right, classPairs, leftLocals, rightLocals,
          proof, reverse);
      IdMap map = new IdMap(namespace, leftIds, rightIds, proof); result.put(namespace, map);
      for (Map.Entry<String, String> entry : map.mapping.entrySet())
        mappingRows.add(comparison + "\t" + namespace + "\t" + entry.getKey() + "\t" + entry.getValue() + "\t" + entry.getKey().equals(entry.getValue()));
    }
    return result;
  }

  private static List<String> unmatchedSkeletonClasses(Map<String, List<ClassModel>> from,
                                                        Map<String, List<ClassModel>> other) {
    List<String> result = new ArrayList<String>();
    for (Map.Entry<String, List<ClassModel>> entry : from.entrySet()) {
      if (other.containsKey(entry.getKey())) continue;
      for (ClassModel model : entry.getValue()) {
        result.add(model.name);
        if (result.size() == 5) return result;
      }
    }
    return result;
  }

  private static String skeletonDifferenceDiagnostic(Map<String, List<ClassModel>> leftGroups,
                                                       Map<String, List<ClassModel>> rightGroups,
                                                       Normalizer rightEraser,
                                                       Map<String, CaptureSpec> rightCaptures) {
    Map<String, ClassModel> rightByName = new HashMap<String, ClassModel>();
    Map<String, List<ClassModel>> rightByErasedName = new HashMap<String, List<ClassModel>>();
    for (List<ClassModel> group : rightGroups.values()) for (ClassModel model : group) {
      rightByName.put(model.name, model);
      rightByErasedName.computeIfAbsent(eraseIds(model.name), ignored -> new ArrayList<ClassModel>()).add(model);
    }
    for (Map.Entry<String, List<ClassModel>> entry : leftGroups.entrySet()) {
      if (rightGroups.containsKey(entry.getKey())) continue;
      for (ClassModel left : entry.getValue()) {
        ClassModel right = rightByName.get(left.name);
        if (right == null) {
          List<ClassModel> erased = rightByErasedName.get(eraseIds(left.name));
          if (erased != null && !erased.isEmpty()) {
            int longestPrefix = -1;
            for (ClassModel candidate : erased) {
              String candidateText = renderClass(candidate, rightEraser, rightCaptures).text;
              int prefix = firstDifference(entry.getKey(), candidateText);
              if (prefix > longestPrefix) { longestPrefix = prefix; right = candidate; }
            }
          }
        }
        if (right == null) continue;
        String a = entry.getKey();
        String b = renderClass(right, rightEraser, rightCaptures).text;
        int at = firstDifference(a, b);
        int begin = Math.max(0, at - 120), aEnd = Math.min(a.length(), at + 240), bEnd = Math.min(b.length(), at + 240);
        return left.name + "@" + at + " left=" + q(a.substring(begin, aEnd)) + " right=" + q(b.substring(begin, bEnd));
      }
    }
    return "no-same-raw-name-example";
  }

 private static Map<String, String> classNamespaces(Map<String, ClassModel> classes) { Map<String, String> result = new HashMap<String, String>(); for (ClassModel c : classes.values()) result.put(c.name, c.namespace); return result; }

  private static Map<String, IdMap> identityIdMaps(Map<String, ClassModel> classes) {
    Map<String, IdMap> result = new HashMap<String, IdMap>();
    LocalUniverse locals = LocalUniverse.of(classes, stateSlotUniverse(classes));
    for (Map.Entry<String, List<ClassModel>> entry : byNamespace(classes).entrySet()) {
      Set<String> values = compilerNodeIds(entry.getValue(), locals);
      Map<String, String> identity = new HashMap<String, String>(); for (String value : values) identity.put(value, value);
      result.put(entry.getKey(), new IdMap(entry.getKey(), values, values, identity));
    }
    return result;
  }

  private static Map<String, String> identityClassMap(Map<String, ClassModel> classes) {
    Map<String, String> result = new HashMap<String, String>();
    for (String name : classes.keySet()) result.put(name, name);
    return result;
  }

  private static FieldModel findField(ClassModel owner, MemberKey key) {
    for (FieldModel field : owner.fields)
      if (field.name.equals(key.name) && field.descriptor.equals(key.descriptor)) return field;
    throw new Failure("generated field declaration disappeared: " + key);
  }

  private static MethodModel findMethod(ClassModel owner, MemberKey key) {
    for (MethodModel method : owner.methods)
      if (method.name.equals(key.name) && method.descriptor.equals(key.descriptor)) return method;
    throw new Failure("generated method declaration disappeared: " + key);
  }

  private static Map<MemberKey, List<String>> memberUseContexts(Map<String, ClassModel> classes,
                                                                 MemberUniverse universe,
                                                                 Normalizer eraser,
                                                                 Map<String, CaptureSpec> activeCaptures) {
    Map<MemberKey, List<String>> result = new TreeMap<MemberKey, List<String>>();
    for (MemberKey key : universe.generated) result.put(key, new ArrayList<String>());
    for (ClassModel owner : classes.values()) for (MethodModel method : owner.methods) {
      if (method.name.equals("<init>")) continue;
      RenderedMethod rendered = renderMethod(owner, method, eraser, activeCaptures);
      if (rendered.collapsedSites != 0) continue;
      String source = eraser.internal(owner.name, owner.namespace) + "." +
          eraser.memberName(owner.name, "M", method.name, method.descriptor, owner.namespace) +
          eraser.descriptor(method.descriptor, owner.namespace);
      for (int i = 0; i < method.insns.size(); i++) {
        Insn insn = method.insns.get(i);
        if (!(insn.kind.equals("FIELD") || insn.kind.equals("METHOD"))) continue;
        MemberKey target = new MemberKey(insn.owner(), insn.kind.equals("FIELD") ? "F" : "M",
            insn.memberName(), insn.descriptor());
        if (!universe.generated.contains(target)) continue;
        List<String> window = new ArrayList<String>();
        for (int j = Math.max(0, i - 4); j <= Math.min(method.insns.size() - 1, i + 4); j++)
          window.add((j - i) + ":" + method.insns.get(j).render(eraser, owner.namespace));
        result.get(target).add(source + "|at=" + i + "|opcode=" + insn.opcode + "|window=" + join(window));
      }
    }
    for (List<String> contexts : result.values()) Collections.sort(contexts);
    return result;
  }

  private static String generatedMemberFingerprint(MemberKey key, Map<String, ClassModel> classes,
                                                   Normalizer eraser, Map<String, CaptureSpec> activeCaptures,
                                                   Map<MemberKey, List<String>> useContexts) {
    ClassModel owner = classes.get(key.owner);
    require(owner != null, "generated member owner is not in the exact cohort: " + key);
    String declaration;
    if (key.kind.equals("F")) declaration = findField(owner, key).render(eraser, owner.namespace, owner.name);
    else declaration = renderMethod(owner, findMethod(owner, key), eraser, activeCaptures).text;
    return key.kind + "|owner=" + eraser.internal(owner.name, owner.namespace) + "|decl=" + declaration +
        "|uses=[" + join(useContexts.get(key)) + "]";
  }

  private static final class MemberPairingProblem {
    final Map<MemberKey, MemberKey> fixed = new TreeMap<MemberKey, MemberKey>();
    final Map<MemberKey, List<MemberKey>> choices = new TreeMap<MemberKey, List<MemberKey>>();
    final Set<MemberKey> allRight = new TreeSet<MemberKey>();
  }

  private static MemberPairingProblem deriveMemberProblem(Map<String, ClassModel> left,
                                                            Map<String, ClassModel> right,
                                                            Map<String, IdMap> idMaps,
                                                            Map<String, String> classPairs,
                                                            MemberUniverse leftMembers,
                                                            MemberUniverse rightMembers,
                                                            Map<String, CaptureSpec> activeLeft,
                                                            Map<String, CaptureSpec> activeRight,
                                                            StateSlotUniverse leftSlots,
                                                            StateSlotUniverse rightSlots,
                                                            LocalUniverse leftLocals,
                                                            LocalUniverse rightLocals) {
    Map<String, String> rightClasses = identityClassMap(right);
    Normalizer leftEraser = new Normalizer(idMaps, classNamespaces(left), classPairs, false, true,
        leftMembers, Collections.emptyMap(), true, leftSlots, Collections.emptyMap(), null,
        true, leftLocals, Collections.emptyMap(), null);
    Normalizer rightEraser = new Normalizer(identityIdMaps(right), classNamespaces(right), rightClasses, false, true,
        rightMembers, Collections.emptyMap(), true, rightSlots, Collections.emptyMap(), null,
        true, rightLocals, Collections.emptyMap(), null);
    Map<MemberKey, List<String>> leftUses = memberUseContexts(left, leftMembers, leftEraser, activeLeft);
    Map<MemberKey, List<String>> rightUses = memberUseContexts(right, rightMembers, rightEraser, activeRight);
    Map<String, List<MemberKey>> leftGroups = new TreeMap<String, List<MemberKey>>();
    Map<String, List<MemberKey>> rightGroups = new TreeMap<String, List<MemberKey>>();
    for (MemberKey key : leftMembers.generated)
      leftGroups.computeIfAbsent(generatedMemberFingerprint(key, left, leftEraser, activeLeft, leftUses), ignored -> new ArrayList<MemberKey>()).add(key);
    for (MemberKey key : rightMembers.generated)
      rightGroups.computeIfAbsent(generatedMemberFingerprint(key, right, rightEraser, activeRight, rightUses), ignored -> new ArrayList<MemberKey>()).add(key);
    require(leftGroups.keySet().equals(rightGroups.keySet()),
        "generated member-node fingerprint sets differ: left=" + leftMembers.generated.size() + " right=" + rightMembers.generated.size() +
        " left-only=" + firstValues(difference(leftGroups.keySet(), rightGroups.keySet()), 3) +
        " right-only=" + firstValues(difference(rightGroups.keySet(), leftGroups.keySet()), 3));
    MemberPairingProblem result = new MemberPairingProblem();
    Set<MemberKey> rightMapped = new HashSet<MemberKey>();
    for (String fingerprint : leftGroups.keySet()) {
      List<MemberKey> a = leftGroups.get(fingerprint), b = rightGroups.get(fingerprint);
      require(a.size() == b.size(), "generated member-node fingerprint multiplicity differs: " + a + " vs " + b);
      Collections.sort(a); Collections.sort(b);
      for (MemberKey leftKey : a) {
        String expectedOwner = classPairs.get(leftKey.owner);
        require(expectedOwner != null, "generated member owner lacks a class-node mapping: " + leftKey);
        for (MemberKey rightKey : b)
          require(rightKey.owner.equals(expectedOwner) && rightKey.kind.equals(leftKey.kind),
              "generated member candidate crosses an owner/kind boundary: " + leftKey + " -> " + rightKey);
      }
      if (a.size() == 1) {
        result.fixed.put(a.get(0), b.get(0));
        require(rightMapped.add(b.get(0)), "generated member-node graph mapping is not injective: " + b.get(0));
      } else {
        for (MemberKey leftKey : a) result.choices.put(leftKey, new ArrayList<MemberKey>(b));
        result.allRight.addAll(b);
      }
    }
    result.allRight.addAll(rightMapped);
    require(result.fixed.size() + result.choices.size() == leftMembers.generated.size() &&
        result.allRight.size() == rightMembers.generated.size(), "generated member-node graph problem is incomplete");
    return result;
  }

  private static final class MemberPairingSearch {
    final String label;
    final Map<String, ClassModel> left;
    final Map<String, RenderedClass> rightRendered;
    final Map<String, IdMap> idMaps;
    final Map<String, String> classPairs;
    final MemberUniverse leftMembers;
    final Map<String, CaptureSpec> leftCaptures;
    final StateSlotUniverse leftSlots;
    final LocalUniverse leftLocals;
    final MemberPairingProblem problem;
    final List<MemberKey> order;
    long explored;
    String lastMismatch = "no complete witness evaluated";
    Map<MemberKey, MemberKey> solution;

    MemberPairingSearch(String label, Map<String, ClassModel> left, Map<String, RenderedClass> rightRendered,
                        Map<String, IdMap> idMaps, Map<String, String> classPairs,
                        MemberUniverse leftMembers, Map<String, CaptureSpec> leftCaptures,
                        StateSlotUniverse leftSlots, LocalUniverse leftLocals, MemberPairingProblem problem) {
      this.label = label; this.left = left; this.rightRendered = rightRendered; this.idMaps = idMaps;
      this.classPairs = classPairs; this.leftMembers = leftMembers; this.leftCaptures = leftCaptures;
      this.leftSlots = leftSlots; this.leftLocals = leftLocals;
      this.problem = problem; this.order = new ArrayList<MemberKey>(problem.choices.keySet());
    }

    boolean validateReady(Map<MemberKey, MemberKey> mapping, Set<String> validated) {
      Normalizer normalizer = new Normalizer(idMaps, classNamespaces(left), classPairs, false, false,
          leftMembers, mapping, true, leftSlots, Collections.emptyMap(), null,
          true, leftLocals, Collections.emptyMap(), null);
      for (ClassModel model : left.values()) {
        if (validated.contains(model.name)) continue;
        RenderedClass rendered;
        try { rendered = renderClass(model, normalizer, leftCaptures); }
        catch (IncompleteMapping notReady) { continue; }
        RenderedClass expected = rightRendered.get(rendered.name);
        if (expected == null) { lastMismatch = "normalized class is absent on right: " + rendered.name; return false; }
        if (!rendered.text.equals(expected.text)) {
          int at = firstDifference(rendered.text, expected.text);
          int begin = Math.max(0, at - 120);
          lastMismatch = rendered.name + "@" + at + " left=" +
              q(rendered.text.substring(begin, Math.min(rendered.text.length(), at + 240))) + " right=" +
              q(expected.text.substring(begin, Math.min(expected.text.length(), at + 240)));
          return false;
        }
        validated.add(model.name);
      }
      return true;
    }

    boolean search(int index, Map<MemberKey, MemberKey> mapping, Set<MemberKey> usedRight,
                   Set<String> validated) {
      explored++;
      if (!validateReady(mapping, validated)) return false;
      if (index == order.size()) {
        if (validated.size() != left.size()) {
          lastMismatch = "complete member mapping left classes unrenderable: " + (left.size() - validated.size());
          return false;
        }
        solution = new TreeMap<MemberKey, MemberKey>(mapping);
        return true;
      }
      MemberKey selected = order.get(index);
      for (MemberKey candidate : problem.choices.get(selected)) {
        if (!usedRight.add(candidate)) continue;
        mapping.put(selected, candidate);
        Set<String> nextValidated = new HashSet<String>(validated);
        if (search(index + 1, mapping, usedRight, nextValidated)) return true;
        mapping.remove(selected); usedRight.remove(candidate);
      }
      return false;
    }

    Map<MemberKey, MemberKey> solve() {
      Map<MemberKey, MemberKey> initial = new TreeMap<MemberKey, MemberKey>(problem.fixed);
      Set<MemberKey> used = new HashSet<MemberKey>(problem.fixed.values());
      require(search(0, initial, used, new HashSet<String>()),
          "no complete generated member-node graph isomorphism in " + label +
          ": explored=" + explored + " last-mismatch=" + lastMismatch);
      require(solution.size() == leftMembers.generated.size() && new HashSet<MemberKey>(solution.values()).size() == solution.size(),
          "selected generated member-node witness is incomplete or non-injective");
      return solution;
    }
  }

  private static Map<StateSlotKey, StateSlotKey> identityStateSlotMap(StateSlotUniverse universe) {
    Map<StateSlotKey, StateSlotKey> result = new TreeMap<StateSlotKey, StateSlotKey>();
    for (StateSlotKey key : universe.nodes) result.put(key, key);
    return result;
  }

  private static String stateSlotFingerprint(StateSlotKey selected,
                                             Map<String, ClassModel> classes,
                                             Map<String, IdMap> idMaps,
                                             Map<String, String> classMap,
                                             MemberUniverse members,
                                             Map<MemberKey, MemberKey> memberMap,
                                             StateSlotUniverse slots,
                                             LocalUniverse locals,
                                             Map<String, CaptureSpec> captures) {
    Normalizer normalizer = new Normalizer(idMaps, classNamespaces(classes), classMap, false, false,
        members, memberMap, true, slots, Collections.emptyMap(), selected,
        true, locals, Collections.emptyMap(), null);
    Set<MethodModel> seen = Collections.newSetFromMap(new IdentityHashMap<MethodModel, Boolean>());
    List<String> contexts = new ArrayList<String>();
    for (StateSlotAccess access : slots.byNode.get(selected)) {
      if (!seen.add(access.method)) continue;
      RenderedMethod method = renderMethod(access.owner, access.method, normalizer, captures);
      contexts.add(normalizer.internal(access.owner.name, access.owner.namespace) + "." +
          normalizer.memberName(access.owner.name, "M", access.method.name, access.method.descriptor,
              access.owner.namespace) + normalizer.descriptor(access.method.descriptor, access.owner.namespace) +
          "|" + method.text);
    }
    Collections.sort(contexts);
    return "owner=" + normalizer.internal(selected.machineOwner,
        classes.get(selected.machineOwner).namespace) + "|contexts=[" + join(contexts) + "]";
  }

  private static final class StateSlotPairingProblem {
    final Map<StateSlotKey, StateSlotKey> fixed = new TreeMap<StateSlotKey, StateSlotKey>();
    final Map<StateSlotKey, List<StateSlotKey>> choices = new TreeMap<StateSlotKey, List<StateSlotKey>>();
    final Set<StateSlotKey> allRight = new TreeSet<StateSlotKey>();
  }

  private static StateSlotPairingProblem deriveStateSlotProblem(Map<String, ClassModel> left,
                                                                 Map<String, ClassModel> right,
                                                                 Map<String, IdMap> idMaps,
                                                                 Map<String, String> classPairs,
                                                                 MemberUniverse leftMembers,
                                                                 MemberUniverse rightMembers,
                                                                 Map<MemberKey, MemberKey> memberPairs,
                                                                 StateSlotUniverse leftSlots,
                                                                 StateSlotUniverse rightSlots,
                                                                 LocalUniverse leftLocals,
                                                                 LocalUniverse rightLocals,
                                                                 Map<String, CaptureSpec> leftCaptures,
                                                                 Map<String, CaptureSpec> rightCaptures) {
    Map<MemberKey, MemberKey> rightMembersIdentity = new TreeMap<MemberKey, MemberKey>();
    for (MemberKey key : rightMembers.generated) rightMembersIdentity.put(key, key);
    Map<String, String> rightClasses = identityClassMap(right);
    Map<String, IdMap> rightIds = identityIdMaps(right);
    Map<String, List<StateSlotKey>> leftGroups = new TreeMap<String, List<StateSlotKey>>();
    Map<String, List<StateSlotKey>> rightGroups = new TreeMap<String, List<StateSlotKey>>();
    for (StateSlotKey key : leftSlots.nodes) {
      require(left.containsKey(key.machineOwner), "state-slot machine owner is absent from left cohort: " + key);
      String fingerprint = stateSlotFingerprint(key, left, idMaps, classPairs, leftMembers, memberPairs,
          leftSlots, leftLocals, leftCaptures);
      leftGroups.computeIfAbsent(fingerprint, ignored -> new ArrayList<StateSlotKey>()).add(key);
    }
    for (StateSlotKey key : rightSlots.nodes) {
      require(right.containsKey(key.machineOwner), "state-slot machine owner is absent from right cohort: " + key);
      String fingerprint = stateSlotFingerprint(key, right, rightIds, rightClasses, rightMembers,
          rightMembersIdentity, rightSlots, rightLocals, rightCaptures);
      rightGroups.computeIfAbsent(fingerprint, ignored -> new ArrayList<StateSlotKey>()).add(key);
    }
    require(leftGroups.keySet().equals(rightGroups.keySet()),
        "typed state-slot fingerprint sets differ: left=" + leftSlots.nodes.size() + " right=" + rightSlots.nodes.size() +
        " left-only=" + firstValues(difference(leftGroups.keySet(), rightGroups.keySet()), 3) +
        " right-only=" + firstValues(difference(rightGroups.keySet(), leftGroups.keySet()), 3));
    StateSlotPairingProblem result = new StateSlotPairingProblem();
    Set<StateSlotKey> fixedRight = new HashSet<StateSlotKey>();
    for (String fingerprint : leftGroups.keySet()) {
      List<StateSlotKey> a = leftGroups.get(fingerprint), b = rightGroups.get(fingerprint);
      require(a.size() == b.size(), "typed state-slot fingerprint multiplicity differs: " + a + " vs " + b);
      Collections.sort(a); Collections.sort(b);
      for (StateSlotKey leftKey : a) {
        String expectedOwner = classPairs.get(leftKey.machineOwner);
        require(expectedOwner != null, "state-machine owner lacks a class-node mapping: " + leftKey);
        for (StateSlotKey rightKey : b)
          require(rightKey.machineOwner.equals(expectedOwner),
              "typed state-slot candidate crosses a state-machine owner: " + leftKey + " -> " + rightKey);
      }
      if (a.size() == 1) {
        result.fixed.put(a.get(0), b.get(0));
        require(fixedRight.add(b.get(0)), "typed state-slot fixed mapping is not injective: " + b.get(0));
      } else {
        for (StateSlotKey key : a) result.choices.put(key, new ArrayList<StateSlotKey>(b));
        result.allRight.addAll(b);
      }
    }
    result.allRight.addAll(fixedRight);
    require(result.fixed.size() + result.choices.size() == leftSlots.nodes.size() &&
        result.allRight.size() == rightSlots.nodes.size(), "typed state-slot graph problem is incomplete");
    return result;
  }

  private static final class StateSlotPairingSearch {
    final String label;
    final Map<String, ClassModel> left;
    final Map<String, RenderedClass> rightRendered;
    final Map<String, IdMap> idMaps;
    final Map<String, String> classPairs;
    final MemberUniverse leftMembers;
    final Map<MemberKey, MemberKey> memberPairs;
    final StateSlotUniverse leftSlots;
    final LocalUniverse leftLocals;
    final Map<String, CaptureSpec> leftCaptures;
    final StateSlotPairingProblem problem;
    final List<StateSlotKey> order;
    long explored;
    String lastMismatch = "no complete witness evaluated";
    Map<StateSlotKey, StateSlotKey> solution;

    StateSlotPairingSearch(String label, Map<String, ClassModel> left,
                           Map<String, RenderedClass> rightRendered,
                           Map<String, IdMap> idMaps, Map<String, String> classPairs,
                           MemberUniverse leftMembers, Map<MemberKey, MemberKey> memberPairs,
                           StateSlotUniverse leftSlots, LocalUniverse leftLocals,
                           Map<String, CaptureSpec> leftCaptures,
                           StateSlotPairingProblem problem) {
      this.label = label; this.left = left; this.rightRendered = rightRendered;
      this.idMaps = idMaps; this.classPairs = classPairs; this.leftMembers = leftMembers;
      this.memberPairs = memberPairs; this.leftSlots = leftSlots; this.leftLocals = leftLocals;
      this.leftCaptures = leftCaptures;
      this.problem = problem; this.order = new ArrayList<StateSlotKey>(problem.choices.keySet());
    }

    boolean validateReady(Map<StateSlotKey, StateSlotKey> mapping, Set<String> validated) {
      Normalizer normalizer = new Normalizer(idMaps, classNamespaces(left), classPairs, false, false,
          leftMembers, memberPairs, false, leftSlots, mapping, null,
          true, leftLocals, Collections.emptyMap(), null);
      for (ClassModel model : left.values()) {
        if (validated.contains(model.name)) continue;
        RenderedClass rendered;
        try { rendered = renderClass(model, normalizer, leftCaptures); }
        catch (IncompleteMapping notReady) { continue; }
        RenderedClass expected = rightRendered.get(rendered.name);
        if (expected == null) { lastMismatch = "normalized class is absent on right: " + rendered.name; return false; }
        if (!rendered.text.equals(expected.text)) {
          int at = firstDifference(rendered.text, expected.text);
          int begin = Math.max(0, at - 120);
          lastMismatch = rendered.name + "@" + at + " left=" +
              q(rendered.text.substring(begin, Math.min(rendered.text.length(), at + 240))) + " right=" +
              q(expected.text.substring(begin, Math.min(expected.text.length(), at + 240)));
          return false;
        }
        validated.add(model.name);
      }
      return true;
    }

    boolean search(int index, Map<StateSlotKey, StateSlotKey> mapping,
                   Set<StateSlotKey> usedRight, Set<String> validated) {
      explored++;
      if (!validateReady(mapping, validated)) return false;
      if (index == order.size()) {
        if (validated.size() != left.size()) {
          lastMismatch = "complete state-slot mapping left classes unrenderable: " + (left.size() - validated.size());
          return false;
        }
        solution = new TreeMap<StateSlotKey, StateSlotKey>(mapping);
        return true;
      }
      StateSlotKey selected = order.get(index);
      for (StateSlotKey candidate : problem.choices.get(selected)) {
        if (!usedRight.add(candidate)) continue;
        mapping.put(selected, candidate);
        if (search(index + 1, mapping, usedRight, new HashSet<String>(validated))) return true;
        mapping.remove(selected); usedRight.remove(candidate);
      }
      return false;
    }

    Map<StateSlotKey, StateSlotKey> solve() {
      Map<StateSlotKey, StateSlotKey> initial = new TreeMap<StateSlotKey, StateSlotKey>(problem.fixed);
      Set<StateSlotKey> used = new HashSet<StateSlotKey>(problem.fixed.values());
      require(search(0, initial, used, new HashSet<String>()),
          "no complete typed state-slot graph isomorphism in " + label +
          ": explored=" + explored + " last-mismatch=" + lastMismatch);
      require(solution.size() == leftSlots.nodes.size() &&
          new HashSet<StateSlotKey>(solution.values()).size() == solution.size(),
          "selected typed state-slot witness is incomplete or non-injective");
      return solution;
    }
  }

  private static Map<LocalNodeKey, LocalNodeKey> identityLocalMap(LocalUniverse universe) {
    Map<LocalNodeKey, LocalNodeKey> result = new TreeMap<LocalNodeKey, LocalNodeKey>();
    for (LocalNodeKey key : universe.generated) result.put(key, key);
    return result;
  }

  private static String localNodeFingerprint(LocalNodeKey selected,
                                             Map<String, ClassModel> classes,
                                             Map<String, IdMap> idMaps,
                                             Map<String, String> classMap,
                                             MemberUniverse members,
                                             Map<MemberKey, MemberKey> memberMap,
                                             StateSlotUniverse slots,
                                             Map<StateSlotKey, StateSlotKey> slotMap,
                                             LocalUniverse locals,
                                             Map<String, CaptureSpec> captures) {
    LocalNodeInfo info = locals.info.get(selected);
    require(info != null && !info.boundary, "selected local node is not quotient-eligible: " + selected);
    Normalizer normalizer = new Normalizer(idMaps, classNamespaces(classes), classMap, false, false,
        members, memberMap, false, slots, slotMap, null,
        true, locals, Collections.emptyMap(), selected);
    RenderedMethod rendered = renderMethod(info.owner, info.method, normalizer, captures);
    return "method=" + normalizer.internal(info.owner.name, info.owner.namespace) + "." +
        normalizer.memberName(info.owner.name, "M", info.method.name, info.method.descriptor, info.owner.namespace) +
        normalizer.descriptor(info.method.descriptor, info.owner.namespace) + "|category=" + selected.category +
        "|physical-count=" + info.physicalSlots.size() + "|model=" + rendered.text;
  }

  private static final class LocalPairingProblem {
    final Map<LocalNodeKey, LocalNodeKey> fixed = new TreeMap<LocalNodeKey, LocalNodeKey>();
    final Map<LocalNodeKey, List<LocalNodeKey>> choices = new TreeMap<LocalNodeKey, List<LocalNodeKey>>();
    final Set<LocalNodeKey> allRight = new TreeSet<LocalNodeKey>();
  }

  private static LocalPairingProblem deriveLocalProblem(Map<String, ClassModel> left,
                                                         Map<String, ClassModel> right,
                                                         Map<String, IdMap> idMaps,
                                                         Map<String, String> classPairs,
                                                         MemberUniverse leftMembers,
                                                         MemberUniverse rightMembers,
                                                         Map<MemberKey, MemberKey> memberPairs,
                                                         StateSlotUniverse leftSlots,
                                                         StateSlotUniverse rightSlots,
                                                         Map<StateSlotKey, StateSlotKey> stateSlotPairs,
                                                         LocalUniverse leftLocals,
                                                         LocalUniverse rightLocals,
                                                         Map<String, CaptureSpec> leftCaptures,
                                                         Map<String, CaptureSpec> rightCaptures) {
    Map<MemberKey, MemberKey> rightMemberIdentity = new TreeMap<MemberKey, MemberKey>();
    for (MemberKey key : rightMembers.generated) rightMemberIdentity.put(key, key);
    Map<String, String> rightClasses = identityClassMap(right);
    Map<String, IdMap> rightIds = identityIdMaps(right);
    Map<StateSlotKey, StateSlotKey> rightSlotIdentity = identityStateSlotMap(rightSlots);
    Map<String, List<LocalNodeKey>> leftGroups = new TreeMap<String, List<LocalNodeKey>>();
    Map<String, List<LocalNodeKey>> rightGroups = new TreeMap<String, List<LocalNodeKey>>();
    for (LocalNodeKey key : leftLocals.generated) {
      String fingerprint = localNodeFingerprint(key, left, idMaps, classPairs, leftMembers, memberPairs,
          leftSlots, stateSlotPairs, leftLocals, leftCaptures);
      leftGroups.computeIfAbsent(fingerprint, ignored -> new ArrayList<LocalNodeKey>()).add(key);
    }
    for (LocalNodeKey key : rightLocals.generated) {
      String fingerprint = localNodeFingerprint(key, right, rightIds, rightClasses, rightMembers,
          rightMemberIdentity, rightSlots, rightSlotIdentity, rightLocals, rightCaptures);
      rightGroups.computeIfAbsent(fingerprint, ignored -> new ArrayList<LocalNodeKey>()).add(key);
    }
    require(leftGroups.keySet().equals(rightGroups.keySet()),
        "typed local def-use fingerprint sets differ: left=" + leftLocals.generated.size() +
        " right=" + rightLocals.generated.size() + " left-only=" +
        firstValues(difference(leftGroups.keySet(), rightGroups.keySet()), 3) + " right-only=" +
        firstValues(difference(rightGroups.keySet(), leftGroups.keySet()), 3));
    LocalPairingProblem result = new LocalPairingProblem();
    Set<LocalNodeKey> fixedRight = new HashSet<LocalNodeKey>();
    for (String fingerprint : leftGroups.keySet()) {
      List<LocalNodeKey> a = leftGroups.get(fingerprint), b = rightGroups.get(fingerprint);
      require(a.size() == b.size(), "typed local def-use fingerprint multiplicity differs: " + a + " vs " + b);
      Collections.sort(a); Collections.sort(b);
      for (LocalNodeKey leftKey : a) {
        String expectedOwner = classPairs.get(leftKey.owner);
        require(expectedOwner != null, "local-node method owner lacks a class mapping: " + leftKey);
        for (LocalNodeKey rightKey : b)
          require(rightKey.owner.equals(expectedOwner) && rightKey.methodName.equals(leftKey.methodName) &&
                  rightKey.methodDescriptor.equals(leftKey.methodDescriptor) && rightKey.category.equals(leftKey.category),
              "typed local candidate crosses a method/category boundary: " + leftKey + " -> " + rightKey);
      }
      if (a.size() == 1) {
        result.fixed.put(a.get(0), b.get(0));
        require(fixedRight.add(b.get(0)), "typed local fixed mapping is not injective: " + b.get(0));
      } else {
        for (LocalNodeKey key : a) result.choices.put(key, new ArrayList<LocalNodeKey>(b));
        result.allRight.addAll(b);
      }
    }
    result.allRight.addAll(fixedRight);
    require(result.fixed.size() + result.choices.size() == leftLocals.generated.size() &&
        result.allRight.size() == rightLocals.generated.size(), "typed local def-use graph problem is incomplete");
    return result;
  }

  private static final class LocalPairingSearch {
    final String label;
    final Map<String, ClassModel> left;
    final Map<String, RenderedClass> rightRendered;
    final Map<String, IdMap> idMaps;
    final Map<String, String> classPairs;
    final MemberUniverse leftMembers;
    final Map<MemberKey, MemberKey> memberPairs;
    final StateSlotUniverse leftSlots;
    final Map<StateSlotKey, StateSlotKey> stateSlotPairs;
    final LocalUniverse leftLocals;
    final Map<String, CaptureSpec> leftCaptures;
    final LocalPairingProblem problem;
    final List<LocalNodeKey> order;
    long explored;
    String lastMismatch = "no complete witness evaluated";
    Map<LocalNodeKey, LocalNodeKey> solution;

    LocalPairingSearch(String label, Map<String, ClassModel> left,
                       Map<String, RenderedClass> rightRendered,
                       Map<String, IdMap> idMaps, Map<String, String> classPairs,
                       MemberUniverse leftMembers, Map<MemberKey, MemberKey> memberPairs,
                       StateSlotUniverse leftSlots, Map<StateSlotKey, StateSlotKey> stateSlotPairs,
                       LocalUniverse leftLocals, Map<String, CaptureSpec> leftCaptures,
                       LocalPairingProblem problem) {
      this.label = label; this.left = left; this.rightRendered = rightRendered; this.idMaps = idMaps;
      this.classPairs = classPairs; this.leftMembers = leftMembers; this.memberPairs = memberPairs;
      this.leftSlots = leftSlots; this.stateSlotPairs = stateSlotPairs; this.leftLocals = leftLocals;
      this.leftCaptures = leftCaptures; this.problem = problem;
      this.order = new ArrayList<LocalNodeKey>(problem.choices.keySet());
    }

    boolean validateReady(Map<LocalNodeKey, LocalNodeKey> mapping, Set<String> validated) {
      Normalizer normalizer = new Normalizer(idMaps, classNamespaces(left), classPairs, false, false,
          leftMembers, memberPairs, false, leftSlots, stateSlotPairs, null,
          false, leftLocals, mapping, null);
      for (ClassModel model : left.values()) {
        if (validated.contains(model.name)) continue;
        RenderedClass rendered;
        try { rendered = renderClass(model, normalizer, leftCaptures); }
        catch (IncompleteMapping notReady) { continue; }
        RenderedClass expected = rightRendered.get(rendered.name);
        if (expected == null) { lastMismatch = "normalized class is absent on right: " + rendered.name; return false; }
        if (!rendered.text.equals(expected.text)) {
          int at = firstDifference(rendered.text, expected.text);
          int begin = Math.max(0, at - 120);
          lastMismatch = rendered.name + "@" + at + " left=" +
              q(rendered.text.substring(begin, Math.min(rendered.text.length(), at + 240))) + " right=" +
              q(expected.text.substring(begin, Math.min(expected.text.length(), at + 240)));
          return false;
        }
        validated.add(model.name);
      }
      return true;
    }

    boolean search(int index, Map<LocalNodeKey, LocalNodeKey> mapping,
                   Set<LocalNodeKey> usedRight, Set<String> validated) {
      explored++;
      if (!validateReady(mapping, validated)) return false;
      if (index == order.size()) {
        if (validated.size() != left.size()) {
          lastMismatch = "complete local mapping left classes unrenderable: " + (left.size() - validated.size());
          return false;
        }
        solution = new TreeMap<LocalNodeKey, LocalNodeKey>(mapping); return true;
      }
      LocalNodeKey selected = order.get(index);
      for (LocalNodeKey candidate : problem.choices.get(selected)) {
        if (!usedRight.add(candidate)) continue;
        mapping.put(selected, candidate);
        if (search(index + 1, mapping, usedRight, new HashSet<String>(validated))) return true;
        mapping.remove(selected); usedRight.remove(candidate);
      }
      return false;
    }

    Map<LocalNodeKey, LocalNodeKey> solve() {
      Map<LocalNodeKey, LocalNodeKey> initial = new TreeMap<LocalNodeKey, LocalNodeKey>(problem.fixed);
      Set<LocalNodeKey> used = new HashSet<LocalNodeKey>(problem.fixed.values());
      require(search(0, initial, used, new HashSet<String>()),
          "no complete typed local def-use graph isomorphism in " + label +
          ": explored=" + explored + " last-mismatch=" + lastMismatch);
      require(solution.size() == leftLocals.generated.size() &&
          new HashSet<LocalNodeKey>(solution.values()).size() == solution.size(),
          "selected typed local witness is incomplete or non-injective");
      return solution;
    }
  }

  private static final class ComparisonResult {
    final String label;
    int classes;
    int capturesLeft;
    int capturesRight;
    int useSitesLeft;
    int useSitesRight;
    int sourceSetsLeft;
    int sourceSetsRight;
    int discardedLocalNoopsLeft;
    int discardedLocalNoopsRight;
    int classNodes;
    int memberNodes;
    int stateSlotNodes;
    int localNodes;
    int stateSlotAccessesLeft;
    int stateSlotAccessesRight;
    ComparisonResult(String label) { this.label = label; }
  }

  private static final class RelationEvidence {
    final List<String> mappingRows = new ArrayList<String>();
    final List<String> captureRows = new ArrayList<String>();
    final List<String> classRows = new ArrayList<String>();
    final List<String> memberRows = new ArrayList<String>();
    final List<String> stateSlotRows = new ArrayList<String>();
    final List<String> localRows = new ArrayList<String>();

    void mergeInto(RelationEvidence destination) {
      destination.mappingRows.addAll(mappingRows);
      destination.captureRows.addAll(captureRows);
      destination.classRows.addAll(classRows);
      destination.memberRows.addAll(memberRows);
      destination.stateSlotRows.addAll(stateSlotRows);
      destination.localRows.addAll(localRows);
    }
  }

  private interface RelationOperation {
    ComparisonResult run(RelationEvidence evidence);
  }

  private static final class RelationSpec {
    final String label;
    final RelationOperation operation;

    RelationSpec(String label, RelationOperation operation) {
      this.label = label;
      this.operation = operation;
    }
  }

  private static final class RelationOutcome {
    final String label;
    final ComparisonResult result;
    final Failure failure;

    RelationOutcome(String label, ComparisonResult result, Failure failure) {
      this.label = label;
      this.result = result;
      this.failure = failure;
    }

    boolean passed() { return result != null && failure == null; }
  }

  private static final class RelationRun {
    final List<RelationOutcome> outcomes;
    final RelationEvidence evidence;

    RelationRun(List<RelationOutcome> outcomes, RelationEvidence evidence) {
      this.outcomes = outcomes;
      this.evidence = evidence;
    }
  }

  private static RelationRun runRelations(List<RelationSpec> relations) {
    RelationEvidence acceptedEvidence = new RelationEvidence();
    List<RelationOutcome> outcomes = new ArrayList<RelationOutcome>();
    for (RelationSpec relation : relations) {
      RelationEvidence relationEvidence = new RelationEvidence();
      try {
        ComparisonResult result = relation.operation.run(relationEvidence);
        require(result != null, "relation returned no result: " + relation.label);
        require(relation.label.equals(result.label), "relation result label differs: expected " +
            relation.label + " got " + result.label);
        relationEvidence.mergeInto(acceptedEvidence);
        outcomes.add(new RelationOutcome(relation.label, result, null));
      } catch (Failure failure) {
        outcomes.add(new RelationOutcome(relation.label, null, failure));
      }
    }
    return new RelationRun(outcomes, acceptedEvidence);
  }

  private static List<String> relationLabels(List<RelationSpec> relations) {
    List<String> labels = new ArrayList<String>();
    for (RelationSpec relation : relations) labels.add(relation.label);
    return labels;
  }

  private static boolean relationLedgerComplete(List<String> requested,
                                                List<RelationOutcome> outcomes) {
    if (requested.size() != outcomes.size()) return false;
    Set<String> seen = new HashSet<String>();
    for (int index = 0; index < requested.size(); index++) {
      RelationOutcome outcome = outcomes.get(index);
      if (!requested.get(index).equals(outcome.label) || !seen.add(outcome.label)) return false;
      if ((outcome.result != null) == (outcome.failure != null)) return false;
    }
    return true;
  }

  private static int relationRunStatus(List<String> requested,
                                       List<RelationOutcome> outcomes) {
    if (!relationLedgerComplete(requested, outcomes)) return 1;
    for (RelationOutcome outcome : outcomes) if (!outcome.passed()) return 1;
    return 0;
  }

  private static String comparisonRow(RelationOutcome outcome) {
    if (!outcome.passed()) return outcome.label + "\tFAIL\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0";
    ComparisonResult result = outcome.result;
    return result.label + "\tPASS\t" + result.classes + "\t" + result.classNodes + "\t" +
        result.memberNodes + "\t" + result.stateSlotNodes + "\t" + result.localNodes + "\t" +
        result.capturesLeft + "\t" + result.capturesRight + "\t" + result.useSitesLeft + "\t" +
        result.useSitesRight + "\t" + result.stateSlotAccessesLeft + "\t" +
        result.stateSlotAccessesRight;
  }

  private static ComparisonResult syntheticRelationResult(String label) {
    ComparisonResult result = new ComparisonResult(label);
    result.classes = 1;
    result.classNodes = 1;
    return result;
  }

  private static RelationSpec syntheticRelation(String label, List<String> execution,
                                                boolean fail) {
    return new RelationSpec(label, evidence -> {
      execution.add(label);
      evidence.mappingRows.add(label + "\tsynthetic-evidence");
      if (fail) throw new Failure("synthetic relation failure: " + label);
      return syntheticRelationResult(label);
    });
  }

  private static ComparisonResult compare(String label, Map<String, ClassModel> left, Map<String, ClassModel> right, List<String> mappingRows, List<String> captureRows) {
    return compare(label, left, right, mappingRows, captureRows, new ArrayList<String>(), new ArrayList<String>());
  }

  private static ComparisonResult compare(String label, Map<String, ClassModel> left, Map<String, ClassModel> right,
                                          List<String> mappingRows, List<String> captureRows,
                                          List<String> memberRows, List<String> stateSlotRows) {
    return compare(label, left, right, mappingRows, captureRows, new ArrayList<String>(), memberRows,
        stateSlotRows, new ArrayList<String>());
  }

  private static ComparisonResult compare(String label, Map<String, ClassModel> left, Map<String, ClassModel> right,
                                          List<String> mappingRows, List<String> captureRows,
                                          List<String> classRows, List<String> memberRows,
                                          List<String> stateSlotRows, List<String> localRows) {
    MemberUniverse leftMembers = MemberUniverse.of(left), rightMembers = MemberUniverse.of(right);
    StateSlotUniverse leftSlots = stateSlotUniverse(left), rightSlots = stateSlotUniverse(right);
    LocalUniverse leftLocals = LocalUniverse.of(left, leftSlots), rightLocals = LocalUniverse.of(right, rightSlots);
    ActiveCapturePair active = activeCapturePairs(left, right);
    Map<String, String> classPairs = new TreeMap<String, String>();
    Map<String, IdMap> maps = label.equals("candidate-a--candidate-b")
        ? exactPathIdMaps(label, left, right, mappingRows, classPairs, leftLocals, rightLocals)
        : idMaps(label, left, right, active.left, active.right, leftMembers, rightMembers,
            leftSlots, rightSlots, leftLocals, rightLocals, mappingRows, classPairs);
    require(classPairs.keySet().equals(left.keySet()), "proved class-node mapping is incomplete in " + label);
    require(new HashSet<String>(classPairs.values()).equals(right.keySet()) &&
        new HashSet<String>(classPairs.values()).size() == classPairs.size(),
        "proved class-node mapping is not a complete bijection in " + label);
    MemberPairingProblem memberProblem = deriveMemberProblem(left, right, maps, classPairs, leftMembers,
        rightMembers, active.left, active.right, leftSlots, rightSlots, leftLocals, rightLocals);
    Map<MemberKey, MemberKey> rightIdentityMembers = new TreeMap<MemberKey, MemberKey>();
    for (MemberKey key : rightMembers.generated) rightIdentityMembers.put(key, key);
    Map<String, String> rightIdentityClasses = identityClassMap(right);
    Map<String, CaptureSpec> leftCaptures = active.left, rightCaptures = active.right;
    Normalizer rightMemberNorm = new Normalizer(identityIdMaps(right), classNamespaces(right), rightIdentityClasses,
        false, false, rightMembers, rightIdentityMembers, true, rightSlots, Collections.emptyMap(), null,
        true, rightLocals, Collections.emptyMap(), null);
    Map<String, RenderedClass> memberRenderedRight = new TreeMap<String, RenderedClass>();
    for (ClassModel c : right.values()) { RenderedClass rendered = renderClass(c, rightMemberNorm, rightCaptures); require(memberRenderedRight.put(rendered.name, rendered) == null, "right class-name collision in " + label + ": " + rendered.name); }
    MemberPairingSearch memberSearch = new MemberPairingSearch(label, left, memberRenderedRight, maps, classPairs,
        leftMembers, leftCaptures, leftSlots, leftLocals, memberProblem);
    Map<MemberKey, MemberKey> memberPairs = memberSearch.solve();
    Map<StateSlotKey, StateSlotKey> rightIdentitySlots = identityStateSlotMap(rightSlots);
    Normalizer rightSlotNorm = new Normalizer(identityIdMaps(right), classNamespaces(right), rightIdentityClasses,
        false, false, rightMembers, rightIdentityMembers, false, rightSlots, rightIdentitySlots, null,
        true, rightLocals, Collections.emptyMap(), null);
    Map<String, RenderedClass> slotRenderedRight = new TreeMap<String, RenderedClass>();
    for (ClassModel c : right.values()) { RenderedClass rendered = renderClass(c, rightSlotNorm, rightCaptures); require(slotRenderedRight.put(rendered.name, rendered) == null, "right state-slot class-name collision in " + label + ": " + rendered.name); }
    StateSlotPairingProblem slotProblem = deriveStateSlotProblem(left, right, maps, classPairs, leftMembers,
        rightMembers, memberPairs, leftSlots, rightSlots, leftLocals, rightLocals, leftCaptures, rightCaptures);
    StateSlotPairingSearch slotSearch = new StateSlotPairingSearch(label, left, slotRenderedRight, maps, classPairs,
        leftMembers, memberPairs, leftSlots, leftLocals, leftCaptures, slotProblem);
    Map<StateSlotKey, StateSlotKey> stateSlotPairs = slotSearch.solve();
    Map<LocalNodeKey, LocalNodeKey> rightIdentityLocals = identityLocalMap(rightLocals);
    Normalizer rightNorm = new Normalizer(identityIdMaps(right), classNamespaces(right), rightIdentityClasses,
        false, false, rightMembers, rightIdentityMembers, false, rightSlots, rightIdentitySlots, null,
        false, rightLocals, rightIdentityLocals, null);
    Map<String, RenderedClass> renderedRight = new TreeMap<String, RenderedClass>();
    for (ClassModel c : right.values()) { RenderedClass rendered = renderClass(c, rightNorm, rightCaptures); require(renderedRight.put(rendered.name, rendered) == null, "right final class-name collision in " + label + ": " + rendered.name); }
    LocalPairingProblem localProblem = deriveLocalProblem(left, right, maps, classPairs, leftMembers, rightMembers,
        memberPairs, leftSlots, rightSlots, stateSlotPairs, leftLocals, rightLocals, leftCaptures, rightCaptures);
    LocalPairingSearch localSearch = new LocalPairingSearch(label, left, renderedRight, maps, classPairs,
        leftMembers, memberPairs, leftSlots, stateSlotPairs, leftLocals, leftCaptures, localProblem);
    Map<LocalNodeKey, LocalNodeKey> localPairs = localSearch.solve();
    Normalizer leftNorm = new Normalizer(maps, classNamespaces(left), classPairs, false, false,
        leftMembers, memberPairs, false, leftSlots, stateSlotPairs, null,
        false, leftLocals, localPairs, null);
    Map<String, RenderedClass> renderedLeft = new TreeMap<String, RenderedClass>();
    for (ClassModel c : left.values()) { RenderedClass rendered = renderClass(c, leftNorm, leftCaptures); require(renderedLeft.put(rendered.name, rendered) == null, "normalized class-name collision in " + label + ": " + rendered.name); }
    require(renderedLeft.keySet().equals(renderedRight.keySet()), "normalized class-name sets differ in " + label + ": left-only=" + difference(renderedLeft.keySet(), renderedRight.keySet()) + " right-only=" + difference(renderedRight.keySet(), renderedLeft.keySet()));
    for (String name : renderedLeft.keySet()) {
      String a = renderedLeft.get(name).text, b = renderedRight.get(name).text;
      int difference = firstDifference(a, b);
      int begin = Math.max(0, difference - 160);
      int aEnd = Math.min(a.length(), difference + 320), bEnd = Math.min(b.length(), difference + 320);
      require(a.equals(b), "normalized ABI/instruction model differs in " + label + " for " + name +
          ": first-difference=" + difference + " left-context=" + q(a.substring(begin, aEnd)) +
          " right-context=" + q(b.substring(begin, bEnd)));
    }
    ComparisonResult result = new ComparisonResult(label);
    result.classes = renderedLeft.size(); result.capturesLeft = leftCaptures.size(); result.capturesRight = rightCaptures.size();
    result.classNodes = classPairs.size(); result.memberNodes = memberPairs.size();
    result.stateSlotNodes = stateSlotPairs.size(); result.localNodes = localPairs.size();
    for (RenderedClass c : renderedLeft.values()) { result.useSitesLeft += c.collapsedSites; result.sourceSetsLeft += c.sourceSetLiterals; result.discardedLocalNoopsLeft += c.discardedLocalNoops; result.stateSlotAccessesLeft += c.stateSlotAccesses; }
    for (RenderedClass c : renderedRight.values()) { result.useSitesRight += c.collapsedSites; result.sourceSetsRight += c.sourceSetLiterals; result.discardedLocalNoopsRight += c.discardedLocalNoops; result.stateSlotAccessesRight += c.stateSlotAccesses; }
    require(result.useSitesLeft == result.useSitesRight, "captured-constructor use-site counts differ in " + label + ": " + result.useSitesLeft + " vs " + result.useSitesRight);
    require(result.sourceSetsLeft == result.sourceSetsRight, "bounded source-set literal counts differ in " + label + ": " + result.sourceSetsLeft + " vs " + result.sourceSetsRight);
    require(result.stateSlotAccessesLeft == result.stateSlotAccessesRight,
        "typed state-slot access counts differ in " + label + ": " + result.stateSlotAccessesLeft + " vs " + result.stateSlotAccessesRight);
    for (CaptureSpec spec : leftCaptures.values()) captureRows.add(label + "\tleft\t" + spec.owner.namespace + "\t" + spec.owner.name + "\t" + spec.constructor.descriptor + "\t" + spec.fieldsByArgument.size());
    for (CaptureSpec spec : rightCaptures.values()) captureRows.add(label + "\tright\t" + spec.owner.namespace + "\t" + spec.owner.name + "\t" + spec.constructor.descriptor + "\t" + spec.fieldsByArgument.size());
    for (Map.Entry<String, String> entry : classPairs.entrySet())
      classRows.add(label + "\t" + left.get(entry.getKey()).namespace + "\t" + entry.getKey() + "\t" +
          entry.getValue() + "\t" + entry.getKey().equals(entry.getValue()));
    for (Map.Entry<MemberKey, MemberKey> entry : memberPairs.entrySet())
      memberRows.add(label + "\t" + entry.getKey().owner + "\t" + entry.getKey().kind + "\t" +
          entry.getKey().name + "\t" + entry.getKey().descriptor + "\t" + entry.getValue().owner + "\t" +
          entry.getValue().name + "\t" + entry.getValue().descriptor);
    for (Map.Entry<StateSlotKey, StateSlotKey> entry : stateSlotPairs.entrySet())
      stateSlotRows.add(label + "\t" + entry.getKey().machineOwner + "\t" + entry.getKey().index + "\t" +
          entry.getValue().machineOwner + "\t" + entry.getValue().index + "\t" +
          leftSlots.byNode.get(entry.getKey()).size() + "\t" + rightSlots.byNode.get(entry.getValue()).size());
    for (Map.Entry<LocalNodeKey, LocalNodeKey> entry : localPairs.entrySet()) {
      LocalNodeKey a = entry.getKey(), b = entry.getValue();
      LocalNodeInfo aInfo = leftLocals.info.get(a), bInfo = rightLocals.info.get(b);
      localRows.add(label + "\t" + a.owner + "\t" + a.methodName + "\t" + a.methodDescriptor + "\t" +
          a.ordinal + "\t" + a.category + "\t" + aInfo.physicalSlots + "\t" + b.owner + "\t" +
          b.methodName + "\t" + b.methodDescriptor + "\t" + b.ordinal + "\t" + b.category + "\t" +
          bInfo.physicalSlots);
    }
    return result;
  }

  private static <T> Set<T> difference(Set<T> a, Set<T> b) { Set<T> result = new TreeSet<T>(); result.addAll(a); result.removeAll(b); return result; }
  private static <T> List<T> firstValues(Collection<T> values, int limit) { List<T> result = new ArrayList<T>(); for (T value : values) { result.add(value); if (result.size() == limit) break; } return result; }
  private static int firstDifference(String a, String b) { int limit = Math.min(a.length(), b.length()); for (int i = 0; i < limit; i++) if (a.charAt(i) != b.charAt(i)) return i; return limit; }

  private static void write(Path path, String header, List<String> rows) throws IOException {
    List<String> lines = new ArrayList<String>(); lines.add(header); lines.addAll(rows);
    Files.write(path, lines, StandardCharsets.UTF_8, StandardOpenOption.CREATE_NEW);
  }

  private static int selfTest(Path output) throws IOException {
    Files.createDirectories(output);
    List<String> rows = new ArrayList<String>();
    test(rows, "relation-driver-pass-fail-pass-is-sequential-and-complete", () -> {
      List<String> execution = new ArrayList<String>();
      List<RelationSpec> relations = Arrays.asList(
          syntheticRelation("relation-1", execution, false),
          syntheticRelation("relation-2", execution, true),
          syntheticRelation("relation-3", execution, false));
      List<String> requested = relationLabels(relations);
      RelationRun run = runRelations(relations);
      require(execution.equals(requested), "pass/fail/pass execution order differs: " + execution);
      require(relationLedgerComplete(requested, run.outcomes),
          "pass/fail/pass ledger is incomplete");
      require(relationRunStatus(requested, run.outcomes) == 1,
          "pass/fail/pass run did not fail overall");
      require(run.outcomes.get(0).passed() && !run.outcomes.get(1).passed() &&
          run.outcomes.get(2).passed(), "pass/fail/pass outcomes differ");
      require(run.evidence.mappingRows.equals(Arrays.asList(
          "relation-1\tsynthetic-evidence", "relation-3\tsynthetic-evidence")),
          "failed relation leaked partial evidence: " + run.evidence.mappingRows);
    });
    test(rows, "relation-driver-all-pass-is-complete", () -> {
      List<String> execution = new ArrayList<String>();
      List<RelationSpec> relations = Arrays.asList(
          syntheticRelation("relation-1", execution, false),
          syntheticRelation("relation-2", execution, false),
          syntheticRelation("relation-3", execution, false));
      List<String> requested = relationLabels(relations);
      RelationRun run = runRelations(relations);
      require(execution.equals(requested), "all-pass execution order differs: " + execution);
      require(relationLedgerComplete(requested, run.outcomes) &&
          relationRunStatus(requested, run.outcomes) == 0,
          "all-pass relation run did not close");
      require(run.evidence.mappingRows.size() == 3,
          "all-pass evidence count differs: " + run.evidence.mappingRows.size());
    });
    test(rows, "relation-driver-first-failure-does-not-stop-later-relations", () -> {
      List<String> execution = new ArrayList<String>();
      List<RelationSpec> relations = Arrays.asList(
          syntheticRelation("relation-1", execution, true),
          syntheticRelation("relation-2", execution, false),
          syntheticRelation("relation-3", execution, false));
      List<String> requested = relationLabels(relations);
      RelationRun run = runRelations(relations);
      require(execution.equals(requested), "first-fail execution order differs: " + execution);
      require(!run.outcomes.get(0).passed() && run.outcomes.get(1).passed() &&
          run.outcomes.get(2).passed(), "first-fail outcomes differ");
      require(relationRunStatus(requested, run.outcomes) == 1,
          "first-fail run did not fail overall");
      require(run.evidence.mappingRows.equals(Arrays.asList(
          "relation-2\tsynthetic-evidence", "relation-3\tsynthetic-evidence")),
          "first failed relation leaked evidence: " + run.evidence.mappingRows);
    });
    test(rows, "relation-ledger-absence-is-an-overall-failure", () -> {
      List<String> execution = new ArrayList<String>();
      List<RelationSpec> relations = Arrays.asList(
          syntheticRelation("relation-1", execution, false),
          syntheticRelation("relation-2", execution, false),
          syntheticRelation("relation-3", execution, false));
      List<String> requested = relationLabels(relations);
      RelationRun complete = runRelations(relations);
      List<String> completeRows = new ArrayList<String>();
      for (RelationOutcome outcome : complete.outcomes) completeRows.add(comparisonRow(outcome));
      require(completeRows.size() == requested.size(),
          "complete relation run did not render one row per request");
      for (int index = 0; index < completeRows.size(); index++) {
        String[] fields = completeRows.get(index).split("\\t", -1);
        require(fields.length == 13 && fields[0].equals(requested.get(index)) &&
            fields[1].equals("PASS"), "rendered relation ledger row is malformed: " +
            completeRows.get(index));
      }
      List<RelationOutcome> missing = new ArrayList<RelationOutcome>(complete.outcomes);
      missing.remove(1);
      require(!relationLedgerComplete(requested, missing),
          "relation ledger accepted a missing requested row");
      require(relationRunStatus(requested, missing) == 1,
          "missing requested relation was not an overall failure");
    });
    test(rows, "id-bijection-explicitly-proved", () -> { Map<String, String> proof = new HashMap<String, String>(); proof.put("10", "100"); proof.put("20", "200"); proof.put("6789", "300"); IdMap m = new IdMap("test", set("10", "20", "6789"), set("100", "200", "300"), proof); require(m.mapping.equals(proof), "unexpected ID map"); });
    testFailure(rows, "id-incomplete-proof-rejected", () -> new IdMap("test", set("1"), set("2", "3"), Collections.singletonMap("1", "2")));
    testFailure(rows, "id-conflicting-constraint-rejected", () -> { Map<String, String> forward = new HashMap<String, String>(); Map<String, String> reverse = new HashMap<String, String>(); constrainIds("test", "x__1", "x__2", forward, reverse); constrainIds("test", "y__1", "y__3", forward, reverse); });
    test(rows, "proxy-hash-preserved", () -> { String value = "proxy$abc123$fn__10"; String normalized = replaceIds(value, Collections.singletonMap("10", "20"), true); require(normalized.equals("proxy$abc123$fn__20"), "proxy hash changed: " + normalized); });
    testFailure(rows, "unknown-id-rejected", () -> replaceIds("x__11", Collections.singletonMap("10", "20"), true));
    List<Insn> positive = Arrays.asList(new Insn("VAR", Opcodes.ALOAD, 1), new Insn("VAR", Opcodes.ALOAD, 2), new Insn("INSN", Opcodes.ACONST_NULL), new Insn("VAR", Opcodes.ASTORE, 2), new Insn("VAR", Opcodes.ALOAD, 3));
    Normalizer identity = new Normalizer(Collections.emptyMap(), Collections.emptyMap());
    ClassModel safeOwner = basicClass("test/Owner", "test");
    test(rows, "unique-safe-partition", () -> { List<Partition> ps = new ArrayList<Partition>(); partitions(positive, 0, positive.size(), new Type[] {Type.getType(Object.class), Type.getType(Object.class), Type.getType(Object.class)}, 0, identity, "test", safeOwner, false, new ArrayList<String>(), ps); require(ps.size() == 1 && ps.get(0).blocks.size() == 3, "partition not unique"); });
    test(rows, "side-effect-call-rejected", () -> require(safeBlock(Collections.singletonList(new Insn("METHOD", Opcodes.INVOKESTATIC, "x", "y", "()Ljava/lang/Object;", false)), 0, 1, "Ljava/lang/Object;", identity, "test", safeOwner, false) == null, "method call accepted"));
    test(rows, "inconsistent-clear-rejected", () -> require(safeBlock(Arrays.asList(new Insn("VAR", Opcodes.ALOAD, 1), new Insn("INSN", Opcodes.ACONST_NULL), new Insn("VAR", Opcodes.ASTORE, 2)), 0, 3, "Ljava/lang/Object;", identity, "test", safeOwner, false) == null, "different slot clear accepted"));
    test(rows, "string-ldc-never-normalized", () -> { Map<String, String> proof = Collections.singletonMap("10", "20"); IdMap map = new IdMap("test", set("10"), set("20"), proof); Normalizer normalizer = new Normalizer(Collections.singletonMap("test", map), Collections.emptyMap()); require(normalizer.value("literal__10", "test").contains(q("literal__10")), "semantic string was normalized"); });
    test(rows, "const-member-name-never-normalized", () -> { Map<String, String> proof = Collections.singletonMap("10", "20"); IdMap map = new IdMap("test", set("10"), set("20"), proof); Normalizer normalizer = new Normalizer(Collections.singletonMap("test", map), Collections.emptyMap()); require(normalizer.known("const__10", "test").equals("const__10"), "constant-slot member name was normalized"); });
    test(rows, "nan-payload-bits-preserved", () -> { Normalizer normalizer = new Normalizer(Collections.emptyMap(), Collections.emptyMap()); Float a = Float.intBitsToFloat(0x7fc00001), b = Float.intBitsToFloat(0x7fc00002); require(!normalizer.value(a, "test").equals(normalizer.value(b, "test")), "distinct NaN payloads collapsed"); });
    test(rows, "end-to-end-captured-permutation-accepted", () -> {
      ComparisonResult result = compare("synthetic-permutation", syntheticCaptureCohort("10", false), syntheticCaptureCohort("20", true), new ArrayList<String>(), new ArrayList<String>());
      require(result.classes == 2 && result.useSitesLeft == 1 && result.useSitesRight == 1, "unexpected synthetic comparison counts");
    });
    test(rows, "typed-class-reference-order-disambiguates-identical-children", () -> compare(
        "synthetic-class-graph",
        syntheticClassGraphCohort("1", "2", "3"),
        syntheticClassGraphCohort("10", "20", "30"),
        new ArrayList<String>(), new ArrayList<String>()));
    testFailure(rows, "end-to-end-id-ambiguity-rejected", () -> compare("synthetic-ambiguity", syntheticAmbiguousCohort("1", "2"), syntheticAmbiguousCohort("10", "20"), new ArrayList<String>(), new ArrayList<String>()));
    testFailure(rows, "end-to-end-id-conflict-rejected", () -> compare("synthetic-conflict", syntheticConflictingCohort("1", "1"), syntheticConflictingCohort("10", "20"), new ArrayList<String>(), new ArrayList<String>()));
    testFailure(rows, "end-to-end-const-member-swap-rejected", () -> compare("synthetic-const-swap", syntheticConstCohort("const__0"), syntheticConstCohort("const__1"), new ArrayList<String>(), new ArrayList<String>()));
    test(rows, "typed-state-slot-remap-accepted", () -> {
      ComparisonResult result = compare("candidate-a--candidate-b",
          syntheticStateSlotCohort(new long[] {10}, new long[] {10}, 77L, true),
          syntheticStateSlotCohort(new long[] {20}, new long[] {20}, 77L, true),
          new ArrayList<String>(), new ArrayList<String>());
      require(result.stateSlotNodes == 1 && result.stateSlotAccessesLeft == result.stateSlotAccessesRight,
          "typed state-slot witness/count is incomplete");
    });
    testFailure(rows, "typed-state-slot-cross-use-conflict-rejected", () -> compare("candidate-a--candidate-b",
        syntheticStateSlotCohort(new long[] {10, 11}, new long[] {10, 11}, 77L, true),
        syntheticStateSlotCohort(new long[] {20, 21}, new long[] {21, 20}, 77L, true),
        new ArrayList<String>(), new ArrayList<String>()));
    testFailure(rows, "typed-state-slot-unmatched-node-rejected", () -> compare("candidate-a--candidate-b",
        syntheticStateSlotCohort(new long[] {10, 11}, new long[] {10}, 77L, true),
        syntheticStateSlotCohort(new long[] {20}, new long[] {20}, 77L, true),
        new ArrayList<String>(), new ArrayList<String>()));
    testFailure(rows, "ordinary-long-remains-exact", () -> compare("candidate-a--candidate-b",
        syntheticStateSlotCohort(new long[] {10}, new long[] {10}, 77L, true),
        syntheticStateSlotCohort(new long[] {20}, new long[] {20}, 78L, true),
        new ArrayList<String>(), new ArrayList<String>()));
    testFailure(rows, "state-write-nonfinal-getstatic-value-rejected", () -> compare("candidate-a--candidate-b",
        syntheticStateSlotCohort(new long[] {10}, new long[] {10}, 77L, false),
        syntheticStateSlotCohort(new long[] {20}, new long[] {20}, 77L, false),
        new ArrayList<String>(), new ArrayList<String>()));
    test(rows, "typed-local-physical-slot-remap-accepted", () -> compare("candidate-a--candidate-b",
        syntheticObjectLocalCohort(2, 3, false, false),
        syntheticObjectLocalCohort(5, 7, false, false),
        new ArrayList<String>(), new ArrayList<String>()));
    testFailure(rows, "typed-local-parameter-immobility", () -> compare("candidate-a--candidate-b",
        syntheticObjectLocalCohort(2, 3, false, false),
        syntheticObjectLocalCohort(5, 7, true, false),
        new ArrayList<String>(), new ArrayList<String>()));
    testFailure(rows, "typed-local-this-immobility", () -> compare("candidate-a--candidate-b",
        syntheticBoundaryCohort(true, false), syntheticBoundaryCohort(true, true),
        new ArrayList<String>(), new ArrayList<String>()));
    testFailure(rows, "typed-local-same-arity-cross-use-swap-rejected", () -> compare("candidate-a--candidate-b",
        syntheticObjectLocalCohort(2, 3, false, false),
        syntheticObjectLocalCohort(5, 7, false, true),
        new ArrayList<String>(), new ArrayList<String>()));
    testFailure(rows, "typed-local-iinc-semantic-change-rejected", () -> compare("candidate-a--candidate-b",
        syntheticIincCohort(1, 1), syntheticIincCohort(4, 2),
        new ArrayList<String>(), new ArrayList<String>()));
    testFailure(rows, "typed-local-category-width-conflict-rejected", () -> compare("candidate-a--candidate-b",
        syntheticFrameCohort(Opcodes.LONG), syntheticFrameCohort(Opcodes.INTEGER, Opcodes.TOP),
        new ArrayList<String>(), new ArrayList<String>()));
    testFailure(rows, "typed-local-unmatched-node-rejected", () -> compare("candidate-a--candidate-b",
        syntheticLvtCohort("value", null, false, true),
        syntheticLvtCohort("value", null, false, false),
        new ArrayList<String>(), new ArrayList<String>()));
    testFailure(rows, "lvt-name-conflict-rejected", () -> compare("candidate-a--candidate-b",
        syntheticLvtCohort("value", null, false, false),
        syntheticLvtCohort("other", null, false, false),
        new ArrayList<String>(), new ArrayList<String>()));
    testFailure(rows, "lvt-signature-conflict-rejected", () -> compare("candidate-a--candidate-b",
        syntheticLvtCohort("value", "Ljava/util/List<Ljava/lang/String;>;", false, false),
        syntheticLvtCohort("value", "Ljava/util/List<Ljava/lang/Object;>;", false, false),
        new ArrayList<String>(), new ArrayList<String>()));
    testFailure(rows, "lvt-control-region-range-conflict-rejected", () -> compare("candidate-a--candidate-b",
        syntheticLvtCohort("value", null, false, false),
        syntheticLvtCohort("value", null, true, false),
        new ArrayList<String>(), new ArrayList<String>()));
    String iocOwner = "clojure/core/async$pipeline_STAR_$fn__10$state_machine__11__auto____12$fn__13";
    test(rows, "core-async-ioc-metadata-only-lvt-name-variation-accepted", () -> compare(
        "candidate-a--candidate-b",
        syntheticCoreAsyncIocLvtCohort("jobs", iocOwner, CORE_ASYNC_NAMESPACE,
            "invoke", "()Ljava/lang/Object;", 1, false, false, true),
        syntheticCoreAsyncIocLvtCohort("G__20", iocOwner, CORE_ASYNC_NAMESPACE,
            "invoke", "()Ljava/lang/Object;", 1, false, false, true),
        new ArrayList<String>(), new ArrayList<String>()));
    testFailure(rows, "core-async-ioc-lvt-wrong-source-identity-rejected", () -> compare(
        "candidate-a--candidate-b",
        syntheticCoreAsyncIocLvtCohort("jobs", iocOwner, CORE_ASYNC_NAMESPACE,
            "invoke", "()Ljava/lang/Object;", 1, false, false, false),
        syntheticCoreAsyncIocLvtCohort("from", iocOwner, CORE_ASYNC_NAMESPACE,
            "invoke", "()Ljava/lang/Object;", 1, false, false, false),
        new ArrayList<String>(), new ArrayList<String>()));
    testFailure(rows, "ordinary-core-async-invoke-lvt-name-conflict-rejected", () -> compare(
        "candidate-a--candidate-b",
        syntheticCoreAsyncIocLvtCohort("jobs", "clojure/core/async$ordinary__10",
            CORE_ASYNC_NAMESPACE, "invoke", "()Ljava/lang/Object;", 1, false, false, true),
        syntheticCoreAsyncIocLvtCohort("from", "clojure/core/async$ordinary__10",
            CORE_ASYNC_NAMESPACE, "invoke", "()Ljava/lang/Object;", 1, false, false, true),
        new ArrayList<String>(), new ArrayList<String>()));
    testFailure(rows, "ioc-shaped-lvt-outside-core-async-rejected", () -> compare(
        "candidate-a--candidate-b",
        syntheticCoreAsyncIocLvtCohort("jobs",
            "test/core$pipeline$state_machine__11__auto____12$fn__13", "test",
            "invoke", "()Ljava/lang/Object;", 1, false, false, true),
        syntheticCoreAsyncIocLvtCohort("from",
            "test/core$pipeline$state_machine__11__auto____12$fn__13", "test",
            "invoke", "()Ljava/lang/Object;", 1, false, false, true),
        new ArrayList<String>(), new ArrayList<String>()));
    testFailure(rows, "core-async-ioc-executable-lvt-name-conflict-rejected", () -> compare(
        "candidate-a--candidate-b",
        syntheticCoreAsyncIocLvtCohort("jobs", iocOwner, CORE_ASYNC_NAMESPACE,
            "invoke", "()Ljava/lang/Object;", 1, true, false, true),
        syntheticCoreAsyncIocLvtCohort("from", iocOwner, CORE_ASYNC_NAMESPACE,
            "invoke", "()Ljava/lang/Object;", 1, true, false, true),
        new ArrayList<String>(), new ArrayList<String>()));
    testFailure(rows, "core-async-ioc-frame-bound-lvt-name-conflict-rejected", () -> compare(
        "candidate-a--candidate-b",
        syntheticCoreAsyncIocLvtCohort("jobs", iocOwner, CORE_ASYNC_NAMESPACE,
            "invoke", "()Ljava/lang/Object;", 1, true, true, true),
        syntheticCoreAsyncIocLvtCohort("from", iocOwner, CORE_ASYNC_NAMESPACE,
            "invoke", "()Ljava/lang/Object;", 1, true, true, true),
        new ArrayList<String>(), new ArrayList<String>()));
    testFailure(rows, "core-async-ioc-method-name-mismatch-rejected", () -> compare(
        "candidate-a--candidate-b",
        syntheticCoreAsyncIocLvtCohort("jobs", iocOwner, CORE_ASYNC_NAMESPACE,
            "call", "()Ljava/lang/Object;", 1, false, false, true),
        syntheticCoreAsyncIocLvtCohort("from", iocOwner, CORE_ASYNC_NAMESPACE,
            "call", "()Ljava/lang/Object;", 1, false, false, true),
        new ArrayList<String>(), new ArrayList<String>()));
    testFailure(rows, "core-async-ioc-method-descriptor-mismatch-rejected", () -> compare(
        "candidate-a--candidate-b",
        syntheticCoreAsyncIocLvtCohort("jobs", iocOwner, CORE_ASYNC_NAMESPACE,
            "invoke", "(I)Ljava/lang/Object;", 2, false, false, true),
        syntheticCoreAsyncIocLvtCohort("from", iocOwner, CORE_ASYNC_NAMESPACE,
            "invoke", "(I)Ljava/lang/Object;", 2, false, false, true),
        new ArrayList<String>(), new ArrayList<String>()));
    testFailure(rows, "core-async-ioc-boundary-semantic-lvt-name-conflict-rejected", () -> compare(
        "candidate-a--candidate-b",
        syntheticCoreAsyncIocLvtCohort("this", iocOwner, CORE_ASYNC_NAMESPACE,
            "invoke", "()Ljava/lang/Object;", 0, false, false, true),
        syntheticCoreAsyncIocLvtCohort("receiver", iocOwner, CORE_ASYNC_NAMESPACE,
            "invoke", "()Ljava/lang/Object;", 0, false, false, true),
        new ArrayList<String>(), new ArrayList<String>()));
    test(rows, "core-async-alts-exact-generated-lvt-family-accepted", () -> {
      AltsLvtFixture fixture = syntheticAltsLvtFixture("clojure/core/async$alts_BANG_", true,
          "invokeStatic", "(Ljava/lang/Object;Lclojure/lang/ISeq;)Ljava/lang/Object;",
          "map__11", "p__10", 1, 3, false);
      require(coreAsyncAltsGeneratedLvtFamily(fixture.locals, fixture.method),
          "exact alts! generated-LVT family was not recognized");
      require(coreAsyncAltsGeneratedLvtSpelling(fixture.locals, fixture.method, fixture.mapSlot2) &&
          coreAsyncAltsGeneratedLvtSpelling(fixture.locals, fixture.method, fixture.parameter),
          "exact alts! target rows were not recognized");
      require(localVariableSkeleton(fixture.method, fixture.mapSlot2, fixture.locals).startsWith("map__0"),
          "map local numeric spelling was not isolated");
      require(compilerNodeIds(Collections.singleton(fixture.owner), fixture.locals).isEmpty(),
          "source-bound LVT-only IDs leaked into the compilation-unit ID bijection");
    });
    test(rows, "core-async-alts-wrong-source-rejected", () -> {
      AltsLvtFixture fixture = syntheticAltsLvtFixture("clojure/core/async$alts_BANG_", false,
          "invokeStatic", "(Ljava/lang/Object;Lclojure/lang/ISeq;)Ljava/lang/Object;",
          "map__11", "p__10", 1, 3, false);
      require(!coreAsyncAltsGeneratedLvtFamily(fixture.locals, fixture.method), "wrong source accepted");
    });
    test(rows, "core-async-alts-wrong-class-rejected", () -> {
      AltsLvtFixture fixture = syntheticAltsLvtFixture("clojure/core/async$alts_BANG_$fn__1", true,
          "invokeStatic", "(Ljava/lang/Object;Lclojure/lang/ISeq;)Ljava/lang/Object;",
          "map__11", "p__10", 1, 3, false);
      require(!coreAsyncAltsGeneratedLvtFamily(fixture.locals, fixture.method), "wrong class accepted");
    });
    test(rows, "core-async-alts-wrong-method-rejected", () -> {
      AltsLvtFixture fixture = syntheticAltsLvtFixture("clojure/core/async$alts_BANG_", true,
          "invoke", "(Ljava/lang/Object;Lclojure/lang/ISeq;)Ljava/lang/Object;",
          "map__11", "p__10", 1, 3, false);
      require(!coreAsyncAltsGeneratedLvtFamily(fixture.locals, fixture.method), "wrong method accepted");
    });
    test(rows, "core-async-alts-wrong-method-descriptor-rejected", () -> {
      AltsLvtFixture fixture = syntheticAltsLvtFixture("clojure/core/async$alts_BANG_", true,
          "invokeStatic", "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;",
          "map__11", "p__10", 1, 3, false);
      require(!coreAsyncAltsGeneratedLvtFamily(fixture.locals, fixture.method), "wrong descriptor accepted");
    });
    test(rows, "core-async-alts-altered-range-rejected", () -> {
      AltsLvtFixture fixture = syntheticAltsLvtFixture("clojure/core/async$alts_BANG_", true,
          "invokeStatic", "(Ljava/lang/Object;Lclojure/lang/ISeq;)Ljava/lang/Object;",
          "map__11", "p__10", 2, 3, false);
      require(!coreAsyncAltsGeneratedLvtFamily(fixture.locals, fixture.method), "altered range accepted");
    });
    test(rows, "core-async-alts-altered-slot-rejected", () -> {
      AltsLvtFixture fixture = syntheticAltsLvtFixture("clojure/core/async$alts_BANG_", true,
          "invokeStatic", "(Ljava/lang/Object;Lclojure/lang/ISeq;)Ljava/lang/Object;",
          "map__11", "p__10", 1, 5, false);
      require(!coreAsyncAltsGeneratedLvtFamily(fixture.locals, fixture.method), "altered slot accepted");
    });
    test(rows, "core-async-alts-altered-multiplicity-rejected", () -> {
      AltsLvtFixture fixture = syntheticAltsLvtFixture("clojure/core/async$alts_BANG_", true,
          "invokeStatic", "(Ljava/lang/Object;Lclojure/lang/ISeq;)Ljava/lang/Object;",
          "map__11", "p__10", 1, 3, true);
      require(!coreAsyncAltsGeneratedLvtFamily(fixture.locals, fixture.method), "altered multiplicity accepted");
    });
    test(rows, "core-async-alts-wrong-name-family-rejected", () -> {
      AltsLvtFixture fixture = syntheticAltsLvtFixture("clojure/core/async$alts_BANG_", true,
          "invokeStatic", "(Ljava/lang/Object;Lclojure/lang/ISeq;)Ljava/lang/Object;",
          "mapping__11", "p__10", 1, 3, false);
      require(!coreAsyncAltsGeneratedLvtFamily(fixture.locals, fixture.method), "wrong name family accepted");
    });
    test(rows, "core-async-alts-nonadjacent-ids-rejected", () -> {
      AltsLvtFixture fixture = syntheticAltsLvtFixture("clojure/core/async$alts_BANG_", true,
          "invokeStatic", "(Ljava/lang/Object;Lclojure/lang/ISeq;)Ljava/lang/Object;",
          "map__11", "p__8", 1, 3, false);
      require(!coreAsyncAltsGeneratedLvtFamily(fixture.locals, fixture.method), "nonadjacent IDs accepted");
    });
    test(rows, "core-async-auto-local-terminal-uniquifier-accepted", () -> {
      AutoLocalLvtFixture fixture = syntheticAutoLocalLvtFixture(
          "clojure/core/async$bounded_count", true, "invokeStatic",
          "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;",
          "and__5579__auto__7674", "Ljava/lang/Object;", 5, 12, 18,
          4, "A", false, Collections.singleton(5), 4, 7, 1, false, 1);
      require(coreAsyncAutoLocalWitnessHash(fixture.locals, fixture.method, fixture.variable) != null,
          "exact auto-local witness row was not recognized");
      require(coreAsyncAutoLocalTerminalSpelling(fixture.locals, fixture.method, fixture.variable),
          "closed auto-local spelling quotient was not selected");
      require(localVariableSkeleton(fixture.method, fixture.variable, fixture.locals)
          .startsWith("and__5579__auto__\u0000"), "full auto-local base was not preserved verbatim");
      require(compilerNodeIds(Collections.singleton(fixture.owner), fixture.locals).isEmpty(),
          "terminal auto-local suffix leaked into the compilation-unit ID bijection");
    });
    test(rows, "core-async-auto-local-altered-base-rejected", () -> requireAutoLocalRejected(
        syntheticAutoLocalLvtFixture("clojure/core/async$bounded_count", true, "invokeStatic",
            "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;",
            "other__5579__auto__7674", "Ljava/lang/Object;", 5, 12, 18,
            4, "A", false, Collections.singleton(5), 4, 7, 1, false, 1)));
    test(rows, "core-async-auto-local-altered-earlier-id-rejected", () -> requireAutoLocalRejected(
        syntheticAutoLocalLvtFixture("clojure/core/async$bounded_count", true, "invokeStatic",
            "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;",
            "and__5580__auto__7674", "Ljava/lang/Object;", 5, 12, 18,
            4, "A", false, Collections.singleton(5), 4, 7, 1, false, 1)));
    test(rows, "core-async-auto-local-anchored-suffix-rejected", () -> requireAutoLocalRejected(
        syntheticAutoLocalLvtFixture("clojure/core/async$bounded_count", true, "invokeStatic",
            "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;",
            "and__5579__auto__7674", "Ljava/lang/Object;", 5, 12, 18,
            4, "A", false, Collections.singleton(5), 4, 7, 1, true, 1)));
    test(rows, "core-async-auto-local-duplicate-suffix-rejected", () -> requireAutoLocalRejected(
        syntheticAutoLocalLvtFixture("clojure/core/async$bounded_count", true, "invokeStatic",
            "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;",
            "and__5579__auto__7674", "Ljava/lang/Object;", 5, 12, 18,
            4, "A", false, Collections.singleton(5), 4, 7, 1, false, 2)));
    test(rows, "core-async-auto-local-wrong-source-rejected", () -> requireAutoLocalRejected(
        syntheticAutoLocalLvtFixture("clojure/core/async$bounded_count", false, "invokeStatic",
            "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;",
            "and__5579__auto__7674", "Ljava/lang/Object;", 5, 12, 18,
            4, "A", false, Collections.singleton(5), 4, 7, 1, false, 1)));
    test(rows, "core-async-auto-local-wrong-class-rejected", () -> requireAutoLocalRejected(
        syntheticAutoLocalLvtFixture("clojure/core/async$bounded_count$fn__10", true, "invokeStatic",
            "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;",
            "and__5579__auto__7674", "Ljava/lang/Object;", 5, 12, 18,
            4, "A", false, Collections.singleton(5), 4, 7, 1, false, 1)));
    test(rows, "core-async-auto-local-wrong-method-rejected", () -> requireAutoLocalRejected(
        syntheticAutoLocalLvtFixture("clojure/core/async$bounded_count", true, "invoke",
            "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;",
            "and__5579__auto__7674", "Ljava/lang/Object;", 5, 12, 18,
            4, "A", false, Collections.singleton(5), 4, 7, 1, false, 1)));
    test(rows, "core-async-auto-local-wrong-method-descriptor-rejected", () -> requireAutoLocalRejected(
        syntheticAutoLocalLvtFixture("clojure/core/async$bounded_count", true, "invokeStatic",
            "(Ljava/lang/Object;)Ljava/lang/Object;",
            "and__5579__auto__7674", "Ljava/lang/Object;", 5, 12, 18,
            4, "A", false, Collections.singleton(5), 4, 7, 1, false, 1)));
    test(rows, "core-async-auto-local-altered-range-rejected", () -> requireAutoLocalRejected(
        syntheticAutoLocalLvtFixture("clojure/core/async$bounded_count", true, "invokeStatic",
            "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;",
            "and__5579__auto__7674", "Ljava/lang/Object;", 5, 11, 18,
            4, "A", false, Collections.singleton(5), 4, 7, 1, false, 1)));
    test(rows, "core-async-auto-local-altered-slot-rejected", () -> requireAutoLocalRejected(
        syntheticAutoLocalLvtFixture("clojure/core/async$bounded_count", true, "invokeStatic",
            "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;",
            "and__5579__auto__7674", "Ljava/lang/Object;", 6, 12, 18,
            4, "A", false, Collections.singleton(6), 4, 7, 1, false, 1)));
    test(rows, "core-async-auto-local-altered-descriptor-rejected", () -> requireAutoLocalRejected(
        syntheticAutoLocalLvtFixture("clojure/core/async$bounded_count", true, "invokeStatic",
            "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;",
            "and__5579__auto__7674", "J", 5, 12, 18,
            4, "J", false, Collections.singleton(5), 4, 7, 1, false, 1)));
    test(rows, "core-async-auto-local-altered-def-use-graph-rejected", () -> requireAutoLocalRejected(
        syntheticAutoLocalLvtFixture("clojure/core/async$bounded_count", true, "invokeStatic",
            "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;",
            "and__5579__auto__7674", "Ljava/lang/Object;", 5, 12, 18,
            4, "A", false, new TreeSet<Integer>(Arrays.asList(5, 6)), 4, 7, 1, false, 1)));
    test(rows, "core-async-auto-local-altered-frame-graph-rejected", () -> requireAutoLocalRejected(
        syntheticAutoLocalLvtFixture("clojure/core/async$bounded_count", true, "invokeStatic",
            "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;",
            "and__5579__auto__7674", "Ljava/lang/Object;", 5, 12, 18,
            4, "A", false, Collections.singleton(5), 4, 6, 1, false, 1)));
    test(rows, "core-async-auto-local-duplicate-node-alias-rejected", () -> requireAutoLocalRejected(
        syntheticAutoLocalLvtFixture("clojure/core/async$bounded_count", true, "invokeStatic",
            "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;",
            "and__5579__auto__7674", "Ljava/lang/Object;", 5, 12, 18,
            4, "A", false, Collections.singleton(5), 4, 7, 2, false, 1)));
    test(rows, "generic-tmp11-never-auto-local-quotiented", () -> requireAutoLocalRejected(
        syntheticAutoLocalLvtFixture("clojure/core/async$bounded_count", true, "invokeStatic",
            "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;",
            "tmp11", "Ljava/lang/Object;", 5, 12, 18,
            4, "A", false, Collections.singleton(5), 4, 7, 1, false, 1)));
    test(rows, "core-async-sharp-local-seven-row-seal-accepted", () -> {
      SharpLocalLvtFixture fixture = syntheticSharpLocalLvtFixture(
          "clojure/core/async$defblockingop$fn__6867", true, "invoke",
          "(Ljava/lang/Object;)Ljava/lang/Object;", Opcodes.ACC_PUBLIC,
          "p1__6866_SHARP_", "Ljava/lang/Object;", 1, 0, 3,
          1, "A", true, Collections.singleton(1), 2, 0, 1, 0, 1);
      require(coreAsyncSharpLocalWitnessHash(fixture.locals, fixture.method, fixture.variable) != null,
          "exact sharp-local witness row was not recognized");
      require(coreAsyncSharpLocalSpelling(fixture.locals, fixture.method, fixture.variable),
          "closed sharp-local spelling quotient was not selected");
      require(localVariableSkeleton(fixture.method, fixture.variable, fixture.locals)
          .startsWith("p1__SHARP_\u0000"), "sharp-local spelling prefix was not preserved");
      Set<String> compilerIds = compilerNodeIds(Collections.singleton(fixture.owner), fixture.locals);
      require(!compilerIds.contains("6866") && compilerIds.contains("6867"),
          "sharp-local-only ID was not isolated from the independently owned class ID");
    });
    test(rows, "core-async-sharp-local-count-alteration-rejected", () -> {
      require(!coreAsyncSharpLocalShapeCountsClosed(6,
          CORE_ASYNC_SHARP_LOCAL_WITNESS_SHAPE_COUNTS), "short sharp-local inventory accepted");
      require(!coreAsyncSharpLocalShapeCountsClosed(8,
          CORE_ASYNC_SHARP_LOCAL_WITNESS_SHAPE_COUNTS), "long sharp-local inventory accepted");
    });
    test(rows, "core-async-sharp-local-extra-row-rejected", () -> {
      Map<String, Integer> extra = new TreeMap<String, Integer>(CORE_ASYNC_SHARP_LOCAL_WITNESS_SHAPE_COUNTS);
      extra.put("0000000000000000000000000000000000000000000000000000000000000000", 1);
      require(!coreAsyncSharpLocalShapeCountsClosed(8, extra), "extra sharp-local row accepted");
    });
    test(rows, "core-async-sharp-local-altered-slot-rejected", () -> requireSharpLocalRejected(
        syntheticSharpLocalLvtFixture("clojure/core/async$defblockingop$fn__6867", true, "invoke",
            "(Ljava/lang/Object;)Ljava/lang/Object;", Opcodes.ACC_PUBLIC,
            "p1__6866_SHARP_", "Ljava/lang/Object;", 2, 0, 3,
            1, "A", true, Collections.singleton(2), 2, 0, 1, 0, 1)));
    test(rows, "core-async-sharp-local-altered-descriptor-rejected", () -> requireSharpLocalRejected(
        syntheticSharpLocalLvtFixture("clojure/core/async$defblockingop$fn__6867", true, "invoke",
            "(Ljava/lang/Object;)Ljava/lang/Object;", Opcodes.ACC_PUBLIC,
            "p1__6866_SHARP_", "J", 1, 0, 3,
            1, "J", true, Collections.singleton(1), 2, 0, 1, 0, 1)));
    test(rows, "core-async-sharp-local-altered-range-rejected", () -> requireSharpLocalRejected(
        syntheticSharpLocalLvtFixture("clojure/core/async$defblockingop$fn__6867", true, "invoke",
            "(Ljava/lang/Object;)Ljava/lang/Object;", Opcodes.ACC_PUBLIC,
            "p1__6866_SHARP_", "Ljava/lang/Object;", 1, 0, 4,
            1, "A", true, Collections.singleton(1), 2, 0, 1, 0, 1)));
    test(rows, "core-async-sharp-local-parameter-role-rejected", () -> requireSharpLocalRejected(
        syntheticSharpLocalLvtFixture("clojure/core/async$defblockingop$fn__6867", true, "invoke",
            "(Ljava/lang/Object;)Ljava/lang/Object;", Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC,
            "p1__6866_SHARP_", "Ljava/lang/Object;", 1, 0, 3,
            1, "A", true, Collections.singleton(1), 2, 0, 1, 0, 1)));
    test(rows, "core-async-sharp-local-earlier-anchor-rejected", () -> requireSharpLocalRejected(
        syntheticSharpLocalLvtFixture("clojure/core/async$defblockingop$fn__6867", true, "invoke",
            "(Ljava/lang/Object;)Ljava/lang/Object;", Opcodes.ACC_PUBLIC,
            "p1__6866_SHARP_", "Ljava/lang/Object;", 1, 0, 3,
            1, "A", true, Collections.singleton(1), 2, 0, 1, -1, 1)));
    test(rows, "core-async-sharp-local-later-anchor-rejected", () -> requireSharpLocalRejected(
        syntheticSharpLocalLvtFixture("clojure/core/async$defblockingop$fn__6867", true, "invoke",
            "(Ljava/lang/Object;)Ljava/lang/Object;", Opcodes.ACC_PUBLIC,
            "p1__6866_SHARP_", "Ljava/lang/Object;", 1, 0, 3,
            1, "A", true, Collections.singleton(1), 2, 0, 1, 1, 1)));
    test(rows, "core-async-sharp-local-id-occurrence-count-rejected", () -> requireSharpLocalRejected(
        syntheticSharpLocalLvtFixture("clojure/core/async$defblockingop$fn__6867", true, "invoke",
            "(Ljava/lang/Object;)Ljava/lang/Object;", Opcodes.ACC_PUBLIC,
            "p1__6866_SHARP_", "Ljava/lang/Object;", 1, 0, 3,
            1, "A", true, Collections.singleton(1), 2, 0, 1, 0, 2)));
    test(rows, "core-async-sharp-local-duplicate-alias-rejected", () -> requireSharpLocalRejected(
        syntheticSharpLocalLvtFixture("clojure/core/async$defblockingop$fn__6867", true, "invoke",
            "(Ljava/lang/Object;)Ljava/lang/Object;", Opcodes.ACC_PUBLIC,
            "p1__6866_SHARP_", "Ljava/lang/Object;", 1, 0, 3,
            1, "A", true, Collections.singleton(1), 2, 0, 2, 0, 1)));
    test(rows, "core-async-sharp-local-spelling-prefix-rejected", () -> requireSharpLocalRejected(
        syntheticSharpLocalLvtFixture("clojure/core/async$defblockingop$fn__6867", true, "invoke",
            "(Ljava/lang/Object;)Ljava/lang/Object;", Opcodes.ACC_PUBLIC,
            "p2__6866_SHARP_", "Ljava/lang/Object;", 1, 0, 3,
            1, "A", true, Collections.singleton(1), 2, 0, 1, 0, 1)));
    test(rows, "core-async-sharp-local-source-hash-rejected", () -> requireSharpLocalRejected(
        syntheticSharpLocalLvtFixture("clojure/core/async$defblockingop$fn__6867", false, "invoke",
            "(Ljava/lang/Object;)Ljava/lang/Object;", Opcodes.ACC_PUBLIC,
            "p1__6866_SHARP_", "Ljava/lang/Object;", 1, 0, 3,
            1, "A", true, Collections.singleton(1), 2, 0, 1, 0, 1)));
    test(rows, "core-async-sharp-local-wrong-class-rejected", () -> requireSharpLocalRejected(
        syntheticSharpLocalLvtFixture("clojure/core/async$other$fn__6867", true, "invoke",
            "(Ljava/lang/Object;)Ljava/lang/Object;", Opcodes.ACC_PUBLIC,
            "p1__6866_SHARP_", "Ljava/lang/Object;", 1, 0, 3,
            1, "A", true, Collections.singleton(1), 2, 0, 1, 0, 1)));
    test(rows, "core-async-sharp-local-wrong-method-rejected", () -> requireSharpLocalRejected(
        syntheticSharpLocalLvtFixture("clojure/core/async$defblockingop$fn__6867", true, "applyTo",
            "(Ljava/lang/Object;)Ljava/lang/Object;", Opcodes.ACC_PUBLIC,
            "p1__6866_SHARP_", "Ljava/lang/Object;", 1, 0, 3,
            1, "A", true, Collections.singleton(1), 2, 0, 1, 0, 1)));
    test(rows, "core-async-do-alt-vec-local-one-row-seal-accepted", () -> {
      DoAltVecLocalLvtFixture fixture = syntheticDoAltVecLocalLvtFixture("");
      String shape = coreAsyncDoAltVecLocalWitnessShape(fixture.locals, fixture.method, fixture.variable);
      require(CORE_ASYNC_DO_ALT_VEC_LOCAL_WITNESS_SHAPE_SHA256.equals(sha256(shape)),
          "exact do-alt vec-local witness shape changed: " + sha256(shape));
      require(coreAsyncDoAltVecLocalWitnessHash(fixture.locals, fixture.method, fixture.variable) != null,
          "exact do-alt vec-local witness row was not recognized");
      require(coreAsyncDoAltVecLocalInventoryClosed(fixture.locals,
          Collections.singleton(fixture.owner)), "one-row do-alt vec-local inventory did not close");
      require(coreAsyncDoAltVecLocalSpelling(fixture.locals, fixture.method, fixture.variable),
          "closed do-alt vec-local spelling quotient was not selected");
      require(localVariableSkeleton(fixture.method, fixture.variable, fixture.locals)
          .startsWith("vec__\u0000"), "do-alt vec-local stable prefix was not preserved");
      require(!compilerNodeIds(Collections.singleton(fixture.owner), fixture.locals).contains("6929"),
          "do-alt vec-local-only ID leaked into the compilation-unit bijection");
    });
    test(rows, "core-async-do-alt-vec-local-count-alteration-rejected", () -> {
      Map<String, Integer> exact = Collections.singletonMap(
          CORE_ASYNC_DO_ALT_VEC_LOCAL_WITNESS_SHAPE_SHA256, 1);
      require(!coreAsyncDoAltVecLocalShapeCountsClosed(0, exact), "empty do-alt vec-local inventory accepted");
      require(!coreAsyncDoAltVecLocalShapeCountsClosed(2, exact), "long do-alt vec-local inventory accepted");
    });
    test(rows, "core-async-do-alt-vec-local-extra-row-rejected", () -> {
      DoAltVecLocalLvtFixture fixture = syntheticDoAltVecLocalLvtFixture("extra-row");
      require(!coreAsyncDoAltVecLocalInventoryClosed(fixture.locals,
          Collections.singleton(fixture.owner)), "extra do-alt vec-local row accepted");
    });
    for (String mutation : Arrays.asList(
        "source", "owner", "class-header", "method", "method-descriptor", "method-access",
        "spelling", "local-descriptor", "local-count", "slot", "range", "boundary",
        "category", "physical", "def-use", "frame", "alias", "id-anchor", "id-count")) {
      final String selectedMutation = mutation;
      test(rows, "core-async-do-alt-vec-local-" + mutation + "-rejected", () ->
          requireDoAltVecLocalRejected(syntheticDoAltVecLocalLvtFixture(selectedMutation)));
    }
    testFailure(rows, "generated-local-unknown-id-rejected", () -> compare("synthetic-unknown-local-id",
        syntheticUnknownLocalIdCohort("10", "11"), syntheticUnknownLocalIdCohort("20", "21"),
        new ArrayList<String>(), new ArrayList<String>()));
    testFailure(rows, "frame-top-retained", () -> compare("candidate-a--candidate-b",
        syntheticFrameCohort(Opcodes.TOP), syntheticFrameCohort(),
        new ArrayList<String>(), new ArrayList<String>()));
    testFailure(rows, "frame-null-retained", () -> compare("candidate-a--candidate-b",
        syntheticFrameCohort(Opcodes.NULL), syntheticFrameCohort(),
        new ArrayList<String>(), new ArrayList<String>()));
    testFailure(rows, "frame-uninitialized-this-retained", () -> compare("candidate-a--candidate-b",
        syntheticFrameCohort(Opcodes.UNINITIALIZED_THIS), syntheticFrameCohort(),
        new ArrayList<String>(), new ArrayList<String>()));
    testFailure(rows, "external-captured-use-rejected", () -> {
      Map<String, ClassModel> cohort = syntheticCaptureCohort("10", false);
      ClassModel outside = basicClass("external/Caller", "external");
      MethodModel method = basicMethod("make", "()V", Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC);
      method.codeEvents.add("CODE");
      method.insns.add(new Insn("TYPE", Opcodes.NEW, "test/Fn__10"));
      method.insns.add(new Insn("INSN", Opcodes.POP));
      method.insns.add(new Insn("INSN", Opcodes.RETURN));
      outside.methods.add(method);
      List<ClassModel> all = new ArrayList<ClassModel>(cohort.values()); all.add(outside);
      rejectExternalModelUses(all, cohort.keySet(), captureSpecs(cohort));
    });
    write(output.resolve("self-test.tsv"), "test\tstatus\tdetail", rows);
    long failures = rows.stream().filter(row -> row.contains("\tFAIL\t")).count();
    return failures == 0 ? 0 : 1;
  }

  private interface CheckedRunnable { void run() throws Exception; }
  private static void test(List<String> rows, String name, CheckedRunnable body) { try { body.run(); rows.add(name + "\tPASS\taccepted"); } catch (Throwable t) { rows.add(name + "\tFAIL\t" + clean(t.getMessage())); } }
  private static void testFailure(List<String> rows, String name, CheckedRunnable body) { try { body.run(); rows.add(name + "\tFAIL\texpected rejection"); } catch (Failure expected) { rows.add(name + "\tPASS\trejected:" + clean(expected.getMessage())); } catch (Throwable t) { rows.add(name + "\tFAIL\twrong exception:" + clean(t.toString())); } }
  private static Set<String> set(String... values) { return new HashSet<String>(Arrays.asList(values)); }
  private static String clean(String value) { return value == null ? "-" : value.replace('\t', ' ').replace('\n', ' ').replace('\r', ' '); }

  private static ClassModel basicClass(String name, String namespace) {
    ClassModel model = new ClassModel();
    model.name = name; model.namespace = namespace; model.version = Opcodes.V1_8;
    model.access = Opcodes.ACC_PUBLIC | Opcodes.ACC_FINAL; model.superName = "java/lang/Object";
    return model;
  }

  private static MethodModel basicMethod(String name, String descriptor, int access) {
    MethodModel method = new MethodModel(); method.name = name; method.descriptor = descriptor; method.access = access; return method;
  }

  private static final class AltsLvtFixture {
    final ClassModel owner;
    final MethodModel method;
    final LocalUniverse locals;
    final LocalVariableModel mapSlot2;
    final LocalVariableModel mapSlot3;
    final LocalVariableModel parameter;
    AltsLvtFixture(ClassModel owner, MethodModel method, LocalUniverse locals,
                   LocalVariableModel mapSlot2, LocalVariableModel mapSlot3,
                   LocalVariableModel parameter) {
      this.owner = owner; this.method = method; this.locals = locals;
      this.mapSlot2 = mapSlot2; this.mapSlot3 = mapSlot3; this.parameter = parameter;
    }
  }

  private static final class AutoLocalLvtFixture {
    final ClassModel owner;
    final MethodModel method;
    final LocalUniverse locals;
    final LocalVariableModel variable;
    AutoLocalLvtFixture(ClassModel owner, MethodModel method, LocalUniverse locals,
                        LocalVariableModel variable) {
      this.owner = owner; this.method = method; this.locals = locals; this.variable = variable;
    }
  }

  private static final class DoAltVecLocalLvtFixture {
    final ClassModel owner;
    final MethodModel method;
    final LocalUniverse locals;
    final LocalVariableModel variable;
    DoAltVecLocalLvtFixture(ClassModel owner, MethodModel method, LocalUniverse locals,
                            LocalVariableModel variable) {
      this.owner = owner; this.method = method; this.locals = locals; this.variable = variable;
    }
  }

  private static void requireDoAltVecLocalRejected(DoAltVecLocalLvtFixture fixture) {
    require(coreAsyncDoAltVecLocalWitnessHash(fixture.locals, fixture.method, fixture.variable) == null,
        "non-witness do-alt vec-local row was accepted: " + fixture.variable.name);
  }

  private static DoAltVecLocalLvtFixture syntheticDoAltVecLocalLvtFixture(String mutation) {
    String ownerName = mutation.equals("owner") ? "clojure/core/async$do_alt$other" :
        "clojure/core/async$do_alt";
    ClassModel owner = basicClass(ownerName, CORE_ASYNC_NAMESPACE);
    owner.access |= Opcodes.ACC_SUPER;
    owner.superName = mutation.equals("class-header") ? "java/lang/Object" : "clojure/lang/AFunction";
    stampExactCoreAsyncSource(owner, !mutation.equals("source"));
    String methodName = mutation.equals("method") ? "invoke" : "invokeStatic";
    String methodDescriptor = mutation.equals("method-descriptor") ?
        "(Ljava/lang/Object;)Ljava/lang/Object;" :
        "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;";
    int methodAccess = mutation.equals("method-access") ? Opcodes.ACC_PUBLIC :
        Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC;
    MethodModel method = basicMethod(methodName, methodDescriptor, methodAccess);
    method.codeEvents.add("CODE");
    LabelRef control0 = new LabelRef(0), control1 = new LabelRef(1), control2 = new LabelRef(2);
    LabelRef start = new LabelRef(23), end = new LabelRef(mutation.equals("range") ? 111 : 112);
    for (int i = 0; i < 116; i++) method.insns.add(new Insn("INSN", Opcodes.NOP));
    method.insns.set(0, new Insn("LABEL", -1, control0));
    method.insns.set(1, new Insn("JUMP", Opcodes.GOTO, control0));
    method.insns.set(2, new Insn("LABEL", -1, control1));
    method.insns.set(3, new Insn("JUMP", Opcodes.GOTO, control1));
    method.insns.set(4, new Insn("LABEL", -1, control2));
    method.insns.set(5, new Insn("JUMP", Opcodes.GOTO, control2));
    method.insns.set(100, new Insn("LABEL", -1, start));
    method.insns.set(115, new Insn("LABEL", -1, end));
    int slot = mutation.equals("slot") ? 7 : 6;
    method.insns.set(102, new Insn("VAR", Opcodes.ASTORE, slot));
    method.insns.set(104, new Insn("VAR", mutation.equals("def-use") ? Opcodes.ASTORE : Opcodes.ALOAD, slot));
    method.insns.set(112, new Insn("VAR", Opcodes.ALOAD, slot));
    method.insns.set(114, new Insn("VAR", Opcodes.ASTORE, slot));
    if (mutation.equals("frame"))
      method.insns.set(108, new Insn("FRAME", -1, Opcodes.F_NEW, new Object[0], new Object[0]));
    String localName = mutation.equals("spelling") ? "seq__6929" : "vec__6929";
    String localDescriptor = mutation.equals("local-descriptor") ? "J" : "Ljava/lang/Object;";
    LocalVariableModel variable = new LocalVariableModel(localName, localDescriptor, null,
        start, end, slot);
    method.localVariables.add(variable);
    for (int i = 0; i < 10; i++)
      method.localVariables.add(new LocalVariableModel("ordinary" + i, "Ljava/lang/Object;", null,
          start, end, i));
    if (mutation.equals("local-count")) method.localVariables.remove(method.localVariables.size() - 1);
    LocalVariableModel alias = null;
    if (mutation.equals("alias")) {
      alias = new LocalVariableModel("alias", localDescriptor, null, start, end, slot);
      method.localVariables.add(alias);
    }
    LocalVariableModel extra = null;
    if (mutation.equals("extra-row")) {
      extra = new LocalVariableModel("vec__6930", "Ljava/lang/Object;", null, start, end, 7);
      method.localVariables.add(extra);
    }
    owner.methods.add(method);

    LocalUniverse locals = new LocalUniverse();
    String category = mutation.equals("category") ? "I" : "A";
    LocalNodeKey key = new LocalNodeKey(owner.name, method.name, method.descriptor, 6, category);
    Set<Integer> physical = mutation.equals("physical") ?
        new TreeSet<Integer>(Arrays.asList(slot, slot + 1)) : Collections.singleton(slot);
    LocalNodeInfo info = new LocalNodeInfo(key, owner, method, mutation.equals("boundary"), physical);
    info.instructionOccurrences = 4;
    info.frameOccurrences = mutation.equals("frame") ? 1 : 0;
    info.lvtOccurrences = mutation.equals("alias") ? 2 : 1;
    locals.info.put(key, info);
    locals.variableNodes.put(variable, key);
    if (alias != null) locals.variableNodes.put(alias, key);
    locals.methodNodes.computeIfAbsent(method, ignored -> new TreeSet<LocalNodeKey>()).add(key);
    if (!info.boundary) locals.generated.add(key);
    for (int at : new int[] {102, 104, 112, 114}) locals.instructionNodes.put(method.insns.get(at), key);
    if (mutation.equals("frame")) {
      Map<Integer, LocalNodeKey> cells = new TreeMap<Integer, LocalNodeKey>(); cells.put(slot, key);
      locals.frameNodes.put(method.insns.get(108), cells);
    }
    locals.lvtCompilerIdOccurrences.put("6929", mutation.equals("id-count") ? 2 : 1);
    if (mutation.equals("id-anchor")) locals.nonLvtCompilerIds.add("6929");
    if (extra != null) {
      LocalNodeKey extraKey = new LocalNodeKey(owner.name, method.name, method.descriptor, 7, "A");
      LocalNodeInfo extraInfo = new LocalNodeInfo(extraKey, owner, method, false, Collections.singleton(7));
      extraInfo.lvtOccurrences = 1;
      locals.info.put(extraKey, extraInfo); locals.variableNodes.put(extra, extraKey);
      locals.methodNodes.get(method).add(extraKey); locals.generated.add(extraKey);
      locals.lvtCompilerIdOccurrences.put("6930", 1);
    }
    locals.coreAsyncDoAltVecLocalInventoryClosed = mutation.isEmpty();
    return new DoAltVecLocalLvtFixture(owner, method, locals, variable);
  }

  private static void requireAutoLocalRejected(AutoLocalLvtFixture fixture) {
    require(coreAsyncAutoLocalWitnessHash(fixture.locals, fixture.method, fixture.variable) == null,
        "non-witness auto-local row was accepted: " + fixture.variable.name);
  }

  private static AutoLocalLvtFixture syntheticAutoLocalLvtFixture(
      String ownerName, boolean exactSource, String methodName, String methodDescriptor,
      String localName, String localDescriptor, int slot, int startLabel, int endLabel,
      int ordinal, String category, boolean boundary, Set<Integer> physicalSlots,
      int instructionOccurrences, int frameOccurrences, int lvtAliases,
      boolean suffixAnchored, int suffixOccurrences) {
    ClassModel owner = basicClass(ownerName, CORE_ASYNC_NAMESPACE);
    stampExactCoreAsyncSource(owner, exactSource);
    MethodModel method = basicMethod(methodName, methodDescriptor,
        Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC);
    LabelRef[] labels = new LabelRef[25];
    for (int i = 0; i < labels.length; i++) {
      labels[i] = new LabelRef(i);
      method.insns.add(new Insn("LABEL", -1, labels[i]));
    }
    for (int target : new int[] {4, 8, 12, 13, 14, 15, 16, 18})
      method.insns.add(new Insn("JUMP", Opcodes.GOTO, labels[target]));
    LocalVariableModel variable = new LocalVariableModel(localName, localDescriptor, null,
        labels[startLabel], labels[endLabel], slot);
    method.localVariables.add(variable);
    owner.methods.add(method);

    LocalUniverse locals = new LocalUniverse();
    LocalNodeKey key = new LocalNodeKey(owner.name, method.name, method.descriptor, ordinal, category);
    LocalNodeInfo info = new LocalNodeInfo(key, owner, method, boundary,
        Collections.unmodifiableSet(new TreeSet<Integer>(physicalSlots)));
    info.instructionOccurrences = instructionOccurrences;
    info.frameOccurrences = frameOccurrences;
    info.lvtOccurrences = lvtAliases;
    locals.info.put(key, info);
    locals.variableNodes.put(variable, key);
    locals.methodNodes.computeIfAbsent(method, ignored -> new TreeSet<LocalNodeKey>()).add(key);
    if (!boundary) locals.generated.add(key);
    Matcher matcher = AUTO_LOCAL_TERMINAL_UNIQUIFIER.matcher(localName);
    if (matcher.matches()) {
      locals.autoLocalSuffixOccurrences.put(matcher.group(2), suffixOccurrences);
      if (suffixAnchored) locals.nonLvtCompilerIds.add(matcher.group(2));
    }
    /* The direct fixture exercises a single sealed row.  Production sets this
     * flag only after the complete 76-row multiset equals the frozen witness. */
    locals.coreAsyncAutoLocalInventoryClosed = true;
    return new AutoLocalLvtFixture(owner, method, locals, variable);
  }

  private static final class SharpLocalLvtFixture {
    final ClassModel owner;
    final MethodModel method;
    final LocalUniverse locals;
    final LocalVariableModel variable;
    SharpLocalLvtFixture(ClassModel owner, MethodModel method, LocalUniverse locals,
                         LocalVariableModel variable) {
      this.owner = owner; this.method = method; this.locals = locals; this.variable = variable;
    }
  }

  private static void requireSharpLocalRejected(SharpLocalLvtFixture fixture) {
    require(coreAsyncSharpLocalWitnessHash(fixture.locals, fixture.method, fixture.variable) == null,
        "non-witness sharp-local row was accepted: " + fixture.variable.name);
  }

  private static SharpLocalLvtFixture syntheticSharpLocalLvtFixture(
      String ownerName, boolean exactSource, String methodName, String methodDescriptor, int methodAccess,
      String localName, String localDescriptor, int slot, int startLabel, int endLabel,
      int ordinal, String category, boolean boundary, Set<Integer> physicalSlots,
      int instructionOccurrences, int frameOccurrences, int lvtAliases,
      int anchorPosition, int idOccurrences) {
    ClassModel owner = basicClass(ownerName, CORE_ASYNC_NAMESPACE);
    owner.access |= Opcodes.ACC_SUPER;
    owner.superName = "clojure/lang/AFunction";
    stampExactCoreAsyncSource(owner, exactSource);
    MethodModel method = basicMethod(methodName, methodDescriptor, methodAccess);
    LabelRef[] labels = new LabelRef[10];
    for (int i = 0; i < labels.length; i++) {
      labels[i] = new LabelRef(i);
      method.insns.add(new Insn("LABEL", -1, labels[i]));
    }
    LocalVariableModel variable = new LocalVariableModel(localName, localDescriptor, null,
        labels[startLabel], labels[endLabel], slot);
    method.localVariables.add(variable);
    owner.methods.add(method);
    LocalUniverse locals = new LocalUniverse();
    Matcher matcher = SHARP_LOCAL_UNIQUIFIER.matcher(localName);
    if (matcher.matches() && anchorPosition < 0) locals.nonLvtCompilerIds.add(matcher.group(2));
    LocalNodeKey key = new LocalNodeKey(owner.name, method.name, method.descriptor, ordinal, category);
    LocalNodeInfo info = new LocalNodeInfo(key, owner, method, boundary,
        Collections.unmodifiableSet(new TreeSet<Integer>(physicalSlots)));
    info.instructionOccurrences = instructionOccurrences;
    info.frameOccurrences = frameOccurrences;
    info.lvtOccurrences = lvtAliases;
    locals.info.put(key, info);
    locals.variableNodes.put(variable, key);
    locals.methodNodes.computeIfAbsent(method, ignored -> new TreeSet<LocalNodeKey>()).add(key);
    if (!boundary) locals.generated.add(key);
    if (matcher.matches()) {
      locals.lvtCompilerIdOccurrences.put(matcher.group(2), idOccurrences);
      if (anchorPosition > 0) locals.nonLvtCompilerIds.add(matcher.group(2));
    }
    locals.coreAsyncSharpLocalInventoryClosed = true;
    return new SharpLocalLvtFixture(owner, method, locals, variable);
  }

  private static void addSyntheticLocalInfo(AltsLvtFixture ignored, ClassModel owner,
                                            MethodModel method, LocalUniverse locals,
                                            LocalVariableModel variable, int ordinal,
                                            boolean boundary, int instructionOccurrences,
                                            int frameOccurrences) {
    LocalNodeKey key = new LocalNodeKey(owner.name, method.name, method.descriptor, ordinal, "A");
    LocalNodeInfo info = new LocalNodeInfo(key, owner, method, boundary,
        Collections.singleton(variable.index));
    info.instructionOccurrences = instructionOccurrences;
    info.frameOccurrences = frameOccurrences;
    info.lvtOccurrences = 1;
    locals.info.put(key, info);
    locals.variableNodes.put(variable, key);
    locals.methodNodes.computeIfAbsent(method, value -> new TreeSet<LocalNodeKey>()).add(key);
    if (!boundary) locals.generated.add(key);
  }

  private static AltsLvtFixture syntheticAltsLvtFixture(String ownerName, boolean exactSource,
                                                        String methodName, String methodDescriptor,
                                                        String mapName, String parameterName,
                                                        int mapSlot2Start, int mapSlot3,
                                                        boolean extraMap) {
    ClassModel owner = basicClass(ownerName, CORE_ASYNC_NAMESPACE);
    owner.access |= Opcodes.ACC_SUPER;
    owner.superName = "clojure/lang/RestFn";
    stampExactCoreAsyncSource(owner, exactSource);
    MethodModel method = basicMethod(methodName, methodDescriptor,
        Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC);
    LabelRef[] labels = new LabelRef[27];
    for (int i = 0; i < labels.length; i++) {
      labels[i] = new LabelRef(i);
      method.insns.add(new Insn("LABEL", -1, labels[i]));
    }
    for (int target : new int[] {2, 3, 4, 5, 6, 7, 8, 9, 24, 25, 26})
      method.insns.add(new Insn("JUMP", Opcodes.GOTO, labels[target]));
    LocalVariableModel map2 = new LocalVariableModel(mapName, "Ljava/lang/Object;", null,
        labels[mapSlot2Start], labels[26], 2);
    LocalVariableModel map3 = new LocalVariableModel(mapName, "Ljava/lang/Object;", null,
        labels[22], labels[26], mapSlot3);
    LocalVariableModel opts = new LocalVariableModel("opts", "Ljava/lang/Object;", null,
        labels[23], labels[26], 4);
    LocalVariableModel ports = new LocalVariableModel("ports", "Ljava/lang/Object;", null,
        labels[0], labels[26], 0);
    LocalVariableModel parameter = new LocalVariableModel(parameterName, "Lclojure/lang/ISeq;", null,
        labels[0], labels[26], 1);
    method.localVariables.addAll(Arrays.asList(map2, map3, opts, ports, parameter));
    LocalVariableModel extra = null;
    if (extraMap) {
      extra = new LocalVariableModel(mapName, "Ljava/lang/Object;", null, labels[23], labels[26], 5);
      method.localVariables.add(extra);
    }
    owner.methods.add(method);
    LocalUniverse locals = new LocalUniverse();
    AltsLvtFixture fixture = new AltsLvtFixture(owner, method, locals, map2, map3, parameter);
    addSyntheticLocalInfo(fixture, owner, method, locals, map2, 2, false, 10, 11);
    addSyntheticLocalInfo(fixture, owner, method, locals, map3, 3, false, 3, 0);
    addSyntheticLocalInfo(fixture, owner, method, locals, opts, 4, false, 0, 0);
    addSyntheticLocalInfo(fixture, owner, method, locals, ports, 5, false, 0, 0);
    addSyntheticLocalInfo(fixture, owner, method, locals, parameter, 1, true, 2, 0);
    if (extra != null) addSyntheticLocalInfo(fixture, owner, method, locals, extra, 6, false, 0, 0);
    return fixture;
  }

  private static Map<String, ClassModel> syntheticCaptureCohort(String id, boolean reversed) {
    String closureName = "test/Fn__" + id;
    ClassModel closure = basicClass(closureName, "test");
    FieldModel a = new FieldModel(); a.name = "a"; a.descriptor = "Ljava/lang/Object;"; a.access = Opcodes.ACC_FINAL;
    FieldModel b = new FieldModel(); b.name = "b"; b.descriptor = "Ljava/lang/Object;"; b.access = Opcodes.ACC_FINAL;
    closure.fields.add(a); closure.fields.add(b);
    MethodModel constructor = basicMethod("<init>", "(Ljava/lang/Object;Ljava/lang/Object;)V", Opcodes.ACC_PUBLIC);
    constructor.codeEvents.add("CODE");
    constructor.insns.add(new Insn("VAR", Opcodes.ALOAD, 0));
    constructor.insns.add(new Insn("METHOD", Opcodes.INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false));
    FieldModel[] order = reversed ? new FieldModel[] {b, a} : new FieldModel[] {a, b};
    for (int i = 0; i < order.length; i++) {
      constructor.insns.add(new Insn("VAR", Opcodes.ALOAD, 0));
      constructor.insns.add(new Insn("VAR", Opcodes.ALOAD, i + 1));
      constructor.insns.add(new Insn("FIELD", Opcodes.PUTFIELD, closureName, order[i].name, order[i].descriptor));
    }
    constructor.insns.add(new Insn("INSN", Opcodes.RETURN));
    closure.methods.add(constructor);

    ClassModel caller = basicClass("test/Caller", "test");
    MethodModel make = basicMethod("make", "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;", Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC);
    make.codeEvents.add("CODE");
    make.insns.add(new Insn("TYPE", Opcodes.NEW, closureName));
    make.insns.add(new Insn("INSN", Opcodes.DUP));
    make.insns.add(new Insn("VAR", Opcodes.ALOAD, reversed ? 1 : 0));
    make.insns.add(new Insn("VAR", Opcodes.ALOAD, reversed ? 0 : 1));
    make.insns.add(new Insn("METHOD", Opcodes.INVOKESPECIAL, closureName, "<init>", constructor.descriptor, false));
    make.insns.add(new Insn("INSN", Opcodes.ARETURN));
    caller.methods.add(make);
    Map<String, ClassModel> result = new TreeMap<String, ClassModel>(); result.put(closure.name, closure); result.put(caller.name, caller); return result;
  }

  private static ClassModel syntheticEmptyGenerated(String prefix, String id) {
    ClassModel model = basicClass("test/" + prefix + "__" + id, "test");
    MethodModel method = basicMethod("run", "()V", Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC);
    method.codeEvents.add("CODE"); method.insns.add(new Insn("INSN", Opcodes.RETURN)); model.methods.add(method); return model;
  }

  private static Map<String, ClassModel> syntheticAmbiguousCohort(String first, String second) {
    Map<String, ClassModel> result = new TreeMap<String, ClassModel>();
    ClassModel a = syntheticEmptyGenerated("Fn", first), b = syntheticEmptyGenerated("Fn", second);
    result.put(a.name, a); result.put(b.name, b); return result;
  }

  private static Map<String, ClassModel> syntheticClassGraphCohort(String first, String second,
                                                                   String callerId) {
    Map<String, ClassModel> result = syntheticAmbiguousCohort(first, second);
    ClassModel caller = basicClass("test/Caller__" + callerId, "test");
    MethodModel method = basicMethod("construct", "()V", Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC);
    method.codeEvents.add("CODE");
    method.insns.add(new Insn("TYPE", Opcodes.NEW, "test/Fn__" + first));
    method.insns.add(new Insn("INSN", Opcodes.POP));
    method.insns.add(new Insn("TYPE", Opcodes.NEW, "test/Fn__" + second));
    method.insns.add(new Insn("INSN", Opcodes.POP));
    method.insns.add(new Insn("INSN", Opcodes.RETURN));
    caller.methods.add(method);
    result.put(caller.name, caller);
    return result;
  }

  private static Map<String, ClassModel> syntheticConflictingCohort(String first, String second) {
    Map<String, ClassModel> result = new TreeMap<String, ClassModel>();
    ClassModel a = syntheticEmptyGenerated("A", first), b = syntheticEmptyGenerated("B", second);
    result.put(a.name, a); result.put(b.name, b); return result;
  }

  private static Map<String, ClassModel> syntheticConstCohort(String fieldName) {
    ClassModel model = basicClass("test/Constants", "test");
    FieldModel field = new FieldModel(); field.name = fieldName; field.descriptor = "Ljava/lang/Object;";
    field.access = Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC; model.fields.add(field);
    Map<String, ClassModel> result = new TreeMap<String, ClassModel>(); result.put(model.name, model); return result;
  }

  private static FieldModel syntheticField(String name, String descriptor, int access) {
    FieldModel field = new FieldModel(); field.name = name; field.descriptor = descriptor; field.access = access;
    return field;
  }

  private static Map<String, ClassModel> singletonCohort(ClassModel model) {
    Map<String, ClassModel> result = new TreeMap<String, ClassModel>();
    result.put(model.name, model);
    return result;
  }

  private static Map<String, ClassModel> syntheticObjectLocalCohort(int firstSlot, int secondSlot,
                                                                    boolean swapParameters,
                                                                    boolean swapUses) {
    ClassModel model = basicClass("test/TypedLocals", "test");
    MethodModel method = basicMethod("combine",
        "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;",
        Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC);
    method.codeEvents.add("CODE");
    method.insns.add(new Insn("VAR", Opcodes.ALOAD, swapParameters ? 1 : 0));
    method.insns.add(new Insn("VAR", Opcodes.ASTORE, firstSlot));
    method.insns.add(new Insn("VAR", Opcodes.ALOAD, swapParameters ? 0 : 1));
    method.insns.add(new Insn("VAR", Opcodes.ASTORE, secondSlot));
    method.insns.add(new Insn("VAR", Opcodes.ALOAD, swapUses ? secondSlot : firstSlot));
    method.insns.add(new Insn("VAR", Opcodes.ALOAD, swapUses ? firstSlot : secondSlot));
    method.insns.add(new Insn("METHOD", Opcodes.INVOKESTATIC, "test/Sink", "combine",
        "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;", false));
    method.insns.add(new Insn("INSN", Opcodes.ARETURN));
    model.methods.add(method);
    return singletonCohort(model);
  }

  private static Map<String, ClassModel> syntheticBoundaryCohort(boolean instance, boolean swapped) {
    ClassModel model = basicClass("test/BoundaryLocals", "test");
    String descriptor = instance ? "(Ljava/lang/Object;)Ljava/lang/Object;" :
        "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;";
    int access = Opcodes.ACC_PUBLIC | (instance ? 0 : Opcodes.ACC_STATIC);
    MethodModel method = basicMethod("select", descriptor, access);
    method.codeEvents.add("CODE");
    method.insns.add(new Insn("VAR", Opcodes.ALOAD, swapped ? 1 : 0));
    method.insns.add(new Insn("INSN", Opcodes.POP));
    method.insns.add(new Insn("VAR", Opcodes.ALOAD, swapped ? 0 : 1));
    method.insns.add(new Insn("INSN", Opcodes.ARETURN));
    model.methods.add(method);
    return singletonCohort(model);
  }

  private static Map<String, ClassModel> syntheticIincCohort(int slot, int increment) {
    ClassModel model = basicClass("test/IincLocals", "test");
    MethodModel method = basicMethod("increment", "(I)I", Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC);
    method.codeEvents.add("CODE");
    method.insns.add(new Insn("VAR", Opcodes.ILOAD, 0));
    method.insns.add(new Insn("VAR", Opcodes.ISTORE, slot));
    method.insns.add(new Insn("IINC", Opcodes.IINC, slot, increment));
    method.insns.add(new Insn("VAR", Opcodes.ILOAD, slot));
    method.insns.add(new Insn("INSN", Opcodes.IRETURN));
    model.methods.add(method);
    return singletonCohort(model);
  }

  private static Map<String, ClassModel> syntheticFrameCohort(Object... locals) {
    ClassModel model = basicClass("test/FrameLocals", "test");
    MethodModel method = basicMethod("run", "()V", Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC);
    method.codeEvents.add("CODE");
    method.insns.add(new Insn("FRAME", -1, Opcodes.F_NEW, locals, new Object[0]));
    method.insns.add(new Insn("INSN", Opcodes.RETURN));
    model.methods.add(method);
    return singletonCohort(model);
  }

  private static Map<String, ClassModel> syntheticLvtCohort(String name, String signature,
                                                             boolean startAtControl,
                                                             boolean extraRow) {
    ClassModel model = basicClass("test/LvtLocals", "test");
    MethodModel method = basicMethod("run", "()V", Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC);
    method.codeEvents.add("CODE");
    LabelRef start = new LabelRef(0), control = new LabelRef(1), end = new LabelRef(2);
    method.insns.add(new Insn("LABEL", -1, start));
    method.insns.add(new Insn("JUMP", Opcodes.GOTO, control));
    method.insns.add(new Insn("LABEL", -1, control));
    method.insns.add(new Insn("INSN", Opcodes.RETURN));
    method.insns.add(new Insn("LABEL", -1, end));
    method.localVariables.add(new LocalVariableModel(name, "Ljava/lang/Object;", signature,
        startAtControl ? control : start, end, 0));
    if (extraRow) method.localVariables.add(new LocalVariableModel("extra", "Ljava/lang/Object;", null,
        start, end, 1));
    model.methods.add(method);
    return singletonCohort(model);
  }

  private static Map<String, ClassModel> syntheticUnknownLocalIdCohort(String classId,
                                                                       String localId) {
    ClassModel model = basicClass("test/Fn__" + classId, "test");
    MethodModel method = basicMethod("run", "()V", Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC);
    method.codeEvents.add("CODE");
    LabelRef start = new LabelRef(0), end = new LabelRef(1);
    method.insns.add(new Insn("LABEL", -1, start));
    method.insns.add(new Insn("INSN", Opcodes.RETURN));
    method.insns.add(new Insn("LABEL", -1, end));
    method.localVariables.add(new LocalVariableModel("tmp" + localId, "Ljava/lang/Object;", null,
        start, end, 0));
    model.methods.add(method);
    return singletonCohort(model);
  }

  private static void stampExactCoreAsyncSource(ClassModel model, boolean exactIdentity) {
    model.sourceEntry = CORE_ASYNC_SOURCE_ENTRY;
    model.sourceSha256 = exactIdentity ? CORE_ASYNC_SOURCE_SHA256 :
        "0000000000000000000000000000000000000000000000000000000000000000";
    model.sourceOwnerJar = CORE_ASYNC_OWNER_JAR;
    model.sourceOwnerSha256 = CORE_ASYNC_OWNER_SHA256;
    model.events.add("SOURCE(" + q("async.clj") + "," + q(null) + ")");
  }

  private static Map<String, ClassModel> syntheticCoreAsyncIocLvtCohort(
      String localName, String owner, String namespace, String methodName,
      String methodDescriptor, int slot, boolean executable, boolean frame,
      boolean exactIdentity) {
    ClassModel model = basicClass(owner, namespace);
    stampExactCoreAsyncSource(model, exactIdentity);
    MethodModel method = basicMethod(methodName, methodDescriptor, Opcodes.ACC_PUBLIC);
    method.codeEvents.add("CODE");
    LabelRef start = new LabelRef(0), end = new LabelRef(1);
    method.insns.add(new Insn("LABEL", -1, start));
    if (executable) {
      method.insns.add(new Insn("INSN", Opcodes.ACONST_NULL));
      method.insns.add(new Insn("VAR", Opcodes.ASTORE, slot));
      if (frame) {
        int frameSize = slot + 1;
        Object[] frameLocals = new Object[frameSize];
        Arrays.fill(frameLocals, Opcodes.TOP);
        frameLocals[0] = owner;
        frameLocals[slot] = "java/lang/Object";
        method.insns.add(new Insn("FRAME", -1, Opcodes.F_NEW, frameLocals, new Object[0]));
      }
      method.insns.add(new Insn("VAR", Opcodes.ALOAD, slot));
      method.insns.add(new Insn("INSN", Opcodes.POP));
    } else if (slot == 0) {
      /* Bind the LVT row to the immutable receiver node for the boundary test. */
      method.insns.add(new Insn("VAR", Opcodes.ALOAD, 0));
      method.insns.add(new Insn("INSN", Opcodes.POP));
    }
    method.insns.add(new Insn("INSN", Opcodes.ACONST_NULL));
    method.insns.add(new Insn("INSN", Opcodes.ARETURN));
    method.insns.add(new Insn("LABEL", -1, end));
    method.localVariables.add(new LocalVariableModel(localName, "Ljava/lang/Object;", null,
        start, end, slot));
    model.methods.add(method);
    Map<String, ClassModel> result = singletonCohort(model);
    Matcher family = STATE_MACHINE_OWNER.matcher(owner);
    if (family.matches() && !family.group(1).equals(owner)) {
      ClassModel machine = basicClass(family.group(1), namespace);
      stampExactCoreAsyncSource(machine, exactIdentity);
      result.put(machine.name, machine);
    }
    return result;
  }

  private static void syntheticVarBinding(MethodModel clinit, String owner, String field, String varName) {
    clinit.insns.add(new Insn("LDC", Opcodes.LDC, IOC_MACROS_NS));
    clinit.insns.add(new Insn("LDC", Opcodes.LDC, varName));
    clinit.insns.add(new Insn("METHOD", Opcodes.INVOKESTATIC, "clojure/lang/RT", "var",
        "(Ljava/lang/String;Ljava/lang/String;)Lclojure/lang/Var;", false));
    clinit.insns.add(new Insn("TYPE", Opcodes.CHECKCAST, "clojure/lang/Var"));
    clinit.insns.add(new Insn("FIELD", Opcodes.PUTSTATIC, owner, field, "Lclojure/lang/Var;"));
  }

  private static void syntheticStateRead(MethodModel method, String owner, long slot) {
    method.insns.add(new Insn("FIELD", Opcodes.GETSTATIC, owner, "aget", "Lclojure/lang/Var;"));
    method.insns.add(new Insn("METHOD", Opcodes.INVOKEVIRTUAL, "clojure/lang/Var", "getRawRoot",
        "()Ljava/lang/Object;", false));
    method.insns.add(new Insn("TYPE", Opcodes.CHECKCAST, "clojure/lang/IFn$OLO"));
    method.insns.add(new Insn("VAR", Opcodes.ALOAD, 0));
    method.insns.add(new Insn("FIELD", Opcodes.GETFIELD, owner, "state", "Ljava/lang/Object;"));
    method.insns.add(new Insn("LDC", Opcodes.LDC, Long.valueOf(slot)));
    method.insns.add(new Insn("METHOD", Opcodes.INVOKEINTERFACE, "clojure/lang/IFn$OLO", "invokePrim",
        "(Ljava/lang/Object;J)Ljava/lang/Object;", true));
    method.insns.add(new Insn("INSN", Opcodes.POP));
  }

  private static void syntheticStateWriteStatic(MethodModel method, String owner, long slot) {
    method.insns.add(new Insn("FIELD", Opcodes.GETSTATIC, owner, "aset", "Lclojure/lang/Var;"));
    method.insns.add(new Insn("METHOD", Opcodes.INVOKEVIRTUAL, "clojure/lang/Var", "getRawRoot",
        "()Ljava/lang/Object;", false));
    method.insns.add(new Insn("TYPE", Opcodes.CHECKCAST, "clojure/lang/IFn$OLOO"));
    method.insns.add(new Insn("VAR", Opcodes.ALOAD, 0));
    method.insns.add(new Insn("FIELD", Opcodes.GETFIELD, owner, "state", "Ljava/lang/Object;"));
    method.insns.add(new Insn("LDC", Opcodes.LDC, Long.valueOf(slot)));
    method.insns.add(new Insn("FIELD", Opcodes.GETSTATIC, owner, "value", "Ljava/lang/Object;"));
    method.insns.add(new Insn("METHOD", Opcodes.INVOKEINTERFACE, "clojure/lang/IFn$OLOO", "invokePrim",
        "(Ljava/lang/Object;JLjava/lang/Object;)Ljava/lang/Object;", true));
    method.insns.add(new Insn("INSN", Opcodes.POP));
  }

  private static Map<String, ClassModel> syntheticStateSlotCohort(long[] firstSlots, long[] secondSlots,
                                                                  long ordinaryLong, boolean finalValue) {
    String owner = "test/Fn__10$state_machine__10__auto____10";
    ClassModel model = basicClass(owner, "test");
    model.fields.add(syntheticField("state", "Ljava/lang/Object;", Opcodes.ACC_PUBLIC | Opcodes.ACC_FINAL));
    model.fields.add(syntheticField("aget", "Lclojure/lang/Var;",
        Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC | Opcodes.ACC_FINAL));
    model.fields.add(syntheticField("aset", "Lclojure/lang/Var;",
        Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC | Opcodes.ACC_FINAL));
    model.fields.add(syntheticField("value", "Ljava/lang/Object;",
        Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC | (finalValue ? Opcodes.ACC_FINAL : 0)));

    MethodModel constructor = basicMethod("<init>", "(Ljava/lang/Object;)V", Opcodes.ACC_PUBLIC);
    constructor.codeEvents.add("CODE");
    constructor.insns.add(new Insn("VAR", Opcodes.ALOAD, 0));
    constructor.insns.add(new Insn("METHOD", Opcodes.INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false));
    constructor.insns.add(new Insn("VAR", Opcodes.ALOAD, 0));
    constructor.insns.add(new Insn("VAR", Opcodes.ALOAD, 1));
    constructor.insns.add(new Insn("FIELD", Opcodes.PUTFIELD, owner, "state", "Ljava/lang/Object;"));
    constructor.insns.add(new Insn("INSN", Opcodes.RETURN));
    model.methods.add(constructor);

    MethodModel clinit = basicMethod("<clinit>", "()V", Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC);
    clinit.codeEvents.add("CODE");
    syntheticVarBinding(clinit, owner, "aget", "aget-object");
    syntheticVarBinding(clinit, owner, "aset", "aset-object");
    clinit.insns.add(new Insn("INSN", Opcodes.RETURN));
    model.methods.add(clinit);

    MethodModel first = basicMethod("first", "()V", Opcodes.ACC_PUBLIC);
    first.codeEvents.add("CODE");
    for (long slot : firstSlots) syntheticStateRead(first, owner, slot);
    if (firstSlots.length > 0) syntheticStateWriteStatic(first, owner, firstSlots[0]);
    first.insns.add(new Insn("LDC", Opcodes.LDC, Long.valueOf(ordinaryLong)));
    first.insns.add(new Insn("INSN", Opcodes.POP2));
    first.insns.add(new Insn("INSN", Opcodes.RETURN));
    model.methods.add(first);

    MethodModel second = basicMethod("second", "()V", Opcodes.ACC_PUBLIC);
    second.codeEvents.add("CODE");
    for (long slot : secondSlots) syntheticStateRead(second, owner, slot);
    second.insns.add(new Insn("INSN", Opcodes.RETURN));
    model.methods.add(second);

    Map<String, ClassModel> result = new TreeMap<String, ClassModel>(); result.put(owner, model); return result;
  }

  public static void main(String[] args) throws Exception {
    Locale.setDefault(Locale.ROOT);
    if (args.length == 2 && args[0].equals("--self-test")) {
      int status = selfTest(Path.of(args[1]));
      if (status != 0) System.exit(status);
      return;
    }
    if (args.length != 6) {
      System.err.println("usage: CompareExactSourceAot CANDIDATE_A CANDIDATE_B ORIGINAL_JAR COHORT_TSV CLASS_OWNERSHIP_TSV OUTPUT_DIR");
      System.exit(2);
    }
    Path output = Path.of(args[5]);
    require(!Files.exists(output), "refusing to overwrite comparator output: " + output);
    Files.createDirectories(output);
    List<String> failures = new ArrayList<String>();
    List<String> mappingRows = new ArrayList<String>();
    List<String> captureRows = new ArrayList<String>();
    List<String> classRows = new ArrayList<String>();
    List<String> memberRows = new ArrayList<String>();
    List<String> stateSlotRows = new ArrayList<String>();
    List<String> localRows = new ArrayList<String>();
    List<String> comparisonRows = new ArrayList<String>();
    List<String> externalRows = new ArrayList<String>();
    List<String> requestedRelations = Arrays.asList(
        "candidate-a--candidate-b", "candidate-a--original", "candidate-b--original");
    List<RelationOutcome> relationOutcomes = new ArrayList<RelationOutcome>();
    int status = 0;
    Throwable unexpectedFailure = null;
    try {
      Map<String, String> ownership = readOwnership(Path.of(args[3]), Path.of(args[4]));
      Map<String, SourceIdentity> identities = readSourceIdentities(Path.of(args[3]));
      Map<String, ClassModel> candidateA = readCandidate(Path.of(args[0]), ownership);
      Map<String, ClassModel> candidateB = readCandidate(Path.of(args[1]), ownership);
      Map<String, ClassModel> original = readOriginal(Path.of(args[2]), ownership);
      stampSourceIdentities(candidateA, identities);
      stampSourceIdentities(candidateB, identities);
      stampSourceIdentities(original, identities);
      require(candidateA.size() == ownership.size(), "candidate A class count differs: " + candidateA.size() + " vs " + ownership.size());
      require(candidateB.size() == ownership.size(), "candidate B class count differs: " + candidateB.size() + " vs " + ownership.size());
      require(original.size() == ownership.size(), "original class count differs: " + original.size() + " vs " + ownership.size());
      Set<String> generatedOwnedClasses = new HashSet<String>();
      for (String ownedClass : ownership.keySet()) if (!ids(ownedClass).isEmpty()) generatedOwnedClasses.add(ownedClass);
      ActiveCapturePair candidateAOriginalCaptures = activeCapturePairs(candidateA, original);
      ActiveCapturePair candidateBOriginalCaptures = activeCapturePairs(candidateB, original);
      for (CaptureSpec spec : candidateAOriginalCaptures.right.values()) generatedOwnedClasses.add(spec.owner.name);
      for (CaptureSpec spec : candidateBOriginalCaptures.right.values()) generatedOwnedClasses.add(spec.owner.name);
      externalRows.addAll(externalOriginalUses(Path.of(args[2]), ownership.keySet(), generatedOwnedClasses));
      require(externalRows.isEmpty(), "compiler-ID-bearing owned classes have references outside the exact-source cohort: " + externalRows.subList(0, Math.min(5, externalRows.size())));
      List<RelationSpec> relations = Arrays.asList(
          new RelationSpec("candidate-a--candidate-b", evidence -> compare(
              "candidate-a--candidate-b", candidateA, candidateB,
              evidence.mappingRows, evidence.captureRows, evidence.classRows,
              evidence.memberRows, evidence.stateSlotRows, evidence.localRows)),
          new RelationSpec("candidate-a--original", evidence -> compare(
              "candidate-a--original", candidateA, original,
              evidence.mappingRows, evidence.captureRows, evidence.classRows,
              evidence.memberRows, evidence.stateSlotRows, evidence.localRows)),
          new RelationSpec("candidate-b--original", evidence -> compare(
              "candidate-b--original", candidateB, original,
              evidence.mappingRows, evidence.captureRows, evidence.classRows,
              evidence.memberRows, evidence.stateSlotRows, evidence.localRows)));
      require(requestedRelations.equals(relationLabels(relations)),
          "relation driver request order differs from the fixed three-relation boundary");
      RelationRun relationRun = runRelations(relations);
      relationOutcomes.addAll(relationRun.outcomes);
      mappingRows.addAll(relationRun.evidence.mappingRows);
      captureRows.addAll(relationRun.evidence.captureRows);
      classRows.addAll(relationRun.evidence.classRows);
      memberRows.addAll(relationRun.evidence.memberRows);
      stateSlotRows.addAll(relationRun.evidence.stateSlotRows);
      localRows.addAll(relationRun.evidence.localRows);
      status = relationRunStatus(requestedRelations, relationOutcomes);
    } catch (IOException | Failure failure) {
      status = 1;
      failure.printStackTrace(System.err);
      failures.add(failure.getClass().getName() + "\tsetup: " + clean(failure.getMessage()));
      for (String relation : requestedRelations) {
        relationOutcomes.add(new RelationOutcome(relation, null,
            new Failure("not run because comparator setup failed")));
      }
    } catch (Throwable failure) {
      status = 1;
      unexpectedFailure = failure;
      failure.printStackTrace(System.err);
      failures.add(failure.getClass().getName() + "\tunexpected setup/driver failure: " +
          clean(failure.getMessage()));
      for (String relation : requestedRelations) {
        relationOutcomes.add(new RelationOutcome(relation, null,
            new Failure("not run because comparator setup/driver failed unexpectedly")));
      }
    }
    for (RelationOutcome outcome : relationOutcomes) {
      comparisonRows.add(comparisonRow(outcome));
      if (outcome.failure != null) {
        failures.add(outcome.failure.getClass().getName() + "\trelation " + outcome.label +
            ": " + clean(outcome.failure.getMessage()));
        outcome.failure.printStackTrace(System.err);
      }
    }
    boolean relationLedgerComplete = relationLedgerComplete(requestedRelations, relationOutcomes);
    if (!relationLedgerComplete) status = 1;
    write(output.resolve("comparisons.tsv"), "comparison\tstatus\tclasses\tclass_nodes\tmember_nodes\tstate_slot_nodes\tlocal_nodes\tcaptured_constructors_left\tcaptured_constructors_right\tconstruction_sites_left\tconstruction_sites_right\tstate_slot_accesses_left\tstate_slot_accesses_right", comparisonRows);
    Collections.sort(mappingRows);
    Collections.sort(captureRows);
    Collections.sort(classRows);
    Collections.sort(memberRows);
    Collections.sort(stateSlotRows);
    Collections.sort(localRows);
    write(output.resolve("compiler-id-mappings.tsv"), "comparison\tnamespace\tleft_id\tright_id\tidentity", mappingRows);
    write(output.resolve("captured-constructors.tsv"), "comparison\tside\tnamespace\tclass\tdescriptor\tfields", captureRows);
    write(output.resolve("class-mappings.tsv"), "comparison\tnamespace\tleft_class\tright_class\tidentity", classRows);
    write(output.resolve("member-mappings.tsv"), "comparison\tleft_owner\tkind\tleft_name\tleft_descriptor\tright_owner\tright_name\tright_descriptor", memberRows);
    write(output.resolve("state-slot-mappings.tsv"), "comparison\tleft_machine\tleft_index\tright_machine\tright_index\tleft_accesses\tright_accesses", stateSlotRows);
    write(output.resolve("local-node-mappings.tsv"), "comparison\tleft_owner\tleft_method\tleft_descriptor\tleft_ordinal\tleft_category\tleft_physical_slots\tright_owner\tright_method\tright_descriptor\tright_ordinal\tright_category\tright_physical_slots", localRows);
    write(output.resolve("external-original-use-sites.tsv"), "caller\tmethod\topcode\ttarget", externalRows);
    write(output.resolve("failures.tsv"), "failure_type\tdetail", failures);
    List<String> summary = new ArrayList<String>();
    summary.add("status\t" + (status == 0 ? "PASS" : "FAIL"));
    summary.add("model\tcomplete ABI/instructions and expanded stack-map frame values recorded; max stack/locals are verifier-derived and excluded");
    summary.add("compiler_id_policy\texact owned-class-node pairing; numeric consistency checked per fresh-JVM namespace compile unit; no rank/common-ID inference; member names and semantic strings exact");
    summary.add("capture_policy\tpair-specific changed constructors only; exact assignments; uniquely partitioned independent loads/constants/own-field clears; ambiguity fails");
    summary.add("outside_cohort_policy\tall references to compiler-ID-bearing owned classes and normalized-constructor owners scanned in the original artifact");
    summary.add("full_runtime_boundary_scan\tNOT_RUN_BY_COMPARATOR; separate acceptance blocker");
    summary.add("explicit_jvm_verification\tNOT_RUN_BY_COMPARATOR; separate acceptance blocker");
    summary.add("comparisons.requested\t3");
    summary.add("comparisons.recorded\t" + relationOutcomes.size());
    summary.add("comparisons.passed\t" + relationOutcomes.stream().filter(RelationOutcome::passed).count());
    summary.add("comparisons.failed\t" + relationOutcomes.stream().filter(outcome -> !outcome.passed()).count());
    summary.add("comparison_ledger.complete\t" + relationLedgerComplete);
    summary.add("comparison_failure_policy\texpected semantic Failure is relation-local; later relations still run; failed-relation partial mappings are discarded");
    summary.add("failures\t" + failures.size());
    write(output.resolve("summary.tsv"), "metric\tvalue", summary);
    if (unexpectedFailure instanceof Error) throw (Error) unexpectedFailure;
    if (unexpectedFailure instanceof RuntimeException) throw (RuntimeException) unexpectedFailure;
    if (unexpectedFailure instanceof Exception) throw (Exception) unexpectedFailure;
    if (unexpectedFailure != null) throw new RuntimeException(unexpectedFailure);
    if (status != 0) System.exit(status);
  }
}
