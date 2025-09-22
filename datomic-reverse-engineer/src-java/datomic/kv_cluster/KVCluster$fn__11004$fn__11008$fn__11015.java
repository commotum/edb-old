/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.Delay
 *  clojure.lang.IFn
 */
package datomic.kv_cluster;

import clojure.lang.AFunction;
import clojure.lang.Delay;
import clojure.lang.IFn;
import datomic.kv_cluster.KVCluster$fn__11004$fn__11008$fn__11015$fn__11016;

public final class KVCluster$fn__11004$fn__11008$fn__11015
extends AFunction {
    Object pod_garbage_handler;
    Object kvs;
    Object retrying_read;
    Object cs;
    Object oldtail;

    public KVCluster$fn__11004$fn__11008$fn__11015(Object object, Object object2, Object object3, Object object4, Object object5) {
        this.pod_garbage_handler = object;
        this.kvs = object2;
        this.retrying_read = object3;
        this.cs = object4;
        this.oldtail = object5;
    }

    public Object invoke() {
        Delay tail_keys_ref;
        Delay delay = tail_keys_ref = new Delay((IFn)new KVCluster$fn__11004$fn__11008$fn__11015$fn__11016(this_.kvs, this_.retrying_read, this_.oldtail));
        tail_keys_ref = null;
        KVCluster$fn__11004$fn__11008$fn__11015 this_ = null;
        return ((IFn)this_.pod_garbage_handler).invoke(this_.cs, (Object)delay);
    }
}

