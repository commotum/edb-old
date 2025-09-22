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

public final class db$dget
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.iter", (String)"iget");

    public static Object invokeStatic(Object iter2) {
        Object object = iter2;
        iter2 = null;
        return ((IFn)const__0.getRawRoot()).invoke(object);
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return db$dget.invokeStatic(object2);
    }
}

