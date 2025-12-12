/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 */
package datomic;

import clojure.lang.AFunction;
import datomic.cluster.AsyncWriter;

public final class cluster$fn__10695$G__10689__10697
extends AFunction {
    public Object invoke(Object gf_____10696) {
        Object object = gf_____10696;
        gf_____10696 = null;
        return ((AsyncWriter)object).finish_writer();
    }
}

