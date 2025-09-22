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

public final class catalog$create_database_STAR_$fn__11116
extends AFunction {
    Object assigned_db_id;
    Object db_name;
    public static final Var const__0 = RT.var((String)"datomic.catalog", (String)"add-database");

    public catalog$create_database_STAR_$fn__11116(Object object, Object object2) {
        this.assigned_db_id = object;
        this.db_name = object2;
    }

    public Object invoke(Object p1__11113_SHARP_) {
        Object object = p1__11113_SHARP_;
        p1__11113_SHARP_ = null;
        catalog$create_database_STAR_$fn__11116 this_ = null;
        return ((IFn)const__0.getRawRoot()).invoke(object, this_.db_name, this_.assigned_db_id);
    }
}

