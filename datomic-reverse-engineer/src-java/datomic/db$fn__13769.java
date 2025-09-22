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
import java.math.BigInteger;

public final class db$fn__13769
extends AFunction {
    public static Object invokeStatic(Object v) {
        Object object = v;
        v = null;
        return Numbers.lte((long)((BigInteger)object).bitLength(), (long)256L) ? Boolean.TRUE : Boolean.FALSE;
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return db$fn__13769.invokeStatic(object2);
    }
}

