/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 */
package datomic;

import clojure.lang.AFunction;

public final class error$fn__684
extends AFunction {
    public static Object invokeStatic(Object t) {
        Object object = t;
        t = null;
        ((Throwable)object).printStackTrace();
        return null;
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return error$fn__684.invokeStatic(object2);
    }
}

