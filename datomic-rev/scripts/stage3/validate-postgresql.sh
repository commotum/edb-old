#!/usr/bin/env bash

# Safety-gated aggregate Stage 3 runner for the recovered Datomic peer.
#
# Candidate JVMs are assembled exclusively from the classpath audited by the
# Stage 2 dry-run preflight.  PostgreSQL and the licensed transactor are fresh,
# disposable external fixtures; the transactor JAR never enters that classpath.

set -euo pipefail

export LC_ALL=C
export TZ=UTC
umask 077

script_dir=$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd)
scripts_dir=$(cd -- "$script_dir/.." && pwd)
project_dir=$(cd -- "$scripts_dir/.." && pwd)
stage2_runner="$scripts_dir/stage2/validate-postgresql.sh"
stage2_logback="$scripts_dir/stage2/logback-stage2.xml"

confirmation_token=DATOMIC_STAGE3_DISPOSABLE

usage() {
  cat <<EOF
Usage:
  $0 [OPTIONS]

Required for an executing run:
  --confirm-disposable $confirmation_token

Candidate inputs:
  --datomic-home DIR          Licensed Datomic 1.0.7277 distribution used for
                              dependency JARs and the external transactor.
  --artifact JAR             Recovered source artifact. If omitted, delegate
                              canonical discovery to the Stage 2 preflight.
  --dependency-manifest TSV  Optional manifest; Stage 2 verifies it against
                              the artifact's embedded copy.
  --stub-dir DIR              Optional prebuilt four-class Hot Rod stub tree.

PostgreSQL inputs:
  --postgres-root DIR        Extracted PostgreSQL root.
  --pg-bin-dir DIR           Directory containing postgres/initdb/pg_ctl.
  --pg-lib-dir DIR           Optional directory containing libpq libraries.
  --pg-share-dir DIR         Optional initdb input directory.
  --pg-major N               PostgreSQL major below postgres-root (default: 16).
  --pg-host HOST             Must be 127.0.0.1 (default).
  --pg-port PORT             Dedicated Stage 3 port (default: 55435).
  --pg-superuser NAME        Fresh-cluster superuser (default: stage3_admin).
  --pg-user NAME             Datomic SQL role (default: datomic_stage3).
  --pg-password VALUE        Disposable password (default: datomic_stage3).
  --catalog NAME             Fresh catalog (default: datomic_stage3_peer).
  --database-name NAME       Datomic name (default: stage3-peer).

Runtime and evidence:
  --work-root DIR            Must be absent or empty, with basename beginning
                              datomic-stage3-. Default: a new mktemp directory.
  --pgdata DIR               Must be a new descendant of work-root.
  --pg-socket-dir DIR        Must be a new descendant of work-root.
  --transactor-port PORT     Dedicated Stage 3 port (default: 54339).
  --startup-timeout SEC      Service readiness deadline (default: 120).
  --local-timeout SEC        Per-local-probe deadline (default: 120).
  --probe-timeout SEC        Per-peer-probe deadline (default: 180).
  --transport-timeout SEC    Whole transport-probe deadline (default: 150).
  --transport-watchdog SEC   Forced SIGCONT deadline, 35..60 (default: 45).
  --dry-run                  Run isolation/origin/load preflight only; do not
                              start PostgreSQL or the transactor.
  -h, --help                 Show this help.
EOF
}

die() {
  echo "stage3-postgresql: $*" >&2
  exit 1
}

need_command() {
  command -v "$1" >/dev/null 2>&1 || die "missing required command: $1"
}

sha256_file() {
  sha256sum "$1" | awk '{print $1}'
}

sha256_text() {
  printf '%s' "$1" | sha256sum | awk '{print $1}'
}

require_safe_path_text() {
  local label=$1
  local value=$2
  [[ "$value" != *$'\n'* && "$value" != *$'\t'* && "$value" != *:* ]] ||
    die "$label may not contain a newline, tab, or classpath separator: $value"
}

require_identifier() {
  local label=$1
  local value=$2
  [[ "$value" =~ ^[a-z][a-z0-9_]*$ ]] ||
    die "$label must match [a-z][a-z0-9_]*: $value"
}

require_port() {
  local label=$1
  local value=$2
  [[ "$value" =~ ^[0-9]+$ ]] || die "$label is not an integer: $value"
  ((value >= 1024 && value <= 65535)) ||
    die "$label is outside 1024..65535: $value"
}

require_positive_integer() {
  local label=$1
  local value=$2
  [[ "$value" =~ ^[0-9]+$ ]] && ((value > 0)) ||
    die "$label must be a positive integer: $value"
}

assert_descendant() {
  local label=$1
  local path=$2
  case "$path" in
    "$work_root"/*) ;;
    *) die "$label must be a descendant of work-root: $path" ;;
  esac
  [[ "$path" != "$work_root" ]] || die "$label may not equal work-root"
}

marker_count() {
  local file=$1
  local prefix=$2
  awk -v prefix="$prefix" \
    'substr($0, 1, length(prefix)) == prefix {count += 1} END {print count + 0}' \
    "$file"
}

artifact_input=${DATOMIC_REV_ARTIFACT:-}
dependency_manifest_input=
stub_dir_input=
datomic_home=${DATOMIC_HOME:-}
postgres_root=${STAGE3_POSTGRES_ROOT:-}
pg_bin_dir=
pg_lib_dir=
pg_share_dir=
pg_major=16
pg_host=127.0.0.1
pg_port=55435
pg_superuser=stage3_admin
pg_user=datomic_stage3
pg_password=datomic_stage3
catalog=datomic_stage3_peer
database_name=stage3-peer
work_root_input=
pgdata_input=
pg_socket_dir_input=
transactor_port=54339
startup_timeout=120
local_timeout=120
probe_timeout=180
transport_timeout=150
transport_watchdog_seconds=45
expected_peer_canonical_sha=abe3a8e000587079b64965cb99d468ef355d14ef11bd0855aeefab4eba394510
confirmation=${STAGE3_CONFIRM_DISPOSABLE:-}
dry_run=false

while (($#)); do
  case "$1" in
    --datomic-home) datomic_home=${2:?missing value for --datomic-home}; shift 2 ;;
    --artifact) artifact_input=${2:?missing value for --artifact}; shift 2 ;;
    --dependency-manifest) dependency_manifest_input=${2:?missing value for --dependency-manifest}; shift 2 ;;
    --stub-dir) stub_dir_input=${2:?missing value for --stub-dir}; shift 2 ;;
    --postgres-root) postgres_root=${2:?missing value for --postgres-root}; shift 2 ;;
    --pg-bin-dir) pg_bin_dir=${2:?missing value for --pg-bin-dir}; shift 2 ;;
    --pg-lib-dir) pg_lib_dir=${2:?missing value for --pg-lib-dir}; shift 2 ;;
    --pg-share-dir) pg_share_dir=${2:?missing value for --pg-share-dir}; shift 2 ;;
    --pg-major) pg_major=${2:?missing value for --pg-major}; shift 2 ;;
    --pg-host) pg_host=${2:?missing value for --pg-host}; shift 2 ;;
    --pg-port) pg_port=${2:?missing value for --pg-port}; shift 2 ;;
    --pg-superuser) pg_superuser=${2:?missing value for --pg-superuser}; shift 2 ;;
    --pg-user) pg_user=${2:?missing value for --pg-user}; shift 2 ;;
    --pg-password) pg_password=${2:?missing value for --pg-password}; shift 2 ;;
    --catalog) catalog=${2:?missing value for --catalog}; shift 2 ;;
    --database-name) database_name=${2:?missing value for --database-name}; shift 2 ;;
    --work-root) work_root_input=${2:?missing value for --work-root}; shift 2 ;;
    --pgdata) pgdata_input=${2:?missing value for --pgdata}; shift 2 ;;
    --pg-socket-dir) pg_socket_dir_input=${2:?missing value for --pg-socket-dir}; shift 2 ;;
    --transactor-port) transactor_port=${2:?missing value for --transactor-port}; shift 2 ;;
    --startup-timeout) startup_timeout=${2:?missing value for --startup-timeout}; shift 2 ;;
    --local-timeout) local_timeout=${2:?missing value for --local-timeout}; shift 2 ;;
    --probe-timeout) probe_timeout=${2:?missing value for --probe-timeout}; shift 2 ;;
    --transport-timeout) transport_timeout=${2:?missing value for --transport-timeout}; shift 2 ;;
    --transport-watchdog) transport_watchdog_seconds=${2:?missing value for --transport-watchdog}; shift 2 ;;
    --confirm-disposable) confirmation=${2:?missing value for --confirm-disposable}; shift 2 ;;
    --dry-run) dry_run=true; shift ;;
    -h|--help) usage; exit 0 ;;
    *) usage >&2; die "unknown argument: $1" ;;
  esac
done

for command_name in awk chmod cmp cp date dirname find grep java kill mkdir \
  mkfifo mktemp readlink sed sha256sum sleep sort tail timeout tr uname unzip \
  uniq wc; do
  need_command "$command_name"
done

[[ "$(uname -s)" == Linux ]] ||
  die "Stage 3 process ownership and transport faults require Linux"
[[ -r /proc/self/cmdline && -r /proc/self/status ]] ||
  die "Stage 3 requires readable Linux /proc process metadata"
kill -l STOP >/dev/null 2>&1 && kill -l CONT >/dev/null 2>&1 ||
  die "Stage 3 requires STOP and CONT process signals"
for gnu_command in find readlink timeout; do
  [[ "$($gnu_command --version 2>/dev/null | sed -n '1p')" == *GNU* ]] ||
    die "Stage 3 requires GNU $gnu_command"
done

[[ -x "$stage2_runner" ]] || die "missing executable Stage 2 preflight: $stage2_runner"
[[ -f "$stage2_logback" ]] || die "missing Stage 2 logging configuration: $stage2_logback"
[[ "$pg_major" =~ ^[0-9]+$ ]] || die "pg-major is not numeric: $pg_major"
require_positive_integer startup-timeout "$startup_timeout"
require_positive_integer local-timeout "$local_timeout"
require_positive_integer probe-timeout "$probe_timeout"
require_positive_integer transport-timeout "$transport_timeout"
require_positive_integer transport-watchdog "$transport_watchdog_seconds"
((transport_watchdog_seconds >= 35 && transport_watchdog_seconds <= 60)) ||
  die "transport-watchdog must be within 35..60 seconds"
((probe_timeout >= 120)) || die "probe-timeout must be at least 120 seconds"
((transport_timeout >= 90)) || die "transport-timeout must be at least 90 seconds"
[[ "$pg_host" == 127.0.0.1 ]] || die "PostgreSQL must bind only to 127.0.0.1"
require_port pg-port "$pg_port"
require_port transactor-port "$transactor_port"
[[ "$pg_port" != "$transactor_port" ]] || die "PostgreSQL and transactor ports must differ"
require_identifier pg-superuser "$pg_superuser"
require_identifier pg-user "$pg_user"
[[ "$pg_superuser" != "$pg_user" ]] || die "PostgreSQL roles must differ"
[[ "$pg_password" =~ ^[A-Za-z0-9_.-]+$ ]] ||
  die "pg-password must use only letters, digits, dot, underscore, or hyphen"
[[ "$catalog" =~ ^datomic_stage3_[a-z0-9_]+$ ]] ||
  die "catalog must be visibly disposable and start with datomic_stage3_: $catalog"
case "$catalog" in
  postgres|template0|template1) die "refusing protected catalog: $catalog" ;;
esac
[[ "$database_name" =~ ^stage3-[a-z0-9-]+$ ]] ||
  die "database-name must start with stage3-: $database_name"

if [[ -z "$datomic_home" ]]; then
  default_datomic_home="$project_dir/../../datomic/datomic-pro-1.0.7277"
  [[ -d "$default_datomic_home" ]] && datomic_home=$default_datomic_home
fi
[[ -n "$datomic_home" && -d "$datomic_home" ]] ||
  die "provide --datomic-home pointing at datomic-pro-1.0.7277"
datomic_home=$(readlink -f -- "$datomic_home")
require_safe_path_text datomic-home "$datomic_home"
[[ -x "$datomic_home/bin/transactor" ]] ||
  die "missing external transactor launcher: $datomic_home/bin/transactor"
transactor_jar="$datomic_home/datomic-transactor-pro-1.0.7277.jar"
[[ -f "$transactor_jar" ]] || die "missing licensed transactor fixture: $transactor_jar"

if [[ -n "$artifact_input" ]]; then
  [[ -f "$artifact_input" ]] || die "missing recovered artifact: $artifact_input"
  artifact_input=$(readlink -f -- "$artifact_input")
  require_safe_path_text artifact "$artifact_input"
fi
if [[ -n "$dependency_manifest_input" ]]; then
  [[ -f "$dependency_manifest_input" ]] ||
    die "missing dependency manifest: $dependency_manifest_input"
  dependency_manifest_input=$(readlink -f -- "$dependency_manifest_input")
  require_safe_path_text dependency-manifest "$dependency_manifest_input"
fi
if [[ -n "$stub_dir_input" ]]; then
  [[ -d "$stub_dir_input" ]] || die "missing stub directory: $stub_dir_input"
  stub_dir_input=$(readlink -f -- "$stub_dir_input")
  require_safe_path_text stub-dir "$stub_dir_input"
fi

if [[ -n "$work_root_input" ]]; then
  [[ ! -L "$work_root_input" ]] || die "work-root input may not be a symbolic link"
  work_root=$(readlink -m -- "$work_root_input")
  [[ "${work_root##*/}" == datomic-stage3-* ]] ||
    die "work-root basename must start with datomic-stage3-: $work_root"
  require_safe_path_text work-root "$work_root"
  [[ ! -L "$work_root" ]] || die "work-root may not be a symbolic link"
  [[ ! -e "$work_root" || -d "$work_root" ]] ||
    die "work-root exists but is not a directory: $work_root"
  if [[ -d "$work_root" && -n "$(find "$work_root" -mindepth 1 -print -quit 2>/dev/null)" ]]; then
    die "refusing non-empty work-root: $work_root"
  fi
  mkdir -p -- "$work_root"
else
  work_root=$(mktemp -d -t datomic-stage3-run.XXXXXXXX)
fi

logs_dir="$work_root/logs"
results_dir="$work_root/results"
inputs_dir="$work_root/inputs"
runtime_dir="$work_root/runtime"
mkdir -p -- "$logs_dir" "$results_dir" "$inputs_dir" "$runtime_dir"

pgdata=$(readlink -m -- "${pgdata_input:-$runtime_dir/postgres-data}")
pg_socket_dir=$(readlink -m -- "${pg_socket_dir_input:-$runtime_dir/postgres-socket}")
for path_spec in "pgdata:$pgdata" "pg-socket-dir:$pg_socket_dir"; do
  label=${path_spec%%:*}
  value=${path_spec#*:}
  require_safe_path_text "$label" "$value"
  assert_descendant "$label" "$value"
  [[ ! -e "$value" ]] || die "$label must not exist at run start: $value"
done
[[ "$pg_socket_dir" != *[[:space:]]* ]] || die "pg-socket-dir may not contain whitespace"
((${#pg_socket_dir} < 90)) || die "pg-socket-dir is too long for PostgreSQL"

if [[ -n "$postgres_root" ]]; then
  postgres_root=$(readlink -f -- "$postgres_root")
  [[ -d "$postgres_root" ]] || die "missing postgres-root: $postgres_root"
  if [[ -z "$pg_bin_dir" ]]; then
    if [[ -d "$postgres_root/usr/lib/postgresql/$pg_major/bin" ]]; then
      pg_bin_dir="$postgres_root/usr/lib/postgresql/$pg_major/bin"
    else
      pg_bin_dir="$postgres_root/bin"
    fi
  fi
  [[ -n "$pg_lib_dir" ]] || pg_lib_dir="$postgres_root/usr/lib/x86_64-linux-gnu"
  [[ -n "$pg_share_dir" ]] || pg_share_dir="$postgres_root/usr/share/postgresql/$pg_major"
fi
if [[ -z "$pg_bin_dir" ]]; then
  postgres_command=$(command -v postgres 2>/dev/null || true)
  [[ -n "$postgres_command" ]] || die "provide --postgres-root or --pg-bin-dir"
  pg_bin_dir=$(dirname -- "$(readlink -f -- "$postgres_command")")
fi
pg_bin_dir=$(readlink -f -- "$pg_bin_dir")
require_safe_path_text pg-bin-dir "$pg_bin_dir"
if [[ -n "$pg_lib_dir" ]]; then
  [[ -d "$pg_lib_dir" ]] || die "missing PostgreSQL library directory: $pg_lib_dir"
  pg_lib_dir=$(readlink -f -- "$pg_lib_dir")
  require_safe_path_text pg-lib-dir "$pg_lib_dir"
  export LD_LIBRARY_PATH="$pg_lib_dir${LD_LIBRARY_PATH:+:$LD_LIBRARY_PATH}"
fi
if [[ -n "$pg_share_dir" ]]; then
  [[ -d "$pg_share_dir" ]] || die "missing PostgreSQL share directory: $pg_share_dir"
  pg_share_dir=$(readlink -f -- "$pg_share_dir")
  require_safe_path_text pg-share-dir "$pg_share_dir"
fi
for pg_tool in postgres initdb pg_ctl psql createdb; do
  [[ -x "$pg_bin_dir/$pg_tool" ]] || die "missing PostgreSQL tool: $pg_bin_dir/$pg_tool"
done

# Reuse the complete Stage 2 dry-run: artifact provenance, dependency hashes,
# stubs, classpath isolation, and the Stage 1 origin gate are not reimplemented.
preflight_root="$runtime_dir/datomic-stage2-p"
preflight_socket_dir="$preflight_root/p"
preflight_stdout="$logs_dir/00-stage2-preflight.out"
preflight_stderr="$logs_dir/00-stage2-preflight.err"
preflight_args=(
  --dry-run
  --work-root "$preflight_root"
  --pg-socket-dir "$preflight_socket_dir"
  --datomic-home "$datomic_home"
  --pg-host "$pg_host"
  --pg-port "$pg_port"
  --transactor-port "$transactor_port"
  --pg-major "$pg_major"
  --pg-bin-dir "$pg_bin_dir"
  --probe-timeout "$probe_timeout"
)
[[ -z "$artifact_input" ]] || preflight_args+=(--artifact "$artifact_input")
[[ -z "$dependency_manifest_input" ]] ||
  preflight_args+=(--dependency-manifest "$dependency_manifest_input")
[[ -z "$stub_dir_input" ]] || preflight_args+=(--stub-dir "$stub_dir_input")
[[ -z "$pg_lib_dir" ]] || preflight_args+=(--pg-lib-dir "$pg_lib_dir")
[[ -z "$pg_share_dir" ]] || preflight_args+=(--pg-share-dir "$pg_share_dir")

if ! timeout --foreground --signal=TERM --kill-after=15s 600s \
  "$stage2_runner" "${preflight_args[@]}" \
  >"$preflight_stdout" 2>"$preflight_stderr"; then
  tail -n 100 "$preflight_stdout" >&2 || true
  tail -n 100 "$preflight_stderr" >&2 || true
  die "Stage 2 dry-run isolation preflight failed"
fi
grep -Fqx 'Stage 2 PostgreSQL dry-run validation passed' "$preflight_stdout" ||
  die "Stage 2 preflight omitted its exact success marker"
grep -Fqx 'candidate.boundary.validated=true' "$preflight_root/run-status.properties" ||
  die "Stage 2 preflight did not validate the candidate boundary"

preflight_evidence_manifest="$preflight_root/evidence.sha256"
[[ -f "$preflight_evidence_manifest" && ! -L "$preflight_evidence_manifest" ]] ||
  die "Stage 2 preflight omitted its evidence manifest"
(cd -- "$preflight_root" && sha256sum -c evidence.sha256) \
  >"$logs_dir/00-stage2-evidence-check.out" ||
  die "Stage 2 preflight evidence verification failed"

preflight_copy="$inputs_dir/stage2-preflight"
mkdir -p -- "$preflight_copy"
cp -- "$preflight_evidence_manifest" "$preflight_copy/evidence.sha256"
preflight_evidence_count=0
while IFS=' ' read -r expected_sha evidence_path extra; do
  [[ "$expected_sha" =~ ^[0-9a-f]{64}$ && -n "$evidence_path" && -z "${extra:-}" ]] ||
    die "malformed Stage 2 preflight evidence row"
  case "$evidence_path" in
    /*|..|../*|*/..|*/../*)
      die "unsafe Stage 2 preflight evidence path: $evidence_path"
      ;;
  esac
  preflight_source="$preflight_root/$evidence_path"
  [[ -f "$preflight_source" && ! -L "$preflight_source" ]] ||
    die "Stage 2 preflight evidence is missing or symbolic: $evidence_path"
  [[ "$(sha256_file "$preflight_source")" == "$expected_sha" ]] ||
    die "Stage 2 preflight evidence changed while copying: $evidence_path"
  mkdir -p -- "$preflight_copy/$(dirname -- "$evidence_path")"
  cp -- "$preflight_source" "$preflight_copy/$evidence_path"
  ((preflight_evidence_count += 1))
done <"$preflight_evidence_manifest"
((preflight_evidence_count > 0)) || die "Stage 2 preflight evidence manifest is empty"
(cd -- "$preflight_copy" && sha256sum -c evidence.sha256) \
  >"$logs_dir/00-stage2-evidence-copy-check.out" ||
  die "copied Stage 2 preflight evidence verification failed"

preflight_harness_manifest="$preflight_root/inputs/stage2-harness.tsv"
[[ -f "$preflight_harness_manifest" && ! -L "$preflight_harness_manifest" ]] ||
  die "Stage 2 preflight omitted its harness manifest"
[[ -z "$(find "$scripts_dir" -type l -print -quit)" ]] ||
  die "shared scripts classpath root may not contain symbolic links"
harness_recheck="$inputs_dir/stage2-harness-recheck.tsv"
printf 'sha256\tpath\n' >"$harness_recheck"
while IFS= read -r harness_path; do
  harness_name=${harness_path#"$scripts_dir/"}
  printf '%s\t%s\n' "$(sha256_file "$harness_path")" "$harness_name" \
    >>"$harness_recheck"
done < <(find "$scripts_dir" -type f -print | sort)
cmp -s "$preflight_harness_manifest" "$harness_recheck" ||
  die "shared scripts classpath changed after the Stage 2 preflight"

preflight_classpath="$preflight_root/candidate-classpath.tsv"
[[ -f "$preflight_classpath" ]] || die "Stage 2 preflight omitted candidate-classpath.tsv"
[[ "$(sed -n '1p' "$preflight_classpath")" == $'position\trole\tsha256\tpath' ]] ||
  die "candidate classpath record has the wrong header"
[[ "$(wc -l <"$preflight_classpath")" -eq 536 ]] ||
  die "candidate classpath record must contain exactly 535 entries"
awk -F '\t' 'NR > 1 {
  expected = NR - 1
  if (NF != 4 || $1 != expected || $3 !~ /^[0-9a-f]{64}$/ || $4 == "") exit 1
} END {if (NR != 536) exit 1}' "$preflight_classpath" ||
  die "candidate classpath positions or rows are malformed"

candidate_entries=()
position=0
while IFS=$'\t' read -r recorded_position role expected_sha entry extra; do
  [[ "$recorded_position" == position ]] && continue
  [[ -n "$recorded_position" && -n "$role" && -n "$expected_sha" && -n "$entry" && -z "${extra:-}" ]] ||
    die "malformed candidate classpath row"
  ((position += 1))
  [[ "$recorded_position" -eq "$position" ]] || die "candidate classpath position discontinuity"
  require_safe_path_text candidate-classpath-entry "$entry"
  [[ "$entry" != *'*'* && "$entry" != *'?'* ]] || die "classpath wildcards are forbidden"
  if [[ "$position" -eq 2 ]]; then
    [[ "$role" == stage2-test-harness && "$entry" == "$scripts_dir" ]] ||
      die "candidate harness root differs from the shared scripts directory"
    [[ "$expected_sha" == "$(sha256_file "$preflight_harness_manifest")" ]] ||
      die "candidate harness hash differs from the exhaustive preflight manifest"
  fi
  if [[ "$position" -eq 3 ]]; then
    [[ "$role" == compile-only-hotrod-stubs && -d "$entry" ]] ||
      die "candidate Hot Rod stub tree differs from the Stage 2 preflight"
    preflight_stub_manifest="$preflight_root/inputs/infinispan-stubs.tsv"
    [[ -f "$preflight_stub_manifest" && ! -L "$preflight_stub_manifest" ]] ||
      die "Stage 2 preflight omitted its Hot Rod stub manifest"
    [[ "$expected_sha" == "$(sha256_file "$preflight_stub_manifest")" ]] ||
      die "candidate Hot Rod stub manifest hash differs from the classpath record"
    [[ -z "$(find "$entry" -type l -print -quit)" ]] ||
      die "candidate Hot Rod stub tree contains a symbolic link"
    stub_recheck="$inputs_dir/infinispan-stubs-recheck.tsv"
    printf 'sha256\tpath\n' >"$stub_recheck"
    while IFS= read -r stub_path; do
      printf '%s\t%s\n' "$(sha256_file "$entry/$stub_path")" "$stub_path" \
        >>"$stub_recheck"
    done < <(find "$entry" -type f -printf '%P\n' | sort)
    cmp -s "$preflight_stub_manifest" "$stub_recheck" ||
      die "candidate Hot Rod stub tree changed after the Stage 2 preflight"
  fi
  if ((position >= 4)); then
    case "$entry" in
      "$datomic_home"/lib/*) ;;
      *) die "candidate dependency escaped the licensed lib directory: $entry" ;;
    esac
    case "${entry##*/}" in
      peer-*.jar|core2-*.jar|datomic-transactor*.jar|*transactor-pro*.jar)
        die "forbidden original implementation entered candidate classpath: $entry"
        ;;
    esac
  fi
  [[ -f "$entry" || -d "$entry" ]] || die "candidate classpath entry disappeared: $entry"
  if [[ -f "$entry" ]]; then
    [[ "$(sha256_file "$entry")" == "$expected_sha" ]] ||
      die "candidate classpath file changed after Stage 2 preflight: $entry"
  fi
  candidate_entries+=("$entry")
done <"$preflight_classpath"
[[ "$position" -eq 535 && "${#candidate_entries[@]}" -eq 535 ]] ||
  die "candidate classpath cardinality changed after preflight"

artifact_jar=${candidate_entries[0]}
[[ -f "$artifact_jar" ]] || die "preflight artifact path is not a file"
[[ "${artifact_jar##*/}" == datomic-rev-peer-1.0.7277-source.jar ]] ||
  die "preflight selected an unexpected artifact: $artifact_jar"
[[ -z "$artifact_input" || "$artifact_jar" == "$artifact_input" ]] ||
  die "preflight selected a different artifact than requested"

cp -- "$preflight_classpath" "$work_root/candidate-classpath.tsv"
cp -- "$preflight_root/results/01-artifact-origins.result" \
  "$results_dir/00-artifact-origins.result"
mkdir -p -- "$inputs_dir/stage2-origin-evidence"
cp -- "$preflight_root/inputs/origin-gate/"* "$inputs_dir/stage2-origin-evidence/"
cp -- "$preflight_root/inputs/artifact-build.properties" "$inputs_dir/"
cp -- "$preflight_root/inputs/artifact-dependencies.tsv" "$inputs_dir/"
cp -- "$preflight_root/inputs/infinispan-stubs.tsv" "$inputs_dir/"
cmp -s "$results_dir/00-artifact-origins.result" \
  "$preflight_root/results/01-artifact-origins.result" ||
  die "copied origin evidence differs from Stage 2 preflight"

candidate_classpath=$(IFS=:; echo "${candidate_entries[*]}")
candidate_java=(
  java
  -XX:-UsePerfData
  -Ddatomic.peerConnectionTTLMsec=10000
  -Ddatomic.txTimeoutMsec=10000
  -Ddatomic.queryPool=2
  "-Dlogback.configurationFile=$stage2_logback"
  -cp "$candidate_classpath"
  clojure.main
)

harness_manifest="$inputs_dir/stage3-harness.tsv"
printf 'sha256\tpath\n' >"$harness_manifest"
while IFS= read -r harness_path; do
  harness_name=${harness_path#"$scripts_dir/"}
  printf '%s\t%s\n' "$(sha256_file "$harness_path")" "$harness_name" >>"$harness_manifest"
done < <(find "$script_dir" -maxdepth 1 -type f -print | sort)

required_harness_files=(
  core2_async_probe.clj
  peer_probe.clj
  pool_probe.clj
  promise_probe.clj
  query_timeout_probe.clj
  support.clj
  transport_probe.clj
  validate-local.sh
  validate-postgresql.sh
)
for harness_file in "${required_harness_files[@]}"; do
  [[ -f "$script_dir/$harness_file" ]] || die "missing Stage 3 harness file: $harness_file"
  grep -Fq $'\tstage3/'"$harness_file" "$harness_manifest" ||
    die "Stage 3 harness manifest omitted: $harness_file"
done
[[ ! -e "$scripts_dir/datomic" ]] ||
  die "shared harness classpath root may not contain a loose datomic namespace"

load_stdout="$logs_dir/01-stage3-load.out"
load_stderr="$logs_dir/01-stage3-load.err"
if ! timeout --foreground --signal=TERM --kill-after=15s "$local_timeout"s \
  "${candidate_java[@]}" -e \
  "(doseq [n '[stage3.support stage3.promise-probe stage3.core2-async-probe stage3.pool-probe stage3.query-timeout-probe stage3.peer-probe stage3.transport-probe]] (require n)) (println :stage3/namespaces-loaded)" \
  >"$load_stdout" 2>"$load_stderr"; then
  tail -n 100 "$load_stdout" >&2 || true
  tail -n 100 "$load_stderr" >&2 || true
  die "Stage 3 namespace load gate failed"
fi
grep -Fqx ':stage3/namespaces-loaded' "$load_stdout" ||
  die "Stage 3 namespace load gate omitted its marker"
cp -- "$load_stdout" "$results_dir/01-stage3-load.result"

postgres_version=$("$pg_bin_dir/postgres" --version)
password_sha=$(sha256_text "$pg_password")
config_record="$work_root/config.properties"
{
  printf 'stage=3\n'
  printf 'backend=postgresql\n'
  printf 'artifact.path=%s\n' "$artifact_jar"
  printf 'artifact.sha256=%s\n' "$(sha256_file "$artifact_jar")"
  printf 'stage2.preflight.runner.sha256=%s\n' "$(sha256_file "$stage2_runner")"
  printf 'stage2.preflight.origin-result.sha256=%s\n' "$(sha256_file "$results_dir/00-artifact-origins.result")"
  printf 'stage2.preflight.evidence.manifest.sha256=%s\n' "$(sha256_file "$preflight_copy/evidence.sha256")"
  printf 'stage2.preflight.evidence.file.count=%s\n' "$preflight_evidence_count"
  printf 'stage3.harness.manifest.sha256=%s\n' "$(sha256_file "$harness_manifest")"
  printf 'candidate.classpath.entry.count=%s\n' "${#candidate_entries[@]}"
  printf 'candidate.original.peer=false\n'
  printf 'candidate.original.core2=false\n'
  printf 'candidate.original.transactor=false\n'
  printf 'candidate.peer-connection-ttl-msec=10000\n'
  printf 'candidate.tx-timeout-msec=10000\n'
  printf 'candidate.query-pool=2\n'
  printf 'transactor.role=external-fixture-only\n'
  printf 'transactor.fixture.jar.sha256=%s\n' "$(sha256_file "$transactor_jar")"
  printf 'transactor.fixture.launcher.sha256=%s\n' "$(sha256_file "$datomic_home/bin/transactor")"
  printf 'postgres.version=%s\n' "$postgres_version"
  printf 'postgres.binary.sha256=%s\n' "$(sha256_file "$pg_bin_dir/postgres")"
  printf 'postgres.initdb.sha256=%s\n' "$(sha256_file "$pg_bin_dir/initdb")"
  printf 'postgres.psql.sha256=%s\n' "$(sha256_file "$pg_bin_dir/psql")"
  printf 'postgres.host=%s\n' "$pg_host"
  printf 'postgres.port=%s\n' "$pg_port"
  printf 'postgres.superuser=%s\n' "$pg_superuser"
  printf 'postgres.user=%s\n' "$pg_user"
  printf 'postgres.password.sha256=%s\n' "$password_sha"
  printf 'postgres.catalog=%s\n' "$catalog"
  printf 'datomic.database.name=%s\n' "$database_name"
  printf 'transactor.port=%s\n' "$transactor_port"
  printf 'transport.watchdog.seconds=%s\n' "$transport_watchdog_seconds"
  printf 'work.root=%s\n' "$work_root"
  printf 'stage2.preflight.root=%s\n' "$preflight_root"
} >"$config_record"

write_evidence_hashes() {
  local output="$work_root/evidence.sha256"
  (
    cd -- "$work_root"
    find inputs logs results -type f -print
    printf '%s\n' candidate-classpath.tsv config.properties run-status.properties stage-3-summary.properties
  ) | sort | while IFS= read -r evidence_file; do
    if [[ -f "$work_root/$evidence_file" ]]; then
      (cd -- "$work_root" && sha256sum "$evidence_file")
    fi
  done >"$output"
}

if [[ "$dry_run" == true ]]; then
  {
    printf 'status=dry-run\n'
    printf 'database.processes.started=false\n'
    printf 'candidate.boundary.validated=true\n'
    printf 'stage3.namespaces.loaded=true\n'
  } >"$work_root/run-status.properties"
  write_evidence_hashes
  echo "Stage 3 PostgreSQL dry-run validation passed"
  echo "candidate classpath: exact Stage 2-audited 535 entries"
  echo "no PostgreSQL or Datomic transactor process was started"
  echo "evidence: $work_root"
  exit 0
fi

[[ "$confirmation" == "$confirmation_token" ]] ||
  die "executing run requires --confirm-disposable $confirmation_token"

port_is_open() {
  local host=$1
  local port=$2
  (exec 9<>"/dev/tcp/$host/$port") >/dev/null 2>&1
}

port_is_open "$pg_host" "$pg_port" && die "PostgreSQL port is already in use: $pg_host:$pg_port"
port_is_open "$pg_host" "$transactor_port" &&
  die "transactor port is already in use: $pg_host:$transactor_port"

pg_started=false
transactor_pid=
transactor_properties=
transactor_paused=false
transport_probe_pid=
transport_watchdog_pid=
transport_fd=
pause_state_file="$runtime_dir/transactor-pause.state"
current_step=local-probes

verify_transactor_process() {
  [[ -n "$transactor_pid" ]] || return 1
  kill -0 "$transactor_pid" 2>/dev/null || return 1
  local command_line
  command_line=$(tr '\0' ' ' <"/proc/$transactor_pid/cmdline" 2>/dev/null || true)
  [[ "$command_line" == *datomic.launcher* &&
     -n "$transactor_properties" &&
     "$command_line" == *"$transactor_properties"* ]]
}

resume_transactor_owned() {
  [[ -n "$transactor_pid" ]] || return 0
  kill -0 "$transactor_pid" 2>/dev/null || {
    transactor_paused=false
    return 0
  }
  verify_transactor_process || {
    echo "refusing to SIGCONT unverified process $transactor_pid" >&2
    return 1
  }
  kill -CONT "$transactor_pid" || return 1
  transactor_paused=false
  printf 'resumed\n' >"$pause_state_file"
  for ((resume_index = 0; resume_index < 5; resume_index += 1)); do
    process_state=$(awk '/^State:/ {print $2; exit}' "/proc/$transactor_pid/status" 2>/dev/null || true)
    [[ "$process_state" != T && "$process_state" != t ]] && return 0
    sleep 1
  done
  echo "transactor remained stopped after SIGCONT: $transactor_pid" >&2
  return 1
}

stop_transport_watchdog() {
  [[ -n "$transport_watchdog_pid" ]] || return 0
  [[ -f "$pause_state_file" ]] && printf 'resumed\n' >"$pause_state_file"
  wait "$transport_watchdog_pid" 2>/dev/null || true
  transport_watchdog_pid=
}

stop_transport_probe() {
  if [[ -n "$transport_fd" ]]; then
    exec {transport_fd}>&- || true
    transport_fd=
  fi
  [[ -n "$transport_probe_pid" ]] || return 0
  if kill -0 "$transport_probe_pid" 2>/dev/null; then
    kill -TERM "$transport_probe_pid" 2>/dev/null || true
  fi
  wait "$transport_probe_pid" 2>/dev/null || true
  transport_probe_pid=
}

stop_transactor() {
  [[ -n "$transactor_pid" ]] || return 0
  if kill -0 "$transactor_pid" 2>/dev/null; then
    # SIGCONT is deliberately unconditional and precedes every stop attempt.
    resume_transactor_owned || return 1
    verify_transactor_process || {
      echo "refusing to stop unverified process $transactor_pid" >&2
      return 1
    }
    kill -INT "$transactor_pid"
    for ((wait_index = 0; wait_index < 30; wait_index += 1)); do
      kill -0 "$transactor_pid" 2>/dev/null || break
      sleep 1
    done
    if kill -0 "$transactor_pid" 2>/dev/null; then
      kill -TERM "$transactor_pid"
      for ((wait_index = 0; wait_index < 10; wait_index += 1)); do
        kill -0 "$transactor_pid" 2>/dev/null || break
        sleep 1
      done
    fi
    kill -0 "$transactor_pid" 2>/dev/null && {
      echo "external transactor did not stop: $transactor_pid" >&2
      return 1
    }
  fi
  wait "$transactor_pid" 2>/dev/null || true
  transactor_pid=
  transactor_properties=
  transactor_paused=false
}

stop_postgres() {
  [[ "$pg_started" == true ]] || return 0
  "$pg_bin_dir/pg_ctl" -D "$pgdata" -m fast -w stop \
    >>"$logs_dir/postgres-control.log" 2>&1 || return 1
  pg_started=false
}

cleanup() {
  local status=$?
  trap - EXIT
  set +e
  resume_transactor_owned
  stop_transport_watchdog
  stop_transport_probe
  stop_transactor
  stop_postgres
  if ((status != 0)); then
    {
      printf 'status=failed\n'
      printf 'last.step=%s\n' "$current_step"
      printf 'stage.complete=false\n'
    } >"$work_root/run-status.properties"
    write_evidence_hashes
    echo "Stage 3 PostgreSQL run failed during $current_step; evidence retained at $work_root" >&2
  fi
  exit "$status"
}
trap cleanup EXIT

last_result_file=

run_stage3_case() {
  local label=$1
  local timeout_seconds=$2
  shift 2
  local stdout_file="$logs_dir/$label.out"
  local stderr_file="$logs_dir/$label.err"
  local result_file="$results_dir/$label.result"
  current_step=$label
  if ! timeout --foreground --signal=TERM --kill-after=15s "$timeout_seconds"s \
    "$@" >"$stdout_file" 2>"$stderr_file"; then
    tail -n 100 "$stdout_file" >&2 || true
    tail -n 100 "$stderr_file" >&2 || true
    die "Stage 3 candidate case failed: $label"
  fi
  [[ "$(marker_count "$stdout_file" 'STAGE3-RESULT ')" -eq 1 ]] ||
    die "$label did not emit exactly one STAGE3-RESULT"
  [[ "$(marker_count "$stderr_file" 'STAGE3-ERROR ')" -eq 0 ]] ||
    die "$label emitted STAGE3-ERROR despite a zero exit status"
  awk '/^STAGE3-RESULT / {print}' "$stdout_file" >"$result_file"
  last_result_file=$result_file
}

# Local cases run before any database service and each receives a fresh JVM.
run_stage3_case 10-promise "$local_timeout" \
  "${candidate_java[@]}" -m stage3.promise-probe
promise_result=$last_result_file
grep -Fq ':status :passed' "$promise_result" || die "promise probe omitted passed status"

run_stage3_case 11-core2-async "$local_timeout" \
  "${candidate_java[@]}" -m stage3.core2-async-probe
core2_async_result=$last_result_file
grep -Fq ':status :passed' "$core2_async_result" || die "core2 async probe omitted passed status"

run_stage3_case 12-pool "$local_timeout" \
  "${candidate_java[@]}" -m stage3.pool-probe
pool_result=$last_result_file
grep -Fq ':status :passed' "$pool_result" || die "pool probe omitted passed status"

run_stage3_case 13-query-timeout "$local_timeout" \
  "${candidate_java[@]}" -m stage3.query-timeout-probe
query_timeout_result=$last_result_file
grep -Fq ':status :passed' "$query_timeout_result" || die "query timeout probe omitted passed status"

current_step=postgres-initdb
mkdir -p -- "$pg_socket_dir"
initdb_args=(
  -D "$pgdata"
  -U "$pg_superuser"
  --auth-local=trust
  --auth-host=scram-sha-256
  --encoding=UTF8
  --locale=C
  --no-instructions
)
[[ -z "$pg_share_dir" ]] || initdb_args+=(-L "$pg_share_dir")
"$pg_bin_dir/initdb" "${initdb_args[@]}" \
  >"$logs_dir/postgres-initdb.out" 2>"$logs_dir/postgres-initdb.err"

current_step=postgres-start
postgres_options="-h $pg_host -p $pg_port -k $pg_socket_dir -c timezone=UTC -c log_line_prefix="
pg_started=true
if ! "$pg_bin_dir/pg_ctl" -D "$pgdata" -w -l "$logs_dir/postgres-server.log" \
  -o "$postgres_options" start >"$logs_dir/postgres-control.log" 2>&1; then
  die "disposable PostgreSQL fixture did not start"
fi

current_step=postgres-provision
"$pg_bin_dir/psql" -X -v ON_ERROR_STOP=1 \
  -h "$pg_socket_dir" -p "$pg_port" -U "$pg_superuser" -d postgres \
  --set=stage3_role="$pg_user" --set=stage3_password="$pg_password" \
  >"$logs_dir/postgres-role.out" 2>"$logs_dir/postgres-role.err" <<'SQL'
CREATE ROLE :"stage3_role" LOGIN PASSWORD :'stage3_password';
SQL
"$pg_bin_dir/createdb" -h "$pg_socket_dir" -p "$pg_port" -U "$pg_superuser" \
  --owner="$pg_user" "$catalog" \
  >"$logs_dir/postgres-createdb.out" 2>"$logs_dir/postgres-createdb.err"
"$pg_bin_dir/psql" -X -v ON_ERROR_STOP=1 \
  -h "$pg_socket_dir" -p "$pg_port" -U "$pg_superuser" -d "$catalog" \
  --set=stage3_owner="$pg_user" \
  >"$logs_dir/postgres-schema.out" 2>"$logs_dir/postgres-schema.err" <<'SQL'
CREATE TABLE public.datomic_kvs (
  id text PRIMARY KEY,
  rev integer,
  map text,
  val bytea
);
ALTER TABLE public.datomic_kvs OWNER TO :"stage3_owner";
SQL

initial_rows=$("$pg_bin_dir/psql" -X -A -t -v ON_ERROR_STOP=1 \
  -h "$pg_socket_dir" -p "$pg_port" -U "$pg_superuser" -d "$catalog" \
  -c 'SELECT count(*) FROM public.datomic_kvs;')
[[ "$initial_rows" == 0 ]] || die "fresh Stage 3 catalog is unexpectedly nonempty"
printf 'catalog=%s\ninitial-row-count=%s\n' "$catalog" "$initial_rows" \
  >"$results_dir/20-catalog-precondition.result"

write_transactor_properties() {
  local properties="$runtime_dir/transactor-stage3.properties"
  local data_dir="$runtime_dir/transactor-stage3-data"
  local log_dir="$runtime_dir/transactor-stage3-log"
  mkdir -p -- "$data_dir" "$log_dir"
  {
    printf 'protocol=sql\n'
    printf 'host=%s\n' "$pg_host"
    printf 'port=%s\n' "$transactor_port"
    printf '\n'
    printf 'sql-url=jdbc:postgresql://%s:%s/%s\n' "$pg_host" "$pg_port" "$catalog"
    printf 'sql-user=%s\n' "$pg_user"
    printf 'sql-password=%s\n' "$pg_password"
    printf 'sql-driver-class=org.postgresql.Driver\n'
    printf 'sql-validation-query=select 1\n'
    printf '\n'
    printf 'memory-index-threshold=32m\n'
    printf 'memory-index-max=256m\n'
    printf 'object-cache-max=128m\n'
    printf 'encrypt-channel=false\n'
    printf '\n'
    printf 'data-dir=%s\n' "$data_dir"
    printf 'log-dir=%s\n' "$log_dir"
    printf 'pid-file=%s\n' "$runtime_dir/transactor-stage3.pid"
  } >"$properties"
  chmod 600 "$properties"
  printf '%s' "$properties"
}

start_transactor() {
  local output_log="$logs_dir/transactor-stage3.out"
  [[ -z "$transactor_pid" ]] || die "attempted to overlap transactor fixtures"
  port_is_open "$pg_host" "$transactor_port" &&
    die "transactor port became occupied before fixture start"
  transactor_properties=$(write_transactor_properties)
  "$datomic_home/bin/transactor" -Xms128m -Xmx512m "$transactor_properties" \
    >"$output_log" 2>&1 &
  transactor_pid=$!
  for ((wait_index = 0; wait_index < startup_timeout; wait_index += 1)); do
    if grep -Fq 'System started' "$output_log" &&
       verify_transactor_process &&
       port_is_open "$pg_host" "$transactor_port"; then
      return 0
    fi
    kill -0 "$transactor_pid" 2>/dev/null || {
      tail -n 100 "$output_log" >&2
      die "external transactor exited before readiness"
    }
    sleep 1
  done
  tail -n 100 "$output_log" >&2
  die "external transactor did not become ready within $startup_timeout seconds"
}

current_step=transactor-start
start_transactor

sql_uri=$(printf 'datomic:sql://%s?jdbc:postgresql://%s:%s/%s?user=%s&password=%s' \
  "$database_name" "$pg_host" "$pg_port" "$catalog" "$pg_user" "$pg_password")

run_stage3_case 30-peer-seed "$probe_timeout" \
  "${candidate_java[@]}" -m stage3.peer-probe seed "$sql_uri" true
peer_seed_result=$last_result_file
grep -Fq ':mode :seed' "$peer_seed_result" &&
  grep -Fq ':created? true' "$peer_seed_result" &&
  grep -Fq ':baseline-count 32' "$peer_seed_result" ||
  die "peer seed result is not canonical"

run_stage3_case 31-peer-query-controls "$probe_timeout" \
  "${candidate_java[@]}" -m stage3.peer-probe query-controls "$sql_uri"
peer_query_result=$last_result_file
grep -Fq ':mode :query-controls' "$peer_query_result" &&
  grep -Fq ':configured-timeout-ms 30' "$peer_query_result" &&
  grep -Fq ':marker-count 1' "$peer_query_result" ||
  die "peer query-control result is not canonical"

run_stage3_case 32-peer-lifecycle-race "$probe_timeout" \
  "${candidate_java[@]}" -m stage3.peer-probe lifecycle-race "$sql_uri"
peer_lifecycle_result=$last_result_file
grep -Fq ':mode :lifecycle-race' "$peer_lifecycle_result" &&
  grep -Fq ':held-snapshot-count 4' "$peer_lifecycle_result" &&
  grep -Fq ':late-rejection-count 4' "$peer_lifecycle_result" &&
  grep -Fq ':reconnected-object-distinct? true' "$peer_lifecycle_result" ||
  die "peer lifecycle-race result is not canonical"

run_stage3_case 33-peer-contention "$probe_timeout" \
  "${candidate_java[@]}" -m stage3.peer-probe contention "$sql_uri"
peer_contention_result=$last_result_file
grep -Fq ':mode :contention' "$peer_contention_result" &&
  grep -Fq ':worker-count 8' "$peer_contention_result" &&
  grep -Fq ':success-count 8' "$peer_contention_result" &&
  grep -Fq ':cas-conflict-count 56' "$peer_contention_result" &&
  grep -Fq ':counter 8' "$peer_contention_result" &&
  grep -Fq ':contention-event-count 8' "$peer_contention_result" ||
  die "peer contention result is not canonical"

run_stage3_case 34-peer-audit "$probe_timeout" \
  "${candidate_java[@]}" -m stage3.peer-probe audit "$sql_uri"
peer_audit_result=$last_result_file
grep -Fq ':mode :audit' "$peer_audit_result" &&
  grep -Fq ':counter-history-count 17' "$peer_audit_result" &&
  grep -Fq ':counter 8' "$peer_audit_result" ||
  die "peer audit result is not canonical"
contention_canonical=$(sed -nE 's/.*:canonical-sha256 "([0-9a-f]{64})".*/\1/p' "$peer_contention_result")
audit_canonical=$(sed -nE 's/.*:canonical-sha256 "([0-9a-f]{64})".*/\1/p' "$peer_audit_result")
[[ "$contention_canonical" =~ ^[0-9a-f]{64}$ &&
   "$audit_canonical" == "$contention_canonical" ]] ||
  die "contention and audit canonical hashes differ"
[[ "$audit_canonical" == "$expected_peer_canonical_sha" ]] ||
  die "peer audit canonical hash differs from the fixed Stage 3 workload"

wait_for_marker() {
  local file=$1
  local prefix=$2
  local max_seconds=$3
  local process_pid=$4
  for ((marker_wait = 0; marker_wait < max_seconds; marker_wait += 1)); do
    if [[ -f "$file" && "$(marker_count "$file" "$prefix")" -eq 1 ]]; then
      return 0
    fi
    if ! kill -0 "$process_pid" 2>/dev/null; then
      return 1
    fi
    sleep 1
  done
  return 1
}

start_transport_watchdog() {
  local owned_pid=$transactor_pid
  local owned_properties=$transactor_properties
  local watchdog_log="$logs_dir/40-transport-watchdog.log"
  (
    for ((watchdog_tick = 0; watchdog_tick < transport_watchdog_seconds; watchdog_tick += 1)); do
      sleep 1
      [[ "$(sed -n '1p' "$pause_state_file" 2>/dev/null || true)" == paused ]] || exit 0
    done
    command_line=$(tr '\0' ' ' <"/proc/$owned_pid/cmdline" 2>/dev/null || true)
    if kill -0 "$owned_pid" 2>/dev/null &&
       [[ "$command_line" == *datomic.launcher* &&
          "$command_line" == *"$owned_properties"* ]]; then
      kill -CONT "$owned_pid"
      printf 'watchdog-resumed\n' >"$pause_state_file"
      echo "watchdog issued SIGCONT to verified transactor PID $owned_pid"
    else
      echo "watchdog refused to signal an unverified transactor PID $owned_pid" >&2
      exit 1
    fi
  ) >"$watchdog_log" 2>&1 &
  transport_watchdog_pid=$!
}

current_step=40-transport-interruption
transport_fifo="$runtime_dir/transport-control.fifo"
[[ ! -e "$transport_fifo" ]] || die "transport FIFO path already exists"
mkfifo -m 600 "$transport_fifo"
exec {transport_fd}<>"$transport_fifo"
transport_stdout="$logs_dir/40-transport.out"
transport_stderr="$logs_dir/40-transport.err"
timeout --foreground --signal=TERM --kill-after=15s "$transport_timeout"s \
  "${candidate_java[@]}" -m stage3.transport-probe "$sql_uri" \
  <"$transport_fifo" >"$transport_stdout" 2>"$transport_stderr" &
transport_probe_pid=$!

if ! wait_for_marker "$transport_stdout" 'STAGE3-FAULT-READY ' \
  "$startup_timeout" "$transport_probe_pid"; then
  tail -n 100 "$transport_stdout" >&2 || true
  tail -n 100 "$transport_stderr" >&2 || true
  die "transport probe did not become fault-ready"
fi
verify_transactor_process || die "transactor ownership changed before SIGSTOP"
kill -STOP "$transactor_pid"
transactor_paused=true
printf 'paused\n' >"$pause_state_file"
pause_started_seconds=$SECONDS
for ((stop_check = 0; stop_check < 5; stop_check += 1)); do
  process_state=$(awk '/^State:/ {print $2; exit}' "/proc/$transactor_pid/status" 2>/dev/null || true)
  [[ "$process_state" == T || "$process_state" == t ]] && break
  sleep 1
done
[[ "$process_state" == T || "$process_state" == t ]] ||
  die "verified transactor did not enter stopped state after SIGSTOP"
start_transport_watchdog
printf 'FAULT\n' >&"$transport_fd"

if ! wait_for_marker "$transport_stdout" 'STAGE3-FAULT-UNAVAILABLE ' 35 "$transport_probe_pid"; then
  tail -n 100 "$transport_stdout" >&2 || true
  tail -n 100 "$transport_stderr" >&2 || true
  die "transport probe did not report unavailable within its bound"
fi
wait_for_marker "$transport_stdout" 'STAGE3-RESUME-READY ' 5 "$transport_probe_pid" ||
  die "transport probe did not request RESUME"
pause_elapsed=$((SECONDS - pause_started_seconds))
if ((pause_elapsed < 11)); then
  sleep $((11 - pause_elapsed))
fi
[[ "$(sed -n '1p' "$pause_state_file")" == paused ]] ||
  die "transport SIGCONT watchdog fired before normal recovery orchestration"
resume_transactor_owned || die "could not SIGCONT verified transactor"
stop_transport_watchdog
verify_transactor_process || die "transactor ownership changed after SIGCONT"
port_is_open "$pg_host" "$transactor_port" || die "transactor port was not ready after SIGCONT"
printf 'RESUME\n' >&"$transport_fd"

set +e
wait "$transport_probe_pid"
transport_status=$?
set -e
transport_probe_pid=
exec {transport_fd}>&-
transport_fd=
[[ "$transport_status" -eq 0 ]] || {
  tail -n 100 "$transport_stdout" >&2 || true
  tail -n 100 "$transport_stderr" >&2 || true
  die "transport probe failed with status $transport_status"
}

for marker_prefix in \
  'STAGE3-FAULT-READY ' \
  'STAGE3-FAULT-SYNC-START ' \
  'STAGE3-FAULT-UNAVAILABLE ' \
  'STAGE3-RESUME-READY ' \
  'STAGE3-RECOVERY-SYNC-START ' \
  'STAGE3-SENTINEL-START ' \
  'STAGE3-RESULT '; do
  [[ "$(marker_count "$transport_stdout" "$marker_prefix")" -eq 1 ]] ||
    die "transport output did not contain exactly one $marker_prefix marker"
done
previous_marker_line=0
for marker_prefix in \
  'STAGE3-FAULT-READY ' \
  'STAGE3-FAULT-SYNC-START ' \
  'STAGE3-FAULT-UNAVAILABLE ' \
  'STAGE3-RESUME-READY ' \
  'STAGE3-RECOVERY-SYNC-START ' \
  'STAGE3-SENTINEL-START ' \
  'STAGE3-RESULT '; do
  marker_line=$(awk -v prefix="$marker_prefix" \
    'substr($0, 1, length(prefix)) == prefix {print NR}' "$transport_stdout")
  [[ "$marker_line" =~ ^[0-9]+$ && "$marker_line" -gt "$previous_marker_line" ]] ||
    die "transport markers were emitted out of order at $marker_prefix"
  previous_marker_line=$marker_line
done
[[ "$(marker_count "$transport_stderr" 'STAGE3-ERROR ')" -eq 0 ]] ||
  die "transport probe emitted STAGE3-ERROR"
awk '/^STAGE3-/ {print}' "$transport_stdout" >"$results_dir/40-transport-markers.result"
awk '/^STAGE3-RESULT / {print}' "$transport_stdout" >"$results_dir/40-transport.result"
transport_result="$results_dir/40-transport.result"
grep -Fq ':category :cognitect.anomalies/unavailable' "$transport_result" &&
  grep -Fq ':status :succeeded' "$transport_result" &&
  grep -Fq ':read-count 1' "$transport_result" &&
  grep -Fq ':transaction-sentinel-datom-count 1' "$transport_result" ||
  die "transport result omitted unavailable, recovery, or exact sentinel evidence"

# The transport sentinel uses only :db/doc, outside the canonical Stage 3
# workload attributes.  A fresh-JVM post-recovery audit must therefore match
# the exact pre-fault canonical hash.
run_stage3_case 41-peer-post-transport-audit "$probe_timeout" \
  "${candidate_java[@]}" -m stage3.peer-probe audit "$sql_uri"
peer_post_transport_audit_result=$last_result_file
grep -Fq ':mode :audit' "$peer_post_transport_audit_result" &&
  grep -Fq ':counter-history-count 17' "$peer_post_transport_audit_result" &&
  grep -Fq ':counter 8' "$peer_post_transport_audit_result" ||
  die "post-transport peer audit result is not canonical"
post_transport_canonical=$(sed -nE \
  's/.*:canonical-sha256 "([0-9a-f]{64})".*/\1/p' \
  "$peer_post_transport_audit_result")
[[ "$post_transport_canonical" == "$audit_canonical" &&
   "$post_transport_canonical" == "$expected_peer_canonical_sha" ]] ||
  die "transport recovery changed the canonical Stage 3 workload"

current_step=transactor-stop
stop_transactor || die "could not stop external transactor safely"
current_step=postgres-stop
stop_postgres || die "could not stop disposable PostgreSQL safely"
port_is_open "$pg_host" "$transactor_port" && die "transactor port remains open after stop"
port_is_open "$pg_host" "$pg_port" && die "PostgreSQL port remains open after stop"

summary_file="$work_root/stage-3-summary.properties"
{
  printf 'stage=3\n'
  printf 'status=complete\n'
  printf 'stage.complete=true\n'
  printf 'backend=postgresql\n'
  printf 'artifact.sha256=%s\n' "$(sha256_file "$artifact_jar")"
  printf 'candidate.classpath.entry.count=%s\n' "${#candidate_entries[@]}"
  printf 'candidate.original.peer=false\n'
  printf 'candidate.original.core2=false\n'
  printf 'candidate.original.transactor=false\n'
  printf 'local.promise=true\n'
  printf 'local.core2-async=true\n'
  printf 'local.pool-rejection=true\n'
  printf 'local.query-timeout=true\n'
  printf 'peer.seed=true\n'
  printf 'peer.query-controls=true\n'
  printf 'peer.lifecycle-race=true\n'
  printf 'peer.contention=true\n'
  printf 'peer.audit=true\n'
  printf 'peer.post-transport-audit=true\n'
  printf 'peer.canonical.sha256=%s\n' "$audit_canonical"
  printf 'transport.sigstop-observed-unavailable=true\n'
  printf 'transport.sigstop.minimum-seconds=10\n'
  printf 'transport.sigcont-recovered=true\n'
  printf 'transport.sentinel.exact=true\n'
  printf 'services.stopped=true\n'
  printf 'result.promise.sha256=%s\n' "$(sha256_file "$promise_result")"
  printf 'result.core2-async.sha256=%s\n' "$(sha256_file "$core2_async_result")"
  printf 'result.pool.sha256=%s\n' "$(sha256_file "$pool_result")"
  printf 'result.query-timeout.sha256=%s\n' "$(sha256_file "$query_timeout_result")"
  printf 'result.peer-seed.sha256=%s\n' "$(sha256_file "$peer_seed_result")"
  printf 'result.peer-query-controls.sha256=%s\n' "$(sha256_file "$peer_query_result")"
  printf 'result.peer-lifecycle.sha256=%s\n' "$(sha256_file "$peer_lifecycle_result")"
  printf 'result.peer-contention.sha256=%s\n' "$(sha256_file "$peer_contention_result")"
  printf 'result.peer-audit.sha256=%s\n' "$(sha256_file "$peer_audit_result")"
  printf 'result.transport.sha256=%s\n' "$(sha256_file "$transport_result")"
  printf 'result.peer-post-transport-audit.sha256=%s\n' \
    "$(sha256_file "$peer_post_transport_audit_result")"
} >"$summary_file"
{
  printf 'status=complete\n'
  printf 'scope=bounded-concurrency-cancellation-lifecycle-and-transport\n'
  printf 'stage.complete=true\n'
  printf 'services.stopped=true\n'
  printf 'evidence.complete=true\n'
} >"$work_root/run-status.properties"
write_evidence_hashes

trap - EXIT
echo "Stage 3 PostgreSQL bounded concurrency and recovery gate passed"
echo "candidate boundary: exact Stage 2-audited 535 entries"
echo "external PostgreSQL and licensed transactor fixtures: stopped"
echo "evidence: $work_root"
