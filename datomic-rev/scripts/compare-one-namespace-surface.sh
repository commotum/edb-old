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
  echo "usage: compare-one-namespace-surface.sh NAMESPACE" >&2
  exit 2
}
namespace_name=$1
script_dir=$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd)
project_dir=$(cd -- "$script_dir/.." && pwd)
: "${DATOMIC_REV_ORIGINAL_CLASSPATH:?missing original peer classpath}"
: "${DATOMIC_REV_SOURCE_CLASSPATH:?missing recovered-source classpath}"
: "${DATOMIC_REV_SURFACE_ROOT:?missing namespace-surface output root}"
: "${DATOMIC_REV_JAVA_BIN:?missing pinned Java launcher}"
namespace_timeout_seconds=${DATOMIC_REV_NAMESPACE_TIMEOUT_SECONDS:-180}

[[ "$namespace_name" =~ ^[A-Za-z0-9_.-]+$ ]] || {
  echo "invalid namespace name: $namespace_name" >&2
  exit 2
}
[[ ${DATOMIC_VAR_METADATA_VIEW:-exact} == exact ]] || {
  echo "promotion surface comparison requires DATOMIC_VAR_METADATA_VIEW=exact" >&2
  exit 2
}
[[ -f "$DATOMIC_REV_JAVA_BIN" && ! -L "$DATOMIC_REV_JAVA_BIN" ]] || {
  echo "pinned Java launcher is missing or symbolic" >&2
  exit 2
}
[[ "$namespace_timeout_seconds" =~ ^[1-9][0-9]*$ && \
   "$namespace_timeout_seconds" -le 1800 ]] || {
  echo "DATOMIC_REV_NAMESPACE_TIMEOUT_SECONDS must be 1..1800" >&2
  exit 2
}
[[ -x /usr/bin/timeout && ! -L /usr/bin/timeout ]] || {
  echo "pinned timeout launcher is missing or symbolic" >&2
  exit 2
}

peer_emitter="$script_dir/emit_namespace_surface.clj"
structural_emitter="$project_dir/transactor/scripts/emit_structural_surface.clj"
for emitter in "$peer_emitter" "$structural_emitter"; do
  [[ -f "$emitter" && ! -L "$emitter" ]] || {
    echo "surface emitter is missing or symbolic: $emitter" >&2
    exit 2
  }
done

[[ -d "$DATOMIC_REV_SURFACE_ROOT" && ! -L "$DATOMIC_REV_SURFACE_ROOT" ]] || {
  echo "namespace-surface root is missing or symbolic" >&2
  exit 2
}
mkdir -p "$DATOMIC_REV_SURFACE_ROOT/original" \
  "$DATOMIC_REV_SURFACE_ROOT/recovered" "$DATOMIC_REV_SURFACE_ROOT/logs"

safe_name=${namespace_name//[^A-Za-z0-9_.-]/_}
original_out="$DATOMIC_REV_SURFACE_ROOT/original/$safe_name.edn"
source_out="$DATOMIC_REV_SURFACE_ROOT/recovered/$safe_name.edn"
original_raw="$DATOMIC_REV_SURFACE_ROOT/logs/$safe_name.original.out"
source_raw="$DATOMIC_REV_SURFACE_ROOT/logs/$safe_name.recovered.out"
original_err="$DATOMIC_REV_SURFACE_ROOT/logs/$safe_name.original.err"
source_err="$DATOMIC_REV_SURFACE_ROOT/logs/$safe_name.recovered.err"
result_dir="$DATOMIC_REV_SURFACE_ROOT/results"
result_file="$result_dir/$safe_name.tsv"
proxy_root="$DATOMIC_REV_SURFACE_ROOT/proxy-exclusions"
original_proxy="$proxy_root/original/$safe_name.edn"
source_proxy="$proxy_root/recovered/$safe_name.edn"
metadata_type_root="$DATOMIC_REV_SURFACE_ROOT/metadata-types"
original_metadata_types="$metadata_type_root/original/$safe_name.txt"
source_metadata_types="$metadata_type_root/recovered/$safe_name.txt"
semantic_proxy_root="$DATOMIC_REV_SURFACE_ROOT/semantic-proxies"
original_semantic_proxies="$semantic_proxy_root/original/$safe_name.edn"
source_semantic_proxies="$semantic_proxy_root/recovered/$safe_name.edn"
java_tmp_root="$DATOMIC_REV_SURFACE_ROOT/java-tmp/$safe_name"
original_exact_java_tmp="$java_tmp_root/original-exact"
source_exact_java_tmp="$java_tmp_root/recovered-exact"
original_diagnostic_java_tmp="$java_tmp_root/original-diagnostic"
source_diagnostic_java_tmp="$java_tmp_root/recovered-diagnostic"
mkdir -p "$result_dir" "$proxy_root/original" "$proxy_root/recovered"
mkdir -p "$metadata_type_root/original" "$metadata_type_root/recovered"
mkdir -p "$semantic_proxy_root/original" "$semantic_proxy_root/recovered"
for java_tmp_dir in "$original_exact_java_tmp" "$source_exact_java_tmp" \
    "$original_diagnostic_java_tmp" "$source_diagnostic_java_tmp"; do
  [[ ! -e "$java_tmp_dir" && ! -L "$java_tmp_dir" ]] || {
    echo "refusing existing per-probe Java temp directory: $java_tmp_dir" >&2
    exit 2
  }
  mkdir -p "$java_tmp_dir"
done

require_empty_java_tmp() {
  local java_tmp_dir=$1
  local label=$2
  [[ -z $(find "$java_tmp_dir" -mindepth 1 -print -quit) ]] || {
    echo "$label left content in its isolated Java temp directory: $java_tmp_dir" >&2
    exit 2
  }
}

result_written=0
current_step=setup
record_unhandled_failure() {
  local status=$?
  if [[ "$status" -ne 0 && "$result_written" -eq 0 && \
        ! -e "$result_file" && ! -L "$result_file" ]]; then
    printf 'DATOMIC_SURFACE_RESULT\tFAIL\t%s\texact\tharness-%s\tunavailable\tunavailable\n' \
      "$namespace_name" "$current_step" >"$result_file"
  fi
}
trap record_unhandled_failure EXIT

for output_file in "$original_out" "$source_out" "$original_raw" \
    "$source_raw" "$original_err" "$source_err" "$result_file" \
    "$original_proxy" "$source_proxy" "$original_metadata_types" \
    "$source_metadata_types" "$original_semantic_proxies" \
    "$source_semantic_proxies"; do
  [[ ! -e "$output_file" && ! -L "$output_file" ]] || {
    echo "refusing to overwrite namespace evidence: $output_file" >&2
    exit 2
  }
done

current_step=original-exact-jvm
DATOMIC_VAR_METADATA_VIEW=exact DATOMIC_VAR_METADATA_EMIT_TYPES=1 \
  /usr/bin/timeout --signal=TERM --kill-after=15s \
  "${namespace_timeout_seconds}s" \
  "$DATOMIC_REV_JAVA_BIN" -XX:+PerfDisableSharedMem -Xmx2g \
  -Djava.io.tmpdir="$original_exact_java_tmp" \
  -Ddatomic.surface.semantic-proxy-origin=classpath-bytecode \
  '-Dclojure.compiler.elide-meta=[:doc :file :line]' \
  -cp "$DATOMIC_REV_ORIGINAL_CLASSPATH" \
  clojure.main "$structural_emitter" "$peer_emitter" "$namespace_name" \
  "$original_out" \
  >"$original_raw" 2>"$original_err"
require_empty_java_tmp "$original_exact_java_tmp" original-exact

current_step=recovered-exact-jvm
DATOMIC_VAR_METADATA_VIEW=exact DATOMIC_VAR_METADATA_EMIT_TYPES=1 \
  /usr/bin/timeout --signal=TERM --kill-after=15s \
  "${namespace_timeout_seconds}s" \
  "$DATOMIC_REV_JAVA_BIN" -XX:+PerfDisableSharedMem -Xmx2g \
  -Djava.io.tmpdir="$source_exact_java_tmp" \
  -Ddatomic.surface.semantic-proxy-origin=source-generated \
  '-Dclojure.compiler.elide-meta=[:doc :file :line]' \
  -cp "$DATOMIC_REV_SOURCE_CLASSPATH" \
  clojure.main "$structural_emitter" "$peer_emitter" "$namespace_name" \
  "$source_out" \
  >"$source_raw" 2>"$source_err"
require_empty_java_tmp "$source_exact_java_tmp" recovered-exact

current_step=dedicated-payload
for dedicated_surface in "$original_out" "$source_out"; do
  [[ -s "$dedicated_surface" && -f "$dedicated_surface" && \
     ! -L "$dedicated_surface" ]] || {
    echo "dedicated surface payload is missing or symbolic: $dedicated_surface" >&2
    exit 2
  }
done

current_step=proxy-exclusion-ledger
grep '^VAR_SURFACE_PROXY_EXCLUSIONS ' "$original_err" >"$original_proxy" || {
  echo "missing original proxy-exclusion ledger for $namespace_name" >&2
  exit 2
}
grep '^VAR_SURFACE_PROXY_EXCLUSIONS ' "$source_err" >"$source_proxy" || {
  echo "missing recovered proxy-exclusion ledger for $namespace_name" >&2
  exit 2
}
[[ $(wc -l <"$original_proxy") -eq 1 && \
   $(wc -l <"$source_proxy") -eq 1 ]] || {
  echo "proxy-exclusion ledger is not exactly one tagged record for $namespace_name" >&2
  exit 2
}

current_step=semantic-proxy-ledger
grep '^VAR_METADATA_SEMANTIC_PROXIES ' "$original_err" \
  >"$original_semantic_proxies" || {
  echo "missing original semantic-proxy ledger for $namespace_name" >&2
  exit 2
}
grep '^VAR_METADATA_SEMANTIC_PROXIES ' "$source_err" \
  >"$source_semantic_proxies" || {
  echo "missing recovered semantic-proxy ledger for $namespace_name" >&2
  exit 2
}
[[ $(wc -l <"$original_semantic_proxies") -eq 1 && \
   $(wc -l <"$source_semantic_proxies") -eq 1 ]] || {
  echo "semantic-proxy ledger is not exactly one tagged record for $namespace_name" >&2
  exit 2
}

current_step=metadata-type-ledger
grep '^VAR_METADATA_VALUE_TYPES ' "$original_err" \
  >"$original_metadata_types" || {
  echo "missing original metadata-type ledger for $namespace_name" >&2
  exit 2
}
grep '^VAR_METADATA_VALUE_TYPES ' "$source_err" \
  >"$source_metadata_types" || {
  echo "missing recovered metadata-type ledger for $namespace_name" >&2
  exit 2
}
[[ $(wc -l <"$original_metadata_types") -eq 1 && \
   $(wc -l <"$source_metadata_types") -eq 1 ]] || {
  echo "metadata-type ledger is not exactly one tagged record for $namespace_name" >&2
  exit 2
}
# Metadata type ledgers include the runtime Class name of the one approved
# semantic metadata proxy.  That generated fn class is expected to differ
# between AOT and source lanes.  The global comparator first proves the exact
# classpath-bytecode/source-generated lane origins, projects only the Class
# named by that semantic contract, then requires every remaining type to
# match.  A byte comparison here would reject the honest AOT/source relation
# before the constrained projection can run.

current_step=exact-surface-relation
if ! cmp -s "$original_out" "$source_out"; then
  diagnostic_original="$DATOMIC_REV_SURFACE_ROOT/original/$safe_name.diagnostic.edn"
  diagnostic_source="$DATOMIC_REV_SURFACE_ROOT/recovered/$safe_name.diagnostic.edn"
  diagnostic_original_raw="$DATOMIC_REV_SURFACE_ROOT/logs/$safe_name.original.diagnostic.out"
  diagnostic_source_raw="$DATOMIC_REV_SURFACE_ROOT/logs/$safe_name.recovered.diagnostic.out"
  diagnostic_original_err="$DATOMIC_REV_SURFACE_ROOT/logs/$safe_name.original.diagnostic.err"
  diagnostic_source_err="$DATOMIC_REV_SURFACE_ROOT/logs/$safe_name.recovered.diagnostic.err"
  current_step=original-diagnostic-jvm
  DATOMIC_VAR_METADATA_VIEW=diagnostic \
    /usr/bin/timeout --signal=TERM --kill-after=15s \
    "${namespace_timeout_seconds}s" \
    "$DATOMIC_REV_JAVA_BIN" -XX:+PerfDisableSharedMem -Xmx2g \
    -Djava.io.tmpdir="$original_diagnostic_java_tmp" \
    -Ddatomic.surface.semantic-proxy-origin=classpath-bytecode \
    '-Dclojure.compiler.elide-meta=[:doc :file :line]' \
    -cp "$DATOMIC_REV_ORIGINAL_CLASSPATH" \
    clojure.main "$structural_emitter" "$peer_emitter" "$namespace_name" \
    "$diagnostic_original" \
    >"$diagnostic_original_raw" 2>"$diagnostic_original_err"
  require_empty_java_tmp "$original_diagnostic_java_tmp" original-diagnostic
  current_step=recovered-diagnostic-jvm
  DATOMIC_VAR_METADATA_VIEW=diagnostic \
    /usr/bin/timeout --signal=TERM --kill-after=15s \
    "${namespace_timeout_seconds}s" \
    "$DATOMIC_REV_JAVA_BIN" -XX:+PerfDisableSharedMem -Xmx2g \
    -Djava.io.tmpdir="$source_diagnostic_java_tmp" \
    -Ddatomic.surface.semantic-proxy-origin=source-generated \
    '-Dclojure.compiler.elide-meta=[:doc :file :line]' \
    -cp "$DATOMIC_REV_SOURCE_CLASSPATH" \
    clojure.main "$structural_emitter" "$peer_emitter" "$namespace_name" \
    "$diagnostic_source" \
    >"$diagnostic_source_raw" 2>"$diagnostic_source_err"
  require_empty_java_tmp "$source_diagnostic_java_tmp" recovered-diagnostic
  mismatch_kind=runtime-structure-or-root
  cmp -s "$diagnostic_original" "$diagnostic_source" && \
    mismatch_kind=exact-metadata
  printf 'DATOMIC_SURFACE_RESULT\tFAIL\t%s\texact\t%s\t%s\t%s\n' \
    "$namespace_name" "$mismatch_kind" \
    "$(sha256sum "$original_out" | awk '{print $1}')" \
    "$(sha256sum "$source_out" | awk '{print $1}')" >"$result_file"
  result_written=1
  echo "FAIL $namespace_name exact surface mismatch ($mismatch_kind)"
  diff -u "$original_out" "$source_out" \
    >"$DATOMIC_REV_SURFACE_ROOT/logs/$safe_name.diff" || true
  exit 1
fi

printf 'DATOMIC_SURFACE_RESULT\tPASS\t%s\texact\tnone\t%s\t%s\n' \
  "$namespace_name" \
  "$(sha256sum "$original_out" | awk '{print $1}')" \
  "$(sha256sum "$source_out" | awk '{print $1}')" >"$result_file"
result_written=1
current_step=complete
echo "PASS $namespace_name"
