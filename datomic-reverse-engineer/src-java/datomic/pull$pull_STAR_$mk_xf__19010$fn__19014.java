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

public final class pull$pull_STAR_$mk_xf__19010$fn__19014
extends AFunction {
    Object db;
    Object _recursed;
    Object _subspec;
    Object prefer_aevt_QMARK_;
    public static final Var const__0 = RT.var((String)"datomic.pull", (String)"pull*");

    public pull$pull_STAR_$mk_xf__19010$fn__19014(Object object, Object object2, Object object3, Object object4) {
        this.db = object;
        this._recursed = object2;
        this._subspec = object3;
        this.prefer_aevt_QMARK_ = object4;
    }

    public Object invoke(Object e) {
        Object object = e;
        e = null;
        pull$pull_STAR_$mk_xf__19010$fn__19014 this_ = null;
        return ((IFn)const__0.getRawRoot()).invoke(this_.db, this_._subspec, this_._recursed, this_.prefer_aevt_QMARK_, object);
    }
}

