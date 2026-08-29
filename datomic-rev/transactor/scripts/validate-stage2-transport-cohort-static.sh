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
evidence_root=${2:-/tmp/datomic-stage2-transport-cohort-static-v1}
java_root=${DATOMIC_STAGE2_COHORT_JAVA_ROOT:-/tmp/amazon-corretto-11.0.22.7.1}
javap_bin="$java_root/bin/javap"
peer_jar="$datomic_home/peer-1.0.7277.jar"
transactor_jar="$datomic_home/datomic-transactor-pro-1.0.7277.jar"

fail() {
  echo "Stage 2 transport-cohort static gate: $*" >&2
  exit 1
}

for command_name in awk cmp comm cut find paste realpath sed sha256sum \
                    sort unzip wc; do
  command -v "$command_name" >/dev/null || fail "missing command: $command_name"
done
[[ -x "$javap_bin" ]] || fail "pinned javap is missing: $javap_bin"
[[ ! -L "$evidence_root" ]] || fail "evidence root may not be a symlink"
project_abs=$(realpath "$project_dir")
evidence_abs=$(realpath -m "$evidence_root")
case "$evidence_abs" in
  "$project_abs"|"$project_abs"/*)
    fail "evidence must remain outside the repository: $evidence_abs"
    ;;
esac
if [[ -e "$evidence_abs" ]] &&
   [[ -n $(find "$evidence_abs" -mindepth 1 -print -quit 2>/dev/null) ]]; then
  fail "refusing to overwrite non-empty evidence root: $evidence_abs"
fi
evidence_root=$evidence_abs
mkdir -p "$evidence_root/inputs" "$evidence_root/namespaces"

verify_hash() {
  local path=$1
  local expected=$2
  local label=$3
  [[ -f "$path" && ! -L "$path" ]] || fail "$label is missing or symbolic: $path"
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
verify_hash "$javap_bin" \
  21b9d9be1c5aed0d6d072f322c9cb2dc0f720a13c92eb9b7d67d7628da140e30 \
  "Corretto javap"

sha256sum "$peer_jar" "$transactor_jar" "$javap_bin" \
  >"$evidence_root/inputs/artifacts-and-tool.sha256"
printf 'lane\tnamespace\tpath\tsha256\n' >"$evidence_root/inputs/sources.tsv"

namespaces=(queue builtins reconnector2 connector artemis-client)
for namespace in "${namespaces[@]}"; do
  namespace_path=${namespace//-/_}
  for lane in peer transactor; do
    if [[ "$lane" == peer ]]; then
      source="$project_dir/src-clj/datomic/$namespace_path.clj"
    else
      source="$project_dir/transactor/src-clj/datomic/$namespace_path.clj"
    fi
    [[ -f "$source" && ! -L "$source" ]] || fail "source is missing or symbolic: $source"
    printf '%s\t%s\t%s\t%s\n' "$lane" "$namespace" "$source" \
      "$(sha256sum "$source" | awk '{print $1}')" \
      >>"$evidence_root/inputs/sources.tsv"
  done
done

write_role_map() {
  local artifact=$1
  local namespace_path=$2
  local output=$3
  unzip -Z1 "$artifact" |
    awk -v prefix="datomic/${namespace_path}" '
      index($0, prefix) == 1 && /\.class$/ {
        suffix=substr($0, length(prefix) + 1)
        if (suffix == "__init.class" ||
            suffix ~ /^\$.*\.class$/ ||
            suffix ~ /^\/.*\.class$/) print
      }
    ' |
    sort |
    awk '{
      entry=$0
      role=$0
      gsub(/__[0-9]+/, "__ID", role)
      seen[role]++
      print role "#" seen[role] "\t" entry
    }' >"$output"
}

declare -A expected_classes=(
  [queue]=56 [builtins]=11 [reconnector2]=17 [connector]=85 [artemis-client]=92
)
declare -A expected_methods=(
  [queue]=197 [builtins]=40 [reconnector2]=61 [connector]=300 [artemis-client]=347
)
declare -A expected_peer_fields=(
  [queue]=170 [builtins]=88 [reconnector2]=108 [connector]=548 [artemis-client]=371
)
declare -A expected_transactor_fields=(
  [queue]=173 [builtins]=94 [reconnector2]=111 [connector]=555 [artemis-client]=392
)

printf 'namespace\tclasses\tmethods\tpeer_fields\ttransactor_fields\trole_relation\tmethod_relation\tfield_delta\tbytecode_relation\n' \
  >"$evidence_root/summary.tsv"

for namespace in "${namespaces[@]}"; do
  namespace_path=${namespace//-/_}
  namespace_root="$evidence_root/namespaces/$namespace_path"
  mkdir -p "$namespace_root/peer" "$namespace_root/transactor"
  write_role_map "$peer_jar" "$namespace_path" "$namespace_root/peer/roles.tsv"
  write_role_map "$transactor_jar" "$namespace_path" "$namespace_root/transactor/roles.tsv"
  cmp -s <(cut -f1 "$namespace_root/peer/roles.tsv") \
    <(cut -f1 "$namespace_root/transactor/roles.tsv") ||
    fail "$namespace normalized class-role inventory differs"

  mapfile -t peer_classes < <(cut -f2 "$namespace_root/peer/roles.tsv" |
    sed -E 's/\.class$//; s#/#.#g')
  mapfile -t transactor_classes < <(cut -f2 "$namespace_root/transactor/roles.tsv" |
    sed -E 's/\.class$//; s#/#.#g')
  [[ ${#peer_classes[@]} == "${expected_classes[$namespace]}" ]] ||
    fail "$namespace Peer class count changed"
  [[ ${#transactor_classes[@]} == "${expected_classes[$namespace]}" ]] ||
    fail "$namespace Transactor class count changed"

  "$javap_bin" -classpath "$peer_jar" -c -p -s "${peer_classes[@]}" \
    >"$namespace_root/peer/all.javap"
  "$javap_bin" -classpath "$transactor_jar" -c -p -s "${transactor_classes[@]}" \
    >"$namespace_root/transactor/all.javap"

  for lane in peer transactor; do
    sed -E \
      -e 's/__[0-9]+/__ID/g' \
      -e 's/#[0-9]+/#CP/g' \
      -e 's/[[:space:]]+/ /g' \
      -e 's/^ //; s/ $//' \
      "$namespace_root/$lane/all.javap" >"$namespace_root/$lane/all.normalized"
    awk '
      index($0, "descriptor: ") == 1 {
        print previous "\t" substr($0, 13)
      }
      {previous=$0}
    ' "$namespace_root/$lane/all.normalized" | sort \
      >"$namespace_root/$lane/surface.tsv"
    awk -F '\t' '$2 ~ /^\(/' "$namespace_root/$lane/surface.tsv" \
      >"$namespace_root/$lane/methods.tsv"
    awk -F '\t' '$2 !~ /^\(/' "$namespace_root/$lane/surface.tsv" \
      >"$namespace_root/$lane/fields.tsv"
  done

  cmp -s "$namespace_root/peer/methods.tsv" \
    "$namespace_root/transactor/methods.tsv" ||
    fail "$namespace method ABI differs"
  methods=$(wc -l <"$namespace_root/peer/methods.tsv")
  peer_fields=$(wc -l <"$namespace_root/peer/fields.tsv")
  transactor_fields=$(wc -l <"$namespace_root/transactor/fields.tsv")
  [[ "$methods" == "${expected_methods[$namespace]}" ]] ||
    fail "$namespace method count changed"
  [[ "$peer_fields" == "${expected_peer_fields[$namespace]}" ]] ||
    fail "$namespace Peer field count changed"
  [[ "$transactor_fields" == "${expected_transactor_fields[$namespace]}" ]] ||
    fail "$namespace Transactor field count changed"
  comm -23 "$namespace_root/peer/fields.tsv" \
    "$namespace_root/transactor/fields.tsv" \
    >"$namespace_root/peer-only-fields.tsv"
  comm -13 "$namespace_root/peer/fields.tsv" \
    "$namespace_root/transactor/fields.tsv" \
    >"$namespace_root/transactor-only-fields.tsv"
  [[ ! -s "$namespace_root/peer-only-fields.tsv" ]] ||
    fail "$namespace has unexpected Peer-only fields"
  expected_delta=$((expected_transactor_fields[$namespace] - expected_peer_fields[$namespace]))
  [[ $(wc -l <"$namespace_root/transactor-only-fields.tsv") == "$expected_delta" ]] ||
    fail "$namespace Transactor-only field count changed"
  awk -F '\t' '
    $1 != "public static final clojure.lang.Var const__ID;" ||
    $2 != "Lclojure/lang/Var;" {bad=1}
    END {exit bad}
  ' "$namespace_root/transactor-only-fields.tsv" ||
    fail "$namespace field delta is not solely compiler-generated static Vars"

  if cmp -s "$namespace_root/peer/all.normalized" \
    "$namespace_root/transactor/all.normalized"; then
    bytecode_relation=exact
  else
    bytecode_relation=classified-residual-required
  fi
  printf '%s\t%s\t%s\t%s\t%s\texact\texact\ttransactor-only-static-var-constants:%s\t%s\n' \
    "$namespace" "${expected_classes[$namespace]}" "$methods" "$peer_fields" \
    "$transactor_fields" "$expected_delta" "$bytecode_relation" \
    >>"$evidence_root/summary.tsv"
done

sha256sum "$script_dir/validate-stage2-transport-cohort-static.sh" \
  >"$evidence_root/inputs/runner.sha256"
printf 'Stage 2 transport-cohort static gate passed: %s\n' "$evidence_root"
