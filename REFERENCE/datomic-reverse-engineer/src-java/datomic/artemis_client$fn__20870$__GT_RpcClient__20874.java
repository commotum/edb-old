/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 */
package datomic;

import clojure.lang.AFunction;
import datomic.artemis_client.RpcClient;

public final class artemis_client$fn__20870$__GT_RpcClient__20874
extends AFunction {
    public Object invoke(Object session, Object producer, Object consumer, Object serializer, Object response_map, Object producer_queue, Object consumer_queue, Object cleanup2) {
        Object object = session;
        session = null;
        Object object2 = producer;
        producer = null;
        Object object3 = consumer;
        consumer = null;
        Object object4 = serializer;
        serializer = null;
        Object object5 = response_map;
        response_map = null;
        Object object6 = producer_queue;
        producer_queue = null;
        Object object7 = consumer_queue;
        consumer_queue = null;
        Object object8 = cleanup2;
        cleanup2 = null;
        return new RpcClient(object, object2, object3, object4, object5, object6, object7, object8);
    }
}

