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

public final class extension_resolver$wildcard_pred$fn__14307
extends AFunction {
    Object nses;
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"contains?");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"namespace");

    public extension_resolver$wildcard_pred$fn__14307(Object object) {
        this.nses = object;
    }

    public Object invoke(Object p1__14306_SHARP_) {
        Object object = p1__14306_SHARP_;
        p1__14306_SHARP_ = null;
        extension_resolver$wildcard_pred$fn__14307 this_ = null;
        return ((IFn)const__0.getRawRoot()).invoke(this_.nses, ((IFn)const__1.getRawRoot()).invoke(object));
    }
}

