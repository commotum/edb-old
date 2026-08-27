/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.RT
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.RT;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

public final class queue$fn__12126
extends AFunction {
    public static Object invokeStatic(Object q2, Object item, Object msec) {
        Object object = q2;
        q2 = null;
        Object object2 = item;
        item = null;
        Object object3 = msec;
        msec = null;
        return ((BlockingQueue)object).offer(object2, RT.longCast((Object)((Number)object3)), TimeUnit.MILLISECONDS) ? Boolean.TRUE : Boolean.FALSE;
    }

    public Object invoke(Object object, Object object2, Object object3) {
        Object object4 = object;
        object = null;
        Object object5 = object2;
        object2 = null;
        Object object6 = object3;
        object3 = null;
        return queue$fn__12126.invokeStatic(object4, object5, object6);
    }
}

