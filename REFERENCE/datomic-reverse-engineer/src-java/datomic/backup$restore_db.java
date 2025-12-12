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
import clojure.lang.Var;
import datomic.backup$restore_db$fn__20255;

public final class backup$restore_db
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"seq?");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"seq");
    public static final Keyword const__3 = RT.keyword(null, (String)"from-storage");
    public static final Keyword const__4 = RT.keyword(null, (String)"t");
    public static final Keyword const__5 = RT.keyword(null, (String)"to-uri");
    public static final Var const__6 = RT.var((String)"datomic.backup", (String)"create-restore-job");
    public static final Keyword const__8 = RT.keyword(null, (String)"index-top-node");
    public static final Keyword const__9 = RT.keyword(null, (String)"log-root-node");
    public static final Keyword const__10 = RT.keyword(null, (String)"db-id");
    public static final Keyword const__11 = RT.keyword(null, (String)"lookup");
    public static final Var const__12 = RT.var((String)"datomic.backup", (String)"create-restore-target");
    public static final Var const__13 = RT.var((String)"datomic.backup", (String)"create-value-restore");
    public static final Keyword const__14 = RT.keyword(null, (String)"to-cluster");
    public static final Keyword const__15 = RT.keyword(null, (String)"backup-version");
    public static final Keyword const__16 = RT.keyword(null, (String)"progress");
    public static final Keyword const__17 = RT.keyword(null, (String)"ids->nodes");
    public static final Var const__18 = RT.var((String)"datomic.backup", (String)"create-ids->nodes");
    public static final Keyword const__19 = RT.keyword(null, (String)"incremental?");
    public static final Keyword const__20 = RT.keyword(null, (String)"concurrency");
    public static final Var const__21 = RT.var((String)"clojure.core", (String)"future-call");
    static final KeywordLookupSite __site__0__ = new KeywordLookupSite(RT.keyword((String)"backup", (String)"version"));
    static ILookupThunk __thunk__0__ = __site__0__;

    public static Object invokeStatic(Object p__20250, Object p__20251, Object progress, Object concurrency, Object incremental_QMARK_) {
        Object object;
        Object map__20253;
        Object object2;
        Object object3;
        Object object4 = p__20250;
        p__20250 = null;
        Object map__20252 = object4;
        Object object5 = ((IFn)const__0.getRawRoot()).invoke(map__20252);
        if (object5 != null && object5 != Boolean.FALSE) {
            Object object6 = map__20252;
            map__20252 = null;
            object3 = PersistentHashMap.create((ISeq)((ISeq)((IFn)const__1.getRawRoot()).invoke(object6)));
        } else {
            object3 = map__20252;
            map__20252 = null;
        }
        Object map__202522 = object3;
        Object from_storage = RT.get((Object)map__202522, (Object)const__3);
        Object object7 = map__202522;
        map__202522 = null;
        Object t = RT.get((Object)object7, (Object)const__4);
        Object object8 = p__20251;
        p__20251 = null;
        Object map__202532 = object8;
        Object object9 = ((IFn)const__0.getRawRoot()).invoke(map__202532);
        if (object9 != null && object9 != Boolean.FALSE) {
            Object object10 = map__202532;
            map__202532 = null;
            object2 = PersistentHashMap.create((ISeq)((ISeq)((IFn)const__1.getRawRoot()).invoke(object10)));
        } else {
            object2 = map__202532;
            map__202532 = null;
        }
        Object object11 = map__20253 = object2;
        map__20253 = null;
        Object to_uri = RT.get((Object)object11, (Object)const__5);
        Object job = ((IFn)const__6.getRawRoot()).invoke(from_storage, t);
        ILookupThunk iLookupThunk = __thunk__0__;
        Object object12 = job;
        Object object13 = iLookupThunk.get(object12);
        if (iLookupThunk == object13) {
            __thunk__0__ = __site__0__.fault(object12);
            object13 = __thunk__0__.get(object12);
        }
        Object backup_version = object13;
        Object map__20254 = job;
        Object object14 = ((IFn)const__0.getRawRoot()).invoke(map__20254);
        if (object14 != null && object14 != Boolean.FALSE) {
            Object object15 = map__20254;
            map__20254 = null;
            object = PersistentHashMap.create((ISeq)((ISeq)((IFn)const__1.getRawRoot()).invoke(object15)));
        } else {
            object = map__20254;
            map__20254 = null;
        }
        Object map__202542 = object;
        Object index_top_node = RT.get((Object)map__202542, (Object)const__8);
        Object log_root_node = RT.get((Object)map__202542, (Object)const__9);
        Object db_id = RT.get((Object)map__202542, (Object)const__10);
        Object object16 = map__202542;
        map__202542 = null;
        Object lookup = RT.get((Object)object16, (Object)const__11);
        Object object17 = to_uri;
        to_uri = null;
        Object to_cluster = ((IFn)const__12.getRawRoot()).invoke(object17, (Object)RT.mapUniqueKeys((Object[])new Object[]{const__10, db_id}), concurrency);
        Object object18 = from_storage;
        from_storage = null;
        Object object19 = backup_version;
        backup_version = null;
        Object object20 = progress;
        progress = null;
        Object object21 = lookup;
        lookup = null;
        Object object22 = incremental_QMARK_;
        incremental_QMARK_ = null;
        Object object23 = concurrency;
        concurrency = null;
        Object restore2 = ((IFn)const__13.getRawRoot()).invoke((Object)const__3, object18, (Object)const__14, to_cluster, (Object)const__15, object19, (Object)const__16, object20, (Object)const__17, ((IFn)const__18.getRawRoot()).invoke(object21), (Object)const__19, object22, (Object)const__20, object23);
        Object object24 = t;
        t = null;
        Object object25 = db_id;
        db_id = null;
        Object object26 = log_root_node;
        log_root_node = null;
        Object object27 = job;
        job = null;
        Object object28 = index_top_node;
        index_top_node = null;
        Object object29 = to_cluster;
        to_cluster = null;
        Object object30 = restore2;
        restore2 = null;
        return ((IFn)const__21.getRawRoot()).invoke((Object)new backup$restore_db$fn__20255(object24, object25, object26, object27, object28, object29, object30));
    }

    public Object invoke(Object object, Object object2, Object object3, Object object4, Object object5) {
        Object object6 = object;
        object = null;
        Object object7 = object2;
        object2 = null;
        Object object8 = object3;
        object3 = null;
        Object object9 = object4;
        object4 = null;
        Object object10 = object5;
        object5 = null;
        return backup$restore_db.invokeStatic(object6, object7, object8, object9, object10);
    }
}

