/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 */
package datomic;

import clojure.lang.AFunction;

public final class kv_cluster$root_cause
extends AFunction {
    public static Object invokeStatic(Object x) {
        Object object;
        block2: {
            while (x instanceof Throwable) {
                Throwable cause;
                Throwable throwable = cause = ((Throwable)x).getCause();
                if (throwable != null && throwable != Boolean.FALSE) {
                    Throwable throwable2 = cause;
                    cause = null;
                    x = throwable2;
                    continue;
                }
                object = x;
                x = null;
                break block2;
            }
            object = x;
            Object object2 = null;
        }
        return object;
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return kv_cluster$root_cause.invokeStatic(object2);
    }
}

