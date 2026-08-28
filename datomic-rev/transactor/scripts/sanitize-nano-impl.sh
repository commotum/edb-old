#!/usr/bin/env bash
set -euo pipefail

export LC_ALL=C
export TZ=UTC
export SOURCE_DATE_EPOCH=315532800
unset JAVA_TOOL_OPTIONS _JAVA_OPTIONS JDK_JAVA_OPTIONS
umask 022

die() {
  printf 'sanitize-nano-impl: %s\n' "$*" >&2
  exit 1
}

usage() {
  printf 'usage: %s DATOMIC_PRO_1_0_7277_DIRECTORY OUTPUT_DIRECTORY\n' "$0" >&2
  exit 2
}

[[ $# -eq 2 ]] || usage

for required_command in cmp cp diff dirname find grep java mkdir mktemp \
    realpath rm sed sha256sum sort stat wc xargs; do
  command -v "$required_command" >/dev/null 2>&1 \
    || die "required command is unavailable: $required_command"
done

wrapper="$(realpath -e -- "${BASH_SOURCE[0]}")"
script_dir="$(cd -- "$(dirname -- "$wrapper")" && pwd -P)"
transactor_dir="$(realpath -e -- "$script_dir/..")"
repo_root="$(realpath -e -- "$transactor_dir/..")"
datomic_home="$(realpath -e -- "$1")" \
  || die "cannot resolve Datomic distribution directory: $1"
output_dir="$(realpath -m -- "$2")"

[[ -d "$datomic_home" ]] || die "Datomic distribution is not a directory: $datomic_home"
[[ ! -e "$output_dir" && ! -L "$output_dir" ]] \
  || die "refusing to overwrite output path: $output_dir"
[[ "$output_dir" != "$repo_root" && "$output_dir" != "$repo_root/"* ]] \
  || die "output must remain outside the repository: $output_dir"
[[ "$output_dir" != "$datomic_home" && "$output_dir" != "$datomic_home/"* ]] \
  || die "output must remain outside the licensed distribution: $output_dir"

input_jar="$datomic_home/lib/nano-impl-0.1.325.jar"
policy="$transactor_dir/reports/stage-1-nano-entry-policy.tsv"
sanitizer="$transactor_dir/tools/SanitizeNanoImpl.java"
packager="$repo_root/scripts/DeterministicJar.java"
baseline_dir="$transactor_dir/baseline"

expected_input_sha='fbde8184da356bd3a9995a9f05f01493efe4baa87c4d48377cda3e53331807cd'
expected_input_bytes='25293'
expected_output_sha='08f9699d6e35b9e052ec85ee15e0896b3a685c74e6e226d8cd89d9c61dbf8d5f'
expected_output_bytes='81218'
expected_policy_sha='ff8bffd9a2d653750308ab9455509f02a613d30adf5bd11ecae767fadaf4a5f1'
expected_sanitizer_sha='2d97c17887cefbbcb3b19b9d4e68e7607b3350e16a68ae1ed2a48263d87c55df'
expected_packager_sha='0ccc5df5ea072a55739d61d300e175978f27a3e7db11f831f319bb1000baaef1'
expected_baseline_manifest_sha='8777e071f03f3ecf1008bfc151f4ae77c16bc26f2b61f8185798b697786dfa82'
expected_distribution_jars_sha='db25519069bfd327c161220264b89fb011f2d9a3e42e4857e5233d4b61811860'
expected_baseline_row=$'nano-impl-0.1.325.jar\tlib/nano-impl-0.1.325.jar\tfbde8184da356bd3a9995a9f05f01493efe4baa87c4d48377cda3e53331807cd\t25293\t21\t8\t2\t11\tfalse\ttrue\ttrue\tdependency'

sha256_file() {
  local line
  line="$(sha256sum -- "$1")"
  printf '%s\n' "${line%% *}"
}

require_sha() {
  local path="$1"
  local expected="$2"
  local label="$3"
  local actual
  [[ -f "$path" && ! -L "$path" ]] || die "missing regular $label: $path"
  actual="$(sha256_file "$path")"
  [[ "$actual" == "$expected" ]] \
    || die "$label SHA-256 mismatch: expected $expected, got $actual"
}

require_sha "$input_jar" "$expected_input_sha" 'input JAR'
[[ "$(stat -c '%s' -- "$input_jar")" == "$expected_input_bytes" ]] \
  || die "input JAR byte-size mismatch"
require_sha "$policy" "$expected_policy_sha" 'entry policy'
require_sha "$sanitizer" "$expected_sanitizer_sha" 'sanitizer source'
require_sha "$packager" "$expected_packager_sha" 'deterministic packager source'
require_sha "$baseline_dir/manifest.sha256" "$expected_baseline_manifest_sha" \
  'Stage 0 baseline manifest'
require_sha "$baseline_dir/distribution-jars.tsv" "$expected_distribution_jars_sha" \
  'Stage 0 distribution-JAR inventory'
[[ "$(grep -Fxc -- "$expected_baseline_row" \
      "$baseline_dir/distribution-jars.tsv")" == '1' ]] \
  || die "Stage 0 nano-impl inventory row is absent or duplicated"
(cd -- "$baseline_dir" && sha256sum -c manifest.sha256 >/dev/null)

work_dir="$(mktemp -d /tmp/datomic-nano-sanitizer.XXXXXX)"
cleanup() {
  if [[ -n "${work_dir:-}" && "$work_dir" == /tmp/datomic-nano-sanitizer.* \
      && -d "$work_dir" ]]; then
    rm -rf -- "$work_dir"
  fi
}
trap cleanup EXIT

java_tmp_requested=${DATOMIC_NANO_JAVA_TMPDIR:-}
if [[ -n "$java_tmp_requested" ]]; then
  [[ "$java_tmp_requested" == /* ]] \
    || die "DATOMIC_NANO_JAVA_TMPDIR must be an absolute path"
  [[ "$java_tmp_requested" != *:* && "$java_tmp_requested" != *$'\t'* && \
     "$java_tmp_requested" != *$'\n'* ]] \
    || die "DATOMIC_NANO_JAVA_TMPDIR contains an invalid path character"
  [[ -d "$java_tmp_requested" && ! -L "$java_tmp_requested" ]] \
    || die "DATOMIC_NANO_JAVA_TMPDIR must be an existing non-symlink directory: $java_tmp_requested"
  java_tmp_dir="$(realpath -e -- "$java_tmp_requested")"
  [[ "$java_tmp_dir" != "$repo_root" && "$java_tmp_dir" != "$repo_root/"* ]] \
    || die "DATOMIC_NANO_JAVA_TMPDIR must remain outside the repository"
  [[ "$java_tmp_dir" != "$datomic_home" && \
     "$java_tmp_dir" != "$datomic_home/"* ]] \
    || die "DATOMIC_NANO_JAVA_TMPDIR must remain outside the licensed distribution"
  [[ "$java_tmp_dir" != "$output_dir" && "$java_tmp_dir" != "$output_dir/"* && \
     "$output_dir" != "$java_tmp_dir/"* ]] \
    || die "DATOMIC_NANO_JAVA_TMPDIR and output roots must be ancestor-disjoint"
else
  java_tmp_dir="$work_dir/java-tmp"
fi
mkdir -p -- "$java_tmp_dir"
[[ -d "$java_tmp_dir" && ! -L "$java_tmp_dir" ]] \
  || die "isolated Java temp root is not a regular directory: $java_tmp_dir"
[[ -z "$(find "$java_tmp_dir" -mindepth 1 -print -quit)" ]] \
  || die "isolated Java temp root is not empty: $java_tmp_dir"

run_java() {
  java -XX:+PerfDisableSharedMem -Djava.io.tmpdir="$java_tmp_dir" "$@"
}

staging_a="$work_dir/staging-a"
staging_b="$work_dir/staging-b"
prepare_a="$work_dir/prepare-a"
prepare_b="$work_dir/prepare-b"
verify_a="$work_dir/verify-a"
verify_b="$work_dir/verify-b"
negative_dir="$work_dir/negative"
build_a="$work_dir/nano-impl-sanitized-a.jar"
build_b="$work_dir/nano-impl-sanitized-b.jar"
mkdir -p -- "$staging_a" "$staging_b" "$prepare_a" "$prepare_b" \
  "$verify_a" "$verify_b" "$negative_dir"

run_java "$sanitizer" prepare \
  "$input_jar" "$policy" "$staging_a" "$prepare_a" >/dev/null
run_java "$sanitizer" prepare \
  "$input_jar" "$policy" "$staging_b" "$prepare_b" >/dev/null
diff -qr -- "$staging_a" "$staging_b" >/dev/null \
  || die "independent staging trees differ"
diff -qr -- "$prepare_a" "$prepare_b" >/dev/null \
  || die "independent preparation evidence differs"

run_java "$packager" "$staging_a" "$build_a" >/dev/null
run_java "$packager" "$staging_b" "$build_b" >/dev/null
cmp -s -- "$build_a" "$build_b" || die "independent JAR builds are not byte-identical"
require_sha "$build_a" "$expected_output_sha" 'first derivative build'
require_sha "$build_b" "$expected_output_sha" 'second derivative build'
[[ "$(stat -c '%s' -- "$build_a")" == "$expected_output_bytes" ]] \
  || die "first derivative build byte-size mismatch"
[[ "$(stat -c '%s' -- "$build_b")" == "$expected_output_bytes" ]] \
  || die "second derivative build byte-size mismatch"

run_java "$sanitizer" verify \
  "$build_a" "$policy" "$verify_a" >/dev/null
run_java "$sanitizer" verify \
  "$build_b" "$policy" "$verify_b" >/dev/null
diff -qr -- "$verify_a" "$verify_b" >/dev/null \
  || die "independent derivative verification evidence differs"

negative_results="$work_dir/negative-tests.tsv"
printf 'test\texpected_result\tdiagnostic_required\tobserved_result\tstatus\n' \
  > "$negative_results"

expect_rejection() {
  local test_name="$1"
  local diagnostic="$2"
  shift 2
  local stdout_file="$negative_dir/$test_name.stdout"
  local stderr_file="$negative_dir/$test_name.stderr"
  if "$@" >"$stdout_file" 2>"$stderr_file"; then
    die "negative test unexpectedly succeeded: $test_name"
  fi
  grep -Fq -- "$diagnostic" "$stderr_file" \
    || die "negative test failed for the wrong reason: $test_name"
  printf '%s\treject\t%s\treject\tpass\n' "$test_name" "$diagnostic" \
    >> "$negative_results"
}

bad_input="$negative_dir/nano-impl-appended.jar"
cp -- "$input_jar" "$bad_input"
printf 'x' >> "$bad_input"
expect_rejection 'changed-input-archive' 'unexpected input JAR size: 25294' \
  run_java "$sanitizer" prepare \
  "$bad_input" "$policy" "$negative_dir/bad-input-stage" \
  "$negative_dir/bad-input-evidence"

bad_policy="$negative_dir/changed-policy.tsv"
cp -- "$policy" "$bad_policy"
printf '\n' >> "$bad_policy"
expect_rejection 'changed-entry-policy' 'unexpected entry-policy SHA-256:' \
  run_java "$sanitizer" prepare \
  "$input_jar" "$bad_policy" "$negative_dir/bad-policy-stage" \
  "$negative_dir/bad-policy-evidence"

expect_rejection 'licensed-original-as-derivative' \
  'derivative must be file-only: META-INF/' \
  run_java "$sanitizer" verify \
  "$input_jar" "$policy" "$negative_dir/original-verify-evidence"

tampered_staging="$negative_dir/tampered-staging"
tampered_jar="$negative_dir/tampered-content.jar"
cp -a -- "$staging_a" "$tampered_staging"
printf '\n' >> "$tampered_staging/cognitect/nano_impl.clj"
run_java "$packager" \
  "$tampered_staging" "$tampered_jar" >/dev/null
expect_rejection 'changed-retained-content' \
  'derivative size mismatch: cognitect/nano_impl.clj' \
  run_java "$sanitizer" verify \
  "$tampered_jar" "$policy" "$negative_dir/tampered-verify-evidence"

extra_staging="$negative_dir/extra-staging"
extra_jar="$negative_dir/unexpected-entry.jar"
cp -a -- "$staging_a" "$extra_staging"
printf 'unexpected\n' > "$extra_staging/zz-unexpected.txt"
run_java "$packager" "$extra_staging" "$extra_jar" >/dev/null
expect_rejection 'unexpected-derivative-entry' \
  'unexpected derivative entry: zz-unexpected.txt' \
  run_java "$sanitizer" verify \
  "$extra_jar" "$policy" "$negative_dir/extra-verify-evidence"

java_version_file="$work_dir/java-version.txt"
run_java --version > "$java_version_file"
[[ -z "$(find "$java_tmp_dir" -mindepth 1 -print -quit)" ]] \
  || die "Java invocations retained content in isolated temp root: $java_tmp_dir"
java_version="$(sed -n '1p' "$java_version_file")"
java_executable="$(realpath -e -- "$(command -v java)")"
java_executable_sha="$(sha256_file "$java_executable")"
wrapper_sha="$(sha256_file "$wrapper")"
sanitizer_sha="$(sha256_file "$sanitizer")"
packager_sha="$(sha256_file "$packager")"
policy_sha="$(sha256_file "$policy")"

mkdir -p -- "$output_dir/evidence"
derivative="$output_dir/nano-impl-0.1.325-sanitized.jar"
cp -- "$build_a" "$derivative"
cp -- "$prepare_a/original-entry-validation.tsv" \
  "$output_dir/evidence/original-entry-validation.tsv"
cp -- "$prepare_a/prepare-summary.tsv" \
  "$output_dir/evidence/prepare-summary.tsv"
cp -- "$prepare_a/removed-entries.tsv" \
  "$output_dir/evidence/removed-entries.tsv"
cp -- "$prepare_a/retained-entries.tsv" \
  "$output_dir/evidence/retained-entries.tsv"
cp -- "$verify_a/sanitized-entry-validation.tsv" \
  "$output_dir/evidence/sanitized-entry-validation.tsv"
cp -- "$verify_a/forbidden-hits.tsv" \
  "$output_dir/evidence/forbidden-hits.tsv"
cp -- "$verify_a/sanitized-summary.tsv" \
  "$output_dir/evidence/sanitized-summary.tsv"
cp -- "$negative_results" "$output_dir/evidence/negative-tests.tsv"

printf 'build\tsha256\tbytes\tcomparison\tstatus\n' \
  > "$output_dir/evidence/twin-builds.tsv"
printf 'a\t%s\t%s\tbyte-identical-to-b\tpass\n' \
  "$expected_output_sha" "$expected_output_bytes" \
  >> "$output_dir/evidence/twin-builds.tsv"
printf 'b\t%s\t%s\tbyte-identical-to-a\tpass\n' \
  "$expected_output_sha" "$expected_output_bytes" \
  >> "$output_dir/evidence/twin-builds.tsv"

{
  printf 'item\tvalue\n'
  printf 'java.command\t%s\n' "$java_executable"
  printf 'java.executable.sha256\t%s\n' "$java_executable_sha"
  printf 'java.version\t%s\n' "$java_version"
  printf 'java.io.tmpdir.explicit\ttrue\n'
  printf 'java.io.tmpdir.retained_entries\t0\n'
  printf 'timezone\tUTC\n'
  printf 'source_date_epoch\t315532800\n'
  printf 'wrapper.source.sha256\t%s\n' "$wrapper_sha"
  printf 'sanitizer.source.sha256\t%s\n' "$sanitizer_sha"
  printf 'packager.source.sha256\t%s\n' "$packager_sha"
  printf 'entry_policy.sha256\t%s\n' "$policy_sha"
  printf 'stage0.manifest.sha256\t%s\n' "$expected_baseline_manifest_sha"
  printf 'stage0.distribution_jars.sha256\t%s\n' \
    "$expected_distribution_jars_sha"
} > "$output_dir/evidence/toolchain.tsv"

{
  printf 'metric\tvalue\n'
  printf 'input.jar.sha256\t%s\n' "$expected_input_sha"
  printf 'input.jar.bytes\t%s\n' "$expected_input_bytes"
  printf 'input.entries.total\t21\n'
  printf 'input.entries.retained\t11\n'
  printf 'input.entries.removed_forbidden\t2\n'
  printf 'input.entries.dropped_directories\t8\n'
  printf 'derivative.jar.sha256\t%s\n' "$expected_output_sha"
  printf 'derivative.jar.bytes\t%s\n' "$expected_output_bytes"
  printf 'derivative.entries.total\t11\n'
  printf 'derivative.payload.bytes\t79362\n'
  printf 'forbidden.hash.hits\t0\n'
  printf 'independent.builds\t2\n'
  printf 'independent.builds.byte_identical\ttrue\n'
  printf 'negative.tests.total\t5\n'
  printf 'negative.tests.passed\t5\n'
  printf 'runtime.claim\tnone-content-sanitization-only\n'
} > "$output_dir/evidence/summary.tsv"

(
  cd -- "$output_dir"
  find . -type f ! -path './manifest.sha256' -print0 \
    | sort -z \
    | xargs -0 sha256sum --
) > "$output_dir/manifest.sha256"
(cd -- "$output_dir" && sha256sum -c manifest.sha256 >/dev/null)

[[ "$(sha256_file "$derivative")" == "$expected_output_sha" ]] \
  || die "published derivative changed after verification"
[[ "$(grep -c $'\tpass$' "$output_dir/evidence/negative-tests.tsv")" == '5' ]] \
  || die "published negative-test evidence is incomplete"
[[ "$(sed -n '2,$p' "$output_dir/evidence/forbidden-hits.tsv" | wc -l)" == '0' ]] \
  || die "published evidence reports a forbidden hash hit"

printf 'sanitized derivative: %s\n' "$derivative"
printf 'sha256: %s\n' "$expected_output_sha"
printf 'output manifest: %s\n' "$(sha256_file "$output_dir/manifest.sha256")"
