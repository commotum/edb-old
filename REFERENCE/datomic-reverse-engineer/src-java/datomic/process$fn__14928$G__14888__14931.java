/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 */
package datomic;

import clojure.lang.AFunction;
import datomic.process.CriticalFailure;

public final class process$fn__14928$G__14888__14931
extends AFunction {
    public Object invoke(Object gf_____14929, Object gf__h__14930) {
        Object object = gf_____14929;
        gf_____14929 = null;
        Object object2 = gf__h__14930;
        gf__h__14930 = null;
        return ((CriticalFailure)object).add_fail_handler(object2);
    }
}

