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

public final class caffeine$fn__601
extends AFunction {
    public static Object invokeStatic(Object this_, Object k) {
        Object object = this_;
        this_ = null;
        Object object2 = k;
        k = null;
        return ((Cache)object).getIfPresent(object2);
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return caffeine$fn__601.invokeStatic(object3, object4);
    }
}

