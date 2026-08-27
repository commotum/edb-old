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

public final class peer$fn__21677
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"pr");

    public static Object invokeStatic(Object x) {
        Object object = x;
        x = null;
        return ((IFn)const__0.get()).invoke(object);
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return peer$fn__21677.invokeStatic(object2);
    }
}

