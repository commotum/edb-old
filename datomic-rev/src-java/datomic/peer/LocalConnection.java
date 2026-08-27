/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFn
 *  clojure.lang.IFn
 *  clojure.lang.ILookupThunk
 *  clojure.lang.IObj
 *  clojure.lang.IPersistentMap
 *  clojure.lang.IPersistentVector
 *  clojure.lang.IType
 *  clojure.lang.Keyword
 *  clojure.lang.KeywordLookupSite
 *  clojure.lang.Numbers
 *  clojure.lang.RT
 *  clojure.lang.Symbol
 *  clojure.lang.Tuple
 *  clojure.lang.Util
 *  clojure.lang.Var
 */
package datomic.peer;

import clojure.lang.AFn;
import clojure.lang.IFn;
import clojure.lang.ILookupThunk;
import clojure.lang.IObj;
import clojure.lang.IPersistentMap;
import clojure.lang.IPersistentVector;
import clojure.lang.IType;
import clojure.lang.Keyword;
import clojure.lang.KeywordLookupSite;
import clojure.lang.Numbers;
import clojure.lang.RT;
import clojure.lang.Symbol;
import clojure.lang.Tuple;
import clojure.lang.Util;
import clojure.lang.Var;
import datomic.Connection;
import datomic.Database;
import datomic.ListenableFuture;
import datomic.Log;
import datomic.peer.LocalConnection$fn__21602;
import datomic.peer.LocalConnection$fn__21604;
import datomic.peer.LocalConnection$reify__21600;
import datomic.peer.TWatcher;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;

public final class LocalConnection
implements Connection,
IType {
    public final Object dbname;
    public final Object db_ref;
    public final Object tx_report_queue;
    public final Object released;
    public final Object tx_watcher;
    private static Class __cached_class__0;
    private static Class __cached_class__1;
    private static Class __cached_class__2;
    private static Class __cached_class__3;
    private static Class __cached_class__4;
    public static final Var const__0;
    public static final Var const__1;
    public static final Var const__2;
    public static final Keyword const__3;
    public static final Var const__4;
    public static final AFn const__10;
    public static final Var const__11;
    public static final Var const__12;
    public static final Var const__13;
    public static final Keyword const__14;
    public static final Keyword const__15;
    public static final Var const__16;
    public static final Var const__19;
    public static final Var const__21;
    public static final Var const__22;
    static final KeywordLookupSite __site__0__;
    static ILookupThunk __thunk__0__;
    static final KeywordLookupSite __site__1__;
    static ILookupThunk __thunk__1__;
    static final KeywordLookupSite __site__2__;
    static ILookupThunk __thunk__2__;

    public LocalConnection(Object object, Object object2, Object object3, Object object4, Object object5) {
        this.dbname = object;
        this.db_ref = object2;
        this.tx_report_queue = object3;
        this.released = object4;
        this.tx_watcher = object5;
    }

    public static IPersistentVector getBasis() {
        return Tuple.create((Object)Symbol.intern(null, (String)"dbname"), (Object)Symbol.intern(null, (String)"db-ref"), (Object)Symbol.intern(null, (String)"tx-report-queue"), (Object)Symbol.intern(null, (String)"released"), (Object)Symbol.intern(null, (String)"tx-watcher"));
    }

    public void gcStorage(Date older_than) {
    }

    public void removeTxReportQueue() {
        LocalConnection this_ = null;
        ((IFn)const__19.getRawRoot()).invoke(this_.tx_report_queue, null);
    }

    public BlockingQueue txReportQueue() {
        Object object;
        Object or__5238__auto__21607;
        Object object2 = or__5238__auto__21607 = ((IFn)const__4.getRawRoot()).invoke(this_.tx_report_queue);
        if (object2 != null && object2 != Boolean.FALSE) {
            object = or__5238__auto__21607;
            or__5238__auto__21607 = null;
        } else {
            LocalConnection this_ = null;
            object = ((IFn)const__22.getRawRoot()).invoke(this_.tx_report_queue, (Object)new LocalConnection$fn__21604());
        }
        return (BlockingQueue)object;
    }

    /*
     * Unable to fully structure code
     */
    public ListenableFuture transactAsync(List txdata) {
        block11: {
            v0 = ((IFn)LocalConnection.const__1.getRawRoot()).invoke(this.released);
            if (v0 == null || v0 == Boolean.FALSE) break block11;
            this = null;
            v1 = ((IFn)LocalConnection.const__2.getRawRoot()).invoke((Object)LocalConnection.const__3, (Object)"The connection has been released.");
            ** GOTO lbl71
        }
        lockee__5436__auto__21608 = this;
        try {
            synchronized (lockee__5436__auto__21608) {
                block13: {
                    block12: {
                        v2 = db = ((IFn)LocalConnection.const__4.getRawRoot()).invoke(this.db_ref);
                        db = null;
                        v3 = txdata;
                        txdata = null;
                        report = ((IFn)new LocalConnection$fn__21602(v2, v3)).invoke();
                        txrq = ((IFn)LocalConnection.const__4.getRawRoot()).invoke(this.tx_report_queue);
                        if (!(report instanceof Throwable)) break block12;
                        break block13;
                    }
                    v4 = (IFn)LocalConnection.const__19.getRawRoot();
                    v5 = LocalConnection.__thunk__1__;
                    v6 = report;
                    v7 = v5.get(v6);
                    if (v5 == v7) {
                        LocalConnection.__thunk__1__ = LocalConnection.__site__1__.fault(v6);
                        v7 = LocalConnection.__thunk__1__.get(v6);
                    }
                    v4.invoke(this.db_ref, v7);
                    v8 = txrq;
                    if (v8 != null && v8 != Boolean.FALSE) {
                        v9 = txrq;
                        txrq = null;
                        v10 = ((Queue)v9).add(report) != false ? Boolean.TRUE : Boolean.FALSE;
                    }
                    if (Util.classOf((Object)(v11 = this.tx_watcher)) == LocalConnection.__cached_class__4) ** GOTO lbl40
                    if (!(v11 instanceof TWatcher)) {
                        v11 = v11;
                        LocalConnection.__cached_class__4 = Util.classOf((Object)v11);
lbl40:
                        // 2 sources

                        v12 = LocalConnection.__thunk__2__;
                        v13 = report;
                        v14 = v12.get(v13);
                        if (v12 == v14) {
                            LocalConnection.__thunk__2__ = LocalConnection.__site__2__.fault(v13);
                            v14 = LocalConnection.__thunk__2__.get(v13);
                        }
                        v15 = LocalConnection.const__21.getRawRoot().invoke(v11, v14);
                    } else {
                        v16 = (TWatcher)v11;
                        v17 = LocalConnection.__thunk__2__;
                        v18 = report;
                        v19 = v17.get(v18);
                        if (v17 == v19) {
                            LocalConnection.__thunk__2__ = LocalConnection.__site__2__.fault(v18);
                            v19 = LocalConnection.__thunk__2__.get(v18);
                        }
                        v15 = v16.release_pending_syncs(v19);
                    }
                }
                v20 = report;
                report = null;
                var6_6 = ((IFn)LocalConnection.const__11.getRawRoot()).invoke(v20);
            }
        }
        finally {
            v21 = lockee__5436__auto__21608;
            lockee__5436__auto__21608 = null;
            // ** MonitorExit[v21] (shouldn't be in output)
        }
        {
            v1 = var6_6;
lbl71:
            // 2 sources

            return (ListenableFuture)v1;
        }
    }

    public ListenableFuture transactAsync(List txdata, Object _) {
        List list = txdata;
        txdata = null;
        return ((Connection)this).transactAsync(list);
    }

    public ListenableFuture transact(List txdata) {
        List list = txdata;
        txdata = null;
        ListenableFuture<Map> listenableFuture = ((Connection)this_).transactAsync(list);
        LocalConnection this_ = null;
        return (ListenableFuture)((IFn)const__16.getRawRoot()).invoke(listenableFuture);
    }

    public ListenableFuture transact(List txdata, Object _) {
        List list = txdata;
        txdata = null;
        return ((Connection)this).transact(list);
    }

    /*
     * Enabled aggressive block sorting
     */
    public ListenableFuture syncExcise(long t) {
        Object object;
        Object object2 = this_.tx_watcher;
        if (Util.classOf((Object)object2) != __cached_class__3) {
            if (object2 instanceof TWatcher) {
                object = ((TWatcher)object2).sync_background_t(const__15, Numbers.num((long)t));
                return (ListenableFuture)object;
            }
            object2 = object2;
            __cached_class__3 = Util.classOf((Object)object2);
        }
        LocalConnection this_ = null;
        object = const__13.getRawRoot().invoke(object2, (Object)const__15, (Object)Numbers.num((long)t));
        return (ListenableFuture)object;
    }

    /*
     * Enabled aggressive block sorting
     */
    public ListenableFuture syncSchema(long t) {
        Object object;
        Object object2 = this_.tx_watcher;
        if (Util.classOf((Object)object2) != __cached_class__2) {
            if (object2 instanceof TWatcher) {
                object = ((TWatcher)object2).sync_background_t(const__14, Numbers.num((long)t));
                return (ListenableFuture)object;
            }
            object2 = object2;
            __cached_class__2 = Util.classOf((Object)object2);
        }
        LocalConnection this_ = null;
        object = const__13.getRawRoot().invoke(object2, (Object)const__14, (Object)Numbers.num((long)t));
        return (ListenableFuture)object;
    }

    /*
     * Enabled aggressive block sorting
     */
    public ListenableFuture syncIndex(long t) {
        Object object;
        Object object2 = this_.tx_watcher;
        if (Util.classOf((Object)object2) != __cached_class__1) {
            if (object2 instanceof TWatcher) {
                object = ((TWatcher)object2).sync_t(Numbers.num((long)t));
                return (ListenableFuture)object;
            }
            object2 = object2;
            __cached_class__1 = Util.classOf((Object)object2);
        }
        LocalConnection this_ = null;
        object = const__12.getRawRoot().invoke(object2, (Object)Numbers.num((long)t));
        return (ListenableFuture)object;
    }

    /*
     * Enabled aggressive block sorting
     */
    public ListenableFuture sync(long t) {
        Object object;
        Object object2 = this_.tx_watcher;
        if (Util.classOf((Object)object2) != __cached_class__0) {
            if (object2 instanceof TWatcher) {
                object = ((TWatcher)object2).sync_t(Numbers.num((long)t));
                return (ListenableFuture)object;
            }
            object2 = object2;
            __cached_class__0 = Util.classOf((Object)object2);
        }
        LocalConnection this_ = null;
        object = const__12.getRawRoot().invoke(object2, (Object)Numbers.num((long)t));
        return (ListenableFuture)object;
    }

    public ListenableFuture sync() {
        Object object;
        LocalConnection this_;
        Object object2 = ((IFn)const__1.getRawRoot()).invoke(this_.released);
        if (object2 != null && object2 != Boolean.FALSE) {
            this_ = null;
            object = ((IFn)const__2.getRawRoot()).invoke((Object)const__3, (Object)"The connection has been released.");
        } else {
            this_ = null;
            object = ((IFn)const__11.getRawRoot()).invoke(((IFn)const__4.getRawRoot()).invoke(this_.db_ref));
        }
        return (ListenableFuture)object;
    }

    public Log log() {
        Object memlog2;
        Object db2 = ((IFn)const__4.getRawRoot()).invoke(this.db_ref);
        ILookupThunk iLookupThunk = __thunk__0__;
        Object object = db2;
        Object object2 = iLookupThunk.get(object);
        if (iLookupThunk == object2) {
            __thunk__0__ = __site__0__.fault(object);
            object2 = __thunk__0__.get(object);
        }
        Object object3 = memlog2 = object2;
        memlog2 = null;
        Object object4 = db2;
        db2 = null;
        return (Log)((IObj)new LocalConnection$reify__21600(null, object3, object4)).withMeta((IPersistentMap)const__10);
    }

    public Database db() {
        Object object;
        LocalConnection this_;
        Object object2 = ((IFn)const__1.getRawRoot()).invoke(this_.released);
        if (object2 != null && object2 != Boolean.FALSE) {
            this_ = null;
            object = ((IFn)const__2.getRawRoot()).invoke((Object)const__3, (Object)"The connection has been released.");
        } else {
            this_ = null;
            object = ((IFn)const__4.getRawRoot()).invoke(this_.db_ref);
        }
        return (Database)object;
    }

    public boolean requestIndex() {
        return Boolean.TRUE;
    }

    public void release() {
        LocalConnection this_ = null;
        ((IFn)const__0.getRawRoot()).invoke(this_.dbname);
    }

    static {
        const__0 = RT.var((String)"datomic.peer", (String)"delete-local-database");
        const__1 = RT.var((String)"clojure.core", (String)"realized?");
        const__2 = RT.var((String)"datomic.error", (String)"state");
        const__3 = RT.keyword((String)"db.error", (String)"connection-released");
        const__4 = RT.var((String)"clojure.core", (String)"deref");
        const__10 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"line"), 536, RT.keyword(null, (String)"column"), 6});
        const__11 = RT.var((String)"datomic.promise", (String)"delivered");
        const__12 = RT.var((String)"datomic.peer", (String)"sync-t");
        const__13 = RT.var((String)"datomic.peer", (String)"sync-background-t");
        const__14 = RT.keyword(null, (String)"schema");
        const__15 = RT.keyword(null, (String)"excise");
        const__16 = RT.var((String)"datomic.peer", (String)"await-tx-result");
        const__19 = RT.var((String)"clojure.core", (String)"reset!");
        const__21 = RT.var((String)"datomic.peer", (String)"release-pending-syncs");
        const__22 = RT.var((String)"clojure.core", (String)"swap!");
        __site__0__ = new KeywordLookupSite(RT.keyword(null, (String)"memlog"));
        __thunk__0__ = __site__0__;
        __site__1__ = new KeywordLookupSite(RT.keyword(null, (String)"db-after"));
        __thunk__1__ = __site__1__;
        __site__2__ = new KeywordLookupSite(RT.keyword(null, (String)"db-after"));
        __thunk__2__ = __site__2__;
    }
}

