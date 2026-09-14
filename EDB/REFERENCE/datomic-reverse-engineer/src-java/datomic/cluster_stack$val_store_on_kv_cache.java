/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.RT;
import clojure.lang.Var;

public final class cluster_stack$val_store_on_kv_cache
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.cluster-stack", (String)"->ValStoreOnKvCache");

    public static Object invokeStatic(Object exec, Object kv_cache2) {
        Object object = exec;
        exec = null;
        Object object2 = kv_cache2;
        kv_cache2 = null;
        return ((IFn)const__0.getRawRoot()).invoke(object, object2);
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return cluster_stack$val_store_on_kv_cache.invokeStatic(object3, object4);
    }
}

