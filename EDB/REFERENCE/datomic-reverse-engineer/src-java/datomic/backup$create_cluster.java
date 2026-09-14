/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.Keyword
 *  clojure.lang.Numbers
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Keyword;
import clojure.lang.Numbers;
import clojure.lang.RT;
import clojure.lang.Var;

public final class backup$create_cluster
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.coordination", (String)"create-db-cluster");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"assoc");
    public static final Keyword const__2 = RT.keyword(null, (String)"read-concurrency");
    public static final Var const__4 = RT.var((String)"datomic.config", (String)"property");
    public static final Keyword const__5 = RT.keyword(null, (String)"write-concurrency");
    public static final Keyword const__6 = RT.keyword(null, (String)"shared-pool?");

    public static Object invokeStatic(Object cluster_conf, Object concurrency) {
        Object object = cluster_conf;
        cluster_conf = null;
        Object object2 = Numbers.max((Object)concurrency, (Object)((IFn)const__4.getRawRoot()).invoke((Object)"datomic.readConcurrency"));
        Object object3 = concurrency;
        concurrency = null;
        return ((IFn)const__0.getRawRoot()).invoke(((IFn)const__1.getRawRoot()).invoke(object, (Object)const__2, object2, (Object)const__5, Numbers.max((Object)object3, (Object)((IFn)const__4.getRawRoot()).invoke((Object)"datomic.writeConcurrency")), (Object)const__6, (Object)Boolean.FALSE));
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return backup$create_cluster.invokeStatic(object3, object4);
    }
}

