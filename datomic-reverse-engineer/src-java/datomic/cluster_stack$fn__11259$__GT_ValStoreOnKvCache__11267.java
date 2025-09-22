/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 */
package datomic;

import clojure.lang.AFunction;
import datomic.cluster_stack.ValStoreOnKvCache;

public final class cluster_stack$fn__11259$__GT_ValStoreOnKvCache__11267
extends AFunction {
    public Object invoke(Object exec, Object kv_cache2) {
        Object object = exec;
        exec = null;
        Object object2 = kv_cache2;
        kv_cache2 = null;
        return new ValStoreOnKvCache(object, object2);
    }
}

