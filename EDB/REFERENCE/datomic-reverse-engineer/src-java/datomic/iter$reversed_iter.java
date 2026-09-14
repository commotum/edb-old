/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 */
package datomic;

import clojure.lang.AFunction;
import datomic.iter.ReversedIter;

public final class iter$reversed_iter
extends AFunction {
    public static Object invokeStatic(Object iter2) {
        ReversedIter reversedIter;
        Object object = iter2;
        if (object != null && object != Boolean.FALSE) {
            iter2 = null;
            reversedIter = new ReversedIter(iter2);
        } else {
            reversedIter = null;
        }
        return reversedIter;
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return iter$reversed_iter.invokeStatic(object2);
    }
}

