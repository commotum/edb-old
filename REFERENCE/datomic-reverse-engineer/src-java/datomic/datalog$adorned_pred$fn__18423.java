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

public final class datalog$adorned_pred$fn__18423
extends AFunction {
    Object bindset;
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"not");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"contains?");

    public datalog$adorned_pred$fn__18423(Object object) {
        this.bindset = object;
    }

    public Object invoke(Object p1__18422_SHARP_) {
        Object object = p1__18422_SHARP_;
        p1__18422_SHARP_ = null;
        datalog$adorned_pred$fn__18423 this_ = null;
        return ((IFn)const__0.getRawRoot()).invoke(((IFn)const__1.getRawRoot()).invoke(this_.bindset, object));
    }
}

