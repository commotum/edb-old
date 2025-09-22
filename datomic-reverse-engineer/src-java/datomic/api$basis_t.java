/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.Numbers
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.Numbers;
import datomic.Database;

public final class api$basis_t
extends AFunction {
    public static Object invokeStatic(Object db2) {
        Object object = db2;
        db2 = null;
        return Numbers.num((long)((Database)object).basisT());
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return api$basis_t.invokeStatic(object2);
    }
}

