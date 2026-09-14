#!/usr/bin/env bash

set -euo pipefail

script_dir=$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd)
project_dir=$(cd -- "$script_dir/.." && pwd)
datomic_home=${1:-"$project_dir/../../datomic/datomic-pro-1.0.7277"}
work_root=${2:-$(mktemp -d -t datomic-surface-validation.XXXXXXXX)}
jobs=${JOBS:-4}
namespace_index="$project_dir/reports/source-index/namespaces.tsv"
peer_jar="$datomic_home/peer-1.0.7277.jar"
expected_sha=cb55c9d01e155f9965e3332cd8ef70be42cdefacb50c072076e9ecf22b9a23ba

[[ -f "$peer_jar" && -f "$namespace_index" ]] || {
  echo "missing peer JAR or namespace index" >&2
  exit 1
}

actual_sha=$(sha256sum "$peer_jar" | awk '{print $1}')
[[ "$actual_sha" == "$expected_sha" ]] || {
  echo "unexpected peer JAR SHA-256: $actual_sha" >&2
  exit 1
}

if [[ -e "$work_root" ]] && [[ -n "$(find "$work_root" -mindepth 1 -print -quit 2>/dev/null)" ]]; then
  echo "refusing to overwrite non-empty validation directory: $work_root" >&2
  exit 1
fi

build_root="$work_root/source-validation"
surface_root="$work_root/surfaces"
mkdir -p "$surface_root/original" "$surface_root/recovered" "$surface_root/logs"

JOBS="$jobs" "$script_dir/validate-all-namespaces.sh" "$datomic_home" "$build_root"

export DATOMIC_REV_ORIGINAL_CLASSPATH="$peer_jar:$build_root/infinispan-compile-stubs:$datomic_home/lib/*"
export DATOMIC_REV_SOURCE_CLASSPATH="$project_dir/src-clj:$build_root/handwritten-classes:$project_dir/resources:$build_root/infinispan-compile-stubs:$build_root/libraries-without-aot-core2/*"
export DATOMIC_REV_SURFACE_ROOT="$surface_root"

if ! awk -F '\t' 'NR > 1 {print $1}' "$namespace_index" \
  | xargs -P "$jobs" -n 1 "$script_dir/compare-one-namespace-surface.sh" \
  | tee "$surface_root/results.txt"; then
  echo "one or more runtime namespace surfaces differ; inspect $surface_root/logs" >&2
  exit 1
fi

pass_count=$(grep -c '^PASS ' "$surface_root/results.txt")
[[ "$pass_count" -eq 142 ]] || {
  echo "expected 142 surface matches, found $pass_count" >&2
  exit 1
}

echo "runtime Var surfaces match for all 142 namespaces"
echo "compared names, flags, callable arities, and root-value kinds"
echo "details: $surface_root"
