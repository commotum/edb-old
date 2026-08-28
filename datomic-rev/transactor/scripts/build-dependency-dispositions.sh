#!/usr/bin/env bash
set -euo pipefail

export LC_ALL=C
export TZ=UTC

script_dir=$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd)
transactor_dir=$(cd -- "$script_dir/.." && pwd)
output_dir=${1:-/tmp/datomic-goal2-dependency-dispositions}

distribution="$transactor_dir/baseline/distribution-jars.tsv"
pom="$transactor_dir/baseline/pom-dependencies.tsv"
candidate="$transactor_dir/reports/stage-1-candidate-dependencies.tsv"
bundled="$transactor_dir/reports/stage-1-bundled-source-ownership.tsv"
datomic="$transactor_dir/reports/stage-1-datomic-dependency-source-ownership.tsv"

expected_distribution_sha=db25519069bfd327c161220264b89fb011f2d9a3e42e4857e5233d4b61811860
expected_pom_sha=50eeca64244495b4cccc615c8488279dae7ae36dff7634d83716d1ba950117d1
expected_candidate_sha=2a60a620248948eb8e8f338e6d9a3e7100c03ce0837a020009686cb158913df5
expected_bundled_sha=01926ddaf34277f6bf69cce1284e6098b4adffc5ada940e02a721f0c89b3455f
expected_datomic_sha=04e98999d62b6b19b0a10ae7cce385bf45dc7f9fa0bdc18c3020ac46a00ab0fe
expected_sanitized_nano_sha=08f9699d6e35b9e052ec85ee15e0896b3a685c74e6e226d8cd89d9c61dbf8d5f

for command_name in awk find mkdir realpath sha256sum sort; do
  command -v "$command_name" >/dev/null || {
    echo "missing required command: $command_name" >&2
    exit 1
  }
done

for input in "$distribution" "$pom" "$candidate" "$bundled" "$datomic"; do
  [[ -f "$input" && ! -L "$input" ]] || {
    echo "missing or symbolic disposition input: $input" >&2
    exit 1
  }
done

verify_hash() {
  local path=$1
  local expected=$2
  local actual
  actual=$(sha256sum "$path" | awk '{print $1}')
  [[ "$actual" == "$expected" ]] || {
    echo "input hash mismatch for $path: $actual" >&2
    exit 1
  }
}

verify_hash "$distribution" "$expected_distribution_sha"
verify_hash "$pom" "$expected_pom_sha"
verify_hash "$candidate" "$expected_candidate_sha"
verify_hash "$bundled" "$expected_bundled_sha"
verify_hash "$datomic" "$expected_datomic_sha"

output_abs=$(realpath -m "$output_dir")
if [[ -e "$output_abs" ]] && \
   [[ -n $(find "$output_abs" -mindepth 1 -print -quit 2>/dev/null) ]]; then
  echo "refusing to overwrite non-empty output directory: $output_abs" >&2
  exit 1
fi
[[ ! -L "$output_abs" ]] || {
  echo "output directory may not be a symbolic link: $output_abs" >&2
  exit 1
}
mkdir -p "$output_abs"

disposition="$output_abs/dependency-dispositions.tsv"
awk -F '\t' -v OFS='\t' \
    -v candidate_file="$candidate" \
    -v bundled_file="$bundled" \
    -v datomic_file="$datomic" \
    -v distribution_file="$distribution" \
    -v sanitized_nano_sha="$expected_sanitized_nano_sha" '
  function scope(jar, lower) {
    lower = tolower(jar)
    if (jar == "core2-1.0.140.jar") return "recovered-peer-core2-support"
    if (jar == "nano-impl-0.1.325.jar") return "transactor-extension"
    if (lower ~ /^postgresql-/) return "postgresql-primary"
    if (lower ~ /artemis|hornetq/) return "peer-transactor-transport"
    if (lower ~ /lucene/) return "index-and-fulltext"
    if (lower ~ /cassandra|datastax/) return "alternative-cassandra-deferred"
    if (lower ~ /infinispan|hotrod/) return "alternative-hotrod-deferred"
    if (lower ~ /memcache|spymemcached|couchbase/) return "optional-cache-deferred"
    if (lower ~ /^h2-/) return "h2-development-deferred"
    if (lower ~ /aws|amazon|dynamo|cloudformation|kinesis/) return "aws-and-alternative-storage-deferred"
    if (lower ~ /jetty|ring|hiccup|liberator|moustache|servlet/) return "http-and-optional-access"
    if (lower ~ /logback|slf4j|metrics|hdrhistogram|jmx/) return "operations-and-observability"
    return "shared-runtime-support"
  }

  FILENAME == candidate_file {
    if (FNR == 1) next
    candidate_count++
    key = ($3 == "distribution" ? $4 : "derived:" $4)
    if (key in candidate_role) {
      print "duplicate candidate key: " key > "/dev/stderr"
      failed = 1
    }
    candidate_role[key] = $2
    candidate_sha[key] = $6
    next
  }

  FILENAME == bundled_file || FILENAME == datomic_file {
    if (FNR == 1 || $6 == "") next
    count = split($6, owners, ";")
    for (owner_index = 1; owner_index <= count; owner_index++) {
      if (owners[owner_index] != "") source_namespaces[owners[owner_index]]++
    }
    next
  }

  FILENAME == distribution_file {
    if (FNR == 1 || $12 != "dependency") next
    dependency_count++
    jar = $1
    path = $2
    original_sha = $3
    pom_relation = ($10 == "true" ? "direct-pom" : "transitive-or-packaged")
    later_scope = scope(jar)

    if (jar == "core2-1.0.140.jar") {
      action = "EXCLUDE_ORIGINAL_RECOVER_SOURCE"
      artifact = "recovered Peer core2 source"
      replacement_sha = "n/a"
      reason = "original core2 AOT excluded; 25 non-Transactor support namespaces come from the recovered Peer source boundary"
      core2_count++
    } else if (jar == "nano-impl-0.1.325.jar") {
      key = "derived:nano-impl-0.1.325-sanitized.jar"
      action = "SANITIZE_AND_REPLACE"
      artifact = "nano-impl-0.1.325-sanitized.jar"
      replacement_sha = candidate_sha[key]
      reason = "JKS-bearing original replaced by the deterministic derivative with both forbidden payloads removed"
      if (!(key in candidate_role) || replacement_sha != sanitized_nano_sha) {
        print "missing or unexpected sanitized nano candidate" > "/dev/stderr"
        failed = 1
      }
      used[key] = 1
      nano_count++
    } else {
      key = path
      action = "RETAIN_HASH_PINNED_STAGE1"
      artifact = path
      replacement_sha = candidate_sha[key]
      reason = "retained for complete-corpus structural compatibility; necessity is re-evaluated at the named later-stage scope"
      if (!(key in candidate_role) || replacement_sha != original_sha) {
        print "candidate relation failed for " path > "/dev/stderr"
        failed = 1
      }
      used[key] = 1
      retained_count++
    }

    print dependency_count, jar, path, original_sha, pom_relation,
          source_namespaces[path] + 0, action, artifact, replacement_sha,
          later_scope, reason
    next
  }

  END {
    for (key in candidate_role) {
      if (!(key in used)) {
        print "unaccounted candidate key: " key > "/dev/stderr"
        failed = 1
      }
    }
    if (dependency_count != 533 || candidate_count != 532 ||
        retained_count != 531 || core2_count != 1 || nano_count != 1) {
      print "unexpected disposition cardinality" > "/dev/stderr"
      failed = 1
    }
    if (failed) exit 2
  }
' "$candidate" "$bundled" "$datomic" "$distribution" \
  >"$output_abs/disposition-rows.tsv"

{
  printf 'ordinal\tjar\tdistribution_path\toriginal_sha256\tpom_relation\trecovered_source_namespaces\tstage1_disposition\tcandidate_artifact\tcandidate_sha256\tlater_scope\tdecision_reason\n'
  cat "$output_abs/disposition-rows.tsv"
} >"$disposition"
rm "$output_abs/disposition-rows.tsv"

rows=$(awk 'END {print NR - 1}' "$disposition")
retained=$(awk -F '\t' '$7 == "RETAIN_HASH_PINNED_STAGE1" {count++} END {print count + 0}' "$disposition")
replaced=$(awk -F '\t' '$7 == "SANITIZE_AND_REPLACE" {count++} END {print count + 0}' "$disposition")
excluded=$(awk -F '\t' '$7 == "EXCLUDE_ORIGINAL_RECOVER_SOURCE" {count++} END {print count + 0}' "$disposition")
source_providers=$(awk -F '\t' 'NR > 1 && $6 > 0 {count++} END {print count + 0}' "$disposition")
source_namespaces=$(awk -F '\t' 'NR > 1 {count += $6} END {print count + 0}' "$disposition")

[[ "$rows" == 533 && "$retained" == 531 && "$replaced" == 1 && \
   "$excluded" == 1 && "$source_providers" == 24 && \
   "$source_namespaces" == 87 ]] || {
  echo "generated disposition counts are inconsistent" >&2
  exit 1
}

{
  printf 'metric\tvalue\n'
  printf 'distribution.dependencies\t%s\n' "$rows"
  printf 'candidate.artifacts\t%s\n' "$((retained + replaced))"
  printf 'disposition.retain_hash_pinned_stage1\t%s\n' "$retained"
  printf 'disposition.sanitize_and_replace\t%s\n' "$replaced"
  printf 'disposition.exclude_original_recover_source\t%s\n' "$excluded"
  printf 'exact_source_provider_jars\t%s\n' "$source_providers"
  printf 'exact_source_namespaces\t%s\n' "$source_namespaces"
  printf 'unaccounted.dependencies\t0\n'
  printf 'postgresql.priority\tprimary before alternative backends\n'
  printf 'stage1.retention_claim\tstructural compatibility, not final runtime necessity\n'
} >"$output_abs/summary.tsv"

(
  cd "$transactor_dir"
  sha256sum \
    scripts/build-dependency-dispositions.sh \
    baseline/distribution-jars.tsv \
    baseline/pom-dependencies.tsv \
    reports/stage-1-candidate-dependencies.tsv \
    reports/stage-1-bundled-source-ownership.tsv \
    reports/stage-1-datomic-dependency-source-ownership.tsv
) >"$output_abs/inputs.sha256"
(
  cd "$output_abs"
  sha256sum dependency-dispositions.tsv inputs.sha256 summary.tsv
) >"$output_abs/manifest.sha256"
(cd "$output_abs" && sha256sum -c manifest.sha256 >/dev/null)

echo "dependency dispositions passed: $rows/533"
echo "retained=$retained sanitized=$replaced recovered/excluded=$excluded"
echo "output: $output_abs"
