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

public final class function$fn__11973
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.function", (String)"print-function");

    public static Object invokeStatic(Object dbfn, Object w) {
        Object object = dbfn;
        dbfn = null;
        Object object2 = w;
        w = null;
        return ((IFn)const__0.getRawRoot()).invoke(object, object2);
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return function$fn__11973.invokeStatic(object3, object4);
    }
}

