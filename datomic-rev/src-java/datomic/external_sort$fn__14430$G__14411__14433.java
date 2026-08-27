/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 */
package datomic;

import clojure.lang.AFunction;
import datomic.external_sort.ExternalSort;

public final class external_sort$fn__14430$G__14411__14433
extends AFunction {
    public Object invoke(Object gf_____14431, Object gf__handler__14432) {
        Object object = gf_____14431;
        gf_____14431 = null;
        Object object2 = gf__handler__14432;
        gf__handler__14432 = null;
        return ((ExternalSort)object).consume_iter(object2);
    }
}

