/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 */
package datomic;

import clojure.lang.AFunction;
import datomic.log.LogValue;

public final class log$fn__16485$__GT_LogValue__16510
extends AFunction {
    public Object invoke(Object db2, Object olookup, Object root_id2, Object tail) {
        Object object = db2;
        db2 = null;
        Object object2 = olookup;
        olookup = null;
        Object object3 = root_id2;
        root_id2 = null;
        Object object4 = tail;
        tail = null;
        return new LogValue(object, object2, object3, object4);
    }
}

