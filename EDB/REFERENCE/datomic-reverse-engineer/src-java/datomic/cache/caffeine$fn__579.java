/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  com.github.benmanes.caffeine.cache.Cache
 */
package datomic.cache;

import clojure.lang.AFunction;
import com.github.benmanes.caffeine.cache.Cache;

public final class caffeine$fn__579
extends AFunction {
    public static Object invokeStatic(Object c, Object k) {
        Object v = ((Cache)c).getIfPresent(k);
        Object object = c;
        c = null;
        Object object2 = k;
        k = null;
        ((Cache)object).invalidate(object2);
        Object var2_2 = null;
        return v;
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return caffeine$fn__579.invokeStatic(object3, object4);
    }
}

