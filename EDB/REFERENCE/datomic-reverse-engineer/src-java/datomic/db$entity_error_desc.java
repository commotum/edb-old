/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 */
package datomic;

import clojure.lang.AFunction;
import datomic.Database;

public final class db$entity_error_desc
extends AFunction {
    public static Object invokeStatic(Object db2, Object eid) {
        Object object;
        Object or__5238__auto__13026;
        Object object2 = db2;
        db2 = null;
        Object object3 = or__5238__auto__13026 = ((Database)object2).ident(eid);
        if (object3 != null && object3 != Boolean.FALSE) {
            object = or__5238__auto__13026;
            or__5238__auto__13026 = null;
        } else {
            object = eid;
            Object var1_1 = null;
        }
        return object;
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return db$entity_error_desc.invokeStatic(object3, object4);
    }
}

