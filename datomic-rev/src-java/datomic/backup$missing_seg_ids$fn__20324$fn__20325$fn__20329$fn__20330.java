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

public final class backup$missing_seg_ids$fn__20324$fn__20325$fn__20329$fn__20330
extends AFunction {
    Object seg_id_set;
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"contains?");

    public backup$missing_seg_ids$fn__20324$fn__20325$fn__20329$fn__20330(Object object) {
        this.seg_id_set = object;
    }

    public Object invoke(Object p1__20318_SHARP_) {
        Object object = p1__20318_SHARP_;
        p1__20318_SHARP_ = null;
        backup$missing_seg_ids$fn__20324$fn__20325$fn__20329$fn__20330 this_ = null;
        return ((IFn)const__0.getRawRoot()).invoke(this_.seg_id_set, object);
    }
}

