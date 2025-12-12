/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 */
package datomic.query;

import clojure.lang.AFunction;

public final class support$counted_seq$fn__19110
extends AFunction {
    Object base_seq;

    public support$counted_seq$fn__19110(Object object) {
        this.base_seq = object;
    }

    public Object invoke(Object object) {
        return this.base_seq.hashCode();
    }
}

