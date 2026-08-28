#!/usr/bin/env bash
set -euo pipefail

usage() {
  echo "usage: $0 DATOMIC_HOME [WORK_ROOT]" >&2
  exit 2
}

[[ $# -ge 1 && $# -le 2 ]] || usage

datomic_home=$1
work_root=${2:-}
script_dir=$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd -P)
fixture_root="$script_dir/fixtures/analyze-corpus"

[[ -d "$datomic_home/lib" ]] || {
  echo "missing Datomic lib directory: $datomic_home/lib" >&2
  exit 2
}
[[ -d "$fixture_root" ]] || {
  echo "missing analyzer fixtures: $fixture_root" >&2
  exit 2
}
command -v java >/dev/null || {
  echo "java is required" >&2
  exit 2
}
command -v zip >/dev/null || {
  echo "zip is required" >&2
  exit 2
}

if [[ -z "$work_root" ]]; then
  work_root=$(mktemp -d "${TMPDIR:-/tmp}/datomic-analyze-corpus-regression.XXXXXXXX")
else
  mkdir -p -- "$work_root"
  work_root=$(cd -- "$work_root" && pwd -P)
fi

source_root="$work_root/source"
empty_jar_root="$work_root/empty-jar"
peer_jar="$work_root/empty-peer.jar"
output_root="$work_root/output"
mkdir -p -- "$source_root" "$empty_jar_root"
cp -- "$fixture_root/ordinary.clj" "$fixture_root/decompiled.clj" "$source_root/"
touch "$empty_jar_root/fixture-marker"
(cd -- "$empty_jar_root" && zip -q "$peer_jar" fixture-marker)

java -XX:+PerfDisableSharedMem -cp "$datomic_home/lib/*" \
  clojure.main "$script_dir/analyze_corpus.clj" \
  "$source_root" "$peer_jar" "$output_root"

namespace_index="$output_root/index/namespaces.tsv"
var_index="$output_root/index/vars.tsv"

[[ $(awk -F '\t' 'NR > 1 {count++} END {print count + 0}' "$namespace_index") -eq 2 ]]
grep -Fx $'fixture.decompiled\tdecompiled.clj\t4\t1\t1\t0\t\t' "$namespace_index" >/dev/null
grep -F $'fixture.ordinary\tordinary.clj\t13\t2\t1\t1\tclojure.string\t' "$namespace_index" >/dev/null
! grep -F 'fixture.wrong' "$namespace_index" >/dev/null

grep -F $'fixture.decompiled\trecovered\tdefn\tpublic\t' "$var_index" >/dev/null
grep -F $'fixture.ordinary\tvisible\tdefn\tpublic\t' "$var_index" >/dev/null
grep -F $'fixture.ordinary\thidden\tdefn-\tprivate\t' "$var_index" >/dev/null
! grep -F $'\tphantom\t' "$var_index" >/dev/null

echo "analyze-corpus multi-form regression passed: $work_root"
