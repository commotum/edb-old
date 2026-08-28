#!/usr/bin/env bash

set -euo pipefail

export LC_ALL=C
export TZ=UTC

script_dir=$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd)
project_dir=$(cd -- "$script_dir/.." && pwd)

if [[ $# -lt 1 || $# -gt 2 ]]; then
  echo "usage: $0 ARTIFACT_JAR [DATOMIC_HOME]" >&2
  exit 2
fi

artifact_jar=$1
datomic_home=${2:-"$project_dir/../../datomic/datomic-pro-1.0.7277"}
namespace_index="$project_dir/reports/source-index/namespaces.tsv"
java_source_list="$project_dir/reports/handwritten-java-sources.txt"
java_class_list="$project_dir/reports/source-index/unmapped-classes.tsv"
resource_list="$project_dir/reports/peer-resource-paths.txt"

for command_name in awk cmp find grep java sed sha256sum sort uniq unzip zipinfo; do
  command -v "$command_name" >/dev/null || {
    echo "missing required command: $command_name" >&2
    exit 1
  }
done

[[ -f "$artifact_jar" && -d "$datomic_home/lib" ]] || {
  echo "missing artifact JAR or Datomic dependency directory" >&2
  exit 1
}

work_root=$(mktemp -d -t datomic-artifact-inspection.XXXXXXXX)
cleanup() {
  rm -rf -- "$work_root"
}
trap cleanup EXIT

actual_entries="$work_root/actual-entries.txt"
expected_entries="$work_root/expected-entries.txt"
zipinfo -1 "$artifact_jar" >"$actual_entries"

if awk '/\/$/ {found=1} END {exit !found}' "$actual_entries"; then
  echo "artifact contains directory entries; expected files only" >&2
  exit 1
fi
if sort "$actual_entries" | uniq -d | grep -q .; then
  echo "artifact contains duplicate entry names" >&2
  exit 1
fi

{
  awk -F '\t' 'NR > 1 {print $2}' "$namespace_index"
  awk 'NR > 1 {print $1}' "$java_class_list"
  awk 'NF && $1 !~ /^#/ {print $1}' "$resource_list"
  printf '%s\n' \
    META-INF/datomic-rev/build.properties \
    META-INF/datomic-rev/inputs/build-tools.tsv \
    META-INF/datomic-rev/inputs/clojure-sources.tsv \
    META-INF/datomic-rev/inputs/dependencies.tsv \
    META-INF/datomic-rev/inputs/java-sources.tsv \
    META-INF/datomic-rev/inputs/resources.tsv
} | sort >"$expected_entries"
sort "$actual_entries" >"$work_root/sorted-actual-entries.txt"
if ! cmp -s "$expected_entries" "$work_root/sorted-actual-entries.txt"; then
  echo "artifact entry set differs from the canonical 205-entry boundary" >&2
  diff -u "$expected_entries" "$work_root/sorted-actual-entries.txt" >&2 || true
  exit 1
fi

actual_entry_count=$(wc -l <"$actual_entries")
[[ "$actual_entry_count" -eq 205 ]] || {
  echo "expected 205 artifact entries, found $actual_entry_count" >&2
  exit 1
}

recompute_manifest() {
  local kind=$1
  local output=$2
  local path source_file dependency dependency_name
  printf 'sha256\tpath\n' >"$output"
  case "$kind" in
    clojure-sources)
      while IFS=$'\t' read -r namespace_name path _; do
        [[ "$namespace_name" == namespace ]] && continue
        source_file="$project_dir/src-clj/$path"
        printf '%s\t%s\n' \
          "$(sha256sum "$source_file" | awk '{print $1}')" \
          "src-clj/$path" >>"$output"
      done <"$namespace_index"
      ;;
    java-sources)
      while IFS= read -r path; do
        [[ -z "$path" || "$path" == \#* ]] && continue
        source_file="$project_dir/src-java/$path"
        printf '%s\t%s\n' \
          "$(sha256sum "$source_file" | awk '{print $1}')" \
          "src-java/$path" >>"$output"
      done <"$java_source_list"
      ;;
    resources)
      while IFS= read -r path; do
        [[ -z "$path" || "$path" == \#* ]] && continue
        source_file="$project_dir/resources/$path"
        printf '%s\t%s\n' \
          "$(sha256sum "$source_file" | awk '{print $1}')" \
          "resources/$path" >>"$output"
      done <"$resource_list"
      ;;
    dependencies)
      while IFS= read -r dependency; do
        dependency_name=${dependency##*/}
        case "$dependency_name" in
          peer-*.jar|core2-*.jar) continue ;;
        esac
        printf '%s\tlib/%s\n' \
          "$(sha256sum "$dependency" | awk '{print $1}')" \
          "$dependency_name" >>"$output"
      done < <(find "$datomic_home/lib" -maxdepth 1 -type f -name '*.jar' | sort)
      ;;
    build-tools)
      for path in \
        reports/handwritten-java-sources.txt \
        reports/peer-resource-paths.txt \
        reports/source-index/namespaces.tsv \
        reports/source-index/unmapped-classes.tsv \
        tools/bytecode-inventory/baseline/jars.tsv \
        scripts/CompileSources.java \
        scripts/DeterministicJar.java \
        scripts/build-source-artifact.sh; do
        printf '%s\t%s\n' \
          "$(sha256sum "$project_dir/$path" | awk '{print $1}')" \
          "$path" >>"$output"
      done
      ;;
    *)
      echo "unknown input manifest: $kind" >&2
      exit 2
      ;;
  esac
}

for manifest_name in build-tools clojure-sources dependencies java-sources resources; do
  expected_manifest="$work_root/$manifest_name.expected.tsv"
  actual_manifest="$work_root/$manifest_name.actual.tsv"
  recompute_manifest "$manifest_name" "$expected_manifest"
  unzip -p "$artifact_jar" \
    "META-INF/datomic-rev/inputs/$manifest_name.tsv" >"$actual_manifest"
  if ! cmp -s "$expected_manifest" "$actual_manifest"; then
    echo "artifact input manifest differs from current inputs: $manifest_name" >&2
    diff -u "$expected_manifest" "$actual_manifest" >&2 || true
    exit 1
  fi
done

build_properties="$work_root/build.properties"
unzip -p "$artifact_jar" META-INF/datomic-rev/build.properties >"$build_properties"
for required_property in \
  'format.version=1' \
  'artifact.kind=clojure-source-plus-handwritten-java-classes' \
  'clojure.namespace.count=142' \
  'handwritten.java.source.count=43' \
  'handwritten.java.class.count=47' \
  'peer.resource.count=10' \
  'dependency.jar.count=532' \
  'dependency.classpath.order=inputs/dependencies.tsv' \
  'original.peer.aot.on.build.classpath=false' \
  'original.core2.aot.on.build.classpath=false' \
  'clojure.aot.classes.packaged=false'; do
  grep -Fqx "$required_property" "$build_properties" || {
    echo "artifact provenance is missing: $required_property" >&2
    exit 1
  }
done

for manifest_name in build-tools clojure-sources dependencies java-sources resources; do
  manifest_sha=$(sha256sum "$work_root/$manifest_name.actual.tsv" | awk '{print $1}')
  grep -Fqx "inputs.$manifest_name.sha256=$manifest_sha" "$build_properties" || {
    echo "artifact provenance hash mismatch: $manifest_name" >&2
    exit 1
  }
done

while IFS=$'\t' read -r namespace_name source_path _; do
  [[ "$namespace_name" == namespace ]] && continue
  cmp -s <(unzip -p "$artifact_jar" "$source_path") \
         "$project_dir/src-clj/$source_path" || {
    echo "packaged Clojure source differs: $source_path" >&2
    exit 1
  }
done <"$namespace_index"

while IFS= read -r resource_path; do
  [[ -z "$resource_path" || "$resource_path" == \#* ]] && continue
  cmp -s <(unzip -p "$artifact_jar" "$resource_path") \
         "$project_dir/resources/$resource_path" || {
    echo "packaged resource differs: $resource_path" >&2
    exit 1
  }
done <"$resource_list"

artifact_sha=$(sha256sum "$artifact_jar" | awk '{print $1}')
echo "source artifact inspection passed"
echo "SHA-256: $artifact_sha"
echo "entries: 205 (142 Clojure sources, 47 handwritten Java classes, 10 peer resources, 6 provenance records)"
echo "original peer/core2 AOT class entries: 0"
echo "all material input manifests match the current workspace and dependency distribution"
