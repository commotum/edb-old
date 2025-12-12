/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 */
package datomic;

import clojure.lang.AFunction;
import datomic.backup.ValueRestore;

public final class backup$fn__20170$__GT_ValueRestore__20207
extends AFunction {
    public Object invoke(Object value_storage, Object to_cluster, Object progress, Object incremental_QMARK_, Object k__GT_backup_k, Object ids__GT_nodes, Object sem) {
        Object object = value_storage;
        value_storage = null;
        Object object2 = to_cluster;
        to_cluster = null;
        Object object3 = progress;
        progress = null;
        Object object4 = incremental_QMARK_;
        incremental_QMARK_ = null;
        Object object5 = k__GT_backup_k;
        k__GT_backup_k = null;
        Object object6 = ids__GT_nodes;
        ids__GT_nodes = null;
        Object object7 = sem;
        sem = null;
        return new ValueRestore(object, object2, object3, object4, object5, object6, object7);
    }
}

