/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.Numbers
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic.cache;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Numbers;
import clojure.lang.RT;
import clojure.lang.Var;
import datomic.cache.caffeine$create_scaled_weight_limited$scaled_f__619;

public final class caffeine$create_scaled_weight_limited
extends AFunction {
    public static final Var const__2 = RT.var((String)"datomic.cache.caffeine", (String)"create-weight-limited");

    public static Object invokeStatic(Object weight, Object f, Object divisor) {
        caffeine$create_scaled_weight_limited$scaled_f__619 scaled_f;
        Object object = weight;
        weight = null;
        long scaled_weight = RT.longCast((double)Math.ceil(RT.doubleCast((Object)Numbers.divide((Object)object, (Object)divisor))));
        Object object2 = divisor;
        divisor = null;
        Object object3 = f;
        f = null;
        caffeine$create_scaled_weight_limited$scaled_f__619 caffeine$create_scaled_weight_limited$scaled_f__619 = scaled_f = new caffeine$create_scaled_weight_limited$scaled_f__619(object2, object3);
        scaled_f = null;
        return ((IFn)const__2.getRawRoot()).invoke((Object)Numbers.num((long)scaled_weight), (Object)caffeine$create_scaled_weight_limited$scaled_f__619);
    }

    public Object invoke(Object object, Object object2, Object object3) {
        Object object4 = object;
        object = null;
        Object object5 = object2;
        object2 = null;
        Object object6 = object3;
        object3 = null;
        return caffeine$create_scaled_weight_limited.invokeStatic(object4, object5, object6);
    }
}

