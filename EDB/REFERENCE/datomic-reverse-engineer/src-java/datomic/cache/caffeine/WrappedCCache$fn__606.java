/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.Numbers
 *  clojure.lang.RT
 *  clojure.lang.Util
 *  clojure.lang.Var
 */
package datomic.cache.caffeine;

import clojure.lang.AFunction;
import clojure.lang.Numbers;
import clojure.lang.RT;
import clojure.lang.Util;
import clojure.lang.Var;
import datomic.memory_size.MemorySize;

public final class WrappedCCache$fn__606
extends AFunction {
    Object cache;
    private static Class __cached_class__0;
    private static Class __cached_class__1;
    public static final Var const__2;

    public WrappedCCache$fn__606(Object object) {
        this.cache = object;
    }

    /*
     * Unable to fully structure code
     */
    public Object invoke(Object n, Object k) {
        v0 = n;
        n = null;
        v1 = Numbers.add((Object)v0, (long)82L);
        v2 = k;
        if (Util.classOf((Object)v2) == WrappedCCache$fn__606.__cached_class__0) ** GOTO lbl9
        if (!(v2 instanceof MemorySize)) {
            v2 = v2;
            WrappedCCache$fn__606.__cached_class__0 = Util.classOf((Object)v2);
lbl9:
            // 2 sources

            v3 = WrappedCCache$fn__606.const__2.getRawRoot().invoke(v2);
        } else {
            v3 = ((MemorySize)v2).memory_size();
        }
        v4 = Numbers.add((Object)v1, (Object)v3);
        v5 = k;
        k = null;
        v6 = RT.get((Object)this.cache, (Object)v5);
        if (Util.classOf((Object)v6) == WrappedCCache$fn__606.__cached_class__1) ** GOTO lbl20
        if (!(v6 instanceof MemorySize)) {
            v6 = v6;
            WrappedCCache$fn__606.__cached_class__1 = Util.classOf((Object)v6);
lbl20:
            // 2 sources

            v7 = WrappedCCache$fn__606.const__2.getRawRoot().invoke(v6);
        } else {
            v7 = ((MemorySize)v6).memory_size();
        }
        this = null;
        return Numbers.add((Object)v4, (Object)v7);
    }

    static {
        const__2 = RT.var((String)"datomic.memory-size", (String)"memory-size");
    }
}

