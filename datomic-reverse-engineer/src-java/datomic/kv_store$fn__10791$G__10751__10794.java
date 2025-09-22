/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 */
package datomic;

import clojure.lang.AFunction;
import datomic.kv_store.KVStore;

public final class kv_store$fn__10791$G__10751__10794
extends AFunction {
    public Object invoke(Object gf_____10792, Object gf__val_map__10793) {
        Object object = gf_____10792;
        gf_____10792 = null;
        Object object2 = gf__val_map__10793;
        gf__val_map__10793 = null;
        return ((KVStore)object).put(object2);
    }
}

