/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 */
package datomic;

import clojure.lang.AFunction;
import datomic.Database;

public final class api$is_filtered
extends AFunction {
    public static Object invokeStatic(Object db2) {
        Object object = db2;
        db2 = null;
        return ((Database)object).isFiltered() ? Boolean.TRUE : Boolean.FALSE;
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return api$is_filtered.invokeStatic(object2);
    }
}

