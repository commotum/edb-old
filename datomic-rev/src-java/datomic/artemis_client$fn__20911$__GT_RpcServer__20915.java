/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 */
package datomic;

import clojure.lang.AFunction;
import datomic.artemis_client.RpcServer;

public final class artemis_client$fn__20911$__GT_RpcServer__20915
extends AFunction {
    public Object invoke(Object consumer, Object producer, Object cleanup2) {
        Object object = consumer;
        consumer = null;
        Object object2 = producer;
        producer = null;
        Object object3 = cleanup2;
        cleanup2 = null;
        return new RpcServer(object, object2, object3);
    }
}

