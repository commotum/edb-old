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

public final class config$fn__846
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.config", (String)"reset!");

    public static Object invokeStatic() {
        return ((IFn)const__0.getRawRoot()).invoke();
    }

    public Object invoke() {
        return config$fn__846.invokeStatic();
    }
}

