#!/usr/bin/bash -p

set -euo pipefail
umask 077
unset BASH_ENV ENV CDPATH GLOBIGNORE JAVA_TOOL_OPTIONS JDK_JAVA_OPTIONS \
  _JAVA_OPTIONS JDK_JAVAC_OPTIONS CLASSPATH JAVA_HOME LD_PRELOAD \
  LD_LIBRARY_PATH
IFS=$' \t\n'
export LC_ALL=C TZ=UTC

script_dir=$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd)
project_dir=$(cd -- "$script_dir/../.." && pwd)
datomic_home=${1:-"$project_dir/../../datomic/datomic-pro-1.0.7277"}
classifier_root=${2:-/tmp/datomic-stage2-global-overlap-classifier-v8}
evidence_root=${3:-/tmp/datomic-stage2-dead-verifier-slots-global-v2-corrected-v8}
java_root=${DATOMIC_STAGE2_DEAD_SLOT_JAVA_ROOT:-/tmp/amazon-corretto-11.0.22.7.1}
javac_bin="$java_root/bin/javac"
java_bin="$java_root/bin/java"
peer_jar="$datomic_home/peer-1.0.7277.jar"
transactor_jar="$datomic_home/datomic-transactor-pro-1.0.7277.jar"
tool_source="$project_dir/transactor/tools/ProveDeadVerifierSlots.java"

fail() {
  echo "Stage 2 global dead-verifier-slot proof: $*" >&2
  exit 1
}

for command_name in awk find realpath sha256sum sort xargs; do
  command -v "$command_name" >/dev/null || fail "missing command: $command_name"
done
[[ -x "$javac_bin" && -x "$java_bin" ]] || fail "pinned Corretto compiler/runtime missing: $java_root"
[[ -f "$tool_source" && ! -L "$tool_source" ]] || fail "proof tool missing or symbolic: $tool_source"
for input in classifier.tsv normalized-method-body-differences.tsv evidence.sha256 \
             work/peer-classes.tsv work/transactor-classes.tsv; do
  [[ -f "$classifier_root/$input" && ! -L "$classifier_root/$input" ]] ||
    fail "global classifier input missing or symbolic: $classifier_root/$input"
done

verify_hash() {
  local path=$1
  local expected=$2
  local label=$3
  [[ -f "$path" && ! -L "$path" ]] || fail "$label missing or symbolic: $path"
  local actual
  actual=$(sha256sum "$path" | awk '{print $1}')
  [[ "$actual" == "$expected" ]] ||
    fail "$label hash mismatch: expected $expected, found $actual"
}

verify_hash "$peer_jar" \
  cb55c9d01e155f9965e3332cd8ef70be42cdefacb50c072076e9ecf22b9a23ba \
  "original Peer"
verify_hash "$transactor_jar" \
  d90819b57e2138085ddc26c93314f953c2cd776d1d1aa8b43a6bec1e475d3692 \
  "original Transactor"
verify_hash "$java_bin" \
  3e98d0f812482808f701ffa0d4e94b8dd8e31a2c6a07fb52662ceef8e75d66f1 \
  "Corretto java"

(cd "$classifier_root" && sha256sum -c evidence.sha256 >/dev/null) ||
  fail "global classifier evidence manifest does not verify"

project_abs=$(realpath "$project_dir")
evidence_abs=$(realpath -m "$evidence_root")
case "$evidence_abs" in
  "$project_abs"|"$project_abs"/*)
    fail "evidence must remain outside the repository: $evidence_abs"
    ;;
esac
[[ ! -L "$evidence_abs" ]] || fail "evidence root may not be a symlink"
if [[ -e "$evidence_abs" ]] &&
   [[ -n $(find "$evidence_abs" -mindepth 1 -print -quit 2>/dev/null) ]]; then
  fail "refusing to overwrite non-empty evidence root: $evidence_abs"
fi
evidence_root=$evidence_abs
mkdir -p "$evidence_root/inputs" "$evidence_root/work/classes" "$evidence_root/results"

sha256sum "$peer_jar" "$transactor_jar" "$java_bin" "$javac_bin" \
  "$tool_source" "$script_dir/prove-stage2-dead-verifier-slots-global.sh" \
  "$classifier_root/classifier.tsv" \
  "$classifier_root/normalized-method-body-differences.tsv" \
  "$classifier_root/work/peer-classes.tsv" \
  "$classifier_root/work/transactor-classes.tsv" \
  "$classifier_root/evidence.sha256" \
  >"$evidence_root/inputs/artifacts-tools-and-classifier.sha256"

"$javac_bin" \
  --add-exports java.base/jdk.internal.org.objectweb.asm=ALL-UNNAMED \
  --add-exports java.base/jdk.internal.org.objectweb.asm.tree=ALL-UNNAMED \
  --add-exports java.base/jdk.internal.org.objectweb.asm.util=ALL-UNNAMED \
  -d "$evidence_root/work/classes" "$tool_source"

"$java_bin" \
  --add-exports java.base/jdk.internal.org.objectweb.asm=ALL-UNNAMED \
  --add-exports java.base/jdk.internal.org.objectweb.asm.tree=ALL-UNNAMED \
  --add-exports java.base/jdk.internal.org.objectweb.asm.util=ALL-UNNAMED \
  -cp "$evidence_root/work/classes" ProveDeadVerifierSlots \
  "$peer_jar" "$transactor_jar" \
  "$classifier_root/work/peer-classes.tsv" \
  "$classifier_root/work/transactor-classes.tsv" \
  "$classifier_root/classifier.tsv" \
  "$classifier_root/normalized-method-body-differences.tsv" \
  "$evidence_root/results"

awk -F '\t' '
  NR == 1 {
    if (NF != 20 || $1 != "namespace" || $18 != "dead_slot_family_relation") exit 1
    next
  }
  NF != 20 || seen[$1]++ {exit 1}
  END {if (NR - 1 != 117) exit 1}
' "$evidence_root/results/namespace-summary.tsv" || fail "invalid 117-row namespace proof summary"

awk -F '\t' '
  NR == 1 {
    if (NF != 4 || $1 != "control" || $4 != "result") exit 1
    next
  }
  NF != 4 || $4 != "PASS" {exit 1}
  $1 == "unreachable-pop-versus-athrow-positive" && $3 == "DEAD_ONLY" {positive=1}
  $1 == "reachable-predecessor-negative" && $3 == "DISQUALIFIED" {predecessor=1}
  $1 == "exception-handler-root-negative" && $3 == "DISQUALIFIED" {handler=1}
  END {if (NR != 4 || !positive || !predecessor || !handler) exit 1}
' "$evidence_root/results/negative-controls.tsv" || fail "CFG negative controls did not pass"

find "$evidence_root" -type f ! -name evidence.sha256 -print0 |
  sort -z | xargs -0 sha256sum >"$evidence_root/evidence.sha256"

printf 'Stage 2 global dead-verifier-slot proof passed: %s\n' "$evidence_root"
