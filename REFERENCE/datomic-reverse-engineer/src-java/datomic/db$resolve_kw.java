/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 */
package datomic;

import clojure.lang.AFunction;
import datomic.db.IDb;

public final class db$resolve_kw
extends AFunction {
    public static Object invokeStatic(Object db2, Object x) {
        Object object;
        if (x instanceof Number) {
            Object object2 = db2;
            db2 = null;
            Object object3 = x;
            x = null;
            object = ((IDb)object2).keywordOf(object3);
        } else {
            object = x;
            Object var1_1 = null;
        }
        return object;
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return db$resolve_kw.invokeStatic(object3, object4);
    }
}

