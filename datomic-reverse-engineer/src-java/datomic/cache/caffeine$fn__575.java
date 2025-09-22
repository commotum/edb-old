/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.Numbers
 *  com.github.benmanes.caffeine.cache.Cache
 */
package datomic.cache;

import clojure.lang.AFunction;
import clojure.lang.Numbers;
import com.github.benmanes.caffeine.cache.Cache;

public final class caffeine$fn__575
extends AFunction {
    public static Object invokeStatic(Object coll) {
        Object object = coll;
        coll = null;
        return Numbers.num((long)((Cache)object).estimatedSize());
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return caffeine$fn__575.invokeStatic(object2);
    }
}

