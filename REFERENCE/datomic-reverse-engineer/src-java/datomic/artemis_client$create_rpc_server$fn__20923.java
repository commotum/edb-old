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
import datomic.artemis_client$create_rpc_server$fn__20923$fn__20924;

public final class artemis_client$create_rpc_server$fn__20923
extends AFunction {
    Object session;

    public artemis_client$create_rpc_server$fn__20923(Object object) {
        this.session = object;
    }

    public Object invoke() {
        ((IFn)new artemis_client$create_rpc_server$fn__20923$fn__20924(this.session)).invoke();
        return null;
    }
}

