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
import datomic.artemis_client$create_rpc_server$fn__20931$fn__20932;

public final class artemis_client$create_rpc_server$fn__20931
extends AFunction {
    Object cleanup;
    Object dq;
    Object response_queue;

    public artemis_client$create_rpc_server$fn__20931(Object object, Object object2, Object object3) {
        this.cleanup = object;
        this.dq = object2;
        this.response_queue = object3;
    }

    public Object invoke() {
        ((IFn)new artemis_client$create_rpc_server$fn__20931$fn__20932(this_.dq, this_.response_queue)).invoke();
        artemis_client$create_rpc_server$fn__20931 this_ = null;
        return ((IFn)this_.cleanup).invoke();
    }
}

