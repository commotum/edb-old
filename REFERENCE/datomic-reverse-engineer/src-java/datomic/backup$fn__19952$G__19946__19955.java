/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 */
package datomic;

import clojure.lang.AFunction;
import datomic.backup.Storage;

public final class backup$fn__19952$G__19946__19955
extends AFunction {
    public Object invoke(Object gf_____19953, Object gf__k__19954) {
        Object object = gf_____19953;
        gf_____19953 = null;
        Object object2 = gf__k__19954;
        gf__k__19954 = null;
        return ((Storage)object).exists_QMARK_(object2);
    }
}

