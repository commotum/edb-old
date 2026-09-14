/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.Keyword
 *  clojure.lang.RT
 *  clojure.lang.Util
 *  clojure.lang.Var
 */
package datomic.connector;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Keyword;
import clojure.lang.RT;
import clojure.lang.Util;
import clojure.lang.Var;
import datomic.common.AsyncShutdown;

public final class TransactorHornetConnector$fn__21227
extends AFunction {
    Object arg;
    Object timeout_msec;
    Object transactor_endpoint;
    Object hornet_factory;
    Object request;
    Object timeout;
    private static Class __cached_class__0;
    private static Class __cached_class__1;
    public static final Var const__0;
    public static final Keyword const__1;
    public static final Var const__2;
    public static final Var const__3;
    public static final Var const__4;
    public static final Var const__5;
    public static final Var const__6;
    public static final Var const__7;
    public static final Var const__8;
    public static final Var const__9;
    public static final Var const__10;
    public static final Var const__11;

    public TransactorHornetConnector$fn__21227(Object object, Object object2, Object object3, Object object4, Object object5, Object object6) {
        this.arg = object;
        this.timeout_msec = object2;
        this.transactor_endpoint = object3;
        this.hornet_factory = object4;
        this.request = object5;
        this.timeout = object6;
    }

    /*
     * Unable to fully structure code
     */
    public Object invoke() {
        try {
            session = ((IFn)TransactorHornetConnector$fn__21227.const__0.getRawRoot()).invoke(this.hornet_factory, this.transactor_endpoint, (Object)TransactorHornetConnector$fn__21227.const__1, (Object)Boolean.TRUE);
            try {
                rpc_client = ((IFn)TransactorHornetConnector$fn__21227.const__2.getRawRoot()).invoke(session, (Object)"admin.request", (Object)"admin.response");
                try {
                    this.request = null;
                    this.arg = null;
                    this.timeout_msec = null;
                    this.timeout = null;
                    var3_5 = ((IFn)TransactorHornetConnector$fn__21227.const__3.getRawRoot()).invoke(((IFn)TransactorHornetConnector$fn__21227.const__4.getRawRoot()).invoke(rpc_client, ((IFn)TransactorHornetConnector$fn__21227.const__5.getRawRoot()).invoke(((IFn)TransactorHornetConnector$fn__21227.const__6.getRawRoot()).invoke(((IFn)TransactorHornetConnector$fn__21227.const__7.getRawRoot()).invoke(this.request), ((IFn)TransactorHornetConnector$fn__21227.const__7.getRawRoot()).invoke(this.arg)))), this.timeout_msec, this.timeout);
                }
                catch (Throwable var4_6) {
                    v0 = (IFn)TransactorHornetConnector$fn__21227.const__3.getRawRoot();
                    v1 = rpc_client;
                    rpc_client = null;
                    v2 = v1;
                    if (Util.classOf((Object)v1) == TransactorHornetConnector$fn__21227.__cached_class__0) ** GOTO lbl20
                    if (!(v2 instanceof AsyncShutdown)) {
                        v2 = v2;
                        TransactorHornetConnector$fn__21227.__cached_class__0 = Util.classOf((Object)v2);
lbl20:
                        // 2 sources

                        v3 = TransactorHornetConnector$fn__21227.const__8.getRawRoot().invoke(v2);
                    } else {
                        v3 = ((AsyncShutdown)v2).async_shutdown();
                    }
                    v0.invoke(v3);
                    throw var4_6;
                }
                v4 = (IFn)TransactorHornetConnector$fn__21227.const__3.getRawRoot();
                v5 = rpc_client;
                rpc_client = null;
                v6 = v5;
                if (Util.classOf((Object)v5) == TransactorHornetConnector$fn__21227.__cached_class__0) ** GOTO lbl34
                if (!(v6 instanceof AsyncShutdown)) {
                    v6 = v6;
                    TransactorHornetConnector$fn__21227.__cached_class__0 = Util.classOf((Object)v6);
lbl34:
                    // 2 sources

                    v7 = TransactorHornetConnector$fn__21227.const__8.getRawRoot().invoke(v6);
                } else {
                    v7 = ((AsyncShutdown)v6).async_shutdown();
                }
                v4.invoke(v7);
                var5_7 = var3_5;
            }
            catch (Throwable var6_8) {
                v8 = (IFn)TransactorHornetConnector$fn__21227.const__3.getRawRoot();
                v9 = session;
                session = null;
                v10 = v9;
                if (Util.classOf((Object)v9) == TransactorHornetConnector$fn__21227.__cached_class__1) ** GOTO lbl50
                if (!(v10 instanceof AsyncShutdown)) {
                    v10 = v10;
                    TransactorHornetConnector$fn__21227.__cached_class__1 = Util.classOf((Object)v10);
lbl50:
                    // 2 sources

                    v11 = TransactorHornetConnector$fn__21227.const__8.getRawRoot().invoke(v10);
                } else {
                    v11 = ((AsyncShutdown)v10).async_shutdown();
                }
                v8.invoke(v11);
                throw var6_8;
            }
            v12 = (IFn)TransactorHornetConnector$fn__21227.const__3.getRawRoot();
            v13 = session;
            session = null;
            v14 = v13;
            if (Util.classOf((Object)v13) == TransactorHornetConnector$fn__21227.__cached_class__1) ** GOTO lbl64
            if (!(v14 instanceof AsyncShutdown)) {
                v14 = v14;
                TransactorHornetConnector$fn__21227.__cached_class__1 = Util.classOf((Object)v14);
lbl64:
                // 2 sources

                v15 = TransactorHornetConnector$fn__21227.const__8.getRawRoot().invoke(v14);
            } else {
                v15 = ((AsyncShutdown)v14).async_shutdown();
            }
            v12.invoke(v15);
            var7_9 = var5_7;
        }
        catch (Throwable t) {
            ((IFn)TransactorHornetConnector$fn__21227.const__9.getRawRoot()).invoke(TransactorHornetConnector$fn__21227.const__10.getRawRoot());
            t = null;
            throw (Throwable)((IFn)TransactorHornetConnector$fn__21227.const__11.getRawRoot()).invoke(this.transactor_endpoint, (Object)t);
        }
        return var7_9;
    }

    static {
        const__0 = RT.var((String)"datomic.artemis-client", (String)"start-session");
        const__1 = RT.keyword(null, (String)"pre-acknowledge");
        const__2 = RT.var((String)"datomic.artemis-client", (String)"create-rpc-client");
        const__3 = RT.var((String)"clojure.core", (String)"deref");
        const__4 = RT.var((String)"datomic.artemis-client", (String)"rpc-request");
        const__5 = RT.var((String)"clojure.core", (String)"seq");
        const__6 = RT.var((String)"clojure.core", (String)"concat");
        const__7 = RT.var((String)"clojure.core", (String)"list");
        const__8 = RT.var((String)"datomic.common", (String)"async-shutdown");
        const__9 = RT.var((String)"datomic.cache", (String)"clear");
        const__10 = RT.var((String)"datomic.connector", (String)"sfb-cache");
        const__11 = RT.var((String)"datomic.connector", (String)"endpoint-error");
    }
}

