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
package datomic.artemis_client;

import clojure.lang.IFn;
import clojure.lang.IObj;
import clojure.lang.IPersistentVector;
import clojure.lang.IType;
import clojure.lang.RT;
import clojure.lang.Symbol;
import clojure.lang.Tuple;
import clojure.lang.Var;
import datomic.artemis_client.RpcServer$fn__20912;
import datomic.common.AsyncShutdown;

public final class RpcServer
implements AsyncShutdown,
IType {
    public final Object consumer;
    public final Object producer;
    public final Object cleanup;
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"future-call");

    public RpcServer(Object object, Object object2, Object object3) {
        this.consumer = object;
        this.producer = object2;
        this.cleanup = object3;
    }

    public static IPersistentVector getBasis() {
        return Tuple.create((Object)((IObj)Symbol.intern(null, (String)"consumer")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), Symbol.intern(null, (String)"ClientConsumer")})), (Object)((IObj)Symbol.intern(null, (String)"producer")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), Symbol.intern(null, (String)"ClientProducer")})), (Object)Symbol.intern(null, (String)"cleanup"));
    }

    public Object async_shutdown() {
        RpcServer this_ = null;
        return ((IFn)const__0.getRawRoot()).invoke((Object)new RpcServer$fn__20912(this_.cleanup));
    }
}

