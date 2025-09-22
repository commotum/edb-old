/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.Util
 */
package datomic.query;

import clojure.lang.AFunction;
import clojure.lang.Util;

public final class support$counted_seq$fn__19122
extends AFunction {
    Object base_seq;

    public support$counted_seq$fn__19122(Object object) {
        this.base_seq = object;
    }

    public Object invoke(Object object, Object o) {
        Object object2 = o;
        o = null;
        support$counted_seq$fn__19122 this_ = null;
        return Util.equiv((Object)this_.base_seq, (Object)object2) ? Boolean.TRUE : Boolean.FALSE;
    }
}

