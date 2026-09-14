/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 */
package datomic;

import clojure.lang.AFunction;
import datomic.combined_cluster.CombinedCluster;

public final class combined_cluster$fn__11176$__GT_CombinedCluster__11178
extends AFunction {
    public Object invoke(Object ref_cluster, Object val_cluster2) {
        Object object = ref_cluster;
        ref_cluster = null;
        Object object2 = val_cluster2;
        val_cluster2 = null;
        return new CombinedCluster(object, object2);
    }
}

