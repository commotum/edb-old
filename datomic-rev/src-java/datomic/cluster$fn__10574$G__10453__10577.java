/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 */
package datomic;

import clojure.lang.AFunction;
import datomic.cluster.ClusteredStore;

public final class cluster$fn__10574$G__10453__10577
extends AFunction {
    public Object invoke(Object gf__cs__10575, Object gf__pod_key__10576) {
        Object object = gf__cs__10575;
        gf__cs__10575 = null;
        Object object2 = gf__pod_key__10576;
        gf__pod_key__10576 = null;
        return ((ClusteredStore)object).get_pod_meta(object2);
    }
}

