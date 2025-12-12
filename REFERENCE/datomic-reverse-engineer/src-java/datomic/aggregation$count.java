/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 */
package datomic;

import clojure.lang.AFunction;
import java.util.Collection;

public final class aggregation$count
extends AFunction {
    public static Object invokeStatic(Object coll) {
        Object object = coll;
        coll = null;
        return ((Collection)object).size();
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return aggregation$count.invokeStatic(object2);
    }
}

