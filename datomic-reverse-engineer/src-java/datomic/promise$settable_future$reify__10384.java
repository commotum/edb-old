/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.IBlockingDeref
 *  clojure.lang.IDeref
 *  clojure.lang.IFn
 *  clojure.lang.IObj
 *  clojure.lang.IPending
 *  clojure.lang.IPersistentMap
 *  clojure.lang.Indexed
 *  clojure.lang.Keyword
 *  clojure.lang.Numbers
 *  clojure.lang.RT
 *  clojure.lang.Util
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.IBlockingDeref;
import clojure.lang.IDeref;
import clojure.lang.IFn;
import clojure.lang.IObj;
import clojure.lang.IPending;
import clojure.lang.IPersistentMap;
import clojure.lang.Indexed;
import clojure.lang.Keyword;
import clojure.lang.Numbers;
import clojure.lang.RT;
import clojure.lang.Util;
import clojure.lang.Var;
import datomic.ListenableFuture;
import datomic.promise$settable_future$reify__10384$fn__10385;
import datomic.promise$settable_future$reify__10384$fn__10387;
import datomic.promise$settable_future$reify__10384$fn__10399;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public final class promise$settable_future$reify__10384
implements IPending,
ListenableFuture,
IBlockingDeref,
Future,
IDeref,
IFn,
IObj {
    final IPersistentMap __meta;
    Object v;
    Object listeners;
    Object d;
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"str");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"push-thread-bindings");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"hash-map");
    public static final Var const__3 = RT.var((String)"clojure.core", (String)"*print-length*");
    public static final Object const__4 = 5L;
    public static final Var const__5 = RT.var((String)"clojure.core", (String)"*print-level*");
    public static final Object const__6 = 3L;
    public static final Keyword const__7 = RT.keyword(null, (String)"pending");
    public static final Var const__9 = RT.var((String)"clojure.core", (String)"deliver");
    public static final Var const__10 = RT.var((String)"clojure.core", (String)"realized?");
    public static final Var const__13 = RT.var((String)"clojure.core", (String)"deref");
    public static final Var const__15 = RT.var((String)"datomic.promise", (String)"throw-executionexception-if-throwable");
    public static final Var const__18 = RT.var((String)"clojure.core", (String)"compare-and-set!");
    public static final Var const__19 = RT.var((String)"clojure.core", (String)"seq");
    public static final Var const__24 = RT.var((String)"datomic.promise", (String)"call-user-code");
    public static final Var const__26 = RT.var((String)"clojure.core", (String)"chunked-seq?");
    public static final Var const__27 = RT.var((String)"clojure.core", (String)"chunk-first");
    public static final Var const__28 = RT.var((String)"clojure.core", (String)"chunk-rest");
    public static final Var const__31 = RT.var((String)"clojure.core", (String)"first");
    public static final Var const__32 = RT.var((String)"clojure.core", (String)"next");

    public promise$settable_future$reify__10384(IPersistentMap iPersistentMap, Object object, Object object2, Object object3) {
        this.__meta = iPersistentMap;
        this.v = object;
        this.listeners = object2;
        this.d = object3;
    }

    public promise$settable_future$reify__10384(Object object, Object object2, Object object3) {
        this(null, object, object2, object3);
    }

    public IPersistentMap meta() {
        return this.__meta;
    }

    public IObj withMeta(IPersistentMap iPersistentMap) {
        return new promise$settable_future$reify__10384(iPersistentMap, this.v, this.listeners, this.d);
    }

    public void addListener(Runnable listener, Executor exec) {
        Object object;
        Object execute_now;
        Object lockee__5436__auto__10402;
        Object object2 = lockee__5436__auto__10402 = this_.listeners;
        lockee__5436__auto__10402 = null;
        Object object3 = execute_now = ((IFn)new promise$settable_future$reify__10384$fn__10399(this_, exec, listener, object2, this_.listeners)).invoke();
        execute_now = null;
        if (object3 != null && object3 != Boolean.FALSE) {
            Executor executor = exec;
            exec = null;
            Runnable runnable = listener;
            listener = null;
            promise$settable_future$reify__10384 this_ = null;
            object = ((IFn)const__24.getRawRoot()).invoke((Object)executor, (Object)runnable);
        } else {
            object = null;
        }
    }

    public Object invoke(Object x) {
        promise$settable_future$reify__10384 promise$settable_future$reify__10384;
        Object object;
        boolean and__5236__auto__10403 = Numbers.isPos((long)((CountDownLatch)this.d).getCount());
        if (and__5236__auto__10403) {
            Object object2 = x;
            x = null;
            object = ((IFn)const__18.getRawRoot()).invoke(this.v, this.d, object2);
        } else {
            object = and__5236__auto__10403 ? Boolean.TRUE : Boolean.FALSE;
        }
        if (object != null && object != Boolean.FALSE) {
            Object lockee__5436__auto__10404;
            Object object3 = lockee__5436__auto__10404 = this.listeners;
            lockee__5436__auto__10404 = null;
            ((IFn)new promise$settable_future$reify__10384$fn__10387(object3, this.d)).invoke();
            Object seq_10389 = ((IFn)const__19.getRawRoot()).invoke(((IFn)const__13.getRawRoot()).invoke(this.listeners));
            Object chunk_10390 = null;
            long count_10391 = 0L;
            long i_10392 = 0L;
            while (true) {
                Object exec;
                Object temp__5457__auto__10406;
                if (i_10392 < count_10391) {
                    Object exec2;
                    Object vec__10393 = ((Indexed)chunk_10390).nth(RT.intCast((long)i_10392));
                    Object listener = RT.nth((Object)vec__10393, (int)RT.intCast((long)0L), null);
                    Object object4 = vec__10393;
                    vec__10393 = null;
                    Object object5 = exec2 = RT.nth((Object)object4, (int)RT.intCast((long)1L), null);
                    exec2 = null;
                    Object object6 = listener;
                    listener = null;
                    ((IFn)const__24.getRawRoot()).invoke(object5, object6);
                    Object object7 = seq_10389;
                    seq_10389 = null;
                    Object object8 = chunk_10390;
                    chunk_10390 = null;
                    ++i_10392;
                    chunk_10390 = object8;
                    seq_10389 = object7;
                    continue;
                }
                Object object9 = seq_10389;
                seq_10389 = null;
                Object object10 = temp__5457__auto__10406 = ((IFn)const__19.getRawRoot()).invoke(object9);
                if (object10 == null || object10 == Boolean.FALSE) break;
                Object object11 = temp__5457__auto__10406;
                temp__5457__auto__10406 = null;
                Object seq_103892 = object11;
                Object object12 = ((IFn)const__26.getRawRoot()).invoke(seq_103892);
                if (object12 != null && object12 != Boolean.FALSE) {
                    Object c__5719__auto__10405 = ((IFn)const__27.getRawRoot()).invoke(seq_103892);
                    Object object13 = seq_103892;
                    seq_103892 = null;
                    Object object14 = c__5719__auto__10405;
                    Object object15 = c__5719__auto__10405;
                    c__5719__auto__10405 = null;
                    i_10392 = RT.intCast((long)0L);
                    count_10391 = RT.intCast((int)RT.count((Object)object15));
                    chunk_10390 = object14;
                    seq_10389 = ((IFn)const__28.getRawRoot()).invoke(object13);
                    continue;
                }
                Object vec__10396 = ((IFn)const__31.getRawRoot()).invoke(seq_103892);
                Object listener = RT.nth((Object)vec__10396, (int)RT.intCast((long)0L), null);
                Object object16 = vec__10396;
                vec__10396 = null;
                Object object17 = exec = RT.nth((Object)object16, (int)RT.intCast((long)1L), null);
                exec = null;
                Object object18 = listener;
                listener = null;
                ((IFn)const__24.getRawRoot()).invoke(object17, object18);
                Object object19 = seq_103892;
                seq_103892 = null;
                i_10392 = 0L;
                count_10391 = 0L;
                chunk_10390 = null;
                seq_10389 = ((IFn)const__32.getRawRoot()).invoke(object19);
            }
            promise$settable_future$reify__10384 = this;
        } else {
            promise$settable_future$reify__10384 = null;
        }
        return promise$settable_future$reify__10384;
    }

    public boolean isRealized() {
        return Numbers.isZero((long)((CountDownLatch)this.d).getCount());
    }

    public Object deref(long timeout_ms, Object object) {
        Object object2;
        if (((CountDownLatch)this_.d).await(timeout_ms, TimeUnit.MILLISECONDS)) {
            promise$settable_future$reify__10384 this_ = null;
            object2 = ((IFn)const__15.getRawRoot()).invoke(((IFn)const__13.getRawRoot()).invoke(this_.v));
        } else {
            object2 = object;
            object = null;
        }
        return object2;
    }

    public Object deref() {
        ((CountDownLatch)this_.d).await();
        promise$settable_future$reify__10384 this_ = null;
        return ((IFn)const__15.getRawRoot()).invoke(((IFn)const__13.getRawRoot()).invoke(this_.v));
    }

    public Object get(long timeout, TimeUnit timeUnit) throws InterruptedException, ExecutionException, TimeoutException {
        TimeUnit timeUnit2 = timeUnit;
        timeUnit = null;
        Object result2 = ((IBlockingDeref)this_).deref(TimeUnit.MILLISECONDS.convert(timeout, timeUnit2), (Object)this_);
        if (Util.equiv((Object)result2, (Object)this_)) {
            throw (Throwable)new TimeoutException();
        }
        Object object = result2;
        result2 = null;
        promise$settable_future$reify__10384 this_ = null;
        return ((IFn)const__15.getRawRoot()).invoke(object);
    }

    public Object get() throws InterruptedException, ExecutionException {
        return ((IDeref)this).deref();
    }

    public boolean isDone() {
        promise$settable_future$reify__10384 promise$settable_future$reify__10384 = this_;
        promise$settable_future$reify__10384 this_ = null;
        return (Boolean)((IFn)const__10.getRawRoot()).invoke((Object)promise$settable_future$reify__10384);
    }

    public boolean isCancelled() {
        Object object;
        Object and__5236__auto__10407;
        Object object2 = and__5236__auto__10407 = ((IFn)const__10.getRawRoot()).invoke((Object)this);
        if (object2 != null && object2 != Boolean.FALSE) {
            object = ((IFn)const__13.getRawRoot()).invoke(this.v) instanceof CancellationException ? Boolean.TRUE : Boolean.FALSE;
        } else {
            object = and__5236__auto__10407;
            Object var1_1 = null;
        }
        return (Boolean)object;
    }

    public boolean cancel(boolean may_interrupt) {
        return RT.booleanCast((Object)((IFn)const__9.getRawRoot()).invoke((Object)this, (Object)new CancellationException()));
    }

    public String toString() {
        Object object;
        IFn iFn = (IFn)const__0.getRawRoot();
        if (((IPending)this_).isRealized()) {
            ((IFn)const__1.getRawRoot()).invoke(((IFn)const__2.getRawRoot()).invoke((Object)const__3, const__4, (Object)const__5, const__6));
            object = ((IFn)new promise$settable_future$reify__10384$fn__10385(this_.v)).invoke();
        } else {
            object = const__7;
        }
        promise$settable_future$reify__10384 this_ = null;
        return (String)iFn.invoke((Object)"#<Future: ", object, (Object)">");
    }
}

