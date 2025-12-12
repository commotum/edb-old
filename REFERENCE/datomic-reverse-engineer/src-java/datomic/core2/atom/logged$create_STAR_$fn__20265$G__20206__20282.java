/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 */
package datomic.core2.atom;

import clojure.lang.AFunction;

public final class logged$create_STAR_$fn__20265$G__20206__20282
extends AFunction {
    Object logged_atom;

    public logged$create_STAR_$fn__20265$G__20206__20282(Object object) {
        this.logged_atom = object;
    }

    public Object invoke() {
        this.logged_atom = null;
        return this.logged_atom;
    }
}

