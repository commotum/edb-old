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

public final class garbage$append_leaf$fn__19789
extends AFunction {
    Object root_entry;
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"conj");

    public garbage$append_leaf$fn__19789(Object object) {
        this.root_entry = object;
    }

    public Object invoke(Object p1__19781_SHARP_) {
        Object object = p1__19781_SHARP_;
        p1__19781_SHARP_ = null;
        garbage$append_leaf$fn__19789 this_ = null;
        return ((IFn)const__0.getRawRoot()).invoke(object, this_.root_entry);
    }
}

