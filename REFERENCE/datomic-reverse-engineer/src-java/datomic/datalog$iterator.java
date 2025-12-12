/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 */
package datomic;

import clojure.lang.AFunction;

public final class datalog$iterator
extends AFunction {
    public static Object invokeStatic(Object xs) {
        Object object = xs;
        xs = null;
        return ((Iterable)object).iterator();
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return datalog$iterator.invokeStatic(object2);
    }
}

