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

public final class kv_cache$shutdown
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"reset!");
    public static final Var const__1 = RT.var((String)"datomic.kv-cache", (String)"kv-cache-ref");

    public static Object invokeStatic() {
        return ((IFn)const__0.getRawRoot()).invoke(const__1.getRawRoot(), null);
    }

    public Object invoke() {
        return kv_cache$shutdown.invokeStatic();
    }
}

