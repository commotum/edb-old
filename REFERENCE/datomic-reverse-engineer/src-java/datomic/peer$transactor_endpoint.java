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

public final class peer$transactor_endpoint
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.peer", (String)"tx-group-endpoint");
    public static final Var const__1 = RT.var((String)"datomic.coordination", (String)"pod-key");

    public static Object invokeStatic(Object uri2) {
        Object object = uri2;
        uri2 = null;
        return ((IFn)const__0.getRawRoot()).invoke(object, const__1.getRawRoot());
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return peer$transactor_endpoint.invokeStatic(object2);
    }
}

