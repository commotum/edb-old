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

public final class memory_size$fn__446$fn__447
extends AFunction {
    private static Class __cached_class__0;
    public static final Var const__2;

    /*
     * Unable to fully structure code
     */
    public Object invoke(Object s, Object v) {
        v0 = s;
        s = null;
        v1 = Numbers.add((Object)v0, (long)24L);
        v2 = v;
        v = null;
        v3 = v2;
        if (Util.classOf((Object)v2) == memory_size$fn__446$fn__447.__cached_class__0) ** GOTO lbl11
        if (!(v3 instanceof MemorySize)) {
            v3 = v3;
            memory_size$fn__446$fn__447.__cached_class__0 = Util.classOf((Object)v3);
lbl11:
            // 2 sources

            v4 = memory_size$fn__446$fn__447.const__2.getRawRoot().invoke(v3);
        } else {
            v4 = ((MemorySize)v3).memory_size();
        }
        this = null;
        return Numbers.add((Object)v1, (Object)v4);
    }

    static {
        const__2 = RT.var((String)"datomic.memory-size", (String)"memory-size");
    }
}

