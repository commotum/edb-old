/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 */
package datomic;

import clojure.lang.AFunction;
import datomic.Database;

public final class api$entity
extends AFunction {
    public static Object invokeStatic(Object db2, Object eid) {
        Object object = db2;
        db2 = null;
        Object object2 = eid;
        eid = null;
        return ((Database)object).entity(object2);
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return api$entity.invokeStatic(object3, object4);
    }
}

