/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 */
package datomic;

import clojure.lang.AFunction;
import datomic.cluster.ClusteredStore;

public final class cluster$fn__10459$G__10443__10462
extends AFunction {
    public Object invoke(Object gf__cs__10460, Object gf__key__10461) {
        Object object = gf__cs__10460;
        gf__cs__10460 = null;
        Object object2 = gf__key__10461;
        gf__key__10461 = null;
        return ((ClusteredStore)object).delete(object2);
    }
}

