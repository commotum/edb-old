import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Builds and verifies the version-pinned classpath boundary for the Stage 1
 * Transactor structural-load gate.
 *
 * This deliberately does not load Datomic classes.  It inventories the clean
 * candidate inputs first, so a later JVM cannot accidentally pass by resolving
 * an initializer from the licensed Peer, Transactor, or core2 artifacts.
 */
public final class ScanStructuralClasspath {
  private static final String CORE2_SHA =
      "81fdf81586c7be1a4b61655b568348d12db7892cb4562d0db7529bbc7af8a94b";
  private static final String QUERY_SUPPORT_SHA =
      "086973461a46353cb10f6f4a9bfff9e083bd05f3754494069acf6f0a7651f84e";
  private static final String NANO_IMPL_SHA =
      "fbde8184da356bd3a9995a9f05f01493efe4baa87c4d48377cda3e53331807cd";
  private static final String SANITIZED_NANO_IMPL_SHA =
      "08f9699d6e35b9e052ec85ee15e0896b3a685c74e6e226d8cd89d9c61dbf8d5f";
  private static final Set<String> FORBIDDEN_JKS_SHAS = new TreeSet<String>();

  static {
    FORBIDDEN_JKS_SHAS.add(
        "f3627f52580b84fe8f536423643fb46999fd2458498989307f2820d778eb73a1");
    FORBIDDEN_JKS_SHAS.add(
        "ca64f839d051d909974a624b4345d83cc739a0b90edbe54e7c3605a2fd8fc13b");
  }

  private static final int EXPECTED_TRANSACTOR_NAMESPACES = 247;
  private static final int EXPECTED_CORE2_NAMESPACES = 25;
  private static final int EXPECTED_TRANSACTOR_CLASSES = 9682;
  private static final int EXPECTED_CORE2_CLASSES = 510;
  private static final int EXPECTED_JAVA_SOURCES = 46;
  private static final int EXPECTED_JAVA_CLASSES = 52;
  private static final int EXPECTED_ORACLE_DEPENDENCIES = 533;
  private static final int EXPECTED_CANDIDATE_DEPENDENCIES = 532;
  private static final int EXPECTED_DEPENDENCY_ENTRIES = 191870;
  private static final int EXPECTED_DISTINCT_DEPENDENCY_CLASSES = 181555;
  private static final int EXPECTED_DUPLICATE_CLASSES = 35;
  private static final int EXPECTED_IDENTICAL_DUPLICATE_CLASSES = 14;
  private static final int EXPECTED_DIFFERING_DUPLICATE_CLASSES = 21;
  private static final int EXPECTED_SOURCE_COLLISIONS = 87;

  private static final class Source {
    final String owner;
    final String namespace;
    final String initializerNamespace;
    final String entry;
    final String sha256;

    Source(String owner, String namespace, String initializerNamespace,
           String entry, String sha256) {
      this.owner = owner;
      this.namespace = namespace;
      this.initializerNamespace = initializerNamespace;
      this.entry = entry;
      this.sha256 = sha256;
    }
  }

  private static final class JarInput {
    final int ordinal;
    final String name;
    final String relativePath;
    final String sha256;
    final Path path;

    JarInput(int ordinal, String name, String relativePath, String sha256, Path path) {
      this.ordinal = ordinal;
      this.name = name;
      this.relativePath = relativePath;
      this.sha256 = sha256;
      this.path = path;
    }
  }

  private static final class ClassHit {
    final JarInput jar;
    final String sha256;

    ClassHit(JarInput jar, String sha256) {
      this.jar = jar;
      this.sha256 = sha256;
    }
  }

  private ScanStructuralClasspath() {}

  private static void require(boolean condition, String message) {
    if (!condition) {
      throw new IllegalStateException(message);
    }
  }

  private static String sha256(InputStream input) throws Exception {
    MessageDigest digest = MessageDigest.getInstance("SHA-256");
    byte[] buffer = new byte[65536];
    for (int read; (read = input.read(buffer)) >= 0; ) {
      if (read != 0) {
        digest.update(buffer, 0, read);
      }
    }
    StringBuilder result = new StringBuilder();
    for (byte value : digest.digest()) {
      result.append(String.format("%02x", value & 0xff));
    }
    return result.toString();
  }

  private static String sha256(Path path) throws Exception {
    try (InputStream input = Files.newInputStream(path)) {
      return sha256(input);
    }
  }

  private static List<String[]> readTsv(Path path) throws IOException {
    List<String[]> rows = new ArrayList<String[]>();
    try (BufferedReader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
      String header = reader.readLine();
      require(header != null, "empty TSV: " + path);
      for (String line; (line = reader.readLine()) != null; ) {
        if (!line.isEmpty()) {
          rows.add(line.split("\\t", -1));
        }
      }
    }
    return rows;
  }

  private static void write(Path path, List<String> rows) throws IOException {
    Files.write(path, rows, StandardCharsets.UTF_8);
  }

  private static Source sourceForInit(Path sourceRoot, String owner,
                                      String initializerNamespace, String initEntry)
      throws Exception {
    String suffix = "__init.class";
    require(initEntry.endsWith(suffix), "not an initializer: " + initEntry);
    String stem = initEntry.substring(0, initEntry.length() - suffix.length());
    Path clj = sourceRoot.resolve(stem + ".clj");
    Path cljc = sourceRoot.resolve(stem + ".cljc");
    int matches = (Files.isRegularFile(clj) ? 1 : 0) + (Files.isRegularFile(cljc) ? 1 : 0);
    require(matches == 1, "expected exactly one source for " + initializerNamespace
        + ", found " + matches);
    Path source = Files.isRegularFile(clj) ? clj : cljc;
    String entry = sourceRoot.relativize(source).toString().replace('\\', '/');
    String namespace = declaredNamespace(source);
    String expectedStem = namespace.replace('.', '/').replace('-', '_');
    require(stem.equals(expectedStem), "declared namespace " + namespace
        + " does not munge to initializer path " + stem);
    return new Source(owner, namespace, initializerNamespace, entry, sha256(source));
  }

  private static final Pattern IN_NS = Pattern.compile(
      "\\((?:clojure\\.core/)?in-ns\\s+(?:\\(\\.withMeta\\s+)?'([^\\s\\)]+)");
  private static final Pattern NS_START = Pattern.compile(
      "(?m)^\\s*\\(ns(?=\\s|\\^)");

  private static int skipReaderSpace(String text, int start) {
    int index = start;
    while (index < text.length()) {
      char value = text.charAt(index);
      if (Character.isWhitespace(value) || value == ',') {
        index++;
      } else if (value == ';') {
        while (index < text.length() && text.charAt(index) != '\n') {
          index++;
        }
      } else {
        break;
      }
    }
    return index;
  }

  private static int skipBalancedMap(String text, int start) {
    int depth = 0;
    boolean inString = false;
    boolean escaped = false;
    for (int index = start; index < text.length(); index++) {
      char value = text.charAt(index);
      if (inString) {
        if (escaped) {
          escaped = false;
        } else if (value == '\\') {
          escaped = true;
        } else if (value == '"') {
          inString = false;
        }
      } else if (value == '"') {
        inString = true;
      } else if (value == '{') {
        depth++;
      } else if (value == '}' && --depth == 0) {
        return index + 1;
      }
    }
    throw new IllegalStateException("unterminated namespace metadata map");
  }

  private static boolean readerDelimiter(char value) {
    return Character.isWhitespace(value) || value == ',' || value == '('
        || value == ')' || value == '[' || value == ']' || value == '{'
        || value == '}';
  }

  private static int skipToken(String text, int start) {
    int index = start;
    while (index < text.length() && !readerDelimiter(text.charAt(index))) {
      index++;
    }
    return index;
  }

  private static String declaredNamespace(Path source) throws Exception {
    String text = new String(Files.readAllBytes(source), StandardCharsets.UTF_8);
    Matcher inNs = IN_NS.matcher(text);
    if (inNs.find()) {
      return inNs.group(1);
    }
    Matcher ns = NS_START.matcher(text);
    require(ns.find(), "source has neither in-ns nor ns form: " + source);
    int index = skipReaderSpace(text, ns.end());
    while (index < text.length() && text.charAt(index) == '^') {
      index = skipReaderSpace(text, index + 1);
      if (index < text.length() && text.charAt(index) == '{') {
        index = skipBalancedMap(text, index);
      } else {
        index = skipToken(text, index);
      }
      index = skipReaderSpace(text, index);
    }
    int end = skipToken(text, index);
    require(end > index, "could not read declared namespace: " + source);
    return text.substring(index, end);
  }

  private static String join(List<String> values, String delimiter) {
    StringBuilder out = new StringBuilder();
    for (String value : values) {
      if (out.length() != 0) {
        out.append(delimiter);
      }
      out.append(value);
    }
    return out.toString();
  }

  public static void main(String[] args) throws Exception {
    if (args.length != 11) {
      throw new IllegalArgumentException(
          "usage: transactor-namespaces.tsv transactor-classes.tsv "
          + "peer-namespaces.tsv distribution-jars.tsv datomic-home "
          + "transactor-src peer-src java-src java-class-list "
          + "sanitized-nano-impl.jar output-dir");
    }

    Path transactorNamespaces = Paths.get(args[0]);
    Path transactorClasses = Paths.get(args[1]);
    Path peerNamespaces = Paths.get(args[2]);
    Path distributionJars = Paths.get(args[3]);
    Path datomicHome = Paths.get(args[4]).toRealPath();
    Path transactorSourceRoot = Paths.get(args[5]).toRealPath();
    Path peerSourceRoot = Paths.get(args[6]).toRealPath();
    Path javaSourceRoot = Paths.get(args[7]).toRealPath();
    Path javaClassList = Paths.get(args[8]);
    Path sanitizedNanoImpl = Paths.get(args[9]).toRealPath();
    Path output = Paths.get(args[10]);
    Files.createDirectories(output);

    LinkedHashMap<String, Source> sources = new LinkedHashMap<String, Source>();
    List<String> transactorNamespaceNames = new ArrayList<String>();
    for (String[] row : readTsv(transactorNamespaces)) {
      require(row.length >= 2, "malformed Transactor namespace row");
      Source source = sourceForInit(transactorSourceRoot, "transactor", row[0], row[1]);
      require(!sources.containsKey(source.entry), "duplicate Transactor source: " + source.entry);
      sources.put(source.entry, source);
      transactorNamespaceNames.add(source.namespace);
    }
    require(sources.size() == EXPECTED_TRANSACTOR_NAMESPACES,
        "expected 247 Transactor sources, found " + sources.size());

    List<String> core2NamespaceNames = new ArrayList<String>();
    for (String[] row : readTsv(peerNamespaces)) {
      require(row.length >= 2, "malformed Peer namespace row");
      if (!row[0].equals("datomic.core2") && !row[0].startsWith("datomic.core2.")) {
        continue;
      }
      Path sourcePath = peerSourceRoot.resolve(row[1]);
      require(Files.isRegularFile(sourcePath), "missing Peer core2 source: " + row[1]);
      Source source = new Source("peer-core2", row[0], row[0], row[1], sha256(sourcePath));
      require(!sources.containsKey(source.entry), "core2 overlaps Transactor source: " + source.entry);
      sources.put(source.entry, source);
      core2NamespaceNames.add(row[0]);
    }
    require(core2NamespaceNames.size() == EXPECTED_CORE2_NAMESPACES,
        "expected 25 Peer core2 sources, found " + core2NamespaceNames.size());
    require(sources.size() == EXPECTED_TRANSACTOR_NAMESPACES + EXPECTED_CORE2_NAMESPACES,
        "candidate source closure is not exactly 272 files");

    Set<String> transactorClassEntries = new TreeSet<String>();
    for (String[] row : readTsv(transactorClasses)) {
      require(row.length >= 1, "malformed Transactor class row");
      require(transactorClassEntries.add(row[0]), "duplicate Transactor class: " + row[0]);
    }
    require(transactorClassEntries.size() == EXPECTED_TRANSACTOR_CLASSES,
        "expected 9682 Transactor classes, found " + transactorClassEntries.size());

    List<JarInput> oracleDependencies = new ArrayList<JarInput>();
    JarInput core2 = null;
    int dependencyOrdinal = 0;
    for (String[] row : readTsv(distributionJars)) {
      require(row.length >= 12, "malformed distribution JAR row");
      if (!"dependency".equals(row[11])) {
        continue;
      }
      Path jarPath = datomicHome.resolve(row[1]).normalize().toRealPath();
      require(jarPath.startsWith(datomicHome.resolve("lib")),
          "dependency escaped lib/: " + jarPath);
      String actualSha = sha256(jarPath);
      require(actualSha.equals(row[2]), "dependency hash mismatch: " + row[1]);
      JarInput input = new JarInput(++dependencyOrdinal, row[0], row[1], row[2], jarPath);
      oracleDependencies.add(input);
      if (row[0].startsWith("core2-") || CORE2_SHA.equals(row[2])) {
        require(core2 == null, "multiple core2 dependencies");
        require(CORE2_SHA.equals(row[2]), "unexpected core2 hash: " + row[2]);
        core2 = input;
      }
    }
    require(oracleDependencies.size() == EXPECTED_ORACLE_DEPENDENCIES,
        "expected 533 dependencies, found " + oracleDependencies.size());
    require(core2 != null, "core2 dependency not found");
    require(Files.size(sanitizedNanoImpl) == 81218,
        "sanitized nano-impl size is not 81218 bytes");
    require(sha256(sanitizedNanoImpl).equals(SANITIZED_NANO_IMPL_SHA),
        "sanitized nano-impl hash mismatch");

    Set<String> core2ClassEntries = new TreeSet<String>();
    try (ZipFile zip = new ZipFile(core2.path.toFile())) {
      Enumeration<? extends ZipEntry> entries = zip.entries();
      while (entries.hasMoreElements()) {
        ZipEntry entry = entries.nextElement();
        if (!entry.isDirectory() && entry.getName().endsWith(".class")) {
          core2ClassEntries.add(entry.getName());
        }
      }
    }
    require(core2ClassEntries.size() == EXPECTED_CORE2_CLASSES,
        "expected 510 core2 classes, found " + core2ClassEntries.size());
    Set<String> candidateClassEntries = new TreeSet<String>(transactorClassEntries);
    candidateClassEntries.addAll(core2ClassEntries);
    require(candidateClassEntries.size()
            == EXPECTED_TRANSACTOR_CLASSES + EXPECTED_CORE2_CLASSES,
        "Transactor and core2 class closures overlap unexpectedly");

    List<Path> javaSources = new ArrayList<Path>();
    Files.walk(javaSourceRoot).forEach(path -> {
      if (Files.isRegularFile(path) && path.toString().endsWith(".java")) {
        javaSources.add(path);
      }
    });
    Collections.sort(javaSources);
    require(javaSources.size() == EXPECTED_JAVA_SOURCES,
        "expected 46 Java sources, found " + javaSources.size());
    List<String> javaClasses = new ArrayList<String>();
    for (String line : Files.readAllLines(javaClassList, StandardCharsets.UTF_8)) {
      if (!line.trim().isEmpty() && !line.startsWith("#")) {
        javaClasses.add(line);
      }
    }
    require(javaClasses.size() == EXPECTED_JAVA_CLASSES,
        "expected 52 Java classes, found " + javaClasses.size());
    require(new LinkedHashSet<String>(javaClasses).size() == javaClasses.size(),
        "duplicate Java class-list entries");
    for (String javaClass : javaClasses) {
      require(transactorClassEntries.contains(javaClass),
          "Java class outside Transactor closure: " + javaClass);
    }

    List<JarInput> candidateDependencies = new ArrayList<JarInput>();
    int nanoSubstitutions = 0;
    for (JarInput dependency : oracleDependencies) {
      if (dependency != core2) {
        if (NANO_IMPL_SHA.equals(dependency.sha256)) {
          candidateDependencies.add(new JarInput(
              dependency.ordinal, "nano-impl-0.1.325-sanitized.jar",
              dependency.relativePath, SANITIZED_NANO_IMPL_SHA, sanitizedNanoImpl));
          nanoSubstitutions++;
        } else {
          candidateDependencies.add(dependency);
        }
      }
    }
    require(candidateDependencies.size() == EXPECTED_CANDIDATE_DEPENDENCIES,
        "expected 532 candidate dependencies, found " + candidateDependencies.size());
    require(nanoSubstitutions == 1,
        "expected exactly one sanitized nano-impl substitution, found " + nanoSubstitutions);

    List<String> classCollisions = new ArrayList<String>();
    classCollisions.add("class_entry\tcandidate_owner\tdependency_ordinal\tdependency_jar"
        + "\tdependency_jar_sha256\tentry_sha256");
    List<String> sourceCollisions = new ArrayList<String>();
    sourceCollisions.add("source_entry\tcandidate_owner\tnamespace\tcandidate_sha256"
        + "\tdependency_ordinal\tdependency_jar\tdependency_jar_sha256"
        + "\tdependency_entry_sha256\tbyte_relation");
    List<String> jdbcServices = new ArrayList<String>();
    jdbcServices.add("resource\tdependency_ordinal\tdependency_jar"
        + "\tdependency_jar_sha256\tentry_sha256");
    List<String> jksResources = new ArrayList<String>();
    jksResources.add("resource\tdependency_ordinal\tdependency_jar"
        + "\tdependency_jar_sha256\tentry_sha256\tforbidden_vendor_hash");
    Map<String, List<ClassHit>> dependencyClasses =
        new TreeMap<String, List<ClassHit>>();
    int dependencyEntryCount = 0;
    int rootDataReaders = 0;
    int forbiddenJks = 0;
    boolean querySupportPresent = false;

    for (JarInput dependency : candidateDependencies) {
      if (QUERY_SUPPORT_SHA.equals(dependency.sha256)) {
        querySupportPresent = true;
      }
      try (ZipFile zip = new ZipFile(dependency.path.toFile())) {
        Enumeration<? extends ZipEntry> entries = zip.entries();
        while (entries.hasMoreElements()) {
          ZipEntry entry = entries.nextElement();
          dependencyEntryCount++;
          if (entry.isDirectory()) {
            continue;
          }
          String name = entry.getName();
          boolean isClass = name.endsWith(".class");
          boolean candidateClassCollision = isClass && candidateClassEntries.contains(name);
          Source candidateSource = sources.get(name);
          boolean jdbcService = name.equals("META-INF/services/java.sql.Driver");
          if (name.equals("data_readers.clj")) {
            rootDataReaders++;
          }
          boolean jksResource = name.endsWith(".jks");
          if (!isClass && candidateSource == null && !jdbcService && !jksResource) {
            continue;
          }
          String entrySha;
          try (InputStream input = zip.getInputStream(entry)) {
            entrySha = sha256(input);
          }
          if (isClass) {
            List<ClassHit> hits = dependencyClasses.get(name);
            if (hits == null) {
              hits = new ArrayList<ClassHit>();
              dependencyClasses.put(name, hits);
            }
            hits.add(new ClassHit(dependency, entrySha));
          }
          if (candidateClassCollision) {
            String owner = transactorClassEntries.contains(name) ? "transactor" : "peer-core2";
            classCollisions.add(name + "\t" + owner + "\t" + dependency.ordinal
                + "\t" + dependency.path + "\t" + dependency.sha256
                + "\t" + entrySha);
          }
          if (candidateSource != null) {
            sourceCollisions.add(name + "\t" + candidateSource.owner + "\t"
                + candidateSource.namespace + "\t" + candidateSource.sha256 + "\t"
                + dependency.ordinal + "\t" + dependency.path + "\t"
                + dependency.sha256 + "\t" + entrySha + "\t"
                + (candidateSource.sha256.equals(entrySha) ? "MATCH" : "DIFFER"));
          }
          if (jdbcService) {
            jdbcServices.add(name + "\t" + dependency.ordinal + "\t"
                + dependency.path + "\t" + dependency.sha256 + "\t" + entrySha);
          }
          if (jksResource) {
            boolean forbidden = FORBIDDEN_JKS_SHAS.contains(entrySha);
            if (forbidden) {
              forbiddenJks++;
            }
            jksResources.add(name + "\t" + dependency.ordinal + "\t"
                + dependency.path + "\t" + dependency.sha256 + "\t" + entrySha
                + "\t" + forbidden);
          }
        }
      }
    }

    List<String> duplicateClasses = new ArrayList<String>();
    duplicateClasses.add("class_entry\towner_count\tdistinct_sha256_count"
        + "\tbyte_relation\tordered_owners\tsha256_by_owner");
    int duplicateCount = 0;
    int identicalDuplicateCount = 0;
    int differingDuplicateCount = 0;
    int maxOwners = 0;
    for (Map.Entry<String, List<ClassHit>> entry : dependencyClasses.entrySet()) {
      List<ClassHit> hits = entry.getValue();
      if (hits.size() < 2) {
        continue;
      }
      duplicateCount++;
      maxOwners = Math.max(maxOwners, hits.size());
      Set<String> distinctShas = new TreeSet<String>();
      List<String> owners = new ArrayList<String>();
      List<String> shaByOwner = new ArrayList<String>();
      for (ClassHit hit : hits) {
        distinctShas.add(hit.sha256);
        owners.add(hit.jar.ordinal + ":" + hit.jar.path);
        shaByOwner.add(hit.jar.ordinal + ":" + hit.jar.path + "=" + hit.sha256);
      }
      boolean identical = distinctShas.size() == 1;
      if (identical) {
        identicalDuplicateCount++;
      } else {
        differingDuplicateCount++;
      }
      duplicateClasses.add(entry.getKey() + "\t" + hits.size() + "\t"
          + distinctShas.size() + "\t" + (identical ? "IDENTICAL" : "DIFFER")
          + "\t" + join(owners, ";") + "\t" + join(shaByOwner, ";"));
    }

    require(dependencyEntryCount == EXPECTED_DEPENDENCY_ENTRIES,
        "expected 191870 dependency entries, found " + dependencyEntryCount);
    require(dependencyClasses.size() == EXPECTED_DISTINCT_DEPENDENCY_CLASSES,
        "expected 181555 distinct dependency classes, found " + dependencyClasses.size());
    require(classCollisions.size() == 1,
        "candidate class collision count is " + (classCollisions.size() - 1));
    require(sourceCollisions.size() - 1 == EXPECTED_SOURCE_COLLISIONS,
        "expected 87 source collisions, found " + (sourceCollisions.size() - 1));
    for (int index = 1; index < sourceCollisions.size(); index++) {
      require(sourceCollisions.get(index).endsWith("\tMATCH"),
          "differing candidate/dependency source collision: " + sourceCollisions.get(index));
    }
    require(duplicateCount == EXPECTED_DUPLICATE_CLASSES,
        "expected 35 dependency duplicate classes, found " + duplicateCount);
    require(identicalDuplicateCount == EXPECTED_IDENTICAL_DUPLICATE_CLASSES,
        "expected 14 identical duplicate classes, found " + identicalDuplicateCount);
    require(differingDuplicateCount == EXPECTED_DIFFERING_DUPLICATE_CLASSES,
        "expected 21 differing duplicate classes, found " + differingDuplicateCount);
    require(maxOwners == 5, "expected dependency duplicate max owner count 5, found " + maxOwners);
    require(rootDataReaders == 0,
        "a dependency unexpectedly owns root data_readers.clj");
    require(forbiddenJks == 0,
        "a candidate dependency contains a forbidden vendor JKS payload");
    require(querySupportPresent, "pinned query-support dependency is absent");
    require(jdbcServices.size() == 3,
        "expected H2 and PostgreSQL JDBC services, found " + (jdbcServices.size() - 1));

    List<String> candidateSourceRows = new ArrayList<String>();
    candidateSourceRows.add("owner\tnamespace\tsource_entry\tsha256\tinitializer_namespace");
    for (Source source : sources.values()) {
      candidateSourceRows.add(source.owner + "\t" + source.namespace + "\t"
          + source.entry + "\t" + source.sha256 + "\t" + source.initializerNamespace);
    }
    write(output.resolve("candidate-sources.tsv"), candidateSourceRows);
    write(output.resolve("transactor-namespaces.txt"), transactorNamespaceNames);
    write(output.resolve("peer-core2-namespaces.txt"), core2NamespaceNames);

    List<String> javaSourceRows = new ArrayList<String>();
    javaSourceRows.add("source_entry\tsha256");
    for (Path source : javaSources) {
      javaSourceRows.add(javaSourceRoot.relativize(source).toString().replace('\\', '/')
          + "\t" + sha256(source));
    }
    write(output.resolve("java-sources.tsv"), javaSourceRows);
    write(output.resolve("java-classes.txt"), javaClasses);

    List<String> candidateDependencyRows = new ArrayList<String>();
    candidateDependencyRows.add("candidate_ordinal\tdistribution_ordinal\tjar"
        + "\tartifact_path\tsha256\tsource_distribution_path\tsubstitution");
    int candidateOrdinal = 0;
    for (JarInput dependency : candidateDependencies) {
      candidateDependencyRows.add((++candidateOrdinal) + "\t" + dependency.ordinal + "\t"
          + dependency.name + "\t" + dependency.path + "\t" + dependency.sha256
          + "\t" + dependency.relativePath + "\t"
          + (SANITIZED_NANO_IMPL_SHA.equals(dependency.sha256)
              ? "sanitized-nano-impl" : "none"));
    }
    write(output.resolve("ordered-candidate-dependencies.tsv"), candidateDependencyRows);

    List<String> oracleDependencyRows = new ArrayList<String>();
    oracleDependencyRows.add("distribution_ordinal\tjar\tdistribution_path\tsha256"
        + "\tcandidate_allowed");
    for (JarInput dependency : oracleDependencies) {
      oracleDependencyRows.add(dependency.ordinal + "\t" + dependency.name + "\t"
          + dependency.relativePath + "\t" + dependency.sha256 + "\t"
          + (dependency == core2 ? "false" : "true"));
    }
    write(output.resolve("ordered-oracle-dependencies.tsv"), oracleDependencyRows);
    write(output.resolve("candidate-class-collisions.tsv"), classCollisions);
    write(output.resolve("candidate-source-collisions.tsv"), sourceCollisions);
    write(output.resolve("dependency-class-duplicates.tsv"), duplicateClasses);
    write(output.resolve("jdbc-driver-services.tsv"), jdbcServices);
    write(output.resolve("dependency-jks-resources.tsv"), jksResources);

    List<String> summary = new ArrayList<String>();
    summary.add("status=PASS");
    summary.add("transactor.namespace.count=" + transactorNamespaceNames.size());
    summary.add("peer.core2.namespace.count=" + core2NamespaceNames.size());
    summary.add("candidate.source.count=" + sources.size());
    summary.add("transactor.class.closure.count=" + transactorClassEntries.size());
    summary.add("peer.core2.class.closure.count=" + core2ClassEntries.size());
    summary.add("transactor.java.source.count=" + javaSources.size());
    summary.add("transactor.java.class.count=" + javaClasses.size());
    summary.add("oracle.dependency.jar.count=" + oracleDependencies.size());
    summary.add("candidate.dependency.jar.count=" + candidateDependencies.size());
    summary.add("candidate.sanitized.nano-impl.substitution.count=" + nanoSubstitutions);
    summary.add("candidate.dependency.entry.count=" + dependencyEntryCount);
    summary.add("candidate.dependency.distinct.class.count=" + dependencyClasses.size());
    summary.add("candidate.class.collision.count=" + (classCollisions.size() - 1));
    summary.add("candidate.source.collision.count=" + (sourceCollisions.size() - 1));
    summary.add("candidate.source.collision.differing.count=0");
    summary.add("dependency.duplicate.class.count=" + duplicateCount);
    summary.add("dependency.duplicate.class.identical.count=" + identicalDuplicateCount);
    summary.add("dependency.duplicate.class.differing.count=" + differingDuplicateCount);
    summary.add("dependency.duplicate.class.max.owner.count=" + maxOwners);
    summary.add("candidate.root.data-readers.dependency.owner.count=" + rootDataReaders);
    summary.add("candidate.forbidden.datomic-jks.dependency.owner.count=" + forbiddenJks);
    summary.add("candidate.query-support.dependency.present=" + querySupportPresent);
    summary.add("candidate.jdbc-driver.service.count=" + (jdbcServices.size() - 1));
    summary.add("original.peer.on.candidate.classpath=false");
    summary.add("original.transactor.on.candidate.classpath=false");
    summary.add("original.core2.aot.on.candidate.classpath=false");
    write(output.resolve("summary.properties"), summary);

    System.out.println("structural classpath discovery: PASS");
    for (String line : summary) {
      System.out.println(line);
    }
  }
}
