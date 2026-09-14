# Deterministic Datomic bytecode inventory

This is a read-only ASM 9 inventory. It never loads or executes classes from
the analyzed JARs. The supplied runner scans the Datomic peer in detail, scans
all 533 distribution `lib/*.jar` files as a companion definition/resource
index, scans POM-matched direct dependencies in detail, and automatically
detail-scans every companion that defines a primary class name so overlap
classes can be compared by SHA-256.

Run:

```bash
tools/bytecode-inventory/run-datomic.sh \
  /home/jake/Developer/datomic/datomic-pro-1.0.7277 \
  "$PWD" \
  /tmp/datomic-bytecode-inventory/out
```

`BytecodeInventory.java` also accepts repeated `--detail-jar JAR` arguments.
Omit `--detail-pom-deps` for a small peer-only detailed scan while retaining
full external-owner resolution against the companion directory.

## Exact counting contract

- Classes, methods, and fields are class-file definitions, including synthetic
  and bridge members.
- An instruction is one ASM `visit*Insn` callback. Labels, frames, line-number
  nodes, annotations, and local-variable table entries are not instructions.
- Calls are method invocation instructions plus `INVOKEDYNAMIC`; the latter is
  also reported separately.
- Instruction literals are `ACONST_NULL`, numeric `xCONST_n`, `BIPUSH`,
  `SIPUSH`, and `LDC`. Field literals are non-null `ConstantValue` attributes.
- Each event is retained in a sorted TSV or contributes to a per-method opcode
  histogram. Floating-point values use raw IEEE-754 bits so NaNs and signed
  zero remain distinguishable.

## Outputs

- `summary.tsv`, `architecture.md`, `jar-bytecode-summary.tsv`: primary totals,
  per-detailed-JAR totals, and report overview.
- `jars.tsv`, `resources.tsv`, `pom-dependencies.tsv`: distribution evidence.
- `classes.tsv`, `fields.tsv`, `methods.tsv`, `opcodes.tsv`: definition and
  instruction catalogs.
- `calls.tsv`, `field-accesses.tsv`, `literals.tsv`: occurrence-level facts.
- `namespace-classes.tsv`, `namespace-summary.tsv`: AOT/Java source mapping and
  the complete generated-class closure for each namespace.
- `namespace-dependencies.tsv`, `external-owners.tsv`: primary namespace edges
  and companion/JDK/unresolved owner resolution.
- `duplicate-classes.tsv`: ambiguous definitions across distribution JARs.
- `warnings.tsv`: parse failures; a validated run should contain only its
  header.

All records are sorted. ZIP timestamps and wall-clock time are excluded. Input
paths and SHA-256 hashes bind a run to exact artifacts.

Run `validate.sh OUT DIST REV` to cross-check ZIP entry counts, every primary
definition count, occurrence tables, opcode sums, namespace initializers,
source mapping, the embedded POM hash, and the empty warning set.
