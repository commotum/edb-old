#!/usr/bin/env bash

# Fail-closed PostgreSQL vertical slice for the recovered Datomic 1.0.7277
# Peer and Transactor.  The supplied classpath TSVs are discovery records, not
# trust anchors: every directory is snapshotted, every runtime byte is sealed,
# and the seal is checked around every candidate JVM.

set -euo pipefail

export LC_ALL=C
export TZ=UTC
export AWS_EC2_METADATA_DISABLED=true
umask 077

script_dir=$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd)
transactor_dir=$(cd -- "$script_dir/.." && pwd)
project_dir=$(cd -- "$transactor_dir/.." && pwd)
shared_scripts_dir="$project_dir/scripts"
peer_workload_source="$shared_scripts_dir/stage2/peer_workload.clj"
peer_logback_source="$shared_scripts_dir/stage2/logback-stage2.xml"
peer_builder="$shared_scripts_dir/build-source-artifact.sh"
transactor_origin_source="$script_dir/verify_structural_origins.clj"
transactor_resource_builder="$script_dir/stage-structural-resources.sh"
transactor_runtime_regression_source="$script_dir/validate_runtime_regressions.clj"

confirmation_token=DATOMIC_RECOVERED_PAIR_DISPOSABLE
peer_artifact_name=datomic-rev-peer-1.0.7277-source.jar
expected_peer_dependency_count=532
expected_peer_classpath_count=535
expected_transactor_classpath_count=537
expected_sanitized_nano_sha=08f9699d6e35b9e052ec85ee15e0896b3a685c74e6e226d8cd89d9c61dbf8d5f
expected_sanitized_nano_bytes=81218
original_peer_sha=cb55c9d01e155f9965e3332cd8ef70be42cdefacb50c072076e9ecf22b9a23ba
original_transactor_sha=d90819b57e2138085ddc26c93314f953c2cd776d1d1aa8b43a6bec1e475d3692
original_core2_sha=81fdf81586c7be1a4b61655b568348d12db7892cb4562d0db7529bbc7af8a94b
original_nano_sha=fbde8184da356bd3a9995a9f05f01493efe4baa87c4d48377cda3e53331807cd
licensed_key_sha=f3627f52580b84fe8f536423643fb46999fd2458498989307f2820d778eb73a1
licensed_trust_sha=ca64f839d051d909974a624b4345d83cc739a0b90edbe54e7c3605a2fd8fc13b
expected_stub_manifest_sha=5bb9a3440c4fe00f3b299a7e48fbf2aa6ef5f3ef2208fe389af13304eb267e02

usage() {
  cat <<EOF
Usage:
  $0 [OPTIONS]

Required candidate inputs:
  --datomic-home DIR                    Distribution root used only for the
                                        hash-bound third-party lib/*.jar set.
  --peer-artifact JAR                  Recovered Peer artifact. It must equal
                                        a fresh build from the current repo.
  --peer-dependency-manifest TSV       Peer sha256/path dependency manifest;
                                        must equal the artifact's embedded TSV.
  --peer-classpath-manifest TSV        position/role/sha256/path discovery TSV.
  --transactor-classpath-manifest TSV  ordinal/role/path discovery TSV from a
                                        structural candidate preparation.
  --sanitized-nano JAR                 Canonical sanitized Nano derivative.
  --java-bin FILE                      Candidate runtime; exact Corretto
                                        11.0.22.7.1 is required.
  --build-java-bin FILE                Java 21 runtime used only by the checked
                                        Peer artifact builder (default: java).

PostgreSQL inputs:
  --postgres-root DIR   Extracted PostgreSQL root; infers bin/lib/share paths.
  --pg-bin-dir DIR      Directory containing PostgreSQL server/client tools.
  --pg-lib-dir DIR      Optional runtime library directory.
  --pg-share-dir DIR    Optional initdb share directory.
  --pg-major N          PostgreSQL major below postgres-root (default: 16).
  --pg-host HOST        Must be 127.0.0.1 (default).
  --pg-port PORT        Dedicated PostgreSQL port (default: 55439).
  --pg-superuser NAME   Fresh-cluster superuser (default: recovered_admin).
  --pg-user NAME        Datomic SQL role (default: datomic_recovered).
  --pg-password VALUE   Disposable password (default: datomic_recovered).
  --catalog NAME        Fresh SQL catalog (default: datomic_recovered_pair).
  --database-name NAME  Datomic logical name (default: recovered-pair).

Runtime and evidence:
  --work-root DIR        Must be absent or empty, non-symlink, and have basename
                         datomic-recovered-pair-*. Default: a fresh /tmp root.
  --pgdata DIR           New descendant of work-root.
  --pg-socket-dir DIR    New descendant of work-root.
  --transactor-port PORT Dedicated transport port (default: 54339).
  --startup-timeout SEC  Transactor readiness deadline (default: 180).
  --probe-timeout SEC    Peer/origin JVM deadline (default: 300).
  --confirm-disposable $confirmation_token
                         Required before starting any service.
  --dry-run              Build, stage, seal, and validate both candidate
                         classpaths/origins without starting services.
  -h, --help             Show this help.

Executing order:
  seed -> graceful stop -> restart -> equal snapshot -> augment -> graceful
  persistent-index publication -> stop -> restart/index adoption -> equal
  snapshot -> final service shutdown.
EOF
}

die() {
  echo "recovered-pair-postgresql: $*" >&2
  exit 1
}

need_command() {
  command -v "$1" >/dev/null 2>&1 || die "missing required command: $1"
}

sha256_file() {
  sha256sum -- "$1" | awk '{print $1}'
}

sha256_text() {
  printf '%s' "$1" | sha256sum | awk '{print $1}'
}

require_safe_path_text() {
  local label=$1
  local value=$2
  [[ "$value" == /* ]] || die "$label must be absolute: $value"
  [[ "$value" != *$'\n'* && "$value" != *$'\r'* && "$value" != *$'\t'* ]] ||
    die "$label contains a control character: $value"
  [[ "$value" != *:* && "$value" != *'*'* && "$value" != *'?'* &&
     "$value" != *'['* && "$value" != *']'* && "$value" != *'\\'* ]] ||
    die "$label contains a classpath separator, wildcard, or backslash: $value"
  [[ "$value" != *[[:space:]]* ]] || die "$label may not contain whitespace: $value"
}

assert_no_symlink_components() {
  local label=$1
  local path=$2
  local current=/
  local component
  local -a components=()
  require_safe_path_text "$label" "$path"
  IFS=/ read -r -a components <<<"${path#/}"
  for component in "${components[@]}"; do
    [[ -n "$component" ]] || continue
    current="${current%/}/$component"
    [[ ! -L "$current" ]] || die "$label traverses symbolic link: $current"
  done
}

require_regular_file() {
  local label=$1
  local path=$2
  [[ -f "$path" && ! -L "$path" ]] || die "$label is not a regular non-symlink file: $path"
  assert_no_symlink_components "$label" "$path"
  [[ "$(readlink -f -- "$path")" == "$path" ]] || die "$label is not canonical: $path"
}

require_clean_directory() {
  local label=$1
  local path=$2
  local bad
  [[ -d "$path" && ! -L "$path" ]] || die "$label is not a non-symlink directory: $path"
  assert_no_symlink_components "$label" "$path"
  [[ "$(readlink -f -- "$path")" == "$path" ]] || die "$label is not canonical: $path"
  bad=$(find -P "$path" -mindepth 1 ! -type f ! -type d -print -quit)
  [[ -z "$bad" ]] || die "$label contains a symlink or special entry: $bad"
}

require_identifier() {
  local label=$1
  local value=$2
  [[ "$value" =~ ^[a-z][a-z0-9_]*$ ]] || die "$label must match [a-z][a-z0-9_]*: $value"
}

require_database_name() {
  local value=$1
  [[ "$value" =~ ^[a-z][a-z0-9-]*$ ]] ||
    die "database-name must use lowercase letters, digits, and hyphens: $value"
}

require_port() {
  local label=$1
  local value=$2
  [[ "$value" =~ ^[0-9]+$ ]] || die "$label is not an integer: $value"
  ((value >= 1024 && value <= 65535)) || die "$label is outside 1024..65535: $value"
}

require_positive_integer() {
  local label=$1
  local value=$2
  [[ "$value" =~ ^[1-9][0-9]*$ ]] || die "$label must be a positive integer: $value"
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

port_is_open() {
  local host=$1
  local port=$2
  (exec 9<>"/dev/tcp/$host/$port") >/dev/null 2>&1
}

wait_for_closed_port() {
  local label=$1
  local host=$2
  local port=$3
  local attempt
  for ((attempt = 0; attempt < 40; attempt += 1)); do
    port_is_open "$host" "$port" || return 0
    sleep 1
  done
  echo "$label port remained open: $host:$port" >&2
  return 1
}

datomic_home=${DATOMIC_HOME:-}
peer_artifact_input=
peer_dependency_manifest_input=
peer_classpath_manifest_input=
transactor_classpath_manifest_input=
sanitized_nano_input=
java_bin_input=/tmp/amazon-corretto-11.0.22.7.1/bin/java
build_java_bin_input=
postgres_root=${RECOVERED_PAIR_POSTGRES_ROOT:-}
pg_bin_dir=
pg_lib_dir=
pg_share_dir=
pg_major=16
pg_host=127.0.0.1
pg_port=55439
pg_superuser=recovered_admin
pg_user=datomic_recovered
pg_password=datomic_recovered
catalog=datomic_recovered_pair
database_name=recovered-pair
work_root_input=
pgdata_input=
pg_socket_dir_input=
transactor_port=54339
startup_timeout=180
probe_timeout=300
confirmation=${RECOVERED_PAIR_CONFIRM_DISPOSABLE:-}
dry_run=false

while (($#)); do
  case "$1" in
    --datomic-home) datomic_home=${2:?missing value for --datomic-home}; shift 2 ;;
    --peer-artifact) peer_artifact_input=${2:?missing value for --peer-artifact}; shift 2 ;;
    --peer-dependency-manifest) peer_dependency_manifest_input=${2:?missing value for --peer-dependency-manifest}; shift 2 ;;
    --peer-classpath-manifest) peer_classpath_manifest_input=${2:?missing value for --peer-classpath-manifest}; shift 2 ;;
    --transactor-classpath-manifest) transactor_classpath_manifest_input=${2:?missing value for --transactor-classpath-manifest}; shift 2 ;;
    --sanitized-nano) sanitized_nano_input=${2:?missing value for --sanitized-nano}; shift 2 ;;
    --java-bin) java_bin_input=${2:?missing value for --java-bin}; shift 2 ;;
    --build-java-bin) build_java_bin_input=${2:?missing value for --build-java-bin}; shift 2 ;;
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
    --probe-timeout) probe_timeout=${2:?missing value for --probe-timeout}; shift 2 ;;
    --confirm-disposable) confirmation=${2:?missing value for --confirm-disposable}; shift 2 ;;
    --dry-run) dry_run=true; shift ;;
    -h|--help) usage; exit 0 ;;
    *) usage >&2; die "unknown argument: $1" ;;
  esac
done

for ambient_name in CLASSPATH JAVA_TOOL_OPTIONS _JAVA_OPTIONS JDK_JAVA_OPTIONS; do
  [[ -z "${!ambient_name-}" ]] || die "ambient $ambient_name must be unset or empty"
done

for command_name in awk chmod cmp cp dirname env find grep java kill mkdir mktemp \
  mv readlink rm sed sha256sum sleep sort stat tail timeout tr uname unzip uniq wc xargs; do
  need_command "$command_name"
done
env --help 2>&1 | grep -q -- '--default-signal' ||
  die "GNU env with --default-signal is required for deterministic JVM signal handling"
[[ "$(uname -s)" == Linux && -r /proc/self/cmdline && -r /proc/self/status ]] ||
  die "Linux /proc process identity semantics are required"
[[ "$pg_major" =~ ^[0-9]+$ ]] || die "pg-major is not numeric: $pg_major"
require_positive_integer startup-timeout "$startup_timeout"
require_positive_integer probe-timeout "$probe_timeout"
[[ "$pg_host" == 127.0.0.1 ]] || die "PostgreSQL must bind only to 127.0.0.1"
require_port pg-port "$pg_port"
require_port transactor-port "$transactor_port"
[[ "$pg_port" != "$transactor_port" ]] || die "PostgreSQL and Transactor ports must differ"
require_identifier pg-superuser "$pg_superuser"
require_identifier pg-user "$pg_user"
[[ "$pg_superuser" != "$pg_user" ]] || die "PostgreSQL roles must differ"
[[ "$pg_password" =~ ^[A-Za-z0-9_.-]+$ ]] || die "pg-password contains unsafe characters"
require_identifier catalog "$catalog"
[[ "$catalog" == datomic_* ]] || die "catalog must be visibly Datomic-specific: $catalog"
require_database_name "$database_name"

[[ -n "$datomic_home" ]] || die "provide --datomic-home"
[[ "$datomic_home" == /* ]] || die "datomic-home must be absolute"
assert_no_symlink_components datomic-home "$datomic_home"
datomic_home=$(readlink -f -- "$datomic_home")
require_clean_directory datomic-home "$datomic_home"
[[ -d "$datomic_home/lib" ]] || die "datomic-home has no lib directory"

for input_spec in \
  "peer-artifact:$peer_artifact_input" \
  "peer-dependency-manifest:$peer_dependency_manifest_input" \
  "peer-classpath-manifest:$peer_classpath_manifest_input" \
  "transactor-classpath-manifest:$transactor_classpath_manifest_input" \
  "sanitized-nano:$sanitized_nano_input"; do
  input_label=${input_spec%%:*}
  input_value=${input_spec#*:}
  [[ -n "$input_value" ]] || die "provide --$input_label"
  [[ "$input_value" == /* ]] || die "$input_label must be absolute"
  assert_no_symlink_components "$input_label" "$input_value"
  input_value=$(readlink -f -- "$input_value")
  require_regular_file "$input_label" "$input_value"
  case "$input_label" in
    peer-artifact) peer_artifact=$input_value ;;
    peer-dependency-manifest) peer_dependency_manifest=$input_value ;;
    peer-classpath-manifest) peer_classpath_manifest=$input_value ;;
    transactor-classpath-manifest) transactor_classpath_manifest=$input_value ;;
    sanitized-nano) sanitized_nano=$input_value ;;
  esac
done

case "$peer_artifact" in "$datomic_home"/*) die "recovered Peer artifact may not live in datomic-home" ;; esac
case "$sanitized_nano" in "$datomic_home"/*) die "sanitized Nano may not live in datomic-home" ;; esac
[[ "$(sha256_file "$sanitized_nano")" == "$expected_sanitized_nano_sha" ]] ||
  die "sanitized Nano SHA-256 mismatch"
[[ "$(stat -c '%s' -- "$sanitized_nano")" == "$expected_sanitized_nano_bytes" ]] ||
  die "sanitized Nano byte-size mismatch"
unzip -tqq "$sanitized_nano" || die "sanitized Nano is not a valid JAR"
[[ -z "$(unzip -Z1 "$sanitized_nano" | grep -E '\.(jks|p12|pfx|keystore)$' || true)" ]] ||
  die "sanitized Nano contains a key/trust-store entry"

if [[ -n "$work_root_input" ]]; then
  [[ "$work_root_input" == /* ]] || die "work-root must be absolute"
  [[ ! -L "$work_root_input" ]] || die "work-root may not be a symlink"
  work_root=$(readlink -m -- "$work_root_input")
  require_safe_path_text work-root "$work_root"
  [[ "${work_root##*/}" == datomic-recovered-pair-* ]] ||
    die "work-root basename must start datomic-recovered-pair-: $work_root"
  assert_no_symlink_components work-root-parent "$(dirname -- "$work_root")"
  if [[ -e "$work_root" ]]; then
    [[ -d "$work_root" && ! -L "$work_root" ]] || die "work-root is not a plain directory"
    [[ -z "$(find -P "$work_root" -mindepth 1 -print -quit)" ]] ||
      die "refusing non-empty work-root: $work_root"
  fi
  mkdir -p -- "$work_root"
else
  work_root=$(mktemp -d -t datomic-recovered-pair-run.XXXXXXXX)
fi

inputs_dir="$work_root/inputs"
logs_dir="$work_root/logs"
results_dir="$work_root/results"
runtime_dir="$work_root/runtime"
staging_dir="$work_root/staging"
mkdir -p -- "$inputs_dir" "$logs_dir" "$results_dir" "$runtime_dir" "$staging_dir"
pgdata=$(readlink -m -- "${pgdata_input:-$runtime_dir/postgres-data}")
pg_socket_dir=$(readlink -m -- "${pg_socket_dir_input:-$runtime_dir/postgres-socket}")
for path_spec in "pgdata:$pgdata" "pg-socket-dir:$pg_socket_dir"; do
  path_label=${path_spec%%:*}
  path_value=${path_spec#*:}
  require_safe_path_text "$path_label" "$path_value"
  assert_descendant "$path_label" "$path_value"
  [[ ! -e "$path_value" ]] || die "$path_label must not exist at run start: $path_value"
done
((${#pg_socket_dir} < 90)) || die "pg-socket-dir is too long"

[[ "$java_bin_input" == /* ]] || die "java-bin must be absolute"
assert_no_symlink_components java-bin "$java_bin_input"
java_bin=$(readlink -f -- "$java_bin_input")
require_regular_file java "$java_bin"
"$java_bin" -version 2>&1 | grep -q 'Corretto-11.0.22.7.1' ||
  die "candidate runtime must be exact Amazon Corretto 11.0.22.7.1: $java_bin"
if [[ -z "$build_java_bin_input" ]]; then
  build_java_bin_input=$(command -v java)
fi
build_java_bin=$(readlink -f -- "$build_java_bin_input")
require_regular_file build-java "$build_java_bin"

if [[ -n "$postgres_root" ]]; then
  [[ "$postgres_root" == /* ]] || die "postgres-root must be absolute"
  assert_no_symlink_components postgres-root "$postgres_root"
  postgres_root=$(readlink -f -- "$postgres_root")
  [[ -d "$postgres_root" && ! -L "$postgres_root" ]] ||
    die "postgres-root is not a plain directory: $postgres_root"
  assert_no_symlink_components postgres-root "$postgres_root"
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
  pg_lib_dir=$(readlink -f -- "$pg_lib_dir")
  require_safe_path_text pg-lib-dir "$pg_lib_dir"
  [[ -d "$pg_lib_dir" ]] || die "missing PostgreSQL library directory: $pg_lib_dir"
  export LD_LIBRARY_PATH="$pg_lib_dir${LD_LIBRARY_PATH:+:$LD_LIBRARY_PATH}"
fi
if [[ -n "$pg_share_dir" ]]; then
  pg_share_dir=$(readlink -f -- "$pg_share_dir")
  require_safe_path_text pg-share-dir "$pg_share_dir"
  [[ -d "$pg_share_dir" ]] || die "missing PostgreSQL share directory: $pg_share_dir"
fi
for pg_tool in postgres initdb pg_ctl pg_controldata psql createdb; do
  [[ -x "$pg_bin_dir/$pg_tool" ]] || die "missing PostgreSQL tool: $pg_bin_dir/$pg_tool"
done

port_is_open "$pg_host" "$pg_port" && die "PostgreSQL port is occupied: $pg_port"
port_is_open "$pg_host" "$transactor_port" && die "Transactor port is occupied: $transactor_port"

snapshot_directory() {
  local label=$1
  local source=$2
  local destination=$3
  local before="$inputs_dir/$label-source-before.sha256"
  local after="$inputs_dir/$label-source-after.sha256"
  local copied="$inputs_dir/$label-staged.sha256"
  require_clean_directory "$label" "$source"
  [[ -z "$(find -P "$source" -mindepth 1 -print -quit)" ]] &&
    die "$label source directory is empty: $source"
  mkdir -p -- "$destination"
  (
    cd -- "$source"
    find . -type f -print0 | sort -z | xargs -0 -r sha256sum
  ) >"$before"
  cp -a -- "$source/." "$destination/"
  (
    cd -- "$source"
    find . -type f -print0 | sort -z | xargs -0 -r sha256sum
  ) >"$after"
  cmp -s "$before" "$after" || die "$label changed while being snapshotted"
  (
    cd -- "$destination"
    find . -type f -print0 | sort -z | xargs -0 -r sha256sum
  ) >"$copied"
  cmp -s "$before" "$copied" || die "$label staged bytes differ from source"
  require_clean_directory "$label-staged" "$destination"
  chmod -R a-w -- "$destination"
}

# Build the Peer from current checked-in sources and demand byte identity with
# the explicit artifact input.  Java 21 is a build-tool boundary only; it is
# never included in either candidate classpath or used by a candidate JVM.
peer_build_root="$work_root/peer-build"
mkdir -p -- "$peer_build_root"
if ! PATH="$(dirname -- "$build_java_bin"):$PATH" \
  "$peer_builder" "$datomic_home" "$peer_build_root" \
  >"$logs_dir/peer-current-build.out" 2>"$logs_dir/peer-current-build.err"; then
  tail -n 100 "$logs_dir/peer-current-build.out" >&2 || true
  tail -n 100 "$logs_dir/peer-current-build.err" >&2 || true
  die "fresh current Peer build failed"
fi
built_peer_artifact="$peer_build_root/$peer_artifact_name"
require_regular_file built-peer-artifact "$built_peer_artifact"
cmp -s "$built_peer_artifact" "$peer_artifact" ||
  die "provided Peer artifact is stale or differs from a fresh current-repo build"
peer_artifact_sha=$(sha256_file "$built_peer_artifact")
unzip -tqq "$built_peer_artifact" || die "fresh Peer artifact is not a valid JAR"

embedded_peer_dependencies="$inputs_dir/peer-dependencies-embedded.tsv"
unzip -p "$built_peer_artifact" META-INF/datomic-rev/inputs/dependencies.tsv \
  >"$embedded_peer_dependencies" || die "Peer artifact lacks embedded dependencies.tsv"
cmp -s "$embedded_peer_dependencies" "$peer_dependency_manifest" ||
  die "explicit Peer dependency manifest differs from the current artifact"
[[ "$(sed -n '1p' "$peer_dependency_manifest")" == $'sha256\tpath' ]] ||
  die "Peer dependency manifest header is invalid"

peer_dependency_paths=()
peer_dependency_shas=()
peer_dependency_relative_paths=()
peer_resolved_dependencies=()
peer_dependency_count=0
nano_manifest_rows=0
while IFS=$'\t' read -r expected_sha relative_path extra; do
  [[ "$expected_sha" == sha256 ]] && continue
  [[ "$expected_sha" =~ ^[0-9a-f]{64}$ && -n "$relative_path" && -z "${extra:-}" ]] ||
    die "malformed Peer dependency manifest row"
  [[ "$relative_path" =~ ^lib/[A-Za-z0-9._+-]+[.]jar$ ]] ||
    die "Peer dependency is not a direct lib/*.jar path: $relative_path"
  dependency="$datomic_home/$relative_path"
  assert_no_symlink_components peer-dependency "$dependency"
  dependency=$(readlink -f -- "$dependency")
  require_regular_file peer-dependency "$dependency"
  case "$dependency" in "$datomic_home"/lib/*) ;; *) die "Peer dependency escaped datomic-home/lib" ;; esac
  [[ "$(sha256_file "$dependency")" == "$expected_sha" ]] ||
    die "Peer dependency hash mismatch: $relative_path"
  case "${dependency##*/}" in
    peer-*.jar|core2-*.jar|datomic-transactor*.jar|*transactor-pro*.jar)
      die "forbidden implementation in Peer dependencies: $dependency" ;;
  esac
  peer_dependency_paths+=("$dependency")
  peer_dependency_shas+=("$expected_sha")
  peer_dependency_relative_paths+=("$relative_path")
  if [[ "$expected_sha" == "$original_nano_sha" || "$relative_path" == lib/nano-impl-0.1.325.jar ]]; then
    [[ "$expected_sha" == "$original_nano_sha" && "$relative_path" == lib/nano-impl-0.1.325.jar ]] ||
      die "original Nano dependency appeared under a noncanonical identity"
    peer_resolved_dependencies+=("$sanitized_nano")
    ((nano_manifest_rows += 1))
  else
    peer_resolved_dependencies+=("$dependency")
  fi
  ((peer_dependency_count += 1))
done <"$peer_dependency_manifest"
[[ "$peer_dependency_count" -eq "$expected_peer_dependency_count" ]] ||
  die "expected $expected_peer_dependency_count Peer dependencies, found $peer_dependency_count"
[[ "$nano_manifest_rows" -eq 1 ]] || die "Peer manifest must identify original Nano exactly once"
duplicate_peer_paths=$(printf '%s\n' "${peer_dependency_relative_paths[@]}" | sort | uniq -d)
[[ -z "$duplicate_peer_paths" ]] || die "duplicate Peer dependency paths: $duplicate_peer_paths"

# Validate the Peer discovery TSV against the artifact/dependency provenance.
[[ "$(sed -n '1p' "$peer_classpath_manifest")" == $'position\trole\tsha256\tpath' ]] ||
  die "Peer classpath manifest header is invalid"
peer_stub_source=
peer_manifest_rows=0
while IFS=$'\t' read -r position role recorded_sha recorded_path extra; do
  [[ "$position" == position ]] && continue
  [[ "$position" =~ ^[0-9]+$ && "$recorded_sha" =~ ^[0-9a-f]{64}$ &&
     -n "$role" && -n "$recorded_path" && -z "${extra:-}" ]] ||
    die "malformed Peer classpath row"
  ((peer_manifest_rows += 1))
  [[ "$position" -eq "$peer_manifest_rows" ]] || die "Peer classpath positions are discontinuous"
  require_safe_path_text peer-classpath-path "$recorded_path"
  if ((position == 1)); then
    [[ "$role" == recovered-artifact && "$recorded_path" == "$peer_artifact" &&
       "$recorded_sha" == "$peer_artifact_sha" ]] ||
      die "Peer classpath artifact row differs from the explicit current artifact"
  elif ((position == 2)); then
    [[ "$role" == stage2-test-harness && "$recorded_path" == "$shared_scripts_dir" ]] ||
      die "Peer classpath harness discovery row is not the repository harness"
  elif ((position == 3)); then
    [[ "$role" == compile-only-hotrod-stubs && "$recorded_sha" == "$expected_stub_manifest_sha" ]] ||
      die "Peer classpath stub row is not canonical"
    peer_stub_source=$recorded_path
  else
    dependency_index=$((position - 4))
    ((dependency_index >= 0 && dependency_index < peer_dependency_count)) ||
      die "Peer classpath has an excess dependency row"
    expected_path=${peer_dependency_paths[$dependency_index]}
    expected_sha=${peer_dependency_shas[$dependency_index]}
    if [[ "$expected_sha" == "$original_nano_sha" ]]; then
      [[ "$role" == sanitized-nano-dependency &&
         "$recorded_sha" == "$expected_sanitized_nano_sha" ]] ||
        die "Peer classpath did not replace original Nano with the canonical derivative"
      require_regular_file peer-discovery-sanitized-nano "$recorded_path"
      [[ "$(sha256_file "$recorded_path")" == "$expected_sanitized_nano_sha" &&
         "$(stat -c '%s' -- "$recorded_path")" == "$expected_sanitized_nano_bytes" ]] ||
        die "Peer discovery Nano derivative bytes differ from the canonical derivative"
    else
      [[ "$role" == dependency && "$recorded_path" == "$expected_path" &&
         "$recorded_sha" == "$expected_sha" ]] ||
        die "Peer classpath dependency row differs at position $position"
    fi
  fi
done <"$peer_classpath_manifest"
[[ "$peer_manifest_rows" -eq "$expected_peer_classpath_count" ]] ||
  die "Peer classpath has $peer_manifest_rows entries, expected $expected_peer_classpath_count"
[[ -n "$peer_stub_source" ]] || die "Peer classpath omitted its stub root"
assert_no_symlink_components peer-stub-source "$peer_stub_source"
peer_stub_source=$(readlink -f -- "$peer_stub_source")
require_clean_directory peer-stub-source "$peer_stub_source"

# Unpack the freshly built Peer as a runtime-only derivative, verify and remove
# the two licensed stores, and never put the canonical JAR itself on a JVM CP.
peer_runtime_root="$staging_dir/peer-runtime"
mkdir -p -- "$peer_runtime_root"
key_entry_hash=$(unzip -p "$built_peer_artifact" datomic/transactor-key.jks | sha256sum | awk '{print $1}')
trust_entry_hash=$(unzip -p "$built_peer_artifact" datomic/transactor-trust.jks | sha256sum | awk '{print $1}')
[[ "$key_entry_hash" == "$licensed_key_sha" && "$trust_entry_hash" == "$licensed_trust_sha" ]] ||
  die "canonical Peer key/trust provenance differs from the bound licensed evidence"
unzip -q "$built_peer_artifact" -d "$peer_runtime_root"
rm -f -- "$peer_runtime_root/datomic/transactor-key.jks" \
  "$peer_runtime_root/datomic/transactor-trust.jks"
[[ ! -e "$peer_runtime_root/datomic/transactor-key.jks" &&
   ! -e "$peer_runtime_root/datomic/transactor-trust.jks" ]] ||
  die "licensed Peer key/trust stores survived runtime sanitization"
require_clean_directory peer-runtime "$peer_runtime_root"
[[ -z "$(find "$peer_runtime_root" -type f \
  \( -name '*.jks' -o -name '*.p12' -o -name '*.pfx' -o -name '*.keystore' \) -print -quit)" ]] ||
  die "Peer runtime derivative contains a key/trust store"
chmod -R a-w -- "$peer_runtime_root"

peer_harness_root="$staging_dir/peer-harness"
mkdir -p -- "$peer_harness_root/stage2"
cp -- "$peer_workload_source" "$peer_harness_root/stage2/peer_workload.clj"
cp -- "$peer_logback_source" "$peer_harness_root/logback-stage2.xml"
chmod -R a-w -- "$peer_harness_root"
peer_stub_root="$staging_dir/peer-stubs"
snapshot_directory peer-stubs "$peer_stub_source" "$peer_stub_root"
stub_manifest="$inputs_dir/peer-stubs.tsv"
printf 'sha256\tpath\n' >"$stub_manifest"
while IFS= read -r stub_class; do
  printf '%s\t%s\n' "$(sha256_file "$peer_stub_root/$stub_class")" "$stub_class" >>"$stub_manifest"
done < <(find "$peer_stub_root" -type f -name '*.class' -printf '%P\n' | sort)
[[ "$(sha256_file "$stub_manifest")" == "$expected_stub_manifest_sha" ]] ||
  die "staged Peer stubs differ from canonical bytes"

# Parse the Transactor discovery TSV and stage all five candidate-owned roots.
# Its historical source hashes are deliberately not trusted; a fresh source
# manifest is generated below from the staged current bytes.
[[ "$(sed -n '1p' "$transactor_classpath_manifest")" == $'ordinal\trole\tpath' ]] ||
  die "Transactor classpath manifest header is invalid"
transactor_discovery_paths=()
transactor_discovery_roles=()
transactor_dependency_paths=()
transactor_manifest_rows=0
while IFS=$'\t' read -r ordinal role path extra; do
  [[ "$ordinal" == ordinal ]] && continue
  [[ "$ordinal" =~ ^[0-9]+$ && -n "$role" && -n "$path" && -z "${extra:-}" ]] ||
    die "malformed Transactor classpath row"
  ((transactor_manifest_rows += 1))
  [[ "$ordinal" -eq "$transactor_manifest_rows" ]] ||
    die "Transactor classpath ordinals are discontinuous"
  require_safe_path_text transactor-classpath-path "$path"
  assert_no_symlink_components transactor-classpath-path "$path"
  path=$(readlink -f -- "$path")
  if ((ordinal <= 5)); then
    case "$ordinal:$role" in
      1:transactor-source|2:peer-core2-support-source|3:transactor-java|4:candidate-resources|5:hotrod-load-stubs) ;;
      *) die "unexpected Transactor owned role at ordinal $ordinal: $role" ;;
    esac
    require_clean_directory "transactor-$role" "$path"
    case "$path" in "$datomic_home"/*) die "candidate-owned Transactor root lives in datomic-home: $path" ;; esac
    transactor_discovery_roles+=("$role")
    transactor_discovery_paths+=("$path")
  else
    dependency_index=$((ordinal - 6))
    ((dependency_index >= 0 && dependency_index < peer_dependency_count)) ||
      die "Transactor classpath has an excess dependency row"
    expected_path=${peer_resolved_dependencies[$dependency_index]}
    if [[ "${peer_dependency_shas[$dependency_index]}" == "$original_nano_sha" ]]; then
      [[ "$(sha256_file "$path")" == "$expected_sanitized_nano_sha" &&
         "$(stat -c '%s' -- "$path")" == "$expected_sanitized_nano_bytes" ]] ||
        die "Transactor discovery Nano derivative bytes differ from the canonical derivative"
      path=$sanitized_nano
    else
      [[ "$path" == "$expected_path" ]] ||
        die "Transactor and Peer resolved dependencies differ at index $dependency_index"
    fi
    [[ "$role" == "dependency:${path##*/}" ]] ||
      die "Transactor dependency role/path mismatch at ordinal $ordinal"
    transactor_dependency_paths+=("$path")
  fi
done <"$transactor_classpath_manifest"
[[ "$transactor_manifest_rows" -eq "$expected_transactor_classpath_count" ]] ||
  die "Transactor classpath has $transactor_manifest_rows entries, expected $expected_transactor_classpath_count"
[[ "${#transactor_dependency_paths[@]}" -eq "$expected_peer_dependency_count" ]] ||
  die "Transactor dependency count is not $expected_peer_dependency_count"
[[ "${transactor_discovery_paths[0]}" == "$transactor_dir/src-clj" ]] ||
  die "Transactor source role is not the current repository source root"

transactor_stage_roots=()
for owned_index in 0 1 2 3 4; do
  role=${transactor_discovery_roles[$owned_index]}
  source=${transactor_discovery_paths[$owned_index]}
  if ((owned_index == 3)); then
    resource_build_root="$work_root/transactor-resource-build"
    if ! "$transactor_resource_builder" "$transactor_dir/candidate-resources" \
      "$resource_build_root" 1.0.7277 >"$logs_dir/transactor-resource-build.out" \
      2>"$logs_dir/transactor-resource-build.err"; then
      tail -n 100 "$logs_dir/transactor-resource-build.out" >&2 || true
      tail -n 100 "$logs_dir/transactor-resource-build.err" >&2 || true
      die "fresh Transactor candidate-resource preparation failed"
    fi
    source="$resource_build_root/runtime"
  fi
  destination="$staging_dir/transactor-$((owned_index + 1))-$role"
  snapshot_directory "transactor-$((owned_index + 1))-$role" "$source" "$destination"
  transactor_stage_roots+=("$destination")
done
transactor_source_root=${transactor_stage_roots[0]}
core2_source_root=${transactor_stage_roots[1]}
transactor_java_root=${transactor_stage_roots[2]}
transactor_resource_root=${transactor_stage_roots[3]}
transactor_stub_root=${transactor_stage_roots[4]}
[[ -z "$(find "$transactor_resource_root" -type f \
  \( -name '*.jks' -o -name '*.p12' -o -name '*.pfx' -o -name '*.keystore' \) -print -quit)" ]] ||
  die "Transactor candidate resources contain key/trust material"

structural_output_root=$(cd -- "$(dirname -- "$transactor_classpath_manifest")/.." && pwd)
source_template="$structural_output_root/discovery/candidate-sources.tsv"
duplicate_template="$structural_output_root/discovery/dependency-class-duplicates.tsv"
require_regular_file structural-source-template "$source_template"
require_regular_file structural-duplicate-template "$duplicate_template"

runtime_preparation_dir="$inputs_dir/transactor-runtime-preparation"
mkdir -p -- "$runtime_preparation_dir"
fresh_candidate_sources="$runtime_preparation_dir/candidate-sources.tsv"
printf 'owner\tnamespace\tsource_entry\tsha256\tinitializer_namespace\n' >"$fresh_candidate_sources"
fresh_source_rows=0
while IFS=$'\t' read -r owner namespace source_entry _old_sha initializer_namespace extra; do
  [[ "$owner" == owner ]] && continue
  [[ -n "$owner" && -n "$namespace" && -n "$source_entry" &&
     -n "$initializer_namespace" && -z "${extra:-}" ]] ||
    die "malformed structural source-template row"
  case "$owner" in
    transactor) source_root=$transactor_source_root ;;
    peer-core2) source_root=$core2_source_root ;;
    *) die "unknown structural source owner: $owner" ;;
  esac
  [[ "$source_entry" =~ ^[A-Za-z0-9_./-]+[.]clj[c]?$ ]] ||
    die "unsafe structural source entry: $source_entry"
  source_file="$source_root/$source_entry"
  require_regular_file staged-structural-source "$source_file"
  printf '%s\t%s\t%s\t%s\t%s\n' "$owner" "$namespace" "$source_entry" \
    "$(sha256_file "$source_file")" "$initializer_namespace" >>"$fresh_candidate_sources"
  ((fresh_source_rows += 1))
done <"$source_template"
[[ "$fresh_source_rows" -eq 272 ]] ||
  die "fresh Transactor source preparation has $fresh_source_rows rows, expected 272"
actual_structural_source_count=$(find "$transactor_source_root" "$core2_source_root" \
  -type f \( -name '*.clj' -o -name '*.cljc' \) | wc -l)
[[ "$actual_structural_source_count" -eq 272 ]] ||
  die "staged Transactor/core2 roots contain $actual_structural_source_count source files"

fresh_java_classes="$runtime_preparation_dir/java-classes.txt"
find "$transactor_java_root" -type f -name '*.class' -printf '%P\n' | sort >"$fresh_java_classes"
[[ "$(wc -l <"$fresh_java_classes")" -eq 52 ]] ||
  die "staged Transactor Java root does not contain exactly 52 classes"

fresh_ordered_dependencies="$runtime_preparation_dir/ordered-candidate-dependencies.tsv"
printf 'candidate_ordinal\tdistribution_ordinal\tjar\tartifact_path\tsha256\tsource_distribution_path\tsubstitution\n' \
  >"$fresh_ordered_dependencies"
for ((dependency_index = 0; dependency_index < peer_dependency_count; dependency_index += 1)); do
  path=${peer_resolved_dependencies[$dependency_index]}
  source_relative=${peer_dependency_relative_paths[$dependency_index]}
  original_sha=${peer_dependency_shas[$dependency_index]}
  substitution=none
  resolved_sha=$original_sha
  if [[ "$original_sha" == "$original_nano_sha" ]]; then
    substitution=sanitized-nano-impl
    resolved_sha=$expected_sanitized_nano_sha
  fi
  printf '%s\t%s\t%s\t%s\t%s\t%s\t%s\n' \
    "$((dependency_index + 1))" "$((dependency_index + 1))" "${path##*/}" "$path" \
    "$resolved_sha" "$source_relative" "$substitution" >>"$fresh_ordered_dependencies"
done
fresh_duplicates="$runtime_preparation_dir/dependency-class-duplicates.tsv"
cp -- "$duplicate_template" "$fresh_duplicates"
[[ "$(awk 'END {print NR - 1}' "$fresh_duplicates")" -eq 35 ]] ||
  die "dependency duplicate inventory is not exactly 35 rows"
[[ "$(awk -F '\t' 'NR > 1 && $4 == "DIFFER" {n++} END {print n + 0}' "$fresh_duplicates")" -eq 21 ]] ||
  die "dependency duplicate inventory no longer has 21 differing rows"

transactor_origin_probe="$runtime_preparation_dir/verify-structural-origins.clj"
cp -- "$transactor_origin_source" "$transactor_origin_probe"
transactor_runtime_regression_probe="$runtime_preparation_dir/validate-runtime-regressions.clj"
cp -- "$transactor_runtime_regression_source" "$transactor_runtime_regression_probe"

# Repository-owned Peer origin verifier for the unpacked, JKS-free derivative.
peer_origin_probe="$inputs_dir/verify-peer-runtime-origins.clj"
cat >"$peer_origin_probe" <<'CLOJURE'
(require '[clojure.java.io :as io]
         '[clojure.string :as str])
(defn fail! [message data] (throw (ex-info message data)))
(defn canonical [path] (.getCanonicalFile (io/file path)))
(defn urls [loader entry] (->> (.getResources loader entry) enumeration-seq (mapv str)))
(let [[root namespace-index resource-list java-class-list] *command-line-args*
      root-file (canonical root)
      root-url (str (.toURI root-file))
      loader (.getContextClassLoader (Thread/currentThread))
      classpath (mapv canonical (str/split (System/getProperty "java.class.path")
                                           (re-pattern java.io.File/pathSeparator)))
      source-paths (->> (line-seq (io/reader namespace-index)) rest
                        (map #(second (str/split % #"\t" -1))) vec)
      forbidden #{"datomic/transactor-key.jks" "datomic/transactor-trust.jks"
                  "nano_impl/transactor-key.jks" "nano_impl/transactor-trust.jks"}
      resource-paths (->> (line-seq (io/reader resource-list))
                          (remove str/blank?) (remove #(str/starts-with? % "#"))
                          (remove forbidden) vec)
      class-names (->> (line-seq (io/reader java-class-list)) rest
                       (map #(-> % (str/replace #"[.]class$" "")
                                 (str/replace "/" "."))) vec)
      expected-url (fn [entry] (str (.toURI (canonical (io/file root entry)))))]
  (when-not (= root-file (first classpath))
    (fail! "sanitized Peer root is not classpath-first" {:classpath classpath}))
  (when-not (= 142 (count source-paths))
    (fail! "Peer source index is not 142 rows" {:count (count source-paths)}))
  (doseq [entry source-paths]
    (when-not (= (expected-url entry) (first (urls loader entry)))
      (fail! "Peer source is not candidate-first" {:entry entry :urls (urls loader entry)}))
    (let [init-entry (str (subs entry 0 (- (count entry) 4)) "__init.class")]
      (when (seq (urls loader init-entry))
        (fail! "Peer AOT initializer is visible" {:entry init-entry :urls (urls loader init-entry)}))))
  (when-not (= 8 (count resource-paths))
    (fail! "sanitized Peer allowed-resource count is not 8" {:resources resource-paths}))
  (doseq [entry resource-paths]
    (when-not (= (expected-url entry) (first (urls loader entry)))
      (fail! "Peer resource is not candidate-first" {:entry entry :urls (urls loader entry)})))
  (doseq [entry forbidden]
    (when (seq (urls loader entry))
      (fail! "licensed key/trust material is visible" {:entry entry :urls (urls loader entry)})))
  (when-not (= 47 (count class-names))
    (fail! "Peer Java class index is not 47 rows" {:count (count class-names)}))
  (doseq [class-name class-names
          :let [c (Class/forName class-name false loader)
                actual (some-> c .getProtectionDomain .getCodeSource .getLocation str)]]
    (when-not (= root-url actual)
      (fail! "Peer Java class is not candidate-owned"
             {:class class-name :expected root-url :actual actual})))
  (println "RECOVERED_PEER_ORIGIN_PASS")
  (shutdown-agents))
CLOJURE

transactor_logback_config="$inputs_dir/transactor-logback.xml"
cat >"$transactor_logback_config" <<'XML'
<configuration>
  <appender name="STDERR" class="ch.qos.logback.core.ConsoleAppender">
    <target>System.err</target>
    <encoder><pattern>%date{ISO8601} %-5level %logger - %msg%n</pattern></encoder>
  </appender>
  <root level="INFO"><appender-ref ref="STDERR" /></root>
</configuration>
XML

# Copy immutable repository indexes used by the Peer origin proof.
peer_namespace_index="$inputs_dir/peer-namespaces.tsv"
peer_resource_list="$inputs_dir/peer-resources.txt"
peer_java_class_list="$inputs_dir/peer-java-classes.tsv"
cp -- "$project_dir/reports/source-index/namespaces.tsv" "$peer_namespace_index"
cp -- "$project_dir/reports/peer-resource-paths.txt" "$peer_resource_list"
cp -- "$project_dir/reports/source-index/unmapped-classes.tsv" "$peer_java_class_list"

write_absolute_directory_manifest() {
  local root=$1
  local output=$2
  find "$root" -type f -print0 | sort -z | xargs -0 -r sha256sum >"$output"
}

peer_runtime_manifest="$inputs_dir/peer-runtime-files.sha256"
peer_harness_manifest="$inputs_dir/peer-harness-files.sha256"
write_absolute_directory_manifest "$peer_runtime_root" "$peer_runtime_manifest"
write_absolute_directory_manifest "$peer_harness_root" "$peer_harness_manifest"

peer_entries=("$peer_runtime_root" "$peer_harness_root" "$peer_stub_root" "${peer_resolved_dependencies[@]}")
transactor_entries=("${transactor_stage_roots[@]}" "${transactor_dependency_paths[@]}")
[[ "${#peer_entries[@]}" -eq "$expected_peer_classpath_count" ]] || die "resolved Peer classpath cardinality changed"
[[ "${#transactor_entries[@]}" -eq "$expected_transactor_classpath_count" ]] || die "resolved Transactor classpath cardinality changed"
peer_classpath=$(IFS=:; echo "${peer_entries[*]}")
transactor_classpath=$(IFS=:; echo "${transactor_entries[*]}")

resolved_peer_manifest="$inputs_dir/resolved-peer-classpath.tsv"
{
  printf 'ordinal\trole\tsha256\tpath\n'
  printf '1\trecovered-peer-jks-free-runtime\t%s\t%s\n' "$(sha256_file "$peer_runtime_manifest")" "$peer_runtime_root"
  printf '2\trepository-peer-workload\t%s\t%s\n' "$(sha256_file "$peer_harness_manifest")" "$peer_harness_root"
  printf '3\tcompile-only-hotrod-stubs\t%s\t%s\n' "$(sha256_file "$stub_manifest")" "$peer_stub_root"
  for ((dependency_index = 0; dependency_index < peer_dependency_count; dependency_index += 1)); do
    path=${peer_resolved_dependencies[$dependency_index]}
    role=dependency
    sha=${peer_dependency_shas[$dependency_index]}
    if [[ "$sha" == "$original_nano_sha" ]]; then
      role=sanitized-nano-dependency
      sha=$expected_sanitized_nano_sha
    fi
    printf '%s\t%s\t%s\t%s\n' "$((dependency_index + 4))" "$role" "$sha" "$path"
  done
} >"$resolved_peer_manifest"

resolved_transactor_manifest="$inputs_dir/resolved-transactor-classpath.tsv"
{
  printf 'ordinal\trole\tsha256\tpath\n'
  for owned_index in 0 1 2 3 4; do
    path=${transactor_stage_roots[$owned_index]}
    source_manifest="$inputs_dir/transactor-$((owned_index + 1))-${transactor_discovery_roles[$owned_index]}-staged.sha256"
    printf '%s\t%s\t%s\t%s\n' "$((owned_index + 1))" \
      "${transactor_discovery_roles[$owned_index]}" "$(sha256_file "$source_manifest")" "$path"
  done
  for ((dependency_index = 0; dependency_index < peer_dependency_count; dependency_index += 1)); do
    path=${transactor_dependency_paths[$dependency_index]}
    sha=$(sha256_file "$path")
    printf '%s\tdependency:%s\t%s\t%s\n' "$((dependency_index + 6))" "${path##*/}" "$sha" "$path"
  done
} >"$resolved_transactor_manifest"

runtime_membership="$inputs_dir/candidate-runtime-membership.tsv"
write_runtime_membership() {
  local output=$1
  {
    printf 'role\ttype\trelative_path\n'
    for membership_spec in \
      "peer-runtime:$peer_runtime_root" \
      "peer-harness:$peer_harness_root" \
      "peer-stubs:$peer_stub_root" \
      "transactor-source:$transactor_source_root" \
      "transactor-core2:$core2_source_root" \
      "transactor-java:$transactor_java_root" \
      "transactor-resources:$transactor_resource_root" \
      "transactor-stubs:$transactor_stub_root"; do
      membership_role=${membership_spec%%:*}
      membership_root=${membership_spec#*:}
      find "$membership_root" -mindepth 1 \( -type f -o -type d \) \
        -printf "$membership_role\t%y\t%P\n"
    done | sort
  } >"$output"
}
write_runtime_membership "$runtime_membership"
runtime_membership_sha=$(sha256_file "$runtime_membership")

runtime_seal="$inputs_dir/candidate-runtime-inputs.sha256"
{
  find "$peer_runtime_root" "$peer_harness_root" "$peer_stub_root" \
    "$transactor_source_root" "$core2_source_root" "$transactor_java_root" \
    "$transactor_resource_root" "$transactor_stub_root" -type f -print
  printf '%s\n' "${peer_resolved_dependencies[@]}"
  printf '%s\n' "$java_bin" "$peer_origin_probe" "$transactor_origin_probe" \
    "$transactor_runtime_regression_probe" "$transactor_logback_config" \
    "$fresh_candidate_sources" "$fresh_java_classes" "$fresh_ordered_dependencies" \
    "$fresh_duplicates" "$peer_namespace_index" "$peer_resource_list" "$peer_java_class_list"
} | sort -u | while IFS= read -r runtime_input; do
  require_regular_file runtime-input "$runtime_input"
  sha256sum -- "$runtime_input"
done >"$runtime_seal"
runtime_seal_sha=$(sha256_file "$runtime_seal")

toolchain_seal="$inputs_dir/build-toolchain-inputs.sha256"
for tool_input in "$build_java_bin" "$peer_builder" "$built_peer_artifact" "$peer_artifact"; do
  sha256sum -- "$tool_input"
done >"$toolchain_seal"

forbidden_runtime_hash_pattern="$original_peer_sha|$original_transactor_sha|$original_core2_sha|$original_nano_sha|$licensed_key_sha|$licensed_trust_sha"
if awk -v pattern="^($forbidden_runtime_hash_pattern)$" '$1 ~ pattern {print; found=1} END {exit !found}' \
  "$runtime_seal" >"$results_dir/forbidden-runtime-hash-hits.txt"; then
  die "a licensed implementation/Nano/key/trust hash entered the candidate runtime seal"
fi

# Scan key-store-shaped archive entries as well as direct candidate files.
for archive in "${peer_resolved_dependencies[@]}"; do
  while IFS= read -r key_entry; do
    [[ -n "$key_entry" ]] || continue
    entry_sha=$(unzip -p "$archive" "$key_entry" | sha256sum | awk '{print $1}')
    [[ "$entry_sha" != "$licensed_key_sha" && "$entry_sha" != "$licensed_trust_sha" ]] ||
      die "licensed key/trust bytes are nested in runtime archive $archive:$key_entry"
  done < <(unzip -Z1 "$archive" | grep -E '\.(jks|p12|pfx|keystore)$' || true)
done

runtime_seal_verification_log="$logs_dir/runtime-seal-verifications.log"
verify_runtime_seal() {
  local phase=$1
  local current_membership="$runtime_dir/membership-$phase.tsv"
  [[ "$(sha256_file "$runtime_seal")" == "$runtime_seal_sha" ]] ||
    die "runtime seal itself changed during $phase"
  sha256sum -c "$runtime_seal" >>"$runtime_seal_verification_log" 2>&1 ||
    die "candidate runtime input changed during $phase"
  write_runtime_membership "$current_membership"
  [[ "$(sha256_file "$current_membership")" == "$runtime_membership_sha" ]] &&
    cmp -s "$runtime_membership" "$current_membership" ||
    die "candidate runtime directory membership changed during $phase"
  printf 'RUNTIME_SEAL_PASS\t%s\t%s\n' "$phase" "$runtime_seal_sha" \
    >>"$runtime_seal_verification_log"
}

run_candidate_probe() {
  local label=$1
  shift
  verify_runtime_seal "before-$label"
  if ! timeout --foreground --signal=TERM --kill-after=15s "${probe_timeout}s" "$@" \
    >"$logs_dir/$label.out" 2>"$logs_dir/$label.err"; then
    tail -n 100 "$logs_dir/$label.out" >&2 || true
    tail -n 100 "$logs_dir/$label.err" >&2 || true
    die "candidate probe failed: $label"
  fi
  verify_runtime_seal "after-$label"
}

run_candidate_probe peer-origin \
  "$java_bin" -XX:-UsePerfData -Djava.awt.headless=true -cp "$peer_classpath" \
  clojure.main "$peer_origin_probe" "$peer_runtime_root" "$peer_namespace_index" \
  "$peer_resource_list" "$peer_java_class_list"
grep -Fqx RECOVERED_PEER_ORIGIN_PASS "$logs_dir/peer-origin.out" ||
  die "Peer origin probe omitted its success marker"

run_candidate_probe transactor-origin \
  "$java_bin" -XX:-UsePerfData -Djava.awt.headless=true \
  -Dcom.amazonaws.sdk.disableEc2Metadata=true -cp "$transactor_classpath" \
  clojure.main "$transactor_origin_probe" "$transactor_source_root" "$core2_source_root" \
  "$transactor_java_root" "$transactor_resource_root" "$transactor_stub_root" \
  "$fresh_candidate_sources" "$fresh_java_classes" "$fresh_ordered_dependencies" \
  "$fresh_duplicates" "$datomic_home"
grep -Fqx STRUCTURAL_ORIGIN_PASS "$logs_dir/transactor-origin.out" ||
  die "Transactor origin probe omitted its success marker"

run_candidate_probe transactor-runtime-regressions \
  "$java_bin" -XX:-UsePerfData -Djava.awt.headless=true \
  -Dcom.amazonaws.sdk.disableEc2Metadata=true -cp "$transactor_classpath" \
  clojure.main "$transactor_runtime_regression_probe"
grep -Fqx 'transactor focused runtime regressions passed' \
  "$logs_dir/transactor-runtime-regressions.out" ||
  die "Transactor focused runtime probe omitted its success marker"

postgres_version=$("$pg_bin_dir/postgres" --version)
candidate_java_version=$("$java_bin" -version 2>&1 | tr '\n' ' ')
build_java_version=$("$build_java_bin" -version 2>&1 | tr '\n' ' ')
config_record="$work_root/config.properties"
{
  printf 'gate=recovered-peer-transactor-postgresql-vertical-slice\n'
  printf 'candidate.peer.canonical.path=%s\n' "$peer_artifact"
  printf 'candidate.peer.canonical.sha256=%s\n' "$peer_artifact_sha"
  printf 'candidate.peer.runtime.mode=unpacked-jks-free-derivative\n'
  printf 'candidate.peer.licensed.key.visible=false\n'
  printf 'candidate.peer.licensed.trust.visible=false\n'
  printf 'candidate.peer.classpath.entries=%s\n' "${#peer_entries[@]}"
  printf 'candidate.transactor.classpath.entries=%s\n' "${#transactor_entries[@]}"
  printf 'candidate.transactor.focused-runtime-regression.sha256=%s\n' \
    "$(sha256_file "$transactor_runtime_regression_probe")"
  printf 'candidate.runtime.seal.sha256=%s\n' "$runtime_seal_sha"
  printf 'candidate.runtime.membership.sha256=%s\n' "$runtime_membership_sha"
  printf 'candidate.original.peer=false\n'
  printf 'candidate.original.transactor=false\n'
  printf 'candidate.original.core2=false\n'
  printf 'candidate.original.nano=false\n'
  printf 'candidate.sanitized.nano.sha256=%s\n' "$expected_sanitized_nano_sha"
  printf 'candidate.java.path=%s\n' "$java_bin"
  printf 'candidate.java.version=%s\n' "$candidate_java_version"
  printf 'build.java.path=%s\n' "$build_java_bin"
  printf 'build.java.version=%s\n' "$build_java_version"
  printf 'build.java.runtime.role=peer-construction-only-never-candidate\n'
  printf 'postgres.version=%s\n' "$postgres_version"
  printf 'postgres.host=%s\n' "$pg_host"
  printf 'postgres.port=%s\n' "$pg_port"
  printf 'postgres.catalog=%s\n' "$catalog"
  printf 'postgres.user=%s\n' "$pg_user"
  printf 'postgres.password.sha256=%s\n' "$(sha256_text "$pg_password")"
  printf 'datomic.database.name=%s\n' "$database_name"
  printf 'transactor.port=%s\n' "$transactor_port"
  printf 'work.root=%s\n' "$work_root"
} >"$config_record"

write_evidence_hashes() {
  local output="$work_root/evidence.sha256"
  (
    cd -- "$work_root"
    find inputs logs results -type f -print
    for top_file in config.properties run-status.properties summary.properties; do
      [[ -f "$top_file" ]] && printf '%s\n' "$top_file"
    done
  ) | sort -u | while IFS= read -r evidence_file; do
    (cd -- "$work_root" && sha256sum -- "$evidence_file")
  done >"$output"
  (cd -- "$work_root" && sha256sum -c evidence.sha256) >/dev/null ||
    die "final evidence hash verification failed"
}

if [[ "$dry_run" == true ]]; then
  verify_runtime_seal dry-run-final
  {
    printf 'status=PASS\n'
    printf 'mode=dry-run\n'
    printf 'services.started=false\n'
    printf 'peer.current-repo-build=PASS\n'
    printf 'peer.jks-free-runtime=PASS\n'
    printf 'peer.origin=PASS\n'
    printf 'transactor.fresh-runtime-preparation=PASS\n'
    printf 'transactor.272-origin-proof=PASS\n'
    printf 'transactor.focused-runtime-regressions=PASS\n'
    printf 'recovery.common.compare-byte-arrays=PASS\n'
    printf 'candidate.runtime.seal=PASS\n'
  } >"$work_root/summary.properties"
  {
    printf 'status=dry-run\n'
    printf 'database.processes.started=false\n'
    printf 'candidate.boundary.validated=true\n'
  } >"$work_root/run-status.properties"
  write_evidence_hashes
  echo "Recovered Peer/Transactor PostgreSQL dry-run passed"
  echo "fresh current Peer, JKS-free runtime derivative, 272 Transactor origins, and both sealed classpaths passed"
  echo "no PostgreSQL or Transactor service was started"
  echo "evidence: $work_root"
  exit 0
fi

[[ "$confirmation" == "$confirmation_token" ]] ||
  die "executing run requires --confirm-disposable $confirmation_token"

current_step=initializing
pg_cleanup_armed=false
pg_pid=
pg_starttime=
transactor_pid=
transactor_starttime=
transactor_properties=
transactor_label=
transactor_expected_argv=()
run_succeeded=false

process_state() {
  local pid=$1
  awk '{print $3}' "/proc/$pid/stat" 2>/dev/null || true
}

process_starttime() {
  local pid=$1
  awk '{print $22}' "/proc/$pid/stat" 2>/dev/null || true
}

process_running() {
  local pid=$1
  [[ -n "$pid" && -d "/proc/$pid" && "$(process_state "$pid")" != Z ]]
}

verify_postgres_identity() {
  local pid=$1
  local expected_start=$2
  local exe
  local -a argv=()
  local found_data=false
  local index
  process_running "$pid" || return 1
  [[ "$(process_starttime "$pid")" == "$expected_start" ]] || return 1
  exe=$(readlink -f -- "/proc/$pid/exe" 2>/dev/null || true)
  [[ "$exe" == "$(readlink -f -- "$pg_bin_dir/postgres")" ]] || return 1
  mapfile -d '' -t argv <"/proc/$pid/cmdline"
  for ((index = 0; index + 1 < ${#argv[@]}; index += 1)); do
    if [[ "${argv[$index]}" == -D && "${argv[$((index + 1))]}" == "$pgdata" ]]; then
      found_data=true
    fi
  done
  [[ "$found_data" == true ]]
}

verify_transactor_identity() {
  local pid=$1
  local exe
  local -a actual_argv=()
  local index
  process_running "$pid" || return 1
  [[ "$(process_starttime "$pid")" == "$transactor_starttime" ]] || return 1
  exe=$(readlink -f -- "/proc/$pid/exe" 2>/dev/null || true)
  [[ "$exe" == "$java_bin" ]] || return 1
  mapfile -d '' -t actual_argv <"/proc/$pid/cmdline"
  [[ "${#actual_argv[@]}" -eq "${#transactor_expected_argv[@]}" ]] || return 1
  for ((index = 0; index < ${#actual_argv[@]}; index += 1)); do
    [[ "${actual_argv[$index]}" == "${transactor_expected_argv[$index]}" ]] || return 1
  done
}

stop_postgres() {
  [[ "$pg_cleanup_armed" == true ]] || return 0
  local candidate_pid=$pg_pid
  if [[ -z "$candidate_pid" && -f "$pgdata/postmaster.pid" ]]; then
    candidate_pid=$(sed -n '1p' "$pgdata/postmaster.pid")
  fi
  if [[ -n "$candidate_pid" ]] && process_running "$candidate_pid"; then
    local expected_start=$pg_starttime
    [[ -n "$expected_start" ]] || expected_start=$(process_starttime "$candidate_pid")
    verify_postgres_identity "$candidate_pid" "$expected_start" || {
      echo "refusing to stop unverified PostgreSQL PID $candidate_pid" >&2
      return 1
    }
    "$pg_bin_dir/pg_ctl" -D "$pgdata" -m fast -w stop \
      >>"$logs_dir/postgres-control.out" 2>>"$logs_dir/postgres-control.err" || return 1
  fi
  pg_cleanup_armed=false
  pg_pid=
  pg_starttime=
  wait_for_closed_port PostgreSQL "$pg_host" "$pg_port" || return 1
  "$pg_bin_dir/pg_controldata" "$pgdata" >"$results_dir/postgres-final-control.txt" 2>&1 || return 1
  grep -Eq 'Database cluster state:[[:space:]]+shut down' "$results_dir/postgres-final-control.txt" ||
    return 1
}

wait_for_process_exit() {
  local pid=$1
  local seconds=$2
  local index
  for ((index = 0; index < seconds; index += 1)); do
    process_running "$pid" || return 0
    sleep 1
  done
  ! process_running "$pid"
}

stop_transactor() {
  local require_graceful=${1:-false}
  [[ -n "$transactor_pid" ]] || return 0
  local owned_pid=$transactor_pid
  local graceful=false
  local term_used=false
  local kill_used=false
  local wait_status=0
  if process_running "$owned_pid"; then
    verify_transactor_identity "$owned_pid" || {
      echo "refusing to signal unverified Transactor PID $owned_pid" >&2
      return 1
    }
    kill -INT "$owned_pid"
    if wait_for_process_exit "$owned_pid" 20; then
      graceful=true
    else
      verify_transactor_identity "$owned_pid" || return 1
      kill -TERM "$owned_pid"
      term_used=true
      if ! wait_for_process_exit "$owned_pid" 10; then
        verify_transactor_identity "$owned_pid" || return 1
        kill -KILL "$owned_pid"
        kill_used=true
        wait_for_process_exit "$owned_pid" 5 || return 1
      fi
    fi
  fi
  if wait "$owned_pid"; then wait_status=0; else wait_status=$?; fi
  {
    printf 'pid=%s\n' "$owned_pid"
    printf 'sigint.graceful=%s\n' "$graceful"
    printf 'sigterm.used=%s\n' "$term_used"
    printf 'sigkill.used=%s\n' "$kill_used"
    printf 'wait.status=%s\n' "$wait_status"
  } >"$results_dir/transactor-$transactor_label-stop.properties"
  transactor_pid=
  transactor_starttime=
  transactor_properties=
  transactor_expected_argv=()
  wait_for_closed_port Transactor "$pg_host" "$transactor_port" || return 1
  verify_runtime_seal "after-transactor-$transactor_label-stop"
  if [[ "$require_graceful" == true && "$graceful" != true ]]; then
    echo "Transactor $transactor_label required TERM/KILL instead of bounded SIGINT shutdown" >&2
    return 1
  fi
}

cleanup() {
  local status=$?
  local cleanup_status=0
  trap - EXIT
  set +e
  stop_transactor false || cleanup_status=1
  stop_postgres || cleanup_status=1
  if ((status != 0 || cleanup_status != 0)); then
    {
      printf 'status=failed\n'
      printf 'last.step=%s\n' "$current_step"
      printf 'cleanup.status=%s\n' "$cleanup_status"
    } >"$work_root/run-status.properties"
    write_evidence_hashes || true
    echo "Recovered-pair PostgreSQL run failed during $current_step; evidence: $work_root" >&2
  fi
  ((status == 0 && cleanup_status == 0)) || exit 1
  exit 0
}
trap cleanup EXIT

write_transactor_properties() {
  local label=$1
  local properties="$runtime_dir/transactor-$label.properties"
  local data_dir="$runtime_dir/transactor-$label-data"
  local pid_file="$runtime_dir/transactor-$label.pid"
  mkdir -p -- "$data_dir"
  {
    printf 'protocol=sql\n'
    printf 'host=%s\n' "$pg_host"
    printf 'port=%s\n' "$transactor_port"
    printf 'sql-url=jdbc:postgresql://%s:%s/%s\n' "$pg_host" "$pg_port" "$catalog"
    printf 'sql-user=%s\n' "$pg_user"
    printf 'sql-password=%s\n' "$pg_password"
    printf 'sql-driver-class=org.postgresql.Driver\n'
    printf 'sql-validation-query=select 1\n'
    printf 'memory-index-threshold=32m\n'
    printf 'memory-index-max=256m\n'
    printf 'object-cache-max=128m\n'
    printf 'encrypt-channel=false\n'
    printf 'data-dir=%s\n' "$data_dir"
    printf 'pid-file=%s\n' "$pid_file"
  } >"$properties"
  chmod 600 "$properties"
  printf '%s' "$properties"
}

start_transactor() {
  local label=$1
  local stdout_log="$logs_dir/transactor-$label.out"
  local stderr_log="$logs_dir/transactor-$label.err"
  local internal_dir="$logs_dir/transactor-$label-internal"
  local pid_file
  local attempt
  [[ -z "$transactor_pid" ]] || die "attempted to overlap Transactor processes"
  port_is_open "$pg_host" "$transactor_port" &&
    die "Transactor port became occupied before $label"
  verify_runtime_seal "before-transactor-$label-start"
  transactor_label=$label
  transactor_properties=$(write_transactor_properties "$label")
  pid_file="$runtime_dir/transactor-$label.pid"
  mkdir -p -- "$internal_dir"
  transactor_expected_argv=(
    "$java_bin"
    -Xms128m
    -Xmx512m
    -XX:-UsePerfData
    -Djava.awt.headless=true
    -Duser.timezone=UTC
    -Dcom.amazonaws.sdk.disableEc2Metadata=true
    "-Dclojure.compiler.elide-meta=[:doc :file :line]"
    "-Dlogback.configurationFile=$transactor_logback_config"
    -cp "$transactor_classpath"
    clojure.main
    -m datomic.launcher
    "$transactor_properties"
  )
  {
    printf 'ordinal\targv\n'
    for ((attempt = 0; attempt < ${#transactor_expected_argv[@]}; attempt += 1)); do
      printf '%s\t%s\n' "$((attempt + 1))" "${transactor_expected_argv[$attempt]}"
    done
  } >"$results_dir/transactor-$label-argv.tsv"
  (
    cd -- "$internal_dir"
    exec env --default-signal=INT,QUIT,TERM "${transactor_expected_argv[@]}"
  ) >"$stdout_log" 2>"$stderr_log" &
  transactor_pid=$!
  transactor_starttime=$(process_starttime "$transactor_pid")
  [[ -n "$transactor_starttime" ]] || die "could not capture Transactor starttime: $label"
  for ((attempt = 0; attempt < startup_timeout; attempt += 1)); do
    if grep -Fq 'System started' "$stdout_log" &&
       port_is_open "$pg_host" "$transactor_port" &&
       verify_transactor_identity "$transactor_pid" &&
       [[ -f "$pid_file" ]]; then
      [[ "$(sed -n '1p' "$pid_file")" == "$transactor_pid" &&
         "$(stat -c '%s' -- "$pid_file")" -eq "${#transactor_pid}" ]] ||
        die "Transactor pid-file does not exactly name the owned PID: $label"
      printf 'label=%s\npid=%s\nstarttime=%s\nproperties=%s\n' \
        "$label" "$transactor_pid" "$transactor_starttime" "$transactor_properties" \
        >"$results_dir/transactor-$label-start.properties"
      return 0
    fi
    process_running "$transactor_pid" || {
      tail -n 100 "$stdout_log" >&2 || true
      tail -n 100 "$stderr_log" >&2 || true
      die "Transactor exited before readiness: $label"
    }
    sleep 1
  done
  tail -n 100 "$stdout_log" >&2 || true
  tail -n 100 "$stderr_log" >&2 || true
  die "Transactor did not become ready within $startup_timeout seconds: $label"
}

last_marker_file=
run_peer_workload() {
  local label=$1
  local mode=$2
  shift 2
  run_candidate_probe "$label" \
    "$java_bin" -XX:-UsePerfData -Djava.awt.headless=true -Duser.timezone=UTC \
    "-Dlogback.configurationFile=$peer_harness_root/logback-stage2.xml" \
    -cp "$peer_classpath" clojure.main -m stage2.peer-workload "$mode" "$@"
  local marker_file="$results_dir/$label.result"
  awk '/^STAGE2-PEER-RESULT / {print}' "$logs_dir/$label.out" >"$marker_file"
  [[ "$(wc -l <"$marker_file")" -eq 1 ]] ||
    die "Peer workload $label did not emit exactly one result marker"
  grep -Fq ":mode \"$mode\"" "$marker_file" || die "Peer workload mode mismatch: $label"
  last_marker_file=$marker_file
}

extract_marker_number() {
  local file=$1
  local key=$2
  local value
  value=$(sed -nE "s/.*:$key ([0-9]+).*/\\1/p" "$file")
  [[ "$value" =~ ^[0-9]+$ ]] || die "could not extract one numeric :$key from $file"
  printf '%s' "$value"
}

extract_marker_hash() {
  local file=$1
  local key=$2
  local value
  value=$(sed -nE "s/.*:$key \"([0-9a-f]{64})\".*/\\1/p" "$file")
  [[ "$value" =~ ^[0-9a-f]{64}$ ]] || die "could not extract one :$key hash from $file"
  printf '%s' "$value"
}

extract_marker_string() {
  local file=$1
  local key=$2
  local value
  value=$(sed -nE "s/.*:$key \"([^\"]+)\".*/\\1/p" "$file")
  [[ -n "$value" && "$value" != *$'\n'* ]] || die "could not extract one :$key string from $file"
  printf '%s' "$value"
}

write_marker_fingerprint() {
  local marker=$1
  local output=$2
  grep -Fq ':as-of-t nil' "$marker" || die "snapshot marker is not current: $marker"
  {
    printf 'database-id=%s\n' "$(extract_marker_string "$marker" database-id)"
    printf 'basis-t=%s\n' "$(extract_marker_number "$marker" basis-t)"
    printf 'row-count=%s\n' "$(extract_marker_number "$marker" row-count)"
    for hash_key in datoms-sha256 history-sha256 logical-sha256 rows-sha256; do
      printf '%s=%s\n' "$hash_key" "$(extract_marker_hash "$marker" "$hash_key")"
    done
    printf 'as-of-t=nil\n'
  } >"$output"
}

last_sql_rows=
last_sql_bytes=
last_sql_revisioned=
capture_sql_metrics() {
  local label=$1
  local output="$results_dir/sql-$label.tsv"
  local values
  values=$("$pg_bin_dir/psql" -X -A -t -F $'\t' -v ON_ERROR_STOP=1 \
    -h "$pg_socket_dir" -p "$pg_port" -U "$pg_superuser" -d "$catalog" \
    -c 'SELECT count(*), COALESCE(sum(octet_length(val)), 0), count(*) FILTER (WHERE rev IS NOT NULL) FROM public.datomic_kvs;' \
    2>"$logs_dir/sql-$label.err")
  IFS=$'\t' read -r last_sql_rows last_sql_bytes last_sql_revisioned <<<"$values"
  [[ "$last_sql_rows" =~ ^[0-9]+$ && "$last_sql_bytes" =~ ^[0-9]+$ &&
     "$last_sql_revisioned" =~ ^[0-9]+$ ]] || die "invalid SQL metrics during $label: $values"
  printf 'rows\tval-bytes\trevisioned-rows\n%s\t%s\t%s\n' \
    "$last_sql_rows" "$last_sql_bytes" "$last_sql_revisioned" >"$output"
}

require_log_catchup() {
  local label=$1
  local expected_tail=$2
  local minimum_index_t=${3:-0}
  local require_replay_bytes=${4:-true}
  local stdout_log="$logs_dir/transactor-$label.out"
  local stderr_log="$logs_dir/transactor-$label.err"
  local record=
  local attempt
  for ((attempt = 0; attempt < 30; attempt += 1)); do
    record=$(sed -nE 's/.*:event :log\/catchup, :bytes ([0-9]+), :tail-t ([0-9]+), :index-t ([0-9]+).*/\1\t\2\t\3/p' \
      "$stdout_log" "$stderr_log" | tail -n 1)
    [[ -n "$record" ]] && break
    sleep 1
  done
  [[ -n "$record" ]] || die "restart $label emitted no log/catchup evidence"
  local bytes tail_t index_t
  IFS=$'\t' read -r bytes tail_t index_t <<<"$record"
  case "$require_replay_bytes" in
    true) ((bytes > 0)) || die "restart $label catchup did not replay durable log bytes" ;;
    false) ;;
    *) die "invalid replay-byte requirement for restart $label: $require_replay_bytes" ;;
  esac
  [[ "$tail_t" == "$expected_tail" ]] ||
    die "restart $label catchup tail $tail_t differs from expected $expected_tail"
  ((index_t >= minimum_index_t)) ||
    die "restart $label adopted index-t $index_t below required $minimum_index_t"
  ((index_t <= tail_t)) || die "restart $label index-t exceeds tail-t"
  printf 'bytes\ttail-t\tindex-t\n%s\t%s\t%s\n' "$bytes" "$tail_t" "$index_t" \
    >"$results_dir/transactor-$label-log-catchup.tsv"
}

sql_uri=$(printf 'datomic:sql://%s?jdbc:postgresql://%s:%s/%s?user=%s&password=%s' \
  "$database_name" "$pg_host" "$pg_port" "$catalog" "$pg_user" "$pg_password")

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
pg_cleanup_armed=true
"$pg_bin_dir/pg_ctl" -D "$pgdata" -w -l "$logs_dir/postgres-server.log" \
  -o "$postgres_options" start >"$logs_dir/postgres-control.out" \
  2>"$logs_dir/postgres-control.err" || die "PostgreSQL failed to start"
pg_pid=$(sed -n '1p' "$pgdata/postmaster.pid")
[[ "$pg_pid" =~ ^[0-9]+$ ]] || die "PostgreSQL postmaster.pid is invalid"
pg_starttime=$(process_starttime "$pg_pid")
[[ -n "$pg_starttime" ]] && verify_postgres_identity "$pg_pid" "$pg_starttime" ||
  die "PostgreSQL process ownership verification failed"
port_is_open "$pg_host" "$pg_port" || die "PostgreSQL port did not open"

current_step=postgres-provision
"$pg_bin_dir/psql" -X -v ON_ERROR_STOP=1 \
  -h "$pg_socket_dir" -p "$pg_port" -U "$pg_superuser" -d postgres \
  --set=gate_role="$pg_user" --set=gate_password="$pg_password" \
  >"$logs_dir/postgres-role.out" 2>"$logs_dir/postgres-role.err" <<'SQL'
CREATE ROLE :"gate_role" LOGIN PASSWORD :'gate_password';
SQL
"$pg_bin_dir/createdb" -h "$pg_socket_dir" -p "$pg_port" -U "$pg_superuser" \
  --owner="$pg_user" "$catalog" >"$logs_dir/postgres-createdb.out" \
  2>"$logs_dir/postgres-createdb.err"
"$pg_bin_dir/psql" -X -v ON_ERROR_STOP=1 \
  -h "$pg_socket_dir" -p "$pg_port" -U "$pg_superuser" -d "$catalog" \
  --set=gate_owner="$pg_user" >"$logs_dir/postgres-schema.out" \
  2>"$logs_dir/postgres-schema.err" <<'SQL'
CREATE TABLE public.datomic_kvs (
  id text PRIMARY KEY,
  rev integer,
  map text,
  val bytea
);
ALTER TABLE public.datomic_kvs OWNER TO :"gate_owner";
SQL
capture_sql_metrics initial
[[ "$last_sql_rows" -eq 0 && "$last_sql_bytes" -eq 0 && "$last_sql_revisioned" -eq 0 ]] ||
  die "fresh datomic_kvs table is unexpectedly nonempty"

# A: seed and durable SQL state.
current_step=transactor-boot-1
start_transactor boot-1
current_step=peer-seed
run_peer_workload peer-seed seed "$sql_uri" true
seed_marker=$last_marker_file
seed_fingerprint="$results_dir/peer-seed.fingerprint"
write_marker_fingerprint "$seed_marker" "$seed_fingerprint"
seed_basis=$(extract_marker_number "$seed_marker" basis-t)
seed_rows=$(extract_marker_number "$seed_marker" row-count)
database_id=$(extract_marker_string "$seed_marker" database-id)
((seed_basis > 0 && seed_rows > 0)) || die "seed marker did not advance to a nonempty database"
[[ "$database_id" =~ ^[A-Za-z0-9._-]+$ ]] || die "seed database-id has an unsafe shape"
capture_sql_metrics after-seed
seed_sql_rows=$last_sql_rows
seed_sql_bytes=$last_sql_bytes
seed_sql_revisioned=$last_sql_revisioned
((seed_sql_rows > 0 && seed_sql_bytes > 0 && seed_sql_revisioned > 0)) ||
  die "seed produced no durable PostgreSQL KV state"
current_step=transactor-stop-1
stop_transactor true || die "boot-1 did not stop gracefully"

# B: restart, replay/adopt durable log/index state, and prove equal snapshot.
current_step=transactor-boot-2
start_transactor boot-2
current_step=peer-snapshot-after-seed
run_peer_workload peer-snapshot-after-seed snapshot "$sql_uri"
seed_snapshot_marker=$last_marker_file
seed_snapshot_fingerprint="$results_dir/peer-snapshot-after-seed.fingerprint"
write_marker_fingerprint "$seed_snapshot_marker" "$seed_snapshot_fingerprint"
cmp -s "$seed_fingerprint" "$seed_snapshot_fingerprint" ||
  die "post-restart Peer snapshot differs from the seed"
require_log_catchup boot-2 "$seed_basis"
capture_sql_metrics after-seed-restart
((last_sql_rows >= seed_sql_rows && last_sql_bytes >= seed_sql_bytes &&
  last_sql_revisioned >= seed_sql_revisioned)) ||
  die "durable SQL state regressed across the first Transactor restart"

# C: submit a second transaction after adoption and prove a new durable basis.
current_step=peer-augment
run_peer_workload peer-augment augment "$sql_uri"
augment_marker=$last_marker_file
augment_fingerprint="$results_dir/peer-augment.fingerprint"
write_marker_fingerprint "$augment_marker" "$augment_fingerprint"
augment_basis=$(extract_marker_number "$augment_marker" basis-t)
augment_rows=$(extract_marker_number "$augment_marker" row-count)
augment_database_id=$(extract_marker_string "$augment_marker" database-id)
((augment_basis > seed_basis && augment_rows > seed_rows)) ||
  die "augment did not advance basis and logical row count"
[[ "$augment_database_id" == "$database_id" ]] ||
  die "augment changed Datomic database identity"
cmp -s "$seed_fingerprint" "$augment_fingerprint" &&
  die "augment unexpectedly reproduced the seed fingerprint"
capture_sql_metrics after-augment
augment_sql_rows=$last_sql_rows
augment_sql_bytes=$last_sql_bytes
augment_sql_revisioned=$last_sql_revisioned
((augment_sql_rows >= seed_sql_rows && augment_sql_bytes > seed_sql_bytes &&
  augment_sql_revisioned >= seed_sql_revisioned)) ||
  die "augment produced no additional durable PostgreSQL value bytes"

# Force the memory index through the real persistent-index publication path.
# The production-sized 32m threshold deliberately does not fire for this
# small deterministic workload, so an explicit request is the stable gate.
current_step=peer-request-index
run_peer_workload peer-request-index request-index "$sql_uri"
index_marker=$last_marker_file
requested_index_t=$(extract_marker_number "$index_marker" requested-t)
reported_indexed_t=$(extract_marker_number "$index_marker" indexed-t)
indexed_database_id=$(extract_marker_string "$index_marker" database-id)
[[ "$requested_index_t" == "$augment_basis" ]] ||
  die "index request basis $requested_index_t differs from augment basis $augment_basis"
((reported_indexed_t >= requested_index_t)) ||
  die "Peer reported index-t $reported_indexed_t below requested $requested_index_t"
[[ "$indexed_database_id" == "$database_id" ]] ||
  die "index request changed Datomic database identity"
grep -Fq ':request-accepted? true' "$index_marker" ||
  die "Peer did not confirm acceptance of the explicit index request"
capture_sql_metrics after-index
index_sql_rows=$last_sql_rows
index_sql_bytes=$last_sql_bytes
index_sql_revisioned=$last_sql_revisioned
((index_sql_rows > augment_sql_rows && index_sql_bytes > augment_sql_bytes &&
  index_sql_revisioned >= augment_sql_revisioned)) ||
  die "persistent index publication did not add PostgreSQL rows and value bytes"
current_step=transactor-stop-2
stop_transactor true || die "boot-2 did not stop gracefully"

# Final restart and equal snapshot closes the durable commit/adoption loop.
current_step=transactor-boot-3
start_transactor boot-3
current_step=peer-snapshot-after-augment
run_peer_workload peer-snapshot-after-augment snapshot "$sql_uri"
augment_snapshot_marker=$last_marker_file
augment_snapshot_fingerprint="$results_dir/peer-snapshot-after-augment.fingerprint"
write_marker_fingerprint "$augment_snapshot_marker" "$augment_snapshot_fingerprint"
cmp -s "$augment_fingerprint" "$augment_snapshot_fingerprint" ||
  die "post-restart Peer snapshot differs from augment"
# A fully current persistent index can legitimately require zero log replay;
# the new process must nevertheless load that durable root at the requested t.
require_log_catchup boot-3 "$augment_basis" "$requested_index_t" false
capture_sql_metrics after-augment-restart
[[ "$last_sql_rows" == "$index_sql_rows" &&
   "$last_sql_bytes" == "$index_sql_bytes" &&
   "$last_sql_revisioned" == "$index_sql_revisioned" ]] ||
  die "published PostgreSQL index state changed during fresh-process adoption"
current_step=transactor-stop-3
stop_transactor true || die "boot-3 did not stop gracefully"

current_step=postgres-stop
stop_postgres || die "PostgreSQL did not stop cleanly"
verify_runtime_seal final-after-all-writers-stopped
port_is_open "$pg_host" "$pg_port" && die "PostgreSQL port remains occupied after final stop"
port_is_open "$pg_host" "$transactor_port" && die "Transactor port remains occupied after final stop"

{
  printf 'status=PASS\n'
  printf 'mode=executing\n'
  printf 'peer.current-repo-build=PASS\n'
  printf 'peer.jks-free-runtime=PASS\n'
  printf 'peer.origin=PASS\n'
  printf 'transactor.fresh-runtime-preparation=PASS\n'
  printf 'transactor.272-origin-proof=PASS\n'
  printf 'transactor.focused-runtime-regressions=PASS\n'
  printf 'candidate.runtime.seal=PASS\n'
  printf 'postgresql.fresh-catalog=PASS\n'
  printf 'seed.basis-t=%s\n' "$seed_basis"
  printf 'augment.basis-t=%s\n' "$augment_basis"
  printf 'requested.index-t=%s\n' "$requested_index_t"
  printf 'reported.index-t=%s\n' "$reported_indexed_t"
  printf 'database.id=%s\n' "$database_id"
  printf 'seed.restart.snapshot-equality=PASS\n'
  printf 'augment.restart.snapshot-equality=PASS\n'
  printf 'persistent-index.publication=PASS\n'
  printf 'persistent-index.fresh-process-adoption=PASS\n'
  printf 'restart.log-index-adoption=PASS\n'
  printf 'transactor.graceful-stop.count=3\n'
  printf 'services.finally-stopped=PASS\n'
  printf 'recovery.common.compare-byte-arrays=PASS\n'
  printf 'ha.fencing=NOT_RUN\n'
} >"$work_root/summary.properties"
{
  printf 'status=passed\n'
  printf 'last.step=complete\n'
  printf 'services.running=false\n'
} >"$work_root/run-status.properties"
write_evidence_hashes
run_succeeded=true
echo "Recovered Peer + recovered Transactor PostgreSQL vertical slice passed"
echo "seed $seed_basis -> restart -> augment $augment_basis -> request-index -> restart/adopt"
echo "all three Transactors stopped by bounded SIGINT; PostgreSQL is shut down"
echo "evidence: $work_root"
