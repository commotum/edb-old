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

public final class memory_size$fn__436$fn__438
extends AFunction {
    private static Class __cached_class__0;
    private static Class __cached_class__1;
    public static final Var const__5;

    /*
     * Unable to fully structure code
     */
    public Object invoke(Object s, Object p__437) {
        v0 = p__437;
        p__437 = null;
        vec__439 = v0;
        k = RT.nth((Object)vec__439, (int)RT.intCast((long)0L), null);
        v1 = vec__439;
        vec__439 = null;
        v = RT.nth((Object)v1, (int)RT.intCast((long)1L), null);
        v2 = s;
        s = null;
        v3 = Numbers.add((Object)v2, (long)36L);
        v4 = k;
        k = null;
        v5 = v4;
        if (Util.classOf((Object)v4) == memory_size$fn__436$fn__438.__cached_class__0) ** GOTO lbl18
        if (!(v5 instanceof MemorySize)) {
            v5 = v5;
            memory_size$fn__436$fn__438.__cached_class__0 = Util.classOf((Object)v5);
lbl18:
            // 2 sources

            v6 = memory_size$fn__436$fn__438.const__5.getRawRoot().invoke(v5);
        } else {
            v6 = ((MemorySize)v5).memory_size();
        }
        v7 = Numbers.add((Object)v3, (Object)v6);
        v8 = v;
        v = null;
        v9 = v8;
        if (Util.classOf((Object)v8) == memory_size$fn__436$fn__438.__cached_class__1) ** GOTO lbl29
        if (!(v9 instanceof MemorySize)) {
            v9 = v9;
            memory_size$fn__436$fn__438.__cached_class__1 = Util.classOf((Object)v9);
lbl29:
            // 2 sources

            v10 = memory_size$fn__436$fn__438.const__5.getRawRoot().invoke(v9);
        } else {
            v10 = ((MemorySize)v9).memory_size();
        }
        this = null;
        return Numbers.add((Object)v7, (Object)v10);
    }

    static {
        const__5 = RT.var((String)"datomic.memory-size", (String)"memory-size");
    }
}

