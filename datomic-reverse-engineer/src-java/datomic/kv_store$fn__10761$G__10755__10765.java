/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 */
package datomic;

import clojure.lang.AFunction;
import datomic.kv_store.KVStore;

public final class kv_store$fn__10761$G__10755__10765
extends AFunction {
    public Object invoke(Object gf_____10762, Object gf__key__10763, Object gf__consistent_QMARK___10764) {
        Object object = gf_____10762;
        gf_____10762 = null;
        Object object2 = gf__key__10763;
        gf__key__10763 = null;
        Object object3 = gf__consistent_QMARK___10764;
        gf__consistent_QMARK___10764 = null;
        return ((KVStore)object).delete(object2, object3);
    }
}

