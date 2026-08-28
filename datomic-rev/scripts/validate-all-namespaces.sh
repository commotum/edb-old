#!/usr/bin/bash -p

set -euo pipefail
umask 077
unset BASH_ENV ENV CDPATH GLOBIGNORE JAVA_TOOL_OPTIONS JDK_JAVA_OPTIONS \
  _JAVA_OPTIONS CLASSPATH JAVA_HOME LD_PRELOAD LD_LIBRARY_PATH
IFS=$' \t\n'
export LC_ALL=C TZ=UTC
PATH=${DATOMIC_REV_SAFE_PATH:-/usr/lib/jvm/java-21-openjdk-amd64/bin:/usr/bin:/bin}
export PATH

script_dir=$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd)
project_dir=$(cd -- "$script_dir/.." && pwd)
datomic_home=${1:-"$project_dir/../../datomic/datomic-pro-1.0.7277"}
work_root=${2:-$(mktemp -d -t datomic-source-validation.XXXXXXXX)}
jobs=${JOBS:-4}
source_root=${DATOMIC_REV_CANDIDATE_SOURCE_ROOT:-"$project_dir/src-clj"}
java_bin=${DATOMIC_REV_JAVA_BIN:-/usr/lib/jvm/java-21-openjdk-amd64/bin/java}
namespace_timeout_seconds=${DATOMIC_REV_NAMESPACE_TIMEOUT_SECONDS:-180}
setup_timeout_seconds=${DATOMIC_REV_SETUP_TIMEOUT_SECONDS:-600}
namespace_index="$project_dir/reports/source-index/namespaces.tsv"
peer_resource_list="$project_dir/reports/peer-resource-paths.txt"
peer_jar="$datomic_home/peer-1.0.7277.jar"
transactor_jar="$datomic_home/datomic-transactor-pro-1.0.7277.jar"
distribution_tsv="$project_dir/transactor/baseline/distribution-jars.tsv"
nano_sanitizer="$project_dir/transactor/scripts/sanitize-nano-impl.sh"
nano_sanitizer_source="$project_dir/transactor/tools/SanitizeNanoImpl.java"
nano_policy="$project_dir/transactor/reports/stage-1-nano-entry-policy.tsv"
deterministic_packager="$script_dir/DeterministicJar.java"
expected_sha=cb55c9d01e155f9965e3332cd8ef70be42cdefacb50c072076e9ecf22b9a23ba
expected_java_sha=377196a32c5e4442b604bbf36add1c49cfe8680cd27edd693b60872e58895e9e
expected_nano_sha=fbde8184da356bd3a9995a9f05f01493efe4baa87c4d48377cda3e53331807cd
expected_sanitized_nano_sha=08f9699d6e35b9e052ec85ee15e0896b3a685c74e6e226d8cd89d9c61dbf8d5f
expected_sanitized_nano_bytes=81218
expected_nano_sanitizer_wrapper_sha=e906b8e5ae56207677bb45457519d782fba31fabfe539bcd4465e089d0566186
expected_peer_resource_list_sha=45d789d7da96ae47b209e2bef0b12f3d379bb4af411a70a74fa768acfd3222d1
forbidden_vendor_key_sha=f3627f52580b84fe8f536423643fb46999fd2458498989307f2820d778eb73a1
forbidden_vendor_trust_sha=ca64f839d051d909974a624b4345d83cc739a0b90edbe54e7c3605a2fd8fc13b

die() {
  echo "Peer source-only validation: $*" >&2
  exit 1
}

[[ "$jobs" =~ ^[1-9][0-9]*$ ]] || die "JOBS must be a positive integer"
[[ "$namespace_timeout_seconds" =~ ^[1-9][0-9]*$ && \
   "$namespace_timeout_seconds" -le 1800 ]] || \
  die "DATOMIC_REV_NAMESPACE_TIMEOUT_SECONDS must be 1..1800"
[[ "$setup_timeout_seconds" =~ ^[1-9][0-9]*$ && \
   "$setup_timeout_seconds" -le 3600 ]] || \
  die "DATOMIC_REV_SETUP_TIMEOUT_SECONDS must be 1..3600"

for required_file in "$peer_jar" "$transactor_jar" "$namespace_index" \
    "$peer_resource_list" \
    "$distribution_tsv" "$java_bin" "$nano_sanitizer" \
    "$nano_sanitizer_source" "$nano_policy" "$deterministic_packager"; do
  [[ -f "$required_file" && ! -L "$required_file" ]] || \
    die "required regular input is missing or symbolic: $required_file"
done
[[ -d "$source_root" && ! -L "$source_root" ]] || \
  die "candidate source root is missing or symbolic: $source_root"

actual_sha=$(sha256sum "$peer_jar" | awk '{print $1}')
[[ "$actual_sha" == "$expected_sha" ]] || \
  die "unexpected peer JAR SHA-256: $actual_sha"
[[ $(sha256sum "$java_bin" | awk '{print $1}') == "$expected_java_sha" ]] || \
  die "pinned Java launcher hash mismatch"
[[ $(sha256sum "$nano_sanitizer" | awk '{print $1}') == \
   "$expected_nano_sanitizer_wrapper_sha" ]] || \
  die "Nano sanitizer wrapper hash mismatch"
[[ $(sha256sum "$peer_resource_list" | awk '{print $1}') == \
   "$expected_peer_resource_list_sha" ]] || \
  die "canonical Peer resource-list hash mismatch"

if [[ -e "$work_root" ]] && [[ -n "$(find "$work_root" -mindepth 1 -print -quit 2>/dev/null)" ]]; then
  die "refusing to overwrite non-empty validation directory: $work_root"
fi
[[ ! -L "$work_root" ]] || die "validation directory may not be symbolic: $work_root"

source_abs=$(realpath "$source_root")
work_abs=$(realpath -m "$work_root")
case "$work_abs/" in
  "$source_abs/"* ) die "validation output is nested under candidate source input" ;;
esac
case "$source_abs/" in
  "$work_abs/"* ) die "candidate source input is nested under validation output" ;;
esac

classes_dir="$work_root/handwritten-classes"
stub_dir="$work_root/infinispan-compile-stubs"
library_dir="$work_root/libraries-without-aot-core2"
candidate_resource_dir="$work_root/peer-resources-without-vendor-keystores"
sanitized_nano_root="$work_root/sanitized-nano-derivative"
sanitized_nano_jar="$sanitized_nano_root/nano-impl-0.1.325-sanitized.jar"
runtime_sanitized_nano_jar="$library_dir/nano-impl-0.1.325-sanitized.jar"
log_dir="$work_root/namespace-logs"
runtime_context="$work_root/runtime-context"
mkdir -p "$classes_dir" "$stub_dir" "$library_dir" "$log_dir" \
  "$candidate_resource_dir" "$runtime_context/home" "$runtime_context/tmp" \
  "$runtime_context/java-tmp" "$work_root/immutability"
export HOME="$runtime_context/home" TMPDIR="$runtime_context/tmp"

java_tmp_root="$runtime_context/java-tmp"
prepare_java_tmp() {
  local variable_name=$1
  local label=$2
  local destination="$java_tmp_root/$label"
  [[ ! -e "$destination" && ! -L "$destination" ]] || \
    die "refusing existing per-probe Java temp directory: $destination"
  mkdir -p "$destination"
  printf -v "$variable_name" '%s' "$destination"
}
require_empty_java_tmp() {
  local destination=$1
  local label=$2
  [[ -z $(find "$destination" -mindepth 1 -print -quit) ]] || \
    die "$label left content in its isolated Java temp directory"
}

prepare_java_tmp sanitizer_java_tmp nano-sanitizer
prepare_java_tmp compile_java_tmp handwritten-java-compile
prepare_java_tmp stubs_java_tmp infinispan-stub-compile
prepare_java_tmp behavior_java_tmp recovered-behaviors

grep -Fq 'DATOMIC_NANO_JAVA_TMPDIR' "$nano_sanitizer" || \
  die "Nano sanitizer does not expose the required isolated Java temp contract"

/usr/bin/timeout --signal=TERM --kill-after=30s "${setup_timeout_seconds}s" \
  /usr/bin/env DATOMIC_NANO_JAVA_TMPDIR="$sanitizer_java_tmp" \
  /usr/bin/bash -p "$nano_sanitizer" "$datomic_home" \
  "$sanitized_nano_root" \
  >"$work_root/sanitize-nano.stdout" \
  2>"$work_root/sanitize-nano.stderr"
require_empty_java_tmp "$sanitizer_java_tmp" nano-sanitizer
[[ -f "$sanitized_nano_jar" && ! -L "$sanitized_nano_jar" ]] || \
  die "Nano sanitized derivative was not produced as a regular file"
[[ $(sha256sum "$sanitized_nano_jar" | awk '{print $1}') == \
   "$expected_sanitized_nano_sha" ]] || \
  die "Nano sanitized derivative hash mismatch"
[[ $(stat -c '%s' "$sanitized_nano_jar") == \
   "$expected_sanitized_nano_bytes" ]] || \
  die "Nano sanitized derivative byte-size mismatch"
[[ -z $(zipinfo -1 "$sanitized_nano_jar" \
  | grep -E '\.(jks|p12|pfx|keystore)$' || true) ]] || \
  die "Nano sanitized derivative retained a keystore entry"
(cd "$sanitized_nano_root" && sha256sum -c manifest.sha256 >/dev/null) || \
  die "Nano sanitized derivative evidence manifest does not verify"

/usr/bin/timeout --signal=TERM --kill-after=15s "${setup_timeout_seconds}s" \
  /usr/bin/bash -p "$script_dir/validate-peer-resources.sh" \
  "$datomic_home" "$project_dir/resources"

peer_resource_sanitization="$work_root/peer-resource-sanitization.tsv"
printf 'relative_path\tsha256\ttreatment\n' >"$peer_resource_sanitization"
retained_peer_resources=0
removed_peer_keystores=0
while IFS= read -r peer_resource_path; do
  [[ -n "$peer_resource_path" && "$peer_resource_path" != /* && \
     "$peer_resource_path" != *$'\t'* && \
     "$peer_resource_path" != *$'\n'* ]] || \
    die "canonical Peer resource list contains an invalid path"
  peer_resource="$project_dir/resources/$peer_resource_path"
  [[ -f "$peer_resource" && ! -L "$peer_resource" ]] || \
    die "canonical Peer resource is missing or symbolic: $peer_resource_path"
  peer_resource_sha=$(sha256sum "$peer_resource" | awk '{print $1}')
  case "$peer_resource_path" in
    datomic/transactor-key.jks)
      [[ "$peer_resource_sha" == "$forbidden_vendor_key_sha" ]] || \
        die "Peer vendor key resource hash mismatch"
      printf '%s\t%s\tremove-vendor-keystore\n' \
        "$peer_resource_path" "$peer_resource_sha" \
        >>"$peer_resource_sanitization"
      ((removed_peer_keystores += 1))
      ;;
    datomic/transactor-trust.jks)
      [[ "$peer_resource_sha" == "$forbidden_vendor_trust_sha" ]] || \
        die "Peer vendor trust resource hash mismatch"
      printf '%s\t%s\tremove-vendor-keystore\n' \
        "$peer_resource_path" "$peer_resource_sha" \
        >>"$peer_resource_sanitization"
      ((removed_peer_keystores += 1))
      ;;
    *)
      peer_resource_destination="$candidate_resource_dir/$peer_resource_path"
      mkdir -p "$(dirname "$peer_resource_destination")"
      cp -- "$peer_resource" "$peer_resource_destination"
      [[ $(sha256sum "$peer_resource_destination" | awk '{print $1}') == \
         "$peer_resource_sha" ]] || \
        die "candidate Peer resource copy changed: $peer_resource_path"
      printf '%s\t%s\tretain-exact\n' \
        "$peer_resource_path" "$peer_resource_sha" \
        >>"$peer_resource_sanitization"
      ((retained_peer_resources += 1))
      ;;
  esac
done <"$peer_resource_list"
[[ "$removed_peer_keystores" -eq 2 && "$retained_peer_resources" -eq 8 ]] || \
  die "candidate Peer resource sanitization is not exact 8 retained / 2 removed"
[[ -z $(find "$candidate_resource_dir" -type f \
  \( -name 'transactor-key.jks' -o -name 'transactor-trust.jks' \) \
  -print -quit) ]] || die "candidate Peer resources retained a vendor keystore"
[[ $(find "$candidate_resource_dir" -type f -print0 | sort -z \
  | xargs -0 sha256sum | grep -Ec \
      "^($forbidden_vendor_key_sha|$forbidden_vendor_trust_sha)  " || true) \
  -eq 0 ]] || die "candidate Peer resources retained renamed vendor keystore bytes"

/usr/bin/timeout --signal=TERM --kill-after=15s "${setup_timeout_seconds}s" \
  "$java_bin" -XX:+PerfDisableSharedMem \
  -Djava.io.tmpdir="$compile_java_tmp" \
  "$script_dir/CompileSources.java" \
  "$project_dir/src-java" \
  "$classes_dir" \
  "$peer_jar:$datomic_home/lib/*" \
  "$project_dir/reports/handwritten-java-sources.txt"
require_empty_java_tmp "$compile_java_tmp" handwritten-java-compile

/usr/bin/timeout --signal=TERM --kill-after=15s "${setup_timeout_seconds}s" \
  "$java_bin" -XX:+PerfDisableSharedMem \
  -Djava.io.tmpdir="$stubs_java_tmp" \
  '-Dclojure.compiler.elide-meta=[:doc :file :line]' \
  -cp "$datomic_home/lib/*" clojure.main \
  "$project_dir/tools/infinispan-compile-stubs/build_stubs.clj" "$stub_dir"
require_empty_java_tmp "$stubs_java_tmp" infinispan-stub-compile

core2_sha=$(awk -F '\t' '$2 == "lib/core2-1.0.140.jar" {print $3}' \
  "$distribution_tsv")
peer_sha=$(awk -F '\t' '$2 == "peer-1.0.7277.jar" {print $3}' \
  "$distribution_tsv")
transactor_sha=$(awk -F '\t' \
  '$2 == "datomic-transactor-pro-1.0.7277.jar" {print $3}' \
  "$distribution_tsv")
nano_sha=$(awk -F '\t' '$2 == "lib/nano-impl-0.1.325.jar" {print $3}' \
  "$distribution_tsv")
[[ -n "$core2_sha" && -n "$peer_sha" && -n "$transactor_sha" && \
   "$nano_sha" == "$expected_nano_sha" ]] || \
  die "licensed archive hashes are absent from distribution inventory"
linked_dependencies=0
nano_substitutions=0
for jar in "$datomic_home"/lib/*.jar; do
  [[ -f "$jar" && ! -L "$jar" ]] || \
    die "distribution dependency is missing or symbolic: $jar"
  jar_name=$(basename -- "$jar")
  jar_sha=$(sha256sum "$jar" | awk '{print $1}')
  distribution_path="lib/$jar_name"
  expected_dependency_sha=$(awk -F '\t' -v path="$distribution_path" \
    '$2 == path {print $3}' "$distribution_tsv")
  [[ -n "$expected_dependency_sha" && \
     "$jar_sha" == "$expected_dependency_sha" ]] || \
    die "dependency is absent from or differs from distribution inventory: $jar_name"
  if [[ "$jar_sha" == "$core2_sha" ]]; then
    continue
  fi
  if [[ "$jar_sha" == "$nano_sha" ]]; then
    [[ "$distribution_path" == "lib/nano-impl-0.1.325.jar" ]] || \
      die "licensed Nano archive content appeared under an unexpected name"
    ((nano_substitutions += 1))
    continue
  fi
  [[ "$jar_sha" != "$peer_sha" && "$jar_sha" != "$transactor_sha" ]] || \
    die "licensed implementation archive content appeared under lib/: $jar_name"
  ln -s "$jar" "$library_dir/$jar_name"
  ((linked_dependencies += 1))
done
[[ "$nano_substitutions" -eq 1 ]] || \
  die "expected exactly one licensed Nano dependency substitution"
cp -- "$sanitized_nano_jar" "$runtime_sanitized_nano_jar"
[[ $(sha256sum "$runtime_sanitized_nano_jar" | awk '{print $1}') == \
   "$expected_sanitized_nano_sha" ]] || \
  die "runtime Nano derivative copy differs from the verified derivative"
((linked_dependencies += 1))
expected_candidate_dependencies=$(awk -F '\t' \
  'NR > 1 && $2 ~ /^lib\/.*\.jar$/ {n++} END {print n - 1}' \
  "$distribution_tsv")
[[ "$linked_dependencies" -eq "$expected_candidate_dependencies" ]] || \
  die "candidate dependency count is $linked_dependencies; expected $expected_candidate_dependencies"

write_immutable_state() {
  local destination=$1
  shift
  printf 'root\ttype\tmode\trelative_path\tlink_target\tresolved_target\tsha256\n' \
    >"$destination"
  while [[ $# -gt 0 ]]; do
    local root_label=$1
    local root_path=$2
    shift 2
    while IFS= read -r -d '' entry; do
      local relative_path=${entry#"$root_path"/}
      if [[ -L "$entry" ]]; then
        local link_target resolved_target target_sha
        link_target=$(readlink "$entry")
        resolved_target=$(readlink -f "$entry")
        [[ -n "$resolved_target" && -f "$resolved_target" ]] || \
          die "immutable input contains a broken/non-file symlink: $entry"
        target_sha=$(sha256sum "$resolved_target" | awk '{print $1}')
        printf '%s\tl\t%s\t%s\t%s\t%s\t%s\n' "$root_label" \
          "$(stat -c '%a' "$entry")" "$relative_path" "$link_target" \
          "$resolved_target" "$target_sha" >>"$destination"
      elif [[ -f "$entry" ]]; then
        printf '%s\tf\t%s\t%s\t\t\t%s\n' "$root_label" \
          "$(stat -c '%a' "$entry")" "$relative_path" \
          "$(sha256sum "$entry" | awk '{print $1}')" >>"$destination"
      elif [[ -d "$entry" ]]; then
        printf '%s\td\t%s\t%s\t\t\t\n' "$root_label" \
          "$(stat -c '%a' "$entry")" "$relative_path" >>"$destination"
      else
        die "immutable input contains a special entry: $entry"
      fi
    done < <(find "$root_path" -mindepth 1 -print0 | sort -z)
  done
}

immutable_pre="$work_root/immutability/pre-runtime.tsv"
write_immutable_state "$immutable_pre" \
  candidate-source "$source_root" \
  handwritten-classes "$classes_dir" \
  licensed-peer-reference-resources "$project_dir/resources" \
  candidate-peer-resources "$candidate_resource_dir" \
  compile-stubs "$stub_dir" \
  dependencies "$library_dir" \
  sanitized-nano-evidence "$sanitized_nano_root"

export DATOMIC_REV_SOURCE_CLASSPATH="$source_root:$classes_dir:$candidate_resource_dir:$stub_dir:$library_dir/*"
export DATOMIC_REV_LOG_ROOT="$log_dir"
export DATOMIC_REV_JAVA_BIN="$java_bin"
export DATOMIC_REV_NAMESPACE_TIMEOUT_SECONDS="$namespace_timeout_seconds"

if ! awk -F '\t' 'NR > 1 {print $1}' "$namespace_index" \
  | xargs -P "$jobs" -n 1 /usr/bin/bash -p \
      "$script_dir/require-one-namespace-aligned.sh"; then
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
if ! /usr/bin/timeout --signal=TERM --kill-after=15s \
  "${setup_timeout_seconds}s" \
  "$java_bin" -XX:+PerfDisableSharedMem \
  -Djava.io.tmpdir="$behavior_java_tmp" \
  '-Dclojure.compiler.elide-meta=[:doc :file :line]' \
  -cp "$DATOMIC_REV_SOURCE_CLASSPATH" clojure.main \
  "$script_dir/validate_recovered_behaviors.clj" \
  >"$behavior_out" 2>"$behavior_err"; then
  echo "recovered behavior regression failed" >&2
  cat "$behavior_out" >&2
  cat "$behavior_err" >&2
  exit 1
fi
require_empty_java_tmp "$behavior_java_tmp" recovered-behaviors
cat "$behavior_out"

immutable_post="$work_root/immutability/post-runtime.tsv"
write_immutable_state "$immutable_post" \
  candidate-source "$source_root" \
  handwritten-classes "$classes_dir" \
  licensed-peer-reference-resources "$project_dir/resources" \
  candidate-peer-resources "$candidate_resource_dir" \
  compile-stubs "$stub_dir" \
  dependencies "$library_dir" \
  sanitized-nano-evidence "$sanitized_nano_root"
if ! cmp -s "$immutable_pre" "$immutable_post"; then
  diff -u "$immutable_pre" "$immutable_post" \
    >"$work_root/immutability/runtime-mutation.diff" || true
  die "candidate source/build/dependency inputs changed during runtime validation"
fi
sha256sum "$immutable_pre" "$immutable_post" \
  >"$work_root/immutability/verified.sha256"

[[ -z $(find "$java_tmp_root" -mindepth 1 ! -type d -print -quit) ]] || \
  die "isolated Java temp root retained a file after source validation"

echo "source-only validation passed for all 142 namespaces"
echo "licensed Peer, Transactor, core2, and keystore-bearing Nano originals were excluded"
echo "exactly one verified sanitized Nano derivative replaced the original dependency"
echo "the two vendor keystores were removed from the candidate Peer resource root"
echo "all per-probe java.io.tmpdir roots were isolated and empty after use"
echo "Hot Rod classes were compile-only stubs; do not use them at runtime"
echo "logs: $log_dir"
