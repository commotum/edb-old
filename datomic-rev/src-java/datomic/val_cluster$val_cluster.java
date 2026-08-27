/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 */
package datomic;

import clojure.lang.AFunction;
import datomic.val_cluster.ValCluster;

public final class val_cluster$val_cluster
extends AFunction {
    public static Object invokeStatic(Object val_store2) {
        Object object = val_store2;
        val_store2 = null;
        return new ValCluster(object);
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return val_cluster$val_cluster.invokeStatic(object2);
    }
}

