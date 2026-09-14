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

public final class integrity$merge_seqs$fn__22313
extends AFunction {
    Object o1;
    Object m1;
    Object cmp;
    Object s2;
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"cons");
    public static final Var const__1 = RT.var((String)"datomic.integrity", (String)"merge-seqs");

    public integrity$merge_seqs$fn__22313(Object object, Object object2, Object object3, Object object4) {
        this.o1 = object;
        this.m1 = object2;
        this.cmp = object3;
        this.s2 = object4;
    }

    public Object invoke() {
        integrity$merge_seqs$fn__22313 this_ = null;
        return ((IFn)const__0.getRawRoot()).invoke(this_.o1, ((IFn)const__1.getRawRoot()).invoke(this_.cmp, this_.m1, this_.s2));
    }
}

