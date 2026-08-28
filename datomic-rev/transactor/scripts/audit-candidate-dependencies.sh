#!/usr/bin/env bash
set -euo pipefail

export LC_ALL=C
export TZ=UTC
unset JAVA_TOOL_OPTIONS _JAVA_OPTIONS JDK_JAVA_OPTIONS
umask 022

die() {
  printf 'audit-candidate-dependencies: %s\n' "$*" >&2
  exit 1
}

usage() {
  printf 'usage: %s DATOMIC_PRO_1_0_7277_DIRECTORY SANITIZED_NANO_JAR OUTPUT_DIRECTORY\n' "$0" >&2
  exit 2
}

[[ $# -eq 3 ]] || usage

for command_name in awk cp dd dirname find grep java ln mkdir mktemp realpath rm \
    sed sha256sum sort stat wc xargs; do
  command -v "$command_name" >/dev/null 2>&1 \
    || die "required command is unavailable: $command_name"
done

script_dir=$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd -P)
transactor_dir=$(realpath -e -- "$script_dir/..")
repo_root=$(realpath -e -- "$transactor_dir/..")
[[ ! -L "$1" ]] || die "Datomic distribution argument may not be a symlink: $1"
[[ ! -L "$2" ]] || die "sanitized nano argument may not be a symlink: $2"
datomic_home=$(realpath -e -- "$1") \
  || die "cannot resolve Datomic distribution directory: $1"
sanitized_nano=$(realpath -e -- "$2") \
  || die "cannot resolve sanitized nano derivative: $2"
output_dir=$(realpath -m -- "$3")

[[ -d "$datomic_home" && ! -L "$datomic_home" ]] \
  || die "Datomic distribution is not a real directory: $datomic_home"
[[ -f "$sanitized_nano" && ! -L "$sanitized_nano" ]] \
  || die "sanitized nano derivative is not a regular file: $sanitized_nano"
[[ ! -e "$output_dir" && ! -L "$output_dir" ]] \
  || die "refusing to overwrite output path: $output_dir"
[[ "$output_dir" != "$repo_root" && "$output_dir" != "$repo_root/"* ]] \
  || die "output must remain outside the repository: $output_dir"
[[ "$output_dir" != "$datomic_home" && "$output_dir" != "$datomic_home/"* ]] \
  || die "output must remain outside the licensed distribution: $output_dir"

baseline_dir="$transactor_dir/baseline"
distribution_jars="$baseline_dir/distribution-jars.tsv"
scanner="$transactor_dir/tools/ScanCandidateClasspath.java"
packager="$repo_root/scripts/DeterministicJar.java"

expected_baseline_manifest_sha=8777e071f03f3ecf1008bfc151f4ae77c16bc26f2b61f8185798b697786dfa82
expected_distribution_jars_sha=db25519069bfd327c161220264b89fb011f2d9a3e42e4857e5233d4b61811860
expected_scanner_sha=0c2fc038183997e900e27b228aa427aecf881779d1237005e2ef7eb1332948b8
expected_packager_sha=0ccc5df5ea072a55739d61d300e175978f27a3e7db11f831f319bb1000baaef1
expected_sanitized_nano_sha=08f9699d6e35b9e052ec85ee15e0896b3a685c74e6e226d8cd89d9c61dbf8d5f
expected_sanitized_nano_bytes=81218

peer_sha=cb55c9d01e155f9965e3332cd8ef70be42cdefacb50c072076e9ecf22b9a23ba
transactor_sha=d90819b57e2138085ddc26c93314f953c2cd776d1d1aa8b43a6bec1e475d3692
core2_sha=81fdf81586c7be1a4b61655b568348d12db7892cb4562d0db7529bbc7af8a94b
vendor_key_sha=f3627f52580b84fe8f536423643fb46999fd2458498989307f2820d778eb73a1
vendor_trust_sha=ca64f839d051d909974a624b4345d83cc739a0b90edbe54e7c3605a2fd8fc13b

sha256_file() {
  local line
  line=$(sha256sum -- "$1")
  printf '%s\n' "${line%% *}"
}

require_sha() {
  local path=$1
  local expected=$2
  local label=$3
  [[ -f "$path" && ! -L "$path" ]] || die "missing regular $label: $path"
  local actual
  actual=$(sha256_file "$path")
  [[ "$actual" == "$expected" ]] \
    || die "$label SHA-256 mismatch: expected $expected, got $actual"
}

require_sha "$baseline_dir/manifest.sha256" \
  "$expected_baseline_manifest_sha" 'Stage 0 baseline manifest'
require_sha "$distribution_jars" "$expected_distribution_jars_sha" \
  'Stage 0 distribution-JAR inventory'
require_sha "$scanner" "$expected_scanner_sha" 'classpath scanner source'
require_sha "$packager" "$expected_packager_sha" 'deterministic packager source'
require_sha "$sanitized_nano" "$expected_sanitized_nano_sha" \
  'sanitized nano derivative'
[[ "$(stat -c '%s' -- "$sanitized_nano")" == "$expected_sanitized_nano_bytes" ]] \
  || die "sanitized nano derivative byte-size mismatch"
(cd -- "$baseline_dir" && sha256sum -c manifest.sha256 >/dev/null)

sanitized_root=$(dirname -- "$sanitized_nano")
sanitized_name=${sanitized_nano##*/}
case "$sanitized_name" in
  *$'\t'*|*$'\n'*|*$'\r'*|*:*|*/*) die 'unsafe sanitized nano filename' ;;
esac

mkdir -p -- "$output_dir"
classpath_tsv="$output_dir/candidate-dependencies.tsv"
forbidden_tsv="$output_dir/forbidden-content-policy.tsv"

awk -F '\t' -v OFS='\t' \
  -v sanitized_name="$sanitized_name" \
  -v sanitized_sha="$expected_sanitized_nano_sha" '
    BEGIN {
      print "position", "role", "root", "relative_path", "kind", "expected_sha256"
    }
    NR == 1 {next}
    $12 != "dependency" {next}
    $1 == "core2-1.0.140.jar" {next}
    {
      position++
      if ($1 == "nano-impl-0.1.325.jar") {
        print position, "sanitized-datomic-dependency", "derived", \
          sanitized_name, "jar", sanitized_sha
      } else {
        print position, "retained-distribution-dependency", "distribution", \
          $2, "jar", $3
      }
    }
    END {
      if (position != 532) exit 42
    }
  ' "$distribution_jars" > "$classpath_tsv" \
  || die 'could not construct the 532-element candidate dependency manifest'

{
  printf 'rule_id\tsha256\treason\n'
  printf 'peer-original\t%s\tlicensed Peer implementation artifact\n' "$peer_sha"
  printf 'transactor-original\t%s\tlicensed Transactor implementation artifact\n' \
    "$transactor_sha"
  printf 'core2-original\t%s\toriginal Peer core2 AOT support artifact\n' "$core2_sha"
  printf 'vendor-key\t%s\tvendor TLS identity payload\n' "$vendor_key_sha"
  printf 'vendor-trust\t%s\tvendor TLS trust payload\n' "$vendor_trust_sha"
} > "$forbidden_tsv"

scan_dir="$output_dir/scan"
java -XX:+PerfDisableSharedMem "$scanner" \
  "$classpath_tsv" "$forbidden_tsv" "$scan_dir" \
  "distribution=$datomic_home" "derived=$sanitized_root"

metric() {
  awk -F '\t' -v metric="$1" '$1 == metric {print $2}' \
    "$scan_dir/summary.tsv"
}

[[ "$(metric classpath.elements)" == 532 ]] \
  || die 'candidate dependency element count differs from 532'
[[ "$(metric top-level.files)" == 532 ]] \
  || die 'candidate dependency file count differs from 532'
[[ "$(metric top-level.bytes)" == 387428686 ]] \
  || die 'candidate dependency byte total changed'
[[ "$(metric archive.entries)" == 185393 ]] \
  || die 'candidate dependency non-directory entry count changed'
[[ "$(metric archive.entry.bytes)" == 2312738044 ]] \
  || die 'candidate dependency uncompressed payload total changed'
[[ "$(metric nested.archive.entries)" == 95 ]] \
  || die 'candidate dependency nested-archive entry count changed'
[[ "$(metric nested.archive.entry.bytes)" == 649478 ]] \
  || die 'candidate dependency nested-archive payload total changed'
[[ "$(metric forbidden.hits)" == 0 && "$(metric violations)" == 0 \
    && "$(metric status)" == PASS ]] \
  || die 'candidate dependency scan did not close cleanly'

work_dir=$(mktemp -d /tmp/datomic-candidate-dependency-audit.XXXXXX)
cleanup() {
  if [[ -n "${work_dir:-}" && "$work_dir" == /tmp/datomic-candidate-dependency-audit.* \
      && -d "$work_dir" ]]; then
    rm -rf -- "$work_dir"
  fi
}
trap cleanup EXIT

negative_results="$output_dir/negative-tests.tsv"
printf 'test\texpected_rule\texpected_scope\tobserved\tstatus\n' \
  > "$negative_results"

run_rejection() {
  local test_name=$1
  local expected_rule=$2
  local expected_scope=$3
  local manifest=$4
  local root_argument=$5
  local result="$work_dir/$test_name-result"
  if java -XX:+PerfDisableSharedMem "$scanner" \
      "$manifest" "$forbidden_tsv" "$result" "$root_argument" \
      > "$work_dir/$test_name.stdout" 2> "$work_dir/$test_name.stderr"; then
    die "negative classpath test unexpectedly succeeded: $test_name"
  fi
  awk -F '\t' -v rule="$expected_rule" -v scope="$expected_scope" \
    'NR > 1 && $1 == rule && $4 == scope {found=1} END {exit !found}' \
    "$result/forbidden-hits.tsv" \
    || die "negative classpath test missed $expected_rule/$expected_scope: $test_name"
  printf '%s\t%s\t%s\treject\tpass\n' \
    "$test_name" "$expected_rule" "$expected_scope" >> "$negative_results"
}

run_violation_rejection() {
  local test_name=$1
  local expected_scope=$2
  local expected_reason=$3
  local manifest=$4
  local root_argument=$5
  local result="$work_dir/$test_name-result"
  if java -XX:+PerfDisableSharedMem "$scanner" \
      "$manifest" "$forbidden_tsv" "$result" "$root_argument" \
      > "$work_dir/$test_name.stdout" 2> "$work_dir/$test_name.stderr"; then
    die "negative violation test unexpectedly succeeded: $test_name"
  fi
  awk -F '\t' -v scope="$expected_scope" -v reason="$expected_reason" \
    'NR > 1 && $3 == scope && index($5, reason) {found=1}
     END {exit !found}' "$result/violations.tsv" \
    || die "negative test missed $expected_scope/$expected_reason: $test_name"
  printf '%s\tviolation\t%s\treject\tpass\n' \
    "$test_name" "$expected_scope" >> "$negative_results"
}

printf 'position\trole\troot\trelative_path\tkind\texpected_sha256\n' \
  > "$work_dir/original-nano.tsv"
printf '1\tnegative-test\tdistribution\tlib/nano-impl-0.1.325.jar\tjar\t%s\n' \
  'fbde8184da356bd3a9995a9f05f01493efe4baa87c4d48377cda3e53331807cd' \
  >> "$work_dir/original-nano.tsv"
run_rejection original-nano vendor-key archive-entry \
  "$work_dir/original-nano.tsv" "distribution=$datomic_home"
awk -F '\t' 'NR > 1 && ($1 == "vendor-key" || $1 == "vendor-trust") {n++}
  END {exit n != 2}' "$work_dir/original-nano-result/forbidden-hits.tsv" \
  || die 'original nano negative test did not find exactly the two JKS payloads'
cp -- "$work_dir/original-nano-result/forbidden-hits.tsv" \
  "$output_dir/original-nano-forbidden-hits.tsv"

negative_root="$work_dir/negative-root"
mkdir -p -- "$negative_root/renamed" "$negative_root/embedded-stage"
cp -- "$datomic_home/peer-1.0.7277.jar" \
  "$negative_root/renamed/reference-a.bin"
cp -- "$datomic_home/datomic-transactor-pro-1.0.7277.jar" \
  "$negative_root/renamed/reference-b.bin"
cp -- "$datomic_home/lib/core2-1.0.140.jar" \
  "$negative_root/renamed/reference-c.bin"

for specification in \
  "renamed-peer reference-a.bin $peer_sha peer-original" \
  "renamed-transactor reference-b.bin $transactor_sha transactor-original" \
  "renamed-core2 reference-c.bin $core2_sha core2-original"; do
  set -- $specification
  test_name=$1
  test_file=$2
  test_sha=$3
  test_rule=$4
  printf 'position\trole\troot\trelative_path\tkind\texpected_sha256\n' \
    > "$work_dir/$test_name.tsv"
  printf '1\tnegative-test\ttest\trenamed/%s\tjar\t%s\n' \
    "$test_file" "$test_sha" >> "$work_dir/$test_name.tsv"
  run_rejection "$test_name" "$test_rule" top-level-file \
    "$work_dir/$test_name.tsv" "test=$negative_root"
done

cp -- "$datomic_home/peer-1.0.7277.jar" \
  "$negative_root/embedded-stage/disguised-payload.bin"
embedded_jar="$negative_root/embedded-original.jar"
java -XX:+PerfDisableSharedMem "$packager" \
  "$negative_root/embedded-stage" "$embedded_jar" >/dev/null
embedded_sha=$(sha256_file "$embedded_jar")
printf 'position\trole\troot\trelative_path\tkind\texpected_sha256\n' \
  > "$work_dir/embedded-original.tsv"
printf '1\tnegative-test\ttest\tembedded-original.jar\tjar\t%s\n' \
  "$embedded_sha" >> "$work_dir/embedded-original.tsv"
run_rejection direct-embedded-peer peer-original archive-entry \
  "$work_dir/embedded-original.tsv" "test=$negative_root"

nested_stage="$negative_root/nested-stage"
outer_stage="$negative_root/outer-stage"
mkdir -p -- "$nested_stage" "$outer_stage"
cp -- "$datomic_home/peer-1.0.7277.jar" \
  "$nested_stage/disguised-payload.bin"
nested_container="$negative_root/container.bin"
java -XX:+PerfDisableSharedMem "$packager" \
  "$nested_stage" "$nested_container" >/dev/null
cp -- "$nested_container" "$outer_stage/container.bin"
nested_outer="$negative_root/hidden-nested-original.jar"
java -XX:+PerfDisableSharedMem "$packager" \
  "$outer_stage" "$nested_outer" >/dev/null
printf 'position\trole\troot\trelative_path\tkind\texpected_sha256\n' \
  > "$work_dir/hidden-nested-original.tsv"
printf '1\tnegative-test\ttest\thidden-nested-original.jar\tjar\t%s\n' \
  "$(sha256_file "$nested_outer")" >> "$work_dir/hidden-nested-original.tsv"
run_rejection hidden-nested-peer peer-original nested-archive-entry \
  "$work_dir/hidden-nested-original.tsv" "test=$negative_root"

trailing_stage="$negative_root/trailing-stage"
mkdir -p -- "$trailing_stage"
trailing_payload="$trailing_stage/empty-zip-peer.bin"
printf '\x50\x4b\x05\x06\x00\x00\x00\x00\x00\x00\x00\x00\x00\x00\x00\x00\x00\x00\x00\x00\x00\x00' \
  > "$trailing_payload"
dd if="$datomic_home/peer-1.0.7277.jar" of="$trailing_payload" \
  oflag=append conv=notrunc status=none
trailing_outer="$negative_root/empty-zip-peer-suffix.jar"
java -XX:+PerfDisableSharedMem "$packager" \
  "$trailing_stage" "$trailing_outer" >/dev/null
printf 'position\trole\troot\trelative_path\tkind\texpected_sha256\n' \
  > "$work_dir/empty-zip-peer-suffix.tsv"
printf '1\tnegative-test\ttest\tempty-zip-peer-suffix.jar\tjar\t%s\n' \
  "$(sha256_file "$trailing_outer")" \
  >> "$work_dir/empty-zip-peer-suffix.tsv"
run_violation_rejection empty-zip-peer-suffix nested-archive \
  'ZIP central-directory entry count differs from streamed entries' \
  "$work_dir/empty-zip-peer-suffix.tsv" "test=$negative_root"

malformed_stage="$negative_root/malformed-stage"
mkdir -p -- "$malformed_stage"
printf 'not a ZIP archive\n' > "$malformed_stage/broken.zip"
malformed_outer="$negative_root/malformed-nested.jar"
java -XX:+PerfDisableSharedMem "$packager" \
  "$malformed_stage" "$malformed_outer" >/dev/null
printf 'position\trole\troot\trelative_path\tkind\texpected_sha256\n' \
  > "$work_dir/malformed-nested.tsv"
printf '1\tnegative-test\ttest\tmalformed-nested.jar\tjar\t%s\n' \
  "$(sha256_file "$malformed_outer")" >> "$work_dir/malformed-nested.tsv"
run_violation_rejection malformed-nested archive \
  'archive-named entry lacks a ZIP signature' \
  "$work_dir/malformed-nested.tsv" "test=$negative_root"

printf 'position\trole\troot\trelative_path\tkind\texpected_sha256\n' \
  > "$work_dir/hash-mismatch.tsv"
printf '1\tnegative-test\tdistribution\tlib/anomalies-0.1.12.jar\tjar\t%s\n' \
  '0000000000000000000000000000000000000000000000000000000000000000' \
  >> "$work_dir/hash-mismatch.tsv"
run_violation_rejection hash-mismatch element 'top-level SHA-256 mismatch' \
  "$work_dir/hash-mismatch.tsv" "distribution=$datomic_home"

ln -s -- reference-a.bin "$negative_root/renamed/link.jar"
printf 'position\trole\troot\trelative_path\tkind\texpected_sha256\n' \
  > "$work_dir/symlink-element.tsv"
printf '1\tnegative-test\ttest\trenamed/link.jar\tjar\t%s\n' "$peer_sha" \
  >> "$work_dir/symlink-element.tsv"
run_violation_rejection symlink-element element \
  'symbolic link in classpath path' \
  "$work_dir/symlink-element.tsv" "test=$negative_root"

[[ "$(grep -c $'\tpass$' "$negative_results")" == 10 ]] \
  || die 'negative classpath test evidence is incomplete'

java_version_file="$work_dir/java-version.txt"
java --version > "$java_version_file"
{
  printf 'metric\tvalue\n'
  printf 'candidate.dependencies\t532\n'
  printf 'distribution.dependencies.retained\t531\n'
  printf 'core2.original.excluded\ttrue\n'
  printf 'nano.original.replaced\ttrue\n'
  printf 'nano.sanitized.sha256\t%s\n' "$expected_sanitized_nano_sha"
  printf 'classpath.top_level.bytes\t387428686\n'
  printf 'classpath.archive.entries\t185393\n'
  printf 'classpath.archive.entry.bytes\t2312738044\n'
  printf 'classpath.nested_archive.entries\t95\n'
  printf 'classpath.nested_archive.entry.bytes\t649478\n'
  printf 'forbidden.content.hits\t0\n'
  printf 'scan.violations\t0\n'
  printf 'negative.tests.passed\t10\n'
  printf 'runtime.claim\tnone-dependency-isolation-only\n'
} > "$output_dir/summary.tsv"
{
  printf 'item\tvalue\n'
  printf 'java.version\t%s\n' "$(sed -n '1p' "$java_version_file")"
  printf 'audit.script.sha256\t%s\n' \
    "$(sha256_file "${BASH_SOURCE[0]}")"
  printf 'scanner.source.sha256\t%s\n' "$expected_scanner_sha"
  printf 'packager.source.sha256\t%s\n' "$expected_packager_sha"
  printf 'stage0.manifest.sha256\t%s\n' "$expected_baseline_manifest_sha"
  printf 'stage0.distribution_jars.sha256\t%s\n' \
    "$expected_distribution_jars_sha"
} > "$output_dir/toolchain.tsv"

(
  cd -- "$output_dir"
  find . -type f ! -path './manifest.sha256' -print0 \
    | sort -z | xargs -0 sha256sum --
) > "$output_dir/manifest.sha256"
(cd -- "$output_dir" && sha256sum -c manifest.sha256 >/dev/null)

printf 'candidate dependency isolation passed: 532 JARs, zero forbidden hits\n'
printf 'negative isolation tests passed: 10/10\n'
printf 'evidence manifest: %s\n' "$(sha256_file "$output_dir/manifest.sha256")"
