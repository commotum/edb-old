/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.IFn
 *  clojure.lang.ILookup
 *  clojure.lang.IPersistentVector
 *  clojure.lang.IType
 *  clojure.lang.Keyword
 *  clojure.lang.Numbers
 *  clojure.lang.RT
 *  clojure.lang.Symbol
 *  clojure.lang.Tuple
 *  clojure.lang.Util
 *  clojure.lang.Var
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 */
package datomic.valcache_direct;

import clojure.lang.IFn;
import clojure.lang.ILookup;
import clojure.lang.IPersistentVector;
import clojure.lang.IType;
import clojure.lang.Keyword;
import clojure.lang.Numbers;
import clojure.lang.RT;
import clojure.lang.Symbol;
import clojure.lang.Tuple;
import clojure.lang.Util;
import clojure.lang.Var;
import datomic.cache.impl.CachePut;
import datomic.valcache.puts_pool.PutsPool;
import datomic.valcache_direct.ValcacheDirect$fn__9916;
import datomic.valcache_direct.ValcacheDirect$fn__9918;
import datomic.valcache_direct.ValcacheDirect$record_latencies__9911;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ValcacheDirect
implements ILookup,
CachePut,
AutoCloseable,
IType {
    public final Object root;
    public final Object shutdown_fn;
    public final Object puts_pool;
    private static Class __cached_class__0;
    public static final Var const__0;
    public static final Keyword const__1;
    public static final Var const__2;
    public static final Var const__4;
    public static final Object const__6;
    public static final Object const__7;
    public static final Var const__8;
    public static final Keyword const__9;
    public static final Keyword const__11;
    public static final Var const__12;
    public static final Var const__13;
    public static final Keyword const__14;
    public static final Keyword const__15;
    public static final Keyword const__16;
    public static final Keyword const__17;
    public static final Var const__18;
    public static final Keyword const__19;
    public static final Keyword const__20;
    public static final Keyword const__21;
    public static final Object const__22;
    public static final Keyword const__23;

    public ValcacheDirect(Object object, Object object2, Object object3) {
        this.root = object;
        this.shutdown_fn = object2;
        this.puts_pool = object3;
    }

    public static IPersistentVector getBasis() {
        return Tuple.create((Object)Symbol.intern(null, (String)"root"), (Object)Symbol.intern(null, (String)"shutdown-fn"), (Object)Symbol.intern(null, (String)"puts-pool"));
    }

    /*
     * Enabled aggressive block sorting
     */
    public Object put(Object k, Object v) {
        Object object = ((IFn)const__12.getRawRoot()).invoke(v);
        if (object != null && object != Boolean.FALSE) {
            Object object2;
            Object object3 = this_.puts_pool;
            if (Util.classOf((Object)object3) != __cached_class__0) {
                if (object3 instanceof PutsPool) {
                    Object object4 = k;
                    Object[] objectArray = new Object[]{const__14, const__15, const__16, v};
                    Object object5 = k;
                    k = null;
                    Object object6 = v;
                    v = null;
                    object2 = ((PutsPool)object3).submit(object4, RT.mapUniqueKeys((Object[])objectArray), (Object)new ValcacheDirect$fn__9918(this_.root, object5, object6));
                    return object2;
                }
                object3 = object3;
                __cached_class__0 = Util.classOf((Object)object3);
            }
            Object object7 = k;
            Object[] objectArray = new Object[]{const__14, const__15, const__16, v};
            Object object8 = k;
            k = null;
            Object object9 = v;
            v = null;
            ValcacheDirect this_ = null;
            object2 = const__13.getRawRoot().invoke(object3, object7, (Object)RT.mapUniqueKeys((Object[])objectArray), (Object)new ValcacheDirect$fn__9918(this_.root, object8, object9));
            return object2;
        }
        ((IFn)const__8.getRawRoot()).invoke((Object)const__17, const__7);
        Logger logger = LoggerFactory.getLogger((String)"datomic.valcache-direct");
        if (!logger.isInfoEnabled()) return null;
        Logger logger2 = logger;
        logger = null;
        Object[] objectArray = new Object[6];
        objectArray[0] = const__19;
        objectArray[1] = const__20;
        objectArray[2] = const__21;
        objectArray[3] = const__22;
        objectArray[4] = const__23;
        Object object10 = k;
        k = null;
        objectArray[5] = object10;
        logger2.info((String)((IFn)const__18.getRawRoot()).invoke((Object)RT.mapUniqueKeys((Object[])objectArray)));
        return null;
    }

    public Object valAt(Object k, Object not_found) {
        Object object;
        Object object2;
        Object ret;
        Object temp__5455__auto__9923;
        ((IFn)const__0.getRawRoot()).invoke((Object)const__1);
        ValcacheDirect$record_latencies__9911 record_latencies2 = new ValcacheDirect$record_latencies__9911();
        Object object3 = temp__5455__auto__9923 = ((IFn)const__2.getRawRoot()).invoke(this.puts_pool, k);
        if (object3 != null && object3 != Boolean.FALSE) {
            Object object4 = temp__5455__auto__9923;
            temp__5455__auto__9923 = null;
            Object object5 = ret = object4;
            ret = null;
            object2 = Tuple.create((Object)object5);
        } else {
            long start__9163__auto__9921 = System.nanoTime();
            Object object6 = k;
            k = null;
            Object result__9164__auto__9922 = ((IFn)new ValcacheDirect$fn__9916(object6, this.root)).invoke();
            ValcacheDirect$record_latencies__9911 valcacheDirect$record_latencies__9911 = record_latencies2;
            record_latencies2 = null;
            ((IFn)valcacheDirect$record_latencies__9911).invoke((Object)Numbers.num((long)Numbers.minus((long)System.nanoTime(), (long)start__9163__auto__9921)), result__9164__auto__9922);
            Object object7 = result__9164__auto__9922;
            result__9164__auto__9922 = null;
            object2 = ((IFn)const__4.getRawRoot()).invoke(object7);
        }
        IPersistentVector vec__9907 = object2;
        ret = RT.nth((Object)vec__9907, (int)RT.intCast((long)0L), null);
        IPersistentVector iPersistentVector = vec__9907;
        vec__9907 = null;
        Object ex = RT.nth((Object)iPersistentVector, (int)RT.intCast((long)1L), null);
        Object object8 = ret;
        ((IFn)const__8.getRawRoot()).invoke((Object)const__9, object8 != null && object8 != Boolean.FALSE ? const__7 : const__6);
        Object object9 = ex;
        if (object9 != null && object9 != Boolean.FALSE) {
            Object object10 = ex;
            ex = null;
            throw (Throwable)object10;
        }
        if (Util.identical((Object)ret, null)) {
            object = not_found;
            not_found = null;
        } else {
            Keyword keyword = const__11;
            if (keyword != null && keyword != Boolean.FALSE) {
                object = ret;
                ret = null;
            } else {
                object = null;
            }
        }
        return object;
    }

    public Object valAt(Object k) {
        Object object = k;
        k = null;
        return ((ILookup)this).valAt(object, null);
    }

    public void close() throws Exception {
        ValcacheDirect this_ = null;
        ((IFn)this_.shutdown_fn).invoke();
    }

    static {
        const__0 = RT.var((String)"datomic.measure.io-stats", (String)"inc!");
        const__1 = RT.keyword(null, (String)"valcache");
        const__2 = RT.var((String)"datomic.valcache.puts-pool", (String)"get-from-queued-put");
        const__4 = RT.var((String)"datomic.common", (String)"return-or-throw");
        const__6 = 0L;
        const__7 = 1L;
        const__8 = RT.var((String)"datomic.monitor", (String)"add-stat");
        const__9 = RT.keyword(null, (String)"Valcache");
        const__11 = RT.keyword(null, (String)"else");
        const__12 = RT.var((String)"datomic.valcache-direct", (String)"fits-in-cache?");
        const__13 = RT.var((String)"datomic.valcache.puts-pool", (String)"submit");
        const__14 = RT.keyword(null, (String)"source");
        const__15 = RT.keyword(null, (String)"bbuf");
        const__16 = RT.keyword(null, (String)"v");
        const__17 = RT.keyword(null, (String)"ValcachePutFailSize");
        const__18 = RT.var((String)"datomic.slf4j", (String)"process");
        const__19 = RT.keyword(null, (String)"event");
        const__20 = RT.keyword((String)"valcache", (String)"item-too-large");
        const__21 = RT.keyword(null, (String)"max-bytes");
        const__22 = 1000000L;
        const__23 = RT.keyword(null, (String)"key");
    }
}

