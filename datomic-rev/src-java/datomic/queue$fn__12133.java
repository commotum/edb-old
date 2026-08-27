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

public final class queue$fn__12133
extends AFunction {
    public static Object invokeStatic(Object q2, Object or_else, Object msec) {
        Object object;
        Object or__5238__auto__12135;
        Object object2 = q2;
        q2 = null;
        Object object3 = msec;
        msec = null;
        Object e = or__5238__auto__12135 = ((BlockingQueue)object2).poll(RT.longCast((Object)((Number)object3)), TimeUnit.MILLISECONDS);
        if (e != null && e != Boolean.FALSE) {
            object = or__5238__auto__12135;
            or__5238__auto__12135 = null;
        } else {
            object = or_else;
            Object var1_1 = null;
        }
        return object;
    }

    public Object invoke(Object object, Object object2, Object object3) {
        Object object4 = object;
        object = null;
        Object object5 = object2;
        object2 = null;
        Object object6 = object3;
        object3 = null;
        return queue$fn__12133.invokeStatic(object4, object5, object6);
    }
}

