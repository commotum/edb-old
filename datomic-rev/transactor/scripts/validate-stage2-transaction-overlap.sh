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
evidence_root=${2:-/tmp/datomic-stage2-transaction-overlap-v1}
java_root=${DATOMIC_STAGE2_TRANSACTION_JAVA_ROOT:-/tmp/amazon-corretto-11.0.22.7.1}
java_bin="$java_root/bin/java"
javac_bin="$java_root/bin/javac"
probe="$script_dir/stage2/transaction_overlap_probe.clj"

fail() {
  echo "Stage 2 transaction-overlap gate: $*" >&2
  exit 1
}

for command_name in awk bash cmp diff find grep install realpath sha256sum \
                    sort timeout wc xargs; do
  command -v "$command_name" >/dev/null || fail "missing command: $command_name"
done
[[ -x "$java_bin" && -x "$javac_bin" ]] ||
  fail "pinned Corretto Java/Javac are missing: $java_root"
[[ -f "$probe" && ! -L "$probe" ]] || fail "probe is missing or symbolic"
[[ ! -L "$evidence_root" ]] || fail "evidence root may not be a symlink"
project_abs=$(realpath "$project_dir")
evidence_abs=$(realpath -m "$evidence_root")
case "$evidence_abs" in
  "$project_abs"|"$project_abs"/*)
    fail "evidence must remain outside the repository: $evidence_abs"
    ;;
esac
if [[ -e "$evidence_abs" ]] &&
   [[ -n $(find "$evidence_abs" -mindepth 1 -print -quit 2>/dev/null) ]]; then
  fail "refusing to overwrite non-empty evidence root: $evidence_abs"
fi
evidence_root=$evidence_abs

peer_jar="$datomic_home/peer-1.0.7277.jar"
transactor_jar="$datomic_home/datomic-transactor-pro-1.0.7277.jar"
oracle_core2_jar="$datomic_home/lib/core2-1.0.140.jar"
oracle_nano_jar="$datomic_home/lib/nano-impl-0.1.325.jar"
dependency_manifest="$project_dir/transactor/reports/stage-1-candidate-dependencies.tsv"
dependency_elements="$project_dir/transactor/reports/stage-1-candidate-dependency-elements.tsv"
dependency_evidence="$project_dir/transactor/reports/stage-1-candidate-dependency-evidence.sha256"
peer_compile="$project_dir/scripts/compile-handwritten-java.sh"
peer_compile_tool="$project_dir/scripts/CompileSources.java"
peer_source_list="$project_dir/reports/handwritten-java-sources.txt"
transactor_java_validate="$project_dir/transactor/scripts/validate-java.sh"
resource_stage="$project_dir/transactor/scripts/stage-structural-resources.sh"
resource_templates="$project_dir/transactor/candidate-resources"

expected_peer_sha=cb55c9d01e155f9965e3332cd8ef70be42cdefacb50c072076e9ecf22b9a23ba
expected_transactor_sha=d90819b57e2138085ddc26c93314f953c2cd776d1d1aa8b43a6bec1e475d3692
expected_oracle_core2_sha=81fdf81586c7be1a4b61655b568348d12db7892cb4562d0db7529bbc7af8a94b
expected_oracle_nano_sha=fbde8184da356bd3a9995a9f05f01493efe4baa87c4d48377cda3e53331807cd
expected_java_sha=3e98d0f812482808f701ffa0d4e94b8dd8e31a2c6a07fb52662ceef8e75d66f1
expected_javac_sha=f315d031604835a3017268cca49631f4aa441d084d3cc29932d296ecd013106c
expected_dependency_manifest_sha=2a60a620248948eb8e8f338e6d9a3e7100c03ce0837a020009686cb158913df5
expected_dependency_elements_sha=9abfd3705eaa7e5898643901bfaddd442a2805e54a969503eb7269523e442f62
expected_dependency_evidence_sha=5b714d5eed1e084a592202095eca8f9e5b8b00c3f7523d7e60edd942ee166684
expected_peer_compile_sha=a6b030702c90842d9a52c48738206d3c711e5d78875e3094e143577766c19232
expected_peer_compile_tool_sha=64c7cb1599c9255d6ef0b396e99ccff9e5448f7ba0f77a8fc34a2e294873dcc8
expected_peer_source_list_sha=7b2b839a783b3e54890af3fc1e0013557c06f2272a127e9ee50951e7d642241d
expected_transactor_java_validate_sha=65f96b328d033f2502ee3ed4b66a1e38da4f6d21cbb30600bf7465058a879c04
expected_resource_stage_sha=f7ba7f95752cfbed5f0cb5a1783726bded38d0dd756f5be4d71bfddbc5841acf

postgresql_evidence_names=(transactions crash-ack persistent-index transport ha)
postgresql_evidence_roots=(
  /tmp/datomic-recovered-pair-transaction-boundaries-v5
  /tmp/datomic-recovered-pair-ack-crash-v2
  /tmp/datomic-recovered-pair-index-v5
  /tmp/datomic-recovered-pair-transport-v2
  /tmp/datomic-recovered-pair-ha-v1
)
postgresql_evidence_hashes=(
  8efea20819c62f167d774d29d7a330f6ef1e018884c50f16a533376d667a3bdd
  6f3a5ab8d6c877d7a9f2f8e2e23c4670b9a2228995dd290f854426a5a20fd0c5
  c23915e1406f1fac4046643683d93dc9cf06fee187657761e8f96fa1313b4238
  87eaf2e129aa5d3ec12b4e84a9c3143e623b21767a00ee0c3916ea3c037383f6
  62859ef9ec8e66c43743434ea35974dde6dfd8362ff23cc6226d86721f2edf88
)

verify_file() {
  local path=$1
  local expected=$2
  local label=$3
  [[ -f "$path" && ! -L "$path" ]] ||
    fail "$label is missing or symbolic: $path"
  local actual
  actual=$(sha256sum "$path" | awk '{print $1}')
  [[ "$actual" == "$expected" ]] ||
    fail "$label hash mismatch: expected $expected, found $actual"
}

verify_file "$peer_jar" "$expected_peer_sha" "original Peer"
verify_file "$transactor_jar" "$expected_transactor_sha" "original Transactor"
verify_file "$oracle_core2_jar" "$expected_oracle_core2_sha" \
  "oracle-only core2"
verify_file "$oracle_nano_jar" "$expected_oracle_nano_sha" \
  "oracle-only Nano"
verify_file "$java_bin" "$expected_java_sha" "Corretto java"
verify_file "$javac_bin" "$expected_javac_sha" "Corretto javac"
verify_file "$dependency_manifest" "$expected_dependency_manifest_sha" \
  "candidate dependency manifest"
verify_file "$dependency_elements" "$expected_dependency_elements_sha" \
  "candidate dependency element evidence"
verify_file "$dependency_evidence" "$expected_dependency_evidence_sha" \
  "candidate dependency evidence manifest"
(
  cd "$project_dir/transactor/reports"
  sha256sum -c stage-1-candidate-dependency-evidence.sha256 >/dev/null
) || fail "candidate dependency evidence does not self-verify"
verify_file "$peer_compile" "$expected_peer_compile_sha" \
  "Peer Java compile helper"
verify_file "$peer_compile_tool" "$expected_peer_compile_tool_sha" \
  "Peer Java compiler"
verify_file "$peer_source_list" "$expected_peer_source_list_sha" \
  "Peer Java source list"
verify_file "$transactor_java_validate" \
  "$expected_transactor_java_validate_sha" "Transactor Java validator"
verify_file "$resource_stage" "$expected_resource_stage_sha" \
  "candidate resource stage helper"

mkdir -p "$evidence_root/inputs" "$evidence_root/build" \
  "$evidence_root/lanes" "$evidence_root/build-env/home" \
  "$evidence_root/build-env/tmp"

printf 'role\tpath\tsha256\n' >"$evidence_root/inputs/runtime-and-tools.tsv"
record_verified_file() {
  local role=$1
  local path=$2
  local expected=$3
  verify_file "$path" "$expected" "$role"
  printf '%s\t%s\t%s\n' "$role" "$path" "$expected" \
    >>"$evidence_root/inputs/runtime-and-tools.tsv"
}
record_verified_file original-peer "$peer_jar" "$expected_peer_sha"
record_verified_file original-transactor "$transactor_jar" \
  "$expected_transactor_sha"
record_verified_file oracle-only-core2 "$oracle_core2_jar" \
  "$expected_oracle_core2_sha"
record_verified_file oracle-only-nano "$oracle_nano_jar" \
  "$expected_oracle_nano_sha"
record_verified_file java "$java_bin" "$expected_java_sha"
record_verified_file javac "$javac_bin" "$expected_javac_sha"
record_verified_file dependency-manifest "$dependency_manifest" \
  "$expected_dependency_manifest_sha"
record_verified_file dependency-elements "$dependency_elements" \
  "$expected_dependency_elements_sha"
record_verified_file dependency-evidence "$dependency_evidence" \
  "$expected_dependency_evidence_sha"
record_verified_file peer-java-compile-helper "$peer_compile" \
  "$expected_peer_compile_sha"
record_verified_file peer-java-compiler "$peer_compile_tool" \
  "$expected_peer_compile_tool_sha"
record_verified_file peer-java-source-list "$peer_source_list" \
  "$expected_peer_source_list_sha"
record_verified_file transactor-java-validator "$transactor_java_validate" \
  "$expected_transactor_java_validate_sha"
record_verified_file resource-stage-helper "$resource_stage" \
  "$expected_resource_stage_sha"

snapshot_tree() {
  local root=$1
  local output=$2
  [[ -d "$root" && ! -L "$root" ]] ||
    fail "tree root is missing or symbolic: $root"
  [[ -z $(find "$root" -type l -print -quit) ]] ||
    fail "tree contains a symbolic link: $root"
  (
    cd "$root"
    find . -type f -print0 | sort -z | xargs -0 sha256sum
  ) >"$output"
}

snapshot_tree "$project_dir/src-clj" \
  "$evidence_root/inputs/peer-source-tree.sha256"
snapshot_tree "$project_dir/transactor/src-clj" \
  "$evidence_root/inputs/transactor-source-tree.sha256"
snapshot_tree "$project_dir/src-java" \
  "$evidence_root/inputs/peer-java-tree.sha256"
snapshot_tree "$project_dir/transactor/src-java" \
  "$evidence_root/inputs/transactor-java-tree.sha256"
snapshot_tree "$resource_templates" \
  "$evidence_root/inputs/resource-template-tree.sha256"

probe_sha=$(sha256sum "$probe" | awk '{print $1}')
runner_sha=$(sha256sum "$0" | awk '{print $1}')
printf '%s  %s\n' "$probe_sha" "$probe" \
  >"$evidence_root/inputs/probe.sha256"
printf '%s  %s\n' "$runner_sha" "$0" \
  >"$evidence_root/inputs/runner.sha256"

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

base_classpath=
dependency_count=0
printf 'position\trole\tpath\tsha256\n' \
  >"$evidence_root/inputs/recovered-runtime-dependencies.tsv"
while IFS=$'\t' read -r position role root relative kind expected; do
  [[ "$position" == position ]] && continue
  [[ "$role" == retained-distribution-dependency ]] || continue
  [[ "$root" == distribution && "$kind" == jar ]] ||
    fail "invalid retained dependency row at position $position"
  dependency="$datomic_home/$relative"
  verify_file "$dependency" "$expected" "dependency $relative"
  case "$dependency" in
    *peer-1.0.7277.jar*|*datomic-transactor-pro-1.0.7277.jar*|*core2-*.jar*|*nano-impl-*.jar*)
      fail "implementation archive entered retained dependency path: $dependency"
      ;;
  esac
  printf '%s\t%s\t%s\t%s\n' "$position" "$role" "$dependency" "$expected" \
    >>"$evidence_root/inputs/recovered-runtime-dependencies.tsv"
  if [[ -z "$base_classpath" ]]; then
    base_classpath=$dependency
  else
    base_classpath="$base_classpath:$dependency"
  fi
  dependency_count=$((dependency_count + 1))
done <"$dependency_manifest"
[[ "$dependency_count" == 531 ]] ||
  fail "expected 531 retained runtime dependencies, found $dependency_count"

peer_classes="$evidence_root/build/peer-java-classes"
transactor_java_build="$evidence_root/build/transactor-java-validation"
transactor_classes="$transactor_java_build/classes-a"
resources_build="$evidence_root/build/candidate-resources"
candidate_resources="$resources_build/runtime"

if ! /usr/bin/env -i \
  PATH="$java_root/bin:/usr/bin:/bin" \
  HOME="$evidence_root/build-env/home" TMPDIR="$evidence_root/build-env/tmp" \
  LC_ALL=C TZ=UTC \
  "$peer_compile" "$datomic_home" "$peer_classes" \
  >"$evidence_root/build/peer-java.stdout" \
  2>"$evidence_root/build/peer-java.stderr"; then
  fail "fresh Peer Java compilation failed"
fi
[[ $(wc -l <"$evidence_root/build/peer-java.stdout") == 2 &&
   $(grep -Fc 'compiled 43 Java sources' \
      "$evidence_root/build/peer-java.stdout") == 1 &&
   $(grep -Fc 'compiled the 43 handwritten Java sources into 47 classes' \
      "$evidence_root/build/peer-java.stdout") == 1 ]] ||
  fail "Peer Java compilation violated its exact stdout contract"
[[ $(wc -l <"$evidence_root/build/peer-java.stderr") == 4 &&
   $(grep -Fc 'Note: Some input files use or override a deprecated API.' \
      "$evidence_root/build/peer-java.stderr") == 1 &&
   $(grep -Fc 'Note: Recompile with -Xlint:deprecation for details.' \
      "$evidence_root/build/peer-java.stderr") == 1 &&
   $(grep -Fc 'Note: Some input files use unchecked or unsafe operations.' \
      "$evidence_root/build/peer-java.stderr") == 1 &&
   $(grep -Fc 'Note: Recompile with -Xlint:unchecked for details.' \
      "$evidence_root/build/peer-java.stderr") == 1 ]] ||
  fail "Peer Java compilation violated its exact stderr contract"
[[ $(find "$peer_classes" -type f -name '*.class' | wc -l) == 47 ]] ||
  fail "Peer Java compilation did not emit exactly 47 classes"

if ! /usr/bin/env -i \
  PATH="$java_root/bin:/usr/bin:/bin" \
  HOME="$evidence_root/build-env/home" TMPDIR="$evidence_root/build-env/tmp" \
  LC_ALL=C TZ=UTC \
  "$transactor_java_validate" "$datomic_home" "$transactor_java_build" \
  "$javac_bin" \
  >"$evidence_root/build/transactor-java.stdout" \
  2>"$evidence_root/build/transactor-java.stderr"; then
  fail "fresh Transactor Java validation failed"
fi
[[ $(wc -l <"$evidence_root/build/transactor-java.stdout") == 4 &&
   $(grep -Fc 'Java recovery exact: 52 classes; relation=' \
      "$evidence_root/build/transactor-java.stdout") == 1 &&
   $(grep -Fc 'Transactor Java recovery validation passed: ' \
      "$evidence_root/build/transactor-java.stdout") == 1 &&
   $(grep -Fc '46 sources -> 52 classes; ABI 52/52; normalized code 52/52' \
      "$evidence_root/build/transactor-java.stdout") == 1 &&
   $(grep -Fc 'Deterministic repeat build: MATCH; regenerated raw-byte matches: 20' \
      "$evidence_root/build/transactor-java.stdout") == 1 ]] ||
  fail "Transactor Java validation violated its exact stdout contract"
[[ $(wc -l <"$evidence_root/build/transactor-java.stderr") == 8 &&
   $(grep -Fc 'Note: Some input files use or override a deprecated API.' \
      "$evidence_root/build/transactor-java.stderr") == 2 &&
   $(grep -Fc 'Note: Recompile with -Xlint:deprecation for details.' \
      "$evidence_root/build/transactor-java.stderr") == 2 &&
   $(grep -Fc 'Note: Some input files use unchecked or unsafe operations.' \
      "$evidence_root/build/transactor-java.stderr") == 2 &&
   $(grep -Fc 'Note: Recompile with -Xlint:unchecked for details.' \
      "$evidence_root/build/transactor-java.stderr") == 2 ]] ||
  fail "Transactor Java validation violated its exact stderr contract"
[[ $(find "$transactor_classes" -type f -name '*.class' | wc -l) == 52 ]] ||
  fail "Transactor Java validation did not emit exactly 52 classes"

if ! /usr/bin/env -i \
  PATH="$java_root/bin:/usr/bin:/bin" \
  HOME="$evidence_root/build-env/home" TMPDIR="$evidence_root/build-env/tmp" \
  LC_ALL=C TZ=UTC \
  "$resource_stage" "$resource_templates" "$resources_build" 1.0.7277 \
  >"$evidence_root/build/candidate-resources.stdout" \
  2>"$evidence_root/build/candidate-resources.stderr"; then
  fail "fresh candidate resource staging failed"
fi
[[ ! -s "$evidence_root/build/candidate-resources.stderr" ]] ||
  fail "candidate resource staging wrote stderr"
[[ $(wc -l <"$evidence_root/build/candidate-resources.stdout") == 2 &&
   $(grep -Fc 'Candidate-owned structural resources staged: ' \
      "$evidence_root/build/candidate-resources.stdout") == 1 &&
   $(grep -Fc '4 runtime resources; AWS provisioning behavior remains explicitly unvalidated' \
      "$evidence_root/build/candidate-resources.stdout") == 1 ]] ||
  fail "candidate resource staging violated its exact stdout contract"
(
  cd "$resources_build"
  sha256sum -c manifest.sha256 >/dev/null
) || fail "candidate resource build does not verify"

snapshot_tree "$peer_classes" \
  "$evidence_root/build/peer-java-classes.sha256"
snapshot_tree "$transactor_classes" \
  "$evidence_root/build/transactor-java-classes.sha256"
snapshot_tree "$candidate_resources" \
  "$evidence_root/build/candidate-runtime-resources.sha256"

[[ -z $(find "$evidence_root/build-env/home" "$evidence_root/build-env/tmp" \
               -mindepth 1 -print -quit) ]] ||
  fail "fresh builds left files in isolated HOME or TMPDIR"

printf 'lane\tkind\tordered_classpath\n' \
  >"$evidence_root/inputs/lane-classpaths.tsv"
printf 'lane\tkind\tsupported_sha256\tprovenance_sha256\n' \
  >"$evidence_root/lanes.tsv"

run_lane() {
  local lane=$1
  local kind=$2
  local classpath=$3
  local transaction_origin=$4
  local version_origin=$5
  local java_origin=$6
  local lane_root="$evidence_root/lanes/$lane"
  mkdir -p "$lane_root/home" "$lane_root/tmp" "$lane_root/cwd"
  printf '%s\t%s\t%s\n' "$lane" "$kind" "$classpath" \
    >>"$evidence_root/inputs/lane-classpaths.tsv"
  case "$kind" in
    original)
      [[ "$classpath" == "$peer_jar:"* ||
         "$classpath" == "$transactor_jar:"* ]] ||
        fail "original lane lacks its owned oracle artifact: $lane"
      ;;
    recovered)
      [[ "$classpath" != *"peer-1.0.7277.jar"* &&
         "$classpath" != *"datomic-transactor-pro-1.0.7277.jar"* &&
         "$classpath" != *"core2-"* && "$classpath" != *"nano-impl-"* ]] ||
        fail "recovered lane contains an implementation archive: $lane"
      ;;
    *) fail "unknown lane kind: $kind" ;;
  esac
  if (
    cd "$lane_root/cwd"
    /usr/bin/timeout --signal=TERM --kill-after=5s 90s \
      /usr/bin/env -i \
      PATH="$java_root/bin:/usr/bin:/bin" HOME="$lane_root/home" \
      TMPDIR="$lane_root/tmp" LC_ALL=C TZ=UTC \
      "$java_bin" -XX:+PerfDisableSharedMem \
      -Djava.io.tmpdir="$lane_root/tmp" \
      -Ddatomic.disableAllExtensions=true \
      -Dstage2.transaction.probe="$probe" \
      -Dstage2.transaction.expected-kind="$kind" \
      -Dstage2.transaction.expected-transaction-origin="$transaction_origin" \
      -Dstage2.transaction.expected-version-origin="$version_origin" \
      -Dstage2.transaction.expected-java-origin="$java_origin" \
      -cp "$classpath" clojure.main -e \
      '(binding [*err* (java.io.StringWriter.)] (load-file (System/getProperty "stage2.transaction.probe")))'
  ) >"$lane_root/stdout" 2>"$lane_root/stderr"; then
    :
  else
    lane_status=$?
    fail "$lane JVM failed with status $lane_status; inspect $lane_root/stderr"
  fi
  [[ ! -s "$lane_root/stderr" ]] || fail "$lane wrote stderr"
  [[ $(wc -l <"$lane_root/stdout") == 2 ]] ||
    fail "$lane stdout contains other than the two expected result rows"
  [[ $(grep -Fc 'STAGE2_TRANSACTION_OVERLAP_SUPPORTED_RESULT ' \
              "$lane_root/stdout") == 1 ]] ||
    fail "$lane did not emit exactly one supported-domain result"
  [[ $(grep -Fc 'STAGE2_TRANSACTION_OVERLAP_COMPILER_DOMAIN_RESULT ' \
              "$lane_root/stdout") == 1 ]] ||
    fail "$lane did not emit exactly one compiler-domain result"
  grep -F 'STAGE2_TRANSACTION_OVERLAP_SUPPORTED_RESULT ' \
    "$lane_root/stdout" >"$lane_root/supported.result"
  grep -F 'STAGE2_TRANSACTION_OVERLAP_COMPILER_DOMAIN_RESULT ' \
    "$lane_root/stdout" >"$lane_root/provenance.result"
  [[ -z $(find "$lane_root/home" "$lane_root/tmp" "$lane_root/cwd" \
                 -mindepth 1 -print -quit) ]] ||
    fail "$lane left files in an isolated writable root"
  printf '%s\t%s\t%s\t%s\n' "$lane" "$kind" \
    "$(sha256sum "$lane_root/supported.result" | awk '{print $1}')" \
    "$(sha256sum "$lane_root/provenance.result" | awk '{print $1}')" \
    >>"$evidence_root/lanes.tsv"
}

run_lane original-peer original \
  "$peer_jar:$base_classpath" "$peer_jar" "$peer_jar" "$peer_jar"
run_lane recovered-peer recovered \
  "$project_dir/src-clj:$peer_classes:$candidate_resources:$base_classpath" \
  "$project_dir/src-clj" "$candidate_resources" "$peer_classes"
run_lane original-transactor original \
  "$transactor_jar:$oracle_core2_jar:$oracle_nano_jar:$base_classpath" \
  "$transactor_jar" "$transactor_jar" "$transactor_jar"
run_lane recovered-transactor recovered \
  "$project_dir/transactor/src-clj:$project_dir/src-clj:$transactor_classes:$candidate_resources:$base_classpath" \
  "$project_dir/transactor/src-clj" "$candidate_resources" \
  "$transactor_classes"

supported_reference="$evidence_root/lanes/original-peer/supported.result"
for lane in recovered-peer original-transactor recovered-transactor; do
  cmp -s "$supported_reference" "$evidence_root/lanes/$lane/supported.result" ||
    fail "supported transaction behavior differs between original Peer and $lane"
done
cmp -s "$evidence_root/lanes/original-peer/provenance.result" \
  "$evidence_root/lanes/recovered-peer/provenance.result" ||
  fail "recovered Peer does not preserve Peer compiler-domain behavior"
cmp -s "$evidence_root/lanes/original-transactor/provenance.result" \
  "$evidence_root/lanes/recovered-transactor/provenance.result" ||
  fail "recovered Transactor does not preserve Transactor compiler-domain behavior"
if cmp -s "$evidence_root/lanes/original-peer/provenance.result" \
  "$evidence_root/lanes/original-transactor/provenance.result"; then
  fail "expected classified Peer/Transactor singleton-sequence divergence was absent"
fi

snapshot_tree "$project_dir/src-clj" \
  "$evidence_root/inputs/peer-source-tree.after.sha256"
snapshot_tree "$project_dir/transactor/src-clj" \
  "$evidence_root/inputs/transactor-source-tree.after.sha256"
snapshot_tree "$project_dir/src-java" \
  "$evidence_root/inputs/peer-java-tree.after.sha256"
snapshot_tree "$project_dir/transactor/src-java" \
  "$evidence_root/inputs/transactor-java-tree.after.sha256"
snapshot_tree "$resource_templates" \
  "$evidence_root/inputs/resource-template-tree.after.sha256"
for tree in peer-source-tree transactor-source-tree peer-java-tree \
            transactor-java-tree resource-template-tree; do
  cmp -s "$evidence_root/inputs/$tree.sha256" \
    "$evidence_root/inputs/$tree.after.sha256" ||
    fail "sealed input tree changed during gate: $tree"
done
[[ $(sha256sum "$probe" | awk '{print $1}') == "$probe_sha" ]] ||
  fail "probe changed during the gate"
[[ $(sha256sum "$0" | awk '{print $1}') == "$runner_sha" ]] ||
  fail "runner changed during the gate"

while IFS=$'\t' read -r role path expected; do
  [[ "$role" == role ]] && continue
  verify_file "$path" "$expected" "$role after run"
done <"$evidence_root/inputs/runtime-and-tools.tsv"
(
  cd "$project_dir/transactor/reports"
  sha256sum -c stage-1-candidate-dependency-evidence.sha256 >/dev/null
) || fail "candidate dependency evidence changed during the gate"
while IFS=$'\t' read -r position role path expected; do
  [[ "$position" == position ]] && continue
  verify_file "$path" "$expected" "dependency after run at position $position"
done <"$evidence_root/inputs/recovered-runtime-dependencies.tsv"
(
  cd "$peer_classes"
  sha256sum -c ../peer-java-classes.sha256 >/dev/null
) || fail "recovered Peer Java classes changed during the gate"
(
  cd "$transactor_classes"
  sha256sum -c ../../transactor-java-classes.sha256 >/dev/null
) || fail "recovered Transactor Java classes changed during the gate"
(
  cd "$candidate_resources"
  sha256sum -c ../../candidate-runtime-resources.sha256 >/dev/null
) || fail "candidate runtime resources changed during the gate"
(
  cd "$resources_build"
  sha256sum -c manifest.sha256 >/dev/null
) || fail "candidate resource evidence changed during the gate"
for index in "${!postgresql_evidence_names[@]}"; do
  verify_file "${postgresql_evidence_roots[$index]}/evidence.sha256" \
    "${postgresql_evidence_hashes[$index]}" \
    "attached PostgreSQL evidence ${postgresql_evidence_names[$index]} after run"
  (
    cd "${postgresql_evidence_roots[$index]}"
    sha256sum -c evidence.sha256 >/dev/null
  ) || fail "attached PostgreSQL evidence changed: ${postgresql_evidence_roots[$index]}"
done

supported_sha=$(sha256sum "$supported_reference" | awk '{print $1}')
peer_provenance_sha=$(sha256sum \
  "$evidence_root/lanes/original-peer/provenance.result" | awk '{print $1}')
transactor_provenance_sha=$(sha256sum \
  "$evidence_root/lanes/original-transactor/provenance.result" | awk '{print $1}')
{
  printf 'claim\tstatus\tdetail\n'
  printf 'supported_domain_behavior\tPASS\t%s\n' \
    'all four lanes emitted byte-identical routing, address, Fressian, read-message, procargs, log-cache, completion, monitoring, and logging results'
  printf 'peer_provenance_fidelity\tPASS\t%s\n' \
    'original and recovered Peer emitted byte-identical malformed singleton-sequence behavior'
  printf 'transactor_provenance_fidelity\tPASS\t%s\n' \
    'original and recovered Transactor emitted byte-identical malformed singleton-sequence behavior'
  printf 'classified_cross_artifact_divergence\tPASS\t%s\n' \
    'Peer and Transactor preserve their distinct Clojure compiler map-destructuring behavior in log-completion!'
  printf 'candidate_origin\tPASS\t%s\n' \
    'each recovered lane proved current repository transaction source, freshly compiled Java, and candidate-owned VERSION origins'
  printf 'candidate_isolation\tPASS\t%s\n' \
    'recovered lanes contain no Peer, Transactor, core2, or Nano implementation archive'
  printf 'fresh_candidate_builds\tPASS\t%s\n' \
    'Peer 43-source Java closure and Transactor 46-source Java closure were freshly built; candidate resources were freshly staged'
  printf 'live_runtime_scope\tPASS\t%s\n' \
    'fully reverified transaction, crash/ack, persistent-index, transport-recovery, and HA PostgreSQL evidence is named and hash-bound'
  printf 'supported_result_sha256\tPASS\t%s\n' "$supported_sha"
  printf 'peer_provenance_sha256\tPASS\t%s\n' "$peer_provenance_sha"
  printf 'transactor_provenance_sha256\tPASS\t%s\n' "$transactor_provenance_sha"
} >"$evidence_root/summary.tsv"

(
  cd "$evidence_root"
  find . -type f ! -name evidence-manifest.sha256 -print0 |
    sort -z | xargs -0 sha256sum
) >"$evidence_root/evidence-manifest.sha256"
(
  cd "$evidence_root"
  sha256sum -c evidence-manifest.sha256 >/dev/null
) || fail "final evidence manifest does not self-verify"
manifest_sha=$(sha256sum "$evidence_root/evidence-manifest.sha256" | awk '{print $1}')
echo "STAGE2_TRANSACTION_OVERLAP_PASS supported_sha256=$supported_sha peer_provenance_sha256=$peer_provenance_sha transactor_provenance_sha256=$transactor_provenance_sha manifest_sha256=$manifest_sha evidence_root=$evidence_root"
