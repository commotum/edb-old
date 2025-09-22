/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 */
package datomic;

import clojure.lang.AFunction;
import datomic.cluster.ClusteredStore;

public final class cluster$fn__10472$G__10449__10477
extends AFunction {
    public Object invoke(Object gf__cs__10473, Object gf__ref_key__10474, Object gf__rev__10475, Object gf__vkey__10476) {
        Object object = gf__cs__10473;
        gf__cs__10473 = null;
        Object object2 = gf__ref_key__10474;
        gf__ref_key__10474 = null;
        Object object3 = gf__rev__10475;
        gf__rev__10475 = null;
        Object object4 = gf__vkey__10476;
        gf__vkey__10476 = null;
        return ((ClusteredStore)object).set_ref(object2, object3, object4);
    }
}

