/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 */
package datomic.cache;

import clojure.lang.AFunction;
import java.util.Map;

public final class impl$fn__372
extends AFunction {
    public static Object invokeStatic(Object c, Object k, Object v) {
        Object object = c;
        c = null;
        Object object2 = k;
        k = null;
        Object object3 = v;
        v = null;
        return ((Map)object).put(object2, object3);
    }

    public Object invoke(Object object, Object object2, Object object3) {
        Object object4 = object;
        object = null;
        Object object5 = object2;
        object2 = null;
        Object object6 = object3;
        object3 = null;
        return impl$fn__372.invokeStatic(object4, object5, object6);
    }
}

