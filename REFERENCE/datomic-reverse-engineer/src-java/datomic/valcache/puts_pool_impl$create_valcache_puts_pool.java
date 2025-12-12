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
 *  clojure.lang.PersistentHashMap
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic.valcache;

import clojure.lang.AFn;
import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.IObj;
import clojure.lang.IPersistentMap;
import clojure.lang.ISeq;
import clojure.lang.Keyword;
import clojure.lang.PersistentHashMap;
import clojure.lang.RT;
import clojure.lang.Var;
import datomic.valcache.puts_pool_impl$create_valcache_puts_pool$reify__9894;
import datomic.valcache.puts_pool_impl.ValcachePutsPoolImpl;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public final class puts_pool_impl$create_valcache_puts_pool
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.valcache.puts-pool-impl", (String)"create-valcache-puts-pool");
    public static final Keyword const__1 = RT.keyword(null, (String)"limit");
    public static final Object const__2 = 1000L;
    public static final Keyword const__3 = RT.keyword(null, (String)"threads");
    public static final Var const__4 = RT.var((String)"datomic.config", (String)"property");
    public static final Var const__5 = RT.var((String)"clojure.core", (String)"seq?");
    public static final Var const__6 = RT.var((String)"clojure.core", (String)"seq");
    public static final Var const__8 = RT.var((String)"clojure.core", (String)"atom");
    public static final Object const__9 = 0L;
    public static final AFn const__15 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"line"), 70, RT.keyword(null, (String)"column"), 36});

    public static Object invokeStatic(Object p__9892) {
        Object object;
        Object object2 = p__9892;
        p__9892 = null;
        Object map__9893 = object2;
        Object object3 = ((IFn)const__5.getRawRoot()).invoke(map__9893);
        if (object3 != null && object3 != Boolean.FALSE) {
            Object object4 = map__9893;
            map__9893 = null;
            object = PersistentHashMap.create((ISeq)((ISeq)((IFn)const__6.getRawRoot()).invoke(object4)));
        } else {
            object = map__9893;
            map__9893 = null;
        }
        Object map__98932 = object;
        Object limit2 = RT.get((Object)map__98932, (Object)const__1);
        Object object5 = map__98932;
        map__98932 = null;
        Object threads = RT.get((Object)object5, (Object)const__3);
        Object idx = ((IFn)const__8.getRawRoot()).invoke(const__9);
        int n = RT.intCast((Object)threads);
        Object object6 = threads;
        threads = null;
        Object object7 = idx;
        idx = null;
        ThreadPoolExecutor exec = new ThreadPoolExecutor(n, RT.intCast((Object)object6), 0L, TimeUnit.MILLISECONDS, (BlockingQueue<Runnable>)new LinkedBlockingQueue(), (ThreadFactory)((IObj)new puts_pool_impl$create_valcache_puts_pool$reify__9894(null, object7)).withMeta((IPersistentMap)const__15));
        ConcurrentHashMap puts = new ConcurrentHashMap();
        Object object8 = limit2;
        limit2 = null;
        ConcurrentHashMap concurrentHashMap = puts;
        puts = null;
        ThreadPoolExecutor threadPoolExecutor = exec;
        exec = null;
        return new ValcachePutsPoolImpl(object8, concurrentHashMap, threadPoolExecutor);
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return puts_pool_impl$create_valcache_puts_pool.invokeStatic(object2);
    }

    public static Object invokeStatic() {
        return ((IFn)const__0.getRawRoot()).invoke((Object)RT.mapUniqueKeys((Object[])new Object[]{const__1, const__2, const__3, ((IFn)const__4.getRawRoot()).invoke((Object)"datomic.valcachePutsPool")}));
    }

    public Object invoke() {
        return puts_pool_impl$create_valcache_puts_pool.invokeStatic();
    }
}

