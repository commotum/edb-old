#!/usr/bin/env bash

set -euo pipefail

export LC_ALL=C
export TZ=UTC
umask 022

script_dir=$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd)
project_dir=$(cd -- "$script_dir/.." && pwd)
datomic_home=${1:-"$project_dir/../../datomic/datomic-pro-1.0.7277"}
output_root=${2:-"$project_dir/build/source-artifact"}
artifact_name=datomic-rev-peer-1.0.7277-source.jar
artifact_path="$output_root/$artifact_name"
namespace_index="$project_dir/reports/source-index/namespaces.tsv"
java_source_list="$project_dir/reports/handwritten-java-sources.txt"
java_class_list="$project_dir/reports/source-index/unmapped-classes.tsv"
resource_list="$project_dir/reports/peer-resource-paths.txt"
dependency_baseline="$project_dir/tools/bytecode-inventory/baseline/jars.tsv"
expected_java_version=21.0.12
expected_java_runtime_version=21.0.12+8-1-24.04-Ubuntu
expected_java_vendor=Ubuntu
forbidden_peer_sha=cb55c9d01e155f9965e3332cd8ef70be42cdefacb50c072076e9ecf22b9a23ba
forbidden_core2_sha=81fdf81586c7be1a4b61655b568348d12db7892cb4562d0db7529bbc7af8a94b

for command_name in awk cmp cp find grep java mkdir mktemp mv rm sed sha256sum sort wc; do
  command -v "$command_name" >/dev/null || {
    echo "missing required command: $command_name" >&2
    exit 1
  }
done

[[ -d "$datomic_home/lib" ]] || {
  echo "missing Datomic distribution libraries: $datomic_home" >&2
  exit 1
}
for required_file in \
  "$namespace_index" \
  "$java_source_list" \
  "$java_class_list" \
  "$resource_list" \
  "$dependency_baseline" \
  "$script_dir/CompileSources.java" \
  "$script_dir/DeterministicJar.java"; do
  [[ -f "$required_file" ]] || {
    echo "missing build input: $required_file" >&2
    exit 1
  }
done

java_settings=$(java -XshowSettings:properties -version 2>&1)
java_version=$(awk -F' = ' '/^[[:space:]]*java.version = / {print $2; exit}' \
  <<<"$java_settings")
java_runtime_version=$(awk -F' = ' '/^[[:space:]]*java.runtime.version = / {print $2; exit}' \
  <<<"$java_settings")
java_vendor=$(awk -F' = ' '/^[[:space:]]*java.vendor = / {print $2; exit}' \
  <<<"$java_settings")
[[ "$java_version" == "$expected_java_version" && \
   "$java_runtime_version" == "$expected_java_runtime_version" && \
   "$java_vendor" == "$expected_java_vendor" ]] || {
  echo "canonical build requires Java $expected_java_runtime_version from $expected_java_vendor" >&2
  echo "found Java $java_runtime_version from $java_vendor" >&2
  exit 1
}

if [[ -e "$output_root" ]] && \
   [[ -n "$(find "$output_root" -mindepth 1 -print -quit 2>/dev/null)" ]]; then
  echo "refusing to overwrite non-empty output directory: $output_root" >&2
  exit 1
fi
mkdir -p "$output_root"

work_root=$(mktemp -d -t datomic-source-artifact.XXXXXXXX)
cleanup() {
  rm -rf -- "$work_root"
}
trap cleanup EXIT

classes_dir="$work_root/classes"
staging_dir="$work_root/staging"
input_dir="$work_root/inputs"
mkdir -p "$classes_dir" "$staging_dir" "$input_dir"

dependency_manifest="$input_dir/dependencies.tsv"
printf 'sha256\tpath\n' >"$dependency_manifest"
dependency_classpath=
while IFS= read -r dependency; do
  dependency_name=${dependency##*/}
  case "$dependency_name" in
    peer-*.jar|core2-*.jar)
      continue
      ;;
  esac
  dependency_sha=$(sha256sum "$dependency" | awk '{print $1}')
  if [[ "$dependency_sha" == "$forbidden_peer_sha" || \
        "$dependency_sha" == "$forbidden_core2_sha" ]]; then
    echo "forbidden peer/core2 AOT bytes found in build dependency: $dependency" >&2
    exit 1
  fi
  printf '%s\tlib/%s\n' \
    "$dependency_sha" \
    "$dependency_name" >>"$dependency_manifest"
  if [[ -z "$dependency_classpath" ]]; then
    dependency_classpath=$dependency
  else
    dependency_classpath="$dependency_classpath:$dependency"
  fi
done < <(find "$datomic_home/lib" -maxdepth 1 -type f -name '*.jar' | sort)

dependency_count=$(awk 'NR > 1 {n++} END {print n+0}' "$dependency_manifest")
[[ "$dependency_count" -eq 532 ]] || {
  echo "expected 532 peer/core2-free dependency JARs, found $dependency_count" >&2
  exit 1
}
expected_dependency_manifest="$work_root/expected-dependencies.tsv"
{
  printf 'sha256\tpath\n'
  awk -F '\t' 'NR > 1 && $1 != "peer-1.0.7277.jar" && $1 !~ /^core2-/ {
    print $3 "\tlib/" $1
  }' "$dependency_baseline"
} >"$expected_dependency_manifest"
if ! cmp -s "$expected_dependency_manifest" "$dependency_manifest"; then
  echo "live dependency name/hash set differs from the checked bytecode-inventory baseline" >&2
  diff -u "$expected_dependency_manifest" "$dependency_manifest" >&2 || true
  exit 1
fi

java -XX:-UsePerfData "$script_dir/CompileSources.java" \
  "$project_dir/src-java" \
  "$classes_dir" \
  "$dependency_classpath" \
  "$java_source_list" \
  >"$output_root/java-compile.log" 2>&1 || {
  sed -n '1,240p' "$output_root/java-compile.log" >&2
  exit 1
}

class_count=$(find "$classes_dir" -type f -name '*.class' | wc -l)
[[ "$class_count" -eq 47 ]] || {
  echo "expected 47 compiled handwritten classes, found $class_count" >&2
  exit 1
}
find "$classes_dir" -type f -name '*.class' -printf '%P\n' | sort \
  >"$work_root/actual-java-classes.txt"
awk 'NR > 1 {print $1}' "$java_class_list" | sort \
  >"$work_root/expected-java-classes.txt"
cmp -s "$work_root/expected-java-classes.txt" "$work_root/actual-java-classes.txt" || {
  echo "compiled handwritten class set differs from the 47-class manifest" >&2
  exit 1
}
cp -a "$classes_dir/." "$staging_dir/"

clojure_manifest="$input_dir/clojure-sources.tsv"
printf 'sha256\tpath\n' >"$clojure_manifest"
while IFS=$'\t' read -r namespace_name source_path _; do
  [[ "$namespace_name" == namespace ]] && continue
  source_file="$project_dir/src-clj/$source_path"
  [[ -f "$source_file" ]] || {
    echo "missing recovered Clojure source: $source_path" >&2
    exit 1
  }
  mkdir -p "$staging_dir/${source_path%/*}"
  cp "$source_file" "$staging_dir/$source_path"
  printf '%s\t%s\n' \
    "$(sha256sum "$source_file" | awk '{print $1}')" \
    "src-clj/$source_path" >>"$clojure_manifest"
done <"$namespace_index"

clojure_count=$(awk 'NR > 1 {n++} END {print n+0}' "$clojure_manifest")
[[ "$clojure_count" -eq 142 ]] || {
  echo "expected 142 recovered Clojure namespaces, found $clojure_count" >&2
  exit 1
}

java_manifest="$input_dir/java-sources.tsv"
printf 'sha256\tpath\n' >"$java_manifest"
while IFS= read -r source_path; do
  [[ -z "$source_path" || "$source_path" == \#* ]] && continue
  source_file="$project_dir/src-java/$source_path"
  [[ -f "$source_file" ]] || {
    echo "missing recovered handwritten Java source: $source_path" >&2
    exit 1
  }
  printf '%s\t%s\n' \
    "$(sha256sum "$source_file" | awk '{print $1}')" \
    "src-java/$source_path" >>"$java_manifest"
done <"$java_source_list"

java_count=$(awk 'NR > 1 {n++} END {print n+0}' "$java_manifest")
[[ "$java_count" -eq 43 ]] || {
  echo "expected 43 handwritten Java sources, found $java_count" >&2
  exit 1
}

resource_manifest="$input_dir/resources.tsv"
printf 'sha256\tpath\n' >"$resource_manifest"
while IFS= read -r resource_path; do
  [[ -z "$resource_path" || "$resource_path" == \#* ]] && continue
  resource_file="$project_dir/resources/$resource_path"
  [[ -f "$resource_file" ]] || {
    echo "missing recovered resource: $resource_path" >&2
    exit 1
  }
  resource_parent=${resource_path%/*}
  if [[ "$resource_parent" != "$resource_path" ]]; then
    mkdir -p "$staging_dir/$resource_parent"
  fi
  cp "$resource_file" "$staging_dir/$resource_path"
  printf '%s\t%s\n' \
    "$(sha256sum "$resource_file" | awk '{print $1}')" \
    "resources/$resource_path" >>"$resource_manifest"
done <"$resource_list"

resource_count=$(awk 'NR > 1 {n++} END {print n+0}' "$resource_manifest")
[[ "$resource_count" -eq 10 ]] || {
  echo "expected 10 recovered peer resources, found $resource_count" >&2
  exit 1
}

tool_manifest="$input_dir/build-tools.tsv"
printf 'sha256\tpath\n' >"$tool_manifest"
for tool_path in \
  reports/handwritten-java-sources.txt \
  reports/peer-resource-paths.txt \
  reports/source-index/namespaces.tsv \
        reports/source-index/unmapped-classes.tsv \
        tools/bytecode-inventory/baseline/jars.tsv \
        scripts/CompileSources.java \
  scripts/DeterministicJar.java \
  scripts/build-source-artifact.sh; do
  printf '%s\t%s\n' \
    "$(sha256sum "$project_dir/$tool_path" | awk '{print $1}')" \
    "$tool_path" >>"$tool_manifest"
done

provenance_dir="$staging_dir/META-INF/datomic-rev"
mkdir -p "$provenance_dir/inputs"
cp "$input_dir"/*.tsv "$provenance_dir/inputs/"

{
  printf 'format.version=1\n'
  printf 'artifact.kind=clojure-source-plus-handwritten-java-classes\n'
  printf 'target.peer.version=1.0.7277\n'
  printf 'clojure.namespace.count=%s\n' "$clojure_count"
  printf 'handwritten.java.source.count=%s\n' "$java_count"
  printf 'handwritten.java.class.count=%s\n' "$class_count"
  printf 'peer.resource.count=%s\n' "$resource_count"
  printf 'dependency.jar.count=%s\n' "$dependency_count"
  printf 'dependency.classpath.order=inputs/dependencies.tsv\n'
  printf 'original.peer.aot.on.build.classpath=false\n'
  printf 'original.core2.aot.on.build.classpath=false\n'
  printf 'clojure.aot.classes.packaged=false\n'
  printf 'java.source.level=11\n'
  printf 'java.target.level=11\n'
  printf 'java.platform.api.release.enforced=false\n'
  printf 'java.platform.api.release.limitation=pinned-runtime-lacks-release-11-symbols\n'
  printf 'java.version=%s\n' "$java_version"
  printf 'java.runtime.version=%s\n' "$java_runtime_version"
  printf 'java.vendor=%s\n' "$java_vendor"
  printf 'archive.entries=stored\n'
  printf 'archive.entry.timestamp=1980-01-01T00:00:00\n'
  for manifest_name in build-tools clojure-sources dependencies java-sources resources; do
    printf 'inputs.%s.sha256=%s\n' \
      "$manifest_name" \
      "$(sha256sum "$input_dir/$manifest_name.tsv" | awk '{print $1}')"
  done
} >"$provenance_dir/build.properties"

temporary_artifact="$work_root/$artifact_name"
java -XX:-UsePerfData "$script_dir/DeterministicJar.java" "$staging_dir" "$temporary_artifact" \
  >"$output_root/package.log"

artifact_entry_count=$(find "$staging_dir" -type f | wc -l)
[[ "$artifact_entry_count" -eq 205 ]] || {
  echo "expected 205 artifact entries, found $artifact_entry_count" >&2
  exit 1
}
artifact_sha=$(sha256sum "$temporary_artifact" | awk '{print $1}')
mv "$temporary_artifact" "$artifact_path"
cp -a "$input_dir" "$output_root/inputs"
printf '%s  %s\n' "$artifact_sha" "$artifact_name" >"$output_root/$artifact_name.sha256"
{
  printf 'artifact.name=%s\n' "$artifact_name"
  printf 'artifact.sha256=%s\n' "$artifact_sha"
  printf 'artifact.entry.count=%s\n' "$artifact_entry_count"
  printf 'clojure.namespace.count=%s\n' "$clojure_count"
  printf 'handwritten.java.source.count=%s\n' "$java_count"
  printf 'handwritten.java.class.count=%s\n' "$class_count"
  printf 'peer.resource.count=%s\n' "$resource_count"
  printf 'dependency.jar.count=%s\n' "$dependency_count"
  printf 'dependency.classpath.order=inputs/dependencies.tsv\n'
  printf 'original.peer.aot.on.build.classpath=false\n'
  printf 'original.core2.aot.on.build.classpath=false\n'
  printf 'clojure.aot.classes.packaged=false\n'
  printf 'java.platform.api.release.enforced=false\n'
} >"$output_root/build-result.properties"

echo "built reproducible recovered-source artifact"
echo "artifact: $artifact_path"
echo "SHA-256: $artifact_sha"
echo "entries: $artifact_entry_count"
echo "build classpath: $dependency_count dependency JARs; peer/core2 AOT excluded"
