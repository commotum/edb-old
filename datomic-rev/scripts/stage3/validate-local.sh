#!/usr/bin/env bash
set -euo pipefail

usage() {
  cat >&2 <<'USAGE'
usage: validate-local.sh CANDIDATE_CLASSPATH WORK_ROOT

Runs each candidate-only Stage 3 local probe in a fresh, externally bounded
JVM. CANDIDATE_CLASSPATH must be the exact colon-delimited recovered runtime
classpath and must include the datomic-rev/scripts directory. WORK_ROOT stores
one stdout/stderr log per case plus local-summary.properties.
USAGE
  exit 2
}

die() {
  echo "stage3 local validation: $*" >&2
  exit 1
}

[[ $# -eq 2 ]] || usage
candidate_classpath=$1
work_root=$2
script_dir=$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd -P)
scripts_root=$(cd -- "$script_dir/.." && pwd -P)

[[ -n "$candidate_classpath" ]] || die "candidate classpath is empty"
[[ "$candidate_classpath" != *$'\n'* && "$candidate_classpath" != *$'\r'* ]] ||
  die "candidate classpath contains a newline"
[[ "$candidate_classpath" != *'*'* && "$candidate_classpath" != *'?'* ]] ||
  die "candidate classpath may not contain wildcards"
[[ "$work_root" == /* ]] || die "work root must be absolute"

timeout_seconds=${STAGE3_LOCAL_TIMEOUT_SECONDS:-60}
[[ "$timeout_seconds" =~ ^[0-9]+$ ]] || die "timeout must be an integer"
((timeout_seconds >= 10 && timeout_seconds <= 300)) ||
  die "timeout must be between 10 and 300 seconds"

IFS=: read -r -a classpath_entries <<<"$candidate_classpath"
((${#classpath_entries[@]} > 0)) || die "candidate classpath has no entries"
scripts_present=false
for entry in "${classpath_entries[@]}"; do
  [[ -n "$entry" ]] || die "candidate classpath contains an empty entry"
  [[ "$entry" == /* ]] || die "candidate classpath entry is not absolute: $entry"
  [[ -e "$entry" ]] || die "candidate classpath entry does not exist: $entry"
  case "${entry##*/}" in
    peer-*.jar|core2-*.jar|datomic-transactor*.jar|*transactor-pro*.jar)
      die "forbidden original implementation entered candidate classpath: $entry"
      ;;
  esac
  [[ "$entry" == "$scripts_root" ]] && scripts_present=true
done
[[ "$scripts_present" == true ]] ||
  die "candidate classpath does not contain scripts root: $scripts_root"

artifact=${classpath_entries[0]}
[[ -f "$artifact" ]] || die "first classpath entry is not the recovered artifact"
unzip -p -- "$artifact" META-INF/datomic-rev/build.properties 2>/dev/null |
  grep -Fqx 'artifact.kind=clojure-source-plus-handwritten-java-classes' ||
  die "first classpath entry lacks recovered-artifact provenance"

artifact_entries=$(mktemp)
trap 'rm -f -- "$artifact_entries"' EXIT
unzip -Z1 -- "$artifact" | LC_ALL=C sort >"$artifact_entries"
if grep -Eq '__init[.]class$' "$artifact_entries"; then
  die "recovered artifact contains packaged Clojure AOT initializers"
fi

mkdir -p -- "$work_root"

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
  printf 'original.peer.aot=false\n'
  printf 'original.core2.aot=false\n'
} >"$work_root/local-summary.properties"
