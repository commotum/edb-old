# Stage 4 bytecode-proven warning repairs

Date: `2026-08-27` (`America/Los_Angeles`).

Status: **PASS**.

Stage 4 reduced the exact recovered-source warning inventory only where the
licensed original bytecode fixed the correction at instruction level. It did
not suppress diagnostics or infer types merely from plausible source intent.
The checked-in [pre-hardening baseline](stage-4-pre-hardening-warnings.txt)
contains 317 unique warning lines: 305 reflection warnings, 10 primitive-local
`recur` warnings, and two auto-boxing warnings. Its SHA-256 is
`5105303385e2ce6d749e46d1566e39a816588f119b1b13a2d4e7dee1663bb6e1`.
The final inventory contains 157: 150 reflection, six primitive-local `recur`,
and one auto-boxing warning.

## Repair accounting

| Repair set | Proven warning sites | Unique warning lines removed | Breakdown |
|---|---:|---:|---|
| `datomic.index` | 75 | 75 | 75 reflection |
| `datomic.db` | 39 | 39 | 39 reflection |
| Nine lower-risk namespaces | 45 | 46 | 41 reflection, 4 primitive `recur`, 1 auto-boxing |
| **Total** | **159** | **160** | **155 reflection, 4 primitive `recur`, 1 auto-boxing** |

The lower-risk site and warning-line totals differ by one because the four
repaired `datomic.datalog` primitive-loop sites also jointly emitted the one
unique `Auto-boxing loop arg: h` diagnostic. Removing those four proven source
causes therefore removed five unique warning lines. The lower-risk namespaces
were `datomic.datalog`, `datomic.datafy`, `datomic.core2.thread`,
`datomic.monitor`, `datomic.memory-size`, `datomic.slf4j`, `datomic.io`,
`datomic.crypto`, and `datomic.common`.

The arithmetic is exact:

| Category | Baseline | Removed | Final |
|---|---:|---:|---:|
| Reflection | 305 | 155 | 150 |
| Primitive-local `recur` | 10 | 4 | 6 |
| Auto-boxing | 2 | 1 | 1 |
| **All unique warning lines** | **317** | **160** | **157** |

## Evidence and correction boundary

`stage-4-index-db-bytecode-evidence.tsv` contains 47 grouped evidence rows.
Each row identifies the pre-repair source sites and form, the corresponding
original generated class and method, the decisive cast/invocation/primitive
instructions, the minimal recovered-source correction, and the expected
warning delta. Those deltas sum to 75 for `datomic.index` and 39 for
`datomic.db`.

`stage-4-lowrisk-bytecode-evidence.tsv` contains one row for each of the 45
lower-risk warning sites and one additional `DEF001` row for the semantic
defect described below. Every warning row records the original class, method,
descriptor, and exact opcode evidence. Corrections were limited to forms such
as the demonstrated array/receiver/interface type hint, typed local binding,
primitive branch cast, or overload-disambiguating argument type. The focused
validation column records the fresh-load, surface, and behavior check relevant
to each site.

Examples of the evidence used include:

- original `CHECKCAST` instructions immediately before array access or field
  access, establishing the exact array or receiver type;
- exact method descriptors and `INVOKEVIRTUAL`/`INVOKEINTERFACE` targets,
  establishing an overload or interface dispatch;
- `I2L` and primitive local-slot use, establishing the four `datalog`
  primitive-loop casts; and
- `CHECKCAST clojure/lang/IObj` before `withMeta`, establishing the interface
  type for recovered arglist metadata forms.

The changes preserve the recovered evaluation structure. No unresolved site
was edited simply to reduce the count, and no compiler-warning option was
disabled.

The final Stage 1 behavior gate directly exercises a broader portion of the
lower-risk repairs: overloaded-setter selection; statistics accumulation and
callbacks; UTF-8, base128, CRC, HMAC, and tamper-rejection paths; logging,
retry, scheduling, heterogeneous comparison, key-comparator, and pooled-map
behavior; and selected Datalog hash, join, and invalid-source paths. `DL006`
and `DL007` are compiler-generated hash closures inside the database/query
path, not stable top-level Vars exposed for an isolated direct probe. Their
claim therefore remains bounded by their exact bytecode evidence and surface
parity together with the accumulated database/query gates; the report does not
mislabel them as independently unit-tested functions.

## Separate semantic repair

`datomic.common/compare-byte-arrays` had a non-warning decompiler defect: its
equal-length branch evaluated the comparison loop, discarded that result, and
returned `nil`. The original `invokeStatic` bytecode shows the equal-length
loop converging on the primitive-long return path, including the original
signed-byte subtraction behavior. The recovered function now returns that
loop directly.

This repair is deliberately excluded from the 159 warning-site and 160
warning-line totals. The isolated oracle/candidate matrix and the permanent
recovered-behavior regression cover equal arrays (`0`), both prefix orders
(`-1` and `1`), and signed high-bit comparison (`0x80` versus `0x7f` gives
`-255`, with the reverse giving `255`).

## Focused and source-only validation

The two high-volume namespace surfaces matched the original oracle exactly:

| Namespace | Original/recovered surface SHA-256 |
|---|---|
| `datomic.db` | `621be32f817812d86b4dc930a93936a5e90770e4242386c308f6b849a5b42aab` |
| `datomic.index` | `f958af5741b47a7b7c896743a028ae19a8a4ac32960838d404095e7f5b76a17b` |

All nine lower-risk namespace surface pairs were also byte-identical. Their
surface hashes were:

| Namespace | Original/recovered surface SHA-256 |
|---|---|
| `datomic.common` | `91fafc87c44085cae105e08509866383b5474459a6d15f3cbdf58be56eb58758` |
| `datomic.core2.thread` | `5ae789e3736579f7914ddcc3cd4789f2fd07baeef2ea8cffde2d15e31c035294` |
| `datomic.crypto` | `11767c0aa8797c034e948be605e69d91bb4d95e0a7084f64d904e6490d4fb289` |
| `datomic.datafy` | `78babfc6ebe43f305c075a0db09f71139c4212ad000b00a6ebe984b9e7dccf63` |
| `datomic.datalog` | `6f90ff9ea9db41b9893be574a95568f644a63c3eb53625f0b564b413f426705a` |
| `datomic.io` | `48a9de71f7d16a5f137a1fc811ee7c546a7171a4b7670547999722ce594e4996` |
| `datomic.memory-size` | `d22483965ffc7e67aad0c9a954eeb6b74b6e790c91da8e8c95a5245bd1d9c7a2` |
| `datomic.monitor` | `b615c03ed543bcc978e9ea7e3fb290e725c0e06ceb630a7d2747d381bde0647b` |
| `datomic.slf4j` | `cb3aff233f339823048d55d81c8455db6a62eefc263ff69fd6f4adab85f53248` |

The local source-only gate at `/tmp/datomic-stage4-full-source-v1` then loaded
all 142 namespaces without original Peer/core2 AOT fallback and passed the
then-current recovered behavior regressions. Its combined namespace diagnostics
contained exactly the final 150/6/1 inventory. The successful behavior result
has SHA-256
`07bf6be73cd2fee64bcbd0aff6f4d0ce9e0cbfa8ba9123f8c9d8286ead110ac5`.
That `/tmp` directory is ephemeral; the later Stage 1 adversarial rerun below
contains the expanded direct behavior gate.

## Final Stage 1 rerun

The complete Stage 1 gate passed at
`/tmp/datomic-stage1-adversarial-final-v1`, after two independent clean builds.
It
produced byte-identical 205-entry artifacts with SHA-256
`bc7836b124896706a9bde9cdd0e6af84279dc06dbb0bee41ea448382f818cdfe`.
The candidate classpath contained the recovered artifact, compile/load-only Hot
Rod stubs, and the exact 532 dependencies; original Peer/core2 AOT classes were
absent and origin-audited.

The packaged gate passed 142 namespace loads, 142 runtime namespace surfaces,
the exact 47-class/108-field/279-method handwritten Java surface, focused
recovered-behavior regressions, exact warning-inventory enforcement, and the
unchanged in-memory parity result SHA-256
`228b03dac4a437465937c53dbc7c4917294de408b2311b41a662e5f39eca37be`.
The final `stage-1-summary.properties` has SHA-256
`0ecd7443db30698b19e46c538694ecef14a705bab33b8823959d26620b31b0bb`;
the packaged `validation-summary.properties` has SHA-256
`2cd299ff624ac21e020fc35a7283eeb7d255d86ba2008988f485dc34b8892d76`;
and the 1,230-file evidence manifest has SHA-256
`531b2f2e9ce15e10bbe87ffcbfb5d8b884e7aacd6b20bcb4601b9f59bb5a960a`.
The validation-harness manifest has SHA-256
`8c04b2dd38cf403fe29a981b5f025b1f5bb86863ddeb651f5b4d3f3c7f2dd404`.

## Exact unresolved boundary

`stage-4-unresolved-warnings.txt` is the sorted, exact 157-line final
inventory. `scripts/validate-source-artifact.sh` compares generated candidate
diagnostics to this file, in addition to enforcing the 150/6/1 category counts,
so a disappeared, changed, or newly introduced warning cannot pass unnoticed.

The six residual primitive-local diagnostics are the `ret` loop arguments in
`datomic.btset`; the residual auto-boxing diagnostic is `Auto-boxing loop arg:
ret`. The remaining 150 lines are reflection diagnostics. They were
intentionally left unchanged because this bounded audit did not record
site-specific bytecode evidence decisive enough to authorize a correction.
That is an evidence boundary, not a claim that every residual warning is
intrinsically unrepairable. The exact inventory is the authoritative list.

## Evidence hashes

| Record | SHA-256 |
|---|---|
| `stage-4-pre-hardening-warnings.txt` | `5105303385e2ce6d749e46d1566e39a816588f119b1b13a2d4e7dee1663bb6e1` |
| `stage-4-index-db-bytecode-evidence.tsv` | `18cca469a923aba52344f903f67214c2231ba4aabcdfaf73e3b8b15e8f6e0c45` |
| `stage-4-lowrisk-bytecode-evidence.tsv` | `e320e50bf67cff4198020cbf767db3fe6aca03c2c852889ce749811a5764bb22` |
| `stage-4-unresolved-warnings.txt` | `9c8f20b030205f749edc952255da131bfa6d45c1537bbf0655bd6fe772ce33ad` |
| Final recovered artifact | `bc7836b124896706a9bde9cdd0e6af84279dc06dbb0bee41ea448382f818cdfe` |

## Accumulated PostgreSQL reruns

Both complete PostgreSQL gates were rerun against the exact final artifact,
after the Stage 1 result above:

| Gate | Local final run and result |
|---|---|
| Stage 2 lifecycle/recovery | `/tmp/datomic-stage2-adversarial-final-v2`; all lifecycle, full/incremental backup/restore, exact database-ID/basis/current-view gates, missing/corrupt segment rejection, interrupted restore/retry, exact t3 recovery, and post-recovery write checks passed. The 118-file evidence manifest has SHA-256 `25e5f821a84bbde27cb85a985a2728cffbe73509dfd2f7b70c115ed6ca2cf721`; the summary has SHA-256 `5c93bd80755dfb7bdc82f648eecbef8e14434cb971813a8ad05e146d4733947a`. |
| Stage 3 concurrency/transport | `/tmp/datomic-stage3-adversarial-final-v2`; all local cancellation/rejection cases, SQL query controls, lifecycle race, eight-round 8-way CAS contention, canonical audits, and verified transactor pause/recovery passed. The 88-file evidence manifest has SHA-256 `8f69d3fac2ab0d7d59f449db2c2e3489b292d24a123522a023754a94b01984f6`; the summary has SHA-256 `1d41a632788e515bea16d71c663140eb9d97defcc411a1436ee8b97092a35c06`. The manifest includes the retained post-preflight Hot Rod stub recheck. |

Both summaries record `stage.complete=true`; both run-status records say
`services.stopped=true` and `evidence.complete=true`. Stage 2 records the
exact t3 logical SHA-256
`de1debf98a63e10fa775591c7a57d557d559e7af1c42a4d676f10882cb0ca684`.
Stage 3 records canonical peer-state SHA-256
`abe3a8e000587079b64965cb99d468ef355d14ef11bd0855aeefab4eba394510`,
with byte-identical pre/post-transport audit files. The licensed transactor was
an isolated external fixture in both runs and all database fixtures stopped.
All three adversarial run roots named in this report are local `/tmp` evidence,
not durable repository archives.

## Claim boundary

Stage 4 establishes that the 159 modified warning sites have recorded
instruction-level justification and that the resulting artifact retains the
tested source, surface, in-memory, PostgreSQL, recovery, and bounded concurrency
behavior. It does not claim that the 157 remaining diagnostics are harmless
under every execution path, nor that the recovered source text is identical to
unpublished original source. External storage and concurrency claims remain
bounded by the Stage 2 and Stage 3 matrices. Those matrices validate the Peer
against a licensed external Transactor; Stage 4 makes no Transactor-recovery or
educational-Transactor completion claim.
