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
import datomic.btset.BTSet;
import datomic.memory_size.MemorySize;

public final class domain$fn__16773
extends AFunction {
    private static Class __cached_class__0;
    public static final Var const__3;

    /*
     * Unable to fully structure code
     */
    public static Object invokeStatic(Object d) {
        size = 0L;
        v0 = d;
        d = null;
        iter = ((BTSet)v0).seek();
        while (true) {
            v1 = iter;
            if (v1 == null || v1 == Boolean.FALSE) break;
            v2 = iter.get();
            if (Util.classOf((Object)v2) == domain$fn__16773.__cached_class__0) ** GOTO lbl13
            if (!(v2 instanceof MemorySize)) {
                v2 = v2;
                domain$fn__16773.__cached_class__0 = Util.classOf((Object)v2);
lbl13:
                // 2 sources

                v3 = domain$fn__16773.const__3.getRawRoot().invoke(v2);
            } else {
                v3 = ((MemorySize)v2).memory_size();
            }
            v4 = iter;
            iter = null;
            iter = v4.next();
            size = Numbers.add((long)size, (long)RT.longCast((Object)v3));
        }
        return Numbers.num((long)size);
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return domain$fn__16773.invokeStatic(object2);
    }

    static {
        const__3 = RT.var((String)"datomic.memory-size", (String)"memory-size");
    }
}

