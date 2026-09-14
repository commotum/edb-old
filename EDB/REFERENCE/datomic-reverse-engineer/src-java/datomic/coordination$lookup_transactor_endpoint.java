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

public final class coordination$lookup_transactor_endpoint
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.coordination", (String)"lookup-endpoint");
    public static final Var const__1 = RT.var((String)"datomic.coordination", (String)"pod-key");

    public static Object invokeStatic(Object cluster2) {
        Object object = cluster2;
        cluster2 = null;
        return ((IFn)const__0.getRawRoot()).invoke(object, const__1.getRawRoot());
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return coordination$lookup_transactor_endpoint.invokeStatic(object2);
    }
}

