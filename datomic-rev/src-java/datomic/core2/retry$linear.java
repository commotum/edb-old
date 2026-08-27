/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.PersistentArrayMap
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic.core2;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.PersistentArrayMap;
import clojure.lang.RT;
import clojure.lang.Var;

public final class retry$linear
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.core2.retry", (String)"linear");
    public static final Var const__1 = RT.var((String)"datomic.core2.retry", (String)"retry");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"partial");
    public static final Var const__3 = RT.var((String)"datomic.core2.retry", (String)"limiting-retry");
    public static final Var const__4 = RT.var((String)"clojure.core", (String)"constantly");

    public static Object invokeStatic(Object f, Object pred2, Object iter2, Object backoff, Object opts) {
        Object object = f;
        f = null;
        Object object2 = pred2;
        pred2 = null;
        Object object3 = iter2;
        iter2 = null;
        Object object4 = backoff;
        backoff = null;
        Object object5 = opts;
        opts = null;
        return ((IFn)const__1.getRawRoot()).invoke(object, object2, ((IFn)const__2.getRawRoot()).invoke(const__3.getRawRoot(), object3), ((IFn)const__4.getRawRoot()).invoke(object4), object5);
    }

    public Object invoke(Object object, Object object2, Object object3, Object object4, Object object5) {
        Object object6 = object;
        object = null;
        Object object7 = object2;
        object2 = null;
        Object object8 = object3;
        object3 = null;
        Object object9 = object4;
        object4 = null;
        Object object10 = object5;
        object5 = null;
        return retry$linear.invokeStatic(object6, object7, object8, object9, object10);
    }

    public static Object invokeStatic(Object f, Object pred2, Object iter2, Object backoff) {
        Object object = f;
        f = null;
        Object object2 = pred2;
        pred2 = null;
        Object object3 = iter2;
        iter2 = null;
        Object object4 = backoff;
        backoff = null;
        return ((IFn)const__0.getRawRoot()).invoke(object, object2, object3, object4, (Object)PersistentArrayMap.EMPTY);
    }

    public Object invoke(Object object, Object object2, Object object3, Object object4) {
        Object object5 = object;
        object = null;
        Object object6 = object2;
        object2 = null;
        Object object7 = object3;
        object3 = null;
        Object object8 = object4;
        object4 = null;
        return retry$linear.invokeStatic(object5, object6, object7, object8);
    }
}

