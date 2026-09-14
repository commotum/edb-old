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
import datomic.db$get_prefetch_dispatcher$reify__14046$fn__14047;
import datomic.db.PrefetchDispatcher;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicBoolean;

public final class db$get_prefetch_dispatcher$reify__14046
implements PrefetchDispatcher,
IObj {
    final IPersistentMap __meta;
    Object done_flag;
    Object exec;

    public db$get_prefetch_dispatcher$reify__14046(IPersistentMap iPersistentMap, Object object, Object object2) {
        this.__meta = iPersistentMap;
        this.done_flag = object;
        this.exec = object2;
    }

    public db$get_prefetch_dispatcher$reify__14046(Object object, Object object2) {
        this(null, object, object2);
    }

    public IPersistentMap meta() {
        return this.__meta;
    }

    public IObj withMeta(IPersistentMap iPersistentMap) {
        return new db$get_prefetch_dispatcher$reify__14046(iPersistentMap, this.done_flag, this.exec);
    }

    public Object close() {
        ((AtomicBoolean)this.done_flag).set(Boolean.TRUE);
        return null;
    }

    public Object prefetch1(Object f) {
        Object object = f;
        f = null;
        ((Executor)this.exec).execute((Runnable)((Object)new db$get_prefetch_dispatcher$reify__14046$fn__14047(this.done_flag, object)));
        return null;
    }
}

