/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 */
package datomic;

import clojure.lang.AFunction;
import datomic.db.PartitionRequests;

public final class db$fn__13704$__GT_PartitionRequests__13715
extends AFunction {
    public Object invoke(Object id__GT_part, Object id__GT_match) {
        Object object = id__GT_part;
        id__GT_part = null;
        Object object2 = id__GT_match;
        id__GT_match = null;
        return new PartitionRequests(object, object2);
    }
}

