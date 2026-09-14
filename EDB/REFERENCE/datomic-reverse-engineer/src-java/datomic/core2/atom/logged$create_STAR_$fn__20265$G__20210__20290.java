/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 */
package datomic.core2.atom;

import clojure.lang.AFunction;

public final class logged$create_STAR_$fn__20265$G__20210__20290
extends AFunction {
    Object watches_ref;

    public logged$create_STAR_$fn__20265$G__20210__20290(Object object) {
        this.watches_ref = object;
    }

    public Object invoke() {
        this.watches_ref = null;
        return this.watches_ref;
    }
}

