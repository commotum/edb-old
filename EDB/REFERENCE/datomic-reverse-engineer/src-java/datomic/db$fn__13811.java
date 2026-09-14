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

public final class db$fn__13811
extends AFunction {
    public static Object invokeStatic(Object v) {
        Object object = v;
        v = null;
        return Numbers.num((long)((Short)object).longValue());
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return db$fn__13811.invokeStatic(object2);
    }
}

