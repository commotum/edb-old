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
import datomic.cache.impl$fn__357$G__339__364;
import datomic.cache.impl$fn__357$G__340__360;

public final class impl$fn__357
extends AFunction {
    public static Object invokeStatic(Object cache__7645__auto__) {
        impl$fn__357$G__340__360 G__340;
        impl$fn__357$G__340__360 impl$fn__357$G__340__360 = G__340 = new impl$fn__357$G__340__360();
        G__340 = null;
        impl$fn__357$G__339__364 f__7646__auto__369 = new impl$fn__357$G__339__364((Object)impl$fn__357$G__340__360);
        Object object = cache__7645__auto__;
        cache__7645__auto__ = null;
        ((AFunction)f__7646__auto__369).__methodImplCache = (MethodImplCache)object;
        Object var2_2 = null;
        return f__7646__auto__369;
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return impl$fn__357.invokeStatic(object2);
    }
}

