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
import datomic.memcached$fn__9971$G__9966__9976;
import datomic.memcached$fn__9971$G__9967__9973;

public final class memcached$fn__9971
extends AFunction {
    public static Object invokeStatic(Object cache__7645__auto__) {
        memcached$fn__9971$G__9967__9973 G__9967;
        memcached$fn__9971$G__9967__9973 memcached$fn__9971$G__9967__9973 = G__9967 = new memcached$fn__9971$G__9967__9973();
        G__9967 = null;
        memcached$fn__9971$G__9966__9976 f__7646__auto__9981 = new memcached$fn__9971$G__9966__9976((Object)memcached$fn__9971$G__9967__9973);
        Object object = cache__7645__auto__;
        cache__7645__auto__ = null;
        ((AFunction)f__7646__auto__9981).__methodImplCache = (MethodImplCache)object;
        Object var2_2 = null;
        return f__7646__auto__9981;
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return memcached$fn__9971.invokeStatic(object2);
    }
}

