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
import datomic.artemis_client$create_rpc_server$fn__20939$fn__20940;

public final class artemis_client$create_rpc_server$fn__20939
extends AFunction {
    Object producer;
    Object cleanup;

    public artemis_client$create_rpc_server$fn__20939(Object object, Object object2) {
        this.producer = object;
        this.cleanup = object2;
    }

    public Object invoke() {
        ((IFn)new artemis_client$create_rpc_server$fn__20939$fn__20940(this_.producer)).invoke();
        artemis_client$create_rpc_server$fn__20939 this_ = null;
        return ((IFn)this_.cleanup).invoke();
    }
}

