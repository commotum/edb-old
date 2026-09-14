/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 */
package datomic;

import clojure.lang.AFunction;
import datomic.btset.BTSetBranch;

public final class btset$fn__11830$__GT_BTSetBranch__11834
extends AFunction {
    public Object invoke(Object cmp, Object nks) {
        Object object = cmp;
        cmp = null;
        Object object2 = nks;
        nks = null;
        return new BTSetBranch(object, object2);
    }
}

