/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.RT
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.RT;
import java.lang.reflect.Array;

public final class memory_size$array_type
extends AFunction {
    public static Object invokeStatic(Object t) {
        Object object = t;
        t = null;
        return Array.newInstance((Class)object, RT.intCast((long)0L)).getClass();
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return memory_size$array_type.invokeStatic(object2);
    }
}

