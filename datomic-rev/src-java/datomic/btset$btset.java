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
import datomic.btset.BTSet;

public final class btset$btset
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.btset", (String)"btset");

    public static Object invokeStatic(Object cmp) {
        Object object = cmp;
        cmp = null;
        return new BTSet(object, 0L, null);
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return btset$btset.invokeStatic(object2);
    }

    public static Object invokeStatic() {
        return ((IFn)const__0.getRawRoot()).invoke(null);
    }

    public Object invoke() {
        return btset$btset.invokeStatic();
    }
}

