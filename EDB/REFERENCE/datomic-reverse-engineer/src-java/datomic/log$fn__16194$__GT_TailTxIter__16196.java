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
import datomic.log.TailTxIter;

public final class log$fn__16194$__GT_TailTxIter__16196
extends AFunction {
    public Object invoke(Object txes, Object idx) {
        Object object = txes;
        txes = null;
        Object object2 = idx;
        idx = null;
        return new TailTxIter(object, RT.longCast((Object)((Number)object2)));
    }
}

