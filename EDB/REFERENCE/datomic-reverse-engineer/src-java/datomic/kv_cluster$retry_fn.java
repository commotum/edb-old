/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFn
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.Keyword
 *  clojure.lang.Numbers
 *  clojure.lang.RT
 *  clojure.lang.Tuple
 *  clojure.lang.Util
 *  clojure.lang.Var
 *  org.slf4j.LoggerFactory
 */
package datomic;

import clojure.lang.AFn;
import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Keyword;
import clojure.lang.Numbers;
import clojure.lang.RT;
import clojure.lang.Tuple;
import clojure.lang.Util;
import clojure.lang.Var;
import datomic.kv_cluster$retry_fn$fn__10863;
import datomic.kv_store.Retryable;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import org.slf4j.LoggerFactory;

public final class kv_cluster$retry_fn
extends AFunction {
    private static Class __cached_class__0;
    public static final Var const__0;
    public static final Var const__1;
    public static final Var const__2;
    public static final Var const__3;
    public static final Var const__4;
    public static final Var const__5;
    public static final Keyword const__7;
    public static final Keyword const__8;
    public static final Var const__11;
    public static final Var const__12;
    public static final AFn const__15;
    public static final Var const__16;
    public static final Keyword const__17;
    public static final Object const__18;
    public static final Var const__19;
    public static final Var const__20;
    public static final Var const__21;
    public static final Var const__22;
    public static final Var const__23;
    public static final Var const__24;
    public static final Var const__25;
    public static final Var const__26;
    public static final Var const__36;
    public static final Var const__40;
    public static final Var const__42;
    public static final Var const__43;
    public static final Var const__44;

    /*
     * Unable to fully structure code
     */
    public static Object invokeStatic(Object sem, Object metric, Object nested, Object group_ref, Object backoff, Object f) {
        ((IFn)kv_cluster$retry_fn.const__0.getRawRoot()).invoke(((IFn)kv_cluster$retry_fn.const__1.getRawRoot()).invoke((Object)kv_cluster$retry_fn.const__2, ((IFn)kv_cluster$retry_fn.const__3.getRawRoot()).invoke(kv_cluster$retry_fn.const__4.getRawRoot(), sem, metric, (Object)Boolean.TRUE, group_ref, backoff)));
        try {
            v0 = nested;
            nested = null;
            v1 = and__5236__auto__10866 = ((IFn)kv_cluster$retry_fn.const__5.getRawRoot()).invoke(v0);
            if (v1 != null && v1 != Boolean.FALSE) {
                v2 = Util.equiv((Object)kv_cluster$retry_fn.const__7, (Object)backoff) ? Boolean.TRUE : Boolean.FALSE;
            } else {
                v2 = and__5236__auto__10866;
                and__5236__auto__10866 = null;
            }
            sem_QMARK_ = v2;
            G__10858 = backoff;
            switch (Util.hash((Object)G__10858) >> 3 & 1) {
                case 0: {
                    if (G__10858 == kv_cluster$retry_fn.const__8) {
                        v3 = 20L;
                        break;
                    }
                    ** GOTO lbl24
                }
                case 1: {
                    if (G__10858 == kv_cluster$retry_fn.const__7) {
                        v3 = 9L;
                        break;
                    }
                }
lbl24:
                // 4 sources

                default: {
                    v4 = G__10858;
                    G__10858 = null;
                    throw (Throwable)new IllegalArgumentException((String)((IFn)kv_cluster$retry_fn.const__11.getRawRoot()).invoke((Object)"No matching clause: ", v4));
                }
            }
            max_retries = v3;
            tid = Thread.currentThread().getId();
            v5 = sem_QMARK_;
            sem_QMARK_ = null;
            if (v5 != null && v5 != Boolean.FALSE) {
                permit_QMARK_ = ((Semaphore)sem).tryAcquire(20L, TimeUnit.SECONDS);
                if (permit_QMARK_) {
                } else {
                    logger = LoggerFactory.getLogger((String)"datomic.kv-cluster");
                    if (logger.isInfoEnabled()) {
                        v6 = logger;
                        logger = null;
                        v6.info((String)((IFn)kv_cluster$retry_fn.const__12.getRawRoot()).invoke((Object)kv_cluster$retry_fn.const__15));
                    }
                    ((IFn)kv_cluster$retry_fn.const__16.getRawRoot()).invoke((Object)kv_cluster$retry_fn.const__17);
                }
                v7 = permit_QMARK_ ? Boolean.TRUE : Boolean.FALSE;
            } else {
                v7 = null;
            }
            permit_QMARK_ = v7;
            try {
                retries = 0L;
                elapsed = 0L;
                while (true) {
                    block37: {
                        block36: {
                            if (Util.equiv((Object)kv_cluster$retry_fn.const__7, (Object)backoff)) {
                                ((IFn)kv_cluster$retry_fn.const__19.getRawRoot()).invoke(group_ref, kv_cluster$retry_fn.const__20.getRawRoot(), (Object)Numbers.num((long)tid), (Object)Numbers.num((long)retries));
                            }
                            G__10862 = backoff;
                            switch (Util.hash((Object)G__10862) >> 3 & 1) {
                                case 0: {
                                    if (G__10862 == kv_cluster$retry_fn.const__8) {
                                        v8 = ((IFn)kv_cluster$retry_fn.const__21.getRawRoot()).invoke((Object)Numbers.num((long)retries));
                                        break;
                                    }
                                    ** GOTO lbl67
                                }
                                case 1: {
                                    if (G__10862 == kv_cluster$retry_fn.const__7) {
                                        v8 = ((IFn)kv_cluster$retry_fn.const__22.getRawRoot()).invoke(((IFn)kv_cluster$retry_fn.const__23.getRawRoot()).invoke(kv_cluster$retry_fn.const__24.getRawRoot(), kv_cluster$retry_fn.const__18, ((IFn)kv_cluster$retry_fn.const__25.getRawRoot()).invoke(((IFn)kv_cluster$retry_fn.const__26.getRawRoot()).invoke(group_ref))));
                                        break;
                                    }
                                }
lbl67:
                                // 4 sources

                                default: {
                                    v9 = G__10862;
                                    G__10862 = null;
                                    throw (Throwable)new IllegalArgumentException((String)((IFn)kv_cluster$retry_fn.const__11.getRawRoot()).invoke((Object)"No matching clause: ", v9));
                                }
                            }
                            delay = v8;
                            if (Numbers.isPos((Object)delay)) {
                                Thread.sleep(RT.longCast((Object)((Number)delay)));
                                v10 = null;
                            } else {
                                v10 = null;
                            }
                            start = System.nanoTime();
                            v11 = result = ((IFn)new kv_cluster$retry_fn$fn__10863(f)).invoke();
                            result = null;
                            vec__10859 = Tuple.create((Object)Numbers.num((long)Numbers.quotient((long)Numbers.minus((long)System.nanoTime(), (long)start), (long)1000000L)), (Object)v11);
                            ms = RT.nth((Object)vec__10859, (int)RT.intCast((long)0L), null);
                            v12 = vec__10859;
                            vec__10859 = null;
                            result = RT.nth((Object)v12, (int)RT.intCast((long)1L), null);
                            v13 = ms;
                            ms = null;
                            elapsed = Numbers.add((long)elapsed, (long)RT.longCast((Object)((Number)v13)));
                            and__5236__auto__10870 = result instanceof Throwable;
                            if (!and__5236__auto__10870) break block36;
                            v14 = result;
                            if (Util.classOf((Object)v14) == kv_cluster$retry_fn.__cached_class__0) ** GOTO lbl95
                            if (!(v14 instanceof Retryable)) {
                                v14 = v14;
                                kv_cluster$retry_fn.__cached_class__0 = Util.classOf((Object)v14);
lbl95:
                                // 2 sources

                                v15 = kv_cluster$retry_fn.const__36.getRawRoot().invoke(v14);
                            } else {
                                v15 = ((Retryable)v14).retryable_QMARK_();
                            }
                            v16 = and__5236__auto__10869 = v15;
                            if (v16 != null && v16 != Boolean.FALSE) {
                                and__5236__auto__10868 = Numbers.lt((long)retries, (long)max_retries);
                                v17 = and__5236__auto__10868 ? ((or__5238__auto__10867 = Numbers.lt((long)retries, (long)3L)) ? (or__5238__auto__10867 ? Boolean.TRUE : Boolean.FALSE) : (Numbers.lt((long)elapsed, (long)10000L) ? Boolean.TRUE : Boolean.FALSE)) : (and__5236__auto__10868 ? Boolean.TRUE : Boolean.FALSE);
                            } else {
                                v17 = and__5236__auto__10869;
                                and__5236__auto__10869 = null;
                            }
                            break block37;
                        }
                        v17 = and__5236__auto__10870 != false ? Boolean.TRUE : Boolean.FALSE;
                    }
                    if (v17 == null || v17 == Boolean.FALSE) break;
                    v18 = result;
                    result = null;
                    v19 = delay;
                    delay = null;
                    ((IFn)kv_cluster$retry_fn.const__40.getRawRoot()).invoke(v18, metric, v19, (Object)Numbers.num((long)retries), (Object)Numbers.num((long)max_retries));
                    elapsed = elapsed;
                    retries = Numbers.inc((long)retries);
                }
                v20 = result;
                result = null;
                var13_12 = ((IFn)kv_cluster$retry_fn.const__42.getRawRoot()).invoke(v20);
            }
            finally {
                v21 = permit_QMARK_;
                permit_QMARK_ = null;
                if (v21 != null && v21 != Boolean.FALSE) {
                    v22 = sem;
                    sem = null;
                    ((Semaphore)v22).release();
                }
                v23 = group_ref;
                group_ref = null;
                ((IFn)kv_cluster$retry_fn.const__19.getRawRoot()).invoke(v23, kv_cluster$retry_fn.const__43.getRawRoot(), (Object)Numbers.num((long)tid));
            }
            var32_26 = var13_12;
        }
        finally {
            ((IFn)kv_cluster$retry_fn.const__44.getRawRoot()).invoke();
        }
        return var32_26;
    }

    public Object invoke(Object object, Object object2, Object object3, Object object4, Object object5, Object object6) {
        Object object7 = object;
        object = null;
        Object object8 = object2;
        object2 = null;
        Object object9 = object3;
        object3 = null;
        Object object10 = object4;
        object4 = null;
        Object object11 = object5;
        object5 = null;
        Object object12 = object6;
        object6 = null;
        return kv_cluster$retry_fn.invokeStatic(object7, object8, object9, object10, object11, object12);
    }

    static {
        const__0 = RT.var((String)"clojure.core", (String)"push-thread-bindings");
        const__1 = RT.var((String)"clojure.core", (String)"hash-map");
        const__2 = RT.var((String)"datomic.kv-store", (String)"*retry*");
        const__3 = RT.var((String)"clojure.core", (String)"partial");
        const__4 = RT.var((String)"datomic.kv-cluster", (String)"retry-fn");
        const__5 = RT.var((String)"clojure.core", (String)"not");
        const__7 = RT.keyword(null, (String)"exponential");
        const__8 = RT.keyword(null, (String)"linear");
        const__11 = RT.var((String)"clojure.core", (String)"str");
        const__12 = RT.var((String)"datomic.slf4j", (String)"process");
        const__15 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"event"), RT.keyword((String)"kv-cluster", (String)"semaphore-timeout")});
        const__16 = RT.var((String)"datomic.monitor", (String)"alarm");
        const__17 = RT.keyword(null, (String)"StorageSemaphoreTimeout");
        const__18 = 0L;
        const__19 = RT.var((String)"clojure.core", (String)"swap!");
        const__20 = RT.var((String)"clojure.core", (String)"assoc");
        const__21 = RT.var((String)"datomic.kv-cluster", (String)"linear-backoff");
        const__22 = RT.var((String)"datomic.kv-cluster", (String)"exponential-backoff");
        const__23 = RT.var((String)"clojure.core", (String)"apply");
        const__24 = RT.var((String)"clojure.core", (String)"max");
        const__25 = RT.var((String)"clojure.core", (String)"vals");
        const__26 = RT.var((String)"clojure.core", (String)"deref");
        const__36 = RT.var((String)"datomic.kv-store", (String)"retryable?");
        const__40 = RT.var((String)"datomic.kv-cluster", (String)"notify-retry");
        const__42 = RT.var((String)"datomic.common", (String)"return-or-throw");
        const__43 = RT.var((String)"clojure.core", (String)"dissoc");
        const__44 = RT.var((String)"clojure.core", (String)"pop-thread-bindings");
    }
}

