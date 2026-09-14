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
 *  clojure.lang.Var
 */
package datomic.artemis_client;

import clojure.lang.IFn;
import clojure.lang.IObj;
import clojure.lang.IPersistentVector;
import clojure.lang.IType;
import clojure.lang.RT;
import clojure.lang.Symbol;
import clojure.lang.Var;
import datomic.artemis_client.RpcClient$fn__20871;
import datomic.common.AsyncShutdown;

public final class RpcClient
implements AsyncShutdown,
IType {
    public final Object session;
    public final Object producer;
    public final Object consumer;
    public final Object serializer;
    public final Object response_map;
    public final Object producer_queue;
    public final Object consumer_queue;
    public final Object cleanup;
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"future-call");

    public RpcClient(Object object, Object object2, Object object3, Object object4, Object object5, Object object6, Object object7, Object object8) {
        this.session = object;
        this.producer = object2;
        this.consumer = object3;
        this.serializer = object4;
        this.response_map = object5;
        this.producer_queue = object6;
        this.consumer_queue = object7;
        this.cleanup = object8;
    }

    public static IPersistentVector getBasis() {
        return RT.vector((Object[])new Object[]{((IObj)Symbol.intern(null, (String)"session")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), Symbol.intern(null, (String)"ClientSession")})), ((IObj)Symbol.intern(null, (String)"producer")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), Symbol.intern(null, (String)"ClientProducer")})), ((IObj)Symbol.intern(null, (String)"consumer")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), Symbol.intern(null, (String)"ClientConsumer")})), Symbol.intern(null, (String)"serializer"), Symbol.intern(null, (String)"response-map"), ((IObj)Symbol.intern(null, (String)"producer-queue")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), Symbol.intern(null, (String)"String")})), ((IObj)Symbol.intern(null, (String)"consumer-queue")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), Symbol.intern(null, (String)"String")})), Symbol.intern(null, (String)"cleanup")});
    }

    public Object async_shutdown() {
        RpcClient this_ = null;
        return ((IFn)const__0.getRawRoot()).invoke((Object)new RpcClient$fn__20871(this_.cleanup));
    }
}

