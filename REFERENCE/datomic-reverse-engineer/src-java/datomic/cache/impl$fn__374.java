/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 */
package datomic.cache;

import clojure.lang.AFunction;
import java.util.Map;

public final class impl$fn__374
extends AFunction {
    public static Object invokeStatic(Object c, Object k) {
        Object object = c;
        c = null;
        Object object2 = k;
        k = null;
        return ((Map)object).remove(object2);
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return impl$fn__374.invokeStatic(object3, object4);
    }
}

