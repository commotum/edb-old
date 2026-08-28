#!/usr/bin/env bash
set -euo pipefail

export LC_ALL=C

script_dir=$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd)
transactor_dir=$(cd -- "$script_dir/.." && pwd)
project_dir=$(cd -- "$transactor_dir/.." && pwd)
datomic_home=${1:-"$project_dir/../../datomic/datomic-pro-1.0.7277"}
output_dir=${2:-/tmp/datomic-transactor-decompile}
jobs=${JOBS:-4}
namespace_timeout=${NAMESPACE_TIMEOUT:-1200}
java_heap=${DATOMIC_TRANSACTOR_JAVA_HEAP:-3g}
transactor_jar="$datomic_home/datomic-transactor-pro-1.0.7277.jar"
expected_sha=d90819b57e2138085ddc26c93314f953c2cd776d1d1aa8b43a6bec1e475d3692
decompiler_src="$project_dir/tools/tools.decompiler/src"
decompiler_jar="$project_dir/tools/tools.decompiler/target/tools.decompiler-0.1.0-alpha1-standalone.jar"

if [[ ! -f "$decompiler_jar" ]]; then
  decompiler_jar="$project_dir/tools/tools.decompiler-0.1.0-alpha1-standalone.jar"
fi

for command_name in java unzip sha256sum mktemp timeout xargs find sort awk \
    cp rg sed wc; do
  command -v "$command_name" >/dev/null || {
    echo "missing required command: $command_name" >&2
    exit 1
  }
done

[[ "$jobs" =~ ^[1-9][0-9]*$ ]] || {
  echo "JOBS must be a positive integer: $jobs" >&2
  exit 1
}
[[ "$namespace_timeout" =~ ^[1-9][0-9]*([smhd])?$ ]] || {
  echo "NAMESPACE_TIMEOUT must be a positive timeout value: $namespace_timeout" >&2
  exit 1
}
[[ "$java_heap" =~ ^[1-9][0-9]*[kKmMgG]$ ]] || {
  echo "DATOMIC_TRANSACTOR_JAVA_HEAP must be a positive JVM heap size: $java_heap" >&2
  exit 1
}
[[ -f "$transactor_jar" ]] || {
  echo "Transactor JAR not found: $transactor_jar" >&2
  exit 1
}
[[ -d "$decompiler_src" && -f "$decompiler_jar" ]] || {
  echo "patched tools.decompiler source/JAR is incomplete" >&2
  exit 1
}

actual_sha=$(sha256sum "$transactor_jar" | awk '{print $1}')
[[ "$actual_sha" == "$expected_sha" ]] || {
  echo "unexpected Transactor JAR SHA-256: $actual_sha" >&2
  exit 1
}

if [[ -e "$output_dir" ]] && \
   [[ -n "$(find "$output_dir" -mindepth 1 -print -quit 2>/dev/null)" ]]; then
  echo "refusing to overwrite non-empty output directory: $output_dir" >&2
  exit 1
fi
mkdir -p "$output_dir/source" "$output_dir/logs"

work_dir=$(mktemp -d -t datomic-transactor-decompile.XXXXXXXX)
cleanup() {
  rm -rf -- "$work_dir"
}
trap cleanup EXIT

classes_dir="$work_dir/classes"
mkdir -p "$classes_dir"
unzip -oq "$transactor_jar" -d "$classes_dir"

find "$classes_dir" -type f -name '*__init.class' -printf '%P\n' \
  | sed 's/\.class$//' \
  | sort > "$output_dir/init-classes.txt"

awk '/^datomic\// {print}' "$output_dir/init-classes.txt" \
  > "$output_dir/datomic-init-classes.txt"
awk '!/^datomic\// {print}' "$output_dir/init-classes.txt" \
  > "$output_dir/bundled-init-classes.txt"

init_count=$(wc -l < "$output_dir/init-classes.txt")
datomic_count=$(awk '/^datomic\// {n++} END {print n+0}' \
  "$output_dir/init-classes.txt")
bundled_count=$((init_count - datomic_count))
[[ "$init_count" -eq 247 && "$datomic_count" -eq 162 && "$bundled_count" -eq 85 ]] || {
  echo "unexpected initializer boundary: total=$init_count datomic=$datomic_count bundled=$bundled_count" >&2
  exit 1
}

java -cp "$decompiler_src:$decompiler_jar" clojure.main \
  "$project_dir/scripts/validate_decompiler.clj"

export DATOMIC_TRANSACTOR_CLASSES_ROOT="$classes_dir"
export DATOMIC_TRANSACTOR_SOURCE_ROOT="$output_dir/source"
export DATOMIC_TRANSACTOR_DECOMPILE_LOG_ROOT="$output_dir/logs"
export DATOMIC_TRANSACTOR_DECOMPILER_CP="$decompiler_src:$decompiler_jar"
export DATOMIC_TRANSACTOR_NAMESPACE_TIMEOUT="$namespace_timeout"
export DATOMIC_TRANSACTOR_JAVA_HEAP="$java_heap"

set +e
xargs -P "$jobs" -n 1 "$script_dir/decompile-one.sh" \
  < "$output_dir/datomic-init-classes.txt" \
  > "$output_dir/decompile-results.log"
xargs_status=$?
set -e

bundled_dir="$work_dir/bundled"
"$script_dir/recover-bundled-sources.sh" "$datomic_home" "$bundled_dir" \
  > "$output_dir/bundled-recovery.log"

# The two recovery domains are intentionally disjoint.  Refuse an overwrite
# instead of letting a bundled dependency source silently replace a recovered
# datomic.* namespace.
while IFS= read -r -d '' bundled_source; do
  relative_source=${bundled_source#"$bundled_dir/source/"}
  destination="$output_dir/source/$relative_source"
  [[ ! -e "$destination" && ! -L "$destination" ]] || {
    echo "bundled source collides with decompiled source: $relative_source" >&2
    exit 1
  }
done < <(find "$bundled_dir/source" -type f -print0)

cp -a "$bundled_dir/source/." "$output_dir/source/"
(cd "$output_dir/source" && \
  sha256sum -c "$bundled_dir/source-manifest.sha256") \
  > "$output_dir/bundled-merge-validation.log"
mkdir -p "$output_dir/bundled-evidence"
for evidence_name in ownership.tsv ownership-report.md \
    namespace-validation.tsv source-manifest.sha256 summary.tsv; do
  cp "$bundled_dir/$evidence_name" \
    "$output_dir/bundled-evidence/$evidence_name"
done

{
  printf 'initializer\tscope\trecovery_method\tstatus\texit_status\tsource_entry\n'
  while IFS= read -r class_name; do
    safe_name=${class_name//[^A-Za-z0-9_.-]/_}
    if [[ "$class_name" == datomic/* ]]; then
      scope=datomic
      recovery_method=decompiled
      source_entry=${class_name%__init}.clj
      if [[ -f "$output_dir/logs/$safe_name.status" ]]; then
        IFS=$'\t' read -r status exit_status \
          < "$output_dir/logs/$safe_name.status"
      else
        status=fail
        exit_status=91
      fi
    else
      scope=bundled
      recovery_method=shipped-source
      source_entry=$(awk -F '\t' -v entry="$class_name.class" \
        'NR > 1 && $2 == entry {print $8}' "$bundled_dir/ownership.tsv")
      if [[ -n "$source_entry" && -s "$output_dir/source/$source_entry" ]]; then
        status=pass
        exit_status=0
      else
        status=fail
        exit_status=92
      fi
    fi
    printf '%s\t%s\t%s\t%s\t%s\t%s\n' \
      "$class_name" "$scope" "$recovery_method" "$status" \
      "$exit_status" "$source_entry"
  done < "$output_dir/init-classes.txt"
} > "$output_dir/results.tsv"

result_row_count=$(awk 'NR > 1 {n++} END {print n+0}' \
  "$output_dir/results.tsv")
unique_source_entry_count=$(awk -F '\t' 'NR > 1 && $6 != "" {print $6}' \
  "$output_dir/results.tsv" | sort -u | wc -l)
datomic_pass_count=$(awk -F '\t' \
  '$2 == "datomic" && $4 == "pass" && $5 == "0" {n++} END {print n+0}' \
  "$output_dir/results.tsv")
bundled_pass_count=$(awk -F '\t' \
  '$2 == "bundled" && $4 == "pass" && $5 == "0" {n++} END {print n+0}' \
  "$output_dir/results.tsv")
pass_count=$(awk -F '\t' \
  'NR > 1 && $4 == "pass" && $5 == "0" {n++} END {print n+0}' \
  "$output_dir/results.tsv")
failure_count=$(awk -F '\t' \
  'NR > 1 && !($4 == "pass" && $5 == "0") {n++} END {print n+0}' \
  "$output_dir/results.tsv")
clj_count=$(find "$output_dir/source" -type f -name '*.clj' | wc -l)
cljc_count=$(find "$output_dir/source" -type f -name '*.cljc' | wc -l)
source_count=$((clj_count + cljc_count))

set +e
rg -n --glob '*.clj' --glob '*.cljc' 'BROKEN DECOMP' \
  "$output_dir/source/datomic" > "$output_dir/broken-decomp.tsv"
sentinel_status=$?
set -e
if [[ "$sentinel_status" -eq 1 ]]; then
  sentinel_count=0
elif [[ "$sentinel_status" -eq 0 ]]; then
  sentinel_count=$(wc -l < "$output_dir/broken-decomp.tsv")
else
  echo "BROKEN DECOMP scan failed with status $sentinel_status" >&2
  exit 1
fi

(cd "$output_dir/source" && \
  find . -type f \( -name '*.clj' -o -name '*.cljc' \) -printf '%P\0' \
    | sort -z \
    | xargs -0 sha256sum > "$output_dir/source-manifest.sha256")

java -cp "$decompiler_src:$decompiler_jar" clojure.main \
  "$project_dir/scripts/validate_clojure.clj" \
  "$output_dir/source/datomic" \
  > "$output_dir/read-validation.log"

{
  printf 'metric\tvalue\n'
  printf 'input.sha256\t%s\n' "$expected_sha"
  printf 'initializers.total\t%s\n' "$init_count"
  printf 'initializers.datomic\t%s\n' "$datomic_count"
  printf 'initializers.bundled\t%s\n' "$bundled_count"
  printf 'results.rows\t%s\n' "$result_row_count"
  printf 'results.unique_source_entries\t%s\n' \
    "$unique_source_entry_count"
  printf 'datomic.decompile.passed\t%s\n' "$datomic_pass_count"
  printf 'bundled.shipped_source.passed\t%s\n' "$bundled_pass_count"
  printf 'recovery.passed\t%s\n' "$pass_count"
  printf 'recovery.failed\t%s\n' "$failure_count"
  printf 'sources.written\t%s\n' "$source_count"
  printf 'sources.clj\t%s\n' "$clj_count"
  printf 'sources.cljc\t%s\n' "$cljc_count"
  printf 'datomic.broken_decomp_sentinels\t%s\n' "$sentinel_count"
  printf 'xargs.status\t%s\n' "$xargs_status"
} > "$output_dir/summary.tsv"

echo "Transactor source result: pass=$pass_count fail=$failure_count source=$source_count sentinels=$sentinel_count"
echo "details: $output_dir/results.tsv"

if [[ "$failure_count" -ne 0 || "$pass_count" -ne 247 || \
      "$datomic_pass_count" -ne 162 || "$bundled_pass_count" -ne 85 || \
      "$result_row_count" -ne 247 || \
      "$unique_source_entry_count" -ne 247 || \
      "$source_count" -ne 247 || "$clj_count" -ne 246 || \
      "$cljc_count" -ne 1 || "$sentinel_count" -ne 0 || \
      "$xargs_status" -ne 0 ]]; then
  exit 1
fi
