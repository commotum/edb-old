/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFn
 *  clojure.lang.AFunction
 *  clojure.lang.IObj
 *  clojure.lang.IPersistentMap
 *  clojure.lang.RT
 */
package datomic;

import clojure.lang.AFn;
import clojure.lang.AFunction;
import clojure.lang.IObj;
import clojure.lang.IPersistentMap;
import clojure.lang.RT;
import datomic.cache$lookup_with_inflight_cache$reify__9410;
import java.util.concurrent.ConcurrentHashMap;

public final class cache$lookup_with_inflight_cache
extends AFunction {
    public static final AFn const__4 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"line"), 177, RT.keyword(null, (String)"column"), 5});

    public static Object invokeStatic(Object m) {
        ConcurrentHashMap in_flight;
        ConcurrentHashMap concurrentHashMap = in_flight = new ConcurrentHashMap();
        in_flight = null;
        Object object = m;
        m = null;
        return ((IObj)new cache$lookup_with_inflight_cache$reify__9410(null, concurrentHashMap, object)).withMeta((IPersistentMap)const__4);
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return cache$lookup_with_inflight_cache.invokeStatic(object2);
    }
}

