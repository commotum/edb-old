/*
 * Decompiled with CFR 0.152.
 */
package datomic;

import java.util.concurrent.Executor;
import java.util.concurrent.Future;

public interface ListenableFuture<T>
extends Future<T> {
    public void addListener(Runnable var1, Executor var2);
}

