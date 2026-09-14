/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic.cache;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.RT;
import clojure.lang.Var;

public final class caffeine$adapt_caffeine_cache
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.cache.caffeine", (String)"->WrappedCCache");

    public static Object invokeStatic(Object cache2) {
        Object object = cache2;
        cache2 = null;
        return ((IFn)const__0.getRawRoot()).invoke(object);
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return caffeine$adapt_caffeine_cache.invokeStatic(object2);
    }
}

