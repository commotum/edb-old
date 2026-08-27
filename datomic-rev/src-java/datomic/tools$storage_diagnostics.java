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
import datomic.memory_size.MemorySize;
import java.nio.Buffer;

public final class tools$storage_diagnostics
extends AFunction {
    private static Class __cached_class__0;
    private static Class __cached_class__1;
    private static Class __cached_class__2;
    public static final Var const__0;
    public static final Var const__1;
    public static final Var const__2;
    public static final Keyword const__4;
    public static final Keyword const__5;
    public static final Var const__6;
    public static final Var const__10;
    public static final Var const__11;
    public static final Var const__12;
    public static final Var const__14;
    public static final Var const__15;
    public static final Var const__17;
    public static final Keyword const__18;
    public static final Keyword const__19;
    public static final Keyword const__20;
    public static final Keyword const__21;
    public static final Keyword const__22;
    public static final Keyword const__23;
    public static final Keyword const__24;
    public static final Var const__25;
    public static final Keyword const__26;
    public static final Var const__27;
    static final KeywordLookupSite __site__0__;
    static ILookupThunk __thunk__0__;
    static final KeywordLookupSite __site__1__;
    static ILookupThunk __thunk__1__;

    /*
     * Unable to fully structure code
     */
    public static Object invokeStatic(Object uri) {
        v0 = uri;
        uri = null;
        map__21795 = ((IFn)tools$storage_diagnostics.const__0.getRawRoot()).invoke(v0);
        v1 = ((IFn)tools$storage_diagnostics.const__1.getRawRoot()).invoke(map__21795);
        if (v1 != null && v1 != Boolean.FALSE) {
            v2 = map__21795;
            map__21795 = null;
            v3 = PersistentHashMap.create((ISeq)((ISeq)((IFn)tools$storage_diagnostics.const__2.getRawRoot()).invoke(v2)));
        } else {
            v3 = map__21795;
            map__21795 = null;
        }
        cr = map__21795 = v3;
        cluster = RT.get((Object)map__21795, (Object)tools$storage_diagnostics.const__4);
        v4 = map__21795;
        map__21795 = null;
        olookup = RT.get((Object)v4, (Object)tools$storage_diagnostics.const__5);
        vec__21796 = ((IFn)tools$storage_diagnostics.const__6.getRawRoot()).invoke(cluster);
        desc = RT.nth((Object)vec__21796, (int)RT.intCast((long)0L), null);
        v5 = vec__21796;
        vec__21796 = null;
        buf = RT.nth((Object)v5, (int)RT.intCast((long)1L), null);
        v6 = (IFn)tools$storage_diagnostics.const__10.getRawRoot();
        v7 = cluster;
        if (Util.classOf((Object)v7) == tools$storage_diagnostics.__cached_class__0) ** GOTO lbl28
        if (!(v7 instanceof ClusteredStore)) {
            v7 = v7;
            tools$storage_diagnostics.__cached_class__0 = Util.classOf((Object)v7);
lbl28:
            // 2 sources

            v8 = tools$storage_diagnostics.const__11.getRawRoot().invoke(v7, ((IFn)tools$storage_diagnostics.const__12.getRawRoot()).invoke(cluster));
        } else {
            v8 = ((ClusteredStore)v7).get_ref(((IFn)tools$storage_diagnostics.const__12.getRawRoot()).invoke(cluster));
        }
        index_root_key = v6.invoke(v8);
        v9 = tools$storage_diagnostics.__thunk__0__;
        v10 = index_root_key;
        v11 = v9.get(v10);
        if (v9 == v11) {
            tools$storage_diagnostics.__thunk__0__ = tools$storage_diagnostics.__site__0__.fault(v10);
            v11 = tools$storage_diagnostics.__thunk__0__.get(v10);
        }
        index_root = RT.get((Object)olookup, (Object)v11);
        log_root_key = ((IFn)tools$storage_diagnostics.const__14.getRawRoot()).invoke(cluster);
        v12 = olookup;
        olookup = null;
        log_root = RT.get((Object)v12, (Object)((IFn)tools$storage_diagnostics.const__15.getRawRoot()).invoke(log_root_key));
        v13 = tools$storage_diagnostics.__thunk__1__;
        v14 = (IFn)tools$storage_diagnostics.const__10.getRawRoot();
        v15 = cluster;
        cluster = null;
        v16 = v15;
        if (Util.classOf((Object)v15) == tools$storage_diagnostics.__cached_class__1) ** GOTO lbl53
        if (!(v16 instanceof ClusteredStore)) {
            v16 = v16;
            tools$storage_diagnostics.__cached_class__1 = Util.classOf((Object)v16);
lbl53:
            // 2 sources

            v17 = tools$storage_diagnostics.const__17.getRawRoot().invoke(v16, log_root_key);
        } else {
            v17 = ((ClusteredStore)v16).get_val(log_root_key);
        }
        v18 = v14.invoke(v17);
        v19 = v13.get(v18);
        if (v13 == v19) {
            tools$storage_diagnostics.__thunk__1__ = tools$storage_diagnostics.__site__1__.fault(v18);
            v19 = tools$storage_diagnostics.__thunk__1__.get(v18);
        }
        log_root_disk_bytes = ((Buffer)v19).remaining();
        v20 = new Object[16];
        v20[0] = tools$storage_diagnostics.const__18;
        v21 = desc;
        desc = null;
        v20[1] = v21;
        v20[2] = tools$storage_diagnostics.const__19;
        v22 = buf;
        buf = null;
        v20[3] = ((Buffer)v22).remaining();
        v20[4] = tools$storage_diagnostics.const__20;
        v23 = index_root;
        index_root = null;
        v20[5] = v23;
        v20[6] = tools$storage_diagnostics.const__21;
        v24 = index_root_key;
        index_root_key = null;
        v20[7] = v24;
        v20[8] = tools$storage_diagnostics.const__22;
        v25 = log_root_key;
        log_root_key = null;
        v20[9] = v25;
        v20[10] = tools$storage_diagnostics.const__23;
        v20[11] = log_root_disk_bytes;
        v20[12] = tools$storage_diagnostics.const__24;
        v26 = log_root;
        log_root = null;
        v27 = v26;
        if (Util.classOf((Object)v26) == tools$storage_diagnostics.__cached_class__2) ** GOTO lbl94
        if (!(v27 instanceof MemorySize)) {
            v27 = v27;
            tools$storage_diagnostics.__cached_class__2 = Util.classOf((Object)v27);
lbl94:
            // 2 sources

            v28 = tools$storage_diagnostics.const__25.getRawRoot().invoke(v27);
        } else {
            v28 = ((MemorySize)v27).memory_size();
        }
        v20[13] = v28;
        v20[14] = tools$storage_diagnostics.const__26;
        v29 = cr;
        cr = null;
        v20[15] = ((IFn)tools$storage_diagnostics.const__27.getRawRoot()).invoke(v29);
        return RT.mapUniqueKeys((Object[])v20);
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return tools$storage_diagnostics.invokeStatic(object2);
    }

    static {
        const__0 = RT.var((String)"datomic.tools", (String)"connection-resources");
        const__1 = RT.var((String)"clojure.core", (String)"seq?");
        const__2 = RT.var((String)"clojure.core", (String)"seq");
        const__4 = RT.keyword(null, (String)"cluster");
        const__5 = RT.keyword(null, (String)"olookup");
        const__6 = RT.var((String)"datomic.log", (String)"read-tail-descriptor");
        const__10 = RT.var((String)"clojure.core", (String)"deref");
        const__11 = RT.var((String)"datomic.cluster", (String)"get-ref");
        const__12 = RT.var((String)"datomic.index", (String)"index-ref-key-name");
        const__14 = RT.var((String)"datomic.log", (String)"root-id");
        const__15 = RT.var((String)"datomic.cluster", (String)"val-key->uuid");
        const__17 = RT.var((String)"datomic.cluster", (String)"get-val");
        const__18 = RT.keyword(null, (String)"log-tail");
        const__19 = RT.keyword(null, (String)"log-tail-size");
        const__20 = RT.keyword(null, (String)"index-root");
        const__21 = RT.keyword(null, (String)"index-root-key");
        const__22 = RT.keyword(null, (String)"log-root-key");
        const__23 = RT.keyword(null, (String)"log-root-disk-bytes");
        const__24 = RT.keyword(null, (String)"log-root-mem-bytes");
        const__25 = RT.var((String)"datomic.memory-size", (String)"memory-size");
        const__26 = RT.keyword(null, (String)"log-segments");
        const__27 = RT.var((String)"datomic.tools", (String)"count-log-segs");
        __site__0__ = new KeywordLookupSite(RT.keyword(null, (String)"key"));
        __thunk__0__ = __site__0__;
        __site__1__ = new KeywordLookupSite(RT.keyword(null, (String)"buf"));
        __thunk__1__ = __site__1__;
    }
}

