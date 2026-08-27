/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 */
package datomic;

import clojure.lang.AFunction;
import datomic.log.LogTailValue;

public final class log$fn__16456$__GT_LogTailValue__16471
extends AFunction {
    public Object invoke(Object txes) {
        Object object = txes;
        txes = null;
        return new LogTailValue(object);
    }
}

