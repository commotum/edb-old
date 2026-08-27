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
import datomic.artemis_client$create_rpc_client$fn__20886$fn__20887;

public final class artemis_client$create_rpc_client$fn__20886
extends AFunction {
    Object dq;
    Object response_queue;
    Object cleanup;

    public artemis_client$create_rpc_client$fn__20886(Object object, Object object2, Object object3) {
        this.dq = object;
        this.response_queue = object2;
        this.cleanup = object3;
    }

    public Object invoke() {
        ((IFn)new artemis_client$create_rpc_client$fn__20886$fn__20887(this_.dq, this_.response_queue)).invoke();
        artemis_client$create_rpc_client$fn__20886 this_ = null;
        return ((IFn)this_.cleanup).invoke();
    }
}

