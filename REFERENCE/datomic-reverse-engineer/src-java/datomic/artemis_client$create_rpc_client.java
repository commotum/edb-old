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
import datomic.artemis_client$create_rpc_client$fn__20882;
import datomic.artemis_client$create_rpc_client$fn__20886;
import datomic.artemis_client$create_rpc_client$fn__20890;
import datomic.artemis_client$create_rpc_client$fn__20894;
import datomic.artemis_client$create_rpc_client$fn__20898;
import datomic.artemis_client$create_rpc_client$fn__20902;
import datomic.artemis_client$create_rpc_client$fn__20904;
import datomic.artemis_client$create_rpc_client$fn__20906;
import datomic.artemis_client$create_rpc_client$fn__20908;
import datomic.artemis_client.RpcClient;

public final class artemis_client$create_rpc_client
extends RestFn {
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"seq?");
    public static final Var const__3 = RT.var((String)"clojure.core", (String)"seq");
    public static final Keyword const__5 = RT.keyword(null, (String)"read-handlers");
    public static final Keyword const__6 = RT.keyword(null, (String)"write-handlers");
    public static final Var const__7 = RT.var((String)"datomic.artemis-client", (String)"create-serializer");
    public static final Var const__8 = RT.var((String)"datomic.artemis-client", (String)"create-deserializer");
    public static final Var const__9 = RT.var((String)"datomic.cache", (String)"create-response-map");
    public static final Object const__10 = 2L;
    public static final Var const__11 = RT.var((String)"clojure.core", (String)"partial");
    public static final Var const__12 = RT.var((String)"datomic.artemis-client", (String)"create-temporary-queue");
    public static final Var const__13 = RT.var((String)"datomic.artemis-client", (String)"delete-queue");
    public static final Var const__14 = RT.var((String)"datomic.error", (String)"runonce");
    public static final Var const__15 = RT.var((String)"datomic.artemis-client", (String)"create-producer");
    public static final Var const__16 = RT.var((String)"datomic.artemis-client", (String)"create-consumer");
    public static final Var const__17 = RT.var((String)"datomic.artemis-client", (String)"set-handler");

    public static Object invokeStatic(Object session, Object request_address, Object response_address, ISeq p__20877) {
        RpcClient rpcClient;
        Object write_handlers2;
        Object object;
        ISeq vec__20878;
        ISeq iSeq = p__20877;
        p__20877 = null;
        ISeq iSeq2 = vec__20878 = iSeq;
        vec__20878 = null;
        Object map__20881 = RT.nth((Object)iSeq2, (int)RT.intCast((long)0L), null);
        Object object2 = ((IFn)const__2.getRawRoot()).invoke(map__20881);
        if (object2 != null && object2 != Boolean.FALSE) {
            Object object3 = map__20881;
            map__20881 = null;
            object = PersistentHashMap.create((ISeq)((ISeq)((IFn)const__3.getRawRoot()).invoke(object3)));
        } else {
            object = map__20881;
            map__20881 = null;
        }
        Object map__208812 = object;
        Object read_handlers = RT.get((Object)map__208812, (Object)const__5);
        Object object4 = map__208812;
        map__208812 = null;
        Object object5 = write_handlers2 = RT.get((Object)object4, (Object)const__6);
        write_handlers2 = null;
        Object serializer = ((IFn)const__7.getRawRoot()).invoke(object5);
        Object object6 = read_handlers;
        read_handlers = null;
        Object deserializer = ((IFn)const__8.getRawRoot()).invoke(object6);
        Object response_map = ((IFn)const__9.getRawRoot()).invoke(const__10);
        Object cq = ((IFn)const__11.getRawRoot()).invoke(const__12.getRawRoot(), session);
        Object dq = ((IFn)const__11.getRawRoot()).invoke(const__13.getRawRoot(), session);
        Object request_queue = ((IFn)cq).invoke(request_address);
        Object cleanup2 = ((IFn)const__14.getRawRoot()).invoke((Object)new artemis_client$create_rpc_client$fn__20882(request_queue, dq));
        try {
            RpcClient rpcClient2;
            Object object7 = cq;
            cq = null;
            Object object8 = response_address;
            response_address = null;
            Object response_queue = ((IFn)object7).invoke(object8);
            Object object9 = cleanup2;
            cleanup2 = null;
            Object cleanup3 = ((IFn)const__14.getRawRoot()).invoke((Object)new artemis_client$create_rpc_client$fn__20886(dq, response_queue, object9));
            try {
                RpcClient rpcClient3;
                Object object10 = request_address;
                request_address = null;
                Object producer = ((IFn)const__15.getRawRoot()).invoke(session, object10);
                Object object11 = cleanup3;
                cleanup3 = null;
                Object cleanup4 = ((IFn)const__14.getRawRoot()).invoke((Object)new artemis_client$create_rpc_client$fn__20890(producer, object11));
                try {
                    RpcClient rpcClient4;
                    Object consumer = ((IFn)const__16.getRawRoot()).invoke(session, response_queue);
                    Object object12 = cleanup4;
                    cleanup4 = null;
                    Object cleanup5 = ((IFn)const__14.getRawRoot()).invoke((Object)new artemis_client$create_rpc_client$fn__20894(consumer, object12));
                    try {
                        Object object13 = deserializer;
                        deserializer = null;
                        ((IFn)const__17.getRawRoot()).invoke(consumer, (Object)new artemis_client$create_rpc_client$fn__20898(object13, response_map));
                        Object object14 = session;
                        session = null;
                        Object object15 = serializer;
                        serializer = null;
                        Object object16 = response_map;
                        response_map = null;
                        Object object17 = cleanup5;
                        cleanup5 = null;
                        rpcClient4 = new RpcClient(object14, producer, consumer, object15, object16, request_queue, response_queue, object17);
                    }
                    catch (Throwable t__709__auto__2) {
                        Object object18 = consumer;
                        consumer = null;
                        ((IFn)new artemis_client$create_rpc_client$fn__20902(object18)).invoke();
                        Object t__709__auto__2 = null;
                        throw t__709__auto__2;
                    }
                    rpcClient3 = rpcClient4;
                }
                catch (Throwable t__709__auto__3) {
                    Object object19 = producer;
                    producer = null;
                    ((IFn)new artemis_client$create_rpc_client$fn__20904(object19)).invoke();
                    Object t__709__auto__3 = null;
                    throw t__709__auto__3;
                }
                rpcClient2 = rpcClient3;
            }
            catch (Throwable t__709__auto__4) {
                Object object20 = response_queue;
                response_queue = null;
                ((IFn)new artemis_client$create_rpc_client$fn__20906(dq, object20)).invoke();
                Object t__709__auto__4 = null;
                throw t__709__auto__4;
            }
            rpcClient = rpcClient2;
        }
        catch (Throwable t__709__auto__5) {
            Object object21 = request_queue;
            request_queue = null;
            Object object22 = dq;
            dq = null;
            ((IFn)new artemis_client$create_rpc_client$fn__20908(object21, object22)).invoke();
            Object t__709__auto__5 = null;
            throw t__709__auto__5;
        }
        return rpcClient;
    }

    public Object doInvoke(Object object, Object object2, Object object3, Object object4) {
        Object object5 = object;
        object = null;
        Object object6 = object2;
        object2 = null;
        Object object7 = object3;
        object3 = null;
        ISeq iSeq = (ISeq)object4;
        object4 = null;
        return artemis_client$create_rpc_client.invokeStatic(object5, object6, object7, iSeq);
    }

    public int getRequiredArity() {
        return 3;
    }
}

