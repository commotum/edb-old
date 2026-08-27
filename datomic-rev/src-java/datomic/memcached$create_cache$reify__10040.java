/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.IFn
 *  clojure.lang.ILookup
 *  clojure.lang.IObj
 *  clojure.lang.IPersistentMap
 *  clojure.lang.Keyword
 *  clojure.lang.Numbers
 *  clojure.lang.RT
 *  clojure.lang.Util
 *  clojure.lang.Var
 *  datomic.spy.memcached.internal.OperationCompletionListener
 *  datomic.spy.memcached.internal.OperationFuture
 *  org.slf4j.LoggerFactory
 */
package datomic;

import clojure.lang.IFn;
import clojure.lang.ILookup;
import clojure.lang.IObj;
import clojure.lang.IPersistentMap;
import clojure.lang.Keyword;
import clojure.lang.Numbers;
import clojure.lang.RT;
import clojure.lang.Util;
import clojure.lang.Var;
import datomic.cache.impl.CachePut;
import datomic.memcached$create_cache$reify__10040$fn__10051;
import datomic.memcached$create_cache$reify__10040$record_latencies__10045;
import datomic.memcached.RecoveringClientImpl;
import datomic.spy.memcached.internal.OperationCompletionListener;
import datomic.spy.memcached.internal.OperationFuture;
import java.nio.ByteBuffer;
import org.slf4j.LoggerFactory;

public final class memcached$create_cache$reify__10040
implements CachePut,
ILookup,
AutoCloseable,
IObj {
    final IPersistentMap __meta;
    Object io_counter;
    int ttl;
    Object get_failed;
    Object put_failed;
    Object record_kv;
    Object get_succeeded;
    Object hit_counter;
    Object get_missed;
    Object client;
    Object put_succeeded;
    Object get_timeout;
    Object get_queue_full;
    Object shutdown_client_QMARK_;
    Object bytes_class;
    Object io_latency;
    private static Class __cached_class__0;
    private static Class __cached_class__1;
    public static final Var const__0;
    public static final Var const__1;
    public static final Var const__3;
    public static final Object const__5;
    public static final Object const__6;
    public static final Var const__8;
    public static final Keyword const__9;
    public static final Var const__10;
    public static final Var const__11;
    public static final Var const__13;
    public static final Var const__14;
    public static final Var const__15;
    public static final Var const__16;
    public static final Keyword const__17;
    public static final Var const__18;
    public static final Keyword const__19;
    public static final Keyword const__20;
    public static final Keyword const__21;

    public memcached$create_cache$reify__10040(IPersistentMap iPersistentMap, Object object, int n, Object object2, Object object3, Object object4, Object object5, Object object6, Object object7, Object object8, Object object9, Object object10, Object object11, Object object12, Object object13, Object object14) {
        this.__meta = iPersistentMap;
        this.io_counter = object;
        this.ttl = n;
        this.get_failed = object2;
        this.put_failed = object3;
        this.record_kv = object4;
        this.get_succeeded = object5;
        this.hit_counter = object6;
        this.get_missed = object7;
        this.client = object8;
        this.put_succeeded = object9;
        this.get_timeout = object10;
        this.get_queue_full = object11;
        this.shutdown_client_QMARK_ = object12;
        this.bytes_class = object13;
        this.io_latency = object14;
    }

    public memcached$create_cache$reify__10040(Object object, int n, Object object2, Object object3, Object object4, Object object5, Object object6, Object object7, Object object8, Object object9, Object object10, Object object11, Object object12, Object object13, Object object14) {
        this(null, object, n, object2, object3, object4, object5, object6, object7, object8, object9, object10, object11, object12, object13, object14);
    }

    public IPersistentMap meta() {
        return this.__meta;
    }

    public IObj withMeta(IPersistentMap iPersistentMap) {
        return new memcached$create_cache$reify__10040(iPersistentMap, this.io_counter, this.ttl, this.get_failed, this.put_failed, this.record_kv, this.get_succeeded, this.hit_counter, this.get_missed, this.client, this.put_succeeded, this.get_timeout, this.get_queue_full, this.shutdown_client_QMARK_, this.bytes_class, this.io_latency);
    }

    /*
     * Unable to fully structure code
     */
    public Object put(Object k, Object v) {
        block10: {
            block9: {
                v0 = ((IFn)memcached$create_cache$reify__10040.const__10.getRawRoot()).invoke(v);
                if (v0 == null || v0 == Boolean.FALSE) break block9;
                try {
                    v1 = this.client;
                    if (Util.classOf((Object)v1) == memcached$create_cache$reify__10040.__cached_class__1) ** GOTO lbl9
                    if (!(v1 instanceof RecoveringClientImpl)) {
                        v1 = v1;
                        memcached$create_cache$reify__10040.__cached_class__1 = Util.classOf((Object)v1);
lbl9:
                        // 2 sources

                        v2 = k;
                        k = null;
                        v3 = this.ttl;
                        if (v instanceof ByteBuffer) {
                            v4 = v;
                            v = null;
                            v5 = ((IFn)memcached$create_cache$reify__10040.const__13.getRawRoot()).invoke(v4);
                        } else {
                            v5 = v;
                            v = null;
                        }
                        v6 = memcached$create_cache$reify__10040.const__11.getRawRoot().invoke(v1, v2, (Object)v3, v5);
                    } else {
                        v7 = (RecoveringClientImpl)v1;
                        v8 = k;
                        k = null;
                        v9 = this.ttl;
                        if (v instanceof ByteBuffer) {
                            v10 = v;
                            v = null;
                            v11 = ((IFn)memcached$create_cache$reify__10040.const__13.getRawRoot()).invoke(v10);
                        } else {
                            v11 = v;
                            v = null;
                        }
                        v6 = v7.rc_set(v8, v9, v11);
                    }
                    G__10053 = v6;
                    ((OperationFuture)G__10053).addListener((OperationCompletionListener)((IFn)memcached$create_cache$reify__10040.const__14.getRawRoot()).invoke(((IFn)memcached$create_cache$reify__10040.const__15.getRawRoot()).invoke(memcached$create_cache$reify__10040.const__16.getRawRoot(), this.record_kv, this.put_succeeded, this.put_failed)));
                    v12 = G__10053;
                    G__10053 = null;
                    var4_6 = v12;
                }
                catch (Exception e) {
                    var4_6 = null;
                }
                v13 = var4_6;
                break block10;
            }
            ((IFn)this.record_kv).invoke((Object)memcached$create_cache$reify__10040.const__17, memcached$create_cache$reify__10040.const__6);
            logger = LoggerFactory.getLogger((String)"datomic.memcached");
            if (logger.isInfoEnabled()) {
                v14 = logger;
                logger = null;
                v15 = new Object[4];
                v15[0] = memcached$create_cache$reify__10040.const__19;
                v15[1] = memcached$create_cache$reify__10040.const__20;
                v15[2] = memcached$create_cache$reify__10040.const__21;
                v16 = k;
                k = null;
                v15[3] = v16;
                v14.info((String)((IFn)memcached$create_cache$reify__10040.const__18.getRawRoot()).invoke((Object)RT.mapUniqueKeys((Object[])v15)));
            }
            v13 = null;
        }
        return v13;
    }

    public Object valAt(Object k, Object not_found) {
        Object object;
        ((IFn)const__1.getRawRoot()).invoke(this_.io_counter);
        memcached$create_cache$reify__10040$record_latencies__10045 record_latencies2 = new memcached$create_cache$reify__10040$record_latencies__10045(this_.get_failed, this_.record_kv, this_.get_succeeded, this_.get_missed, this_.get_timeout, this_.get_queue_full, this_.io_latency);
        long start__9163__auto__10055 = System.nanoTime();
        Object object2 = k;
        k = null;
        Object result__9164__auto__10056 = ((IFn)new memcached$create_cache$reify__10040$fn__10051(object2, this_.client)).invoke();
        memcached$create_cache$reify__10040$record_latencies__10045 memcached$create_cache$reify__10040$record_latencies__10045 = record_latencies2;
        record_latencies2 = null;
        ((IFn)memcached$create_cache$reify__10040$record_latencies__10045).invoke((Object)Numbers.num((long)Numbers.minus((long)System.nanoTime(), (long)start__9163__auto__10055)), result__9164__auto__10056);
        Object object3 = result__9164__auto__10056;
        result__9164__auto__10056 = null;
        Object vec__10041 = ((IFn)const__3.getRawRoot()).invoke(object3);
        Object result2 = RT.nth((Object)vec__10041, (int)RT.intCast((long)0L), null);
        Object object4 = vec__10041;
        vec__10041 = null;
        Object ex = RT.nth((Object)object4, (int)RT.intCast((long)1L), null);
        Object object5 = result2;
        ((IFn)this_.record_kv).invoke(this_.hit_counter, object5 != null && object5 != Boolean.FALSE ? const__6 : const__5);
        Object object6 = ex;
        if (object6 != null && object6 != Boolean.FALSE) {
            Object object7 = ex;
            ex = null;
            throw (Throwable)object7;
        }
        if (Util.identical((Object)result2, null)) {
            object = not_found;
            not_found = null;
        } else {
            Object object8 = ((IFn)const__8.getRawRoot()).invoke(this_.bytes_class, result2);
            if (object8 != null && object8 != Boolean.FALSE) {
                Object object9 = result2;
                result2 = null;
                memcached$create_cache$reify__10040 this_ = null;
                object = ByteBuffer.wrap((byte[])object9);
            } else {
                Keyword keyword = const__9;
                if (keyword != null && keyword != Boolean.FALSE) {
                    object = result2;
                    result2 = null;
                } else {
                    object = null;
                }
            }
        }
        return object;
    }

    public Object valAt(Object k) {
        Object object = k;
        k = null;
        return ((ILookup)this).valAt(object, null);
    }

    /*
     * Enabled aggressive block sorting
     */
    public void close() throws Exception {
        Object object;
        Object object2 = this_.shutdown_client_QMARK_;
        if (object2 == null) return;
        if (object2 == Boolean.FALSE) return;
        Object object3 = this_.client;
        if (Util.classOf((Object)object3) != __cached_class__0) {
            if (object3 instanceof RecoveringClientImpl) {
                object = ((RecoveringClientImpl)object3).rc_shutdown();
                return;
            }
            object3 = object3;
            __cached_class__0 = Util.classOf((Object)object3);
        }
        memcached$create_cache$reify__10040 this_ = null;
        object = const__0.getRawRoot().invoke(object3);
    }

    static {
        const__0 = RT.var((String)"datomic.memcached", (String)"rc-shutdown");
        const__1 = RT.var((String)"datomic.measure.io-stats", (String)"inc!");
        const__3 = RT.var((String)"datomic.common", (String)"return-or-throw");
        const__5 = 0L;
        const__6 = 1L;
        const__8 = RT.var((String)"clojure.core", (String)"instance?");
        const__9 = RT.keyword(null, (String)"else");
        const__10 = RT.var((String)"datomic.memcached", (String)"fits-in-memcached?");
        const__11 = RT.var((String)"datomic.memcached", (String)"rc-set");
        const__13 = RT.var((String)"datomic.io", (String)"alias-buf-bytes");
        const__14 = RT.var((String)"datomic.memcached", (String)"op-listener");
        const__15 = RT.var((String)"datomic.memcached", (String)"wrap-metrics");
        const__16 = RT.var((String)"datomic.memcached", (String)"put-result-handler");
        const__17 = RT.keyword(null, (String)"MemcacheItemTooLarge");
        const__18 = RT.var((String)"datomic.slf4j", (String)"process");
        const__19 = RT.keyword(null, (String)"event");
        const__20 = RT.keyword((String)"memcached", (String)"item-too-large");
        const__21 = RT.keyword(null, (String)"key");
    }
}

