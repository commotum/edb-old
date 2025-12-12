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
import datomic.index.TransposedData;
import datomic.memory_size.MemorySize;

public final class domain$fn__16767
extends AFunction {
    private static Class __cached_class__0;
    private static Class __cached_class__1;
    private static Class __cached_class__2;
    private static Class __cached_class__3;
    public static final Var const__2;

    /*
     * Unable to fully structure code
     */
    public static Object invokeStatic(Object td) {
        v0 = ((TransposedData)td).eas;
        if (Util.classOf((Object)v0) == domain$fn__16767.__cached_class__0) ** GOTO lbl6
        if (!(v0 instanceof MemorySize)) {
            v0 = v0;
            domain$fn__16767.__cached_class__0 = Util.classOf((Object)v0);
lbl6:
            // 2 sources

            v1 = domain$fn__16767.const__2.getRawRoot().invoke(v0);
        } else {
            v1 = ((MemorySize)v0).memory_size();
        }
        v2 = Numbers.add((long)48L, (Object)v1);
        v3 = ((TransposedData)td).vs;
        if (Util.classOf((Object)v3) == domain$fn__16767.__cached_class__1) ** GOTO lbl15
        if (!(v3 instanceof MemorySize)) {
            v3 = v3;
            domain$fn__16767.__cached_class__1 = Util.classOf((Object)v3);
lbl15:
            // 2 sources

            v4 = domain$fn__16767.const__2.getRawRoot().invoke(v3);
        } else {
            v4 = ((MemorySize)v3).memory_size();
        }
        v5 = Numbers.add((Object)v2, (Object)v4);
        v6 = ((TransposedData)td).ts;
        if (Util.classOf((Object)v6) == domain$fn__16767.__cached_class__2) ** GOTO lbl24
        if (!(v6 instanceof MemorySize)) {
            v6 = v6;
            domain$fn__16767.__cached_class__2 = Util.classOf((Object)v6);
lbl24:
            // 2 sources

            v7 = domain$fn__16767.const__2.getRawRoot().invoke(v6);
        } else {
            v7 = ((MemorySize)v6).memory_size();
        }
        v8 = Numbers.add((Object)v5, (Object)v7);
        v9 = td;
        td = null;
        v10 = ((TransposedData)v9).ops;
        if (Util.classOf((Object)v10) == domain$fn__16767.__cached_class__3) ** GOTO lbl35
        if (!(v10 instanceof MemorySize)) {
            v10 = v10;
            domain$fn__16767.__cached_class__3 = Util.classOf((Object)v10);
lbl35:
            // 2 sources

            v11 = domain$fn__16767.const__2.getRawRoot().invoke(v10);
        } else {
            v11 = ((MemorySize)v10).memory_size();
        }
        return Numbers.add((Object)v8, (Object)v11);
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return domain$fn__16767.invokeStatic(object2);
    }

    static {
        const__2 = RT.var((String)"datomic.memory-size", (String)"memory-size");
    }
}

