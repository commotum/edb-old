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

public final class db$filter_assess_tx_datoms$fn__13351
extends AFunction {
    Object p0;
    Object p5;
    Object p1;
    Object p4;
    Object p2;
    Object p3;

    public db$filter_assess_tx_datoms$fn__13351(Object object, Object object2, Object object3, Object object4, Object object5, Object object6) {
        this.p0 = object;
        this.p5 = object2;
        this.p1 = object3;
        this.p4 = object4;
        this.p2 = object5;
        this.p3 = object6;
    }

    public Object invoke(Object p1__13350_SHARP_) {
        Object object;
        Object and__5236__auto__13357;
        Object object2 = and__5236__auto__13357 = ((IFn)this_.p0).invoke(p1__13350_SHARP_);
        if (object2 != null && object2 != Boolean.FALSE) {
            Object and__5236__auto__13356;
            Object object3 = and__5236__auto__13356 = ((IFn)this_.p1).invoke(p1__13350_SHARP_);
            if (object3 != null && object3 != Boolean.FALSE) {
                Object and__5236__auto__13355;
                Object object4 = and__5236__auto__13355 = ((IFn)this_.p2).invoke(p1__13350_SHARP_);
                if (object4 != null && object4 != Boolean.FALSE) {
                    Object and__5236__auto__13354;
                    Object object5 = and__5236__auto__13354 = ((IFn)this_.p3).invoke(p1__13350_SHARP_);
                    if (object5 != null && object5 != Boolean.FALSE) {
                        Object and__5236__auto__13353;
                        Object object6 = and__5236__auto__13353 = ((IFn)this_.p4).invoke(p1__13350_SHARP_);
                        if (object6 != null && object6 != Boolean.FALSE) {
                            Object object7 = p1__13350_SHARP_;
                            p1__13350_SHARP_ = null;
                            db$filter_assess_tx_datoms$fn__13351 this_ = null;
                            object = ((IFn)this_.p5).invoke(object7);
                        } else {
                            object = and__5236__auto__13353;
                            and__5236__auto__13353 = null;
                        }
                    } else {
                        object = and__5236__auto__13354;
                        and__5236__auto__13354 = null;
                    }
                } else {
                    object = and__5236__auto__13355;
                    and__5236__auto__13355 = null;
                }
            } else {
                object = and__5236__auto__13356;
                Object var3_3 = null;
            }
        } else {
            object = and__5236__auto__13357;
            Object var2_2 = null;
        }
        return object;
    }
}

