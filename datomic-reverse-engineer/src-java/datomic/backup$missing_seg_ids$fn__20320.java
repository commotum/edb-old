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
import datomic.backup$missing_seg_ids$fn__20320$fn__20321;

public final class backup$missing_seg_ids$fn__20320
extends AFunction {
    Object drain;
    Object seg_id_set;
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"reduce");

    public backup$missing_seg_ids$fn__20320(Object object, Object object2) {
        this.drain = object;
        this.seg_id_set = object2;
    }

    public Object invoke() {
        this_.seg_id_set = null;
        this_.drain = null;
        backup$missing_seg_ids$fn__20320 this_ = null;
        return ((IFn)const__0.getRawRoot()).invoke((Object)new backup$missing_seg_ids$fn__20320$fn__20321(), this_.seg_id_set, ((IFn)this_.drain).invoke());
    }
}

