/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 */
package datomic;

import clojure.lang.AFunction;
import datomic.log$combine_last_if$fn__16535;

public final class log$combine_last_if
extends AFunction {
    public static Object invokeStatic(Object pred2, Object combine) {
        Object object = combine;
        combine = null;
        Object object2 = pred2;
        pred2 = null;
        return new log$combine_last_if$fn__16535(object, object2);
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return log$combine_last_if.invokeStatic(object3, object4);
    }
}

