/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 */
package datomic;

import clojure.lang.AFunction;
import datomic.db.PrefetchDispatcher;

public final class db$prefetch_BANG_
extends AFunction {
    public static Object invokeStatic(Object dispatcher, Object f) {
        Object object = dispatcher;
        dispatcher = null;
        Object object2 = f;
        f = null;
        return ((PrefetchDispatcher)object).prefetch1(object2);
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return db$prefetch_BANG_.invokeStatic(object3, object4);
    }
}

