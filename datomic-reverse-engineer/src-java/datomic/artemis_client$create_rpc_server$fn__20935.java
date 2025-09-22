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
import datomic.artemis_client$create_rpc_server$fn__20935$fn__20936;

public final class artemis_client$create_rpc_server$fn__20935
extends AFunction {
    Object cleanup;
    Object consumer;

    public artemis_client$create_rpc_server$fn__20935(Object object, Object object2) {
        this.cleanup = object;
        this.consumer = object2;
    }

    public Object invoke() {
        ((IFn)new artemis_client$create_rpc_server$fn__20935$fn__20936(this_.consumer)).invoke();
        artemis_client$create_rpc_server$fn__20935 this_ = null;
        return ((IFn)this_.cleanup).invoke();
    }
}

