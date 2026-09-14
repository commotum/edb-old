/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 */
package datomic;

import clojure.lang.AFunction;
import java.lang.ref.ReferenceQueue;

public final class queue$fn__12139
extends AFunction {
    public static Object invokeStatic(Object q2) {
        Object object = q2;
        q2 = null;
        return ((ReferenceQueue)object).remove();
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return queue$fn__12139.invokeStatic(object2);
    }
}

