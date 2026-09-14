/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 */
package datomic;

import clojure.lang.AFunction;
import datomic.iter.Iterator;

public final class iter$iterator
extends AFunction {
    public static Object invokeStatic(Object iter2) {
        Object object = iter2;
        iter2 = null;
        return new Iterator(object);
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return iter$iterator.invokeStatic(object2);
    }
}

