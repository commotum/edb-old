/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic.core2;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.RT;
import clojure.lang.Var;

public final class atom$swap_BANG_$fn__19671
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.core2.anomalies", (String)"anom");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"second");

    public Object invoke(Object p1__19670_SHARP_) {
        Object object;
        Object or__5581__auto__19673;
        Object object2 = or__5581__auto__19673 = ((IFn)const__0.getRawRoot()).invoke(p1__19670_SHARP_);
        if (object2 != null && object2 != Boolean.FALSE) {
            object = or__5581__auto__19673;
            or__5581__auto__19673 = null;
        } else {
            Object object3 = p1__19670_SHARP_;
            p1__19670_SHARP_ = null;
            atom$swap_BANG_$fn__19671 this_ = null;
            object = ((IFn)const__1.getRawRoot()).invoke(object3);
        }
        return object;
    }
}

