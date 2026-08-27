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
import datomic.cache.impl$fn__292$G__287__297;
import datomic.cache.impl$fn__292$G__288__294;

public final class impl$fn__292
extends AFunction {
    public static Object invokeStatic(Object cache__7645__auto__) {
        impl$fn__292$G__288__294 G__288;
        impl$fn__292$G__288__294 impl$fn__292$G__288__294 = G__288 = new impl$fn__292$G__288__294();
        G__288 = null;
        impl$fn__292$G__287__297 f__7646__auto__302 = new impl$fn__292$G__287__297((Object)impl$fn__292$G__288__294);
        Object object = cache__7645__auto__;
        cache__7645__auto__ = null;
        ((AFunction)f__7646__auto__302).__methodImplCache = (MethodImplCache)object;
        Object var2_2 = null;
        return f__7646__auto__302;
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return impl$fn__292.invokeStatic(object2);
    }
}

