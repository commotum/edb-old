/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 */
package datomic;

import clojure.lang.AFunction;
import datomic.kv_mem.KVMem;

public final class kv_mem$fn__23555$__GT_KVMem__23561
extends AFunction {
    public Object invoke(Object m) {
        Object object = m;
        m = null;
        return new KVMem(object);
    }
}

