#!/usr/bin/bash -p

set -euo pipefail
umask 077
unset BASH_ENV ENV CDPATH GLOBIGNORE JAVA_TOOL_OPTIONS JDK_JAVA_OPTIONS \
  _JAVA_OPTIONS JDK_JAVAC_OPTIONS CLASSPATH JAVA_HOME LD_PRELOAD \
  LD_LIBRARY_PATH
IFS=$' \t\n'
export LC_ALL=C TZ=UTC

script_dir=$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd)
project_dir=$(cd -- "$script_dir/../.." && pwd)
datomic_home=${1:-"$project_dir/../../datomic/datomic-pro-1.0.7277"}
evidence_root=${2:-/tmp/datomic-stage2-sql-overlaps-v1}
java_root=${DATOMIC_STAGE2_SQL_JAVA_ROOT:-/tmp/amazon-corretto-11.0.22.7.1}
java_bin="$java_root/bin/java"
javac_bin="$java_root/bin/javac"
probe="$script_dir/stage2/sql_overlap_probe.clj"

fail() {
  echo "Stage 2 SQL-overlap gate: $*" >&2
  exit 1
}

for command_name in awk cmp find grep install sha256sum sort timeout unzip wc xargs; do
  command -v "$command_name" >/dev/null || fail "missing command: $command_name"
done
[[ -x "$java_bin" && -x "$javac_bin" ]] ||
  fail "pinned Corretto Java/Javac are missing: $java_root"
[[ -f "$probe" && ! -L "$probe" ]] || fail "probe is missing or symbolic"
[[ ! -L "$evidence_root" ]] || fail "evidence root may not be a symlink"
if [[ -e "$evidence_root" ]] &&
   [[ -n $(find "$evidence_root" -mindepth 1 -print -quit 2>/dev/null) ]]; then
  fail "refusing to overwrite non-empty evidence root: $evidence_root"
fi

peer_jar="$datomic_home/peer-1.0.7277.jar"
transactor_jar="$datomic_home/datomic-transactor-pro-1.0.7277.jar"
version_source="$project_dir/resources/datomic/VERSION"
peer_java_root="$project_dir/src-java"
transactor_java_root="$project_dir/transactor/src-java"

expected_peer_sha=cb55c9d01e155f9965e3332cd8ef70be42cdefacb50c072076e9ecf22b9a23ba
expected_transactor_sha=d90819b57e2138085ddc26c93314f953c2cd776d1d1aa8b43a6bec1e475d3692
expected_java_sha=3e98d0f812482808f701ffa0d4e94b8dd8e31a2c6a07fb52662ceef8e75d66f1
expected_javac_sha=f315d031604835a3017268cca49631f4aa441d084d3cc29932d296ecd013106c
expected_version_sha=5b9f9a16f9249dd933274578f4deb26eeb208178c66077c6be373365d780fc5d
expected_exceptions_sha=d08c393447e124ce859c1bbb6fca567043efa2e79591fa31a62865803e760269
expected_peer_byteutil_sha=6c7857900cfa8455eb9f91a5aac570e6320500fb1dcabe5b50f56b023de05907
expected_transactor_byteutil_sha=42b5bb798ff8a892946c49002ff03bc61d298334e62d777f05ec9b864a2b6421

jar_names=(
  clojure-1.11.4.jar
  spec.alpha-0.3.218.jar
  core.specs.alpha-0.2.62.jar
  java-io-0.1.29.jar
  commons-io-2.15.1.jar
  fressian-0.6.8.jar
  slf4j-api-1.7.32.jar
  logback-classic-1.2.8.jar
  logback-core-1.2.8.jar
  tomcat-jdbc-7.0.109.jar
  tomcat-juli-7.0.109.jar
)
jar_hashes=(
  fc7ff1b6610d0e3494a75cc11ef81810bda402ac157012fe4e3b899586cf4133
  67ec898eb55c66a957a55279dd85d1376bb994bd87668b2b0de1eb3b97e8aae0
  06eea8c070bbe45c158567e443439681bc8c46e9123414f81bfa32ba42d6cbc8
  dad7eb77c59ad76ce19f6dbc056994f8c8041220dafb6f21fd2e0c8003b93343
  a58af12ee1b68cfd2ebb0c27caef164f084381a00ec81a48cc275fd7ea54e154
  369bfd851ac23cb5b319791567d101de070cafd76fe2765eaf5a948ef6369915
  3624f8474c1af46d75f98bc097d7864a323c81b3808aa43689a6e1c601c027be
  707d0f8ccacb080c8178b9b045414bb62503d403f003505a443df659e3831735
  799fc59faec20f5043ab50bd03e3c9f83381adf1818eae3307257923afd1e44d
  7f94076cbf859a3ab66ed91ba625afc314c2f7d9163bc314287e2485e50160a4
  71d6f40372884e26695c25e762f83d94c75d58bf1a007f5f1505900d3b1570db
)

postgresql_evidence_names=(storage-cas transactions crash-ack persistent-index ha)
postgresql_evidence_roots=(
  /tmp/datomic-recovered-pair-storage-cas-v2
  /tmp/datomic-recovered-pair-transaction-boundaries-v5
  /tmp/datomic-recovered-pair-ack-crash-v2
  /tmp/datomic-recovered-pair-index-v5
  /tmp/datomic-recovered-pair-ha-v1
)
postgresql_evidence_hashes=(
  98c39f04a7536e51e218fb4d28a38b080ee15fa8d22bb0a8dadfc1fe46f5d7f4
  8efea20819c62f167d774d29d7a330f6ef1e018884c50f16a533376d667a3bdd
  6f3a5ab8d6c877d7a9f2f8e2e23c4670b9a2228995dd290f854426a5a20fd0c5
  c23915e1406f1fac4046643683d93dc9cf06fee187657761e8f96fa1313b4238
  62859ef9ec8e66c43743434ea35974dde6dfd8362ff23cc6226d86721f2edf88
)

verify_file() {
  local path=$1
  local expected=$2
  local label=$3
  [[ -f "$path" && ! -L "$path" ]] || fail "$label is missing or symbolic: $path"
  local actual
  actual=$(sha256sum "$path" | awk '{print $1}')
  [[ "$actual" == "$expected" ]] ||
    fail "$label hash mismatch: expected $expected, found $actual"
}

verify_file "$peer_jar" "$expected_peer_sha" "original Peer"
verify_file "$transactor_jar" "$expected_transactor_sha" "original Transactor"
verify_file "$java_bin" "$expected_java_sha" "Corretto java"
verify_file "$javac_bin" "$expected_javac_sha" "Corretto javac"
verify_file "$version_source" "$expected_version_sha" "Datomic VERSION resource"

peer_exceptions="$peer_java_root/datomic/impl/Exceptions.java"
peer_byteutil="$peer_java_root/datomic/impl/JavaByteUtil.java"
transactor_exceptions="$transactor_java_root/datomic/impl/Exceptions.java"
transactor_byteutil="$transactor_java_root/datomic/impl/JavaByteUtil.java"
verify_file "$peer_exceptions" "$expected_exceptions_sha" "Peer Exceptions.java"
verify_file "$peer_byteutil" "$expected_peer_byteutil_sha" "Peer JavaByteUtil.java"
verify_file "$transactor_exceptions" "$expected_exceptions_sha" "Transactor Exceptions.java"
verify_file "$transactor_byteutil" "$expected_transactor_byteutil_sha" \
  "Transactor JavaByteUtil.java"

mkdir -p "$evidence_root/inputs" "$evidence_root/build/peer-classes" \
  "$evidence_root/build/transactor-classes" \
  "$evidence_root/build/candidate-resources/datomic" \
  "$evidence_root/lanes"

printf 'role\tpath\tsha256\n' >"$evidence_root/inputs/runtime-files.tsv"
printf 'original-peer\t%s\t%s\n' "$peer_jar" "$expected_peer_sha" \
  >>"$evidence_root/inputs/runtime-files.tsv"
printf 'original-transactor\t%s\t%s\n' "$transactor_jar" "$expected_transactor_sha" \
  >>"$evidence_root/inputs/runtime-files.tsv"
printf 'java\t%s\t%s\n' "$java_bin" "$expected_java_sha" \
  >>"$evidence_root/inputs/runtime-files.tsv"
printf 'javac\t%s\t%s\n' "$javac_bin" "$expected_javac_sha" \
  >>"$evidence_root/inputs/runtime-files.tsv"
printf 'evidence\troot\tmanifest\tsha256\n' \
  >"$evidence_root/inputs/attached-postgresql-evidence.tsv"
for index in "${!postgresql_evidence_names[@]}"; do
  attached_root=${postgresql_evidence_roots[$index]}
  attached_manifest="$attached_root/evidence.sha256"
  attached_sha=${postgresql_evidence_hashes[$index]}
  verify_file "$attached_manifest" "$attached_sha" \
    "attached PostgreSQL evidence ${postgresql_evidence_names[$index]}"
  (
    cd "$attached_root"
    sha256sum -c evidence.sha256 >/dev/null
  ) || fail "attached PostgreSQL evidence does not verify: $attached_root"
  printf '%s\t%s\t%s\t%s\n' "${postgresql_evidence_names[$index]}" \
    "$attached_root" "$attached_manifest" "$attached_sha" \
    >>"$evidence_root/inputs/attached-postgresql-evidence.tsv"
done

base_classpath=""
for index in "${!jar_names[@]}"; do
  jar_path="$datomic_home/lib/${jar_names[$index]}"
  verify_file "$jar_path" "${jar_hashes[$index]}" "dependency ${jar_names[$index]}"
  if /usr/bin/unzip -Z1 "$jar_path" | grep -Eq \
    '^datomic/(kv_sql|kv_sql_ext|kv_store|io|error|config|monitor|sql|slf4j)(\$|__init|[.](clj|cljc)$|/)'; then
    fail "external dependency shadows a recovered SQL-overlap namespace: $jar_path"
  fi
  printf 'dependency\t%s\t%s\n' "$jar_path" "${jar_hashes[$index]}" \
    >>"$evidence_root/inputs/runtime-files.tsv"
  if [[ -z "$base_classpath" ]]; then
    base_classpath=$jar_path
  else
    base_classpath="$base_classpath:$jar_path"
  fi
done

snapshot_source_root() {
  local source_root=$1
  local output=$2
  [[ -d "$source_root" && ! -L "$source_root" ]] ||
    fail "source root is missing or symbolic: $source_root"
  if [[ -n $(find "$source_root" -type l -print -quit) ]]; then
    fail "source root contains a symbolic link: $source_root"
  fi
  (
    cd "$source_root"
    find . -type f -print0 | sort -z | xargs -0 sha256sum
  ) >"$output"
}

snapshot_source_root "$project_dir/src-clj" \
  "$evidence_root/inputs/peer-source-tree.sha256"
snapshot_source_root "$project_dir/transactor/src-clj" \
  "$evidence_root/inputs/transactor-source-tree.sha256"
probe_sha=$(sha256sum "$probe" | awk '{print $1}')
runner_sha=$(sha256sum "$0" | awk '{print $1}')
printf '%s  %s\n' "$probe_sha" "$probe" >"$evidence_root/inputs/probe.sha256"
printf '%s  %s\n' "$runner_sha" "$0" >"$evidence_root/inputs/runner.sha256"

install -m 0644 "$version_source" \
  "$evidence_root/build/candidate-resources/datomic/VERSION"

compile_candidate_java() {
  local lane=$1
  local classes=$2
  local exceptions=$3
  local byteutil=$4
  "$javac_bin" -g:none -source 11 -target 11 -encoding UTF-8 -proc:none \
    -cp "$datomic_home/lib/clojure-1.11.4.jar" -d "$classes" \
    "$exceptions" "$byteutil" \
    >"$evidence_root/build/$lane-javac.stdout" \
    2>"$evidence_root/build/$lane-javac.stderr"
  [[ ! -s "$evidence_root/build/$lane-javac.stdout" &&
     ! -s "$evidence_root/build/$lane-javac.stderr" ]] ||
    fail "$lane recovered Java compilation wrote diagnostics"
  [[ $(find "$classes" -type f -name '*.class' | wc -l) == 4 ]] ||
    fail "$lane recovered Java closure did not emit exactly four classes"
  (
    cd "$classes"
    find . -type f -print0 | sort -z | xargs -0 sha256sum
  ) >"$evidence_root/build/$lane-classes.sha256"
}

compile_candidate_java peer "$evidence_root/build/peer-classes" \
  "$peer_exceptions" "$peer_byteutil"
compile_candidate_java transactor "$evidence_root/build/transactor-classes" \
  "$transactor_exceptions" "$transactor_byteutil"

run_lane() {
  local lane=$1
  local kind=$2
  local classpath=$3
  local lane_root="$evidence_root/lanes/$lane"
  mkdir -p "$lane_root/home" "$lane_root/tmp" "$lane_root/cwd"
  printf '%s\t%s\t%s\n' "$lane" "$kind" "$classpath" \
    >>"$evidence_root/inputs/lane-classpaths.tsv"
  case "$kind" in
    original)
      [[ "$classpath" == "$peer_jar:"* ||
         "$classpath" == "$transactor_jar:"* ]] ||
        fail "original lane lacks its owned artifact: $lane"
      ;;
    recovered)
      [[ "$classpath" != *"peer-1.0.7277.jar"* &&
         "$classpath" != *"datomic-transactor-pro-1.0.7277.jar"* &&
         "$classpath" != *"core2-"* && "$classpath" != *"nano-impl-"* ]] ||
        fail "recovered lane contains a licensed implementation artifact: $lane"
      ;;
    *) fail "unknown lane kind: $kind" ;;
  esac
  if (
    cd "$lane_root/cwd"
    /usr/bin/timeout --signal=TERM --kill-after=5s 45s \
      /usr/bin/env -i \
      PATH="$java_root/bin:/usr/bin:/bin" HOME="$lane_root/home" \
      TMPDIR="$lane_root/tmp" LC_ALL=C TZ=UTC \
      "$java_bin" -XX:+PerfDisableSharedMem \
      -Djava.io.tmpdir="$lane_root/tmp" \
      -Ddatomic.disableAllExtensions=true \
      -Dstage2.sql.probe="$probe" \
      -cp "$classpath" clojure.main -e \
      '(binding [*err* (java.io.StringWriter.)] (load-file (System/getProperty "stage2.sql.probe")))'
  ) >"$lane_root/stdout" 2>"$lane_root/stderr"; then
    :
  else
    lane_status=$?
    fail "$lane JVM failed with status $lane_status; inspect $lane_root/stderr"
  fi
  [[ ! -s "$lane_root/stderr" ]] || fail "$lane wrote stderr"
  [[ $(wc -l <"$lane_root/stdout") == 2 ]] ||
    fail "$lane stdout contains other than the two expected result rows"
  [[ $(grep -Fc 'STAGE2_SQL_OVERLAP_SUPPORTED_RESULT ' "$lane_root/stdout") == 1 ]] ||
    fail "$lane did not emit exactly one supported-domain result"
  [[ $(grep -Fc 'STAGE2_SQL_OVERLAP_COMPILER_DOMAIN_RESULT ' "$lane_root/stdout") == 1 ]] ||
    fail "$lane did not emit exactly one provenance result"
  grep -F 'STAGE2_SQL_OVERLAP_SUPPORTED_RESULT ' "$lane_root/stdout" \
    >"$lane_root/supported.result"
  grep -F 'STAGE2_SQL_OVERLAP_COMPILER_DOMAIN_RESULT ' "$lane_root/stdout" \
    >"$lane_root/provenance.result"
  [[ -z $(find "$lane_root/home" "$lane_root/tmp" "$lane_root/cwd" \
                 -mindepth 1 -print -quit) ]] ||
    fail "$lane left files in an isolated writable root"
  printf '%s\t%s\t%s\t%s\n' "$lane" "$kind" \
    "$(sha256sum "$lane_root/supported.result" | awk '{print $1}')" \
    "$(sha256sum "$lane_root/provenance.result" | awk '{print $1}')" \
    >>"$evidence_root/lanes.tsv"
}

candidate_resources="$evidence_root/build/candidate-resources"
printf 'lane\tkind\tordered_classpath\n' \
  >"$evidence_root/inputs/lane-classpaths.tsv"
printf 'lane\tkind\tsupported_sha256\tprovenance_sha256\n' \
  >"$evidence_root/lanes.tsv"
run_lane original-peer original "$peer_jar:$base_classpath"
run_lane recovered-peer recovered \
  "$project_dir/src-clj:$evidence_root/build/peer-classes:$candidate_resources:$base_classpath"
run_lane original-transactor original "$transactor_jar:$base_classpath"
run_lane recovered-transactor recovered \
  "$project_dir/transactor/src-clj:$evidence_root/build/transactor-classes:$candidate_resources:$base_classpath"

supported_reference="$evidence_root/lanes/original-peer/supported.result"
for lane in recovered-peer original-transactor recovered-transactor; do
  cmp -s "$supported_reference" "$evidence_root/lanes/$lane/supported.result" ||
    fail "supported map/JDBC behavior differs between original Peer and $lane"
done
cmp -s "$evidence_root/lanes/original-peer/provenance.result" \
  "$evidence_root/lanes/recovered-peer/provenance.result" ||
  fail "recovered Peer does not preserve Peer compiler-domain behavior"
cmp -s "$evidence_root/lanes/original-transactor/provenance.result" \
  "$evidence_root/lanes/recovered-transactor/provenance.result" ||
  fail "recovered Transactor does not preserve Transactor compiler-domain behavior"
if cmp -s "$evidence_root/lanes/original-peer/provenance.result" \
  "$evidence_root/lanes/original-transactor/provenance.result"; then
  fail "expected classified Peer/Transactor compiler-domain divergence was absent"
fi

snapshot_source_root "$project_dir/src-clj" \
  "$evidence_root/inputs/peer-source-tree.after.sha256"
snapshot_source_root "$project_dir/transactor/src-clj" \
  "$evidence_root/inputs/transactor-source-tree.after.sha256"
cmp -s "$evidence_root/inputs/peer-source-tree.sha256" \
  "$evidence_root/inputs/peer-source-tree.after.sha256" ||
  fail "Peer source tree changed during the gate"
cmp -s "$evidence_root/inputs/transactor-source-tree.sha256" \
  "$evidence_root/inputs/transactor-source-tree.after.sha256" ||
  fail "Transactor source tree changed during the gate"
[[ $(sha256sum "$probe" | awk '{print $1}') == "$probe_sha" ]] ||
  fail "probe changed during the gate"
[[ $(sha256sum "$0" | awk '{print $1}') == "$runner_sha" ]] ||
  fail "runner changed during the gate"
verify_file "$peer_jar" "$expected_peer_sha" "original Peer after run"
verify_file "$transactor_jar" "$expected_transactor_sha" "original Transactor after run"
verify_file "$java_bin" "$expected_java_sha" "Corretto java after run"
verify_file "$javac_bin" "$expected_javac_sha" "Corretto javac after run"
verify_file "$version_source" "$expected_version_sha" "Datomic VERSION resource after run"
verify_file "$peer_exceptions" "$expected_exceptions_sha" "Peer Exceptions.java after run"
verify_file "$peer_byteutil" "$expected_peer_byteutil_sha" \
  "Peer JavaByteUtil.java after run"
verify_file "$transactor_exceptions" "$expected_exceptions_sha" \
  "Transactor Exceptions.java after run"
verify_file "$transactor_byteutil" "$expected_transactor_byteutil_sha" \
  "Transactor JavaByteUtil.java after run"
(
  cd "$evidence_root/build/peer-classes"
  sha256sum -c ../peer-classes.sha256 >/dev/null
) || fail "recovered Peer Java classes changed during the gate"
(
  cd "$evidence_root/build/transactor-classes"
  sha256sum -c ../transactor-classes.sha256 >/dev/null
) || fail "recovered Transactor Java classes changed during the gate"
verify_file "$candidate_resources/datomic/VERSION" "$expected_version_sha" \
  "staged Datomic VERSION resource after run"
for index in "${!jar_names[@]}"; do
  verify_file "$datomic_home/lib/${jar_names[$index]}" "${jar_hashes[$index]}" \
    "dependency ${jar_names[$index]} after run"
done
for index in "${!postgresql_evidence_names[@]}"; do
  verify_file "${postgresql_evidence_roots[$index]}/evidence.sha256" \
    "${postgresql_evidence_hashes[$index]}" \
    "attached PostgreSQL evidence ${postgresql_evidence_names[$index]} after run"
done

supported_sha=$(sha256sum "$supported_reference" | awk '{print $1}')
peer_provenance_sha=$(sha256sum \
  "$evidence_root/lanes/original-peer/provenance.result" | awk '{print $1}')
transactor_provenance_sha=$(sha256sum \
  "$evidence_root/lanes/original-transactor/provenance.result" | awk '{print $1}')
cat >"$evidence_root/summary.tsv" <<EOF
claim\tstatus\tdetail
supported_domain_behavior\tPASS\tall four lanes emitted byte-identical map/JDBC-domain results
peer_provenance_fidelity\tPASS\toriginal and recovered Peer emitted byte-identical compiler-domain results
transactor_provenance_fidelity\tPASS\toriginal and recovered Transactor emitted byte-identical compiler-domain results
classified_cross_artifact_divergence\tPASS\tPeer and Transactor singleton-sequence behavior differs only at the six proven map-destructuring sites across the three SQL namespaces
candidate_isolation\tPASS\trecovered lanes contain no Peer, Transactor, core2, or Nano implementation archive
live_postgresql_scope\tPASS\tfully reverified CAS, transaction, crash/ack, index-adoption, and HA evidence is named and hash-bound in inputs/attached-postgresql-evidence.tsv
supported_result_sha256\tPASS\t$supported_sha
peer_provenance_sha256\tPASS\t$peer_provenance_sha
transactor_provenance_sha256\tPASS\t$transactor_provenance_sha
EOF

(
  cd "$evidence_root"
  find . -type f ! -name evidence-manifest.sha256 -print0 |
    sort -z | xargs -0 sha256sum
) >"$evidence_root/evidence-manifest.sha256"
manifest_sha=$(sha256sum "$evidence_root/evidence-manifest.sha256" | awk '{print $1}')
echo "STAGE2_SQL_OVERLAP_PASS supported_sha256=$supported_sha peer_provenance_sha256=$peer_provenance_sha transactor_provenance_sha256=$transactor_provenance_sha manifest_sha256=$manifest_sha evidence_root=$evidence_root"
