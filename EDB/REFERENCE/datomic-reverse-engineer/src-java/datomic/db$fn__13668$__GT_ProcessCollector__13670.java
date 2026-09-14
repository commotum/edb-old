/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 */
package datomic;

import clojure.lang.AFunction;
import datomic.db.ProcessCollector;

public final class db$fn__13668$__GT_ProcessCollector__13670
extends AFunction {
    public Object invoke(Object arraylist) {
        Object object = arraylist;
        arraylist = null;
        return new ProcessCollector(object);
    }
}

