/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 */
package datomic;

import clojure.lang.AFunction;
import datomic.cluster_stack.ValStoreOnCluster;

public final class cluster_stack$fn__11272$__GT_ValStoreOnCluster__11432
extends AFunction {
    public Object invoke(Object cluster2) {
        Object object = cluster2;
        cluster2 = null;
        return new ValStoreOnCluster(object);
    }
}

