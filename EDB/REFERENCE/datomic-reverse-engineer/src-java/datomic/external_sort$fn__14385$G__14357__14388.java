/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 */
package datomic;

import clojure.lang.AFunction;
import datomic.external_sort.IO;

public final class external_sort$fn__14385$G__14357__14388
extends AFunction {
    public Object invoke(Object gf_____14386, Object gf__fname__14387) {
        Object object = gf_____14386;
        gf_____14386 = null;
        Object object2 = gf__fname__14387;
        gf__fname__14387 = null;
        return ((IO)object).delete_temp_file(object2);
    }
}

