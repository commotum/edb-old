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
import datomic.memcached$fn__9982$G__9964__9993;
import datomic.memcached$fn__9982$G__9965__9987;

public final class memcached$fn__9982
extends AFunction {
    public static Object invokeStatic(Object cache__7645__auto__) {
        memcached$fn__9982$G__9965__9987 G__9965;
        memcached$fn__9982$G__9965__9987 memcached$fn__9982$G__9965__9987 = G__9965 = new memcached$fn__9982$G__9965__9987();
        G__9965 = null;
        memcached$fn__9982$G__9964__9993 f__7646__auto__9998 = new memcached$fn__9982$G__9964__9993((Object)memcached$fn__9982$G__9965__9987);
        Object object = cache__7645__auto__;
        cache__7645__auto__ = null;
        ((AFunction)f__7646__auto__9998).__methodImplCache = (MethodImplCache)object;
        Object var2_2 = null;
        return f__7646__auto__9998;
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return memcached$fn__9982.invokeStatic(object2);
    }
}

