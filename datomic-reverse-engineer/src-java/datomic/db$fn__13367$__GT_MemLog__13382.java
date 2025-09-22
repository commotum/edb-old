/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 */
package datomic;

import clojure.lang.AFunction;
import datomic.db.MemLog;

public final class db$fn__13367$__GT_MemLog__13382
extends AFunction {
    public Object invoke(Object txes) {
        Object object = txes;
        txes = null;
        return new MemLog(object);
    }
}

