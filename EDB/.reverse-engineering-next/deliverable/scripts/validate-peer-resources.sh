#!/usr/bin/env bash

set -euo pipefail

script_dir=$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd)
project_dir=$(cd -- "$script_dir/.." && pwd)
datomic_home=${1:-"$project_dir/../../datomic/datomic-pro-1.0.7277"}
resource_root=${2:-"$project_dir/resources"}
peer_jar="$datomic_home/peer-1.0.7277.jar"
expected_sha=cb55c9d01e155f9965e3332cd8ef70be42cdefacb50c072076e9ecf22b9a23ba

for command_name in cmp mktemp sha256sum unzip zipinfo; do
  command -v "$command_name" >/dev/null || {
    echo "missing required command: $command_name" >&2
    exit 1
  }
done

[[ -f "$peer_jar" && -d "$resource_root" ]] || {
  echo "missing peer JAR or recovered resource root" >&2
  exit 1
}

actual_sha=$(sha256sum "$peer_jar" | awk '{print $1}')
[[ "$actual_sha" == "$expected_sha" ]] || {
  echo "unexpected peer JAR SHA-256: $actual_sha" >&2
  exit 1
}

scratch_file=$(mktemp -t datomic-peer-resource.XXXXXXXX)
cleanup() {
  rm -f -- "$scratch_file"
}
trap cleanup EXIT

resource_count=0
while IFS= read -r entry; do
  recovered_file="$resource_root/$entry"
  [[ -f "$recovered_file" ]] || {
    echo "missing recovered peer resource: $entry" >&2
    exit 1
  }

  unzip -p "$peer_jar" "$entry" >"$scratch_file"
  cmp -s "$scratch_file" "$recovered_file" || {
    echo "recovered peer resource differs from JAR: $entry" >&2
    exit 1
  }
  resource_count=$((resource_count + 1))
done < <(zipinfo -1 "$peer_jar" | awk '!/\/$/ && !/\.class$/')

[[ "$resource_count" -eq 10 ]] || {
  echo "expected 10 peer resources, found $resource_count" >&2
  exit 1
}

echo "validated all $resource_count peer resources at exact classpath paths"
