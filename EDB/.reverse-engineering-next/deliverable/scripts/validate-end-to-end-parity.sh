#!/usr/bin/env bash

set -euo pipefail

usage() {
  echo "usage: $0 DATOMIC_HOME [EMPTY_WORK_ROOT]" >&2
}

if [[ $# -lt 1 || $# -gt 2 ]]; then
  usage
  exit 2
fi

script_dir=$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd)
project_dir=$(cd -- "$script_dir/.." && pwd)
datomic_home=$1
peer_jar="$datomic_home/peer-1.0.7277.jar"
expected_peer_sha=cb55c9d01e155f9965e3332cd8ef70be42cdefacb50c072076e9ecf22b9a23ba

if [[ $# -eq 2 ]]; then
  work_root=$2
else
  work_root=$(mktemp -d -t datomic-end-to-end-parity.XXXXXXXX)
fi

[[ -f "$peer_jar" ]] || {
  echo "missing peer JAR: $peer_jar" >&2
  exit 1
}

actual_peer_sha=$(sha256sum "$peer_jar" | awk '{print $1}')
[[ "$actual_peer_sha" == "$expected_peer_sha" ]] || {
  echo "unexpected peer JAR SHA-256: $actual_peer_sha" >&2
  echo "expected: $expected_peer_sha" >&2
  exit 1
}

if [[ -e "$work_root" ]] && [[ -n "$(find "$work_root" -mindepth 1 -print -quit 2>/dev/null)" ]]; then
  echo "refusing to overwrite non-empty validation directory: $work_root" >&2
  exit 1
fi

mkdir -p "$work_root"
source_validation_root="$work_root/source-validation"
result_root="$work_root/end-to-end-parity"
mkdir -p "$result_root"

"$script_dir/validate-all-namespaces.sh" "$datomic_home" "$source_validation_root"

original_classpath="$peer_jar:$datomic_home/lib/*"
recovered_classpath="$project_dir/src-clj:$source_validation_root/handwritten-classes:$project_dir/resources:$source_validation_root/infinispan-compile-stubs:$source_validation_root/libraries-without-aot-core2/*"
probe="$script_dir/end_to_end_probe.clj"

run_probe() {
  local label=$1
  local classpath=$2
  local stdout_file="$result_root/$label.out"
  local stderr_file="$result_root/$label.err"

  if ! java -cp "$classpath" clojure.main "$probe" >"$stdout_file" 2>"$stderr_file"; then
    echo "$label end-to-end probe failed" >&2
    echo "stdout: $stdout_file" >&2
    echo "stderr: $stderr_file" >&2
    sed -n '1,240p' "$stdout_file" >&2
    sed -n '1,240p' "$stderr_file" >&2
    return 1
  fi

  local result_count
  result_count=$(awk '/^PARITY-RESULT / {count++} END {print count + 0}' "$stdout_file")
  if [[ "$result_count" -ne 1 ]]; then
    echo "$label probe emitted $result_count PARITY-RESULT lines; expected exactly one" >&2
    echo "stdout: $stdout_file" >&2
    echo "stderr: $stderr_file" >&2
    return 1
  fi

  awk '/^PARITY-RESULT / {print; exit}' "$stdout_file" >"$result_root/$label.result"
}

run_probe original "$original_classpath"
run_probe recovered "$recovered_classpath"

original_result="$result_root/original.result"
recovered_result="$result_root/recovered.result"
if ! cmp -s "$original_result" "$recovered_result"; then
  echo "end-to-end parity mismatch" >&2
  diff -u "$original_result" "$recovered_result" >&2 || true
  echo "details: $result_root" >&2
  exit 1
fi

result_sha=$(sha256sum "$original_result" | awk '{print $1}')
echo "end-to-end Datomic API parity passed"
echo "peer SHA-256: $actual_peer_sha"
echo "parity result SHA-256: $result_sha"
echo "original and recovered probes ran in independent JVMs"
echo "recovered classpath excluded the original peer and core2 AOT classes"
echo "details: $result_root"
