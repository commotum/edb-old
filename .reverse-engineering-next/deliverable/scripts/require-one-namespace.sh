#!/usr/bin/env bash

set -euo pipefail

namespace_name=$1
script_dir=$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd)
: "${DATOMIC_REV_SOURCE_CLASSPATH:?missing source-validation classpath}"
: "${DATOMIC_REV_LOG_ROOT:?missing source-validation log root}"

safe_name=${namespace_name//[^A-Za-z0-9_.-]/_}

java -Xmx2g -cp "$DATOMIC_REV_SOURCE_CLASSPATH" \
  clojure.main "$script_dir/require_namespace.clj" "$namespace_name" \
  >"$DATOMIC_REV_LOG_ROOT/$safe_name.out" \
  2>"$DATOMIC_REV_LOG_ROOT/$safe_name.err"

tail -n 1 "$DATOMIC_REV_LOG_ROOT/$safe_name.out"
