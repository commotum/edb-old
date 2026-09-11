# EDN data frontend

Atomic accepts EDN data without Clojure or a JVM. Its reader produces native data;
transaction, query and pull adapters pass that data to the existing semantic
engine. Entity maps were already supported by the typed Rust API. EDN adds the
text authoring/interchange layer, not a new storage format or database engine.

`query`, `pull` and `with` also accept `--repository PATH` for
[direct offline backup reads](backup-reads.md). No PostgreSQL credentials or
restore is needed; file/stdin input and EDN results are unchanged.

## Try the executable

Build `cargo build --release --bin atomic`. Configure PostgreSQL and explicit
transport as described in [the application guide](application.md). Use
administrative credentials for migration/creation, a writer role for the
transactor, and a peer role for application reads/submission. The data commands
never migrate, create a database, or start an embedded writer implicitly.

```sh
target/release/atomic migrate
target/release/atomic create --database edn-demo
```

Start the ordinary transactor in a separate terminal. Its endpoint directory
must already be private and owned by you:

```sh
mkdir -m 700 /tmp/atomic-edn-demo
target/release/atomic transactor --database edn-demo --endpoint /tmp/atomic-edn-demo/writer.sock
```

The supplied [schema](../examples/edn/schema.edn) and
[people](../examples/edn/people.edn) are complete transaction inputs:

```sh
target/release/atomic transact --database edn-demo --endpoint /tmp/atomic-edn-demo/writer.sock --request-key install-person-schema --file examples/edn/schema.edn
target/release/atomic transact --database edn-demo --endpoint /tmp/atomic-edn-demo/writer.sock --request-key create-alice --file examples/edn/people.edn
target/release/atomic query --database edn-demo --file examples/edn/names-query.edn
target/release/atomic pull --database edn-demo --file examples/edn/person-pull.edn --entity '[:person/email "alice@example.com"]'
```

All four commands produce EDN on stdout. `--file -` reads stdin instead. Query
`--inputs PATH` contains a vector of the non-source `:in` arguments, in order,
including `%` rules and pull-pattern parameters. Only one input file can use
stdin. `query` supports `--as-of`, `--since`, and `--history`; `pull` supports
point-in-time `--as-of`/`--since` views. Reads execute in this process and do not
require the transactor to be running.

`atomic with --database edn-demo --file PATH` previews a transaction locally. It
returns speculative transaction data/tempids with `:atomic/committed false`; it
does not reserve identities, write facts, or create a durable receipt. Supply
`--tx-instant MILLIS` for a reproducible explicit preview time. To query the
speculative database value itself, use the library report's `db_after`.

`transact --remote` uses existing verified-TLS discovery and credential files in
place of `--endpoint`. There is no new network protocol service or plaintext TCP
mode. Text is converted to the additive native submission representation before
delivery through the normal transport.

## Transactions and exact retries

```edn
;; No :db/id: assert facts about a new/upserted identity.
[{:person/name "Alice" :person/email "alice@example.com"}]

;; Existing identity: change only the facts explicitly asserted.
[{:db/id [:person/email "alice@example.com"] :person/name "Alicia"}]

;; Equivalent authoring with a primitive form.
[[:db/add [:person/email "alice@example.com"] :person/name "Alicia"]]
```

Omitted attributes are not retractions. Lists/vectors of many-values, nested
reference maps, reverse attributes, tuple slots, string tempids, idents, lookup
references and transaction metadata reuse the native normalizer. Nested entities
must satisfy the existing component/uniqueness rules; explicit child IDs do not
waive those rules. Schema is ordinary map data; install attributes before using
them, following the engine's db-before rules. A vector is not guessed to be a
lookup reference solely because it has two elements.

For a cardinality-many reference attribute, wrap a lookup reference in its outer
collection: `:person/friends [[:person/email "bob@example.com"]]`. For a
cardinality-one reference, use the lookup reference directly. Scalar
cardinality-one attributes do not silently unwrap singleton lists/vectors.

Supported built-in transaction forms and native program invocations use the
existing implementation. The reserved string `"datomic.tx"` identifies the
transaction entity. JVM classpath functions and Clojure function definitions are
not evaluated. A `#atomic/function` hash identifies already stored native program
content; reading the tag does not deploy or invoke it.

Qualified versioned symbol calls can invoke an explicitly deployed
[native Rust transaction registry](application-computation.md). Custom aggregates
and portable count/quot/subs/str/string tests use the ordinary query adapter.
The stock CLI does not load native code; use a compiled application transactor
for transaction callbacks and supply query registries in a Rust application.

`transact` requires a caller-supplied `--request-key`. Retry the same request and
options with the same key after a lost response. Whitespace/comments, map/set
order and outer transaction order do not change EDN request identity. Ordered
list/vector contents and numeric representations remain request data. Equivalent
EDN and typed authoring are not promised to have identical request digests;
continue retrying a request through its original representation.

EDN intent remains schema-independent until authoritative transaction processing.
Retained receipt lookup happens before current schema, idents or program bindings
are resolved. Reusing a key with different intent produces a conflict, not a
second transaction. Existing typed submission hashes and durable value encodings
are unchanged; only EDN-containing submissions select the new input grammar.

Successful commit output includes `:atomic/committed`, `:atomic/basis-t`,
`:atomic/replayed`, transaction hash, exact before/after basis, datoms and tempids.
If opening/formatting a full report fails after confirmed commit, a minimal
confirmed receipt is returned with `:atomic/report-error`. Output/I/O failure is
not proof of rollback. Retain the key; do not invent a new one to recover a reply.

## Native library

```rust
use atomic_core::{Database, TransactionRequest};
use atomic_core::edn::{read_edn, write_edn};
use atomic_core::edn_query::{EdnQueryArgument, parse_query_edn};

let data = read_edn(r#"[{:person/name "Alice"}]"#)?;
let text = write_edn(&data)?;
let request = TransactionRequest::from_edn("create-alice", &text)?;
// Send request with the existing Connection/TransactionClient APIs.

let empty = Database::bootstrap()?;
let installed = empty.with_edn(include_str!("../examples/edn/schema.edn"), 10)?;
let preview = installed.db_after.with_edn(&text, 20)?;
let query = parse_query_edn("[:find ?name :where [?e :person/name ?name]]")?;
let bound = query.bind(&[EdnQueryArgument::Source(
    atomic_core::QuerySourceValue::Database(preview.db_after.database_value()),
)])?;
let result = bound.execute(&atomic_core::QueryControl::default(), None)?;
let text_result = write_edn(&bound.result_to_edn(&result.result)?)?;
```

`edn_query` supports query map/list/vector forms, ordered explicit inputs, rules,
find shapes/aggregates, `:with`, return maps, predicates/functions, negation,
disjunction and query pull. `BoundEdnQuery` retains a prepared query for reuse;
execution still takes the original immutable sources and normal query controls.
Native extension functions are supplied through `QueryExtensions`, not resolved
from a JVM namespace. `edn_pull` supplies pattern conversion, entity identifiers
and an explicit `EdnPullTransforms` registry. The same pattern can be built as data
or read from text. Literal static subqueries use the existing native subquery
engine; this does not add evaluation of arbitrary dynamic Clojure forms.
Nested static queries can receive enclosing rules and pattern arguments. Dynamic
query-template variables and nested return-map materialization are explicitly
rejected where the existing typed subquery engine lacks those capabilities.

In `query --sources PATH`, a source file can supply multiple captured database
views, immutable logs and plain tuples. Sources share the configured PostgreSQL
credential boundary; raw tuples are literal data without schema resolution.

```edn
{$then {:database "edn-demo" :as-of 2}
 $now  {:database "edn-demo"}
 $log  {:database "edn-demo" :log true}
 $rows [["alice@example.com" "external information"]]}
```

Raw source patterns support arbitrary positive widths, including short relations,
six-plus-column application data and omitted/trailing blank components. General
cells can contain nil, maps, sets, characters and inert custom tags. Data-only
queries require neither `--database` nor PostgreSQL configuration. See
[general query data](query-data.md) for native APIs, callbacks and source examples.

## EDN types versus stored values

The [EDN specification](https://github.com/edn-format/edn) governs reading/writing,
including arbitrary-key maps, sets, exact numbers and tags. Unknown tags are
preserved by default or explicitly rejected. Applications may register native
tag handlers; discarded forms do not run them. Reading a list containing a
function name does not execute it. Clojure-only reader syntax such as quoting,
auto-resolved keywords and constructor macros is not supported as standard EDN.

Parsed `EdnValue`, stored `Value`, and query `QueryValue` are distinct domains.
Nil and characters are valid EDN and general query values, but are not new stored
attribute types. Stored tuples retain their existing permitted nil slots. Query
adapters preserve general map/character/set/tag inputs without stringifying them
or forcing them into the narrower stored-value domain. Sets are collections, not
positional tuple bindings or relation rows. Known scalar tags retain validation;
unknown custom query tags are inert data, not function calls.
EDN map-key equality is not the engine's cross-numeric equality. Converting an EDN
map whose distinct keys collapse under native query equality fails explicitly.

Integers, `N` big integers and `M` exact decimals retain their types; decimal scale
is preserved across printing without expanding compact exponents. Ordinary decimal
point numbers are Double, not Float. `#inst` retains its RFC3339 precision in parsed
data; conversion to a stored millisecond instant rejects excess precision and
leap seconds rather than rounding. `#uuid` retains UUID value semantics.
Explicit Float conversion allows ordinary binary32 rounding, but rejects overflow
and nonzero underflow; signed zero is preserved.

Native types without an unambiguous standard representation use these data tags:

| Tag | Payload |
| --- | --- |
| `#atomic/ref` | Nonnegative supported entity ID |
| `#atomic/float` | Finite Float text, or an explicitly converted Double literal |
| `#atomic/float-bits`, `#atomic/double-bits` | Hexadecimal IEEE bits; preserve nonfinite values/NaN payloads |
| `#atomic/bytes` | Hexadecimal bytes |
| `#atomic/uri` | Retained URI string |
| `#atomic/function` | 32-byte native program hash as hexadecimal |
| `#atomic/instant-millis` | Signed milliseconds, including years outside RFC3339's four-digit range |

These are namespaced data extensions, not JVM objects or executable constructors.
The reader preserves unfamiliar Datomic-specific tags too, but an adapter rejects
tags without a native meaning instead of treating them as valid facts. The
documented string-tempid form needs no `#db/id` reader extension.

## Limits, errors and evidence

`EdnReadOptions`/`EdnLimits` configure format admission and native tag handlers.
Defaults bound each input/output to 16 MiB, depth 64, one million nodes, token
bytes 16 MiB, numeric bytes 64 KiB and cumulative format work 64 MiB. `EdnReader` iterates
sequential forms from UTF-8 text and is fused after errors. Single-value reading
rejects trailing forms. Discard, malformed input and handler outputs are admitted
too; handlers are trusted cooperative Rust callbacks, not preemptible sandboxes.

Value-domain conversion independently bounds depth 64, one million nodes and
64 MiB of accounted container/value bytes. Transaction report conversion shares
one budget across all datoms and temporary IDs. This is not process RSS. Query/pull
adapters and the existing transaction/query/pull controls impose their own limits.
Errors include stable codes and location/path details; the CLI does not echo
transaction payloads or connection secrets. `--timeout-ms` applies to transaction
delivery/query evaluation, not a guarantee about wall time spent in all startup,
parsing, database I/O and printing. Pull supports explicit entity/depth limits.

Permanent suites: `edn_format`, `edn_values`, `edn_transactions`, `edn_query_pull`
and `edn_cli`. PostgreSQL cases announce skips if `ATOMIC_POSTGRES_URL` is absent;
a skipped run is not PostgreSQL evidence. `edn_cli` uses restricted runtime roles
when permitted, a separate executable writer, restart and exact replay, and
prints increasing-size complete-process cost samples. `edn_values` measures
coefficient-sized exact-number conversion with huge compact scales. These are
bounded fixtures, not universal throughput or deployment certification.

### Observed costs

On 2026-09-10, the optimized EDN acceptance run passed 45 tests (the five EDN
suites plus binary tests), with PostgreSQL enabled and no skips. Environment:
x86-64 Ryzen Threadripper 2950X, Rust 1.90.0, local Unix-socket PostgreSQL with
`fsync` and `synchronous_commit` on, restricted writer/peer roles. Release debug
information and incremental compilation were disabled. These are single samples
under shared-machine load, not statistical throughput estimates.

| New entities | Input bytes | Parse / format (ms) | Parse + canonical request (ms) | Complete CLI submit (ms) | Complete CLI query / pull (ms) |
| --- | ---: | ---: | ---: | ---: | ---: |
| 32 | 2,062 | 0.173 / 0.087 | 0.440 | 106.658 | 31.004 / 43.675 |
| 128 | 8,614 | 0.874 / 0.503 | 2.563 | 141.440 | 44.161 / 42.884 |
| 512 | 35,110 | 2.226 / 1.384 | 7.001 | 333.391 | 57.434 / 47.636 |

Complete CLI samples include process startup, configured connection, normal
transaction/query/pull processing, EDN output, process exit and the harness's
output reading/EDN validation. The transactor is already running. Query rows grow
cumulatively: 34, 162 and 674; output sizes are 1,506, 7,412 and 31,366 bytes.
Pull selects one entity, not the entire growing database.

Exact-number fixtures use 64/256/1,024/4,096 coefficient digits and scales
0/1,000,000/`i64::MAX`. Even at the largest scale, the 4,096-digit value prints in
4,118 bytes; measured parsing was 287–345 µs and conversion/printing 294–329 µs
across that coefficient's three scales. Query fixtures with 32/128/512 rows and
scales 0/1,000/100,000 used 195/771/3,075 native work units respectively at every
scale. No RSS or cluster-wide scalability claim is made from these checks.
