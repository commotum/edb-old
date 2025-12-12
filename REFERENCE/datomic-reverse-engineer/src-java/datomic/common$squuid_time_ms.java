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
import java.util.UUID;

public final class common$squuid_time_ms
extends AFunction {
    public static Object invokeStatic(Object squuid2) {
        Object object = squuid2;
        squuid2 = null;
        return Numbers.num((long)Numbers.unchecked_multiply((long)1000L, (long)(0xFFFFFFFFL & ((UUID)object).getMostSignificantBits() >> (int)32L)));
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return common$squuid_time_ms.invokeStatic(object2);
    }
}

