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

public final class extension_resolver$explicit_pred$fn__14303
extends AFunction {
    Object syms;
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"contains?");

    public extension_resolver$explicit_pred$fn__14303(Object object) {
        this.syms = object;
    }

    public Object invoke(Object p1__14302_SHARP_) {
        Object object = p1__14302_SHARP_;
        p1__14302_SHARP_ = null;
        extension_resolver$explicit_pred$fn__14303 this_ = null;
        return ((IFn)const__0.getRawRoot()).invoke(this_.syms, object);
    }
}

