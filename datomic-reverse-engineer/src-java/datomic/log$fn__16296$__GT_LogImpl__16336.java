/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 */
package datomic;

import clojure.lang.AFunction;
import datomic.log.LogImpl;

public final class log$fn__16296$__GT_LogImpl__16336
extends AFunction {
    public Object invoke(Object olookup, Object desc, Object tail) {
        Object object = olookup;
        olookup = null;
        Object object2 = desc;
        desc = null;
        Object object3 = tail;
        tail = null;
        return new LogImpl(object, object2, object3);
    }
}

