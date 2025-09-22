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
import datomic.impl.db.IDatum;

public final class db$system_datom_QMARK_
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.db", (String)"system-schema-datom?");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"contains?");
    public static final Var const__2 = RT.var((String)"datomic.db", (String)"SYSTEM_ATTRS");

    public static Object invokeStatic(Object db2, Object d) {
        Object object;
        Object or__5238__auto__13187;
        Object object2 = db2;
        db2 = null;
        Object object3 = or__5238__auto__13187 = ((IFn)const__0.getRawRoot()).invoke(object2, d);
        if (object3 != null && object3 != Boolean.FALSE) {
            object = or__5238__auto__13187;
            or__5238__auto__13187 = null;
        } else {
            Object object4 = d;
            d = null;
            object = ((IFn)const__1.getRawRoot()).invoke(const__2.getRawRoot(), (Object)((IDatum)object4).getA());
        }
        return object;
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return db$system_datom_QMARK_.invokeStatic(object3, object4);
    }
}

