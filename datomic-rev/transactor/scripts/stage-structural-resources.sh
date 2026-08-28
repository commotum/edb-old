#!/usr/bin/env bash
set -euo pipefail

export LC_ALL=C
export TZ=UTC

script_dir=$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd)
transactor_dir=$(cd -- "$script_dir/.." && pwd)
project_dir=$(cd -- "$transactor_dir/.." && pwd)
template_root=${1:-"$transactor_dir/candidate-resources"}
output_dir=${2:-}
compatibility_target=${3:-1.0.7277}
classification="$transactor_dir/reports/stage-1-resource-classification.tsv"

die() {
  echo "candidate resource staging: $*" >&2
  exit 1
}

[[ -n "$output_dir" ]] || \
  die "usage: stage-structural-resources.sh TEMPLATE_ROOT OUTPUT_DIR COMPATIBILITY_TARGET"
[[ "$compatibility_target" == 1.0.7277 ]] || \
  die "this frozen structural gate only supports compatibility target 1.0.7277"

for command_name in awk cp find mkdir realpath sha256sum sort wc xargs; do
  command -v "$command_name" >/dev/null || die "missing required command: $command_name"
done

template_root_abs=$(realpath "$template_root")
project_abs=$(realpath "$project_dir")
output_abs=$(realpath -m "$output_dir")
[[ "$template_root_abs" == "$project_abs"/* ]] || \
  die "candidate templates must be repository-owned: $template_root_abs"
[[ "$output_abs" != "$project_abs" && "$output_abs" != "$project_abs"/* ]] || \
  die "staged runtime resources must remain outside the repository: $output_abs"
if [[ -e "$output_abs" ]] && [[ -n $(find "$output_abs" -mindepth 1 -print -quit) ]]; then
  die "refusing to overwrite non-empty output directory: $output_abs"
fi

data_readers_template="$template_root_abs/data_readers.clj"
instance_arch_template="$template_root_abs/datomic/aws/instance-arch.edn"
region_arch_template="$template_root_abs/datomic/aws/region-arch-ami.edn"
for required_file in "$data_readers_template" "$instance_arch_template" \
  "$region_arch_template" "$classification"; do
  [[ -f "$required_file" && ! -L "$required_file" ]] || \
    die "required regular, non-symlink input is missing: $required_file"
done

template_count=$(find "$template_root_abs" -type f | awk 'END {print NR + 0}')
[[ "$template_count" == 3 ]] || \
  die "candidate template boundary is exactly three files, found $template_count"

verify_hash() {
  local path=$1
  local expected=$2
  local label=$3
  local actual
  actual=$(sha256sum "$path" | awk '{print $1}')
  [[ "$actual" == "$expected" ]] || \
    die "$label hash mismatch: expected $expected, found $actual"
}

verify_hash "$data_readers_template" \
  d64c16f95d1a3f1f878c5ec1a46a9e4a404f43dd258415d43fb36fc151c2769d \
  "candidate data_readers.clj template"
verify_hash "$instance_arch_template" \
  bdb39ce49025a9c1c476bed81290efa10b5033752c2c5bbe3c14a963c0fa44d3 \
  "candidate AWS instance architecture deferral template"
verify_hash "$region_arch_template" \
  b9b6967a7f2b3aabe16db61cdcdfd678f88b5cbfd4e206d380bd47d8cba10d32 \
  "candidate AWS region/AMI deferral template"
verify_hash "$classification" \
  a0005b405646621cb102af214b907d737b412458358875fd7fcd5e10d63e2d35 \
  "Stage 1 resource classification"

runtime_root="$output_abs/runtime"
evidence_root="$output_abs/evidence"
mkdir -p "$runtime_root/datomic/aws" "$evidence_root"
cp -- "$data_readers_template" "$runtime_root/data_readers.clj"
cp -- "$instance_arch_template" "$runtime_root/datomic/aws/instance-arch.edn"
cp -- "$region_arch_template" "$runtime_root/datomic/aws/region-arch-ami.edn"
printf '%s\n' "$compatibility_target" >"$runtime_root/datomic/VERSION"
verify_hash "$runtime_root/datomic/VERSION" \
  b269dadae69f1d087cec1a37e53c32e5759b0d27df7a7c62833176f9bd012184 \
  "generated compatibility target"

runtime_count=$(find "$runtime_root" -type f | awk 'END {print NR + 0}')
[[ "$runtime_count" == 4 ]] || \
  die "candidate runtime resource boundary is exactly four files, found $runtime_count"
if [[ -n $(find "$runtime_root" -type f \( -iname '*.jks' \
  -o -name 'transactor-key.jks' -o -name 'transactor-trust.jks' \) -print -quit) ]]; then
  die "candidate runtime contains forbidden key or trust material"
fi

# These are evidence hashes from the licensed distribution.  No candidate
# runtime file may reproduce any of them, including the shared Peer copies.
reference_hashes="$evidence_root/licensed-reference-hash-policy.tsv"
{
  printf 'reference_entry\tsha256\tpolicy\n'
  printf 'data_readers.clj\td681bd3c1854dd6303b29565f18451645a7265d64710fb7d2ee4e6abd55c3fc0\tdeny-byte-copy\n'
  printf 'datomic/VERSION\t5b9f9a16f9249dd933274578f4deb26eeb208178c66077c6be373365d780fc5d\tdeny-byte-copy\n'
  printf 'datomic/aws/instance-arch.edn\ta0ce8dc0e131071523b6be446a8ea1ca91bc7085f1661b44b7d39b6e50750973\tdeny-byte-copy\n'
  printf 'datomic/aws/region-arch-ami.edn\t402a1c4343bdc7951d5806344e026c4e18fa10942a642b4f4b41ccda4f143cb5\tdeny-byte-copy\n'
  printf 'datomic/transactor-key.jks\tf3627f52580b84fe8f536423643fb46999fd2458498989307f2820d778eb73a1\tdeny-secret-byte-copy\n'
  printf 'datomic/transactor-trust.jks\tca64f839d051d909974a624b4345d83cc739a0b90edbe54e7c3605a2fd8fc13b\tdeny-secret-byte-copy\n'
} >"$reference_hashes"

runtime_hashes="$evidence_root/candidate-resources.tsv"
{
  printf 'entry\tprovenance\tmode\tbytes\tsha256\n'
  while IFS= read -r resource; do
    entry=${resource#"$runtime_root/"}
    case "$entry" in
      data_readers.clj)
        provenance=candidate-authored-template
        mode=runtime-registration
        ;;
      datomic/VERSION)
        provenance=generated-from-compatibility-target
        mode=compatibility-metadata
        ;;
      datomic/aws/instance-arch.edn|datomic/aws/region-arch-ami.edn)
        provenance=candidate-authored-template
        mode=non-production-empty-deferral
        ;;
      *) die "unexpected candidate runtime resource: $entry" ;;
    esac
    printf '%s\t%s\t%s\t%s\t%s\n' "$entry" "$provenance" "$mode" \
      "$(wc -c <"$resource" | awk '{print $1}')" \
      "$(sha256sum "$resource" | awk '{print $1}')"
  done < <(find "$runtime_root" -type f | sort)
} >"$runtime_hashes"

forbidden_hits="$evidence_root/forbidden-reference-hash-hits.tsv"
awk -F '\t' 'BEGIN {OFS="\t"; print "candidate_entry", "reference_entry", "sha256"}
  NR == FNR {if (FNR > 1) reference[$2]=$1; next}
  FNR > 1 && ($5 in reference) {print $1, reference[$5], $5}
' "$reference_hashes" "$runtime_hashes" >"$forbidden_hits"
[[ $(awk 'END {print NR - 1}' "$forbidden_hits") == 0 ]] || \
  die "candidate runtime reproduces forbidden licensed resource bytes"

deferred_resources="$evidence_root/deferred-resources.tsv"
awk -F '\t' 'BEGIN {OFS="\t"}
  NR == 1 {
    print "entry", "category", "phase", "evidence_disposition", "candidate_action"
    next
  }
  $1 != "data_readers.clj" &&
  $1 != "datomic/VERSION" &&
  $1 != "datomic/aws/instance-arch.edn" &&
  $1 != "datomic/aws/region-arch-ami.edn" {
    print $1, $2, $3, $5, $6
  }
' "$classification" >"$deferred_resources"
[[ $(awk 'END {print NR - 1}' "$deferred_resources") == 7 ]] || \
  die "resource classification no longer contains exactly seven deferred entries"

{
  printf 'status=PASS\n'
  printf 'compatibility.target=%s\n' "$compatibility_target"
  printf 'candidate.template.count=3\n'
  printf 'candidate.runtime.resource.count=4\n'
  printf 'candidate.resource.origin=repository-owned-template-or-generated\n'
  printf 'licensed.reference.bytes.copied=false\n'
  printf 'forbidden.reference.hash.hit.count=0\n'
  printf 'jks.resource.count=0\n'
  printf 'aws.provisioning.behavior.validated=false\n'
  printf 'aws.resource.mode=non-production-empty-deferral\n'
  printf 'packaging.metadata=deferred\n'
  printf 'legal.notice=deferred-license-review\n'
  printf 'optional.ui.assets=deferred\n'
  printf 'tls.key.trust.generation=deferred-to-environment\n'
} >"$evidence_root/summary.properties"

(cd "$output_abs" && find runtime evidence -type f ! -name manifest.sha256 \
  -print0 | sort -z | xargs -0 sha256sum) >"$output_abs/manifest.sha256"
(cd "$output_abs" && sha256sum -c manifest.sha256) >/dev/null

echo "Candidate-owned structural resources staged: $output_abs"
echo "4 runtime resources; AWS provisioning behavior remains explicitly unvalidated"
