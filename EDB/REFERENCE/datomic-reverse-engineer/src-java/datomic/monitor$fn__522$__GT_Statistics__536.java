/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 */
package datomic;

import clojure.lang.AFunction;
import datomic.monitor.Statistics;

public final class monitor$fn__522$__GT_Statistics__536
extends AFunction {
    public Object invoke(Object lo, Object hi, Object sum2, Object count2) {
        Object object = lo;
        lo = null;
        Object object2 = hi;
        hi = null;
        Object object3 = sum2;
        sum2 = null;
        Object object4 = count2;
        count2 = null;
        return new Statistics(object, object2, object3, object4);
    }
}

