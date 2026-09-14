/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.Keyword
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Keyword;
import clojure.lang.RT;
import clojure.lang.Var;

public final class coordination$create_system_cluster
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.coordination", (String)"create-cluster");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"dissoc");
    public static final Keyword const__2 = RT.keyword(null, (String)"db-id");

    public static Object invokeStatic(Object cluster_conf) {
        Object object = cluster_conf;
        cluster_conf = null;
        return ((IFn)const__0.getRawRoot()).invoke(((IFn)const__1.getRawRoot()).invoke(object, (Object)const__2));
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return coordination$create_system_cluster.invokeStatic(object2);
    }
}

