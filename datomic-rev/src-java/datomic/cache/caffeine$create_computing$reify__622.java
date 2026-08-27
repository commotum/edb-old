/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.IFn
 *  clojure.lang.IObj
 *  clojure.lang.IPersistentMap
 *  com.github.benmanes.caffeine.cache.CacheLoader
 */
package datomic.cache;

import clojure.lang.IFn;
import clojure.lang.IObj;
import clojure.lang.IPersistentMap;
import com.github.benmanes.caffeine.cache.CacheLoader;

public final class caffeine$create_computing$reify__622
implements CacheLoader,
IObj {
    final IPersistentMap __meta;
    Object f;

    public caffeine$create_computing$reify__622(IPersistentMap iPersistentMap, Object object) {
        this.__meta = iPersistentMap;
        this.f = object;
    }

    public caffeine$create_computing$reify__622(Object object) {
        this(null, object);
    }

    public IPersistentMap meta() {
        return this.__meta;
    }

    public IObj withMeta(IPersistentMap iPersistentMap) {
        return new caffeine$create_computing$reify__622(iPersistentMap, this.f);
    }

    public Object load(Object k) throws Exception {
        Object object = k;
        k = null;
        caffeine$create_computing$reify__622 this_ = null;
        return ((IFn)this_.f).invoke(object);
    }
}

