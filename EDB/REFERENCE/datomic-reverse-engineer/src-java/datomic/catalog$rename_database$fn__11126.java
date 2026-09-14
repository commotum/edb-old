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

public final class catalog$rename_database$fn__11126
extends AFunction {
    Object db_name;
    Object new_name;
    public static final Var const__0 = RT.var((String)"datomic.catalog", (String)"rename");

    public catalog$rename_database$fn__11126(Object object, Object object2) {
        this.db_name = object;
        this.new_name = object2;
    }

    public Object invoke(Object p1__11123_SHARP_) {
        Object object = p1__11123_SHARP_;
        p1__11123_SHARP_ = null;
        catalog$rename_database$fn__11126 this_ = null;
        return ((IFn)const__0.getRawRoot()).invoke(object, this_.db_name, this_.new_name);
    }
}

