#!/usr/bin/env bash
set -euo pipefail

export LC_ALL=C
export TZ=UTC

script_dir=$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd)
transactor_dir=$(cd -- "$script_dir/.." && pwd)
project_dir=$(cd -- "$transactor_dir/.." && pwd)
datomic_home=${1:-"$project_dir/../../datomic/datomic-pro-1.0.7277"}
run_a=${2:-/tmp/datomic-goal2-transactor-source-final-v11}
run_b=${3:-/tmp/datomic-goal2-transactor-source-final-v12}
evidence_dir=${4:-/tmp/datomic-goal2-transactor-source-final-v11-v12-evidence}
jobs=${JOBS:-8}
namespace_timeout=${NAMESPACE_TIMEOUT:-600}
namespace_heap=${DATOMIC_TRANSACTOR_JAVA_HEAP:-512m}

java_root=${DATOMIC_RECOVERY_JAVA_ROOT:-/usr/lib/jvm/java-21-openjdk-amd64}
java_bin="$java_root/bin/java"
runner="$script_dir/decompile-clojure.sh"
decompiler_src="$project_dir/tools/tools.decompiler/src"
decompiler_jar="$project_dir/tools/tools.decompiler/target/tools.decompiler-0.1.0-alpha1-standalone.jar"
distribution_tsv="$transactor_dir/baseline/distribution-jars.tsv"
transactor_jar="$datomic_home/datomic-transactor-pro-1.0.7277.jar"
expected_transactor_sha=d90819b57e2138085ddc26c93314f953c2cd776d1d1aa8b43a6bec1e475d3692
expected_java_sha=377196a32c5e4442b604bbf36add1c49cfe8680cd27edd693b60872e58895e9e
expected_decompiler_jar_sha=e96ed7c66a6a2bbf2586fe856f8159dc011c63f6cdb7b1f25d7037ae6a611928

die() {
  echo "Transactor Clojure twin recovery: $*" >&2
  exit 1
}

[[ "$jobs" =~ ^[1-9][0-9]*$ ]] || die "JOBS must be a positive integer"
[[ "$namespace_timeout" =~ ^[1-9][0-9]*([smhd])?$ ]] || \
  die "NAMESPACE_TIMEOUT must be a positive timeout"
[[ "$namespace_heap" =~ ^[1-9][0-9]*[kKmMgG]$ ]] || \
  die "DATOMIC_TRANSACTOR_JAVA_HEAP must be a positive JVM heap size"

for output_path in "$run_a" "$run_b" "$evidence_dir"; do
  output_abs=$(realpath -m "$output_path")
  project_abs=$(realpath "$project_dir")
  [[ "$output_abs" != "$project_abs" && "$output_abs" != "$project_abs"/* ]] || \
    die "recovery output must remain outside the repository: $output_abs"
  [[ ! -L "$output_path" ]] || die "recovery output may not be a symlink: $output_path"
  if [[ -e "$output_path" ]] && \
     [[ -n $(find "$output_path" -mindepth 1 -print -quit 2>/dev/null) ]]; then
    die "refusing to overwrite non-empty recovery output: $output_path"
  fi
done
[[ $(realpath -m "$run_a") != $(realpath -m "$run_b") ]] || \
  die "the two recovery roots must differ"
[[ $(realpath -m "$run_a") != $(realpath -m "$evidence_dir") && \
   $(realpath -m "$run_b") != $(realpath -m "$evidence_dir") ]] || \
  die "the evidence root must differ from both recovery roots"

for required_file in "$java_bin" "$runner" "$decompiler_jar" \
    "$distribution_tsv" "$transactor_jar"; do
  [[ -f "$required_file" && ! -L "$required_file" ]] || \
    die "required regular input is missing or symbolic: $required_file"
done
[[ -d "$decompiler_src" && ! -L "$decompiler_src" ]] || \
  die "decompiler source root is missing or symbolic"

[[ $(sha256sum "$transactor_jar" | awk '{print $1}') == \
   "$expected_transactor_sha" ]] || die "licensed Transactor hash mismatch"
[[ $(sha256sum "$java_bin" | awk '{print $1}') == "$expected_java_sha" ]] || \
  die "Corretto java binary hash mismatch"
[[ $(sha256sum "$decompiler_jar" | awk '{print $1}') == \
   "$expected_decompiler_jar_sha" ]] || die "standalone decompiler JAR hash mismatch"
"$java_bin" -version 2>&1 | grep -q 'openjdk version "21.0.12"' || \
  die "exact OpenJDK 21.0.12 runtime is required"

mkdir -p "$run_a" "$run_b" "$evidence_dir/inputs" "$evidence_dir/runs"
evidence_abs=$(realpath "$evidence_dir")

export PATH="$java_root/bin:$PATH"
[[ $(realpath "$(command -v java)") == $(realpath "$java_bin") ]] || \
  die "PATH did not resolve java to the pinned OpenJDK binary"

command_names=(awk bash comm cp diff dirname find grep head java mkdir \
  mktemp realpath rg rm sed sha256sum sort tail timeout unzip wc xargs)
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

tool_files=(
  transactor/scripts/recover-clojure-twins.sh
  transactor/scripts/decompile-clojure.sh
  transactor/scripts/decompile-one.sh
  transactor/scripts/decompile_one.clj
  transactor/scripts/recover-bundled-sources.sh
  transactor/scripts/validate_bundled_sources.clj
  transactor/tools/MapBundledSources.java
  scripts/validate_decompiler.clj
  scripts/validate_clojure.clj
)

input_paths="$evidence_abs/inputs/input-paths.txt"
{
  find "$decompiler_src" -type f -print
  find "$transactor_dir/baseline" -type f -print
  printf '%s\n' "$decompiler_jar" "$transactor_jar" "$java_bin"
  for tool_file in "${tool_files[@]}"; do
    printf '%s\n' "$project_dir/$tool_file"
  done
  awk -F '\t' -v root="$datomic_home" 'NR > 1 {print root "/" $2}' \
    "$distribution_tsv"
  awk -F '\t' 'NR > 1 {print $2}' "$command_paths"
} | sort -u >"$input_paths"

while IFS= read -r input_path; do
  [[ -f "$input_path" && ! -L "$input_path" ]] || \
    die "sealed input is missing, non-regular, or symbolic: $input_path"
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
    printf 'root\ttype\trelative_path\n'
    for membership_row in \
      "decompiler-src|$decompiler_src" \
      "transactor-baseline|$transactor_dir/baseline"; do
      membership_name=${membership_row%%|*}
      membership_root=${membership_row#*|}
      if [[ -n $(find "$membership_root" -mindepth 1 ! -type f ! -type d -print -quit) ]]; then
        die "sealed input tree contains a symlink or special entry: $membership_root"
      fi
      find "$membership_root" -mindepth 1 \( -type d -o -type f \) \
        -printf "$membership_name\t%y\t%P\n"
    done | sort
  } >"$destination"
}

membership_manifest="$evidence_abs/inputs/pre-membership.tsv"
write_input_membership "$membership_manifest"
membership_sha=$(sha256sum "$membership_manifest" | awk '{print $1}')

verify_inputs_unchanged() {
  local phase=$1
  local phase_membership="$evidence_abs/inputs/$phase-membership.tsv"
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
  printf 'INPUTS_UNCHANGED %s\n' "$phase" \
    >>"$evidence_abs/inputs/$phase-verification.log"
}

verify_distribution_inputs() {
  while IFS=$'\t' read -r _ distribution_path expected_sha _; do
    [[ "$distribution_path" == distribution_path ]] && continue
    distribution_file="$datomic_home/$distribution_path"
    [[ -f "$distribution_file" && ! -L "$distribution_file" ]] || \
      die "distribution input missing or symbolic: $distribution_file"
    [[ $(sha256sum "$distribution_file" | awk '{print $1}') == "$expected_sha" ]] || \
      die "distribution input hash mismatch: $distribution_path"
  done <"$distribution_tsv"
}
verify_distribution_inputs

run_recovery() {
  local label=$1
  local destination=$2
  JOBS="$jobs" NAMESPACE_TIMEOUT="$namespace_timeout" \
    DATOMIC_TRANSACTOR_JAVA_HEAP="$namespace_heap" \
    "$runner" "$datomic_home" "$destination" \
    >"$evidence_abs/runs/$label.stdout" \
    2>"$evidence_abs/runs/$label.stderr"
  grep -qx 'metric[[:space:]]value' "$destination/summary.tsv" || \
    die "$label did not emit its canonical summary"
  [[ $(awk -F '\t' '$1 == "recovery.passed" {print $2}' \
       "$destination/summary.tsv") == 247 ]] || die "$label did not recover 247 namespaces"
  [[ $(awk -F '\t' '$1 == "recovery.failed" {print $2}' \
       "$destination/summary.tsv") == 0 ]] || die "$label recorded a recovery failure"
  [[ $(awk -F '\t' '$1 == "datomic.broken_decomp_sentinels" {print $2}' \
       "$destination/summary.tsv") == 0 ]] || die "$label retained BROKEN DECOMP output"
  [[ $(find "$destination/source" -type f \( -name '*.clj' -o -name '*.cljc' \) \
       | awk 'END {print NR + 0}') == 247 ]] || die "$label source count is not 247"
  [[ -z $(find "$destination/source" -mindepth 1 ! -type f ! -type d -print -quit) ]] || \
    die "$label source tree contains a symlink or special entry"
  (cd "$destination/source" && sha256sum -c "$destination/source-manifest.sha256") \
    >"$evidence_abs/runs/$label-source-verification.log"
}

run_recovery canonical-v11 "$run_a"
verify_inputs_unchanged post-v11
verify_distribution_inputs
run_recovery canonical-v12 "$run_b"
verify_inputs_unchanged post-v12
verify_distribution_inputs

cmp -s "$run_a/source-manifest.sha256" "$run_b/source-manifest.sha256" || \
  die "canonical twin source manifests differ"
if ! diff -qr "$run_a/source" "$run_b/source" \
  >"$evidence_abs/runs/source-tree.diff"; then
  die "canonical twin source trees differ"
fi
for deterministic_report in results.tsv summary.tsv broken-decomp.tsv \
    init-classes.txt datomic-init-classes.txt bundled-init-classes.txt \
    datomic-decompile-init-classes.txt datomic-dependency-init-classes.txt; do
  cmp -s "$run_a/$deterministic_report" "$run_b/$deterministic_report" || \
    die "canonical twin report differs: $deterministic_report"
done

cp -- "$run_a/source-manifest.sha256" \
  "$evidence_abs/runs/canonical-source-manifest.sha256"
cp -- "$run_a/results.tsv" "$evidence_abs/runs/canonical-results.tsv"
cp -- "$run_a/summary.tsv" "$evidence_abs/runs/canonical-summary.tsv"

"$java_bin" -version >"$evidence_abs/inputs/java-version.txt" 2>&1
{
  printf 'status=PASS\n'
  printf 'input.transactor.sha256=%s\n' "$expected_transactor_sha"
  printf 'input.manifest.sha256=%s\n' "$input_manifest_sha"
  printf 'input.membership.sha256=%s\n' "$membership_sha"
  printf 'java.binary=%s\n' "$(realpath "$java_bin")"
  printf 'java.binary.sha256=%s\n' "$expected_java_sha"
  printf 'java.version=OpenJDK-21.0.12+8-1-24.04-Ubuntu\n'
  printf 'decompiler.jar.sha256=%s\n' "$expected_decompiler_jar_sha"
  printf 'configuration.jobs=%s\n' "$jobs"
  printf 'configuration.namespace-timeout=%s\n' "$namespace_timeout"
  printf 'configuration.namespace-heap=%s\n' "$namespace_heap"
  printf 'canonical.v11=%s\n' "$(realpath "$run_a")"
  printf 'canonical.v12=%s\n' "$(realpath "$run_b")"
  printf 'canonical.source.count=247\n'
  printf 'canonical.source.trees.byte-identical=true\n'
  printf 'canonical.source.manifest.sha256=%s\n' \
    "$(sha256sum "$run_a/source-manifest.sha256" | awk '{print $1}')"
  printf 'canonical.results.sha256=%s\n' \
    "$(sha256sum "$run_a/results.tsv" | awk '{print $1}')"
  printf 'canonical.summary.sha256=%s\n' \
    "$(sha256sum "$run_a/summary.tsv" | awk '{print $1}')"
  printf 'canonical.broken-decomp.sha256=%s\n' \
    "$(sha256sum "$run_a/broken-decomp.tsv" | awk '{print $1}')"
  printf 'licensed.originals.copied=false\n'
  printf 'inputs.post-v11=PASS\n'
  printf 'inputs.post-v12=PASS\n'
} >"$evidence_abs/summary.properties"

(cd "$evidence_abs" && \
  find . -type f ! -path './manifest.sha256' -print0 \
    | sort -z | xargs -0 sha256sum) >"$evidence_abs/manifest.sha256"
(cd "$evidence_abs" && sha256sum -c manifest.sha256) >/dev/null

echo "Transactor Clojure canonical twins passed: $run_a and $run_b"
echo "Evidence: $evidence_abs"
