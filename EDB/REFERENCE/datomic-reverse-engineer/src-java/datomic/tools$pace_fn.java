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
import datomic.tools$pace_fn$fn__21787;

public final class tools$pace_fn
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"atom");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"deref");
    public static final Var const__2 = RT.var((String)"datomic.kv-cluster", (String)"val-gets-ref");

    public static Object invokeStatic(Object pace_msec) {
        Object last_val_gets;
        Object object = last_val_gets = ((IFn)const__0.getRawRoot()).invoke(((IFn)const__1.getRawRoot()).invoke(const__2.getRawRoot()));
        last_val_gets = null;
        Object object2 = pace_msec;
        pace_msec = null;
        return new tools$pace_fn$fn__21787(object, object2);
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return tools$pace_fn.invokeStatic(object2);
    }
}

