/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 */
package datomic;

import clojure.lang.AFunction;
import datomic.external_sort.ExternalSort;

public final class external_sort$fn__14443$G__14413__14447
extends AFunction {
    public Object invoke(Object gf_____14444, Object gf__files__14445, Object gf__handler__14446) {
        Object object = gf_____14444;
        gf_____14444 = null;
        Object object2 = gf__files__14445;
        gf__files__14445 = null;
        Object object3 = gf__handler__14446;
        gf__handler__14446 = null;
        return ((ExternalSort)object).consume_files_iter(object2, object3);
    }
}

