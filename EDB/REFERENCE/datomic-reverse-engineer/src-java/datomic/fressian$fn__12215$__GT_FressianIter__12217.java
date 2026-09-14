/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 */
package datomic;

import clojure.lang.AFunction;
import datomic.fressian.FressianIter;

public final class fressian$fn__12215$__GT_FressianIter__12217
extends AFunction {
    public Object invoke(Object reader2, Object item) {
        Object object = reader2;
        reader2 = null;
        Object object2 = item;
        item = null;
        return new FressianIter(object, object2);
    }
}

