/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.Keyword
 *  clojure.lang.Numbers
 *  clojure.lang.RT
 *  clojure.lang.Var
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Keyword;
import clojure.lang.Numbers;
import clojure.lang.RT;
import clojure.lang.Var;
import datomic.domain$create_object_cache$fn__16778;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class domain$create_object_cache
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.domain", (String)"create-object-cache");
    public static final Var const__1 = RT.var((String)"datomic.config", (String)"property");
    public static final Var const__2 = RT.var((String)"datomic.slf4j", (String)"process");
    public static final Keyword const__3 = RT.keyword(null, (String)"event");
    public static final Keyword const__4 = RT.keyword((String)"cache", (String)"create");
    public static final Keyword const__5 = RT.keyword(null, (String)"cache-bytes");
    public static final Var const__6 = RT.var((String)"datomic.cache", (String)"create-scaled-weight-limited");
    public static final Object const__10 = 1000L;

    public static Object invokeStatic(Object cache_bytes) {
        Logger logger = LoggerFactory.getLogger((String)"datomic.domain");
        if (logger.isInfoEnabled()) {
            Logger logger2 = logger;
            logger = null;
            logger2.info((String)((IFn)const__2.getRawRoot()).invoke((Object)RT.mapUniqueKeys((Object[])new Object[]{const__3, const__4, const__5, cache_bytes})));
        }
        Object object = cache_bytes;
        cache_bytes = null;
        return ((IFn)const__6.getRawRoot()).invoke((Object)Numbers.num((long)RT.longCast((double)Numbers.multiply((double)0.9, (Object)object))), (Object)new domain$create_object_cache$fn__16778(), const__10);
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return domain$create_object_cache.invokeStatic(object2);
    }

    public static Object invokeStatic() {
        return ((IFn)const__0.getRawRoot()).invoke(((IFn)const__1.getRawRoot()).invoke((Object)"datomic.objectCacheMax"));
    }

    public Object invoke() {
        return domain$create_object_cache.invokeStatic();
    }
}

