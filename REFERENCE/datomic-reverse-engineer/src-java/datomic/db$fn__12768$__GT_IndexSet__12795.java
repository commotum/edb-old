/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 */
package datomic;

import clojure.lang.AFunction;
import datomic.db.IndexSet;

public final class db$fn__12768$__GT_IndexSet__12795
extends AFunction {
    public Object invoke(Object eavt2, Object avet2, Object aevt2, Object raet2, Object fulltext2) {
        Object object = eavt2;
        eavt2 = null;
        Object object2 = avet2;
        avet2 = null;
        Object object3 = aevt2;
        aevt2 = null;
        Object object4 = raet2;
        raet2 = null;
        Object object5 = fulltext2;
        fulltext2 = null;
        return new IndexSet(object, object2, object3, object4, object5);
    }
}

