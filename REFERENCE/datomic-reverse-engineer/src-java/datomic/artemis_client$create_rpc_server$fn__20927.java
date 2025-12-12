/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import datomic.artemis_client$create_rpc_server$fn__20927$fn__20928;

public final class artemis_client$create_rpc_server$fn__20927
extends AFunction {
    Object cleanup;
    Object request_queue;
    Object dq;

    public artemis_client$create_rpc_server$fn__20927(Object object, Object object2, Object object3) {
        this.cleanup = object;
        this.request_queue = object2;
        this.dq = object3;
    }

    public Object invoke() {
        ((IFn)new artemis_client$create_rpc_server$fn__20927$fn__20928(this_.request_queue, this_.dq)).invoke();
        artemis_client$create_rpc_server$fn__20927 this_ = null;
        return ((IFn)this_.cleanup).invoke();
    }
}

