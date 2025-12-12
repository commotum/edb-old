/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;

public final class db$has_unique_id_QMARK_$fn__13696
extends AFunction {
    Object unique_id_QMARK_;

    public db$has_unique_id_QMARK_$fn__13696(Object object) {
        this.unique_id_QMARK_ = object;
    }

    public Object invoke(Object p1__13693_SHARP_) {
        Object object = p1__13693_SHARP_;
        p1__13693_SHARP_ = null;
        db$has_unique_id_QMARK_$fn__13696 this_ = null;
        return ((IFn)this_.unique_id_QMARK_).invoke(object);
    }
}

