/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.IObj
 *  clojure.lang.IPersistentMap
 */
package datomic;

import clojure.lang.IObj;
import clojure.lang.IPersistentMap;
import datomic.db.PrefetchDispatcher;

public final class db$get_prefetch_dispatcher$reify__14050
implements PrefetchDispatcher,
IObj {
    final IPersistentMap __meta;

    public db$get_prefetch_dispatcher$reify__14050(IPersistentMap iPersistentMap) {
        this.__meta = iPersistentMap;
    }

    public db$get_prefetch_dispatcher$reify__14050() {
        this(null);
    }

    public IPersistentMap meta() {
        return this.__meta;
    }

    public IObj withMeta(IPersistentMap iPersistentMap) {
        return new db$get_prefetch_dispatcher$reify__14050(iPersistentMap);
    }

    public Object close() {
        return null;
    }

    public Object prefetch1(Object _) {
        return null;
    }
}

