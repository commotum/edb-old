/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 */
package datomic.garbage;

import clojure.lang.AFunction;

public final class pod$schedule_gc$fn__22693$G__22600__22694
extends AFunction {
    Object cluster;

    public pod$schedule_gc$fn__22693$G__22600__22694(Object object) {
        this.cluster = object;
    }

    public Object invoke() {
        this.cluster = null;
        return this.cluster;
    }
}

