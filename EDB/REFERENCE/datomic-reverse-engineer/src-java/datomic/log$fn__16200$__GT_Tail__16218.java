/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 */
package datomic;

import clojure.lang.AFunction;
import datomic.log.Tail;

public final class log$fn__16200$__GT_Tail__16218
extends AFunction {
    public Object invoke(Object txes, Object bufs) {
        Object object = txes;
        txes = null;
        Object object2 = bufs;
        bufs = null;
        return new Tail(object, object2);
    }
}

