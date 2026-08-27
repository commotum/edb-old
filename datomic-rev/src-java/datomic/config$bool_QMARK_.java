/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 */
package datomic;

import clojure.lang.AFunction;

public final class config$bool_QMARK_
extends AFunction {
    public static Object invokeStatic(Object x) {
        Object object = x;
        x = null;
        return object instanceof Boolean ? Boolean.TRUE : Boolean.FALSE;
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return config$bool_QMARK_.invokeStatic(object2);
    }
}

