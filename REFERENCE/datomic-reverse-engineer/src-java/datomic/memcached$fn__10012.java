/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.MethodImplCache
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.MethodImplCache;
import datomic.memcached$fn__10012$G__9960__10017;
import datomic.memcached$fn__10012$G__9961__10014;

public final class memcached$fn__10012
extends AFunction {
    public static Object invokeStatic(Object cache__7645__auto__) {
        memcached$fn__10012$G__9961__10014 G__9961;
        memcached$fn__10012$G__9961__10014 memcached$fn__10012$G__9961__10014 = G__9961 = new memcached$fn__10012$G__9961__10014();
        G__9961 = null;
        memcached$fn__10012$G__9960__10017 f__7646__auto__10022 = new memcached$fn__10012$G__9960__10017((Object)memcached$fn__10012$G__9961__10014);
        Object object = cache__7645__auto__;
        cache__7645__auto__ = null;
        ((AFunction)f__7646__auto__10022).__methodImplCache = (MethodImplCache)object;
        Object var2_2 = null;
        return f__7646__auto__10022;
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return memcached$fn__10012.invokeStatic(object2);
    }
}

