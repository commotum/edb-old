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

public final class support$counted_seq$fn__19098
extends AFunction {
    Object base_seq;

    public support$counted_seq$fn__19098(Object object) {
        this.base_seq = object;
    }

    public Object invoke(Object object, Object index2) {
        Object object2 = index2;
        index2 = null;
        return ((List)this.base_seq).listIterator(RT.intCast((Object)((Number)object2)));
    }

    public Object invoke(Object object) {
        return ((List)this.base_seq).listIterator();
    }
}

