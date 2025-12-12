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

public final class db$valid_tuple_assert_QMARK_$fn__13833
extends AFunction {
    Object valid_QMARK_;

    public db$valid_tuple_assert_QMARK_$fn__13833(Object object) {
        this.valid_QMARK_ = object;
    }

    public Object invoke(Object p1__13823_SHARP_, Object p2__13824_SHARP_) {
        Object object = p1__13823_SHARP_;
        p1__13823_SHARP_ = null;
        Object object2 = p2__13824_SHARP_;
        p2__13824_SHARP_ = null;
        db$valid_tuple_assert_QMARK_$fn__13833 this_ = null;
        return ((IFn)this_.valid_QMARK_).invoke(object, object2);
    }
}

