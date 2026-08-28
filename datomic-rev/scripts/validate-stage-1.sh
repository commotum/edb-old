#!/usr/bin/env bash

set -euo pipefail

export LC_ALL=C
export TZ=UTC

script_dir=$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd)
project_dir=$(cd -- "$script_dir/.." && pwd)

if [[ $# -lt 1 || $# -gt 2 ]]; then
  echo "usage: $0 DATOMIC_HOME [EMPTY_WORK_ROOT]" >&2
  exit 2
fi

datomic_home=$(cd -- "$1" && pwd)
work_root=${2:-$(mktemp -d -t datomic-stage-1.XXXXXXXX)}
artifact_name=datomic-rev-peer-1.0.7277-source.jar

if [[ -e "$work_root" ]] && \
   [[ -n "$(find "$work_root" -mindepth 1 -print -quit 2>/dev/null)" ]]; then
  echo "refusing to overwrite non-empty Stage 1 directory: $work_root" >&2
  exit 1
fi
mkdir -p "$work_root"

inputs_dir="$work_root/inputs"
mkdir -p "$inputs_dir"
validation_harness_manifest="$inputs_dir/stage1-validation-harness.tsv"
printf 'sha256\tpath\n' >"$validation_harness_manifest"
validation_inputs=(
  scripts/CompileSources.java
  scripts/DeterministicJar.java
  scripts/InspectJarMetadata.java
  scripts/build-source-artifact.sh
  scripts/compare-artifact-java-surfaces.sh
  scripts/compare-one-namespace-surface.sh
  scripts/emit_namespace_surface.clj
  scripts/end_to_end_probe.clj
  scripts/inspect-source-artifact.sh
  scripts/require-one-namespace.sh
  scripts/require_namespace.clj
  scripts/validate-peer-resources.sh
  scripts/validate-source-artifact.sh
  scripts/validate-stage-1.sh
  scripts/validate_recovered_behaviors.clj
  scripts/verify_artifact_origins.clj
  reports/handwritten-java-sources.txt
  reports/peer-resource-paths.txt
  reports/source-index/namespaces.tsv
  reports/source-index/unmapped-classes.tsv
  reports/stage-4-unresolved-warnings.txt
  tools/bytecode-inventory/BytecodeInventory.java
  tools/bytecode-inventory/baseline/jars.tsv
  tools/infinispan-compile-stubs/build_stubs.clj
)
for validation_input in "${validation_inputs[@]}"; do
  [[ -f "$project_dir/$validation_input" && ! -L "$project_dir/$validation_input" ]] || {
    echo "missing or symbolic Stage 1 validation input: $validation_input" >&2
    exit 1
  }
  printf '%s\t%s\n' \
    "$(sha256sum "$project_dir/$validation_input" | awk '{print $1}')" \
    "$validation_input" >>"$validation_harness_manifest"
done

build_a="$work_root/build-a"
build_b="$work_root/build-b"
validation_root="$work_root/artifact-validation"

"$script_dir/build-source-artifact.sh" "$datomic_home" "$build_a" \
  | tee "$work_root/build-a.log"
"$script_dir/build-source-artifact.sh" "$datomic_home" "$build_b" \
  | tee "$work_root/build-b.log"

artifact_a="$build_a/$artifact_name"
artifact_b="$build_b/$artifact_name"
cmp -s "$artifact_a" "$artifact_b" || {
  echo "independent Stage 1 artifacts differ" >&2
  exit 1
}
diff -qr "$build_a/inputs" "$build_b/inputs" >"$work_root/input-manifest-diff.txt" || {
  echo "independent Stage 1 input manifests differ" >&2
  sed -n '1,240p' "$work_root/input-manifest-diff.txt" >&2
  exit 1
}
cmp -s "$build_a/build-result.properties" "$build_b/build-result.properties" || {
  echo "independent Stage 1 build records differ" >&2
  exit 1
}

JOBS=${JOBS:-4} "$script_dir/validate-source-artifact.sh" \
  "$datomic_home" "$artifact_a" "$validation_root" \
  | tee "$work_root/artifact-validation.log"

artifact_sha=$(sha256sum "$artifact_a" | awk '{print $1}')
validation_harness_sha=$(sha256sum "$validation_harness_manifest" | awk '{print $1}')
validation_summary="$validation_root/validation-summary.properties"
[[ -f "$validation_summary" ]] || {
  echo "artifact validator did not emit its summary" >&2
  exit 1
}

{
  printf 'stage=1\n'
  printf 'status=complete\n'
  printf 'artifact.name=%s\n' "$artifact_name"
  printf 'artifact.sha256=%s\n' "$artifact_sha"
  printf 'independent.build.count=2\n'
  printf 'independent.artifact.bytes.identical=true\n'
  printf 'independent.input.manifests.identical=true\n'
  printf 'stage1.validation.harness.manifest.sha256=%s\n' "$validation_harness_sha"
  printf 'original.peer.aot.on.candidate.classpath=false\n'
  printf 'original.core2.aot.on.candidate.classpath=false\n'
  sed -n '/^namespace\./p; /^handwritten\./p; /^warnings\./p; /^end-to-end\./p' \
    "$validation_summary"
} >"$work_root/stage-1-summary.properties"

evidence_manifest="$work_root/evidence.sha256"
if [[ -n "$(find "$build_a" "$build_b" "$validation_root" "$inputs_dir" -type l -print -quit)" ]]; then
  echo "Stage 1 evidence directories may not contain symbolic links" >&2
  exit 1
fi
(
  cd -- "$work_root"
  find build-a build-b artifact-validation inputs -type f -print
  printf '%s\n' \
    artifact-validation.log \
    build-a.log \
    build-b.log \
    input-manifest-diff.txt \
    stage-1-summary.properties
) | sort -u | while IFS= read -r evidence_file; do
  [[ -f "$work_root/$evidence_file" ]] || {
    echo "missing Stage 1 evidence file: $evidence_file" >&2
    exit 1
  }
  (cd -- "$work_root" && sha256sum "$evidence_file")
done >"$evidence_manifest"
(cd -- "$work_root" && sha256sum -c evidence.sha256 >/dev/null)
evidence_count=$(wc -l <"$evidence_manifest")

echo "Stage 1 reproducible source-artifact gate passed"
echo "two clean artifact builds are byte-identical"
echo "artifact SHA-256: $artifact_sha"
echo "all packaged-artifact validation gates passed"
echo "validation harness SHA-256: $validation_harness_sha"
echo "evidence files: $evidence_count"
echo "evidence manifest: $evidence_manifest"
echo "summary: $work_root/stage-1-summary.properties"
