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

public final class catalog$delete_database$fn__11135
extends AFunction {
    Object db_name;
    public static final Var const__0 = RT.var((String)"datomic.catalog", (String)"delete");

    public catalog$delete_database$fn__11135(Object object) {
        this.db_name = object;
    }

    public Object invoke(Object p1__11132_SHARP_) {
        Object object = p1__11132_SHARP_;
        p1__11132_SHARP_ = null;
        catalog$delete_database$fn__11135 this_ = null;
        return ((IFn)const__0.getRawRoot()).invoke(object, this_.db_name);
    }
}

