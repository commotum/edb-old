/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.Keyword
 *  clojure.lang.Numbers
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic.valcache.puts_pool_impl;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Keyword;
import clojure.lang.Numbers;
import clojure.lang.RT;
import clojure.lang.Var;
import java.util.Map;

public final class ValcachePutsPoolImpl$fn__9883
extends AFunction {
    Object k;
    Object puts;
    long start;
    Object f;
    public static final Var const__0 = RT.var((String)"datomic.monitor", (String)"add-stat");
    public static final Keyword const__1 = RT.keyword(null, (String)"ValcachePutNsec");

    public ValcachePutsPoolImpl$fn__9883(Object object, Object object2, long l, Object object3) {
        this.k = object;
        this.puts = object2;
        this.start = l;
        this.f = object3;
    }

    public Object invoke() {
        Object object;
        try {
            Object object2 = ((IFn)this.f).invoke();
            object = object2 != null && object2 != Boolean.FALSE ? ((IFn)const__0.getRawRoot()).invoke((Object)const__1, (Object)Numbers.num((long)Numbers.minus((long)System.nanoTime(), (long)this.start))) : null;
        }
        finally {
            ((Map)this.puts).remove(this.k);
        }
        return object;
    }
}

