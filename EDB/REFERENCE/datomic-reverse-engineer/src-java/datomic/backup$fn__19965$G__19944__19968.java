/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 */
package datomic;

import clojure.lang.AFunction;
import datomic.backup.Storage;

public final class backup$fn__19965$G__19944__19968
extends AFunction {
    public Object invoke(Object gf_____19966, Object gf__prefix__19967) {
        Object object = gf_____19966;
        gf_____19966 = null;
        Object object2 = gf__prefix__19967;
        gf__prefix__19967 = null;
        return ((Storage)object).list_keys(object2);
    }
}

