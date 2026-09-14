/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 */
package datomic.query;

import clojure.lang.AFunction;

public final class support$counted_seq$fn__19120
extends AFunction {
    Object base_seq;

    public support$counted_seq$fn__19120(Object object) {
        this.base_seq = object;
    }

    public Object invoke(Object object, Object o) {
        Object object2 = o;
        o = null;
        return this.base_seq.equals(object2) ? Boolean.TRUE : Boolean.FALSE;
    }
}

