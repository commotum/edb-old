/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 */
package datomic;

import clojure.lang.AFunction;
import datomic.kv_store.KVStore;

public final class kv_store$fn__10776$G__10753__10780
extends AFunction {
    public Object invoke(Object gf_____10777, Object gf__key__10778, Object gf__consistent_QMARK___10779) {
        Object object = gf_____10777;
        gf_____10777 = null;
        Object object2 = gf__key__10778;
        gf__key__10778 = null;
        Object object3 = gf__consistent_QMARK___10779;
        gf__consistent_QMARK___10779 = null;
        return ((KVStore)object).get(object2, object3);
    }
}

