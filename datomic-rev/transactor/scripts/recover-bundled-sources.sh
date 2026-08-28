#!/usr/bin/env bash
set -euo pipefail

export LC_ALL=C

script_dir=$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd)
transactor_dir=$(cd -- "$script_dir/.." && pwd)
project_dir=$(cd -- "$transactor_dir/.." && pwd)
datomic_home=${1:-"$project_dir/../../datomic/datomic-pro-1.0.7277"}
output_dir=${2:-/tmp/datomic-transactor-bundled-sources}
source_scope=${3:-bundled}
recovery_java_tmp_root=${DATOMIC_RECOVERY_JAVA_TMPDIR:-}
namespace_tsv="$transactor_dir/baseline/transactor-namespace-inits.tsv"
jar_tsv="$transactor_dir/baseline/distribution-jars.tsv"
inventory_tool="$transactor_dir/tools/MapBundledSources.java"
validator="$script_dir/validate_bundled_sources.clj"
clojure_jar="$datomic_home/lib/clojure-1.11.4.jar"
spec_jar="$datomic_home/lib/spec.alpha-0.3.218.jar"
core_specs_jar="$datomic_home/lib/core.specs.alpha-0.2.62.jar"

for command_name in awk find java sha256sum sort unzip wc rmdir; do
  command -v "$command_name" >/dev/null || {
    echo "missing required command: $command_name" >&2
    exit 1
  }
done

if [[ -n "$recovery_java_tmp_root" ]]; then
  [[ "$recovery_java_tmp_root" == /* &&
     -d "$recovery_java_tmp_root" &&
     ! -L "$recovery_java_tmp_root" &&
     -w "$recovery_java_tmp_root" ]] || {
    echo "DATOMIC_RECOVERY_JAVA_TMPDIR must be an absolute, writable, non-symlink directory: $recovery_java_tmp_root" >&2
    exit 1
  }
  [[ -z $(find "$recovery_java_tmp_root" -mindepth 1 -print -quit) ]] || {
    echo "DATOMIC_RECOVERY_JAVA_TMPDIR must start empty: $recovery_java_tmp_root" >&2
    exit 1
  }
  recovery_java_tmp_root=$(cd -- "$recovery_java_tmp_root" && pwd -P)
fi

run_recovery_java() {
  local label=$1
  shift
  if [[ -z "$recovery_java_tmp_root" ]]; then
    java -XX:+PerfDisableSharedMem "$@"
    return
  fi
  local java_tmpdir="$recovery_java_tmp_root/$label"
  [[ ! -e "$java_tmpdir" && ! -L "$java_tmpdir" ]] || {
    echo "bundled-source Java tmp path already exists: $java_tmpdir" >&2
    return 70
  }
  mkdir "$java_tmpdir" || return 70
  local java_status
  if java -XX:+PerfDisableSharedMem \
      "-Djava.io.tmpdir=$java_tmpdir" "$@"; then
    java_status=0
  else
    java_status=$?
  fi
  if [[ -n $(find "$java_tmpdir" -mindepth 1 -print -quit) ]]; then
    echo "bundled-source Java tmp directory is dirty: $java_tmpdir" >&2
    return 70
  fi
  rmdir "$java_tmpdir" || return 70
  return "$java_status"
}

for required_path in \
  "$namespace_tsv" "$jar_tsv" "$inventory_tool" "$validator" \
  "$clojure_jar" "$spec_jar" "$core_specs_jar"; do
  [[ -f "$required_path" ]] || {
    echo "missing bundled-source input: $required_path" >&2
    exit 1
  }
done

case "$source_scope" in
  bundled)
    expected_namespace_count=85
    expected_source_count=85
    expected_clj_count=84
    expected_cljc_count=1
    missing_policy=
    ;;
  datomic)
    expected_namespace_count=162
    expected_source_count=2
    expected_clj_count=2
    expected_cljc_count=0
    missing_policy=allow-missing
    ;;
  *)
    echo "unsupported dependency-source scope: $source_scope" >&2
    exit 1
    ;;
esac

if [[ -e "$output_dir" ]] && \
   [[ -n "$(find "$output_dir" -mindepth 1 -print -quit 2>/dev/null)" ]]; then
  echo "refusing to overwrite non-empty output directory: $output_dir" >&2
  exit 1
fi
[[ ! -L "$output_dir" ]] || {
  echo "output directory may not be a symbolic link: $output_dir" >&2
  exit 1
}
mkdir -p "$output_dir/source"

(
  cd "$transactor_dir/baseline"
  sha256sum -c manifest.sha256
) > "$output_dir/baseline-validation.log"

run_recovery_java inventory "$inventory_tool" \
  "$namespace_tsv" "$jar_tsv" "$datomic_home" \
  "$output_dir/ownership.tsv" "$output_dir/ownership-report.md" "$source_scope" \
  > "$output_dir/inventory.log"

validator_args=(
  "$validator"
  "$output_dir/ownership.tsv"
  "$datomic_home"
  "$output_dir/namespace-validation.tsv"
)
if [[ -n "$missing_policy" ]]; then
  validator_args+=("$missing_policy")
fi
run_recovery_java validate \
  -cp "$clojure_jar:$spec_jar:$core_specs_jar" clojure.main \
  "${validator_args[@]}" > "$output_dir/namespace-validation.log"

source_count=0
while IFS=$'\t' read -r namespace _ _ status match_count owner_jar _ \
      source_entry source_sha; do
  [[ "$namespace" == namespace ]] && continue
  if [[ "$status" == missing && "$match_count" == 0 && \
        "$source_scope" == datomic ]]; then
    continue
  fi
  [[ "$status" == single-exact-path && "$match_count" == 1 ]] || {
    echo "$source_scope namespace lacks one exact source: $namespace" >&2
    exit 1
  }
  [[ "$source_entry" != /* && "$source_entry" != *../* ]] || {
    echo "unsafe $source_scope source entry: $source_entry" >&2
    exit 1
  }
  destination="$output_dir/source/$source_entry"
  mkdir -p -- "$(dirname -- "$destination")"
  unzip -p "$datomic_home/$owner_jar" "$source_entry" > "$destination"
  actual_sha=$(sha256sum "$destination" | awk '{print $1}')
  [[ "$actual_sha" == "$source_sha" ]] || {
    echo "$source_scope source hash mismatch: $namespace" >&2
    exit 1
  }
  source_count=$((source_count + 1))
done < "$output_dir/ownership.tsv"

(
  cd "$output_dir/source"
  find . -type f \( -name '*.clj' -o -name '*.cljc' \) -printf '%P\0' \
    | sort -z \
    | xargs -0 sha256sum
) > "$output_dir/source-manifest.sha256"

validation_count=$(awk -F '\t' 'NR > 1 && $7 == "pass" {n++} END {print n+0}' \
  "$output_dir/namespace-validation.tsv")
failure_count=$(awk -F '\t' 'NR > 1 && $7 != "pass" {n++} END {print n+0}' \
  "$output_dir/namespace-validation.tsv")
clj_count=$(find "$output_dir/source" -type f -name '*.clj' | wc -l)
cljc_count=$(find "$output_dir/source" -type f -name '*.cljc' | wc -l)

namespace_count=$(awk 'NR > 1 {n++} END {print n+0}' \
  "$output_dir/ownership.tsv")

[[ "$namespace_count" -eq "$expected_namespace_count" && \
   "$source_count" -eq "$expected_source_count" && \
   "$validation_count" -eq "$expected_source_count" && \
   "$failure_count" -eq 0 && "$clj_count" -eq "$expected_clj_count" && \
   "$cljc_count" -eq "$expected_cljc_count" ]] || {
  echo "unexpected $source_scope source closure: namespaces=$namespace_count sources=$source_count validation=$validation_count failures=$failure_count clj=$clj_count cljc=$cljc_count" >&2
  exit 1
}

{
  printf 'metric\tvalue\n'
  if [[ "$source_scope" == datomic ]]; then
    printf 'namespace_scope\t%s\n' "$source_scope"
    printf 'namespaces.audited\t%s\n' "$namespace_count"
  fi
  printf 'sources.total\t%s\n' "$source_count"
  printf 'sources.clj\t%s\n' "$clj_count"
  printf 'sources.cljc\t%s\n' "$cljc_count"
  printf 'namespace_validation.passed\t%s\n' "$validation_count"
  printf 'namespace_validation.failed\t%s\n' "$failure_count"
  printf 'ownership.sha256\t%s\n' \
    "$(sha256sum "$output_dir/ownership.tsv" | awk '{print $1}')"
  printf 'ownership_report.sha256\t%s\n' \
    "$(sha256sum "$output_dir/ownership-report.md" | awk '{print $1}')"
  printf 'namespace_validation.sha256\t%s\n' \
    "$(sha256sum "$output_dir/namespace-validation.tsv" | awk '{print $1}')"
  printf 'source_manifest.sha256\t%s\n' \
    "$(sha256sum "$output_dir/source-manifest.sha256" | awk '{print $1}')"
} > "$output_dir/summary.tsv"

echo "recovered and validated $source_count exact $source_scope namespace sources"
echo "details: $output_dir/summary.tsv"
