/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 */
package datomic.core2.atom;

import clojure.lang.AFunction;

public final class logged$create_STAR_$fn__20265$G__20204__20278
extends AFunction {
    Object close_ch;

    public logged$create_STAR_$fn__20265$G__20204__20278(Object object) {
        this.close_ch = object;
    }

    public Object invoke() {
        this.close_ch = null;
        return this.close_ch;
    }
}

