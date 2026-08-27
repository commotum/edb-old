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

public final class kv_cluster$linear_backoff
extends AFunction {
    public static Object invokeStatic(Object n) {
        Object object = n;
        n = null;
        return Numbers.multiply((long)50L, (Object)object);
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return kv_cluster$linear_backoff.invokeStatic(object2);
    }
}

