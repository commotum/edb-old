/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFn
 *  clojure.lang.IFn
 *  clojure.lang.IObj
 *  clojure.lang.IPersistentVector
 *  clojure.lang.IType
 *  clojure.lang.RT
 *  clojure.lang.Symbol
 *  clojure.lang.Tuple
 *  clojure.lang.Util
 *  clojure.lang.Var
 *  datomic.spy.memcached.MemcachedClient
 *  datomic.spy.memcached.internal.OperationCompletionListener
 *  datomic.spy.memcached.internal.OperationFuture
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 */
package datomic.memcached;

import clojure.lang.AFn;
import clojure.lang.IFn;
import clojure.lang.IObj;
import clojure.lang.IPersistentVector;
import clojure.lang.IType;
import clojure.lang.RT;
import clojure.lang.Symbol;
import clojure.lang.Tuple;
import clojure.lang.Util;
import clojure.lang.Var;
import datomic.memcached.RecoveringClient$fn__10025;
import datomic.memcached.RecoveringClient$fn__10028;
import datomic.memcached.RecoveringClientImpl;
import datomic.spy.memcached.MemcachedClient;
import datomic.spy.memcached.internal.OperationCompletionListener;
import datomic.spy.memcached.internal.OperationFuture;
import java.util.concurrent.Semaphore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class RecoveringClient
implements RecoveringClientImpl,
IType {
    public final Object client_ref;
    public final Object create_client;
    public final Object sem;
    private static Class __cached_class__0;
    public static final Var const__0;
    public static final Var const__1;
    public static final Var const__2;
    public static final Var const__3;
    public static final AFn const__6;
    public static final Var const__7;
    public static final Var const__9;

    public RecoveringClient(Object object, Object object2, Object object3) {
        this.client_ref = object;
        this.create_client = object2;
        this.sem = object3;
    }

    public static IPersistentVector getBasis() {
        return Tuple.create((Object)Symbol.intern(null, (String)"client-ref"), (Object)Symbol.intern(null, (String)"create-client"), (Object)((IObj)Symbol.intern(null, (String)"sem")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), Symbol.intern(null, (String)"Semaphore")})));
    }

    public Object rc_set(Object k, Object ttl, Object v) {
        Object object = k;
        k = null;
        Object object2 = ttl;
        ttl = null;
        Object object3 = v;
        v = null;
        OperationFuture G__10027 = ((MemcachedClient)((IFn)const__0.getRawRoot()).invoke(this.client_ref)).set((String)object, RT.intCast((long)RT.longCast((Object)object2)), object3);
        G__10027.addListener((OperationCompletionListener)((IFn)const__9.getRawRoot()).invoke((Object)new RecoveringClient$fn__10028(this)));
        OperationFuture operationFuture = G__10027;
        G__10027 = null;
        return operationFuture;
    }

    public Object rc_reset_if_crashed() {
        Object object;
        Object object2 = ((IFn)const__2.getRawRoot()).invoke((Object)(((Thread)((MemcachedClient)((IFn)const__0.getRawRoot()).invoke(this_.client_ref)).getConnection()).isAlive() ? Boolean.TRUE : Boolean.FALSE));
        if (object2 != null && object2 != Boolean.FALSE) {
            if (((Semaphore)this_.sem).tryAcquire()) {
                Logger logger = LoggerFactory.getLogger((String)"datomic.memcached");
                if (logger.isWarnEnabled()) {
                    Logger logger2 = logger;
                    logger = null;
                    logger2.warn((String)((IFn)const__3.getRawRoot()).invoke((Object)const__6));
                }
                RecoveringClient this_ = null;
                object = ((IFn)const__7.getRawRoot()).invoke((Object)new RecoveringClient$fn__10025(this_.create_client, this_.client_ref, this_.sem));
            } else {
                object = null;
            }
        } else {
            object = null;
        }
        return object;
    }

    /*
     * Unable to fully structure code
     */
    public Object rc_get(Object k) {
        try {
            v0 = k;
            k = null;
            var2_2 = ((MemcachedClient)((IFn)RecoveringClient.const__0.getRawRoot()).invoke(this.client_ref)).get((String)v0);
        }
        catch (Throwable t) {
            v1 = this;
            if (Util.classOf((Object)v1) == RecoveringClient.__cached_class__0) ** GOTO lbl12
            if (!(v1 instanceof RecoveringClientImpl)) {
                v1 = v1;
                RecoveringClient.__cached_class__0 = Util.classOf((Object)v1);
lbl12:
                // 2 sources

                v2 = RecoveringClient.const__1.getRawRoot().invoke((Object)v1);
            } else {
                v2 = ((RecoveringClientImpl)v1).rc_reset_if_crashed();
            }
            t = null;
            throw t;
        }
        return var2_2;
    }

    public Object rc_shutdown() {
        Object var1_1;
        try {
            ((Semaphore)this.sem).acquire();
            ((MemcachedClient)((IFn)const__0.getRawRoot()).invoke(this.client_ref)).shutdown();
            var1_1 = null;
        }
        finally {
            ((Semaphore)this.sem).release();
        }
        return var1_1;
    }

    static {
        const__0 = RT.var((String)"clojure.core", (String)"deref");
        const__1 = RT.var((String)"datomic.memcached", (String)"rc-reset-if-crashed");
        const__2 = RT.var((String)"clojure.core", (String)"not");
        const__3 = RT.var((String)"datomic.slf4j", (String)"process");
        const__6 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"event"), RT.keyword((String)"datomic.memcached", (String)"reset-spy-client")});
        const__7 = RT.var((String)"clojure.core", (String)"future-call");
        const__9 = RT.var((String)"datomic.memcached", (String)"op-listener");
    }
}

