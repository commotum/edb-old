# Datomic Pro 1.0.7277 Architecture

This document summarizes the major components of the classic Datomic Pro
architecture being recovered in Goal 2.

## System overview

```text
Application
    | query / transact
    v
Peer library
    | transaction requests, synchronization, notifications
    |             (Artemis + Fressian transport)
    v
Active Transactor ------ coordination and fencing ------ Standby Transactor
    |
    | durable log, values, catalog, and index roots
    v
PostgreSQL
```

The essential data-plane loop is:

> Peer submits a transaction -> Transactor validates and commits it ->
> PostgreSQL persists it -> Transactor notifies the Peer -> Peer adopts the
> log and index state -> application queries a new immutable database value.

## Major components

### 1. Peer

The Peer is embedded in the application process. It holds immutable database
values, executes Datalog queries, caches data, reads indexes and log state, and
submits transactions. It does not authoritatively commit writes.

### 2. Transactor

The Transactor is the single authoritative writer. It validates transactions,
resolves tempids and lookup references, enforces uniqueness, CAS, and schema
rules, executes transaction functions, assigns basis `t`, and publishes durable
commits.

### 3. Durable storage

PostgreSQL is the primary backend for Goal 2. Datomic stores opaque key/value
records representing database identity, catalog state, transaction-log
structures, values, coordination state, and index-root references. PostgreSQL
provides durable storage; Datomic provides the data model and semantics around
it.

### 4. Transaction log

The transaction log is the ordered history of committed transactions. The
Transactor writes it, and Peers replay its tail to advance from an older indexed
basis to the current database value.

### 5. Persistent indexes

Datomic maintains EAVT, AEVT, AVET, and VAET/reverse-reference indexes.
Background indexing converts accumulated log data into durable index structures
and publishes new index roots. Indexing is a logical subsystem, not necessarily
a separate server.

### 6. Peer-Transactor transport

Artemis messaging and Datomic's Fressian protocol carry transaction
submissions, results, notifications, synchronization, errors, and reconnection
state between Peers and the Transactor.

### 7. Catalog and coordination

The catalog and coordination layers track databases, storage identities, active
endpoints, revisions, log and index roots, heartbeats, and ownership. They
support discovery and ensure that only one Transactor is authoritative.

### 8. High availability

Active and standby Transactors use heartbeats, fencing, semaphores, and takeover
protocols. The central invariant is one writable leader with monotonic
transaction history and no split brain.

### 9. Operational subsystems

Operational facilities include backup and restore, stored-value garbage
collection, excision and repair, full-text search, caches, metrics, logging,
licensing, authentication, diagnostics, and provisioning.

### 10. Optional access layers

Optional interfaces include Peer Server and the thin Client API, REST, Presto
integration, and Console. Alternative storage backends include DynamoDB,
Cassandra, H2, and Hot Rod; Goal 2 defers these until the PostgreSQL-backed core
is stable.

## Deployment roles and boundaries

- The application and Peer normally share a process.
- The active Transactor owns authoritative transaction ordering and commit.
- A standby Transactor participates in coordination and takeover but must not
  become a concurrent writer.
- PostgreSQL is an external storage service, while Datomic's SQL, key/value,
  catalog, coordination, log, and index semantics live in the recovered Peer and
  Transactor code.
- Log writing, persistent indexing, transport, lifecycle, backup, and
  maintenance are logical subsystems rather than additional mandatory
  data-plane servers.

## Goal 2 recovery boundary

The recovered candidate runtime must not load the original Peer or Transactor
AOT implementations. Licensed originals may be used only as isolated,
fingerprinted evidence or behavioral oracles. PostgreSQL and third-party
libraries such as JDBC, Artemis, Lucene, and Jetty remain external machinery;
the recovery focuses on the Datomic-specific semantics around them.
