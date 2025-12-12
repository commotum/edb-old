/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.IObj
 *  clojure.lang.PersistentArrayMap
 *  clojure.lang.PersistentList
 *  clojure.lang.RT
 *  clojure.lang.Symbol
 *  clojure.lang.Util
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.IObj;
import clojure.lang.PersistentArrayMap;
import clojure.lang.PersistentList;
import clojure.lang.RT;
import clojure.lang.Symbol;
import clojure.lang.Util;
import clojure.lang.Var;
import datomic.cluster.ClusteredStore;
import datomic.cluster.Dbid;
import java.util.Arrays;

public final class catalog$get_catalog
extends AFunction {
    private static Class __cached_class__0;
    private static Class __cached_class__1;
    public static final Var const__1;
    public static final Var const__2;
    public static final Var const__3;
    public static final Object const__4;
    public static final Var const__5;
    public static final Var const__6;
    public static final Var const__7;

    /*
     * Unable to fully structure code
     * Could not resolve type clashes
     */
    public static Object invokeStatic(Object cluster) {
        v0 = cluster;
        if (Util.classOf((Object)v0) == catalog$get_catalog.__cached_class__0) ** GOTO lbl6
        if (!(v0 instanceof Dbid)) {
            v0 = v0;
            catalog$get_catalog.__cached_class__0 = Util.classOf((Object)v0);
lbl6:
            // 2 sources

            v1 = catalog$get_catalog.const__1.getRawRoot().invoke(v0);
        } else {
            v1 = ((Dbid)v0).dbId();
        }
        if (!Util.identical((Object)v1, null)) {
            throw (Throwable)new AssertionError(((IFn)catalog$get_catalog.const__2.getRawRoot()).invoke((Object)"Assert failed: ", ((IFn)catalog$get_catalog.const__3.getRawRoot()).invoke(catalog$get_catalog.const__4)));
        }
        v2 = (IFn)catalog$get_catalog.const__5.getRawRoot();
        v3 = cluster;
        cluster = null;
        v4 = v3;
        if (Util.classOf((Object)v3) == catalog$get_catalog.__cached_class__1) ** GOTO lbl19
        if (!(v4 instanceof ClusteredStore)) {
            v4 = v4;
            catalog$get_catalog.__cached_class__1 = Util.classOf((Object)v4);
lbl19:
            // 2 sources

            v5 = catalog$get_catalog.const__6.getRawRoot().invoke(v4, (Object)"pod-catalog");
        } else {
            v5 = ((ClusteredStore)v4).get_pod("pod-catalog");
        }
        v6 = temp__5455__auto__11079 = v2.invoke(v5);
        if (v6 != null && v6 != Boolean.FALSE) {
            v7 = temp__5455__auto__11079;
            temp__5455__auto__11079 = null;
            v8 = pod = v7;
            pod = null;
            v9 /* !! */  = ((IFn)catalog$get_catalog.const__7.getRawRoot()).invoke(v8);
        } else {
            v9 /* !! */  = PersistentArrayMap.EMPTY;
        }
        return v9 /* !! */ ;
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return catalog$get_catalog.invokeStatic(object2);
    }

    static {
        const__1 = RT.var((String)"datomic.cluster", (String)"dbId");
        const__2 = RT.var((String)"clojure.core", (String)"str");
        const__3 = RT.var((String)"clojure.core", (String)"pr-str");
        const__4 = ((IObj)PersistentList.create(Arrays.asList(Symbol.intern(null, (String)"nil?"), ((IObj)PersistentList.create(Arrays.asList(Symbol.intern((String)"cluster", (String)"dbId"), Symbol.intern(null, (String)"cluster")))).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"column"), 17}))))).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"column"), 11}));
        const__5 = RT.var((String)"clojure.core", (String)"deref");
        const__6 = RT.var((String)"datomic.cluster", (String)"get-pod");
        const__7 = RT.var((String)"datomic.catalog", (String)"pod->catalog");
    }
}

