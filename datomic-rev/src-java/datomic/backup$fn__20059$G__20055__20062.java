/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 */
package datomic;

import clojure.lang.AFunction;
import datomic.backup.IValueBackup;

public final class backup$fn__20059$G__20055__20062
extends AFunction {
    public Object invoke(Object gf_____20060, Object gf__node__20061) {
        Object object = gf_____20060;
        gf_____20060 = null;
        Object object2 = gf__node__20061;
        gf__node__20061 = null;
        return ((IValueBackup)object).backup_node(object2);
    }
}

