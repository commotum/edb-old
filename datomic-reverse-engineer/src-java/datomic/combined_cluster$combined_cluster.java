/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 */
package datomic;

import clojure.lang.AFunction;
import datomic.combined_cluster.CombinedCluster;

public final class combined_cluster$combined_cluster
extends AFunction {
    public static Object invokeStatic(Object ref_cluster, Object val_cluster2) {
        Object object = ref_cluster;
        ref_cluster = null;
        Object object2 = val_cluster2;
        val_cluster2 = null;
        return new CombinedCluster(object, object2);
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return combined_cluster$combined_cluster.invokeStatic(object3, object4);
    }
}

