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

public final class memory$object_cache_max
extends AFunction {
    public static Object invokeStatic(Object virtual_memory_size) {
        Object object = virtual_memory_size;
        virtual_memory_size = null;
        return Numbers.quotient((Object)object, (long)480000L);
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return memory$object_cache_max.invokeStatic(object2);
    }
}

