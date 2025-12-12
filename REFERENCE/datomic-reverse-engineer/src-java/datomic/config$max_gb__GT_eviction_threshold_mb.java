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

public final class config$max_gb__GT_eviction_threshold_mb
extends AFunction {
    public static Object invokeStatic(Object max_gb) {
        Object object = max_gb;
        max_gb = null;
        return Numbers.minus((Object)Numbers.multiply((Object)object, (long)900L), (long)500L);
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return config$max_gb__GT_eviction_threshold_mb.invokeStatic(object2);
    }
}

