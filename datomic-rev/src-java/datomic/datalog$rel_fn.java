/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 */
package datomic;

import clojure.lang.AFunction;
import datomic.datalog$rel_fn$fn__18885;

public final class datalog$rel_fn
extends AFunction {
    public static Object invokeStatic(Object f) {
        Object object = f;
        f = null;
        return new datalog$rel_fn$fn__18885(object);
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return datalog$rel_fn.invokeStatic(object2);
    }
}

