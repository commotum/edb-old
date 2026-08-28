#!/usr/bin/env bash
set -euo pipefail

usage() {
  cat >&2 <<'USAGE'
usage: validate-local.sh STAGE2_DRY_RUN_ROOT EMPTY_WORK_ROOT

Runs each candidate-only Stage 3 local probe in a fresh, externally bounded
JVM. STAGE2_DRY_RUN_ROOT must be a completed, hash-verifiable Stage 2 dry run;
the candidate classpath is reconstructed and re-audited from its exact record.
EMPTY_WORK_ROOT stores the origin gate, one log pair per case, and the summary.
USAGE
  exit 2
}

die() {
  echo "stage3 local validation: $*" >&2
  exit 1
}

[[ $# -eq 2 ]] || usage
stage2_root=$1
work_root=$2
script_dir=$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd -P)
scripts_root=$(cd -- "$script_dir/.." && pwd -P)
original_nano_sha=fbde8184da356bd3a9995a9f05f01493efe4baa87c4d48377cda3e53331807cd
expected_sanitized_nano_sha=08f9699d6e35b9e052ec85ee15e0896b3a685c74e6e226d8cd89d9c61dbf8d5f
expected_sanitized_nano_bytes=81218

for command_name in awk cmp cp find grep java mkdir mktemp readlink rm sed \
  sha256sum sort tail timeout unzip wc; do
  command -v "$command_name" >/dev/null 2>&1 || die "missing command: $command_name"
done

harness_recheck=
stub_recheck=
artifact_entries=
cleanup_temp_files() {
  local temp_files=()
  [[ -z "$harness_recheck" ]] || temp_files+=("$harness_recheck")
  [[ -z "$stub_recheck" ]] || temp_files+=("$stub_recheck")
  [[ -z "$artifact_entries" ]] || temp_files+=("$artifact_entries")
  ((${#temp_files[@]} == 0)) || rm -f -- "${temp_files[@]}"
}
trap cleanup_temp_files EXIT

[[ -d "$stage2_root" && ! -L "$stage2_root" ]] ||
  die "Stage 2 dry-run root is missing or symbolic: $stage2_root"
stage2_root=$(readlink -f -- "$stage2_root")
[[ "$work_root" == /* ]] || die "work root must be absolute"
[[ ! -L "$work_root" ]] || die "work root may not be a symbolic link"
[[ ! -e "$work_root" || -d "$work_root" ]] || die "work root is not a directory"
if [[ -d "$work_root" && -n "$(find "$work_root" -mindepth 1 -print -quit)" ]]; then
  die "refusing non-empty work root: $work_root"
fi

evidence_manifest="$stage2_root/evidence.sha256"
classpath_record="$stage2_root/candidate-classpath.tsv"
[[ -f "$evidence_manifest" && -f "$classpath_record" ]] ||
  die "Stage 2 root lacks evidence.sha256 or candidate-classpath.tsv"
(cd -- "$stage2_root" && sha256sum -c evidence.sha256 >/dev/null) ||
  die "Stage 2 dry-run evidence verification failed"
grep -Fqx 'candidate.boundary.validated=true' "$stage2_root/run-status.properties" ||
  die "Stage 2 run did not complete its candidate-boundary gate"

[[ "$(sed -n '1p' "$classpath_record")" == $'position\trole\tsha256\tpath' ]] ||
  die "candidate classpath record has the wrong header"
[[ "$(wc -l <"$classpath_record")" -eq 536 ]] ||
  die "candidate classpath record must contain exactly 535 entries"

classpath_entries=()
position=0
sanitized_nano_role_count=0
dependency_lib_dir=
while IFS=$'\t' read -r recorded_position role expected_sha entry extra; do
  [[ "$recorded_position" == position ]] && continue
  ((position += 1))
  [[ "$recorded_position" -eq "$position" && -n "$role" &&
     "$expected_sha" =~ ^[0-9a-f]{64}$ && -n "$entry" && -z "${extra:-}" ]] ||
    die "malformed candidate classpath row at position $position"
  [[ "$entry" == /* && "$entry" != *$'\n'* && "$entry" != *$'\r'* &&
     "$entry" != *'*'* && "$entry" != *'?'* ]] ||
    die "unsafe candidate classpath entry: $entry"
  [[ -e "$entry" ]] || die "candidate classpath entry disappeared: $entry"
  case "$position:$role" in
    1:recovered-artifact)
      [[ -f "$entry" && "$(sha256sum "$entry" | awk '{print $1}')" == "$expected_sha" ]] ||
        die "recovered artifact differs from its classpath record"
      artifact=$entry
      ;;
    2:stage2-test-harness)
      [[ "$entry" == "$scripts_root" && -d "$entry" ]] ||
        die "candidate harness is not the current scripts root"
      harness_manifest="$stage2_root/inputs/stage2-harness.tsv"
      [[ -f "$harness_manifest" &&
         "$(sha256sum "$harness_manifest" | awk '{print $1}')" == "$expected_sha" ]] ||
        die "Stage 2 harness manifest differs from its classpath record"
      [[ -z "$(find "$scripts_root" -type l -print -quit)" ]] ||
        die "scripts classpath root contains a symbolic link"
      harness_recheck=$(mktemp)
      printf 'sha256\tpath\n' >"$harness_recheck"
      while IFS= read -r harness_path; do
        printf '%s\t%s\n' \
          "$(sha256sum "$harness_path" | awk '{print $1}')" \
          "${harness_path#"$scripts_root/"}" >>"$harness_recheck"
      done < <(find "$scripts_root" -type f -print | sort)
      cmp -s "$harness_manifest" "$harness_recheck" ||
        die "scripts classpath root differs from the exhaustive Stage 2 manifest"
      rm -f -- "$harness_recheck"
      ;;
    3:compile-only-hotrod-stubs)
      [[ -d "$entry" && -z "$(find "$entry" -type l -print -quit)" ]] ||
        die "compile-only stub tree is missing or symbolic"
      stub_manifest="$stage2_root/inputs/infinispan-stubs.tsv"
      [[ -f "$stub_manifest" &&
         "$(sha256sum "$stub_manifest" | awk '{print $1}')" == "$expected_sha" ]] ||
        die "stub manifest differs from its classpath record"
      stub_recheck=$(mktemp)
      printf 'sha256\tpath\n' >"$stub_recheck"
      while IFS= read -r stub_path; do
        printf '%s\t%s\n' \
          "$(sha256sum "$entry/$stub_path" | awk '{print $1}')" \
          "$stub_path" >>"$stub_recheck"
      done < <(find "$entry" -type f -printf '%P\n' | sort)
      cmp -s "$stub_manifest" "$stub_recheck" ||
        die "compile-only stub tree differs from its Stage 2 manifest"
      rm -f -- "$stub_recheck"
      ;;
    *:dependency)
      [[ "$position" -ge 4 && -f "$entry" &&
         "$(sha256sum "$entry" | awk '{print $1}')" == "$expected_sha" ]] ||
        die "candidate dependency differs at position $position"
      [[ "$expected_sha" != "$original_nano_sha" ]] ||
        die "original Nano implementation hash entered candidate classpath: $entry"
      [[ ! -L "$entry" ]] ||
        die "candidate dependency is symbolic at position $position"
      entry_dir=${entry%/*}
      [[ "${entry_dir##*/}" == lib ]] ||
        die "ordinary candidate dependency is not a direct datomic-home/lib entry: $entry"
      if [[ -z "$dependency_lib_dir" ]]; then
        dependency_lib_dir=$entry_dir
      else
        [[ "$entry_dir" == "$dependency_lib_dir" ]] ||
          die "ordinary candidate dependencies do not share one datomic-home/lib: $entry"
      fi
      case "${entry##*/}" in
        peer-*.jar|core2-*.jar|datomic-transactor*.jar|*transactor-pro*.jar|nano-impl-*.jar)
          die "forbidden original implementation entered candidate classpath: $entry"
          ;;
      esac
      ;;
    *:sanitized-nano-dependency)
      # Stage 2 substitutes exactly one manifest row in place, preserving the
      # 535-entry cardinality. This is the only dependency role allowed outside
      # the single inferred datomic-home/lib directory checked above.
      [[ "$position" -ge 4 ]] ||
        die "sanitized Nano appeared before the dependency portion of the classpath"
      ((sanitized_nano_role_count += 1))
      [[ "$sanitized_nano_role_count" -eq 1 ]] ||
        die "candidate classpath contains more than one sanitized Nano role"
      [[ "$expected_sha" == "$expected_sanitized_nano_sha" ]] ||
        die "candidate classpath records a noncanonical sanitized Nano SHA-256"
      [[ -f "$entry" && ! -L "$entry" ]] ||
        die "candidate sanitized Nano is missing or symbolic: $entry"
      [[ "$(sha256sum "$entry" | awk '{print $1}')" == "$expected_sanitized_nano_sha" ]] ||
        die "candidate sanitized Nano bytes are not canonical"
      [[ "$(wc -c <"$entry")" -eq "$expected_sanitized_nano_bytes" ]] ||
        die "candidate sanitized Nano size is not canonical"
      ;;
    *) die "unexpected candidate role at position $position: $role" ;;
  esac
  classpath_entries+=("$entry")
done <"$classpath_record"
[[ "$position" -eq 535 ]] || die "candidate classpath cardinality changed"
[[ "$sanitized_nano_role_count" -eq 1 ]] ||
  die "candidate classpath must contain exactly one sanitized Nano role"
[[ -n "$dependency_lib_dir" ]] ||
  die "candidate classpath contains no ordinary datomic-home/lib dependencies"
candidate_classpath=$(IFS=:; echo "${classpath_entries[*]}")

timeout_seconds=${STAGE3_LOCAL_TIMEOUT_SECONDS:-60}
[[ "$timeout_seconds" =~ ^[0-9]+$ ]] || die "timeout must be an integer"
((timeout_seconds >= 10 && timeout_seconds <= 300)) ||
  die "timeout must be between 10 and 300 seconds"

[[ -f "$artifact" ]] || die "first classpath entry is not the recovered artifact"
unzip -p -- "$artifact" META-INF/datomic-rev/build.properties 2>/dev/null |
  grep -Fqx 'artifact.kind=clojure-source-plus-handwritten-java-classes' ||
  die "first classpath entry lacks recovered-artifact provenance"

artifact_entries=$(mktemp)
unzip -Z1 -- "$artifact" | LC_ALL=C sort >"$artifact_entries"
if grep -Eq '__init[.]class$' "$artifact_entries"; then
  die "recovered artifact contains packaged Clojure AOT initializers"
fi

mkdir -p -- "$work_root"
cp -- "$classpath_record" "$work_root/candidate-classpath.tsv"
origin_stdout="$work_root/artifact-origins.out"
origin_stderr="$work_root/artifact-origins.err"
if ! timeout --foreground --signal=TERM --kill-after=10s \
     "${timeout_seconds}s" java -XX:-UsePerfData -cp "$candidate_classpath" \
     clojure.main "$scripts_root/verify_artifact_origins.clj" \
     "$artifact" \
     "$stage2_root/inputs/origin-gate/namespaces.tsv" \
     "$stage2_root/inputs/origin-gate/peer-resource-paths.txt" \
     "$stage2_root/inputs/origin-gate/unmapped-classes.tsv" \
     >"$origin_stdout" 2>"$origin_stderr"; then
  tail -n 120 "$origin_stderr" >&2 || true
  die "candidate origin gate failed"
fi
grep -Fqx 'artifact origin and candidate classpath isolation passed' "$origin_stdout" ||
  die "candidate origin gate omitted its exact success marker"

cases=(promise-cleanup core2-async pool-rejection query-timeout)
namespaces=(stage3.promise-probe stage3.core2-async-probe
            stage3.pool-probe stage3.query-timeout-probe)

for index in "${!cases[@]}"; do
  case_name=${cases[$index]}
  namespace=${namespaces[$index]}
  stdout="$work_root/$case_name.out"
  stderr="$work_root/$case_name.err"
  marker="STAGE3-RESULT {:case :$case_name, :status :passed}"

  if ! timeout --foreground --signal=TERM --kill-after=10s \
       "${timeout_seconds}s" \
       java -XX:-UsePerfData -cp "$candidate_classpath" \
       clojure.main -m "$namespace" >"$stdout" 2>"$stderr"; then
    tail -n 120 "$stderr" >&2 || true
    die "fresh-JVM probe failed: $case_name"
  fi
  [[ "$(cat -- "$stdout")" == "$marker" ]] || {
    sed -n '1,120p' "$stdout" >&2
    die "probe stdout did not match its sole stable marker: $case_name"
  }
  printf '%s\n' "$marker"
done

{
  printf 'status=pass\n'
  printf 'case.count=%s\n' "${#cases[@]}"
  printf 'fresh.jvm.per.case=true\n'
  printf 'timeout.seconds=%s\n' "$timeout_seconds"
  printf 'stage2.evidence.manifest.sha256=%s\n' \
    "$(sha256sum "$evidence_manifest" | awk '{print $1}')"
  printf 'candidate.classpath.record.sha256=%s\n' \
    "$(sha256sum "$classpath_record" | awk '{print $1}')"
  printf 'artifact.origin.gate=true\n'
  printf 'original.peer.aot=false\n'
  printf 'original.core2.aot=false\n'
  printf 'original.nano.impl=false\n'
  printf 'sanitized.nano.role.count=%s\n' "$sanitized_nano_role_count"
  printf 'sanitized.nano.sha256=%s\n' "$expected_sanitized_nano_sha"
  printf 'sanitized.nano.bytes=%s\n' "$expected_sanitized_nano_bytes"
} >"$work_root/local-summary.properties"
