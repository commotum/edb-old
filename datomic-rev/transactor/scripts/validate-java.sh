#!/usr/bin/env bash
set -euo pipefail

export LC_ALL=C

script_dir=$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd)
transactor_dir=$(cd -- "$script_dir/.." && pwd)
project_dir=$(cd -- "$transactor_dir/.." && pwd)
datomic_home=${1:-"$project_dir/../../datomic/datomic-pro-1.0.7277"}
output_dir=${2:-/tmp/datomic-transactor-java-validation}
requested_javac=${3:-${DATOMIC_JAVA_JAVAC:-}}

source_dir="$transactor_dir/src-java"
source_manifest="$transactor_dir/reports/stage-1-java-source-manifest.sha256"
class_list="$transactor_dir/reports/stage-1-java-class-list.txt"
canonical_code_relation="$transactor_dir/reports/stage-1-java-code-relation.tsv"
canonical_validation_relation="$transactor_dir/reports/stage-1-java-validation-relation.tsv"
canonical_validation_summary="$transactor_dir/reports/stage-1-java-validation-summary.tsv"
compile_classpath_manifest="$transactor_dir/reports/stage-1-java-compile-classpath.tsv"
verification_tool="$transactor_dir/tools/VerifyJavaRecovery.java"
original_jar="$datomic_home/datomic-transactor-pro-1.0.7277.jar"
library_dir="$datomic_home/lib"
asm_jar="$library_dir/asm-9.2.jar"

expected_original_sha=d90819b57e2138085ddc26c93314f953c2cd776d1d1aa8b43a6bec1e475d3692
expected_asm_sha=b9d4fe4d71938df38839f0eca42aaaa64cf8b313d678da036f0cb3ca199b47f5
expected_source_manifest_file_sha=f48edbd567b53316b6e20cd1b2bc889f71a9c45eee3a6576cacf454a20da7f5f
expected_class_list_sha=c5fe736882eed01ca09dfc9e7d7de6eb7999ae2cfa1431ff0e39f716702a8c2b
expected_code_relation_sha=d76a019725d1077eebe3c2698436a91628589aa10b59c78e429eeb28725a8dcc
expected_validation_relation_sha=13547dc0ea153fc6922e0bdfc292746de728355b9b63d93b7200ecc90a57fd1c
expected_validation_summary_sha=ef3a9d35e97c94f85d46c6ac226b2324c9395921e208f802c82967ae8d81fee3
expected_compile_classpath_manifest_sha=b99670e560b39157bbeceeb71f00b82d8cdf6815dc0c84dfe5c0c8ae01ee3f81
corretto_url=https://corretto.aws/downloads/resources/11.0.22.7.1/amazon-corretto-11.0.22.7.1-linux-x64.tar.gz
corretto_archive_sha=f512bedb85adbef31c3823e219d9369e2bccb650575615478619b499f8e21117

for command_name in awk diff find realpath sha256sum sort xargs; do
  command -v "$command_name" >/dev/null || {
    echo "missing required command: $command_name" >&2
    exit 1
  }
done

for required_file in \
  "$original_jar" "$asm_jar" "$source_manifest" "$class_list" \
  "$canonical_code_relation" "$canonical_validation_relation" \
  "$canonical_validation_summary" "$compile_classpath_manifest" \
  "$verification_tool"; do
  [[ -f "$required_file" ]] || {
    echo "required file not found: $required_file" >&2
    exit 1
  }
done

project_abs=$(realpath -m "$project_dir")
original_abs=$(realpath "$original_jar")
output_abs=$(realpath -m "$output_dir")
case "$original_abs" in
  "$project_abs"|"$project_abs"/*)
    echo "licensed original must remain outside the repository: $original_abs" >&2
    exit 1
    ;;
esac
case "$output_abs" in
  "$project_abs"|"$project_abs"/*)
    echo "validation build/evidence must remain outside the repository: $output_abs" >&2
    exit 1
    ;;
esac

actual_original_sha=$(sha256sum "$original_jar" | awk '{print $1}')
actual_asm_sha=$(sha256sum "$asm_jar" | awk '{print $1}')
[[ "$actual_original_sha" == "$expected_original_sha" ]] || {
  echo "unexpected Transactor SHA-256: $actual_original_sha" >&2
  exit 1
}
[[ "$actual_asm_sha" == "$expected_asm_sha" ]] || {
  echo "unexpected ASM SHA-256: $actual_asm_sha" >&2
  exit 1
}
[[ "$(sha256sum "$source_manifest" | awk '{print $1}')" == \
    "$expected_source_manifest_file_sha" ]] || {
  echo "checked-in Java source manifest changed" >&2
  exit 1
}
[[ "$(sha256sum "$class_list" | awk '{print $1}')" == "$expected_class_list_sha" ]] || {
  echo "checked-in Java class list changed" >&2
  exit 1
}
[[ "$(sha256sum "$canonical_code_relation" | awk '{print $1}')" == \
    "$expected_code_relation_sha" ]] || {
  echo "checked-in canonical code relation changed" >&2
  exit 1
}
[[ "$(sha256sum "$canonical_validation_relation" | awk '{print $1}')" == \
    "$expected_validation_relation_sha" ]] || {
  echo "checked-in Java validation relation changed" >&2
  exit 1
}
[[ "$(sha256sum "$canonical_validation_summary" | awk '{print $1}')" == \
    "$expected_validation_summary_sha" ]] || {
  echo "checked-in Java validation summary changed" >&2
  exit 1
}
[[ "$(sha256sum "$compile_classpath_manifest" | awk '{print $1}')" == \
    "$expected_compile_classpath_manifest_sha" ]] || {
  echo "checked-in Java compile classpath manifest changed" >&2
  exit 1
}

compile_classpath=
compile_dependency_count=0
while IFS=$'\t' read -r jar_name expected_jar_sha compile_reason; do
  [[ "$jar_name" == jar ]] && continue
  [[ -n "$jar_name" && -n "$expected_jar_sha" && -n "$compile_reason" ]] || {
    echo "invalid Java compile-classpath row: $jar_name" >&2
    exit 1
  }
  jar_path="$library_dir/$jar_name"
  [[ -f "$jar_path" ]] || {
    echo "Java compile dependency not found: $jar_path" >&2
    exit 1
  }
  actual_jar_sha=$(sha256sum "$jar_path" | awk '{print $1}')
  [[ "$actual_jar_sha" == "$expected_jar_sha" ]] || {
    echo "unexpected Java compile dependency hash: $jar_name $actual_jar_sha" >&2
    exit 1
  }
  if [[ -z "$compile_classpath" ]]; then
    compile_classpath=$jar_path
  else
    compile_classpath="$compile_classpath:$jar_path"
  fi
  compile_dependency_count=$((compile_dependency_count + 1))
done < "$compile_classpath_manifest"
[[ "$compile_dependency_count" == 10 ]] || {
  echo "expected 10 pinned Java compile dependencies, found $compile_dependency_count" >&2
  exit 1
}

source_count=$(find "$source_dir" -type f -name '*.java' | awk 'END {print NR}')
class_count=$(awk 'NF && $1 !~ /^#/ {count++} END {print count + 0}' "$class_list")
canonical_matches=$(awk -F '\t' \
  'NF != 4 || $1 != "MATCH" || $3 != $4 {bad++} END {if (bad) exit 1; print NR + 0}' \
  "$canonical_code_relation")
[[ "$source_count" == 46 ]] || {
  echo "expected 46 Java sources, found $source_count" >&2
  exit 1
}
[[ "$class_count" == 52 ]] || {
  echo "expected 52 Java classes in closure, found $class_count" >&2
  exit 1
}
[[ "$canonical_matches" == 52 ]] || {
  echo "expected 52 rows in canonical code relation, found $canonical_matches" >&2
  exit 1
}
(cd "$source_dir" && sha256sum -c "$source_manifest") >/dev/null

if [[ -n "$requested_javac" ]]; then
  javac_bin=$requested_javac
elif [[ -x /tmp/amazon-corretto-11.0.22.7.1/bin/javac ]]; then
  javac_bin=/tmp/amazon-corretto-11.0.22.7.1/bin/javac
else
  javac_bin=$(command -v javac || true)
fi
[[ -n "$javac_bin" && -x "$javac_bin" ]] || {
  echo "Amazon Corretto javac 11.0.22 is required." >&2
  echo "Official archive: $corretto_url" >&2
  echo "Expected archive SHA-256: $corretto_archive_sha" >&2
  exit 1
}
javac_bin=$(realpath "$javac_bin")
java_bin=$(dirname "$javac_bin")/java
[[ -x "$java_bin" ]] || {
  echo "matching Java runtime not found beside javac: $java_bin" >&2
  exit 1
}
javac_version=$("$javac_bin" -version 2>&1)
java_version=$("$java_bin" -version 2>&1)
[[ "$javac_version" == "javac 11.0.22" ]] || {
  echo "exact javac 11.0.22 required; found: $javac_version" >&2
  exit 1
}
[[ "$java_version" == *"Corretto-11.0.22.7.1"* ]] || {
  echo "exact Amazon Corretto 11.0.22.7.1 runtime required" >&2
  exit 1
}

if [[ -e "$output_abs" ]] && \
   [[ -n "$(find "$output_abs" -mindepth 1 -print -quit 2>/dev/null)" ]]; then
  echo "refusing to overwrite non-empty output directory: $output_abs" >&2
  exit 1
fi
mkdir -p "$output_abs/classes-a" "$output_abs/classes-b" \
  "$output_abs/tool-classes" "$output_abs/evidence"

find "$source_dir" -type f -name '*.java' | sort > "$output_abs/sources.txt"
compile() {
  local destination=$1
  "$javac_bin" \
    -g -source 11 -target 11 -encoding UTF-8 -proc:none \
    -classpath "$compile_classpath" \
    -d "$destination" \
    @"$output_abs/sources.txt"
}
compile "$output_abs/classes-a"
compile "$output_abs/classes-b"

(cd "$output_abs/classes-a" && \
  find . -type f -name '*.class' -print0 | sort -z | xargs -0 sha256sum) \
  > "$output_abs/evidence/candidate-classes-a.sha256"
(cd "$output_abs/classes-b" && \
  find . -type f -name '*.class' -print0 | sort -z | xargs -0 sha256sum) \
  > "$output_abs/evidence/candidate-classes-b.sha256"
diff -u "$output_abs/evidence/candidate-classes-a.sha256" \
  "$output_abs/evidence/candidate-classes-b.sha256" >/dev/null

"$javac_bin" -g:none -proc:none -classpath "$asm_jar" \
  -d "$output_abs/tool-classes" "$verification_tool"
"$java_bin" -classpath "$output_abs/tool-classes:$asm_jar" VerifyJavaRecovery \
  "$output_abs/classes-a" "$original_jar" "$class_list" "$output_abs/evidence"
diff -u "$canonical_validation_relation" \
  "$output_abs/evidence/java-class-relation.tsv" >/dev/null
diff -u "$canonical_validation_summary" "$output_abs/evidence/summary.tsv" >/dev/null

for expected_metric in \
  'classes.expected 52' 'classes.candidate 52' \
  'fields.candidate 122' 'fields.original 122' \
  'methods.candidate 303' 'methods.original 303' \
  'abi.matches 52' 'abi.differences 0' \
  'code.matches 52' 'code.differences 0'; do
  metric=${expected_metric% *}
  expected=${expected_metric##* }
  actual=$(awk -F '\t' -v metric="$metric" '$1 == metric {print $2}' \
    "$output_abs/evidence/summary.tsv")
  [[ "$actual" == "$expected" ]] || {
    echo "unexpected $metric: expected $expected, found ${actual:-missing}" >&2
    exit 1
  }
done

raw_byte_matches=$(awk -F '\t' 'NR > 1 && $8 == $9 {count++} END {print count + 0}' \
  "$output_abs/evidence/java-class-relation.tsv")
{
  printf 'metric\tvalue\n'
  printf 'compiler\t%s\n' "$javac_version"
  printf 'runtime\tCorretto-11.0.22.7.1\n'
  printf 'original.jar.sha256\t%s\n' "$actual_original_sha"
  printf 'asm.jar.sha256\t%s\n' "$actual_asm_sha"
  printf 'sources\t%s\n' "$source_count"
  printf 'classes\t%s\n' "$class_count"
  printf 'abi.matches\t52\n'
  printf 'code.matches\t52\n'
  printf 'raw-byte-identical-after-recompile\t%s\n' "$raw_byte_matches"
  printf 'repeat-build-manifest\tMATCH\n'
  printf 'candidate.compile.dependencies\t%s pinned JARs\n' "$compile_dependency_count"
  printf 'candidate.compile.classpath.manifest.sha256\t%s\n' \
    "$expected_compile_classpath_manifest_sha"
  printf 'candidate.compile.implementation-jars\tnone\n'
  printf 'original.oracle.access\tstreamed ZIP entries only\n'
} > "$output_abs/evidence/validation.tsv"

(cd "$output_abs/evidence" && sha256sum -- \
  candidate-classes-a.sha256 candidate-classes-b.sha256 \
  java-class-relation.tsv summary.tsv validation.tsv > manifest.sha256)
(cd "$output_abs/evidence" && sha256sum -c manifest.sha256) >/dev/null

echo "Transactor Java recovery validation passed: $output_abs"
echo "46 sources -> 52 classes; ABI 52/52; normalized code 52/52"
echo "Deterministic repeat build: MATCH; regenerated raw-byte matches: $raw_byte_matches"
