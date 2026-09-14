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
import datomic.iter.MergeIter;

public final class iter$fn__11779$__GT_MergeIter__11781
extends AFunction {
    public Object invoke(Object cmp, Object iters, Object i) {
        Object object = cmp;
        cmp = null;
        Object object2 = iters;
        iters = null;
        Object object3 = i;
        i = null;
        return new MergeIter(object, object2, RT.longCast((Object)((Number)object3)));
    }
}

