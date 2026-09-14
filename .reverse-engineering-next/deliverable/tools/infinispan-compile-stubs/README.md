# Infinispan 5.1 compile-only audit stubs

This isolated harness closes the `datomic.kv-hotrod` source-compilation check
without modifying the reconstructed sources or pretending to provide a working
Hot Rod client. The stubs expose only members referenced by Datomic 1.0.7277
bytecode:

- `Flag/FORCE_RETURN_VALUE`
- `RemoteCache.withFlags(Flag[]) -> RemoteCache`
- `RemoteCache.getVersioned(Object) -> VersionedValue`
- `RemoteCache.replaceWithVersion(Object, Object, long) -> boolean`
- inherited `ConcurrentMap` operations used by the generated source
- `VersionedValue.getVersion() -> long`
- `VersionedValue.getValue() -> Object`
- `RemoteCacheManager(String, int)`
- `RemoteCacheManager.getCache(String) -> RemoteCache`

The repository's strict source-only validator builds these classes in an
isolated temporary directory:

```bash
JOBS=4 scripts/validate-all-namespaces.sh \
  /home/jake/Developer/datomic/datomic-pro-1.0.7277 \
  /tmp/datomic-source-validation
```

The emitted classes are compile-only and must not be used as runtime client
implementations.
