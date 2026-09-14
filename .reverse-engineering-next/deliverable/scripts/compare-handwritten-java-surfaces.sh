#!/usr/bin/env bash

set -euo pipefail

script_dir=$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd)
project_dir=$(cd -- "$script_dir/.." && pwd)
datomic_home=${1:-"$project_dir/../../datomic/datomic-pro-1.0.7277"}
work_root=${2:-$(mktemp -d -t datomic-java-surface.XXXXXXXX)}
peer_jar="$datomic_home/peer-1.0.7277.jar"
asm_jar="$datomic_home/lib/asm-9.2.jar"
source_list="$project_dir/reports/handwritten-java-sources.txt"
expected_sha=cb55c9d01e155f9965e3332cd8ef70be42cdefacb50c072076e9ecf22b9a23ba

for command_name in java zip sha256sum awk sort cmp diff find mkdir wc; do
  command -v "$command_name" >/dev/null || {
    echo "missing required command: $command_name" >&2
    exit 1
  }
done

[[ -f "$peer_jar" && -f "$asm_jar" && -f "$source_list" ]] || {
  echo "missing peer JAR, ASM dependency, or handwritten source list" >&2
  exit 1
}

actual_sha=$(sha256sum "$peer_jar" | awk '{print $1}')
[[ "$actual_sha" == "$expected_sha" ]] || {
  echo "unexpected peer JAR SHA-256: $actual_sha" >&2
  exit 1
}

if [[ -e "$work_root" ]] && [[ -n "$(find "$work_root" -mindepth 1 -print -quit 2>/dev/null)" ]]; then
  echo "refusing to overwrite non-empty validation directory: $work_root" >&2
  exit 1
fi

classes_dir="$work_root/classes"
candidate_jar="$work_root/handwritten.jar"
candidate_inventory="$work_root/candidate-inventory"
original_inventory="$work_root/original-inventory"
normalized="$work_root/normalized"
mkdir -p "$classes_dir" "$normalized"

java "$script_dir/CompileSources.java" \
  "$project_dir/src-java" \
  "$classes_dir" \
  "$peer_jar:$datomic_home/lib/*" \
  "$source_list"

(cd -- "$classes_dir" && zip -q -r "$candidate_jar" .)

java -cp "$asm_jar" "$project_dir/tools/bytecode-inventory/BytecodeInventory.java" \
  --primary "$candidate_jar" --out "$candidate_inventory"
java -cp "$asm_jar" "$project_dir/tools/bytecode-inventory/BytecodeInventory.java" \
  --primary "$peer_jar" --out "$original_inventory"

awk -F '\t' 'NR > 1 {print $3}' "$candidate_inventory/classes.tsv" \
  | sort >"$normalized/class-names.txt"

awk -F '\t' 'NR > 1 {print $3 FS $5 FS $6 FS $7 FS $8 FS $13 FS $14}' \
  "$candidate_inventory/classes.tsv" | sort >"$normalized/candidate-classes.tsv"
awk -F '\t' 'NR == FNR {wanted[$1] = 1; next}
  NR > 1 && ($3 in wanted) {print $3 FS $5 FS $6 FS $7 FS $8 FS $13 FS $14}' \
  "$normalized/class-names.txt" "$original_inventory/classes.tsv" \
  | sort >"$normalized/original-classes.tsv"

awk -F '\t' 'NR > 1 {print $2 FS $3 FS $4 FS $5 FS $6 FS $7 FS $8}' \
  "$candidate_inventory/fields.tsv" | sort >"$normalized/candidate-fields.tsv"
awk -F '\t' 'NR == FNR {wanted[$1] = 1; next}
  NR > 1 && ($2 in wanted) {print $2 FS $3 FS $4 FS $5 FS $6 FS $7 FS $8}' \
  "$normalized/class-names.txt" "$original_inventory/fields.tsv" \
  | sort >"$normalized/original-fields.tsv"

awk -F '\t' 'NR > 1 {print $2 FS $3 FS $4 FS $5 FS $6 FS $7}' \
  "$candidate_inventory/methods.tsv" | sort >"$normalized/candidate-methods.tsv"
awk -F '\t' 'NR == FNR {wanted[$1] = 1; next}
  NR > 1 && ($2 in wanted) {print $2 FS $3 FS $4 FS $5 FS $6 FS $7}' \
  "$normalized/class-names.txt" "$original_inventory/methods.tsv" \
  | sort >"$normalized/original-methods.tsv"

for surface in classes fields methods; do
  if ! cmp -s "$normalized/original-$surface.tsv" \
             "$normalized/candidate-$surface.tsv"; then
    echo "handwritten Java $surface surface mismatch" >&2
    diff -u "$normalized/original-$surface.tsv" \
            "$normalized/candidate-$surface.tsv" >&2 || true
    exit 1
  fi
done

class_count=$(wc -l <"$normalized/candidate-classes.tsv")
field_count=$(wc -l <"$normalized/candidate-fields.tsv")
method_count=$(wc -l <"$normalized/candidate-methods.tsv")
[[ "$class_count" -eq 47 && "$field_count" -eq 108 && "$method_count" -eq 279 ]] || {
  echo "unexpected Java surface counts: $class_count classes, $field_count fields, $method_count methods" >&2
  exit 1
}

echo "handwritten Java surfaces match: 47 classes, 108 fields, 279 methods"
echo "compared class version/access/super/interfaces and field/method descriptors, signatures, access, constants, and exceptions"
echo "details: $work_root"
