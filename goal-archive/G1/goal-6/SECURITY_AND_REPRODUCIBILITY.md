# Goal 6 security and reproducibility audit

## Authority boundary

Persisted programs are data interpreted by `ProgramRuntime`; they are never
loaded as Rust, native, JVM, Clojure, SQL, or WebAssembly code. The closed
instruction enum has no filesystem, network, process, environment, clock,
randomness, thread, allocation, pointer, dynamic-load, SQL, or foreign-call
operation. It has no jump, loop, or recursive call. The only host read is a
bounded current-value lookup on the immutable database argument.

Transaction programs can only construct ordinary add/retract operations.
Those operations receive no special authority and must pass the existing
kernel's type, identity, cardinality, uniqueness, predicate, and atomicity
checks. Attribute predicates return one boolean. Query functions return rows
that pass the existing binding and parent query resource checks. Local Rust
query functions are explicitly process-local and panic-contained; they cannot
be required by durable recovery.

## Reproducibility chain

The checked canonical bytes include format version, program kind, arity,
constant values, and instructions. SHA-256 of those bytes is the only cache and
exact-resolution identity. Deployment binds immutable name/version rows to
that hash; active-name publication uses an expected-version transaction.
Decoding verifies envelope checksum, canonical re-encoding, static stack
shape, kind authority, and metadata agreement. Canonical observation encoding
sorts unordered transaction operations and relation rows, allowing independent
runtimes to compare byte-identical output.

Committed transactions contain only validated material datoms and schema
changes. Recovery does not resolve or execute a program. Program transaction
idempotency includes exact function and predicate hashes, arguments, basis,
and transaction instant, so a changed activation cannot silently reinterpret
an ambiguous retry or create a second commit.

## Resource envelope

Static validation caps arity and instruction count. Execution charges every
instruction and examined database value, checks cancellation between
instructions and reads, and enforces caller-selected stack and output limits.
The language cannot amplify work with control flow. Batch transaction calls
each receive the same locked db-before and their combined output still enters
the kernel's 100,000-operation transaction boundary. Persisted query programs
inherit the parent query's work, intermediate, result, and cancellation
controls.

## Evidence and remaining limits

Focused tests cover golden encoding/hash, malformed programs, checksum and
stored-content corruption, type/arithmetic failures, fuel/stack/output limits,
cancellation, local callback panic, independent-runtime byte equality,
immutable deployment, activation races, exact retry, PostgreSQL restart,
same-db-before composition, predicate rejection, snapshot concurrency, and
recovery without executable code. The integrated suite also retains the prior
kernel, durability, peer, query, and pull tests.

This intentionally does not support arbitrary Clojure/JVM functions, imports,
dynamic dispatch, loops, recursion, general collection transforms, tempids
inside bytecode, or ambient effects. More expressiveness requires a new
versioned program kind and threat review; it must not reinterpret this ABI.
