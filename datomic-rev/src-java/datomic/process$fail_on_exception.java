/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 */
package datomic;

import clojure.lang.AFunction;
import datomic.process$fail_on_exception$fn__14991;

public final class process$fail_on_exception
extends AFunction {
    public static Object invokeStatic(Object f) {
        Object object = f;
        f = null;
        return new process$fail_on_exception$fn__14991(object);
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return process$fail_on_exception.invokeStatic(object2);
    }
}

