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

public final class config$fn__863
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.config", (String)"pro?");

    public static Object invokeStatic(Object _) {
        return ((IFn)const__0.getRawRoot()).invoke();
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return config$fn__863.invokeStatic(object2);
    }
}

