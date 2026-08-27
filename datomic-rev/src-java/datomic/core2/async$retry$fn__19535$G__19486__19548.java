/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 */
package datomic.core2;

import clojure.lang.AFunction;

public final class async$retry$fn__19535$G__19486__19548
extends AFunction {
    Object fail;

    public async$retry$fn__19535$G__19486__19548(Object object) {
        this.fail = object;
    }

    public Object invoke() {
        this.fail = null;
        return this.fail;
    }
}

