/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.PersistentVector
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.PersistentVector;
import java.util.List;

public final class common$ensure_vector
extends AFunction {
    public static Object invokeStatic(Object x) {
        Object object;
        if (x instanceof List) {
            Object object2 = x;
            x = null;
            object = PersistentVector.create((List)((List)object2));
        } else {
            object = x;
            Object object3 = null;
        }
        return object;
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return common$ensure_vector.invokeStatic(object2);
    }
}

