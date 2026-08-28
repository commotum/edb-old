#!/usr/bin/env bash

# Reproducible PostgreSQL integration gate for the recovered Datomic peer.
#
# This script deliberately keeps two trust boundaries separate:
#   * candidate JVMs get only the recovered artifact, this Stage 2 harness,
#     four compile-only Hot Rod stubs, and hash-verified manifest dependencies;
#   * the licensed Datomic transactor is launched only as an external fixture.
#
# Every database and mutable path belongs to a newly initialized, disposable
# PostgreSQL cluster.  Nothing is dropped or overwritten, and a conspicuous
# confirmation token is required before database processes are started.

set -euo pipefail

export LC_ALL=C
export TZ=UTC
umask 077

script_dir=$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd)
scripts_dir=$(cd -- "$script_dir/.." && pwd)
project_dir=$(cd -- "$scripts_dir/.." && pwd)

artifact_name=datomic-rev-peer-1.0.7277-source.jar
confirmation_token=DATOMIC_STAGE2_DISPOSABLE
expected_stub_manifest_sha=5bb9a3440c4fe00f3b299a7e48fbf2aa6ef5f3ef2208fe389af13304eb267e02
expected_nano_path=lib/nano-impl-0.1.325.jar
expected_nano_sha=fbde8184da356bd3a9995a9f05f01493efe4baa87c4d48377cda3e53331807cd
expected_sanitized_nano_sha=08f9699d6e35b9e052ec85ee15e0896b3a685c74e6e226d8cd89d9c61dbf8d5f
expected_sanitized_nano_bytes=81218

usage() {
  cat <<EOF
Usage:
  $0 [OPTIONS]

Required for an executing run:
  --confirm-disposable $confirmation_token

Artifact inputs:
  --datomic-home DIR          Licensed Datomic distribution used for recorded
                              dependency JARs and the external transactor.
  --artifact JAR             Recovered source artifact. If omitted, use
                              DATOMIC_REV_ARTIFACT or discover exactly one
                              canonical artifact below datomic-rev/build.
  --dependency-manifest TSV  Optional external dependencies.tsv; it must match
                              the artifact's embedded manifest byte-for-byte.
  --sanitized-nano JAR       Required canonical sanitized Nano derivative. The
                              original recorded Nano dependency is replaced by
                              this content-addressed JAR in the candidate JVM.
  --stub-dir DIR             Optional prebuilt four-class Hot Rod stub tree.
                              By default, build it inside the run directory.

PostgreSQL inputs:
  --postgres-root DIR        Extracted PostgreSQL root. Infers bin, lib, and
                              share directories for --pg-major (default: 16).
  --pg-bin-dir DIR           Directory containing postgres/initdb/pg_ctl tools.
  --pg-lib-dir DIR           Optional directory containing libpq and companions.
  --pg-share-dir DIR         Optional initdb input directory.
  --pg-major N               Major version used with --postgres-root (16).
  --pg-host HOST             Must be loopback 127.0.0.1 (default).
  --pg-port PORT             Dedicated PostgreSQL port (default: 55432).
  --pg-superuser NAME        Fresh-cluster superuser (default: stage2_admin).
  --pg-user NAME             Datomic SQL role (default: datomic_stage2).
  --pg-password VALUE        Disposable SQL password (default: datomic_stage2).

Disposable database names:
  --source-db NAME           Default: datomic_stage2_source
  --restore-full-db NAME     Default: datomic_stage2_restore_full
  --restore-incr-db NAME     Default: datomic_stage2_restore_incremental
  --fault-target-db NAME     Default: datomic_stage2_fault_target
  --recovery-db NAME         Default: datomic_stage2_recovery
  --source-name NAME         Datomic database name (default: stage2-source)
  --restore-full-name NAME   Datomic database name (default: stage2-restore-full)
  --restore-incr-name NAME   Datomic database name (default: stage2-restore-incremental)
  --fault-target-name NAME   Datomic database name (default: stage2-fault-target)
  --recovery-name NAME       Datomic database name (default: stage2-recovery)

Runtime and output:
  --work-root DIR            Must be absent or empty and have a basename that
                              starts with datomic-stage2-. Default: mktemp.
  --pgdata DIR               Must be a new descendant of work-root.
  --pg-socket-dir DIR        Must be a new descendant of work-root.
  --backup-root DIR          Must be a new descendant of work-root.
  --transactor-port PORT     Reused sequentially by fixtures (default: 54334).
  --startup-timeout SEC      Fixture readiness timeout (default: 120).
  --probe-timeout SEC        Hard timeout for each candidate JVM (default: 300).
  --dry-run                  Validate inputs/build boundary records, but do not
                              initialize PostgreSQL or launch a transactor.
  -h, --help                 Show this help.

The workflow is deterministic: seed -> full backup -> augment -> incremental
backup -> full restore at t1 -> writable check -> full t1 plus incremental t2
restore -> writable check. Raw logs, one-line result records, classpath
provenance, and SHA-256 evidence are retained below work-root.
EOF
}

die() {
  echo "stage2-postgresql: $*" >&2
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

require_disposable_catalog() {
  local value=$1
  [[ "$value" =~ ^datomic_stage2_[a-z0-9_]+$ ]] ||
    die "database name is not visibly disposable: $value"
  case "$value" in
    postgres|template0|template1) die "refusing protected database name: $value" ;;
  esac
}

require_logical_name() {
  local label=$1
  local value=$2
  [[ "$value" =~ ^stage2-[a-z0-9-]+$ ]] ||
    die "$label must start with stage2- and contain lowercase letters, digits, or hyphens: $value"
}

require_port() {
  local label=$1
  local value=$2
  [[ "$value" =~ ^[0-9]+$ ]] || die "$label is not an integer: $value"
  ((value >= 1024 && value <= 65535)) || die "$label is outside 1024..65535: $value"
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

artifact_input=${DATOMIC_REV_ARTIFACT:-}
dependency_manifest_input=
sanitized_nano_input=
stub_dir_input=
datomic_home=${DATOMIC_HOME:-}
postgres_root=${STAGE2_POSTGRES_ROOT:-}
pg_bin_dir=
pg_lib_dir=
pg_share_dir=
pg_major=16
pg_host=127.0.0.1
pg_port=55432
pg_superuser=stage2_admin
pg_user=datomic_stage2
pg_password=datomic_stage2
source_catalog=datomic_stage2_source
restore_full_catalog=datomic_stage2_restore_full
restore_incr_catalog=datomic_stage2_restore_incremental
fault_target_catalog=datomic_stage2_fault_target
recovery_catalog=datomic_stage2_recovery
source_name=stage2-source
restore_full_name=stage2-restore-full
restore_incr_name=stage2-restore-incremental
fault_target_name=stage2-fault-target
recovery_name=stage2-recovery
work_root_input=
pgdata_input=
pg_socket_dir_input=
backup_root_input=
transactor_port=54334
startup_timeout=120
probe_timeout=300
confirmation=${STAGE2_CONFIRM_DISPOSABLE:-}
dry_run=false

while (($#)); do
  case "$1" in
    --datomic-home) datomic_home=${2:?missing value for --datomic-home}; shift 2 ;;
    --artifact) artifact_input=${2:?missing value for --artifact}; shift 2 ;;
    --dependency-manifest) dependency_manifest_input=${2:?missing value for --dependency-manifest}; shift 2 ;;
    --sanitized-nano) sanitized_nano_input=${2:?missing value for --sanitized-nano}; shift 2 ;;
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
    --source-db) source_catalog=${2:?missing value for --source-db}; shift 2 ;;
    --restore-full-db) restore_full_catalog=${2:?missing value for --restore-full-db}; shift 2 ;;
    --restore-incr-db) restore_incr_catalog=${2:?missing value for --restore-incr-db}; shift 2 ;;
    --fault-target-db) fault_target_catalog=${2:?missing value for --fault-target-db}; shift 2 ;;
    --recovery-db) recovery_catalog=${2:?missing value for --recovery-db}; shift 2 ;;
    --source-name) source_name=${2:?missing value for --source-name}; shift 2 ;;
    --restore-full-name) restore_full_name=${2:?missing value for --restore-full-name}; shift 2 ;;
    --restore-incr-name) restore_incr_name=${2:?missing value for --restore-incr-name}; shift 2 ;;
    --fault-target-name) fault_target_name=${2:?missing value for --fault-target-name}; shift 2 ;;
    --recovery-name) recovery_name=${2:?missing value for --recovery-name}; shift 2 ;;
    --work-root) work_root_input=${2:?missing value for --work-root}; shift 2 ;;
    --pgdata) pgdata_input=${2:?missing value for --pgdata}; shift 2 ;;
    --pg-socket-dir) pg_socket_dir_input=${2:?missing value for --pg-socket-dir}; shift 2 ;;
    --backup-root) backup_root_input=${2:?missing value for --backup-root}; shift 2 ;;
    --transactor-port) transactor_port=${2:?missing value for --transactor-port}; shift 2 ;;
    --startup-timeout) startup_timeout=${2:?missing value for --startup-timeout}; shift 2 ;;
    --probe-timeout) probe_timeout=${2:?missing value for --probe-timeout}; shift 2 ;;
    --confirm-disposable) confirmation=${2:?missing value for --confirm-disposable}; shift 2 ;;
    --dry-run) dry_run=true; shift ;;
    -h|--help) usage; exit 0 ;;
    *) usage >&2; die "unknown argument: $1" ;;
  esac
done

for command_name in \
  awk cat chmod cmp cp dirname find grep java kill mkdir mktemp mv readlink sed \
  sha256sum sleep sort stat tail timeout tr uname unzip uniq wc; do
  need_command "$command_name"
done

[[ "$(uname -s)" == Linux ]] ||
  die "Stage 2 requires Linux process identity semantics"
[[ -r "/proc/$$/cmdline" && -n "$(tr '\0' ' ' <"/proc/$$/cmdline")" ]] ||
  die "Stage 2 requires a readable Linux /proc process table"

[[ "$pg_major" =~ ^[0-9]+$ ]] || die "pg-major is not numeric: $pg_major"
[[ "$startup_timeout" =~ ^[0-9]+$ ]] && ((startup_timeout > 0)) ||
  die "startup-timeout must be a positive integer"
[[ "$probe_timeout" =~ ^[0-9]+$ ]] && ((probe_timeout > 0)) ||
  die "probe-timeout must be a positive integer"
[[ "$pg_host" == 127.0.0.1 ]] || die "PostgreSQL must bind only to 127.0.0.1"
require_port pg-port "$pg_port"
require_port transactor-port "$transactor_port"
[[ "$pg_port" != "$transactor_port" ]] || die "PostgreSQL and transactor ports must differ"
require_identifier pg-superuser "$pg_superuser"
require_identifier pg-user "$pg_user"
[[ "$pg_superuser" != "$pg_user" ]] || die "PostgreSQL superuser and Datomic role must differ"
[[ "$pg_password" =~ ^[A-Za-z0-9_.-]+$ ]] ||
  die "pg-password must use only letters, digits, dot, underscore, or hyphen"
require_disposable_catalog "$source_catalog"
require_disposable_catalog "$restore_full_catalog"
require_disposable_catalog "$restore_incr_catalog"
require_disposable_catalog "$fault_target_catalog"
require_disposable_catalog "$recovery_catalog"
catalogs=(
  "$source_catalog"
  "$restore_full_catalog"
  "$restore_incr_catalog"
  "$fault_target_catalog"
  "$recovery_catalog"
)
[[ "$(printf '%s\n' "${catalogs[@]}" | sort -u | wc -l)" -eq "${#catalogs[@]}" ]] ||
  die "all Stage 2 PostgreSQL database names must be distinct"
require_logical_name source-name "$source_name"
require_logical_name restore-full-name "$restore_full_name"
require_logical_name restore-incr-name "$restore_incr_name"
require_logical_name fault-target-name "$fault_target_name"
require_logical_name recovery-name "$recovery_name"
logical_names=(
  "$source_name"
  "$restore_full_name"
  "$restore_incr_name"
  "$fault_target_name"
  "$recovery_name"
)
[[ "$(printf '%s\n' "${logical_names[@]}" | sort -u | wc -l)" -eq "${#logical_names[@]}" ]] ||
  die "all Stage 2 Datomic database names must be distinct"

if [[ -z "$datomic_home" ]]; then
  default_datomic_home="$project_dir/../../datomic/datomic-pro-1.0.7277"
  [[ -d "$default_datomic_home" ]] && datomic_home=$default_datomic_home
fi
[[ -n "$datomic_home" && -d "$datomic_home" ]] ||
  die "provide --datomic-home pointing at datomic-pro-1.0.7277"
datomic_home=$(readlink -f -- "$datomic_home")
require_safe_path_text datomic-home "$datomic_home"
[[ -x "$datomic_home/bin/transactor" ]] || die "missing external transactor launcher: $datomic_home/bin/transactor"
transactor_jar="$datomic_home/datomic-transactor-pro-1.0.7277.jar"
[[ -f "$transactor_jar" ]] || die "missing licensed external transactor fixture: $transactor_jar"

if [[ -z "$artifact_input" ]]; then
  canonical_artifact="$project_dir/build/source-artifact/$artifact_name"
  if [[ -f "$canonical_artifact" ]]; then
    artifact_input=$canonical_artifact
  elif [[ -d "$project_dir/build" ]]; then
    mapfile -t discovered_artifacts < <(
      find "$project_dir/build" -type f -name "$artifact_name" -print | sort
    )
    ((${#discovered_artifacts[@]} == 1)) ||
      die "artifact discovery found ${#discovered_artifacts[@]} candidates; pass --artifact explicitly"
    artifact_input=${discovered_artifacts[0]}
  else
    die "no canonical artifact found; pass --artifact"
  fi
fi
[[ -f "$artifact_input" ]] || die "missing recovered artifact: $artifact_input"
artifact_jar=$(readlink -f -- "$artifact_input")
require_safe_path_text artifact "$artifact_jar"
unzip -tqq "$artifact_jar" || die "artifact is not a valid ZIP/JAR: $artifact_jar"

[[ -n "$sanitized_nano_input" ]] ||
  die "provide --sanitized-nano with the canonical sanitized Nano derivative"
[[ ! -L "$sanitized_nano_input" ]] ||
  die "sanitized Nano input may not be a symbolic link: $sanitized_nano_input"
[[ -f "$sanitized_nano_input" ]] ||
  die "missing sanitized Nano derivative: $sanitized_nano_input"
sanitized_nano_jar=$(readlink -f -- "$sanitized_nano_input")
[[ -f "$sanitized_nano_jar" && ! -L "$sanitized_nano_jar" ]] ||
  die "sanitized Nano derivative is not a regular non-symlink file: $sanitized_nano_jar"
require_safe_path_text sanitized-nano "$sanitized_nano_jar"
sanitized_nano_name=${sanitized_nano_jar##*/}
case "$sanitized_nano_name" in
  *$'\r'*) die "sanitized Nano filename contains a carriage return" ;;
esac
sanitized_nano_sha=$(sha256_file "$sanitized_nano_jar")
[[ "$sanitized_nano_sha" == "$expected_sanitized_nano_sha" ]] ||
  die "sanitized Nano derivative SHA-256 mismatch: expected $expected_sanitized_nano_sha, got $sanitized_nano_sha"
sanitized_nano_bytes=$(stat -c '%s' -- "$sanitized_nano_jar")
[[ "$sanitized_nano_bytes" == "$expected_sanitized_nano_bytes" ]] ||
  die "sanitized Nano derivative byte-size mismatch: expected $expected_sanitized_nano_bytes, got $sanitized_nano_bytes"
unzip -tqq "$sanitized_nano_jar" ||
  die "sanitized Nano derivative is not a valid ZIP/JAR: $sanitized_nano_jar"
[[ -z "$(unzip -Z1 "$sanitized_nano_jar" |
  grep -E '\.(jks|p12|pfx|keystore)$' || true)" ]] ||
  die "sanitized Nano derivative retained a keystore entry"

if [[ -n "$work_root_input" ]]; then
  [[ ! -L "$work_root_input" ]] ||
    die "work-root input may not be a symbolic link: $work_root_input"
  work_root=$(readlink -m -- "$work_root_input")
  [[ "${work_root##*/}" == datomic-stage2-* ]] ||
    die "work-root basename must start with datomic-stage2-: $work_root"
  require_safe_path_text work-root "$work_root"
  if [[ -L "$work_root" ]]; then
    die "work-root may not be a symbolic link: $work_root"
  fi
  if [[ -e "$work_root" && ! -d "$work_root" ]]; then
    die "work-root exists but is not a directory: $work_root"
  fi
  if [[ -e "$work_root" && -n "$(find "$work_root" -mindepth 1 -print -quit 2>/dev/null)" ]]; then
    die "refusing non-empty work-root: $work_root"
  fi
  mkdir -p -- "$work_root"
else
  work_root=$(mktemp -d -t datomic-stage2-run.XXXXXXXX)
fi

logs_dir="$work_root/logs"
results_dir="$work_root/results"
inputs_dir="$work_root/inputs"
runtime_dir="$work_root/runtime"
mkdir -p -- "$logs_dir" "$results_dir" "$inputs_dir" "$runtime_dir"

pgdata=$(readlink -m -- "${pgdata_input:-$runtime_dir/postgres-data}")
pg_socket_dir=$(readlink -m -- "${pg_socket_dir_input:-$runtime_dir/postgres-socket}")
backup_root=$(readlink -m -- "${backup_root_input:-$work_root/backup}")
missing_backup_root=$(readlink -m -- "$work_root/backup-missing")
corrupt_backup_root=$(readlink -m -- "$work_root/backup-corrupt")
for path_spec in \
  "pgdata:$pgdata" \
  "pg-socket-dir:$pg_socket_dir" \
  "backup-root:$backup_root" \
  "missing-backup-root:$missing_backup_root" \
  "corrupt-backup-root:$corrupt_backup_root"; do
  label=${path_spec%%:*}
  value=${path_spec#*:}
  require_safe_path_text "$label" "$value"
  assert_descendant "$label" "$value"
  [[ ! -e "$value" ]] || die "$label must not exist at run start: $value"
done
[[ "$pg_socket_dir" != *[[:space:]]* ]] ||
  die "pg-socket-dir may not contain whitespace: $pg_socket_dir"
((${#pg_socket_dir} < 90)) ||
  die "pg-socket-dir is too long for a portable Unix-domain socket: $pg_socket_dir"

artifact_entries="$inputs_dir/artifact-entries.txt"
unzip -Z1 "$artifact_jar" | sort >"$artifact_entries"
grep -qx 'datomic/backup.clj' "$artifact_entries" ||
  die "artifact does not contain recovered datomic/backup.clj"
if grep -Eq '^datomic/fsbackup(__init[.]class|[.]clj)$' "$artifact_entries"; then
  die "artifact unexpectedly contains licensed datomic.fsbackup implementation"
fi
if grep -Eq '__init[.]class$' "$artifact_entries"; then
  die "artifact contains AOT Clojure initializers; recovered source boundary is not clean"
fi

artifact_build_properties="$inputs_dir/artifact-build.properties"
unzip -p "$artifact_jar" META-INF/datomic-rev/build.properties >"$artifact_build_properties" ||
  die "could not extract artifact build provenance"
for required_property in \
  'artifact.kind=clojure-source-plus-handwritten-java-classes' \
  'target.peer.version=1.0.7277' \
  'clojure.namespace.count=142' \
  'handwritten.java.class.count=47' \
  'peer.resource.count=10' \
  'dependency.jar.count=532' \
  'original.peer.aot.on.build.classpath=false' \
  'original.core2.aot.on.build.classpath=false' \
  'clojure.aot.classes.packaged=false'; do
  grep -Fqx "$required_property" "$artifact_build_properties" ||
    die "artifact build provenance is missing: $required_property"
done

dependency_manifest="$inputs_dir/dependencies.tsv"
embedded_dependency_manifest="$inputs_dir/artifact-dependencies.tsv"
unzip -p "$artifact_jar" META-INF/datomic-rev/inputs/dependencies.tsv \
  >"$embedded_dependency_manifest" || die "could not extract embedded dependency manifest"
if [[ -n "$dependency_manifest_input" ]]; then
  [[ -f "$dependency_manifest_input" ]] || die "missing dependency manifest: $dependency_manifest_input"
  cp -- "$dependency_manifest_input" "$dependency_manifest"
  cmp -s "$embedded_dependency_manifest" "$dependency_manifest" ||
    die "external dependency manifest differs from artifact provenance"
else
  cp -- "$embedded_dependency_manifest" "$dependency_manifest"
fi
[[ "$(sed -n '1p' "$dependency_manifest")" == $'sha256\tpath' ]] ||
  die "dependency manifest has the wrong header"

duplicate_dependency_paths=$(
  awk -F '\t' 'NR > 1 {print $2}' "$dependency_manifest" | sort | uniq -d
)
[[ -z "$duplicate_dependency_paths" ]] ||
  die "dependency manifest contains duplicate paths: $duplicate_dependency_paths"

dependency_paths=()
candidate_dependency_roles=()
candidate_dependency_shas=()
candidate_dependency_paths=()
dependency_count=0
nano_substitution_count=0
while IFS=$'\t' read -r expected_sha relative_path extra; do
  [[ "$expected_sha" == sha256 ]] && continue
  [[ -n "$expected_sha" && -n "$relative_path" && -z "${extra:-}" ]] ||
    die "malformed dependency manifest row"
  [[ "$expected_sha" =~ ^[0-9a-f]{64}$ ]] ||
    die "invalid dependency SHA-256 for $relative_path"
  [[ "$relative_path" =~ ^lib/[A-Za-z0-9._+-]+[.]jar$ ]] ||
    die "dependency must be a direct lib/*.jar path: $relative_path"
  dependency_path=$(readlink -f -- "$datomic_home/$relative_path")
  case "$dependency_path" in
    "$datomic_home"/lib/*) ;;
    *) die "dependency escaped Datomic lib directory: $relative_path" ;;
  esac
  [[ -f "$dependency_path" ]] || die "missing recorded dependency: $relative_path"
  dependency_name=${dependency_path##*/}
  case "$dependency_name" in
    peer-*.jar|core2-*.jar|datomic-transactor*.jar|*transactor-pro*.jar)
      die "forbidden original implementation dependency: $relative_path"
      ;;
  esac
  actual_sha=$(sha256_file "$dependency_path")
  [[ "$actual_sha" == "$expected_sha" ]] ||
    die "dependency SHA-256 mismatch: $relative_path"
  require_safe_path_text dependency "$dependency_path"
  if [[ "$relative_path" == "$expected_nano_path" ||
        "$expected_sha" == "$expected_nano_sha" ]]; then
    [[ "$relative_path" == "$expected_nano_path" &&
       "$expected_sha" == "$expected_nano_sha" ]] ||
      die "original Nano dependency appeared under a non-canonical identity: $relative_path"
    dependency_paths+=("$sanitized_nano_jar")
    candidate_dependency_roles+=(sanitized-nano-dependency)
    candidate_dependency_shas+=("$sanitized_nano_sha")
    candidate_dependency_paths+=("$sanitized_nano_jar")
    ((nano_substitution_count += 1))
  else
    dependency_paths+=("$dependency_path")
    candidate_dependency_roles+=(dependency)
    candidate_dependency_shas+=("$expected_sha")
    candidate_dependency_paths+=("$dependency_path")
  fi
  ((dependency_count += 1))
done <"$dependency_manifest"
[[ "$dependency_count" -eq 532 ]] ||
  die "expected 532 peer/core2-free dependencies, found $dependency_count"
[[ "$nano_substitution_count" -eq 1 ]] ||
  die "expected exactly one original Nano dependency substitution, found $nano_substitution_count"

candidate_sanitized_nano_count=0
candidate_original_nano_count=0
for dependency_sha in "${candidate_dependency_shas[@]}"; do
  [[ "$dependency_sha" != "$expected_sanitized_nano_sha" ]] ||
    ((candidate_sanitized_nano_count += 1))
  [[ "$dependency_sha" != "$expected_nano_sha" ]] ||
    ((candidate_original_nano_count += 1))
done
[[ "$candidate_sanitized_nano_count" -eq 1 ]] ||
  die "candidate classpath must contain exactly one sanitized Nano derivative"
[[ "$candidate_original_nano_count" -eq 0 ]] ||
  die "candidate classpath retained the original Nano implementation"
[[ "${#candidate_dependency_roles[@]}" -eq "$dependency_count" &&
   "${#candidate_dependency_shas[@]}" -eq "$dependency_count" &&
   "${#candidate_dependency_paths[@]}" -eq "$dependency_count" ]] ||
  die "candidate dependency provenance cardinality changed during Nano substitution"

dependency_classpath=$(IFS=:; echo "${dependency_paths[*]}")
stub_dir="$runtime_dir/infinispan-compile-stubs"
stub_build_log="$logs_dir/00-infinispan-stubs.out"
if [[ -n "$stub_dir_input" ]]; then
  [[ -d "$stub_dir_input" ]] || die "missing stub directory: $stub_dir_input"
  stub_dir=$(readlink -f -- "$stub_dir_input")
  require_safe_path_text stub-dir "$stub_dir"
else
  mkdir -p -- "$stub_dir"
  timeout --foreground --signal=TERM --kill-after=15s "${probe_timeout}s" \
    java -XX:-UsePerfData -cp "$dependency_classpath" clojure.main \
    "$project_dir/tools/infinispan-compile-stubs/build_stubs.clj" "$stub_dir" \
    >"$stub_build_log" 2>&1 || {
      tail -n 80 "$stub_build_log" >&2
      die "failed to build compile-only Hot Rod stubs"
    }
fi

stub_manifest="$inputs_dir/infinispan-stubs.tsv"
printf 'sha256\tpath\n' >"$stub_manifest"
while IFS= read -r stub_class; do
  printf '%s\t%s\n' "$(sha256_file "$stub_dir/$stub_class")" "$stub_class" >>"$stub_manifest"
done < <(find "$stub_dir" -type f -name '*.class' -printf '%P\n' | sort)
expected_stub_paths=$'org/infinispan/client/hotrod/Flag.class\norg/infinispan/client/hotrod/RemoteCache.class\norg/infinispan/client/hotrod/RemoteCacheManager.class\norg/infinispan/client/hotrod/VersionedValue.class'
actual_stub_paths=$(awk -F '\t' 'NR > 1 {print $2}' "$stub_manifest")
[[ "$actual_stub_paths" == "$expected_stub_paths" ]] ||
  die "stub directory must contain exactly the four compile-only Hot Rod classes"
all_stub_paths=$(find "$stub_dir" -type f -printf '%P\n' | sort)
[[ "$all_stub_paths" == "$expected_stub_paths" ]] ||
  die "stub directory contains non-stub files"
[[ -z "$(find "$stub_dir" -type l -print -quit)" ]] ||
  die "stub directory may not contain symbolic links"
actual_stub_manifest_sha=$(sha256_file "$stub_manifest")
[[ "$actual_stub_manifest_sha" == "$expected_stub_manifest_sha" ]] ||
  die "compile-only Hot Rod stubs differ from the canonical deterministic bytes"

harness_manifest="$inputs_dir/stage2-harness.tsv"
printf 'sha256\tpath\n' >"$harness_manifest"
if ! harness_symlink=$(find "$scripts_dir" -type l -print -quit); then
  die "could not inspect shared scripts classpath root for symbolic links"
fi
[[ -z "$harness_symlink" ]] ||
  die "shared scripts classpath root may not contain symbolic links"
harness_inventory="$runtime_dir/stage2-harness-paths.nul"
if ! find "$scripts_dir" -type f -printf '%P\0' | sort -z >"$harness_inventory"; then
  die "could not inventory shared scripts classpath root"
fi
harness_file_count=0
while IFS= read -r -d '' harness_file; do
  require_safe_path_text harness-path "$harness_file"
  [[ -n "$harness_file" && "$harness_file" != /* ]] ||
    die "invalid relative Stage 2 harness path: $harness_file"
  [[ -f "$scripts_dir/$harness_file" && ! -L "$scripts_dir/$harness_file" ]] ||
    die "Stage 2 harness entry is not a regular file: $harness_file"
  printf '%s\t%s\n' "$(sha256_file "$scripts_dir/$harness_file")" "$harness_file" >>"$harness_manifest"
  ((harness_file_count += 1))
done <"$harness_inventory"
((harness_file_count > 0)) ||
  die "shared scripts classpath root contains no regular files"
[[ ! -e "$scripts_dir/datomic" ]] ||
  die "Stage 2 harness classpath root may not contain a loose datomic namespace"

origin_inputs_dir="$inputs_dir/origin-gate"
mkdir -p -- "$origin_inputs_dir"
cp -- "$project_dir/reports/source-index/namespaces.tsv" \
  "$origin_inputs_dir/namespaces.tsv"
cp -- "$project_dir/reports/peer-resource-paths.txt" \
  "$origin_inputs_dir/peer-resource-paths.txt"
cp -- "$project_dir/reports/source-index/unmapped-classes.tsv" \
  "$origin_inputs_dir/unmapped-classes.tsv"

candidate_entries=("$artifact_jar" "$scripts_dir" "$stub_dir" "${dependency_paths[@]}")
for ((entry_index = 0; entry_index < ${#candidate_entries[@]}; entry_index += 1)); do
  entry=${candidate_entries[$entry_index]}
  require_safe_path_text candidate-classpath "$entry"
  [[ "$entry" != *'*'* && "$entry" != *'?'* ]] ||
    die "candidate classpath may not contain wildcards: $entry"
  if ((entry_index >= 3)); then
    case "${entry##*/}" in
      peer-*.jar|core2-*.jar|datomic-transactor*.jar|*transactor-pro*.jar)
        die "forbidden implementation entered candidate classpath: $entry"
        ;;
    esac
  fi
done
candidate_classpath=$(IFS=:; echo "${candidate_entries[*]}")
candidate_java=(
  java
  -XX:-UsePerfData
  "-Dlogback.configurationFile=$script_dir/logback-stage2.xml"
  -cp "$candidate_classpath"
  clojure.main
)

candidate_classpath_record="$work_root/candidate-classpath.tsv"
{
  printf 'position\trole\tsha256\tpath\n'
  printf '1\trecovered-artifact\t%s\t%s\n' "$(sha256_file "$artifact_jar")" "$artifact_jar"
  printf '2\tstage2-test-harness\t%s\t%s\n' "$(sha256_file "$harness_manifest")" "$scripts_dir"
  printf '3\tcompile-only-hotrod-stubs\t%s\t%s\n' "$(sha256_file "$stub_manifest")" "$stub_dir"
  position=4
  for ((dependency_index = 0;
       dependency_index < ${#candidate_dependency_paths[@]};
       dependency_index += 1)); do
    printf '%s\t%s\t%s\t%s\n' \
      "$position" \
      "${candidate_dependency_roles[$dependency_index]}" \
      "${candidate_dependency_shas[$dependency_index]}" \
      "${candidate_dependency_paths[$dependency_index]}"
    ((position += 1))
  done
} >"$candidate_classpath_record"
[[ "$position" -eq 536 ]] || die "candidate classpath record has an unexpected size"
[[ "$(awk -F '\t' '$2 == "sanitized-nano-dependency" {n++} END {print n + 0}' \
  "$candidate_classpath_record")" -eq 1 ]] ||
  die "candidate classpath record does not identify exactly one sanitized Nano derivative"
[[ "$(awk -F '\t' -v sha="$expected_nano_sha" '$3 == sha {n++} END {print n + 0}' \
  "$candidate_classpath_record")" -eq 0 ]] ||
  die "candidate classpath record retained the original Nano implementation hash"

# This is the complete Stage 1 origin gate, run before any recovered Datomic
# namespace is loaded by a workload or backup/restore probe.
origin_stdout="$logs_dir/01-artifact-origins.out"
origin_stderr="$logs_dir/01-artifact-origins.err"
if ! timeout --foreground --signal=TERM --kill-after=15s "${probe_timeout}s" \
  "${candidate_java[@]}" "$scripts_dir/verify_artifact_origins.clj" \
  "$artifact_jar" \
  "$origin_inputs_dir/namespaces.tsv" \
  "$origin_inputs_dir/peer-resource-paths.txt" \
  "$origin_inputs_dir/unmapped-classes.tsv" \
  >"$origin_stdout" 2>"$origin_stderr"; then
  tail -n 100 "$origin_stdout" >&2 || true
  tail -n 100 "$origin_stderr" >&2 || true
  die "artifact-origin and candidate-classpath gate failed"
fi
grep -Fqx 'artifact origin and candidate classpath isolation passed' "$origin_stdout" ||
  die "artifact-origin gate did not emit its success marker"
cp -- "$origin_stdout" "$results_dir/01-artifact-origins.result"

# Exercise the test-only storage adapter after the origin gate but before any
# database service is created. This is also part of --dry-run validation.
storage_stdout="$logs_dir/02-storage-self-test.out"
storage_stderr="$logs_dir/02-storage-self-test.err"
storage_result="$results_dir/02-storage-self-test.result"
if ! timeout --foreground --signal=TERM --kill-after=15s "${probe_timeout}s" \
  "${candidate_java[@]}" -m stage2.storage-self-test \
  >"$storage_stdout" 2>"$storage_stderr"; then
  tail -n 100 "$storage_stdout" >&2 || true
  tail -n 100 "$storage_stderr" >&2 || true
  die "Stage 2 storage self-test failed"
fi
awk '/^STAGE2-STORAGE-SELF-TEST-PASS / {print}' "$storage_stdout" >"$storage_result"
[[ "$(wc -l <"$storage_result")" -eq 1 ]] ||
  die "storage self-test did not emit exactly one success marker"

if [[ -n "$postgres_root" ]]; then
  postgres_root=$(readlink -f -- "$postgres_root")
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
postgres_version=$("$pg_bin_dir/postgres" --version)
java_settings=$(java -XshowSettings:properties -version 2>&1)
java_runtime_version=$(awk -F' = ' '/^[[:space:]]*java.runtime.version = / {print $2; exit}' <<<"$java_settings")
java_vendor=$(awk -F' = ' '/^[[:space:]]*java.vendor = / {print $2; exit}' <<<"$java_settings")
[[ -n "$java_runtime_version" && -n "$java_vendor" ]] ||
  die "could not fingerprint the candidate Java runtime"
expected_java_runtime_version=$(awk -F= '$1 == "java.runtime.version" {print $2; exit}' "$artifact_build_properties")
expected_java_vendor=$(awk -F= '$1 == "java.vendor" {print $2; exit}' "$artifact_build_properties")
[[ "$java_runtime_version" == "$expected_java_runtime_version" &&
   "$java_vendor" == "$expected_java_vendor" ]] ||
  die "candidate Java runtime differs from artifact provenance: $java_runtime_version / $java_vendor"

password_sha=$(sha256_text "$pg_password")
config_record="$work_root/config.properties"
{
  printf 'stage=2\n'
  printf 'backend=postgresql\n'
  printf 'artifact.path=%s\n' "$artifact_jar"
  printf 'artifact.sha256=%s\n' "$(sha256_file "$artifact_jar")"
  printf 'dependency.manifest.sha256=%s\n' "$(sha256_file "$dependency_manifest")"
  printf 'stage2.harness.manifest.sha256=%s\n' "$(sha256_file "$harness_manifest")"
  printf 'stage2.harness.file.count=%s\n' "$harness_file_count"
  printf 'dependency.count=%s\n' "$dependency_count"
  printf 'candidate.classpath.entry.count=%s\n' "${#candidate_entries[@]}"
  printf 'candidate.original.peer=false\n'
  printf 'candidate.original.core2=false\n'
  printf 'candidate.original.transactor=false\n'
  printf 'candidate.original.nano=false\n'
  printf 'candidate.nano.sanitized=true\n'
  printf 'candidate.nano.sanitized.path=%s\n' "$sanitized_nano_jar"
  printf 'candidate.nano.sanitized.sha256=%s\n' "$sanitized_nano_sha"
  printf 'candidate.nano.sanitized.bytes=%s\n' "$sanitized_nano_bytes"
  printf 'transactor.role=external-fixture-only\n'
  printf 'transactor.fixture.jar.sha256=%s\n' "$(sha256_file "$transactor_jar")"
  printf 'transactor.fixture.launcher.sha256=%s\n' "$(sha256_file "$datomic_home/bin/transactor")"
  printf 'java.runtime.version=%s\n' "$java_runtime_version"
  printf 'java.vendor=%s\n' "$java_vendor"
  printf 'postgres.version=%s\n' "$postgres_version"
  printf 'postgres.binary.sha256=%s\n' "$(sha256_file "$pg_bin_dir/postgres")"
  printf 'postgres.initdb.sha256=%s\n' "$(sha256_file "$pg_bin_dir/initdb")"
  printf 'postgres.psql.sha256=%s\n' "$(sha256_file "$pg_bin_dir/psql")"
  printf 'postgres.host=%s\n' "$pg_host"
  printf 'postgres.port=%s\n' "$pg_port"
  printf 'postgres.user=%s\n' "$pg_user"
  printf 'postgres.password.sha256=%s\n' "$password_sha"
  printf 'postgres.source.catalog=%s\n' "$source_catalog"
  printf 'postgres.restore.full.catalog=%s\n' "$restore_full_catalog"
  printf 'postgres.restore.incremental.catalog=%s\n' "$restore_incr_catalog"
  printf 'postgres.fault.target.catalog=%s\n' "$fault_target_catalog"
  printf 'postgres.recovery.catalog=%s\n' "$recovery_catalog"
  printf 'datomic.source.name=%s\n' "$source_name"
  printf 'datomic.restore.full.name=%s\n' "$restore_full_name"
  printf 'datomic.restore.incremental.name=%s\n' "$restore_incr_name"
  printf 'datomic.fault.target.name=%s\n' "$fault_target_name"
  printf 'datomic.recovery.name=%s\n' "$recovery_name"
  printf 'transactor.port=%s\n' "$transactor_port"
  printf 'work.root=%s\n' "$work_root"
  printf 'backup.root=%s\n' "$backup_root"
  printf 'backup.missing.root=%s\n' "$missing_backup_root"
  printf 'backup.corrupt.root=%s\n' "$corrupt_backup_root"
} >"$config_record"

write_evidence_hashes() {
  local output="$work_root/evidence.sha256"
  (
    cd -- "$work_root"
    find inputs logs results -type f -print
    printf '%s\n' candidate-classpath.tsv config.properties run-status.properties stage-2-summary.properties
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
  } >"$work_root/run-status.properties"
  write_evidence_hashes
  echo "Stage 2 PostgreSQL dry-run validation passed"
  echo "candidate classpath: recovered artifact + Stage 2 harness + four stubs + 531 verified distribution dependencies + one verified sanitized Nano derivative"
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
current_step=initializing

stop_transactor() {
  [[ -n "$transactor_pid" ]] || return 0
  if kill -0 "$transactor_pid" 2>/dev/null; then
    local command_line
    command_line=$(tr '\0' ' ' <"/proc/$transactor_pid/cmdline" 2>/dev/null || true)
    [[ "$command_line" == *datomic.launcher* && "$command_line" == *"$transactor_properties"* ]] || {
      echo "refusing to signal unverified process $transactor_pid" >&2
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
}

stop_postgres() {
  [[ "$pg_started" == true ]] || return 0
  "$pg_bin_dir/pg_ctl" -D "$pgdata" -m fast -w stop >>"$logs_dir/postgres-control.log" 2>&1 || return 1
  pg_started=false
}

cleanup() {
  local status=$?
  trap - EXIT
  set +e
  stop_transactor
  stop_postgres
  if ((status != 0)); then
    {
      printf 'status=failed\n'
      printf 'last.step=%s\n' "$current_step"
    } >"$work_root/run-status.properties"
    write_evidence_hashes
    echo "Stage 2 PostgreSQL run failed during $current_step; evidence retained at $work_root" >&2
  fi
  exit "$status"
}
trap cleanup EXIT

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
"$pg_bin_dir/initdb" "${initdb_args[@]}" >"$logs_dir/postgres-initdb.out" 2>"$logs_dir/postgres-initdb.err"

current_step=postgres-start
postgres_options="-h $pg_host -p $pg_port -k $pg_socket_dir -c timezone=UTC -c log_line_prefix="
# Mark the fixture for cleanup before pg_ctl runs: a startup error can occur
# after postgres has forked, and cleanup must still attempt an exact-PGDATA stop.
pg_started=true
if ! "$pg_bin_dir/pg_ctl" -D "$pgdata" -w -l "$logs_dir/postgres-server.log" \
  -o "$postgres_options" start >"$logs_dir/postgres-control.log" 2>&1; then
  die "disposable PostgreSQL fixture did not start"
fi

current_step=postgres-provision-role
"$pg_bin_dir/psql" -X -v ON_ERROR_STOP=1 \
  -h "$pg_socket_dir" -p "$pg_port" -U "$pg_superuser" -d postgres \
  --set=stage2_role="$pg_user" --set=stage2_password="$pg_password" \
  >"$logs_dir/postgres-role.out" 2>"$logs_dir/postgres-role.err" <<'SQL'
CREATE ROLE :"stage2_role" LOGIN PASSWORD :'stage2_password';
SQL

for catalog in "${catalogs[@]}"; do
  current_step="postgres-create-$catalog"
  "$pg_bin_dir/createdb" -h "$pg_socket_dir" -p "$pg_port" -U "$pg_superuser" \
    --owner="$pg_user" "$catalog" \
    >"$logs_dir/$catalog-createdb.out" 2>"$logs_dir/$catalog-createdb.err"
  "$pg_bin_dir/psql" -X -v ON_ERROR_STOP=1 \
    -h "$pg_socket_dir" -p "$pg_port" -U "$pg_superuser" -d "$catalog" \
    --set=stage2_owner="$pg_user" \
    >"$logs_dir/$catalog-schema.out" 2>"$logs_dir/$catalog-schema.err" <<'SQL'
CREATE TABLE public.datomic_kvs (
  id text PRIMARY KEY,
  rev integer,
  map text,
  val bytea
);
ALTER TABLE public.datomic_kvs OWNER TO :"stage2_owner";
SQL
done

catalog_row_count() {
  local catalog=$1
  "$pg_bin_dir/psql" -X -A -t -v ON_ERROR_STOP=1 \
    -h "$pg_socket_dir" -p "$pg_port" -U "$pg_superuser" -d "$catalog" \
    -c 'SELECT count(*) FROM public.datomic_kvs;'
}

require_empty_catalog() {
  local catalog=$1
  local row_count
  row_count=$(catalog_row_count "$catalog")
  [[ "$row_count" == 0 ]] ||
    die "restore target is not empty: $catalog has $row_count rows"
}

require_nonempty_catalog() {
  local catalog=$1
  local row_count
  row_count=$(catalog_row_count "$catalog")
  [[ "$row_count" =~ ^[0-9]+$ ]] && ((row_count > 0)) ||
    die "incremental restore target has no base state: $catalog"
}

{
  printf 'catalog\tinitial-row-count\n'
  for catalog in "${catalogs[@]}"; do
    initial_rows=$(catalog_row_count "$catalog")
    [[ "$initial_rows" == 0 ]] || die "fresh catalog is unexpectedly nonempty: $catalog"
    printf '%s\t%s\n' "$catalog" "$initial_rows"
  done
} >"$results_dir/03-catalog-preconditions.result"

write_transactor_properties() {
  local label=$1
  local catalog=$2
  local properties="$runtime_dir/transactor-$label.properties"
  local data_dir="$runtime_dir/transactor-$label-data"
  local log_dir="$runtime_dir/transactor-$label-log"
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
    printf 'pid-file=%s\n' "$runtime_dir/transactor-$label.pid"
  } >"$properties"
  chmod 600 "$properties"
  printf '%s' "$properties"
}

start_transactor() {
  local label=$1
  local catalog=$2
  local output_log="$logs_dir/transactor-$label.out"
  local command_line
  [[ -z "$transactor_pid" ]] || die "attempted to overlap external transactor fixtures"
  port_is_open "$pg_host" "$transactor_port" &&
    die "transactor port became occupied before $label"
  transactor_properties=$(write_transactor_properties "$label" "$catalog")
  "$datomic_home/bin/transactor" -Xms128m -Xmx512m "$transactor_properties" \
    >"$output_log" 2>&1 &
  transactor_pid=$!
  for ((wait_index = 0; wait_index < startup_timeout; wait_index += 1)); do
    if grep -Fq 'System started' "$output_log" &&
       kill -0 "$transactor_pid" 2>/dev/null &&
       port_is_open "$pg_host" "$transactor_port"; then
      command_line=$(tr '\0' ' ' <"/proc/$transactor_pid/cmdline" 2>/dev/null || true)
      [[ "$command_line" == *datomic.launcher* && "$command_line" == *"$transactor_properties"* ]] ||
        die "ready transactor PID does not belong to the expected fixture: $label"
      return 0
    fi
    kill -0 "$transactor_pid" 2>/dev/null || {
      tail -n 100 "$output_log" >&2
      die "external transactor exited before becoming ready: $label"
    }
    sleep 1
  done
  tail -n 100 "$output_log" >&2
  die "external transactor did not become ready within $startup_timeout seconds: $label"
}

last_result_file=

run_captured() {
  local label=$1
  local marker_prefix=$2
  shift 2
  local stdout_file="$logs_dir/$label.out"
  local stderr_file="$logs_dir/$label.err"
  local result_file="$results_dir/$label.result"
  current_step=$label
  if ! timeout --foreground --signal=TERM --kill-after=15s "${probe_timeout}s" \
    "$@" >"$stdout_file" 2>"$stderr_file"; then
    tail -n 100 "$stdout_file" >&2 || true
    tail -n 100 "$stderr_file" >&2 || true
    die "candidate step failed: $label"
  fi
  awk -v prefix="$marker_prefix" \
    'substr($0, 1, length(prefix)) == prefix {print}' "$stdout_file" >"$result_file"
  result_count=$(wc -l <"$result_file")
  [[ "$result_count" -eq 1 ]] ||
    die "candidate step emitted $result_count result markers: $label"
  last_result_file=$result_file
}

run_expected_failure() {
  local label=$1
  local marker_prefix=$2
  shift 2
  local stdout_file="$logs_dir/$label.out"
  local stderr_file="$logs_dir/$label.err"
  local result_file="$results_dir/$label.result"
  local exit_status
  current_step=$label
  if timeout --foreground --signal=TERM --kill-after=15s "${probe_timeout}s" \
    "$@" >"$stdout_file" 2>"$stderr_file"; then
    die "candidate step unexpectedly succeeded: $label"
  else
    exit_status=$?
  fi
  [[ "$exit_status" -eq 1 ]] || {
    tail -n 100 "$stdout_file" >&2 || true
    tail -n 100 "$stderr_file" >&2 || true
    die "candidate step failed with unexpected status $exit_status: $label"
  }
  if grep -q '^STAGE2-.*-RESULT ' "$stdout_file"; then
    die "expected-failure step emitted a success marker: $label"
  fi
  awk -v prefix="$marker_prefix" \
    'substr($0, 1, length(prefix)) == prefix {print}' "$stderr_file" >"$result_file"
  result_count=$(wc -l <"$result_file")
  [[ "$result_count" -eq 1 ]] ||
    die "candidate step emitted $result_count error markers: $label"
  last_result_file=$result_file
}

extract_single_number() {
  local file=$1
  local keyword=$2
  local values
  values=$(sed -nE "s/.*:$keyword ([0-9]+).*/\\1/p" "$file")
  [[ "$values" =~ ^[0-9]+$ ]] || die "could not extract one numeric :$keyword from $file"
  printf '%s' "$values"
}

extract_single_hash() {
  local file=$1
  local keyword=$2
  local values
  values=$(sed -nE "s/.*:$keyword \"([0-9a-f]{64})\".*/\\1/p" "$file")
  [[ "$values" =~ ^[0-9a-f]{64}$ ]] || die "could not extract one :$keyword hash from $file"
  printf '%s' "$values"
}

extract_single_string() {
  local file=$1
  local keyword=$2
  local values
  values=$(sed -nE "s/.*:$keyword \"([^\"]+)\".*/\\1/p" "$file")
  [[ -n "$values" && "$values" != *$'\n'* ]] ||
    die "could not extract one string :$keyword from $file"
  printf '%s' "$values"
}

require_single_literal() {
  local file=$1
  local keyword=$2
  local literal=$3
  local occurrence_count
  occurrence_count=$(
    awk -v needle=":$keyword $literal" '
      {
        text = $0
        while ((position = index(text, needle)) != 0) {
          count += 1
          text = substr(text, position + length(needle))
        }
      }
      END {print count + 0}
    ' "$file"
  )
  [[ "$occurrence_count" -eq 1 ]] ||
    die "expected exactly one :$keyword $literal in $file, found $occurrence_count"
}

sql_uri() {
  local logical_name=$1
  local catalog=$2
  printf 'datomic:sql://%s?jdbc:postgresql://%s:%s/%s?user=%s&password=%s' \
    "$logical_name" "$pg_host" "$pg_port" "$catalog" "$pg_user" "$pg_password"
}

source_uri=$(sql_uri "$source_name" "$source_catalog")
current_step=source-transactor-start
start_transactor source "$source_catalog"

run_captured 10-seed 'STAGE2-PEER-RESULT ' \
  "${candidate_java[@]}" -m stage2.peer-workload seed "$source_uri" true
seed_result=$last_result_file
t1=$(extract_single_number "$seed_result" basis-t)
t1_logical_sha=$(extract_single_hash "$seed_result" logical-sha256)
source_database_id=$(extract_single_string "$seed_result" database-id)
require_single_literal "$seed_result" as-of-t nil

[[ ! -e "$backup_root" ]] ||
  die "full backup requires a new backup root: $backup_root"
run_captured 20-full-backup 'STAGE2-BACKUP-RESULT ' \
  "${candidate_java[@]}" -m stage2.backup-probe full "$source_uri" "$backup_root"
full_backup_result=$last_result_file
full_backup_t=$(extract_single_number "$full_backup_result" latest-t)
full_backup_database_id=$(extract_single_string "$full_backup_result" db-id)
[[ "$full_backup_t" == "$t1" ]] ||
  die "full backup restore point $full_backup_t differs from seeded basis $t1"
[[ "$full_backup_database_id" == "$source_database_id" ]] ||
  die "full backup database identity differs from the source database"
grep -Fq ":ts [$t1]" "$full_backup_result" ||
  die "full backup did not publish exactly the seeded restore point"
full_copied=$(extract_single_number "$full_backup_result" copied)
full_skipped=$(extract_single_number "$full_backup_result" skipped)
((full_copied > 0)) || die "full backup did not copy any segments"
[[ "$full_skipped" == 0 ]] || die "new full backup unexpectedly skipped segments"
[[ -d "$backup_root/roots" && -n "$(find "$backup_root/roots" -type f -print -quit)" ]] ||
  die "full backup did not publish a restore-point root"
full_root_inventory="$results_dir/21-full-root-inventory.result"
find "$backup_root/roots" -maxdepth 1 -type f -printf '%f\n' | sort >"$full_root_inventory"
[[ "$(wc -l <"$full_root_inventory")" -eq 1 ]] &&
  grep -Fqx "$t1" "$full_root_inventory" ||
  die "full backup physical root inventory is not exactly t1"

run_captured 30-augment 'STAGE2-PEER-RESULT ' \
  "${candidate_java[@]}" -m stage2.peer-workload augment "$source_uri"
augment_result=$last_result_file
t2=$(extract_single_number "$augment_result" basis-t)
t2_logical_sha=$(extract_single_hash "$augment_result" logical-sha256)
augment_database_id=$(extract_single_string "$augment_result" database-id)
require_single_literal "$augment_result" as-of-t nil
[[ "$augment_database_id" == "$source_database_id" ]] ||
  die "source database identity changed during phase two"
((t2 > t1)) || die "augmented basis $t2 did not advance beyond $t1"

run_captured 40-incremental-backup 'STAGE2-BACKUP-RESULT ' \
  "${candidate_java[@]}" -m stage2.backup-probe incremental "$source_uri" "$backup_root"
incremental_backup_result=$last_result_file
incremental_backup_t=$(extract_single_number "$incremental_backup_result" latest-t)
incremental_backup_database_id=$(extract_single_string "$incremental_backup_result" db-id)
[[ "$incremental_backup_t" == "$t2" ]] ||
  die "incremental backup restore point $incremental_backup_t differs from augmented basis $t2"
[[ "$incremental_backup_database_id" == "$source_database_id" ]] ||
  die "incremental backup database identity differs from the source database"
incremental_skipped=$(extract_single_number "$incremental_backup_result" skipped)
((incremental_skipped > 0)) ||
  die "incremental backup did not reuse any previously stored segments"
grep -Fq ":ts [$t2 $t1]" "$incremental_backup_result" ||
  die "incremental backup restore-point order is not exactly [t2 t1]"
incremental_root_inventory="$results_dir/41-incremental-root-inventory.result"
root_inventory_delta="$results_dir/42-root-inventory-delta.result"
find "$backup_root/roots" -maxdepth 1 -type f -printf '%f\n' | sort >"$incremental_root_inventory"
[[ "$(wc -l <"$incremental_root_inventory")" -eq 2 ]] &&
  grep -Fqx "$t1" "$incremental_root_inventory" &&
  grep -Fqx "$t2" "$incremental_root_inventory" ||
  die "incremental backup physical roots are not exactly t1 and t2"
grep -Fvx -f "$full_root_inventory" "$incremental_root_inventory" >"$root_inventory_delta" || true
[[ "$(wc -l <"$root_inventory_delta")" -eq 1 ]] &&
  grep -Fqx "$t2" "$root_inventory_delta" ||
  die "incremental backup did not add exactly the t2 physical root"

run_captured 43-phase-three 'STAGE2-PEER-RESULT ' \
  "${candidate_java[@]}" -m stage2.peer-workload phase-three "$source_uri"
phase_three_result=$last_result_file
t3=$(extract_single_number "$phase_three_result" basis-t)
t3_logical_sha=$(extract_single_hash "$phase_three_result" logical-sha256)
phase_three_database_id=$(extract_single_string "$phase_three_result" database-id)
require_single_literal "$phase_three_result" as-of-t nil
[[ "$phase_three_database_id" == "$source_database_id" ]] ||
  die "source database identity changed during phase three"
((t3 > t2)) || die "phase-three basis $t3 did not advance beyond $t2"

run_captured 44-root-store-failure 'STAGE2-FAULT-RESULT ' \
  "${candidate_java[@]}" -m stage2.fault-probe \
  root-store-failure "$source_uri" "$backup_root"
root_store_failure_result=$last_result_file
grep -Fq ':restore-points-unchanged? true' "$root_store_failure_result" ||
  die "root-store fault did not report unchanged restore points"
post_fault_root_inventory="$results_dir/44-post-fault-root-inventory.result"
find "$backup_root/roots" -maxdepth 1 -type f -printf '%f\n' | sort >"$post_fault_root_inventory"
cmp -s "$incremental_root_inventory" "$post_fault_root_inventory" ||
  die "failed root publication changed the physical restore-point inventory"

run_captured 45-recovery-backup 'STAGE2-BACKUP-RESULT ' \
  "${candidate_java[@]}" -m stage2.backup-probe incremental "$source_uri" "$backup_root"
recovery_backup_result=$last_result_file
recovery_backup_t=$(extract_single_number "$recovery_backup_result" latest-t)
recovery_backup_database_id=$(extract_single_string "$recovery_backup_result" db-id)
[[ "$recovery_backup_t" == "$t3" ]] ||
  die "recovery backup restore point $recovery_backup_t differs from phase-three basis $t3"
[[ "$recovery_backup_database_id" == "$source_database_id" ]] ||
  die "recovery backup database identity differs from the source database"
recovery_skipped=$(extract_single_number "$recovery_backup_result" skipped)
((recovery_skipped > 0)) ||
  die "recovery backup did not reuse immutable values copied before failed root publication"
grep -Fq ":ts [$t3 $t2 $t1]" "$recovery_backup_result" ||
  die "recovery backup restore-point order is not exactly [t3 t2 t1]"
t3_root_inventory="$results_dir/45-recovery-root-inventory.result"
t3_root_delta="$results_dir/45-recovery-root-delta.result"
find "$backup_root/roots" -maxdepth 1 -type f -printf '%f\n' | sort >"$t3_root_inventory"
[[ "$(wc -l <"$t3_root_inventory")" -eq 3 ]] &&
  grep -Fqx "$t1" "$t3_root_inventory" &&
  grep -Fqx "$t2" "$t3_root_inventory" &&
  grep -Fqx "$t3" "$t3_root_inventory" ||
  die "recovery backup physical roots are not exactly t1, t2, and t3"
grep -Fvx -f "$post_fault_root_inventory" "$t3_root_inventory" >"$t3_root_delta" || true
[[ "$(wc -l <"$t3_root_delta")" -eq 1 ]] &&
  grep -Fqx "$t3" "$t3_root_delta" ||
  die "recovery backup did not publish exactly the t3 physical root"

current_step=source-transactor-stop
stop_transactor || die "could not stop source transactor safely"

restore_full_uri=$(sql_uri "$restore_full_name" "$restore_full_catalog")
require_empty_catalog "$restore_full_catalog"
run_captured 50-restore-t1-full 'STAGE2-RESTORE-RESULT ' \
  "${candidate_java[@]}" -m stage2.restore-probe \
  "$backup_root" "$t1" "$restore_full_uri" false
restore_t1_result=$last_result_file
restore_t1_copied=$(extract_single_number "$restore_t1_result" copied)
((restore_t1_copied > 0)) || die "t1 full restore did not copy any values"
grep -Fq ':incremental false' "$restore_t1_result" ||
  die "t1 full restore reported the wrong mode"

current_step=restore-full-transactor-start
start_transactor restore-full "$restore_full_catalog"
run_captured 60-restore-t1-writable 'STAGE2-PEER-RESULT ' \
  "${candidate_java[@]}" -m stage2.peer-workload post-restore \
  "$restore_full_uri" "$t1_logical_sha" "$source_database_id" "$t1"
restore_t1_writable_result=$last_result_file
grep -Fq ':restore-identity {' "$restore_t1_writable_result" ||
  die "t1 restore omitted exact database identity evidence"
require_single_literal "$restore_t1_writable_result" exact? true
current_step=restore-full-transactor-stop
stop_transactor || die "could not stop full-restore transactor safely"

restore_incr_uri=$(sql_uri "$restore_incr_name" "$restore_incr_catalog")
require_empty_catalog "$restore_incr_catalog"
run_captured 70-restore-t2-base 'STAGE2-RESTORE-RESULT ' \
  "${candidate_java[@]}" -m stage2.restore-probe \
  "$backup_root" "$t1" "$restore_incr_uri" false
restore_t2_base_result=$last_result_file
restore_t2_base_copied=$(extract_single_number "$restore_t2_base_result" copied)
((restore_t2_base_copied > 0)) ||
  die "incremental target's t1 base restore did not copy any values"
require_nonempty_catalog "$restore_incr_catalog"
run_captured 80-restore-t2-incremental 'STAGE2-RESTORE-RESULT ' \
  "${candidate_java[@]}" -m stage2.restore-probe \
  "$backup_root" "$t2" "$restore_incr_uri" true
restore_t2_incremental_result=$last_result_file
grep -Fq ':incremental true' "$restore_t2_incremental_result" ||
  die "incremental restore did not report incremental mode"
restore_t2_incremental_skipped=$(extract_single_number "$restore_t2_incremental_result" skipped)
((restore_t2_incremental_skipped > 0)) ||
  die "incremental restore did not reuse values from its t1 base"

current_step=restore-incremental-transactor-start
start_transactor restore-incremental "$restore_incr_catalog"
run_captured 90-restore-t2-writable 'STAGE2-PEER-RESULT ' \
  "${candidate_java[@]}" -m stage2.peer-workload post-restore \
  "$restore_incr_uri" "$t2_logical_sha" "$source_database_id" "$t2"
restore_t2_writable_result=$last_result_file
grep -Fq ':restore-identity {' "$restore_t2_writable_result" ||
  die "t2 restore omitted exact database identity evidence"
require_single_literal "$restore_t2_writable_result" exact? true
current_step=restore-incremental-transactor-stop
stop_transactor || die "could not stop incremental-restore transactor safely"

run_captured 91-leaf-faults 'STAGE2-FAULT-RESULT ' \
  "${candidate_java[@]}" -m stage2.fault-probe \
  leaf-faults "$backup_root" "$t3"
leaf_faults_result=$last_result_file
selected_leaf=$(extract_single_string "$leaf_faults_result" storage-key)
selected_segment_id=$(extract_single_string "$leaf_faults_result" segment-id)
[[ "$selected_leaf" =~ ^values/[0-9a-f]{2}/[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$ ]] ||
  die "fault probe selected an unsafe leaf path: $selected_leaf"
[[ "${selected_leaf##*/}" == "$selected_segment_id" ]] ||
  die "selected leaf path and segment ID differ"
[[ -f "$backup_root/$selected_leaf" && ! -L "$backup_root/$selected_leaf" ]] ||
  die "selected fault leaf is not a regular file in the canonical backup"

run_captured 92-clean-verify 'STAGE2-FAULT-RESULT ' \
  "${candidate_java[@]}" -m stage2.fault-probe \
  verify "$backup_root" "$t3" clean
clean_verify_result=$last_result_file
grep -Fq ':missing-segment-ids []' "$clean_verify_result" &&
  grep -Fq ':unreadable-segment-ids []' "$clean_verify_result" ||
  die "clean backup verification did not return empty fault vectors"

current_step=materialize-leaf-faults
cp -a -- "$backup_root" "$missing_backup_root"
cp -a -- "$backup_root" "$corrupt_backup_root"
[[ -z "$(find "$missing_backup_root" "$corrupt_backup_root" -type l -print -quit)" ]] ||
  die "physical backup copies unexpectedly contain symbolic links"
missing_leaf="$missing_backup_root/$selected_leaf"
corrupt_leaf="$corrupt_backup_root/$selected_leaf"
[[ -f "$missing_leaf" && ! -L "$missing_leaf" ]] ||
  die "selected leaf is absent from missing-fault copy"
[[ -f "$corrupt_leaf" && ! -L "$corrupt_leaf" ]] ||
  die "selected leaf is absent from corrupt-fault copy"
original_leaf_sha=$(sha256_file "$backup_root/$selected_leaf")
mv -- "$missing_leaf" "$runtime_dir/missing-leaf.saved"
cp -- "$corrupt_leaf" "$runtime_dir/corrupt-leaf.saved"
: >"$corrupt_leaf"
{
  printf 'selected.storage.key=%s\n' "$selected_leaf"
  printf 'selected.segment.id=%s\n' "$selected_segment_id"
  printf 'selected.original.sha256=%s\n' "$original_leaf_sha"
  printf 'missing.copy.leaf.present=false\n'
  printf 'corrupt.copy.leaf.sha256=%s\n' "$(sha256_file "$corrupt_leaf")"
} >"$results_dir/93-physical-leaf-faults.result"

run_captured 94-missing-verify 'STAGE2-FAULT-RESULT ' \
  "${candidate_java[@]}" -m stage2.fault-probe \
  verify "$missing_backup_root" "$t3" missing
missing_verify_result=$last_result_file
grep -Fq ":missing-segment-ids [\"$selected_segment_id\"]" "$missing_verify_result" &&
  grep -Fq ':unreadable-segment-ids []' "$missing_verify_result" ||
  die "missing-leaf verifier did not report exactly the selected segment"

run_captured 95-corrupt-verify 'STAGE2-FAULT-RESULT ' \
  "${candidate_java[@]}" -m stage2.fault-probe \
  verify "$corrupt_backup_root" "$t3" unreadable
corrupt_verify_result=$last_result_file
grep -Fq ':missing-segment-ids []' "$corrupt_verify_result" &&
  grep -Fq ":unreadable-segment-ids [\"$selected_segment_id\"]" "$corrupt_verify_result" ||
  die "corrupt-leaf verifier did not report exactly the selected segment"

fault_target_uri=$(sql_uri "$fault_target_name" "$fault_target_catalog")
require_empty_catalog "$fault_target_catalog"
run_expected_failure 96-missing-restore-rejected 'STAGE2-RESTORE-ERROR ' \
  "${candidate_java[@]}" -m stage2.restore-probe \
  "$missing_backup_root" "$t3" "$fault_target_uri" false
missing_restore_rejected_result=$last_result_file
grep -Fq ':probe-error :restore-probe-failed' \
  "$missing_restore_rejected_result" ||
  die "missing-leaf restore failed outside the verifier boundary"
require_empty_catalog "$fault_target_catalog"

run_expected_failure 97-corrupt-restore-rejected 'STAGE2-RESTORE-ERROR ' \
  "${candidate_java[@]}" -m stage2.restore-probe \
  "$corrupt_backup_root" "$t3" "$fault_target_uri" false
corrupt_restore_rejected_result=$last_result_file
grep -Fq ':probe-error :restore-probe-failed' \
  "$corrupt_restore_rejected_result" ||
  die "corrupt-leaf restore failed outside the verifier boundary"
require_empty_catalog "$fault_target_catalog"

recovery_uri=$(sql_uri "$recovery_name" "$recovery_catalog")
require_empty_catalog "$recovery_catalog"
run_captured 98-restore-interruption 'STAGE2-FAULT-RESULT ' \
  "${candidate_java[@]}" -m stage2.fault-probe \
  restore-interruption "$backup_root" "$t3" "$recovery_uri"
restore_interruption_result=$last_result_file
grep -Fq ':db-errors [:restore/read-failed]' "$restore_interruption_result" &&
  grep -Fq ':result :succeeded' "$restore_interruption_result" ||
  die "interrupted restore did not report the injected failure and successful retry"
require_nonempty_catalog "$recovery_catalog"

current_step=recovery-transactor-start
start_transactor recovery "$recovery_catalog"
run_captured 99-recovery-writable 'STAGE2-PEER-RESULT ' \
  "${candidate_java[@]}" -m stage2.peer-workload post-restore \
  "$recovery_uri" "$t3_logical_sha" "$source_database_id" "$t3"
recovery_writable_result=$last_result_file
grep -Fq ':restore-identity {' "$recovery_writable_result" ||
  die "t3 recovery omitted exact database identity evidence"
require_single_literal "$recovery_writable_result" exact? true
current_step=recovery-transactor-stop
stop_transactor || die "could not stop recovery transactor safely"

current_step=postgres-stop
stop_postgres || die "could not stop disposable PostgreSQL cluster safely"

summary_file="$work_root/stage-2-summary.properties"
{
  printf 'stage=2\n'
  printf 'status=complete\n'
  printf 'stage.complete=true\n'
  printf 'fault-injection.included=true\n'
  printf 'backend=postgresql\n'
  printf 'artifact.sha256=%s\n' "$(sha256_file "$artifact_jar")"
  printf 'dependency.manifest.sha256=%s\n' "$(sha256_file "$dependency_manifest")"
  printf 'candidate.classpath.entry.count=%s\n' "${#candidate_entries[@]}"
  printf 'candidate.original.peer=false\n'
  printf 'candidate.original.core2=false\n'
  printf 'candidate.original.transactor=false\n'
  printf 'candidate.original.nano=false\n'
  printf 'candidate.nano.sanitized=true\n'
  printf 'candidate.nano.sanitized.sha256=%s\n' "$sanitized_nano_sha"
  printf 'source.database.id=%s\n' "$source_database_id"
  printf 'source.current.as-of-t=nil\n'
  printf 'source.t1=%s\n' "$t1"
  printf 'source.t1.logical.sha256=%s\n' "$t1_logical_sha"
  printf 'source.t2=%s\n' "$t2"
  printf 'source.t2.logical.sha256=%s\n' "$t2_logical_sha"
  printf 'source.t3=%s\n' "$t3"
  printf 'source.t3.logical.sha256=%s\n' "$t3_logical_sha"
  printf 'backup.full.verified=true\n'
  printf 'backup.incremental.verified=true\n'
  printf 'backup.database-id.exact=true\n'
  printf 'backup.failed-root.unpublished=true\n'
  printf 'backup.failed-root.retry.verified=true\n'
  printf 'backup.missing-segment.exact=true\n'
  printf 'backup.unreadable-segment.exact=true\n'
  printf 'restore.t1.full.writable=true\n'
  printf 'restore.t2.incremental.writable=true\n'
  printf 'restore.missing.rejected-before-write=true\n'
  printf 'restore.unreadable.rejected-before-write=true\n'
  printf 'restore.interruption.incremental-retry=true\n'
  printf 'restore.t3.recovery.writable=true\n'
  printf 'restore.database-id.exact=true\n'
  printf 'restore.basis-t.exact=true\n'
  printf 'restore.current.as-of-t=nil\n'
  printf 'result.seed.sha256=%s\n' "$(sha256_file "$seed_result")"
  printf 'result.full-backup.sha256=%s\n' "$(sha256_file "$full_backup_result")"
  printf 'result.augment.sha256=%s\n' "$(sha256_file "$augment_result")"
  printf 'result.incremental-backup.sha256=%s\n' "$(sha256_file "$incremental_backup_result")"
  printf 'result.phase-three.sha256=%s\n' "$(sha256_file "$phase_three_result")"
  printf 'result.root-store-failure.sha256=%s\n' "$(sha256_file "$root_store_failure_result")"
  printf 'result.recovery-backup.sha256=%s\n' "$(sha256_file "$recovery_backup_result")"
  printf 'result.restore-t1-full.sha256=%s\n' "$(sha256_file "$restore_t1_result")"
  printf 'result.restore-t1-writable.sha256=%s\n' "$(sha256_file "$restore_t1_writable_result")"
  printf 'result.restore-t2-base.sha256=%s\n' "$(sha256_file "$restore_t2_base_result")"
  printf 'result.restore-t2-incremental.sha256=%s\n' "$(sha256_file "$restore_t2_incremental_result")"
  printf 'result.restore-t2-writable.sha256=%s\n' "$(sha256_file "$restore_t2_writable_result")"
  printf 'result.leaf-faults.sha256=%s\n' "$(sha256_file "$leaf_faults_result")"
  printf 'result.clean-verify.sha256=%s\n' "$(sha256_file "$clean_verify_result")"
  printf 'result.missing-verify.sha256=%s\n' "$(sha256_file "$missing_verify_result")"
  printf 'result.corrupt-verify.sha256=%s\n' "$(sha256_file "$corrupt_verify_result")"
  printf 'result.missing-restore-rejected.sha256=%s\n' "$(sha256_file "$missing_restore_rejected_result")"
  printf 'result.corrupt-restore-rejected.sha256=%s\n' "$(sha256_file "$corrupt_restore_rejected_result")"
  printf 'result.restore-interruption.sha256=%s\n' "$(sha256_file "$restore_interruption_result")"
  printf 'result.recovery-writable.sha256=%s\n' "$(sha256_file "$recovery_writable_result")"
} >"$summary_file"
{
  printf 'status=complete\n'
  printf 'scope=postgresql-lifecycle-and-fault-recovery\n'
  printf 'stage.complete=true\n'
  printf 'services.stopped=true\n'
  printf 'evidence.complete=true\n'
} >"$work_root/run-status.properties"
write_evidence_hashes

trap - EXIT
echo "Stage 2 PostgreSQL lifecycle and fault-recovery gate passed"
echo "full restore point: $t1"
echo "incremental restore point: $t2"
echo "fault-recovery restore point: $t3"
echo "candidate boundary: recovered artifact + Stage 2 harness + four stubs + 532 verified dependencies"
echo "external PostgreSQL and transactor fixtures: stopped"
echo "evidence: $work_root"
