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
import datomic.backup$unreadable_seg_ids$fn__20349$fn__20350;

public final class backup$unreadable_seg_ids$fn__20349
extends AFunction {
    Object unreadable_QMARK_;
    public static final Var const__0 = RT.var((String)"datomic.common", (String)"pfuture");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"deref");
    public static final Var const__2 = RT.var((String)"datomic.backup", (String)"thread-pool");

    public backup$unreadable_seg_ids$fn__20349(Object object) {
        this.unreadable_QMARK_ = object;
    }

    public Object invoke(Object k) {
        Object object = k;
        k = null;
        backup$unreadable_seg_ids$fn__20349 this_ = null;
        return ((IFn)const__0.getRawRoot()).invoke(((IFn)const__1.getRawRoot()).invoke(const__2.getRawRoot()), (Object)new backup$unreadable_seg_ids$fn__20349$fn__20350(this_.unreadable_QMARK_, object));
    }
}

