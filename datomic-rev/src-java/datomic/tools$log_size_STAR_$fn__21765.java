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
package datomic;

import clojure.lang.AFunction;
import clojure.lang.Numbers;
import clojure.lang.RT;
import clojure.lang.Util;
import clojure.lang.Var;
import datomic.memory_size.MemorySize;

public final class tools$log_size_STAR_$fn__21765
extends AFunction {
    private static Class __cached_class__0;
    public static final Var const__1;

    /*
     * Unable to fully structure code
     */
    public Object invoke(Object size, Object logentry) {
        v0 = size;
        size = null;
        v1 = logentry;
        logentry = null;
        v2 = v1;
        if (Util.classOf((Object)v1) == tools$log_size_STAR_$fn__21765.__cached_class__0) ** GOTO lbl10
        if (!(v2 instanceof MemorySize)) {
            v2 = v2;
            tools$log_size_STAR_$fn__21765.__cached_class__0 = Util.classOf((Object)v2);
lbl10:
            // 2 sources

            v3 = tools$log_size_STAR_$fn__21765.const__1.getRawRoot().invoke(v2);
        } else {
            v3 = ((MemorySize)v2).memory_size();
        }
        this = null;
        return Numbers.add((Object)v0, (Object)v3);
    }

    static {
        const__1 = RT.var((String)"datomic.memory-size", (String)"memory-size");
    }
}

