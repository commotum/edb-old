/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 */
package datomic.core2.val_store;

import clojure.lang.AFunction;
import datomic.core2.val_store.double_store.ValStore;

public final class double_store$fn__21086$__GT_ValStore__21248
extends AFunction {
    public Object invoke(Object near_store, Object far_store, Object repair_metric, Object get_fallback_msec) {
        Object object = near_store;
        near_store = null;
        Object object2 = far_store;
        far_store = null;
        Object object3 = repair_metric;
        repair_metric = null;
        Object object4 = get_fallback_msec;
        get_fallback_msec = null;
        return new ValStore(object, object2, object3, object4);
    }
}

