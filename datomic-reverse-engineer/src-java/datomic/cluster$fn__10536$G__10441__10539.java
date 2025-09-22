/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 */
package datomic;

import clojure.lang.AFunction;
import datomic.cluster.ClusteredStore;

public final class cluster$fn__10536$G__10441__10539
extends AFunction {
    public Object invoke(Object gf__cs__10537, Object gf__val_key__10538) {
        Object object = gf__cs__10537;
        gf__cs__10537 = null;
        Object object2 = gf__val_key__10538;
        gf__val_key__10538 = null;
        return ((ClusteredStore)object).get_val(object2);
    }
}

