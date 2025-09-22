/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 */
package datomic;

import clojure.lang.AFunction;
import datomic.cluster.ClusteredStore;

public final class cluster$fn__10549$G__10439__10557
extends AFunction {
    public Object invoke(Object gf__cs__10553, Object gf__priority__10554, Object gf__val_key__10555, Object gf__buf__10556) {
        Object object = gf__cs__10553;
        gf__cs__10553 = null;
        Object object2 = gf__priority__10554;
        gf__priority__10554 = null;
        Object object3 = gf__val_key__10555;
        gf__val_key__10555 = null;
        Object object4 = gf__buf__10556;
        gf__buf__10556 = null;
        return ((ClusteredStore)object).create_val(object2, object3, object4);
    }

    public Object invoke(Object gf__cs__10550, Object gf__val_key__10551, Object gf__buf__10552) {
        Object object = gf__cs__10550;
        gf__cs__10550 = null;
        Object object2 = gf__val_key__10551;
        gf__val_key__10551 = null;
        Object object3 = gf__buf__10552;
        gf__buf__10552 = null;
        return ((ClusteredStore)object).create_val(object2, object3);
    }
}

