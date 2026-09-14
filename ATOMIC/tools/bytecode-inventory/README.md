# Bytecode inventory

`BytecodeInventory.java` is a read-only ASM-based inspector for JVM archives.
It parses class files without loading or executing them and emits stable TSV
inventories for definitions, instructions, calls, literals, dependencies,
resources, and duplicate classes.

Compile and inspect its command-line interface with an ASM 9 JAR on the
classpath:

```bash
javac -cp /path/to/asm-9.x.jar BytecodeInventory.java
java -cp /path/to/asm-9.x.jar:. BytecodeInventory --help
```

`BytecodeInventory.java` also accepts repeated `--detail-jar JAR` arguments.
Its output is deterministic: records are sorted, floating-point values use raw
IEEE-754 bits, input archives are bound by SHA-256, and wall-clock data and ZIP
timestamps are excluded.

This repository intentionally does not retain an old Datomic baseline or its
campaign-specific validation wrapper. Generate a fresh inventory when a
specific source or compatibility question requires one.
