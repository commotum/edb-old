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

public final class slf4j$fn__9005$fn__9006
extends AFunction {
    Object redact_QMARK_;
    public static final Var const__0 = RT.var((String)"datomic.slf4j", (String)"redact");

    public slf4j$fn__9005$fn__9006(Object object) {
        this.redact_QMARK_ = object;
    }

    public Object invoke(Object p1__9004_SHARP_) {
        Object object = p1__9004_SHARP_;
        p1__9004_SHARP_ = null;
        slf4j$fn__9005$fn__9006 this_ = null;
        return ((IFn)const__0.getRawRoot()).invoke(object, this_.redact_QMARK_);
    }
}

