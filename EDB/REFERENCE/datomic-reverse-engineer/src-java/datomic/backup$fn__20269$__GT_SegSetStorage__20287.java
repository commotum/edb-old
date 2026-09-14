/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 */
package datomic;

import clojure.lang.AFunction;
import datomic.backup.SegSetStorage;

public final class backup$fn__20269$__GT_SegSetStorage__20287
extends AFunction {
    public Object invoke(Object storage, Object seg_id_set) {
        Object object = storage;
        storage = null;
        Object object2 = seg_id_set;
        seg_id_set = null;
        return new SegSetStorage(object, object2);
    }
}

