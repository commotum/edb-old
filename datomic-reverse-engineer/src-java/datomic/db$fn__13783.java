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
import java.math.BigDecimal;

public final class db$fn__13783
extends AFunction {
    public static Object invokeStatic(Object v) {
        Object object = v;
        v = null;
        return Numbers.lte((long)((BigDecimal)object).precision(), (long)256L) ? Boolean.TRUE : Boolean.FALSE;
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return db$fn__13783.invokeStatic(object2);
    }
}

