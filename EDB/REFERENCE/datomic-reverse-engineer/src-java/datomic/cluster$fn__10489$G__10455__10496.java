/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 */
package datomic;

import clojure.lang.AFunction;
import datomic.cluster.ClusteredStore;

public final class cluster$fn__10489$G__10455__10496
extends AFunction {
    public Object invoke(Object gf__cs__10490, Object gf__pod_key__10491, Object gf__rev__10492, Object gf__etag__10493, Object gf__buf__10494, Object gf__metamap__10495) {
        Object object = gf__cs__10490;
        gf__cs__10490 = null;
        Object object2 = gf__pod_key__10491;
        gf__pod_key__10491 = null;
        Object object3 = gf__rev__10492;
        gf__rev__10492 = null;
        Object object4 = gf__etag__10493;
        gf__etag__10493 = null;
        Object object5 = gf__buf__10494;
        gf__buf__10494 = null;
        Object object6 = gf__metamap__10495;
        gf__metamap__10495 = null;
        return ((ClusteredStore)object).update_pod_STAR_(object2, object3, object4, object5, object6);
    }
}

