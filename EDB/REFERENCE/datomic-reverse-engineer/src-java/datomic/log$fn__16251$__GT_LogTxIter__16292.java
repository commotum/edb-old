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
import datomic.log.LogTxIter;

public final class log$fn__16251$__GT_LogTxIter__16292
extends AFunction {
    public Object invoke(Object lookup, Object root_val, Object tail, Object ridx, Object dir, Object didx, Object seg, Object sidx) {
        Object object = lookup;
        lookup = null;
        Object object2 = root_val;
        root_val = null;
        Object object3 = tail;
        tail = null;
        Object object4 = ridx;
        ridx = null;
        Object object5 = dir;
        dir = null;
        Object object6 = didx;
        didx = null;
        Object object7 = seg;
        seg = null;
        Object object8 = sidx;
        sidx = null;
        return new LogTxIter(object, object2, object3, RT.longCast((Object)((Number)object4)), object5, RT.longCast((Object)((Number)object6)), object7, RT.longCast((Object)((Number)object8)));
    }
}

