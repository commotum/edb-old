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

public final class db$fn__14161
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.db", (String)"bootstrap-db*");

    public static Object invokeStatic() {
        return ((IFn)const__0.getRawRoot()).invoke();
    }

    public Object invoke() {
        return db$fn__14161.invokeStatic();
    }
}

