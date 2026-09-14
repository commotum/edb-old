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
import datomic.cache.impl$fn__324$G__319__333;
import datomic.cache.impl$fn__324$G__320__328;

public final class impl$fn__324
extends AFunction {
    public static Object invokeStatic(Object cache__7645__auto__) {
        impl$fn__324$G__320__328 G__320;
        impl$fn__324$G__320__328 impl$fn__324$G__320__328 = G__320 = new impl$fn__324$G__320__328();
        G__320 = null;
        impl$fn__324$G__319__333 f__7646__auto__338 = new impl$fn__324$G__319__333((Object)impl$fn__324$G__320__328);
        Object object = cache__7645__auto__;
        cache__7645__auto__ = null;
        ((AFunction)f__7646__auto__338).__methodImplCache = (MethodImplCache)object;
        Object var2_2 = null;
        return f__7646__auto__338;
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return impl$fn__324.invokeStatic(object2);
    }
}

