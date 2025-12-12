/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.RT;
import clojure.lang.Var;
import datomic.db.Attribute;
import datomic.db.IDbImpl;

public final class datalog$attr_QMARK_
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.db", (String)"resolve-id");

    public static Object invokeStatic(Object db2, Object x) {
        Boolean bl;
        Object attrid;
        Object object = x;
        x = null;
        Object object2 = attrid = ((IFn)const__0.getRawRoot()).invoke(db2, object);
        if (object2 != null && object2 != Boolean.FALSE) {
            Object object3 = db2;
            db2 = null;
            Object object4 = attrid;
            attrid = null;
            bl = ((IDbImpl)object3).elementAt(object4) instanceof Attribute ? Boolean.TRUE : Boolean.FALSE;
        } else {
            bl = null;
        }
        return bl;
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return datalog$attr_QMARK_.invokeStatic(object3, object4);
    }
}

