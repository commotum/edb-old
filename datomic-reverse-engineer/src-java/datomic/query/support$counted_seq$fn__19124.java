/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 */
package datomic.query;

import clojure.lang.AFunction;
import java.util.Collection;
import java.util.List;

public final class support$counted_seq$fn__19124
extends AFunction {
    Object base_seq;

    public support$counted_seq$fn__19124(Object object) {
        this.base_seq = object;
    }

    public Object invoke(Object object, Object c) {
        Object object2 = c;
        c = null;
        return ((List)this.base_seq).containsAll((Collection)object2) ? Boolean.TRUE : Boolean.FALSE;
    }
}

