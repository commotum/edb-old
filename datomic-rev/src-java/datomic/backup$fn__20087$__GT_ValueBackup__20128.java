/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 */
package datomic;

import clojure.lang.AFunction;
import datomic.backup.ValueBackup;

public final class backup$fn__20087$__GT_ValueBackup__20128
extends AFunction {
    public Object invoke(Object from_cluster, Object value_storage, Object progress, Object incremental_QMARK_, Object ids__GT_nodes, Object throttle, Object sem) {
        Object object = from_cluster;
        from_cluster = null;
        Object object2 = value_storage;
        value_storage = null;
        Object object3 = progress;
        progress = null;
        Object object4 = incremental_QMARK_;
        incremental_QMARK_ = null;
        Object object5 = ids__GT_nodes;
        ids__GT_nodes = null;
        Object object6 = throttle;
        throttle = null;
        Object object7 = sem;
        sem = null;
        return new ValueBackup(object, object2, object3, object4, object5, object6, object7);
    }
}

