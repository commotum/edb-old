/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.RT
 */
package datomic.query;

import clojure.lang.AFunction;
import clojure.lang.RT;

public final class support$counted_seq$fn__19118
extends AFunction {
    Object base_seq;

    public support$counted_seq$fn__19118(Object object) {
        this.base_seq = object;
    }

    public Object invoke(Object object, Object index2) {
        Object object2 = index2;
        index2 = null;
        support$counted_seq$fn__19118 this_ = null;
        return RT.nth((Object)this_.base_seq, (int)RT.intCast((Object)((Number)object2)));
    }
}

