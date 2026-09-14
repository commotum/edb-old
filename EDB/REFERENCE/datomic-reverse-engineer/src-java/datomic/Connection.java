/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.RT
 */
package datomic;

import clojure.lang.RT;
import datomic.Database;
import datomic.ListenableFuture;
import datomic.Log;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;

public interface Connection {
    public static final Object DB_BEFORE = RT.keyword(null, (String)"db-before");
    public static final Object DB_AFTER = RT.keyword(null, (String)"db-after");
    public static final Object TX_DATA = RT.keyword(null, (String)"tx-data");
    public static final Object TEMPIDS = RT.keyword(null, (String)"tempids");

    public boolean requestIndex();

    public Database db();

    public Log log();

    public ListenableFuture<Database> sync();

    public ListenableFuture<Database> sync(long var1);

    public ListenableFuture<Database> syncIndex(long var1);

    public ListenableFuture<Database> syncSchema(long var1);

    public ListenableFuture<Database> syncExcise(long var1);

    public ListenableFuture<Map> transact(List var1);

    public ListenableFuture<Map> transact(List var1, Object var2);

    public ListenableFuture<Map> transactAsync(List var1);

    public ListenableFuture<Map> transactAsync(List var1, Object var2);

    public BlockingQueue<Map> txReportQueue();

    public void removeTxReportQueue();

    public void gcStorage(Date var1);

    public void release();
}

