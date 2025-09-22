/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 */
package datomic;

import clojure.lang.AFunction;
import datomic.excise.ExcisePred;

public final class excise$fn__14796$G__14777__14798
extends AFunction {
    public Object invoke(Object gf__epred__14797) {
        Object object = gf__epred__14797;
        gf__epred__14797 = null;
        return ((ExcisePred)object).ep_datoms();
    }
}

