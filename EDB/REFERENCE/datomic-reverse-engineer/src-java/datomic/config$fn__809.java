/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.RT
 *  clojure.lang.Util
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.RT;
import clojure.lang.Util;
import clojure.lang.Var;

public final class config$fn__809
extends AFunction {
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"int?");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"<=");
    public static final Object const__3 = 1L;
    public static final Object const__4 = 1000L;

    public static Object invokeStatic(Object v) {
        Object object;
        boolean or__5238__auto__812 = Util.identical((Object)v, null);
        if (or__5238__auto__812) {
            object = or__5238__auto__812 ? Boolean.TRUE : Boolean.FALSE;
        } else {
            Object and__5236__auto__811;
            Object object2 = and__5236__auto__811 = ((IFn)const__1.getRawRoot()).invoke(v);
            if (object2 != null && object2 != Boolean.FALSE) {
                Object object3 = v;
                v = null;
                object = ((IFn)const__2.getRawRoot()).invoke(const__3, object3, const__4);
            } else {
                object = and__5236__auto__811;
                Object var2_2 = null;
            }
        }
        return object;
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return config$fn__809.invokeStatic(object2);
    }
}

