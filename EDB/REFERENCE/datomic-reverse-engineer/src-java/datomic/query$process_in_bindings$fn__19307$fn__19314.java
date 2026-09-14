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

public final class query$process_in_bindings$fn__19307$fn__19314
extends AFunction {
    Object gs;
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"vector");

    public query$process_in_bindings$fn__19307$fn__19314(Object object) {
        this.gs = object;
    }

    public Object invoke(Object p1__19302_SHARP_) {
        Object object = p1__19302_SHARP_;
        p1__19302_SHARP_ = null;
        query$process_in_bindings$fn__19307$fn__19314 this_ = null;
        return ((IFn)const__0.getRawRoot()).invoke(this_.gs, object);
    }
}

