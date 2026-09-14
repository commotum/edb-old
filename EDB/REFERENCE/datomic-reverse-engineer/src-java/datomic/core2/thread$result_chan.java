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

public final class thread$result_chan
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core.async", (String)"chan");
    public static final Object const__1 = 1L;
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"take");
    public static final Var const__3 = RT.var((String)"clojure.core", (String)"comp");

    public static Object invokeStatic(Object n, Object xform) {
        Object object = n;
        Object object2 = xform;
        xform = null;
        Object object3 = n;
        n = null;
        return ((IFn)const__0.getRawRoot()).invoke(object, ((IFn)const__3.getRawRoot()).invoke(object2, ((IFn)const__2.getRawRoot()).invoke(object3)));
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return thread$result_chan.invokeStatic(object3, object4);
    }

    public static Object invokeStatic(Object n) {
        Object object = n;
        Object object2 = n;
        n = null;
        return ((IFn)const__0.getRawRoot()).invoke(object, ((IFn)const__2.getRawRoot()).invoke(object2));
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return thread$result_chan.invokeStatic(object2);
    }

    public static Object invokeStatic() {
        return ((IFn)const__0.getRawRoot()).invoke(const__1, ((IFn)const__2.getRawRoot()).invoke(const__1));
    }

    public Object invoke() {
        return thread$result_chan.invokeStatic();
    }
}

