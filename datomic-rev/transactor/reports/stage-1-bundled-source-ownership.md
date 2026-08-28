# Bundled Transactor namespace source ownership

This read-only scan checked all **533** hash-bound shipped dependency JARs (192412 physical ZIP entries) for exact `.clj`/`.cljc` paths derived from the 85 non-`datomic.*` Transactor initializer class paths.

## Result

- `single-exact-path`: 85

## Owner JARs

- `lib/anomalies-0.1.12.jar`: 1 namespace source entries
- `lib/core.async-1.6.681.jar`: 10 namespace source entries
- `lib/core.cache-1.0.225.jar`: 1 namespace source entries
- `lib/core.memoize-1.0.253.jar`: 1 namespace source entries
- `lib/data.csv-0.1.3.jar`: 1 namespace source entries
- `lib/data.json-2.4.0.jar`: 1 namespace source entries
- `lib/data.priority-map-1.1.0.jar`: 1 namespace source entries
- `lib/hiccup-1.0.1.jar`: 6 namespace source entries
- `lib/hmac-authn-0.1.211.jar`: 1 namespace source entries
- `lib/http-client-1.0.126.jar`: 2 namespace source entries
- `lib/java.jmx-1.0.0.jar`: 1 namespace source entries
- `lib/liberator-0.15.3.jar`: 4 namespace source entries
- `lib/moustache-1.1.0.jar`: 1 namespace source entries
- `lib/nano-impl-0.1.325.jar`: 5 namespace source entries
- `lib/ring-codec-1.2.0.jar`: 1 namespace source entries
- `lib/ring-core-1.10.0.jar`: 8 namespace source entries
- `lib/ring-servlet-1.10.0.jar`: 1 namespace source entries
- `lib/tools.analyzer-1.1.1.jar`: 14 namespace source entries
- `lib/tools.analyzer.jvm-1.2.3.jar`: 16 namespace source entries
- `lib/tools.cli-1.0.219.jar`: 1 namespace source entries
- `lib/tools.reader-1.3.2.jar`: 7 namespace source entries
- `lib/transit-clj-1.0.333.jar`: 1 namespace source entries

## Missing exact source paths

None.

## Ambiguous exact paths

None.

## Interpretation boundary

A single exact path in the distribution establishes a concrete shipped source candidate and ownership/version lead. It does not alone prove that the embedded AOT class was compiled from byte-for-byte that source; AOT surface or recompilation evidence is a separate validation gate. Missing paths remain decompiler recovery inputs.
