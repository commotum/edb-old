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
import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;

public final class queue$fn__12141
extends AFunction {
    public static Object invokeStatic(Object q2, Object or_else, Object msec) {
        Reference reference;
        Reference or__5238__auto__12143;
        Object object = q2;
        q2 = null;
        Object object2 = msec;
        msec = null;
        Reference reference2 = or__5238__auto__12143 = ((ReferenceQueue)object).remove(RT.longCast((Object)((Number)object2)));
        if (reference2 != null && reference2 != Boolean.FALSE) {
            reference = or__5238__auto__12143;
            or__5238__auto__12143 = null;
        } else {
            reference = or_else;
            Object var1_1 = null;
        }
        return reference;
    }

    public Object invoke(Object object, Object object2, Object object3) {
        Object object4 = object;
        object = null;
        Object object5 = object2;
        object2 = null;
        Object object6 = object3;
        object3 = null;
        return queue$fn__12141.invokeStatic(object4, object5, object6);
    }
}

