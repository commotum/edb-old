# Rust port readiness and roadmap

The intended outcome is a native Rust implementation preserving Datomic's core
semantics and architecture, rather than an immediately drop-in wire-compatible
replacement. The project is ready to begin now.

We are not ready to port the entire system blindly. We are ready to build the first Rust vertical slice while extracting precise contracts from the recovered implementation.

## Current readiness

| Area | Readiness | What we have |
|---|---|---|
| Recovered implementation | High | Complete recovered Peer and Transactor source corpus |
| Executable oracle | Needs rebuilding | The broad recovery runner was intentionally removed; its proof remains in Git history |
| Core architecture | Medium-high | Central flows are known and visible in source, but the old generated handoff was removed |
| Source navigation | Medium | Organized namespaces and reusable analysis tools remain; campaign-generated indexes do not |
| Language-neutral specification | Medium-low | Knowledge exists, but much remains embedded in Clojure |
| Portable golden fixtures | Low | No active conformance fixtures; they must be written around Rust-facing questions |
| Storage-format compatibility | Low-medium | Implementations are recovered, but formats need explicit documentation and fixtures |
| Transport compatibility | Low | Artemis/Fressian protocol has not been packaged as a standalone compatibility specification |
| Rust design | Not started | We still need to decide what is invariant versus JVM/Clojure machinery |

The key active asset is now the recovered source itself. Recovery plans,
reports, generated indexes, large validation runners, packaged resources, and
compiled artifacts were deliberately removed so they cannot dictate the Rust
project by accident. The last pre-clean state is Git commit
`b8ebb1af74e6357d8242b2713af92f98d9e20e3a`; use it only to answer a narrow,
identified historical question.

The readiness labels are not completion percentages:

- **High** means there is enough trustworthy material to use this area while
  beginning the Rust implementation. It does not mean every optional feature
  or obscure branch is understood.
- **Medium-high** means the important model is understood, but some of its
  convenient explanatory scaffolding was intentionally removed and must now be
  rebuilt around the port.
- **Medium** means the raw material and ordinary tools are present, but a new
  reader must still trace and organize the relevant source.
- **Medium-low** means useful evidence exists, but it has not yet been turned
  into the small, precise contracts that a new implementation can follow.
- **Low-medium** means we can see the implementation and broad design, but
  important byte-level or compatibility details are still implicit.
- **Low** means the recovered system exercises the area, but a new independent
  implementation would still need substantial protocol discovery.
- **Not started** means the Rust-specific choices have not been made. It does
  not mean we know nothing about the problem being implemented.

### Recovered implementation — High

#### What this area means

An **implementation** is the actual program that performs the work, as opposed
to documentation that merely describes the work. The **recovered
implementation** is the source code reconstructed from Datomic's compiled JAR
files.

A **JAR** is a Java archive: a ZIP-shaped package containing JVM class files,
resources, and metadata. The **JVM**, or Java Virtual Machine, is the runtime
that executes Java bytecode. Clojure normally compiles to the same JVM
bytecode, so a Clojure program can be packaged in a JAR alongside Java code.

The recovered system has two main parts:

- The **Peer** is the library embedded in an application. It holds immutable
  database snapshots, runs queries locally, submits transactions, and learns
  about new database states.
- The **Transactor** is the service that serializes writes. "Serializes" here
  means that accepted transactions are placed into one authoritative order,
  not that they are merely converted into bytes. It validates transactions,
  assigns transaction time, publishes the durable log, and coordinates index
  work.

A **source corpus** is simply the complete collected body of source files for a
target. "Complete" here means that every expected Peer and Transactor namespace
or class has a recovered source representative, not that the files reproduce
the original authors' formatting and comments.

#### What we have

We have the complete recovered Peer and Transactor source trees needed by the
supported PostgreSQL runtime. The Peer contains all 142 recovered Peer
namespaces. The Transactor recovery contains all 247 Transactor Clojure source
files, the recovered Java sources, and the supporting namespaces needed to run
the service.

**Clojure** is a Lisp-family language. Its code is written as nested lists,
vectors, maps, and symbols. A Clojure **namespace** is roughly comparable to a
module in Rust: it groups related names and controls what code is imported.
The recovery campaign previously demonstrated that this source could be loaded
and executed without silently loading the original Peer or Transactor
implementation JARs. That was an important provenance check, but the active
tree no longer includes the dependency closure, resources, build scripts, or
runner needed to repeat it directly.

That last property is called **no original fallback**. It means a missing or
broken recovered function cannot accidentally appear to work because the JVM
found the licensed original class somewhere else on its classpath. A
**classpath** is the ordered list of JARs and directories from which the JVM
loads code.

Before the clean, the recovered Peer and Transactor also ran together against
PostgreSQL across transaction, log, index, restart, acknowledgement, and
bounded high-availability paths. That historical result gives us confidence
that the corpus describes a coherent system. It is not a claim that the
trimmed repository is currently runnable.

#### What this does not give us

The recovered files are not literally the unpublished source tree written by
the Datomic team. Compilation erased or transformed some information:

- comments and most formatting;
- some original local variable names;
- the exact macro forms the author wrote before expansion;
- some source organization and metadata;
- the reasoning behind a design choice;
- reliable information about which individual wrote a particular section.

Clojure uses **macros**, which are functions that transform code into other
code during compilation. Decompilation often recovers the expanded mechanism,
not the small, expressive macro form an author originally wrote. This is one
reason the recovered source can be correct but harder to read than ordinary
handwritten Clojure.

#### What is still missing for Rust

We do not need to recover more files before beginning Rust. We need to identify
which recovered code expresses essential Datomic behavior and which code is an
accident of Clojure, Java, the JVM, or the original libraries.

For example, the invariant "transactions have one authoritative order" must
survive the port. The precise Clojure queue, Java thread, or Artemis callback
used to achieve it does not necessarily need to survive.

The remaining work is therefore:

- annotate the core source by concept rather than only by filename;
- convert compiler-shaped sections into readable algorithms or pseudocode;
- identify dynamic Clojure features that need a Rust policy;
- investigate exact-AOT differences only when they affect an observed
  contract or differential test.

**AOT**, or ahead-of-time compilation, means compiling Clojure into JVM class
files before the program starts. Exact AOT parity asks whether newly compiled
class bytes match the original class bytes. That is useful forensic evidence,
but it is not a prerequisite for expressing the same behavior in Rust.

### Executable oracle — Needs rebuilding

#### What this area means

An **oracle** is an answer key. An **executable oracle** is a program we can run
to ask questions such as:

- What datoms result from this transaction?
- Which temporary IDs are assigned?
- Does this transaction succeed or fail?
- What is the exact query result?
- What does a database snapshot contain before and after restart?

A **datom** is Datomic's atomic fact. Conceptually it contains an entity, an
attribute, a value, a transaction, and whether the fact was added or retracted.
It is the small unit from which Datomic database state is built.

The recovered implementation can become a useful oracle because the recovery
campaign proved it could run without original-implementation fallback. To use
it now, however, we must build a fresh, narrow runner around the particular
behavior being specified.

#### What we have

We have the recovered Peer/Transactor source and historical proof that the pair
ran over PostgreSQL. The deleted pre-clean tree also contains focused probes
for transactions, query behavior, storage compare-and-swap, publication and
acknowledgement failures, restart, index adoption, transport recovery, and high
availability.

Those probes are not active dependencies of the Rust project. If one contains
an observation needed for a fixture, inspect or recover that one case from Git
history rather than reviving the old campaign harness.

**PostgreSQL** is the relational database used as durable storage in the
supported recovered configuration. "Durable" means the authoritative data is
expected to survive process restarts and machine failures once PostgreSQL has
committed it.

The active tree contains no runnable oracle or saved probe output. This is an
intentional cost of the clean, and rebuilding a small oracle is now prerequisite
work for differential testing.

#### What is still missing for Rust

The deleted oracle was optimized for proving broad recovery gates, not for
answering thousands of small porting questions. A **gate** is a pass/fail
validation boundary, often covering an entire operational scenario. The Rust
port needs a new, smaller oracle interface.

We need an oracle adapter that can:

1. start from a known small database;
2. accept a portable input document;
3. run exactly one transaction, query, or database operation;
4. normalize unstable details such as generated UUIDs and timestamps;
5. emit a small canonical result document;
6. reset cleanly for the next case.

**Canonical** means that equivalent results are always written in one stable
representation. For example, map keys may be sorted, generated IDs may be
replaced with stable labels, and unordered query results may be explicitly
ordered before comparison.

Without normalization, Rust and Clojure could behave identically but produce
different timestamps or randomly generated identifiers, causing a meaningless
test failure.

We also need the oracle runner to be versioned. If a recovered-source change
changes an expected result, we should know whether the semantic contract
changed or whether only the test formatting changed.

### Core architecture — Medium-high

#### What this area means

**Architecture** describes the important components of a system, the
responsibilities assigned to each component, and the rules governing how they
interact. It is a level above individual functions.

The mapped core includes:

- A **transaction**, which is a requested atomic change to the database.
  "Atomic" means it is accepted as one whole unit or rejected without a
  partial visible result.
- The **log**, which records accepted transactions in authoritative order.
- An **index**, which stores the same facts in an order that makes particular
  reads efficient. EAVT, for example, orders facts by entity, then attribute,
  then value, then transaction.
- **Restart and recovery**, which reconstruct usable state after a process
  exits and starts again.
- A **cache**, which keeps reusable derived or immutable data close to the
  process for speed. A cache must not become the only authoritative copy.
- **HA**, or high availability, which allows another Transactor process to take
  over when the active one fails while preventing both processes from
  remaining authoritative writers.

An architectural **flow** traces a meaningful operation across components. For
example, the transaction flow begins at the application, passes through the
Peer and transport, reaches the Transactor, publishes a log descriptor in
PostgreSQL, and eventually advances the Peer's database value.

#### What we have

The recovered source and the study summarized in this document expose the
central PostgreSQL state model and transaction spine, including:

- transaction submission and local Peer backpressure;
- message admission and result correlation;
- novelty checking and transaction ordering;
- speculative application before publication;
- immutable log-tail creation;
- authoritative descriptor compare-and-swap;
- result delivery and unknown outcomes;
- Peer basis advancement;
- persistent-index publication and adoption;
- restart catchup;
- coordination, takeover, and stale-writer fencing;
- cache and maintenance boundaries.

**Backpressure** means preventing a producer from submitting unlimited work
when the consumer cannot keep up. **Correlation** means matching an eventual
response to the original request. **Speculative** means work is prepared before
it becomes authoritative and may need to be discarded if publication loses a
race.

A **compare-and-swap**, abbreviated CAS, changes a value only if its current
revision still equals an expected revision. It is the basic mechanism used to
ensure that stale writers cannot silently overwrite newer authority. Clojure's
atomic-reference API names the equivalent primitive `compare-and-set!`, and
Java names its method `compareAndSet`, but Datomic's public transaction and
storage terminology is compare-and-swap.

#### What is still missing for Rust

Architecture diagrams and source maps do not yet constitute an implementation
specification. They tell us the major stages and invariants, but many exact
rules remain inside Clojure data structures and functions.

Before or during the Rust port, each architectural section needs to be reduced
to:

- input and output types;
- state owned by the component;
- state transitions;
- invariants that may never be violated;
- error and retry behavior;
- concurrency assumptions;
- small examples and oracle fixtures.

We also need to separate **strategy** from **mechanism**. A strategy is the
lasting design decision, such as "immutable values are stored separately from
small mutable selectors." A mechanism is the technology used in this version,
such as Clojure persistent maps, JDBC, or Artemis.

Rust should preserve a good strategy while being free to choose a better Rust
mechanism.

### Source navigation — Medium

#### What this area means

A large recovered codebase is difficult to study if the only available method
is text search. **Source navigation** means having maps that answer questions
such as:

- Which file defines this function?
- Which functions call it?
- Which namespaces does this namespace depend on?
- Which files participate in transaction processing?
- Where does a message cross from the Peer into the Transactor?

Important terms include:

- A **namespace** is a named Clojure module.
- A **Var** is Clojure's named, dynamically bindable reference to a function or
  value. A function name such as `datomic.api/transact` identifies the Var
  `transact` in namespace `datomic.api`.
- A **call graph** is a map from functions to the functions they invoke.
- A **dependency graph** is a map of which modules require other modules.
- A **subsystem index** groups files by a larger responsibility such as query,
  transaction, log, index, or storage.
- A **protocol** in Clojure is a named set of operations that different types
  can implement. It is similar in purpose to a Rust `trait`, although the
  runtime and dispatch rules differ.

Rust **traits** define behavior that multiple Rust types can implement. Rust
uses static types and often resolves trait calls at compile time, while Clojure
is dynamically typed and performs more decisions at runtime.

#### What we have

The source is separated into Peer and Transactor trees and organized by
Clojure namespace and Java package. Ordinary text search works well, and the
handwritten Peer Java boundary has a retained manifest.

The old analyzer and its generated call, reference, keyword, class-closure,
and dependency indexes were removed. So were the curated recovery guide and
architecture handoff. They were useful products of the recovery campaign, but
retaining them would encourage a new agent to inherit that campaign's
categories instead of building the conceptual map needed by the Rust port.

#### What is still missing for Rust

Static indexes cannot perfectly see dynamic behavior. Clojure can store
functions in maps, look up Vars by name, dispatch through protocols or
multimethods, and construct callbacks at runtime. A **multimethod** is a
function whose implementation is selected by a dispatch value rather than by
only the receiver's concrete type.

We need a Rust-port navigation layer that adds:

- one conceptual name for each important mechanism;
- links from that concept to every relevant Peer and Transactor function;
- links from the concept to its language-neutral specification;
- links from the specification to Rust modules and tests;
- notes for dynamic calls that the generated call graph cannot resolve;
- a distinction between authored concepts and compiler-generated helpers.

This would let a reader move in both directions:

```text
Datomic concept -> recovered evidence -> portable contract -> Rust code
Rust code -> contract -> recovered evidence and oracle fixtures
```

### Language-neutral specification — Medium-low

#### What this area means

A **language-neutral specification** describes what the system means and does
without describing it only in Clojure or only in Rust.

For example, the Clojure expression used to find a datom is not the contract.
The contract is the accepted input, the ordering rules, the returned datoms,
and the possible errors. That contract could be implemented in Clojure, Rust,
Java, or another language.

This distinction matters because Clojure and Rust encourage very different
programming styles:

- Clojure is **dynamically typed**. Values carry their types at runtime, and a
  local name does not usually declare one fixed compile-time type.
- Rust is **statically typed**. The compiler normally knows the precise allowed
  types before the program runs.
- Clojure frequently uses generic maps, lists, vectors, keywords, and `nil`.
- Rust usually represents important alternatives explicitly using `struct`
  and `enum` definitions.
- A Rust `struct` is a named group of fields.
- A Rust `enum` defines a closed set of possible variants, where each variant
  may carry different data.
- Clojure `nil` often means "no value," while Rust normally uses `Option<T>`,
  which is either `Some(value)` or `None`.
- Clojure commonly throws exceptions. Rust commonly returns `Result<T, E>`,
  which is either a successful value `T` or an error `E`.

A port cannot safely translate every generic Clojure map directly into a Rust
map. It must discover the real variants and fields and represent them
explicitly.

#### What we have

We have high-level contracts and source evidence for the central transaction,
storage, index, recovery, and HA flows. We know many important invariants, such
as:

- database values are immutable snapshots;
- transaction time advances in one authoritative order;
- immutable payloads are published behind revisioned selectors;
- result delivery is distinct from durable publication;
- indexes and caches are derived state rather than transaction authority;
- stale Transactors must lose authority and fence themselves.

The recovered source also contains the exact behavior needed to answer more
detailed questions.

#### What is still missing for Rust

Much of the exact contract remains expressed only as Clojure conditions, maps,
metadata, Java calls, and exception data. We need to specify at least:

- the complete value algebra and value ordering;
- entity, attribute, transaction, and temporary ID rules;
- schema representation and evolution;
- every accepted transaction input form;
- lookup references, upsert, uniqueness, cardinality, component semantics,
  compare-and-swap, retraction, and entity retraction;
- exact datom ordering in EAVT, AEVT, AVET, and VAET/RAET;
- immutable database views such as `as-of`, `since`, history, and filtering;
- query input and result shapes;
- error categories and the data attached to each error;
- publication, acknowledgement, retry, and unknown-outcome rules;
- the supported extension policy for transaction and query functions.

An **algebra** here means a defined set of value variants plus the legal
operations and comparison rules for those variants. It does not mean advanced
mathematics is required to read the specification.

The specification should combine prose, type-like definitions, state diagrams,
pseudocode, and executable examples. **Pseudocode** is deliberately simplified
code used to explain an algorithm without requiring the reader to know the
syntax of a particular language.

This area is medium-low because the knowledge is available, but extracting it
is real work. We should do that extraction incrementally alongside Rust rather
than attempt to write a perfect specification of all Datomic features first.

### Portable golden fixtures — Low

#### What this area means

A **test fixture** is a saved input and the setup needed to run a test. A
**golden fixture** also stores the known-correct output, called the golden
result.

For example:

```text
Input:
  schema with :person/email as a unique identity
  transaction adding the same email through two temporary IDs

Golden result:
  one resolved entity ID
  exact asserted datoms
  exact temporary-ID mapping
  final basis t
```

**Portable** means the fixture does not depend on Clojure objects, JVM class
names, local temporary paths, or one PostgreSQL installation. Both the Clojure
oracle and Rust implementation must be able to read it.

**EDN**, or Extensible Data Notation, is a data format closely related to
Clojure's literal syntax. It supports maps, vectors, sets, keywords, symbols,
numbers, strings, and tagged values. It is often a better fit than JSON for
Datomic-shaped data, although JSON can be useful for wider tooling support.

#### What we have

The active repository has no golden fixtures. The recovery campaign once had
many focused probes and large evidence manifests, but they were broad proof
artifacts rather than a catalog of small, independent semantic examples. They
were removed to prevent recovery-specific acceptance rules from becoming the
port's test design by default.

A **probe** is a small program that exercises a particular behavior and records
what happened. Historical probes can still be inspected at the pre-clean
commit when a specific transaction, restart, indexing, transport, or takeover
case needs provenance.

#### What is still missing for Rust

We need to split broad probes into small fixtures that each teach and test one
rule. A useful conformance suite needs:

- positive cases that should succeed;
- negative cases that should fail;
- exact expected error categories and error data;
- stable ordering rules;
- normalization for generated entity IDs, UUIDs, transaction times, and wall
  clock timestamps;
- a declared initial schema and database state;
- a machine-readable expected result;
- a short plain-English explanation of the rule;
- provenance linking the fixture to recovered source and oracle output.

A **conformance suite** is a collection of tests used to determine whether an
independent implementation follows a specification. A **differential test**
runs the same case against two implementations and compares their normalized
outputs.

The missing harness should support a loop like this:

```text
fixture
  -> recovered Clojure oracle -> canonical result A
  -> Rust implementation      -> canonical result B
  -> structural comparison of A and B
```

The first suite should emphasize the semantic kernel: values, datom ordering,
schema, temporary IDs, uniqueness/upsert, transaction application, immutable
database snapshots, and basic queries. Operational HA fixtures should be
created later, when the Rust implementation reaches that layer.

### Storage-format compatibility — Low-medium

#### What this area means

**Storage semantics** describe what must be durable and authoritative.
**Storage format** describes the exact keys, rows, byte encodings, and metadata
used to store it. Two systems can have the same semantics while using
incompatible storage formats.

For example, both systems may preserve an append-only transaction log, but one
may encode a log segment with Fressian while the other uses a Rust-specific
binary format. They implement the same strategy but cannot open the same
database files or PostgreSQL rows.

**Serialization** is the conversion of in-memory values into bytes that can be
stored or transmitted. **Deserialization** reconstructs values from those
bytes. Fressian is the principal binary serialization format used in these
Datomic paths.

#### What we have

We have recovered implementations for PostgreSQL key/value access, immutable
value storage, revisioned references, log descriptors, log segments,
persistent-index roots, and Fressian reading and writing. Runtime evidence shows
those pieces composing correctly through publication, restart, indexing, and
HA.

We understand the main storage strategy:

- large values are immutable once written;
- small root or descriptor records identify the currently authoritative
  immutable lineage;
- descriptor changes use revision-checked CAS;
- persistent indexes are published as new immutable structures and then
  referenced by a mutable root;
- restart follows the current roots and replays only the referenced log tail.

This strategy is one of the most valuable things to preserve in Rust.

#### What is still missing for Rust

If Rust must open the same PostgreSQL database or interoperate with JVM
components at the storage layer, we need an explicit byte-level specification:

- PostgreSQL table and column meanings;
- key naming and namespace rules;
- mutable-row revision semantics;
- exact Fressian tags and handlers;
- integer, string, UUID, instant, URI, keyword, symbol, datom, and collection
  encodings;
- log descriptor and log-segment layouts;
- persistent-index node and root layouts;
- compression, encryption, checksums, and version markers;
- canonical ordering of maps, sets, and datoms where encoding depends on
  order;
- behavior when data is missing, corrupt, stale, or from another version;
- crash boundaries for immutable writes and subsequent root publication.

We then need **golden byte fixtures**: small values and structures paired with
their exact encoded bytes, plus PostgreSQL row fixtures captured from known
transactions.

If the first Rust target is behavior-compatible but not storage-compatible, we
can postpone most byte parity. We should still preserve the architectural
split between immutable content and revisioned roots so that a compatibility
adapter can be added later.

### Transport compatibility — Low

#### What this area means

**Transport** is the mechanism that carries messages between processes.
**Protocol** is the agreed meaning, structure, ordering, and lifecycle of those
messages. **Serialization** is how each message is represented as bytes.

These are separate decisions. A transaction protocol could be carried over
Artemis, TCP, QUIC, or an in-process channel, and it could use Fressian, JSON,
or another encoding.

The recovered system uses:

- **Apache ActiveMQ Artemis**, a message broker that provides queues,
  connections, sessions, producers, consumers, and acknowledgements;
- **Fressian**, the binary encoding used for many message bodies;
- coordination records that tell Peers which Transactor endpoint is currently
  authoritative.

An **acknowledgement**, or ACK, is a signal that a layer has accepted or
processed something. Datomic has several different acknowledgement boundaries:
broker admission, durable log publication, and delivery of the transaction
result are not the same event.

#### What we have

We have recovered Peer and Transactor transport code and integrated evidence
for:

- endpoint discovery;
- Peer connection and reconnection;
- transaction request submission;
- request/result correlation;
- queue admission and backpressure;
- result notification;
- unavailability and unknown outcomes;
- failover from one Transactor endpoint to another;
- the durable-publication versus result-delivery failure boundary.

This is enough to understand the transport's role in the architecture.

#### What is still missing for Rust

It is not yet enough to write an interoperable Rust Peer or Transactor. That
requires a standalone protocol specification containing:

- connection and session setup;
- endpoint naming and discovery;
- queue and address names;
- every message type and field;
- exact Fressian handlers and byte encodings;
- request IDs and correlation rules;
- transaction request and result schemas;
- message ordering guarantees;
- delivery and broker acknowledgement modes;
- timeout, cancellation, retry, and reconnect rules;
- authentication and transport-security behavior;
- compatibility/version negotiation;
- malformed-message and partial-failure behavior.

We also need captured, redacted wire fixtures and a small protocol exerciser
that can test one side independently.

For the recommended first target, Rust does not need Artemis compatibility.
It can initially run in one process or use a simpler Rust-facing protocol. The
design must still preserve the semantic boundaries between submission,
publication, acknowledgement, and result delivery. Exact Artemis/Fressian
interoperability can be a later adapter or milestone.

### Rust design — Not started

#### What this area means

The recovered system tells us what Datomic does and how its Clojure/JVM version
does it. **Rust design** decides how to express the same important contracts in
Rust.

This should not be a line-by-line translation. Clojure and Rust solve many
programming problems differently:

- The JVM uses garbage collection to reclaim objects automatically. Rust uses
  **ownership** and **borrowing** rules checked by the compiler to determine
  when memory can be safely released.
- Clojure uses persistent immutable collections extensively. "Persistent"
  here means a new version can share most of its internal structure with the
  old version; it does not mean disk persistence. Rust can use immutable
  structures, copy-on-write structures, or reference-counted sharing.
- `Arc<T>` in Rust is an atomically reference-counted shared pointer. It lets
  multiple threads or snapshots share ownership of immutable data.
- Clojure commonly uses dynamically dispatched protocols, Vars, functions in
  maps, and generic data. Rust commonly uses `trait`, `struct`, and `enum`
  definitions with explicit types.
- Clojure/JVM code uses threads, executors, futures, callbacks, and core.async
  channels. Rust can use operating-system threads, synchronous channels, or an
  asynchronous runtime such as Tokio.
- **Async** programming lets one thread make progress on other work while an
  operation is waiting for I/O. It is not automatically faster, and it should
  be used only where the architecture benefits from it.

#### Invariant versus machinery

An **invariant** is a rule that must always remain true. **Machinery** is the
particular implementation used to maintain that rule.

Examples:

| Invariant to preserve | Original machinery that may change in Rust |
|---|---|
| Accepted transactions have one authoritative order | Clojure queues, processor loops, Java executors |
| Database values are immutable snapshots | Clojure persistent maps and JVM objects |
| Stale writers cannot publish a newer root | JDBC storage calls and expected-revision CAS |
| Caches do not own authority | Caffeine/Guava-style cache implementations |
| Publication and result delivery are distinct | Artemis sessions, queues, and callbacks |
| A Peer can retain an older database value while newer values exist | JVM object identity and shared persistent structures |

Rust should normally preserve the left column and reconsider the right column.

#### What we have

We have enough information to identify the core invariants and the major state
machines. A new executable oracle must be built before Rust behavior can be
differentially tested.

The architecture suggests useful Rust directions, such as:

- explicit `enum` variants for Datomic values and transaction operations;
- immutable database snapshots sharing index structures through `Arc` or a
  persistent collection library;
- ordered index keys with one carefully specified total ordering;
- `Result`-based error categories rather than loosely typed exceptions;
- storage traits separating immutable values from revisioned roots;
- a single explicit transaction-ordering task or state machine;
- caches keyed only by immutable identities or validated revisions.

These are directions, not settled design decisions.

#### What is still missing for Rust

We need explicit decisions and short **architecture decision records**, or
ADRs. An ADR records the problem, alternatives considered, decision, and
consequences so that later changes do not accidentally violate an invariant.

The first ADRs should decide:

- the initial compatibility target;
- crate and module boundaries;
- the `Value`, `Datom`, entity-ID, attribute-ID, and transaction-ID types;
- total value and datom ordering;
- schema representation;
- immutable snapshot and structural-sharing strategy;
- the four in-memory index representations;
- transaction input and normalized operation types;
- error categories and diagnostic data;
- synchronous versus asynchronous transaction processing;
- extension policy for transaction functions and query predicates;
- EDN and Fressian library policy;
- PostgreSQL client and transaction/CAS strategy;
- cache ownership and eviction boundaries;
- observability, metrics, and tracing;
- public API shape;
- testing and differential-fixture layout.

A Rust **crate** is a compilation and packaging unit, roughly comparable to a
library or executable project. A crate can contain many Rust modules. Choosing
crate boundaries too early can create unnecessary complexity, so the first
implementation can begin with a small workspace and split only when real
ownership boundaries emerge.

"Not started" therefore means that these language-specific choices have not
been committed to code. It does not block a first vertical slice. The first
slice is precisely how we can test and refine these choices without designing
the entire system in advance.

### Revised practical assessment

We are ready to begin a Rust semantic kernel, but the first work should combine
specification, oracle fixtures, and implementation rather than treating them as
three separate projects.

The immediate sequence should be:

1. Write a short compatibility charter stating that the first milestone is
   behavior-compatible, not yet storage- or wire-compatible.
2. Define portable `Value`, `Datom`, schema, transaction-input, transaction-
   result, query-result, and error documents.
3. Build a small recovered-system oracle adapter that reads one fixture and
   writes one canonical result.
4. Create the first focused fixtures for value ordering, datom ordering,
   schema installation, temporary IDs, uniqueness/upsert, cardinality,
   assertions, retractions, and basic queries.
5. Create a minimal Rust workspace implementing only the types and pure
   in-memory behavior needed by those fixtures.
6. Run every fixture against both systems and investigate each disagreement at
   the smallest implicated recovered-source boundary.

This sequence lets the specification become more precise because Rust forces
implicit Clojure alternatives into explicit types. At the same time, the
oracle prevents the Rust type design from quietly changing Datomic semantics.

## Exact JAR parity is no longer the highest-value prerequisite

Exact reconstruction would increase confidence in obscure branches, but Rust will not reproduce Clojure AOT bytecode anyway. What the Rust effort needs is:

- exact observable semantics;
- explicit invariants;
- representative edge cases;
- storage and message formats where compatibility matters;
- an executable oracle for differential testing.

We already have enough source to discover strategies and architecture. Remaining exact-AOT differences can be investigated when a differential Rust test exposes ambiguity.

## First decision: compatibility target

Before substantial implementation, define which of these you want:

1. **Datomic-inspired Rust database**  
   Preserve the important ideas but freely change APIs, storage formats, and protocols.

2. **Behavior-compatible PostgreSQL core**  
   Match Datomic transaction, database-value, query, and durability semantics, without initially matching its network or storage bytes.

3. **Storage- and wire-compatible implementation**  
   Rust Peers or Transactors can interoperate with recovered/JVM components and existing databases.

4. **Drop-in replacement**  
   Match APIs, messages, encodings, operational behavior, and obscure edge cases.

I recommend target 2 first, designed so target 3 remains possible. It gives you the intellectual substance without beginning with Artemis, JVM interop, or binary-format archaeology.

## What we need next

### 1. A language-neutral value model

Specify every value Rust must represent:

- entity, attribute, and transaction identifiers;
- keywords, symbols, strings, booleans, integers, floats, decimals;
- instants, UUIDs, URIs, byte arrays;
- references and lookup refs;
- tuples;
- total ordering and equality;
- cardinality, uniqueness, identity, components, and indexing flags.

This should become both a Rust `Value` algebra and an EDN-based fixture format.

### 2. An executable transaction specification

Extract the transaction pipeline into named stages:

```text
transaction input
  -> normalize forms
  -> resolve tempids and lookup refs
  -> identify schema changes
  -> validate types/cardinality/uniqueness
  -> perform identity upsert
  -> apply CAS/retract/retractEntity/component rules
  -> derive asserted and retracted datoms
  -> allocate transaction t
  -> produce Database-after and transaction report
  -> publish durably
```

For every stage, capture successful cases, rejection cases, error categories, and invariants.

### 3. Canonical differential fixtures

Turn the recovered system into an oracle with fixtures such as:

```text
initial schema + database
transaction input
expected datoms
expected tempid map
expected basis
expected transaction report
expected error
expected query results
```

The Rust implementation should consume the same fixture and emit canonical EDN or JSON for comparison.

Historical probes may be useful evidence for individual cases, but the new
fixtures should be designed from the contract outward rather than converted
wholesale from “gate passed” recovery checks.

### 4. Index semantics

Specify the four principal orderings:

- EAVT
- AEVT
- AVET
- VAET/RAET

For each one, capture:

- exact key ordering;
- range and seek behavior;
- history representation;
- retractions;
- value comparison;
- uniqueness and lookup behavior.

These indexes are central to both query performance and immutable Database values.

### 5. Database-value semantics

Before networking, Rust should reproduce:

- immutable snapshots;
- basis `t`;
- `as-of`;
- `since`;
- history;
- filtering;
- entity lookup;
- datoms/index-range;
- transaction reports;
- advancing from one immutable value to another.

This is one of the most important Datomic ideas and an excellent first Rust milestone.

### 6. Query semantics

Do not begin by porting the entire recovered query engine. Build upward:

1. basic data patterns;
2. joins;
3. predicates and functions;
4. rules;
5. aggregates;
6. collection/tuple/relation bindings;
7. pull;
8. timeouts and cancellation.

Use differential fixtures for every feature. Decide explicitly how Rust will handle arbitrary Clojure predicates and query functions—they cannot simply be carried across.

### 7. Durable model and publication boundary

Document and fixture:

- immutable stored values;
- revisioned mutable selectors;
- expected-revision CAS;
- log-tail descriptor structure;
- transaction payload encoding;
- index-root structure;
- restart replay;
- publication versus acknowledgement;
- unknown transaction outcomes.

This is where a Rust implementation should preserve invariants without necessarily copying JVM mechanisms.

### 8. Dynamic-feature policy

Some Datomic features assume a Clojure runtime:

- transaction functions;
- arbitrary query predicates/functions;
- Java objects and interfaces;
- custom Fressian handlers;
- dynamically loaded code.

For each, choose:

- implement an equivalent Rust extension interface;
- provide a restricted expression language;
- support only predefined functions;
- postpone it;
- declare it incompatible.

This decision is more important than exact AOT parity.

## Recommended Rust build order

```text
1. Value algebra and datom ordering
2. Schema representation
3. Four in-memory indexes
4. Immutable Database snapshots
5. Pure transaction application
6. Differential transaction fixture runner
7. Basic Datalog engine
8. In-memory append-only log
9. PostgreSQL immutable-value and CAS storage
10. Durable publication and restart
11. Persistent index construction/adoption
12. Client/service protocol
13. Acknowledgement fault handling
14. HA and stale-writer fencing
```

The first meaningful milestone should be:

> Given an EDN schema and transaction, Rust produces the same datoms, tempids, basis, immutable Database value, and basic query results as the recovered system.

That milestone avoids networking and PostgreSQL while testing the conceptual heart of Datomic.

## How the Rust port becomes the study guide

For every subsystem, write a short design note with:

- **Observed contract:** what the recovered system does.
- **Invariant:** what must remain true.
- **Original mechanism:** how Datomic implements it.
- **Design interpretation:** why that mechanism appears useful.
- **Rust translation:** what Rust should preserve and what it should change.
- **Oracle fixtures:** cases proving the translation.

For example:

```text
Invariant:
  Caches never own authoritative mutable state.

Datomic mechanism:
  Immutable UUID-addressed values may be cached, while log and index roots
  are read and changed through revisioned storage operations.

Rust translation:
  Cache Arc<Value> by immutable content ID. Never cache a mutable root without
  revision validation.

Evidence:
  Exact recovered source anchors plus restart and failover fixtures.
```

That process will reveal the architectural thinking far better than transliterating Clojure line by line.

## Bottom line

We have completed enough reverse engineering to start the Rust implementation. I would describe us as:

- ready for the semantic kernel and differential harness;
- nearly ready for the PostgreSQL transaction/log vertical slice;
- not yet ready for wire-compatible Peer/Transactor interoperability;
- far from a complete drop-in replacement.

The next phase is no longer “recover more Datomic.” It is:

> Convert the recovered PostgreSQL core into a language-neutral executable specification, then implement and differentially validate a Rust semantic kernel beginning with values, schema, immutable indexes, transactions, and Database snapshots.
