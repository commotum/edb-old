#!/usr/bin/bash -p

set -euo pipefail
umask 077
unset BASH_ENV ENV CDPATH GLOBIGNORE JAVA_TOOL_OPTIONS JDK_JAVA_OPTIONS \
  _JAVA_OPTIONS CLASSPATH JAVA_HOME LD_PRELOAD LD_LIBRARY_PATH
IFS=$' \t\n'
export LC_ALL=C TZ=UTC
PATH=${DATOMIC_REV_SAFE_PATH:-/usr/lib/jvm/java-21-openjdk-amd64/bin:/usr/bin:/bin}
export PATH

script_dir=$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd)
project_dir=$(cd -- "$script_dir/.." && pwd)
datomic_home=${1:-"$project_dir/../../datomic/datomic-pro-1.0.7277"}
work_root=${2:-$(mktemp -d -t datomic-surface-validation.XXXXXXXX)}
jobs=${JOBS:-4}
source_root=${DATOMIC_REV_CANDIDATE_SOURCE_ROOT:-"$project_dir/src-clj"}
java_bin=${DATOMIC_REV_JAVA_BIN:-/usr/lib/jvm/java-21-openjdk-amd64/bin/java}
namespace_timeout_seconds=${DATOMIC_REV_NAMESPACE_TIMEOUT_SECONDS:-180}
setup_timeout_seconds=${DATOMIC_REV_SETUP_TIMEOUT_SECONDS:-600}
validation_timeout_seconds=${DATOMIC_REV_VALIDATION_TIMEOUT_SECONDS:-7200}
namespace_index="$project_dir/reports/source-index/namespaces.tsv"
peer_jar="$datomic_home/peer-1.0.7277.jar"
transactor_jar="$datomic_home/datomic-transactor-pro-1.0.7277.jar"
distribution_tsv="$project_dir/transactor/baseline/distribution-jars.tsv"
peer_pom="$project_dir/resources/META-INF/maven/com.datomic/peer/pom.xml"
surface_ledger_comparator="$script_dir/compare_peer_surface_ledgers.clj"
semantic_proxy_origin_inspector="$script_dir/inspect_peer_semantic_proxy_origin.clj"
tools_analyzer_jar="$datomic_home/lib/tools.analyzer-1.1.1.jar"
peer_emitter="$script_dir/emit_namespace_surface.clj"
structural_emitter="$project_dir/transactor/scripts/emit_structural_surface.clj"
expected_sha=cb55c9d01e155f9965e3332cd8ef70be42cdefacb50c072076e9ecf22b9a23ba
expected_java_sha=377196a32c5e4442b604bbf36add1c49cfe8680cd27edd693b60872e58895e9e
expected_peer_pom_sha=34b9caaa92b72b5a6d69397c5d7908a5b79fc7c29cb75ba4af490ff59fed0be9
expected_analyzer_source_sha=6d97ead2cf4a0fd350b038b6bc6f65cc2f3ac924723473329d5a2b7543ae5d12
expected_sanitized_nano_sha=08f9699d6e35b9e052ec85ee15e0896b3a685c74e6e226d8cd89d9c61dbf8d5f
expected_peer_emitter_sha=a1ca4454920514f650bbcc76034e5269e20f083a4ab33806678feef27429a497
expected_structural_emitter_sha=f82eb4a09c903a8325d17f2f54ae4aebc6457f4de6ede8f95e363d8c82a13c86

die() {
  echo "Peer namespace-surface validation: $*" >&2
  exit 1
}

[[ "$jobs" =~ ^[1-9][0-9]*$ ]] || die "JOBS must be a positive integer"
[[ "$namespace_timeout_seconds" =~ ^[1-9][0-9]*$ && \
   "$namespace_timeout_seconds" -le 1800 ]] || \
  die "DATOMIC_REV_NAMESPACE_TIMEOUT_SECONDS must be 1..1800"
[[ "$setup_timeout_seconds" =~ ^[1-9][0-9]*$ && \
   "$setup_timeout_seconds" -le 3600 ]] || \
  die "DATOMIC_REV_SETUP_TIMEOUT_SECONDS must be 1..3600"
[[ "$validation_timeout_seconds" =~ ^[1-9][0-9]*$ && \
   "$validation_timeout_seconds" -le 14400 ]] || \
  die "DATOMIC_REV_VALIDATION_TIMEOUT_SECONDS must be 1..14400"

for required_file in "$peer_jar" "$transactor_jar" "$namespace_index" "$distribution_tsv" \
    "$java_bin" "$script_dir/PrintRuntimeClasspath.java" \
    "$script_dir/audit-peer-runtime-classpath.sh" "$peer_pom" \
    "$surface_ledger_comparator" "$tools_analyzer_jar" \
    "$peer_emitter" "$structural_emitter" \
    "$semantic_proxy_origin_inspector"; do
  [[ -f "$required_file" && ! -L "$required_file" ]] || \
    die "required regular input is missing or symbolic: $required_file"
done
[[ -d "$source_root" && ! -L "$source_root" ]] || \
  die "candidate source root is missing or symbolic: $source_root"

actual_sha=$(sha256sum "$peer_jar" | awk '{print $1}')
[[ "$actual_sha" == "$expected_sha" ]] || \
  die "unexpected peer JAR SHA-256: $actual_sha"
[[ $(sha256sum "$java_bin" | awk '{print $1}') == "$expected_java_sha" ]] || \
  die "pinned Java launcher hash mismatch"
[[ $(sha256sum "$peer_pom" | awk '{print $1}') == \
   "$expected_peer_pom_sha" ]] || die "recovered Peer POM hash mismatch"
[[ $(sha256sum "$peer_emitter" | awk '{print $1}') == \
   "$expected_peer_emitter_sha" ]] || die "Peer surface emitter hash mismatch"
[[ $(sha256sum "$structural_emitter" | awk '{print $1}') == \
   "$expected_structural_emitter_sha" ]] || \
  die "structural surface emitter hash mismatch"
grep -Fq -- "-Dclojure.compiler.elide-meta='[:doc :file :line]'" \
  "$peer_pom" || die "recovered Peer POM lacks the shipped metadata-elision setting"

if [[ -e "$work_root" ]] && [[ -n "$(find "$work_root" -mindepth 1 -print -quit 2>/dev/null)" ]]; then
  die "refusing to overwrite non-empty validation directory: $work_root"
fi
[[ ! -L "$work_root" ]] || die "validation directory may not be symbolic: $work_root"

source_abs=$(realpath "$source_root")
work_abs=$(realpath -m "$work_root")
case "$work_abs/" in
  "$source_abs/"* ) die "validation output is nested under candidate source input" ;;
esac
case "$source_abs/" in
  "$work_abs/"* ) die "candidate source input is nested under validation output" ;;
esac

build_root="$work_root/source-validation"
surface_root="$work_root/surfaces"
runtime_context="$work_root/runtime-context"
mkdir -p "$surface_root/original" "$surface_root/recovered" \
  "$surface_root/logs" "$surface_root/results" \
  "$surface_root/proxy-exclusions/original" \
  "$surface_root/proxy-exclusions/recovered" \
  "$runtime_context/home" "$runtime_context/tmp" \
  "$runtime_context/java-tmp"
export HOME="$runtime_context/home" TMPDIR="$runtime_context/tmp"
export DATOMIC_REV_JAVA_BIN="$java_bin"
export DATOMIC_REV_SAFE_PATH="$PATH"
export DATOMIC_REV_NAMESPACE_TIMEOUT_SECONDS="$namespace_timeout_seconds"
export DATOMIC_REV_SETUP_TIMEOUT_SECONDS="$setup_timeout_seconds"

java_tmp_root="$runtime_context/java-tmp"
prepare_java_tmp() {
  local variable_name=$1
  local label=$2
  local destination="$java_tmp_root/$label"
  [[ ! -e "$destination" && ! -L "$destination" ]] || \
    die "refusing existing per-probe Java temp directory: $destination"
  mkdir -p "$destination"
  printf -v "$variable_name" '%s' "$destination"
}
require_empty_java_tmp() {
  local destination=$1
  local label=$2
  [[ -z $(find "$destination" -mindepth 1 -print -quit) ]] || \
    die "$label left content in its isolated Java temp directory"
}

DATOMIC_REV_CANDIDATE_SOURCE_ROOT="$source_root" JOBS="$jobs" \
  /usr/bin/timeout --signal=TERM --kill-after=30s \
  "${validation_timeout_seconds}s" \
  /usr/bin/bash -p "$script_dir/validate-all-namespaces.sh" \
  "$datomic_home" "$build_root"

export DATOMIC_REV_ORIGINAL_CLASSPATH="$peer_jar:$build_root/infinispan-compile-stubs:$datomic_home/lib/*"
export DATOMIC_REV_SOURCE_CLASSPATH="$source_root:$build_root/handwritten-classes:$build_root/peer-resources-without-vendor-keystores:$build_root/infinispan-compile-stubs:$build_root/libraries-without-aot-core2/*"
export DATOMIC_REV_SURFACE_ROOT="$surface_root"
export DATOMIC_VAR_METADATA_VIEW=exact

capture_and_audit_classpaths() {
  local phase=$1
  local phase_root="$work_root/classpath-$phase"
  local original_java_tmp candidate_java_tmp
  prepare_java_tmp original_java_tmp "classpath-$phase-original"
  prepare_java_tmp candidate_java_tmp "classpath-$phase-candidate"
  mkdir -p "$phase_root"
  /usr/bin/timeout --signal=TERM --kill-after=15s \
    "${setup_timeout_seconds}s" \
    "$java_bin" -XX:+PerfDisableSharedMem \
    -Djava.io.tmpdir="$original_java_tmp" \
    -cp "$DATOMIC_REV_ORIGINAL_CLASSPATH" \
    "$script_dir/PrintRuntimeClasspath.java" "$phase_root/original.actual.txt"
  require_empty_java_tmp "$original_java_tmp" "classpath-$phase-original"
  /usr/bin/timeout --signal=TERM --kill-after=15s \
    "${setup_timeout_seconds}s" \
    "$java_bin" -XX:+PerfDisableSharedMem \
    -Djava.io.tmpdir="$candidate_java_tmp" \
    -cp "$DATOMIC_REV_SOURCE_CLASSPATH" \
    "$script_dir/PrintRuntimeClasspath.java" "$phase_root/candidate.actual.txt"
  require_empty_java_tmp "$candidate_java_tmp" "classpath-$phase-candidate"
  /usr/bin/timeout --signal=TERM --kill-after=15s \
    "${setup_timeout_seconds}s" \
    /usr/bin/bash -p "$script_dir/audit-peer-runtime-classpath.sh" \
    original "$phase_root/original.actual.txt" "$phase_root/original" \
    "$distribution_tsv" "$datomic_home" "$source_root" \
    "$build_root/handwritten-classes" \
    "$build_root/peer-resources-without-vendor-keystores" \
    "$build_root/infinispan-compile-stubs" \
    "$build_root/libraries-without-aot-core2" "$namespace_index" \
    "$build_root/libraries-without-aot-core2/nano-impl-0.1.325-sanitized.jar"
  /usr/bin/timeout --signal=TERM --kill-after=15s \
    "${setup_timeout_seconds}s" \
    /usr/bin/bash -p "$script_dir/audit-peer-runtime-classpath.sh" \
    candidate "$phase_root/candidate.actual.txt" "$phase_root/candidate" \
    "$distribution_tsv" "$datomic_home" "$source_root" \
    "$build_root/handwritten-classes" \
    "$build_root/peer-resources-without-vendor-keystores" \
    "$build_root/infinispan-compile-stubs" \
    "$build_root/libraries-without-aot-core2" "$namespace_index" \
    "$build_root/libraries-without-aot-core2/nano-impl-0.1.325-sanitized.jar"
  grep -Fqx 'licensed.peer.archive.present=false' \
    "$phase_root/candidate/candidate-summary.properties" || \
    die "candidate classpath audit did not exclude the Peer archive"
  grep -Fqx 'licensed.transactor.archive.present=false' \
    "$phase_root/candidate/candidate-summary.properties" || \
    die "candidate classpath audit did not exclude the Transactor archive"
  grep -Fqx 'licensed.core2.archive.present=false' \
    "$phase_root/candidate/candidate-summary.properties" || \
    die "candidate classpath audit did not exclude the core2 archive"
  grep -Fqx 'licensed.nano.original.present=false' \
    "$phase_root/candidate/candidate-summary.properties" || \
    die "candidate classpath audit did not exclude the keystore-bearing Nano original"
  grep -Fqx 'licensed.implementation.archive-payload.present=false' \
    "$phase_root/candidate/candidate-summary.properties" || \
    die "candidate classpath audit did not exclude renamed licensed archive payloads from directories"
  grep -Fqx 'candidate.directory.nested-archive.present=false' \
    "$phase_root/candidate/candidate-summary.properties" || \
    die "candidate classpath audit did not exclude nested archives from candidate directories"
  grep -Fqx 'vendor.nano.keystore.payload.present=false' \
    "$phase_root/candidate/candidate-summary.properties" || \
    die "candidate classpath audit did not exclude vendor Nano keystore payloads"
  grep -Fqx 'sanitized.nano.derivative.entries=1' \
    "$phase_root/candidate/candidate-summary.properties" || \
    die "candidate classpath audit did not prove exactly one sanitized Nano derivative"
  grep -Fqx "sanitized.nano.derivative.sha256=$expected_sanitized_nano_sha" \
    "$phase_root/candidate/candidate-summary.properties" || \
    die "candidate classpath audit did not bind the sanitized Nano derivative hash"
  grep -Fqx 'licensed.datomic.clojure.aot.present=false' \
    "$phase_root/candidate/candidate-summary.properties" || \
    die "candidate classpath audit did not exclude licensed Datomic AOT"
}

capture_and_audit_classpaths pre

compiler_option_root="$work_root/compiler-option-gate"
mkdir -p "$compiler_option_root"
compiler_option_original_cp="$transactor_jar:$datomic_home/lib/*"
compiler_option_source_root="$project_dir/transactor/src-clj"
compiler_option_owner_source="$compiler_option_source_root/clojure/tools/analyzer/passes/add_binding_atom.clj"
[[ -d "$compiler_option_source_root" && ! -L "$compiler_option_source_root" ]] || \
  die "compiler-option source root is missing or symbolic"
[[ -f "$compiler_option_owner_source" && ! -L "$compiler_option_owner_source" ]] || \
  die "compiler-option semantic-proxy owner source is missing or symbolic"
[[ $(sha256sum "$compiler_option_owner_source" | awk '{print $1}') == \
   "$expected_analyzer_source_sha" ]] || \
  die "compiler-option semantic-proxy owner source hash mismatch"
[[ -z $(find "$compiler_option_source_root" -type f -name '*.class' -print -quit) ]] || \
  die "compiler-option source root contains compiled classes"
compiler_option_source_cp="$compiler_option_source_root:$build_root/libraries-without-aot-core2/*"
compiler_option_original="$compiler_option_root/original-aligned.edn"
compiler_option_source="$compiler_option_root/source-aligned.edn"
compiler_option_omitted="$compiler_option_root/source-option-omitted.edn"
compiler_option_omitted_diagnostic="$compiler_option_root/source-option-omitted-diagnostic.edn"
prepare_java_tmp compiler_option_classpath_java_tmp compiler-option-classpath
prepare_java_tmp compiler_option_original_java_tmp compiler-option-original
prepare_java_tmp compiler_option_source_java_tmp compiler-option-source
prepare_java_tmp compiler_option_omitted_java_tmp compiler-option-omitted-exact
prepare_java_tmp compiler_option_omitted_diagnostic_java_tmp \
  compiler-option-omitted-diagnostic
prepare_java_tmp compiler_option_origin_java_tmp compiler-option-origin

/usr/bin/timeout --signal=TERM --kill-after=15s \
  "${setup_timeout_seconds}s" \
  "$java_bin" -XX:+PerfDisableSharedMem \
  -Djava.io.tmpdir="$compiler_option_classpath_java_tmp" \
  -cp "$compiler_option_source_cp" \
  "$script_dir/PrintRuntimeClasspath.java" \
  "$compiler_option_root/source.actual-classpath.txt"
require_empty_java_tmp "$compiler_option_classpath_java_tmp" \
  compiler-option-classpath
compiler_option_source_abs=$(realpath "$compiler_option_source_root")
[[ $(sed -n '1p' "$compiler_option_root/source.actual-classpath.txt") == \
   "$compiler_option_source_abs" ]] || \
  die "compiler-option owner source root is not first on the actual JVM classpath"
compiler_option_library_abs=$(realpath "$build_root/libraries-without-aot-core2")
compiler_option_peer_sha=$(awk -F '\t' \
  '$2 == "peer-1.0.7277.jar" {print $3}' "$distribution_tsv")
compiler_option_transactor_sha=$(awk -F '\t' \
  '$2 == "datomic-transactor-pro-1.0.7277.jar" {print $3}' \
  "$distribution_tsv")
compiler_option_core2_sha=$(awk -F '\t' \
  '$2 == "lib/core2-1.0.140.jar" {print $3}' "$distribution_tsv")
compiler_option_nano_sha=$(awk -F '\t' \
  '$2 == "lib/nano-impl-0.1.325.jar" {print $3}' "$distribution_tsv")
printf 'position\tdeclared_path\tresolved_path\trole\tsha256\towner_aot_entries\n' \
  >"$compiler_option_root/source-classpath-inventory.tsv"
compiler_option_position=0
compiler_option_sanitized_nano_count=0
while IFS= read -r compiler_option_entry; do
  [[ -n "$compiler_option_entry" ]] || \
    die "compiler-option actual classpath contains an empty entry"
  ((compiler_option_position += 1))
  compiler_option_resolved=$(realpath "$compiler_option_entry")
  compiler_option_role=
  compiler_option_entry_sha=
  compiler_option_aot_count=0
  if [[ "$compiler_option_position" -eq 1 ]]; then
    [[ "$compiler_option_resolved" == "$compiler_option_source_abs" ]] || \
      die "compiler-option first origin changed during inventory"
    compiler_option_role=semantic-proxy-owner-source
    compiler_option_entry_sha=$(find "$compiler_option_resolved" -type f -print0 \
      | sort -z | xargs -0 sha256sum | sha256sum | awk '{print $1}')
  else
    [[ -f "$compiler_option_entry" ]] || \
      die "compiler-option dependency is not a regular archive: $compiler_option_entry"
    [[ $(realpath -m "$(dirname "$compiler_option_entry")") == \
       "$compiler_option_library_abs" ]] || \
      die "compiler-option dependency path escaped the isolated library root"
    compiler_option_entry_sha=$(sha256sum "$compiler_option_resolved" \
      | awk '{print $1}')
    [[ "$compiler_option_entry_sha" != "$compiler_option_peer_sha" && \
       "$compiler_option_entry_sha" != "$compiler_option_transactor_sha" && \
       "$compiler_option_entry_sha" != "$compiler_option_core2_sha" && \
       "$compiler_option_entry_sha" != "$compiler_option_nano_sha" ]] || \
      die "compiler-option source lane contains a licensed implementation/original Nano archive"
    compiler_option_aot_count=$(zipinfo -1 "$compiler_option_resolved" \
      | grep -Ec '^clojure/tools/analyzer/passes/add_binding_atom(__init|\$).*\.class$' \
      || true)
    [[ "$compiler_option_aot_count" -eq 0 ]] || \
      die "compiler-option source lane contains owner AOT classes"
    if [[ "$compiler_option_entry_sha" == "$expected_sanitized_nano_sha" ]]; then
      compiler_option_role=sanitized-nano-dependency
      ((compiler_option_sanitized_nano_count += 1))
    else
      compiler_option_role=distribution-dependency
    fi
  fi
  printf '%s\t%s\t%s\t%s\t%s\t%s\n' \
    "$compiler_option_position" "$compiler_option_entry" \
    "$compiler_option_resolved" "$compiler_option_role" \
    "$compiler_option_entry_sha" "$compiler_option_aot_count" \
    >>"$compiler_option_root/source-classpath-inventory.tsv"
done <"$compiler_option_root/source.actual-classpath.txt"
[[ "$compiler_option_sanitized_nano_count" -eq 1 ]] || \
  die "compiler-option source lane lacks exactly one sanitized Nano derivative"

DATOMIC_VAR_METADATA_VIEW=exact DATOMIC_VAR_METADATA_EMIT_TYPES=1 \
  /usr/bin/timeout --signal=TERM --kill-after=15s \
  "${namespace_timeout_seconds}s" \
  "$java_bin" -XX:+PerfDisableSharedMem \
  -Djava.io.tmpdir="$compiler_option_original_java_tmp" \
  -Ddatomic.surface.semantic-proxy-origin=classpath-bytecode \
  '-Dclojure.compiler.elide-meta=[:doc :file :line]' \
  -cp "$compiler_option_original_cp" clojure.main "$structural_emitter" \
  "$peer_emitter" clojure.tools.analyzer.passes.add-binding-atom \
  "$compiler_option_original" \
  >"$compiler_option_root/original-aligned.stdout" \
  2>"$compiler_option_root/original-aligned.stderr"
require_empty_java_tmp "$compiler_option_original_java_tmp" \
  compiler-option-original
DATOMIC_VAR_METADATA_VIEW=exact DATOMIC_VAR_METADATA_EMIT_TYPES=1 \
  /usr/bin/timeout --signal=TERM --kill-after=15s \
  "${namespace_timeout_seconds}s" \
  "$java_bin" -XX:+PerfDisableSharedMem \
  -Djava.io.tmpdir="$compiler_option_source_java_tmp" \
  -Ddatomic.surface.semantic-proxy-origin=source-generated \
  '-Dclojure.compiler.elide-meta=[:doc :file :line]' \
  -cp "$compiler_option_source_cp" clojure.main "$structural_emitter" \
  "$peer_emitter" clojure.tools.analyzer.passes.add-binding-atom \
  "$compiler_option_source" \
  >"$compiler_option_root/source-aligned.stdout" \
  2>"$compiler_option_root/source-aligned.stderr"
require_empty_java_tmp "$compiler_option_source_java_tmp" \
  compiler-option-source
expect_compiler_option_omission_failure() {
  local view=$1
  local output_file=$2
  local log_prefix=$3
  local java_tmp_dir=$4
  local status=0
  if DATOMIC_VAR_METADATA_VIEW="$view" \
    /usr/bin/timeout --signal=TERM --kill-after=15s \
    "${namespace_timeout_seconds}s" \
    "$java_bin" -XX:+PerfDisableSharedMem \
    -Djava.io.tmpdir="$java_tmp_dir" \
    -Ddatomic.surface.semantic-proxy-origin=source-generated \
    -cp "$compiler_option_source_cp" clojure.main \
    "$structural_emitter" "$peer_emitter" \
    clojure.tools.analyzer.passes.add-binding-atom "$output_file" \
    >"$compiler_option_root/$log_prefix.stdout" \
    2>"$compiler_option_root/$log_prefix.stderr"; then
    die "compiler-option omission unexpectedly succeeded in $view view"
  else
    status=$?
  fi
  [[ "$status" -eq 1 ]] || \
    die "compiler-option omission failed with non-contract status $status in $view view"
  [[ ! -e "$output_file" && ! -L "$output_file" ]] || \
    die "compiler-option omission produced a canonical payload in $view view"
  [[ ! -s "$compiler_option_root/$log_prefix.stdout" ]] || \
    die "compiler-option omission wrote an unexpected stdout payload in $view view"
  [[ $(grep -Fxc \
      'FAIL clojure.tools.analyzer.passes.add-binding-atom ["runtime must pin clojure.compiler.elide-meta"]' \
      "$compiler_option_root/$log_prefix.stderr" || true) -eq 1 ]] || \
    die "compiler-option omission did not fail with the exact contract diagnostic in $view view"
  [[ $(grep -c '^VAR_' "$compiler_option_root/$log_prefix.stderr" || true) -eq 0 ]] || \
    die "compiler-option omission emitted a side ledger in $view view"
  require_empty_java_tmp "$java_tmp_dir" "compiler-option-omission-$view"
}

expect_compiler_option_omission_failure exact "$compiler_option_omitted" \
  source-option-omitted "$compiler_option_omitted_java_tmp"
expect_compiler_option_omission_failure diagnostic \
  "$compiler_option_omitted_diagnostic" source-option-omitted-diagnostic \
  "$compiler_option_omitted_diagnostic_java_tmp"

cmp -s "$compiler_option_original" "$compiler_option_source" || \
  die "source-loaded analyzer metadata differs despite shipped compiler option"
compiler_option_original_semantic="$compiler_option_root/original-aligned.semantic.edn"
compiler_option_source_semantic="$compiler_option_root/source-aligned.semantic.edn"
compiler_option_original_types="$compiler_option_root/original-aligned.metadata-types.txt"
compiler_option_source_types="$compiler_option_root/source-aligned.metadata-types.txt"
grep '^VAR_METADATA_SEMANTIC_PROXIES ' \
  "$compiler_option_root/original-aligned.stderr" \
  >"$compiler_option_original_semantic" || \
  die "AOT compiler-option oracle omitted semantic-proxy evidence"
grep '^VAR_METADATA_SEMANTIC_PROXIES ' \
  "$compiler_option_root/source-aligned.stderr" \
  >"$compiler_option_source_semantic" || \
  die "source compiler-option lane omitted semantic-proxy evidence"
[[ $(wc -l <"$compiler_option_original_semantic") -eq 1 && \
   $(wc -l <"$compiler_option_source_semantic") -eq 1 ]] || \
  die "compiler-option semantic-proxy evidence is not exactly one tagged line per side"
grep '^VAR_METADATA_VALUE_TYPES ' \
  "$compiler_option_root/original-aligned.stderr" \
  >"$compiler_option_original_types" || \
  die "AOT compiler-option oracle omitted metadata-type evidence"
grep '^VAR_METADATA_VALUE_TYPES ' \
  "$compiler_option_root/source-aligned.stderr" \
  >"$compiler_option_source_types" || \
  die "source compiler-option lane omitted metadata-type evidence"
[[ $(wc -l <"$compiler_option_original_types") -eq 1 && \
   $(wc -l <"$compiler_option_source_types") -eq 1 ]] || \
  die "compiler-option metadata-type evidence is not exactly one tagged line per side"

/usr/bin/timeout --signal=TERM --kill-after=15s \
  "${namespace_timeout_seconds}s" \
  "$java_bin" -XX:+PerfDisableSharedMem \
  -Djava.io.tmpdir="$compiler_option_origin_java_tmp" \
  '-Dclojure.compiler.elide-meta=[:doc :file :line]' \
  -cp "$compiler_option_source_cp" clojure.main \
  "$semantic_proxy_origin_inspector" "$compiler_option_source_root" \
  "$compiler_option_root/source-semantic-proxy-origin.edn"
require_empty_java_tmp "$compiler_option_origin_java_tmp" compiler-option-origin
grep -Fq ':contract :source-generated-in-memory-class-v1' \
  "$compiler_option_root/source-semantic-proxy-origin.edn" || \
  die "compiler-option source-generated semantic proxy origin contract is absent"
grep -Fq ':class-resource nil' \
  "$compiler_option_root/source-semantic-proxy-origin.edn" || \
  die "compiler-option source-generated semantic proxy unexpectedly has a class resource"
grep -Fq ':code-source nil' \
  "$compiler_option_root/source-semantic-proxy-origin.edn" || \
  die "compiler-option source-generated semantic proxy unexpectedly has an AOT CodeSource"
source_semantic_runtime_class=$(sed -n \
  's/.*:runtime-class "\([^"]*\)".*/\1/p' \
  "$compiler_option_source_semantic")
[[ "$source_semantic_runtime_class" =~ \
  ^clojure\.tools\.analyzer\.passes\.add_binding_atom\$fn__[0-9]+$ ]] || \
  die "compiler-option source semantic ledger has an unexpected generated Class"
source_semantic_class_entry=${source_semantic_runtime_class//./\/}.class
while IFS= read -r compiler_option_entry; do
  [[ -f "$compiler_option_entry" ]] || continue
  if zipinfo -1 "$(realpath "$compiler_option_entry")" \
    | grep -Fqx "$source_semantic_class_entry"; then
    die "compiler-option reported source semantic proxy Class is present as AOT"
  fi
done <"$compiler_option_root/source.actual-classpath.txt"
{
  printf 'peer.pom.sha256=%s\n' "$expected_peer_pom_sha"
  printf 'compiler.option={:elide-meta [:doc :file :line]}\n'
  printf 'aligned.surface.sha256=%s\n' \
    "$(sha256sum "$compiler_option_original" | awk '{print $1}')"
  printf 'owner.source.sha256=%s\n' "$expected_analyzer_source_sha"
  printf 'owner.source.actual-classpath-first=true\n'
  printf 'owner.aot.on.source-classpath=false\n'
  printf 'source.semantic-proxy.runtime-class=%s\n' \
    "$source_semantic_runtime_class"
  printf 'source.semantic-proxy.code-source=nil\n'
  printf 'source.semantic-proxy.class-resource=nil\n'
  printf 'omission.exact.fail-closed=PASS\n'
  printf 'omission.diagnostic.fail-closed=PASS\n'
} >"$compiler_option_root/summary.properties"

namespace_list="$surface_root/namespaces.txt"
awk -F '\t' 'NR > 1 {print $1}' "$namespace_index" >"$namespace_list"
[[ $(wc -l <"$namespace_list") -eq 142 ]] || \
  die "namespace index does not contain 142 rows"
[[ $(sort -u "$namespace_list" | wc -l) -eq 142 ]] || \
  die "namespace index contains duplicate namespace names"

surface_driver_status=0
xargs -a "$namespace_list" -P "$jobs" -n 1 /usr/bin/bash -p \
  "$script_dir/compare-one-namespace-surface.sh" \
  >"$surface_root/driver.stdout" 2>"$surface_root/driver.stderr" || \
  surface_driver_status=$?

result_file_count=$(find "$surface_root/results" -type f -name '*.tsv' | wc -l)
[[ "$result_file_count" -eq 142 ]] || \
  die "dedicated result channel contains $result_file_count/142 records"
{
  printf 'tag\tstatus\tnamespace\tmetadata_view\tmismatch_kind\toriginal_sha256\trecovered_sha256\n'
  find "$surface_root/results" -type f -name '*.tsv' -print0 \
    | sort -z | xargs -0 cat
} >"$surface_root/results.tsv"

tagged_result_count=$(grep -c '^DATOMIC_SURFACE_RESULT' \
  "$surface_root/results.tsv" || true)
pass_count=$(grep -c $'^DATOMIC_SURFACE_RESULT\tPASS\t.*\texact\tnone\t' \
  "$surface_root/results.tsv" || true)
fail_count=$(grep -c $'^DATOMIC_SURFACE_RESULT\tFAIL\t' \
  "$surface_root/results.tsv" || true)
[[ "$tagged_result_count" -eq 142 ]] || \
  die "dedicated result channel contains malformed/missing tags"
[[ "$surface_driver_status" -eq 0 ]] || \
  die "one or more exact runtime namespace surfaces failed"
[[ "$fail_count" -eq 0 ]] || die "$fail_count namespace surfaces recorded FAIL"
[[ "$pass_count" -eq 142 ]] || \
  die "expected 142 exact surface matches, found $pass_count"

proxy_original_count=$(find "$surface_root/proxy-exclusions/original" \
  -type f -name '*.edn' | wc -l)
proxy_recovered_count=$(find "$surface_root/proxy-exclusions/recovered" \
  -type f -name '*.edn' | wc -l)
[[ "$proxy_original_count" -eq 142 && "$proxy_recovered_count" -eq 142 ]] || \
  die "proxy-exclusion ledgers are incomplete"
metadata_type_original_count=$(find "$surface_root/metadata-types/original" \
  -type f -name '*.txt' | wc -l)
metadata_type_recovered_count=$(find "$surface_root/metadata-types/recovered" \
  -type f -name '*.txt' | wc -l)
[[ "$metadata_type_original_count" -eq 142 && \
   "$metadata_type_recovered_count" -eq 142 ]] || \
  die "metadata-type ledgers are incomplete"
semantic_original_count=$(find "$surface_root/semantic-proxies/original" \
  -type f -name '*.edn' | wc -l)
semantic_recovered_count=$(find "$surface_root/semantic-proxies/recovered" \
  -type f -name '*.edn' | wc -l)
[[ "$semantic_original_count" -eq 142 && \
   "$semantic_recovered_count" -eq 142 ]] || \
  die "semantic metadata-proxy ledgers are incomplete"

side_ledger_relation="$surface_root/side-ledger-relations.tsv"
prepare_java_tmp side_ledger_java_tmp side-ledger-comparison
/usr/bin/timeout --signal=TERM --kill-after=15s \
  "${setup_timeout_seconds}s" \
  "$java_bin" -XX:+PerfDisableSharedMem \
  -Djava.io.tmpdir="$side_ledger_java_tmp" \
  -cp "$datomic_home/lib/*" clojure.main \
  "$surface_ledger_comparator" \
  "$surface_root/proxy-exclusions/original" \
  "$surface_root/proxy-exclusions/recovered" \
  "$surface_root/semantic-proxies/original" \
  "$surface_root/semantic-proxies/recovered" \
  "$surface_root/metadata-types/original" \
  "$surface_root/metadata-types/recovered" \
  "$source_root" "$namespace_index" "$tools_analyzer_jar" \
  "$side_ledger_relation" "$compiler_option_original_semantic" \
  "$compiler_option_source_semantic" "$compiler_option_original_types" \
  "$compiler_option_source_types" \
  >"$surface_root/side-ledger-comparison.stdout" \
  2>"$surface_root/side-ledger-comparison.stderr"
require_empty_java_tmp "$side_ledger_java_tmp" side-ledger-comparison
[[ $(awk 'END {print NR - 1}' "$side_ledger_relation") -eq 142 ]] || \
  die "side-ledger relation does not contain 142 namespaces"
[[ $(awk -F '\t' 'NR > 1 {n += $5} END {print n + 0}' \
      "$side_ledger_relation") -eq 0 ]] || \
  die "oracle/AOT retained an unexplained proxy scratch Var"
[[ $(awk -F '\t' \
  'NR > 1 && $9 == "contract-equal-after-exact-lane-origin-validation" {n++}
   END {print n + 0}' "$side_ledger_relation") -eq 142 ]] || \
  die "semantic proxy relation lacks exact lane-origin validation for all namespaces"
grep -Fq 'bundled_source_owner_sha256=6d97ead2cf4a0fd350b038b6bc6f65cc2f3ac924723473329d5a2b7543ae5d12' \
  "$surface_root/side-ledger-comparison.stdout" || \
  die "semantic metadata-proxy source ownership was not proven"
grep -Fqx 'semantic.proxy.runtime-class.projected=true' \
  "$side_ledger_relation.compiler-option.properties" || \
  die "compiler-option semantic proxy was not compared by stable contract"
grep -Fqx 'semantic.proxy.oracle.origin=classpath-bytecode' \
  "$side_ledger_relation.compiler-option.properties" || \
  die "compiler-option oracle semantic proxy lacks exact AOT origin evidence"
grep -Fqx 'semantic.proxy.recovered.origin=source-generated' \
  "$side_ledger_relation.compiler-option.properties" || \
  die "compiler-option recovered semantic proxy lacks exact source origin evidence"
grep -Fqx 'semantic.proxy.origin-evidence.exact=true' \
  "$side_ledger_relation.compiler-option.properties" || \
  die "compiler-option semantic proxy lane-origin evidence is incomplete"
grep -Fqx 'metadata.types.semantic-proxy.projected=true' \
  "$side_ledger_relation.compiler-option.properties" || \
  die "compiler-option metadata types were not compared after the constrained semantic projection"
[[ $(awk -F '\t' \
  'NR > 1 && $11 == "exact-after-origin-validated-semantic-proxy-projection" {n++} END {print n + 0}' \
  "$side_ledger_relation") -eq 142 ]] || \
  die "metadata-type side-ledger relation is not exact for all 142 namespaces"

capture_and_audit_classpaths post
cmp -s "$work_root/classpath-pre/original.actual.txt" \
  "$work_root/classpath-post/original.actual.txt" || \
  die "original actual JVM classpath changed during surface validation"
cmp -s "$work_root/classpath-pre/candidate.actual.txt" \
  "$work_root/classpath-post/candidate.actual.txt" || \
  die "candidate actual JVM classpath changed during surface validation"
if ! diff -qr "$work_root/classpath-pre/original" \
    "$work_root/classpath-post/original" \
    >"$work_root/original-classpath-runtime.diff"; then
  die "original classpath origins/content changed during surface validation"
fi
if ! diff -qr "$work_root/classpath-pre/candidate" \
    "$work_root/classpath-post/candidate" \
    >"$work_root/candidate-classpath-runtime.diff"; then
  die "candidate classpath origins/content changed during surface validation"
fi

[[ -z $(find "$java_tmp_root" -mindepth 1 ! -type d -print -quit) ]] || \
  die "isolated Java temp root retained a file after validation"

echo "runtime Var surfaces match for all 142 namespaces"
echo "exact typed Var metadata, root identities, and classes match"
echo "proxy exclusions are validated against exact source type/interface identities"
echo "semantic proxy origins are exact AOT/source lane evidence before stable projection"
echo "metadata type ledgers match after the sole origin-validated semantic proxy projection"
echo "actual JVM-expanded classpath origins/hashes exclude licensed implementation AOT"
echo "all per-probe java.io.tmpdir roots were isolated and empty after use"
echo "details: $surface_root"
