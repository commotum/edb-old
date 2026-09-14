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

public final class qtune$partial_query$fn__23429
extends AFunction {
    Object bindings;
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"every?");
    public static final Var const__1 = RT.var((String)"datomic.qtune", (String)"cvars");

    public qtune$partial_query$fn__23429(Object object) {
        this.bindings = object;
    }

    public Object invoke(Object p1__23426_SHARP_) {
        Object object = p1__23426_SHARP_;
        p1__23426_SHARP_ = null;
        qtune$partial_query$fn__23429 this_ = null;
        return ((IFn)const__0.getRawRoot()).invoke(this_.bindings, ((IFn)const__1.getRawRoot()).invoke(object));
    }
}

