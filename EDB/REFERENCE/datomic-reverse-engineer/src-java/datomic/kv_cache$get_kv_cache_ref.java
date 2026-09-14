/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.RT;
import clojure.lang.Var;

public final class kv_cache$get_kv_cache_ref
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.kv-cache", (String)"kv-cache-ref");

    public static Object invokeStatic() {
        return const__0.getRawRoot();
    }

    public Object invoke() {
        return kv_cache$get_kv_cache_ref.invokeStatic();
    }
}

