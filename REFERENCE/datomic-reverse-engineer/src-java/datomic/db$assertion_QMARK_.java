/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 */
package datomic;

import clojure.lang.AFunction;
import datomic.impl.db.IDatum;

public final class db$assertion_QMARK_
extends AFunction {
    public static Object invokeStatic(Object d) {
        Object object = d;
        d = null;
        return ((IDatum)object).isAssertion() ? Boolean.TRUE : Boolean.FALSE;
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return db$assertion_QMARK_.invokeStatic(object2);
    }
}

