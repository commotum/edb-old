/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 */
package datomic;

import clojure.lang.AFunction;
import datomic.excise.ExcisePred;

public final class excise$fn__14783$G__14779__14786
extends AFunction {
    public Object invoke(Object gf__epred__14784, Object gf__datom__14785) {
        Object object = gf__epred__14784;
        gf__epred__14784 = null;
        Object object2 = gf__datom__14785;
        gf__datom__14785 = null;
        return ((ExcisePred)object).ep_remove_QMARK_(object2);
    }
}

