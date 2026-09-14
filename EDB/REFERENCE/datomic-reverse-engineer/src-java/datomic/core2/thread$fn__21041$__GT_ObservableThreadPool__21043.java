/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 */
package datomic.core2;

import clojure.lang.AFunction;
import datomic.core2.thread.ObservableThreadPool;

public final class thread$fn__21041$__GT_ObservableThreadPool__21043
extends AFunction {
    public Object invoke(Object pool, Object callback2) {
        Object object = pool;
        pool = null;
        Object object2 = callback2;
        callback2 = null;
        return new ObservableThreadPool(object, object2);
    }
}

