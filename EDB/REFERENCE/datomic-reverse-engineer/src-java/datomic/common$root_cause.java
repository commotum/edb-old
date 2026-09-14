/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 */
package datomic;

import clojure.lang.AFunction;

public final class common$root_cause
extends AFunction {
    public static Object invokeStatic(Object x) {
        Object object;
        block2: {
            block1: {
                while (true) {
                    Throwable cause;
                    Object object2 = x;
                    if (object2 == null || object2 == Boolean.FALSE) break block1;
                    Throwable throwable = cause = ((Throwable)x).getCause();
                    if (throwable == null || throwable == Boolean.FALSE) break;
                    Throwable throwable2 = cause;
                    cause = null;
                    x = throwable2;
                }
                object = x;
                x = null;
                break block2;
            }
            object = null;
        }
        return object;
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return common$root_cause.invokeStatic(object2);
    }
}

