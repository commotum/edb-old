/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 */
package datomic;

import clojure.lang.AFunction;
import datomic.Connection;

public final class api$log
extends AFunction {
    public static Object invokeStatic(Object connection) {
        Object object = connection;
        connection = null;
        return ((Connection)object).log();
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return api$log.invokeStatic(object2);
    }
}

