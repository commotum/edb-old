#!/usr/bin/env bash
set -euo pipefail

class_name=${1:?missing initializer class name}
: "${DATOMIC_TRANSACTOR_CLASSES_ROOT:?missing extracted class root}"
: "${DATOMIC_TRANSACTOR_SOURCE_ROOT:?missing recovered source root}"
: "${DATOMIC_TRANSACTOR_DECOMPILE_LOG_ROOT:?missing decompile log root}"
: "${DATOMIC_TRANSACTOR_DECOMPILER_CP:?missing decompiler classpath}"
: "${DATOMIC_TRANSACTOR_NAMESPACE_TIMEOUT:=1200}"
: "${DATOMIC_TRANSACTOR_JAVA_HEAP:=3g}"

[[ "$DATOMIC_TRANSACTOR_JAVA_HEAP" =~ ^[1-9][0-9]*[kKmMgG]$ ]] || {
  echo "DATOMIC_TRANSACTOR_JAVA_HEAP must be a positive JVM heap size: $DATOMIC_TRANSACTOR_JAVA_HEAP" >&2
  exit 2
}

script_dir=$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd)
safe_name=${class_name//[^A-Za-z0-9_.-]/_}
source_path=${class_name%__init}.clj
status_file="$DATOMIC_TRANSACTOR_DECOMPILE_LOG_ROOT/$safe_name.status"
stdout_file="$DATOMIC_TRANSACTOR_DECOMPILE_LOG_ROOT/$safe_name.out"
stderr_file="$DATOMIC_TRANSACTOR_DECOMPILE_LOG_ROOT/$safe_name.err"

rm -f -- "$DATOMIC_TRANSACTOR_SOURCE_ROOT/$source_path" "$status_file"
mkdir -p -- "$(dirname -- "$DATOMIC_TRANSACTOR_SOURCE_ROOT/$source_path")"

set +e
timeout --signal=TERM --kill-after=10s \
  "$DATOMIC_TRANSACTOR_NAMESPACE_TIMEOUT" \
  java -XX:+PerfDisableSharedMem "-Xmx$DATOMIC_TRANSACTOR_JAVA_HEAP" \
  -cp "$DATOMIC_TRANSACTOR_DECOMPILER_CP" \
  clojure.main "$script_dir/decompile_one.clj" \
  "$DATOMIC_TRANSACTOR_CLASSES_ROOT" \
  "$DATOMIC_TRANSACTOR_SOURCE_ROOT" \
  "$class_name" \
  >"$stdout_file" 2>"$stderr_file"
exit_status=$?
set -e

if [[ "$exit_status" -eq 0 && -s "$DATOMIC_TRANSACTOR_SOURCE_ROOT/$source_path" ]]; then
  printf 'pass\t0\n' > "$status_file"
  echo "PASS $class_name"
  exit 0
fi

if [[ "$exit_status" -eq 0 ]]; then
  exit_status=90
  echo "decompiler returned success without source: $source_path" \
    >> "$stderr_file"
fi
printf 'fail\t%s\n' "$exit_status" > "$status_file"
echo "FAIL $class_name status=$exit_status"
exit 1
