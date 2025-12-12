/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 */
package datomic;

import clojure.lang.AFunction;
import datomic.cluster.AsyncWriter;

public final class cluster$fn__10706$G__10691__10708
extends AFunction {
    public Object invoke(Object gf_____10707) {
        Object object = gf_____10707;
        gf_____10707 = null;
        return ((AsyncWriter)object).sync_writes();
    }
}

