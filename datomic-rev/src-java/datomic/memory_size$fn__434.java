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

public final class memory_size$fn__434
extends AFunction {
    public static Object invokeStatic(Object s) {
        Object object = s;
        s = null;
        return Numbers.num((long)Numbers.add((long)56L, (long)Numbers.multiply((long)2L, (long)((String)object).length())));
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return memory_size$fn__434.invokeStatic(object2);
    }
}

