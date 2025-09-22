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
import datomic.index.DirNode;
import datomic.memory_size.MemorySize;

public final class domain$fn__16771
extends AFunction {
    private static Class __cached_class__0;
    private static Class __cached_class__1;
    private static Class __cached_class__2;
    private static Class __cached_class__3;
    public static final Var const__2;

    /*
     * Unable to fully structure code
     */
    public static Object invokeStatic(Object dir) {
        v0 = ((DirNode)dir).keydata;
        if (Util.classOf((Object)v0) == domain$fn__16771.__cached_class__0) ** GOTO lbl6
        if (!(v0 instanceof MemorySize)) {
            v0 = v0;
            domain$fn__16771.__cached_class__0 = Util.classOf((Object)v0);
lbl6:
            // 2 sources

            v1 = domain$fn__16771.const__2.getRawRoot().invoke(v0);
        } else {
            v1 = ((MemorySize)v0).memory_size();
        }
        v2 = Numbers.add((long)48L, (Object)v1);
        v3 = ((DirNode)dir).segids;
        if (Util.classOf((Object)v3) == domain$fn__16771.__cached_class__1) ** GOTO lbl15
        if (!(v3 instanceof MemorySize)) {
            v3 = v3;
            domain$fn__16771.__cached_class__1 = Util.classOf((Object)v3);
lbl15:
            // 2 sources

            v4 = domain$fn__16771.const__2.getRawRoot().invoke(v3);
        } else {
            v4 = ((MemorySize)v3).memory_size();
        }
        v5 = Numbers.add((Object)v2, (Object)v4);
        v6 = ((DirNode)dir).offsets;
        if (Util.classOf((Object)v6) == domain$fn__16771.__cached_class__2) ** GOTO lbl24
        if (!(v6 instanceof MemorySize)) {
            v6 = v6;
            domain$fn__16771.__cached_class__2 = Util.classOf((Object)v6);
lbl24:
            // 2 sources

            v7 = domain$fn__16771.const__2.getRawRoot().invoke(v6);
        } else {
            v7 = ((MemorySize)v6).memory_size();
        }
        v8 = Numbers.add((Object)v5, (Object)v7);
        v9 = ((DirNode)dir).counts;
        if (Util.classOf((Object)v9) == domain$fn__16771.__cached_class__3) ** GOTO lbl33
        if (!(v9 instanceof MemorySize)) {
            v9 = v9;
            domain$fn__16771.__cached_class__3 = Util.classOf((Object)v9);
lbl33:
            // 2 sources

            v10 = domain$fn__16771.const__2.getRawRoot().invoke(v9);
        } else {
            v10 = ((MemorySize)v9).memory_size();
        }
        v11 = dir;
        dir = null;
        return Numbers.add((Object)Numbers.add((Object)v8, (Object)v10), (long)Numbers.multiply((long)8L, (long)RT.count((Object)((DirNode)v11).segs)));
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return domain$fn__16771.invokeStatic(object2);
    }

    static {
        const__2 = RT.var((String)"datomic.memory-size", (String)"memory-size");
    }
}

