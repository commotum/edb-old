/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 */
package datomic;

import clojure.lang.AFunction;
import java.util.concurrent.ExecutionException;

public final class promise$throw_executionexception_if_throwable
extends AFunction {
    public static Object invokeStatic(Object o) {
        if (o instanceof Throwable) {
            Object object = o;
            o = null;
            throw (Throwable)new ExecutionException((Throwable)object);
        }
        Object object = null;
        return o;
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return promise$throw_executionexception_if_throwable.invokeStatic(object2);
    }
}

