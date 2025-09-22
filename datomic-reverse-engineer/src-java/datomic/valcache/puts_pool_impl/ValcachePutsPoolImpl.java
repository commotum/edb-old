/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.IFn
 *  clojure.lang.IObj
 *  clojure.lang.IPersistentMap
 *  clojure.lang.IPersistentVector
 *  clojure.lang.ISeq
 *  clojure.lang.IType
 *  clojure.lang.Keyword
 *  clojure.lang.Numbers
 *  clojure.lang.PersistentHashMap
 *  clojure.lang.RT
 *  clojure.lang.Symbol
 *  clojure.lang.Tuple
 *  clojure.lang.Var
 */
package datomic.valcache.puts_pool_impl;

import clojure.lang.IFn;
import clojure.lang.IObj;
import clojure.lang.IPersistentMap;
import clojure.lang.IPersistentVector;
import clojure.lang.ISeq;
import clojure.lang.IType;
import clojure.lang.Keyword;
import clojure.lang.Numbers;
import clojure.lang.PersistentHashMap;
import clojure.lang.RT;
import clojure.lang.Symbol;
import clojure.lang.Tuple;
import clojure.lang.Var;
import datomic.valcache.puts_pool.PutsPool;
import datomic.valcache.puts_pool_impl.ValcachePutsPoolImpl$fn__9883;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ThreadPoolExecutor;

public final class ValcachePutsPoolImpl
implements PutsPool,
AutoCloseable,
IType {
    public final Object limit;
    public final Object puts;
    public final Object pool;
    public static final Var const__0 = RT.var((String)"datomic.monitor", (String)"add-stat");
    public static final Keyword const__1 = RT.keyword(null, (String)"ValcachePutQueueLength");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"not");
    public static final Var const__4 = RT.var((String)"clojure.core", (String)"promise");
    public static final Keyword const__5 = RT.keyword(null, (String)"data");
    public static final Keyword const__6 = RT.keyword(null, (String)"thunk");
    public static final Var const__7 = RT.var((String)"clojure.core", (String)"seq?");
    public static final Var const__8 = RT.var((String)"clojure.core", (String)"seq");
    public static final Keyword const__10 = RT.keyword(null, (String)"ValcachePutInFlight");
    public static final Object const__11 = 1L;
    public static final Var const__12 = RT.var((String)"clojure.core", (String)"deref");
    public static final Var const__13 = RT.var((String)"clojure.core", (String)"deliver");
    public static final Var const__14 = RT.var((String)"datomic.common", (String)"pfuture");
    public static final Keyword const__15 = RT.keyword(null, (String)"ValcachePutFailFull");
    public static final Keyword const__16 = RT.keyword(null, (String)"fut");

    public ValcachePutsPoolImpl(Object object, Object object2, Object object3) {
        this.limit = object;
        this.puts = object2;
        this.pool = object3;
    }

    public static IPersistentVector getBasis() {
        return Tuple.create((Object)Symbol.intern(null, (String)"limit"), (Object)((IObj)Symbol.intern(null, (String)"puts")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), Symbol.intern(null, (String)"ConcurrentMap")})), (Object)((IObj)Symbol.intern(null, (String)"pool")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), Symbol.intern(null, (String)"ThreadPoolExecutor")})));
    }

    public Object get_queued_put(Object k) {
        IPersistentMap iPersistentMap;
        Object temp__5457__auto__9887;
        Object object = k;
        k = null;
        Object object2 = temp__5457__auto__9887 = RT.get((Object)this.puts, (Object)object);
        if (object2 != null && object2 != Boolean.FALSE) {
            Object object3;
            Object object4 = temp__5457__auto__9887;
            temp__5457__auto__9887 = null;
            Object map__9885 = object4;
            Object object5 = ((IFn)const__7.getRawRoot()).invoke(map__9885);
            if (object5 != null && object5 != Boolean.FALSE) {
                Object object6 = map__9885;
                map__9885 = null;
                object3 = PersistentHashMap.create((ISeq)((ISeq)((IFn)const__8.getRawRoot()).invoke(object6)));
            } else {
                object3 = map__9885;
                map__9885 = null;
            }
            Object map__98852 = object3;
            Object data2 = RT.get((Object)map__98852, (Object)const__5);
            Object object7 = map__98852;
            map__98852 = null;
            Object thunk = RT.get((Object)object7, (Object)const__6);
            Object[] objectArray = new Object[4];
            objectArray[0] = const__5;
            Object object8 = data2;
            data2 = null;
            objectArray[1] = object8;
            objectArray[2] = const__16;
            Object object9 = thunk;
            thunk = null;
            objectArray[3] = ((IFn)const__12.getRawRoot()).invoke(object9);
            iPersistentMap = RT.mapUniqueKeys((Object[])objectArray);
        } else {
            iPersistentMap = null;
        }
        return iPersistentMap;
    }

    public Object submit(Object k, Object data2, Object f) {
        Object object;
        Object object2;
        Object and__5236__auto__9888;
        ((IFn)const__0.getRawRoot()).invoke((Object)const__1, (Object)((Map)this_.puts).size());
        Object object3 = and__5236__auto__9888 = ((IFn)const__2.getRawRoot()).invoke((Object)(((ThreadPoolExecutor)this_.pool).isShutdown() ? Boolean.TRUE : Boolean.FALSE));
        if (object3 != null && object3 != Boolean.FALSE) {
            object2 = Numbers.lte((long)((Map)this_.puts).size(), (Object)this_.limit) ? Boolean.TRUE : Boolean.FALSE;
        } else {
            object2 = and__5236__auto__9888;
            and__5236__auto__9888 = null;
        }
        if (object2 != null && object2 != Boolean.FALSE) {
            ValcachePutsPoolImpl this_;
            Object thunk;
            IPersistentMap map__9882;
            IPersistentMap iPersistentMap;
            IPersistentMap new_data;
            Object new_thunk = ((IFn)const__4.getRawRoot()).invoke();
            Object[] objectArray = new Object[4];
            objectArray[0] = const__5;
            Object object4 = data2;
            data2 = null;
            objectArray[1] = object4;
            objectArray[2] = const__6;
            objectArray[3] = new_thunk;
            IPersistentMap iPersistentMap2 = new_data = RT.mapUniqueKeys((Object[])objectArray);
            new_data = null;
            IPersistentMap map__98822 = ((ConcurrentMap)this_.puts).putIfAbsent(k, iPersistentMap2);
            Object object5 = ((IFn)const__7.getRawRoot()).invoke((Object)map__98822);
            if (object5 != null && object5 != Boolean.FALSE) {
                IPersistentMap iPersistentMap3 = map__98822;
                map__98822 = null;
                iPersistentMap = PersistentHashMap.create((ISeq)((ISeq)((IFn)const__8.getRawRoot()).invoke((Object)iPersistentMap3)));
            } else {
                iPersistentMap = map__98822;
                map__98822 = null;
            }
            IPersistentMap iPersistentMap4 = map__9882 = iPersistentMap;
            map__9882 = null;
            Object object6 = thunk = RT.get((Object)iPersistentMap4, (Object)const__6);
            if (object6 != null && object6 != Boolean.FALSE) {
                ((IFn)const__0.getRawRoot()).invoke((Object)const__10, const__11);
                Object object7 = thunk;
                thunk = null;
                this_ = null;
                object = ((IFn)const__12.getRawRoot()).invoke(object7);
            } else {
                long start = System.nanoTime();
                Object object8 = k;
                k = null;
                Object object9 = f;
                f = null;
                ((IFn)const__13.getRawRoot()).invoke(new_thunk, ((IFn)const__14.getRawRoot()).invoke(this_.pool, (Object)new ValcachePutsPoolImpl$fn__9883(object8, this_.puts, start, object9)));
                Object object10 = new_thunk;
                new_thunk = null;
                this_ = null;
                object = ((IFn)const__12.getRawRoot()).invoke(object10);
            }
        } else {
            ((IFn)const__0.getRawRoot()).invoke((Object)const__15, const__11);
            object = null;
        }
        return object;
    }

    public void close() throws Exception {
        ((ThreadPoolExecutor)this.pool).shutdown();
    }
}

