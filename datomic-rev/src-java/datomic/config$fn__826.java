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

public final class config$fn__826
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"integer?");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"<=");
    public static final Object const__2 = 1L;
    public static final Object const__3 = 5L;

    public static Object invokeStatic(Object p1__781_SHARP_) {
        Object object;
        Object and__5236__auto__828;
        Object object2 = and__5236__auto__828 = ((IFn)const__0.getRawRoot()).invoke(p1__781_SHARP_);
        if (object2 != null && object2 != Boolean.FALSE) {
            Object object3 = p1__781_SHARP_;
            p1__781_SHARP_ = null;
            object = ((IFn)const__1.getRawRoot()).invoke(const__2, object3, const__3);
        } else {
            object = and__5236__auto__828;
            Object var1_1 = null;
        }
        return object;
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return config$fn__826.invokeStatic(object2);
    }
}

