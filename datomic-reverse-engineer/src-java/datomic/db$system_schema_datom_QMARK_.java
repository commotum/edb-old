/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.Numbers
 *  clojure.lang.RT
 *  clojure.lang.Util
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Numbers;
import clojure.lang.RT;
import clojure.lang.Util;
import clojure.lang.Var;
import datomic.Database;
import datomic.impl.db.IDatum;

public final class db$system_schema_datom_QMARK_
extends AFunction {
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"namespace");
    public static final Var const__2 = RT.var((String)"datomic.db", (String)"system-schema-namespace?");

    public static Object invokeStatic(Object db2, Object d) {
        Object object;
        Object temp__5457__auto__13185;
        Object object2;
        Object object3 = db2;
        db2 = null;
        Object object4 = d;
        d = null;
        Object G__13183 = ((Database)object3).ident(Numbers.num((long)((IDatum)object4).getE()));
        if (Util.identical((Object)G__13183, null)) {
            object2 = null;
        } else {
            Object object5 = G__13183;
            G__13183 = null;
            object2 = ((IFn)const__1.getRawRoot()).invoke(object5);
        }
        Object object6 = temp__5457__auto__13185 = object2;
        if (object6 != null && object6 != Boolean.FALSE) {
            Object n;
            Object object7 = temp__5457__auto__13185;
            temp__5457__auto__13185 = null;
            Object object8 = n = object7;
            n = null;
            object = ((IFn)const__2.getRawRoot()).invoke(object8);
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
        return db$system_schema_datom_QMARK_.invokeStatic(object3, object4);
    }
}

