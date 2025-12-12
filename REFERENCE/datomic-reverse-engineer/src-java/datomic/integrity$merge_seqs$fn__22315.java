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

public final class integrity$merge_seqs$fn__22315
extends AFunction {
    Object s1;
    Object o2;
    Object cmp;
    Object m2;
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"cons");
    public static final Var const__1 = RT.var((String)"datomic.integrity", (String)"merge-seqs");

    public integrity$merge_seqs$fn__22315(Object object, Object object2, Object object3, Object object4) {
        this.s1 = object;
        this.o2 = object2;
        this.cmp = object3;
        this.m2 = object4;
    }

    public Object invoke() {
        integrity$merge_seqs$fn__22315 this_ = null;
        return ((IFn)const__0.getRawRoot()).invoke(this_.o2, ((IFn)const__1.getRawRoot()).invoke(this_.cmp, this_.s1, this_.m2));
    }
}

