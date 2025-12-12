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

public final class h2$create_sql_spec
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.h2", (String)"driver-manager-lock");
    public static final Var const__1 = RT.var((String)"datomic.h2", (String)"create-sql-spec*");

    public static Object invokeStatic(Object cluster_map) {
        Object object;
        Object lockee__5436__auto__11599 = const__0.getRawRoot();
        try {
            synchronized (lockee__5436__auto__11599) {
                Object object2 = cluster_map;
                cluster_map = null;
                object = ((IFn)const__1.getRawRoot()).invoke(object2);
            }
        }
        finally {
            Object object3 = lockee__5436__auto__11599;
            lockee__5436__auto__11599 = null;
            // ** MonitorExit[v1] (shouldn't be in output)
        }
        {
            return object;
        }
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return h2$create_sql_spec.invokeStatic(object2);
    }
}

