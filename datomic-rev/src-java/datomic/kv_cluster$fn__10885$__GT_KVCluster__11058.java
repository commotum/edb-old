/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 */
package datomic;

import clojure.lang.AFunction;
import datomic.kv_cluster.KVCluster;

public final class kv_cluster$fn__10885$__GT_KVCluster__11058
extends AFunction {
    public Object invoke(Object kvs, Object path_map, Object exec, Object retrying_write, Object retrying_read, Object retrying_delete, Object protocol, Object protocol_nsec_k, Object pod_garbage_handler) {
        Object object = kvs;
        kvs = null;
        Object object2 = path_map;
        path_map = null;
        Object object3 = exec;
        exec = null;
        Object object4 = retrying_write;
        retrying_write = null;
        Object object5 = retrying_read;
        retrying_read = null;
        Object object6 = retrying_delete;
        retrying_delete = null;
        Object object7 = protocol;
        protocol = null;
        Object object8 = protocol_nsec_k;
        protocol_nsec_k = null;
        Object object9 = pod_garbage_handler;
        pod_garbage_handler = null;
        return new KVCluster(object, object2, object3, object4, object5, object6, object7, object8, object9);
    }
}

