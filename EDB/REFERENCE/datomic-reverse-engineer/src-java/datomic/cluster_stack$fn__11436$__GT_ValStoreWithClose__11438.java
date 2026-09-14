/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 */
package datomic;

import clojure.lang.AFunction;
import datomic.cluster_stack.ValStoreWithClose;

public final class cluster_stack$fn__11436$__GT_ValStoreWithClose__11438
extends AFunction {
    public Object invoke(Object store, Object close2) {
        Object object = store;
        store = null;
        Object object2 = close2;
        close2 = null;
        return new ValStoreWithClose(object, object2);
    }
}

