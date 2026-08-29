#!/usr/bin/bash -p

set -euo pipefail
umask 077
unset BASH_ENV ENV CDPATH GLOBIGNORE JAVA_TOOL_OPTIONS JDK_JAVA_OPTIONS \
  _JAVA_OPTIONS JDK_JAVAC_OPTIONS CLASSPATH JAVA_HOME LD_PRELOAD \
  LD_LIBRARY_PATH
IFS=$' \t\n'
export LC_ALL=C TZ=UTC

script_dir=$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd)
project_dir=$(cd -- "$script_dir/../.." && pwd)
datomic_home=${1:-"$project_dir/../../datomic/datomic-pro-1.0.7277"}
evidence_root=${2:-/tmp/datomic-stage2-trivial-overlaps-v2}
java_root=${DATOMIC_RECOVERY_JAVA_ROOT:-/usr/lib/jvm/java-21-openjdk-amd64}
java_bin="$java_root/bin/java"
probe="$script_dir/stage2/trivial_overlap_probe.clj"

fail() {
  echo "Stage 2 trivial-overlap gate: $*" >&2
  exit 1
}

[[ -x "$java_bin" ]] || fail "Java executable is missing: $java_bin"
[[ -f "$probe" && ! -L "$probe" ]] || fail "probe is missing or symbolic"
[[ ! -L "$evidence_root" ]] || fail "evidence root may not be a symlink"
if [[ -e "$evidence_root" ]] &&
   [[ -n $(find "$evidence_root" -mindepth 1 -print -quit 2>/dev/null) ]]; then
  fail "refusing to overwrite non-empty evidence root: $evidence_root"
fi

peer_jar="$datomic_home/peer-1.0.7277.jar"
transactor_jar="$datomic_home/datomic-transactor-pro-1.0.7277.jar"
clojure_jar="$datomic_home/lib/clojure-1.11.4.jar"
spec_jar="$datomic_home/lib/spec.alpha-0.3.218.jar"
core_specs_jar="$datomic_home/lib/core.specs.alpha-0.2.62.jar"

declare -A expected_sha=(
  ["$peer_jar"]="cb55c9d01e155f9965e3332cd8ef70be42cdefacb50c072076e9ecf22b9a23ba"
  ["$transactor_jar"]="d90819b57e2138085ddc26c93314f953c2cd776d1d1aa8b43a6bec1e475d3692"
  ["$clojure_jar"]="fc7ff1b6610d0e3494a75cc11ef81810bda402ac157012fe4e3b899586cf4133"
  ["$spec_jar"]="67ec898eb55c66a957a55279dd85d1376bb994bd87668b2b0de1eb3b97e8aae0"
  ["$core_specs_jar"]="06eea8c070bbe45c158567e443439681bc8c46e9123414f81bfa32ba42d6cbc8"
)

mkdir -p "$evidence_root/inputs" "$evidence_root/lanes"
for input in "$peer_jar" "$transactor_jar" "$clojure_jar" \
             "$spec_jar" "$core_specs_jar"; do
  [[ -f "$input" && ! -L "$input" ]] || fail "input is missing or symbolic: $input"
  actual=$(sha256sum "$input" | awk '{print $1}')
  [[ "$actual" == "${expected_sha[$input]}" ]] ||
    fail "input hash mismatch: $input"
  printf '%s\t%s\n' "$actual" "$input" >>"$evidence_root/inputs/licensed-and-runtime.sha256"
done

selected_sources=(
  datomic/cache/impl.clj
  datomic/kv_store.clj
  datomic/simple_kv.clj
  datomic/valcache/puts_pool.clj
)
printf 'lane\tpath\tsha256\n' >"$evidence_root/inputs/source-files.tsv"
for relative in "${selected_sources[@]}"; do
  for lane in peer transactor; do
    if [[ "$lane" == peer ]]; then
      source_file="$project_dir/src-clj/$relative"
    else
      source_file="$project_dir/transactor/src-clj/$relative"
    fi
    [[ -f "$source_file" && ! -L "$source_file" ]] ||
      fail "source input is missing or symbolic: $source_file"
    printf '%s\t%s\t%s\n' "$lane" "$relative" \
      "$(sha256sum "$source_file" | awk '{print $1}')" \
      >>"$evidence_root/inputs/source-files.tsv"
  done
done
sha256sum "$probe" "$java_bin" >"$evidence_root/inputs/tool-files.sha256"

runtime_jars="$clojure_jar:$spec_jar:$core_specs_jar"

run_lane() {
  local lane=$1
  local classpath=$2
  local kind=$3
  local lane_root="$evidence_root/lanes/$lane"
  mkdir -p "$lane_root/home" "$lane_root/tmp" "$lane_root/cwd"
  case "$kind" in
    original)
      [[ "$classpath" == "$peer_jar:"* ||
         "$classpath" == "$transactor_jar:"* ]] ||
        fail "original lane lacks its owned artifact: $lane"
      ;;
    recovered)
      [[ "$classpath" != *"peer-1.0.7277.jar"* &&
         "$classpath" != *"datomic-transactor-pro-1.0.7277.jar"* &&
         "$classpath" != *"core2-"* ]] ||
        fail "recovered lane contains a licensed implementation artifact: $lane"
      ;;
    *) fail "unknown lane kind: $kind" ;;
  esac
  /usr/bin/timeout --signal=TERM --kill-after=5s 30s \
    /usr/bin/env -i \
    PATH="$java_root/bin:/usr/bin:/bin" HOME="$lane_root/home" \
    TMPDIR="$lane_root/tmp" LC_ALL=C TZ=UTC \
    "$java_bin" -XX:+PerfDisableSharedMem \
    -Djava.io.tmpdir="$lane_root/tmp" -cp "$classpath" \
    clojure.main "$probe" \
    >"$lane_root/stdout" 2>"$lane_root/stderr"
  [[ ! -s "$lane_root/stderr" ]] || fail "$lane wrote stderr"
  [[ $(grep -Fc 'STAGE2_TRIVIAL_OVERLAP_RESULT ' "$lane_root/stdout") == 1 ]] ||
    fail "$lane did not emit exactly one result"
  [[ -z $(find "$lane_root/home" "$lane_root/tmp" "$lane_root/cwd" \
                 -mindepth 1 -print -quit) ]] ||
    fail "$lane left files in an isolated writable root"
  printf '%s\t%s\t%s\n' "$lane" "$kind" \
    "$(sha256sum "$lane_root/stdout" | awk '{print $1}')" \
    >>"$evidence_root/lanes.tsv"
}

printf 'lane\tkind\tstdout_sha256\n' >"$evidence_root/lanes.tsv"
run_lane original-peer "$peer_jar:$runtime_jars" original
run_lane recovered-peer "$project_dir/src-clj:$runtime_jars" recovered
run_lane original-transactor "$transactor_jar:$runtime_jars" original
run_lane recovered-transactor \
  "$project_dir/transactor/src-clj:$runtime_jars" recovered

while IFS=$'\t' read -r lane relative expected; do
  [[ "$lane" == lane ]] && continue
  if [[ "$lane" == peer ]]; then
    source_file="$project_dir/src-clj/$relative"
  else
    source_file="$project_dir/transactor/src-clj/$relative"
  fi
  [[ $(sha256sum "$source_file" | awk '{print $1}') == "$expected" ]] ||
    fail "source input changed during the gate: $lane/$relative"
done <"$evidence_root/inputs/source-files.tsv"
for input in "$peer_jar" "$transactor_jar" "$clojure_jar" \
             "$spec_jar" "$core_specs_jar"; do
  [[ $(sha256sum "$input" | awk '{print $1}') == "${expected_sha[$input]}" ]] ||
    fail "runtime input changed during the gate: $input"
done

reference="$evidence_root/lanes/original-peer/stdout"
for lane in recovered-peer original-transactor recovered-transactor; do
  cmp -s "$reference" "$evidence_root/lanes/$lane/stdout" ||
    fail "behavior differs between original Peer and $lane"
done

result_sha=$(sha256sum "$reference" | awk '{print $1}')
cat >"$evidence_root/summary.tsv" <<EOF
claim\tstatus\tdetail
four_lane_behavior\tPASS\toriginal Peer, recovered Peer, original Transactor, and recovered Transactor emitted byte-identical results
candidate_isolation\tPASS\trecovered lanes contain no Peer, Transactor, or core2 implementation archive
cache_impl\tPASS\tConcurrentMap put/remove/clear and fast-count
kv_store\tPASS\tdynamic Var binding and retryable Throwable/InterruptedException dispatch
valcache_puts_pool\tPASS\tMultiFn default/custom dispatch, queued lookup, and protocol submit
simple_kv\tPASS\tpack/unpack bytes and metadata, raw boundaries, immediate get-with-retry, put/delete
result_sha256\tPASS\t$result_sha
EOF

(
  cd "$evidence_root"
  find . -type f ! -name evidence-manifest.sha256 -print0 |
    sort -z | xargs -0 sha256sum
) >"$evidence_root/evidence-manifest.sha256"
manifest_sha=$(sha256sum "$evidence_root/evidence-manifest.sha256" | awk '{print $1}')
echo "STAGE2_TRIVIAL_OVERLAP_PASS result_sha256=$result_sha manifest_sha256=$manifest_sha evidence_root=$evidence_root"
