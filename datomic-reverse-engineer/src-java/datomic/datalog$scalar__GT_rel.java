/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.PersistentVector
 *  clojure.lang.Tuple
 *  clojure.lang.Util
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.PersistentVector;
import clojure.lang.Tuple;
import clojure.lang.Util;

public final class datalog$scalar__GT_rel
extends AFunction {
    public static Object invokeStatic(Object x) {
        PersistentVector persistentVector;
        if (Util.identical((Object)x, null)) {
            persistentVector = PersistentVector.EMPTY;
        } else {
            Object object = x;
            x = null;
            persistentVector = Tuple.create((Object)Tuple.create((Object)object));
        }
        return persistentVector;
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return datalog$scalar__GT_rel.invokeStatic(object2);
    }
}

