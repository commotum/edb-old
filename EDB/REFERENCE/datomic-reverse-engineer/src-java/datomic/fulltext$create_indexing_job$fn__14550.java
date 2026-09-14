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

public final class fulltext$create_indexing_job$fn__14550
extends AFunction {
    Object delete_requests;
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"swap!");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"conj");

    public fulltext$create_indexing_job$fn__14550(Object object) {
        this.delete_requests = object;
    }

    public Object invoke(Object p1__14549_SHARP_) {
        Object object = p1__14549_SHARP_;
        p1__14549_SHARP_ = null;
        fulltext$create_indexing_job$fn__14550 this_ = null;
        return ((IFn)const__0.getRawRoot()).invoke(this_.delete_requests, const__1.getRawRoot(), object);
    }
}

