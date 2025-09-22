/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 */
package datomic;

import clojure.lang.AFunction;
import datomic.backup.IValueRestore;

public final class backup$fn__20144$G__20140__20147
extends AFunction {
    public Object invoke(Object gf_____20145, Object gf__node__20146) {
        Object object = gf_____20145;
        gf_____20145 = null;
        Object object2 = gf__node__20146;
        gf__node__20146 = null;
        return ((IValueRestore)object).restore_node(object2);
    }
}

