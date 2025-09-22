/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 */
package datomic;

import clojure.lang.AFunction;
import datomic.process.SharedCriticalFailure;

public final class process$fn__14942$__GT_SharedCriticalFailure__14967
extends AFunction {
    public Object invoke(Object handlers, Object prom, Object shutdown2) {
        Object object = handlers;
        handlers = null;
        Object object2 = prom;
        prom = null;
        Object object3 = shutdown2;
        shutdown2 = null;
        return new SharedCriticalFailure(object, object2, object3);
    }
}

