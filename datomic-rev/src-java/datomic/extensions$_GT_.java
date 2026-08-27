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

public final class extensions$_GT_
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.extensions", (String)"<");

    public static Object invokeStatic(Object a, Object b) {
        Object object = b;
        b = null;
        Object object2 = a;
        a = null;
        return ((IFn)const__0.getRawRoot()).invoke(object, object2);
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return extensions$_GT_.invokeStatic(object3, object4);
    }
}

