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

public final class db$has_unique_id_QMARK_$unique_id_QMARK___13694
extends AFunction {
    Object db;
    public static final Var const__2 = RT.var((String)"datomic.db", (String)"require-attr");

    public db$has_unique_id_QMARK_$unique_id_QMARK___13694(Object object) {
        this.db = object;
    }

    public Object invoke(Object p1__13692_SHARP_) {
        Object object = p1__13692_SHARP_;
        p1__13692_SHARP_ = null;
        db$has_unique_id_QMARK_$unique_id_QMARK___13694 this_ = null;
        return Util.equiv((long)38L, (Object)((Attribute)((IFn)db$has_unique_id_QMARK_$unique_id_QMARK___13694.const__2.getRawRoot()).invoke((Object)this_.db, (Object)object)).unique) ? Boolean.TRUE : Boolean.FALSE;
    }
}

