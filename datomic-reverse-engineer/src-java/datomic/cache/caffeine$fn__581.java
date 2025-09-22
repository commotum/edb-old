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

public final class caffeine$fn__581
extends AFunction {
    public static Object invokeStatic(Object c) {
        Object object = c;
        c = null;
        ((Cache)object).invalidateAll();
        return null;
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return caffeine$fn__581.invokeStatic(object2);
    }
}

