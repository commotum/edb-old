#!/usr/bin/env bash
set -euo pipefail

export LC_ALL=C

script_dir=$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd)
transactor_dir=$(cd -- "$script_dir/.." && pwd)
project_dir=$(cd -- "$transactor_dir/.." && pwd)
datomic_home=${1:-"$project_dir/../../datomic/datomic-pro-1.0.7277"}
output_dir=${2:-/tmp/datomic-transactor-bundled-sources}
namespace_tsv="$transactor_dir/baseline/transactor-namespace-inits.tsv"
jar_tsv="$transactor_dir/baseline/distribution-jars.tsv"
inventory_tool="$transactor_dir/tools/MapBundledSources.java"
validator="$script_dir/validate_bundled_sources.clj"
clojure_jar="$datomic_home/lib/clojure-1.11.4.jar"
spec_jar="$datomic_home/lib/spec.alpha-0.3.218.jar"
core_specs_jar="$datomic_home/lib/core.specs.alpha-0.2.62.jar"

for command_name in awk find java sha256sum sort unzip wc; do
  command -v "$command_name" >/dev/null || {
    echo "missing required command: $command_name" >&2
    exit 1
  }
done

for required_path in \
  "$namespace_tsv" "$jar_tsv" "$inventory_tool" "$validator" \
  "$clojure_jar" "$spec_jar" "$core_specs_jar"; do
  [[ -f "$required_path" ]] || {
    echo "missing bundled-source input: $required_path" >&2
    exit 1
  }
done

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

java -XX:+PerfDisableSharedMem "$inventory_tool" \
  "$namespace_tsv" "$jar_tsv" "$datomic_home" \
  "$output_dir/ownership.tsv" "$output_dir/ownership-report.md" \
  > "$output_dir/inventory.log"

java -XX:+PerfDisableSharedMem \
  -cp "$clojure_jar:$spec_jar:$core_specs_jar" clojure.main \
  "$validator" "$output_dir/ownership.tsv" "$datomic_home" \
  "$output_dir/namespace-validation.tsv" \
  > "$output_dir/namespace-validation.log"

source_count=0
while IFS=$'\t' read -r namespace _ _ status match_count owner_jar _ \
      source_entry source_sha; do
  [[ "$namespace" == namespace ]] && continue
  [[ "$status" == single-exact-path && "$match_count" == 1 ]] || {
    echo "bundled namespace lacks one exact source: $namespace" >&2
    exit 1
  }
  [[ "$source_entry" != /* && "$source_entry" != *../* ]] || {
    echo "unsafe bundled source entry: $source_entry" >&2
    exit 1
  }
  destination="$output_dir/source/$source_entry"
  mkdir -p -- "$(dirname -- "$destination")"
  unzip -p "$datomic_home/$owner_jar" "$source_entry" > "$destination"
  actual_sha=$(sha256sum "$destination" | awk '{print $1}')
  [[ "$actual_sha" == "$source_sha" ]] || {
    echo "bundled source hash mismatch: $namespace" >&2
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

[[ "$source_count" -eq 85 && "$validation_count" -eq 85 && \
   "$failure_count" -eq 0 && "$clj_count" -eq 84 && \
   "$cljc_count" -eq 1 ]] || {
  echo "unexpected bundled-source closure: sources=$source_count validation=$validation_count failures=$failure_count clj=$clj_count cljc=$cljc_count" >&2
  exit 1
}

{
  printf 'metric\tvalue\n'
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

echo "recovered and validated $source_count exact bundled namespace sources"
echo "details: $output_dir/summary.tsv"
