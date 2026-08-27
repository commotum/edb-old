/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.IFn
 *  clojure.lang.IObj
 *  clojure.lang.IPersistentVector
 *  clojure.lang.IType
 *  clojure.lang.RT
 *  clojure.lang.Symbol
 *  clojure.lang.Tuple
 */
package datomic.core2.thread;

import clojure.lang.IFn;
import clojure.lang.IObj;
import clojure.lang.IPersistentVector;
import clojure.lang.IType;
import clojure.lang.RT;
import clojure.lang.Symbol;
import clojure.lang.Tuple;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.AbstractExecutorService;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public final class ObservableThreadPool
implements ExecutorService,
IType {
    public final Object pool;
    public final Object callback;

    public ObservableThreadPool(Object object, Object object2) {
        this.pool = object;
        this.callback = object2;
    }

    public static IPersistentVector getBasis() {
        return Tuple.create((Object)((IObj)Symbol.intern(null, (String)"pool")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), Symbol.intern(null, (String)"ThreadPoolExecutor")})), (Object)Symbol.intern(null, (String)"callback"));
    }

    public Future submit(Runnable task, Object result2) {
        Runnable runnable = task;
        task = null;
        Object object = result2;
        result2 = null;
        Future<Object> result3 = ((AbstractExecutorService)this.pool).submit(runnable, object);
        ((IFn)this.callback).invoke(this.pool);
        Object var3_3 = null;
        return result3;
    }

    public Future submit(Runnable task) {
        Runnable runnable = task;
        task = null;
        Future<?> result2 = ((AbstractExecutorService)this.pool).submit(runnable);
        ((IFn)this.callback).invoke(this.pool);
        Object var2_2 = null;
        return result2;
    }

    public Future submit(Callable task) {
        Callable callable = task;
        task = null;
        Future result2 = ((AbstractExecutorService)this.pool).submit(callable);
        ((IFn)this.callback).invoke(this.pool);
        Object var2_2 = null;
        return result2;
    }

    public List shutdownNow() {
        return ((ThreadPoolExecutor)this.pool).shutdownNow();
    }

    @Override
    public void shutdown() {
        ((ThreadPoolExecutor)this.pool).shutdown();
    }

    @Override
    public boolean isTerminated() {
        ObservableThreadPool this_ = null;
        return ((ThreadPoolExecutor)this_.pool).isTerminated();
    }

    @Override
    public boolean isShutdown() {
        ObservableThreadPool this_ = null;
        return ((ThreadPoolExecutor)this_.pool).isShutdown();
    }

    public Object invokeAny(Collection tasks, long timeout, TimeUnit timeUnit) throws InterruptedException, ExecutionException, TimeoutException {
        Collection collection = tasks;
        tasks = null;
        TimeUnit timeUnit2 = timeUnit;
        timeUnit = null;
        Object result2 = ((AbstractExecutorService)this.pool).invokeAny(collection, timeout, timeUnit2);
        ((IFn)this.callback).invoke(this.pool);
        Object t = result2;
        result2 = null;
        return t;
    }

    public Object invokeAny(Collection tasks) throws InterruptedException, ExecutionException {
        Collection collection = tasks;
        tasks = null;
        Object result2 = ((AbstractExecutorService)this.pool).invokeAny(collection);
        ((IFn)this.callback).invoke(this.pool);
        Object var2_2 = null;
        return result2;
    }

    public List invokeAll(Collection tasks, long timeout, TimeUnit timeUnit) throws InterruptedException {
        Collection collection = tasks;
        tasks = null;
        TimeUnit timeUnit2 = timeUnit;
        timeUnit = null;
        List result2 = ((AbstractExecutorService)this.pool).invokeAll(collection, timeout, timeUnit2);
        ((IFn)this.callback).invoke(this.pool);
        List list = result2;
        result2 = null;
        return list;
    }

    public List invokeAll(Collection tasks) throws InterruptedException {
        Collection collection = tasks;
        tasks = null;
        List result2 = ((AbstractExecutorService)this.pool).invokeAll(collection);
        ((IFn)this.callback).invoke(this.pool);
        Object var2_2 = null;
        return result2;
    }

    @Override
    public void execute(Runnable task) {
        Runnable runnable = task;
        task = null;
        ((ThreadPoolExecutor)this_.pool).execute(runnable);
        ObservableThreadPool this_ = null;
        ((IFn)this_.callback).invoke(this_.pool);
    }

    @Override
    public boolean awaitTermination(long timeout, TimeUnit timeUnit) throws InterruptedException {
        TimeUnit timeUnit2 = timeUnit;
        timeUnit = null;
        ObservableThreadPool this_ = null;
        return ((ThreadPoolExecutor)this_.pool).awaitTermination(timeout, timeUnit2);
    }
}

