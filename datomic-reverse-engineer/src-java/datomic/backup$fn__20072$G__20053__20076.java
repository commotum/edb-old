/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 */
package datomic;

import clojure.lang.AFunction;
import datomic.backup.IValueBackup;

public final class backup$fn__20072$G__20053__20076
extends AFunction {
    public Object invoke(Object gf_____20073, Object gf__k__20074, Object gf__backup_k__20075) {
        Object object = gf_____20073;
        gf_____20073 = null;
        Object object2 = gf__k__20074;
        gf__k__20074 = null;
        Object object3 = gf__backup_k__20075;
        gf__backup_k__20075 = null;
        return ((IValueBackup)object).backup_val(object2, object3);
    }
}

