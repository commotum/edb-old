# Stage 4 bytecode-proven warning repairs

Date: `2026-08-27` (`America/Los_Angeles`).

Status: **PASS**.

Stage 4 reduced the exact recovered-source warning inventory only where the
licensed original bytecode fixed the correction at instruction level. It did
not suppress diagnostics or infer types merely from plausible source intent.
The baseline contained 317 unique warning lines: 305 reflection warnings, 10
primitive-local `recur` warnings, and two auto-boxing warnings. The final
inventory contains 157: 150 reflection, six primitive-local `recur`, and one
auto-boxing warning.

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

The complete source-only gate at `/tmp/datomic-stage4-full-source-v1` then
loaded all 142 namespaces without original Peer/core2 AOT fallback and passed
the recovered behavior regressions. Its combined namespace diagnostics
contained exactly the final 150/6/1 inventory. The successful behavior result
has SHA-256
`07bf6be73cd2fee64bcbd0aff6f4d0ce9e0cbfa8ba9123f8c9d8286ead110ac5`.

## Final Stage 1 rerun

The complete Stage 1 gate passed at
`/tmp/datomic-stage1-stage4-final-v1`, after two independent clean builds. It
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
`1b14f9fcb2e2a77a5205fbceb05bd1a4b0f2dc9b64724eab3f702a7dc52da579`;
the packaged `validation-summary.properties` has SHA-256
`2cd299ff624ac21e020fc35a7283eeb7d255d86ba2008988f485dc34b8892d76`.

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
| `stage-4-index-db-bytecode-evidence.tsv` | `7183b407fc4d9edabaad8f8d29d463eea6fe692043e22dc0951db388ea8dc9bf` |
| `stage-4-lowrisk-bytecode-evidence.tsv` | `e320e50bf67cff4198020cbf767db3fe6aca03c2c852889ce749811a5764bb22` |
| `stage-4-unresolved-warnings.txt` | `9c8f20b030205f749edc952255da131bfa6d45c1537bbf0655bd6fe772ce33ad` |
| Final recovered artifact | `bc7836b124896706a9bde9cdd0e6af84279dc06dbb0bee41ea448382f818cdfe` |

## Accumulated PostgreSQL reruns

Both complete PostgreSQL gates were rerun against the exact final artifact,
after the Stage 1 result above:

| Gate | Retained run and result |
|---|---|
| Stage 2 lifecycle/recovery | `/tmp/datomic-stage2-stage4-final-v1`; all lifecycle, full/incremental backup/restore, missing/corrupt segment rejection, interrupted restore/retry, exact t3 recovery, and post-recovery write checks passed. The 118-file evidence manifest has SHA-256 `78ba14d01454f90d650174c7f92f9fa1bf80244686d95faa6029770ba831dca5`; the summary has SHA-256 `5e50f0be223d508711e3058b820ae62275a34c716312132f4ebd16d86a71ff31`. |
| Stage 3 concurrency/transport | `/tmp/datomic-stage3-stage4-final-v1`; all local cancellation/rejection cases, SQL query controls, lifecycle race, eight-round 8-way CAS contention, canonical audits, and verified transactor pause/recovery passed. The 64-file evidence manifest has SHA-256 `f3033d28744e604e621dcbe9d0596a73f46c6ab8860d4db9ff84a878d9736bc8`; the summary has SHA-256 `462dee72fdf018ebd9e44355009c0a8004903f04b503419e6faefdb48ce82733`. |

Both summaries record `stage.complete=true`; both run-status records say
`services.stopped=true` and `evidence.complete=true`. Stage 2 retained the
exact t3 logical SHA-256
`de1debf98a63e10fa775591c7a57d557d559e7af1c42a4d676f10882cb0ca684`.
Stage 3 retained canonical peer-state SHA-256
`abe3a8e000587079b64965cb99d468ef355d14ef11bd0855aeefab4eba394510`,
with byte-identical pre/post-transport audit files. The licensed transactor was
an isolated external fixture in both runs and all database fixtures stopped.

## Claim boundary

Stage 4 establishes that the 159 modified warning sites have recorded
instruction-level justification and that the resulting artifact retains the
tested source, surface, in-memory, PostgreSQL, recovery, and bounded concurrency
behavior. It does not claim that the 157 remaining diagnostics are harmless
under every execution path, nor that the recovered source text is identical to
unpublished original source. External storage and concurrency claims remain
bounded by the Stage 2 and Stage 3 matrices.
