/*
 * Decompiled with CFR 0.152.
 */
package datomic.impl;

import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.RunnableFuture;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class PriorityExecutor
extends ThreadPoolExecutor {
    public PriorityExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, new PriorityBlockingQueue<Runnable>());
    }

    @Override
    protected <T> RunnableFuture<T> newTaskFor(Callable<T> tCallable) {
        return new ComparableFutureTask<T>(tCallable);
    }

    static class ComparableFutureTask<V>
    extends FutureTask<V>
    implements Comparable<ComparableFutureTask<V>> {
        Comparable c;

        ComparableFutureTask(Callable<V> vCallable) {
            super(vCallable);
            this.c = (Comparable)((Object)vCallable);
        }

        @Override
        public int compareTo(ComparableFutureTask<V> vComparableFutureTask) {
            return this.c.compareTo(vComparableFutureTask.c);
        }
    }
}

