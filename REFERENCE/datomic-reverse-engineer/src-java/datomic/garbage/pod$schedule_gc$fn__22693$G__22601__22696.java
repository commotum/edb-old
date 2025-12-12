/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 */
package datomic.garbage;

import clojure.lang.AFunction;

public final class pod$schedule_gc$fn__22693$G__22601__22696
extends AFunction {
    Object garbage_ids_ref;

    public pod$schedule_gc$fn__22693$G__22601__22696(Object object) {
        this.garbage_ids_ref = object;
    }

    public Object invoke() {
        this.garbage_ids_ref = null;
        return this.garbage_ids_ref;
    }
}

