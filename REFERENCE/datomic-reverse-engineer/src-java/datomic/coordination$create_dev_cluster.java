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

public final class coordination$create_dev_cluster
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.kv-cluster", (String)"kv-cluster");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"deref");
    public static final Var const__2 = RT.var((String)"datomic.coordination", (String)"devspec");
    public static final Var const__3 = RT.var((String)"datomic.kv-sql", (String)"from-spec");
    public static final Var const__4 = RT.var((String)"datomic.h2", (String)"local-jdbc-spec");
    public static final Var const__5 = RT.var((String)"datomic.h2", (String)"remote-jdbc-spec");

    public static Object invokeStatic(Object cluster_conf) {
        Object object;
        Object temp__5455__auto__11670;
        IFn iFn = (IFn)const__0.getRawRoot();
        Object object2 = temp__5455__auto__11670 = ((IFn)const__1.getRawRoot()).invoke(const__2.getRawRoot());
        if (object2 != null && object2 != Boolean.FALSE) {
            Object spec;
            Object object3 = temp__5455__auto__11670;
            temp__5455__auto__11670 = null;
            Object object4 = spec = object3;
            spec = null;
            object = ((IFn)const__3.getRawRoot()).invoke(((IFn)const__4.getRawRoot()).invoke(object4));
        } else {
            object = ((IFn)const__3.getRawRoot()).invoke(((IFn)const__5.getRawRoot()).invoke(cluster_conf));
        }
        Object object5 = cluster_conf;
        cluster_conf = null;
        return iFn.invoke(object, object5);
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return coordination$create_dev_cluster.invokeStatic(object2);
    }
}

