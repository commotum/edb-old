/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 */
package datomic.cache;

import clojure.lang.AFunction;
import datomic.cache.impl.CachePut;

public final class impl$fn__324$G__320__328
extends AFunction {
    public Object invoke(Object gf__c__325, Object gf__k__326, Object gf__v__327) {
        Object object = gf__c__325;
        gf__c__325 = null;
        Object object2 = gf__k__326;
        gf__k__326 = null;
        Object object3 = gf__v__327;
        gf__v__327 = null;
        return ((CachePut)object).put(object2, object3);
    }
}

