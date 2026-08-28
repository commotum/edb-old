#!/usr/bin/env bash

set -euo pipefail

export LC_ALL=C

script_dir=$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd)
project_dir=$(cd -- "$script_dir/.." && pwd)

if [[ $# -lt 1 || $# -gt 2 ]]; then
  echo "usage: $0 DATOMIC_HOME [EMPTY_OUTPUT_ROOT]" >&2
  exit 2
fi

datomic_home=$1
output_root=${2:-$(mktemp -d -t datomic-final-clojure.XXXXXXXX)}
namespace_index="$project_dir/reports/source-index/namespaces.tsv"
expected_manifest_sha=186b247033a461b4e5f391a302c43b9a727cf4fde0db2b10487a6c001b9da2e1

if [[ -e "$output_root" ]] &&
   [[ -n "$(find "$output_root" -mindepth 1 -print -quit 2>/dev/null)" ]]; then
  echo "refusing to overwrite non-empty output root: $output_root" >&2
  exit 1
fi
[[ ! -L "$output_root" ]] || {
  echo "output root may not be a symbolic link: $output_root" >&2
  exit 1
}
mkdir -p -- "$output_root"

baseline_dir="$output_root/decompiler-baseline"
final_dir="$output_root/src-clj"
mkdir -p -- "$final_dir"
"$script_dir/decompile-clojure.sh" "$datomic_home" "$baseline_dir"

while IFS=$'\t' read -r namespace_name source_path _; do
  [[ "$namespace_name" == namespace ]] && continue
  [[ -f "$baseline_dir/$source_path" ]] || {
    echo "decompiler baseline omitted namespace source: $source_path" >&2
    exit 1
  }
  mkdir -p -- "$final_dir/${source_path%/*}"
  cp -- "$baseline_dir/$source_path" "$final_dir/$source_path"
done <"$namespace_index"

overlay_paths=(
  datomic/common.clj
  datomic/core2/thread.clj
  datomic/crypto.clj
  datomic/datafy.clj
  datomic/datalog.clj
  datomic/db.clj
  datomic/future.clj
  datomic/index.clj
  datomic/io.clj
  datomic/memory_size.clj
  datomic/monitor.clj
  datomic/slf4j.clj
)

overlay_manifest="$output_root/hardening-overlay.tsv"
printf 'baseline_sha256\tfinal_sha256\tpath\n' >"$overlay_manifest"
for source_path in "${overlay_paths[@]}"; do
  [[ -f "$project_dir/src-clj/$source_path" ]] || {
    echo "checked-in hardening overlay omitted: $source_path" >&2
    exit 1
  }
  printf '%s\t%s\t%s\n' \
    "$(sha256sum "$baseline_dir/$source_path" | awk '{print $1}')" \
    "$(sha256sum "$project_dir/src-clj/$source_path" | awk '{print $1}')" \
    "$source_path" >>"$overlay_manifest"
  cp -- "$project_dir/src-clj/$source_path" "$final_dir/$source_path"
done

final_manifest="$output_root/final-clojure-sources.tsv"
printf 'sha256\tpath\n' >"$final_manifest"
while IFS=$'\t' read -r namespace_name source_path _; do
  [[ "$namespace_name" == namespace ]] && continue
  cmp -s "$project_dir/src-clj/$source_path" "$final_dir/$source_path" || {
    echo "reproduced final source differs: $source_path" >&2
    exit 1
  }
  printf '%s\t%s\n' \
    "$(sha256sum "$final_dir/$source_path" | awk '{print $1}')" \
    "src-clj/$source_path" >>"$final_manifest"
done <"$namespace_index"

actual_manifest_sha=$(sha256sum "$final_manifest" | awk '{print $1}')
[[ "$actual_manifest_sha" == "$expected_manifest_sha" ]] || {
  echo "final source manifest differs: $actual_manifest_sha" >&2
  exit 1
}

java -cp \
  "$project_dir/tools/tools.decompiler/src:$project_dir/tools/tools.decompiler/target/tools.decompiler-0.1.0-alpha1-standalone.jar" \
  clojure.main "$script_dir/validate_clojure.clj" "$final_dir"

echo "final 142-namespace recovered source reproduced"
echo "decompiler baseline: $baseline_dir"
echo "hardening overlay: $overlay_manifest"
echo "final manifest SHA-256: $actual_manifest_sha"
echo "final source: $final_dir"
