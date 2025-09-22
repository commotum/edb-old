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

public final class coordination$lookup_compatible_transactor_endpoint
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.coordination", (String)"lookup-transactor-endpoint");
    public static final Var const__1 = RT.var((String)"datomic.coordination", (String)"check-peer-version");

    public static Object invokeStatic(Object cluster2) {
        Object object = cluster2;
        cluster2 = null;
        Object endpoint = ((IFn)const__0.getRawRoot()).invoke(object);
        ((IFn)const__1.getRawRoot()).invoke(endpoint);
        Object var1_1 = null;
        return endpoint;
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return coordination$lookup_compatible_transactor_endpoint.invokeStatic(object2);
    }
}

