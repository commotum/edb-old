/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 */
package datomic;

import clojure.lang.AFunction;
import datomic.monitor.Metrics;

public final class monitor$fn__503$G__499__505
extends AFunction {
    public Object invoke(Object gf_____504) {
        Object object = gf_____504;
        gf_____504 = null;
        return ((Metrics)object).metrics();
    }
}

