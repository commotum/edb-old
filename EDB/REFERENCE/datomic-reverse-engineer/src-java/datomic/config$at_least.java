/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 */
package datomic;

import clojure.lang.AFunction;
import datomic.config$at_least$fn__764;

public final class config$at_least
extends AFunction {
    public static Object invokeStatic(Object minval) {
        Object object = minval;
        minval = null;
        return new config$at_least$fn__764(object);
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return config$at_least.invokeStatic(object2);
    }
}

