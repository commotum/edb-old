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

public final class memory$transactor_cache_bytes
extends AFunction {
    public static Object invokeStatic(Object memidx_max) {
        Object object = memidx_max;
        memidx_max = null;
        return Numbers.minus((long)Numbers.minus((long)Runtime.getRuntime().maxMemory(), (long)Numbers.multiply((long)Numbers.multiply((long)100L, (long)1024L), (long)1024L)), (Object)object);
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return memory$transactor_cache_bytes.invokeStatic(object2);
    }
}

