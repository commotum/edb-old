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

public final class index$merge_db_STAR_$fn__15858
extends AFunction {
    Object db;
    public static final Var const__0 = RT.var((String)"datomic.db", (String)"resolve-id");

    public index$merge_db_STAR_$fn__15858(Object object) {
        this.db = object;
    }

    public Object invoke(Object p1__15740_SHARP_) {
        Object object = p1__15740_SHARP_;
        p1__15740_SHARP_ = null;
        index$merge_db_STAR_$fn__15858 this_ = null;
        return ((IFn)const__0.getRawRoot()).invoke(this_.db, object);
    }
}

