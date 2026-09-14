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

public final class tools$class_sym
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"symbol");

    public static Object invokeStatic(Object obj) {
        Object object = obj;
        obj = null;
        return ((IFn)const__0.getRawRoot()).invoke((Object)object.getClass().getName());
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return tools$class_sym.invokeStatic(object2);
    }
}

