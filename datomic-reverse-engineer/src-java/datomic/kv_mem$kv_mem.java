/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 */
package datomic;

import clojure.lang.AFunction;
import datomic.kv_mem.KVMem;

public final class kv_mem$kv_mem
extends AFunction {
    public static Object invokeStatic(Object m) {
        Object object = m;
        m = null;
        return new KVMem(object);
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return kv_mem$kv_mem.invokeStatic(object2);
    }
}

