#!/usr/bin/env bash

set -euo pipefail

namespace_name=$1
script_dir=$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd)
: "${DATOMIC_REV_ORIGINAL_CLASSPATH:?missing original peer classpath}"
: "${DATOMIC_REV_SOURCE_CLASSPATH:?missing recovered-source classpath}"
: "${DATOMIC_REV_SURFACE_ROOT:?missing namespace-surface output root}"

safe_name=${namespace_name//[^A-Za-z0-9_.-]/_}
original_out="$DATOMIC_REV_SURFACE_ROOT/original/$safe_name.edn"
source_out="$DATOMIC_REV_SURFACE_ROOT/recovered/$safe_name.edn"
original_raw="$DATOMIC_REV_SURFACE_ROOT/logs/$safe_name.original.out"
source_raw="$DATOMIC_REV_SURFACE_ROOT/logs/$safe_name.recovered.out"
original_err="$DATOMIC_REV_SURFACE_ROOT/logs/$safe_name.original.err"
source_err="$DATOMIC_REV_SURFACE_ROOT/logs/$safe_name.recovered.err"

java -Xmx2g -cp "$DATOMIC_REV_ORIGINAL_CLASSPATH" \
  clojure.main "$script_dir/emit_namespace_surface.clj" "$namespace_name" \
  >"$original_raw" 2>"$original_err"

java -Xmx2g -cp "$DATOMIC_REV_SOURCE_CLASSPATH" \
  clojure.main "$script_dir/emit_namespace_surface.clj" "$namespace_name" \
  >"$source_raw" 2>"$source_err"

tail -n 1 "$original_raw" >"$original_out"
tail -n 1 "$source_raw" >"$source_out"

if ! cmp -s "$original_out" "$source_out"; then
  echo "FAIL $namespace_name surface mismatch"
  diff -u "$original_out" "$source_out" \
    >"$DATOMIC_REV_SURFACE_ROOT/logs/$safe_name.diff" || true
  exit 1
fi

echo "PASS $namespace_name"
