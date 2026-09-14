# Validation record

Validated against:

- peer JAR SHA-256: `cb55c9d01e155f9965e3332cd8ef70be42cdefacb50c072076e9ecf22b9a23ba`
- distribution POM SHA-256: `34b9caaa92b72b5a6d69397c5d7908a5b79fc7c29cb75ba4af490ff59fed0be9`
- `BytecodeInventory.java` SHA-256: `319dfd7e55be596220ca8f87709a09131e64a78ed79d5dd8a98c3efda07c3a36`

Two complete runs (`out-a` and `out-b`) were compared with `diff -qr` and
were byte-for-byte identical. Both runs indexed 533 companion JARs, detailed
the primary, 31 shipped direct-POM matches, and the overlapping `core2` JAR.
There were zero ASM parse warnings.

`validate.sh` passed every independent table/ZIP cross-check:

| Check | Exact value |
|---|---:|
| primary ZIP class entries / parsed class rows | 5,517 |
| field definitions / field rows | 38,718 |
| method definitions / method rows | 22,014 |
| class instruction sum / opcode histogram sum | 850,016 |
| class call sum / call occurrence rows | 169,882 |
| field-access sum / occurrence rows | 107,220 |
| instruction literal sum / occurrence rows | 191,255 |
| ConstantValue fields / field literal rows | 10 |
| primary ZIP resources / resource rows | 10 |
| namespace initializer classes / reconstructed namespaces | 142 |
| warnings | 0 |

The embedded POM hash exactly equals the distribution POM hash. All 510 class
names shared by the peer and `core2-1.0.140.jar` were detail-scanned and are
byte-identical by per-class SHA-256.

The existing `src-java` tree contains 5,513 Java files although the peer has
5,517 class entries. The four-file difference is fully explained by these
nested handwritten classes being coalesced into enclosing Java sources:

- `datomic/Database$Predicate` -> `datomic/Database.java`
- `datomic/impl/Exceptions$IllegalArgumentExceptionInfo` -> `datomic/impl/Exceptions.java`
- `datomic/impl/Exceptions$IllegalStateExceptionInfo` -> `datomic/impl/Exceptions.java`
- `datomic/impl/PriorityExecutor$ComparableFutureTask` -> `datomic/impl/PriorityExecutor.java`
