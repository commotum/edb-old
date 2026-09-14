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

public final class cluster_stack$start_kv_cache$wrap__11445
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.cluster-stack", (String)"val-store-on-kv-cache");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"deref");
    public static final Var const__2 = RT.var((String)"datomic.cluster-stack", (String)"pool-ref");

    public Object invoke(Object x) {
        Object object;
        Object object2 = x;
        if (object2 != null && object2 != Boolean.FALSE) {
            Object object3 = x;
            x = null;
            cluster_stack$start_kv_cache$wrap__11445 this_ = null;
            object = ((IFn)const__0.getRawRoot()).invoke(((IFn)const__1.getRawRoot()).invoke(const__2.getRawRoot()), object3);
        } else {
            object = null;
        }
        return object;
    }
}

