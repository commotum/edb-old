/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 */
package datomic;

import clojure.lang.AFunction;
import datomic.btset.BTSet;

public final class btset$rseek
extends AFunction {
    public static Object invokeStatic(Object bt, Object k) {
        Object object = bt;
        bt = null;
        Object object2 = k;
        k = null;
        return ((BTSet)object).rseek(object2);
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return btset$rseek.invokeStatic(object3, object4);
    }

    public static Object invokeStatic(Object bt) {
        Object object = bt;
        bt = null;
        return ((BTSet)object).rseek();
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return btset$rseek.invokeStatic(object2);
    }
}

