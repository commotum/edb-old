#!/usr/bin/bash -p

set -euo pipefail
umask 077
unset BASH_ENV ENV CDPATH GLOBIGNORE JAVA_TOOL_OPTIONS JDK_JAVA_OPTIONS \
  _JAVA_OPTIONS CLASSPATH JAVA_HOME LD_PRELOAD LD_LIBRARY_PATH
IFS=$' \t\n'
export LC_ALL=C TZ=UTC
PATH=${DATOMIC_REV_SAFE_PATH:-/usr/lib/jvm/java-21-openjdk-amd64/bin:/usr/bin:/bin}
export PATH

die() {
  echo "Peer runtime classpath audit: $*" >&2
  exit 1
}

[[ $# -eq 12 ]] || die "usage: audit-peer-runtime-classpath.sh LANE ACTUAL_CLASSPATH OUTPUT_ROOT DISTRIBUTION_TSV DATOMIC_HOME SOURCE_ROOT CLASSES_ROOT RESOURCES_ROOT STUB_ROOT LIBRARY_ROOT NAMESPACE_INDEX SANITIZED_NANO_JAR"

lane=$1
actual_classpath=$2
output_root=$3
distribution_tsv=$4
datomic_home=$5
source_root=$6
classes_root=$7
resources_root=$8
stub_root=$9
library_root=${10}
namespace_index=${11}
sanitized_nano_jar=${12}

expected_original_nano_sha=fbde8184da356bd3a9995a9f05f01493efe4baa87c4d48377cda3e53331807cd
expected_sanitized_nano_sha=08f9699d6e35b9e052ec85ee15e0896b3a685c74e6e226d8cd89d9c61dbf8d5f
expected_sanitized_nano_bytes=81218
forbidden_nano_key_sha=f3627f52580b84fe8f536423643fb46999fd2458498989307f2820d778eb73a1
forbidden_nano_trust_sha=ca64f839d051d909974a624b4345d83cc739a0b90edbe54e7c3605a2fd8fc13b

[[ "$lane" == original || "$lane" == candidate ]] || \
  die "lane must be original or candidate"
for required_file in "$actual_classpath" "$distribution_tsv" "$namespace_index"; do
  [[ -f "$required_file" && ! -L "$required_file" ]] || \
    die "required regular input is missing or symbolic: $required_file"
done
[[ -f "$sanitized_nano_jar" && ! -L "$sanitized_nano_jar" ]] || \
  die "sanitized Nano derivative is missing or symbolic: $sanitized_nano_jar"
for required_dir in "$datomic_home" "$source_root" "$classes_root" \
    "$resources_root" "$stub_root" "$library_root"; do
  [[ -d "$required_dir" && ! -L "$required_dir" ]] || \
    die "required input directory is missing or symbolic: $required_dir"
done
[[ ! -e "$output_root" ]] || die "output already exists: $output_root"
mkdir -p "$output_root/manifests" "$output_root/classes"

datomic_abs=$(realpath "$datomic_home")
source_abs=$(realpath "$source_root")
classes_abs=$(realpath "$classes_root")
resources_abs=$(realpath "$resources_root")
stub_abs=$(realpath "$stub_root")
library_abs=$(realpath "$library_root")
sanitized_nano_abs=$(realpath "$sanitized_nano_jar")
peer_jar="$datomic_abs/peer-1.0.7277.jar"
transactor_jar="$datomic_abs/datomic-transactor-pro-1.0.7277.jar"
core2_jar="$datomic_abs/lib/core2-1.0.140.jar"
original_nano_jar="$datomic_abs/lib/nano-impl-0.1.325.jar"

for licensed_jar in "$peer_jar" "$transactor_jar" "$core2_jar" \
    "$original_nano_jar"; do
  [[ -f "$licensed_jar" && ! -L "$licensed_jar" ]] || \
    die "licensed class-boundary input is missing or symbolic: $licensed_jar"
done

expected_distribution_sha() {
  local relative_path=$1
  awk -F '\t' -v path="$relative_path" \
    'NR > 1 && $2 == path {print $3}' "$distribution_tsv"
}

peer_sha=$(expected_distribution_sha peer-1.0.7277.jar)
transactor_sha=$(expected_distribution_sha datomic-transactor-pro-1.0.7277.jar)
core2_sha=$(expected_distribution_sha lib/core2-1.0.140.jar)
nano_sha=$(expected_distribution_sha lib/nano-impl-0.1.325.jar)
[[ -n "$peer_sha" && -n "$transactor_sha" && -n "$core2_sha" && \
   "$nano_sha" == "$expected_original_nano_sha" ]] || \
  die "licensed implementation hashes are absent from distribution inventory"
[[ $(sha256sum "$peer_jar" | awk '{print $1}') == "$peer_sha" ]] || \
  die "Peer oracle hash differs from distribution inventory"
[[ $(sha256sum "$transactor_jar" | awk '{print $1}') == "$transactor_sha" ]] || \
  die "Transactor oracle hash differs from distribution inventory"
[[ $(sha256sum "$core2_jar" | awk '{print $1}') == "$core2_sha" ]] || \
  die "core2 oracle hash differs from distribution inventory"
[[ $(sha256sum "$original_nano_jar" | awk '{print $1}') == "$nano_sha" ]] || \
  die "Nano original hash differs from distribution inventory"
[[ $(sha256sum "$sanitized_nano_abs" | awk '{print $1}') == \
   "$expected_sanitized_nano_sha" ]] || \
  die "sanitized Nano derivative hash mismatch"
[[ $(stat -c '%s' "$sanitized_nano_abs") == \
   "$expected_sanitized_nano_bytes" ]] || \
  die "sanitized Nano derivative byte-size mismatch"
[[ -z $(zipinfo -1 "$sanitized_nano_abs" \
  | grep -E '(^|/)(transactor-key|transactor-trust)\.jks$' || true) ]] || \
  die "sanitized Nano derivative retained a vendor keystore entry"

licensed_prefixes="$output_root/licensed-datomic-aot-prefixes.txt"
for archive in "$peer_jar" "$transactor_jar" "$core2_jar"; do
  zipinfo -1 "$archive"
done | sed -n 's/__init\.class$//p' | grep '^datomic/' | sort -u \
  >"$licensed_prefixes"
[[ -s "$licensed_prefixes" ]] || die "licensed Datomic AOT prefix ledger is empty"

dependency_prefix_owners="$output_root/distribution-dependency-datomic-aot-prefixes.tsv"
printf 'aot_prefix\tdistribution_path\tarchive_sha256\n' >"$dependency_prefix_owners"
while IFS=$'\t' read -r _ distribution_path distribution_sha _; do
  [[ "$distribution_path" == distribution_path ]] && continue
  [[ "$distribution_path" == lib/*.jar ]] || continue
  [[ "$distribution_sha" != "$core2_sha" ]] || continue
  dependency_archive="$datomic_abs/$distribution_path"
  while IFS= read -r dependency_prefix; do
    printf '%s\t%s\t%s\n' "$dependency_prefix" "$distribution_path" \
      "$distribution_sha" >>"$dependency_prefix_owners"
  done < <(zipinfo -1 "$dependency_archive" \
    | sed -n 's/__init\.class$//p' | grep '^datomic/' | sort -u || true)
done <"$distribution_tsv"
allowed_dependency_prefixes="$output_root/allowed-distribution-dependency-aot-prefixes.txt"
awk -F '\t' 'NR > 1 {print $1}' "$dependency_prefix_owners" | sort -u \
  >"$allowed_dependency_prefixes"
forbidden_prefixes="$output_root/forbidden-datomic-aot-prefixes.txt"
comm -23 "$licensed_prefixes" "$allowed_dependency_prefixes" \
  >"$forbidden_prefixes"
[[ -s "$forbidden_prefixes" ]] || die "proprietary Datomic AOT prefix ledger is empty"
peer_prefix_coverage="$output_root/peer-namespace-aot-prefix-coverage.tsv"
printf 'namespace\tsource_path\taot_prefix\tpresent_in_licensed_ledger\n' \
  >"$peer_prefix_coverage"
while IFS=$'\t' read -r namespace_name source_path _; do
  [[ "$namespace_name" == namespace ]] && continue
  aot_prefix=${source_path%.clj}
  prefix_present=false
  grep -Fqx "$aot_prefix" "$licensed_prefixes" && prefix_present=true
  printf '%s\t%s\t%s\t%s\n' "$namespace_name" "$source_path" \
    "$aot_prefix" "$prefix_present" >>"$peer_prefix_coverage"
  [[ "$prefix_present" == true ]] || \
    die "Peer namespace is missing from licensed AOT-prefix ledger: $namespace_name"
done <"$namespace_index"
[[ $(awk 'END {print NR - 1}' "$peer_prefix_coverage") -eq 142 ]] || \
  die "Peer namespace AOT-prefix coverage is not 142"

inventory="$output_root/$lane-classpath.tsv"
printf 'position\tdeclared_path\tresolved_path\ttype\trole\tsha256\tclass_entries\tforbidden_aot_entries\tdistribution_path\n' \
  >"$inventory"
seen="$output_root/$lane-resolved-paths.txt"
: >"$seen"

position=0
forbidden_total=0
sanitized_nano_count=0
original_nano_count=0
while IFS= read -r declared_path; do
  [[ -n "$declared_path" ]] || die "runtime classpath contains an empty entry"
  [[ "$declared_path" != *$'\t'* && "$declared_path" != *$'\n'* ]] || \
    die "runtime classpath contains an unrepresentable path"
  ((position += 1))
  [[ -e "$declared_path" && ! -L "$declared_path" ]] || {
    # JVM wildcard expansion preserves symlink paths for dependency JARs.
    [[ -L "$declared_path" && -e "$declared_path" ]] || \
      die "runtime classpath entry is missing: $declared_path"
  }
  resolved_path=$(realpath "$declared_path")
  if grep -Fqx "$resolved_path" "$seen"; then
    die "runtime classpath repeats a canonical origin: $resolved_path"
  fi
  printf '%s\n' "$resolved_path" >>"$seen"

  role=
  distribution_path=
  if [[ "$resolved_path" == "$source_abs" ]]; then
    role=candidate-source
  elif [[ "$resolved_path" == "$classes_abs" ]]; then
    role=recovered-handwritten-java
  elif [[ "$resolved_path" == "$resources_abs" ]]; then
    role=recovered-peer-resources
  elif [[ "$resolved_path" == "$stub_abs" ]]; then
    role=compile-only-stubs
  elif [[ "$resolved_path" == "$peer_jar" ]]; then
    role=licensed-peer-oracle
    distribution_path=peer-1.0.7277.jar
  elif [[ "$resolved_path" == "$sanitized_nano_abs" ]]; then
    role=sanitized-nano-dependency
    ((sanitized_nano_count += 1))
  elif [[ "$resolved_path" == "$datomic_abs/lib/"*.jar ]]; then
    role=distribution-dependency
    distribution_path="lib/${resolved_path##*/}"
  else
    die "runtime classpath origin is unclassified: $declared_path -> $resolved_path"
  fi

  class_list="$output_root/classes/$lane-$position.txt"
  entry_sha=
  entry_type=
  if [[ -d "$resolved_path" ]]; then
    entry_type=directory
    membership="$output_root/manifests/$lane-$position.membership.tsv"
    content_manifest="$output_root/manifests/$lane-$position.sha256"
    find "$resolved_path" -mindepth 1 -printf '%y\t%m\t%P\t%l\n' | sort \
      >"$membership"
    if [[ -n $(find "$resolved_path" -mindepth 1 ! -type f ! -type d ! -type l -print -quit) ]]; then
      die "classpath directory contains a special entry: $resolved_path"
    fi
    while IFS= read -r symlink_path; do
      [[ -e "$symlink_path" ]] || die "classpath directory has a broken symlink: $symlink_path"
    done < <(find "$resolved_path" -type l | sort)
    find -L "$resolved_path" -type f -print0 | sort -z | xargs -0 sha256sum \
      >"$content_manifest"
    sha256sum "$membership" "$content_manifest" | awk '{print $1}' \
      | sha256sum | awk '{print $1}' \
      >"$output_root/manifests/$lane-$position.digest"
    entry_sha=$(<"$output_root/manifests/$lane-$position.digest")
    find -L "$resolved_path" -type f -name '*.class' -printf '%P\n' | sort -u \
      >"$class_list"
  elif [[ -f "$resolved_path" ]]; then
    entry_type=archive
    entry_sha=$(sha256sum "$resolved_path" | awk '{print $1}')
    zipinfo -1 "$resolved_path" | grep '\.class$' | sort -u >"$class_list" || true
  else
    die "runtime classpath entry is neither directory nor regular file: $resolved_path"
  fi

  if [[ -n "$distribution_path" ]]; then
    expected_sha=$(expected_distribution_sha "$distribution_path")
    [[ -n "$expected_sha" ]] || \
      die "distribution classpath entry is absent from inventory: $distribution_path"
    [[ "$entry_sha" == "$expected_sha" ]] || \
      die "distribution classpath hash mismatch: $distribution_path"
  fi

  if [[ "$lane" == candidate ]]; then
    [[ "$entry_sha" != "$peer_sha" ]] || die "candidate classpath contains licensed Peer archive content"
    [[ "$entry_sha" != "$transactor_sha" ]] || die "candidate classpath contains licensed Transactor archive content"
    [[ "$entry_sha" != "$core2_sha" ]] || die "candidate classpath contains licensed core2 archive content"
    [[ "$entry_sha" != "$nano_sha" ]] || die "candidate classpath contains the licensed keystore-bearing Nano archive content"
    [[ "$role" != licensed-peer-oracle ]] || die "candidate classpath resolves to the licensed Peer oracle"
  fi
  [[ "$entry_sha" != "$forbidden_nano_key_sha" && \
     "$entry_sha" != "$forbidden_nano_trust_sha" ]] || \
    die "classpath contains a raw vendor Nano keystore payload"
  [[ "$entry_sha" != "$nano_sha" ]] || ((original_nano_count += 1))

  if [[ "$entry_type" == archive ]]; then
    forbidden_nano_entries="$output_root/classes/$lane-$position.forbidden-nano-entries.txt"
    zipinfo -1 "$resolved_path" \
      | grep -E '(^|/)(transactor-key|transactor-trust)\.jks$' \
      >"$forbidden_nano_entries" || true
    if [[ "$lane" == candidate && -s "$forbidden_nano_entries" ]]; then
      die "candidate classpath archive exposes a vendor Nano keystore entry: $resolved_path"
    fi
  elif [[ "$lane" == candidate ]]; then
    forbidden_nano_files="$output_root/classes/$lane-$position.forbidden-nano-files.txt"
    find -L "$resolved_path" -type f \
      \( -name 'transactor-key.jks' -o -name 'transactor-trust.jks' \) \
      -print | sort >"$forbidden_nano_files"
    [[ ! -s "$forbidden_nano_files" ]] || \
      die "candidate classpath directory exposes a vendor Nano keystore file: $resolved_path"
    content_manifest_forbidden_hits=$(grep -Ec \
      "^($forbidden_nano_key_sha|$forbidden_nano_trust_sha)  " \
      "$content_manifest" || true)
    [[ "$content_manifest_forbidden_hits" -eq 0 ]] || \
      die "candidate classpath directory contains a renamed vendor Nano keystore payload"
    forbidden_archive_payloads="$output_root/classes/$lane-$position.forbidden-archive-payloads.txt"
    awk -v peer="$peer_sha" -v transactor="$transactor_sha" \
        -v core2="$core2_sha" -v nano="$nano_sha" \
        '$1 == peer || $1 == transactor || $1 == core2 || $1 == nano' \
        "$content_manifest" >"$forbidden_archive_payloads"
    [[ ! -s "$forbidden_archive_payloads" ]] || \
      die "candidate classpath directory contains a copied or renamed licensed implementation archive"
    unexpected_archives="$output_root/classes/$lane-$position.unexpected-archives.txt"
    : >"$unexpected_archives"
    while IFS= read -r -d '' candidate_file; do
      if zipinfo -1 "$candidate_file" >/dev/null 2>&1; then
        printf '%s\n' "$candidate_file" >>"$unexpected_archives"
      fi
    done < <(find -L "$resolved_path" -type f -print0 | sort -z)
    [[ ! -s "$unexpected_archives" ]] || \
      die "candidate-owned classpath directory contains an unexpected nested archive"
  fi

  forbidden_file="$output_root/classes/$lane-$position.forbidden.txt"
  comparison_prefixes=$forbidden_prefixes
  if [[ "$lane" == candidate && "$role" != distribution-dependency && \
        "$role" != sanitized-nano-dependency ]]; then
    comparison_prefixes=$licensed_prefixes
  fi
  awk 'NR == FNR {prefix[$0] = 1; next}
       {
         for (p in prefix) {
           if ($0 == p "__init.class" || index($0, p "$") == 1) {
             print $0
             break
           }
         }
       }' "$comparison_prefixes" "$class_list" | sort -u >"$forbidden_file"
  class_count=$(wc -l <"$class_list")
  forbidden_count=$(wc -l <"$forbidden_file")
  forbidden_total=$((forbidden_total + forbidden_count))
  if [[ "$lane" == candidate && "$forbidden_count" -ne 0 ]]; then
    die "candidate classpath exposes licensed Datomic Clojure AOT entries in $resolved_path"
  fi

  if [[ "$lane" == candidate && "$position" -eq 1 && "$role" != candidate-source ]]; then
    die "candidate source root is not first on the actual JVM classpath"
  fi
  if [[ "$lane" == original && "$position" -eq 1 && "$role" != licensed-peer-oracle ]]; then
    die "licensed Peer oracle is not first on the original JVM classpath"
  fi

  printf '%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\n' \
    "$position" "$declared_path" "$resolved_path" "$entry_type" "$role" \
    "$entry_sha" "$class_count" "$forbidden_count" "$distribution_path" \
    >>"$inventory"
done <"$actual_classpath"

[[ "$position" -gt 0 ]] || die "actual runtime classpath inventory is empty"
if [[ "$lane" == candidate ]]; then
  [[ "$forbidden_total" -eq 0 ]] || die "candidate classpath AOT exclusion total is nonzero"
  for required_role in candidate-source recovered-handwritten-java \
      recovered-peer-resources compile-only-stubs distribution-dependency \
      sanitized-nano-dependency; do
    grep -q $'\t'"$required_role"$'\t' "$inventory" || \
      die "candidate classpath is missing role: $required_role"
  done
  [[ "$sanitized_nano_count" -eq 1 ]] || \
    die "candidate classpath must contain exactly one sanitized Nano derivative"
  [[ "$original_nano_count" -eq 0 ]] || \
    die "candidate classpath contains the licensed Nano original"
fi

{
  printf 'lane=%s\n' "$lane"
  printf 'actual.classpath.entries=%s\n' "$position"
  printf 'licensed.datomic.aot.prefixes=%s\n' "$(wc -l <"$licensed_prefixes")"
  printf 'allowed.distribution.dependency.aot.prefixes=%s\n' \
    "$(wc -l <"$allowed_dependency_prefixes")"
  printf 'proprietary.datomic.aot.prefixes=%s\n' \
    "$(wc -l <"$forbidden_prefixes")"
  printf 'forbidden.aot.entries=%s\n' "$forbidden_total"
  printf 'canonical.origins.unique=true\n'
  printf 'origin.and.hash.inventory.complete=true\n'
  printf 'sanitized.nano.derivative.sha256=%s\n' \
    "$expected_sanitized_nano_sha"
  printf 'sanitized.nano.derivative.entries=%s\n' "$sanitized_nano_count"
  printf 'licensed.nano.original.entries=%s\n' "$original_nano_count"
  if [[ "$lane" == candidate ]]; then
    printf 'licensed.peer.archive.present=false\n'
    printf 'licensed.transactor.archive.present=false\n'
    printf 'licensed.core2.archive.present=false\n'
    printf 'licensed.nano.original.present=false\n'
    printf 'licensed.implementation.archive-payload.present=false\n'
    printf 'candidate.directory.nested-archive.present=false\n'
    printf 'vendor.nano.keystore.payload.present=false\n'
    printf 'licensed.datomic.clojure.aot.present=false\n'
  fi
} >"$output_root/$lane-summary.properties"
