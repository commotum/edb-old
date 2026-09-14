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

public final class TransactorHornetConnector$fn__21266$fn__21270
extends AFunction {
    Object fut;
    Object hornet_producer;
    Object session;
    private static Class __cached_class__0;
    private static Class __cached_class__1;
    public static final Keyword const__0;
    public static final Var const__1;
    public static final Var const__2;
    public static final Var const__3;
    public static final Keyword const__4;

    public TransactorHornetConnector$fn__21266$fn__21270(Object object, Object object2, Object object3) {
        this.fut = object;
        this.hornet_producer = object2;
        this.session = object3;
    }

    /*
     * Unable to fully structure code
     */
    public Object invoke() {
        try {
            v0 = new Object[2];
            v0[0] = TransactorHornetConnector$fn__21266$fn__21270.const__0;
            this.fut = null;
            ((IFn)TransactorHornetConnector$fn__21266$fn__21270.const__1.getRawRoot()).invoke(this.fut);
            v1 = (IFn)TransactorHornetConnector$fn__21266$fn__21270.const__2.getRawRoot();
            v2 = this.hornet_producer;
            this.hornet_producer = null;
            v3 = v2;
            if (Util.classOf((Object)v2) == TransactorHornetConnector$fn__21266$fn__21270.__cached_class__0) ** GOTO lbl15
            if (!(v3 instanceof AsyncShutdown)) {
                v3 = v3;
                TransactorHornetConnector$fn__21266$fn__21270.__cached_class__0 = Util.classOf((Object)v3);
lbl15:
                // 2 sources

                v4 = TransactorHornetConnector$fn__21266$fn__21270.const__3.getRawRoot().invoke(v3);
            } else {
                v4 = ((AsyncShutdown)v3).async_shutdown();
            }
            v1.invoke(v4);
            v5 = (IFn)TransactorHornetConnector$fn__21266$fn__21270.const__2.getRawRoot();
            v6 = this.session;
            this.session = null;
            v7 = v6;
            if (Util.classOf((Object)v6) == TransactorHornetConnector$fn__21266$fn__21270.__cached_class__1) ** GOTO lbl28
            if (!(v7 instanceof AsyncShutdown)) {
                v7 = v7;
                TransactorHornetConnector$fn__21266$fn__21270.__cached_class__1 = Util.classOf((Object)v7);
lbl28:
                // 2 sources

                v8 = TransactorHornetConnector$fn__21266$fn__21270.const__3.getRawRoot().invoke(v7);
            } else {
                v8 = ((AsyncShutdown)v7).async_shutdown();
            }
            v0[1] = v5.invoke(v8);
            var1_1 = RT.mapUniqueKeys((Object[])v0);
        }
        catch (Throwable t__8983__auto__) {
            v9 = new Object[2];
            v9[0] = TransactorHornetConnector$fn__21266$fn__21270.const__4;
            t__8983__auto__ = null;
            v9[1] = t__8983__auto__;
            var1_1 = RT.mapUniqueKeys((Object[])v9);
        }
        return var1_1;
    }

    static {
        const__0 = RT.keyword(null, (String)"returned");
        const__1 = RT.var((String)"clojure.core", (String)"future-cancel");
        const__2 = RT.var((String)"clojure.core", (String)"deref");
        const__3 = RT.var((String)"datomic.common", (String)"async-shutdown");
        const__4 = RT.keyword(null, (String)"threw");
    }
}

