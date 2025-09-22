/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 */
package datomic;

import clojure.lang.AFunction;
import datomic.db.Attribute;
import datomic.db.IDbImpl;

public final class db$attribute
extends AFunction {
    public static Object invokeStatic(Object db2, Object attrid) {
        Object object;
        Object temp__5457__auto__12600;
        Object object2 = db2;
        db2 = null;
        Object object3 = attrid;
        attrid = null;
        Object object4 = temp__5457__auto__12600 = ((IDbImpl)object2).elementAt(object3);
        if (object4 != null && object4 != Boolean.FALSE) {
            Object object5 = temp__5457__auto__12600;
            temp__5457__auto__12600 = null;
            Object attr = object5;
            if (attr instanceof Attribute) {
                object = attr;
                attr = null;
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
        return db$attribute.invokeStatic(object3, object4);
    }
}

