#!/usr/bin/env bash
set -euo pipefail

export LC_ALL=C

inventory_root=${1:?usage: render-baseline.sh RAW_INVENTORY OUTPUT_DIRECTORY}
output_dir=${2:?usage: render-baseline.sh RAW_INVENTORY OUTPUT_DIRECTORY}
transactor=datomic-transactor-pro-1.0.7277.jar
peer=peer-1.0.7277.jar

for file in classes.tsv resources.tsv summary.tsv pom-dependencies.tsv jars.tsv warnings.tsv; do
  [[ -f "$inventory_root/$file" ]] || {
    echo "missing raw inventory file: $inventory_root/$file" >&2
    exit 1
  }
done

if [[ -e "$output_dir" ]] && \
   [[ -n "$(find "$output_dir" -mindepth 1 -print -quit 2>/dev/null)" ]]; then
  echo "refusing to overwrite non-empty output directory: $output_dir" >&2
  exit 1
fi
mkdir -p "$output_dir"

metric() {
  awk -F '\t' -v key="$1" '$1 == key {print $2}' \
    "$inventory_root/summary.tsv"
}

assert_eq() {
  if [[ "$2" != "$3" ]]; then
    echo "FAIL $1: expected $2, found $3" >&2
    exit 1
  fi
}

assert_eq transactor_sha \
  d90819b57e2138085ddc26c93314f953c2cd776d1d1aa8b43a6bec1e475d3692 \
  "$(metric primary.sha256)"
assert_eq class_entries 9682 "$(metric primary.class_entries)"
assert_eq parsed_classes 9682 "$(metric primary.parsed_classes)"
assert_eq clojure_aot_classes 9630 "$(metric primary.clojure_aot_classes)"
assert_eq java_classes 52 "$(metric primary.java_classes)"
assert_eq unknown_classes 0 "$(metric primary.unknown_source_classes)"
assert_eq namespace_initializers 247 "$(metric primary.namespace_init_classes)"
assert_eq resource_entries 11 "$(metric primary.resource_entries)"
assert_eq embedded_pom_hash \
  8de366709ed36188a8596ae9e412c6e9ed9a226938eb39e6b6650a36a8892304 \
  "$(metric pom.embedded_sha256)"
assert_eq embedded_pom_match true "$(metric pom.embedded_matches_input)"
assert_eq direct_dependencies 61 "$(metric pom.direct_dependencies)"
assert_eq exact_dependencies 59 "$(metric pom.exactly_resolved)"
assert_eq missing_dependencies 2 "$(metric pom.missing)"
assert_eq parse_warnings 0 "$(metric parse.warnings)"

printf 'role\tbytes\tsha256\tartifact\ntransactor\t12689398\td90819b57e2138085ddc26c93314f953c2cd776d1d1aa8b43a6bec1e475d3692\tdatomic-transactor-pro-1.0.7277.jar\npeer\t6801387\tcb55c9d01e155f9965e3332cd8ef70be42cdefacb50c072076e9ecf22b9a23ba\tpeer-1.0.7277.jar\n' \
  > "$output_dir/archive-inputs.tsv"

awk -F '\t' 'BEGIN { OFS="\t"; print "entry","internal_name","source_file","source_kind","major","bytes","sha256" }
NR > 1 && $1 == "datomic-transactor-pro-1.0.7277.jar" { print $2,$3,$8,$9,$5,$24,$25 }' \
  "$inventory_root/classes.tsv" > "$output_dir/transactor-classes.tsv"

awk -F '\t' 'BEGIN { OFS="\t"; print "entry","size","compressed_size","crc32","method","sha256" }
NR > 1 && $1 == "datomic-transactor-pro-1.0.7277.jar" { print $2,$3,$4,$5,$6,$7 }' \
  "$inventory_root/resources.tsv" > "$output_dir/transactor-resources.tsv"

awk -F '\t' 'BEGIN { OFS="\t"; print "namespace","entry","scope","sha256" }
NR > 1 && $1 == "datomic-transactor-pro-1.0.7277.jar" && $3 ~ /__init$/ {
  n=$3; sub(/__init$/, "", n); gsub(/\//, ".", n)
  scope=(n ~ /^datomic\./ ? "datomic" : "bundled")
  print n,$2,scope,$25
}' "$inventory_root/classes.tsv" > "$output_dir/transactor-namespace-inits.tsv"

awk -F '\t' 'BEGIN { OFS="\t" }
NR > 1 {
  if ($1 == "datomic-transactor-pro-1.0.7277.jar") t[$3]=$25
  else if ($1 == "peer-1.0.7277.jar") p[$3]=$25
}
END {
  print "internal_name","transactor_sha256","peer_sha256","byte_identical"
  for (n in t) if (n in p) print n,t[n],p[n],(t[n] == p[n] ? "true" : "false")
}' "$inventory_root/classes.tsv" | {
  IFS= read -r header
  printf '%s\n' "$header"
  sort
} > "$output_dir/transactor-peer-class-overlap.tsv"

awk -F '\t' 'BEGIN { OFS="\t" }
NR > 1 && $3 ~ /^datomic\/.*__init$/ {
  if ($1 == "datomic-transactor-pro-1.0.7277.jar") t[$3]=$25
  else if ($1 == "peer-1.0.7277.jar") p[$3]=$25
}
END {
  print "namespace","relationship","transactor_init_sha256","peer_init_sha256"
  for (n in t) u[n]=1
  for (n in p) u[n]=1
  for (n in u) {
    d=n; sub(/__init$/, "", d); gsub(/\//, ".", d)
    rel=((n in t) && (n in p) ? "shared" : ((n in t) ? "transactor-only" : "peer-only"))
    print d,rel,t[n],p[n]
  }
}' "$inventory_root/classes.tsv" | {
  IFS= read -r header
  printf '%s\n' "$header"
  sort
} > "$output_dir/datomic-namespace-relationship.tsv"

awk -F '\t' 'BEGIN { OFS="\t" }
NR > 1 && $9 == "java" {
  name=$3; sub(/[^\/]+$/, $8, name)
  if ($1 == "datomic-transactor-pro-1.0.7277.jar") t[name]=1
  else if ($1 == "peer-1.0.7277.jar") p[name]=1
}
END {
  print "source_path","relationship"
  for (n in t) print n,((n in p) ? "shared" : "transactor-only")
}' "$inventory_root/classes.tsv" | {
  IFS= read -r header
  printf '%s\n' "$header"
  sort
} > "$output_dir/java-source-candidates.tsv"

awk -F '\t' '$1 != "pom.input_path"' "$inventory_root/summary.tsv" \
  > "$output_dir/summary.tsv"
cp "$inventory_root/pom-dependencies.tsv" "$output_dir/pom-dependencies.tsv"
cp "$inventory_root/warnings.tsv" "$output_dir/warnings.tsv"

awk -F '\t' -v OFS='\t' -v transactor="$transactor" -v peer="$peer" '
NR == 1 { print $1,"distribution_path",$3,$4,$5,$6,$7,$8,$9,$10,$11,"boundary"; next }
{
  path=($1 == transactor ? $1 : ($1 == peer ? $1 : "lib/" $1))
  boundary=($1 == transactor ? "licensed-input" : ($1 == peer ? "peer-evidence-only" : "dependency"))
  print $1,path,$3,$4,$5,$6,$7,$8,$9,$10,$11,boundary
}' "$inventory_root/jars.tsv" > "$output_dir/distribution-jars.tsv"

assert_eq compact_class_rows 9683 "$(wc -l < "$output_dir/transactor-classes.tsv")"
assert_eq compact_resource_rows 12 "$(wc -l < "$output_dir/transactor-resources.tsv")"
assert_eq compact_init_rows 248 "$(wc -l < "$output_dir/transactor-namespace-inits.tsv")"
assert_eq compact_overlap_rows 2091 "$(wc -l < "$output_dir/transactor-peer-class-overlap.tsv")"
assert_eq compact_relationship_rows 188 "$(wc -l < "$output_dir/datomic-namespace-relationship.tsv")"
assert_eq compact_java_source_rows 47 "$(wc -l < "$output_dir/java-source-candidates.tsv")"

(cd "$output_dir" && sha256sum -- *.tsv > manifest.sha256)
echo "rendered compact Transactor baseline: $output_dir"
