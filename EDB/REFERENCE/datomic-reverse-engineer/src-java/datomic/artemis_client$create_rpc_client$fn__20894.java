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
import datomic.artemis_client$create_rpc_client$fn__20894$fn__20895;

public final class artemis_client$create_rpc_client$fn__20894
extends AFunction {
    Object consumer;
    Object cleanup;

    public artemis_client$create_rpc_client$fn__20894(Object object, Object object2) {
        this.consumer = object;
        this.cleanup = object2;
    }

    public Object invoke() {
        ((IFn)new artemis_client$create_rpc_client$fn__20894$fn__20895(this_.consumer)).invoke();
        artemis_client$create_rpc_client$fn__20894 this_ = null;
        return ((IFn)this_.cleanup).invoke();
    }
}

