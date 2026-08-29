#!/usr/bin/bash -p

set -euo pipefail
umask 077
unset BASH_ENV ENV CDPATH GLOBIGNORE JAVA_TOOL_OPTIONS JDK_JAVA_OPTIONS \
  _JAVA_OPTIONS JDK_JAVAC_OPTIONS CLASSPATH JAVA_HOME LD_PRELOAD \
  LD_LIBRARY_PATH
IFS=$' \t\n'
export LC_ALL=C TZ=UTC

script_dir=$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd)
project_dir=$(cd -- "$script_dir/../.." && pwd)
datomic_home=${1:-"$project_dir/../../datomic/datomic-pro-1.0.7277"}
evidence_root=${2:-/tmp/datomic-stage2-global-overlap-classifier-v1}
java_root=${DATOMIC_STAGE2_CLASSIFIER_JAVA_ROOT:-/tmp/amazon-corretto-11.0.22.7.1}
javap_bin="$java_root/bin/javap"
peer_jar="$datomic_home/peer-1.0.7277.jar"
transactor_jar="$datomic_home/datomic-transactor-pro-1.0.7277.jar"
shared_namespaces="$project_dir/transactor/reports/stage-2-shared-namespaces.tsv"
classification_ledger="$project_dir/transactor/reports/stage-2-overlap-classification.tsv"
tab=$'\t'

fail() {
  echo "Stage 2 global overlap classifier: $*" >&2
  exit 1
}

for command_name in awk comm cut find join mapfile paste perl realpath sed \
                    sha256sum sort split unzip wc xargs; do
  command -v "$command_name" >/dev/null || fail "missing command: $command_name"
done
perl -MDigest::SHA=sha256_hex -e 'exit 0' ||
  fail "Perl Digest::SHA is unavailable"
[[ -x "$javap_bin" ]] || fail "pinned javap is missing: $javap_bin"
[[ -f "$shared_namespaces" && ! -L "$shared_namespaces" ]] ||
  fail "shared namespace inventory is missing or symbolic"
[[ -f "$classification_ledger" && ! -L "$classification_ledger" ]] ||
  fail "classification ledger is missing or symbolic"
[[ ! -L "$evidence_root" ]] || fail "evidence root may not be a symlink"
project_abs=$(realpath "$project_dir")
evidence_abs=$(realpath -m "$evidence_root")
case "$evidence_abs" in
  "$project_abs"|"$project_abs"/*)
    fail "evidence must remain outside the repository: $evidence_abs"
    ;;
esac
if [[ -e "$evidence_abs" ]] &&
   [[ -n $(find "$evidence_abs" -mindepth 1 -print -quit 2>/dev/null) ]]; then
  fail "refusing to overwrite non-empty evidence root: $evidence_abs"
fi
evidence_root=$evidence_abs
mkdir -p "$evidence_root/inputs" "$evidence_root/raw" "$evidence_root/work"

verify_hash() {
  local path=$1
  local expected=$2
  local label=$3
  [[ -f "$path" && ! -L "$path" ]] || fail "$label is missing or symbolic: $path"
  local actual
  actual=$(sha256sum "$path" | awk '{print $1}')
  [[ "$actual" == "$expected" ]] ||
    fail "$label hash mismatch: expected $expected, found $actual"
}

verify_hash "$peer_jar" \
  cb55c9d01e155f9965e3332cd8ef70be42cdefacb50c072076e9ecf22b9a23ba \
  "original Peer"
verify_hash "$transactor_jar" \
  d90819b57e2138085ddc26c93314f953c2cd776d1d1aa8b43a6bec1e475d3692 \
  "original Transactor"
verify_hash "$javap_bin" \
  21b9d9be1c5aed0d6d072f322c9cb2dc0f720a13c92eb9b7d67d7628da140e30 \
  "Corretto javap"

namespace_rows=$(awk -F '\t' 'NR > 1 {n++} END {print n + 0}' "$shared_namespaces")
[[ "$namespace_rows" == 117 ]] ||
  fail "expected 117 shared namespaces, found $namespace_rows"
awk -F '\t' '
  NR == 1 {
    if (NF != 2 || $1 != "namespace" || $2 != "path") exit 1
    next
  }
  NF != 2 || seen[$1]++ || paths[$2]++ || $2 !~ /\.clj$/ {exit 1}
' "$shared_namespaces" || fail "invalid shared namespace inventory"
awk -F '\t' '
  NR == 1 {
    if (NF != 8 || $1 != "namespace" || $2 != "status") exit 1
    next
  }
  NF != 8 || seen[$1]++ {exit 1}
' "$classification_ledger" || fail "invalid classification ledger"

sha256sum "$peer_jar" "$transactor_jar" "$javap_bin" \
  "$shared_namespaces" "$classification_ledger" \
  >"$evidence_root/inputs/artifacts-tools-and-ledgers.sha256"

# Record the two recovered source owners directly. This is provenance inventory,
# not a claim that differing source text is semantically equivalent.
printf 'namespace\tpath\tpeer_owner\tpeer_sha256\ttransactor_owner\ttransactor_sha256\tsource_text_relation\n' \
  >"$evidence_root/source-ownership.tsv"
while IFS=$'\t' read -r namespace source_path; do
  [[ "$namespace" == namespace ]] && continue
  peer_source="$project_dir/src-clj/$source_path"
  transactor_source="$project_dir/transactor/src-clj/$source_path"
  [[ -f "$peer_source" && ! -L "$peer_source" ]] ||
    fail "Peer source is missing or symbolic: $peer_source"
  [[ -f "$transactor_source" && ! -L "$transactor_source" ]] ||
    fail "Transactor source is missing or symbolic: $transactor_source"
  peer_source_sha=$(sha256sum "$peer_source" | awk '{print $1}')
  transactor_source_sha=$(sha256sum "$transactor_source" | awk '{print $1}')
  source_text_relation=different
  [[ "$peer_source_sha" == "$transactor_source_sha" ]] && source_text_relation=exact
  printf '%s\t%s\tpeer-recovery\t%s\ttransactor-recovery\t%s\t%s\n' \
    "$namespace" "$source_path" "$peer_source_sha" "$transactor_source_sha" \
    "$source_text_relation" >>"$evidence_root/source-ownership.tsv"
done <"$shared_namespaces"

# Build namespace-exact class inventories. A namespace owns only its initializer
# and dollar-prefixed generated classes; child namespace directories are excluded.
write_class_inventory() {
  local artifact=$1
  local output=$2
  unzip -Z1 "$artifact" |
    awk -F '\t' '
      NR == FNR {
        if (FNR > 1) {
          path=$2
          sub(/\.clj$/, "", path)
          namespace_by_path[path]=$1
        }
        next
      }
      /\.class$/ {
        internal=$0
        sub(/\.class$/, "", internal)
        entries[++entry_count]=internal
        if (internal ~ /__init$/) {
          initializer=internal
          sub(/__init$/, "", initializer)
          initializer_namespaces[initializer]=1
        }
      }
      END {
        for (entry_index=1; entry_index <= entry_count; entry_index++) {
          internal=entries[entry_index]
          owner=""
          if (internal ~ /\$/) {
            generated_base=internal
            sub(/\$.*/, "", generated_base)
            if (generated_base in initializer_namespaces) {
              owner=generated_base
            }
          } else if (internal ~ /__init$/) {
            owner=internal
            sub(/__init$/, "", owner)
          }
          if (owner == "") {
            for (initializer in initializer_namespaces) {
              if (index(internal, initializer "/") == 1 &&
                  length(initializer) > length(owner)) {
                owner=initializer
              }
            }
          }
          if (owner in namespace_by_path) {
            print namespace_by_path[owner] "\t" internal
          }
        }
      }
    ' "$shared_namespaces" - |
    sort -t "$tab" -k1,1 -k2,2 |
    awk -F '\t' '
      BEGIN {OFS="\t"}
      {
        role=$2
        gsub(/__[0-9]+/, "__ID", role)
        if (role ~ /\$[[:xdigit:]]{8}$/) sub(/\$[[:xdigit:]]{8}$/, "$HASH", role)
        occurrence[$1 SUBSEP role]++
        print $1, role "#" occurrence[$1 SUBSEP role], $2
      }
    ' >"$output"
}

write_class_inventory "$peer_jar" "$evidence_root/work/peer-classes.tsv"
write_class_inventory "$transactor_jar" "$evidence_root/work/transactor-classes.tsv"
[[ -s "$evidence_root/work/peer-classes.tsv" ]] || fail "empty Peer class inventory"
[[ -s "$evidence_root/work/transactor-classes.tsv" ]] ||
  fail "empty Transactor class inventory"

cut -f1,2 "$evidence_root/work/peer-classes.tsv" | sort -u \
  >"$evidence_root/work/peer-roles.tsv"
cut -f1,2 "$evidence_root/work/transactor-classes.tsv" | sort -u \
  >"$evidence_root/work/transactor-roles.tsv"
comm -23 "$evidence_root/work/peer-roles.tsv" \
  "$evidence_root/work/transactor-roles.tsv" >"$evidence_root/peer-only-class-roles.tsv"
comm -13 "$evidence_root/work/peer-roles.tsv" \
  "$evidence_root/work/transactor-roles.tsv" >"$evidence_root/transactor-only-class-roles.tsv"

# javap is deliberately batched: at most seven JVMs per artifact for the current
# corpus, rather than one JVM startup for each of roughly 4,500 classes.
run_batched_javap() {
  local lane=$1
  local artifact=$2
  local inventory=$3
  local raw_output="$evidence_root/raw/$lane.javap"
  local classes_file="$evidence_root/work/$lane-class-names.txt"
  local chunks_root="$evidence_root/work/$lane-javap-chunks"
  mkdir -p "$chunks_root"
  cut -f3 "$inventory" | sed -e 's#/#.#g' >"$classes_file"
  split -d -a 3 -l 700 "$classes_file" "$chunks_root/chunk."
  : >"$raw_output"
  local chunk
  for chunk in "$chunks_root"/chunk.*; do
    mapfile -t classes <"$chunk"
    [[ ${#classes[@]} -gt 0 ]] || continue
    "$javap_bin" -classpath "$artifact" -sysinfo -c -p -s "${classes[@]}" \
      >>"$raw_output"
  done
}

run_batched_javap peer "$peer_jar" "$evidence_root/work/peer-classes.tsv"
run_batched_javap transactor "$transactor_jar" \
  "$evidence_root/work/transactor-classes.tsv"

# Convert javap blocks to normalized member records. Keys use an ASCII unit
# separator internally so signatures can remain intact in the tab-separated files.
parse_members() {
  local inventory=$1
  local raw_input=$2
  local methods_output=$3
  local fields_output=$4
  awk -F '\t' -v methods_output="$methods_output" -v fields_output="$fields_output" '
    function normalize(value) {
      gsub(/__[0-9]+/, "__ID", value)
      gsub(/\$[[:xdigit:]]{8}/, "$HASH", value)
      gsub(/#[0-9]+/, "#CP", value)
      gsub(/[[:space:]]+/, " ", value)
      sub(/^ /, "", value)
      sub(/ $/, "", value)
      return value
    }
    function flush_member() {
      if (namespace == "" || signature == "" || descriptor == "") {
        signature=""; descriptor=""; body=""; return
      }
      if (descriptor ~ /^\(/) {
        print namespace "\t" role "\t" signature "\t" descriptor "\t" body >> methods_output
      } else {
        print namespace "\t" role "\t" signature "\t" descriptor >> fields_output
      }
      signature=""; descriptor=""; body=""
    }
    NR == FNR {
      namespace_by_internal[$3]=$1
      role_by_internal[$3]=$2
      next
    }
    /^Classfile / {
      flush_member()
      internal=$0
      sub(/^Classfile .*!\//, "", internal)
      sub(/\.class$/, "", internal)
      namespace=namespace_by_internal[internal]
      role=role_by_internal[internal]
      if (namespace == "" || role == "") {
        print "unmapped javap class: " internal > "/dev/stderr"
        exit 2
      }
      next
    }
    /^  [^ ].*;$/ || /^  static \{\};$/ {
      flush_member()
      signature=normalize($0)
      next
    }
    /^    descriptor: / && signature != "" {
      descriptor=$0
      sub(/^    descriptor: /, "", descriptor)
      descriptor=normalize(descriptor)
      next
    }
    /^}$/ {
      flush_member()
      namespace=""; role=""
      next
    }
    signature != "" && descriptor != "" {
      line=normalize($0)
      if (line != "" && line !~ /^Last modified / &&
          line !~ /^(MD5|SHA-256) checksum / && line !~ /^Compiled from /) {
        if (body == "") body=line
        else body=body " || " line
      }
    }
    END {flush_member()}
  ' "$inventory" "$raw_input"
  sort -t "$tab" -k1,1 -k3,3 -k4,4 -k5,5 -k2,2 \
    -o "$methods_output.raw" "$methods_output"
  perl -F'\t' -lane '
    my $group=join("\x1f", @F[0,2,3]);
    $occurrence=0 if !defined($previous) || $group ne $previous;
    $occurrence++;
    $previous=$group;
    my $key=join("\x1f", @F[0,2,3], sprintf("%06d", $occurrence));
    my $body=length($F[4]) ? $F[4] : "__EMPTY_METHOD_BODY__";
    print join("\t", $key, $F[1], $body);
  ' "$methods_output.raw" >"$methods_output"
  sort -t "$tab" -k1,1 -k3,3 -k4,4 -k2,2 \
    -o "$fields_output.raw" "$fields_output"
  perl -F'\t' -lane '
    my $group=join("\x1f", @F[0,2,3]);
    $occurrence=0 if !defined($previous) || $group ne $previous;
    $occurrence++;
    $previous=$group;
    my $key=join("\x1f", @F[0,2,3], sprintf("%06d", $occurrence));
    print join("\t", $key, $F[1]);
  ' "$fields_output.raw" >"$fields_output"
}

parse_members "$evidence_root/work/peer-classes.tsv" \
  "$evidence_root/raw/peer.javap" "$evidence_root/work/peer-methods.tsv" \
  "$evidence_root/work/peer-fields.tsv"
parse_members "$evidence_root/work/transactor-classes.tsv" \
  "$evidence_root/raw/transactor.javap" \
  "$evidence_root/work/transactor-methods.tsv" \
  "$evidence_root/work/transactor-fields.tsv"

cut -f1 "$evidence_root/work/peer-methods.tsv" \
  >"$evidence_root/work/peer-method-keys.tsv"
cut -f1 "$evidence_root/work/transactor-methods.tsv" \
  >"$evidence_root/work/transactor-method-keys.tsv"
comm -23 "$evidence_root/work/peer-method-keys.tsv" \
  "$evidence_root/work/transactor-method-keys.tsv" \
  >"$evidence_root/peer-only-method-abi.tsv"
comm -13 "$evidence_root/work/peer-method-keys.tsv" \
  "$evidence_root/work/transactor-method-keys.tsv" \
  >"$evidence_root/transactor-only-method-abi.tsv"

join -t "$tab" -a 1 -a 2 -e '__MISSING__' -o 0,1.2,1.3,2.2,2.3 \
  "$evidence_root/work/peer-methods.tsv" \
  "$evidence_root/work/transactor-methods.tsv" \
  >"$evidence_root/work/joined-methods.tsv"

perl -MDigest::SHA=sha256_hex -F'\t' -lane '
  BEGIN {
    print join("\t", qw(namespace peer_class_role transactor_class_role method_signature descriptor occurrence
      peer_body_sha256 transactor_body_sha256 body_relation family_signals));
  }
  my @key=split(/\x1f/, $F[0], -1);
  my ($namespace,$signature,$descriptor,$occurrence)=@key;
  my ($peer_role,$peer,$transactor_role,$transactor)=@F[1,2,3,4];
  my $relation=($peer eq "__MISSING__" || $transactor eq "__MISSING__")
    ? "abi-missing" : ($peer eq $transactor ? "exact" : "residual");
  my @signals;
  if ($relation eq "residual") {
    my $both="$peer || $transactor";
    push @signals, "namespace-protocol-metadata-scaffold"
      if $peer_role =~ /__init#/ || $transactor_role =~ /__init#/ ||
         $both =~ /(?:Var\.(?:setMeta|setDynamic)|RT\.var)/;
    push @signals, "map-or-sequence-destructuring"
      if $both =~ /(?:RT\.(?:seq|nth)|ISeq\.(?:first|next)|RT\.map)/;
    push @signals, "capture-or-clearing-order"
      if $both =~ /aconst_null/ && $both =~ /putfield/;
    push @signals, "locking-lowering"
      if $both =~ /(?:monitorenter|monitorexit)/;
    push @signals, "dead-verifier-slot"
      if $both =~ /(?:\bathrow\b|\bpop\b)/;
    push @signals, "nil-placement" if !@signals && $both =~ /aconst_null/;
    push @signals, "unexplained-executable" unless @signals;
  }
  my $peer_sha=$peer eq "__MISSING__" ? "missing" : sha256_hex($peer);
  my $transactor_sha=$transactor eq "__MISSING__" ? "missing" : sha256_hex($transactor);
  print join("\t", $namespace,$peer_role,$transactor_role,$signature,$descriptor,$occurrence,
    $peer_sha,$transactor_sha,$relation,(@signals ? join(",",@signals) : "none"));
' "$evidence_root/work/joined-methods.tsv" \
  >"$evidence_root/normalized-method-body-differences.tsv"

cut -f1 "$evidence_root/work/peer-fields.tsv" \
  >"$evidence_root/work/peer-field-keys.tsv"
cut -f1 "$evidence_root/work/transactor-fields.tsv" \
  >"$evidence_root/work/transactor-field-keys.tsv"
join -t "$tab" -v 1 "$evidence_root/work/peer-fields.tsv" \
  "$evidence_root/work/transactor-fields.tsv" \
  >"$evidence_root/work/peer-only-field-keys.tsv"
join -t "$tab" -v 2 "$evidence_root/work/peer-fields.tsv" \
  "$evidence_root/work/transactor-fields.tsv" \
  >"$evidence_root/work/transactor-only-field-keys.tsv"

printf 'namespace\tclass_role\tfield_signature\tdescriptor\toccurrence\tlane\tdelta_family\n' \
  >"$evidence_root/field-deltas.tsv"
perl -F'\t' -lane '
  my @key=split(/\x1f/, $F[0], -1);
  my ($namespace,$signature,$descriptor,$occurrence)=@key;
  my $family=($signature eq "public static final clojure.lang.Var const__ID;" &&
              $descriptor eq "Lclojure/lang/Var;")
    ? "generated-static-var" : "other-field";
  print join("\t", $namespace,$F[1],$signature,$descriptor,$occurrence,"peer-only",$family);
' "$evidence_root/work/peer-only-field-keys.tsv" \
  >>"$evidence_root/field-deltas.tsv"
perl -F'\t' -lane '
  my @key=split(/\x1f/, $F[0], -1);
  my ($namespace,$signature,$descriptor,$occurrence)=@key;
  my $family=($signature eq "public static final clojure.lang.Var const__ID;" &&
              $descriptor eq "Lclojure/lang/Var;")
    ? "generated-static-var" : "other-field";
  print join("\t", $namespace,$F[1],$signature,$descriptor,$occurrence,"transactor-only",$family);
' "$evidence_root/work/transactor-only-field-keys.tsv" \
  >>"$evidence_root/field-deltas.tsv"

# Runtime evidence is only marked present when the checked-in machine-readable
# ledger records it. The classifier does not infer coverage from prose or filenames.
awk -F '\t' '
  BEGIN {OFS="\t"}
  NR == FNR {
    if (FNR > 1) {
      status[$1]=$2
      behavior[$1]=$4
    }
    next
  }
  FNR == 1 {
    print "namespace", "ledger_status", "runtime_coverage_relation", "coverage_source", "coverage_description"
    next
  }
  {
    state=($1 in status ? status[$1] : "OPEN")
    coverage=(($1 in behavior) && behavior[$1] != "" ? "recorded" : "none-machine-readable")
    source=(coverage == "recorded" ? "stage-2-overlap-classification.tsv" : "none")
    description=(coverage == "recorded" ? behavior[$1] : "")
    print $1, state, coverage, source, description
  }
' "$classification_ledger" "$shared_namespaces" \
  >"$evidence_root/runtime-coverage.tsv"

# Collapse the detailed relations into one fixed-schema row per namespace. Tiers
# are triage outputs, not automatic semantic promotion decisions.
perl -F'\t' -lane '
  BEGIN {
    $unit="\x1f";
    $source_file=$ARGV[0]; $coverage_file=$ARGV[1];
    $peer_classes=$ARGV[2]; $transactor_classes=$ARGV[3];
    $peer_role_delta=$ARGV[4]; $transactor_role_delta=$ARGV[5];
    $body_file=$ARGV[6]; $field_file=$ARGV[7]; $shared_file=$ARGV[8];

    open my $source_fh, "<", $source_file or die $!;
    <$source_fh>;
    while (<$source_fh>) {chomp; my @v=split(/\t/,$_,-1); $source{$v[0]}=\@v;}
    close $source_fh;
    open my $coverage_fh, "<", $coverage_file or die $!;
    <$coverage_fh>;
    while (<$coverage_fh>) {chomp; my @v=split(/\t/,$_,-1); $coverage{$v[0]}=\@v;}
    close $coverage_fh;
    for my $spec ([$peer_classes,"peer_classes"],[$transactor_classes,"transactor_classes"]) {
      open my $fh, "<", $spec->[0] or die $!;
      while (<$fh>) {chomp; my ($ns)=split(/\t/); $counts{$spec->[1]}{$ns}++;}
      close $fh;
    }
    for my $spec ([$peer_role_delta,"peer_role_delta"],[$transactor_role_delta,"transactor_role_delta"]) {
      open my $fh, "<", $spec->[0] or die $!;
      while (<$fh>) {chomp; next unless length; my ($ns)=split(/\t/); $counts{$spec->[1]}{$ns}++;}
      close $fh;
    }
    open my $body_fh, "<", $body_file or die $!;
    <$body_fh>;
    while (<$body_fh>) {
      chomp; my @v=split(/\t/,$_,-1); my ($ns,$relation,$signals)=@v[0,8,9];
      $counts{peer_methods}{$ns}++ unless $v[6] eq "missing";
      $counts{transactor_methods}{$ns}++ unless $v[7] eq "missing";
      if ($relation eq "exact") {$counts{exact_methods}{$ns}++;}
      elsif ($relation eq "residual") {
        $counts{residual_methods}{$ns}++;
        for my $signal (split(/,/, $signals)) {$families{$ns}{$signal}++;}
      } else {$counts{abi_method_deltas}{$ns}++;}
    }
    close $body_fh;
    open my $field_fh, "<", $field_file or die $!;
    <$field_fh>;
    while (<$field_fh>) {
      chomp; my @v=split(/\t/,$_,-1); my ($ns,$lane,$family)=@v[0,5,6];
      $counts{"${lane}_fields"}{$ns}++;
      $counts{other_fields}{$ns}++ if $family eq "other-field";
      $counts{generated_var_fields}{$ns}++ if $family eq "generated-static-var";
    }
    close $field_fh;

    print join("\t", qw(namespace ledger_status source_path source_text_relation
      peer_classes transactor_classes class_role_relation peer_methods
      transactor_methods method_abi_relation exact_method_bodies residual_method_bodies
      normalized_body_relation peer_only_fields transactor_only_fields field_relation
      family_signals runtime_coverage_relation coverage_source triage_tier));
    open my $shared_fh, "<", $shared_file or die $!;
    <$shared_fh>;
    while (<$shared_fh>) {
      chomp; my ($ns,$path)=split(/\t/,$_,-1);
      my $peer_classes=$counts{peer_classes}{$ns}//0;
      my $transactor_classes=$counts{transactor_classes}{$ns}//0;
      my $role_delta=($counts{peer_role_delta}{$ns}//0)+($counts{transactor_role_delta}{$ns}//0);
      my $role_relation=$role_delta ? "different" : "exact";
      my $peer_methods=$counts{peer_methods}{$ns}//0;
      my $transactor_methods=$counts{transactor_methods}{$ns}//0;
      my $abi_delta=$counts{abi_method_deltas}{$ns}//0;
      my $abi_relation=$abi_delta ? "different" : "exact";
      my $exact=$counts{exact_methods}{$ns}//0;
      my $residual=$counts{residual_methods}{$ns}//0;
      my $body_relation=$abi_delta ? "abi-different" : ($residual ? "residual" : "exact");
      my $peer_fields=$counts{"peer-only_fields"}{$ns}//0;
      my $transactor_fields=$counts{"transactor-only_fields"}{$ns}//0;
      my $other_fields=$counts{other_fields}{$ns}//0;
      my $generated_fields=$counts{generated_var_fields}{$ns}//0;
      my $field_relation=($peer_fields+$transactor_fields == 0) ? "exact" :
        (!$other_fields && !$peer_fields ? "transactor-only-generated-static-vars" : "other-field-delta");
      my @families=sort keys %{$families{$ns}//{}};
      my $family_text=@families ? join(",", map {$_ . ":" . $families{$ns}{$_}} @families) : "none";
      my $ledger_status=$coverage{$ns}[1]//"OPEN";
      my $coverage_relation=$coverage{$ns}[2]//"none-machine-readable";
      my $coverage_source=$coverage{$ns}[3]//"none";
      my $tier;
      if ($role_relation ne "exact" || $abi_relation ne "exact" || $other_fields) {
        $tier="ABI_OR_FIELD_INVESTIGATE";
      } elsif ($body_relation eq "exact") {
        $tier="EXACT_EXECUTABLE_RELATION";
      } elsif (exists $families{$ns}{"unexplained-executable"}) {
        $tier="SEMANTIC_INVESTIGATE";
      } else {
        $tier="PROVEN_FAMILY_REVIEW";
      }
      $tier="LEDGER_RESOLVED" if $ledger_status eq "RESOLVED";
      print join("\t", $ns,$ledger_status,$path,$source{$ns}[6],$peer_classes,
        $transactor_classes,$role_relation,$peer_methods,$transactor_methods,
        $abi_relation,$exact,$residual,$body_relation,$peer_fields,$transactor_fields,
        $field_relation,$family_text,$coverage_relation,$coverage_source,$tier);
    }
    close $shared_fh;
    exit 0;
  }
' "$evidence_root/source-ownership.tsv" "$evidence_root/runtime-coverage.tsv" \
  "$evidence_root/work/peer-classes.tsv" \
  "$evidence_root/work/transactor-classes.tsv" \
  "$evidence_root/peer-only-class-roles.tsv" \
  "$evidence_root/transactor-only-class-roles.tsv" \
  "$evidence_root/normalized-method-body-differences.tsv" \
  "$evidence_root/field-deltas.tsv" "$shared_namespaces" \
  >"$evidence_root/classifier.tsv"

awk -F '\t' '
  NR == 1 {next}
  {tiers[$20]++; roles[$7]++; abi[$10]++; bodies[$13]++; fields[$16]++}
  END {
    print "claim\tvalue"
    print "namespace_rows\t" NR-1
    for (key in tiers) print "triage." key "\t" tiers[key]
    for (key in roles) print "class_roles." key "\t" roles[key]
    for (key in abi) print "method_abi." key "\t" abi[key]
    for (key in bodies) print "method_bodies." key "\t" bodies[key]
    for (key in fields) print "fields." key "\t" fields[key]
  }
' "$evidence_root/classifier.tsv" | sort >"$evidence_root/summary.tsv"

sha256sum "$script_dir/classify-stage2-overlaps-global.sh" \
  >"$evidence_root/inputs/runner.sha256"
find "$evidence_root" -type f ! -name evidence.sha256 -print0 |
  sort -z | xargs -0 sha256sum >"$evidence_root/evidence.sha256"

printf 'Stage 2 global overlap classifier passed: %s\n' "$evidence_root"
