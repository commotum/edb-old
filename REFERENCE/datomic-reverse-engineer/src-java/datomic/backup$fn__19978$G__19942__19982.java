/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 */
package datomic;

import clojure.lang.AFunction;
import datomic.backup.Storage;

public final class backup$fn__19978$G__19942__19982
extends AFunction {
    public Object invoke(Object gf_____19979, Object gf__k__19980, Object gf__buf__19981) {
        Object object = gf_____19979;
        gf_____19979 = null;
        Object object2 = gf__k__19980;
        gf__k__19980 = null;
        Object object3 = gf__buf__19981;
        gf__buf__19981 = null;
        return ((Storage)object).store(object2, object3);
    }
}

