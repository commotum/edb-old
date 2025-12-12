/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 */
package datomic;

import clojure.lang.AFunction;
import datomic.index$make_sparse_lt$fn__15384;

public final class index$make_sparse_lt
extends AFunction {
    public static Object invokeStatic(Object cmp) {
        Object object = cmp;
        cmp = null;
        return new index$make_sparse_lt$fn__15384(object);
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return index$make_sparse_lt.invokeStatic(object2);
    }
}

