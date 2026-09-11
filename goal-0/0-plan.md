# Goal 0 — Understand Datomic, then rebuild Atomic from that understanding

## Objective

Make Atomic a readable, source-grounded, idiomatic Rust realization of Datomic's
behavior, architecture and design principles—not merely a database that passes
similar tests. First explain the recovered implementation in place, then connect
the Datomic Pro documentation to the implementing source, then use that knowledge
to cut over Atomic's code, component boundaries and user documentation completely.

The intended reader is a human trying to understand, use and maintain the system.
They should be able to follow a documented behavior through its Datomic rationale
and implementation into the corresponding Rust component without rediscovering
the architecture or decoding a flat collection of files.

## Authority and constraints

- `datomic_pro_docs/` governs documented behavior. `1.0.7705/` supplies the
  implementation mechanisms, algorithms and responsibility boundaries to learn
  from. Read both; do not use the existing Rust implementation as the default
  architectural authority. Existing tests are evidence, not an immutable spec.
- Prefer the recovered design unless a concrete Rust/platform constraint or
  demonstrated benefit justifies an adaptation. Record the mechanism preserved,
  the difference and its consequence. “Idiomatic Rust,” “PostgreSQL only,” and
  passing tests are not sufficient explanations for architectural substitution.
- Native Rust and PostgreSQL remain the product target. PostgreSQL is the sole
  provider behind a small opaque-storage boundary, not the database policy
  engine. No JVM execution/wire parity, other-provider project or clean-room
  reconstruction process is required. Personal use is the user's objective.
- This is a full first-release cutover. Public Rust APIs, paths, schemas and
  durable formats may change. No old readers, migration/rollback converters,
  compatibility re-exports, historical-executable matrix or permanent dual engine.
  Update all current callers. Do not reset unrelated databases or erase reference
  material; validate on explicitly disposable fresh databases.
- Preserve useful current user capabilities and current-version correctness:
  exact values, schema/identity, immutable database values, declarative serialized
  transactions, history/time views, peer-local reads, durable acknowledgements,
  receipt-first exact retries, recovery, failover and backup/restore. An Atomic
  feature without a direct Datomic counterpart needs an explicit disposition,
  not silent removal or automatic architectural veto power.
- Keep source commentary visibly separate from recovered code and docstrings.
  Preserve source bodies and original provenance records; use comments for the
  added explanation. Keep the unannotated baseline recoverable in Git and record
  its revision. Do not create another copied source tree or mislabel annotated
  files as pristine artifact output.
- Use proportionate permanent regressions and actual application/PostgreSQL
  checks. Measure complete-path work when claiming a cost improvement. No test
  count, line-count target, annotation word quota or arbitrary performance-parity
  threshold substitutes for understanding and a working product.

## Starting facts

- The reference has `peer/` and `transactor/` artifact trees, each with `src-clj/`,
  `src-java/` and `source-manifests/`. These are recovered initializer-level
  sources, not the authors' original tree; some dependency sources are exact.
  Compiler scaffolding and erased names limit what can be inferred about intent.
- Distribution membership is not exclusive component ownership. Shared Java
  renderings are duplicated, and same-path Clojure files can differ. Compare
  before deduplicating analysis; retain provenance for both artifacts.
- The retained corpus is not a runnable Datomic distribution. Use an original
  runtime as an additional oracle only if legitimately available and useful;
  do not make rebuilding the JVM distribution a prerequisite for this port.
- Atomic currently has one `atomic-core` crate, mostly flat `src/*.rs`, several
  `#[path]` inclusions, and flat user docs. Existing opaque PostgreSQL storage
  and prior repairs are useful work to evaluate, not proof of full fidelity.
- Archived goals are historical evidence, not active instructions. This plan
  owns the work. Only the scaffold has been created; no stage is yet complete.

## Learning and traceability contract

**Source coverage.** Account for every source file in both artifact trees. Walk
each distinct Datomic implementation and annotate every major namespace, class,
protocol, data structure and function. Explain purpose, callers/callees, data
flow, invariants, ownership of mutable state, coordination, failure behavior and
important cost/tradeoff decisions where relevant. Explain why the mechanism
exists and why the obvious alternative would change the design—not a line-by-line
English translation of syntax. Small helpers sharing one rationale may share a
note with explicit symbol coverage.

Interlace clearly marked `ATOMIC-NOTE` comments next to the relevant source.
Separate **observed mechanism**, **documented rationale**, **inferred rationale**
and **unknown**. Cite the evidence supporting a WHY; never invent the authors'
thought process. Mark decompiler artifacts and reconstruction uncertainty.
Reuse a canonical explanation for byte-identical copies/generated forwarding
scaffolding, with explicit counterpart links. Classify bundled libraries and
non-target backends and explain their role; inspect implementation details used
by the port without re-porting or exhaustively explaining unrelated dependencies.
No file disappears into an unrecorded “not relevant” bucket.

**Documentation coverage.** Walk every Pro-doc chapter. Give each important
behavioral paragraph, rule, table entry and example a stable locator and a trace
to the source that actually implements it: artifact/path, namespace/class and
symbol, section/line range, and baseline revision or equivalent stable identity.
Put clearly separated development trace notes beside the reference material,
or in chapter-local companions linked from those passages when notes would swamp
the text. Preserve the original wording and source URLs. A link to a namespace
alone is insufficient when it leaves the implementing path unexplained.

A trace can span multiple functions/components; do not force a one-to-one map.
Distinguish implementation, delegation to a dependency, documentation newer than
the recovered version, non-code operational guidance, explicitly non-target
integration, and unresolved evidence. Missing source evidence is not permission
to drop a documented core behavior. Explain and resolve consequential conflicts.
Link each adopted contract onward to its Rust owner and focused acceptance case.

**Economy.** Keep one navigable corpus/trace index plus the annotations and
chapter-local links, not competing audit reports and trackers. Reuse existing
explanations after checking them. Start with one representative end-to-end path
to calibrate useful depth, then process coherent components rather than repeatedly
reading the whole repository. Source understanding and doc mapping precede the
production redesign; annotations are maintained as the port reveals new facts.

## Ordered stages

### 1. Establish the source atlas — pending

- **Outcome:** A trustworthy map of what source exists, what is duplicated or
  generated, and how to read it without wasting work.
- **Focus:** Record the baseline and file provenance; identify canonical shared
  implementations and meaningful artifact differences. Pilot the annotation and
  trace method on entity-map submission through expansion, durable publication
  and a peer read. Expose current Rust correspondences without redesigning yet.
- **Completion signal:** Every source file has a classification/counterpart;
  the pilot has useful inline WHY notes and precise doc/source links, with
  evidence versus inference visible. The next component to explain is clear.

### 2. Explain the recovered implementation — pending

- **Outcome:** An interlaced, human-readable explanation of Datomic's actual
  component design and major algorithms.
- **Focus:** Walk the distinct source component by component and follow the
  important calls, not just filenames. Cover the information model, transaction
  expansion/application, log/index trees, novelty merging, query/rules, Pull and
  navigation, peer communication/caches, coordination, lifecycle and maintenance.
  Study Java/support implementations when they carry the mechanism.
- **Completion signal:** The atlas accounts for every file, major implementation
  symbols have explanations or explicit shared-note links, and a reader can trace
  writes, reads, indexing, recovery and maintenance through the source. Material
  unknowns are named with their consequences rather than filled with guesses.

### 3. Connect the Pro docs to source — pending

- **Outcome:** A passage-level map of user promises to implementing mechanisms,
  including functionality earlier audits overlooked.
- **Focus:** Traverse `00_start_here` through `09_optional`, including API details,
  examples and operational qualifications. Connect the docs to the annotated
  source, reconcile version differences and identify current Atomic deviations.
  Shared Java/Clojure API descriptions may share a semantic trace.
- **Completion signal:** Every important passage has an implementation trace or
  an explicit justified disposition. No relevant behavior remains silently
  unmapped; unresolved core gaps and architectural departures are actionable.

### 4. Derive the native component architecture — pending

- **Outcome:** A concrete, source-justified replacement layout and dependency
  direction, with a bounded cutover route for each current component.
- **Focus:** Separate peer and transactor packages and organize shared model,
  transaction, index/log, query, storage-provider and operational components into
  coherent Rust modules. Use a small Cargo workspace where package boundaries
  enforce real separation; do not create a crate per namespace. Shared Datomic
  code becomes shared Rust code, not copied peer/transactor implementations.
  Keep executables thin and peer reads independent of a runnable transactor.
  Derive the exact layout from the source atlas, not the current filename prefixes.
- **Completion signal:** The target tree and dependencies are recorded; every
  existing capability/module has a keep/adapt/replace/remove destination and
  source rationale. Native adaptations preserve the source mechanism or document
  a concrete reason to differ. The docs chapter layout is also decided.

### 5. Cut over shared foundations and write processing — pending

- **Outcome:** Shared data structures and the transactor implement the annotated
  design in the new organization, over minimal PostgreSQL storage primitives.
- **Focus:** Port or retain proven equivalents for values/schema/identity,
  persistent structures, codecs, transaction expansion and validation, serialized
  application, durability, receipts, publication, indexing, fencing and maintenance.
  Translate Clojure mechanisms into Rust ownership/types/iterators/concurrency
  while preserving their information flow and coordination boundaries. Remove
  superseded implementations and their callers as each component takes over.
- **Completion signal:** Source-to-Rust traces land on the new owners. Focused
  semantics and real fresh-database write/retry/restart/failover/maintenance checks
  pass, and inspected dependency/state flow matches the intended architecture.
  Pure/speculative and durable paths share the appropriate underlying machinery.

### 6. Cut over peers, queries and application interfaces — pending

- **Outcome:** The application has a coherent native peer library with Datomic's
  local computation model, backed by the same shared structures as the writer.
- **Focus:** Port database values and time views, selective index/log reads,
  novelty merging/caching, Datalog/rules, Pull/entity navigation, fulltext,
  transactions/reports, EDN, native application functions and current transport
  workflows. Preserve the source's algorithmic leverage and demand-driven work;
  a nested full scan is not equivalent to an indexed/set-oriented algorithm just
  because small examples agree. Remove replaced readers/evaluators/adapters.
- **Completion signal:** Documented examples and current application workflows
  run through the new peer/transactor organization. Held values, history,
  speculation, source composition and exact retries remain correct. Complete-path
  measurements test the claimed selective work and sharing, not isolated helpers.

### 7. Finish the documentation and full cutover — pending

- **Outcome:** One understandable, working product and one current set of user
  docs, with no abandoned implementation or unresolved core fidelity gap.
- **Focus:** Rebuild `docs/` into the Pro docs' ordered chapter hierarchy:
  `00_start_here`, `01_tutorials`, `02_core_concepts`, `03_schema`,
  `04_transactions`, `05_query_and_pull`, `06_indexes`, `07_peer_api`,
  `08_operations`, `09_optional`. Mirror meaningful subchapters; replace JVM API
  guides with Rust equivalents and explain non-target integrations without fake
  support or empty placeholder chapters. Separate development commentary from
  user instructions. Update README/rustdoc/examples/CLI links and remove flat
  superseded docs, dead code/dependencies, obsolete fixtures and test scaffolding.
  Port useful behavioral regressions; discard tests that only defend the replaced
  architecture. Do not retain old paths through compatibility aliases.
- **Completion signal:** A human can follow docs → annotated Datomic source →
  organized Rust implementation → working example/check. Fresh PostgreSQL-backed
  workflows, recovery, exact retries, failover, backup/restore and relevant
  semantic/algorithmic checks pass on the final tree. Structural review confirms
  the source-derived boundaries, link checks pass, and no obsolete engine path
  or unreported relevant gap remains. Reopen its owning stage for any failure.

## Completion and continuation

Completion requires all seven outcomes: the source learning and traceability
artifacts **and** the fully cut-over product. An annotated corpus, a prettier
directory tree, a scaffold or a green test count alone is not completion.

Current state: scaffold only. Begin Stage 1 by checking the reference READMEs and
manifests, recording the source baseline, and choosing the canonical paths for
the entity-map/write/read pilot. Leave unrelated worktree changes intact.
Maintain a concise continuation note here with the active component, material
findings, verified results and next action; do not append a session transcript.
