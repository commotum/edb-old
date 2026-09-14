/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 */
package datomic;

import clojure.lang.AFunction;
import datomic.btset.BTSetSplit;

public final class btset$fn__11814$__GT_BTSetSplit__11816
extends AFunction {
    public Object invoke(Object left, Object k, Object right) {
        Object object = left;
        left = null;
        Object object2 = k;
        k = null;
        Object object3 = right;
        right = null;
        return new BTSetSplit(object, object2, object3);
    }
}

