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

public final class db$tuple_install_errors$fn__13067
extends AFunction {
    Object db;
    public static final Var const__0 = RT.var((String)"datomic.db", (String)"entity-error-desc");

    public db$tuple_install_errors$fn__13067(Object object) {
        this.db = object;
    }

    public Object invoke(Object p1__13062_SHARP_) {
        Object object = p1__13062_SHARP_;
        p1__13062_SHARP_ = null;
        db$tuple_install_errors$fn__13067 this_ = null;
        return ((IFn)const__0.getRawRoot()).invoke(this_.db, object);
    }
}

