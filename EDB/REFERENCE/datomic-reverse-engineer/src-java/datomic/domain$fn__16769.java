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
import datomic.index.RootNode;
import datomic.memory_size.MemorySize;

public final class domain$fn__16769
extends AFunction {
    private static Class __cached_class__0;
    private static Class __cached_class__1;
    public static final Var const__2;

    /*
     * Unable to fully structure code
     */
    public static Object invokeStatic(Object root) {
        v0 = ((RootNode)root).keydata;
        if (Util.classOf((Object)v0) == domain$fn__16769.__cached_class__0) ** GOTO lbl6
        if (!(v0 instanceof MemorySize)) {
            v0 = v0;
            domain$fn__16769.__cached_class__0 = Util.classOf((Object)v0);
lbl6:
            // 2 sources

            v1 = domain$fn__16769.const__2.getRawRoot().invoke(v0);
        } else {
            v1 = ((MemorySize)v0).memory_size();
        }
        v2 = Numbers.add((long)32L, (Object)v1);
        v3 = ((RootNode)root).dirids;
        if (Util.classOf((Object)v3) == domain$fn__16769.__cached_class__1) ** GOTO lbl15
        if (!(v3 instanceof MemorySize)) {
            v3 = v3;
            domain$fn__16769.__cached_class__1 = Util.classOf((Object)v3);
lbl15:
            // 2 sources

            v4 = domain$fn__16769.const__2.getRawRoot().invoke(v3);
        } else {
            v4 = ((MemorySize)v3).memory_size();
        }
        v5 = root;
        root = null;
        return Numbers.add((Object)Numbers.add((Object)v2, (Object)v4), (long)Numbers.multiply((long)8L, (long)RT.count((Object)((RootNode)v5).dirs)));
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return domain$fn__16769.invokeStatic(object2);
    }

    static {
        const__2 = RT.var((String)"datomic.memory-size", (String)"memory-size");
    }
}

