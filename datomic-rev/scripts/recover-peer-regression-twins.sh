#!/usr/bin/bash -p

set -euo pipefail
umask 077
unset BASH_ENV ENV CDPATH GLOBIGNORE JAVA_TOOL_OPTIONS JDK_JAVA_OPTIONS \
  _JAVA_OPTIONS CLASSPATH JAVA_HOME LD_PRELOAD LD_LIBRARY_PATH
IFS=$' \t\n'
export LC_ALL=C TZ=UTC

script_dir=$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd)
project_dir=$(cd -- "$script_dir/.." && pwd)
datomic_home=${1:-"$project_dir/../../datomic/datomic-pro-1.0.7277"}
run_a=${2:-/tmp/datomic-goal2-peer-decompile-metadata-v3}
run_b=${3:-/tmp/datomic-goal2-peer-decompile-metadata-v4}
evidence_dir=${4:-/tmp/datomic-goal2-peer-decompile-metadata-v3-v4-evidence}
jobs=${JOBS:-8}
namespace_timeout_seconds=${DATOMIC_REV_NAMESPACE_TIMEOUT_SECONDS:-180}
setup_timeout_seconds=${DATOMIC_REV_SETUP_TIMEOUT_SECONDS:-600}
recovery_timeout_seconds=${DATOMIC_REV_RECOVERY_TIMEOUT_SECONDS:-3600}
validation_timeout_seconds=${DATOMIC_REV_VALIDATION_TIMEOUT_SECONDS:-7200}

java_root=${DATOMIC_RECOVERY_JAVA_ROOT:-/usr/lib/jvm/java-21-openjdk-amd64}
[[ "$java_root" != *:* && "$java_root" != *$'\t'* && \
   "$java_root" != *$'\n'* ]] || {
  echo "Peer decompiler regression twins: invalid Java root path" >&2
  exit 1
}
safe_path="$java_root/bin:/usr/bin:/bin"
PATH=$safe_path
export PATH

java_bin="$java_root/bin/java"
runner="$script_dir/decompile-clojure.sh"
surface_validator="$script_dir/compare-namespace-surfaces.sh"
source_form_comparator="$script_dir/compare_peer_source_forms.clj"
historical_proxy_fixture="$script_dir/fixtures/peer-ledger-guards/historical-proxy-class-collapse.tsv"
source_form_fixture_root="$script_dir/fixtures/peer-source-form-guards"
decompiler_src="$project_dir/tools/tools.decompiler/src"
decompiler_jar="$project_dir/tools/tools.decompiler/target/tools.decompiler-0.1.0-alpha1-standalone.jar"
distribution_tsv="$project_dir/transactor/baseline/distribution-jars.tsv"
namespace_index="$project_dir/reports/source-index/namespaces.tsv"
peer_jar="$datomic_home/peer-1.0.7277.jar"
expected_peer_sha=cb55c9d01e155f9965e3332cd8ef70be42cdefacb50c072076e9ecf22b9a23ba
expected_java_sha=377196a32c5e4442b604bbf36add1c49cfe8680cd27edd693b60872e58895e9e
expected_decompiler_jar_sha=e96ed7c66a6a2bbf2586fe856f8159dc011c63f6cdb7b1f25d7037ae6a611928
expected_jdk_membership_sha=5a51bde8de0dadf80002781777ca705fd19c5a584e508078895813bcf985b93c
expected_jdk_content_manifest_sha=7a4e525926fccf4060f6643eb49ddf7841c87d8879c85a343ef720125a256c4d
expected_peer_runner_sha=659b9ea26156517e2ecf3fe06763515b7eba26f7293d2659983b7f88e9cf7a03
expected_peer_worker_sha=04e6608fbeb8510d58bc3838969fe0116ee2105080d002ea6d9fbe2b86697ff4
expected_peer_emitter_sha=a1ca4454920514f650bbcc76034e5269e20f083a4ab33806678feef27429a497
expected_structural_emitter_sha=f82eb4a09c903a8325d17f2f54ae4aebc6457f4de6ede8f95e363d8c82a13c86
expected_nano_sanitizer_wrapper_sha=e906b8e5ae56207677bb45457519d782fba31fabfe539bcd4465e089d0566186

ledger=
failure_recorded=0
finalized=0
current_phase=bootstrap

record_failure() {
  local message=${1//$'\t'/ }
  message=${message//$'\n'/ }
  if [[ -n "$ledger" && -f "$ledger" && "$failure_recorded" -eq 0 ]]; then
    printf 'FAIL\t%s\t1\t%s\n' "$current_phase" "$message" >>"$ledger"
    failure_recorded=1
  fi
}

die() {
  record_failure "$*"
  echo "Peer decompiler regression twins: $*" >&2
  exit 1
}

on_exit() {
  local status=$?
  if [[ "$finalized" -eq 0 && "$failure_recorded" -eq 0 && \
        -n "$ledger" && -f "$ledger" ]]; then
    printf 'FAIL\t%s\t%s\tunhandled harness exit\n' \
      "$current_phase" "$status" >>"$ledger"
  fi
}
trap on_exit EXIT

[[ $- == *p* ]] || die "harness must run under /usr/bin/bash -p"
[[ "$jobs" =~ ^[1-9][0-9]*$ ]] || die "JOBS must be a positive integer"
[[ "$namespace_timeout_seconds" =~ ^[1-9][0-9]*$ && \
   "$namespace_timeout_seconds" -le 1800 ]] || \
  die "DATOMIC_REV_NAMESPACE_TIMEOUT_SECONDS must be 1..1800"
[[ "$setup_timeout_seconds" =~ ^[1-9][0-9]*$ && \
   "$setup_timeout_seconds" -le 3600 ]] || \
  die "DATOMIC_REV_SETUP_TIMEOUT_SECONDS must be 1..3600"
[[ "$recovery_timeout_seconds" =~ ^[1-9][0-9]*$ && \
   "$recovery_timeout_seconds" -le 7200 ]] || \
  die "DATOMIC_REV_RECOVERY_TIMEOUT_SECONDS must be 1..7200"
[[ "$validation_timeout_seconds" =~ ^[1-9][0-9]*$ && \
   "$validation_timeout_seconds" -le 14400 ]] || \
  die "DATOMIC_REV_VALIDATION_TIMEOUT_SECONDS must be 1..14400"

canonical_missing_ok() {
  realpath -m -- "$1"
}

assert_disjoint() {
  local left=$1
  local right=$2
  local relation=$3
  case "$right/" in
    "$left/"*) die "$relation overlap: $left is an ancestor of $right" ;;
  esac
  case "$left/" in
    "$right/"*) die "$relation overlap: $right is an ancestor of $left" ;;
  esac
}

for output_path in "$run_a" "$run_b" "$evidence_dir"; do
  [[ "$output_path" != *:* && "$output_path" != *$'\t'* && \
     "$output_path" != *$'\n'* ]] || die "invalid regression output path: $output_path"
  [[ ! -L "$output_path" ]] || die "regression output may not be a symlink: $output_path"
  if [[ -e "$output_path" ]] && \
     [[ -n $(find "$output_path" -mindepth 1 -print -quit 2>/dev/null) ]]; then
    die "refusing to overwrite non-empty regression output: $output_path"
  fi
done

project_abs=$(realpath "$project_dir")
datomic_abs=$(realpath "$datomic_home")
java_abs=$(realpath "$java_root")
run_a_abs=$(canonical_missing_ok "$run_a")
run_b_abs=$(canonical_missing_ok "$run_b")
evidence_abs=$(canonical_missing_ok "$evidence_dir")

assert_disjoint "$project_abs" "$datomic_abs" "repository/licensed distribution"
assert_disjoint "$run_a_abs" "$run_b_abs" "Peer twin roots"
assert_disjoint "$run_a_abs" "$evidence_abs" "Peer run/evidence roots"
assert_disjoint "$run_b_abs" "$evidence_abs" "Peer run/evidence roots"
for output_abs in "$run_a_abs" "$run_b_abs" "$evidence_abs"; do
  assert_disjoint "$output_abs" "$project_abs" "output/repository"
  assert_disjoint "$output_abs" "$datomic_abs" "output/licensed distribution"
  assert_disjoint "$output_abs" "$java_abs" "output/JDK input"
  assert_disjoint "$output_abs" /usr/bin "output/toolchain"
done

for required_file in "$java_bin" /usr/bin/bash /usr/bin/env "$runner" \
    "$surface_validator" "$source_form_comparator" "$decompiler_jar" \
    "$historical_proxy_fixture" "$distribution_tsv" "$namespace_index" \
    "$peer_jar"; do
  [[ -f "$required_file" && ! -L "$required_file" ]] || \
    die "required regular input is missing or symbolic: $required_file"
done
for required_dir in "$decompiler_src" "$project_dir/src-clj" \
    "$project_dir/src-java" "$project_dir/resources" \
    "$project_dir/transactor/src-clj" "$java_root" \
    "$source_form_fixture_root"; do
  [[ -d "$required_dir" && ! -L "$required_dir" ]] || \
    die "required input directory is missing or symbolic: $required_dir"
done

[[ $(sha256sum "$peer_jar" | awk '{print $1}') == "$expected_peer_sha" ]] || \
  die "licensed Peer hash mismatch"
[[ $(sha256sum "$java_bin" | awk '{print $1}') == "$expected_java_sha" ]] || \
  die "OpenJDK Java launcher hash mismatch"
[[ $(sha256sum "$decompiler_jar" | awk '{print $1}') == \
   "$expected_decompiler_jar_sha" ]] || die "standalone decompiler JAR hash mismatch"
[[ $(sha256sum "$runner" | awk '{print $1}') == \
   "$expected_peer_runner_sha" ]] || die "Peer recovery runner hash mismatch"
[[ $(sha256sum "$project_dir/scripts/decompile_clojure.clj" \
     | awk '{print $1}') == "$expected_peer_worker_sha" ]] || \
  die "Peer recovery worker hash mismatch"
[[ $(sha256sum "$project_dir/scripts/emit_namespace_surface.clj" \
     | awk '{print $1}') == "$expected_peer_emitter_sha" ]] || \
  die "Peer surface emitter hash mismatch"
[[ $(sha256sum "$project_dir/transactor/scripts/emit_structural_surface.clj" \
     | awk '{print $1}') == "$expected_structural_emitter_sha" ]] || \
  die "structural surface emitter hash mismatch"
[[ $(sha256sum "$project_dir/transactor/scripts/sanitize-nano-impl.sh" \
     | awk '{print $1}') == "$expected_nano_sanitizer_wrapper_sha" ]] || \
  die "Nano sanitizer wrapper hash mismatch"
mkdir -p "$run_a_abs" "$run_b_abs" "$evidence_abs/inputs" \
  "$evidence_abs/runs" "$evidence_abs/contexts/peer-v3/home" \
  "$evidence_abs/contexts/peer-v3/tmp" \
  "$evidence_abs/contexts/peer-v3/java-tmp" \
  "$evidence_abs/contexts/peer-v4/home" \
  "$evidence_abs/contexts/peer-v4/tmp" \
  "$evidence_abs/contexts/peer-v4/java-tmp" \
  "$evidence_abs/contexts/source-comparison/home" \
  "$evidence_abs/contexts/source-comparison/tmp" \
  "$evidence_abs/contexts/source-comparison/java-tmp" \
  "$evidence_abs/contexts/runtime/home" \
  "$evidence_abs/contexts/runtime/tmp" \
  "$evidence_abs/contexts/harness/home" \
  "$evidence_abs/contexts/harness/tmp" \
  "$evidence_abs/contexts/harness/java-tmp-historical" \
  "$evidence_abs/contexts/harness/java-tmp-source-positive" \
  "$evidence_abs/contexts/harness/java-tmp-source-tag-negative" \
  "$evidence_abs/contexts/harness/java-tmp-source-body-negative" \
  "$evidence_abs/contexts/harness/java-tmp-version-pre" \
  "$evidence_abs/contexts/harness/java-tmp-version-final"
[[ $(realpath "$run_a_abs") == "$run_a_abs" && \
   $(realpath "$run_b_abs") == "$run_b_abs" && \
   $(realpath "$evidence_abs") == "$evidence_abs" ]] || \
  die "regression root canonicalization changed during creation"

ledger="$evidence_abs/failure-ledger.tsv"
printf 'status\tphase\texit_status\tdetail\n' >"$ledger"
java_tmp_ledger="$evidence_abs/runs/java-tmp-checks.tsv"
printf 'label\tpath\tjava_io_tmpdir_explicit\tretained_entries\tstatus\n' \
  >"$java_tmp_ledger"
export HOME="$evidence_abs/contexts/harness/home"
export TMPDIR="$evidence_abs/contexts/harness/tmp"

record_empty_java_tmp() {
  local label=$1
  local destination=$2
  [[ -d "$destination" && ! -L "$destination" ]] || \
    die "isolated Java temp directory is missing or symbolic: $destination"
  local retained
  retained=$(find "$destination" -mindepth 1 -print | wc -l)
  printf '%s\t%s\ttrue\t%s\t%s\n' "$label" "$destination" "$retained" \
    "$([[ "$retained" -eq 0 ]] && printf PASS || printf FAIL)" \
    >>"$java_tmp_ledger"
  [[ "$retained" -eq 0 ]] || \
    die "$label retained content in isolated Java temp: $destination"
}

"$java_bin" -XX:+PerfDisableSharedMem \
  -Djava.io.tmpdir="$evidence_abs/contexts/harness/java-tmp-version-pre" \
  -version 2>&1 | grep -q 'openjdk version "21.0.12"' || \
  die "exact OpenJDK 21.0.12 runtime is required"
record_empty_java_tmp version-pre \
  "$evidence_abs/contexts/harness/java-tmp-version-pre"

command_names=(awk basename bash cat cmp comm cp diff dirname env find grep java \
  ln mkdir mktemp mv readlink realpath rm rmdir sed sha256sum sort stat tail tee \
  timeout unzip wc xargs zipinfo)
command_paths="$evidence_abs/inputs/command-paths.tsv"
printf 'command\tresolved_path\tsha256\n' >"$command_paths"
for command_name in "${command_names[@]}"; do
  command_path=$(command -v "$command_name") || die "missing command: $command_name"
  command_path=$(realpath "$command_path")
  [[ -f "$command_path" && ! -L "$command_path" ]] || \
    die "resolved command is not a regular file: $command_name -> $command_path"
  printf '%s\t%s\t%s\n' "$command_name" "$command_path" \
    "$(sha256sum "$command_path" | awk '{print $1}')" >>"$command_paths"
done
[[ $(awk -F '\t' '$1 == "bash" {print $2}' "$command_paths") == \
   /usr/bin/bash ]] || die "bash does not resolve to /usr/bin/bash"
[[ $(awk -F '\t' '$1 == "env" {print $2}' "$command_paths") == \
   /usr/bin/env ]] || die "env does not resolve to /usr/bin/env"
[[ $(awk -F '\t' '$1 == "java" {print $2}' "$command_paths") == \
   "$(realpath "$java_bin")" ]] || die "java does not resolve to the pinned launcher"

tool_files=(
  scripts/recover-peer-regression-twins.sh
  scripts/decompile-clojure.sh
  scripts/decompile_clojure.clj
  scripts/validate_decompiler.clj
  scripts/validate_clojure.clj
  scripts/compare-namespace-surfaces.sh
  scripts/compare-one-namespace-surface.sh
  scripts/emit_namespace_surface.clj
  scripts/validate-all-namespaces.sh
  scripts/require-one-namespace.sh
  scripts/require_namespace.clj
  scripts/validate-peer-resources.sh
  scripts/validate_recovered_behaviors.clj
  scripts/CompileSources.java
  scripts/PrintRuntimeClasspath.java
  scripts/audit-peer-runtime-classpath.sh
  scripts/compare_peer_source_forms.clj
  scripts/compare_peer_surface_ledgers.clj
  scripts/fixtures/peer-ledger-guards/historical-proxy-class-collapse.tsv
  scripts/inspect_peer_semantic_proxy_origin.clj
  scripts/require-one-namespace-aligned.sh
  scripts/DeterministicJar.java
  tools/infinispan-compile-stubs/build_stubs.clj
  transactor/scripts/emit_structural_surface.clj
  transactor/scripts/sanitize-nano-impl.sh
  transactor/tools/SanitizeNanoImpl.java
  transactor/reports/stage-1-nano-entry-policy.tsv
  transactor/baseline/manifest.sha256
  transactor/src-clj/clojure/tools/reader/default_data_readers.clj
  transactor/src-clj/cognitect/hmac_authn.clj
  reports/source-index/namespaces.tsv
  reports/handwritten-java-sources.txt
  reports/peer-resource-paths.txt
)

shell_tools=(
  scripts/recover-peer-regression-twins.sh
  scripts/decompile-clojure.sh
  scripts/compare-namespace-surfaces.sh
  scripts/compare-one-namespace-surface.sh
  scripts/validate-all-namespaces.sh
  scripts/require-one-namespace.sh
  scripts/require-one-namespace-aligned.sh
  scripts/validate-peer-resources.sh
  scripts/audit-peer-runtime-classpath.sh
  transactor/scripts/sanitize-nano-impl.sh
)
shebang_inventory="$evidence_abs/inputs/shell-interpreters.tsv"
printf 'path\tshebang\tactual_invocation\tinterpreter\tinterpreter_sha256\n' \
  >"$shebang_inventory"
for shell_tool in "${shell_tools[@]}"; do
  shell_path="$project_dir/$shell_tool"
  [[ -f "$shell_path" && ! -L "$shell_path" ]] || \
    die "shell harness input is missing or symbolic: $shell_path"
  shebang=$(sed -n '1p' "$shell_path")
  [[ "$shebang" == '#!/usr/bin/bash -p' || \
     "$shebang" == '#!/usr/bin/env bash' ]] || \
    die "unrecognized shell harness shebang: $shell_tool -> $shebang"
  printf '%s\t%s\t%s\t%s\t%s\n' "$shell_tool" "$shebang" \
    '/usr/bin/bash -p SCRIPT' /usr/bin/bash \
    "$(sha256sum /usr/bin/bash | awk '{print $1}')" >>"$shebang_inventory"
done

input_paths="$evidence_abs/inputs/input-paths.txt"
{
  find "$decompiler_src" "$project_dir/src-clj" "$project_dir/src-java" \
    "$project_dir/resources" "$project_dir/transactor/src-clj" \
    -type f -print
  printf '%s\n' "$decompiler_jar" "$peer_jar" "$java_bin" \
    "$distribution_tsv" /usr/bin/bash /usr/bin/env
  for tool_file in "${tool_files[@]}"; do
    printf '%s\n' "$project_dir/$tool_file"
  done
  find "$project_dir/transactor/baseline" -type f -print
  find "$source_form_fixture_root" -type f -print
  awk -F '\t' -v root="$datomic_abs" 'NR > 1 {print root "/" $2}' \
    "$distribution_tsv"
  awk -F '\t' 'NR > 1 {print $2}' "$command_paths"
} | sort -u >"$input_paths"

while IFS= read -r input_path; do
  [[ -f "$input_path" && ! -L "$input_path" ]] || \
    die "sealed input is missing, non-regular, or symbolic: $input_path"
  for output_abs in "$run_a_abs" "$run_b_abs" "$evidence_abs"; do
    assert_disjoint "$output_abs" "$(realpath "$input_path")" \
      "output/sealed input"
  done
done <"$input_paths"

input_manifest="$evidence_abs/inputs/pre.sha256"
while IFS= read -r input_path; do
  sha256sum -- "$input_path"
done <"$input_paths" >"$input_manifest"
input_manifest_sha=$(sha256sum "$input_manifest" | awk '{print $1}')
sha256sum -c "$input_manifest" >"$evidence_abs/inputs/pre-verification.log"

write_input_membership() {
  local destination=$1
  {
    printf 'root\ttype\tmode\trelative_path\tlink_target\n'
    for membership_row in \
      "decompiler-src|$decompiler_src" \
      "peer-reference|$project_dir/src-clj" \
      "peer-java|$project_dir/src-java" \
      "peer-resources|$project_dir/resources" \
      "transactor-source-reference|$project_dir/transactor/src-clj" \
      "source-form-guard-fixtures|$source_form_fixture_root" \
      "proxy-guard-fixtures|$(dirname "$historical_proxy_fixture")"; do
      membership_name=${membership_row%%|*}
      membership_root=${membership_row#*|}
      if [[ -n $(find "$membership_root" -mindepth 1 ! -type f ! -type d -print -quit) ]]; then
        die "sealed input tree contains a symlink or special entry: $membership_root"
      fi
      find "$membership_root" -mindepth 1 \( -type d -o -type f \) \
        -printf "$membership_name\t%y\t%m\t%P\t%l\n"
    done | sort
  } >"$destination"
}

membership_manifest="$evidence_abs/inputs/pre-membership.tsv"
write_input_membership "$membership_manifest"
membership_sha=$(sha256sum "$membership_manifest" | awk '{print $1}')

write_jdk_membership() {
  find "$java_root" -mindepth 1 -printf '%y\t%m\t%P\t%l\n' | sort >"$1"
}

jdk_membership="$evidence_abs/inputs/jdk-membership.tsv"
jdk_content_manifest="$evidence_abs/inputs/jdk-content.sha256"
write_jdk_membership "$jdk_membership"
find -L "$java_root" -type f -print0 | sort -z | xargs -0 sha256sum \
  >"$jdk_content_manifest"
[[ $(sha256sum "$jdk_membership" | awk '{print $1}') == \
   "$expected_jdk_membership_sha" ]] || die "full JDK membership differs from pinned image"
[[ $(sha256sum "$jdk_content_manifest" | awk '{print $1}') == \
   "$expected_jdk_content_manifest_sha" ]] || \
  die "full JDK content differs from pinned image"
sha256sum -c "$jdk_content_manifest" \
  >"$evidence_abs/inputs/jdk-pre-verification.log"

verify_distribution_inputs() {
  while IFS=$'\t' read -r _ distribution_path expected_sha _; do
    [[ "$distribution_path" == distribution_path ]] && continue
    distribution_file="$datomic_abs/$distribution_path"
    [[ -f "$distribution_file" && ! -L "$distribution_file" ]] || \
      die "distribution input missing or symbolic: $distribution_file"
    [[ $(sha256sum "$distribution_file" | awk '{print $1}') == \
       "$expected_sha" ]] || die "distribution input hash mismatch: $distribution_path"
  done <"$distribution_tsv"
}

verify_inputs_unchanged() {
  local phase=$1
  local phase_membership="$evidence_abs/inputs/$phase-membership.tsv"
  local phase_jdk_membership="$evidence_abs/inputs/$phase-jdk-membership.tsv"
  [[ $(sha256sum "$input_manifest" | awk '{print $1}') == \
     "$input_manifest_sha" ]] || die "input manifest changed during $phase"
  sha256sum -c "$input_manifest" >"$evidence_abs/inputs/$phase-verification.log" || \
    die "a sealed input changed during $phase"
  [[ $(sha256sum "$membership_manifest" | awk '{print $1}') == \
     "$membership_sha" ]] || die "input membership manifest changed during $phase"
  write_input_membership "$phase_membership"
  cmp -s "$membership_manifest" "$phase_membership" || {
    diff -u "$membership_manifest" "$phase_membership" \
      >"$evidence_abs/inputs/$phase-membership.diff" || true
    die "sealed input-tree membership changed during $phase"
  }
  write_jdk_membership "$phase_jdk_membership"
  cmp -s "$jdk_membership" "$phase_jdk_membership" || \
    die "full JDK membership changed during $phase"
  sha256sum -c "$jdk_content_manifest" \
    >"$evidence_abs/inputs/$phase-jdk-verification.log" || \
    die "full JDK content changed during $phase"
  verify_distribution_inputs
  printf 'INPUTS_UNCHANGED %s\n' "$phase" \
    >>"$evidence_abs/inputs/$phase-verification.log"
}

write_tree_state() {
  local root=$1
  local membership_destination=$2
  local content_destination=$3
  if [[ -n $(find "$root" -mindepth 1 ! -type f ! -type d ! -type l -print -quit) ]]; then
    die "sealed output tree contains a special entry: $root"
  fi
  while IFS= read -r symlink_path; do
    [[ -e "$symlink_path" ]] || \
      die "sealed output tree contains a broken symlink: $symlink_path"
  done < <(find "$root" -type l | sort)
  if ! find -L "$root" -mindepth 1 -print >/dev/null; then
    die "sealed output tree contains a symlink cycle: $root"
  fi
  (cd "$root" && find . -mindepth 1 -printf '%y\t%m\t%P\t%l\n' | sort) \
    >"$membership_destination"
  (cd "$root" && find -L . -type f -print0 | sort -z | xargs -0 sha256sum) \
    >"$content_destination"
}

declare -A output_membership_hashes
declare -A output_content_hashes
declare -A output_roots

seal_output_tree() {
  local label=$1
  local root=$2
  local membership_file="$evidence_abs/runs/$label-output-membership.tsv"
  local content_file="$evidence_abs/runs/$label-output.sha256"
  write_tree_state "$root" "$membership_file" "$content_file"
  output_membership_hashes[$label]=$(sha256sum "$membership_file" | awk '{print $1}')
  output_content_hashes[$label]=$(sha256sum "$content_file" | awk '{print $1}')
  output_roots[$label]=$root
}

verify_output_tree() {
  local label=$1
  local phase=$2
  local membership_file="$evidence_abs/runs/$label-$phase-membership.tsv"
  local content_file="$evidence_abs/runs/$label-$phase.sha256"
  write_tree_state "${output_roots[$label]}" "$membership_file" "$content_file"
  [[ $(sha256sum "$membership_file" | awk '{print $1}') == \
     "${output_membership_hashes[$label]}" ]] || \
    die "$label output membership changed during $phase"
  [[ $(sha256sum "$content_file" | awk '{print $1}') == \
     "${output_content_hashes[$label]}" ]] || \
    die "$label output content changed during $phase"
}

run_recovery() {
  local label=$1
  local destination=$2
  local context="$evidence_abs/contexts/$label"
  current_phase="recover-$label"
  /usr/bin/timeout --signal=TERM --kill-after=30s \
    "${recovery_timeout_seconds}s" \
    /usr/bin/env -i PATH="$safe_path" HOME="$context/home" \
    TMPDIR="$context/tmp" LC_ALL=C TZ=UTC \
    DATOMIC_RECOVERY_JAVA_ROOT="$java_root" \
    DATOMIC_RECOVERY_JAVA_TMPDIR="$context/java-tmp" \
    /usr/bin/bash -p "$runner" "$datomic_abs" "$destination" \
    >"$evidence_abs/runs/$label.stdout" \
    2>"$evidence_abs/runs/$label.stderr"
  record_empty_java_tmp "$label-recovery" "$context/java-tmp"
  [[ $(find "$destination/datomic" -type f -name '*.clj' \
        | awk 'END {print NR + 0}') == 142 ]] || \
    die "$label source count is not 142"
  grep -q ':success-count 142' "$destination/decompile-report.edn" || \
    die "$label did not report 142 successful namespaces"
  grep -q ':failure-count 0' "$destination/decompile-report.edn" || \
    die "$label recorded a decompiler failure"
  [[ -z $(find "$destination" -mindepth 1 ! -type f ! -type d -print -quit) ]] || \
    die "$label output contains a symlink or special entry"
  (cd "$destination" && \
    find datomic -type f -name '*.clj' -printf '%p\0' | sort -z \
      | xargs -0 sha256sum) \
    >"$evidence_abs/runs/$label-source-manifest.sha256"
  seal_output_tree "$label" "$destination"
}

current_phase=preflight-input-verification
verify_distribution_inputs
current_phase=historical-proxy-guard-fixtures
/usr/bin/timeout --signal=TERM --kill-after=15s \
  "${setup_timeout_seconds}s" \
  /usr/bin/env -i PATH="$safe_path" \
  HOME="$evidence_abs/contexts/harness/home" \
  TMPDIR="$evidence_abs/contexts/harness/tmp" LC_ALL=C TZ=UTC \
  "$java_bin" -XX:+PerfDisableSharedMem \
  -Djava.io.tmpdir="$evidence_abs/contexts/harness/java-tmp-historical" \
  -cp "$datomic_abs/lib/*" clojure.main \
  "$project_abs/scripts/compare_peer_surface_ledgers.clj" \
  --historical-proxy-self-test "$historical_proxy_fixture" \
  "$project_abs/transactor/src-clj" \
  "$evidence_abs/runs/historical-proxy-negative-witnesses.tsv" \
  >"$evidence_abs/runs/historical-proxy-negative-witnesses.stdout" \
  2>"$evidence_abs/runs/historical-proxy-negative-witnesses.stderr"
record_empty_java_tmp historical-proxy-guard \
  "$evidence_abs/contexts/harness/java-tmp-historical"
grep -Fqx \
  'PEER_HISTORICAL_PROXY_GUARD_RESULT witnesses=2 rejected=2 corrected_source_bound=2' \
  "$evidence_abs/runs/historical-proxy-negative-witnesses.stdout" || \
  die "historical proxy Class-collapse guard fixtures did not pass"
[[ $(awk 'END {print NR - 1}' \
  "$evidence_abs/runs/historical-proxy-negative-witnesses.tsv") == 2 ]] || \
  die "historical proxy Class-collapse evidence is incomplete"

current_phase=source-form-guard-fixtures
source_guard_reference="$source_form_fixture_root/reference"
source_guard_results="$source_form_fixture_root/results"
source_guard_original_surfaces="$source_form_fixture_root/surfaces/original"
source_guard_recovered_surfaces="$source_form_fixture_root/surfaces/recovered"
/usr/bin/timeout --signal=TERM --kill-after=15s \
  "${setup_timeout_seconds}s" \
  /usr/bin/env -i PATH="$safe_path" \
  HOME="$evidence_abs/contexts/harness/home" \
  TMPDIR="$evidence_abs/contexts/harness/tmp" LC_ALL=C TZ=UTC \
  "$java_bin" -XX:+PerfDisableSharedMem \
  -Djava.io.tmpdir="$evidence_abs/contexts/harness/java-tmp-source-positive" \
  -cp "$datomic_abs/lib/*" clojure.main "$source_form_comparator" \
  "$source_guard_reference" "$source_form_fixture_root/recovered-positive" \
  "$source_form_fixture_root/positive-index.tsv" \
  "$evidence_abs/runs/source-form-positive.tsv" \
  "$source_guard_results" "$source_guard_original_surfaces" \
  "$source_guard_recovered_surfaces" \
  >"$evidence_abs/runs/source-form-positive.stdout" \
  2>"$evidence_abs/runs/source-form-positive.stderr"
record_empty_java_tmp source-form-positive \
  "$evidence_abs/contexts/harness/java-tmp-source-positive"
[[ $(awk -F '\t' \
  'NR > 1 && ($9 == "location-metadata-or-format-only" || $9 == "oracle-proved-protocol-scaffold") {n++} END {print n + 0}' \
  "$evidence_abs/runs/source-form-positive.tsv") == 2 ]] || \
  die "source-form positive guard fixtures did not both pass by the intended relation"

run_source_form_negative() {
  local label=$1
  local regenerated_root=$2
  local index_file=$3
  local java_tmp_dir=$4
  local report="$evidence_abs/runs/source-form-$label.tsv"
  local status=0
  if /usr/bin/timeout --signal=TERM --kill-after=15s \
    "${setup_timeout_seconds}s" \
    /usr/bin/env -i PATH="$safe_path" \
    HOME="$evidence_abs/contexts/harness/home" \
    TMPDIR="$evidence_abs/contexts/harness/tmp" LC_ALL=C TZ=UTC \
    "$java_bin" -XX:+PerfDisableSharedMem \
    -Djava.io.tmpdir="$java_tmp_dir" \
    -cp "$datomic_abs/lib/*" clojure.main "$source_form_comparator" \
    "$source_guard_reference" "$regenerated_root" "$index_file" \
    "$report" "$source_guard_results" "$source_guard_original_surfaces" \
    "$source_guard_recovered_surfaces" \
    >"$evidence_abs/runs/source-form-$label.stdout" \
    2>"$evidence_abs/runs/source-form-$label.stderr"; then
    die "source-form negative unexpectedly passed: $label"
  else
    status=$?
  fi
  [[ "$status" -eq 2 ]] || \
    die "source-form negative failed with non-contract status $status: $label"
  [[ $(awk -F '\t' \
    'NR == 2 && $9 == "semantic-body-or-structure" {print 1}' \
    "$report") == 1 ]] || \
    die "source-form negative was rejected for the wrong relation: $label"
  record_empty_java_tmp "source-form-$label" "$java_tmp_dir"
}

run_source_form_negative tag-negative \
  "$source_form_fixture_root/recovered-tag-negative" \
  "$source_form_fixture_root/tag-negative-index.tsv" \
  "$evidence_abs/contexts/harness/java-tmp-source-tag-negative"
run_source_form_negative body-negative \
  "$source_form_fixture_root/recovered-body-negative" \
  "$source_form_fixture_root/body-negative-index.tsv" \
  "$evidence_abs/contexts/harness/java-tmp-source-body-negative"
run_recovery peer-v3 "$run_a_abs"
current_phase=post-v3-input-verification
verify_inputs_unchanged post-v3
run_recovery peer-v4 "$run_b_abs"
current_phase=post-v4-input-verification
verify_inputs_unchanged post-v4

current_phase=twin-source-identity
cmp -s "$evidence_abs/runs/peer-v3-source-manifest.sha256" \
  "$evidence_abs/runs/peer-v4-source-manifest.sha256" || \
  die "Peer twin source manifests differ"
if ! diff -qr "$run_a_abs/datomic" "$run_b_abs/datomic" \
  >"$evidence_abs/runs/source-tree.diff"; then
  die "Peer twin source trees differ"
fi

current_phase=runtime-exact-surface-validation
runtime_validation="$evidence_abs/runtime-validation"
/usr/bin/timeout --signal=TERM --kill-after=30s \
  "${validation_timeout_seconds}s" \
  /usr/bin/env -i PATH="$safe_path" \
  HOME="$evidence_abs/contexts/runtime/home" \
  TMPDIR="$evidence_abs/contexts/runtime/tmp" LC_ALL=C TZ=UTC \
  JOBS="$jobs" DATOMIC_REV_CANDIDATE_SOURCE_ROOT="$run_a_abs" \
  DATOMIC_REV_JAVA_BIN="$java_bin" DATOMIC_REV_SAFE_PATH="$safe_path" \
  DATOMIC_REV_NAMESPACE_TIMEOUT_SECONDS="$namespace_timeout_seconds" \
  DATOMIC_REV_SETUP_TIMEOUT_SECONDS="$setup_timeout_seconds" \
  DATOMIC_REV_VALIDATION_TIMEOUT_SECONDS="$validation_timeout_seconds" \
  DATOMIC_RECOVERY_JAVA_ROOT="$java_root" \
  /usr/bin/bash -p "$surface_validator" "$datomic_abs" "$runtime_validation" \
  >"$evidence_abs/runs/runtime-validation.stdout" \
  2>"$evidence_abs/runs/runtime-validation.stderr"
grep -q 'runtime Var surfaces match for all 142 namespaces' \
  "$evidence_abs/runs/runtime-validation.stdout" || \
  die "Peer runtime validation did not report 142 exact matching surfaces"
source_load_passes=$(grep -h '^PASS ' \
  "$runtime_validation/source-validation/namespace-logs"/*.out | wc -l)
[[ "$source_load_passes" == 142 ]] || \
  die "Peer source-only validation evidence is not exactly 142 PASS records"
inner_java_tmp_passes=$(grep -Fxc \
  'all per-probe java.io.tmpdir roots were isolated and empty after use' \
  "$evidence_abs/runs/runtime-validation.stdout" || true)
[[ "$inner_java_tmp_passes" == 2 ]] || \
  die "nested source/surface validators did not both attest isolated empty Java temp roots"

current_phase=post-runtime-gate-pre-source-seal
verify_inputs_unchanged post-runtime-gate-pre-source
verify_output_tree peer-v3 post-runtime-gate-pre-source
verify_output_tree peer-v4 post-runtime-gate-pre-source
seal_output_tree runtime-validation "$runtime_validation"

current_phase=peer-source-semantic-relation
source_relation="$evidence_abs/runs/peer-reference-source-form-relation.tsv"
/usr/bin/timeout --signal=TERM --kill-after=15s \
  "${setup_timeout_seconds}s" \
  /usr/bin/env -i PATH="$safe_path" \
  HOME="$evidence_abs/contexts/source-comparison/home" \
  TMPDIR="$evidence_abs/contexts/source-comparison/tmp" LC_ALL=C TZ=UTC \
  "$java_bin" -XX:+PerfDisableSharedMem \
  -Djava.io.tmpdir="$evidence_abs/contexts/source-comparison/java-tmp" \
  -cp "$datomic_abs/lib/*" clojure.main \
  "$source_form_comparator" "$project_abs/src-clj" "$run_a_abs" \
  "$namespace_index" "$source_relation" \
  "$runtime_validation/surfaces/results" \
  "$runtime_validation/surfaces/original" \
  "$runtime_validation/surfaces/recovered" \
  >"$evidence_abs/runs/source-form-comparison.stdout" \
  2>"$evidence_abs/runs/source-form-comparison.stderr"
record_empty_java_tmp source-form-comparison \
  "$evidence_abs/contexts/source-comparison/java-tmp"
[[ $(awk 'END {print NR - 1}' "$source_relation") == 142 ]] || \
  die "Peer source-form relation does not contain 142 namespaces"
body_differences=$(awk -F '\t' \
  'NR > 1 && $9 == "semantic-body-or-structure" {n++} END {print n + 0}' \
  "$source_relation")
[[ "$body_differences" == 0 ]] || \
  die "Peer reference/recovery relation contains semantic source-body deltas"
current_phase=post-source-comparison-reverification
verify_inputs_unchanged post-source-comparison
verify_output_tree peer-v3 post-source-comparison
verify_output_tree peer-v4 post-source-comparison
verify_output_tree runtime-validation post-source-comparison

candidate_classpath_summary="$runtime_validation/classpath-post/candidate/candidate-summary.properties"
for required_claim in \
    'licensed.peer.archive.present=false' \
    'licensed.transactor.archive.present=false' \
    'licensed.core2.archive.present=false' \
    'licensed.nano.original.present=false' \
    'licensed.implementation.archive-payload.present=false' \
    'candidate.directory.nested-archive.present=false' \
    'vendor.nano.keystore.payload.present=false' \
    'sanitized.nano.derivative.entries=1' \
    'licensed.datomic.clojure.aot.present=false' \
    'origin.and.hash.inventory.complete=true'; do
  grep -Fqx "$required_claim" "$candidate_classpath_summary" || \
    die "candidate classpath evidence is missing: $required_claim"
done

symlink_inventory="$evidence_abs/runs/runtime-symlinks.tsv"
printf 'path\ttarget\ttarget_sha256\tdistribution_path\n' >"$symlink_inventory"
while IFS= read -r symlink_path; do
  symlink_target=$(readlink -f "$symlink_path")
  [[ "$symlink_target" == "$datomic_abs/lib/"*.jar ]] || \
    die "runtime validation created an unexpected symlink: $symlink_path -> $symlink_target"
  target_sha=$(sha256sum "$symlink_target" | awk '{print $1}')
  distribution_path="lib/${symlink_target##*/}"
  expected_target_sha=$(awk -F '\t' -v path="$distribution_path" \
    '$2 == path {print $3}' "$distribution_tsv")
  [[ -n "$expected_target_sha" && "$target_sha" == "$expected_target_sha" ]] || \
    die "runtime dependency symlink target is absent from distribution inventory"
  printf '%s\t%s\t%s\t%s\n' "${symlink_path#$evidence_abs/}" \
    "$symlink_target" "$target_sha" "$distribution_path" >>"$symlink_inventory"
done < <(find "$runtime_validation" -type l | sort)

surface_results="$runtime_validation/surfaces/results.tsv"
surface_matches=$(grep -c $'^DATOMIC_SURFACE_RESULT\tPASS\t.*\texact\tnone\t' \
  "$surface_results" || true)
surface_failures=$(grep -c $'^DATOMIC_SURFACE_RESULT\tFAIL\t' \
  "$surface_results" || true)
[[ "$surface_matches" == 142 && "$surface_failures" == 0 ]] || \
  die "exact Peer surface result channel is not 142 PASS / 0 FAIL"
proxy_original_count=$(find "$runtime_validation/surfaces/proxy-exclusions/original" \
  -type f -name '*.edn' | wc -l)
proxy_recovered_count=$(find "$runtime_validation/surfaces/proxy-exclusions/recovered" \
  -type f -name '*.edn' | wc -l)
[[ "$proxy_original_count" == 142 && "$proxy_recovered_count" == 142 ]] || \
  die "proxy exclusion ledgers are incomplete"
metadata_type_original_count=$(find "$runtime_validation/surfaces/metadata-types/original" \
  -type f -name '*.txt' | wc -l)
metadata_type_recovered_count=$(find "$runtime_validation/surfaces/metadata-types/recovered" \
  -type f -name '*.txt' | wc -l)
[[ "$metadata_type_original_count" == 142 && \
   "$metadata_type_recovered_count" == 142 ]] || \
  die "metadata-type ledgers are incomplete"
semantic_proxy_original_count=$(find \
  "$runtime_validation/surfaces/semantic-proxies/original" \
  -type f -name '*.edn' | wc -l)
semantic_proxy_recovered_count=$(find \
  "$runtime_validation/surfaces/semantic-proxies/recovered" \
  -type f -name '*.edn' | wc -l)
[[ "$semantic_proxy_original_count" == 142 && \
   "$semantic_proxy_recovered_count" == 142 ]] || \
  die "semantic-proxy ledgers are incomplete"
semantic_origin_relations=$(awk -F '\t' \
  'NR > 1 && $9 == "contract-equal-after-exact-lane-origin-validation" {n++}
   END {print n + 0}' \
  "$runtime_validation/surfaces/side-ledger-relations.tsv")
[[ "$semantic_origin_relations" == 142 ]] || \
  die "semantic-proxy side relations lack exact lane-origin validation"

current_phase=final-output-reverification
verify_output_tree peer-v3 final
verify_output_tree peer-v4 final
verify_output_tree runtime-validation final
verify_inputs_unchanged final

source_byte_matches=$(awk -F '\t' \
  'NR > 1 && $9 == "byte-exact" {n++} END {print n + 0}' "$source_relation")
source_format_only=$(awk -F '\t' \
  'NR > 1 && $9 == "format-only" {n++} END {print n + 0}' "$source_relation")
source_location_metadata_only=$(awk -F '\t' \
  'NR > 1 && $9 == "location-metadata-or-format-only" {n++} END {print n + 0}' \
  "$source_relation")
source_protocol_scaffolds=$(awk -F '\t' \
  'NR > 1 && $9 == "oracle-proved-protocol-scaffold" {n++} END {print n + 0}' \
  "$source_relation")

"$java_bin" -XX:+PerfDisableSharedMem \
  -Djava.io.tmpdir="$evidence_abs/contexts/harness/java-tmp-version-final" \
  -version >"$evidence_abs/inputs/java-version.txt" 2>&1
record_empty_java_tmp version-final \
  "$evidence_abs/contexts/harness/java-tmp-version-final"
expected_java_tmp_labels=(version-pre historical-proxy-guard \
  source-form-positive source-form-tag-negative source-form-body-negative \
  peer-v3-recovery peer-v4-recovery source-form-comparison version-final)
[[ $(awk -F '\t' \
  'NR > 1 {rows++; labels[$1]++; if ($3 != "true" || $4 != 0 || $5 != "PASS") bad++}
   END {print (rows == 9 && length(labels) == 9 && bad == 0) ? "PASS" : "FAIL"}' \
  "$java_tmp_ledger") == PASS ]] || \
  die "outer harness Java temp ledger is not exactly 9 distinct PASS records"
for java_tmp_label in "${expected_java_tmp_labels[@]}"; do
  [[ $(awk -F '\t' -v label="$java_tmp_label" \
    'NR > 1 && $1 == label && $3 == "true" && $4 == 0 && $5 == "PASS" {n++}
     END {print n + 0}' "$java_tmp_ledger") == 1 ]] || \
    die "outer harness Java temp ledger is missing exact PASS: $java_tmp_label"
done
{
  printf 'status=PASS\n'
  printf 'input.peer.sha256=%s\n' "$expected_peer_sha"
  printf 'input.manifest.sha256=%s\n' "$input_manifest_sha"
  printf 'input.membership.sha256=%s\n' "$membership_sha"
  printf 'jdk.membership.sha256=%s\n' "$expected_jdk_membership_sha"
  printf 'jdk.content.manifest.sha256=%s\n' "$expected_jdk_content_manifest_sha"
  printf 'shell.interpreter=/usr/bin/bash\n'
  printf 'shell.privileged-mode=true\n'
  printf 'environment.jvm-and-bash-injection.sanitized=true\n'
  printf 'environment.home-and-tmp.isolated=true\n'
  printf 'java.binary=%s\n' "$(realpath "$java_bin")"
  printf 'java.binary.sha256=%s\n' "$expected_java_sha"
  printf 'java.version=OpenJDK-21.0.12+8-1-24.04-Ubuntu\n'
  printf 'decompiler.jar.sha256=%s\n' "$expected_decompiler_jar_sha"
  printf 'peer.recovery.runner.sha256=%s\n' "$expected_peer_runner_sha"
  printf 'peer.recovery.worker.sha256=%s\n' "$expected_peer_worker_sha"
  printf 'peer.surface.emitter.sha256=%s\n' "$expected_peer_emitter_sha"
  printf 'structural.surface.emitter.sha256=%s\n' \
    "$expected_structural_emitter_sha"
  printf 'nano.sanitizer.wrapper.sha256=%s\n' \
    "$expected_nano_sanitizer_wrapper_sha"
  printf 'configuration.jobs=%s\n' "$jobs"
  printf 'configuration.namespace-timeout-seconds=%s\n' "$namespace_timeout_seconds"
  printf 'configuration.setup-timeout-seconds=%s\n' "$setup_timeout_seconds"
  printf 'configuration.recovery-timeout-seconds=%s\n' "$recovery_timeout_seconds"
  printf 'configuration.validation-timeout-seconds=%s\n' "$validation_timeout_seconds"
  printf 'peer.v3=%s\n' "$run_a_abs"
  printf 'peer.v4=%s\n' "$run_b_abs"
  printf 'roots.pairwise.ancestor-disjoint=true\n'
  printf 'roots.disjoint.from.repository.distribution.inputs=true\n'
  printf 'regenerated.source.count=142\n'
  printf 'regenerated.source.trees.byte-identical=true\n'
  printf 'negative.proxy.java-lang-Class-collapse.witnesses=2\n'
  printf 'negative.proxy.java-lang-Class-collapse.rejected=2\n'
  printf 'source-form.guard.positive.pass=2\n'
  printf 'source-form.guard.semantic-negative.rejected=2\n'
  printf 'regenerated.source.manifest.sha256=%s\n' \
    "$(sha256sum "$evidence_abs/runs/peer-v3-source-manifest.sha256" | awk '{print $1}')"
  printf 'checked-in.peer.reference.modified=false\n'
  printf 'checked-in.peer.reference.byte-exact=%s\n' "$source_byte_matches"
  printf 'checked-in.peer.reference.format-only=%s\n' "$source_format_only"
  printf 'checked-in.peer.reference.location-metadata-or-format-only=%s\n' \
    "$source_location_metadata_only"
  printf 'checked-in.peer.reference.oracle-proved-protocol-scaffold=%s\n' \
    "$source_protocol_scaffolds"
  printf 'checked-in.peer.reference.semantic-body-or-structure=%s\n' \
    "$body_differences"
  printf 'source-only.namespace.loads.pass=%s\n' "$source_load_passes"
  printf 'runtime.namespace.surfaces.exact-metadata-view=true\n'
  printf 'runtime.namespace.surfaces.match=%s\n' "$surface_matches"
  printf 'runtime.proxy-exclusion-ledgers.validated-and-source-bound=142\n'
  printf 'runtime.semantic-proxy.origins.oracle=classpath-bytecode\n'
  printf 'runtime.semantic-proxy.origins.recovered=source-generated\n'
  printf 'runtime.semantic-proxy.origin-evidence.exact=true\n'
  printf 'runtime.semantic-proxy.origin-relations=%s\n' \
    "$semantic_origin_relations"
  printf 'runtime.metadata-type-ledgers.exact-after-origin-validated-semantic-proxy-projection=142\n'
  printf 'candidate.classpath.actual-jvm-expanded=true\n'
  printf 'candidate.classpath.origin-and-hash-inventory.complete=true\n'
  printf 'original.peer.on.candidate.runtime.classpath=false\n'
  printf 'original.transactor.on.candidate.runtime.classpath=false\n'
  printf 'original.core2.on.candidate.runtime.classpath=false\n'
  printf 'original.nano.on.candidate.runtime.classpath=false\n'
  printf 'licensed.implementation.archive-payload.in.candidate-directory=false\n'
  printf 'candidate.directory.nested-archive=false\n'
  printf 'vendor.nano.keystore.on.candidate.runtime.classpath=false\n'
  printf 'sanitized.nano.derivative.on.candidate.runtime.classpath=1\n'
  printf 'licensed.datomic.aot.on.candidate.runtime.classpath=false\n'
  printf 'java.io.tmpdir.explicit-per-direct-and-nested-probe=true\n'
  printf 'java.io.tmpdir.outer-pass-records=9\n'
  printf 'java.io.tmpdir.nested-validator-attestations=%s\n' \
    "$inner_java_tmp_passes"
  printf 'runtime.validation.sealed-before-source-comparison=true\n'
  printf 'recovered.outputs.reverified.after-runtime=true\n'
  printf 'recovered.outputs.reverified.after-source-comparison=true\n'
  printf 'inputs.post-v3=PASS\n'
  printf 'inputs.post-v4=PASS\n'
  printf 'inputs.post-runtime-gate-pre-source=PASS\n'
  printf 'inputs.post-source-comparison=PASS\n'
  printf 'inputs.final=PASS\n'
} >"$evidence_abs/summary.properties"

printf 'PASS\tfinalization\t0\tnone\n' >>"$ledger"
current_phase=evidence-manifest
manifest_tmp="$evidence_abs/.manifest.sha256.tmp"
manifest_file="$evidence_abs/manifest.sha256"
manifest_verification="$evidence_abs/manifest-verification.log"
(cd "$evidence_abs" && \
  find . -type f ! -path './manifest.sha256' \
    ! -path './manifest-verification.log' \
    ! -path './.manifest.sha256.tmp' -print0 \
    | sort -z | xargs -0 sha256sum) >"$manifest_tmp"
mv "$manifest_tmp" "$manifest_file"
(cd "$evidence_abs" && sha256sum -c manifest.sha256) \
  >"$manifest_verification"
manifest_sha=$(sha256sum "$manifest_file" | awk '{print $1}')
finalized=1

echo "Peer full decompiler regression twins passed: $run_a_abs and $run_b_abs"
echo "142/142 exact runtime surfaces and source-body relations match the Peer oracle"
echo "Evidence manifest SHA-256: $manifest_sha"
echo "Evidence: $evidence_abs"
