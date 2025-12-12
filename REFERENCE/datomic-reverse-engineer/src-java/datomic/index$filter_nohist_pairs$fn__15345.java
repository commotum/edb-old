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

public final class index$filter_nohist_pairs$fn__15345
extends AFunction {
    Object d;
    Object data;
    Object db;
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"cons");
    public static final Var const__1 = RT.var((String)"datomic.index", (String)"filter-nohist-pairs");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"next");

    public index$filter_nohist_pairs$fn__15345(Object object, Object object2, Object object3) {
        this.d = object;
        this.data = object2;
        this.db = object3;
    }

    public Object invoke() {
        index$filter_nohist_pairs$fn__15345 this_ = null;
        return ((IFn)const__0.getRawRoot()).invoke(this_.d, ((IFn)const__1.getRawRoot()).invoke(this_.db, ((IFn)const__2.getRawRoot()).invoke(this_.data)));
    }
}

