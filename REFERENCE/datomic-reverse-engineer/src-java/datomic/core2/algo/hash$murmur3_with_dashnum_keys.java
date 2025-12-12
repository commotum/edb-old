/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic.core2.algo;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.RT;
import clojure.lang.Var;
import datomic.core2.algo.hash$murmur3_with_dashnum_keys$fn__19407;

public final class hash$murmur3_with_dashnum_keys
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"map");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"range");

    public static Object invokeStatic(Object k) {
        Object object = k;
        k = null;
        return ((IFn)const__0.getRawRoot()).invoke((Object)new hash$murmur3_with_dashnum_keys$fn__19407(object), ((IFn)const__1.getRawRoot()).invoke());
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return hash$murmur3_with_dashnum_keys.invokeStatic(object2);
    }
}

