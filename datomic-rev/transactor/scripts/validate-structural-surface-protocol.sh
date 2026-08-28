#!/usr/bin/env bash
set -euo pipefail

export LC_ALL=C
export TZ=UTC

script_dir=$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd)
transactor_dir=$(cd -- "$script_dir/.." && pwd)
project_dir=$(cd -- "$transactor_dir/.." && pwd)
java_bin=${1:-}
clojure_classpath=${2:-}
output_dir=${3:-}
emitter="$script_dir/emit_structural_surface.clj"
valid_fixture="$transactor_dir/fixtures/surface-valid-with-process-log.clj"
trailing_fixture="$transactor_dir/fixtures/surface-trailing-form.clj"

die() {
  echo "structural surface protocol: $*" >&2
  exit 1
}

[[ -x "$java_bin" && -n "$clojure_classpath" && -n "$output_dir" ]] || \
  die "usage: validate-structural-surface-protocol.sh JAVA CLOJURE_CLASSPATH OUTPUT_DIR"
for required_file in "$emitter" "$valid_fixture" "$trailing_fixture"; do
  [[ -f "$required_file" ]] || die "required fixture input is missing: $required_file"
done

project_abs=$(realpath "$project_dir")
output_abs=$(realpath -m "$output_dir")
[[ "$output_abs" != "$project_abs" && "$output_abs" != "$project_abs"/* ]] || \
  die "fixture output must remain outside the repository"
if [[ -e "$output_abs" ]] && [[ -n $(find "$output_abs" -mindepth 1 -print -quit) ]]; then
  die "refusing to overwrite non-empty output directory: $output_abs"
fi
mkdir -p "$output_abs"

valid_surface="$output_abs/valid.surface.edn"
"$java_bin" -cp "$clojure_classpath" clojure.main "$emitter" \
  "$valid_fixture" fixture.valid "$valid_surface" \
  >"$output_abs/valid.stdout" 2>"$output_abs/valid.stderr"
[[ $(awk 'NF {count++} END {print count + 0}' "$valid_surface") == 1 ]] || \
  die "valid fixture did not publish exactly one surface line"
grep -qx 'fixture-process-stdout' "$output_abs/valid.stdout" || \
  die "valid fixture process stdout was not preserved separately"
grep -qx '{:vars {}, :classes {}}' "$valid_surface" || \
  die "valid fixture dedicated surface changed"

trailing_surface="$output_abs/trailing.surface.edn"
set +e
"$java_bin" -cp "$clojure_classpath" clojure.main "$emitter" \
  "$trailing_fixture" fixture.trailing "$trailing_surface" \
  >"$output_abs/trailing.stdout" 2>"$output_abs/trailing.stderr"
trailing_exit=$?
set -e
[[ "$trailing_exit" == 2 ]] || \
  die "same-line trailing EDN fixture exited $trailing_exit instead of 2"
[[ ! -e "$trailing_surface" ]] || \
  die "same-line trailing EDN fixture published an invalid surface"
grep -q 'dedicated payload has a trailing EDN form' "$output_abs/trailing.stderr" || \
  die "same-line trailing EDN fixture failed for the wrong reason"

{
  printf 'status=PASS\n'
  printf 'valid.dedicated.surface=PASS\n'
  printf 'valid.process.stdout.preserved=PASS\n'
  printf 'same.line.trailing.edn.rejected=PASS\n'
  printf 'same.line.trailing.edn.exit=2\n'
  printf 'emitter.sha256=%s\n' "$(sha256sum "$emitter" | awk '{print $1}')"
  printf 'valid.fixture.sha256=%s\n' "$(sha256sum "$valid_fixture" | awk '{print $1}')"
  printf 'trailing.fixture.sha256=%s\n' "$(sha256sum "$trailing_fixture" | awk '{print $1}')"
} >"$output_abs/summary.properties"

(cd "$output_abs" && find . -type f ! -name manifest.sha256 -print0 \
  | sort -z | xargs -0 sha256sum) >"$output_abs/manifest.sha256"
(cd "$output_abs" && sha256sum -c manifest.sha256) >/dev/null

echo "Structural surface protocol fixtures passed: $output_abs"
