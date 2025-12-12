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

public final class cassandra_v4$update_stmt_STAR_$fn__10105
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"str");

    public Object invoke(Object p1__10104_SHARP_) {
        Object object = p1__10104_SHARP_;
        p1__10104_SHARP_ = null;
        cassandra_v4$update_stmt_STAR_$fn__10105 this_ = null;
        return ((IFn)const__0.getRawRoot()).invoke(object, (Object)" = ?");
    }
}

