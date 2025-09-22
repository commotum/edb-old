/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 */
package datomic;

import clojure.lang.AFunction;
import java.util.Collection;

public final class queue$fn__12120
extends AFunction {
    public static Object invokeStatic(Object q2) {
        ((Collection)q2).clear();
        Object object = null;
        return q2;
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return queue$fn__12120.invokeStatic(object2);
    }
}

