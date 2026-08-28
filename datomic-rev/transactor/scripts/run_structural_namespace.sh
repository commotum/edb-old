#!/usr/bin/env bash
set -u -o pipefail

mode=$1
namespace_name=$2

: "${STRUCTURAL_JAVA:?missing STRUCTURAL_JAVA}"
: "${STRUCTURAL_TIMEOUT_SECONDS:?missing STRUCTURAL_TIMEOUT_SECONDS}"
: "${STRUCTURAL_CANDIDATE_CLASSPATH:?missing STRUCTURAL_CANDIDATE_CLASSPATH}"
: "${STRUCTURAL_ORACLE_CLASSPATH:?missing STRUCTURAL_ORACLE_CLASSPATH}"
: "${STRUCTURAL_LOG_ROOT:?missing STRUCTURAL_LOG_ROOT}"
: "${STRUCTURAL_RESULT_ROOT:?missing STRUCTURAL_RESULT_ROOT}"
: "${STRUCTURAL_SCRIPT_DIR:?missing STRUCTURAL_SCRIPT_DIR}"
: "${STRUCTURAL_PEER_EMITTER:?missing STRUCTURAL_PEER_EMITTER}"
: "${STRUCTURAL_PROCESS_CWD:?missing STRUCTURAL_PROCESS_CWD}"
: "${STRUCTURAL_BATCH_RECORD_ONLY:?missing STRUCTURAL_BATCH_RECORD_ONLY}"
[[ "$STRUCTURAL_BATCH_RECORD_ONLY" == 1 ]] || {
  echo "STRUCTURAL_BATCH_RECORD_ONLY must be 1 for this internal worker" >&2
  exit 2
}

safe_name=${namespace_name//[^A-Za-z0-9_.-]/_}
mode_log_root="$STRUCTURAL_LOG_ROOT/$mode"
mode_result_root="$STRUCTURAL_RESULT_ROOT/$mode"
mkdir -p "$mode_log_root" "$mode_result_root" || exit 70
stdout_file="$mode_log_root/$safe_name.out"
stderr_file="$mode_log_root/$safe_name.err"
surface_file="$mode_log_root/$safe_name.surface.edn"
result_file="$mode_result_root/$safe_name.tsv"
process_cwd="$STRUCTURAL_PROCESS_CWD/$mode/$safe_name"
mkdir -p "$process_cwd" || exit 70
[[ -z $(find "$process_cwd" -mindepth 1 -print -quit) ]] || {
  echo "isolated process working directory is not empty: $process_cwd" >&2
  exit 70
}
java_tmpdir="$process_cwd/java-tmp"
mkdir "$java_tmpdir" || exit 70

case "$mode" in
  require-candidate)
    classpath=$STRUCTURAL_CANDIDATE_CLASSPATH
    script="$STRUCTURAL_SCRIPT_DIR/require_structural_namespace.clj"
    arguments=("$namespace_name")
    ;;
  require-topological)
    classpath=$STRUCTURAL_CANDIDATE_CLASSPATH
    script="$STRUCTURAL_SCRIPT_DIR/require_structural_namespace.clj"
    case "$namespace_name" in
      datomic.transactor-ext)
        arguments=("$namespace_name" datomic.transactor)
        ;;
      *)
        echo "no pinned topological preload for: $namespace_name" >&2
        exit 2
        ;;
    esac
    ;;
  surface-candidate)
    classpath=$STRUCTURAL_CANDIDATE_CLASSPATH
    script="$STRUCTURAL_SCRIPT_DIR/emit_structural_surface.clj"
    arguments=("$STRUCTURAL_PEER_EMITTER" "$namespace_name" "$surface_file")
    semantic_proxy_origin=source-generated
    ;;
  surface-oracle)
    classpath=$STRUCTURAL_ORACLE_CLASSPATH
    script="$STRUCTURAL_SCRIPT_DIR/emit_structural_surface.clj"
    arguments=("$STRUCTURAL_PEER_EMITTER" "$namespace_name" "$surface_file")
    semantic_proxy_origin=classpath-bytecode
    ;;
  *)
    echo "unknown structural namespace mode: $mode" >&2
    exit 2
    ;;
esac

semantic_proxy_java_option=()
if [[ -n ${semantic_proxy_origin-} ]]; then
  semantic_proxy_java_option=(
    "-Ddatomic.surface.semantic-proxy-origin=$semantic_proxy_origin"
  )
fi

if [[ "$namespace_name" == datomic.transactor-ext ]] && \
   [[ "$mode" == surface-candidate || "$mode" == surface-oracle ]]; then
  arguments+=(datomic.transactor)
fi

start_millis=$(date +%s%3N)
cd "$process_cwd"
timeout --signal=TERM --kill-after=5s "${STRUCTURAL_TIMEOUT_SECONDS}s" \
  "$STRUCTURAL_JAVA" \
  -XX:+PerfDisableSharedMem \
  -Xms64m -Xmx2g \
  -Djava.awt.headless=true \
  -Duser.timezone=UTC \
  -Dcom.amazonaws.sdk.disableEc2Metadata=true \
  -Djava.io.tmpdir="$java_tmpdir" \
  -Dclojure.compiler.elide-meta='[:doc :file :line]' \
  "${semantic_proxy_java_option[@]}" \
  -cp "$classpath" \
  clojure.main "$script" "${arguments[@]}" \
  >"$stdout_file" 2>"$stderr_file"
exit_code=$?
end_millis=$(date +%s%3N)
duration_millis=$((end_millis - start_millis))

status=FAIL
if [[ $exit_code -eq 124 || $exit_code -eq 137 ]]; then
  status=TIMEOUT
elif [[ $exit_code -eq 0 ]]; then
  case "$mode" in
    require-candidate|require-topological)
      if [[ $(tail -n 1 "$stdout_file") == "STRUCTURAL_REQUIRE_PASS $namespace_name" ]]; then
        status=PASS
      else
        status=INVALID_OUTPUT
      fi
      ;;
    surface-candidate|surface-oracle)
      nonempty_lines=0
      [[ -f "$surface_file" && ! -L "$surface_file" ]] && \
        nonempty_lines=$(awk 'NF {count++} END {print count + 0}' "$surface_file")
      if [[ $nonempty_lines -eq 1 ]] && \
         head -n 1 "$surface_file" | grep -q '^{:vars '; then
        status=PASS
      else
        status=INVALID_OUTPUT
      fi
      ;;
  esac
fi

if [[ -d "$java_tmpdir" ]] && \
   [[ -z $(find "$java_tmpdir" -mindepth 1 -print -quit) ]]; then
  rmdir "$java_tmpdir" || status=PROCESS_CWD_DIRTY
fi
if [[ -n $(find "$process_cwd" -mindepth 1 -print -quit) ]]; then
  status=PROCESS_CWD_DIRTY
fi

stdout_sha=$(sha256sum "$stdout_file" | awk '{print $1}')
stderr_sha=$(sha256sum "$stderr_file" | awk '{print $1}')
surface_sha=-
[[ -f "$surface_file" ]] && surface_sha=$(sha256sum "$surface_file" | awk '{print $1}')
printf '%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\n' \
  "$mode" "$namespace_name" "$status" "$exit_code" "$duration_millis" \
  "$stdout_sha" "$stderr_sha" "$surface_sha" >"$result_file" || exit 70

# INTERNAL BATCH CONTRACT: zero means the worker successfully recorded the
# namespace process outcome above.  It does not mean that the namespace passed.
# The aggregate evidence table is the sole gate, allowing every bounded probe
# to run even when an earlier namespace records FAIL or TIMEOUT.
exit 0
