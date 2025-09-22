/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 */
package datomic;

import clojure.lang.AFunction;
import datomic.Connection;
import java.util.Date;

public final class api$gc_storage
extends AFunction {
    public static Object invokeStatic(Object connection, Object older_than) {
        Object object = connection;
        connection = null;
        Object object2 = older_than;
        older_than = null;
        ((Connection)object).gcStorage((Date)object2);
        return null;
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return api$gc_storage.invokeStatic(object3, object4);
    }
}

