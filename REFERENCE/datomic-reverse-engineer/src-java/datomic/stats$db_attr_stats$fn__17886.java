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

public final class stats$db_attr_stats$fn__17886
extends AFunction {
    Object db;
    public static final Var const__0 = RT.var((String)"datomic.stats", (String)"index-attr-stats");

    public stats$db_attr_stats$fn__17886(Object object) {
        this.db = object;
    }

    public Object invoke(Object p1__17885_SHARP_) {
        Object object = p1__17885_SHARP_;
        p1__17885_SHARP_ = null;
        stats$db_attr_stats$fn__17886 this_ = null;
        return ((IFn)const__0.getRawRoot()).invoke(this_.db, object);
    }
}

