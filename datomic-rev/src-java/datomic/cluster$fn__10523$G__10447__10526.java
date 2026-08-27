/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 */
package datomic;

import clojure.lang.AFunction;
import datomic.cluster.ClusteredStore;

public final class cluster$fn__10523$G__10447__10526
extends AFunction {
    public Object invoke(Object gf__cs__10524, Object gf__ref_key__10525) {
        Object object = gf__cs__10524;
        gf__cs__10524 = null;
        Object object2 = gf__ref_key__10525;
        gf__ref_key__10525 = null;
        return ((ClusteredStore)object).get_ref(object2);
    }
}

