#!/usr/bin/env bash

set -euo pipefail

script_dir=$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd)
project_dir=$(cd -- "$script_dir/.." && pwd)
datomic_home=${1:-"$project_dir/../../datomic/datomic-pro-1.0.7277"}
work_root=${2:-$(mktemp -d -t datomic-source-validation.XXXXXXXX)}
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

classes_dir="$work_root/handwritten-classes"
stub_dir="$work_root/infinispan-compile-stubs"
library_dir="$work_root/libraries-without-aot-core2"
log_dir="$work_root/namespace-logs"
mkdir -p "$classes_dir" "$stub_dir" "$library_dir" "$log_dir"

"$script_dir/validate-peer-resources.sh" "$datomic_home" "$project_dir/resources"

java "$script_dir/CompileSources.java" \
  "$project_dir/src-java" \
  "$classes_dir" \
  "$peer_jar:$datomic_home/lib/*" \
  "$project_dir/reports/handwritten-java-sources.txt"

java -cp "$datomic_home/lib/*" clojure.main \
  "$project_dir/tools/infinispan-compile-stubs/build_stubs.clj" "$stub_dir"

for jar in "$datomic_home"/lib/*.jar; do
  jar_name=$(basename -- "$jar")
  if [[ "$jar_name" == core2-* ]]; then
    continue
  fi
  ln -s "$jar" "$library_dir/$jar_name"
done

export DATOMIC_REV_SOURCE_CLASSPATH="$project_dir/src-clj:$classes_dir:$project_dir/resources:$stub_dir:$library_dir/*"
export DATOMIC_REV_LOG_ROOT="$log_dir"

if ! awk -F '\t' 'NR > 1 {print $1}' "$namespace_index" \
  | xargs -P "$jobs" -n 1 "$script_dir/require-one-namespace.sh"; then
  echo "one or more source-only namespace loads failed; inspect $log_dir" >&2
  grep -h '^FAIL ' "$log_dir"/*.out >&2 || true
  exit 1
fi

pass_count=$(grep -h '^PASS ' "$log_dir"/*.out | wc -l)
[[ "$pass_count" -eq 142 ]] || {
  echo "expected 142 namespace passes, found $pass_count" >&2
  exit 1
}

behavior_out="$work_root/recovered-behaviors.out"
behavior_err="$work_root/recovered-behaviors.err"
if ! java -cp "$DATOMIC_REV_SOURCE_CLASSPATH" clojure.main \
  "$script_dir/validate_recovered_behaviors.clj" \
  >"$behavior_out" 2>"$behavior_err"; then
  echo "recovered behavior regression failed" >&2
  cat "$behavior_out" >&2
  cat "$behavior_err" >&2
  exit 1
fi
cat "$behavior_out"

echo "source-only validation passed for all 142 namespaces"
echo "original peer and core2 AOT classes were excluded"
echo "Hot Rod classes were compile-only stubs; do not use them at runtime"
echo "logs: $log_dir"
