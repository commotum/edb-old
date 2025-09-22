/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic.core2;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.RT;
import clojure.lang.Var;

public final class atom$swap_BANG_$fn__19674
extends AFunction {
    Object args;
    Object f;
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"apply");

    public atom$swap_BANG_$fn__19674(Object object, Object object2) {
        this.args = object;
        this.f = object2;
    }

    public Object invoke(Object v) {
        Object object = v;
        v = null;
        atom$swap_BANG_$fn__19674 this_ = null;
        return ((IFn)const__0.getRawRoot()).invoke(this_.f, object, this_.args);
    }
}

