#!/usr/bin/env bash
set -euo pipefail

export LC_ALL=C

script_dir=$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd)
transactor_dir=$(cd -- "$script_dir/.." && pwd)
project_dir=$(cd -- "$transactor_dir/.." && pwd)
datomic_home=${1:-"$project_dir/../../datomic/datomic-pro-1.0.7277"}
output_dir=${2:-/tmp/datomic-transactor-entry-closure}
transactor_jar="$datomic_home/datomic-transactor-pro-1.0.7277.jar"
peer_jar="$datomic_home/peer-1.0.7277.jar"
accounting_tool="$transactor_dir/tools/MapTransactorEntries.java"

for command_name in find java realpath sha256sum; do
  command -v "$command_name" >/dev/null || {
    echo "missing required command: $command_name" >&2
    exit 1
  }
done

for required_path in "$transactor_jar" "$peer_jar" "$accounting_tool"; do
  [[ -f "$required_path" ]] || {
    echo "missing entry-accounting input: $required_path" >&2
    exit 1
  }
done

project_path=$(realpath -m "$project_dir")
output_path=$(realpath -m "$output_dir")
if [[ "$output_path" == "$project_path" || \
      "$output_path" == "$project_path/"* ]]; then
  echo "licensed evidence output must remain outside the repository: $output_path" >&2
  exit 1
fi

if [[ -e "$output_path" ]] && \
   [[ -n "$(find "$output_path" -mindepth 1 -print -quit 2>/dev/null)" ]]; then
  echo "refusing to overwrite non-empty output directory: $output_path" >&2
  exit 1
fi
[[ ! -L "$output_path" ]] || {
  echo "output directory may not be a symbolic link: $output_path" >&2
  exit 1
}

(
  cd "$transactor_dir/baseline"
  sha256sum -c manifest.sha256
)

java -XX:+PerfDisableSharedMem "$accounting_tool" \
  "$transactor_dir/baseline" "$transactor_jar" "$peer_jar" "$output_path"

(
  cd "$output_path"
  sha256sum -c manifest.sha256
)

echo "complete Transactor entry accounting passed"
echo "licensed resource evidence remains isolated at: $output_path/licensed-evidence"
echo "details: $output_path/summary.tsv"
