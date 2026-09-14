/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 */
package datomic.cache;

import clojure.lang.AFunction;
import datomic.cache.impl.CacheRemove;

public final class impl$fn__357$G__340__360
extends AFunction {
    public Object invoke(Object gf__c__358, Object gf__k__359) {
        Object object = gf__c__358;
        gf__c__358 = null;
        Object object2 = gf__k__359;
        gf__k__359 = null;
        return ((CacheRemove)object).remove(object2);
    }
}

