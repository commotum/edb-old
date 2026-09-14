/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.IFn
 *  clojure.lang.ILookup
 *  clojure.lang.IObj
 *  clojure.lang.IPersistentMap
 */
package datomic;

import clojure.lang.IFn;
import clojure.lang.ILookup;
import clojure.lang.IObj;
import clojure.lang.IPersistentMap;

public final class cache$fn$reify__9416
implements ILookup,
IObj {
    final IPersistentMap __meta;
    Object f;

    public cache$fn$reify__9416(IPersistentMap iPersistentMap, Object object) {
        this.__meta = iPersistentMap;
        this.f = object;
    }

    public cache$fn$reify__9416(Object object) {
        this(null, object);
    }

    public IPersistentMap meta() {
        return this.__meta;
    }

    public IObj withMeta(IPersistentMap iPersistentMap) {
        return new cache$fn$reify__9416(iPersistentMap, this.f);
    }

    public Object valAt(Object k, Object _) {
        Object object = k;
        k = null;
        cache$fn$reify__9416 this_ = null;
        return ((IFn)this_.f).invoke(object);
    }

    public Object valAt(Object k) {
        Object object = k;
        k = null;
        cache$fn$reify__9416 this_ = null;
        return ((IFn)this_.f).invoke(object);
    }
}

