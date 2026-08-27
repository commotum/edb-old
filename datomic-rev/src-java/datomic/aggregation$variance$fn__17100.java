/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.Numbers
 *  clojure.lang.RT
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.Numbers;
import clojure.lang.RT;

public final class aggregation$variance$fn__17100
extends AFunction {
    Object av;
    public static final Object const__1 = 2L;

    public aggregation$variance$fn__17100(Object object) {
        this.av = object;
    }

    public Object invoke(Object p1__17099_SHARP_) {
        Object object = p1__17099_SHARP_;
        p1__17099_SHARP_ = null;
        aggregation$variance$fn__17100 this_ = null;
        return Math.pow(RT.doubleCast((Object)Numbers.minus((Object)object, (Object)this_.av)), RT.doubleCast((Object)((Number)const__1)));
    }
}

