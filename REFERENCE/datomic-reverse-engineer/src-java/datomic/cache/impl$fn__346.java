/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.MethodImplCache
 */
package datomic.cache;

import clojure.lang.AFunction;
import clojure.lang.MethodImplCache;
import datomic.cache.impl$fn__346$G__341__351;
import datomic.cache.impl$fn__346$G__342__348;

public final class impl$fn__346
extends AFunction {
    public static Object invokeStatic(Object cache__7645__auto__) {
        impl$fn__346$G__342__348 G__342;
        impl$fn__346$G__342__348 impl$fn__346$G__342__348 = G__342 = new impl$fn__346$G__342__348();
        G__342 = null;
        impl$fn__346$G__341__351 f__7646__auto__356 = new impl$fn__346$G__341__351((Object)impl$fn__346$G__342__348);
        Object object = cache__7645__auto__;
        cache__7645__auto__ = null;
        ((AFunction)f__7646__auto__356).__methodImplCache = (MethodImplCache)object;
        Object var2_2 = null;
        return f__7646__auto__356;
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return impl$fn__346.invokeStatic(object2);
    }
}

