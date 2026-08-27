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
import datomic.valcache$start_server$accept_loop__9804;
import datomic.valcache$start_server$fn__9808;
import datomic.valcache$start_server$internal_shutdown__9790;
import datomic.valcache$start_server$socket_loop__9798;
import datomic.valcache.Server;
import java.net.InetSocketAddress;
import java.nio.channels.ServerSocketChannel;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class valcache$start_server
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"seq?");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"seq");
    public static final Keyword const__3 = RT.keyword(null, (String)"host");
    public static final Keyword const__4 = RT.keyword(null, (String)"port");
    public static final Object const__5 = 11211L;
    public static final Keyword const__6 = RT.keyword(null, (String)"path");
    public static final Keyword const__7 = RT.keyword(null, (String)"eviction-threshold-mb");
    public static final Keyword const__8 = RT.keyword(null, (String)"concurrency");
    public static final Object const__9 = 8L;
    public static final Keyword const__10 = RT.keyword(null, (String)"eviction-interval-secs");
    public static final Object const__11 = 60L;
    public static final Keyword const__12 = RT.keyword(null, (String)"eviction-file-window");
    public static final Object const__13 = 10000L;
    public static final Keyword const__14 = RT.keyword(null, (String)"sasl");
    public static final Keyword const__15 = RT.keyword(null, (String)"ip-validator");
    public static final Var const__16 = RT.var((String)"clojure.core", (String)"constantly");
    public static final Var const__17 = RT.var((String)"datomic.slf4j", (String)"process");
    public static final Keyword const__18 = RT.keyword(null, (String)"event");
    public static final Keyword const__19 = RT.keyword((String)"valcache", (String)"start");
    public static final Keyword const__20 = RT.keyword((String)"datomic.valcache", (String)"host");
    public static final Keyword const__21 = RT.keyword((String)"datomic.valcache", (String)"port");
    public static final Keyword const__22 = RT.keyword((String)"datomic.valcache", (String)"path");
    public static final Keyword const__23 = RT.keyword((String)"datomic.valcache", (String)"eviction-threshold-mb");
    public static final Var const__24 = RT.var((String)"clojure.core", (String)"atom");
    public static final Object const__25 = 0L;
    public static final Var const__26 = RT.var((String)"datomic.valcache", (String)"mkdirs");
    public static final Var const__27 = RT.var((String)"datomic.async", (String)"daemon");
    public static final Var const__28 = RT.var((String)"clojure.core", (String)"reset!");
    public static final Var const__29 = RT.var((String)"datomic.valcache", (String)"valcache-ref");

    public static Object invokeStatic(Object p__9788) {
        Object object;
        Object object2 = p__9788;
        p__9788 = null;
        Object map__9789 = object2;
        Object object3 = ((IFn)const__0.getRawRoot()).invoke(map__9789);
        if (object3 != null && object3 != Boolean.FALSE) {
            Object object4 = map__9789;
            map__9789 = null;
            object = PersistentHashMap.create((ISeq)((ISeq)((IFn)const__1.getRawRoot()).invoke(object4)));
        } else {
            object = map__9789;
            map__9789 = null;
        }
        Object map__97892 = object;
        Object host = RT.get((Object)map__97892, (Object)const__3);
        Object port = RT.get((Object)map__97892, (Object)const__4, (Object)const__5);
        Object path2 = RT.get((Object)map__97892, (Object)const__6);
        Object eviction_threshold_mb = RT.get((Object)map__97892, (Object)const__7);
        Object concurrency = RT.get((Object)map__97892, (Object)const__8, (Object)const__9);
        Object eviction_interval_secs = RT.get((Object)map__97892, (Object)const__10, (Object)const__11);
        Object eviction_file_window = RT.get((Object)map__97892, (Object)const__12, (Object)const__13);
        Object sasl = RT.get((Object)map__97892, (Object)const__14);
        Object object5 = map__97892;
        map__97892 = null;
        Object ip_validator = RT.get((Object)object5, (Object)const__15, (Object)((IFn)const__16.getRawRoot()).invoke((Object)Boolean.TRUE));
        Logger logger = LoggerFactory.getLogger((String)"datomic.valcache");
        if (logger.isInfoEnabled()) {
            Logger logger2 = logger;
            logger = null;
            logger2.info((String)((IFn)const__17.getRawRoot()).invoke((Object)RT.mapUniqueKeys((Object[])new Object[]{const__18, const__19, const__20, host, const__21, port, const__22, path2, const__23, eviction_threshold_mb})));
        }
        ServerSocketChannel ssc = ServerSocketChannel.open();
        Semaphore sem = new Semaphore(RT.intCast((Object)((Number)concurrency)), Boolean.TRUE);
        ConcurrentHashMap socket_registry = new ConcurrentHashMap();
        Object handled = ((IFn)const__24.getRawRoot()).invoke(const__25);
        Object shutdown_requested = ((IFn)const__24.getRawRoot()).invoke(null);
        valcache$start_server$internal_shutdown__9790 internal_shutdown = new valcache$start_server$internal_shutdown__9790(ssc, socket_registry, shutdown_requested);
        Object object6 = sasl;
        sasl = null;
        valcache$start_server$socket_loop__9798 socket_loop = new valcache$start_server$socket_loop__9798(object6, handled, path2, sem, socket_registry);
        Object object7 = ip_validator;
        ip_validator = null;
        valcache$start_server$socket_loop__9798 valcache$start_server$socket_loop__9798 = socket_loop;
        socket_loop = null;
        valcache$start_server$accept_loop__9804 accept_loop = new valcache$start_server$accept_loop__9804(ssc, (Object)internal_shutdown, object7, (Object)valcache$start_server$socket_loop__9798, socket_registry);
        ((IFn)const__26.getRawRoot()).invoke(path2);
        ServerSocketChannel serverSocketChannel = ssc;
        ssc = null;
        serverSocketChannel.socket().bind(new InetSocketAddress((String)host, RT.intCast((Object)((Number)port))));
        Object object8 = eviction_file_window;
        eviction_file_window = null;
        Object object9 = eviction_interval_secs;
        eviction_interval_secs = null;
        Object object10 = eviction_threshold_mb;
        eviction_threshold_mb = null;
        Object object11 = shutdown_requested;
        shutdown_requested = null;
        ((IFn)const__27.getRawRoot()).invoke((Object)new valcache$start_server$fn__9808(object8, object9, path2, object10, object11), (Object)"valcache-eviction-loop");
        valcache$start_server$accept_loop__9804 valcache$start_server$accept_loop__9804 = accept_loop;
        accept_loop = null;
        ((IFn)const__27.getRawRoot()).invoke((Object)valcache$start_server$accept_loop__9804, (Object)"valcache-accept-loop");
        Semaphore semaphore = sem;
        sem = null;
        Object object12 = concurrency;
        concurrency = null;
        ConcurrentHashMap concurrentHashMap = socket_registry;
        socket_registry = null;
        Object object13 = handled;
        handled = null;
        Object object14 = host;
        host = null;
        Object object15 = port;
        port = null;
        Object object16 = path2;
        path2 = null;
        valcache$start_server$internal_shutdown__9790 valcache$start_server$internal_shutdown__9790 = internal_shutdown;
        internal_shutdown = null;
        return ((IFn)const__28.getRawRoot()).invoke(const__29.getRawRoot(), (Object)new Server(semaphore, RT.longCast((Object)((Number)object12)), concurrentHashMap, object13, object14, object15, object16, (Object)valcache$start_server$internal_shutdown__9790));
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return valcache$start_server.invokeStatic(object2);
    }
}

