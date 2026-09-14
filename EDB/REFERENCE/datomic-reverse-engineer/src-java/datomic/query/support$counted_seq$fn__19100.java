/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic.query;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.RT;
import clojure.lang.Var;

public final class support$counted_seq$fn__19100
extends AFunction {
    Object base_seq;
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"cons");

    public support$counted_seq$fn__19100(Object object) {
        this.base_seq = object;
    }

    public Object invoke(Object object, Object o) {
        Object object2 = o;
        o = null;
        support$counted_seq$fn__19100 this_ = null;
        return ((IFn)const__0.getRawRoot()).invoke(object2, this_.base_seq);
    }
}

