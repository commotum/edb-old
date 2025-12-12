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

public final class kv_cluster$exponential_backoff
extends AFunction {
    public static final Object const__1 = 0L;
    public static final Object const__4 = 2L;

    public static Object invokeStatic(Object n) {
        Object object;
        if (Numbers.isZero((Object)n)) {
            object = const__1;
        } else {
            Object object2 = n;
            n = null;
            object = Numbers.multiply((long)50L, (double)Math.pow(RT.doubleCast((Object)((Number)const__4)), RT.doubleCast((Object)Numbers.dec((Object)object2))));
        }
        return object;
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return kv_cluster$exponential_backoff.invokeStatic(object2);
    }
}

