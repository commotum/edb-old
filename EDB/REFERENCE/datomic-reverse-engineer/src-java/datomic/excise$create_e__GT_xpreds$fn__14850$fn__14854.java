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

public final class excise$create_e__GT_xpreds$fn__14850$fn__14854
extends AFunction {
    Object epred;
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"update");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"conj");

    public excise$create_e__GT_xpreds$fn__14850$fn__14854(Object object) {
        this.epred = object;
    }

    public Object invoke(Object m, Object e) {
        Object object = m;
        m = null;
        Object object2 = e;
        e = null;
        excise$create_e__GT_xpreds$fn__14850$fn__14854 this_ = null;
        return ((IFn)const__0.getRawRoot()).invoke(object, object2, const__1.getRawRoot(), this_.epred);
    }
}

