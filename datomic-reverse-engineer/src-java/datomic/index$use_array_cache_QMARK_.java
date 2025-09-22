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

public final class index$use_array_cache_QMARK_
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"deref");
    public static final Var const__1 = RT.var((String)"datomic.index", (String)"array-cache-ref");

    public static Object invokeStatic() {
        return ((IFn)const__0.getRawRoot()).invoke(const__1.getRawRoot());
    }

    public Object invoke() {
        return index$use_array_cache_QMARK_.invokeStatic();
    }
}

