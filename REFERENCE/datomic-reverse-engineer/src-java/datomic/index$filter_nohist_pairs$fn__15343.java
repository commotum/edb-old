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

public final class index$filter_nohist_pairs$fn__15343
extends AFunction {
    Object nd;
    Object d;
    Object db;
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"cons");
    public static final Var const__1 = RT.var((String)"datomic.index", (String)"filter-nohist-pairs");

    public index$filter_nohist_pairs$fn__15343(Object object, Object object2, Object object3) {
        this.nd = object;
        this.d = object2;
        this.db = object3;
    }

    public Object invoke() {
        index$filter_nohist_pairs$fn__15343 this_ = null;
        return ((IFn)const__0.getRawRoot()).invoke(this_.d, ((IFn)const__1.getRawRoot()).invoke(this_.db, this_.nd));
    }
}

