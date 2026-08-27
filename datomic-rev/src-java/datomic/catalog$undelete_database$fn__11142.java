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

public final class catalog$undelete_database$fn__11142
extends AFunction {
    Object db_name;
    Object db_id;
    public static final Var const__0 = RT.var((String)"datomic.catalog", (String)"add-database");

    public catalog$undelete_database$fn__11142(Object object, Object object2) {
        this.db_name = object;
        this.db_id = object2;
    }

    public Object invoke(Object p1__11139_SHARP_) {
        Object object = p1__11139_SHARP_;
        p1__11139_SHARP_ = null;
        catalog$undelete_database$fn__11142 this_ = null;
        return ((IFn)const__0.getRawRoot()).invoke(object, this_.db_name, this_.db_id);
    }
}

