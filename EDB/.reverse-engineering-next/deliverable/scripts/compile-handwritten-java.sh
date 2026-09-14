#!/usr/bin/env bash

set -euo pipefail

script_dir=$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd)
project_dir=$(cd -- "$script_dir/.." && pwd)
datomic_home=${1:-"$project_dir/../../datomic/datomic-pro-1.0.7277"}
output_dir=${2:-"$project_dir/build/handwritten-java-classes"}
peer_jar="$datomic_home/peer-1.0.7277.jar"
source_list="$project_dir/reports/handwritten-java-sources.txt"
expected_sha=cb55c9d01e155f9965e3332cd8ef70be42cdefacb50c072076e9ecf22b9a23ba

[[ -f "$peer_jar" && -f "$source_list" ]] || {
  echo "missing peer JAR or handwritten source manifest" >&2
  exit 1
}

actual_sha=$(sha256sum "$peer_jar" | awk '{print $1}')
[[ "$actual_sha" == "$expected_sha" ]] || {
  echo "unexpected peer JAR SHA-256: $actual_sha" >&2
  exit 1
}

if [[ -e "$output_dir" ]] && [[ -n "$(find "$output_dir" -mindepth 1 -print -quit 2>/dev/null)" ]]; then
  echo "refusing to overwrite non-empty output directory: $output_dir" >&2
  exit 1
fi

mkdir -p "$output_dir"
java "$script_dir/CompileSources.java" \
  "$project_dir/src-java" \
  "$output_dir" \
  "$peer_jar:$datomic_home/lib/*" \
  "$source_list"

class_count=$(find "$output_dir" -type f -name '*.class' | wc -l)
[[ "$class_count" -eq 47 ]] || {
  echo "expected 47 compiled classes, found $class_count" >&2
  exit 1
}

echo "compiled the 43 handwritten Java sources into $class_count classes"
