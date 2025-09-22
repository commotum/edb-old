/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.Numbers
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.Numbers;

public final class integrity$tx_instant_ts$fn__22434
extends AFunction {
    public Object invoke(Object p1__22433_SHARP_) {
        Object object = p1__22433_SHARP_;
        p1__22433_SHARP_ = null;
        integrity$tx_instant_ts$fn__22434 this_ = null;
        return Numbers.lt((Object)object, (long)1000L) ? Boolean.TRUE : Boolean.FALSE;
    }
}

