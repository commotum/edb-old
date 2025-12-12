/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 */
package datomic;

import clojure.lang.AFunction;

public final class io$context_resource
extends AFunction {
    public static Object invokeStatic(Object r) {
        ClassLoader cl2;
        ClassLoader classLoader = cl2 = Thread.currentThread().getContextClassLoader();
        cl2 = null;
        Object object = r;
        r = null;
        return classLoader.getResource((String)object);
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return io$context_resource.invokeStatic(object2);
    }
}

