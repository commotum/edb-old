/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFn
 *  clojure.lang.IFn
 *  clojure.lang.ILookupThunk
 *  clojure.lang.IPersistentMap
 *  clojure.lang.IPersistentVector
 *  clojure.lang.ISeq
 *  clojure.lang.IType
 *  clojure.lang.Keyword
 *  clojure.lang.KeywordLookupSite
 *  clojure.lang.Numbers
 *  clojure.lang.PersistentHashMap
 *  clojure.lang.RT
 *  clojure.lang.Symbol
 *  clojure.lang.Tuple
 *  clojure.lang.Util
 *  clojure.lang.Var
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 */
package datomic.peer;

import clojure.lang.AFn;
import clojure.lang.IFn;
import clojure.lang.ILookupThunk;
import clojure.lang.IPersistentMap;
import clojure.lang.IPersistentVector;
import clojure.lang.ISeq;
import clojure.lang.IType;
import clojure.lang.Keyword;
import clojure.lang.KeywordLookupSite;
import clojure.lang.Numbers;
import clojure.lang.PersistentHashMap;
import clojure.lang.RT;
import clojure.lang.Symbol;
import clojure.lang.Tuple;
import clojure.lang.Util;
import clojure.lang.Var;
import datomic.Database;
import datomic.ListenableFuture;
import datomic.Log;
import datomic.common.AsyncShutdown;
import datomic.connector.NotificationHandler;
import datomic.monitor.Metrics;
import datomic.peer.Connection$fn__21505;
import datomic.peer.Connection$fn__21537;
import datomic.peer.Connection$fn__21545;
import datomic.peer.Connection$fn__21552;
import datomic.peer.Connection$fn__21555;
import datomic.peer.RemoteConnection;
import datomic.peer.TWatcher;
import datomic.queue.BlockingProducer;
import datomic.reconnector2.Reconnectable;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class Connection
implements RemoteConnection,
Reconnectable,
Metrics,
datomic.Connection,
AsyncShutdown,
NotificationHandler,
IType {
    public final Object db_id;
    public final Object cluster;
    public final Object olookup;
    public final Object state_ref;
    public final Object db_ref;
    public final Object pending_txes;
    public final Object unsent_updates_queue;
    public final Object lucene_queue;
    public final Object tx_report_queue;
    public final Object tx_watcher;
    public final Object idx_watcher;
    public final Object bg_watcher;
    private static Class __cached_class__0;
    private static Class __cached_class__1;
    private static Class __cached_class__2;
    private static Class __cached_class__3;
    private static Class __cached_class__4;
    private static Class __cached_class__5;
    private static Class __cached_class__6;
    private static Class __cached_class__7;
    private static Class __cached_class__8;
    private static Class __cached_class__9;
    private static Class __cached_class__10;
    private static Class __cached_class__11;
    private static Class __cached_class__12;
    private static Class __cached_class__13;
    private static Class __cached_class__14;
    public static final Var const__0;
    public static final Var const__1;
    public static final Keyword const__2;
    public static final Object const__3;
    public static final Var const__4;
    public static final Var const__5;
    public static final Var const__6;
    public static final Var const__7;
    public static final Keyword const__8;
    public static final AFn const__10;
    public static final Var const__11;
    public static final AFn const__16;
    public static final Keyword const__17;
    public static final Keyword const__18;
    public static final Var const__19;
    public static final Var const__20;
    public static final Keyword const__21;
    public static final Keyword const__22;
    public static final Var const__24;
    public static final Keyword const__25;
    public static final Keyword const__26;
    public static final Keyword const__27;
    public static final Var const__28;
    public static final Var const__29;
    public static final Keyword const__30;
    public static final Object const__31;
    public static final Var const__32;
    public static final Var const__33;
    public static final Var const__34;
    public static final Keyword const__35;
    public static final Keyword const__36;
    public static final Keyword const__38;
    public static final Var const__39;
    public static final Keyword const__40;
    public static final Keyword const__41;
    public static final Keyword const__42;
    public static final Var const__43;
    public static final Var const__45;
    public static final Var const__46;
    public static final Keyword const__47;
    public static final Var const__48;
    public static final Var const__49;
    public static final Keyword const__50;
    public static final Var const__51;
    public static final Keyword const__52;
    public static final Keyword const__53;
    public static final Var const__54;
    public static final Keyword const__55;
    public static final Keyword const__56;
    public static final Keyword const__57;
    public static final Var const__58;
    public static final Var const__59;
    public static final Var const__60;
    public static final Var const__61;
    public static final Var const__62;
    public static final Var const__63;
    public static final Keyword const__64;
    public static final Keyword const__65;
    public static final Var const__66;
    public static final Var const__67;
    public static final Var const__68;
    public static final Var const__69;
    public static final Keyword const__70;
    public static final Var const__71;
    public static final Var const__72;
    public static final Var const__73;
    public static final Keyword const__74;
    public static final Keyword const__75;
    public static final Var const__76;
    public static final Var const__77;
    public static final Var const__78;
    public static final Keyword const__80;
    public static final Keyword const__81;
    public static final Keyword const__82;
    public static final Var const__83;
    public static final Keyword const__84;
    public static final Keyword const__85;
    public static final Var const__86;
    public static final Keyword const__87;
    public static final Keyword const__88;
    public static final Keyword const__89;
    public static final Keyword const__90;
    public static final Var const__91;
    public static final Keyword const__92;
    public static final Var const__95;
    public static final Var const__96;
    static final KeywordLookupSite __site__0__;
    static ILookupThunk __thunk__0__;
    static final KeywordLookupSite __site__1__;
    static ILookupThunk __thunk__1__;
    static final KeywordLookupSite __site__2__;
    static ILookupThunk __thunk__2__;
    static final KeywordLookupSite __site__3__;
    static ILookupThunk __thunk__3__;
    static final KeywordLookupSite __site__4__;
    static ILookupThunk __thunk__4__;
    static final KeywordLookupSite __site__5__;
    static ILookupThunk __thunk__5__;
    static final KeywordLookupSite __site__6__;
    static ILookupThunk __thunk__6__;
    static final KeywordLookupSite __site__7__;
    static ILookupThunk __thunk__7__;
    static final KeywordLookupSite __site__8__;
    static ILookupThunk __thunk__8__;
    static final KeywordLookupSite __site__9__;
    static ILookupThunk __thunk__9__;
    static final KeywordLookupSite __site__10__;
    static ILookupThunk __thunk__10__;
    static final KeywordLookupSite __site__11__;
    static ILookupThunk __thunk__11__;

    public Connection(Object object, Object object2, Object object3, Object object4, Object object5, Object object6, Object object7, Object object8, Object object9, Object object10, Object object11, Object object12) {
        this.db_id = object;
        this.cluster = object2;
        this.olookup = object3;
        this.state_ref = object4;
        this.db_ref = object5;
        this.pending_txes = object6;
        this.unsent_updates_queue = object7;
        this.lucene_queue = object8;
        this.tx_report_queue = object9;
        this.tx_watcher = object10;
        this.idx_watcher = object11;
        this.bg_watcher = object12;
    }

    public static IPersistentVector getBasis() {
        return RT.vector((Object[])new Object[]{Symbol.intern(null, (String)"db-id"), Symbol.intern(null, (String)"cluster"), Symbol.intern(null, (String)"olookup"), Symbol.intern(null, (String)"state-ref"), Symbol.intern(null, (String)"db-ref"), Symbol.intern(null, (String)"pending-txes"), Symbol.intern(null, (String)"unsent-updates-queue"), Symbol.intern(null, (String)"lucene-queue"), Symbol.intern(null, (String)"tx-report-queue"), Symbol.intern(null, (String)"tx-watcher"), Symbol.intern(null, (String)"idx-watcher"), Symbol.intern(null, (String)"bg-watcher")});
    }

    public Object notify_index() {
        Connection this_ = null;
        return ((IFn)const__96.getRawRoot()).invoke((Object)new Connection$fn__21555(this_.tx_watcher, this_.db_id, this_.idx_watcher, this_.bg_watcher, this_.db_ref, this_.olookup, this_.cluster));
    }

    /*
     * Unable to fully structure code
     */
    public Object notify_db(Object db) {
        ((IFn)Connection.const__73.getRawRoot()).invoke(this.db_ref, db);
        v0 = this.tx_watcher;
        if (Util.classOf((Object)v0) == Connection.__cached_class__12) ** GOTO lbl8
        if (!(v0 instanceof TWatcher)) {
            v0 = v0;
            Connection.__cached_class__12 = Util.classOf((Object)v0);
lbl8:
            // 2 sources

            v1 = Connection.const__91.getRawRoot().invoke(v0, db);
        } else {
            v1 = ((TWatcher)v0).release_pending_syncs(db);
        }
        if (Util.classOf((Object)(v2 = this.idx_watcher)) == Connection.__cached_class__13) ** GOTO lbl15
        if (!(v2 instanceof TWatcher)) {
            v2 = v2;
            Connection.__cached_class__13 = Util.classOf((Object)v2);
lbl15:
            // 2 sources

            v3 = Connection.const__91.getRawRoot().invoke(v2, db);
        } else {
            v3 = ((TWatcher)v2).release_pending_syncs(db);
        }
        if (Util.classOf((Object)(v4 = this.bg_watcher)) == Connection.__cached_class__14) ** GOTO lbl22
        if (!(v4 instanceof TWatcher)) {
            v4 = v4;
            Connection.__cached_class__14 = Util.classOf((Object)v4);
lbl22:
            // 2 sources

            v5 = db;
            db = null;
            this = null;
            v6 = Connection.const__91.getRawRoot().invoke(v4, v5);
        } else {
            v7 = db;
            db = null;
            v6 = ((TWatcher)v4).release_pending_syncs(v7);
        }
        return v6;
    }

    public Object notify_error(Object id, Object o) {
        Object object;
        Object prom;
        Object object2 = prom = ((IFn)const__76.getRawRoot()).invoke(this_.pending_txes, id);
        if (object2 != null && object2 != Boolean.FALSE) {
            Object object3;
            Logger logger = LoggerFactory.getLogger((String)"datomic.peer");
            if (logger.isDebugEnabled()) {
                Logger logger2 = logger;
                logger = null;
                Object[] objectArray = new Object[6];
                objectArray[0] = const__8;
                objectArray[1] = const__70;
                objectArray[2] = const__56;
                Object object4 = id;
                id = null;
                objectArray[3] = object4;
                objectArray[4] = const__21;
                objectArray[5] = const__26;
                logger2.debug((String)((IFn)const__4.getRawRoot()).invoke((Object)RT.mapUniqueKeys((Object[])objectArray)));
            }
            IFn iFn = (IFn)const__60.getRawRoot();
            Object object5 = prom;
            prom = null;
            if (o instanceof Throwable) {
                object3 = o;
                o = null;
            } else {
                Object object6 = o;
                o = null;
                object3 = ((IFn)const__95.getRawRoot()).invoke(object6);
            }
            Connection this_ = null;
            object = iFn.invoke(object5, object3);
        } else {
            object = null;
        }
        return object;
    }

    /*
     * Unable to fully structure code
     * Could not resolve type clashes
     */
    public Object notify_data(Object msg) {
        block27: {
            v0 = msg;
            msg = null;
            map__21547 = v0;
            v1 = ((IFn)Connection.const__77.getRawRoot()).invoke(map__21547);
            if (v1 != null && v1 != Boolean.FALSE) {
                v2 = map__21547;
                map__21547 = null;
                v3 = PersistentHashMap.create((ISeq)((ISeq)((IFn)Connection.const__78.getRawRoot()).invoke(v2)));
            } else {
                v3 = map__21547;
                map__21547 = null;
            }
            map__21547 = v3;
            id = RT.get((Object)map__21547, (Object)Connection.const__50);
            data = RT.get((Object)map__21547, (Object)Connection.const__80);
            tempids = RT.get((Object)map__21547, (Object)Connection.const__81);
            v4 = map__21547;
            map__21547 = null;
            io_stats = RT.get((Object)v4, (Object)Connection.const__82);
            start = System.currentTimeMillis();
            prom = ((IFn)Connection.const__76.getRawRoot()).invoke(this.pending_txes, id);
            map__21548 = ((IFn)Connection.const__83.getRawRoot()).invoke(prom);
            v5 = ((IFn)Connection.const__77.getRawRoot()).invoke(map__21548);
            if (v5 != null && v5 != Boolean.FALSE) {
                v6 = map__21548;
                map__21548 = null;
                v7 = PersistentHashMap.create((ISeq)((ISeq)((IFn)Connection.const__78.getRawRoot()).invoke(v6)));
            } else {
                v7 = map__21548;
                map__21548 = null;
            }
            v8 = map__21548 = v7;
            map__21548 = null;
            io_context = RT.get((Object)v8, (Object)Connection.const__84);
            old_db = ((IFn)Connection.const__6.getRawRoot()).invoke(this.db_ref);
            m_21549 = RT.mapUniqueKeys((Object[])new Object[]{Connection.const__8, Connection.const__85, Connection.const__50, id});
            logger = LoggerFactory.getLogger((String)"datomic.peer");
            if (logger.isDebugEnabled()) {
                v9 = logger;
                logger = null;
                v9.debug((String)((IFn)Connection.const__4.getRawRoot()).invoke(((IFn)Connection.const__20.getRawRoot()).invoke((Object)m_21549, (Object)Connection.const__21, (Object)Connection.const__22)));
            }
            start__8981__auto__21562 = System.nanoTime();
            result__8982__auto__21563 = ((IFn)new Connection$fn__21552(this.db_ref, data)).invoke();
            elapsed_21550 = Numbers.minus((long)System.nanoTime(), (long)start__8981__auto__21562);
            msec_21551 = ((IFn)Connection.const__24.getRawRoot()).invoke((Object)Numbers.num((long)elapsed_21550));
            ((IFn)Connection.const__86.getRawRoot()).invoke((Object)Connection.const__87, msec_21551);
            v10 = (IFn)Connection.const__7.getRawRoot();
            v11 = m_21549;
            m_21549 = null;
            v12 = msec_21551;
            msec_21551 = null;
            v13 = ((IFn)Connection.const__20.getRawRoot()).invoke((Object)v11, (Object)Connection.const__25, v12, (Object)Connection.const__21, (Object)Connection.const__26);
            v14 = Connection.__thunk__8__;
            v15 = result__8982__auto__21563;
            v16 = v14.get(v15);
            if (v14 == v16) {
                Connection.__thunk__8__ = Connection.__site__8__.fault(v15);
                v16 = Connection.__thunk__8__.get(v15);
            }
            if (v16 != null && v16 != Boolean.FALSE) {
                v17 = new Object[2];
                v17[0] = Connection.const__27;
                v18 = (IFn)Connection.const__28.getRawRoot();
                v19 = Connection.__thunk__9__;
                v20 = result__8982__auto__21563;
                v21 = v19.get(v20);
                if (v19 == v21) {
                    Connection.__thunk__9__ = Connection.__site__9__.fault(v20);
                    v21 = Connection.__thunk__9__.get(v20);
                }
                v17[1] = v18.invoke(v21);
                v22 = RT.mapUniqueKeys((Object[])v17);
            } else {
                v22 = null;
            }
            endmsg__8984__auto__21560 = v10.invoke(v13, v22);
            logger = LoggerFactory.getLogger((String)"datomic.peer");
            if (logger.isDebugEnabled()) {
                v23 = logger;
                logger = null;
                v24 = endmsg__8984__auto__21560;
                endmsg__8984__auto__21560 = null;
                v23.debug((String)((IFn)Connection.const__4.getRawRoot()).invoke(v24));
            }
            v25 = ((IFn)Connection.const__29.getRawRoot()).invoke(result__8982__auto__21563, (Object)Connection.const__30);
            if (v25 != null && v25 != Boolean.FALSE) {
                v26 = Connection.__thunk__10__;
                v27 = result__8982__auto__21563;
                result__8982__auto__21563 = null;
                v28 = v26.get(v27);
                if (v26 == v28) {
                    Connection.__thunk__10__ = Connection.__site__10__.fault(v27);
                    v28 = Connection.__thunk__10__.get(v27);
                }
            } else {
                v29 = Connection.__thunk__11__;
                v30 = result__8982__auto__21563;
                result__8982__auto__21563 = null;
                v31 = v29.get(v30);
                if (v29 == v31) {
                    Connection.__thunk__11__ = Connection.__site__11__.fault(v30);
                    v31 = Connection.__thunk__11__.get(v30);
                }
                throw (Throwable)v31;
            }
            new_db = v28;
            txrq = ((IFn)Connection.const__6.getRawRoot()).invoke(this.tx_report_queue);
            v32 = or__5238__auto__21564 = prom;
            if (v32 != null && v32 != Boolean.FALSE) {
                v33 = or__5238__auto__21564;
                or__5238__auto__21564 = null;
            } else {
                v33 = txrq;
            }
            if (v33 != null && v33 != Boolean.FALSE) {
                v34 = new Object[8];
                v34[0] = Connection.const__88;
                v35 = old_db;
                old_db = null;
                v34[1] = v35;
                v34[2] = Connection.const__89;
                v34[3] = new_db;
                v34[4] = Connection.const__90;
                v34[5] = data;
                v34[6] = Connection.const__81;
                v36 = tempids;
                tempids = null;
                v34[7] = v36;
                G__21554 = RT.mapUniqueKeys((Object[])v34);
                v37 = io_context;
                io_context = null;
                if (v37 != null && v37 != Boolean.FALSE) {
                    v38 = G__21554;
                    G__21554 = null;
                    v39 = io_stats;
                    io_stats = null;
                    v40 /* !! */  = ((IFn)Connection.const__20.getRawRoot()).invoke((Object)v38, (Object)Connection.const__82, v39);
                } else {
                    v40 /* !! */  = G__21554;
                    G__21554 = null;
                }
            } else {
                v40 /* !! */  = report = null;
            }
            if (Util.classOf((Object)(v41 = this.lucene_queue)) == Connection.__cached_class__10) ** GOTO lbl144
            if (!(v41 instanceof BlockingProducer)) {
                v41 = v41;
                Connection.__cached_class__10 = Util.classOf((Object)v41);
lbl144:
                // 2 sources

                v42 = data;
                data = null;
                v43 = Connection.const__59.getRawRoot().invoke(v41, v42);
            } else {
                v44 = data;
                data = null;
                v43 = ((BlockingProducer)v41).put(v44);
            }
            v45 = prom;
            if (v45 != null && v45 != Boolean.FALSE) {
                logger = LoggerFactory.getLogger((String)"datomic.peer");
                if (logger.isInfoEnabled()) {
                    v46 = logger;
                    logger = null;
                    v46.info((String)((IFn)Connection.const__4.getRawRoot()).invoke((Object)RT.mapUniqueKeys((Object[])new Object[]{Connection.const__8, Connection.const__70, Connection.const__56, id, Connection.const__21, Connection.const__26})));
                }
                v47 = prom;
                prom = null;
                ((IFn)Connection.const__60.getRawRoot()).invoke(v47, report);
            }
            v48 = txrq;
            if (v48 != null && v48 != Boolean.FALSE) {
                v49 = txrq;
                txrq = null;
                v50 = report;
                report = null;
                v51 = ((Queue)v49).add(v50) != false ? Boolean.TRUE : Boolean.FALSE;
            }
            if (Util.classOf((Object)(v52 = this.tx_watcher)) == Connection.__cached_class__11) ** GOTO lbl174
            if (!(v52 instanceof TWatcher)) {
                v52 = v52;
                Connection.__cached_class__11 = Util.classOf((Object)v52);
lbl174:
                // 2 sources

                v53 = new_db;
                new_db = null;
                v54 = Connection.const__91.getRawRoot().invoke(v52, v53);
            } else {
                v55 = new_db;
                new_db = null;
                v54 = ((TWatcher)v52).release_pending_syncs(v55);
            }
            logger = LoggerFactory.getLogger((String)"datomic.peer");
            if (!logger.isInfoEnabled()) break block27;
            v56 = logger;
            logger = null;
            v57 = new Object[6];
            v57[0] = Connection.const__8;
            v57[1] = Connection.const__92;
            v57[2] = Connection.const__25;
            v57[3] = Numbers.num((long)Numbers.minus((long)System.currentTimeMillis(), (long)start));
            v57[4] = Connection.const__50;
            v58 = id;
            id = null;
            v57[5] = v58;
            v56.info((String)((IFn)Connection.const__4.getRawRoot()).invoke((Object)RT.mapUniqueKeys((Object[])v57)));
        }
        return null;
    }

    public Object notify_sync(Object id) {
        Object object;
        Object prom;
        Object object2 = prom = ((IFn)const__76.getRawRoot()).invoke(this_.pending_txes, id);
        if (object2 != null && object2 != Boolean.FALSE) {
            Logger logger = LoggerFactory.getLogger((String)"datomic.peer");
            if (logger.isDebugEnabled()) {
                Logger logger2 = logger;
                logger = null;
                Object[] objectArray = new Object[6];
                objectArray[0] = const__8;
                objectArray[1] = const__55;
                objectArray[2] = const__56;
                Object object3 = id;
                id = null;
                objectArray[3] = object3;
                objectArray[4] = const__21;
                objectArray[5] = const__26;
                logger2.debug((String)((IFn)const__4.getRawRoot()).invoke((Object)RT.mapUniqueKeys((Object[])objectArray)));
            }
            Object object4 = prom;
            prom = null;
            Connection this_ = null;
            object = ((IFn)const__60.getRawRoot()).invoke(object4, ((IFn)const__6.getRawRoot()).invoke(this_.db_ref));
        } else {
            object = null;
        }
        return object;
    }

    public void gcStorage(Date older_than) {
        Object object;
        Object temp__5455__auto__21565;
        ILookupThunk iLookupThunk = __thunk__7__;
        Object object2 = ((IFn)const__45.getRawRoot()).invoke(this_.state_ref, null);
        Object object3 = iLookupThunk.get(object2);
        if (iLookupThunk == object3) {
            __thunk__7__ = __site__7__.fault(object2);
            object3 = __thunk__7__.get(object2);
        }
        Object object4 = temp__5455__auto__21565 = object3;
        if (object4 != null && object4 != Boolean.FALSE) {
            Object connector2;
            Object object5 = temp__5455__auto__21565;
            temp__5455__auto__21565 = null;
            Object object6 = connector2 = object5;
            connector2 = null;
            Object[] objectArray = new Object[4];
            objectArray[0] = const__35;
            objectArray[1] = this_.db_id;
            objectArray[2] = const__75;
            Date date = older_than;
            older_than = null;
            objectArray[3] = date;
            Connection this_ = null;
            object = ((IFn)const__46.getRawRoot()).invoke(object6, (Object)const__74, (Object)RT.mapUniqueKeys((Object[])objectArray));
        } else {
            object = null;
        }
    }

    public void removeTxReportQueue() {
        Connection this_ = null;
        ((IFn)const__73.getRawRoot()).invoke(this_.tx_report_queue, null);
    }

    public BlockingQueue txReportQueue() {
        Object object;
        Object or__5238__auto__21566;
        Object object2 = or__5238__auto__21566 = ((IFn)const__6.getRawRoot()).invoke(this_.tx_report_queue);
        if (object2 != null && object2 != Boolean.FALSE) {
            object = or__5238__auto__21566;
            or__5238__auto__21566 = null;
        } else {
            Connection this_ = null;
            object = ((IFn)const__72.getRawRoot()).invoke(this_.tx_report_queue, (Object)new Connection$fn__21545());
        }
        return (BlockingQueue)object;
    }

    /*
     * Unable to fully structure code
     */
    public ListenableFuture transactAsync(List txdata, Object options) {
        block8: {
            block7: {
                v0 = ((IFn)Connection.const__45.getRawRoot()).invoke(this.state_ref, null);
                if (v0 == null || v0 == Boolean.FALSE) break block7;
                try {
                    v1 = txdata;
                    txdata = null;
                    tx_procargs = ((IFn)Connection.const__67.getRawRoot()).invoke((Object)v1, options);
                    sync_key = ((IFn)Connection.const__68.getRawRoot()).invoke(tx_procargs, (Object)Connection.const__50);
                    v2 = options;
                    options = null;
                    p = ((IFn)Connection.const__69.getRawRoot()).invoke(((IFn)Connection.const__54.getRawRoot()).invoke(), v2);
                    logger = LoggerFactory.getLogger((String)"datomic.peer");
                    if (logger.isInfoEnabled()) {
                        v3 = logger;
                        logger = null;
                        v3.info((String)((IFn)Connection.const__4.getRawRoot()).invoke((Object)RT.mapUniqueKeys((Object[])new Object[]{Connection.const__8, Connection.const__70, Connection.const__56, sync_key, Connection.const__21, Connection.const__57})));
                    }
                    v4 = sync_key;
                    sync_key = null;
                    ((IFn)Connection.const__58.getRawRoot()).invoke(this.pending_txes, v4, p);
                    v5 = this.unsent_updates_queue;
                    if (Util.classOf((Object)v5) == Connection.__cached_class__9) ** GOTO lbl26
                    if (!(v5 instanceof BlockingProducer)) {
                        v5 = v5;
                        Connection.__cached_class__9 = Util.classOf((Object)v5);
lbl26:
                        // 2 sources

                        v6 = tx_procargs;
                        tx_procargs = null;
                        v7 = Connection.const__59.getRawRoot().invoke(v5, v6);
                    } else {
                        v8 = tx_procargs;
                        tx_procargs = null;
                        v7 = ((BlockingProducer)v5).put(v8);
                    }
                    if (v7 != null && v7 != Boolean.FALSE) {
                    } else {
                        ((IFn)Connection.const__60.getRawRoot()).invoke(p, ((IFn)Connection.const__61.getRawRoot()).invoke());
                    }
                    v9 = p;
                    p = null;
                    var7_9 = v9;
                }
                catch (Throwable t) {
                    t = null;
                    var7_9 = ((IFn)Connection.const__71.getRawRoot()).invoke((Object)t);
                }
                break block8;
            }
            throw (Throwable)((IFn)Connection.const__48.getRawRoot()).invoke();
        }
        return (ListenableFuture)var7_9;
    }

    public ListenableFuture transactAsync(List txdata) {
        List list = txdata;
        txdata = null;
        return ((datomic.Connection)this).transactAsync(list, null);
    }

    public ListenableFuture transact(List txdata, Object options) {
        List list = txdata;
        txdata = null;
        Object object = options;
        options = null;
        ListenableFuture<Map> listenableFuture = ((datomic.Connection)this_).transactAsync(list, object);
        Connection this_ = null;
        return (ListenableFuture)((IFn)const__66.getRawRoot()).invoke(listenableFuture);
    }

    public ListenableFuture transact(List txdata) {
        List list = txdata;
        txdata = null;
        ListenableFuture<Map> listenableFuture = ((datomic.Connection)this_).transactAsync(list);
        Connection this_ = null;
        return (ListenableFuture)((IFn)const__66.getRawRoot()).invoke(listenableFuture);
    }

    /*
     * Enabled aggressive block sorting
     */
    public ListenableFuture syncExcise(long t) {
        Object object;
        Object object2 = this_.bg_watcher;
        if (Util.classOf((Object)object2) != __cached_class__8) {
            if (object2 instanceof TWatcher) {
                object = ((TWatcher)object2).sync_background_t(const__65, Numbers.num((long)t));
                return (ListenableFuture)object;
            }
            object2 = object2;
            __cached_class__8 = Util.classOf((Object)object2);
        }
        Connection this_ = null;
        object = const__63.getRawRoot().invoke(object2, (Object)const__65, (Object)Numbers.num((long)t));
        return (ListenableFuture)object;
    }

    /*
     * Enabled aggressive block sorting
     */
    public ListenableFuture syncSchema(long t) {
        Object object;
        Object object2 = this_.bg_watcher;
        if (Util.classOf((Object)object2) != __cached_class__7) {
            if (object2 instanceof TWatcher) {
                object = ((TWatcher)object2).sync_background_t(const__64, Numbers.num((long)t));
                return (ListenableFuture)object;
            }
            object2 = object2;
            __cached_class__7 = Util.classOf((Object)object2);
        }
        Connection this_ = null;
        object = const__63.getRawRoot().invoke(object2, (Object)const__64, (Object)Numbers.num((long)t));
        return (ListenableFuture)object;
    }

    /*
     * Enabled aggressive block sorting
     */
    public ListenableFuture syncIndex(long t) {
        Object object;
        Object object2 = this_.idx_watcher;
        if (Util.classOf((Object)object2) != __cached_class__6) {
            if (object2 instanceof TWatcher) {
                object = ((TWatcher)object2).sync_t(Numbers.num((long)t));
                return (ListenableFuture)object;
            }
            object2 = object2;
            __cached_class__6 = Util.classOf((Object)object2);
        }
        Connection this_ = null;
        object = const__62.getRawRoot().invoke(object2, (Object)Numbers.num((long)t));
        return (ListenableFuture)object;
    }

    /*
     * Enabled aggressive block sorting
     */
    public ListenableFuture sync(long t) {
        Object object;
        Object object2 = this_.tx_watcher;
        if (Util.classOf((Object)object2) != __cached_class__5) {
            if (object2 instanceof TWatcher) {
                object = ((TWatcher)object2).sync_t(Numbers.num((long)t));
                return (ListenableFuture)object;
            }
            object2 = object2;
            __cached_class__5 = Util.classOf((Object)object2);
        }
        Connection this_ = null;
        object = const__62.getRawRoot().invoke(object2, (Object)Numbers.num((long)t));
        return (ListenableFuture)object;
    }

    /*
     * Unable to fully structure code
     */
    public ListenableFuture sync() {
        block7: {
            block6: {
                v0 = ((IFn)Connection.const__45.getRawRoot()).invoke(this.state_ref, null);
                if (v0 == null || v0 == Boolean.FALSE) break block6;
                s = RT.mapUniqueKeys((Object[])new Object[]{Connection.const__50, ((IFn)Connection.const__51.getRawRoot()).invoke(), Connection.const__52, Connection.const__53});
                v1 = Connection.__thunk__6__;
                v2 = s;
                v3 = v1.get((Object)v2);
                if (v1 == v3) {
                    Connection.__thunk__6__ = Connection.__site__6__.fault((Object)v2);
                    v3 = Connection.__thunk__6__.get((Object)v2);
                }
                sync_key = v3;
                p = ((IFn)Connection.const__54.getRawRoot()).invoke();
                logger = LoggerFactory.getLogger((String)"datomic.peer");
                if (logger.isDebugEnabled()) {
                    v4 = logger;
                    logger = null;
                    v4.debug((String)((IFn)Connection.const__4.getRawRoot()).invoke((Object)RT.mapUniqueKeys((Object[])new Object[]{Connection.const__8, Connection.const__55, Connection.const__56, sync_key, Connection.const__21, Connection.const__57})));
                }
                v5 = sync_key;
                sync_key = null;
                ((IFn)Connection.const__58.getRawRoot()).invoke(this.pending_txes, v5, p);
                v6 = this.unsent_updates_queue;
                if (Util.classOf((Object)v6) == Connection.__cached_class__4) ** GOTO lbl28
                if (!(v6 instanceof BlockingProducer)) {
                    v6 = v6;
                    Connection.__cached_class__4 = Util.classOf((Object)v6);
lbl28:
                    // 2 sources

                    v7 = s;
                    s = null;
                    v8 = Connection.const__59.getRawRoot().invoke(v6, (Object)v7);
                } else {
                    v9 = s;
                    s = null;
                    v8 = ((BlockingProducer)v6).put(v9);
                }
                if (v8 != null && v8 != Boolean.FALSE) {
                } else {
                    ((IFn)Connection.const__60.getRawRoot()).invoke(p, ((IFn)Connection.const__61.getRawRoot()).invoke());
                }
                break block7;
            }
            throw (Throwable)((IFn)Connection.const__48.getRawRoot()).invoke();
        }
        var3_3 = null;
        return (ListenableFuture)p;
    }

    public Log log() {
        Connection this_ = null;
        return (Log)((IFn)const__49.getRawRoot()).invoke(this_.cluster, this_.olookup, ((IFn)const__6.getRawRoot()).invoke(this_.db_ref));
    }

    public Database db() {
        ((IFn)const__45.getRawRoot()).invoke(this_.state_ref, null);
        Connection this_ = null;
        return (Database)((IFn)const__6.getRawRoot()).invoke(this_.db_ref);
    }

    public boolean requestIndex() {
        Object connector2;
        Object temp__5455__auto__21567;
        ILookupThunk iLookupThunk = __thunk__5__;
        Object object = ((IFn)const__45.getRawRoot()).invoke(this.state_ref, null);
        Object object2 = iLookupThunk.get(object);
        if (iLookupThunk == object2) {
            __thunk__5__ = __site__5__.fault(object);
            object2 = __thunk__5__.get(object);
        }
        Object object3 = temp__5455__auto__21567 = object2;
        if (object3 == null || object3 == Boolean.FALSE) {
            throw (Throwable)((IFn)const__48.getRawRoot()).invoke();
        }
        Object object4 = temp__5455__auto__21567;
        temp__5455__auto__21567 = null;
        Object object5 = connector2 = object4;
        connector2 = null;
        ((IFn)const__46.getRawRoot()).invoke(object5, (Object)const__47, this.db_id);
        return Boolean.TRUE;
    }

    /*
     * Unable to fully structure code
     */
    public void release() {
        v0 = lockee__5436__auto__21568 = Connection.const__43.getRawRoot();
        lockee__5436__auto__21568 = null;
        ((IFn)new Connection$fn__21537(v0, this)).invoke();
        v1 = (IFn)Connection.const__6.getRawRoot();
        v2 = this;
        if (Util.classOf((Object)v2) == Connection.__cached_class__3) ** GOTO lbl11
        if (!(v2 instanceof AsyncShutdown)) {
            v2 = v2;
            Connection.__cached_class__3 = Util.classOf((Object)v2);
lbl11:
            // 2 sources

            v3 = Connection.const__5.getRawRoot().invoke((Object)v2);
        } else {
            v3 = ((AsyncShutdown)v2).async_shutdown();
        }
        this = null;
        v1.invoke(v3);
    }

    public Object metrics() {
        IPersistentMap iPersistentMap;
        Object db2 = ((IFn)const__6.getRawRoot()).invoke(this_.db_ref);
        IFn iFn = (IFn)const__7.getRawRoot();
        IPersistentMap iPersistentMap2 = RT.mapUniqueKeys((Object[])new Object[]{const__36, RT.count((Object)this_.unsent_updates_queue), const__38, ((IFn)const__39.getRawRoot()).invoke(this_.pending_txes)});
        Object object = db2;
        if (object != null && object != Boolean.FALSE) {
            Object[] objectArray = new Object[6];
            objectArray[0] = const__40;
            objectArray[1] = Numbers.num((long)((Database)db2).nextT());
            objectArray[2] = const__41;
            objectArray[3] = Numbers.num((long)((Database)db2).basisT());
            objectArray[4] = const__42;
            ILookupThunk iLookupThunk = __thunk__4__;
            Object object2 = db2;
            db2 = null;
            Object object3 = iLookupThunk.get(object2);
            if (iLookupThunk == object3) {
                __thunk__4__ = __site__4__.fault(object2);
                object3 = __thunk__4__.get(object2);
            }
            objectArray[5] = object3;
            iPersistentMap = RT.mapUniqueKeys((Object[])objectArray);
        } else {
            iPersistentMap = null;
        }
        Connection this_ = null;
        return iFn.invoke((Object)iPersistentMap2, iPersistentMap);
    }

    /*
     * Unable to fully structure code
     */
    public String toString() {
        v0 = (IFn)Connection.const__33.getRawRoot();
        v1 = (IFn)Connection.const__20.getRawRoot();
        v2 = this;
        if (Util.classOf((Object)v2) == Connection.__cached_class__2) ** GOTO lbl8
        if (!(v2 instanceof Metrics)) {
            v2 = v2;
            Connection.__cached_class__2 = Util.classOf((Object)v2);
lbl8:
            // 2 sources

            v3 = Connection.const__34.getRawRoot().invoke((Object)v2);
        } else {
            v3 = ((Metrics)v2).metrics();
        }
        this = null;
        return (String)v0.invoke(v1.invoke(v3, (Object)Connection.const__35, this.db_id));
    }

    /*
     * Enabled aggressive block sorting
     */
    public Object reconnect() {
        Object object;
        Object o;
        Object temp__5455__auto__21569;
        Object object2 = temp__5455__auto__21569 = ((IFn)const__6.getRawRoot()).invoke(this_.state_ref, const__31, null);
        if (object2 == null) throw (Throwable)new IllegalStateException("Peer reconnector setup failed.");
        if (object2 == Boolean.FALSE) throw (Throwable)new IllegalStateException("Peer reconnector setup failed.");
        Object object3 = temp__5455__auto__21569;
        temp__5455__auto__21569 = null;
        Object object4 = o = object3;
        o = null;
        Object object5 = object4;
        if (Util.classOf((Object)object4) != __cached_class__1) {
            if (object5 instanceof Reconnectable) {
                object = ((Reconnectable)object5).reconnect();
                return object;
            }
            object5 = object5;
            __cached_class__1 = Util.classOf((Object)object5);
        }
        Connection this_ = null;
        object = const__32.getRawRoot().invoke(object5);
        return object;
    }

    public Object create_connection_state(Object cluster_conf, Object endpoint, Object mode) {
        Object object;
        IPersistentMap iPersistentMap;
        Logger logger = LoggerFactory.getLogger((String)"datomic.peer");
        if (logger.isInfoEnabled()) {
            Logger logger2 = logger;
            logger = null;
            logger2.info((String)((IFn)const__4.getRawRoot()).invoke(((IFn)const__7.getRawRoot()).invoke((Object)const__10, ((IFn)const__11.getRawRoot()).invoke(endpoint, (Object)const__16))));
        }
        IPersistentMap m_21502 = RT.mapUniqueKeys((Object[])new Object[]{const__8, const__17, const__18, ((IFn)const__19.getRawRoot()).invoke(cluster_conf)});
        Logger logger3 = LoggerFactory.getLogger((String)"datomic.peer");
        if (logger3.isDebugEnabled()) {
            Logger logger4 = logger3;
            logger3 = null;
            logger4.debug((String)((IFn)const__4.getRawRoot()).invoke(((IFn)const__20.getRawRoot()).invoke((Object)m_21502, (Object)const__21, (Object)const__22)));
        }
        long start__8981__auto__21572 = System.nanoTime();
        Object object2 = mode;
        mode = null;
        Object object3 = endpoint;
        endpoint = null;
        Object object4 = cluster_conf;
        cluster_conf = null;
        Object result__8982__auto__21573 = ((IFn)new Connection$fn__21505(this.db_id, this, this.unsent_updates_queue, object2, object3, object4, this.db_ref, this.olookup, this.cluster)).invoke();
        long elapsed_21503 = Numbers.minus((long)System.nanoTime(), (long)start__8981__auto__21572);
        Object msec_21504 = ((IFn)const__24.getRawRoot()).invoke((Object)Numbers.num((long)elapsed_21503));
        IFn iFn = (IFn)const__7.getRawRoot();
        IPersistentMap iPersistentMap2 = m_21502;
        m_21502 = null;
        Object object5 = msec_21504;
        msec_21504 = null;
        Object object6 = ((IFn)const__20.getRawRoot()).invoke((Object)iPersistentMap2, (Object)const__25, object5, (Object)const__21, (Object)const__26);
        ILookupThunk iLookupThunk = __thunk__0__;
        Object object7 = result__8982__auto__21573;
        Object object8 = iLookupThunk.get(object7);
        if (iLookupThunk == object8) {
            __thunk__0__ = __site__0__.fault(object7);
            object8 = __thunk__0__.get(object7);
        }
        if (object8 != null && object8 != Boolean.FALSE) {
            Object[] objectArray = new Object[2];
            objectArray[0] = const__27;
            IFn iFn2 = (IFn)const__28.getRawRoot();
            ILookupThunk iLookupThunk2 = __thunk__1__;
            Object object9 = result__8982__auto__21573;
            Object object10 = iLookupThunk2.get(object9);
            if (iLookupThunk2 == object10) {
                __thunk__1__ = __site__1__.fault(object9);
                object10 = __thunk__1__.get(object9);
            }
            objectArray[1] = iFn2.invoke(object10);
            iPersistentMap = RT.mapUniqueKeys((Object[])objectArray);
        } else {
            iPersistentMap = null;
        }
        Object endmsg__8984__auto__21570 = iFn.invoke(object6, iPersistentMap);
        Logger logger5 = LoggerFactory.getLogger((String)"datomic.peer");
        if (logger5.isDebugEnabled()) {
            Logger logger6 = logger5;
            logger5 = null;
            Object object11 = endmsg__8984__auto__21570;
            endmsg__8984__auto__21570 = null;
            logger6.debug((String)((IFn)const__4.getRawRoot()).invoke(object11));
        }
        Object object12 = ((IFn)const__29.getRawRoot()).invoke(result__8982__auto__21573, (Object)const__30);
        if (object12 != null && object12 != Boolean.FALSE) {
            ILookupThunk iLookupThunk3 = __thunk__2__;
            Object object13 = result__8982__auto__21573;
            result__8982__auto__21573 = null;
            object = iLookupThunk3.get(object13);
            if (iLookupThunk3 == object) {
                __thunk__2__ = __site__2__.fault(object13);
                object = __thunk__2__.get(object13);
            }
        } else {
            ILookupThunk iLookupThunk4 = __thunk__3__;
            Object object14 = result__8982__auto__21573;
            result__8982__auto__21573 = null;
            Object object15 = iLookupThunk4.get(object14);
            if (iLookupThunk4 == object15) {
                __thunk__3__ = __site__3__.fault(object14);
                object15 = __thunk__3__.get(object14);
            }
            throw (Throwable)object15;
        }
        return object;
    }

    public Object get_olookup() {
        return this.olookup;
    }

    public Object get_cluster() {
        return this.cluster;
    }

    /*
     * Enabled aggressive block sorting
     */
    public Object async_shutdown() {
        Object object;
        Object object2;
        ((IFn)const__0.getRawRoot()).invoke(this_.cluster);
        Object object3 = ((IFn)const__1.getRawRoot()).invoke(this_.lucene_queue, (Object)const__2, const__3);
        if (object3 != null && object3 != Boolean.FALSE) {
        } else {
            Logger logger = LoggerFactory.getLogger((String)"datomic.peer");
            if (logger.isWarnEnabled()) {
                Logger logger2 = logger;
                logger = null;
                logger2.warn((String)((IFn)const__4.getRawRoot()).invoke((Object)"Timed out shutting down lucene integration thread"));
            }
        }
        if (Util.classOf((Object)(object2 = ((IFn)const__6.getRawRoot()).invoke(this_.state_ref))) != __cached_class__0) {
            if (object2 instanceof AsyncShutdown) {
                object = ((AsyncShutdown)object2).async_shutdown();
                return object;
            }
            object2 = object2;
            __cached_class__0 = Util.classOf((Object)object2);
        }
        Connection this_ = null;
        object = const__5.getRawRoot().invoke(object2);
        return object;
    }

    static {
        const__0 = RT.var((String)"datomic.cluster", (String)"close");
        const__1 = RT.var((String)"datomic.queue", (String)"offer");
        const__2 = RT.keyword(null, (String)"done");
        const__3 = 1000L;
        const__4 = RT.var((String)"datomic.slf4j", (String)"process");
        const__5 = RT.var((String)"datomic.common", (String)"async-shutdown");
        const__6 = RT.var((String)"clojure.core", (String)"deref");
        const__7 = RT.var((String)"clojure.core", (String)"merge");
        const__8 = RT.keyword(null, (String)"event");
        const__10 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"event"), RT.keyword((String)"peer", (String)"connect-transactor")});
        const__11 = RT.var((String)"clojure.core", (String)"select-keys");
        const__16 = (AFn)Tuple.create((Object)RT.keyword(null, (String)"host"), (Object)RT.keyword(null, (String)"alt-host"), (Object)RT.keyword(null, (String)"port"), (Object)RT.keyword(null, (String)"version"));
        const__17 = RT.keyword((String)"peer", (String)"create-connection-impl");
        const__18 = RT.keyword(null, (String)"cluster-conf");
        const__19 = RT.var((String)"datomic.uri", (String)"loggable-cluster-conf");
        const__20 = RT.var((String)"clojure.core", (String)"assoc");
        const__21 = RT.keyword(null, (String)"phase");
        const__22 = RT.keyword(null, (String)"begin");
        const__24 = RT.var((String)"datomic.slf4j", (String)"format-as-msec");
        const__25 = RT.keyword(null, (String)"msec");
        const__26 = RT.keyword(null, (String)"end");
        const__27 = RT.keyword(null, (String)"threw");
        const__28 = RT.var((String)"clojure.core", (String)"class");
        const__29 = RT.var((String)"clojure.core", (String)"contains?");
        const__30 = RT.keyword(null, (String)"returned");
        const__31 = 10000L;
        const__32 = RT.var((String)"datomic.reconnector2", (String)"reconnect");
        const__33 = RT.var((String)"clojure.core", (String)"str");
        const__34 = RT.var((String)"datomic.monitor", (String)"metrics");
        const__35 = RT.keyword(null, (String)"db-id");
        const__36 = RT.keyword(null, (String)"unsent-updates-queue");
        const__38 = RT.keyword(null, (String)"pending-txes");
        const__39 = RT.var((String)"datomic.cache", (String)"fast-count");
        const__40 = RT.keyword(null, (String)"next-t");
        const__41 = RT.keyword(null, (String)"basis-t");
        const__42 = RT.keyword(null, (String)"index-rev");
        const__43 = RT.var((String)"datomic.peer", (String)"connection-lock");
        const__45 = RT.var((String)"datomic.peer", (String)"get-cstate");
        const__46 = RT.var((String)"datomic.connector", (String)"admin-request");
        const__47 = RT.keyword(null, (String)"request-index");
        const__48 = RT.var((String)"datomic.peer", (String)"transactor-unavailable");
        const__49 = RT.var((String)"datomic.log", (String)"create-log-val");
        const__50 = RT.keyword(null, (String)"id");
        const__51 = RT.var((String)"datomic.common", (String)"rand-uuid");
        const__52 = RT.keyword(null, (String)"type");
        const__53 = RT.keyword(null, (String)"sync");
        const__54 = RT.var((String)"datomic.promise", (String)"settable-future");
        const__55 = RT.keyword((String)"peer", (String)"sync");
        const__56 = RT.keyword(null, (String)"uuid");
        const__57 = RT.keyword(null, (String)"start");
        const__58 = RT.var((String)"datomic.cache", (String)"put");
        const__59 = RT.var((String)"datomic.queue", (String)"put");
        const__60 = RT.var((String)"clojure.core", (String)"deliver");
        const__61 = RT.var((String)"datomic.peer", (String)"peer-queue-exceeded");
        const__62 = RT.var((String)"datomic.peer", (String)"sync-t");
        const__63 = RT.var((String)"datomic.peer", (String)"sync-background-t");
        const__64 = RT.keyword(null, (String)"schema");
        const__65 = RT.keyword(null, (String)"excise");
        const__66 = RT.var((String)"datomic.peer", (String)"await-tx-result");
        const__67 = RT.var((String)"datomic.transaction", (String)"create-procargs");
        const__68 = RT.var((String)"datomic.common", (String)"getx");
        const__69 = RT.var((String)"clojure.core", (String)"with-meta");
        const__70 = RT.keyword((String)"peer", (String)"transact");
        const__71 = RT.var((String)"datomic.promise", (String)"delivered");
        const__72 = RT.var((String)"clojure.core", (String)"swap!");
        const__73 = RT.var((String)"clojure.core", (String)"reset!");
        const__74 = RT.keyword(null, (String)"request-gc");
        const__75 = RT.keyword(null, (String)"older-than");
        const__76 = RT.var((String)"datomic.cache", (String)"remove");
        const__77 = RT.var((String)"clojure.core", (String)"seq?");
        const__78 = RT.var((String)"clojure.core", (String)"seq");
        const__80 = RT.keyword(null, (String)"data");
        const__81 = RT.keyword(null, (String)"tempids");
        const__82 = RT.keyword(null, (String)"io-stats");
        const__83 = RT.var((String)"clojure.core", (String)"meta");
        const__84 = RT.keyword(null, (String)"io-context");
        const__85 = RT.keyword((String)"peer", (String)"accept-new");
        const__86 = RT.var((String)"datomic.monitor", (String)"add-stat");
        const__87 = RT.keyword(null, (String)"PeerAcceptNewMsec");
        const__88 = RT.keyword(null, (String)"db-before");
        const__89 = RT.keyword(null, (String)"db-after");
        const__90 = RT.keyword(null, (String)"tx-data");
        const__91 = RT.var((String)"datomic.peer", (String)"release-pending-syncs");
        const__92 = RT.keyword((String)"peer", (String)"notify-data");
        const__95 = RT.var((String)"datomic.error", (String)"deserialize-exception");
        const__96 = RT.var((String)"clojure.core", (String)"future-call");
        __site__0__ = new KeywordLookupSite(RT.keyword(null, (String)"threw"));
        __thunk__0__ = __site__0__;
        __site__1__ = new KeywordLookupSite(RT.keyword(null, (String)"threw"));
        __thunk__1__ = __site__1__;
        __site__2__ = new KeywordLookupSite(RT.keyword(null, (String)"returned"));
        __thunk__2__ = __site__2__;
        __site__3__ = new KeywordLookupSite(RT.keyword(null, (String)"threw"));
        __thunk__3__ = __site__3__;
        __site__4__ = new KeywordLookupSite(RT.keyword(null, (String)"index-rev"));
        __thunk__4__ = __site__4__;
        __site__5__ = new KeywordLookupSite(RT.keyword(null, (String)"connector"));
        __thunk__5__ = __site__5__;
        __site__6__ = new KeywordLookupSite(RT.keyword(null, (String)"id"));
        __thunk__6__ = __site__6__;
        __site__7__ = new KeywordLookupSite(RT.keyword(null, (String)"connector"));
        __thunk__7__ = __site__7__;
        __site__8__ = new KeywordLookupSite(RT.keyword(null, (String)"threw"));
        __thunk__8__ = __site__8__;
        __site__9__ = new KeywordLookupSite(RT.keyword(null, (String)"threw"));
        __thunk__9__ = __site__9__;
        __site__10__ = new KeywordLookupSite(RT.keyword(null, (String)"returned"));
        __thunk__10__ = __site__10__;
        __site__11__ = new KeywordLookupSite(RT.keyword(null, (String)"threw"));
        __thunk__11__ = __site__11__;
    }
}

