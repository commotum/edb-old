/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFn
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.IObj
 *  clojure.lang.IPersistentMap
 *  clojure.lang.ISeq
 *  clojure.lang.Keyword
 *  clojure.lang.Numbers
 *  clojure.lang.PersistentHashMap
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFn;
import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.IObj;
import clojure.lang.IPersistentMap;
import clojure.lang.ISeq;
import clojure.lang.Keyword;
import clojure.lang.Numbers;
import clojure.lang.PersistentHashMap;
import clojure.lang.RT;
import clojure.lang.Var;
import datomic.memcached$create_cache$reify__10040;

public final class memcached$create_cache
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"seq?");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"seq");
    public static final Keyword const__3 = RT.keyword(null, (String)"shutdown-client?");
    public static final Keyword const__4 = RT.keyword(null, (String)"record-kv");
    public static final Var const__5 = RT.var((String)"datomic.monitor", (String)"add-stat");
    public static final Keyword const__6 = RT.keyword(null, (String)"client");
    public static final Keyword const__7 = RT.keyword(null, (String)"metric-names");
    public static final Var const__8 = RT.var((String)"datomic.memcached", (String)"memcached-metric-names");
    public static final Var const__22 = RT.var((String)"datomic.config", (String)"property");
    public static final AFn const__29 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"line"), 234, RT.keyword(null, (String)"column"), 5});

    public static Object invokeStatic(Object p__10035) {
        Object metric_names;
        Object object;
        Object object2 = p__10035;
        p__10035 = null;
        Object map__10036 = object2;
        Object object3 = ((IFn)const__0.getRawRoot()).invoke(map__10036);
        if (object3 != null && object3 != Boolean.FALSE) {
            Object object4 = map__10036;
            map__10036 = null;
            object = PersistentHashMap.create((ISeq)((ISeq)((IFn)const__1.getRawRoot()).invoke(object4)));
        } else {
            object = map__10036;
            map__10036 = null;
        }
        Object map__100362 = object;
        Object shutdown_client_QMARK_ = RT.get((Object)map__100362, (Object)const__3, (Object)Boolean.TRUE);
        Object record_kv = RT.get((Object)map__100362, (Object)const__4, (Object)const__5.getRawRoot());
        Object client2 = RT.get((Object)map__100362, (Object)const__6);
        Object object5 = map__100362;
        map__100362 = null;
        Object object6 = metric_names = RT.get((Object)object5, (Object)const__7, (Object)const__8.getRawRoot());
        metric_names = null;
        Object vec__10037 = object6;
        Object io_counter = RT.nth((Object)vec__10037, (int)RT.intCast((long)0L), null);
        Object io_latency = RT.nth((Object)vec__10037, (int)RT.intCast((long)1L), null);
        Object hit_counter = RT.nth((Object)vec__10037, (int)RT.intCast((long)2L), null);
        Object get_succeeded = RT.nth((Object)vec__10037, (int)RT.intCast((long)3L), null);
        Object get_failed = RT.nth((Object)vec__10037, (int)RT.intCast((long)4L), null);
        Object get_missed = RT.nth((Object)vec__10037, (int)RT.intCast((long)5L), null);
        Object get_timeout = RT.nth((Object)vec__10037, (int)RT.intCast((long)6L), null);
        Object get_queue_full = RT.nth((Object)vec__10037, (int)RT.intCast((long)7L), null);
        Object put_succeeded = RT.nth((Object)vec__10037, (int)RT.intCast((long)8L), null);
        Object object7 = vec__10037;
        vec__10037 = null;
        Object put_failed = RT.nth((Object)object7, (int)RT.intCast((long)9L), null);
        int ttl = RT.intCast((Object)Numbers.multiply((Object)Numbers.multiply((Object)Numbers.multiply((Object)((IFn)const__22.getRawRoot()).invoke((Object)"datomic.memcachedExpirationDays"), (long)24L), (long)60L), (long)60L));
        Class<?> bytes_class = Class.forName("[B");
        Object object8 = io_counter;
        io_counter = null;
        Object object9 = get_failed;
        get_failed = null;
        Object object10 = put_failed;
        put_failed = null;
        Object object11 = record_kv;
        record_kv = null;
        Object object12 = get_succeeded;
        get_succeeded = null;
        Object object13 = hit_counter;
        hit_counter = null;
        Object object14 = get_missed;
        get_missed = null;
        Object object15 = client2;
        client2 = null;
        Object object16 = put_succeeded;
        put_succeeded = null;
        Object object17 = get_timeout;
        get_timeout = null;
        Object object18 = get_queue_full;
        get_queue_full = null;
        Object object19 = shutdown_client_QMARK_;
        shutdown_client_QMARK_ = null;
        Class<?> clazz = bytes_class;
        bytes_class = null;
        Object object20 = io_latency;
        io_latency = null;
        return ((IObj)new memcached$create_cache$reify__10040(null, object8, ttl, object9, object10, object11, object12, object13, object14, object15, object16, object17, object18, object19, clazz, object20)).withMeta((IPersistentMap)const__29);
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return memcached$create_cache.invokeStatic(object2);
    }
}

