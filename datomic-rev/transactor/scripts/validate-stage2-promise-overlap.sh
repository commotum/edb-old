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
evidence_root=${2:-/tmp/datomic-stage2-promise-overlap-v1}
java_root=${DATOMIC_STAGE2_PROMISE_JAVA_ROOT:-/tmp/amazon-corretto-11.0.22.7.1}
java_bin="$java_root/bin/java"
javac_bin="$java_root/bin/javac"
javap_bin="$java_root/bin/javap"
probe="$script_dir/stage2/promise_overlap_probe.clj"

fail() {
  echo "Stage 2 promise-overlap gate: $*" >&2
  exit 1
}

for command_name in awk bash cmp cut diff find grep install paste realpath \
                    sed sha256sum sort timeout unzip wc xargs; do
  command -v "$command_name" >/dev/null || fail "missing command: $command_name"
done
[[ -x "$java_bin" && -x "$javac_bin" && -x "$javap_bin" ]] ||
  fail "pinned Corretto Java/Javac/Javap are missing: $java_root"
[[ -f "$probe" && ! -L "$probe" ]] || fail "probe is missing or symbolic"
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

peer_jar="$datomic_home/peer-1.0.7277.jar"
transactor_jar="$datomic_home/datomic-transactor-pro-1.0.7277.jar"
oracle_core2_jar="$datomic_home/lib/core2-1.0.140.jar"
oracle_nano_jar="$datomic_home/lib/nano-impl-0.1.325.jar"
dependency_manifest="$project_dir/transactor/reports/stage-1-candidate-dependencies.tsv"
dependency_elements="$project_dir/transactor/reports/stage-1-candidate-dependency-elements.tsv"
dependency_evidence="$project_dir/transactor/reports/stage-1-candidate-dependency-evidence.sha256"
peer_compile="$project_dir/scripts/compile-handwritten-java.sh"
peer_compile_tool="$project_dir/scripts/CompileSources.java"
peer_source_list="$project_dir/reports/handwritten-java-sources.txt"
transactor_java_validate="$project_dir/transactor/scripts/validate-java.sh"
resource_stage="$project_dir/transactor/scripts/stage-structural-resources.sh"
resource_templates="$project_dir/transactor/candidate-resources"
peer_promise_source="$project_dir/src-clj/datomic/promise.clj"
transactor_promise_source="$project_dir/transactor/src-clj/datomic/promise.clj"

expected_peer_sha=cb55c9d01e155f9965e3332cd8ef70be42cdefacb50c072076e9ecf22b9a23ba
expected_transactor_sha=d90819b57e2138085ddc26c93314f953c2cd776d1d1aa8b43a6bec1e475d3692
expected_oracle_core2_sha=81fdf81586c7be1a4b61655b568348d12db7892cb4562d0db7529bbc7af8a94b
expected_oracle_nano_sha=fbde8184da356bd3a9995a9f05f01493efe4baa87c4d48377cda3e53331807cd
expected_java_sha=3e98d0f812482808f701ffa0d4e94b8dd8e31a2c6a07fb52662ceef8e75d66f1
expected_javac_sha=f315d031604835a3017268cca49631f4aa441d084d3cc29932d296ecd013106c
expected_javap_sha=21b9d9be1c5aed0d6d072f322c9cb2dc0f720a13c92eb9b7d67d7628da140e30
expected_dependency_manifest_sha=2a60a620248948eb8e8f338e6d9a3e7100c03ce0837a020009686cb158913df5
expected_dependency_elements_sha=9abfd3705eaa7e5898643901bfaddd442a2805e54a969503eb7269523e442f62
expected_dependency_evidence_sha=5b714d5eed1e084a592202095eca8f9e5b8b00c3f7523d7e60edd942ee166684
expected_peer_compile_sha=a6b030702c90842d9a52c48738206d3c711e5d78875e3094e143577766c19232
expected_peer_compile_tool_sha=64c7cb1599c9255d6ef0b396e99ccff9e5448f7ba0f77a8fc34a2e294873dcc8
expected_peer_source_list_sha=7b2b839a783b3e54890af3fc1e0013557c06f2272a127e9ee50951e7d642241d
expected_transactor_java_validate_sha=65f96b328d033f2502ee3ed4b66a1e38da4f6d21cbb30600bf7465058a879c04
expected_resource_stage_sha=f7ba7f95752cfbed5f0cb5a1783726bded38d0dd756f5be4d71bfddbc5841acf
expected_peer_promise_source_sha=9125c93ab1c5871a5012b12fa2bf51a1896f782eaa243b6a920890d3183d2c01
expected_transactor_promise_source_sha=13e82d38b296a2edf57bf1e14f5b7806ef3ef1241caff8e5fce88ae32d5fae5c
expected_method_abi_sha=b96840ff298052e8edd02f637c7dbd37fc3b0340817a08740e934445052759f0
expected_field_abi_sha=ecc7333f0630894ab3d8dcbeea4934422dfba4c5824e32ed8ecbf19e4ed8f162

verify_file() {
  local path=$1
  local expected=$2
  local label=$3
  [[ -f "$path" && ! -L "$path" ]] ||
    fail "$label is missing or symbolic: $path"
  local actual
  actual=$(sha256sum "$path" | awk '{print $1}')
  [[ "$actual" == "$expected" ]] ||
    fail "$label hash mismatch: expected $expected, found $actual"
}

verify_file "$peer_jar" "$expected_peer_sha" "original Peer"
verify_file "$transactor_jar" "$expected_transactor_sha" "original Transactor"
verify_file "$oracle_core2_jar" "$expected_oracle_core2_sha" \
  "oracle-only core2"
verify_file "$oracle_nano_jar" "$expected_oracle_nano_sha" \
  "oracle-only Nano"
verify_file "$java_bin" "$expected_java_sha" "Corretto java"
verify_file "$javac_bin" "$expected_javac_sha" "Corretto javac"
verify_file "$javap_bin" "$expected_javap_sha" "Corretto javap"
verify_file "$dependency_manifest" "$expected_dependency_manifest_sha" \
  "candidate dependency manifest"
verify_file "$dependency_elements" "$expected_dependency_elements_sha" \
  "candidate dependency element evidence"
verify_file "$dependency_evidence" "$expected_dependency_evidence_sha" \
  "candidate dependency evidence manifest"
(
  cd "$project_dir/transactor/reports"
  sha256sum -c stage-1-candidate-dependency-evidence.sha256 >/dev/null
) || fail "candidate dependency evidence does not self-verify"
verify_file "$peer_compile" "$expected_peer_compile_sha" \
  "Peer Java compile helper"
verify_file "$peer_compile_tool" "$expected_peer_compile_tool_sha" \
  "Peer Java compiler"
verify_file "$peer_source_list" "$expected_peer_source_list_sha" \
  "Peer Java source list"
verify_file "$transactor_java_validate" \
  "$expected_transactor_java_validate_sha" "Transactor Java validator"
verify_file "$resource_stage" "$expected_resource_stage_sha" \
  "candidate resource stage helper"
verify_file "$peer_promise_source" "$expected_peer_promise_source_sha" \
  "recovered Peer promise source"
verify_file "$transactor_promise_source" \
  "$expected_transactor_promise_source_sha" \
  "recovered Transactor promise source"

mkdir -p "$evidence_root/inputs" "$evidence_root/build" \
  "$evidence_root/lanes" "$evidence_root/build-env/home" \
  "$evidence_root/build-env/tmp"

printf 'role\tpath\tsha256\n' >"$evidence_root/inputs/runtime-and-tools.tsv"
record_verified_file() {
  local role=$1
  local path=$2
  local expected=$3
  verify_file "$path" "$expected" "$role"
  printf '%s\t%s\t%s\n' "$role" "$path" "$expected" \
    >>"$evidence_root/inputs/runtime-and-tools.tsv"
}
record_verified_file original-peer "$peer_jar" "$expected_peer_sha"
record_verified_file original-transactor "$transactor_jar" \
  "$expected_transactor_sha"
record_verified_file oracle-only-core2 "$oracle_core2_jar" \
  "$expected_oracle_core2_sha"
record_verified_file oracle-only-nano "$oracle_nano_jar" \
  "$expected_oracle_nano_sha"
record_verified_file java "$java_bin" "$expected_java_sha"
record_verified_file javac "$javac_bin" "$expected_javac_sha"
record_verified_file javap "$javap_bin" "$expected_javap_sha"
record_verified_file dependency-manifest "$dependency_manifest" \
  "$expected_dependency_manifest_sha"
record_verified_file dependency-elements "$dependency_elements" \
  "$expected_dependency_elements_sha"
record_verified_file dependency-evidence "$dependency_evidence" \
  "$expected_dependency_evidence_sha"
record_verified_file peer-java-compile-helper "$peer_compile" \
  "$expected_peer_compile_sha"
record_verified_file peer-java-compiler "$peer_compile_tool" \
  "$expected_peer_compile_tool_sha"
record_verified_file peer-java-source-list "$peer_source_list" \
  "$expected_peer_source_list_sha"
record_verified_file transactor-java-validator "$transactor_java_validate" \
  "$expected_transactor_java_validate_sha"
record_verified_file resource-stage-helper "$resource_stage" \
  "$expected_resource_stage_sha"
record_verified_file recovered-peer-promise-source "$peer_promise_source" \
  "$expected_peer_promise_source_sha"
record_verified_file recovered-transactor-promise-source \
  "$transactor_promise_source" "$expected_transactor_promise_source_sha"

snapshot_tree() {
  local root=$1
  local output=$2
  [[ -d "$root" && ! -L "$root" ]] ||
    fail "tree root is missing or symbolic: $root"
  [[ -z $(find "$root" -type l -print -quit) ]] ||
    fail "tree contains a symbolic link: $root"
  (
    cd "$root"
    find . -type f -print0 | sort -z | xargs -0 sha256sum
  ) >"$output"
}

snapshot_tree "$project_dir/src-clj" \
  "$evidence_root/inputs/peer-source-tree.sha256"
snapshot_tree "$project_dir/transactor/src-clj" \
  "$evidence_root/inputs/transactor-source-tree.sha256"
snapshot_tree "$project_dir/src-java" \
  "$evidence_root/inputs/peer-java-tree.sha256"
snapshot_tree "$project_dir/transactor/src-java" \
  "$evidence_root/inputs/transactor-java-tree.sha256"
snapshot_tree "$resource_templates" \
  "$evidence_root/inputs/resource-template-tree.sha256"

probe_sha=$(sha256sum "$probe" | awk '{print $1}')
runner_sha=$(sha256sum "$0" | awk '{print $1}')
printf '%s  %s\n' "$probe_sha" "$probe" \
  >"$evidence_root/inputs/probe.sha256"
printf '%s  %s\n' "$runner_sha" "$0" \
  >"$evidence_root/inputs/runner.sha256"

base_classpath=
dependency_count=0
printf 'position\trole\tpath\tsha256\n' \
  >"$evidence_root/inputs/recovered-runtime-dependencies.tsv"
while IFS=$'\t' read -r position role root relative kind expected; do
  [[ "$position" == position ]] && continue
  [[ "$role" == retained-distribution-dependency ]] || continue
  [[ "$root" == distribution && "$kind" == jar ]] ||
    fail "invalid retained dependency row at position $position"
  dependency="$datomic_home/$relative"
  verify_file "$dependency" "$expected" "dependency $relative"
  case "$dependency" in
    *peer-1.0.7277.jar*|*datomic-transactor-pro-1.0.7277.jar*|*core2-*.jar*|*nano-impl-*.jar*)
      fail "implementation archive entered retained dependency path: $dependency"
      ;;
  esac
  printf '%s\t%s\t%s\t%s\n' "$position" "$role" "$dependency" "$expected" \
    >>"$evidence_root/inputs/recovered-runtime-dependencies.tsv"
  if [[ -z "$base_classpath" ]]; then
    base_classpath=$dependency
  else
    base_classpath="$base_classpath:$dependency"
  fi
  dependency_count=$((dependency_count + 1))
done <"$dependency_manifest"
[[ "$dependency_count" == 531 ]] ||
  fail "expected 531 retained runtime dependencies, found $dependency_count"

peer_classes="$evidence_root/build/peer-java-classes"
transactor_java_build="$evidence_root/build/transactor-java-validation"
transactor_classes="$transactor_java_build/classes-a"
resources_build="$evidence_root/build/candidate-resources"
candidate_resources="$resources_build/runtime"

if ! /usr/bin/env -i \
  PATH="$java_root/bin:/usr/bin:/bin" \
  HOME="$evidence_root/build-env/home" TMPDIR="$evidence_root/build-env/tmp" \
  LC_ALL=C TZ=UTC \
  "$peer_compile" "$datomic_home" "$peer_classes" \
  >"$evidence_root/build/peer-java.stdout" \
  2>"$evidence_root/build/peer-java.stderr"; then
  fail "fresh Peer Java compilation failed"
fi
[[ $(wc -l <"$evidence_root/build/peer-java.stdout") == 2 &&
   $(grep -Fc 'compiled 43 Java sources' \
      "$evidence_root/build/peer-java.stdout") == 1 &&
   $(grep -Fc 'compiled the 43 handwritten Java sources into 47 classes' \
      "$evidence_root/build/peer-java.stdout") == 1 ]] ||
  fail "Peer Java compilation violated its exact stdout contract"
[[ $(wc -l <"$evidence_root/build/peer-java.stderr") == 4 &&
   $(grep -Fc 'Note: Some input files use or override a deprecated API.' \
      "$evidence_root/build/peer-java.stderr") == 1 &&
   $(grep -Fc 'Note: Recompile with -Xlint:deprecation for details.' \
      "$evidence_root/build/peer-java.stderr") == 1 &&
   $(grep -Fc 'Note: Some input files use unchecked or unsafe operations.' \
      "$evidence_root/build/peer-java.stderr") == 1 &&
   $(grep -Fc 'Note: Recompile with -Xlint:unchecked for details.' \
      "$evidence_root/build/peer-java.stderr") == 1 ]] ||
  fail "Peer Java compilation violated its exact stderr contract"
[[ $(find "$peer_classes" -type f -name '*.class' | wc -l) == 47 ]] ||
  fail "Peer Java compilation did not emit exactly 47 classes"

if ! /usr/bin/env -i \
  PATH="$java_root/bin:/usr/bin:/bin" \
  HOME="$evidence_root/build-env/home" TMPDIR="$evidence_root/build-env/tmp" \
  LC_ALL=C TZ=UTC \
  "$transactor_java_validate" "$datomic_home" "$transactor_java_build" \
  "$javac_bin" \
  >"$evidence_root/build/transactor-java.stdout" \
  2>"$evidence_root/build/transactor-java.stderr"; then
  fail "fresh Transactor Java validation failed"
fi
[[ $(wc -l <"$evidence_root/build/transactor-java.stdout") == 4 &&
   $(grep -Fc 'Java recovery exact: 52 classes; relation=' \
      "$evidence_root/build/transactor-java.stdout") == 1 &&
   $(grep -Fc 'Transactor Java recovery validation passed: ' \
      "$evidence_root/build/transactor-java.stdout") == 1 &&
   $(grep -Fc '46 sources -> 52 classes; ABI 52/52; normalized code 52/52' \
      "$evidence_root/build/transactor-java.stdout") == 1 &&
   $(grep -Fc 'Deterministic repeat build: MATCH; regenerated raw-byte matches: 20' \
      "$evidence_root/build/transactor-java.stdout") == 1 ]] ||
  fail "Transactor Java validation violated its exact stdout contract"
[[ $(wc -l <"$evidence_root/build/transactor-java.stderr") == 8 &&
   $(grep -Fc 'Note: Some input files use or override a deprecated API.' \
      "$evidence_root/build/transactor-java.stderr") == 2 &&
   $(grep -Fc 'Note: Recompile with -Xlint:deprecation for details.' \
      "$evidence_root/build/transactor-java.stderr") == 2 &&
   $(grep -Fc 'Note: Some input files use unchecked or unsafe operations.' \
      "$evidence_root/build/transactor-java.stderr") == 2 &&
   $(grep -Fc 'Note: Recompile with -Xlint:unchecked for details.' \
      "$evidence_root/build/transactor-java.stderr") == 2 ]] ||
  fail "Transactor Java validation violated its exact stderr contract"
[[ $(find "$transactor_classes" -type f -name '*.class' | wc -l) == 52 ]] ||
  fail "Transactor Java validation did not emit exactly 52 classes"

if ! /usr/bin/env -i \
  PATH="$java_root/bin:/usr/bin:/bin" \
  HOME="$evidence_root/build-env/home" TMPDIR="$evidence_root/build-env/tmp" \
  LC_ALL=C TZ=UTC \
  "$resource_stage" "$resource_templates" "$resources_build" 1.0.7277 \
  >"$evidence_root/build/candidate-resources.stdout" \
  2>"$evidence_root/build/candidate-resources.stderr"; then
  fail "fresh candidate resource staging failed"
fi
[[ ! -s "$evidence_root/build/candidate-resources.stderr" ]] ||
  fail "candidate resource staging wrote stderr"
[[ $(wc -l <"$evidence_root/build/candidate-resources.stdout") == 2 &&
   $(grep -Fc 'Candidate-owned structural resources staged: ' \
      "$evidence_root/build/candidate-resources.stdout") == 1 &&
   $(grep -Fc '4 runtime resources; AWS provisioning behavior remains explicitly unvalidated' \
      "$evidence_root/build/candidate-resources.stdout") == 1 ]] ||
  fail "candidate resource staging violated its exact stdout contract"
(
  cd "$resources_build"
  sha256sum -c manifest.sha256 >/dev/null
) || fail "candidate resource build does not verify"

snapshot_tree "$peer_classes" \
  "$evidence_root/build/peer-java-classes.sha256"
snapshot_tree "$transactor_classes" \
  "$evidence_root/build/transactor-java-classes.sha256"
snapshot_tree "$candidate_resources" \
  "$evidence_root/build/candidate-runtime-resources.sha256"

[[ -z $(find "$evidence_root/build-env/home" "$evidence_root/build-env/tmp" \
               -mindepth 1 -print -quit) ]] ||
  fail "fresh builds left files in isolated HOME or TMPDIR"

bytecode_root="$evidence_root/artifact-bytecode"
mkdir -p "$bytecode_root/peer" "$bytecode_root/transactor"

write_role_map() {
  local artifact=$1
  local output=$2
  unzip -Z1 "$artifact" |
    awk '/^datomic\/promise(\$.*|__init)\.class$/' |
    sort |
    awk '{
      entry=$0
      role=$0
      gsub(/__[0-9]+/, "__ID", role)
      seen[role]++
      print role "#" seen[role] "\t" entry
    }' >"$output"
}

write_role_map "$peer_jar" "$bytecode_root/peer/roles.tsv"
write_role_map "$transactor_jar" "$bytecode_root/transactor/roles.tsv"
[[ $(wc -l <"$bytecode_root/peer/roles.tsv") == 11 &&
   $(wc -l <"$bytecode_root/transactor/roles.tsv") == 11 ]] ||
  fail "promise artifact role map does not contain exactly 11 classes per artifact"

{
  printf '%s\t%s\t%s\n' \
    'datomic/promise$call_user_code.class#1' exact normalized-javap-exact
  printf '%s\t%s\t%s\n' \
    'datomic/promise$delivered.class#1' exact normalized-javap-exact
  printf '%s\t%s\t%s\n' \
    'datomic/promise$fn__ID.class#1' exact normalized-javap-exact
  printf '%s\t%s\t%s\n' \
    'datomic/promise$loading__ID__auto____ID.class#1' exact \
    normalized-javap-exact
  printf '%s\t%s\t%s\n' \
    'datomic/promise$settable_future$reify__ID$fn__ID.class#1' residual \
    captured-field-gc-clearing
  printf '%s\t%s\t%s\n' \
    'datomic/promise$settable_future$reify__ID$fn__ID.class#2' residual \
    locking-lowering-and-gc-clearing
  printf '%s\t%s\t%s\n' \
    'datomic/promise$settable_future$reify__ID$fn__ID.class#3' residual \
    capture-order-locking-gc-dead-slot
  printf '%s\t%s\t%s\n' \
    'datomic/promise$settable_future$reify__ID.class#1' residual \
    capture-order-gc-nil-dead-slots
  printf '%s\t%s\t%s\n' \
    'datomic/promise$settable_future.class#1' residual capture-local-order
  printf '%s\t%s\t%s\n' \
    'datomic/promise$throw_executionexception_if_throwable.class#1' residual \
    dead-verifier-slots
  printf '%s\t%s\t%s\n' \
    'datomic/promise__init.class#1' residual dead-verifier-slot
} >"$bytecode_root/expected-relation.tsv"

cut -f1 "$bytecode_root/peer/roles.tsv" >"$bytecode_root/peer-role-order"
cut -f1 "$bytecode_root/transactor/roles.tsv" \
  >"$bytecode_root/transactor-role-order"
cut -f1 "$bytecode_root/expected-relation.tsv" \
  >"$bytecode_root/expected-role-order"
cmp -s "$bytecode_root/expected-role-order" \
  "$bytecode_root/peer-role-order" ||
  fail "Peer promise role inventory differs from the pinned 11-role relation"
cmp -s "$bytecode_root/expected-role-order" \
  "$bytecode_root/transactor-role-order" ||
  fail "Transactor promise role inventory differs from the pinned 11-role relation"

: >"$bytecode_root/peer/methods.unsorted.tsv"
: >"$bytecode_root/peer/fields.unsorted.tsv"
: >"$bytecode_root/transactor/methods.unsorted.tsv"
: >"$bytecode_root/transactor/fields.unsorted.tsv"
printf 'role\tpeer_entry\ttransactor_entry\tpeer_sha256\ttransactor_sha256\traw_relation\tnormalized_javap_relation\tclassification\n' \
  >"$bytecode_root/relation.tsv"

row_number=0
while IFS=$'\t' read -r role peer_entry transactor_role transactor_entry; do
  [[ "$role" == "$transactor_role" ]] ||
    fail "misaligned promise artifact roles: $role / $transactor_role"
  row_number=$((row_number + 1))
  row_tag=$(printf '%02d' "$row_number")
  peer_class=${peer_entry%.class}
  peer_class=${peer_class//\//.}
  transactor_class=${transactor_entry%.class}
  transactor_class=${transactor_class//\//.}
  peer_javap="$bytecode_root/peer/$row_tag.javap"
  transactor_javap="$bytecode_root/transactor/$row_tag.javap"
  peer_javap_err="$bytecode_root/peer/$row_tag.javap.stderr"
  transactor_javap_err="$bytecode_root/transactor/$row_tag.javap.stderr"
  "$javap_bin" -classpath "$peer_jar" -c -p -s "$peer_class" \
    >"$peer_javap" 2>"$peer_javap_err"
  "$javap_bin" -classpath "$transactor_jar" -c -p -s \
    "$transactor_class" >"$transactor_javap" 2>"$transactor_javap_err"
  [[ ! -s "$peer_javap_err" && ! -s "$transactor_javap_err" ]] ||
    fail "javap wrote stderr for promise role $role"

  for artifact in peer transactor; do
    sed -E \
      -e 's/__[0-9]+/__ID/g' \
      -e 's/#[0-9]+/#CP/g' \
      -e 's/[[:space:]]+/ /g' \
      -e 's/^ //; s/ $//' \
      "$bytecode_root/$artifact/$row_tag.javap" \
      >"$bytecode_root/$artifact/$row_tag.normalized-javap"
    awk -v role="$role" '
      index($0, "descriptor: ") == 1 {
        descriptor=substr($0, 13)
        output=role "\t" previous "\t" descriptor
        if (descriptor ~ /^\(/) print output >> methods
        else print output >> fields
      }
      {previous=$0}
    ' methods="$bytecode_root/$artifact/methods.unsorted.tsv" \
      fields="$bytecode_root/$artifact/fields.unsorted.tsv" \
      "$bytecode_root/$artifact/$row_tag.normalized-javap"
  done

  peer_raw_sha=$(unzip -p "$peer_jar" "$peer_entry" |
    sha256sum | awk '{print $1}')
  transactor_raw_sha=$(unzip -p "$transactor_jar" "$transactor_entry" |
    sha256sum | awk '{print $1}')
  if [[ "$peer_raw_sha" == "$transactor_raw_sha" ]]; then
    raw_relation=exact
  else
    raw_relation=different
  fi
  if cmp -s "$bytecode_root/peer/$row_tag.normalized-javap" \
            "$bytecode_root/transactor/$row_tag.normalized-javap"; then
    javap_relation=exact
  else
    javap_relation=residual
  fi
  expected_status=$(awk -F '\t' -v role="$role" \
    '$1 == role {print $2}' "$bytecode_root/expected-relation.tsv")
  classification=$(awk -F '\t' -v role="$role" \
    '$1 == role {print $3}' "$bytecode_root/expected-relation.tsv")
  [[ -n "$expected_status" && -n "$classification" ]] ||
    fail "promise role lacks an expected classification: $role"
  [[ "$javap_relation" == "$expected_status" ]] ||
    fail "promise role changed classification: $role expected $expected_status found $javap_relation"
  printf '%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\n' \
    "$role" "$peer_entry" "$transactor_entry" "$peer_raw_sha" \
    "$transactor_raw_sha" "$raw_relation" "$javap_relation" \
    "$classification" >>"$bytecode_root/relation.tsv"
done < <(paste "$bytecode_root/peer/roles.tsv" \
               "$bytecode_root/transactor/roles.tsv")

[[ "$row_number" == 11 ]] ||
  fail "promise static comparator did not process exactly 11 roles"
[[ $(awk -F '\t' 'NR > 1 && $6 == "exact" {count++} END {print count+0}' \
      "$bytecode_root/relation.tsv") == 0 ]] ||
  fail "expected all 11 raw promise class pairs to differ"
[[ $(awk -F '\t' 'NR > 1 && $7 == "exact" {count++} END {print count+0}' \
      "$bytecode_root/relation.tsv") == 4 ]] ||
  fail "expected exactly four normalized-javap-exact promise roles"
[[ $(awk -F '\t' 'NR > 1 && $7 == "residual" {count++} END {print count+0}' \
      "$bytecode_root/relation.tsv") == 7 ]] ||
  fail "expected exactly seven classified promise bytecode residuals"

for artifact in peer transactor; do
  sort "$bytecode_root/$artifact/methods.unsorted.tsv" \
    >"$bytecode_root/$artifact/methods.abi.tsv"
  sort "$bytecode_root/$artifact/fields.unsorted.tsv" \
    >"$bytecode_root/$artifact/fields.abi.tsv"
  [[ $(wc -l <"$bytecode_root/$artifact/methods.abi.tsv") == 51 ]] ||
    fail "$artifact promise ABI does not contain exactly 51 methods"
  [[ $(wc -l <"$bytecode_root/$artifact/fields.abi.tsv") == 60 ]] ||
    fail "$artifact promise ABI does not contain exactly 60 fields"
done
cmp -s "$bytecode_root/peer/methods.abi.tsv" \
  "$bytecode_root/transactor/methods.abi.tsv" ||
  fail "Peer and Transactor promise method ABIs differ after generated-ID normalization"
cmp -s "$bytecode_root/peer/fields.abi.tsv" \
  "$bytecode_root/transactor/fields.abi.tsv" ||
  fail "Peer and Transactor promise field ABIs differ after generated-ID normalization"
method_abi_sha=$(sha256sum "$bytecode_root/peer/methods.abi.tsv" |
  awk '{print $1}')
field_abi_sha=$(sha256sum "$bytecode_root/peer/fields.abi.tsv" |
  awk '{print $1}')
[[ "$method_abi_sha" == "$expected_method_abi_sha" ]] ||
  fail "promise method ABI hash changed: $method_abi_sha"
[[ "$field_abi_sha" == "$expected_field_abi_sha" ]] ||
  fail "promise field ABI hash changed: $field_abi_sha"

printf 'lane\tkind\tordered_classpath\n' \
  >"$evidence_root/inputs/lane-classpaths.tsv"
printf 'lane\tkind\tsupported_sha256\tmetadata_sha256\n' \
  >"$evidence_root/lanes.tsv"

run_lane() {
  local lane=$1
  local kind=$2
  local classpath=$3
  local promise_origin=$4
  local version_origin=$5
  local java_origin=$6
  local metadata_profile=$7
  local lane_root="$evidence_root/lanes/$lane"
  mkdir -p "$lane_root/home" "$lane_root/tmp" "$lane_root/cwd"
  printf '%s\t%s\t%s\n' "$lane" "$kind" "$classpath" \
    >>"$evidence_root/inputs/lane-classpaths.tsv"
  case "$kind" in
    original)
      [[ "$classpath" == "$peer_jar:"* ||
         "$classpath" == "$transactor_jar:"* ]] ||
        fail "original lane lacks its owned oracle artifact: $lane"
      ;;
    recovered)
      [[ "$classpath" != *"peer-1.0.7277.jar"* &&
         "$classpath" != *"datomic-transactor-pro-1.0.7277.jar"* &&
         "$classpath" != *"core2-"* && "$classpath" != *"nano-impl-"* ]] ||
        fail "recovered lane contains an implementation archive: $lane"
      ;;
    *) fail "unknown lane kind: $kind" ;;
  esac
  if (
    cd "$lane_root/cwd"
    /usr/bin/timeout --signal=TERM --kill-after=5s 90s \
      /usr/bin/env -i \
      PATH="$java_root/bin:/usr/bin:/bin" HOME="$lane_root/home" \
      TMPDIR="$lane_root/tmp" LC_ALL=C TZ=UTC \
      "$java_bin" -XX:+PerfDisableSharedMem \
      -Djava.io.tmpdir="$lane_root/tmp" \
      -Ddatomic.disableAllExtensions=true \
      -Dstage2.promise.probe="$probe" \
      -Dstage2.promise.expected-kind="$kind" \
      -Dstage2.promise.expected-promise-origin="$promise_origin" \
      -Dstage2.promise.expected-version-origin="$version_origin" \
      -Dstage2.promise.expected-java-origin="$java_origin" \
      -Dstage2.promise.expected-metadata-profile="$metadata_profile" \
      -cp "$classpath" clojure.main -e \
      '(binding [*err* (java.io.StringWriter.)] (load-file (System/getProperty "stage2.promise.probe")))'
  ) >"$lane_root/stdout" 2>"$lane_root/stderr"; then
    :
  else
    lane_status=$?
    fail "$lane JVM failed with status $lane_status; inspect $lane_root/stderr"
  fi
  [[ ! -s "$lane_root/stderr" ]] || fail "$lane wrote stderr"
  [[ $(wc -l <"$lane_root/stdout") == 2 ]] ||
    fail "$lane stdout contains other than the two expected result rows"
  [[ $(grep -Fc 'STAGE2_PROMISE_OVERLAP_SUPPORTED_RESULT ' \
              "$lane_root/stdout") == 1 ]] ||
    fail "$lane did not emit exactly one supported-domain result"
  [[ $(grep -Fc 'STAGE2_PROMISE_OVERLAP_METADATA_RESULT ' \
              "$lane_root/stdout") == 1 ]] ||
    fail "$lane did not emit exactly one metadata result"
  grep -F 'STAGE2_PROMISE_OVERLAP_SUPPORTED_RESULT ' \
    "$lane_root/stdout" >"$lane_root/supported.result"
  grep -F 'STAGE2_PROMISE_OVERLAP_METADATA_RESULT ' \
    "$lane_root/stdout" >"$lane_root/metadata.result"
  [[ -z $(find "$lane_root/home" "$lane_root/tmp" "$lane_root/cwd" \
                 -mindepth 1 -print -quit) ]] ||
    fail "$lane left files in an isolated writable root"
  printf '%s\t%s\t%s\t%s\n' "$lane" "$kind" \
    "$(sha256sum "$lane_root/supported.result" | awk '{print $1}')" \
    "$(sha256sum "$lane_root/metadata.result" | awk '{print $1}')" \
    >>"$evidence_root/lanes.tsv"
}

run_lane original-peer original \
  "$peer_jar:$base_classpath" "$peer_jar" "$peer_jar" "$peer_jar" \
  exact-original
run_lane recovered-peer recovered \
  "$project_dir/src-clj:$peer_classes:$candidate_resources:$base_classpath" \
  "$project_dir/src-clj" "$candidate_resources" "$peer_classes" \
  recovered-peer-source
run_lane original-transactor original \
  "$transactor_jar:$oracle_core2_jar:$oracle_nano_jar:$base_classpath" \
  "$transactor_jar" "$transactor_jar" "$transactor_jar" exact-original
run_lane recovered-transactor recovered \
  "$project_dir/transactor/src-clj:$project_dir/src-clj:$transactor_classes:$candidate_resources:$base_classpath" \
  "$project_dir/transactor/src-clj" "$candidate_resources" \
  "$transactor_classes" exact-original

supported_reference="$evidence_root/lanes/original-peer/supported.result"
for lane in recovered-peer original-transactor recovered-transactor; do
  cmp -s "$supported_reference" "$evidence_root/lanes/$lane/supported.result" ||
    fail "supported promise behavior differs between original Peer and $lane"
done
cmp -s "$evidence_root/lanes/original-peer/metadata.result" \
  "$evidence_root/lanes/original-transactor/metadata.result" ||
  fail "original Peer and original Transactor metadata differ"
cmp -s "$evidence_root/lanes/original-transactor/metadata.result" \
  "$evidence_root/lanes/recovered-transactor/metadata.result" ||
  fail "recovered Transactor does not preserve exact original metadata"
if cmp -s "$evidence_root/lanes/original-peer/metadata.result" \
  "$evidence_root/lanes/recovered-peer/metadata.result"; then
  fail "expected recovered-Peer source metadata delta was absent"
fi

snapshot_tree "$project_dir/src-clj" \
  "$evidence_root/inputs/peer-source-tree.after.sha256"
snapshot_tree "$project_dir/transactor/src-clj" \
  "$evidence_root/inputs/transactor-source-tree.after.sha256"
snapshot_tree "$project_dir/src-java" \
  "$evidence_root/inputs/peer-java-tree.after.sha256"
snapshot_tree "$project_dir/transactor/src-java" \
  "$evidence_root/inputs/transactor-java-tree.after.sha256"
snapshot_tree "$resource_templates" \
  "$evidence_root/inputs/resource-template-tree.after.sha256"
for tree in peer-source-tree transactor-source-tree peer-java-tree \
            transactor-java-tree resource-template-tree; do
  cmp -s "$evidence_root/inputs/$tree.sha256" \
    "$evidence_root/inputs/$tree.after.sha256" ||
    fail "sealed input tree changed during gate: $tree"
done
[[ $(sha256sum "$probe" | awk '{print $1}') == "$probe_sha" ]] ||
  fail "probe changed during the gate"
[[ $(sha256sum "$0" | awk '{print $1}') == "$runner_sha" ]] ||
  fail "runner changed during the gate"

while IFS=$'\t' read -r role path expected; do
  [[ "$role" == role ]] && continue
  verify_file "$path" "$expected" "$role after run"
done <"$evidence_root/inputs/runtime-and-tools.tsv"
(
  cd "$project_dir/transactor/reports"
  sha256sum -c stage-1-candidate-dependency-evidence.sha256 >/dev/null
) || fail "candidate dependency evidence changed during the gate"
while IFS=$'\t' read -r position role path expected; do
  [[ "$position" == position ]] && continue
  verify_file "$path" "$expected" "dependency after run at position $position"
done <"$evidence_root/inputs/recovered-runtime-dependencies.tsv"
(
  cd "$peer_classes"
  sha256sum -c ../peer-java-classes.sha256 >/dev/null
) || fail "recovered Peer Java classes changed during the gate"
(
  cd "$transactor_classes"
  sha256sum -c ../../transactor-java-classes.sha256 >/dev/null
) || fail "recovered Transactor Java classes changed during the gate"
(
  cd "$candidate_resources"
  sha256sum -c ../../candidate-runtime-resources.sha256 >/dev/null
) || fail "candidate runtime resources changed during the gate"
(
  cd "$resources_build"
  sha256sum -c manifest.sha256 >/dev/null
) || fail "candidate resource evidence changed during the gate"
supported_sha=$(sha256sum "$supported_reference" | awk '{print $1}')
exact_metadata_sha=$(sha256sum \
  "$evidence_root/lanes/original-peer/metadata.result" | awk '{print $1}')
recovered_peer_metadata_sha=$(sha256sum \
  "$evidence_root/lanes/recovered-peer/metadata.result" | awk '{print $1}')
bytecode_relation_sha=$(sha256sum "$bytecode_root/relation.tsv" |
  awk '{print $1}')
{
  printf 'claim\tstatus\tdetail\n'
  printf 'supported_domain_behavior\tPASS\t%s\n' \
    'all four lanes emitted byte-identical helper, Future, listener, delivery-race, Throwable, cancellation, rejection, interruption, and cleanup results'
  printf 'exact_original_metadata\tPASS\t%s\n' \
    'original Peer, original Transactor, and recovered Transactor emitted byte-identical exact metadata for all four public Vars'
  printf 'recovered_peer_metadata_delta\tPASS\t%s\n' \
    'recovered Peer preserves roots and callable arities while explicitly retaining source file/line/column metadata and omitting the authored Executor argument tag'
  printf 'artifact_role_relation\tPASS\t%s\n' \
    '11/11 roles mapped; 4 normalized javap payloads are exact and 7 residuals are classified as compiler lowering, capture ordering, GC clearing, nil placement, or dead verifier slots'
  printf 'artifact_abi_relation\tPASS\t%s\n' \
    'generated-ID-normalized artifacts agree on all 51 methods and all 60 fields'
  printf 'candidate_origin\tPASS\t%s\n' \
    'each recovered lane proved current repository promise source, freshly compiled Java, and candidate-owned VERSION origins'
  printf 'candidate_isolation\tPASS\t%s\n' \
    'recovered lanes contain no Peer, Transactor, core2, or Nano implementation archive'
  printf 'fresh_candidate_builds\tPASS\t%s\n' \
    'Peer 43-source Java closure and Transactor 46-source Java closure were freshly built; candidate resources were freshly staged'
  printf 'failure_cleanup\tPASS\t%s\n' \
    'bounded daemon races and an interrupted blocking waiter terminate; default handler and System.err are restored after injected failures'
  printf 'supported_result_sha256\tPASS\t%s\n' "$supported_sha"
  printf 'exact_metadata_sha256\tPASS\t%s\n' "$exact_metadata_sha"
  printf 'recovered_peer_metadata_sha256\tPASS\t%s\n' \
    "$recovered_peer_metadata_sha"
  printf 'method_abi_sha256\tPASS\t%s\n' "$method_abi_sha"
  printf 'field_abi_sha256\tPASS\t%s\n' "$field_abi_sha"
  printf 'bytecode_relation_sha256\tPASS\t%s\n' "$bytecode_relation_sha"
} >"$evidence_root/summary.tsv"

(
  cd "$evidence_root"
  find . -type f ! -name evidence-manifest.sha256 -print0 |
    sort -z | xargs -0 sha256sum
) >"$evidence_root/evidence-manifest.sha256"
(
  cd "$evidence_root"
  sha256sum -c evidence-manifest.sha256 >/dev/null
) || fail "final evidence manifest does not self-verify"
manifest_sha=$(sha256sum "$evidence_root/evidence-manifest.sha256" | awk '{print $1}')
echo "STAGE2_PROMISE_OVERLAP_PASS supported_sha256=$supported_sha exact_metadata_sha256=$exact_metadata_sha recovered_peer_metadata_sha256=$recovered_peer_metadata_sha method_abi_sha256=$method_abi_sha field_abi_sha256=$field_abi_sha relation_sha256=$bytecode_relation_sha manifest_sha256=$manifest_sha evidence_root=$evidence_root"
