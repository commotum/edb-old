/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import datomic.garbage$queue_gc$fn__19869$fn__19870;

public final class garbage$queue_gc$fn__19869
extends AFunction {
    Object cluster;
    Object older_than;

    public garbage$queue_gc$fn__19869(Object object, Object object2) {
        this.cluster = object;
        this.older_than = object2;
    }

    public Object invoke(Object _) {
        ((IFn)new garbage$queue_gc$fn__19869$fn__19870(this.cluster, this.older_than)).invoke();
        return null;
    }
}

