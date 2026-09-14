/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 */
package datomic.cache;

import clojure.lang.AFunction;
import datomic.cache.impl.CacheRemove;

public final class impl$fn__346$G__342__348
extends AFunction {
    public Object invoke(Object gf__c__347) {
        Object object = gf__c__347;
        gf__c__347 = null;
        return ((CacheRemove)object).clear();
    }
}

