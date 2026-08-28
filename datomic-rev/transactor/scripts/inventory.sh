#!/usr/bin/env bash
set -euo pipefail

export LC_ALL=C

script_dir=$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd)
transactor_dir=$(cd -- "$script_dir/.." && pwd)
project_dir=$(cd -- "$transactor_dir/.." && pwd)
datomic_home=${1:-"$project_dir/../../datomic/datomic-pro-1.0.7277"}
output_dir=${2:-/tmp/datomic-transactor-inventory}
transactor_jar="$datomic_home/datomic-transactor-pro-1.0.7277.jar"
peer_jar="$datomic_home/peer-1.0.7277.jar"
pom_entry=META-INF/maven/com.datomic/datomic-transactor-pro/pom.xml
expected_transactor_sha=d90819b57e2138085ddc26c93314f953c2cd776d1d1aa8b43a6bec1e475d3692
expected_peer_sha=cb55c9d01e155f9965e3332cd8ef70be42cdefacb50c072076e9ecf22b9a23ba
expected_pom_sha=8de366709ed36188a8596ae9e412c6e9ed9a226938eb39e6b6650a36a8892304

for command_name in java unzip sha256sum awk find sort cp stat; do
  command -v "$command_name" >/dev/null || {
    echo "missing required command: $command_name" >&2
    exit 1
  }
done

[[ -f "$transactor_jar" ]] || {
  echo "Transactor JAR not found: $transactor_jar" >&2
  exit 1
}
[[ -f "$peer_jar" ]] || {
  echo "Peer JAR not found: $peer_jar" >&2
  exit 1
}
[[ -f "$datomic_home/lib/asm-9.2.jar" ]] || {
  echo "ASM dependency not found under: $datomic_home/lib" >&2
  exit 1
}

actual_transactor_sha=$(sha256sum "$transactor_jar" | awk '{print $1}')
actual_peer_sha=$(sha256sum "$peer_jar" | awk '{print $1}')
[[ "$actual_transactor_sha" == "$expected_transactor_sha" ]] || {
  echo "unexpected Transactor SHA-256: $actual_transactor_sha" >&2
  exit 1
}
[[ "$actual_peer_sha" == "$expected_peer_sha" ]] || {
  echo "unexpected Peer SHA-256: $actual_peer_sha" >&2
  exit 1
}

if [[ -e "$output_dir" ]] && \
   [[ -n "$(find "$output_dir" -mindepth 1 -print -quit 2>/dev/null)" ]]; then
  echo "refusing to overwrite non-empty output directory: $output_dir" >&2
  exit 1
fi
mkdir -p "$output_dir/input" "$output_dir/raw" "$output_dir/archive"

unzip -p "$transactor_jar" "$pom_entry" > "$output_dir/input/transactor-pom.xml"
actual_pom_sha=$(sha256sum "$output_dir/input/transactor-pom.xml" | awk '{print $1}')
[[ "$actual_pom_sha" == "$expected_pom_sha" ]] || {
  echo "unexpected embedded Transactor POM SHA-256: $actual_pom_sha" >&2
  exit 1
}

java "$transactor_dir/tools/ArchiveInventory.java" \
  "$transactor_jar" "$output_dir/archive"

java -Xmx3g \
  -cp "$datomic_home/lib/asm-9.2.jar" \
  "$project_dir/tools/bytecode-inventory/BytecodeInventory.java" \
  --primary "$transactor_jar" \
  --companion-dir "$datomic_home/lib" \
  --pom "$output_dir/input/transactor-pom.xml" \
  --detail-pom-deps \
  --detail-jar "$peer_jar" \
  --out "$output_dir/raw"

"$script_dir/render-baseline.sh" "$output_dir/raw" "$output_dir/baseline"

cp "$output_dir/archive/archive-entries.tsv" \
  "$output_dir/archive/archive-summary.tsv" \
  "$output_dir/baseline/"

{
  printf 'distribution_path\tkind\tbytes\tsha256\n'
  while IFS= read -r file; do
    relative=${file#"$datomic_home/"}
    case "$relative" in
      bin/*) kind=distribution-bin ;;
      resources/*) kind=distribution-resource ;;
      *) kind=distribution-file ;;
    esac
    printf '%s\t%s\t%s\t%s\n' \
      "$relative" "$kind" "$(stat -c '%s' "$file")" \
      "$(sha256sum "$file" | awk '{print $1}')"
  done < <(find "$datomic_home/bin" "$datomic_home/resources" -type f | sort)
  for record in \
    'lib/console/datomic-console-0.1.242.jar optional-console' \
    'presto-server/plugin/datomic/datomic-presto-0.9.72.jar optional-presto'; do
    relative=${record% *}
    kind=${record##* }
    file="$datomic_home/$relative"
    [[ -f "$file" ]] || {
      echo "missing optional Datomic artifact: $file" >&2
      exit 1
    }
    printf '%s\t%s\t%s\t%s\n' \
      "$relative" "$kind" "$(stat -c '%s' "$file")" \
      "$(sha256sum "$file" | awk '{print $1}')"
  done
} > "$output_dir/baseline/distribution-files.tsv"

(cd "$output_dir/baseline" && sha256sum -- *.tsv > manifest.sha256)

archive_entries=$(awk -F '\t' '$1 == "entries.total" {print $2}' \
  "$output_dir/archive/archive-summary.tsv")
[[ "$archive_entries" == 9871 ]] || {
  echo "unexpected archive entry count: $archive_entries" >&2
  exit 1
}

echo "Transactor inventory passed: $output_dir"
