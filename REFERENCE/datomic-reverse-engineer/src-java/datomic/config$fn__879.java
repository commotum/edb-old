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

public final class config$fn__879
extends AFunction {
    public static final Var const__1 = RT.var((String)"datomic.config", (String)"property");

    public static Object invokeStatic() {
        return RT.booleanCast((Object)((IFn)const__1.getRawRoot()).invoke((Object)"datomic.prefetchProbes")) ? Boolean.TRUE : Boolean.FALSE;
    }

    public Object invoke() {
        return config$fn__879.invokeStatic();
    }
}

