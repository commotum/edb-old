/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.ILookupThunk
 *  clojure.lang.ISeq
 *  clojure.lang.Keyword
 *  clojure.lang.KeywordLookupSite
 *  clojure.lang.PersistentHashMap
 *  clojure.lang.RT
 *  clojure.lang.Util
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.ILookupThunk;
import clojure.lang.ISeq;
import clojure.lang.Keyword;
import clojure.lang.KeywordLookupSite;
import clojure.lang.PersistentHashMap;
import clojure.lang.RT;
import clojure.lang.Util;
import clojure.lang.Var;
import datomic.cluster.ClusteredStore;
import datomic.integrity$fulltext_storage_seq$branch_QMARK___22504;
import datomic.integrity$fulltext_storage_seq$children__22509;

public final class integrity$fulltext_storage_seq
extends AFunction {
    private static Class __cached_class__0;
    public static final Var const__0;
    public static final Var const__1;
    public static final Var const__2;
    public static final Var const__3;
    public static final Keyword const__5;
    public static final Keyword const__6;
    public static final Var const__8;
    public static final Var const__9;
    public static final Var const__10;
    public static final Var const__11;
    public static final Keyword const__12;
    public static final Keyword const__13;
    public static final Keyword const__14;
    public static final Keyword const__15;
    static final KeywordLookupSite __site__0__;
    static ILookupThunk __thunk__0__;

    /*
     * Unable to fully structure code
     */
    public static Object invokeStatic(Object uri, Object k) {
        v0 = uri;
        uri = null;
        v1 = uri = ((IFn)integrity$fulltext_storage_seq.const__0.getRawRoot()).invoke(v0);
        uri = null;
        map__22502 = ((IFn)integrity$fulltext_storage_seq.const__1.getRawRoot()).invoke(v1);
        v2 = ((IFn)integrity$fulltext_storage_seq.const__2.getRawRoot()).invoke(map__22502);
        if (v2 != null && v2 != Boolean.FALSE) {
            v3 = map__22502;
            map__22502 = null;
            v4 = PersistentHashMap.create((ISeq)((ISeq)((IFn)integrity$fulltext_storage_seq.const__3.getRawRoot()).invoke(v3)));
        } else {
            v4 = map__22502;
            map__22502 = null;
        }
        map__22502 = v4;
        cluster = RT.get((Object)map__22502, (Object)integrity$fulltext_storage_seq.const__5);
        v5 = map__22502;
        map__22502 = null;
        olookup = RT.get((Object)v5, (Object)integrity$fulltext_storage_seq.const__6);
        v6 = integrity$fulltext_storage_seq.__thunk__0__;
        v7 = (IFn)integrity$fulltext_storage_seq.const__8.getRawRoot();
        v8 = cluster;
        if (Util.classOf((Object)v8) == integrity$fulltext_storage_seq.__cached_class__0) ** GOTO lbl26
        if (!(v8 instanceof ClusteredStore)) {
            v8 = v8;
            integrity$fulltext_storage_seq.__cached_class__0 = Util.classOf((Object)v8);
lbl26:
            // 2 sources

            v9 = cluster;
            cluster = null;
            v10 = integrity$fulltext_storage_seq.const__9.getRawRoot().invoke(v8, ((IFn)integrity$fulltext_storage_seq.const__10.getRawRoot()).invoke(v9));
        } else {
            v11 = cluster;
            cluster = null;
            v10 = ((ClusteredStore)v8).get_ref(((IFn)integrity$fulltext_storage_seq.const__10.getRawRoot()).invoke(v11));
        }
        v12 = v7.invoke(v10);
        v13 = v6.get(v12);
        if (v6 == v13) {
            integrity$fulltext_storage_seq.__thunk__0__ = integrity$fulltext_storage_seq.__site__0__.fault(v12);
            v13 = integrity$fulltext_storage_seq.__thunk__0__.get(v12);
        }
        v14 = index_root_id = v13;
        index_root_id = null;
        v15 = index_root = RT.get((Object)olookup, (Object)v14);
        index_root = null;
        v16 = k;
        k = null;
        v17 = root_id = RT.get((Object)v15, (Object)v16);
        if (v17 != null && v17 != Boolean.FALSE) {
            root = RT.get((Object)olookup, (Object)root_id);
            branch_QMARK_ = new integrity$fulltext_storage_seq$branch_QMARK___22504();
            v18 = olookup;
            olookup = null;
            children = new integrity$fulltext_storage_seq$children__22509(v18);
            v19 = branch_QMARK_;
            branch_QMARK_ = null;
            v20 = children;
            children = null;
            v21 = new Object[6];
            v21[0] = integrity$fulltext_storage_seq.const__12;
            v21[1] = integrity$fulltext_storage_seq.const__13;
            v21[2] = integrity$fulltext_storage_seq.const__14;
            v22 = root_id;
            root_id = null;
            v21[3] = v22;
            v21[4] = integrity$fulltext_storage_seq.const__15;
            v23 = root;
            root = null;
            v21[5] = v23;
            v24 = ((IFn)integrity$fulltext_storage_seq.const__11.getRawRoot()).invoke((Object)v19, (Object)v20, (Object)RT.mapUniqueKeys((Object[])v21));
        } else {
            v24 = null;
        }
        return v24;
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return integrity$fulltext_storage_seq.invokeStatic(object3, object4);
    }

    static {
        const__0 = RT.var((String)"datomic.integrity", (String)"enhance-uri");
        const__1 = RT.var((String)"datomic.tools", (String)"connection-resources");
        const__2 = RT.var((String)"clojure.core", (String)"seq?");
        const__3 = RT.var((String)"clojure.core", (String)"seq");
        const__5 = RT.keyword(null, (String)"cluster");
        const__6 = RT.keyword(null, (String)"olookup");
        const__8 = RT.var((String)"clojure.core", (String)"deref");
        const__9 = RT.var((String)"datomic.cluster", (String)"get-ref");
        const__10 = RT.var((String)"datomic.index", (String)"index-ref-key-name");
        const__11 = RT.var((String)"clojure.core", (String)"tree-seq");
        const__12 = RT.keyword(null, (String)"type");
        const__13 = RT.keyword(null, (String)"root");
        const__14 = RT.keyword(null, (String)"uuid");
        const__15 = RT.keyword(null, (String)"seg");
        __site__0__ = new KeywordLookupSite(RT.keyword(null, (String)"key"));
        __thunk__0__ = __site__0__;
    }
}

