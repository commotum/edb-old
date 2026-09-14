#!/usr/bin/env bash
set -euo pipefail

HERE=$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd)
REV=${2:-$(cd -- "$HERE/../.." && pwd)}
DIST=${1:-"$REV/../../datomic/datomic-pro-1.0.7277"}
OUT=${3:-/tmp/datomic-bytecode-inventory/out}

exec java -Xmx3g \
  -cp "$DIST/lib/asm-9.2.jar" \
  "$HERE/BytecodeInventory.java" \
  --primary "$DIST/peer-1.0.7277.jar" \
  --companion-dir "$DIST/lib" \
  --pom "$DIST/pom.xml" \
  --clj-source-root "$REV/src-clj" \
  --java-source-root "$REV/src-java" \
  --detail-pom-deps \
  --out "$OUT"
