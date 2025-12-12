/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 */
package datomic;

import clojure.lang.AFunction;
import datomic.memcached$wrap_metrics$fn__9939;

public final class memcached$wrap_metrics
extends AFunction {
    public static Object invokeStatic(Object f, Object record_kv, Object succ, Object fail2) {
        long start = System.nanoTime();
        Object object = succ;
        succ = null;
        Object object2 = record_kv;
        record_kv = null;
        Object object3 = f;
        f = null;
        Object object4 = fail2;
        fail2 = null;
        return new memcached$wrap_metrics$fn__9939(object, object2, start, object3, object4);
    }

    public Object invoke(Object object, Object object2, Object object3, Object object4) {
        Object object5 = object;
        object = null;
        Object object6 = object2;
        object2 = null;
        Object object7 = object3;
        object3 = null;
        Object object8 = object4;
        object4 = null;
        return memcached$wrap_metrics.invokeStatic(object5, object6, object7, object8);
    }
}

