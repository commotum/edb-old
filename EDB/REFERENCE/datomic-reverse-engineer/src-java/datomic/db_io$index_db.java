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

public final class db_io$index_db
extends AFunction {
    private static Class __cached_class__0;
    public static final Var const__0;
    public static final Var const__1;
    public static final Keyword const__3;
    public static final Keyword const__4;
    public static final Keyword const__5;
    public static final Var const__8;
    public static final Var const__9;
    public static final Var const__10;
    public static final Var const__11;
    public static final Var const__12;
    static final KeywordLookupSite __site__0__;
    static ILookupThunk __thunk__0__;
    static final KeywordLookupSite __site__1__;
    static ILookupThunk __thunk__1__;

    /*
     * Unable to fully structure code
     */
    public static Object invokeStatic(Object p__17059) {
        v0 = p__17059;
        p__17059 = null;
        map__17060 = v0;
        v1 = ((IFn)db_io$index_db.const__0.getRawRoot()).invoke(map__17060);
        if (v1 != null && v1 != Boolean.FALSE) {
            v2 = map__17060;
            map__17060 = null;
            v3 = PersistentHashMap.create((ISeq)((ISeq)((IFn)db_io$index_db.const__1.getRawRoot()).invoke(v2)));
        } else {
            v3 = map__17060;
            map__17060 = null;
        }
        map__17060 = v3;
        cluster = RT.get((Object)map__17060, (Object)db_io$index_db.const__3);
        olookup = RT.get((Object)map__17060, (Object)db_io$index_db.const__4);
        v4 = map__17060;
        map__17060 = null;
        resolved_conf = RT.get((Object)v4, (Object)db_io$index_db.const__5);
        v5 = db_io$index_db.__thunk__0__;
        v6 = resolved_conf;
        resolved_conf = null;
        v7 = v5.get(v6);
        if (v5 == v7) {
            db_io$index_db.__thunk__0__ = db_io$index_db.__site__0__.fault(v6);
            v7 = db_io$index_db.__thunk__0__.get(v6);
        }
        db_id = v7;
        v8 = db_io$index_db.__thunk__1__;
        v9 = (IFn)db_io$index_db.const__8.getRawRoot();
        v10 = cluster;
        if (Util.classOf((Object)v10) == db_io$index_db.__cached_class__0) ** GOTO lbl34
        if (!(v10 instanceof ClusteredStore)) {
            v10 = v10;
            db_io$index_db.__cached_class__0 = Util.classOf((Object)v10);
lbl34:
            // 2 sources

            v11 = cluster;
            cluster = null;
            v12 = db_io$index_db.const__9.getRawRoot().invoke(v10, ((IFn)db_io$index_db.const__10.getRawRoot()).invoke(v11));
        } else {
            v13 = cluster;
            cluster = null;
            v12 = ((ClusteredStore)v10).get_ref(((IFn)db_io$index_db.const__10.getRawRoot()).invoke(v13));
        }
        v14 = v9.invoke(v12);
        v15 = v8.get(v14);
        if (v8 == v15) {
            db_io$index_db.__thunk__1__ = db_io$index_db.__site__1__.fault(v14);
            v15 = db_io$index_db.__thunk__1__.get(v14);
        }
        idxroot = v15;
        v16 = db_id;
        db_id = null;
        v17 = olookup;
        olookup = null;
        v18 = idxroot;
        idxroot = null;
        return ((IFn)db_io$index_db.const__11.getRawRoot()).invoke(v16, ((IFn)db_io$index_db.const__12.getRawRoot()).invoke(v17, v18));
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return db_io$index_db.invokeStatic(object2);
    }

    static {
        const__0 = RT.var((String)"clojure.core", (String)"seq?");
        const__1 = RT.var((String)"clojure.core", (String)"seq");
        const__3 = RT.keyword(null, (String)"cluster");
        const__4 = RT.keyword(null, (String)"olookup");
        const__5 = RT.keyword(null, (String)"resolved-conf");
        const__8 = RT.var((String)"clojure.core", (String)"deref");
        const__9 = RT.var((String)"datomic.cluster", (String)"get-ref");
        const__10 = RT.var((String)"datomic.index", (String)"index-ref-key-name");
        const__11 = RT.var((String)"datomic.db", (String)"db");
        const__12 = RT.var((String)"datomic.index", (String)"load-index");
        __site__0__ = new KeywordLookupSite(RT.keyword(null, (String)"db-id"));
        __thunk__0__ = __site__0__;
        __site__1__ = new KeywordLookupSite(RT.keyword(null, (String)"key"));
        __thunk__1__ = __site__1__;
    }
}

