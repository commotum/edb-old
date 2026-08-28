#!/usr/bin/env bash
set -euo pipefail

export LC_ALL=C

script_dir=$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd)
transactor_dir=$(cd -- "$script_dir/.." && pwd)
project_dir=$(cd -- "$transactor_dir/.." && pwd)
datomic_home=${1:-"$project_dir/../../datomic/datomic-pro-1.0.7277"}
output_dir=${2:-/tmp/datomic-transactor-exact-source-aot-validation}
requested_java=${3:-${DATOMIC_EXACT_AOT_JAVA:-}}
requested_sanitized_nano=${4:-${DATOMIC_EXACT_AOT_SANITIZED_NANO:-}}

source_dir="$transactor_dir/src-clj"
bundled_ownership="$transactor_dir/reports/stage-1-bundled-source-ownership.tsv"
datomic_ownership="$transactor_dir/reports/stage-1-datomic-dependency-source-ownership.tsv"
class_ownership="$transactor_dir/reports/stage-1-class-ownership.tsv"
distribution_jars="$transactor_dir/baseline/distribution-jars.tsv"
compile_runner="$script_dir/compile-exact-source-namespace.clj"
comparator_source="$transactor_dir/tools/CompareExactSourceAot.java"
nano_summary="$transactor_dir/reports/stage-1-nano-sanitization-summary.tsv"
original_jar="$datomic_home/datomic-transactor-pro-1.0.7277.jar"
library_dir="$datomic_home/lib"

expected_original_sha=d90819b57e2138085ddc26c93314f953c2cd776d1d1aa8b43a6bec1e475d3692
expected_peer_sha=cb55c9d01e155f9965e3332cd8ef70be42cdefacb50c072076e9ecf22b9a23ba
expected_core2_sha=81fdf81586c7be1a4b61655b568348d12db7892cb4562d0db7529bbc7af8a94b
expected_namespace_count=87
expected_bundled_namespace_count=85
expected_datomic_namespace_count=2
expected_class_count=3431
expected_dependency_count=532
expected_nano_summary_sha=5fcb3027861a82f4e9c477511ade4422a8503c385df5edc5359a9184509a3354
expected_nano_original_sha=fbde8184da356bd3a9995a9f05f01493efe4baa87c4d48377cda3e53331807cd
expected_nano_derivative_sha=08f9699d6e35b9e052ec85ee15e0896b3a685c74e6e226d8cd89d9c61dbf8d5f
expected_nano_derivative_bytes=81218
expected_nano_manifest_sha=af6b5efdd9a3cdf16d1e1f960910bc1b72a686fecae26734bc4b65c6ba8408eb
expected_bundled_ownership_sha=01926ddaf34277f6bf69cce1284e6098b4adffc5ada940e02a721f0c89b3455f
expected_datomic_ownership_sha=04e98999d62b6b19b0a10ae7cce385bf45dc7f9fa0bdc18c3020ac46a00ab0fe
expected_class_ownership_sha=bb3be13708ba945dd2f8d2edce7adffb7d43c8a6fb265cdca0c3d2fa65122b87
expected_distribution_jars_sha=db25519069bfd327c161220264b89fb011f2d9a3e42e4857e5233d4b61811860
expected_compile_runner_sha=c10f41439273457d414b519961706463721ba8fb3f0f372fe3b79de0e3e17d98
expected_comparator_source_sha=5c5ea8485fcb64676bd41d2bda4c9af6517844993a96337bd4be09917b1bf531
expected_asm_sha=b9d4fe4d71938df38839f0eca42aaaa64cf8b313d678da036f0cb3ca199b47f5
expected_java_bin_sha=3e98d0f812482808f701ffa0d4e94b8dd8e31a2c6a07fb52662ceef8e75d66f1
expected_javac_bin_sha=f315d031604835a3017268cca49631f4aa441d084d3cc29932d296ecd013106c
per_namespace_timeout_seconds=${DATOMIC_EXACT_AOT_TIMEOUT_SECONDS:-180}

for command_name in awk diff find realpath sed sha256sum sort timeout tr wc xargs; do
  command -v "$command_name" >/dev/null || {
    echo "missing required command: $command_name" >&2
    exit 1
  }
done

verify_pinned_file() {
  local path=$1
  local expected=$2
  local label=$3
  local actual
  actual=$(sha256sum "$path" | awk '{print $1}')
  [[ "$actual" == "$expected" ]] || {
    echo "$label SHA-256 mismatch: expected $expected, found $actual" >&2
    exit 1
  }
}

verify_pinned_file "$bundled_ownership" "$expected_bundled_ownership_sha" bundled-source-ownership
verify_pinned_file "$datomic_ownership" "$expected_datomic_ownership_sha" datomic-source-ownership
verify_pinned_file "$class_ownership" "$expected_class_ownership_sha" class-ownership
verify_pinned_file "$distribution_jars" "$expected_distribution_jars_sha" distribution-jars
verify_pinned_file "$compile_runner" "$expected_compile_runner_sha" compile-runner
verify_pinned_file "$comparator_source" "$expected_comparator_source_sha" comparator-source

for required_file in \
  "$original_jar" "$bundled_ownership" "$datomic_ownership" \
  "$class_ownership" "$distribution_jars" "$compile_runner" \
  "$comparator_source" "$nano_summary"; do
  [[ -f "$required_file" ]] || {
    echo "required file not found: $required_file" >&2
    exit 1
  }
done

actual_nano_summary_sha=$(sha256sum "$nano_summary" | awk '{print $1}')
[[ "$actual_nano_summary_sha" == "$expected_nano_summary_sha" ]] || {
  echo "unexpected Nano sanitization summary SHA-256: $actual_nano_summary_sha" >&2
  exit 1
}
summary_nano_output=$(awk -F '\t' '$1 == "canonical.output" {print $2}' "$nano_summary")
summary_nano_sha=$(awk -F '\t' '$1 == "derivative.jar.sha256" {print $2}' "$nano_summary")
summary_nano_bytes=$(awk -F '\t' '$1 == "derivative.jar.bytes" {print $2}' "$nano_summary")
summary_manifest_sha=$(awk -F '\t' '$1 == "canonical.output.manifest.sha256" {print $2}' "$nano_summary")
[[ "$summary_nano_sha" == "$expected_nano_derivative_sha" && \
   "$summary_nano_bytes" == "$expected_nano_derivative_bytes" && \
   "$summary_manifest_sha" == "$expected_nano_manifest_sha" ]] || {
  echo "Nano sanitization summary does not bind the expected derivative" >&2
  exit 1
}
summary_nano_path="$summary_nano_output/nano-impl-0.1.325-sanitized.jar"
if [[ -n "$requested_sanitized_nano" ]]; then
  sanitized_nano=$requested_sanitized_nano
else
  sanitized_nano=$summary_nano_path
fi
[[ ! -L "$sanitized_nano" && -f "$sanitized_nano" ]] || {
  echo "sanitized Nano derivative must be a regular non-symlink file: $sanitized_nano" >&2
  exit 1
}
sanitized_nano=$(realpath "$sanitized_nano")
sanitized_nano_dir=$(dirname "$sanitized_nano")
sanitized_manifest="$sanitized_nano_dir/manifest.sha256"
[[ ! -L "$sanitized_manifest" && -f "$sanitized_manifest" ]] || {
  echo "sanitized Nano manifest must be a regular non-symlink file: $sanitized_manifest" >&2
  exit 1
}
actual_nano_sha=$(sha256sum "$sanitized_nano" | awk '{print $1}')
actual_nano_bytes=$(wc -c < "$sanitized_nano")
actual_nano_manifest_sha=$(sha256sum "$sanitized_manifest" | awk '{print $1}')
[[ "$actual_nano_sha" == "$expected_nano_derivative_sha" && \
   "$actual_nano_bytes" == "$expected_nano_derivative_bytes" && \
   "$actual_nano_manifest_sha" == "$expected_nano_manifest_sha" ]] || {
  echo "sanitized Nano derivative or manifest hash/size mismatch" >&2
  exit 1
}
(cd "$sanitized_nano_dir" && sha256sum -c manifest.sha256 >/dev/null)

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
    echo "validation output must remain outside the repository: $output_abs" >&2
    exit 1
    ;;
esac
case "$sanitized_nano" in
  "$project_abs"|"$project_abs"/*)
    echo "sanitized Nano derivative must remain outside the repository: $sanitized_nano" >&2
    exit 1
    ;;
esac
if [[ -e "$output_abs" ]] && \
   [[ -n "$(find "$output_abs" -mindepth 1 -print -quit 2>/dev/null)" ]]; then
  echo "refusing to overwrite non-empty output directory: $output_abs" >&2
  exit 1
fi

actual_original_sha=$(sha256sum "$original_jar" | awk '{print $1}')
[[ "$actual_original_sha" == "$expected_original_sha" ]] || {
  echo "unexpected Transactor SHA-256: $actual_original_sha" >&2
  exit 1
}

if [[ -n "$requested_java" ]]; then
  java_bin=$requested_java
elif [[ -x /tmp/amazon-corretto-11.0.22.7.1/bin/java ]]; then
  java_bin=/tmp/amazon-corretto-11.0.22.7.1/bin/java
else
  java_bin=$(command -v java || true)
fi
[[ -n "$java_bin" && -x "$java_bin" ]] || {
  echo "Amazon Corretto 11.0.22.7.1 java is required" >&2
  exit 1
}
java_bin=$(realpath "$java_bin")
javac_bin=$(dirname "$java_bin")/javac
asm_jar="$library_dir/asm-9.2.jar"
[[ -x "$javac_bin" && -f "$asm_jar" ]] || {
  echo "matching javac and ASM 9.2 are required" >&2
  exit 1
}
verify_pinned_file "$java_bin" "$expected_java_bin_sha" java-runtime
verify_pinned_file "$javac_bin" "$expected_javac_bin_sha" java-compiler
verify_pinned_file "$asm_jar" "$expected_asm_sha" asm-library
java_version=$($java_bin -version 2>&1)
[[ "$java_version" == *'openjdk version "11.0.22"'* ]] || {
  echo "exact OpenJDK 11.0.22 required" >&2
  echo "$java_version" >&2
  exit 1
}
[[ "$java_version" == *"Corretto-11.0.22.7.1"* ]] || {
  echo "exact Amazon Corretto 11.0.22.7.1 runtime required" >&2
  echo "$java_version" >&2
  exit 1
}

mkdir -p "$output_abs/evidence" "$output_abs/classes-a" "$output_abs/classes-b" \
  "$output_abs/logs-a" "$output_abs/logs-b" "$output_abs/timing-a" "$output_abs/timing-b" \
  "$output_abs/comparator-classes"

"$javac_bin" -g:none -proc:none -cp "$asm_jar" \
  -d "$output_abs/comparator-classes" "$comparator_source"
"$java_bin" -Xmx512M -cp "$output_abs/comparator-classes:$asm_jar" \
  CompareExactSourceAot --self-test "$output_abs/evidence/comparator-self-test"

toolchain="$output_abs/evidence/toolchain.tsv"
{
  printf 'component\tpath\tsha256\n'
  printf 'java\t%s\t%s\n' "$java_bin" "$expected_java_bin_sha"
  printf 'javac\t%s\t%s\n' "$javac_bin" "$expected_javac_bin_sha"
  printf 'asm\t%s\t%s\n' "$asm_jar" "$expected_asm_sha"
  printf 'compile-runner\t%s\t%s\n' "$compile_runner" "$expected_compile_runner_sha"
  printf 'comparator-source\t%s\t%s\n' "$comparator_source" "$expected_comparator_source_sha"
  printf 'sanitized-nano\t%s\t%s\n' "$sanitized_nano" "$actual_nano_sha"
  printf 'sanitized-nano-manifest\t%s\t%s\n' "$sanitized_manifest" "$actual_nano_manifest_sha"
} > "$toolchain"

runtime_input_paths="$output_abs/evidence/runtime-input-paths.txt"
printf '%s\n' \
  "$original_jar" "$bundled_ownership" "$datomic_ownership" \
  "$class_ownership" "$distribution_jars" "$compile_runner" \
  "$comparator_source" "$nano_summary" "$sanitized_nano" \
  "$sanitized_manifest" "$java_bin" "$javac_bin" "$asm_jar" \
  "$(realpath "$0")" > "$runtime_input_paths"

cohort_base="$output_abs/evidence/cohort-base.tsv"
cohort="$output_abs/evidence/cohort.tsv"
{
  printf 'namespace\tscope\tsource_entry\tsource_sha256\towner_jar\towner_jar_sha256\n'
  awk -F '\t' 'NR > 1 {
    print $1 "\tbundled\t" $8 "\t" $9 "\t" $6 "\t" $7
  }' "$bundled_ownership"
  awk -F '\t' 'NR > 1 && $4 == "single-exact-path" {
    print $1 "\tdatomic\t" $8 "\t" $9 "\t" $6 "\t" $7
  }' "$datomic_ownership"
} | {
  IFS= read -r header
  printf '%s\n' "$header"
  sort
} > "$cohort_base"

awk -F '\t' '
  FNR == NR {
    if (FNR > 1) expected[$5]++
    next
  }
  FNR == 1 {
    print $0 "\texpected_classes"
    next
  }
  {
    if (!($1 in expected)) {
      print "no original class ownership for " $1 > "/dev/stderr"
      exit 1
    }
    print $0 "\t" expected[$1]
  }
' "$class_ownership" "$cohort_base" > "$cohort"

namespace_count=$(awk 'NR > 1 {count++} END {print count + 0}' "$cohort")
bundled_count=$(awk -F '\t' 'NR > 1 && $2 == "bundled" {count++} END {print count + 0}' "$cohort")
datomic_count=$(awk -F '\t' 'NR > 1 && $2 == "datomic" {count++} END {print count + 0}' "$cohort")
class_count=$(awk -F '\t' 'NR > 1 {count += $7} END {print count + 0}' "$cohort")
[[ "$namespace_count" == "$expected_namespace_count" ]] || {
  echo "expected $expected_namespace_count exact namespaces, found $namespace_count" >&2
  exit 1
}
[[ "$bundled_count" == "$expected_bundled_namespace_count" ]] || {
  echo "expected $expected_bundled_namespace_count bundled namespaces, found $bundled_count" >&2
  exit 1
}
[[ "$datomic_count" == "$expected_datomic_namespace_count" ]] || {
  echo "expected $expected_datomic_namespace_count exact Datomic namespaces, found $datomic_count" >&2
  exit 1
}
[[ "$class_count" == "$expected_class_count" ]] || {
  echo "expected $expected_class_count owned classes, found $class_count" >&2
  exit 1
}

while IFS=$'\t' read -r namespace_name scope source_entry source_sha owner_jar owner_sha expected_classes; do
  [[ "$namespace_name" == namespace ]] && continue
  source_file="$source_dir/$source_entry"
  [[ -f "$source_file" ]] || {
    echo "exact source not found: $source_file" >&2
    exit 1
  }
  actual_source_sha=$(sha256sum "$source_file" | awk '{print $1}')
  [[ "$actual_source_sha" == "$source_sha" ]] || {
    echo "exact source hash mismatch: $namespace_name $actual_source_sha" >&2
    exit 1
  }
  [[ "$owner_jar" != *,* && "$owner_sha" != *,* ]] || {
    echo "exact source owner must be singular: $namespace_name" >&2
    exit 1
  }
  owner_path="$datomic_home/$owner_jar"
  [[ -f "$owner_path" ]] || {
    echo "exact source owner jar not found: $owner_path" >&2
    exit 1
  }
  actual_owner_sha=$(sha256sum "$owner_path" | awk '{print $1}')
  [[ "$actual_owner_sha" == "$owner_sha" ]] || {
    echo "exact source owner hash mismatch: $namespace_name $actual_owner_sha" >&2
    exit 1
  }
  printf '%s\n%s\n' "$source_file" "$owner_path" >> "$runtime_input_paths"
done < "$cohort"

classpath_manifest="$output_abs/evidence/compile-classpath.tsv"
printf 'distribution_path\tcompile_path\tsha256\tsource\n' > "$classpath_manifest"
compile_classpath="$source_dir"
dependency_count=0
while IFS=$'\t' read -r jar distribution_path expected_sha rest; do
  [[ "$jar" == jar ]] && continue
  [[ "$distribution_path" == lib/* ]] || continue
  [[ "$jar" == core2-1.0.140.jar ]] && continue
  dependency_path="$datomic_home/$distribution_path"
  [[ -f "$dependency_path" ]] || {
    echo "compile dependency not found: $dependency_path" >&2
    exit 1
  }
  actual_dependency_sha=$(sha256sum "$dependency_path" | awk '{print $1}')
  [[ "$actual_dependency_sha" == "$expected_sha" ]] || {
    echo "compile dependency hash mismatch: $distribution_path" >&2
    exit 1
  }
  case "$actual_dependency_sha" in
    "$expected_original_sha"|"$expected_peer_sha"|"$expected_core2_sha")
      echo "forbidden implementation artifact entered compile classpath: $distribution_path" >&2
      exit 1
      ;;
  esac
  compile_dependency_path=$dependency_path
  compile_dependency_sha=$actual_dependency_sha
  compile_dependency_source=licensed-distribution
  if [[ "$distribution_path" == lib/nano-impl-0.1.325.jar ]]; then
    [[ "$actual_dependency_sha" == "$expected_nano_original_sha" ]] || {
      echo "unexpected original Nano implementation hash: $actual_dependency_sha" >&2
      exit 1
    }
    compile_dependency_path=$sanitized_nano
    compile_dependency_sha=$actual_nano_sha
    compile_dependency_source=deterministic-sanitized-derivative
  fi
  printf '%s\t%s\t%s\t%s\n' "$distribution_path" "$compile_dependency_path" \
    "$compile_dependency_sha" "$compile_dependency_source" >> "$classpath_manifest"
  compile_classpath="$compile_classpath:$compile_dependency_path"
  printf '%s\n%s\n' "$dependency_path" "$compile_dependency_path" >> "$runtime_input_paths"
  dependency_count=$((dependency_count + 1))
done < "$distribution_jars"
[[ "$dependency_count" == "$expected_dependency_count" ]] || {
  echo "expected $expected_dependency_count compile dependencies, found $dependency_count" >&2
  exit 1
}

sort -u -o "$runtime_input_paths" "$runtime_input_paths"
runtime_input_manifest="$output_abs/evidence/runtime-inputs.sha256"
while IFS= read -r runtime_input; do
  sha256sum "$runtime_input"
done < "$runtime_input_paths" > "$runtime_input_manifest"
runtime_input_manifest_sha=$(sha256sum "$runtime_input_manifest" | awk '{print $1}')

expected_root="$output_abs/evidence/original-class-names"
mkdir -p "$expected_root"
while IFS=$'\t' read -r namespace_name scope source_entry source_sha owner_jar owner_sha expected_classes; do
  [[ "$namespace_name" == namespace ]] && continue
  awk -F '\t' -v namespace_name="$namespace_name" \
    'NR > 1 && $5 == namespace_name {print $1}' "$class_ownership" | sort \
    > "$expected_root/$namespace_name.txt"
  actual_expected=$(wc -l < "$expected_root/$namespace_name.txt")
  [[ "$actual_expected" == "$expected_classes" ]] || {
    echo "owned class-list count mismatch: $namespace_name" >&2
    exit 1
  }
done < "$cohort"

relation="$output_abs/evidence/namespace-relation.tsv"
printf 'namespace\tscope\texpected_classes\tclasses_a\tclasses_b\tdeterministic_paths\tdeterministic_bytes\tname_skeleton\tinitializer\tstatus\n' > "$relation"

compile_one() {
  local build_name=$1
  local namespace_name=$2
  local source_entry=$3
  local destination=$4
  local log_dir=$5
  local timing_dir=$6
  local stdout_file="$log_dir/$namespace_name.out"
  local stderr_file="$log_dir/$namespace_name.err"
  local timing_file="$timing_dir/$namespace_name.tsv"
  timeout --signal=TERM "$per_namespace_timeout_seconds" \
    /usr/bin/time -f 'elapsed_seconds\t%e\nmax_rss_kib\t%M\nexit_status\t%x' -o "$timing_file" \
    "$java_bin" -server -Xms1G -Xmx1G \
      -Dfile.encoding=UTF-8 \
      -Duser.language=en -Duser.country=US -Duser.timezone=UTC \
      -Dclojure.compiler.elide-meta='[:doc :file :line]' \
      -Dclojure.compile.warn-on-reflection=true \
      -cp "$compile_classpath" \
      clojure.main "$compile_runner" \
      "$namespace_name" "$source_dir" "$source_entry" "$destination" \
      > "$stdout_file" 2> "$stderr_file" || {
        local status=$?
        echo "$build_name compile failed for $namespace_name (exit $status)" >&2
        return "$status"
      }
}

overall_status=0
namespace_index=0
while IFS=$'\t' read -r namespace_name scope source_entry source_sha owner_jar owner_sha expected_classes; do
  [[ "$namespace_name" == namespace ]] && continue
  namespace_index=$((namespace_index + 1))
  echo "[$namespace_index/$namespace_count] validating $namespace_name" >&2
  destination_a="$output_abs/classes-a/$namespace_name"
  destination_b="$output_abs/classes-b/$namespace_name"
  mkdir -p "$destination_a" "$destination_b"
  if ! compile_one A "$namespace_name" "$source_entry" "$destination_a" \
      "$output_abs/logs-a" "$output_abs/timing-a"; then
    printf '%s\t%s\t%s\t0\t0\tfalse\tfalse\tUNSUPPORTED\tfalse\tCOMPILE_A_FAILED\n' \
      "$namespace_name" "$scope" "$expected_classes" >> "$relation"
    overall_status=1
    continue
  fi
  if ! compile_one B "$namespace_name" "$source_entry" "$destination_b" \
      "$output_abs/logs-b" "$output_abs/timing-b"; then
    classes_a=$(find "$destination_a" -type f -name '*.class' | awk 'END {print NR + 0}')
    printf '%s\t%s\t%s\t%s\t0\tfalse\tfalse\tUNSUPPORTED\tfalse\tCOMPILE_B_FAILED\n' \
      "$namespace_name" "$scope" "$expected_classes" "$classes_a" >> "$relation"
    overall_status=1
    continue
  fi

  names_a="$output_abs/evidence/$namespace_name.classes-a.txt"
  names_b="$output_abs/evidence/$namespace_name.classes-b.txt"
  hashes_a="$output_abs/evidence/$namespace_name.classes-a.sha256"
  hashes_b="$output_abs/evidence/$namespace_name.classes-b.sha256"
  skeleton_a="$output_abs/evidence/$namespace_name.skeleton-a.txt"
  skeleton_original="$output_abs/evidence/$namespace_name.skeleton-original.txt"
  (cd "$destination_a" && find . -type f -name '*.class' -printf '%P\n' | sort) > "$names_a"
  (cd "$destination_b" && find . -type f -name '*.class' -printf '%P\n' | sort) > "$names_b"
  (cd "$destination_a" && find . -type f -name '*.class' -print0 | sort -z | xargs -0 sha256sum) > "$hashes_a"
  (cd "$destination_b" && find . -type f -name '*.class' -print0 | sort -z | xargs -0 sha256sum) > "$hashes_b"

  classes_a=$(wc -l < "$names_a")
  classes_b=$(wc -l < "$names_b")
  deterministic_paths=false
  deterministic_bytes=false
  name_skeleton=DIFFER
  initializer=false
  status=PASS
  if diff -u "$names_a" "$names_b" >/dev/null; then deterministic_paths=true; fi
  if diff -u "$hashes_a" "$hashes_b" >/dev/null; then deterministic_bytes=true; fi
  # This is deliberately only a class-name skeleton gate. It recognizes the
  # three compiler-generated numeric name forms observed in the exact cohort;
  # it does not normalize proxy hashes or claim bytecode equivalence.
  sed -E \
    -e 's/__[0-9]+/__ID/g' \
    -e 's/\$eval[0-9]+/\$evalID/g' \
    -e 's/\$inst_[0-9]+__/\$inst_ID__/g' \
    "$names_a" | sort > "$skeleton_a"
  sed -E \
    -e 's/__[0-9]+/__ID/g' \
    -e 's/\$eval[0-9]+/\$evalID/g' \
    -e 's/\$inst_[0-9]+__/\$inst_ID__/g' \
    "$expected_root/$namespace_name.txt" | sort > "$skeleton_original"
  if diff -u "$skeleton_original" "$skeleton_a" >/dev/null; then name_skeleton=MATCH; fi
  namespace_prefix=$(printf '%s' "$namespace_name" | tr '.' '/' | tr '-' '_')
  if [[ -f "$destination_a/${namespace_prefix}__init.class" && \
        -f "$destination_b/${namespace_prefix}__init.class" ]]; then
    initializer=true
  fi
  if [[ "$classes_a" != "$expected_classes" || "$classes_b" != "$expected_classes" || \
        "$deterministic_paths" != true || \
        "$name_skeleton" != MATCH || "$initializer" != true ]]; then
    status=FAIL
    overall_status=1
  fi
  printf '%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\n' \
    "$namespace_name" "$scope" "$expected_classes" "$classes_a" "$classes_b" \
    "$deterministic_paths" "$deterministic_bytes" "$name_skeleton" "$initializer" "$status" \
    >> "$relation"
done < "$cohort"

comparator_status=FAIL
if "$java_bin" -Xmx2G -cp "$output_abs/comparator-classes:$asm_jar" \
    CompareExactSourceAot \
    "$output_abs/classes-a" "$output_abs/classes-b" "$original_jar" \
    "$cohort" "$class_ownership" "$output_abs/evidence/normalized-comparator" \
    > "$output_abs/evidence/normalized-comparator.stdout" \
    2> "$output_abs/evidence/normalized-comparator.stderr"; then
  comparator_status=PASS
else
  overall_status=1
fi

runtime_input_post_manifest="$output_abs/evidence/runtime-inputs-post.sha256"
while IFS= read -r runtime_input; do
  sha256sum "$runtime_input"
done < "$runtime_input_paths" > "$runtime_input_post_manifest"
if ! diff -u "$runtime_input_manifest" "$runtime_input_post_manifest" \
    > "$output_abs/evidence/runtime-input-mutation.diff"; then
  echo "runtime inputs changed during exact-source AOT validation" >&2
  exit 1
fi
runtime_input_post_manifest_sha=$(sha256sum "$runtime_input_post_manifest" | awk '{print $1}')

passed=$(awk -F '\t' 'NR > 1 && $10 == "PASS" {count++} END {print count + 0}' "$relation")
failed=$(awk -F '\t' 'NR > 1 && $10 != "PASS" {count++} END {print count + 0}' "$relation")
cardinality_matches=$(awk -F '\t' 'NR > 1 && $3 == $4 && $3 == $5 {count++} END {print count + 0}' "$relation")
path_matches=$(awk -F '\t' 'NR > 1 && $6 == "true" {count++} END {print count + 0}' "$relation")
byte_matches=$(awk -F '\t' 'NR > 1 && $7 == "true" {count++} END {print count + 0}' "$relation")
skeleton_matches=$(awk -F '\t' 'NR > 1 && $8 == "MATCH" {count++} END {print count + 0}' "$relation")
initializer_matches=$(awk -F '\t' 'NR > 1 && $9 == "true" {count++} END {print count + 0}' "$relation")
compiled_classes_a=$(awk -F '\t' 'NR > 1 {count += $4} END {print count + 0}' "$relation")
compiled_classes_b=$(awk -F '\t' 'NR > 1 {count += $5} END {print count + 0}' "$relation")
summary="$output_abs/evidence/summary.tsv"
{
  printf 'metric\tvalue\n'
  printf 'input.transactor.sha256\t%s\n' "$actual_original_sha"
  printf 'compiler.runtime\tAmazon Corretto 11.0.22.7.1\n'
  printf 'compiler.options\t{:elide-meta [:doc :file :line]}\n'
  printf 'compiler.process_model\tone namespace per fresh JVM; declared dependencies preloaded without AOT emission\n'
  printf 'compiler.original_transactor_peer_core2_aot\texcluded\n'
  printf 'compiler.nano_impl_input\tdeterministic-sanitized-derivative\n'
  printf 'compiler.nano_impl.sha256\t%s\n' "$actual_nano_sha"
  printf 'runtime_inputs.count\t%s\n' "$(wc -l < "$runtime_input_paths")"
  printf 'runtime_inputs.pre.sha256\t%s\n' "$runtime_input_manifest_sha"
  printf 'runtime_inputs.post.sha256\t%s\n' "$runtime_input_post_manifest_sha"
  printf 'runtime_inputs.unchanged\ttrue\n'
  printf 'compile.dependencies\t%s\n' "$dependency_count"
  printf 'namespaces.expected\t%s\n' "$expected_namespace_count"
  printf 'namespaces.passed\t%s\n' "$passed"
  printf 'namespaces.failed\t%s\n' "$failed"
  printf 'classes.expected\t%s\n' "$expected_class_count"
  printf 'classes.build_a\t%s\n' "$compiled_classes_a"
  printf 'classes.build_b\t%s\n' "$compiled_classes_b"
  printf 'cardinality.matches\t%s/%s\n' "$cardinality_matches" "$expected_namespace_count"
  printf 'deterministic.class_paths\t%s/%s\n' "$path_matches" "$expected_namespace_count"
  printf 'deterministic.class_bytes\t%s/%s\n' "$byte_matches" "$expected_namespace_count"
  printf 'original.name_skeleton.matches\t%s/%s\n' "$skeleton_matches" "$expected_namespace_count"
  printf 'initializer.matches\t%s/%s\n' "$initializer_matches" "$expected_namespace_count"
  printf 'original.normalized_non_debug_executable_model\t%s\n' "$comparator_status"
  printf 'original.normalized_model_scope\tcomplete non-debug ABI/instructions; stack-map frames and max stack/locals are verifier-derived and excluded\n'
  printf 'original.raw_class_equivalence\tNOT_REQUIRED-compiler-ids-are-build-order-dependent\n'
  printf 'runtime.namespace_surface_equivalence\tNOT_RUN-by-this-gate\n'
  printf 'compiler_id_scope\tper-fresh-JVM namespace compile unit; cross-namespace owned references routed by exact class ownership\n'
  printf 'compiler_id_ambiguity_policy\tFAIL\n'
  printf 'semantic_string_constant_policy\tNEVER_NORMALIZED\n'
  printf 'proxy_hash_policy\tPRESERVED-and-compared-exactly\n'
} > "$summary"

output_manifest="$output_abs/evidence/output-manifest.sha256"
(cd "$output_abs" && find . -type f ! -path './evidence/output-manifest.sha256' -print0 | \
  sort -z | xargs -0 sha256sum) > "$output_manifest"
(cd "$output_abs" && sha256sum -c evidence/output-manifest.sha256 >/dev/null)

cat "$summary"
exit "$overall_status"
