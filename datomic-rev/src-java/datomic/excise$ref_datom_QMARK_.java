/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.RT
 *  clojure.lang.Util
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.RT;
import clojure.lang.Util;
import clojure.lang.Var;
import datomic.db.Attribute;
import datomic.impl.db.IDatum;

public final class excise$ref_datom_QMARK_
extends AFunction {
    public static final Var const__2 = RT.var((String)"datomic.db", (String)"require-attr");

    public static Object invokeStatic(Object db2, Object d) {
        Object object = db2;
        db2 = null;
        Object object2 = d;
        d = null;
        return Util.equiv((long)20L, (Object)((Attribute)((IFn)excise$ref_datom_QMARK_.const__2.getRawRoot()).invoke((Object)object, (Object)Integer.valueOf((int)((IDatum)object2).getA()))).vtypeid) ? Boolean.TRUE : Boolean.FALSE;
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return excise$ref_datom_QMARK_.invokeStatic(object3, object4);
    }
}

