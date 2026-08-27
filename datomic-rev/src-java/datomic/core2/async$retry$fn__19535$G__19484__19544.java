/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 */
package datomic.core2;

import clojure.lang.AFunction;

public final class async$retry$fn__19535$G__19484__19544
extends AFunction {
    Object backoff;

    public async$retry$fn__19535$G__19484__19544(Object object) {
        this.backoff = object;
    }

    public Object invoke() {
        this.backoff = null;
        return this.backoff;
    }
}

