/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 */
package datomic;

import clojure.lang.AFunction;
import datomic.db.IDbImpl;
import datomic.db.ValueType;

public final class db$value_type
extends AFunction {
    public static Object invokeStatic(Object db2, Object attrid) {
        Object object;
        Object temp__5457__auto__12711;
        Object object2 = db2;
        db2 = null;
        Object object3 = attrid;
        attrid = null;
        Object object4 = temp__5457__auto__12711 = ((IDbImpl)object2).elementAt(object3);
        if (object4 != null && object4 != Boolean.FALSE) {
            Object object5 = temp__5457__auto__12711;
            temp__5457__auto__12711 = null;
            Object vt = object5;
            if (vt instanceof ValueType) {
                object = vt;
                vt = null;
            } else {
                object = null;
            }
        } else {
            object = null;
        }
        return object;
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return db$value_type.invokeStatic(object3, object4);
    }
}

