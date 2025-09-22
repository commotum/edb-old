/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 */
package datomic.core2.val_store;

import clojure.lang.AFunction;
import datomic.core2.val_store.s3$wrap_metric_handler$fn__21408;

public final class s3$wrap_metric_handler
extends AFunction {
    public static Object invokeStatic(Object f, Object k, Object op, Object context) {
        long start_nsec = System.nanoTime();
        Object object = context;
        context = null;
        Object object2 = k;
        k = null;
        Object object3 = op;
        op = null;
        Object object4 = f;
        f = null;
        return new s3$wrap_metric_handler$fn__21408(object, object2, start_nsec, object3, object4);
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
        return s3$wrap_metric_handler.invokeStatic(object5, object6, object7, object8);
    }
}

