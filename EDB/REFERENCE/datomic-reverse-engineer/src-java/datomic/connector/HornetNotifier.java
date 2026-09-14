/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.IFn
 *  clojure.lang.IObj
 *  clojure.lang.IPersistentVector
 *  clojure.lang.IType
 *  clojure.lang.RT
 *  clojure.lang.Symbol
 *  clojure.lang.Tuple
 *  clojure.lang.Var
 */
package datomic.connector;

import clojure.lang.IFn;
import clojure.lang.IObj;
import clojure.lang.IPersistentVector;
import clojure.lang.IType;
import clojure.lang.RT;
import clojure.lang.Symbol;
import clojure.lang.Tuple;
import clojure.lang.Var;
import datomic.common.AsyncShutdown;
import datomic.connector.HornetNotifier$fn__21171;
import datomic.connector.Startable;

public final class HornetNotifier
implements AsyncShutdown,
Startable,
IType {
    public final Object push_handler_ref;
    public final Object session;
    public final Object result_queue;
    public final Object hornet_consumer;
    public final Object starter;
    public final Object cleanup;
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"future-call");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"deref");

    public HornetNotifier(Object object, Object object2, Object object3, Object object4, Object object5, Object object6) {
        this.push_handler_ref = object;
        this.session = object2;
        this.result_queue = object3;
        this.hornet_consumer = object4;
        this.starter = object5;
        this.cleanup = object6;
    }

    public static IPersistentVector getBasis() {
        return Tuple.create((Object)((IObj)Symbol.intern(null, (String)"push-handler-ref")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), Symbol.intern(null, (String)"WeakReference")})), (Object)Symbol.intern(null, (String)"session"), (Object)Symbol.intern(null, (String)"result-queue"), (Object)Symbol.intern(null, (String)"hornet-consumer"), (Object)Symbol.intern(null, (String)"starter"), (Object)Symbol.intern(null, (String)"cleanup"));
    }

    public Object start() {
        HornetNotifier this_ = null;
        return ((IFn)const__1.getRawRoot()).invoke(this_.starter);
    }

    public Object async_shutdown() {
        HornetNotifier this_ = null;
        return ((IFn)const__0.getRawRoot()).invoke((Object)new HornetNotifier$fn__21171(this_.cleanup));
    }
}

