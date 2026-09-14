/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 */
package datomic;

import clojure.lang.AFunction;
import datomic.log$partition_by_weight$fn__16529;

public final class log$partition_by_weight
extends AFunction {
    public static Object invokeStatic(Object weigh, Object target2) {
        Object object = target2;
        target2 = null;
        Object object2 = weigh;
        weigh = null;
        return new log$partition_by_weight$fn__16529(object, object2);
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return log$partition_by_weight.invokeStatic(object3, object4);
    }
}

