/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 */
package datomic;

import clojure.lang.AFunction;
import datomic.Connection;
import java.util.List;

public final class peer$transact
extends AFunction {
    public static Object invokeStatic(Object conn, Object txdata) {
        Object object = conn;
        conn = null;
        Object object2 = txdata;
        txdata = null;
        return ((Connection)object).transact((List)object2);
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return peer$transact.invokeStatic(object3, object4);
    }
}

