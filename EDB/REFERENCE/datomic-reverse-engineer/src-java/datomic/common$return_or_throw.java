/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 */
package datomic;

import clojure.lang.AFunction;

public final class common$return_or_throw
extends AFunction {
    public static Object invokeStatic(Object x) {
        if (x instanceof Throwable) {
            Object object = x;
            x = null;
            throw (Throwable)object;
        }
        Object object = null;
        return x;
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return common$return_or_throw.invokeStatic(object2);
    }
}

