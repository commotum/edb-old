/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.IFn
 *  com.google.common.cache.CacheLoader
 */
package datomic.impl;

import clojure.lang.IFn;
import com.google.common.cache.CacheLoader;

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

