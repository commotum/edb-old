/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 */
package datomic;

import clojure.lang.AFunction;

public final class codec$bytes__GT_string
extends AFunction {
    public static Object invokeStatic(Object b) {
        Object object = b;
        b = null;
        return new String((byte[])object, "UTF-8");
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return codec$bytes__GT_string.invokeStatic(object2);
    }
}

