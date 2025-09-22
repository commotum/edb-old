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

public final class stats$db_attr_splits$fn__17941
extends AFunction {
    Object attr;
    Object db;
    public static final Var const__0 = RT.var((String)"datomic.stats", (String)"index-attr-splits");

    public stats$db_attr_splits$fn__17941(Object object, Object object2) {
        this.attr = object;
        this.db = object2;
    }

    public Object invoke(Object p1__17928_SHARP_) {
        Object object = p1__17928_SHARP_;
        p1__17928_SHARP_ = null;
        stats$db_attr_splits$fn__17941 this_ = null;
        return ((IFn)const__0.getRawRoot()).invoke(this_.db, object, this_.attr);
    }
}

