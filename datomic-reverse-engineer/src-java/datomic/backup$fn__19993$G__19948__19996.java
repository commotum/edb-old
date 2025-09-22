/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 */
package datomic;

import clojure.lang.AFunction;
import datomic.backup.Storage;

public final class backup$fn__19993$G__19948__19996
extends AFunction {
    public Object invoke(Object gf_____19994, Object gf__k__19995) {
        Object object = gf_____19994;
        gf_____19994 = null;
        Object object2 = gf__k__19995;
        gf__k__19995 = null;
        return ((Storage)object).retrieve(object2);
    }
}

