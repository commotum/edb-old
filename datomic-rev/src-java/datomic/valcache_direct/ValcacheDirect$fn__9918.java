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
package datomic.valcache_direct;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Keyword;
import clojure.lang.Numbers;
import clojure.lang.RT;
import clojure.lang.Var;

public final class ValcacheDirect$fn__9918
extends AFunction {
    Object root;
    Object k;
    Object v;
    public static final Var const__0 = RT.var((String)"datomic.valcache", (String)"direct-put");
    public static final Var const__1 = RT.var((String)"datomic.monitor", (String)"add-stat");
    public static final Keyword const__2 = RT.keyword(null, (String)"ValcacheWriteNsec");
    public static final Keyword const__4 = RT.keyword(null, (String)"ValcachePutFailException");
    public static final Object const__5 = 1L;

    public ValcacheDirect$fn__9918(Object object, Object object2, Object object3) {
        this.root = object;
        this.k = object2;
        this.v = object3;
    }

    public Object invoke() {
        Object result2;
        long start = System.nanoTime();
        Object object = result2 = ((IFn)const__0.getRawRoot()).invoke(this.root, this.k, this.v);
        if (object != null && object != Boolean.FALSE) {
            ((IFn)const__1.getRawRoot()).invoke((Object)const__2, (Object)Numbers.num((long)Numbers.minus((long)System.nanoTime(), (long)start)));
        } else {
            ((IFn)const__1.getRawRoot()).invoke((Object)const__4, const__5);
        }
        Object var3_2 = null;
        return result2;
    }
}

