#!/usr/bin/bash -p

set -euo pipefail
umask 077
unset BASH_ENV ENV CDPATH GLOBIGNORE JAVA_TOOL_OPTIONS JDK_JAVA_OPTIONS \
  _JAVA_OPTIONS CLASSPATH JAVA_HOME LD_PRELOAD LD_LIBRARY_PATH
IFS=$' \t\n'
export LC_ALL=C TZ=UTC
PATH=${DATOMIC_REV_SAFE_PATH:-/usr/lib/jvm/java-21-openjdk-amd64/bin:/usr/bin:/bin}
export PATH

[[ $# -eq 1 ]] || {
  echo "usage: require-one-namespace-aligned.sh NAMESPACE" >&2
  exit 2
}
namespace_name=$1
: "${DATOMIC_REV_SOURCE_CLASSPATH:?missing source-validation classpath}"
: "${DATOMIC_REV_LOG_ROOT:?missing source-validation log root}"
: "${DATOMIC_REV_JAVA_BIN:?missing pinned Java launcher}"
timeout_seconds=${DATOMIC_REV_NAMESPACE_TIMEOUT_SECONDS:-180}

[[ "$namespace_name" =~ ^[A-Za-z0-9_.-]+$ ]] || {
  echo "invalid namespace name: $namespace_name" >&2
  exit 2
}
[[ "$timeout_seconds" =~ ^[1-9][0-9]*$ && "$timeout_seconds" -le 1800 ]] || {
  echo "DATOMIC_REV_NAMESPACE_TIMEOUT_SECONDS must be 1..1800" >&2
  exit 2
}

script_dir=$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd)
safe_name=${namespace_name//[^A-Za-z0-9_.-]/_}
stdout_file="$DATOMIC_REV_LOG_ROOT/$safe_name.out"
stderr_file="$DATOMIC_REV_LOG_ROOT/$safe_name.err"
java_tmp_parent="$DATOMIC_REV_LOG_ROOT/java-tmp"
java_tmp_dir="$java_tmp_parent/$safe_name"
[[ ! -e "$stdout_file" && ! -L "$stdout_file" && \
   ! -e "$stderr_file" && ! -L "$stderr_file" ]] || {
  echo "refusing to overwrite namespace-load evidence: $namespace_name" >&2
  exit 2
}
mkdir -p "$java_tmp_parent"
[[ ! -e "$java_tmp_dir" && ! -L "$java_tmp_dir" ]] || {
  echo "refusing existing namespace Java temp directory: $java_tmp_dir" >&2
  exit 2
}
mkdir -p "$java_tmp_dir"

/usr/bin/timeout --signal=TERM --kill-after=15s "${timeout_seconds}s" \
  "$DATOMIC_REV_JAVA_BIN" -XX:+PerfDisableSharedMem -Xmx2g \
  -Djava.io.tmpdir="$java_tmp_dir" \
  '-Dclojure.compiler.elide-meta=[:doc :file :line]' \
  -cp "$DATOMIC_REV_SOURCE_CLASSPATH" clojure.main \
  "$script_dir/require_namespace.clj" "$namespace_name" \
  >"$stdout_file" 2>"$stderr_file"

[[ -z $(find "$java_tmp_dir" -mindepth 1 -print -quit) ]] || {
  echo "namespace load retained content in isolated Java temp: $namespace_name" >&2
  exit 1
}

result_count=$(grep -Fxc "PASS $namespace_name" "$stdout_file" || true)
[[ "$result_count" -eq 1 ]] || {
  echo "namespace load did not emit exactly one tagged result: $namespace_name" >&2
  exit 1
}
printf 'PASS %s\n' "$namespace_name"
