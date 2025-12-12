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
import datomic.index.RootNode;

public final class treewalk$fn__19716
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"map");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"str");

    public static Object invokeStatic(Object rn) {
        Object object = rn;
        rn = null;
        return ((IFn)const__0.getRawRoot()).invoke(const__1.getRawRoot(), ((RootNode)object).dirids);
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return treewalk$fn__19716.invokeStatic(object2);
    }
}

