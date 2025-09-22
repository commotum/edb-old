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

public final class catalog$remove_deleted_database$fn__11150
extends AFunction {
    Object db_id;
    public static final Var const__0 = RT.var((String)"datomic.catalog", (String)"remove-deleted");

    public catalog$remove_deleted_database$fn__11150(Object object) {
        this.db_id = object;
    }

    public Object invoke(Object p1__11147_SHARP_) {
        Object object = p1__11147_SHARP_;
        p1__11147_SHARP_ = null;
        catalog$remove_deleted_database$fn__11150 this_ = null;
        return ((IFn)const__0.getRawRoot()).invoke(object, this_.db_id);
    }
}

