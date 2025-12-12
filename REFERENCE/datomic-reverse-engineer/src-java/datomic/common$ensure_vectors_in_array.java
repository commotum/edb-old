/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.PersistentVector
 *  clojure.lang.RT
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.PersistentVector;
import clojure.lang.RT;
import java.util.List;

public final class common$ensure_vectors_in_array
extends AFunction {
    public static Object invokeStatic(Object vs) {
        long n__5742__auto__9261 = ((Object[])vs).length;
        for (long i = 0L; i < n__5742__auto__9261; ++i) {
            Object elem = RT.aget((Object[])((Object[])vs), (int)((int)i));
            if (!(elem instanceof List)) continue;
            Object object = elem;
            elem = null;
            RT.aset((Object[])((Object[])vs), (int)((int)i), (Object)PersistentVector.create((List)((List)object)));
        }
        Object object = null;
        return vs;
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return common$ensure_vectors_in_array.invokeStatic(object2);
    }
}

