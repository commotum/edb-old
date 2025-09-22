/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 */
package datomic;

import clojure.lang.AFunction;
import datomic.datalog$rel_pred$fn__18888;

public final class datalog$rel_pred
extends AFunction {
    public static Object invokeStatic(Object f) {
        Object object = f;
        f = null;
        return new datalog$rel_pred$fn__18888(object);
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return datalog$rel_pred.invokeStatic(object2);
    }
}

