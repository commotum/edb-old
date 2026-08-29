import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.regex.Pattern;

import jdk.internal.org.objectweb.asm.ClassReader;
import jdk.internal.org.objectweb.asm.Handle;
import jdk.internal.org.objectweb.asm.Opcodes;
import jdk.internal.org.objectweb.asm.Type;
import jdk.internal.org.objectweb.asm.tree.AbstractInsnNode;
import jdk.internal.org.objectweb.asm.tree.ClassNode;
import jdk.internal.org.objectweb.asm.tree.FieldInsnNode;
import jdk.internal.org.objectweb.asm.tree.FrameNode;
import jdk.internal.org.objectweb.asm.tree.IincInsnNode;
import jdk.internal.org.objectweb.asm.tree.InsnNode;
import jdk.internal.org.objectweb.asm.tree.IntInsnNode;
import jdk.internal.org.objectweb.asm.tree.InvokeDynamicInsnNode;
import jdk.internal.org.objectweb.asm.tree.JumpInsnNode;
import jdk.internal.org.objectweb.asm.tree.LabelNode;
import jdk.internal.org.objectweb.asm.tree.LdcInsnNode;
import jdk.internal.org.objectweb.asm.tree.LineNumberNode;
import jdk.internal.org.objectweb.asm.tree.LookupSwitchInsnNode;
import jdk.internal.org.objectweb.asm.tree.MethodInsnNode;
import jdk.internal.org.objectweb.asm.tree.MethodNode;
import jdk.internal.org.objectweb.asm.tree.MultiANewArrayInsnNode;
import jdk.internal.org.objectweb.asm.tree.TableSwitchInsnNode;
import jdk.internal.org.objectweb.asm.tree.TryCatchBlockNode;
import jdk.internal.org.objectweb.asm.tree.TypeInsnNode;
import jdk.internal.org.objectweb.asm.tree.VarInsnNode;
import jdk.internal.org.objectweb.asm.util.Printer;

/**
 * Conservative, instruction-level proof for the dead-verifier-slot family.
 *
 * This deliberately does not try to establish general bytecode equivalence.
 * A residual is called dead-only only when removing instructions unreachable
 * from method entry and every exception handler produces the same normalized
 * reachable instruction stream, CFG, and projected exception table in both
 * artifacts. Every branch, switch arm, fallthrough, and conservative exception
 * edge participates in reachability.
 */
public final class ProveDeadVerifierSlots {
  private static final String TAB = "\t";
  private static final Pattern GENERATED_ID = Pattern.compile("__[0-9]+");
  private static final Pattern GENERATED_HASH = Pattern.compile("\\$[0-9a-fA-F]{8}(?=($|[/;$]))");
  private static final int METHOD_ACCESS_MASK =
      Opcodes.ACC_PUBLIC | Opcodes.ACC_PRIVATE | Opcodes.ACC_PROTECTED |
      Opcodes.ACC_STATIC | Opcodes.ACC_FINAL | Opcodes.ACC_SYNCHRONIZED |
      Opcodes.ACC_BRIDGE | Opcodes.ACC_VARARGS | Opcodes.ACC_NATIVE |
      Opcodes.ACC_ABSTRACT | Opcodes.ACC_STRICT | Opcodes.ACC_SYNTHETIC;

  private static final class InventoryEntry {
    final String namespace;
    final String role;
    final String internalName;

    InventoryEntry(String namespace, String role, String internalName) {
      this.namespace = namespace;
      this.role = role;
      this.internalName = internalName;
    }
  }

  private static final class ClassifierRow {
    final Map<String, String> values;

    ClassifierRow(Map<String, String> values) {
      this.values = values;
    }

    String get(String key) {
      String value = values.get(key);
      return value == null ? "" : value;
    }
  }

  private static final class MethodAnalysis {
    final MethodNode method;
    final List<AbstractInsnNode> instructions = new ArrayList<>();
    final IdentityHashMap<AbstractInsnNode, Integer> instructionIndex = new IdentityHashMap<>();
    final boolean[] reachable;
    final int[] reachableRank;
    final int[] deadGap;
    final int[] deadOrdinal;
    final List<TryCatchBlockNode> tryCatchBlocks;
    final String fullFingerprint;
    final String reachableInstructionFingerprint;
    final String reachableExceptionFingerprint;
    final List<String> gapFingerprints;
    final List<Integer> gapInstructionCounts;

    MethodAnalysis(MethodNode method) {
      this.method = method;
      this.tryCatchBlocks = method.tryCatchBlocks == null
          ? Collections.<TryCatchBlockNode>emptyList() : method.tryCatchBlocks;
      for (AbstractInsnNode node = method.instructions.getFirst(); node != null; node = node.getNext()) {
        if (node.getOpcode() >= 0) {
          instructionIndex.put(node, instructions.size());
          instructions.add(node);
        }
      }
      this.reachable = computeReachability();
      this.reachableRank = new int[instructions.size()];
      Arrays.fill(reachableRank, -1);
      int reachableCount = 0;
      for (int i = 0; i < instructions.size(); i++) {
        if (reachable[i]) reachableRank[i] = reachableCount++;
      }
      this.deadGap = new int[instructions.size()];
      this.deadOrdinal = new int[instructions.size()];
      int gap = 0;
      int ordinal = 0;
      for (int i = 0; i < instructions.size(); i++) {
        if (reachable[i]) {
          gap++;
          ordinal = 0;
        } else {
          deadGap[i] = gap;
          deadOrdinal[i] = ordinal++;
        }
      }
      this.fullFingerprint = fingerprintAllInstructions();
      this.reachableInstructionFingerprint = fingerprintReachableInstructions();
      this.reachableExceptionFingerprint = fingerprintReachableExceptions();
      this.gapFingerprints = new ArrayList<>();
      this.gapInstructionCounts = new ArrayList<>();
      for (int g = 0; g <= reachableCount; g++) {
        StringBuilder value = new StringBuilder();
        int count = 0;
        for (int i = 0; i < instructions.size(); i++) {
          if (!reachable[i] && deadGap[i] == g) {
            appendRecord(value, semanticToken(instructions.get(i)) + "|" + edgeToken(i, IndexMode.GAP));
            count++;
          }
        }
        gapFingerprints.add(sha256(value.toString()));
        gapInstructionCounts.add(count);
      }
    }

    private boolean[] computeReachability() {
      boolean[] seen = new boolean[instructions.size()];
      if (instructions.isEmpty()) return seen;
      ArrayDeque<Integer> pending = new ArrayDeque<>();
      addPending(pending, seen, 0);
      // Handlers are roots, not merely conditional edges. This is intentionally
      // conservative and prevents verifier scaffolding at a handler target from
      // being mislabeled dead.
      for (TryCatchBlockNode block : tryCatchBlocks) {
        addPending(pending, seen, boundaryIndex(block.handler));
      }
      while (!pending.isEmpty()) {
        int index = pending.removeFirst();
        for (int successor : normalSuccessors(index)) addPending(pending, seen, successor);
        for (TryCatchBlockNode block : tryCatchBlocks) {
          int start = boundaryIndex(block.start);
          int end = boundaryIndex(block.end);
          if (index >= start && index < end) {
            addPending(pending, seen, boundaryIndex(block.handler));
          }
        }
      }
      return seen;
    }

    private void addPending(ArrayDeque<Integer> pending, boolean[] seen, int index) {
      if (index >= 0 && index < seen.length && !seen[index]) {
        seen[index] = true;
        pending.addLast(index);
      }
    }

    private List<Integer> normalSuccessors(int index) {
      AbstractInsnNode instruction = instructions.get(index);
      int opcode = instruction.getOpcode();
      List<Integer> result = new ArrayList<>();
      if (instruction instanceof JumpInsnNode) {
        result.add(boundaryIndex(((JumpInsnNode) instruction).label));
        if (opcode != Opcodes.GOTO && opcode != Opcodes.JSR && index + 1 < instructions.size()) {
          result.add(index + 1);
        }
        // A legacy JSR eventually returns to its successor. Treating both edges
        // as reachable is conservative even though modern Clojure emits no JSR.
        if (opcode == Opcodes.JSR && index + 1 < instructions.size()) result.add(index + 1);
      } else if (instruction instanceof TableSwitchInsnNode) {
        TableSwitchInsnNode table = (TableSwitchInsnNode) instruction;
        result.add(boundaryIndex(table.dflt));
        for (LabelNode label : table.labels) result.add(boundaryIndex(label));
      } else if (instruction instanceof LookupSwitchInsnNode) {
        LookupSwitchInsnNode lookup = (LookupSwitchInsnNode) instruction;
        result.add(boundaryIndex(lookup.dflt));
        for (LabelNode label : lookup.labels) result.add(boundaryIndex(label));
      } else if (!isTerminal(opcode) && index + 1 < instructions.size()) {
        result.add(index + 1);
      }
      return result;
    }

    private int boundaryIndex(LabelNode label) {
      for (AbstractInsnNode node = label; node != null; node = node.getNext()) {
        Integer index = instructionIndex.get(node);
        if (index != null) return index;
      }
      return instructions.size();
    }

    private String fingerprintAllInstructions() {
      StringBuilder value = new StringBuilder();
      for (int i = 0; i < instructions.size(); i++) {
        appendRecord(value, semanticToken(instructions.get(i)) + "|" + edgeToken(i, IndexMode.FULL));
      }
      appendRecord(value, "EXCEPTIONS=" + fingerprintFullExceptions());
      return sha256(value.toString());
    }

    private String fingerprintReachableInstructions() {
      StringBuilder value = new StringBuilder();
      for (int i = 0; i < instructions.size(); i++) {
        if (reachable[i]) {
          appendRecord(value, semanticToken(instructions.get(i)) + "|" + edgeToken(i, IndexMode.REACHABLE));
        }
      }
      return sha256(value.toString());
    }

    private String fingerprintFullExceptions() {
      StringBuilder value = new StringBuilder();
      for (TryCatchBlockNode block : tryCatchBlocks) {
        appendRecord(value, boundaryIndex(block.start) + ":" + boundaryIndex(block.end) + ":" +
            boundaryIndex(block.handler) + ":" + normalize(nullToEmpty(block.type)));
      }
      return sha256(value.toString());
    }

    private String fingerprintReachableExceptions() {
      StringBuilder value = new StringBuilder();
      for (TryCatchBlockNode block : tryCatchBlocks) {
        appendRecord(value, reachableBoundaryRank(boundaryIndex(block.start)) + ":" +
            reachableBoundaryRank(boundaryIndex(block.end)) + ":" +
            location(boundaryIndex(block.handler), IndexMode.REACHABLE) + ":" +
            normalize(nullToEmpty(block.type)));
      }
      return sha256(value.toString());
    }

    private int reachableBoundaryRank(int boundary) {
      int count = 0;
      int limit = Math.min(boundary, instructions.size());
      for (int i = 0; i < limit; i++) if (reachable[i]) count++;
      return count;
    }

    private enum IndexMode { FULL, REACHABLE, GAP }

    private String edgeToken(int index, IndexMode mode) {
      AbstractInsnNode instruction = instructions.get(index);
      int opcode = instruction.getOpcode();
      StringBuilder value = new StringBuilder();
      if (instruction instanceof JumpInsnNode) {
        JumpInsnNode jump = (JumpInsnNode) instruction;
        value.append("J=").append(location(boundaryIndex(jump.label), mode));
        if (opcode != Opcodes.GOTO && index + 1 < instructions.size()) {
          value.append(";F=").append(location(index + 1, mode));
        }
      } else if (instruction instanceof TableSwitchInsnNode) {
        TableSwitchInsnNode table = (TableSwitchInsnNode) instruction;
        value.append("D=").append(location(boundaryIndex(table.dflt), mode));
        for (int i = 0; i < table.labels.size(); i++) {
          value.append(";K").append(table.min + i).append('=')
              .append(location(boundaryIndex(table.labels.get(i)), mode));
        }
      } else if (instruction instanceof LookupSwitchInsnNode) {
        LookupSwitchInsnNode lookup = (LookupSwitchInsnNode) instruction;
        value.append("D=").append(location(boundaryIndex(lookup.dflt), mode));
        for (int i = 0; i < lookup.labels.size(); i++) {
          value.append(";K").append(lookup.keys.get(i)).append('=')
              .append(location(boundaryIndex(lookup.labels.get(i)), mode));
        }
      } else if (!isTerminal(opcode) && index + 1 < instructions.size()) {
        value.append("F=").append(location(index + 1, mode));
      } else {
        value.append("END");
      }
      // Exception edges are attached to every covered instruction, even those
      // whose opcode cannot throw. This over-approximation is intentional.
      for (int i = 0; i < tryCatchBlocks.size(); i++) {
        TryCatchBlockNode block = tryCatchBlocks.get(i);
        if (index >= boundaryIndex(block.start) && index < boundaryIndex(block.end)) {
          value.append(";X").append(i).append('=')
              .append(location(boundaryIndex(block.handler), mode)).append(':')
              .append(normalize(nullToEmpty(block.type)));
        }
      }
      return value.toString();
    }

    private String location(int index, IndexMode mode) {
      if (index == instructions.size()) return "END";
      if (index < 0 || index > instructions.size()) return "INVALID";
      if (mode == IndexMode.FULL) return "I" + index;
      if (reachable[index]) return "R" + reachableRank[index];
      if (mode == IndexMode.REACHABLE) return "UNREACHABLE_TARGET";
      return "D" + deadGap[index] + "." + deadOrdinal[index];
    }

    int reachableCount() {
      int count = 0;
      for (boolean value : reachable) if (value) count++;
      return count;
    }

    int deadCount() {
      return reachable.length - reachableCount();
    }
  }

  private static final class MethodComparison {
    final String relation;
    final String disqualifier;
    final MethodAnalysis peer;
    final MethodAnalysis transactor;
    final List<Integer> differingGaps;

    MethodComparison(String relation, String disqualifier, MethodAnalysis peer,
                     MethodAnalysis transactor, List<Integer> differingGaps) {
      this.relation = relation;
      this.disqualifier = disqualifier;
      this.peer = peer;
      this.transactor = transactor;
      this.differingGaps = differingGaps;
    }
  }

  private static final class NamespaceStats {
    final String namespace;
    int pairedClasses;
    int missingClassRoles;
    int exactMethods;
    int deadOnlyMethods;
    int deadBlocks;
    int peerDeadInstructions;
    int transactorDeadInstructions;
    int reachableDifferenceMethods;
    int exceptionDifferenceMethods;
    int unaccountedDifferenceMethods;
    int missingMethods;
    final Set<String> disqualifiers = new TreeSet<>();

    NamespaceStats(String namespace) {
      this.namespace = namespace;
    }
  }

  public static void main(String[] args) throws Exception {
    if (args.length != 7) {
      System.err.println("usage: ProveDeadVerifierSlots PEER_JAR TRANSACTOR_JAR " +
          "PEER_INVENTORY TRANSACTOR_INVENTORY CLASSIFIER_TSV METHOD_DIFF_TSV OUTPUT_DIR");
      System.exit(64);
    }
    Path peerJar = Path.of(args[0]);
    Path transactorJar = Path.of(args[1]);
    Path peerInventoryPath = Path.of(args[2]);
    Path transactorInventoryPath = Path.of(args[3]);
    Path classifierPath = Path.of(args[4]);
    Path methodDiffPath = Path.of(args[5]);
    Path outputDir = Path.of(args[6]);
    Files.createDirectories(outputDir);

    Map<String, InventoryEntry> peerInventory = readInventory(peerInventoryPath);
    Map<String, InventoryEntry> transactorInventory = readInventory(transactorInventoryPath);
    Map<String, ClassifierRow> classifier = readClassifier(classifierPath);
    Map<String, Integer> globalResidualMethods = readGlobalResidualCounts(methodDiffPath);
    if (classifier.size() != 117) {
      throw new IllegalStateException("expected 117 classifier rows, found " + classifier.size());
    }

    writeNegativeControls(outputDir.resolve("negative-controls.tsv"));
    Map<String, NamespaceStats> stats = new TreeMap<>();
    for (String namespace : classifier.keySet()) stats.put(namespace, new NamespaceStats(namespace));

    try (JarFile peer = new JarFile(peerJar.toFile());
         JarFile transactor = new JarFile(transactorJar.toFile());
         BufferedWriter methods = Files.newBufferedWriter(outputDir.resolve("method-analysis.tsv"), StandardCharsets.UTF_8);
         BufferedWriter blocks = Files.newBufferedWriter(outputDir.resolve("dead-residual-blocks.tsv"), StandardCharsets.UTF_8)) {
      methods.write("namespace\tclass_role\tmethod_key\toccurrence\trelation\tpeer_reachable_instructions\ttransactor_reachable_instructions\tpeer_dead_instructions\ttransactor_dead_instructions\tdead_residual_blocks\tpeer_reachable_sha256\ttransactor_reachable_sha256\tpeer_exception_projection_sha256\ttransactor_exception_projection_sha256\tdisqualifier\n");
      blocks.write("namespace\tclass_role\tmethod_key\toccurrence\tgap_after_reachable_instruction\tpeer_dead_instructions\ttransactor_dead_instructions\tpeer_gap_sha256\ttransactor_gap_sha256\n");

      Set<String> roleKeys = new TreeSet<>();
      roleKeys.addAll(peerInventory.keySet());
      roleKeys.addAll(transactorInventory.keySet());
      for (String roleKey : roleKeys) {
        InventoryEntry peerEntry = peerInventory.get(roleKey);
        InventoryEntry transactorEntry = transactorInventory.get(roleKey);
        String namespace = peerEntry != null ? peerEntry.namespace : transactorEntry.namespace;
        NamespaceStats namespaceStats = stats.get(namespace);
        if (namespaceStats == null) continue;
        if (peerEntry == null || transactorEntry == null) {
          namespaceStats.missingClassRoles++;
          namespaceStats.disqualifiers.add("CLASS_ROLE_MISSING");
          continue;
        }
        namespaceStats.pairedClasses++;
        ClassNode peerClass = readClass(peer, peerEntry.internalName);
        ClassNode transactorClass = readClass(transactor, transactorEntry.internalName);
        compareClassMethods(namespaceStats, peerEntry.role, peerClass, transactorClass, methods, blocks);
      }
    }

    writeNamespaceSummary(outputDir.resolve("namespace-summary.tsv"), stats, classifier,
        globalResidualMethods, outputDir.resolve("unlocked-family-covered.tsv"));
    writeSummary(outputDir.resolve("summary.tsv"), stats, classifier,
        outputDir.resolve("unlocked-family-covered.tsv"));
  }

  private static void compareClassMethods(NamespaceStats stats, String role,
                                          ClassNode peerClass, ClassNode transactorClass,
                                          BufferedWriter methods, BufferedWriter blocks) throws IOException {
    Map<String, List<MethodNode>> peerMethods = groupMethods(peerClass.methods);
    Map<String, List<MethodNode>> transactorMethods = groupMethods(transactorClass.methods);
    Set<String> keys = new TreeSet<>();
    keys.addAll(peerMethods.keySet());
    keys.addAll(transactorMethods.keySet());
    for (String methodKey : keys) {
      List<MethodNode> peerGroup = peerMethods.getOrDefault(methodKey, Collections.<MethodNode>emptyList());
      List<MethodNode> transactorGroup = transactorMethods.getOrDefault(methodKey, Collections.<MethodNode>emptyList());
      int count = Math.max(peerGroup.size(), transactorGroup.size());
      for (int occurrence = 0; occurrence < count; occurrence++) {
        MethodNode peerMethod = occurrence < peerGroup.size() ? peerGroup.get(occurrence) : null;
        MethodNode transactorMethod = occurrence < transactorGroup.size() ? transactorGroup.get(occurrence) : null;
        if (peerMethod == null || transactorMethod == null) {
          stats.missingMethods++;
          stats.disqualifiers.add("METHOD_ABI_MISSING");
          writeMissingMethod(methods, stats.namespace, role, methodKey, occurrence + 1);
          continue;
        }
        MethodComparison comparison = compareMethods(peerMethod, transactorMethod);
        recordComparison(stats, role, methodKey, occurrence + 1, comparison, methods, blocks);
      }
    }
  }

  private static MethodComparison compareMethods(MethodNode peerMethod, MethodNode transactorMethod) {
    boolean peerHasCode = peerMethod.instructions != null && peerMethod.instructions.size() > 0;
    boolean transactorHasCode = transactorMethod.instructions != null && transactorMethod.instructions.size() > 0;
    if (!peerHasCode && !transactorHasCode) {
      return new MethodComparison("EXACT_NO_CODE", "none", null, null, Collections.<Integer>emptyList());
    }
    if (peerHasCode != transactorHasCode) {
      return new MethodComparison("DISQUALIFIED", "CODE_PRESENCE_DIFFERENCE", null, null,
          Collections.<Integer>emptyList());
    }
    MethodAnalysis peer = new MethodAnalysis(peerMethod);
    MethodAnalysis transactor = new MethodAnalysis(transactorMethod);
    if (peer.fullFingerprint.equals(transactor.fullFingerprint)) {
      return new MethodComparison("EXACT", "none", peer, transactor, Collections.<Integer>emptyList());
    }
    if (!peer.reachableExceptionFingerprint.equals(transactor.reachableExceptionFingerprint)) {
      return new MethodComparison("DISQUALIFIED", "REACHABLE_EXCEPTION_TABLE_DIFFERENCE", peer,
          transactor, Collections.<Integer>emptyList());
    }
    if (!peer.reachableInstructionFingerprint.equals(transactor.reachableInstructionFingerprint)) {
      return new MethodComparison("DISQUALIFIED", "REACHABLE_INSTRUCTION_OR_CFG_DIFFERENCE", peer,
          transactor, Collections.<Integer>emptyList());
    }
    if (peer.gapFingerprints.size() != transactor.gapFingerprints.size()) {
      return new MethodComparison("DISQUALIFIED", "REACHABLE_ANCHOR_COUNT_DIFFERENCE", peer,
          transactor, Collections.<Integer>emptyList());
    }
    List<Integer> differingGaps = new ArrayList<>();
    for (int i = 0; i < peer.gapFingerprints.size(); i++) {
      if (!peer.gapFingerprints.get(i).equals(transactor.gapFingerprints.get(i))) differingGaps.add(i);
    }
    if (differingGaps.isEmpty()) {
      return new MethodComparison("DISQUALIFIED", "UNACCOUNTED_FULL_CFG_DIFFERENCE", peer,
          transactor, differingGaps);
    }
    return new MethodComparison("DEAD_ONLY", "none", peer, transactor, differingGaps);
  }

  private static void recordComparison(NamespaceStats stats, String role, String methodKey,
                                       int occurrence, MethodComparison comparison,
                                       BufferedWriter methods, BufferedWriter blocks) throws IOException {
    int peerReachable = comparison.peer == null ? 0 : comparison.peer.reachableCount();
    int transactorReachable = comparison.transactor == null ? 0 : comparison.transactor.reachableCount();
    int peerDead = comparison.peer == null ? 0 : comparison.peer.deadCount();
    int transactorDead = comparison.transactor == null ? 0 : comparison.transactor.deadCount();
    if (comparison.relation.startsWith("EXACT")) {
      stats.exactMethods++;
    } else if (comparison.relation.equals("DEAD_ONLY")) {
      stats.deadOnlyMethods++;
      stats.deadBlocks += comparison.differingGaps.size();
      for (int gap : comparison.differingGaps) {
        int peerCount = comparison.peer.gapInstructionCounts.get(gap);
        int transactorCount = comparison.transactor.gapInstructionCounts.get(gap);
        stats.peerDeadInstructions += peerCount;
        stats.transactorDeadInstructions += transactorCount;
        blocks.write(tsv(stats.namespace, role, methodKey, Integer.toString(occurrence),
            Integer.toString(gap), Integer.toString(peerCount), Integer.toString(transactorCount),
            comparison.peer.gapFingerprints.get(gap), comparison.transactor.gapFingerprints.get(gap)));
        blocks.newLine();
      }
    } else {
      stats.disqualifiers.add(comparison.disqualifier);
      if (comparison.disqualifier.equals("REACHABLE_EXCEPTION_TABLE_DIFFERENCE")) {
        stats.exceptionDifferenceMethods++;
      } else if (comparison.disqualifier.equals("REACHABLE_INSTRUCTION_OR_CFG_DIFFERENCE") ||
                 comparison.disqualifier.equals("REACHABLE_ANCHOR_COUNT_DIFFERENCE") ||
                 comparison.disqualifier.equals("CODE_PRESENCE_DIFFERENCE")) {
        stats.reachableDifferenceMethods++;
      } else {
        stats.unaccountedDifferenceMethods++;
      }
    }
    methods.write(tsv(stats.namespace, role, methodKey, Integer.toString(occurrence), comparison.relation,
        Integer.toString(peerReachable), Integer.toString(transactorReachable),
        Integer.toString(peerDead), Integer.toString(transactorDead),
        Integer.toString(comparison.differingGaps.size()),
        comparison.peer == null ? "none" : comparison.peer.reachableInstructionFingerprint,
        comparison.transactor == null ? "none" : comparison.transactor.reachableInstructionFingerprint,
        comparison.peer == null ? "none" : comparison.peer.reachableExceptionFingerprint,
        comparison.transactor == null ? "none" : comparison.transactor.reachableExceptionFingerprint,
        comparison.disqualifier));
    methods.newLine();
  }

  private static void writeMissingMethod(BufferedWriter methods, String namespace, String role,
                                         String methodKey, int occurrence) throws IOException {
    methods.write(tsv(namespace, role, methodKey, Integer.toString(occurrence), "DISQUALIFIED",
        "0", "0", "0", "0", "0", "none", "none", "none", "none", "METHOD_ABI_MISSING"));
    methods.newLine();
  }

  private static Map<String, List<MethodNode>> groupMethods(List<MethodNode> methods) {
    Map<String, List<MethodNode>> result = new TreeMap<>();
    for (MethodNode method : methods) {
      String key = normalize(method.name) + normalize(method.desc) + "@access=" +
          (method.access & METHOD_ACCESS_MASK);
      result.computeIfAbsent(key, ignored -> new ArrayList<>()).add(method);
    }
    for (List<MethodNode> group : result.values()) {
      group.sort(Comparator.comparing((MethodNode value) -> value.name)
          .thenComparing(value -> value.desc)
          .thenComparingInt(value -> value.access));
    }
    return result;
  }

  private static void writeNamespaceSummary(Path summaryPath, Map<String, NamespaceStats> stats,
                                            Map<String, ClassifierRow> classifier,
                                            Map<String, Integer> globalResidualMethods,
                                            Path unlockedPath) throws IOException {
    try (BufferedWriter summary = Files.newBufferedWriter(summaryPath, StandardCharsets.UTF_8);
         BufferedWriter unlocked = Files.newBufferedWriter(unlockedPath, StandardCharsets.UTF_8)) {
      summary.write("namespace\tledger_status\tglobal_class_role_relation\tglobal_method_abi_relation\tglobal_field_relation\tglobal_residual_methods\tpaired_classes\tmissing_class_roles\texact_methods\tproven_dead_only_methods\tproven_dead_residual_blocks\tpeer_dead_residual_instructions\ttransactor_dead_residual_instructions\treachable_difference_methods\texception_difference_methods\tunaccounted_difference_methods\tmissing_methods\tdead_slot_family_relation\trow_unlock\tdisqualifiers\n");
      unlocked.write("namespace\trow_unlock\tproven_dead_only_methods\tproven_dead_residual_blocks\tglobal_field_relation\n");
      for (Map.Entry<String, NamespaceStats> entry : stats.entrySet()) {
        String namespace = entry.getKey();
        NamespaceStats value = entry.getValue();
        ClassifierRow global = classifier.get(namespace);
        if (!global.get("class_role_relation").equals("exact")) value.disqualifiers.add("GLOBAL_CLASS_ROLE_DIFFERENCE");
        if (!global.get("method_abi_relation").equals("exact")) value.disqualifiers.add("GLOBAL_METHOD_ABI_DIFFERENCE");
        if (global.get("field_relation").equals("other-field-delta")) value.disqualifiers.add("GLOBAL_OTHER_FIELD_DELTA");
        boolean executableClean = value.reachableDifferenceMethods == 0 &&
            value.exceptionDifferenceMethods == 0 && value.unaccountedDifferenceMethods == 0 &&
            value.missingMethods == 0 && value.missingClassRoles == 0;
        String familyRelation = value.deadOnlyMethods > 0
            ? (executableClean ? "PROVEN" : "PARTIAL_MIXED_WITH_REACHABLE_OR_ABI_RESIDUAL")
            : "NO_PROVEN_DEAD_RESIDUAL";
        boolean unlock = familyRelation.equals("PROVEN") &&
            global.get("class_role_relation").equals("exact") &&
            global.get("method_abi_relation").equals("exact") &&
            !global.get("field_relation").equals("other-field-delta");
        String rowUnlock;
        if (global.get("ledger_status").equals("RESOLVED")) rowUnlock = unlock ? "ALREADY_RESOLVED" : "NO";
        else rowUnlock = unlock ? "FAMILY_COVERED" : "NO";
        String disqualifiers = value.disqualifiers.isEmpty() ? "none" : String.join(",", value.disqualifiers);
        summary.write(tsv(namespace, global.get("ledger_status"), global.get("class_role_relation"),
            global.get("method_abi_relation"), global.get("field_relation"),
            Integer.toString(globalResidualMethods.getOrDefault(namespace, 0)),
            Integer.toString(value.pairedClasses), Integer.toString(value.missingClassRoles),
            Integer.toString(value.exactMethods), Integer.toString(value.deadOnlyMethods),
            Integer.toString(value.deadBlocks), Integer.toString(value.peerDeadInstructions),
            Integer.toString(value.transactorDeadInstructions),
            Integer.toString(value.reachableDifferenceMethods),
            Integer.toString(value.exceptionDifferenceMethods),
            Integer.toString(value.unaccountedDifferenceMethods), Integer.toString(value.missingMethods),
            familyRelation, rowUnlock, disqualifiers));
        summary.newLine();
        if (!rowUnlock.equals("NO")) {
          unlocked.write(tsv(namespace, rowUnlock, Integer.toString(value.deadOnlyMethods),
              Integer.toString(value.deadBlocks), global.get("field_relation")));
          unlocked.newLine();
        }
      }
    }
  }

  private static void writeSummary(Path path, Map<String, NamespaceStats> stats,
                                   Map<String, ClassifierRow> classifier,
                                   Path unlockedPath) throws IOException {
    Map<String, Integer> claims = new TreeMap<>();
    claims.put("namespace_rows", stats.size());
    int methods = 0;
    int deadMethods = 0;
    int deadBlocks = 0;
    int peerDead = 0;
    int transactorDead = 0;
    int reachableDiff = 0;
    int exceptionDiff = 0;
    for (NamespaceStats value : stats.values()) {
      methods += value.exactMethods + value.deadOnlyMethods + value.reachableDifferenceMethods +
          value.exceptionDifferenceMethods + value.unaccountedDifferenceMethods + value.missingMethods;
      deadMethods += value.deadOnlyMethods;
      deadBlocks += value.deadBlocks;
      peerDead += value.peerDeadInstructions;
      transactorDead += value.transactorDeadInstructions;
      reachableDiff += value.reachableDifferenceMethods;
      exceptionDiff += value.exceptionDifferenceMethods;
    }
    claims.put("method_rows", methods);
    claims.put("proven_dead_only_methods", deadMethods);
    claims.put("proven_dead_residual_blocks", deadBlocks);
    claims.put("peer_dead_residual_instructions", peerDead);
    claims.put("transactor_dead_residual_instructions", transactorDead);
    claims.put("reachable_difference_methods", reachableDiff);
    claims.put("exception_difference_methods", exceptionDiff);
    int unlocked = 0;
    int alreadyResolved = 0;
    List<String> lines = Files.readAllLines(unlockedPath, StandardCharsets.UTF_8);
    for (int i = 1; i < lines.size(); i++) {
      String[] fields = lines.get(i).split("\\t", -1);
      if (fields[1].equals("FAMILY_COVERED")) unlocked++;
      if (fields[1].equals("ALREADY_RESOLVED")) alreadyResolved++;
    }
    claims.put("open_rows_unlocked_family_covered", unlocked);
    claims.put("already_resolved_rows_with_proof", alreadyResolved);
    try (BufferedWriter writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
      writer.write("claim\tvalue\n");
      for (Map.Entry<String, Integer> claim : claims.entrySet()) {
        writer.write(claim.getKey() + TAB + claim.getValue());
        writer.newLine();
      }
    }
  }

  private static void writeNegativeControls(Path path) throws IOException {
    MethodNode positivePeer = new MethodNode(Opcodes.ASM6, Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC,
        "positive", "()V", null, null);
    positivePeer.instructions.add(new InsnNode(Opcodes.RETURN));
    positivePeer.instructions.add(new InsnNode(Opcodes.POP));
    MethodNode positiveTransactor = new MethodNode(Opcodes.ASM6, Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC,
        "positive", "()V", null, null);
    positiveTransactor.instructions.add(new InsnNode(Opcodes.RETURN));
    positiveTransactor.instructions.add(new InsnNode(Opcodes.ATHROW));

    MethodNode predecessorPeer = new MethodNode(Opcodes.ASM6, Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC,
        "predecessor", "()V", null, null);
    LabelNode peerTarget = new LabelNode();
    predecessorPeer.instructions.add(new JumpInsnNode(Opcodes.GOTO, peerTarget));
    predecessorPeer.instructions.add(peerTarget);
    predecessorPeer.instructions.add(new InsnNode(Opcodes.POP));
    predecessorPeer.instructions.add(new InsnNode(Opcodes.RETURN));
    MethodNode predecessorTransactor = new MethodNode(Opcodes.ASM6, Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC,
        "predecessor", "()V", null, null);
    LabelNode transactorTarget = new LabelNode();
    predecessorTransactor.instructions.add(new JumpInsnNode(Opcodes.GOTO, transactorTarget));
    predecessorTransactor.instructions.add(transactorTarget);
    predecessorTransactor.instructions.add(new InsnNode(Opcodes.ATHROW));
    predecessorTransactor.instructions.add(new InsnNode(Opcodes.RETURN));

    MethodNode handlerPeer = handlerControl("handler", Opcodes.POP);
    MethodNode handlerTransactor = handlerControl("handler", Opcodes.ATHROW);

    List<String[]> rows = new ArrayList<>();
    addControl(rows, "unreachable-pop-versus-athrow-positive", compareMethods(positivePeer, positiveTransactor), true);
    addControl(rows, "reachable-predecessor-negative", compareMethods(predecessorPeer, predecessorTransactor), false);
    addControl(rows, "exception-handler-root-negative", compareMethods(handlerPeer, handlerTransactor), false);
    try (BufferedWriter writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
      writer.write("control\texpected_dead_only\tobserved_relation\tresult\n");
      for (String[] row : rows) {
        writer.write(tsv(row));
        writer.newLine();
      }
    }
  }

  private static MethodNode handlerControl(String name, int handlerOpcode) {
    MethodNode method = new MethodNode(Opcodes.ASM6, Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC,
        name, "()V", null, null);
    LabelNode start = new LabelNode();
    LabelNode end = new LabelNode();
    LabelNode handler = new LabelNode();
    method.instructions.add(start);
    method.instructions.add(new InsnNode(Opcodes.RETURN));
    method.instructions.add(end);
    method.instructions.add(handler);
    method.instructions.add(new InsnNode(handlerOpcode));
    method.instructions.add(new InsnNode(Opcodes.RETURN));
    method.tryCatchBlocks.add(new TryCatchBlockNode(start, end, handler, "java/lang/Throwable"));
    return method;
  }

  private static void addControl(List<String[]> rows, String name, MethodComparison comparison,
                                 boolean expectedDeadOnly) {
    boolean observed = comparison.relation.equals("DEAD_ONLY");
    String result = observed == expectedDeadOnly ? "PASS" : "FAIL";
    if (!result.equals("PASS")) {
      throw new IllegalStateException("negative control failed: " + name + " observed " + comparison.relation);
    }
    rows.add(new String[] {name, Boolean.toString(expectedDeadOnly), comparison.relation, result});
  }

  private static ClassNode readClass(JarFile jar, String internalName) throws IOException {
    JarEntry entry = jar.getJarEntry(internalName + ".class");
    if (entry == null) throw new IOException("missing class " + internalName + " in " + jar.getName());
    ClassNode node = new ClassNode(Opcodes.ASM6);
    try (java.io.InputStream input = jar.getInputStream(entry)) {
      new ClassReader(input).accept(node, ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
    }
    return node;
  }

  private static Map<String, InventoryEntry> readInventory(Path path) throws IOException {
    Map<String, InventoryEntry> result = new TreeMap<>();
    for (String line : Files.readAllLines(path, StandardCharsets.UTF_8)) {
      if (line.isEmpty()) continue;
      String[] fields = line.split("\\t", -1);
      if (fields.length != 3) throw new IOException("invalid inventory row: " + line);
      InventoryEntry entry = new InventoryEntry(fields[0], fields[1], fields[2]);
      String key = fields[0] + TAB + fields[1];
      if (result.put(key, entry) != null) throw new IOException("duplicate inventory key: " + key);
    }
    return result;
  }

  private static Map<String, ClassifierRow> readClassifier(Path path) throws IOException {
    List<String> lines = Files.readAllLines(path, StandardCharsets.UTF_8);
    if (lines.isEmpty()) throw new IOException("empty classifier: " + path);
    String[] header = lines.get(0).split("\\t", -1);
    Map<String, ClassifierRow> result = new TreeMap<>();
    for (int i = 1; i < lines.size(); i++) {
      String[] fields = lines.get(i).split("\\t", -1);
      if (fields.length != header.length) throw new IOException("invalid classifier row " + (i + 1));
      Map<String, String> values = new LinkedHashMap<>();
      for (int j = 0; j < header.length; j++) values.put(header[j], fields[j]);
      result.put(fields[0], new ClassifierRow(values));
    }
    return result;
  }

  private static Map<String, Integer> readGlobalResidualCounts(Path path) throws IOException {
    Map<String, Integer> result = new HashMap<>();
    List<String> lines = Files.readAllLines(path, StandardCharsets.UTF_8);
    for (int i = 1; i < lines.size(); i++) {
      String[] fields = lines.get(i).split("\\t", -1);
      if (fields.length >= 9 && fields[8].equals("residual")) result.merge(fields[0], 1, Integer::sum);
    }
    return result;
  }

  private static String semanticToken(AbstractInsnNode instruction) {
    int opcode = instruction.getOpcode();
    String mnemonic = opcode >= 0 && opcode < Printer.OPCODES.length
        ? Printer.OPCODES[opcode].toLowerCase(Locale.ROOT) : "opcode-" + opcode;
    if (instruction instanceof InsnNode) return mnemonic;
    if (instruction instanceof IntInsnNode) return mnemonic + ":" + ((IntInsnNode) instruction).operand;
    if (instruction instanceof VarInsnNode) return mnemonic + ":" + ((VarInsnNode) instruction).var;
    if (instruction instanceof TypeInsnNode) return mnemonic + ":" + normalize(((TypeInsnNode) instruction).desc);
    if (instruction instanceof FieldInsnNode) {
      FieldInsnNode value = (FieldInsnNode) instruction;
      return mnemonic + ":" + normalize(value.owner) + "." + normalize(value.name) + normalize(value.desc);
    }
    if (instruction instanceof MethodInsnNode) {
      MethodInsnNode value = (MethodInsnNode) instruction;
      return mnemonic + ":" + normalize(value.owner) + "." + normalize(value.name) +
          normalize(value.desc) + ":itf=" + value.itf;
    }
    if (instruction instanceof InvokeDynamicInsnNode) {
      InvokeDynamicInsnNode value = (InvokeDynamicInsnNode) instruction;
      StringBuilder token = new StringBuilder(mnemonic).append(':').append(normalize(value.name))
          .append(normalize(value.desc)).append(':').append(normalizeConstant(value.bsm));
      for (Object argument : value.bsmArgs) token.append(':').append(normalizeConstant(argument));
      return token.toString();
    }
    if (instruction instanceof JumpInsnNode) return mnemonic;
    if (instruction instanceof LdcInsnNode) {
      return mnemonic + ":" + normalizeConstant(((LdcInsnNode) instruction).cst);
    }
    if (instruction instanceof IincInsnNode) {
      IincInsnNode value = (IincInsnNode) instruction;
      return mnemonic + ":" + value.var + ":" + value.incr;
    }
    if (instruction instanceof TableSwitchInsnNode) {
      TableSwitchInsnNode value = (TableSwitchInsnNode) instruction;
      return mnemonic + ":" + value.min + ":" + value.max;
    }
    if (instruction instanceof LookupSwitchInsnNode) {
      LookupSwitchInsnNode value = (LookupSwitchInsnNode) instruction;
      return mnemonic + ":" + value.keys.toString();
    }
    if (instruction instanceof MultiANewArrayInsnNode) {
      MultiANewArrayInsnNode value = (MultiANewArrayInsnNode) instruction;
      return mnemonic + ":" + normalize(value.desc) + ":" + value.dims;
    }
    if (instruction instanceof LabelNode || instruction instanceof LineNumberNode || instruction instanceof FrameNode) {
      throw new IllegalArgumentException("non-executable instruction passed to semanticToken");
    }
    return mnemonic + ":" + normalize(instruction.toString());
  }

  private static String normalizeConstant(Object value) {
    if (value == null) return "null";
    if (value instanceof Type) return "Type(" + normalize(((Type) value).getDescriptor()) + ")";
    if (value instanceof Handle) {
      Handle handle = (Handle) value;
      return "Handle(" + handle.getTag() + "," + normalize(handle.getOwner()) + "," +
          normalize(handle.getName()) + "," + normalize(handle.getDesc()) + "," + handle.isInterface() + ")";
    }
    return value.getClass().getName() + "(" + normalize(String.valueOf(value)) + ")";
  }

  private static boolean isTerminal(int opcode) {
    return (opcode >= Opcodes.IRETURN && opcode <= Opcodes.RETURN) ||
        opcode == Opcodes.ATHROW || opcode == Opcodes.RET;
  }

  private static String normalize(String value) {
    String result = GENERATED_ID.matcher(value).replaceAll("__ID");
    return GENERATED_HASH.matcher(result).replaceAll("\\$HASH");
  }

  private static String nullToEmpty(String value) {
    return value == null ? "" : value;
  }

  private static void appendRecord(StringBuilder builder, String value) {
    builder.append(value.length()).append(':').append(value).append(';');
  }

  private static String sha256(String value) {
    try {
      MessageDigest digest = MessageDigest.getInstance("SHA-256");
      byte[] bytes = digest.digest(value.getBytes(StandardCharsets.UTF_8));
      StringBuilder result = new StringBuilder();
      for (byte valueByte : bytes) result.append(String.format(Locale.ROOT, "%02x", valueByte & 0xff));
      return result.toString();
    } catch (NoSuchAlgorithmException impossible) {
      throw new AssertionError(impossible);
    }
  }

  private static String tsv(String... fields) {
    return String.join(TAB, fields);
  }
}
