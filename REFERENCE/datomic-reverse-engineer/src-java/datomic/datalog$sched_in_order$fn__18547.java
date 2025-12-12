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

public final class datalog$sched_in_order$fn__18547
extends AFunction {
    Object cbinds;
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"set");

    public datalog$sched_in_order$fn__18547(Object object) {
        this.cbinds = object;
    }

    public Object invoke(Object p1__18453_SHARP_) {
        Object object = p1__18453_SHARP_;
        p1__18453_SHARP_ = null;
        datalog$sched_in_order$fn__18547 this_ = null;
        return ((IFn)const__0.getRawRoot()).invoke(((IFn)this_.cbinds).invoke(object));
    }
}

