/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  org.slf4j.Logger
 */
package datomic;

import clojure.lang.AFunction;
import org.slf4j.Logger;

public final class slf4j$caused_by
extends AFunction {
    public static Object invokeStatic(Object logger, Object t) {
        Object object = t;
        t = null;
        Throwable t2 = ((Throwable)object).getCause();
        while (true) {
            Throwable throwable = t2;
            if (throwable == null || throwable == Boolean.FALSE) break;
            ((Logger)logger).warn("... caused by ...", t2);
            Throwable throwable2 = t2;
            t2 = null;
            t2 = throwable2.getCause();
        }
        return null;
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return slf4j$caused_by.invokeStatic(object3, object4);
    }
}

