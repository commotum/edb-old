/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 */
package datomic.core2.atom;

import clojure.lang.AFunction;

public final class logged$create_STAR_$fn__20265$G__20200__20270
extends AFunction {
    Object refresh_msec;

    public logged$create_STAR_$fn__20265$G__20200__20270(Object object) {
        this.refresh_msec = object;
    }

    public Object invoke() {
        this.refresh_msec = null;
        return this.refresh_msec;
    }
}

