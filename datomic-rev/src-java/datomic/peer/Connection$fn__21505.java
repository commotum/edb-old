/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.Delay
 *  clojure.lang.IFn
 *  clojure.lang.ILookupThunk
 *  clojure.lang.Keyword
 *  clojure.lang.KeywordLookupSite
 *  clojure.lang.RT
 *  clojure.lang.Util
 *  clojure.lang.Var
 */
package datomic.peer;

import clojure.lang.AFunction;
import clojure.lang.Delay;
import clojure.lang.IFn;
import clojure.lang.ILookupThunk;
import clojure.lang.Keyword;
import clojure.lang.KeywordLookupSite;
import clojure.lang.RT;
import clojure.lang.Util;
import clojure.lang.Var;
import datomic.connector.TransactorConnector;
import datomic.peer.Connection$fn__21505$fn__21506;
import datomic.peer.Connection$fn__21505$fn__21509;
import datomic.peer.Connection$fn__21505$fn__21511;
import datomic.peer.Connection$fn__21505$fn__21515;
import datomic.peer.Connection$fn__21505$fn__21519;
import datomic.peer.Connection$fn__21505$load_db__21513;
import java.lang.ref.WeakReference;

public final class Connection$fn__21505
extends AFunction {
    Object db_id;
    Object conn;
    Object unsent_updates_queue;
    Object mode;
    Object endpoint;
    Object cluster_conf;
    Object db_ref;
    Object olookup;
    Object cluster;
    private static Class __cached_class__0;
    private static Class __cached_class__1;
    public static final Keyword const__0;
    public static final Var const__1;
    public static final Var const__2;
    public static final Var const__3;
    public static final Var const__4;
    public static final Var const__5;
    public static final Var const__6;
    public static final Keyword const__7;
    public static final Keyword const__9;
    public static final Object const__10;
    public static final Object const__11;
    public static final Keyword const__13;
    public static final Var const__14;
    public static final Var const__15;
    public static final Var const__16;
    public static final Var const__17;
    public static final Keyword const__18;
    static final KeywordLookupSite __site__0__;
    static ILookupThunk __thunk__0__;
    static final KeywordLookupSite __site__1__;
    static ILookupThunk __thunk__1__;
    static final KeywordLookupSite __site__2__;
    static ILookupThunk __thunk__2__;

    public Connection$fn__21505(Object object, Object object2, Object object3, Object object4, Object object5, Object object6, Object object7, Object object8, Object object9) {
        this.db_id = object;
        this.conn = object2;
        this.unsent_updates_queue = object3;
        this.mode = object4;
        this.endpoint = object5;
        this.cluster_conf = object6;
        this.db_ref = object7;
        this.olookup = object8;
        this.cluster = object9;
    }

    /*
     * Unable to fully structure code
     */
    public Object invoke() {
        try {
            v0 = new Object[2];
            v0[0] = Connection$fn__21505.const__0;
            v1 = conn_ref = new WeakReference<Object>(this.conn);
            conn_ref = null;
            v2 = recon = new Delay((IFn)new Connection$fn__21505$fn__21506(v1));
            recon = null;
            failure_handler = ((IFn)Connection$fn__21505.const__1.getRawRoot()).invoke((Object)new Connection$fn__21505$fn__21509(v2));
            shutdown = ((IFn)Connection$fn__21505.const__2.getRawRoot()).invoke(Connection$fn__21505.const__3.getRawRoot(), Connection$fn__21505.const__4.getRawRoot());
            this.endpoint = null;
            connector = ((IFn)Connection$fn__21505.const__5.getRawRoot()).invoke(this.cluster_conf, this.endpoint);
            v3 = connector;
            if (Util.classOf((Object)v3) == Connection$fn__21505.__cached_class__0) ** GOTO lbl17
            if (!(v3 instanceof TransactorConnector)) {
                v3 = v3;
                Connection$fn__21505.__cached_class__0 = Util.classOf((Object)v3);
lbl17:
                // 2 sources

                this.mode = null;
                v4 = Connection$fn__21505.const__6.getRawRoot().invoke(v3, (Object)Connection$fn__21505.const__7, this.db_id, Util.equiv((Object)this.mode, (Object)Connection$fn__21505.const__9) != false ? Connection$fn__21505.const__10 : Connection$fn__21505.const__11);
            } else {
                this.mode = null;
                v4 = ((TransactorConnector)v3).admin_request_STAR_(Connection$fn__21505.const__7, this.db_id, Util.equiv((Object)this.mode, (Object)Connection$fn__21505.const__9) != false ? Connection$fn__21505.const__10 : Connection$fn__21505.const__11);
            }
            start_result = v4;
            v5 = Connection$fn__21505.__thunk__0__;
            v6 = start_result;
            v7 = v5.get(v6);
            if (v5 == v7) {
                Connection$fn__21505.__thunk__0__ = Connection$fn__21505.__site__0__.fault(v6);
                v7 = Connection$fn__21505.__thunk__0__.get(v6);
            }
            if (v7 != null && v7 != Boolean.FALSE) {
                v8 = Connection$fn__21505.__thunk__1__;
                v9 = start_result;
                v10 = v8.get(v9);
                if (v8 == v10) {
                    Connection$fn__21505.__thunk__1__ = Connection$fn__21505.__site__1__.fault(v9);
                    v10 = Connection$fn__21505.__thunk__1__.get(v9);
                }
                if (Util.equiv((Object)v10, (Object)"database does not exist")) {
                    v11 = lockee__5436__auto__21536 = Connection$fn__21505.const__14.getRawRoot();
                    lockee__5436__auto__21536 = null;
                    ((IFn)new Connection$fn__21505$fn__21511(this.cluster_conf, v11)).invoke();
                }
                v12 = (IFn)Connection$fn__21505.const__15.getRawRoot();
                v13 = Connection$fn__21505.__thunk__2__;
                v14 = start_result;
                v15 = v13.get(v14);
                if (v13 == v15) {
                    Connection$fn__21505.__thunk__2__ = Connection$fn__21505.__site__2__.fault(v14);
                    v15 = Connection$fn__21505.__thunk__2__.get(v14);
                }
                v16 = start_result;
                start_result = null;
                throw (Throwable)v12.invoke(v15, ((IFn)Connection$fn__21505.const__16.getRawRoot()).invoke(v16, (Object)Connection$fn__21505.const__13));
            }
            this.cluster_conf = null;
            load_db = new Connection$fn__21505$load_db__21513(this.cluster_conf, this.olookup, this.cluster);
            db = ((IFn)load_db).invoke(((IFn)Connection$fn__21505.const__3.getRawRoot()).invoke(this.db_ref));
            v17 = connector;
            if (Util.classOf((Object)v17) == Connection$fn__21505.__cached_class__1) ** GOTO lbl62
            if (!(v17 instanceof TransactorConnector)) {
                v17 = v17;
                Connection$fn__21505.__cached_class__1 = Util.classOf((Object)v17);
lbl62:
                // 2 sources

                v18 = Connection$fn__21505.const__17.getRawRoot().invoke(v17, this.conn, failure_handler);
            } else {
                v18 = ((TransactorConnector)v17).create_notifier(this.conn, failure_handler);
            }
            notifier = v18;
            cleanup = ((IFn)Connection$fn__21505.const__1.getRawRoot()).invoke((Object)new Connection$fn__21505$fn__21515(shutdown, notifier));
            v19 = db;
            db = null;
            v20 = cleanup;
            cleanup = null;
            v21 = shutdown;
            shutdown = null;
            v22 = connector;
            connector = null;
            v23 = failure_handler;
            failure_handler = null;
            v24 = notifier;
            notifier = null;
            v25 = load_db;
            load_db = null;
            v0[1] = ((IFn)new Connection$fn__21505$fn__21519(v19, v20, this.conn, v21, v22, v23, this.unsent_updates_queue, v24, (Object)v25)).invoke();
            var12_14 = RT.mapUniqueKeys((Object[])v0);
        }
        catch (Throwable t__8983__auto__) {
            v26 = new Object[2];
            v26[0] = Connection$fn__21505.const__18;
            t__8983__auto__ = null;
            v26[1] = t__8983__auto__;
            var12_14 = RT.mapUniqueKeys((Object[])v26);
        }
        return var12_14;
    }

    static {
        const__0 = RT.keyword(null, (String)"returned");
        const__1 = RT.var((String)"datomic.error", (String)"runonce");
        const__2 = RT.var((String)"clojure.core", (String)"comp");
        const__3 = RT.var((String)"clojure.core", (String)"deref");
        const__4 = RT.var((String)"datomic.common", (String)"async-shutdown");
        const__5 = RT.var((String)"datomic.connector", (String)"create-transactor-hornet-connector");
        const__6 = RT.var((String)"datomic.connector", (String)"admin-request*");
        const__7 = RT.keyword(null, (String)"start-database");
        const__9 = RT.keyword(null, (String)"initial");
        const__10 = 60000L;
        const__11 = 5000L;
        const__13 = RT.keyword(null, (String)"message");
        const__14 = RT.var((String)"datomic.peer", (String)"connection-lock");
        const__15 = RT.var((String)"clojure.core", (String)"ex-info");
        const__16 = RT.var((String)"clojure.core", (String)"dissoc");
        const__17 = RT.var((String)"datomic.connector", (String)"create-notifier");
        const__18 = RT.keyword(null, (String)"threw");
        __site__0__ = new KeywordLookupSite(RT.keyword((String)"db", (String)"error"));
        __thunk__0__ = __site__0__;
        __site__1__ = new KeywordLookupSite(RT.keyword(null, (String)"message"));
        __thunk__1__ = __site__1__;
        __site__2__ = new KeywordLookupSite(RT.keyword(null, (String)"message"));
        __thunk__2__ = __site__2__;
    }
}

