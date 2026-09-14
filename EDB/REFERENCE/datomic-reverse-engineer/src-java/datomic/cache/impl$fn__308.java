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
import datomic.cache.impl$fn__308$G__303__313;
import datomic.cache.impl$fn__308$G__304__310;

public final class impl$fn__308
extends AFunction {
    public static Object invokeStatic(Object cache__7645__auto__) {
        impl$fn__308$G__304__310 G__304;
        impl$fn__308$G__304__310 impl$fn__308$G__304__310 = G__304 = new impl$fn__308$G__304__310();
        G__304 = null;
        impl$fn__308$G__303__313 f__7646__auto__318 = new impl$fn__308$G__303__313((Object)impl$fn__308$G__304__310);
        Object object = cache__7645__auto__;
        cache__7645__auto__ = null;
        ((AFunction)f__7646__auto__318).__methodImplCache = (MethodImplCache)object;
        Object var2_2 = null;
        return f__7646__auto__318;
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return impl$fn__308.invokeStatic(object2);
    }
}

