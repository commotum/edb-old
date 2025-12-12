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

public final class tools$pace_msec_per_seg
extends AFunction {
    public static Object invokeStatic(Object provisioned_kbs) {
        Object object = provisioned_kbs;
        provisioned_kbs = null;
        Number use_kbs = Numbers.divide((Object)object, (long)2L);
        double item_per_kb = 0.03;
        double msec_per_sec = 1000.0;
        long latency_msec = 25L;
        Number number = use_kbs;
        use_kbs = null;
        double pace_msec = Numbers.minus((double)(Numbers.divide((double)msec_per_sec, (Object)number) / item_per_kb), (long)latency_msec);
        return pace_msec > 0.0 ? (Number)Numbers.num((long)RT.longCast((double)pace_msec)) : (Number)null;
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return tools$pace_msec_per_seg.invokeStatic(object2);
    }
}

