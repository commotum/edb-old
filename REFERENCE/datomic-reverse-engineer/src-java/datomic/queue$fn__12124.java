/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 */
package datomic;

import clojure.lang.AFunction;
import java.util.concurrent.BlockingQueue;

public final class queue$fn__12124
extends AFunction {
    public static Object invokeStatic(Object q2, Object item) {
        Object object = q2;
        q2 = null;
        Object object2 = item;
        item = null;
        ((BlockingQueue)object).put(object2);
        return Boolean.TRUE;
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return queue$fn__12124.invokeStatic(object3, object4);
    }
}

