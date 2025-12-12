/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.RT
 *  clojure.lang.Util
 *  clojure.lang.Var
 */
package datomic.peer;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.RT;
import clojure.lang.Util;
import clojure.lang.Var;
import datomic.connector.NotificationHandler;
import datomic.connector.Startable;
import datomic.connector.TransactorConnector;
import datomic.peer.Connection$fn__21505$fn__21519$fn__21520;
import datomic.peer.Connection$fn__21505$fn__21519$fn__21524;
import datomic.peer.Connection$fn__21505$fn__21519$fn__21528;
import datomic.peer.Connection$fn__21505$fn__21519$fn__21530;
import datomic.peer.Connection$fn__21505$fn__21519$fn__21532;
import datomic.peer.ConnectionState;

public final class Connection$fn__21505$fn__21519
extends AFunction {
    Object db;
    Object cleanup;
    Object conn;
    Object shutdown;
    Object connector;
    Object failure_handler;
    Object unsent_updates_queue;
    Object notifier;
    Object load_db;
    private static Class __cached_class__0;
    private static Class __cached_class__1;
    private static Class __cached_class__2;
    public static final Var const__0;
    public static final Var const__1;
    public static final Var const__2;
    public static final Var const__3;
    public static final Var const__4;
    public static final Var const__5;
    public static final Var const__6;

    public Connection$fn__21505$fn__21519(Object object, Object object2, Object object3, Object object4, Object object5, Object object6, Object object7, Object object8, Object object9) {
        this.db = object;
        this.cleanup = object2;
        this.conn = object3;
        this.shutdown = object4;
        this.connector = object5;
        this.failure_handler = object6;
        this.unsent_updates_queue = object7;
        this.notifier = object8;
        this.load_db = object9;
    }

    /*
     * Unable to fully structure code
     */
    public Object invoke() {
        try {
            v0 = this.conn;
            if (Util.classOf((Object)v0) == Connection$fn__21505$fn__21519.__cached_class__0) ** GOTO lbl7
            if (!(v0 instanceof NotificationHandler)) {
                v0 = v0;
                Connection$fn__21505$fn__21519.__cached_class__0 = Util.classOf((Object)v0);
lbl7:
                // 2 sources

                this.load_db = null;
                this.db = null;
                v1 = Connection$fn__21505$fn__21519.const__0.getRawRoot().invoke(v0, ((IFn)this.load_db).invoke(this.db));
            } else {
                this.load_db = null;
                this.db = null;
                v1 = ((NotificationHandler)v0).notify_db(((IFn)this.load_db).invoke(this.db));
            }
            _ = v1;
            this.cleanup = null;
            cleanup = ((IFn)Connection$fn__21505$fn__21519.const__1.getRawRoot()).invoke((Object)new Connection$fn__21505$fn__21519$fn__21520(this.cleanup, _));
            try {
                v2 = this.connector;
                if (Util.classOf((Object)v2) == Connection$fn__21505$fn__21519.__cached_class__1) ** GOTO lbl23
                if (!(v2 instanceof TransactorConnector)) {
                    v2 = v2;
                    Connection$fn__21505$fn__21519.__cached_class__1 = Util.classOf((Object)v2);
lbl23:
                    // 2 sources

                    this.failure_handler = null;
                    v3 = Connection$fn__21505$fn__21519.const__2.getRawRoot().invoke(v2, this.unsent_updates_queue, this.conn, this.failure_handler);
                } else {
                    this.failure_handler = null;
                    v3 = ((TransactorConnector)v2).start_updater(this.unsent_updates_queue, this.conn, this.failure_handler);
                }
                updater = v3;
                v4 = cleanup;
                cleanup = null;
                cleanup = ((IFn)Connection$fn__21505$fn__21519.const__1.getRawRoot()).invoke((Object)new Connection$fn__21505$fn__21519$fn__21524(v4, this.shutdown, updater));
                try {
                    v5 = this.notifier;
                    if (Util.classOf((Object)v5) == Connection$fn__21505$fn__21519.__cached_class__2) ** GOTO lbl38
                    if (!(v5 instanceof Startable)) {
                        v5 = v5;
                        Connection$fn__21505$fn__21519.__cached_class__2 = Util.classOf((Object)v5);
lbl38:
                        // 2 sources

                        v6 = Connection$fn__21505$fn__21519.const__3.getRawRoot().invoke(v5);
                    } else {
                        v6 = ((Startable)v5).start();
                    }
                    this.connector = null;
                    state = new ConnectionState(this.connector, this.notifier, updater, cleanup);
                    v7 = cleanup;
                    cleanup = null;
                    ((IFn)Connection$fn__21505$fn__21519.const__4.getRawRoot()).invoke(((IFn)Connection$fn__21505$fn__21519.const__5.getRawRoot()).invoke(Connection$fn__21505$fn__21519.const__6.getRawRoot()), (Object)state, v7);
                    v8 = state;
                    state = null;
                    var6_12 = v8;
                }
                catch (Throwable t__709__auto__) {
                    v9 = updater;
                    updater = null;
                    ((IFn)new Connection$fn__21505$fn__21519$fn__21528(this.shutdown, v9)).invoke();
                    t__709__auto__ = null;
                    throw t__709__auto__;
                }
                var7_13 = var6_12;
            }
            catch (Throwable t__709__auto__) {
                v10 = _;
                _ = null;
                ((IFn)new Connection$fn__21505$fn__21519$fn__21530(v10)).invoke();
                t__709__auto__ = null;
                throw t__709__auto__;
            }
            var8_14 = var7_13;
        }
        catch (Throwable t__709__auto__) {
            this.shutdown = null;
            this.notifier = null;
            ((IFn)new Connection$fn__21505$fn__21519$fn__21532(this.shutdown, this.notifier)).invoke();
            t__709__auto__ = null;
            throw t__709__auto__;
        }
        return var8_14;
    }

    static {
        const__0 = RT.var((String)"datomic.connector", (String)"notify-db");
        const__1 = RT.var((String)"datomic.error", (String)"runonce");
        const__2 = RT.var((String)"datomic.connector", (String)"start-updater");
        const__3 = RT.var((String)"datomic.connector", (String)"start");
        const__4 = RT.var((String)"datomic.cleanup", (String)"register-cleanup");
        const__5 = RT.var((String)"clojure.core", (String)"deref");
        const__6 = RT.var((String)"datomic.cleanup", (String)"shared-manager-ref");
    }
}

