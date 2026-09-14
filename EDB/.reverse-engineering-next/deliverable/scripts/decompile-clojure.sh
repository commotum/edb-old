#!/usr/bin/env bash

set -euo pipefail

script_dir=$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd)
project_dir=$(cd -- "$script_dir/.." && pwd)
datomic_home=${1:-"$project_dir/../../datomic/datomic-pro-1.0.7277"}
output_dir=${2:-"$project_dir/src-clj-regenerated"}
peer_jar="$datomic_home/peer-1.0.7277.jar"
decompiler_src="$project_dir/tools/tools.decompiler/src"
decompiler_jar="$project_dir/tools/tools.decompiler/target/tools.decompiler-0.1.0-alpha1-standalone.jar"
expected_sha=cb55c9d01e155f9965e3332cd8ef70be42cdefacb50c072076e9ecf22b9a23ba

if [[ ! -f "$decompiler_jar" ]]; then
  decompiler_jar="$project_dir/tools/tools.decompiler-0.1.0-alpha1-standalone.jar"
fi

for command_name in java unzip sha256sum mktemp; do
  command -v "$command_name" >/dev/null || {
    echo "missing required command: $command_name" >&2
    exit 1
  }
done

[[ -f "$peer_jar" ]] || {
  echo "peer JAR not found: $peer_jar" >&2
  exit 1
}

[[ -d "$decompiler_src" && -f "$decompiler_jar" ]] || {
  echo "patched tools.decompiler source/JAR is incomplete" >&2
  exit 1
}

actual_sha=$(sha256sum "$peer_jar" | awk '{print $1}')
[[ "$actual_sha" == "$expected_sha" ]] || {
  echo "unexpected peer JAR SHA-256: $actual_sha" >&2
  echo "expected: $expected_sha" >&2
  exit 1
}

if [[ -e "$output_dir" ]] && [[ -n "$(find "$output_dir" -mindepth 1 -print -quit 2>/dev/null)" ]]; then
  echo "refusing to overwrite non-empty output directory: $output_dir" >&2
  exit 1
fi

mkdir -p "$output_dir"
work_dir=$(mktemp -d -t datomic-rev.XXXXXXXX)
cleanup() {
  rm -rf -- "$work_dir"
}
trap cleanup EXIT

classes_dir="$work_dir/classes"
report_file="$output_dir/decompile-report.edn"
log_file="$output_dir/decompile.log"
mkdir -p "$classes_dir"
unzip -oq "$peer_jar" -d "$classes_dir"

java -cp "$decompiler_src:$decompiler_jar" clojure.main \
  "$script_dir/validate_decompiler.clj"

java -Xmx4g -cp "$decompiler_src:$decompiler_jar" clojure.main \
  "$script_dir/decompile_clojure.clj" \
  "$classes_dir" "$output_dir" "$report_file" \
  >"$log_file" 2>&1

source_count=$(find "$output_dir" -type f -name '*.clj' | wc -l)
if [[ "$source_count" -ne 142 ]]; then
  echo "expected 142 reconstructed namespaces, found $source_count" >&2
  exit 1
fi

grep -q ':failure-count 0' "$report_file" || {
  echo "decompilation report contains failures: $report_file" >&2
  exit 1
}

java -cp "$decompiler_src:$decompiler_jar" clojure.main \
  "$script_dir/validate_clojure.clj" "$output_dir"

echo "reconstructed $source_count namespaces in $output_dir"
echo "details: $report_file"
