#!/usr/bin/env bash
set -euo pipefail

export LC_ALL=C
export TZ=UTC

script_dir=$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd)
transactor_dir=$(cd -- "$script_dir/.." && pwd)
project_dir=$(cd -- "$transactor_dir/.." && pwd)
datomic_home=${1:-"$project_dir/../../datomic/datomic-pro-1.0.7277"}
output_dir=${2:-/tmp/datomic-exact-datomic-source-validation}
transactor_jar="$datomic_home/datomic-transactor-pro-1.0.7277.jar"
compile_driver="$script_dir/compile_aligned_clojure_source.clj"
surface_emitter="$project_dir/scripts/emit_namespace_surface.clj"
expected_transactor_sha=d90819b57e2138085ddc26c93314f953c2cd776d1d1aa8b43a6bec1e475d3692
expected_ownership_sha=04e98999d62b6b19b0a10ae7cce385bf45dc7f9fa0bdc18c3020ac46a00ab0fe
expected_source_manifest_sha=d6535ec1315e1b306c871e6334e107561ce597815c2fe8321b18a94c1fcfc19e

for command_name in awk cmp diff find grep java mkdir sed sha256sum sort tail \
    unzip wc; do
  command -v "$command_name" >/dev/null || {
    echo "missing required command: $command_name" >&2
    exit 1
  }
done

for required_path in "$transactor_jar" "$compile_driver" "$surface_emitter"; do
  [[ -f "$required_path" && ! -L "$required_path" ]] || {
    echo "missing or symbolic exact-source input: $required_path" >&2
    exit 1
  }
done

actual_transactor_sha=$(sha256sum "$transactor_jar" | awk '{print $1}')
[[ "$actual_transactor_sha" == "$expected_transactor_sha" ]] || {
  echo "unexpected Transactor JAR SHA-256: $actual_transactor_sha" >&2
  exit 1
}

if [[ -e "$output_dir" ]] && \
   [[ -n "$(find "$output_dir" -mindepth 1 -print -quit 2>/dev/null)" ]]; then
  echo "refusing to overwrite non-empty output directory: $output_dir" >&2
  exit 1
fi
[[ ! -L "$output_dir" ]] || {
  echo "output directory may not be a symbolic link: $output_dir" >&2
  exit 1
}
mkdir -p "$output_dir"
output_abs=$(cd -- "$output_dir" && pwd)

if [[ -x /tmp/amazon-corretto-11.0.22.7.1/bin/java ]]; then
  java11=/tmp/amazon-corretto-11.0.22.7.1/bin/java
elif [[ -n "${JAVA_HOME_11_CORRETTO:-}" && \
        -x "$JAVA_HOME_11_CORRETTO/bin/java" ]]; then
  java11="$JAVA_HOME_11_CORRETTO/bin/java"
else
  echo "Amazon Corretto 11.0.22.7.1 is required" >&2
  exit 1
fi
java_version=$($java11 -version 2>&1 | sed -n '1p')
[[ "$java_version" == *"11.0.22"* ]] || {
  echo "exact Corretto Java 11.0.22 required; found: $java_version" >&2
  exit 1
}

clojure_jar="$datomic_home/lib/clojure-1.11.4.jar"
spec_jar="$datomic_home/lib/spec.alpha-0.3.218.jar"
core_specs_jar="$datomic_home/lib/core.specs.alpha-0.2.62.jar"
for dependency in "$clojure_jar" "$spec_jar" "$core_specs_jar"; do
  [[ -f "$dependency" && ! -L "$dependency" ]] || {
    echo "missing exact-source compile dependency: $dependency" >&2
    exit 1
  }
done

# The hash-bound embedded POM is also the evidence for the metadata-elision
# setting reproduced by the compile driver.
unzip -p "$transactor_jar" \
  META-INF/maven/com.datomic/datomic-transactor-pro/pom.xml \
  | grep -F "clojure.compiler.elide-meta='[:doc :file :line]'" \
    >/dev/null || {
    echo "Transactor POM no longer records the expected Clojure compiler options" >&2
    exit 1
  }

recovery_dir="$output_abs/recovery"
"$script_dir/recover-bundled-sources.sh" \
  "$datomic_home" "$recovery_dir" datomic \
  > "$output_abs/recovery.log"

[[ "$(sha256sum "$recovery_dir/ownership.tsv" | awk '{print $1}')" == \
    "$expected_ownership_sha" ]] || {
  echo "exact Datomic dependency-source ownership changed" >&2
  exit 1
}
[[ "$(sha256sum "$recovery_dir/source-manifest.sha256" | awk '{print $1}')" == \
    "$expected_source_manifest_sha" ]] || {
  echo "exact Datomic dependency-source manifest changed" >&2
  exit 1
}

compile_cp="$recovery_dir/source:$clojure_jar:$spec_jar:$core_specs_jar"
compile_namespace() {
  local classes_root=$1
  local namespace_name=$2
  local target_id=$3
  mkdir -p "$classes_root"
  "$java11" -XX:+PerfDisableSharedMem -cp "$compile_cp" clojure.main \
    "$compile_driver" "$namespace_name" "$target_id" "$classes_root"
}

for build_name in classes-a classes-b; do
  classes_root="$output_abs/$build_name"
  compile_namespace "$classes_root" datomic.query.support 15244 \
    > "$output_abs/$build_name-query.out" \
    2> "$output_abs/$build_name-query.err"
  compile_namespace "$classes_root" datomic.specs 23980 \
    > "$output_abs/$build_name-specs.out" \
    2> "$output_abs/$build_name-specs.err"
done

diff -qr "$output_abs/classes-a" "$output_abs/classes-b" \
  > "$output_abs/independent-class-tree.diff" || {
    echo "independent exact-source AOT builds differ" >&2
    exit 1
  }

find "$output_abs/classes-a" -type f -name '*.class' -printf '%P\n' \
  | sort > "$output_abs/candidate-class-list.txt"
unzip -Z1 "$transactor_jar" \
  | awk '
      /^datomic\/query\/support(__init|\$).*\.class$/ ||
      /^datomic\/query\/support\/proxy\$.*\.class$/ ||
      /^datomic\/specs(__init|\$).*\.class$/ {print}
    ' \
  | sort > "$output_abs/original-class-list.txt"
cmp "$output_abs/candidate-class-list.txt" \
  "$output_abs/original-class-list.txt" || {
    echo "exact-source AOT class closure differs from the Transactor" >&2
    exit 1
  }

class_count=$(wc -l < "$output_abs/candidate-class-list.txt")
query_class_count=$(awk '/^datomic\/query\/support/ {n++} END {print n+0}' \
  "$output_abs/candidate-class-list.txt")
specs_class_count=$(awk '/^datomic\/specs/ {n++} END {print n+0}' \
  "$output_abs/candidate-class-list.txt")
[[ "$class_count" -eq 36 && "$query_class_count" -eq 32 && \
   "$specs_class_count" -eq 4 ]] || {
  echo "unexpected exact-source AOT closure: total=$class_count query=$query_class_count specs=$specs_class_count" >&2
  exit 1
}

relation="$output_abs/raw-class-relation.tsv"
printf 'class\tcandidate_sha256\toriginal_sha256\tstatus\n' > "$relation"
raw_match_count=0
while IFS= read -r class_entry; do
  candidate_sha=$(sha256sum "$output_abs/classes-a/$class_entry" \
    | awk '{print $1}')
  original_sha=$(unzip -p "$transactor_jar" "$class_entry" | sha256sum \
    | awk '{print $1}')
  if [[ "$candidate_sha" == "$original_sha" ]]; then
    status=MATCH
    raw_match_count=$((raw_match_count + 1))
  else
    status=DIFFER
  fi
  printf '%s\t%s\t%s\t%s\n' \
    "$class_entry" "$candidate_sha" "$original_sha" "$status" \
    >> "$relation"
done < "$output_abs/candidate-class-list.txt"
[[ "$raw_match_count" -eq 36 ]] || {
  echo "only $raw_match_count of 36 exact-source AOT classes match raw bytes" >&2
  exit 1
}

mkdir -p "$output_abs/surfaces/original" "$output_abs/surfaces/candidate"
original_cp="$transactor_jar:$datomic_home/lib/*"
candidate_cp="$recovery_dir/source:$clojure_jar:$spec_jar:$core_specs_jar"
surface_relation="$output_abs/surface-relation.tsv"
printf 'namespace\toriginal_sha256\tcandidate_sha256\tstatus\n' \
  > "$surface_relation"
surface_match_count=0
for namespace_name in datomic.query.support datomic.specs; do
  "$java11" -XX:+PerfDisableSharedMem -cp "$original_cp" clojure.main \
    "$surface_emitter" "$namespace_name" \
    > "$output_abs/surfaces/original/$namespace_name.out" \
    2> "$output_abs/surfaces/original/$namespace_name.err"
  "$java11" -XX:+PerfDisableSharedMem -cp "$candidate_cp" clojure.main \
    "$surface_emitter" "$namespace_name" \
    > "$output_abs/surfaces/candidate/$namespace_name.out" \
    2> "$output_abs/surfaces/candidate/$namespace_name.err"
  tail -n 1 "$output_abs/surfaces/original/$namespace_name.out" \
    > "$output_abs/surfaces/original/$namespace_name.edn"
  tail -n 1 "$output_abs/surfaces/candidate/$namespace_name.out" \
    > "$output_abs/surfaces/candidate/$namespace_name.edn"
  original_surface_sha=$(sha256sum \
    "$output_abs/surfaces/original/$namespace_name.edn" | awk '{print $1}')
  candidate_surface_sha=$(sha256sum \
    "$output_abs/surfaces/candidate/$namespace_name.edn" | awk '{print $1}')
  if [[ "$original_surface_sha" == "$candidate_surface_sha" ]]; then
    status=MATCH
    surface_match_count=$((surface_match_count + 1))
  else
    status=DIFFER
  fi
  printf '%s\t%s\t%s\t%s\n' "$namespace_name" \
    "$original_surface_sha" "$candidate_surface_sha" "$status" \
    >> "$surface_relation"
done
[[ "$surface_match_count" -eq 2 ]] || {
  echo "exact-source namespace surface parity failed" >&2
  exit 1
}

summary="$output_abs/summary.tsv"
{
  printf 'metric\tvalue\n'
  printf 'input.transactor.sha256\t%s\n' "$expected_transactor_sha"
  printf 'source.namespaces\t2\n'
  printf 'source.ownership.sha256\t%s\n' "$expected_ownership_sha"
  printf 'source.manifest.sha256\t%s\n' "$expected_source_manifest_sha"
  printf 'compiler.runtime\t%s\n' "$java_version"
  printf 'compiler.options\t{:elide-meta [:doc :file :line]}\n'
  printf 'compiler.id.query_support\t15244\n'
  printf 'compiler.id.specs\t23980\n'
  printf 'independent.builds\t2\n'
  printf 'independent.class_trees.identical\ttrue\n'
  printf 'classes.total\t%s\n' "$class_count"
  printf 'classes.query_support\t%s\n' "$query_class_count"
  printf 'classes.specs\t%s\n' "$specs_class_count"
  printf 'classes.raw_byte_matches\t%s\n' "$raw_match_count"
  printf 'namespace.surface_matches\t%s\n' "$surface_match_count"
  printf 'candidate.compile.original_implementation_jars\tnone\n'
  printf 'candidate.surface.original_implementation_jars\tnone\n'
  printf 'original.oracle.class_loading\tseparate surface process only\n'
  printf 'original.oracle.class_bytes\tstreamed ZIP comparison only\n'
} > "$summary"

inputs_manifest="$output_abs/inputs.sha256"
{
  sha256sum "$compile_driver" "$surface_emitter" \
    "$script_dir/recover-bundled-sources.sh" \
    "$script_dir/validate_bundled_sources.clj" \
    "$transactor_dir/tools/MapBundledSources.java"
  sha256sum "$clojure_jar" "$spec_jar" "$core_specs_jar"
} > "$inputs_manifest"

evidence_manifest="$output_abs/evidence.sha256"
(
  cd "$output_abs"
  find . -type f ! -name evidence.sha256 -printf '%P\n' \
    | sort \
    | while IFS= read -r evidence_file; do
        sha256sum "$evidence_file"
      done
) > "$evidence_manifest"
(cd "$output_abs" && sha256sum -c evidence.sha256 >/dev/null)

echo "exact Datomic dependency-source AOT validation passed"
echo "2 exact sources reproduce all 36 embedded classes byte for byte"
echo "namespace surfaces match: 2/2"
echo "summary: $summary"
echo "evidence: $evidence_manifest"
