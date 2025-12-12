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

public final class db$memdb_needs_index_QMARK_$fn__13189
extends AFunction {
    Object db;
    public static final Var const__0 = RT.var((String)"datomic.db", (String)"system-datom?");

    public db$memdb_needs_index_QMARK_$fn__13189(Object object) {
        this.db = object;
    }

    public Object invoke(Object p1__13188_SHARP_) {
        Object object = p1__13188_SHARP_;
        p1__13188_SHARP_ = null;
        db$memdb_needs_index_QMARK_$fn__13189 this_ = null;
        return ((IFn)const__0.getRawRoot()).invoke(this_.db, object);
    }
}

