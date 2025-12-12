/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic.core2;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.RT;
import clojure.lang.Var;

public final class async$_LT__BANG__BANG_x
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core.async", (String)"<!!");
    public static final Var const__1 = RT.var((String)"datomic.core2.async", (String)"channel-closed-error");

    public static Object invokeStatic(Object ch) {
        Object object;
        Object or__5581__auto__19657;
        Object object2 = ch;
        ch = null;
        Object v = ((IFn)const__0.getRawRoot()).invoke(object2);
        Object object3 = or__5581__auto__19657 = ((IFn)const__1.getRawRoot()).invoke(v);
        if (object3 != null && object3 != Boolean.FALSE) {
            object = or__5581__auto__19657;
            or__5581__auto__19657 = null;
        } else {
            object = v;
            Object var1_1 = null;
        }
        return object;
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return async$_LT__BANG__BANG_x.invokeStatic(object2);
    }
}

