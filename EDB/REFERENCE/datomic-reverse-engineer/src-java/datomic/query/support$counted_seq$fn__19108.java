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

public final class support$counted_seq$fn__19108
extends AFunction {
    Object ct;
    Object base_seq;
    public static final Var const__0 = RT.var((String)"datomic.query.support", (String)"counted-seq");

    public support$counted_seq$fn__19108(Object object, Object object2) {
        this.ct = object;
        this.base_seq = object2;
    }

    public Object invoke(Object object, Object meta) {
        Object object2 = meta;
        meta = null;
        support$counted_seq$fn__19108 this_ = null;
        return ((IFn)const__0.getRawRoot()).invoke(this_.base_seq, this_.ct, object2);
    }
}

