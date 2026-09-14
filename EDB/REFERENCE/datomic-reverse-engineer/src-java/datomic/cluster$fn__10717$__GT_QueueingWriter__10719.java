/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 */
package datomic;

import clojure.lang.AFunction;
import datomic.cluster.QueueingWriter;

public final class cluster$fn__10717$__GT_QueueingWriter__10719
extends AFunction {
    public Object invoke(Object cluster2, Object done_reason, Object bounding_timeout_msec, Object queue2) {
        Object object = cluster2;
        cluster2 = null;
        Object object2 = done_reason;
        done_reason = null;
        Object object3 = bounding_timeout_msec;
        bounding_timeout_msec = null;
        Object object4 = queue2;
        queue2 = null;
        return new QueueingWriter(object, object2, object3, object4);
    }
}

