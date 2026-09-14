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

public final class datalog$sched_in_order$delay_ins__18500$fn__18504$fn__18505
extends AFunction {
    Object bset;
    Object cargs;
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"some");

    public datalog$sched_in_order$delay_ins__18500$fn__18504$fn__18505(Object object, Object object2) {
        this.bset = object;
        this.cargs = object2;
    }

    public Object invoke(Object p1__18447_SHARP_) {
        Object object = p1__18447_SHARP_;
        p1__18447_SHARP_ = null;
        datalog$sched_in_order$delay_ins__18500$fn__18504$fn__18505 this_ = null;
        return ((IFn)const__0.getRawRoot()).invoke(this_.bset, ((IFn)this_.cargs).invoke(object));
    }
}

