/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 */
package datomic;

import clojure.lang.AFunction;
import datomic.Connection;

public final class api$request_index
extends AFunction {
    public static Object invokeStatic(Object connection) {
        Object object = connection;
        connection = null;
        return ((Connection)object).requestIndex() ? Boolean.TRUE : Boolean.FALSE;
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return api$request_index.invokeStatic(object2);
    }
}

