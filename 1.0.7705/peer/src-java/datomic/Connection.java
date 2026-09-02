package datomic;

import clojure.lang.RT;
import datomic.Database;
import datomic.ListenableFuture;
import datomic.Log;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;

/**
 * A thread-safe, long-lived connection to a database.
 *
 * <p>A connection submits and monitors transactions and supplies current
 * immutable database and log values. Live connections are cached by URI and
 * intended to be shared throughout a process. Read-only and backup connections
 * represent independent snapshots and are not cached.</p>
 */
public interface Connection {
    /** Transaction report key containing the database before the transaction. */
    public static final Object DB_BEFORE = RT.keyword(null, (String)"db-before");
    /** Transaction report key containing the database after the transaction. */
    public static final Object DB_AFTER = RT.keyword(null, (String)"db-after");
    /** Transaction report key containing datoms produced by the transaction. */
    public static final Object TX_DATA = RT.keyword(null, (String)"tx-data");
    /** Transaction report key containing temporary-id resolutions. */
    public static final Object TEMPIDS = RT.keyword(null, (String)"tempids");

    /**
     * Requests that a background indexing job begin asynchronously.
     *
     * @return {@code true} when the job was successfully scheduled
     */
    public boolean requestIndex();

    /**
     * Returns the current immutable database value without blocking or
     * communicating with the transactor.
     *
     * @return the current immutable database value
     */
    public Database db();

    /**
     * Returns the current immutable transaction log value without blocking or
     * communicating with the transactor.
     *
     * @return the current immutable transaction log value
     */
    public Log log();

    /**
     * Returns a future database value containing every transaction completed
     * when this method was called.
     *
     * <p>This operation coordinates with the transactor and may take an
     * arbitrary amount of time. Callers waiting on the future should supply a
     * timeout.</p>
     *
     * @return a future database value current through all transactions completed at call time
     */
    public ListenableFuture<Database> sync();

    /**
     * Returns a future database value containing transactions through
     * {@code t}, inclusive.
     *
     * <p>This operation does not communicate with the transactor. It waits for
     * the peer to observe the requested basis and may take an arbitrary amount
     * of time, so callers waiting on the future should supply a timeout.</p>
     *
     * @param t database t through which the result must be current
     * @return a future database value current through {@code t}
     */
    public ListenableFuture<Database> sync(long t);

    /**
     * Returns a future database value whose indexes include transactions
     * through {@code t}.
     *
     * <p>This operation does not communicate with the transactor and may take
     * an arbitrary amount of time. Callers waiting on the future should supply
     * a timeout.</p>
     *
     * @param t database t through which indexes must be current
     * @return a future database value whose indexes include {@code t}
     */
    public ListenableFuture<Database> syncIndex(long t);

    /**
     * Returns a future database value aware of schema changes through
     * {@code t}.
     *
     * <p>This operation does not communicate with the transactor and may take
     * an arbitrary amount of time. Callers waiting on the future should supply
     * a timeout.</p>
     *
     * @param t database t through which schema changes must be visible
     * @return a future database value aware of schema changes through {@code t}
     */
    public ListenableFuture<Database> syncSchema(long t);

    /**
     * Returns a future database value aware of excisions through {@code t}.
     *
     * <p>This operation does not communicate with the transactor and may take
     * an arbitrary amount of time. Callers waiting on the future should supply
     * a timeout.</p>
     *
     * @param t database t through which excisions must be visible
     * @return a future database value aware of excisions through {@code t}
     */
    public ListenableFuture<Database> syncExcise(long t);

    /**
     * Submits transaction data and waits until the transaction result is
     * available.
     *
     * <p>The completed future contains {@link #DB_BEFORE}, {@link #DB_AFTER},
     * {@link #TX_DATA}, and {@link #TEMPIDS}. A failed transaction is reported
     * by an {@link java.util.concurrent.ExecutionException} when the future is
     * read. A transaction timeout throws a {@link RuntimeException} from this
     * method. The {@code datomic.txTimeoutMsec} system property controls that
     * timeout and defaults to 10,000 milliseconds.</p>
     *
     * @param txData assertions, retractions, transaction-function calls, and
     *               entity maps to transact
     * @return a completed future containing the transaction report
     * @throws RuntimeException when no transaction result is available before
     *                          the configured timeout
     */
    public ListenableFuture<Map> transact(List txData);

    /**
     * Submits transaction data with request options and waits until the result
     * is available.
     *
     * @param txData transaction data to submit
     * @param options transaction request options
     * @return a completed future containing the transaction report
     */
    public ListenableFuture<Map> transact(List txData, Object options);

    /**
     * Submits transaction data and returns immediately.
     * Timeout policy is left to the caller waiting on the future.
     *
     * @param txData transaction data to submit
     * @return a future containing the transaction report
     */
    public ListenableFuture<Map> transactAsync(List txData);

    /**
     * Submits transaction data with request options and returns immediately.
     * Timeout policy is left to the caller waiting on the future.
     *
     * @param txData transaction data to submit
     * @param options transaction request options
     * @return a future containing the transaction report
     */
    public ListenableFuture<Map> transactAsync(List txData, Object options);

    /**
     * Returns the single transaction-report queue for this connection,
     * creating it when necessary.
     *
     * <p>The queue receives reports for every transaction in the system and
     * may be consumed by multiple threads. Producers never block, so consumers
     * must drain the queue to bound its memory use. A transaction submitted by
     * this connection completes its future before its report enters the
     * queue.</p>
     *
     * @return the connection's transaction-report queue
     */
    public BlockingQueue<Map> txReportQueue();

    /**
     * Removes the transaction-report queue associated with this connection.
     */
    public void removeTxReportQueue();

    /**
     * Reclaims storage garbage older than the supplied date.
     * Storage garbage collection should be scheduled regularly as part of
     * system capacity management.
     *
     * @param olderThan newest age eligible for garbage collection
     */
    public void gcStorage(Date olderThan);

    /**
     * Requests asynchronous release of resources associated with this
     * connection.
     *
     * <p>Call this only when the entire process has finished using the shared
     * connection.</p>
     */
    public void release();
}
