/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 */
package datomic;

import clojure.lang.AFunction;
import datomic.db.ProcessInpoint;

public final class db$fn__13853$__GT_ProcessInpoint__13863
extends AFunction {
    public Object invoke(Object db2, Object part_reqs, Object nextp) {
        Object object = db2;
        db2 = null;
        Object object2 = part_reqs;
        part_reqs = null;
        Object object3 = nextp;
        nextp = null;
        return new ProcessInpoint(object, object2, object3);
    }
}

