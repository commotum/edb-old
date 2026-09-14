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
import datomic.cache.caffeine$fn__588$G__583__595;
import datomic.cache.caffeine$fn__588$G__584__591;

public final class caffeine$fn__588
extends AFunction {
    public static Object invokeStatic(Object cache__7645__auto__) {
        caffeine$fn__588$G__584__591 G__584;
        caffeine$fn__588$G__584__591 caffeine$fn__588$G__584__591 = G__584 = new caffeine$fn__588$G__584__591();
        G__584 = null;
        caffeine$fn__588$G__583__595 f__7646__auto__600 = new caffeine$fn__588$G__583__595((Object)caffeine$fn__588$G__584__591);
        Object object = cache__7645__auto__;
        cache__7645__auto__ = null;
        ((AFunction)f__7646__auto__600).__methodImplCache = (MethodImplCache)object;
        Object var2_2 = null;
        return f__7646__auto__600;
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return caffeine$fn__588.invokeStatic(object2);
    }
}

