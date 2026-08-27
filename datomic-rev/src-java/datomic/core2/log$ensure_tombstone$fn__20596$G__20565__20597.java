/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 */
package datomic.core2;

import clojure.lang.AFunction;

public final class log$ensure_tombstone$fn__20596$G__20565__20597
extends AFunction {
    Object log;

    public log$ensure_tombstone$fn__20596$G__20565__20597(Object object) {
        this.log = object;
    }

    public Object invoke() {
        this.log = null;
        return this.log;
    }
}

