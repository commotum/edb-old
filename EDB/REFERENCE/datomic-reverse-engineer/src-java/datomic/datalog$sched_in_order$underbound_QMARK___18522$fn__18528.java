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

public final class datalog$sched_in_order$underbound_QMARK___18522$fn__18528
extends AFunction {
    Object bindings;
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"contains?");

    public datalog$sched_in_order$underbound_QMARK___18522$fn__18528(Object object) {
        this.bindings = object;
    }

    public Object invoke(Object p1__18451_SHARP_) {
        Object object = p1__18451_SHARP_;
        p1__18451_SHARP_ = null;
        datalog$sched_in_order$underbound_QMARK___18522$fn__18528 this_ = null;
        return ((IFn)const__0.getRawRoot()).invoke(this_.bindings, object);
    }
}

