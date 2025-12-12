/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 */
package datomic;

import clojure.lang.AFunction;
import datomic.db.IDbImpl;

public final class db$accept_data_no_check
extends AFunction {
    public static Object invokeStatic(Object db2, Object tx) {
        Object object = db2;
        db2 = null;
        Object object2 = tx;
        tx = null;
        return ((IDbImpl)object).acceptDataCheck(object2, Boolean.FALSE);
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return db$accept_data_no_check.invokeStatic(object3, object4);
    }
}

