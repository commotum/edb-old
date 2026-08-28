# Frozen Transactor baseline

These files are compact, deterministic evidence for the exact Datomic Pro
1.0.7277 Transactor and its relationship to the recovered Peer. No original
JAR or extracted implementation bytecode is stored here.

- `archive-entries.tsv` classifies all 9,871 physical ZIP entries in original
  order and records accessible ZIP metadata and a content hash for every file.
- `transactor-classes.tsv` accounts for all 9,682 classfiles without loading
  them.
- `transactor-namespace-inits.tsv` separates 162 Datomic initializers from 85
  bundled-library initializers.
- `datomic-namespace-relationship.tsv` records the 117 shared, 45
  Transactor-only, and 25 Peer-only Datomic namespace names and initializer
  hashes.
- `transactor-peer-class-overlap.tsv` records every one of the 2,090 shared
  class names and whether its bytes agree.
- `java-source-candidates.tsv` coalesces the 52 Java classfiles to 46 source
  paths. "Candidate" does not assert authorship; three Transactor-only paths
  are Jetty-facing or vendored code that still require provenance treatment.
- `pom-dependencies.tsv`, `distribution-jars.tsv`, and
  `distribution-files.tsv` freeze the embedded-POM, shipped-library, launcher,
  setup-file, and optional-product boundaries.
- `manifest.sha256` binds every checked-in TSV. Verify it from this directory
  with `sha256sum -c manifest.sha256`.

Regenerate into a fresh temporary directory with
`../scripts/inventory.sh`. The large occurrence-level ASM tables are retained
only as temporary evidence; this baseline keeps the exhaustive entry and
ownership facts needed to resume recovery.
