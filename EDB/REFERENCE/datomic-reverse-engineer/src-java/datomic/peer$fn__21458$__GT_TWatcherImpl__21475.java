/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 */
package datomic;

import clojure.lang.AFunction;
import datomic.peer.TWatcherImpl;

public final class peer$fn__21458$__GT_TWatcherImpl__21475
extends AFunction {
    public Object invoke(Object q2, Object db_ref, Object f, Object lck) {
        Object object = q2;
        q2 = null;
        Object object2 = db_ref;
        db_ref = null;
        Object object3 = f;
        f = null;
        Object object4 = lck;
        lck = null;
        return new TWatcherImpl(object, object2, object3, object4);
    }
}

