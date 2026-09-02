package datomic;

import java.util.concurrent.Executor;
import java.util.concurrent.Future;

/**
 * A future that supports completion listeners.
 *
 * @param <T> result type
 */
public interface ListenableFuture<T>
extends Future<T> {
    /**
     * Registers a listener that runs exactly once when this future completes.
     * A listener registered after completion is scheduled immediately.
     * Listener ordering is unspecified.
     *
     * @param listener work to run after completion
     * @param executor executor on which the listener runs
     */
    public void addListener(Runnable listener, Executor executor);
}
