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

public final class datalog$unifying_vars$fn__18438
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"set");
    public static final Var const__1 = RT.var((String)"datomic.datalog", (String)"unifying-vars");

    public Object invoke(Object p1__18430_SHARP_) {
        Object object = p1__18430_SHARP_;
        p1__18430_SHARP_ = null;
        datalog$unifying_vars$fn__18438 this_ = null;
        return ((IFn)const__0.getRawRoot()).invoke(((IFn)const__1.getRawRoot()).invoke(object));
    }
}

