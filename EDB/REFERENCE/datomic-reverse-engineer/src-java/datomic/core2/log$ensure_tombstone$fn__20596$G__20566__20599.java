/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 */
package datomic.core2;

import clojure.lang.AFunction;

public final class log$ensure_tombstone$fn__20596$G__20566__20599
extends AFunction {
    Object tombstone;

    public log$ensure_tombstone$fn__20596$G__20566__20599(Object object) {
        this.tombstone = object;
    }

    public Object invoke() {
        this.tombstone = null;
        return this.tombstone;
    }
}

