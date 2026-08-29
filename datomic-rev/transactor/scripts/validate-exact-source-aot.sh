#!/usr/bin/bash -p
set -Eeuo pipefail

export PATH=/usr/bin:/bin
export LC_ALL=C
export TZ=UTC
umask 077

[[ $- == *p* ]] || {
  echo "invoke this validator executable directly so /usr/bin/bash -p owns startup" >&2
  exit 1
}

# Reject shell/native/path helpers before the first command lookup.  Privileged
# Bash also declines BASH_ENV/SHELLOPTS startup injection when this executable
# is invoked normally; the explicit checks keep an accidental `bash script`
# invocation fail-closed for the named channels that remain observable here.
for injected_name in BASH_ENV ENV CDPATH GLOBIGNORE LD_PRELOAD LD_LIBRARY_PATH \
  JAVA_TOOL_OPTIONS _JAVA_OPTIONS JDK_JAVA_OPTIONS JDK_JAVAC_OPTIONS CLASSPATH \
  UNZIP UNZIPOPT ZIPINFO ZIPINFOOPT; do
  if [[ -n "${!injected_name-}" ]]; then
    echo "refusing injected shell/native/Java environment: $injected_name" >&2
    exit 1
  fi
done
unset BASH_ENV ENV CDPATH GLOBIGNORE LD_PRELOAD LD_LIBRARY_PATH \
  JAVA_TOOL_OPTIONS _JAVA_OPTIONS JDK_JAVA_OPTIONS JDK_JAVAC_OPTIONS CLASSPATH \
  UNZIP UNZIPOPT ZIPINFO ZIPINFOOPT

# This is an acceptance driver, not a convenient development compiler.  Every
# executable or data input is pinned, every Java process runs in a clean and
# isolated environment, and a post-preflight failure still leaves sealed
# evidence explaining which gates ran.

script_dir=$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd)
transactor_dir=$(cd -- "$script_dir/.." && pwd)
project_dir=$(cd -- "$transactor_dir/.." && pwd)
datomic_home=${1:-"$project_dir/../../datomic/datomic-pro-1.0.7277"}
output_dir=${2:-/tmp/datomic-transactor-exact-source-aot-validation}
requested_java=${3:-${DATOMIC_EXACT_AOT_JAVA:-}}
requested_sanitized_nano=${4:-${DATOMIC_EXACT_AOT_SANITIZED_NANO:-}}
preflight_only=${DATOMIC_EXACT_AOT_PREFLIGHT_ONLY:-false}

source_dir="$transactor_dir/src-clj"
bundled_ownership="$transactor_dir/reports/stage-1-bundled-source-ownership.tsv"
datomic_ownership="$transactor_dir/reports/stage-1-datomic-dependency-source-ownership.tsv"
class_ownership="$transactor_dir/reports/stage-1-class-ownership.tsv"
distribution_jars="$transactor_dir/baseline/distribution-jars.tsv"
compile_runner="$script_dir/compile-exact-source-namespace.clj"
comparator_source="$transactor_dir/tools/CompareExactSourceAot.java"
scanner_source="$transactor_dir/tools/ScanExactAotBoundary.java"
verifier_source="$transactor_dir/tools/VerifyExactAotRuntime.java"
nano_summary="$transactor_dir/reports/stage-1-nano-sanitization-summary.tsv"
original_jar="$datomic_home/datomic-transactor-pro-1.0.7277.jar"
peer_jar="$datomic_home/peer-1.0.7277.jar"
core2_jar="$datomic_home/lib/core2-1.0.140.jar"
nano_original_jar="$datomic_home/lib/nano-impl-0.1.325.jar"
library_dir="$datomic_home/lib"

expected_original_sha=d90819b57e2138085ddc26c93314f953c2cd776d1d1aa8b43a6bec1e475d3692
expected_peer_sha=cb55c9d01e155f9965e3332cd8ef70be42cdefacb50c072076e9ecf22b9a23ba
expected_core2_sha=81fdf81586c7be1a4b61655b568348d12db7892cb4562d0db7529bbc7af8a94b
expected_nano_original_sha=fbde8184da356bd3a9995a9f05f01493efe4baa87c4d48377cda3e53331807cd
expected_namespace_count=87
expected_bundled_namespace_count=85
expected_datomic_namespace_count=2
expected_class_count=3431
expected_candidate_dependency_count=532
expected_oracle_dependency_count=533
expected_collision_count=33
expected_identical_collision_count=14
expected_different_collision_count=19
expected_jdk_file_count=465
expected_jdk_directory_count=90
expected_jdk_manifest_sha=2b2a172939d612fe77ca5ffeeb15102f5206dd159fd5809e1f35b8e5abdaf133
expected_nano_summary_sha=5fcb3027861a82f4e9c477511ade4422a8503c385df5edc5359a9184509a3354
expected_nano_derivative_sha=08f9699d6e35b9e052ec85ee15e0896b3a685c74e6e226d8cd89d9c61dbf8d5f
expected_nano_derivative_bytes=81218
expected_nano_manifest_sha=af6b5efdd9a3cdf16d1e1f960910bc1b72a686fecae26734bc4b65c6ba8408eb
expected_bundled_ownership_sha=01926ddaf34277f6bf69cce1284e6098b4adffc5ada940e02a721f0c89b3455f
expected_datomic_ownership_sha=04e98999d62b6b19b0a10ae7cce385bf45dc7f9fa0bdc18c3020ac46a00ab0fe
expected_class_ownership_sha=bb3be13708ba945dd2f8d2edce7adffb7d43c8a6fb265cdca0c3d2fa65122b87
expected_distribution_jars_sha=db25519069bfd327c161220264b89fb011f2d9a3e42e4857e5233d4b61811860
expected_compile_runner_sha=c10f41439273457d414b519961706463721ba8fb3f0f372fe3b79de0e3e17d98
# Replace this one literal only after the final three-relation, 3,431-class
# comparator closes.  Repinning a diagnostic intermediate is forbidden.
expected_comparator_source_sha=COMPARATOR_SHA256_PENDING_FINAL_RELATION
# A preflight-only run may bind the exact source currently under review so the
# wrapper machinery and all three tool self-tests can be exercised before the
# accepted production pin exists.  Production mode never honors this override.
if [[ "$preflight_only" == true && -n "${DATOMIC_EXACT_AOT_PREFLIGHT_COMPARATOR_SHA256:-}" ]]; then
  expected_comparator_source_sha=$DATOMIC_EXACT_AOT_PREFLIGHT_COMPARATOR_SHA256
fi
expected_scanner_source_sha=6e9e701e740ea0cf743dbd666bed979fd89e82dc36612f2c7b0faabc2c8b4d7c
expected_verifier_source_sha=812d88bb40bdd4d01e95dccd034ba40869add32e9768c8e40b340c01e382e60d
expected_asm_sha=b9d4fe4d71938df38839f0eca42aaaa64cf8b313d678da036f0cb3ca199b47f5
expected_java_bin_sha=3e98d0f812482808f701ffa0d4e94b8dd8e31a2c6a07fb52662ceef8e75d66f1
expected_javac_bin_sha=f315d031604835a3017268cca49631f4aa441d084d3cc29932d296ecd013106c

per_namespace_timeout_seconds=${DATOMIC_EXACT_AOT_TIMEOUT_SECONDS:-180}
tool_timeout_seconds=${DATOMIC_EXACT_AOT_TOOL_TIMEOUT_SECONDS:-600}
gate_timeout_seconds=${DATOMIC_EXACT_AOT_GATE_TIMEOUT_SECONDS:-3600}
kill_grace_seconds=${DATOMIC_EXACT_AOT_KILL_GRACE_SECONDS:-10}

case "$preflight_only" in true|false) ;; *) echo "DATOMIC_EXACT_AOT_PREFLIGHT_ONLY must be true or false" >&2; exit 1;; esac
for number in "$per_namespace_timeout_seconds" "$tool_timeout_seconds" \
  "$gate_timeout_seconds" "$kill_grace_seconds"; do
  [[ "$number" =~ ^[1-9][0-9]*$ ]] || {
    echo "timeouts must be positive integers" >&2
    exit 1
  }
done

for command_name in awk cmp cp cut diff dirname find grep head mkdir mktemp \
  readlink realpath sed setsid sha256sum sort stat timeout tr unzip uniq wc xargs; do
  command -v "$command_name" >/dev/null || {
    echo "missing required command: $command_name" >&2
    exit 1
  }
done
[[ -x /usr/bin/time ]] || { echo "missing required command: /usr/bin/time" >&2; exit 1; }

die() {
  failure_reason=$*
  echo "$*" >&2
  exit 1
}

sha_of() { sha256sum "$1" | awk '{print $1}'; }

verify_pinned_file() {
  local path=$1 expected=$2 label=$3 actual
  [[ "$expected" =~ ^[0-9a-f]{64}$ ]] ||
    die "$label has no accepted SHA-256 pin: $expected"
  [[ ! -L "$path" && -f "$path" ]] ||
    die "$label must be a regular non-symlink file: $path"
  actual=$(sha_of "$path")
  [[ "$actual" == "$expected" ]] ||
    die "$label SHA-256 mismatch: expected $expected, found $actual"
}

path_overlaps() {
  local left=$1 right=$2
  [[ "$left" == "$right" || "$left" == "$right/"* || "$right" == "$left/"* ]]
}

require_disjoint() {
  local left=$1 right=$2 label=$3
  if path_overlaps "$left" "$right"; then
    die "$label roots overlap: $left and $right"
  fi
  return 0
}

require_empty_or_absent() {
  local path=$1 label=$2
  if [[ -e "$path" || -L "$path" ]]; then
    [[ ! -L "$path" && -d "$path" ]] || die "$label is not a regular directory: $path"
    [[ -z "$(find "$path" -mindepth 1 -print -quit 2>/dev/null)" ]] ||
      die "refusing to overwrite non-empty $label: $path"
  fi
}

safe_relative() {
  local value=$1
  [[ -n "$value" && "$value" != /* && "$value" != *$'\t'* && \
     "$value" != *$'\n'* && "$value" != *'\'* && ":$value:" != *':../'* && \
     "$value" != '../'* && "$value" != *'/../'* && "$value" != *'/..' && \
     "$value" != '.' && "$value" != '..' && "$value" != *'//'* ]]
}

safe_absolute() {
  local value=$1
  [[ "$value" == /* && "$value" != *$'\t'* && "$value" != *$'\n'* && \
     "$value" != *:* ]]
}

# The requested output and every mutable execution root are siblings.  This
# lets the wrapper prove disjointness before creating any of them; evidence
# seals refer to the retained sibling roots by canonical absolute path.
project_abs=$(realpath -m "$project_dir")
datomic_abs=$(realpath "$datomic_home")
datomic_home=$datomic_abs
original_jar="$datomic_home/datomic-transactor-pro-1.0.7277.jar"
peer_jar="$datomic_home/peer-1.0.7277.jar"
core2_jar="$datomic_home/lib/core2-1.0.140.jar"
nano_original_jar="$datomic_home/lib/nano-impl-0.1.325.jar"
library_dir="$datomic_home/lib"
output_abs=$(realpath -m "$output_dir")
output_lexical=$(realpath -ms "$output_dir")
[[ "$output_lexical" == "$output_abs" ]] ||
  die "output path contains a symlinked canonical component: $output_dir"
work_root=$(realpath -m "$output_abs.work")
staged_source=$(realpath -m "$output_abs.source")
candidate_a=$(realpath -m "$output_abs.candidate-a")
candidate_b=$(realpath -m "$output_abs.candidate-b")
isolated_home=$(realpath -m "$output_abs.home")
isolated_tmp=$(realpath -m "$output_abs.tmp")

for canonical_path in "$project_abs" "$datomic_abs" "$output_abs" \
  "$work_root" "$staged_source" "$candidate_a" "$candidate_b" \
  "$isolated_home" "$isolated_tmp"; do
  safe_absolute "$canonical_path" ||
    die "unsafe canonical path for TSV/classpath boundary: $canonical_path"
done
require_disjoint "$project_abs" "$datomic_abs" \
  "repository/licensed-distribution"

mutable_roots=("$output_abs" "$work_root" "$staged_source" "$candidate_a" \
  "$candidate_b" "$isolated_home" "$isolated_tmp")
mutable_labels=(output work source candidate-a candidate-b home tmp)
mutable_parent=$(dirname "$output_abs")
[[ ! -L "$mutable_parent" && -d "$mutable_parent" && \
   $(realpath "$mutable_parent") == "$mutable_parent" ]] ||
  die "mutable-root parent must be an existing canonical non-symlink directory: $mutable_parent"
mutable_parent_identity=$(stat -Lc '%d:%i' "$mutable_parent")
for index in "${!mutable_roots[@]}"; do
  [[ $(dirname "${mutable_roots[$index]}") == "$mutable_parent" ]] ||
    die "mutable roots are not siblings under the sealed parent"
  require_empty_or_absent "${mutable_roots[$index]}" "${mutable_labels[$index]} root"
  require_disjoint "${mutable_roots[$index]}" "$project_abs" \
    "${mutable_labels[$index]}/repository"
  require_disjoint "${mutable_roots[$index]}" "$datomic_abs" \
    "${mutable_labels[$index]}/licensed-distribution"
done
for ((left=0; left<${#mutable_roots[@]}; left++)); do
  for ((right=left+1; right<${#mutable_roots[@]}; right++)); do
    require_disjoint "${mutable_roots[$left]}" "${mutable_roots[$right]}" \
      "${mutable_labels[$left]}/${mutable_labels[$right]}"
  done
done

verify_pinned_file "$bundled_ownership" "$expected_bundled_ownership_sha" bundled-source-ownership
verify_pinned_file "$datomic_ownership" "$expected_datomic_ownership_sha" datomic-source-ownership
verify_pinned_file "$class_ownership" "$expected_class_ownership_sha" class-ownership
verify_pinned_file "$distribution_jars" "$expected_distribution_jars_sha" distribution-jars
verify_pinned_file "$compile_runner" "$expected_compile_runner_sha" compile-runner
verify_pinned_file "$comparator_source" "$expected_comparator_source_sha" comparator-source
verify_pinned_file "$scanner_source" "$expected_scanner_source_sha" scanner-source
verify_pinned_file "$verifier_source" "$expected_verifier_source_sha" verifier-source
verify_pinned_file "$original_jar" "$expected_original_sha" original-transactor
verify_pinned_file "$peer_jar" "$expected_peer_sha" original-peer
verify_pinned_file "$core2_jar" "$expected_core2_sha" original-core2
verify_pinned_file "$nano_original_jar" "$expected_nano_original_sha" original-nano
verify_pinned_file "$nano_summary" "$expected_nano_summary_sha" nano-summary

summary_nano_output=$(awk -F '\t' '$1 == "canonical.output" {print $2}' "$nano_summary")
summary_nano_sha=$(awk -F '\t' '$1 == "derivative.jar.sha256" {print $2}' "$nano_summary")
summary_nano_bytes=$(awk -F '\t' '$1 == "derivative.jar.bytes" {print $2}' "$nano_summary")
summary_manifest_sha=$(awk -F '\t' '$1 == "canonical.output.manifest.sha256" {print $2}' "$nano_summary")
[[ "$summary_nano_sha" == "$expected_nano_derivative_sha" && \
   "$summary_nano_bytes" == "$expected_nano_derivative_bytes" && \
   "$summary_manifest_sha" == "$expected_nano_manifest_sha" ]] ||
  die "Nano sanitization summary does not bind the expected derivative"
sanitized_nano=${requested_sanitized_nano:-"$summary_nano_output/nano-impl-0.1.325-sanitized.jar"}
[[ ! -L "$sanitized_nano" && -f "$sanitized_nano" ]] ||
  die "sanitized Nano derivative must be a regular non-symlink file: $sanitized_nano"
sanitized_nano=$(realpath "$sanitized_nano")
safe_absolute "$sanitized_nano" || die "unsafe sanitized Nano path: $sanitized_nano"
require_disjoint "$project_abs" "$sanitized_nano" "repository/sanitized-nano"
require_disjoint "$datomic_abs" "$sanitized_nano" \
  "licensed-distribution/sanitized-nano"
sanitized_manifest="$(dirname "$sanitized_nano")/manifest.sha256"
verify_pinned_file "$sanitized_nano" "$expected_nano_derivative_sha" sanitized-nano
verify_pinned_file "$sanitized_manifest" "$expected_nano_manifest_sha" sanitized-nano-manifest
[[ $(wc -c < "$sanitized_nano") == "$expected_nano_derivative_bytes" ]] ||
  die "sanitized Nano derivative size differs"
(cd "$(dirname "$sanitized_nano")" && sha256sum -c manifest.sha256 >/dev/null)
for mutable in "${mutable_roots[@]}"; do
  require_disjoint "$mutable" "$sanitized_nano" "mutable/sanitized-nano"
done

if [[ -n "$requested_java" ]]; then
  java_bin=$requested_java
elif [[ -x /tmp/amazon-corretto-11.0.22.7.1/bin/java ]]; then
  java_bin=/tmp/amazon-corretto-11.0.22.7.1/bin/java
else
  java_bin=$(command -v java || true)
fi
[[ -n "$java_bin" && -x "$java_bin" ]] || die "Amazon Corretto 11.0.22.7.1 java is required"
java_bin=$(realpath "$java_bin")
javac_bin=$(realpath "$(dirname "$java_bin")/javac")
jdk_root=$(realpath "$(dirname "$java_bin")/..")
asm_jar="$library_dir/asm-9.2.jar"
for canonical_path in "$java_bin" "$javac_bin" "$jdk_root" "$asm_jar"; do
  safe_absolute "$canonical_path" || die "unsafe toolchain path: $canonical_path"
done
verify_pinned_file "$java_bin" "$expected_java_bin_sha" java-runtime
verify_pinned_file "$javac_bin" "$expected_javac_bin_sha" java-compiler
verify_pinned_file "$asm_jar" "$expected_asm_sha" asm-library
jdk_release="$jdk_root/release"
[[ ! -L "$jdk_release" && -f "$jdk_release" ]] ||
  die "JDK release descriptor is missing: $jdk_release"
grep -Fx 'JAVA_VERSION="11.0.22"' "$jdk_release" >/dev/null ||
  die "JDK release descriptor does not declare Java 11.0.22"
grep -Fx 'IMPLEMENTOR="Amazon.com Inc."' "$jdk_release" >/dev/null ||
  die "JDK release descriptor does not declare Amazon Corretto"
jdk_files=$(find "$jdk_root" -type f | wc -l)
jdk_directories=$(find "$jdk_root" -type d | wc -l)
jdk_links=$(find "$jdk_root" -type l | wc -l)
[[ "$jdk_files" == "$expected_jdk_file_count" && \
   "$jdk_directories" == "$expected_jdk_directory_count" && "$jdk_links" == 0 ]] ||
  die "JDK tree shape differs: files=$jdk_files directories=$jdk_directories links=$jdk_links"
for mutable in "${mutable_roots[@]}"; do
  require_disjoint "$mutable" "$jdk_root" "mutable/JDK"
done

evidence_started=false
run_complete=false
failure_reason=""
active_pid=""
declare -A gate_status
gate_names=(preflight source-stage runtime-policy java-runtime-probe tool-compile comparator-self-test \
  scanner-self-test verifier-self-test candidate-compile comparator scanner verifier \
  final-seal)
for gate in "${gate_names[@]}"; do gate_status[$gate]=NOT_RUN; done

kill_active() {
  [[ -n "$active_pid" ]] || return 0
  kill -TERM -- "-$active_pid" 2>/dev/null || kill -TERM "$active_pid" 2>/dev/null || true
  local ticks=$((kill_grace_seconds * 10))
  while ((ticks > 0)) && kill -0 "$active_pid" 2>/dev/null; do
    sleep 0.1
    ticks=$((ticks - 1))
  done
  if kill -0 "$active_pid" 2>/dev/null; then
    kill -KILL -- "-$active_pid" 2>/dev/null || kill -KILL "$active_pid" 2>/dev/null || true
  fi
  wait "$active_pid" 2>/dev/null || true
  active_pid=""
}

on_signal() {
  failure_reason="received signal $1"
  kill_active
  exit "$2"
}

write_tree_manifest() {
  local root=$1 destination=$2
  [[ -d "$root" && ! -L "$root" ]] || return 1
  (
    cd "$root"
    printf 'type\tpath\tsha256\n'
    printf 'directory\t.\t-\n'
    while IFS= read -r -d '' path; do
      printf 'directory\t%s\t-\n' "${path#./}"
    done < <(find . -mindepth 1 -type d -print0 | sort -z)
    while IFS= read -r -d '' path; do
      printf 'file\t%s\t%s\n' "${path#./}" "$(sha_of "$path")"
    done < <(find . -type f -print0 | sort -z)
    if [[ -n "$(find . \( -type l -o \( ! -type d ! -type f \) \) -print -quit)" ]]; then
      exit 1
    fi
  ) > "$destination"
}

# Tool self-tests deliberately create hostile filesystem fixtures (including
# symlinks and FIFOs) below their isolated HOME/TMP roots.  Exit evidence must
# describe those nodes without following them; it must not confuse a proved
# negative fixture with contamination of the strict candidate/input trees.
write_mutable_tree_manifest() {
  local root=$1 destination=$2 path relative escaped target_sha kind metadata
  [[ -d "$root" && ! -L "$root" ]] || return 1
  (
    cd "$root"
    printf 'type\tpath\tcontent_sha256\tmetadata\n'
    printf 'directory\t.\t-\tmode=%s\n' "$(stat -c '%a' .)"
    while IFS= read -r -d '' path; do
      relative=${path#./}; printf -v escaped '%q' "$relative"
      printf 'directory\t%s\t-\tmode=%s\n' "$escaped" "$(stat -c '%a' "$path")"
    done < <(find . -mindepth 1 -type d -print0 | sort -z)
    while IFS= read -r -d '' path; do
      relative=${path#./}; printf -v escaped '%q' "$relative"
      printf 'file\t%s\t%s\tbytes=%s,mode=%s\n' "$escaped" \
        "$(sha_of "$path")" "$(stat -c '%s' "$path")" "$(stat -c '%a' "$path")"
    done < <(find . -type f -print0 | sort -z)
    while IFS= read -r -d '' path; do
      relative=${path#./}; printf -v escaped '%q' "$relative"
      target_sha=$(readlink -z "$path" | sha256sum | awk '{print $1}') || exit 1
      printf 'symlink\t%s\t%s\tbytes=%s,mode=%s\n' "$escaped" "$target_sha" \
        "$(stat -c '%s' "$path")" "$(stat -c '%a' "$path")"
    done < <(find . -type l -print0 | sort -z)
    while IFS= read -r -d '' path; do
      relative=${path#./}; printf -v escaped '%q' "$relative"
      kind=$(stat -c '%F' "$path"); kind=${kind// /_}
      metadata="mode=$(stat -c '%a' "$path"),rdev=$(stat -c '%r' "$path")"
      printf '%s\t%s\t-\t%s\n' "$kind" "$escaped" "$metadata"
    done < <(find . ! -type d ! -type f ! -type l -print0 | sort -z)
  ) > "$destination"
}

tree_digest() {
  local root=$1
  (
    cd "$root"
    printf 'directory\t.\t-\n'
    while IFS= read -r -d '' path; do printf 'directory\t%s\t-\n' "${path#./}"; done \
      < <(find . -mindepth 1 -type d -print0 | sort -z)
    while IFS= read -r -d '' path; do printf 'file\t%s\t%s\n' "${path#./}" "$(sha_of "$path")"; done \
      < <(find . -type f -print0 | sort -z)
    [[ -z "$(find . \( -type l -o \( ! -type d ! -type f \) \) -print -quit)" ]]
  ) | sha256sum | awk '{print $1}'
}

write_output_manifest() {
  local manifest="$output_abs/evidence/output-manifest.sha256"
  [[ -d "$output_abs" ]] || return 1
  find "$output_abs" \( -type l -o \( ! -type d ! -type f \) \) -print -quit | \
    awk 'NF {bad=1} END {exit bad ? 0 : 1}' && return 1
  (
    cd "$output_abs"
    find . -type f ! -path './evidence/output-manifest.sha256' -print0 | \
      sort -z | xargs -0 sha256sum
  ) > "$manifest"
  (cd "$output_abs" && sha256sum -c evidence/output-manifest.sha256 >/dev/null)
}

finalize() {
  local original_status=$?
  trap - EXIT
  set +e
  kill_active
  if [[ "$evidence_started" == true && -d "$output_abs/evidence" ]]; then
    exit_seal_failed=false
    seal_exit_tree() {
      local root=$1 name=$2
      if [[ ! -d "$root" || -L "$root" ]] || ! write_tree_manifest "$root" \
          "$output_abs/evidence/$name"; then
        exit_seal_failed=true
        failure_reason="${failure_reason:+$failure_reason; }failed exit tree seal: $root"
      fi
    }
    seal_mutable_exit_tree() {
      local root=$1 name=$2
      if [[ ! -d "$root" || -L "$root" ]] || \
          ! write_mutable_tree_manifest "$root" "$output_abs/evidence/$name"; then
        exit_seal_failed=true
        failure_reason="${failure_reason:+$failure_reason; }failed mutable exit tree seal: $root"
      fi
    }
    # These exit seals are attempted on both success and failure.  A failed
    # Java gate therefore cannot suppress the final state of its mutable and
    # immutable execution roots.
    seal_exit_tree "$staged_source" staged-source-exit.tsv
    seal_exit_tree "$candidate_a" candidate-a-exit.tsv
    seal_exit_tree "$candidate_b" candidate-b-exit.tsv
    seal_mutable_exit_tree "$work_root" work-root-exit.tsv
    seal_mutable_exit_tree "$isolated_home" home-root-exit.tsv
    seal_mutable_exit_tree "$isolated_tmp" tmp-root-exit.tsv
    seal_exit_tree "$jdk_root" jdk-tree-exit.tsv
    if [[ -f "$output_abs/evidence/jdk-tree-exit.tsv" ]] && \
       [[ $(sha_of "$output_abs/evidence/jdk-tree-exit.tsv") != \
          "$expected_jdk_manifest_sha" ]]; then
      exit_seal_failed=true
      failure_reason="${failure_reason:+$failure_reason; }pinned JDK exit manifest differs"
    fi
    if [[ -f "${runtime_input_paths-}" ]]; then
      runtime_exit_status=0
      : > "$output_abs/evidence/runtime-inputs-exit.sha256"
      while IFS= read -r input; do
        sha256sum "$input" >> "$output_abs/evidence/runtime-inputs-exit.sha256" ||
          runtime_exit_status=1
      done < "$runtime_input_paths"
      if [[ "$runtime_exit_status" != 0 ]]; then
        exit_seal_failed=true
        failure_reason="${failure_reason:+$failure_reason; }failed static runtime-input exit seal"
      fi
    else
      exit_seal_failed=true
      failure_reason="${failure_reason:+$failure_reason; }runtime input ledger absent at exit"
    fi
    if [[ "$exit_seal_failed" == true ]]; then
      [[ "$original_status" != 0 ]] || original_status=1
      run_complete=false
    fi
    if [[ "$run_complete" == true && "$original_status" == 0 ]]; then
      result=PASS
    elif [[ "$preflight_only" == true && "$original_status" == 0 ]]; then
      result=PREFLIGHT_PASS
    else
      result=FAIL
    fi
    write_status_ledgers() {
      clean_failure_reason=${failure_reason//$'\t'/ }
      clean_failure_reason=${clean_failure_reason//$'\n'/ }
      {
        printf 'gate\tstatus\n'
        for gate in "${gate_names[@]}"; do
          printf '%s\t%s\n' "$gate" "${gate_status[$gate]}"
        done
      } > "$output_abs/evidence/gate-status.tsv"
      {
        printf 'metric\tvalue\n'
        printf 'status\t%s\n' "$result"
        printf 'exit.status\t%s\n' "$original_status"
        printf 'failure.reason\t%s\n' "$clean_failure_reason"
        printf 'production.requested\t%s\n' "$([[ "$preflight_only" == true ]] && printf false || printf true)"
        printf 'stage.1.pass.claimed\t%s\n' "$([[ "$result" == PASS ]] && printf true || printf false)"
        printf 'process.environment\tenv-i fixed allowlist; no inherited variables\n'
        printf 'output.root\t%s\n' "$output_abs"
        printf 'work.root\t%s\n' "$work_root"
        printf 'source.root\t%s\n' "$staged_source"
        printf 'candidate.a.root\t%s\n' "$candidate_a"
        printf 'candidate.b.root\t%s\n' "$candidate_b"
        printf 'home.root\t%s\n' "$isolated_home"
        printf 'tmp.root\t%s\n' "$isolated_tmp"
        printf 'cleanup.policy\tTERM-%ss-then-KILL-process-group\n' "$kill_grace_seconds"
      } > "$output_abs/evidence/result.tsv"
    }
    write_status_ledgers
    write_output_manifest
    manifest_status=$?
    if [[ "$manifest_status" != 0 ]]; then
      echo "could not seal final evidence" >&2
      original_status=1
      result=FAIL
      gate_status[final-seal]=FAIL
      failure_reason="${failure_reason:+$failure_reason; }initial output evidence seal failed"
      write_status_ledgers
      write_output_manifest || true
    else
      gate_status[final-seal]=PASS
      # Refresh the two small ledgers and seal once more with final-seal=PASS.
      write_status_ledgers
      if ! write_output_manifest; then
        original_status=1
        result=FAIL
        gate_status[final-seal]=FAIL
        failure_reason="${failure_reason:+$failure_reason; }final output evidence reseal failed"
        write_status_ledgers
        write_output_manifest || true
      fi
    fi
  fi
  exit "$original_status"
}

trap 'on_signal INT 130' INT
trap 'on_signal TERM 143' TERM
trap 'status=$?; [[ -n "$failure_reason" ]] || failure_reason="command failed at line $LINENO (exit $status): $BASH_COMMAND"' ERR
trap finalize EXIT

# No writes occur above this line.  All canonical origin and nonempty-output
# checks have passed.
mkdir -p "$output_abs/evidence" "$output_abs/logs-a" "$output_abs/logs-b" \
  "$output_abs/timing-a" "$output_abs/timing-b" "$work_root/tool-classes" \
  "$work_root/cwd" "$work_root/collision" "$staged_source" "$candidate_a" \
  "$candidate_b" "$isolated_home/tools" "$isolated_home/production" \
  "$isolated_tmp/tools" "$isolated_tmp/production"
[[ $(stat -Lc '%d:%i' "$mutable_parent") == "$mutable_parent_identity" ]] ||
  die "mutable-root parent identity changed during creation"
for index in "${!mutable_roots[@]}"; do
  [[ ! -L "${mutable_roots[$index]}" && \
     $(realpath "${mutable_roots[$index]}") == "${mutable_roots[$index]}" ]] ||
    die "mutable root redirected during creation: ${mutable_roots[$index]}"
done
evidence_started=true
gate_status[preflight]=PASS
export HOME="$isolated_home/tools"
export TMPDIR="$isolated_tmp/tools"
export JAVA_HOME="$jdk_root"
unset JAVA_TOOL_OPTIONS _JAVA_OPTIONS JDK_JAVA_OPTIONS JDK_JAVAC_OPTIONS CLASSPATH

cohort_base="$output_abs/evidence/cohort-base.tsv"
cohort="$output_abs/evidence/cohort.tsv"
{
  printf 'namespace\tscope\tsource_entry\tsource_sha256\towner_jar\towner_jar_sha256\n'
  awk -F '\t' 'NR > 1 {print $1 "\tbundled\t" $8 "\t" $9 "\t" $6 "\t" $7}' "$bundled_ownership"
  awk -F '\t' 'NR > 1 && $4 == "single-exact-path" {print $1 "\tdatomic\t" $8 "\t" $9 "\t" $6 "\t" $7}' "$datomic_ownership"
} | { IFS= read -r header; printf '%s\n' "$header"; sort; } > "$cohort_base"
awk -F '\t' '
  FNR == NR {if (FNR > 1) expected[$5]++; next}
  FNR == 1 {print $0 "\texpected_classes"; next}
  !($1 in expected) {print "no original class ownership for " $1 > "/dev/stderr"; exit 1}
  {print $0 "\t" expected[$1]}
' "$class_ownership" "$cohort_base" > "$cohort"
namespace_count=$(awk 'NR > 1 {n++} END {print n+0}' "$cohort")
bundled_count=$(awk -F '\t' 'NR > 1 && $2 == "bundled" {n++} END {print n+0}' "$cohort")
datomic_count=$(awk -F '\t' 'NR > 1 && $2 == "datomic" {n++} END {print n+0}' "$cohort")
class_count=$(awk -F '\t' 'NR > 1 {n += $7} END {print n+0}' "$cohort")
[[ "$namespace_count" == "$expected_namespace_count" && \
   "$bundled_count" == "$expected_bundled_namespace_count" && \
   "$datomic_count" == "$expected_datomic_namespace_count" && \
   "$class_count" == "$expected_class_count" ]] ||
  die "cohort shape differs: namespaces=$namespace_count bundled=$bundled_count datomic=$datomic_count classes=$class_count"
[[ $(awk -F '\t' 'NR>1 && $2=="datomic" {print $1 "\t" $3}' "$cohort") == \
   $'datomic.query.support\tdatomic/query/support.clj\ndatomic.specs\tdatomic/specs.clj' ]] ||
  die "exact Datomic dependency-source identities differ"
[[ $(awk -F '\t' 'NR>1 && $3=="clojure/tools/cli.cljc" {n++} END{print n+0}' \
    "$cohort") == 1 ]] || die "exact cohort must contain clojure/tools/cli.cljc once"

runtime_input_paths="$output_abs/evidence/runtime-input-paths.txt"
printf '%s\n' "$original_jar" "$peer_jar" "$core2_jar" "$nano_original_jar" \
  "$bundled_ownership" "$datomic_ownership" "$class_ownership" \
  "$distribution_jars" "$compile_runner" "$comparator_source" \
  "$scanner_source" "$verifier_source" "$nano_summary" "$sanitized_nano" \
  "$sanitized_manifest" "$java_bin" "$javac_bin" "$jdk_release" "$asm_jar" \
  "$(realpath "$0")" \
  > "$runtime_input_paths"

while IFS=$'\t' read -r namespace_name scope source_entry source_sha owner_jar owner_sha expected_classes; do
  [[ "$namespace_name" == namespace ]] && continue
  [[ "$namespace_name" =~ ^[A-Za-z0-9_.-]+$ ]] ||
    die "unsafe exact namespace identifier: $namespace_name"
  safe_relative "$source_entry" || die "unsafe exact source entry: $source_entry"
  source_file="$source_dir/$source_entry"
  verify_pinned_file "$source_file" "$source_sha" "exact-source:$namespace_name"
  [[ "$owner_jar" != *,* && "$owner_sha" != *,* ]] || die "non-singular source owner: $namespace_name"
  safe_relative "$owner_jar" || die "unsafe exact source owner: $owner_jar"
  owner_path="$datomic_home/$owner_jar"
  verify_pinned_file "$owner_path" "$owner_sha" "source-owner:$namespace_name"
  mkdir -p "$staged_source/$(dirname "$source_entry")"
  cp -- "$source_file" "$staged_source/$source_entry"
  printf '%s\n%s\n' "$source_file" "$owner_path" >> "$runtime_input_paths"
done < "$cohort"
staged_files=$(find "$staged_source" -type f | wc -l)
[[ "$staged_files" == "$expected_namespace_count" ]] ||
  die "staged source root contains $staged_files files, expected $expected_namespace_count"
write_tree_manifest "$staged_source" "$output_abs/evidence/staged-source.tsv"
gate_status[source-stage]=PASS

candidate_runtime="$output_abs/evidence/candidate-runtime.tsv"
oracle_runtime="$output_abs/evidence/oracle-runtime.tsv"
classpath_manifest="$output_abs/evidence/compile-classpath.tsv"
runtime_archive_counts="$work_root/runtime-archive-counts.tsv"
printf 'position\trole\tkind\tpath\texpected_sha256\n' > "$candidate_runtime"
printf 'position\trole\tkind\tpath\texpected_sha256\n' > "$oracle_runtime"
printf 'distribution_path\tcompile_path\tsha256\tsource\n' > "$classpath_manifest"
printf 'position\tpath\texpected_entries\n' > "$runtime_archive_counts"
compile_classpath="$staged_source"
candidate_position=0
oracle_position=0
while IFS=$'\t' read -r jar distribution_path expected_sha bytes entries rest; do
  [[ "$jar" == jar ]] && continue
  [[ "$distribution_path" == lib/* ]] || continue
  safe_relative "$distribution_path" || die "unsafe distribution path: $distribution_path"
  [[ "$jar" == "${distribution_path##*/}" ]] || die "distribution basename mismatch: $distribution_path"
  dependency_path="$datomic_home/$distribution_path"
  verify_pinned_file "$dependency_path" "$expected_sha" "distribution:$distribution_path"
  oracle_position=$((oracle_position + 1))
  oracle_role=oracle-fallback
  [[ "$jar" == core2-1.0.140.jar ]] && oracle_role=core2-original
  [[ "$jar" == nano-impl-0.1.325.jar ]] && oracle_role=nano-original
  printf '%s\t%s\tjar\t%s\t%s\n' "$oracle_position" "$oracle_role" \
    "$dependency_path" "$expected_sha" >> "$oracle_runtime"
  printf '%s\n' "$dependency_path" >> "$runtime_input_paths"
  [[ "$jar" == core2-1.0.140.jar ]] && continue
  candidate_position=$((candidate_position + 1))
  compile_path=$dependency_path
  compile_sha=$expected_sha
  compile_source=licensed-distribution
  candidate_role=retained-distribution-dependency
  candidate_entries=$entries
  if [[ "$jar" == nano-impl-0.1.325.jar ]]; then
    compile_path=$sanitized_nano
    compile_sha=$expected_nano_derivative_sha
    compile_source=deterministic-sanitized-derivative
    candidate_role=nano-sanitized
    candidate_entries=$(unzip -Z1 "$sanitized_nano" | wc -l)
  fi
  printf '%s\t%s\tjar\t%s\t%s\n' "$candidate_position" "$candidate_role" \
    "$compile_path" "$compile_sha" >> "$candidate_runtime"
  printf '%s\t%s\t%s\t%s\n' "$distribution_path" "$compile_path" \
    "$compile_sha" "$compile_source" >> "$classpath_manifest"
  printf '%s\t%s\t%s\n' "$candidate_position" "$compile_path" "$candidate_entries" \
    >> "$runtime_archive_counts"
  compile_classpath="$compile_classpath:$compile_path"
  printf '%s\n' "$compile_path" >> "$runtime_input_paths"
done < "$distribution_jars"
[[ "$candidate_position" == "$expected_candidate_dependency_count" && \
   "$oracle_position" == "$expected_oracle_dependency_count" ]] ||
  die "runtime ledger shape differs: candidate=$candidate_position oracle=$oracle_position"

candidate_runtime_sha=$(sha_of "$candidate_runtime")
oracle_runtime_sha=$(sha_of "$oracle_runtime")
runtime_policy="$output_abs/evidence/runtime-policy.tsv"
policy_suffix=$'\t'"$expected_candidate_dependency_count"$'\t'"$expected_oracle_dependency_count"$'\t'"$candidate_runtime_sha"$'\t'"$oracle_runtime_sha"
{
  printf 'rule_id\tsha256\tcandidate_disposition\tcandidate_role\toracle_disposition\toracle_role\tcandidate_elements\toracle_elements\tcandidate_ledger_sha256\toracle_ledger_sha256\n'
  printf 'transactor-original\t%s\tFORBID\t\tOWNED\t%s\n' "$expected_original_sha" "$policy_suffix"
  printf 'peer-original\t%s\tFORBID\t\tFORBID\t%s\n' "$expected_peer_sha" "$policy_suffix"
  printf 'core2-original\t%s\tFORBID\t\tREQUIRE\tcore2-original%s\n' "$expected_core2_sha" "$policy_suffix"
  printf 'nano-original\t%s\tFORBID\t\tREQUIRE\tnano-original%s\n' "$expected_nano_original_sha" "$policy_suffix"
  printf 'nano-sanitized\t%s\tREQUIRE\tnano-sanitized\tFORBID\t%s\n' "$expected_nano_derivative_sha" "$policy_suffix"
} > "$runtime_policy"

allowed_resources="$output_abs/evidence/allowed-resources.tsv"
printf 'candidate\tpath\towner\tsha256\n' > "$allowed_resources"
forbidden_payloads="$output_abs/evidence/forbidden-payloads.tsv"
nano_key_sha=$(unzip -p "$nano_original_jar" nano_impl/transactor-key.jks | sha256sum | awk '{print $1}')
nano_trust_sha=$(unzip -p "$nano_original_jar" nano_impl/transactor-trust.jks | sha256sum | awk '{print $1}')
{
  printf 'rule_id\tsha256\treason\n'
  printf 'original-transactor-content\t%s\tlicensed Transactor payload\n' "$expected_original_sha"
  printf 'original-peer-content\t%s\tlicensed Peer payload\n' "$expected_peer_sha"
  printf 'original-core2-content\t%s\tlicensed core2 payload\n' "$expected_core2_sha"
  printf 'original-nano-content\t%s\tlicensed original Nano payload\n' "$expected_nano_original_sha"
  printf 'original-nano-key\t%s\tlicensed Nano private keystore payload\n' "$nano_key_sha"
  printf 'original-nano-trust\t%s\tlicensed Nano trust keystore payload\n' "$nano_trust_sha"
} > "$forbidden_payloads"

# Build the exact ordered collision policy from the sealed candidate ledger.
# Java 11 multi-release selection is applied independently inside every JAR;
# versioned classes are active only for a manifest with Multi-Release: true,
# and the highest release <= 11 wins within that runtime element.
effective_inventory="$work_root/collision/effective-classes.tsv"
printf 'internal_name\tposition\trole\tjar\tentry\trelease\n' > "$effective_inventory"
while IFS=$'\t' read -r position role kind jar_path expected_sha; do
  [[ "$position" == position ]] && continue
  names="$work_root/collision/$position.names"
  manifest="$work_root/collision/$position.manifest"
  effective="$work_root/collision/$position.effective"
  unzip -tqq "$jar_path" >/dev/null
  unzip -Z1 "$jar_path" > "$names"
  expected_entries=$(awk -F '\t' -v p="$position" 'NR>1 && $1==p {print $3}' "$runtime_archive_counts")
  [[ -n "$expected_entries" && $(wc -l < "$names") == "$expected_entries" ]] ||
    die "ZIP entry count differs at candidate runtime position $position"
  duplicate_entry=$(sort "$names" | uniq -d | head -1)
  [[ -z "$duplicate_entry" ]] || die "duplicate ZIP entry at position $position: $duplicate_entry"
  if unzip -p "$jar_path" META-INF/MANIFEST.MF > "$manifest" 2>/dev/null; then :; else : > "$manifest"; fi
  multi_release=$(awk '
    function flush() {
      if (tolower(key)=="multi-release" && tolower(value)=="true") found=1
    }
    {sub(/\r$/, "")}
    finished {next}
    $0=="" {flush(); finished=1; next}
    /^[ ]/ {value = value substr($0,2); next}
    {flush(); at=index($0, ":"); if(at){key=substr($0,1,at-1); value=substr($0,at+1); sub(/^ /,"",value)} else {key=""; value=""}}
    END {if (!finished) flush(); print found ? "true" : "false"}
  ' "$manifest")
  awk -v mr="$multi_release" '
    function unsafe(v) {return v=="" || index(v,"\t") || index(v,"\r") || v ~ /^\// || v ~ /\\/ || v ~ /\/\.\.\// || v ~ /^\.\.\// || v ~ /\/\.\.$/ || v ~ /\/\//}
    {if (unsafe($0)) {print "unsafe ZIP entry: " $0 > "/dev/stderr"; exit 2}}
    /\/$/ {next}
    /^META-INF\/versions\/[0-9]+\/.*\.class$/ {
      split($0,p,"/"); release=p[3]+0
      if (p[3] !~ /^[0-9]+$/ || release < 9) {print "invalid MR class release: " $0 > "/dev/stderr"; exit 2}
      if (mr != "true" || release > 11) next
      prefix="META-INF/versions/" p[3] "/"; internal=substr($0,length(prefix)+1); sub(/\.class$/, "", internal)
      if ((internal in best) && release == best[internal] && entry[internal] != $0) {
        print "duplicate effective MR release for " internal > "/dev/stderr"; exit 2
      }
      if (!(internal in best) || release > best[internal]) {best[internal]=release; entry[internal]=$0}
      next
    }
    /^META-INF\/versions\// {next}
    /\.class$/ {internal=$0; sub(/\.class$/, "",internal); if (!(internal in best)) {best[internal]=0; entry[internal]=$0}}
    END {for (internal in best) print internal "\t" entry[internal] "\t" best[internal]}
  ' "$names" | sort -t $'\t' -k1,1 > "$effective"
  while IFS=$'\t' read -r internal entry release; do
    # JPMS descriptors are inventoried by scanner/verifier but are not
    # loadable class identities and therefore do not participate in URL
    # class-loader first-origin collision policy.
    [[ "$internal" == module-info ]] && continue
    printf '%s\t%s\t%s\t%s\t%s\t%s\n' "$internal" "$position" "$role" \
      "$jar_path" "$entry" "$release" >> "$effective_inventory"
  done < "$effective"
done < "$candidate_runtime"

collision_pairs="$work_root/collision/pairs.tsv"
awk 'NR>1' "$effective_inventory" | sort -t $'\t' -k1,1 -k2,2n | awk -F '\t' '
  $1 != previous {previous=$1; fp=$2; fr=$3; fj=$4; fe=$5; frel=$6; next}
  {print $1 "\t" fp "\t" fr "\t" fj "\t" fe "\t" frel "\t" $2 "\t" $3 "\t" $4 "\t" $5 "\t" $6}
' > "$collision_pairs"
candidate_collisions="$output_abs/evidence/candidate-collisions.tsv"
printf 'lane\tinternal_name\tfirst_position\tfirst_role\tfirst_entry\tfirst_release\tfirst_sha256\tlater_position\tlater_role\tlater_entry\tlater_release\tlater_sha256\tbyte_relation\n' > "$candidate_collisions"
while IFS=$'\t' read -r internal fp fr fj fe frel lp lr lj le lrel; do
  first_sha=$(unzip -p "$fj" "$fe" | sha256sum | awk '{print $1}')
  later_sha=$(unzip -p "$lj" "$le" | sha256sum | awk '{print $1}')
  relation=BYTE_DIFFERENT
  [[ "$first_sha" == "$later_sha" ]] && relation=BYTE_IDENTICAL
  printf 'candidate\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\n' \
    "$internal" "$fp" "$fr" "$fe" "$frel" "$first_sha" \
    "$lp" "$lr" "$le" "$lrel" "$later_sha" "$relation" >> "$candidate_collisions"
done < "$collision_pairs"
collision_count=$(awk 'NR>1 {n++} END {print n+0}' "$candidate_collisions")
identical_count=$(awk -F '\t' 'NR>1 && $13=="BYTE_IDENTICAL" {n++} END {print n+0}' "$candidate_collisions")
different_count=$(awk -F '\t' 'NR>1 && $13=="BYTE_DIFFERENT" {n++} END {print n+0}' "$candidate_collisions")
non_jline_count=$(awk -F '\t' 'NR>1 && $2 !~ /^jline\// {n++} END {print n+0}' "$candidate_collisions")
[[ "$collision_count" == "$expected_collision_count" && \
   "$identical_count" == "$expected_identical_collision_count" && \
   "$different_count" == "$expected_different_collision_count" && \
   "$non_jline_count" == 0 ]] ||
  die "collision policy shape differs: rows=$collision_count identical=$identical_count different=$different_count non-jline=$non_jline_count"
gate_status[runtime-policy]=PASS

sort -u -o "$runtime_input_paths" "$runtime_input_paths"
runtime_input_manifest="$output_abs/evidence/runtime-inputs-pre.sha256"
while IFS= read -r input; do sha256sum "$input"; done < "$runtime_input_paths" > "$runtime_input_manifest"

write_tree_manifest "$jdk_root" "$output_abs/evidence/jdk-tree-pre.tsv"
[[ $(awk -F '\t' 'NR>1 && $1=="file" {n++} END{print n+0}' "$output_abs/evidence/jdk-tree-pre.tsv") == "$expected_jdk_file_count" && \
   $(awk -F '\t' 'NR>1 && $1=="directory" {n++} END{print n+0}' "$output_abs/evidence/jdk-tree-pre.tsv") == "$expected_jdk_directory_count" ]] ||
  die "written JDK seal has wrong shape"
verify_pinned_file "$output_abs/evidence/jdk-tree-pre.tsv" \
  "$expected_jdk_manifest_sha" jdk-tree-manifest

toolchain="$output_abs/evidence/toolchain.tsv"
execution_environment="$output_abs/evidence/execution-environment.tsv"
{
  printf 'key\tvalue\n'
  printf 'inheritance\tNONE-env-i\n'
  printf 'PATH\t/usr/bin:/bin\n'
  printf 'LC_ALL\tC\n'
  printf 'TZ\tUTC\n'
  printf 'JAVA_HOME\t%s\n' "$jdk_root"
  printf 'HOME.tools\t%s\n' "$isolated_home/tools"
  printf 'TMPDIR.tools\t%s\n' "$isolated_tmp/tools"
  printf 'HOME.production\t%s\n' "$isolated_home/production"
  printf 'TMPDIR.production\t%s\n' "$isolated_tmp/production"
  printf 'java.options\t-Xverify:all -XX:-UsePerfData fixed locale/home/tmp\n'
} > "$execution_environment"
{
  printf 'component\tpath\tsha256\n'
  printf 'java\t%s\t%s\n' "$java_bin" "$expected_java_bin_sha"
  printf 'javac\t%s\t%s\n' "$javac_bin" "$expected_javac_bin_sha"
  printf 'jdk-tree-manifest\t%s\t%s\n' "$jdk_root" "$expected_jdk_manifest_sha"
  printf 'asm\t%s\t%s\n' "$asm_jar" "$expected_asm_sha"
  printf 'compile-runner\t%s\t%s\n' "$compile_runner" "$expected_compile_runner_sha"
  printf 'comparator-source\t%s\t%s\n' "$comparator_source" "$expected_comparator_source_sha"
  printf 'scanner-source\t%s\t%s\n' "$scanner_source" "$expected_scanner_source_sha"
  printf 'verifier-source\t%s\t%s\n' "$verifier_source" "$expected_verifier_source_sha"
  printf 'wrapper\t%s\t%s\n' "$(realpath "$0")" "$(sha_of "$(realpath "$0")")"
  printf 'execution-environment\t%s\t%s\n' "$execution_environment" \
    "$(sha_of "$execution_environment")"
  printf 'sanitized-nano\t%s\t%s\n' "$sanitized_nano" "$expected_nano_derivative_sha"
} > "$toolchain"

run_logged() {
  local seconds=$1 stdout=$2 stderr=$3
  shift 3
  (
    cd "$work_root/cwd"
    exec /usr/bin/env -i PATH=/usr/bin:/bin HOME="$HOME" TMPDIR="$TMPDIR" \
      LC_ALL=C TZ=UTC JAVA_HOME="$jdk_root" /usr/bin/setsid /usr/bin/timeout \
      --signal=TERM --kill-after="${kill_grace_seconds}s" "${seconds}s" "$@"
  ) > "$stdout" 2> "$stderr" &
  active_pid=$!
  local status=0
  wait "$active_pid" || status=$?
  active_pid=""
  return "$status"
}

tool_java_common=(-Xverify:all -XX:-UsePerfData -Dfile.encoding=UTF-8 -Duser.language=en \
  -Duser.country=US -Duser.timezone=UTC -Duser.home="$isolated_home/tools" \
  -Djava.io.tmpdir="$isolated_tmp/tools")
java_common=(-Xverify:all -XX:-UsePerfData -Dfile.encoding=UTF-8 -Duser.language=en \
  -Duser.country=US -Duser.timezone=UTC -Duser.home="$isolated_home/production" \
  -Djava.io.tmpdir="$isolated_tmp/production")
if ! run_logged 30 "$output_abs/evidence/java-version.stdout" \
  "$output_abs/evidence/java-version.stderr" "$java_bin" "${tool_java_common[@]}" \
  -version; then
  gate_status[java-runtime-probe]=FAIL
  die "pinned Java runtime version probe failed"
fi
if ! grep -F 'openjdk version "11.0.22"' \
    "$output_abs/evidence/java-version.stderr" >/dev/null; then
  gate_status[java-runtime-probe]=FAIL
  die "runtime version probe did not report OpenJDK 11.0.22"
fi
if ! grep -F 'Corretto-11.0.22.7.1' \
    "$output_abs/evidence/java-version.stderr" >/dev/null; then
  gate_status[java-runtime-probe]=FAIL
  die "runtime version probe did not report Corretto-11.0.22.7.1"
fi
gate_status[java-runtime-probe]=PASS
verify_pinned_file "$comparator_source" "$expected_comparator_source_sha" comparator-source-before-compile
verify_pinned_file "$scanner_source" "$expected_scanner_source_sha" scanner-source-before-compile
verify_pinned_file "$verifier_source" "$expected_verifier_source_sha" verifier-source-before-compile
if run_logged "$tool_timeout_seconds" "$output_abs/evidence/tool-compile.stdout" \
  "$output_abs/evidence/tool-compile.stderr" "$javac_bin" -g:none -proc:none \
  -cp "$asm_jar" -d "$work_root/tool-classes" "$comparator_source" \
  "$scanner_source" "$verifier_source"; then
  gate_status[tool-compile]=PASS
else
  gate_status[tool-compile]=FAIL
  die "exact-AOT Java tool compilation failed"
fi
verify_pinned_file "$comparator_source" "$expected_comparator_source_sha" comparator-source-after-compile
verify_pinned_file "$scanner_source" "$expected_scanner_source_sha" scanner-source-after-compile
verify_pinned_file "$verifier_source" "$expected_verifier_source_sha" verifier-source-after-compile
write_tree_manifest "$work_root/tool-classes" "$output_abs/evidence/tool-classes.tsv"

if run_logged "$tool_timeout_seconds" "$output_abs/evidence/comparator-self-test.stdout" \
  "$output_abs/evidence/comparator-self-test.stderr" "$java_bin" "${tool_java_common[@]}" \
  -Xmx2G -cp "$work_root/tool-classes:$asm_jar" CompareExactSourceAot --self-test \
  "$output_abs/evidence/comparator-self-test"; then
  gate_status[comparator-self-test]=PASS
else gate_status[comparator-self-test]=FAIL; die "comparator self-test failed"; fi
if run_logged "$tool_timeout_seconds" "$output_abs/evidence/scanner-self-test.stdout" \
  "$output_abs/evidence/scanner-self-test.stderr" "$java_bin" "${tool_java_common[@]}" \
  -Xmx2G -cp "$work_root/tool-classes:$asm_jar" ScanExactAotBoundary --self-test \
  "$output_abs/evidence/scanner-self-test"; then
  gate_status[scanner-self-test]=PASS
else gate_status[scanner-self-test]=FAIL; die "scanner self-test failed"; fi
if run_logged "$tool_timeout_seconds" "$output_abs/evidence/verifier-self-test.stdout" \
  "$output_abs/evidence/verifier-self-test.stderr" "$java_bin" "${tool_java_common[@]}" \
  -Xmx2G -cp "$work_root/tool-classes:$asm_jar" VerifyExactAotRuntime --self-test \
  "$output_abs/evidence/verifier-self-test"; then
  gate_status[verifier-self-test]=PASS
else gate_status[verifier-self-test]=FAIL; die "verifier self-test failed"; fi

if [[ "$preflight_only" == true ]]; then
  write_tree_manifest "$jdk_root" "$output_abs/evidence/jdk-tree-post.tsv"
  verify_pinned_file "$output_abs/evidence/jdk-tree-post.tsv" \
    "$expected_jdk_manifest_sha" jdk-tree-post-manifest
  cmp "$output_abs/evidence/jdk-tree-pre.tsv" "$output_abs/evidence/jdk-tree-post.tsv" ||
    die "JDK image changed during preflight"
  runtime_input_post="$output_abs/evidence/runtime-inputs-post.sha256"
  while IFS= read -r input; do sha256sum "$input"; done \
    < "$runtime_input_paths" > "$runtime_input_post"
  cmp "$runtime_input_manifest" "$runtime_input_post" ||
    die "static runtime inputs changed during preflight"
  exit 0
fi

export HOME="$isolated_home/production"
export TMPDIR="$isolated_tmp/production"

gate_input_files=("$cohort" "$class_ownership" "$runtime_policy" \
  "$candidate_runtime" "$oracle_runtime" "$candidate_collisions" \
  "$allowed_resources" "$forbidden_payloads")
write_boundary_seal() {
  local gate=$1 phase=$2 extra=${3:-} destination="$output_abs/evidence/$gate-$phase.tsv"
  {
    printf 'kind\tpath\tsha256\n'
    printf 'tree\t%s\t%s\n' "$jdk_root" "$(tree_digest "$jdk_root")"
    printf 'tree\t%s\t%s\n' "$staged_source" "$(tree_digest "$staged_source")"
    printf 'tree\t%s\t%s\n' "$work_root/tool-classes" "$(tree_digest "$work_root/tool-classes")"
    printf 'tree\t%s\t%s\n' "$isolated_home/production" \
      "$(tree_digest "$isolated_home/production")"
    printf 'tree\t%s\t%s\n' "$isolated_tmp/production" \
      "$(tree_digest "$isolated_tmp/production")"
    printf 'tree\t%s\t%s\n' "$candidate_a" "$(tree_digest "$candidate_a")"
    printf 'tree\t%s\t%s\n' "$candidate_b" "$(tree_digest "$candidate_b")"
    while IFS= read -r input; do printf 'file\t%s\t%s\n' "$input" "$(sha_of "$input")"; done < "$runtime_input_paths"
    for input in "${gate_input_files[@]}"; do printf 'file\t%s\t%s\n' "$input" "$(sha_of "$input")"; done
    if [[ -n "$extra" ]]; then printf 'file\t%s\t%s\n' "$extra" "$(sha_of "$extra")"; fi
  } | sort -t $'\t' -k1,1 -k2,2 > "$destination"
}

expected_root="$output_abs/evidence/original-class-names"
mkdir -p "$expected_root"
while IFS=$'\t' read -r namespace_name scope source_entry source_sha owner_jar owner_sha expected_classes; do
  [[ "$namespace_name" == namespace ]] && continue
  awk -F '\t' -v namespace_name="$namespace_name" 'NR>1 && $5==namespace_name {print $1}' \
    "$class_ownership" | sort > "$expected_root/$namespace_name.txt"
  [[ $(wc -l < "$expected_root/$namespace_name.txt") == "$expected_classes" ]] ||
    die "owned class list count differs: $namespace_name"
done < "$cohort"

relation="$output_abs/evidence/namespace-relation.tsv"
printf 'namespace\tscope\texpected_classes\tclasses_a\tclasses_b\tdeterministic_paths\traw_bytes_diagnostic\tname_skeleton_diagnostic\tinitializer\tstatus\n' > "$relation"

compile_one() {
  local build=$1 namespace_name=$2 source_entry=$3 destination=$4 log_dir=$5 timing_dir=$6
  run_logged "$per_namespace_timeout_seconds" "$log_dir/$namespace_name.out" \
    "$log_dir/$namespace_name.err" /usr/bin/time -f \
    'elapsed_seconds\t%e\nmax_rss_kib\t%M\nexit_status\t%x' \
    -o "$timing_dir/$namespace_name.tsv" "$java_bin" "${java_common[@]}" \
    -server -Xms1G -Xmx1G -Dclojure.compiler.elide-meta='[:doc :file :line]' \
    -Dclojure.compile.warn-on-reflection=true -cp "$compile_classpath" \
    clojure.main "$compile_runner" "$namespace_name" "$staged_source" \
    "$source_entry" "$destination"
}

build_failed=false
namespace_index=0
write_boundary_seal candidate-compile pre
while IFS=$'\t' read -r namespace_name scope source_entry source_sha owner_jar owner_sha expected_classes; do
  [[ "$namespace_name" == namespace ]] && continue
  namespace_index=$((namespace_index + 1))
  echo "[$namespace_index/$namespace_count] compiling $namespace_name" >&2
  destination_a="$candidate_a/$namespace_name"
  destination_b="$candidate_b/$namespace_name"
  mkdir -p "$destination_a" "$destination_b"
  if ! compile_one A "$namespace_name" "$source_entry" "$destination_a" \
    "$output_abs/logs-a" "$output_abs/timing-a"; then
    printf '%s\t%s\t%s\t0\t0\tfalse\tfalse\tUNSUPPORTED\tfalse\tCOMPILE_A_FAILED\n' \
      "$namespace_name" "$scope" "$expected_classes" >> "$relation"
    build_failed=true
    continue
  fi
  if ! compile_one B "$namespace_name" "$source_entry" "$destination_b" \
    "$output_abs/logs-b" "$output_abs/timing-b"; then
    classes_a=$(find "$destination_a" -type f -name '*.class' | wc -l)
    printf '%s\t%s\t%s\t%s\t0\tfalse\tfalse\tUNSUPPORTED\tfalse\tCOMPILE_B_FAILED\n' \
      "$namespace_name" "$scope" "$expected_classes" "$classes_a" >> "$relation"
    build_failed=true
    continue
  fi
  names_a="$work_root/$namespace_name.a.names"
  names_b="$work_root/$namespace_name.b.names"
  hashes_a="$work_root/$namespace_name.a.hashes"
  hashes_b="$work_root/$namespace_name.b.hashes"
  skeleton_a="$work_root/$namespace_name.a.skeleton"
  skeleton_o="$work_root/$namespace_name.o.skeleton"
  (cd "$destination_a" && find . -type f -name '*.class' -printf '%P\n' | sort) > "$names_a"
  (cd "$destination_b" && find . -type f -name '*.class' -printf '%P\n' | sort) > "$names_b"
  (cd "$destination_a" && find . -type f -name '*.class' -print0 | sort -z | xargs -0 sha256sum) > "$hashes_a"
  (cd "$destination_b" && find . -type f -name '*.class' -print0 | sort -z | xargs -0 sha256sum) > "$hashes_b"
  classes_a=$(wc -l < "$names_a"); classes_b=$(wc -l < "$names_b")
  deterministic_paths=false; raw_bytes=false; skeleton=DIFFER; initializer=false; status=PASS
  cmp "$names_a" "$names_b" && deterministic_paths=true
  cmp "$hashes_a" "$hashes_b" && raw_bytes=true
  sed -E -e 's/__[0-9]+/__ID/g' -e 's/\$eval[0-9]+/\$evalID/g' \
    -e 's/\$inst_[0-9]+__/\$inst_ID__/g' "$names_a" | sort > "$skeleton_a"
  sed -E -e 's/__[0-9]+/__ID/g' -e 's/\$eval[0-9]+/\$evalID/g' \
    -e 's/\$inst_[0-9]+__/\$inst_ID__/g' "$expected_root/$namespace_name.txt" | sort > "$skeleton_o"
  cmp "$skeleton_a" "$skeleton_o" && skeleton=MATCH
  namespace_prefix=$(printf '%s' "$namespace_name" | tr '.' '/' | tr '-' '_')
  [[ -f "$destination_a/${namespace_prefix}__init.class" && \
     -f "$destination_b/${namespace_prefix}__init.class" ]] && initializer=true
  if [[ "$classes_a" != "$expected_classes" || "$classes_b" != "$expected_classes" || \
        "$deterministic_paths" != true || "$initializer" != true ]]; then
    status=FAIL; build_failed=true
  fi
  printf '%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\n' "$namespace_name" \
    "$scope" "$expected_classes" "$classes_a" "$classes_b" "$deterministic_paths" \
    "$raw_bytes" "$skeleton" "$initializer" "$status" >> "$relation"
done < "$cohort"
write_boundary_seal candidate-compile post
awk -F '\t' -v a="$candidate_a" -v b="$candidate_b" \
  'NR==1 || ($2!=a && $2!=b)' "$output_abs/evidence/candidate-compile-pre.tsv" \
  > "$work_root/candidate-compile-static-pre.tsv"
awk -F '\t' -v a="$candidate_a" -v b="$candidate_b" \
  'NR==1 || ($2!=a && $2!=b)' "$output_abs/evidence/candidate-compile-post.tsv" \
  > "$work_root/candidate-compile-static-post.tsv"
cmp "$work_root/candidate-compile-static-pre.tsv" \
  "$work_root/candidate-compile-static-post.tsv" ||
  die "candidate compilation mutated a sealed static input"
pre_candidate_sha=$(awk -F '\t' -v a="$candidate_a" '$2==a {print $3}' \
  "$output_abs/evidence/candidate-compile-pre.tsv")
post_candidate_sha=$(awk -F '\t' -v a="$candidate_a" '$2==a {print $3}' \
  "$output_abs/evidence/candidate-compile-post.tsv")
[[ -n "$pre_candidate_sha" && -n "$post_candidate_sha" && \
   "$pre_candidate_sha" != "$post_candidate_sha" ]] ||
  die "candidate compile unexpectedly left candidate A unchanged"
if [[ "$build_failed" == true ]]; then gate_status[candidate-compile]=FAIL; die "candidate compilation gate failed"; fi
gate_status[candidate-compile]=PASS

write_boundary_seal comparator pre
if run_logged "$gate_timeout_seconds" "$output_abs/evidence/comparator.stdout" \
  "$output_abs/evidence/comparator.stderr" "$java_bin" "${java_common[@]}" -Xmx3G \
  -cp "$work_root/tool-classes:$asm_jar" CompareExactSourceAot "$candidate_a" \
  "$candidate_b" "$original_jar" "$cohort" "$class_ownership" \
  "$output_abs/evidence/comparator"; then gate_status[comparator]=PASS
else gate_status[comparator]=FAIL; die "normalized whole-cohort comparator failed"; fi
write_boundary_seal comparator post
cmp "$output_abs/evidence/comparator-pre.tsv" "$output_abs/evidence/comparator-post.tsv" ||
  die "comparator mutated a sealed input or candidate tree"
class_mappings="$output_abs/evidence/comparator/class-mappings.tsv"
[[ -f "$class_mappings" ]] || die "comparator did not emit class mappings"

write_boundary_seal scanner pre "$class_mappings"
if run_logged "$gate_timeout_seconds" "$output_abs/evidence/scanner.stdout" \
  "$output_abs/evidence/scanner.stderr" "$java_bin" "${java_common[@]}" -Xmx4G \
  -cp "$work_root/tool-classes:$asm_jar" ScanExactAotBoundary --scan \
  "$candidate_a" "$candidate_b" "$original_jar" "$peer_jar" "$cohort" \
  "$class_ownership" "$class_mappings" "$runtime_policy" "$candidate_runtime" \
  "$oracle_runtime" "$candidate_collisions" "$allowed_resources" \
  "$forbidden_payloads" "$output_abs/evidence/scanner"; then gate_status[scanner]=PASS
else gate_status[scanner]=FAIL; die "exact-AOT boundary scanner failed"; fi
write_boundary_seal scanner post "$class_mappings"
cmp "$output_abs/evidence/scanner-pre.tsv" "$output_abs/evidence/scanner-post.tsv" ||
  die "scanner mutated a sealed input or candidate tree"

write_boundary_seal verifier pre "$class_mappings"
if run_logged "$gate_timeout_seconds" "$output_abs/evidence/verifier.stdout" \
  "$output_abs/evidence/verifier.stderr" "$java_bin" "${java_common[@]}" -Xmx4G \
  -cp "$work_root/tool-classes:$asm_jar" VerifyExactAotRuntime --verify \
  "$candidate_a" "$candidate_b" "$original_jar" "$cohort" "$class_ownership" \
  "$class_mappings" "$runtime_policy" "$candidate_runtime" "$oracle_runtime" \
  "$candidate_collisions" "$output_abs/evidence/verifier"; then gate_status[verifier]=PASS
else gate_status[verifier]=FAIL; die "exact-AOT JVM verifier failed"; fi
write_boundary_seal verifier post "$class_mappings"
cmp "$output_abs/evidence/verifier-pre.tsv" "$output_abs/evidence/verifier-post.tsv" ||
  die "verifier mutated a sealed input or candidate tree"

write_tree_manifest "$jdk_root" "$output_abs/evidence/jdk-tree-post.tsv"
verify_pinned_file "$output_abs/evidence/jdk-tree-post.tsv" \
  "$expected_jdk_manifest_sha" jdk-tree-post-manifest
cmp "$output_abs/evidence/jdk-tree-pre.tsv" "$output_abs/evidence/jdk-tree-post.tsv" ||
  die "JDK image changed during validation"
runtime_input_post="$output_abs/evidence/runtime-inputs-post.sha256"
while IFS= read -r input; do sha256sum "$input"; done < "$runtime_input_paths" > "$runtime_input_post"
cmp "$runtime_input_manifest" "$runtime_input_post" || die "static runtime inputs changed during validation"
run_complete=true
exit 0
