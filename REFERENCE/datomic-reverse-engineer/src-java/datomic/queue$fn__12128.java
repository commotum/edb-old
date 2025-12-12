/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 */
package datomic;

import clojure.lang.AFunction;
import java.util.Queue;

public final class queue$fn__12128
extends AFunction {
    public static Object invokeStatic(Object q2, Object or_else) {
        Object object;
        Object or__5238__auto__12130;
        Object object2 = q2;
        q2 = null;
        Object e = or__5238__auto__12130 = ((Queue)object2).poll();
        if (e != null && e != Boolean.FALSE) {
            object = or__5238__auto__12130;
            or__5238__auto__12130 = null;
        } else {
            object = or_else;
            Object var1_1 = null;
        }
        return object;
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return queue$fn__12128.invokeStatic(object3, object4);
    }
}

