/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.Keyword
 *  clojure.lang.RT
 *  clojure.lang.Util
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Keyword;
import clojure.lang.RT;
import clojure.lang.Util;
import clojure.lang.Var;
import datomic.impl.db.IDatum;

public final class db$ensure_datom_QMARK_
extends AFunction {
    public static final Var const__1 = RT.var((String)"datomic.db", (String)"system-eid");
    public static final Keyword const__2 = RT.keyword((String)"db", (String)"ensure");

    public static Object invokeStatic(Object db2, Object d) {
        Boolean bl;
        Object object = db2;
        db2 = null;
        boolean and__5236__auto__13931 = Util.equiv((Object)((IFn)const__1.getRawRoot()).invoke(object, (Object)const__2), (long)((IDatum)d).getA());
        if (and__5236__auto__13931) {
            Object object2 = d;
            d = null;
            bl = ((IDatum)object2).isAssertion() ? Boolean.TRUE : Boolean.FALSE;
        } else {
            bl = and__5236__auto__13931 ? Boolean.TRUE : Boolean.FALSE;
        }
        return bl;
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return db$ensure_datom_QMARK_.invokeStatic(object3, object4);
    }
}

