/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 */
package datomic.query;

import clojure.lang.AFunction;
import java.util.List;

public final class support$counted_seq$fn__19094
extends AFunction {
    Object base_seq;

    public support$counted_seq$fn__19094(Object object) {
        this.base_seq = object;
    }

    public Object invoke(Object object, Object o) {
        Object object2 = o;
        o = null;
        return ((List)this.base_seq).contains(object2) ? Boolean.TRUE : Boolean.FALSE;
    }
}

