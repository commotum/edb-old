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
import datomic.db$has_unique_id_QMARK_$fn__13696;
import datomic.db$has_unique_id_QMARK_$unique_id_QMARK___13694;

public final class db$has_unique_id_QMARK_
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"some");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"keys");

    public static Object invokeStatic(Object db2, Object emap2) {
        db$has_unique_id_QMARK_$unique_id_QMARK___13694 unique_id_QMARK_;
        Object object = db2;
        db2 = null;
        db$has_unique_id_QMARK_$unique_id_QMARK___13694 db$has_unique_id_QMARK_$unique_id_QMARK___13694 = unique_id_QMARK_ = new db$has_unique_id_QMARK_$unique_id_QMARK___13694(object);
        unique_id_QMARK_ = null;
        Object object2 = emap2;
        emap2 = null;
        return ((IFn)const__0.getRawRoot()).invoke((Object)new db$has_unique_id_QMARK_$fn__13696((Object)db$has_unique_id_QMARK_$unique_id_QMARK___13694), ((IFn)const__1.getRawRoot()).invoke(object2));
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return db$has_unique_id_QMARK_.invokeStatic(object3, object4);
    }
}

