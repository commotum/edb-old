/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.RT
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.RT;
import datomic.btset.BTSetLeaf;

public final class btset$fn__11837$__GT_BTSetLeaf__11841
extends AFunction {
    public Object invoke(Object cnt, Object cmp, Object ks) {
        Object object = cnt;
        cnt = null;
        Object object2 = cmp;
        cmp = null;
        Object object3 = ks;
        ks = null;
        return new BTSetLeaf(RT.uncheckedLongCast((Object)((Number)object)), object2, object3);
    }
}

