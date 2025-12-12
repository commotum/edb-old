/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.Numbers
 *  clojure.lang.RT
 */
package datomic.cache;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Numbers;
import clojure.lang.RT;

public final class caffeine$create_scaled_weight_limited$scaled_f__619
extends AFunction {
    Object divisor;
    Object f;

    public caffeine$create_scaled_weight_limited$scaled_f__619(Object object, Object object2) {
        this.divisor = object;
        this.f = object2;
    }

    public Object invoke(Object k, Object v) {
        Object object = k;
        k = null;
        Object object2 = v;
        v = null;
        caffeine$create_scaled_weight_limited$scaled_f__619 this_ = null;
        return Numbers.num((long)RT.longCast((double)Math.ceil(RT.doubleCast((Object)Numbers.divide((Object)((IFn)this_.f).invoke(object, object2), (Object)this_.divisor)))));
    }
}

