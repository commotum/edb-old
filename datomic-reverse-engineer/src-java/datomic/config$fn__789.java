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

public final class config$fn__789
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"<=");
    public static final Object const__1 = 1L;
    public static final Object const__2 = 32L;

    public static Object invokeStatic(Object p1__778_SHARP_) {
        Object object = p1__778_SHARP_;
        p1__778_SHARP_ = null;
        return ((IFn)const__0.getRawRoot()).invoke(const__1, object, const__2);
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return config$fn__789.invokeStatic(object2);
    }
}

