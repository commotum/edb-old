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
import datomic.memcached$fn__9999$G__9962__10006;
import datomic.memcached$fn__9999$G__9963__10002;

public final class memcached$fn__9999
extends AFunction {
    public static Object invokeStatic(Object cache__7645__auto__) {
        memcached$fn__9999$G__9963__10002 G__9963;
        memcached$fn__9999$G__9963__10002 memcached$fn__9999$G__9963__10002 = G__9963 = new memcached$fn__9999$G__9963__10002();
        G__9963 = null;
        memcached$fn__9999$G__9962__10006 f__7646__auto__10011 = new memcached$fn__9999$G__9962__10006((Object)memcached$fn__9999$G__9963__10002);
        Object object = cache__7645__auto__;
        cache__7645__auto__ = null;
        ((AFunction)f__7646__auto__10011).__methodImplCache = (MethodImplCache)object;
        Object var2_2 = null;
        return f__7646__auto__10011;
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return memcached$fn__9999.invokeStatic(object2);
    }
}

