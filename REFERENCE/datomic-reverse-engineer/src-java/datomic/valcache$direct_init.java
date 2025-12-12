/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFn
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.ISeq
 *  clojure.lang.Keyword
 *  clojure.lang.PersistentHashMap
 *  clojure.lang.RT
 *  clojure.lang.Symbol
 *  clojure.lang.Var
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 */
package datomic;

import clojure.lang.AFn;
import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.ISeq;
import clojure.lang.Keyword;
import clojure.lang.PersistentHashMap;
import clojure.lang.RT;
import clojure.lang.Symbol;
import clojure.lang.Var;
import datomic.valcache$direct_init$fn__9813;
import datomic.valcache$direct_init$fn__9815;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class valcache$direct_init
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"seq?");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"seq");
    public static final Keyword const__3 = RT.keyword(null, (String)"path");
    public static final Keyword const__4 = RT.keyword(null, (String)"eviction-threshold-mb");
    public static final Keyword const__5 = RT.keyword(null, (String)"concurrency");
    public static final Object const__6 = 8L;
    public static final Keyword const__7 = RT.keyword(null, (String)"eviction-interval-secs");
    public static final Object const__8 = 60L;
    public static final Keyword const__9 = RT.keyword(null, (String)"eviction-file-window");
    public static final Object const__10 = 10000L;
    public static final Var const__11 = RT.var((String)"clojure.core", (String)"str");
    public static final Var const__12 = RT.var((String)"clojure.core", (String)"pr-str");
    public static final AFn const__13 = (AFn)Symbol.intern(null, (String)"path");
    public static final AFn const__14 = (AFn)Symbol.intern(null, (String)"eviction-threshold-mb");
    public static final Var const__15 = RT.var((String)"datomic.slf4j", (String)"process");
    public static final Keyword const__16 = RT.keyword(null, (String)"event");
    public static final Keyword const__17 = RT.keyword((String)"valcache", (String)"direct-init");
    public static final Keyword const__18 = RT.keyword((String)"datomic.valcache", (String)"path");
    public static final Keyword const__19 = RT.keyword((String)"datomic.valcache", (String)"eviction-threshold-mb");
    public static final Var const__20 = RT.var((String)"clojure.core", (String)"atom");
    public static final Var const__21 = RT.var((String)"datomic.valcache", (String)"mkdirs");
    public static final Var const__22 = RT.var((String)"datomic.async", (String)"daemon");

    public static Object invokeStatic(Object p__9811) {
        Object object;
        Object object2 = p__9811;
        p__9811 = null;
        Object map__9812 = object2;
        Object object3 = ((IFn)const__0.getRawRoot()).invoke(map__9812);
        if (object3 != null && object3 != Boolean.FALSE) {
            Object object4 = map__9812;
            map__9812 = null;
            object = PersistentHashMap.create((ISeq)((ISeq)((IFn)const__1.getRawRoot()).invoke(object4)));
        } else {
            object = map__9812;
            map__9812 = null;
        }
        Object map__98122 = object;
        Object path2 = RT.get((Object)map__98122, (Object)const__3);
        Object eviction_threshold_mb = RT.get((Object)map__98122, (Object)const__4);
        RT.get((Object)map__98122, (Object)const__5, (Object)const__6);
        Object eviction_interval_secs = RT.get((Object)map__98122, (Object)const__7, (Object)const__8);
        Object object5 = map__98122;
        map__98122 = null;
        Object eviction_file_window = RT.get((Object)object5, (Object)const__9, (Object)const__10);
        Object object6 = path2;
        if (object6 == null || object6 == Boolean.FALSE) {
            throw (Throwable)((Object)new AssertionError(((IFn)const__11.getRawRoot()).invoke((Object)"Assert failed: ", ((IFn)const__12.getRawRoot()).invoke((Object)const__13))));
        }
        Object object7 = eviction_threshold_mb;
        if (object7 == null || object7 == Boolean.FALSE) {
            throw (Throwable)((Object)new AssertionError(((IFn)const__11.getRawRoot()).invoke((Object)"Assert failed: ", ((IFn)const__12.getRawRoot()).invoke((Object)const__14))));
        }
        Logger logger = LoggerFactory.getLogger((String)"datomic.valcache");
        if (logger.isInfoEnabled()) {
            Logger logger2 = logger;
            logger = null;
            logger2.info((String)((IFn)const__15.getRawRoot()).invoke((Object)RT.mapUniqueKeys((Object[])new Object[]{const__16, const__17, const__18, path2, const__19, eviction_threshold_mb})));
        }
        Object shutdown_requested = ((IFn)const__20.getRawRoot()).invoke(null);
        ((IFn)const__21.getRawRoot()).invoke(path2);
        Object object8 = eviction_interval_secs;
        eviction_interval_secs = null;
        Object object9 = eviction_threshold_mb;
        eviction_threshold_mb = null;
        Object object10 = eviction_file_window;
        eviction_file_window = null;
        Object object11 = path2;
        path2 = null;
        ((IFn)const__22.getRawRoot()).invoke((Object)new valcache$direct_init$fn__9813(object8, object9, object10, shutdown_requested, object11), (Object)"valcache-eviction-loop");
        Object object12 = shutdown_requested;
        shutdown_requested = null;
        return new valcache$direct_init$fn__9815(object12);
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return valcache$direct_init.invokeStatic(object2);
    }
}

