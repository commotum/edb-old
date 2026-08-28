#!/usr/bin/env bash
set -euo pipefail

export LC_ALL=C
export TZ=UTC
export AWS_EC2_METADATA_DISABLED=true

script_dir=$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd)
transactor_dir=$(cd -- "$script_dir/.." && pwd)
project_dir=$(cd -- "$transactor_dir/.." && pwd)
datomic_home=${1:-"$project_dir/../../datomic/datomic-pro-1.0.7277"}
output_dir=${2:-/tmp/datomic-transactor-structural-load}
namespace_timeout=${STRUCTURAL_NAMESPACE_TIMEOUT_SECONDS:-120}
jobs=${STRUCTURAL_JOBS:-4}
discovery_only=${STRUCTURAL_DISCOVERY_ONLY:-0}
run_surfaces=${STRUCTURAL_RUN_SURFACES:-1}

scanner_source="$transactor_dir/tools/ScanStructuralClasspath.java"
namespace_baseline="$transactor_dir/baseline/transactor-namespace-inits.tsv"
class_baseline="$transactor_dir/baseline/transactor-classes.tsv"
distribution_baseline="$transactor_dir/baseline/distribution-jars.tsv"
archive_inputs="$transactor_dir/baseline/archive-inputs.tsv"
peer_namespace_index="$project_dir/reports/source-index/namespaces.tsv"
transactor_source_root="$transactor_dir/src-clj"
transactor_source_manifest="$transactor_dir/reports/stage-1-clojure-source-manifest.sha256"
peer_source_root="$project_dir/src-clj"
java_source_root="$transactor_dir/src-java"
java_source_manifest="$transactor_dir/reports/stage-1-java-source-manifest.sha256"
java_class_list="$transactor_dir/reports/stage-1-java-class-list.txt"
java_code_relation="$transactor_dir/reports/stage-1-java-code-relation.tsv"
java_compile_classpath="$transactor_dir/reports/stage-1-java-compile-classpath.tsv"
java_validation_relation="$transactor_dir/reports/stage-1-java-validation-relation.tsv"
java_validation_summary="$transactor_dir/reports/stage-1-java-validation-summary.tsv"
java_validator="$script_dir/validate-java.sh"
origin_verifier="$script_dir/verify_structural_origins.clj"
namespace_runner="$script_dir/run_structural_namespace.sh"
peer_surface_emitter="$project_dir/scripts/emit_namespace_surface.clj"
surface_protocol_validator="$script_dir/validate-structural-surface-protocol.sh"
runtime_seal_validator="$script_dir/validate-structural-runtime-seal-fixtures.sh"
surface_valid_fixture="$transactor_dir/fixtures/surface-valid-with-process-log.clj"
surface_trailing_fixture="$transactor_dir/fixtures/surface-trailing-form.clj"
decompiler_runtime_jar="$project_dir/tools/tools.decompiler/target/tools.decompiler-0.1.0-alpha1-standalone.jar"
stub_builder="$project_dir/tools/infinispan-compile-stubs/build_stubs.clj"
nano_sanitizer="$script_dir/sanitize-nano-impl.sh"
resource_stager="$script_dir/stage-structural-resources.sh"
candidate_resource_templates="$transactor_dir/candidate-resources"
resource_classification="$transactor_dir/reports/stage-1-resource-classification.tsv"
compatibility_target=1.0.7277

original_transactor="$datomic_home/datomic-transactor-pro-1.0.7277.jar"
original_peer="$datomic_home/peer-1.0.7277.jar"
original_core2="$datomic_home/lib/core2-1.0.140.jar"
expected_transactor_sha=d90819b57e2138085ddc26c93314f953c2cd776d1d1aa8b43a6bec1e475d3692
expected_peer_sha=cb55c9d01e155f9965e3332cd8ef70be42cdefacb50c072076e9ecf22b9a23ba
expected_core2_sha=81fdf81586c7be1a4b61655b568348d12db7892cb4562d0db7529bbc7af8a94b
expected_sanitized_nano_sha=08f9699d6e35b9e052ec85ee15e0896b3a685c74e6e226d8cd89d9c61dbf8d5f
expected_transactor_source_manifest_sha=48389f0d223a789e5abebdc8d6b4466848062642ed9473c63453323f1671f106
expected_namespace_baseline_sha=f57dfc78dbe5556e45b7a1fc02d7d6c3c1c0f9565754f38ab92305010e329cf7
expected_class_baseline_sha=6e044398c4a9cab91873a08f7404f0acb4b9d5f6e91c5329e36d01ffeacc4c76
expected_distribution_baseline_sha=db25519069bfd327c161220264b89fb011f2d9a3e42e4857e5233d4b61811860
expected_archive_inputs_sha=7848a25b771b0abd8c953a4577578a0457aeeb3acd66f5daa066516489a9e4b0
expected_peer_namespace_index_sha=3cc2a0bb61ca79221d54c507e74bcb8e3e31147e90f97157002b857d67d353e1
expected_java_source_manifest_sha=f48edbd567b53316b6e20cd1b2bc889f71a9c45eee3a6576cacf454a20da7f5f
expected_java_class_list_sha=c5fe736882eed01ca09dfc9e7d7de6eb7999ae2cfa1431ff0e39f716702a8c2b
expected_resource_classification_sha=a0005b405646621cb102af214b907d737b412458358875fd7fcd5e10d63e2d35
expected_clojure_compiler_vmarg="-Dclojure.compiler.elide-meta='[:doc :file :line]'"

die() {
  echo "structural-load validation: $*" >&2
  exit 1
}

for command_name in awk cmp cp date diff find grep head mkdir realpath sha256sum sort tail timeout unzip xargs; do
  command -v "$command_name" >/dev/null || die "missing required command: $command_name"
done

[[ "$namespace_timeout" =~ ^[1-9][0-9]*$ ]] || die "timeout must be a positive integer"
[[ "$jobs" =~ ^[1-9][0-9]*$ ]] || die "job count must be a positive integer"
[[ "$discovery_only" == 0 || "$discovery_only" == 1 ]] || \
  die "STRUCTURAL_DISCOVERY_ONLY must be 0 or 1"
[[ "$run_surfaces" == 0 || "$run_surfaces" == 1 ]] || \
  die "STRUCTURAL_RUN_SURFACES must be 0 or 1"

for required_file in \
  "$scanner_source" "$namespace_baseline" "$class_baseline" \
  "$distribution_baseline" "$archive_inputs" "$peer_namespace_index" \
  "$transactor_source_manifest" "$java_source_manifest" "$java_class_list" \
  "$java_code_relation" "$java_compile_classpath" \
  "$java_validation_relation" "$java_validation_summary" \
  "$java_validator" "$origin_verifier" \
  "$namespace_runner" "$peer_surface_emitter" "$surface_protocol_validator" \
  "$runtime_seal_validator" \
  "$surface_valid_fixture" "$surface_trailing_fixture" "$decompiler_runtime_jar" \
  "$stub_builder" "$nano_sanitizer" \
  "$resource_stager" "$resource_classification" \
  "$candidate_resource_templates/data_readers.clj" \
  "$candidate_resource_templates/datomic/aws/instance-arch.edn" \
  "$candidate_resource_templates/datomic/aws/region-arch-ami.edn" \
  "$original_transactor" "$original_peer" "$original_core2"; do
  [[ -f "$required_file" ]] || die "required file not found: $required_file"
done
for required_dir in "$transactor_source_root" "$peer_source_root" "$java_source_root"; do
  [[ -d "$required_dir" ]] || die "required directory not found: $required_dir"
done

project_abs=$(realpath "$project_dir")
datomic_home_abs=$(realpath "$datomic_home")
output_abs=$(realpath -m "$output_dir")
[[ "$datomic_home_abs" != "$project_abs" && "$datomic_home_abs" != "$project_abs"/* ]] || \
  die "licensed distribution must remain outside the repository: $datomic_home_abs"
[[ "$output_abs" != "$project_abs" && "$output_abs" != "$project_abs"/* ]] || \
  die "runtime build and detailed evidence must remain outside the repository: $output_abs"
if [[ -e "$output_abs" ]] && [[ -n $(find "$output_abs" -mindepth 1 -print -quit) ]]; then
  die "refusing to overwrite non-empty output directory: $output_abs"
fi
mkdir -p "$output_abs/discovery" "$output_abs/evidence" \
  "$output_abs/logs" "$output_abs/results" "$output_abs/process-cwd"

verify_hash() {
  local path=$1
  local expected=$2
  local label=$3
  local actual
  actual=$(sha256sum "$path" | awk '{print $1}')
  [[ "$actual" == "$expected" ]] || \
    die "$label hash mismatch: expected $expected, found $actual"
}

write_top_evidence_manifest() {
  # Cover every emitted file: compiled Java/scanner classes, staged Peer core2
  # sources, candidate resources, Hot Rod stubs, logs, results, and all nested
  # evidence manifests.  Only this manifest excludes itself.
  (cd "$output_abs" && \
    find . -type f ! -path './evidence/manifest.sha256' -print0 \
      | sort -z | xargs -0 sha256sum) >"$output_abs/evidence/manifest.sha256"
  (cd "$output_abs" && sha256sum -c evidence/manifest.sha256) >/dev/null
}

verify_hash "$original_transactor" "$expected_transactor_sha" "licensed Transactor"
verify_hash "$original_peer" "$expected_peer_sha" "licensed Peer"
verify_hash "$original_core2" "$expected_core2_sha" "licensed core2"
transactor_embedded_pom=$(unzip -p "$original_transactor" \
  META-INF/maven/com.datomic/datomic-transactor-pro/pom.xml) || \
  die "could not read the licensed Transactor's embedded Maven POM"
[[ "$transactor_embedded_pom" == *"$expected_clojure_compiler_vmarg"* ]] || \
  die "licensed Transactor POM does not bind the required Clojure compiler metadata elision"
verify_hash "$transactor_source_manifest" "$expected_transactor_source_manifest_sha" \
  "canonical Transactor source manifest"
verify_hash "$namespace_baseline" "$expected_namespace_baseline_sha" \
  "Transactor namespace baseline"
verify_hash "$class_baseline" "$expected_class_baseline_sha" \
  "Transactor class baseline"
verify_hash "$distribution_baseline" "$expected_distribution_baseline_sha" \
  "distribution JAR baseline"
verify_hash "$archive_inputs" "$expected_archive_inputs_sha" \
  "licensed archive-input baseline"
verify_hash "$peer_namespace_index" "$expected_peer_namespace_index_sha" \
  "Peer namespace index"
verify_hash "$java_source_manifest" "$expected_java_source_manifest_sha" \
  "canonical Transactor Java source manifest"
verify_hash "$java_class_list" "$expected_java_class_list_sha" \
  "canonical Transactor Java class list"
verify_hash "$resource_classification" "$expected_resource_classification_sha" \
  "Stage 1 resource classification"
[[ $(awk 'NF {count++} END {print count + 0}' "$transactor_source_manifest") == 247 ]] || \
  die "canonical Transactor source manifest is not exactly 247 rows"
(cd "$transactor_source_root" && \
  sha256sum -c "$transactor_source_manifest") >/dev/null || \
  die "canonical Transactor sources do not match their checked-in manifest"

{
  printf 'role\tpath\texpected_sha256\tactual_sha256\tstatus\n'
  for frozen_row in \
    "transactor-source-manifest|$transactor_source_manifest|$expected_transactor_source_manifest_sha" \
    "namespace-baseline|$namespace_baseline|$expected_namespace_baseline_sha" \
    "class-baseline|$class_baseline|$expected_class_baseline_sha" \
    "distribution-baseline|$distribution_baseline|$expected_distribution_baseline_sha" \
    "archive-inputs|$archive_inputs|$expected_archive_inputs_sha" \
    "peer-namespace-index|$peer_namespace_index|$expected_peer_namespace_index_sha" \
    "java-source-manifest|$java_source_manifest|$expected_java_source_manifest_sha" \
    "java-class-list|$java_class_list|$expected_java_class_list_sha" \
    "resource-classification|$resource_classification|$expected_resource_classification_sha"; do
    frozen_role=${frozen_row%%|*}
    frozen_remainder=${frozen_row#*|}
    frozen_path=${frozen_remainder%%|*}
    frozen_expected=${frozen_remainder##*|}
    frozen_actual=$(sha256sum "$frozen_path" | awk '{print $1}')
    frozen_status=FAIL
    [[ "$frozen_actual" == "$frozen_expected" ]] && frozen_status=PASS
    printf '%s\t%s\t%s\t%s\t%s\n' "$frozen_role" \
      "${frozen_path#$project_dir/}" "$frozen_expected" "$frozen_actual" "$frozen_status"
  done
} >"$output_abs/evidence/frozen-inputs.tsv"
[[ $(awk -F '\t' 'NR > 1 && $5 == "PASS" {count++} END {print count + 0}' \
  "$output_abs/evidence/frozen-inputs.tsv") == 9 ]] || \
  die "one or more frozen structural inputs failed binding"

frozen_snapshot_root="$output_abs/evidence/frozen-input-snapshots"
mkdir -p "$frozen_snapshot_root"
cp -- "$transactor_source_manifest" \
  "$frozen_snapshot_root/transactor-source-manifest.sha256"
cp -- "$namespace_baseline" "$frozen_snapshot_root/transactor-namespace-inits.tsv"
cp -- "$class_baseline" "$frozen_snapshot_root/transactor-classes.tsv"
cp -- "$distribution_baseline" "$frozen_snapshot_root/distribution-jars.tsv"
cp -- "$archive_inputs" "$frozen_snapshot_root/archive-inputs.tsv"
cp -- "$peer_namespace_index" "$frozen_snapshot_root/peer-namespaces.tsv"
cp -- "$java_source_manifest" "$frozen_snapshot_root/java-source-manifest.sha256"
cp -- "$java_class_list" "$frozen_snapshot_root/java-class-list.txt"
cp -- "$resource_classification" "$frozen_snapshot_root/resource-classification.tsv"
[[ $(find "$frozen_snapshot_root" -type f | awk 'END {print NR + 0}') == 9 ]] || \
  die "frozen input snapshot is incomplete"

# The baseline itself independently binds the two top-level licensed inputs.
awk -F '\t' -v transactor_sha="$expected_transactor_sha" -v peer_sha="$expected_peer_sha" '
  NR == 1 {next}
  $1 == "transactor" && $3 == transactor_sha {transactor = 1}
  $1 == "peer" && $3 == peer_sha {peer = 1}
  END {exit !(transactor && peer)}
' "$archive_inputs" || die "archive input baseline does not bind the expected originals"

corretto_root=/tmp/amazon-corretto-11.0.22.7.1
javac_bin=${DATOMIC_JAVA_JAVAC:-"$corretto_root/bin/javac"}
java_bin=$(dirname "$javac_bin")/java
[[ -x "$javac_bin" && -x "$java_bin" ]] || \
  die "Amazon Corretto 11.0.22.7.1 is required at $corretto_root or DATOMIC_JAVA_JAVAC"
[[ $("$javac_bin" -version 2>&1) == "javac 11.0.22" ]] || \
  die "exact javac 11.0.22 is required"
"$java_bin" -version 2>&1 | grep -q 'Corretto-11.0.22.7.1' || \
  die "exact Amazon Corretto 11.0.22.7.1 runtime is required"

surface_protocol_output="$output_abs/surface-protocol-validation"
if ! "$surface_protocol_validator" "$java_bin" "$decompiler_runtime_jar" \
  "$surface_protocol_output" >"$output_abs/evidence/surface-protocol-validation.log" 2>&1; then
  printf 'status=FAIL\nphase=surface-protocol-validation\n' \
    >"$output_abs/evidence/summary.properties"
  die "dedicated surface protocol fixtures failed"
fi
grep -qx 'status=PASS' "$surface_protocol_output/summary.properties" || \
  die "dedicated surface protocol fixtures did not emit PASS"

runtime_seal_output="$output_abs/runtime-seal-validation"
if ! "$runtime_seal_validator" "$runtime_seal_output" \
  >"$output_abs/evidence/runtime-seal-validation.log" 2>&1; then
  printf 'status=FAIL\nphase=runtime-seal-validation\n' \
    >"$output_abs/evidence/summary.properties"
  die "runtime directory-membership negative fixtures failed"
fi
grep -qx 'status=PASS' "$runtime_seal_output/summary.properties" || \
  die "runtime directory-membership negative fixtures did not emit PASS"

sanitized_nano_output="$output_abs/sanitized-nano"
if ! "$nano_sanitizer" "$datomic_home_abs" "$sanitized_nano_output" \
  >"$output_abs/evidence/nano-sanitization.log" 2>&1; then
  printf 'status=FAIL\nphase=nano-sanitization\n' >"$output_abs/evidence/summary.properties"
  die "nano-impl sanitization failed; see $output_abs/evidence/nano-sanitization.log"
fi
sanitized_nano="$sanitized_nano_output/nano-impl-0.1.325-sanitized.jar"
verify_hash "$sanitized_nano" "$expected_sanitized_nano_sha" \
  "sanitized nano-impl derivative"

resource_build="$output_abs/candidate-resource-build"
if ! "$resource_stager" "$candidate_resource_templates" "$resource_build" \
  "$compatibility_target" >"$output_abs/evidence/resource-staging.log" 2>&1; then
  printf 'status=FAIL\nphase=candidate-resource-staging\n' \
    >"$output_abs/evidence/summary.properties"
  die "candidate-owned resource staging failed; see $output_abs/evidence/resource-staging.log"
fi
grep -qx 'status=PASS' "$resource_build/evidence/summary.properties" || \
  die "candidate-owned resource staging did not emit PASS"
resource_root="$resource_build/runtime"

mkdir -p "$output_abs/scanner-classes"
"$javac_bin" -g:none -source 11 -target 11 -encoding UTF-8 -proc:none \
  -d "$output_abs/scanner-classes" "$scanner_source"
"$java_bin" -cp "$output_abs/scanner-classes" ScanStructuralClasspath \
  "$namespace_baseline" "$class_baseline" "$peer_namespace_index" \
  "$distribution_baseline" "$datomic_home_abs" "$transactor_source_root" \
  "$peer_source_root" "$java_source_root" "$java_class_list" \
  "$sanitized_nano" "$output_abs/discovery" \
  >"$output_abs/evidence/discovery.log" 2>&1 || {
    printf 'status=FAIL\nphase=classpath-discovery\n' >"$output_abs/evidence/summary.properties"
    die "classpath discovery failed; see $output_abs/evidence/discovery.log"
  }
grep -qx 'status=PASS' "$output_abs/discovery/summary.properties" || \
  die "classpath discovery did not emit PASS"

{
  printf 'input\tsha256\n'
  for tool in "$script_dir/validate-structural-load.sh" \
    "$scanner_source" "$origin_verifier" "$namespace_runner" \
    "$script_dir/require_structural_namespace.clj" \
    "$script_dir/emit_structural_surface.clj" "$peer_surface_emitter" \
    "$surface_protocol_validator" "$surface_valid_fixture" \
    "$surface_trailing_fixture" "$runtime_seal_validator" \
    "$decompiler_runtime_jar" \
    "$stub_builder" "$nano_sanitizer" "$java_validator" "$resource_stager" \
    "$namespace_baseline" "$class_baseline" "$distribution_baseline" \
    "$archive_inputs" "$peer_namespace_index" "$transactor_source_manifest" \
    "$java_source_manifest" "$java_class_list" "$java_code_relation" \
    "$java_compile_classpath" "$java_validation_relation" \
    "$java_validation_summary" "$resource_classification" \
    "$candidate_resource_templates/data_readers.clj" \
    "$candidate_resource_templates/datomic/aws/instance-arch.edn" \
    "$candidate_resource_templates/datomic/aws/region-arch-ami.edn"; do
    printf '%s\t%s\n' "${tool#$project_dir/}" "$(sha256sum "$tool" | awk '{print $1}')"
  done
} >"$output_abs/evidence/tool-inputs.tsv"

if [[ "$discovery_only" == 1 ]]; then
  {
    printf 'status=PASS\n'
    printf 'completion=discovery-only\n'
    printf 'loads.executed=false\n'
    printf 'surfaces.executed=false\n'
    printf 'candidate.runtime.resource.count=4\n'
    printf 'transactor.embedded.pom.clojure.compiler.elide-meta=[:doc :file :line]\n'
    printf 'licensed.reference.bytes.copied=false\n'
    printf 'aws.provisioning.behavior.validated=false\n'
    printf 'aws.resource.mode=non-production-empty-deferral\n'
  } >"$output_abs/evidence/summary.properties"
  write_top_evidence_manifest
  echo "Transactor structural classpath discovery passed: $output_abs"
  echo "No namespace load or surface comparison was requested (STRUCTURAL_DISCOVERY_ONLY=1)."
  exit 0
fi

candidate_dependency_cp=
sanitized_nano_rows=0
while IFS=$'\t' read -r candidate_ordinal distribution_ordinal jar artifact_path \
  expected_sha source_distribution_path substitution; do
  [[ "$candidate_ordinal" == candidate_ordinal ]] && continue
  dependency=$artifact_path
  [[ -f "$dependency" ]] || die "candidate dependency disappeared: $dependency"
  verify_hash "$dependency" "$expected_sha" "candidate dependency $jar"
  if [[ "$substitution" == sanitized-nano-impl ]]; then
    sanitized_nano_rows=$((sanitized_nano_rows + 1))
    [[ "$dependency" == "$sanitized_nano" ]] || \
      die "sanitized nano manifest path does not name the validated derivative"
  elif [[ "$substitution" != none ]]; then
    die "unknown candidate dependency substitution: $substitution"
  fi
  if [[ -z "$candidate_dependency_cp" ]]; then
    candidate_dependency_cp=$dependency
  else
    candidate_dependency_cp="$candidate_dependency_cp:$dependency"
  fi
done <"$output_abs/discovery/ordered-candidate-dependencies.tsv"
[[ $(awk 'END {print NR - 1}' "$output_abs/discovery/ordered-candidate-dependencies.tsv") == 532 ]] || \
  die "candidate dependency manifest is not 532 rows"
[[ "$sanitized_nano_rows" == 1 ]] || \
  die "candidate dependency manifest does not contain exactly one sanitized nano derivative"

oracle_dependency_cp=
while IFS=$'\t' read -r distribution_ordinal jar relative_path expected_sha candidate_allowed; do
  [[ "$distribution_ordinal" == distribution_ordinal ]] && continue
  dependency="$datomic_home_abs/$relative_path"
  [[ -f "$dependency" ]] || die "oracle dependency disappeared: $dependency"
  verify_hash "$dependency" "$expected_sha" "oracle dependency $jar"
  if [[ -z "$oracle_dependency_cp" ]]; then
    oracle_dependency_cp=$dependency
  else
    oracle_dependency_cp="$oracle_dependency_cp:$dependency"
  fi
done <"$output_abs/discovery/ordered-oracle-dependencies.tsv"
[[ $(awk 'END {print NR - 1}' "$output_abs/discovery/ordered-oracle-dependencies.tsv") == 533 ]] || \
  die "oracle dependency manifest is not 533 rows"

java_output="$output_abs/java-validation"
if ! DATOMIC_JAVA_JAVAC="$javac_bin" "$java_validator" "$datomic_home_abs" \
  "$java_output" >"$output_abs/evidence/java-validation.log" 2>&1; then
  printf 'status=FAIL\nphase=java-validation\n' >"$output_abs/evidence/summary.properties"
  die "the 46-source/52-class Java recovery gate failed; see $output_abs/evidence/java-validation.log"
fi
java_class_root="$java_output/classes-a"
[[ $(find "$java_class_root" -type f -name '*.class' | awk 'END {print NR + 0}') == 52 ]] || \
  die "validated Java output does not contain exactly 52 classes"

support_source_root="$output_abs/candidate-peer-core2-src"
mkdir -p "$support_source_root"
peer_core2_input_manifest="$output_abs/evidence/peer-core2-source-inputs.sha256"
awk -F '\t' '$1 == "peer-core2" {print $4 "  " $3}' \
  "$output_abs/discovery/candidate-sources.tsv" >"$peer_core2_input_manifest"
[[ $(awk 'NF {count++} END {print count + 0}' "$peer_core2_input_manifest") == 25 ]] || \
  die "Peer core2 source input manifest is not exactly 25 rows"
(cd "$peer_source_root" && sha256sum -c "$peer_core2_input_manifest") >/dev/null || \
  die "the 25 Peer core2 source inputs changed after discovery"
support_count=0
while IFS=$'\t' read -r owner namespace source_entry source_sha initializer_namespace; do
  [[ "$owner" == owner ]] && continue
  [[ "$owner" == peer-core2 ]] || continue
  source_file="$peer_source_root/$source_entry"
  verify_hash "$source_file" "$source_sha" "Peer core2 support source $namespace"
  destination="$support_source_root/$source_entry"
  mkdir -p "$(dirname "$destination")"
  cp -- "$source_file" "$destination"
  support_count=$((support_count + 1))
done <"$output_abs/discovery/candidate-sources.tsv"
[[ "$support_count" == 25 ]] || die "staged Peer core2 support count is $support_count"
[[ $(find "$support_source_root" -type f \( -name '*.clj' -o -name '*.cljc' \) \
  | awk 'END {print NR + 0}') == 25 ]] || die "support source root contains more than 25 files"
(cd "$support_source_root" && sha256sum -c "$peer_core2_input_manifest") >/dev/null || \
  die "staged Peer core2 sources differ from their bound inputs"

stub_root="$output_abs/hotrod-load-stubs"
mkdir -p "$stub_root"
if ! "$java_bin" -cp "$candidate_dependency_cp" clojure.main "$stub_builder" \
  "$stub_root" >"$output_abs/evidence/stub-build.log" 2>&1; then
  printf 'status=FAIL\nphase=stub-build\n' >"$output_abs/evidence/summary.properties"
  die "Hot Rod compile/load-only stub generation failed"
fi
[[ $(find "$stub_root" -type f -name '*.class' | awk 'END {print NR + 0}') == 4 ]] || \
  die "Hot Rod stub output is not exactly four classes"
(cd "$stub_root" && find . -type f -name '*.class' -print0 | sort -z | xargs -0 sha256sum) \
  >"$output_abs/evidence/hotrod-stubs.sha256"

# Seal every declared repository, distribution, staged, and generated runtime
# input.  The manifest itself is hash-bound in memory, then rechecked after the
# load batch and again after the surface batch so a mid-run change cannot be
# hidden by a final evidence manifest.
runtime_input_paths="$output_abs/evidence/runtime-input-paths.txt"
runtime_input_manifest="$output_abs/evidence/runtime-inputs.sha256"
{
  find "$transactor_source_root" "$java_source_root" "$support_source_root" \
    "$java_output" "$resource_root" "$stub_root" "$sanitized_nano_output" \
    "$output_abs/discovery" -type f -print
  awk -F '\t' 'NR > 1 {print $4}' \
    "$output_abs/discovery/ordered-candidate-dependencies.tsv"
  awk -F '\t' -v root="$datomic_home_abs" 'NR > 1 {print root "/" $3}' \
    "$output_abs/discovery/ordered-oracle-dependencies.tsv"
  awk -F '\t' -v root="$peer_source_root" \
    '$1 == "peer-core2" {print root "/" $3}' \
    "$output_abs/discovery/candidate-sources.tsv"
  for runtime_input in \
    "$script_dir/validate-structural-load.sh" \
    "$original_transactor" "$original_peer" "$original_core2" \
    "$javac_bin" "$java_bin" "$scanner_source" "$origin_verifier" \
    "$namespace_runner" "$script_dir/require_structural_namespace.clj" \
    "$script_dir/emit_structural_surface.clj" "$peer_surface_emitter" \
    "$surface_protocol_validator" "$surface_valid_fixture" \
    "$surface_trailing_fixture" "$decompiler_runtime_jar" \
    "$stub_builder" "$nano_sanitizer" "$java_validator" "$resource_stager" \
    "$namespace_baseline" "$class_baseline" "$distribution_baseline" \
    "$archive_inputs" "$peer_namespace_index" "$transactor_source_manifest" \
    "$java_source_manifest" "$java_class_list" "$java_code_relation" \
    "$java_compile_classpath" "$java_validation_relation" \
    "$java_validation_summary" "$resource_classification" \
    "$candidate_resource_templates/data_readers.clj" \
    "$candidate_resource_templates/datomic/aws/instance-arch.edn" \
    "$candidate_resource_templates/datomic/aws/region-arch-ami.edn"; do
    printf '%s\n' "$runtime_input"
  done
} | sort -u >"$runtime_input_paths"

while IFS= read -r runtime_input; do
  [[ -f "$runtime_input" ]] || die "runtime input disappeared before sealing: $runtime_input"
  sha256sum -- "$runtime_input"
done <"$runtime_input_paths" >"$runtime_input_manifest"
runtime_input_count=$(awk 'NF {count++} END {print count + 0}' "$runtime_input_manifest")
runtime_input_manifest_sha=$(sha256sum "$runtime_input_manifest" | awk '{print $1}')
sha256sum -c "$runtime_input_manifest" >/dev/null || \
  die "initial runtime-input manifest verification failed"

write_runtime_directory_membership() {
  local destination=$1
  {
    printf 'role\ttype\trelative_path\n'
    {
      for membership_row in \
        "transactor-source|$transactor_source_root" \
        "peer-core2-source|$support_source_root" \
        "transactor-java-classes|$java_class_root" \
        "candidate-resources|$resource_root" \
        "hotrod-stubs|$stub_root"; do
        membership_role=${membership_row%%|*}
        membership_root=${membership_row#*|}
        [[ -d "$membership_root" && ! -L "$membership_root" ]] || \
          die "declared classpath directory is missing or a symlink: $membership_root"
        if [[ -n $(find "$membership_root" -mindepth 1 \
          ! -type f ! -type d -print -quit) ]]; then
          die "declared classpath directory contains a symlink or special entry: $membership_root"
        fi
        find "$membership_root" -mindepth 1 \( -type d -o -type f \) \
          -printf "$membership_role\t%y\t%P\n"
      done
    } | sort
  } >"$destination"
}

runtime_directory_membership="$output_abs/evidence/runtime-directory-membership.tsv"
write_runtime_directory_membership "$runtime_directory_membership"
runtime_directory_membership_count=$(awk 'END {print NR - 1}' \
  "$runtime_directory_membership")
runtime_directory_membership_sha=$(sha256sum "$runtime_directory_membership" \
  | awk '{print $1}')

write_expected_process_cwd_membership() {
  local phase=$1
  local destination=$2
  {
    printf 'require-candidate\n'
    while IFS= read -r namespace_name; do
      printf 'require-candidate/%s\n' \
        "${namespace_name//[^A-Za-z0-9_.-]/_}"
    done <"$all_namespaces"
    printf 'require-topological\n'
    while IFS= read -r namespace_name; do
      printf 'require-topological/%s\n' \
        "${namespace_name//[^A-Za-z0-9_.-]/_}"
    done <"$topological_namespaces"
    if [[ "$phase" == post-surfaces ]]; then
      for surface_mode in surface-oracle surface-candidate; do
        printf '%s\n' "$surface_mode"
        while IFS= read -r namespace_name; do
          printf '%s/%s\n' "$surface_mode" \
            "${namespace_name//[^A-Za-z0-9_.-]/_}"
        done <"$output_abs/discovery/transactor-namespaces.txt"
      done
    fi
  } | sort -u >"$destination"
}

verify_runtime_inputs_unchanged() {
  local phase=$1
  local actual_manifest_sha
  local verification_log="$output_abs/evidence/runtime-input-verification-$phase.log"
  local phase_membership="$output_abs/evidence/runtime-directory-membership-$phase.tsv"
  local actual_process_cwd_membership="$output_abs/evidence/process-cwd-membership-$phase.txt"
  local expected_process_cwd_membership="$output_abs/evidence/process-cwd-expected-$phase.txt"
  actual_manifest_sha=$(sha256sum "$runtime_input_manifest" | awk '{print $1}')
  [[ "$actual_manifest_sha" == "$runtime_input_manifest_sha" ]] || \
    die "runtime-input manifest itself changed during $phase"
  if ! sha256sum -c "$runtime_input_manifest" >"$verification_log" 2>&1; then
    printf 'status=FAIL\nphase=runtime-input-recheck-%s\n' "$phase" \
      >"$output_abs/evidence/summary.properties"
    die "a sealed runtime input changed during $phase; see $verification_log"
  fi
  [[ $(sha256sum "$runtime_directory_membership" | awk '{print $1}') == \
      "$runtime_directory_membership_sha" ]] || \
    die "runtime directory-membership seal itself changed during $phase"
  write_runtime_directory_membership "$phase_membership"
  cmp -s "$runtime_directory_membership" "$phase_membership" || {
    diff -u "$runtime_directory_membership" "$phase_membership" \
      >"$output_abs/evidence/runtime-directory-membership-$phase.diff" || true
    printf 'status=FAIL\nphase=runtime-directory-membership-%s\n' "$phase" \
      >"$output_abs/evidence/summary.properties"
    die "declared classpath directory membership changed during $phase"
  }
  [[ -d "$output_abs/process-cwd" && ! -L "$output_abs/process-cwd" ]] || \
    die "isolated process working-directory root is missing or a symlink"
  if [[ -n $(find "$output_abs/process-cwd" -mindepth 1 ! -type d -print -quit) ]] || \
     [[ -n $(find "$output_abs/process-cwd" -mindepth 3 -print -quit) ]]; then
    printf 'status=FAIL\nphase=process-cwd-dirty-%s\n' "$phase" \
      >"$output_abs/evidence/summary.properties"
    die "an isolated namespace process wrote into its working directory during $phase"
  fi
  find "$output_abs/process-cwd" -mindepth 1 -maxdepth 2 -type d \
    -printf '%P\n' | sort \
    >"$actual_process_cwd_membership"
  write_expected_process_cwd_membership "$phase" \
    "$expected_process_cwd_membership"
  cmp -s "$expected_process_cwd_membership" \
    "$actual_process_cwd_membership" || {
    diff -u "$expected_process_cwd_membership" \
      "$actual_process_cwd_membership" \
      >"$output_abs/evidence/process-cwd-membership-$phase.diff" || true
    printf 'status=FAIL\nphase=process-cwd-membership-%s\n' "$phase" \
      >"$output_abs/evidence/summary.properties"
    die "isolated namespace process working-directory membership differs during $phase"
  }
  printf 'RUNTIME_INPUTS_UNCHANGED %s %s\n' "$phase" "$runtime_input_count" \
    >>"$verification_log"
  printf 'RUNTIME_DIRECTORY_MEMBERSHIP_UNCHANGED %s %s\n' "$phase" \
    "$runtime_directory_membership_count" >>"$verification_log"
  printf 'PROCESS_CWD_PROBE_DIRECTORIES_EMPTY %s\n' "$phase" \
    >>"$verification_log"
}

candidate_classpath="$transactor_source_root:$support_source_root:$java_class_root:$resource_root:$stub_root:$candidate_dependency_cp"
oracle_classpath="$original_transactor:$stub_root:$oracle_dependency_cp"

{
  printf 'ordinal\trole\tpath\n'
  ordinal=0
  for row in \
    "transactor-source:$transactor_source_root" \
    "peer-core2-support-source:$support_source_root" \
    "transactor-java:$java_class_root" \
    "candidate-resources:$resource_root" \
    "hotrod-load-stubs:$stub_root"; do
    ordinal=$((ordinal + 1))
    printf '%s\t%s\t%s\n' "$ordinal" "${row%%:*}" "${row#*:}"
  done
  while IFS=$'\t' read -r candidate_ordinal distribution_ordinal jar artifact_path \
    expected_sha source_distribution_path substitution; do
    [[ "$candidate_ordinal" == candidate_ordinal ]] && continue
    ordinal=$((ordinal + 1))
    printf '%s\tdependency:%s\t%s\n' "$ordinal" "$jar" "$artifact_path"
  done <"$output_abs/discovery/ordered-candidate-dependencies.tsv"
} >"$output_abs/evidence/candidate-classpath.tsv"

if ! "$java_bin" -Xms64m -Xmx2g -Djava.awt.headless=true -Duser.timezone=UTC \
  -Dcom.amazonaws.sdk.disableEc2Metadata=true -cp "$candidate_classpath" \
  clojure.main "$origin_verifier" "$transactor_source_root" "$support_source_root" \
  "$java_class_root" "$resource_root" "$stub_root" \
  "$output_abs/discovery/candidate-sources.tsv" \
  "$output_abs/discovery/java-classes.txt" \
  "$output_abs/discovery/ordered-candidate-dependencies.tsv" \
  "$output_abs/discovery/dependency-class-duplicates.tsv" "$datomic_home_abs" \
  >"$output_abs/evidence/origin-validation.log" 2>&1; then
  printf 'status=FAIL\nphase=origin-validation\n' >"$output_abs/evidence/summary.properties"
  die "candidate-origin gate failed; see $output_abs/evidence/origin-validation.log"
fi
grep -qx 'STRUCTURAL_ORIGIN_PASS' "$output_abs/evidence/origin-validation.log" || \
  die "candidate-origin gate did not emit its success marker"

export STRUCTURAL_JAVA="$java_bin"
export STRUCTURAL_TIMEOUT_SECONDS="$namespace_timeout"
export STRUCTURAL_CANDIDATE_CLASSPATH="$candidate_classpath"
export STRUCTURAL_ORACLE_CLASSPATH="$oracle_classpath"
export STRUCTURAL_LOG_ROOT="$output_abs/logs"
export STRUCTURAL_RESULT_ROOT="$output_abs/results"
export STRUCTURAL_SCRIPT_DIR="$script_dir"
export STRUCTURAL_PEER_EMITTER="$peer_surface_emitter"
export STRUCTURAL_PROCESS_CWD="$output_abs/process-cwd"
export STRUCTURAL_BATCH_RECORD_ONLY=1

run_batch() {
  local mode=$1
  local namespace_file=$2
  xargs -r -P "$jobs" -n 1 "$namespace_runner" "$mode" <"$namespace_file"
}

aggregate_results() {
  local mode=$1
  local destination=$2
  {
    printf 'mode\tnamespace\tstatus\texit_code\tduration_millis\tprocess_stdout_sha256\tprocess_stderr_sha256\tsurface_sha256\n'
    find "$output_abs/results/$mode" -type f -name '*.tsv' -print0 \
      | sort -z | xargs -0 -r cat
  } >"$destination"
}

all_namespaces="$output_abs/all-candidate-namespaces.txt"
cp -- "$output_abs/discovery/transactor-namespaces.txt" "$all_namespaces"
awk 'NF' "$output_abs/discovery/peer-core2-namespaces.txt" >>"$all_namespaces"
[[ $(awk 'NF {count++} END {print count + 0}' "$all_namespaces") == 272 ]] || \
  die "candidate load list is not exactly 272 namespaces"

run_batch require-candidate "$all_namespaces"
aggregate_results require-candidate "$output_abs/evidence/namespace-loads.tsv"

# datomic.transactor-ext is authored as a child of datomic.transactor: the
# root requires datomic.crypto before loading the extension.  Preserve the
# cold-load result, but also prove that exact production ordering in its own
# bounded JVM rather than adding an unproven require to recovered source.
topological_namespaces="$output_abs/topological-namespaces.txt"
printf 'datomic.transactor-ext\n' >"$topological_namespaces"
run_batch require-topological "$topological_namespaces"
aggregate_results require-topological \
  "$output_abs/evidence/topological-namespace-loads.tsv"

load_rows=$(awk 'END {print NR - 1}' "$output_abs/evidence/namespace-loads.tsv")
cold_load_passes=$(awk -F '\t' 'NR > 1 && $3 == "PASS" {count++} END {print count + 0}' \
  "$output_abs/evidence/namespace-loads.tsv")
[[ "$load_rows" == 272 ]] || die "only $load_rows/272 namespace load results were recorded"
[[ $(awk 'END {print NR - 1}' "$output_abs/evidence/topological-namespace-loads.tsv") == 1 ]] || \
  die "topological namespace probe did not record exactly one result"

topological_result="$output_abs/results/require-topological/datomic.transactor-ext.tsv"
IFS=$'\t' read -r _ _ topological_status _ _ _ _ _ <"$topological_result"
effective_loads="$output_abs/evidence/effective-namespace-loads.tsv"
printf 'namespace\tcold_status\ttopological_root\ttopological_status\teffective_status\n' \
  >"$effective_loads"
while IFS= read -r namespace_name; do
  safe_name=${namespace_name//[^A-Za-z0-9_.-]/_}
  IFS=$'\t' read -r _ _ cold_status _ _ _ _ _ \
    <"$output_abs/results/require-candidate/$safe_name.tsv"
  root=
  ordered_status=NOT_APPLICABLE
  effective_status=$cold_status
  if [[ "$namespace_name" == datomic.transactor-ext ]]; then
    root=datomic.transactor
    ordered_status=$topological_status
    if [[ "$cold_status" != PASS && "$ordered_status" == PASS ]]; then
      effective_status=ORDER_DEPENDENT_PASS
    fi
  fi
  printf '%s\t%s\t%s\t%s\t%s\n' "$namespace_name" "$cold_status" "$root" \
    "$ordered_status" "$effective_status" >>"$effective_loads"
done <"$all_namespaces"
order_dependent_passes=$(awk -F '\t' \
  'NR > 1 && $5 == "ORDER_DEPENDENT_PASS" {count++} END {print count + 0}' \
  "$effective_loads")
effective_load_passes=$(awk -F '\t' \
  'NR > 1 && ($5 == "PASS" || $5 == "ORDER_DEPENDENT_PASS") {count++} END {print count + 0}' \
  "$effective_loads")
verify_runtime_inputs_unchanged post-load

if [[ "$run_surfaces" == 0 ]]; then
  {
    printf 'gate\tnamespace\tstatus\n'
    awk -F '\t' 'NR > 1 && $5 != "PASS" && $5 != "ORDER_DEPENDENT_PASS" {print "load\t" $1 "\t" $5}' \
      "$effective_loads"
  } >"$output_abs/evidence/failures.tsv"
  load_status=PASS
  [[ "$effective_load_passes" == 272 ]] || load_status=FAIL
  {
    printf 'status=%s\n' "$load_status"
    printf 'completion=structural-load-only\n'
    printf 'surfaces.executed=false\n'
    printf 'transactor.namespace.expected=247\n'
    printf 'peer.core2.support.namespace.expected=25\n'
    printf 'candidate.namespace.cold-load.pass=%s\n' "$cold_load_passes"
    printf 'candidate.namespace.order-dependent.pass=%s\n' "$order_dependent_passes"
    printf 'candidate.namespace.effective-load.pass=%s\n' "$effective_load_passes"
    printf 'candidate.namespace.load.total=%s\n' "$load_rows"
    printf 'topological.root=datomic.transactor\n'
    printf 'candidate.dependency.jar.count=532\n'
    printf 'dependency.duplicate.class.differing.count=21\n'
    printf 'original.peer.on.candidate.classpath=false\n'
    printf 'original.transactor.on.candidate.classpath=false\n'
    printf 'original.core2.aot.on.candidate.classpath=false\n'
    printf 'licensed.transactor.key-material.on.candidate.classpath=false\n'
    printf 'candidate.runtime.resource.count=4\n'
    printf 'transactor.embedded.pom.clojure.compiler.elide-meta=[:doc :file :line]\n'
    printf 'candidate.resource.origin.validation=PASS\n'
    printf 'licensed.reference.bytes.copied=false\n'
    printf 'aws.provisioning.behavior.validated=false\n'
    printf 'aws.resource.mode=non-production-empty-deferral\n'
    printf 'packaging.metadata=deferred\n'
    printf 'legal.notice=deferred-license-review\n'
    printf 'optional.ui.assets=deferred\n'
    printf 'tls.key.trust.generation=deferred-to-environment\n'
    printf 'surface.protocol.fixture.validation=PASS\n'
    printf 'runtime.seal.negative-fixture.validation=PASS\n'
    printf 'runtime.input.count=%s\n' "$runtime_input_count"
    printf 'runtime.input.manifest.sha256=%s\n' "$runtime_input_manifest_sha"
    printf 'runtime.directory.membership.count=%s\n' \
      "$runtime_directory_membership_count"
    printf 'runtime.directory.membership.sha256=%s\n' \
      "$runtime_directory_membership_sha"
    printf 'runtime.inputs.post-load=PASS\n'
    printf 'runtime.directory.membership.post-load=PASS\n'
    printf 'process.cwd.isolation=per-probe-directory\n'
    printf 'process.cwd.probe-directories-empty.post-load=PASS\n'
  } >"$output_abs/evidence/summary.properties"
  write_top_evidence_manifest
  if [[ "$load_status" != PASS ]]; then
    echo "Transactor structural load validation is incomplete: $output_abs" >&2
    echo "effective loads: $effective_load_passes/272 ($cold_load_passes cold, $order_dependent_passes order-dependent); exact blockers: $output_abs/evidence/failures.tsv" >&2
    exit 1
  fi
  echo "Transactor structural load validation passed: $output_abs"
  echo "272/272 effective namespace loads ($cold_load_passes cold, $order_dependent_passes order-dependent); surfaces not requested"
  exit 0
fi

run_batch surface-oracle "$output_abs/discovery/transactor-namespaces.txt"
run_batch surface-candidate "$output_abs/discovery/transactor-namespaces.txt"

aggregate_results surface-oracle "$output_abs/evidence/oracle-surfaces.tsv"
aggregate_results surface-candidate "$output_abs/evidence/candidate-surfaces.tsv"

oracle_surface_rows=$(awk 'END {print NR - 1}' "$output_abs/evidence/oracle-surfaces.tsv")
candidate_surface_rows=$(awk 'END {print NR - 1}' "$output_abs/evidence/candidate-surfaces.tsv")
[[ "$oracle_surface_rows" == 247 ]] || die "only $oracle_surface_rows/247 oracle surfaces were recorded"
[[ "$candidate_surface_rows" == 247 ]] || \
  die "only $candidate_surface_rows/247 candidate surfaces were recorded"

surface_relation="$output_abs/evidence/namespace-surface-relation.tsv"
printf 'namespace\toracle_status\tcandidate_status\toracle_sha256\tcandidate_sha256\trelation\n' \
  >"$surface_relation"
while IFS= read -r namespace_name; do
  safe_name=${namespace_name//[^A-Za-z0-9_.-]/_}
  oracle_result="$output_abs/results/surface-oracle/$safe_name.tsv"
  candidate_result="$output_abs/results/surface-candidate/$safe_name.tsv"
  IFS=$'\t' read -r _ _ oracle_status _ _ _ _ oracle_sha <"$oracle_result"
  IFS=$'\t' read -r _ _ candidate_status _ _ _ _ candidate_sha <"$candidate_result"
  relation=NOT_COMPARABLE
  if [[ "$oracle_status" == PASS && "$candidate_status" == PASS ]]; then
    if cmp -s "$output_abs/logs/surface-oracle/$safe_name.surface.edn" \
      "$output_abs/logs/surface-candidate/$safe_name.surface.edn"; then
      relation=MATCH
    else
      relation=DIFFER
    fi
  fi
  printf '%s\t%s\t%s\t%s\t%s\t%s\n' "$namespace_name" "$oracle_status" \
    "$candidate_status" "$oracle_sha" "$candidate_sha" "$relation" \
    >>"$surface_relation"
done <"$output_abs/discovery/transactor-namespaces.txt"

surface_matches=$(awk -F '\t' 'NR > 1 && $6 == "MATCH" {count++} END {print count + 0}' \
  "$surface_relation")
surface_differences=$(awk -F '\t' 'NR > 1 && $6 == "DIFFER" {count++} END {print count + 0}' \
  "$surface_relation")
surface_not_comparable=$(awk -F '\t' 'NR > 1 && $6 == "NOT_COMPARABLE" {count++} END {print count + 0}' \
  "$surface_relation")
verify_runtime_inputs_unchanged post-surfaces

{
  printf 'gate\tnamespace\tstatus\n'
  awk -F '\t' 'NR > 1 && $5 != "PASS" && $5 != "ORDER_DEPENDENT_PASS" {print "load\t" $1 "\t" $5}' \
    "$effective_loads"
  awk -F '\t' 'NR > 1 && $6 != "MATCH" {print "surface\t" $1 "\t" $6}' \
    "$surface_relation"
} >"$output_abs/evidence/failures.tsv"

overall_status=PASS
if [[ "$effective_load_passes" != 272 || "$surface_matches" != 247 ]]; then
  overall_status=FAIL
fi
{
  printf 'status=%s\n' "$overall_status"
  printf 'completion=full-structural-load-and-surface\n'
  printf 'transactor.namespace.expected=247\n'
  printf 'peer.core2.support.namespace.expected=25\n'
  printf 'candidate.namespace.cold-load.pass=%s\n' "$cold_load_passes"
  printf 'candidate.namespace.order-dependent.pass=%s\n' "$order_dependent_passes"
  printf 'candidate.namespace.effective-load.pass=%s\n' "$effective_load_passes"
  printf 'candidate.namespace.load.total=%s\n' "$load_rows"
  printf 'topological.root=datomic.transactor\n'
  printf 'transactor.surface.match=%s\n' "$surface_matches"
  printf 'transactor.surface.differ=%s\n' "$surface_differences"
  printf 'transactor.surface.not-comparable=%s\n' "$surface_not_comparable"
  printf 'surface.payload.channel=dedicated-validated-edn\n'
  printf 'process.stdout.excluded.from.surface.comparison=true\n'
  printf 'candidate.dependency.jar.count=532\n'
  printf 'dependency.duplicate.class.differing.count=21\n'
  printf 'original.peer.on.candidate.classpath=false\n'
  printf 'original.transactor.on.candidate.classpath=false\n'
  printf 'original.core2.aot.on.candidate.classpath=false\n'
  printf 'licensed.transactor.key-material.on.candidate.classpath=false\n'
  printf 'candidate.runtime.resource.count=4\n'
  printf 'transactor.embedded.pom.clojure.compiler.elide-meta=[:doc :file :line]\n'
  printf 'candidate.resource.origin.validation=PASS\n'
  printf 'licensed.reference.bytes.copied=false\n'
  printf 'aws.provisioning.behavior.validated=false\n'
  printf 'aws.resource.mode=non-production-empty-deferral\n'
  printf 'packaging.metadata=deferred\n'
  printf 'legal.notice=deferred-license-review\n'
  printf 'optional.ui.assets=deferred\n'
  printf 'tls.key.trust.generation=deferred-to-environment\n'
  printf 'surface.protocol.fixture.validation=PASS\n'
  printf 'runtime.seal.negative-fixture.validation=PASS\n'
  printf 'runtime.input.count=%s\n' "$runtime_input_count"
  printf 'runtime.input.manifest.sha256=%s\n' "$runtime_input_manifest_sha"
  printf 'runtime.directory.membership.count=%s\n' \
    "$runtime_directory_membership_count"
  printf 'runtime.directory.membership.sha256=%s\n' \
    "$runtime_directory_membership_sha"
  printf 'runtime.inputs.post-load=PASS\n'
  printf 'runtime.inputs.post-surfaces=PASS\n'
  printf 'runtime.directory.membership.post-load=PASS\n'
  printf 'runtime.directory.membership.post-surfaces=PASS\n'
  printf 'process.cwd.isolation=per-probe-directory\n'
  printf 'process.cwd.probe-directories-empty.post-load=PASS\n'
  printf 'process.cwd.probe-directories-empty.post-surfaces=PASS\n'
} >"$output_abs/evidence/summary.properties"

write_top_evidence_manifest

if [[ "$overall_status" != PASS ]]; then
  echo "Transactor structural validation is incomplete: $output_abs" >&2
  echo "effective loads: $effective_load_passes/272 ($cold_load_passes cold, $order_dependent_passes order-dependent); surfaces: $surface_matches/247 match, $surface_differences differ, $surface_not_comparable not comparable" >&2
  echo "Exact blockers: $output_abs/evidence/failures.tsv" >&2
  exit 1
fi

echo "Transactor structural validation passed: $output_abs"
echo "272/272 effective namespace loads ($cold_load_passes cold, $order_dependent_passes order-dependent); 247/247 Transactor surfaces match"
echo "Candidate classpath: 272 sources + 52 Java classes + four load-only stubs + 532 pinned dependencies"
