/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 */
package datomic;

import clojure.lang.AFunction;
import datomic.cluster.RefClusterStore;

public final class cluster$fn__10732$G__10728__10734
extends AFunction {
    public Object invoke(Object gf__cs__10733) {
        Object object = gf__cs__10733;
        gf__cs__10733 = null;
        return ((RefClusterStore)object)._get_ref_store();
    }
}

