/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 */
package datomic;

import clojure.lang.AFunction;
import datomic.domain.DeserializingRepairingLookup;

public final class domain$fn__16803$__GT_DeserializingRepairingLookup__16807
extends AFunction {
    public Object invoke(Object cluster2) {
        Object object = cluster2;
        cluster2 = null;
        return new DeserializingRepairingLookup(object);
    }
}

