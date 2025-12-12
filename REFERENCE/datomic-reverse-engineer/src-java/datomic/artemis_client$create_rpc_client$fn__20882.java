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
import datomic.artemis_client$create_rpc_client$fn__20882$fn__20883;

public final class artemis_client$create_rpc_client$fn__20882
extends AFunction {
    Object request_queue;
    Object dq;

    public artemis_client$create_rpc_client$fn__20882(Object object, Object object2) {
        this.request_queue = object;
        this.dq = object2;
    }

    public Object invoke() {
        ((IFn)new artemis_client$create_rpc_client$fn__20882$fn__20883(this.request_queue, this.dq)).invoke();
        return null;
    }
}

