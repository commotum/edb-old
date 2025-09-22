/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 */
package datomic;

import clojure.lang.AFunction;
import datomic.external_sort.ExternalSort;

public final class external_sort$fn__14419$G__14415__14421
extends AFunction {
    public Object invoke(Object gf_____14420) {
        Object object = gf_____14420;
        gf_____14420 = null;
        return ((ExternalSort)object).merge_step();
    }
}

