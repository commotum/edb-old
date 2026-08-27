/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.Numbers
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.Numbers;

public final class memory$segment_cache_max
extends AFunction {
    public static Object invokeStatic(Object segment_memory_size) {
        Object object = segment_memory_size;
        segment_memory_size = null;
        return Numbers.quotient((Object)object, (long)65536L);
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return memory$segment_cache_max.invokeStatic(object2);
    }
}

