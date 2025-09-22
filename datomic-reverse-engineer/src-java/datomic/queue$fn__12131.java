/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 */
package datomic;

import clojure.lang.AFunction;
import java.util.concurrent.BlockingQueue;

public final class queue$fn__12131
extends AFunction {
    public static Object invokeStatic(Object q2) {
        Object object = q2;
        q2 = null;
        return ((BlockingQueue)object).take();
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return queue$fn__12131.invokeStatic(object2);
    }
}

