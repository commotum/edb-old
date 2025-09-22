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

public final class db$datom_tuple_attrs
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.db", (String)"attr-tuple-attrs");

    public static Object invokeStatic(Object db2, Object d) {
        Object object = db2;
        db2 = null;
        Object object2 = d;
        d = null;
        return ((IFn)const__0.getRawRoot()).invoke(object, (Object)((IDatum)object2).getA());
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return db$datom_tuple_attrs.invokeStatic(object3, object4);
    }
}

