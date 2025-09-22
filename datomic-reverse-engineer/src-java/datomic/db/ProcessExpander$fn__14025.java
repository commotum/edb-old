/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic.db;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.RT;
import clojure.lang.Var;

public final class ProcessExpander$fn__14025
extends AFunction {
    Object db;
    public static final Var const__0 = RT.var((String)"datomic.db", (String)"datom-tuple-attrs");

    public ProcessExpander$fn__14025(Object object) {
        this.db = object;
    }

    public Object invoke(Object p1__13999_SHARP_) {
        Object object = p1__13999_SHARP_;
        p1__13999_SHARP_ = null;
        ProcessExpander$fn__14025 this_ = null;
        return ((IFn)const__0.getRawRoot()).invoke(this_.db, object);
    }
}

