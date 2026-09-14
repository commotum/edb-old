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

public final class db$tuple_install_errors$fn__13065
extends AFunction {
    Object db;
    public static final Var const__0 = RT.var((String)"datomic.db", (String)"tuple-attr-value-type");

    public db$tuple_install_errors$fn__13065(Object object) {
        this.db = object;
    }

    public Object invoke(Object p1__13061_SHARP_) {
        Object object = p1__13061_SHARP_;
        p1__13061_SHARP_ = null;
        db$tuple_install_errors$fn__13065 this_ = null;
        return ((IFn)const__0.getRawRoot()).invoke(this_.db, object);
    }
}

