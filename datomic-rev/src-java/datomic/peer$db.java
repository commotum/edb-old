/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 */
package datomic;

import clojure.lang.AFunction;
import datomic.Connection;

public final class peer$db
extends AFunction {
    public static Object invokeStatic(Object conn) {
        Object object = conn;
        conn = null;
        return ((Connection)object).db();
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return peer$db.invokeStatic(object2);
    }
}

