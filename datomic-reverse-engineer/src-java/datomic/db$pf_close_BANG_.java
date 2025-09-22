/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 */
package datomic;

import clojure.lang.AFunction;
import datomic.db.PrefetchDispatcher;

public final class db$pf_close_BANG_
extends AFunction {
    public static Object invokeStatic(Object dispatcher) {
        Object object = dispatcher;
        dispatcher = null;
        return ((PrefetchDispatcher)object).close();
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return db$pf_close_BANG_.invokeStatic(object2);
    }
}

