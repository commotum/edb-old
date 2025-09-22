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

public final class db$missing_attrs$fn__14072
extends AFunction {
    Object entity;
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"find");

    public db$missing_attrs$fn__14072(Object object) {
        this.entity = object;
    }

    public Object invoke(Object p1__14071_SHARP_) {
        Object object = p1__14071_SHARP_;
        p1__14071_SHARP_ = null;
        db$missing_attrs$fn__14072 this_ = null;
        return ((IFn)const__0.getRawRoot()).invoke(this_.entity, object);
    }
}

