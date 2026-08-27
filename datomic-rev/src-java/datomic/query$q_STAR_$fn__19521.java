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

public final class query$q_STAR_$fn__19521
extends AFunction {
    Object fv;
    public static final Var const__0 = RT.var((String)"datomic.query", (String)"xf-tuple");

    public query$q_STAR_$fn__19521(Object object) {
        this.fv = object;
    }

    public Object invoke(Object p1__19516_SHARP_) {
        Object object = p1__19516_SHARP_;
        p1__19516_SHARP_ = null;
        query$q_STAR_$fn__19521 this_ = null;
        return ((IFn)const__0.getRawRoot()).invoke(this_.fv, object);
    }
}

