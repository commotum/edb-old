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
import datomic.backup$missing_seg_ids$fn__20324$fn__20325;

public final class backup$missing_seg_ids$fn__20324
extends AFunction {
    Object seg_id_set;
    Object value_substorage;
    Object fill;
    public static final Var const__0 = RT.var((String)"datomic.common", (String)"pfuture");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"deref");
    public static final Var const__2 = RT.var((String)"datomic.backup", (String)"thread-pool");

    public backup$missing_seg_ids$fn__20324(Object object, Object object2, Object object3) {
        this.seg_id_set = object;
        this.value_substorage = object2;
        this.fill = object3;
    }

    public Object invoke(Object prefix) {
        Object object = prefix;
        prefix = null;
        backup$missing_seg_ids$fn__20324 this_ = null;
        return ((IFn)const__0.getRawRoot()).invoke(((IFn)const__1.getRawRoot()).invoke(const__2.getRawRoot()), (Object)new backup$missing_seg_ids$fn__20324$fn__20325(this_.seg_id_set, this_.value_substorage, this_.fill, object));
    }
}

