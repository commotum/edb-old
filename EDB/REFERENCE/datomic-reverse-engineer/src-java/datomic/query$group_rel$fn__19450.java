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

public final class query$group_rel$fn__19450
extends AFunction {
    Object fv;
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"subvec");
    public static final Object const__1 = 0L;

    public query$group_rel$fn__19450(Object object) {
        this.fv = object;
    }

    public Object invoke(Object p1__19449_SHARP_) {
        Object object = p1__19449_SHARP_;
        p1__19449_SHARP_ = null;
        query$group_rel$fn__19450 this_ = null;
        return ((IFn)const__0.getRawRoot()).invoke(object, const__1, (Object)RT.count((Object)this_.fv));
    }
}

