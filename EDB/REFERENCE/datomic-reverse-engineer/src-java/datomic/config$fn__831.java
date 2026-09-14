/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.Numbers
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.Numbers;

public final class config$fn__831
extends AFunction {
    public static Object invokeStatic(Object _) {
        return Numbers.gte((long)Runtime.getRuntime().availableProcessors(), (long)2L) ? Boolean.TRUE : Boolean.FALSE;
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return config$fn__831.invokeStatic(object2);
    }
}

