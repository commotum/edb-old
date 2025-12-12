/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 */
package datomic;

import clojure.lang.AFunction;
import datomic.cluster.ClusteredStore;

public final class cluster$fn__10510$G__10445__10513
extends AFunction {
    public Object invoke(Object gf__cs__10511, Object gf__key__10512) {
        Object object = gf__cs__10511;
        gf__cs__10511 = null;
        Object object2 = gf__key__10512;
        gf__key__10512 = null;
        return ((ClusteredStore)object).delete_reference(object2);
    }
}

