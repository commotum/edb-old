/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 */
package datomic.valcache;

import clojure.lang.AFunction;
import datomic.valcache.puts_pool_impl.ValcachePutsPoolImpl;

public final class puts_pool_impl$fn__9881$__GT_ValcachePutsPoolImpl__9889
extends AFunction {
    public Object invoke(Object limit2, Object puts, Object pool) {
        Object object = limit2;
        limit2 = null;
        Object object2 = puts;
        puts = null;
        Object object3 = pool;
        pool = null;
        return new ValcachePutsPoolImpl(object, object2, object3);
    }
}

