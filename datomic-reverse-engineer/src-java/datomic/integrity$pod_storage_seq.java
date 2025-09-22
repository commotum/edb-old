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
import datomic.integrity$pod_storage_seq$fn__22551;
import datomic.integrity$pod_storage_seq$mkv__22545;

public final class integrity$pod_storage_seq
extends AFunction {
    private static Class __cached_class__0;
    public static final Var const__0;
    public static final Var const__1;
    public static final Var const__2;
    public static final Var const__3;
    public static final Keyword const__5;
    public static final Keyword const__6;
    public static final Var const__7;
    public static final Var const__8;
    public static final Var const__9;
    public static final Var const__10;
    public static final Var const__11;
    public static final Keyword const__12;
    public static final Keyword const__13;
    public static final Keyword const__14;
    public static final Var const__15;
    public static final Var const__16;
    public static final Var const__17;
    static final KeywordLookupSite __site__0__;
    static ILookupThunk __thunk__0__;

    /*
     * Unable to fully structure code
     */
    public static Object invokeStatic(Object uri) {
        v0 = uri;
        uri = null;
        v1 = uri = ((IFn)integrity$pod_storage_seq.const__0.getRawRoot()).invoke(v0);
        uri = null;
        map__22544 = ((IFn)integrity$pod_storage_seq.const__1.getRawRoot()).invoke(v1);
        v2 = ((IFn)integrity$pod_storage_seq.const__2.getRawRoot()).invoke(map__22544);
        if (v2 != null && v2 != Boolean.FALSE) {
            v3 = map__22544;
            map__22544 = null;
            v4 = PersistentHashMap.create((ISeq)((ISeq)((IFn)integrity$pod_storage_seq.const__3.getRawRoot()).invoke(v3)));
        } else {
            v4 = map__22544;
            map__22544 = null;
        }
        map__22544 = v4;
        cluster = RT.get((Object)map__22544, (Object)integrity$pod_storage_seq.const__5);
        v5 = map__22544;
        map__22544 = null;
        RT.get((Object)v5, (Object)integrity$pod_storage_seq.const__6);
        pod_key = ((IFn)integrity$pod_storage_seq.const__7.getRawRoot()).invoke(cluster);
        v6 = (IFn)integrity$pod_storage_seq.const__8.getRawRoot();
        v7 = cluster;
        if (Util.classOf((Object)v7) == integrity$pod_storage_seq.__cached_class__0) ** GOTO lbl27
        if (!(v7 instanceof ClusteredStore)) {
            v7 = v7;
            integrity$pod_storage_seq.__cached_class__0 = Util.classOf((Object)v7);
lbl27:
            // 2 sources

            v8 = integrity$pod_storage_seq.const__9.getRawRoot().invoke(v7, pod_key);
        } else {
            v8 = ((ClusteredStore)v7).get_pod_meta(pod_key);
        }
        pod_meta = v6.invoke(v8);
        v9 = cluster;
        cluster = null;
        mkv = new integrity$pod_storage_seq$mkv__22545(v9);
        v10 = (IFn)integrity$pod_storage_seq.const__10.getRawRoot();
        v11 = pod_meta;
        if (v11 != null && v11 != Boolean.FALSE) {
            v12 = ((IFn)integrity$pod_storage_seq.const__11.getRawRoot()).invoke(pod_meta, (Object)integrity$pod_storage_seq.const__12, (Object)integrity$pod_storage_seq.const__13);
        } else {
            v13 = new Object[2];
            v13[0] = integrity$pod_storage_seq.const__14;
            v14 = pod_key;
            pod_key = null;
            v13[1] = v14;
            v12 = RT.mapUniqueKeys((Object[])v13);
        }
        v15 = pod_meta;
        if (v15 != null && v15 != Boolean.FALSE) {
            v16 = (IFn)integrity$pod_storage_seq.const__15.getRawRoot();
            v17 = integrity$pod_storage_seq.const__16.getRawRoot();
            v18 = (IFn)integrity$pod_storage_seq.const__17.getRawRoot();
            v19 = new integrity$pod_storage_seq$fn__22551((Object)mkv);
            v20 = mkv;
            mkv = null;
            v21 = (IFn)v20;
            v22 = integrity$pod_storage_seq.__thunk__0__;
            v23 = pod_meta;
            pod_meta = null;
            v24 = v22.get(v23);
            if (v22 == v24) {
                integrity$pod_storage_seq.__thunk__0__ = integrity$pod_storage_seq.__site__0__.fault(v23);
                v24 = integrity$pod_storage_seq.__thunk__0__.get(v23);
            }
            v25 = v16.invoke(v17, v18.invoke((Object)v19, v21.invoke(v24)));
        } else {
            v25 = null;
        }
        return v10.invoke(v12, v25);
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return integrity$pod_storage_seq.invokeStatic(object2);
    }

    static {
        const__0 = RT.var((String)"datomic.integrity", (String)"enhance-uri");
        const__1 = RT.var((String)"datomic.tools", (String)"connection-resources");
        const__2 = RT.var((String)"clojure.core", (String)"seq?");
        const__3 = RT.var((String)"clojure.core", (String)"seq");
        const__5 = RT.keyword(null, (String)"cluster");
        const__6 = RT.keyword(null, (String)"olookup");
        const__7 = RT.var((String)"datomic.log", (String)"tail-pod-key");
        const__8 = RT.var((String)"clojure.core", (String)"deref");
        const__9 = RT.var((String)"datomic.cluster", (String)"get-pod-meta");
        const__10 = RT.var((String)"clojure.core", (String)"cons");
        const__11 = RT.var((String)"clojure.core", (String)"assoc");
        const__12 = RT.keyword(null, (String)"type");
        const__13 = RT.keyword(null, (String)"pod-key");
        const__14 = RT.keyword(null, (String)"id");
        const__15 = RT.var((String)"clojure.core", (String)"take-while");
        const__16 = RT.var((String)"clojure.core", (String)"identity");
        const__17 = RT.var((String)"clojure.core", (String)"iterate");
        __site__0__ = new KeywordLookupSite(RT.keyword(null, (String)"tail"));
        __thunk__0__ = __site__0__;
    }
}

