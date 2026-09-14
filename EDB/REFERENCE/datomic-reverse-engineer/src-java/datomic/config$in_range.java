/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 */
package datomic;

import clojure.lang.AFunction;
import datomic.config$in_range$fn__768;

public final class config$in_range
extends AFunction {
    public static Object invokeStatic(Object lo, Object hi) {
        Object object = lo;
        lo = null;
        Object object2 = hi;
        hi = null;
        return new config$in_range$fn__768(object, object2);
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return config$in_range.invokeStatic(object3, object4);
    }
}

