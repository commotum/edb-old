/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 */
package datomic;

import clojure.lang.AFunction;
import datomic.cluster.ClusteredStore;

public final class cluster$fn__10587$G__10451__10590
extends AFunction {
    public Object invoke(Object gf__cs__10588, Object gf__pod_key__10589) {
        Object object = gf__cs__10588;
        gf__cs__10588 = null;
        Object object2 = gf__pod_key__10589;
        gf__pod_key__10589 = null;
        return ((ClusteredStore)object).get_pod(object2);
    }
}

