/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFn
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.Keyword
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFn;
import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Keyword;
import clojure.lang.RT;
import clojure.lang.Var;

public final class cluster_stack$cluster_with_cache
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.cluster-stack", (String)"cluster-with-cache");
    public static final Var const__1 = RT.var((String)"datomic.cluster-stack", (String)"val-store-on-cluster");
    public static final Var const__2 = RT.var((String)"datomic.core2.val-store.double-store", (String)"create");
    public static final Var const__3 = RT.var((String)"clojure.core", (String)"merge");
    public static final AFn const__6 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"repair-metric"), RT.keyword(null, (String)"kvc.repair")});
    public static final Keyword const__7 = RT.keyword(null, (String)"near-store");
    public static final Keyword const__8 = RT.keyword(null, (String)"far-store");
    public static final Var const__9 = RT.var((String)"datomic.val-cluster", (String)"val-cluster");
    public static final Var const__10 = RT.var((String)"datomic.combined-cluster", (String)"combined-cluster");

    public static Object invokeStatic(Object cluster2, Object cache2, Object opts) {
        Object caching_store;
        Object cluster_store = ((IFn)const__1.getRawRoot()).invoke(cluster2);
        Object object = opts;
        opts = null;
        Object[] objectArray = new Object[4];
        objectArray[0] = const__7;
        Object object2 = cache2;
        cache2 = null;
        objectArray[1] = object2;
        objectArray[2] = const__8;
        Object object3 = cluster_store;
        cluster_store = null;
        objectArray[3] = object3;
        Object object4 = caching_store = ((IFn)const__2.getRawRoot()).invoke(((IFn)const__3.getRawRoot()).invoke((Object)const__6, object, (Object)RT.mapUniqueKeys((Object[])objectArray)));
        caching_store = null;
        Object val_cluster2 = ((IFn)const__9.getRawRoot()).invoke(object4);
        Object object5 = cluster2;
        cluster2 = null;
        Object object6 = val_cluster2;
        val_cluster2 = null;
        return ((IFn)const__10.getRawRoot()).invoke(object5, object6);
    }

    public Object invoke(Object object, Object object2, Object object3) {
        Object object4 = object;
        object = null;
        Object object5 = object2;
        object2 = null;
        Object object6 = object3;
        object3 = null;
        return cluster_stack$cluster_with_cache.invokeStatic(object4, object5, object6);
    }

    public static Object invokeStatic(Object cluster2, Object cache2) {
        Object object = cluster2;
        cluster2 = null;
        Object object2 = cache2;
        cache2 = null;
        return ((IFn)const__0.getRawRoot()).invoke(object, object2, null);
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return cluster_stack$cluster_with_cache.invokeStatic(object3, object4);
    }
}

