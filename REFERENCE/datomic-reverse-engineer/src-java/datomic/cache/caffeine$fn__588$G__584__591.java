/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 */
package datomic.cache;

import clojure.lang.AFunction;
import datomic.cache.caffeine.CacheGet;

public final class caffeine$fn__588$G__584__591
extends AFunction {
    public Object invoke(Object gf_____589, Object gf__k__590) {
        Object object = gf_____589;
        gf_____589 = null;
        Object object2 = gf__k__590;
        gf__k__590 = null;
        return ((CacheGet)object).cache_get(object2);
    }
}

