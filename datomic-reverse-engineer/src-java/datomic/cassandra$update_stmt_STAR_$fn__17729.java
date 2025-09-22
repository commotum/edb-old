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

public final class cassandra$update_stmt_STAR_$fn__17729
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"str");

    public Object invoke(Object p1__17728_SHARP_) {
        Object object = p1__17728_SHARP_;
        p1__17728_SHARP_ = null;
        cassandra$update_stmt_STAR_$fn__17729 this_ = null;
        return ((IFn)const__0.getRawRoot()).invoke(object, (Object)" = ?");
    }
}

