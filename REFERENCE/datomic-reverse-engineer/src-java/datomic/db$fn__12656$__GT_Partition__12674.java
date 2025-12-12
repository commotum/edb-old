/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 */
package datomic;

import clojure.lang.AFunction;
import datomic.db.Partition;

public final class db$fn__12656$__GT_Partition__12674
extends AFunction {
    public Object invoke(Object id, Object kw) {
        Object object = id;
        id = null;
        Object object2 = kw;
        kw = null;
        return new Partition(object, object2);
    }
}

