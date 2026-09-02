package datomic.impl;

import clojure.lang.IFn;
import com.google.common.cache.CacheLoader;

/**
 * Adapts a one-argument Clojure function to a Guava cache loader. Each cache
 * miss invokes the function with the requested key and caches its result.
 */
public class FnCacheLoader
extends CacheLoader {
    public final IFn fn;

    public FnCacheLoader(IFn fn2) {
        this.fn = fn2;
    }

    public Object load(Object key) {
        return this.fn.invoke(key);
    }
}
