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
  printf 'original.peer.aot.on.candidate.classpath=false\n'
  printf 'original.core2.aot.on.candidate.classpath=false\n'
  sed -n '/^namespace\./p; /^handwritten\./p; /^warnings\./p; /^end-to-end\./p' \
    "$validation_summary"
} >"$work_root/stage-1-summary.properties"

echo "Stage 1 reproducible source-artifact gate passed"
echo "two clean artifact builds are byte-identical"
echo "artifact SHA-256: $artifact_sha"
echo "all packaged-artifact validation gates passed"
echo "summary: $work_root/stage-1-summary.properties"
