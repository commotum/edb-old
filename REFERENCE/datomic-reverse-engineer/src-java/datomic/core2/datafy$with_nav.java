/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFn
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.RT
 *  clojure.lang.Symbol
 *  clojure.lang.Var
 */
package datomic.core2;

import clojure.lang.AFn;
import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.RT;
import clojure.lang.Symbol;
import clojure.lang.Var;

public final class datafy$with_nav
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"vary-meta");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"assoc");
    public static final AFn const__2 = (AFn)Symbol.intern((String)"clojure.core.protocols", (String)"nav");

    public static Object invokeStatic(Object x, Object nav_fn) {
        Object object;
        Object object2 = x;
        if (object2 != null && object2 != Boolean.FALSE) {
            Object object3 = x;
            x = null;
            Object object4 = nav_fn;
            nav_fn = null;
            object = ((IFn)const__0.getRawRoot()).invoke(object3, const__1.getRawRoot(), (Object)const__2, object4);
        } else {
            object = null;
        }
        return object;
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return datafy$with_nav.invokeStatic(object3, object4);
    }
}

