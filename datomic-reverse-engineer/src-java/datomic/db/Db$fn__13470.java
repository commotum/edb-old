/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 */
package datomic.db;

import clojure.lang.AFunction;
import clojure.lang.IFn;

public final class Db$fn__13470
extends AFunction {
    Object pred;
    Object filt;

    public Db$fn__13470(Object object, Object object2) {
        this.pred = object;
        this.filt = object2;
    }

    public Object invoke(Object p1__13417_SHARP_, Object p2__13418_SHARP_) {
        Object object;
        Object and__5236__auto__13472;
        Object object2 = and__5236__auto__13472 = ((IFn)this_.filt).invoke(p1__13417_SHARP_, p2__13418_SHARP_);
        if (object2 != null && object2 != Boolean.FALSE) {
            Object object3 = p1__13417_SHARP_;
            p1__13417_SHARP_ = null;
            Object object4 = p2__13418_SHARP_;
            p2__13418_SHARP_ = null;
            Db$fn__13470 this_ = null;
            object = ((IFn)this_.pred).invoke(object3, object4);
        } else {
            object = and__5236__auto__13472;
            Object var3_3 = null;
        }
        return object;
    }
}

