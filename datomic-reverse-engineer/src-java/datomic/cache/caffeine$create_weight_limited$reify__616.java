/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.IFn
 *  clojure.lang.IObj
 *  clojure.lang.IPersistentMap
 *  com.github.benmanes.caffeine.cache.Weigher
 */
package datomic.cache;

import clojure.lang.IFn;
import clojure.lang.IObj;
import clojure.lang.IPersistentMap;
import com.github.benmanes.caffeine.cache.Weigher;

public final class caffeine$create_weight_limited$reify__616
implements Weigher,
IObj {
    final IPersistentMap __meta;
    Object f;

    public caffeine$create_weight_limited$reify__616(IPersistentMap iPersistentMap, Object object) {
        this.__meta = iPersistentMap;
        this.f = object;
    }

    public caffeine$create_weight_limited$reify__616(Object object) {
        this(null, object);
    }

    public IPersistentMap meta() {
        return this.__meta;
    }

    public IObj withMeta(IPersistentMap iPersistentMap) {
        return new caffeine$create_weight_limited$reify__616(iPersistentMap, this.f);
    }

    public int weigh(Object k, Object v) {
        Object object = k;
        k = null;
        Object object2 = v;
        v = null;
        caffeine$create_weight_limited$reify__616 this_ = null;
        return ((Number)((IFn)this_.f).invoke(object, object2)).intValue();
    }
}

