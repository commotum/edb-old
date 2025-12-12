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
import java.util.List;

public final class support$counted_seq$fn__19104
extends AFunction {
    Object base_seq;

    public support$counted_seq$fn__19104(Object object) {
        this.base_seq = object;
    }

    public Object invoke(Object object, Object from, Object to) {
        Object object2 = from;
        from = null;
        Object object3 = to;
        to = null;
        return ((List)this.base_seq).subList(RT.intCast((Object)((Number)object2)), RT.intCast((Object)((Number)object3)));
    }
}

