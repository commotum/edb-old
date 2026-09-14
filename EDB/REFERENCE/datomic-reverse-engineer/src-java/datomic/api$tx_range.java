/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 */
package datomic;

import clojure.lang.AFunction;
import datomic.Log;

public final class api$tx_range
extends AFunction {
    public static Object invokeStatic(Object log2, Object start, Object end) {
        Object object = log2;
        log2 = null;
        Object object2 = start;
        start = null;
        Object object3 = end;
        end = null;
        return ((Log)object).txRange(object2, object3);
    }

    public Object invoke(Object object, Object object2, Object object3) {
        Object object4 = object;
        object = null;
        Object object5 = object2;
        object2 = null;
        Object object6 = object3;
        object3 = null;
        return api$tx_range.invokeStatic(object4, object5, object6);
    }
}

