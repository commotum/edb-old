/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 */
package datomic.cache;

import clojure.lang.AFunction;
import datomic.cache.caffeine.WrappedCCache;

public final class caffeine$fn__605$__GT_WrappedCCache__609
extends AFunction {
    public Object invoke(Object cache2) {
        Object object = cache2;
        cache2 = null;
        return new WrappedCCache(object);
    }
}

