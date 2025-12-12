/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 */
package datomic;

import clojure.lang.AFunction;
import datomic.peer.RemoteConnection;

public final class peer$fn__21373$G__21347__21378
extends AFunction {
    public Object invoke(Object gf_____21374, Object gf__cluster_conf__21375, Object gf__endpoint__21376, Object gf__mode__21377) {
        Object object = gf_____21374;
        gf_____21374 = null;
        Object object2 = gf__cluster_conf__21375;
        gf__cluster_conf__21375 = null;
        Object object3 = gf__endpoint__21376;
        gf__endpoint__21376 = null;
        Object object4 = gf__mode__21377;
        gf__mode__21377 = null;
        return ((RemoteConnection)object).create_connection_state(object2, object3, object4);
    }
}

