/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.ISeq
 *  clojure.lang.Keyword
 *  clojure.lang.PersistentHashMap
 *  clojure.lang.RT
 *  clojure.lang.Var
 *  datomic.spy.memcached.AddrUtil
 *  datomic.spy.memcached.ConnectionFactory
 *  datomic.spy.memcached.MemcachedClient
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.ISeq;
import clojure.lang.Keyword;
import clojure.lang.PersistentHashMap;
import clojure.lang.RT;
import clojure.lang.Var;
import datomic.spy.memcached.AddrUtil;
import datomic.spy.memcached.ConnectionFactory;
import datomic.spy.memcached.MemcachedClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class memcached$create_client
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"seq?");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"seq");
    public static final Keyword const__3 = RT.keyword(null, (String)"servers");
    public static final Var const__4 = RT.var((String)"datomic.slf4j", (String)"process");
    public static final Keyword const__5 = RT.keyword(null, (String)"event");
    public static final Keyword const__6 = RT.keyword((String)"memcached", (String)"connect");
    public static final Var const__7 = RT.var((String)"datomic.memcached", (String)"factory");

    public static Object invokeStatic(Object p__9956) {
        Object map__9957;
        Object object;
        Object object2 = p__9956;
        p__9956 = null;
        Object map__99572 = object2;
        Object object3 = ((IFn)const__0.getRawRoot()).invoke(map__99572);
        if (object3 != null && object3 != Boolean.FALSE) {
            Object object4 = map__99572;
            map__99572 = null;
            object = PersistentHashMap.create((ISeq)((ISeq)((IFn)const__1.getRawRoot()).invoke(object4)));
        } else {
            object = map__99572;
            map__99572 = null;
        }
        Object args = map__9957 = object;
        Object object5 = map__9957;
        map__9957 = null;
        Object servers = RT.get((Object)object5, (Object)const__3);
        Logger logger = LoggerFactory.getLogger((String)"datomic.memcached");
        if (logger.isInfoEnabled()) {
            Logger logger2 = logger;
            logger = null;
            logger2.info((String)((IFn)const__4.getRawRoot()).invoke((Object)RT.mapUniqueKeys((Object[])new Object[]{const__5, const__6, const__3, servers})));
        }
        Object object6 = args;
        args = null;
        Object object7 = servers;
        servers = null;
        return new MemcachedClient((ConnectionFactory)((IFn)const__7.getRawRoot()).invoke(object6), AddrUtil.getAddresses((String)((String)object7)));
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return memcached$create_client.invokeStatic(object2);
    }
}

