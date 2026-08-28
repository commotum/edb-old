#!/usr/bin/env bash
set -euo pipefail

export LC_ALL=C

script_dir=$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd)
project_dir=$(cd -- "$script_dir/../.." && pwd)
output_dir=${1:-}

die() {
  echo "structural runtime-seal fixtures: $*" >&2
  exit 1
}

[[ -n "$output_dir" ]] || die "usage: $0 EMPTY_OUTPUT_DIR"
output_abs=$(realpath -m "$output_dir")
project_abs=$(realpath "$project_dir")
[[ "$output_abs" != "$project_abs" && "$output_abs" != "$project_abs"/* ]] || \
  die "fixture output must remain outside the repository"
[[ ! -L "$output_dir" ]] || die "fixture output may not be a symlink"
if [[ -e "$output_dir" ]] && \
   [[ -n $(find "$output_dir" -mindepth 1 -print -quit 2>/dev/null) ]]; then
  die "refusing to overwrite non-empty fixture output: $output_dir"
fi
mkdir -p "$output_abs/evidence"

work_dir=$(mktemp -d -t datomic-structural-seal-fixture.XXXXXXXX)
cleanup() {
  rm -rf -- "$work_dir"
}
trap cleanup EXIT

tree_root="$work_dir/classpath"
mkdir -p "$tree_root/nested"
printf 'baseline\n' >"$tree_root/nested/input.clj"

write_membership() {
  local root=$1
  local destination=$2
  [[ -d "$root" && ! -L "$root" ]] || return 3
  [[ -z $(find "$root" -mindepth 1 ! -type f ! -type d -print -quit) ]] || \
    return 4
  {
    printf 'type\trelative_path\n'
    find "$root" -mindepth 1 \( -type d -o -type f \) -printf '%y\t%P\n' \
      | sort
  } >"$destination"
}

baseline="$output_abs/evidence/baseline-membership.tsv"
write_membership "$tree_root" "$baseline" || die "baseline membership failed"

printf 'addition\n' >"$tree_root/late-addition.clj"
with_addition="$output_abs/evidence/added-file-membership.tsv"
write_membership "$tree_root" "$with_addition" || \
  die "added-file membership enumeration failed"
cmp -s "$baseline" "$with_addition" && \
  die "late classpath file did not change the membership seal"
diff -u "$baseline" "$with_addition" \
  >"$output_abs/evidence/added-file.diff" || true
rm -f -- "$tree_root/late-addition.clj"

ln -s nested/input.clj "$tree_root/late-symlink.clj"
symlink_probe="$output_abs/evidence/symlink-probe.tsv"
set +e
write_membership "$tree_root" "$symlink_probe"
symlink_status=$?
set -e
[[ "$symlink_status" == 4 ]] || \
  die "classpath symlink was not rejected with the expected status"
rm -f -- "$tree_root/late-symlink.clj"

restored="$output_abs/evidence/restored-membership.tsv"
write_membership "$tree_root" "$restored" || die "restored membership failed"
cmp -s "$baseline" "$restored" || die "fixture tree did not return to baseline"

{
  printf 'status=PASS\n'
  printf 'added.classpath.file.detected=PASS\n'
  printf 'classpath.symlink.rejected=PASS\n'
  printf 'classpath.symlink.rejection.status=4\n'
  printf 'restored.membership.matches=PASS\n'
} >"$output_abs/summary.properties"

(cd "$output_abs" && \
  find . -type f ! -name manifest.sha256 -print0 | sort -z | xargs -0 sha256sum) \
  >"$output_abs/manifest.sha256"
(cd "$output_abs" && sha256sum -c manifest.sha256) >/dev/null

echo "Structural runtime-seal fixtures passed: $output_abs"
