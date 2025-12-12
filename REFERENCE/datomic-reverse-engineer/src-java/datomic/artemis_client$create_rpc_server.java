/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.IFn
 *  clojure.lang.ISeq
 *  clojure.lang.Keyword
 *  clojure.lang.PersistentHashMap
 *  clojure.lang.RT
 *  clojure.lang.RestFn
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.IFn;
import clojure.lang.ISeq;
import clojure.lang.Keyword;
import clojure.lang.PersistentHashMap;
import clojure.lang.RT;
import clojure.lang.RestFn;
import clojure.lang.Var;
import datomic.artemis_client$create_rpc_server$fn__20923;
import datomic.artemis_client$create_rpc_server$fn__20927;
import datomic.artemis_client$create_rpc_server$fn__20931;
import datomic.artemis_client$create_rpc_server$fn__20935;
import datomic.artemis_client$create_rpc_server$fn__20939;
import datomic.artemis_client$create_rpc_server$fn__20943;
import datomic.artemis_client$create_rpc_server$fn__20949;
import datomic.artemis_client$create_rpc_server$fn__20951;
import datomic.artemis_client$create_rpc_server$fn__20953;
import datomic.artemis_client$create_rpc_server$fn__20955;
import datomic.artemis_client$create_rpc_server$fn__20957;
import datomic.artemis_client.RpcServer;

public final class artemis_client$create_rpc_server
extends RestFn {
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"seq?");
    public static final Var const__3 = RT.var((String)"clojure.core", (String)"seq");
    public static final Keyword const__5 = RT.keyword(null, (String)"read-handlers");
    public static final Keyword const__6 = RT.keyword(null, (String)"write-handlers");
    public static final Var const__7 = RT.var((String)"datomic.artemis-client", (String)"create-serializer");
    public static final Var const__8 = RT.var((String)"datomic.artemis-client", (String)"create-deserializer");
    public static final Var const__9 = RT.var((String)"clojure.core", (String)"partial");
    public static final Var const__10 = RT.var((String)"datomic.artemis-client", (String)"create-temporary-queue");
    public static final Var const__11 = RT.var((String)"datomic.artemis-client", (String)"delete-queue");
    public static final Var const__12 = RT.var((String)"datomic.error", (String)"runonce");
    public static final Var const__13 = RT.var((String)"datomic.artemis-client", (String)"create-consumer");
    public static final Var const__14 = RT.var((String)"datomic.artemis-client", (String)"create-producer");
    public static final Var const__15 = RT.var((String)"datomic.artemis-client", (String)"set-handler");

    public static Object invokeStatic(Object session_fn, Object request_address, Object response_address, Object handler, ISeq p__20918) {
        RpcServer rpcServer;
        Object object;
        ISeq vec__20919;
        ISeq iSeq = p__20918;
        p__20918 = null;
        ISeq iSeq2 = vec__20919 = iSeq;
        vec__20919 = null;
        Object map__20922 = RT.nth((Object)iSeq2, (int)RT.intCast((long)0L), null);
        Object object2 = ((IFn)const__2.getRawRoot()).invoke(map__20922);
        if (object2 != null && object2 != Boolean.FALSE) {
            Object object3 = map__20922;
            map__20922 = null;
            object = PersistentHashMap.create((ISeq)((ISeq)((IFn)const__3.getRawRoot()).invoke(object3)));
        } else {
            object = map__20922;
            map__20922 = null;
        }
        Object map__209222 = object;
        Object read_handlers = RT.get((Object)map__209222, (Object)const__5);
        Object object4 = map__209222;
        map__209222 = null;
        Object write_handlers2 = RT.get((Object)object4, (Object)const__6);
        Object object5 = session_fn;
        session_fn = null;
        Object session = ((IFn)object5).invoke();
        Object object6 = write_handlers2;
        write_handlers2 = null;
        Object serializer = ((IFn)const__7.getRawRoot()).invoke(object6);
        Object object7 = read_handlers;
        read_handlers = null;
        Object deserializer = ((IFn)const__8.getRawRoot()).invoke(object7);
        Object cq = ((IFn)const__9.getRawRoot()).invoke(const__10.getRawRoot(), session);
        Object dq = ((IFn)const__9.getRawRoot()).invoke(const__11.getRawRoot(), session);
        Object object8 = session;
        session = null;
        Object session2 = object8;
        Object cleanup2 = ((IFn)const__12.getRawRoot()).invoke((Object)new artemis_client$create_rpc_server$fn__20923(session2));
        try {
            RpcServer rpcServer2;
            Object object9 = request_address;
            request_address = null;
            Object request_queue = ((IFn)cq).invoke(object9);
            Object object10 = cleanup2;
            cleanup2 = null;
            Object cleanup3 = ((IFn)const__12.getRawRoot()).invoke((Object)new artemis_client$create_rpc_server$fn__20927(object10, request_queue, dq));
            try {
                RpcServer rpcServer3;
                Object object11 = cq;
                cq = null;
                Object response_queue = ((IFn)object11).invoke(response_address);
                Object object12 = cleanup3;
                cleanup3 = null;
                Object cleanup4 = ((IFn)const__12.getRawRoot()).invoke((Object)new artemis_client$create_rpc_server$fn__20931(object12, dq, response_queue));
                try {
                    RpcServer rpcServer4;
                    Object consumer = ((IFn)const__13.getRawRoot()).invoke(session2, request_queue);
                    Object object13 = cleanup4;
                    cleanup4 = null;
                    Object cleanup5 = ((IFn)const__12.getRawRoot()).invoke((Object)new artemis_client$create_rpc_server$fn__20935(object13, consumer));
                    try {
                        RpcServer rpcServer5;
                        Object object14 = response_address;
                        response_address = null;
                        Object producer = ((IFn)const__14.getRawRoot()).invoke(session2, object14);
                        Object object15 = cleanup5;
                        cleanup5 = null;
                        Object cleanup6 = ((IFn)const__12.getRawRoot()).invoke((Object)new artemis_client$create_rpc_server$fn__20939(producer, object15));
                        try {
                            Object object16 = serializer;
                            serializer = null;
                            Object object17 = handler;
                            handler = null;
                            Object object18 = deserializer;
                            deserializer = null;
                            ((IFn)const__15.getRawRoot()).invoke(consumer, (Object)new artemis_client$create_rpc_server$fn__20943(object16, producer, object17, session2, object18));
                            Object object19 = cleanup6;
                            cleanup6 = null;
                            rpcServer5 = new RpcServer(consumer, producer, object19);
                        }
                        catch (Throwable t__709__auto__2) {
                            Object object20 = producer;
                            producer = null;
                            ((IFn)new artemis_client$create_rpc_server$fn__20949(object20)).invoke();
                            Object t__709__auto__2 = null;
                            throw t__709__auto__2;
                        }
                        rpcServer4 = rpcServer5;
                    }
                    catch (Throwable t__709__auto__3) {
                        Object object21 = consumer;
                        consumer = null;
                        ((IFn)new artemis_client$create_rpc_server$fn__20951(object21)).invoke();
                        Object t__709__auto__3 = null;
                        throw t__709__auto__3;
                    }
                    rpcServer3 = rpcServer4;
                }
                catch (Throwable t__709__auto__4) {
                    Object object22 = response_queue;
                    response_queue = null;
                    ((IFn)new artemis_client$create_rpc_server$fn__20953(dq, object22)).invoke();
                    Object t__709__auto__4 = null;
                    throw t__709__auto__4;
                }
                rpcServer2 = rpcServer3;
            }
            catch (Throwable t__709__auto__5) {
                Object object23 = request_queue;
                request_queue = null;
                Object object24 = dq;
                dq = null;
                ((IFn)new artemis_client$create_rpc_server$fn__20955(object23, object24)).invoke();
                Object t__709__auto__5 = null;
                throw t__709__auto__5;
            }
            rpcServer = rpcServer2;
        }
        catch (Throwable t__709__auto__6) {
            Object object25 = session2;
            session2 = null;
            ((IFn)new artemis_client$create_rpc_server$fn__20957(object25)).invoke();
            Object t__709__auto__6 = null;
            throw t__709__auto__6;
        }
        return rpcServer;
    }

    public Object doInvoke(Object object, Object object2, Object object3, Object object4, Object object5) {
        Object object6 = object;
        object = null;
        Object object7 = object2;
        object2 = null;
        Object object8 = object3;
        object3 = null;
        Object object9 = object4;
        object4 = null;
        ISeq iSeq = (ISeq)object5;
        object5 = null;
        return artemis_client$create_rpc_server.invokeStatic(object6, object7, object8, object9, iSeq);
    }

    public int getRequiredArity() {
        return 4;
    }
}

