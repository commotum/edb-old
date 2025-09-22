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

public final class db$add_ensured_data$fn__14094
extends AFunction {
    Object db_before;
    public static final Var const__0 = RT.var((String)"datomic.db", (String)"ensure-datom?");

    public db$add_ensured_data$fn__14094(Object object) {
        this.db_before = object;
    }

    public Object invoke(Object p1__14093_SHARP_) {
        Object object = p1__14093_SHARP_;
        p1__14093_SHARP_ = null;
        db$add_ensured_data$fn__14094 this_ = null;
        return ((IFn)const__0.getRawRoot()).invoke(this_.db_before, object);
    }
}

