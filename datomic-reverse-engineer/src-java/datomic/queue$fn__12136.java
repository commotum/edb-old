/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 */
package datomic;

import clojure.lang.AFunction;
import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;

public final class queue$fn__12136
extends AFunction {
    public static Object invokeStatic(Object q2, Object or_else) {
        Reference reference;
        Reference or__5238__auto__12138;
        Object object = q2;
        q2 = null;
        Reference reference2 = or__5238__auto__12138 = ((ReferenceQueue)object).poll();
        if (reference2 != null && reference2 != Boolean.FALSE) {
            reference = or__5238__auto__12138;
            or__5238__auto__12138 = null;
        } else {
            reference = or_else;
            Object var1_1 = null;
        }
        return reference;
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return queue$fn__12136.invokeStatic(object3, object4);
    }
}

