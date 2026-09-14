#!/usr/bin/env bash
set -euo pipefail

HERE=$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd)
OUT=${1:-/tmp/datomic-bytecode-inventory/out}
REV=${3:-$(cd -- "$HERE/../.." && pwd)}
DIST=${2:-"$REV/../../datomic/datomic-pro-1.0.7277"}
PRIMARY=peer-1.0.7277.jar
JAR=$DIST/$PRIMARY
NAMESPACE_INDEX=$REV/reports/source-index/namespaces.tsv

[[ -f "$NAMESPACE_INDEX" ]] || {
  echo "missing source namespace index: $NAMESPACE_INDEX" >&2
  exit 1
}

metric() {
  awk -F '\t' -v key="$1" '$1 == key {print $2}' "$OUT/summary.tsv"
}

assert_eq() {
  if [[ $2 != "$3" ]]; then
    echo "FAIL\t$1\texpected=$2\tactual=$3" >&2
    exit 1
  fi
  echo "PASS\t$1\t$2"
}

jar_classes=$(zipinfo -1 "$JAR" | awk '/\.class$/ {n++} END {print n+0}')
jar_resources=$(zipinfo -1 "$JAR" | awk '!/\/$/ && !/\.class$/ {n++} END {print n+0}')
parsed_classes=$(awk -F '\t' -v jar="$PRIMARY" 'NR > 1 && $1 == jar {n++} END {print n+0}' "$OUT/classes.tsv")
class_field_sum=$(awk -F '\t' -v jar="$PRIMARY" 'NR > 1 && $1 == jar {n += $15} END {print n+0}' "$OUT/classes.tsv")
class_method_sum=$(awk -F '\t' -v jar="$PRIMARY" 'NR > 1 && $1 == jar {n += $16} END {print n+0}' "$OUT/classes.tsv")
class_insn_sum=$(awk -F '\t' -v jar="$PRIMARY" 'NR > 1 && $1 == jar {n += $17} END {printf "%.0f\n", n}' "$OUT/classes.tsv")
class_call_sum=$(awk -F '\t' -v jar="$PRIMARY" 'NR > 1 && $1 == jar {n += $18} END {printf "%.0f\n", n}' "$OUT/classes.tsv")
class_field_access_sum=$(awk -F '\t' -v jar="$PRIMARY" 'NR > 1 && $1 == jar {n += $20} END {printf "%.0f\n", n}' "$OUT/classes.tsv")
class_literal_insn_sum=$(awk -F '\t' -v jar="$PRIMARY" 'NR > 1 && $1 == jar {n += $21} END {printf "%.0f\n", n}' "$OUT/classes.tsv")
class_literal_field_sum=$(awk -F '\t' -v jar="$PRIMARY" 'NR > 1 && $1 == jar {n += $22} END {printf "%.0f\n", n}' "$OUT/classes.tsv")

field_rows=$(awk -F '\t' -v jar="$PRIMARY" 'NR > 1 && $1 == jar {n++} END {print n+0}' "$OUT/fields.tsv")
method_rows=$(awk -F '\t' -v jar="$PRIMARY" 'NR > 1 && $1 == jar {n++} END {print n+0}' "$OUT/methods.tsv")
opcode_sum=$(awk -F '\t' -v jar="$PRIMARY" 'NR > 1 && $1 == jar {n += $7} END {printf "%.0f\n", n}' "$OUT/opcodes.tsv")
call_rows=$(awk -F '\t' -v jar="$PRIMARY" 'NR > 1 && $1 == jar {n++} END {print n+0}' "$OUT/calls.tsv")
field_access_rows=$(awk -F '\t' -v jar="$PRIMARY" 'NR > 1 && $1 == jar {n++} END {print n+0}' "$OUT/field-accesses.tsv")
literal_insn_rows=$(awk -F '\t' -v jar="$PRIMARY" 'NR > 1 && $1 == jar && $3 == "instruction" {n++} END {print n+0}' "$OUT/literals.tsv")
literal_field_rows=$(awk -F '\t' -v jar="$PRIMARY" 'NR > 1 && $1 == jar && $3 == "field" {n++} END {print n+0}' "$OUT/literals.tsv")
resource_rows=$(awk -F '\t' -v jar="$PRIMARY" 'NR > 1 && $1 == jar {n++} END {print n+0}' "$OUT/resources.tsv")
init_entries=$(zipinfo -1 "$JAR" | awk '/__init\.class$/ {n++} END {print n+0}')
warning_rows=$(awk 'NR > 1 {n++} END {print n+0}' "$OUT/warnings.tsv")
clj_sources=$(find "$REV/src-clj" -type f -name '*.clj' ! -name data_readers.clj | wc -l)
java_sources=$(find "$REV/src-java" -type f -name '*.java' | wc -l)
source_namespace_hash=$(awk -F '\t' 'NR > 1 {print $1}' "$NAMESPACE_INDEX" | sort -u | sha256sum | awk '{print $1}')
bytecode_namespace_hash=$(awk -F '\t' -v jar="$PRIMARY" 'NR > 1 && $1 == jar && $3 == "clojure-aot" {print $2}' "$OUT/namespace-classes.tsv" | sort -u | sha256sum | awk '{print $1}')

assert_eq jar_class_entries "$jar_classes" "$(metric primary.class_entries)"
assert_eq parsed_classes "$jar_classes" "$parsed_classes"
assert_eq summary_parsed_classes "$parsed_classes" "$(metric primary.parsed_classes)"
assert_eq field_definitions "$class_field_sum" "$field_rows"
assert_eq summary_fields "$field_rows" "$(metric primary.fields)"
assert_eq method_definitions "$class_method_sum" "$method_rows"
assert_eq summary_methods "$method_rows" "$(metric primary.methods)"
assert_eq opcode_histogram_sum "$class_insn_sum" "$opcode_sum"
assert_eq summary_instructions "$opcode_sum" "$(metric primary.instructions)"
assert_eq call_occurrences "$class_call_sum" "$call_rows"
assert_eq summary_calls "$call_rows" "$(metric primary.calls)"
assert_eq field_access_occurrences "$class_field_access_sum" "$field_access_rows"
assert_eq summary_field_accesses "$field_access_rows" "$(metric primary.field_accesses)"
assert_eq instruction_literal_occurrences "$class_literal_insn_sum" "$literal_insn_rows"
assert_eq summary_instruction_literals "$literal_insn_rows" "$(metric primary.literal_instructions)"
assert_eq field_literal_occurrences "$class_literal_field_sum" "$literal_field_rows"
assert_eq summary_field_literals "$literal_field_rows" "$(metric primary.literal_fields)"
assert_eq jar_resource_entries "$jar_resources" "$resource_rows"
assert_eq summary_resource_entries "$resource_rows" "$(metric primary.resource_entries)"
assert_eq namespace_initializers "$init_entries" "$(metric primary.namespace_init_classes)"
assert_eq reconstructed_clojure_sources "$clj_sources" "$(metric primary.distinct_clojure_source_paths)"
assert_eq namespace_name_manifest "$source_namespace_hash" "$bytecode_namespace_hash"
assert_eq decompiled_java_file_count "$java_sources" 5513
assert_eq parse_warnings 0 "$warning_rows"
assert_eq embedded_pom_matches true "$(metric pom.embedded_matches_input)"

echo "PASS\tall_checks"
