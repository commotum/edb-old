/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 */
package datomic;

import clojure.lang.AFunction;
import datomic.val_cluster.ValCluster;

public final class val_cluster$fn__11206$__GT_ValCluster__11248
extends AFunction {
    public Object invoke(Object val_store2) {
        Object object = val_store2;
        val_store2 = null;
        return new ValCluster(object);
    }
}

