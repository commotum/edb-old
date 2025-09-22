/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 */
package datomic.cache;

import clojure.lang.AFunction;
import java.util.Map;

public final class impl$fn__376
extends AFunction {
    public static Object invokeStatic(Object c) {
        Object object = c;
        c = null;
        ((Map)object).clear();
        return null;
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return impl$fn__376.invokeStatic(object2);
    }
}

