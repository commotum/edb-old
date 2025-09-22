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

public final class datalog$expr_clause$fn__18646
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"vector");

    public Object invoke(Object p1__18634_SHARP_) {
        Object object = p1__18634_SHARP_;
        p1__18634_SHARP_ = null;
        datalog$expr_clause$fn__18646 this_ = null;
        return ((IFn)const__0.getRawRoot()).invoke(((IFn)const__0.getRawRoot()).invoke(object));
    }
}

