/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.ILookupThunk
 *  clojure.lang.IObj
 *  clojure.lang.Keyword
 *  clojure.lang.KeywordLookupSite
 *  clojure.lang.Numbers
 *  clojure.lang.PersistentList
 *  clojure.lang.RT
 *  clojure.lang.Symbol
 *  clojure.lang.Util
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.ILookupThunk;
import clojure.lang.IObj;
import clojure.lang.Keyword;
import clojure.lang.KeywordLookupSite;
import clojure.lang.Numbers;
import clojure.lang.PersistentList;
import clojure.lang.RT;
import clojure.lang.Symbol;
import clojure.lang.Util;
import clojure.lang.Var;
import datomic.cluster.Dbid;
import java.util.Arrays;

public final class catalog$put_catalog
extends AFunction {
    private static Class __cached_class__0;
    public static final Var const__1;
    public static final Var const__2;
    public static final Var const__3;
    public static final Object const__4;
    public static final Keyword const__5;
    public static final Object const__7;
    public static final Var const__8;
    public static final Var const__9;
    public static final Var const__10;
    public static final Var const__11;
    public static final Var const__13;
    static final KeywordLookupSite __site__0__;
    static ILookupThunk __thunk__0__;
    static final KeywordLookupSite __site__1__;
    static ILookupThunk __thunk__1__;

    /*
     * Unable to fully structure code
     */
    public static Object invokeStatic(Object cluster, Object catalog_map) {
        v0 = cluster;
        if (Util.classOf((Object)v0) == catalog$put_catalog.__cached_class__0) ** GOTO lbl6
        if (!(v0 instanceof Dbid)) {
            v0 = v0;
            catalog$put_catalog.__cached_class__0 = Util.classOf((Object)v0);
lbl6:
            // 2 sources

            v1 = catalog$put_catalog.const__1.getRawRoot().invoke(v0);
        } else {
            v1 = ((Dbid)v0).dbId();
        }
        if (!Util.identical((Object)v1, null)) {
            throw (Throwable)new AssertionError(((IFn)catalog$put_catalog.const__2.getRawRoot()).invoke((Object)"Assert failed: ", ((IFn)catalog$put_catalog.const__3.getRawRoot()).invoke(catalog$put_catalog.const__4)));
        }
        v2 = catalog$put_catalog.__thunk__0__;
        v3 = catalog_map;
        v4 = v2.get(v3);
        if (v2 == v4) {
            catalog$put_catalog.__thunk__0__ = catalog$put_catalog.__site__0__.fault(v3);
            v4 = catalog$put_catalog.__thunk__0__.get(v3);
        }
        v5 = current_rev = v4;
        if (v5 != null && v5 != Boolean.FALSE) {
            v6 = current_rev;
            current_rev = null;
            v7 = Numbers.inc((Object)v6);
        } else {
            v7 = catalog$put_catalog.const__7;
        }
        next_rev = v7;
        v8 = catalog_map;
        catalog_map = null;
        catalog = ((IFn)catalog$put_catalog.const__8.getRawRoot()).invoke(v8, (Object)catalog$put_catalog.const__5);
        v9 = cluster;
        cluster = null;
        v10 = next_rev;
        next_rev = null;
        v11 = catalog;
        catalog = null;
        pod = ((IFn)catalog$put_catalog.const__9.getRawRoot()).invoke(((IFn)catalog$put_catalog.const__10.getRawRoot()).invoke(v9, (Object)"pod-catalog", v10, null, ((IFn)catalog$put_catalog.const__11.getRawRoot()).invoke(v11)));
        v12 = catalog$put_catalog.__thunk__1__;
        v13 = pod;
        v14 = v12.get(v13);
        if (v12 == v14) {
            catalog$put_catalog.__thunk__1__ = catalog$put_catalog.__site__1__.fault(v13);
            v14 = catalog$put_catalog.__thunk__1__.get(v13);
        }
        if (v14 != null && v14 != Boolean.FALSE) {
            v15 = pod;
            pod = null;
        } else {
            v16 = pod;
            pod = null;
            v15 = ((IFn)catalog$put_catalog.const__13.getRawRoot()).invoke(v16);
        }
        return v15;
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return catalog$put_catalog.invokeStatic(object3, object4);
    }

    static {
        const__1 = RT.var((String)"datomic.cluster", (String)"dbId");
        const__2 = RT.var((String)"clojure.core", (String)"str");
        const__3 = RT.var((String)"clojure.core", (String)"pr-str");
        const__4 = ((IObj)PersistentList.create(Arrays.asList(Symbol.intern(null, (String)"nil?"), ((IObj)PersistentList.create(Arrays.asList(Symbol.intern((String)"cluster", (String)"dbId"), Symbol.intern(null, (String)"cluster")))).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"column"), 17}))))).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"column"), 11}));
        const__5 = RT.keyword((String)"datomic", (String)"rev");
        const__7 = 0L;
        const__8 = RT.var((String)"clojure.core", (String)"dissoc");
        const__9 = RT.var((String)"clojure.core", (String)"deref");
        const__10 = RT.var((String)"datomic.cluster", (String)"update-pod");
        const__11 = RT.var((String)"datomic.io", (String)"clj->bbuf");
        const__13 = RT.var((String)"datomic.catalog", (String)"pod->catalog");
        __site__0__ = new KeywordLookupSite(RT.keyword((String)"datomic", (String)"rev"));
        __thunk__0__ = __site__0__;
        __site__1__ = new KeywordLookupSite(RT.keyword(null, (String)"failed"));
        __thunk__1__ = __site__1__;
    }
}

