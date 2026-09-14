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

public final class db$fn__13775
extends AFunction {
    public static Object invokeStatic(Object s) {
        Object object = s;
        s = null;
        return Numbers.lte((long)((String)object).length(), (long)256L) ? Boolean.TRUE : Boolean.FALSE;
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return db$fn__13775.invokeStatic(object2);
    }
}

