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

public final class datalog$sched_in_order$underbound_QMARK___18522$fn__18523
extends AFunction {
    Object bindings;
    public static final Var const__0 = RT.var((String)"datomic.datalog", (String)"variable?");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"not");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"contains?");

    public datalog$sched_in_order$underbound_QMARK___18522$fn__18523(Object object) {
        this.bindings = object;
    }

    public Object invoke(Object p1__18449_SHARP_) {
        Object object;
        Object and__5236__auto__18525;
        Object object2 = and__5236__auto__18525 = ((IFn)const__0.getRawRoot()).invoke(p1__18449_SHARP_);
        if (object2 != null && object2 != Boolean.FALSE) {
            Object object3 = p1__18449_SHARP_;
            p1__18449_SHARP_ = null;
            datalog$sched_in_order$underbound_QMARK___18522$fn__18523 this_ = null;
            object = ((IFn)const__1.getRawRoot()).invoke(((IFn)const__2.getRawRoot()).invoke(this_.bindings, object3));
        } else {
            object = and__5236__auto__18525;
            Object var2_2 = null;
        }
        return object;
    }
}

