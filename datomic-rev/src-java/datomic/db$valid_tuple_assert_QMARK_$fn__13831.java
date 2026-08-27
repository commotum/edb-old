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

public final class db$valid_tuple_assert_QMARK_$fn__13831
extends AFunction {
    Object valid_QMARK_;
    Object kw;

    public db$valid_tuple_assert_QMARK_$fn__13831(Object object, Object object2) {
        this.valid_QMARK_ = object;
        this.kw = object2;
    }

    public Object invoke(Object p1__13822_SHARP_) {
        Object object = p1__13822_SHARP_;
        p1__13822_SHARP_ = null;
        db$valid_tuple_assert_QMARK_$fn__13831 this_ = null;
        return ((IFn)this_.valid_QMARK_).invoke(this_.kw, object);
    }
}

