/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.Numbers
 *  clojure.lang.RT
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.Numbers;
import clojure.lang.RT;

public final class memory_size$fn__418
extends AFunction {
    public static Object invokeStatic(Object x) {
        Object object = x;
        x = null;
        return Numbers.num((long)Numbers.add((long)16L, (long)Numbers.multiply((long)2L, (long)RT.count((Object)object))));
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return memory_size$fn__418.invokeStatic(object2);
    }
}

