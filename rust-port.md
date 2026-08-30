Yes—this goal makes much more sense of the recovery work.

Assuming you want a native Rust implementation preserving Datomic’s core semantics and architecture, rather than an immediately drop-in wire-compatible replacement, we are ready to begin now.

We are not ready to port the entire system blindly. We are ready to build the first Rust vertical slice while extracting precise contracts from the recovered implementation.

## Current readiness

| Area | Readiness | What we have |
|---|---|---|
| Recovered implementation | High | Complete Peer and Transactor source corpus |
| Executable oracle | High | Runnable recovered PostgreSQL system without original fallback |
| Core architecture | High | Transaction, log, index, restart, cache, and HA flows mapped |
| Source navigation | High | Namespace, call, dependency, and subsystem indexes |
| Language-neutral specification | Medium-low | Knowledge exists, but much remains embedded in Clojure |
| Portable golden fixtures | Medium-low | Many probes exist, but they were built as validation gates rather than a Rust conformance suite |
| Storage-format compatibility | Low-medium | Implementations are recovered, but formats need explicit documentation and fixtures |
| Transport compatibility | Low | Artemis/Fressian protocol has not been packaged as a standalone compatibility specification |
| Rust design | Not started | We still need to decide what is invariant versus JVM/Clojure machinery |

The key assets are the [architecture handoff](/home/jake/Developer/atomic/datomic-rev/transactor/reports/recovered-system-architecture.md:31), [Peer source guide](/home/jake/Developer/atomic/datomic-rev/reports/source-guide.md:44), and the working recovered system. Goal 4 itself is now complete at its PostgreSQL boundary.

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

The existing probes are valuable raw material, but they need conversion from “gate passed” into small, readable conformance cases.

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

The next goal should no longer be “recover more Datomic.” It should be:

> Convert the recovered PostgreSQL core into a language-neutral executable specification, then implement and differentially validate a Rust semantic kernel beginning with values, schema, immutable indexes, transactions, and Database snapshots.
