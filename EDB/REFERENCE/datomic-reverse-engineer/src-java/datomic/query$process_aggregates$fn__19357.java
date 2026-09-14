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

public final class query$process_aggregates$fn__19357
extends AFunction {
    Object list_QMARK_;
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"last");

    public query$process_aggregates$fn__19357(Object object) {
        this.list_QMARK_ = object;
    }

    public Object invoke(Object p1__19354_SHARP_) {
        Object object;
        Object object2 = ((IFn)this_.list_QMARK_).invoke(p1__19354_SHARP_);
        if (object2 != null && object2 != Boolean.FALSE) {
            Object object3 = p1__19354_SHARP_;
            p1__19354_SHARP_ = null;
            query$process_aggregates$fn__19357 this_ = null;
            object = ((IFn)const__0.getRawRoot()).invoke(object3);
        } else {
            object = p1__19354_SHARP_;
            Object var1_1 = null;
        }
        return object;
    }
}

