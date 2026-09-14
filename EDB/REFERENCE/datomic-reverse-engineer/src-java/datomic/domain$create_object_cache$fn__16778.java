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

public final class domain$create_object_cache$fn__16778
extends AFunction {
    private static Class __cached_class__0;
    private static Class __cached_class__1;
    public static final Var const__1;

    /*
     * Unable to fully structure code
     */
    public Object invoke(Object k, Object v) {
        v0 = k;
        k = null;
        v1 = v0;
        if (Util.classOf((Object)v0) == domain$create_object_cache$fn__16778.__cached_class__0) ** GOTO lbl8
        if (!(v1 instanceof MemorySize)) {
            v1 = v1;
            domain$create_object_cache$fn__16778.__cached_class__0 = Util.classOf((Object)v1);
lbl8:
            // 2 sources

            v2 = domain$create_object_cache$fn__16778.const__1.getRawRoot().invoke(v1);
        } else {
            v2 = ((MemorySize)v1).memory_size();
        }
        v3 = v;
        v = null;
        v4 = v3;
        if (Util.classOf((Object)v3) == domain$create_object_cache$fn__16778.__cached_class__1) ** GOTO lbl18
        if (!(v4 instanceof MemorySize)) {
            v4 = v4;
            domain$create_object_cache$fn__16778.__cached_class__1 = Util.classOf((Object)v4);
lbl18:
            // 2 sources

            v5 = domain$create_object_cache$fn__16778.const__1.getRawRoot().invoke(v4);
        } else {
            v5 = ((MemorySize)v4).memory_size();
        }
        this = null;
        return Numbers.add((Object)v2, (Object)v5);
    }

    static {
        const__1 = RT.var((String)"datomic.memory-size", (String)"memory-size");
    }
}

