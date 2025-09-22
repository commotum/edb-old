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
import datomic.db$data_needs_index_QMARK_$fn__13177;

public final class db$data_needs_index_QMARK_
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"reduce");

    public static Object invokeStatic(Object db2, Object datoms2) {
        Object object = db2;
        db2 = null;
        Object object2 = datoms2;
        datoms2 = null;
        return ((IFn)const__0.getRawRoot()).invoke((Object)new db$data_needs_index_QMARK_$fn__13177(object), (Object)Boolean.FALSE, object2);
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return db$data_needs_index_QMARK_.invokeStatic(object3, object4);
    }
}

