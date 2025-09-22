/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 */
package datomic;

import clojure.lang.AFunction;
import datomic.backup.IValueRestore;

public final class backup$fn__20157$G__20138__20160
extends AFunction {
    public Object invoke(Object gf_____20158, Object gf__k__20159) {
        Object object = gf_____20158;
        gf_____20158 = null;
        Object object2 = gf__k__20159;
        gf__k__20159 = null;
        return ((IValueRestore)object).restore_val(object2);
    }
}

