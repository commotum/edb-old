/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic.db;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.RT;
import clojure.lang.Var;

public final class ProcessExpander$fn__14027
extends AFunction {
    Object db;
    Object datoms;
    Object check_installs_QMARK_;
    public static final Var const__0 = RT.var((String)"datomic.db", (String)"filter-assess-tx-datoms");

    public ProcessExpander$fn__14027(Object object, Object object2, Object object3) {
        this.db = object;
        this.datoms = object2;
        this.check_installs_QMARK_ = object3;
    }

    public Object invoke() {
        ProcessExpander$fn__14027 this_ = null;
        return ((IFn)const__0.getRawRoot()).invoke(this_.db, this_.check_installs_QMARK_, this_.datoms);
    }
}

