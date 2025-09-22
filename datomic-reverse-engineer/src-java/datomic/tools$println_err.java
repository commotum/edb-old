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

public final class tools$println_err
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"push-thread-bindings");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"hash-map");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"*out*");
    public static final Var const__3 = RT.var((String)"clojure.core", (String)"*err*");
    public static final Var const__4 = RT.var((String)"clojure.core", (String)"println");
    public static final Var const__5 = RT.var((String)"clojure.core", (String)"pop-thread-bindings");

    public static Object invokeStatic(Object x) {
        Object object;
        ((IFn)const__0.getRawRoot()).invoke(((IFn)const__1.getRawRoot()).invoke((Object)const__2, const__3.get()));
        try {
            Object object2 = x;
            x = null;
            object = ((IFn)const__4.getRawRoot()).invoke(object2);
        }
        finally {
            ((IFn)const__5.getRawRoot()).invoke();
        }
        return object;
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return tools$println_err.invokeStatic(object2);
    }
}

