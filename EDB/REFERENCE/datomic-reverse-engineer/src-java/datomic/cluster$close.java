/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 */
package datomic;

import clojure.lang.AFunction;

public final class cluster$close
extends AFunction {
    public static Object invokeStatic(Object x) {
        Object v3;
        Object object;
        Object and__5236__auto__10745;
        Object object2 = and__5236__auto__10745 = x;
        if (object2 != null && object2 != Boolean.FALSE) {
            object = x instanceof AutoCloseable ? Boolean.TRUE : Boolean.FALSE;
        } else {
            object = and__5236__auto__10745;
            Object var1_1 = null;
        }
        if (object != null && object != Boolean.FALSE) {
            Object object3 = x;
            x = null;
            ((AutoCloseable)object3).close();
            v3 = null;
        } else {
            v3 = null;
        }
        return v3;
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return cluster$close.invokeStatic(object2);
    }
}

