/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.RT
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.RT;
import java.util.Map;

public final class cache$lookup_with_inflight_cache$reify__9410$fn__9411
extends AFunction {
    Object not_found;
    Object in_flight;
    Object k;
    Object m;

    public cache$lookup_with_inflight_cache$reify__9410$fn__9411(Object object, Object object2, Object object3, Object object4) {
        this.not_found = object;
        this.in_flight = object2;
        this.k = object3;
        this.m = object4;
    }

    public Object invoke() {
        Object object;
        try {
            this.not_found = null;
            object = RT.get((Object)this.m, (Object)this.k, (Object)this.not_found);
        }
        finally {
            this.k = null;
            ((Map)this.in_flight).remove(this.k);
        }
        return object;
    }
}

