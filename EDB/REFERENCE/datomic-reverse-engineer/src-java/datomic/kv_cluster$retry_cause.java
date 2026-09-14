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

public final class kv_cluster$retry_cause
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"class");
    public static final Var const__1 = RT.var((String)"datomic.kv-cluster", (String)"root-cause");

    public static Object invokeStatic(Object result2) {
        Object object = result2;
        result2 = null;
        return ((Class)((IFn)const__0.getRawRoot()).invoke(((IFn)const__1.getRawRoot()).invoke(object))).getName();
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return kv_cluster$retry_cause.invokeStatic(object2);
    }
}

