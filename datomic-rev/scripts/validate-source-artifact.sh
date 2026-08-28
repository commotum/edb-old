#!/usr/bin/env bash

set -euo pipefail

export LC_ALL=C
export TZ=UTC

script_dir=$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd)
project_dir=$(cd -- "$script_dir/.." && pwd)

if [[ $# -lt 2 || $# -gt 3 ]]; then
  echo "usage: $0 DATOMIC_HOME ARTIFACT_JAR [EMPTY_WORK_ROOT]" >&2
  exit 2
fi

datomic_home=$(cd -- "$1" && pwd)
artifact_jar=$(readlink -f -- "$2")
work_root=${3:-$(mktemp -d -t datomic-artifact-validation.XXXXXXXX)}
peer_jar="$datomic_home/peer-1.0.7277.jar"
namespace_index="$project_dir/reports/source-index/namespaces.tsv"
resource_list="$project_dir/reports/peer-resource-paths.txt"
expected_warning_inventory="$project_dir/reports/stage-4-unresolved-warnings.txt"
expected_peer_sha=cb55c9d01e155f9965e3332cd8ef70be42cdefacb50c072076e9ecf22b9a23ba
expected_parity_sha=228b03dac4a437465937c53dbc7c4917294de408b2311b41a662e5f39eca37be
jobs=${JOBS:-4}

[[ -f "$artifact_jar" && -f "$peer_jar" && -f "$expected_warning_inventory" ]] || {
  echo "missing source artifact, licensed peer oracle, or warning baseline" >&2
  exit 1
}
actual_peer_sha=$(sha256sum "$peer_jar" | awk '{print $1}')
[[ "$actual_peer_sha" == "$expected_peer_sha" ]] || {
  echo "unexpected peer oracle SHA-256: $actual_peer_sha" >&2
  exit 1
}
if [[ -e "$work_root" ]] && \
   [[ -n "$(find "$work_root" -mindepth 1 -print -quit 2>/dev/null)" ]]; then
  echo "refusing to overwrite non-empty validation directory: $work_root" >&2
  exit 1
fi

mkdir -p "$work_root"
inspection_log="$work_root/artifact-inspection.log"
"$script_dir/inspect-source-artifact.sh" "$artifact_jar" "$datomic_home" \
  | tee "$inspection_log"
"$script_dir/validate-peer-resources.sh" "$datomic_home" "$project_dir/resources" \
  | tee "$work_root/peer-resource-oracle.log"

dependency_manifest="$work_root/dependencies.tsv"
unzip -p "$artifact_jar" META-INF/datomic-rev/inputs/dependencies.tsv \
  >"$dependency_manifest"
dependency_classpath=
while IFS=$'\t' read -r expected_sha relative_path; do
  [[ "$expected_sha" == sha256 ]] && continue
  dependency_path="$datomic_home/$relative_path"
  [[ -f "$dependency_path" ]] || {
    echo "missing recorded dependency: $relative_path" >&2
    exit 1
  }
  actual_sha=$(sha256sum "$dependency_path" | awk '{print $1}')
  [[ "$actual_sha" == "$expected_sha" ]] || {
    echo "recorded dependency differs: $relative_path" >&2
    exit 1
  }
  case "${dependency_path##*/}" in
    peer-*.jar|core2-*.jar)
      echo "forbidden peer/core2 AOT dependency in artifact manifest: $relative_path" >&2
      exit 1
      ;;
  esac
  if [[ -z "$dependency_classpath" ]]; then
    dependency_classpath=$dependency_path
  else
    dependency_classpath="$dependency_classpath:$dependency_path"
  fi
done <"$dependency_manifest"

dependency_count=$(awk 'NR > 1 {n++} END {print n+0}' "$dependency_manifest")
[[ "$dependency_count" -eq 532 ]] || {
  echo "expected 532 recorded dependencies, found $dependency_count" >&2
  exit 1
}

stub_dir="$work_root/infinispan-compile-stubs"
mkdir -p "$stub_dir"
java -cp "$dependency_classpath" clojure.main \
  "$project_dir/tools/infinispan-compile-stubs/build_stubs.clj" "$stub_dir" \
  | tee "$work_root/infinispan-stubs.log"

stub_manifest="$work_root/infinispan-stub-classes.tsv"
printf 'sha256\tpath\n' >"$stub_manifest"
while IFS= read -r stub_class; do
  printf '%s\t%s\n' \
    "$(sha256sum "$stub_dir/$stub_class" | awk '{print $1}')" \
    "$stub_class" >>"$stub_manifest"
done < <(find "$stub_dir" -type f -name '*.class' -printf '%P\n' | sort)

candidate_classpath="$artifact_jar:$stub_dir:$dependency_classpath"
candidate_classpath_record="$work_root/candidate-classpath.tsv"
{
  printf 'role\tsha256\tpath\n'
  printf 'recovered-artifact\t%s\t%s\n' \
    "$(sha256sum "$artifact_jar" | awk '{print $1}')" "$artifact_jar"
  printf 'compile-only-stubs\t%s\t%s\n' \
    "$(sha256sum "$stub_manifest" | awk '{print $1}')" "$stub_dir"
  awk -F '\t' -v root="$datomic_home" \
    'NR > 1 {print "dependency" FS $1 FS root "/" $2}' \
    "$dependency_manifest"
} >"$candidate_classpath_record"

java -cp "$candidate_classpath" clojure.main \
  "$script_dir/verify_artifact_origins.clj" \
  "$artifact_jar" "$namespace_index" "$resource_list" \
  "$project_dir/reports/source-index/unmapped-classes.tsv" \
  | tee "$work_root/artifact-origins.log"

namespace_log_root="$work_root/namespace-logs"
mkdir -p "$namespace_log_root"
export DATOMIC_REV_SOURCE_CLASSPATH="$candidate_classpath"
export DATOMIC_REV_LOG_ROOT="$namespace_log_root"

if ! awk -F '\t' 'NR > 1 {print $1}' "$namespace_index" \
  | xargs -P "$jobs" -n 1 "$script_dir/require-one-namespace.sh"; then
  echo "one or more packaged-artifact namespace loads failed; inspect $namespace_log_root" >&2
  grep -h '^FAIL ' "$namespace_log_root"/*.out >&2 || true
  exit 1
fi

namespace_pass_count=$(grep -h '^PASS ' "$namespace_log_root"/*.out | wc -l)
namespace_fail_count=$(grep -h '^FAIL ' "$namespace_log_root"/*.out | wc -l || true)
[[ "$namespace_pass_count" -eq 142 && "$namespace_fail_count" -eq 0 ]] || {
  echo "unexpected namespace results: $namespace_pass_count PASS, $namespace_fail_count FAIL" >&2
  exit 1
}

behavior_out="$work_root/recovered-behaviors.out"
behavior_err="$work_root/recovered-behaviors.err"
if ! java -cp "$candidate_classpath" clojure.main \
  "$script_dir/validate_recovered_behaviors.clj" \
  >"$behavior_out" 2>"$behavior_err"; then
  echo "packaged-artifact recovered behavior regression failed" >&2
  sed -n '1,240p' "$behavior_out" >&2
  sed -n '1,240p' "$behavior_err" >&2
  exit 1
fi
cat "$behavior_out"

warning_inventory="$work_root/unresolved-warnings.txt"
{
  grep -h 'Reflection warning' "$namespace_log_root"/*.err || true
  grep -h 'recur arg for primitive local' "$namespace_log_root"/*.err || true
  grep -h 'Auto-boxing loop arg' "$namespace_log_root"/*.err || true
} | sort -u >"$warning_inventory"
reflection_warning_count=$(grep -c 'Reflection warning' "$warning_inventory" || true)
primitive_recur_warning_count=$(grep -c 'recur arg for primitive local' "$warning_inventory" || true)
autobox_warning_count=$(grep -c 'Auto-boxing loop arg' "$warning_inventory" || true)
[[ "$reflection_warning_count" -eq 150 && \
   "$primitive_recur_warning_count" -eq 6 && \
   "$autobox_warning_count" -eq 1 ]] || {
  echo "artifact warning inventory changed unexpectedly: reflection=$reflection_warning_count primitive-recur=$primitive_recur_warning_count auto-boxing=$autobox_warning_count" >&2
  exit 1
}
if ! cmp -s "$expected_warning_inventory" "$warning_inventory"; then
  echo "artifact unresolved-warning inventory differs from the exact Stage 4 baseline" >&2
  diff -u "$expected_warning_inventory" "$warning_inventory" >&2 || true
  exit 1
fi
warning_inventory_sha=$(sha256sum "$warning_inventory" | awk '{print $1}')

"$script_dir/compare-artifact-java-surfaces.sh" \
  "$datomic_home" "$artifact_jar" "$work_root/java-surfaces" \
  | tee "$work_root/java-surfaces.log"

surface_root="$work_root/namespace-surfaces"
mkdir -p "$surface_root/original" "$surface_root/recovered" "$surface_root/logs"
export DATOMIC_REV_ORIGINAL_CLASSPATH="$peer_jar:$stub_dir:$dependency_classpath"
export DATOMIC_REV_SOURCE_CLASSPATH="$candidate_classpath"
export DATOMIC_REV_SURFACE_ROOT="$surface_root"

if ! awk -F '\t' 'NR > 1 {print $1}' "$namespace_index" \
  | xargs -P "$jobs" -n 1 "$script_dir/compare-one-namespace-surface.sh" \
  | tee "$surface_root/results.txt"; then
  echo "one or more artifact runtime namespace surfaces differ; inspect $surface_root/logs" >&2
  exit 1
fi
surface_pass_count=$(grep -c '^PASS ' "$surface_root/results.txt")
[[ "$surface_pass_count" -eq 142 ]] || {
  echo "expected 142 namespace surface matches, found $surface_pass_count" >&2
  exit 1
}

parity_root="$work_root/end-to-end-parity"
mkdir -p "$parity_root"
run_probe() {
  local label=$1
  local classpath=$2
  local stdout_file="$parity_root/$label.out"
  local stderr_file="$parity_root/$label.err"
  if ! java -cp "$classpath" clojure.main "$script_dir/end_to_end_probe.clj" \
       >"$stdout_file" 2>"$stderr_file"; then
    echo "$label end-to-end probe failed; inspect $stdout_file and $stderr_file" >&2
    return 1
  fi
  local result_count
  result_count=$(awk '/^PARITY-RESULT / {n++} END {print n+0}' "$stdout_file")
  [[ "$result_count" -eq 1 ]] || {
    echo "$label probe emitted $result_count parity results" >&2
    return 1
  }
  awk '/^PARITY-RESULT / {print; exit}' "$stdout_file" \
    >"$parity_root/$label.result"
}

run_probe original "$peer_jar:$stub_dir:$dependency_classpath"
run_probe recovered "$candidate_classpath"
if ! cmp -s "$parity_root/original.result" "$parity_root/recovered.result"; then
  echo "packaged-artifact end-to-end parity mismatch" >&2
  diff -u "$parity_root/original.result" "$parity_root/recovered.result" >&2 || true
  exit 1
fi
parity_sha=$(sha256sum "$parity_root/original.result" | awk '{print $1}')
[[ "$parity_sha" == "$expected_parity_sha" ]] || {
  echo "unexpected canonical parity SHA-256: $parity_sha" >&2
  exit 1
}

artifact_sha=$(sha256sum "$artifact_jar" | awk '{print $1}')
{
  printf 'artifact.sha256=%s\n' "$artifact_sha"
  printf 'peer.oracle.sha256=%s\n' "$actual_peer_sha"
  printf 'candidate.classpath.original.peer.aot=false\n'
  printf 'candidate.classpath.original.core2.aot=false\n'
  printf 'candidate.classpath.dependency.count=%s\n' "$dependency_count"
  printf 'namespace.loads.pass=%s\n' "$namespace_pass_count"
  printf 'namespace.loads.fail=%s\n' "$namespace_fail_count"
  printf 'namespace.surfaces.pass=%s\n' "$surface_pass_count"
  printf 'handwritten.java.classes=47\n'
  printf 'handwritten.java.fields=108\n'
  printf 'handwritten.java.methods=279\n'
  printf 'warnings.reflection.unique=%s\n' "$reflection_warning_count"
  printf 'warnings.primitive-recur.unique=%s\n' "$primitive_recur_warning_count"
  printf 'warnings.auto-boxing.unique=%s\n' "$autobox_warning_count"
  printf 'warnings.inventory.sha256=%s\n' "$warning_inventory_sha"
  printf 'end-to-end.parity.sha256=%s\n' "$parity_sha"
} >"$work_root/validation-summary.properties"

echo "packaged recovered-source artifact validation passed"
echo "artifact SHA-256: $artifact_sha"
echo "candidate boundary: artifact + compile-only Hot Rod stubs + 532 explicitly ordered dependencies"
echo "candidate peer/core2 AOT classes: excluded and origin-audited"
echo "namespace loads: 142 PASS, 0 FAIL"
echo "runtime namespace surfaces: 142 PASS"
echo "handwritten Java surfaces: 47 classes, 108 fields, 279 methods"
echo "in-memory parity SHA-256: $parity_sha"
echo "details: $work_root"
