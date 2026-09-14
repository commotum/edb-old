/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 */
package datomic.core2.val_store;

import clojure.lang.AFunction;
import datomic.core2.val_store.fs$wrap_metric_handler$fn__21259;

public final class fs$wrap_metric_handler
extends AFunction {
    public static Object invokeStatic(Object f, Object k, Object op) {
        long start_nsec = System.nanoTime();
        Object object = op;
        op = null;
        Object object2 = f;
        f = null;
        Object object3 = k;
        k = null;
        return new fs$wrap_metric_handler$fn__21259(object, object2, start_nsec, object3);
    }

    public Object invoke(Object object, Object object2, Object object3) {
        Object object4 = object;
        object = null;
        Object object5 = object2;
        object2 = null;
        Object object6 = object3;
        object3 = null;
        return fs$wrap_metric_handler.invokeStatic(object4, object5, object6);
    }
}

