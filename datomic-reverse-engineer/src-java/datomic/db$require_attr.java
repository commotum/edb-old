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
import datomic.db.IDbImpl;

public final class db$require_attr
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.db", (String)"require-attrid");

    public static Object invokeStatic(Object db2, Object x) {
        IDbImpl iDbImpl = (IDbImpl)db2;
        Object object = db2;
        db2 = null;
        Object object2 = x;
        x = null;
        return iDbImpl.elementAt(((IFn)const__0.getRawRoot()).invoke(object, object2));
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return db$require_attr.invokeStatic(object3, object4);
    }
}

