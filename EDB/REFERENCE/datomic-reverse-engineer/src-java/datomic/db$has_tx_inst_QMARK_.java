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
import datomic.db$has_tx_inst_QMARK_$fn__13893;

public final class db$has_tx_inst_QMARK_
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"reduce");

    public static Object invokeStatic(Object db2, Object now, Object datoms2) {
        Object object = now;
        now = null;
        Object object2 = db2;
        db2 = null;
        Object object3 = datoms2;
        datoms2 = null;
        return ((IFn)const__0.getRawRoot()).invoke((Object)new db$has_tx_inst_QMARK_$fn__13893(object, object2), (Object)Boolean.FALSE, object3);
    }

    public Object invoke(Object object, Object object2, Object object3) {
        Object object4 = object;
        object = null;
        Object object5 = object2;
        object2 = null;
        Object object6 = object3;
        object3 = null;
        return db$has_tx_inst_QMARK_.invokeStatic(object4, object5, object6);
    }
}

