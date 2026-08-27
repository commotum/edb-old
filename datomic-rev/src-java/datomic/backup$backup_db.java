/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.ISeq
 *  clojure.lang.Keyword
 *  clojure.lang.PersistentHashMap
 *  clojure.lang.RT
 *  clojure.lang.Util
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.ISeq;
import clojure.lang.Keyword;
import clojure.lang.PersistentHashMap;
import clojure.lang.RT;
import clojure.lang.Util;
import clojure.lang.Var;
import datomic.backup$backup_db$fn__20298;
import datomic.cluster.Dbid;

public final class backup$backup_db
extends AFunction {
    private static Class __cached_class__0;
    public static final Var const__0;
    public static final Var const__1;
    public static final Var const__2;
    public static final Keyword const__3;
    public static final Var const__4;
    public static final Var const__5;
    public static final Var const__6;
    public static final Var const__7;
    public static final Var const__8;
    public static final Var const__9;
    public static final Var const__10;
    public static final Var const__11;
    public static final Var const__12;
    public static final Keyword const__14;
    public static final Keyword const__15;
    public static final Keyword const__16;
    public static final Keyword const__17;
    public static final Var const__18;
    public static final Keyword const__19;
    public static final Keyword const__20;
    public static final Keyword const__21;
    public static final Keyword const__22;
    public static final Var const__23;
    public static final Keyword const__24;
    public static final Keyword const__25;
    public static final Var const__26;

    /*
     * Unable to fully structure code
     */
    public static Object invokeStatic(Object from_uri, Object to_storage, Object progress, Object concurrency, Object incremental_QMARK_) {
        v0 = cluster_conf = ((IFn)backup$backup_db.const__0.getRawRoot()).invoke(from_uri);
        cluster_conf = null;
        v1 = or__5238__auto__20310 = ((IFn)backup$backup_db.const__1.getRawRoot()).invoke(v0);
        if (v1 != null && v1 != Boolean.FALSE) {
            v2 = or__5238__auto__20310;
            or__5238__auto__20310 = null;
        } else {
            v3 = from_uri;
            from_uri = null;
            v2 = ((IFn)backup$backup_db.const__2.getRawRoot()).invoke((Object)backup$backup_db.const__3, ((IFn)backup$backup_db.const__4.getRawRoot()).invoke((Object)"Database does not exist: ", v3));
        }
        v4 = resolved_cluster_conf = v2;
        resolved_cluster_conf = null;
        from_cluster = ((IFn)backup$backup_db.const__5.getRawRoot()).invoke(v4, concurrency);
        v5 = to_storage;
        to_storage = null;
        to_storage = ((IFn)backup$backup_db.const__6.getRawRoot()).invoke(v5);
        v6 = (IFn)backup$backup_db.const__7.getRawRoot();
        v7 = from_cluster;
        if (Util.classOf((Object)v7) == backup$backup_db.__cached_class__0) ** GOTO lbl23
        if (!(v7 instanceof Dbid)) {
            v7 = v7;
            backup$backup_db.__cached_class__0 = Util.classOf((Object)v7);
lbl23:
            // 2 sources

            v8 = backup$backup_db.const__8.getRawRoot().invoke(v7);
        } else {
            v8 = ((Dbid)v7).dbId();
        }
        v6.invoke(to_storage, v8);
        olookup = ((IFn)backup$backup_db.const__9.getRawRoot()).invoke(from_cluster);
        map__20297 = job = ((IFn)backup$backup_db.const__10.getRawRoot()).invoke(from_cluster, olookup);
        v9 = ((IFn)backup$backup_db.const__11.getRawRoot()).invoke(map__20297);
        if (v9 != null && v9 != Boolean.FALSE) {
            v10 = map__20297;
            map__20297 = null;
            v11 = PersistentHashMap.create((ISeq)((ISeq)((IFn)backup$backup_db.const__12.getRawRoot()).invoke(v10)));
        } else {
            v11 = map__20297;
            map__20297 = null;
        }
        map__20297 = v11;
        index_top_node = RT.get((Object)map__20297, (Object)backup$backup_db.const__14);
        log_root_node = RT.get((Object)map__20297, (Object)backup$backup_db.const__15);
        RT.get((Object)map__20297, (Object)backup$backup_db.const__16);
        v12 = map__20297;
        map__20297 = null;
        t = RT.get((Object)v12, (Object)backup$backup_db.const__17);
        v13 = from_cluster;
        from_cluster = null;
        v14 = progress;
        progress = null;
        v15 = olookup;
        olookup = null;
        v16 = concurrency;
        concurrency = null;
        v17 = incremental_QMARK_;
        incremental_QMARK_ = null;
        backup = ((IFn)backup$backup_db.const__18.getRawRoot()).invoke((Object)backup$backup_db.const__19, v13, (Object)backup$backup_db.const__20, to_storage, (Object)backup$backup_db.const__21, v14, (Object)backup$backup_db.const__22, ((IFn)backup$backup_db.const__23.getRawRoot()).invoke(v15), (Object)backup$backup_db.const__24, v16, (Object)backup$backup_db.const__25, v17);
        v18 = t;
        t = null;
        v19 = to_storage;
        to_storage = null;
        v20 = log_root_node;
        log_root_node = null;
        v21 = job;
        job = null;
        v22 = backup;
        backup = null;
        v23 = index_top_node;
        index_top_node = null;
        return ((IFn)backup$backup_db.const__26.getRawRoot()).invoke((Object)new backup$backup_db$fn__20298(v18, v19, v20, v21, v22, v23));
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
        return backup$backup_db.invokeStatic(object6, object7, object8, object9, object10);
    }

    static {
        const__0 = RT.var((String)"datomic.uri", (String)"parse-db");
        const__1 = RT.var((String)"datomic.coordination", (String)"resolve-db-name");
        const__2 = RT.var((String)"datomic.error", (String)"raise");
        const__3 = RT.keyword((String)"catalog", (String)"db-does-not-exist");
        const__4 = RT.var((String)"clojure.core", (String)"str");
        const__5 = RT.var((String)"datomic.backup", (String)"create-cluster");
        const__6 = RT.var((String)"datomic.backup", (String)"maybe-segset-storage");
        const__7 = RT.var((String)"datomic.backup", (String)"ensure-claim");
        const__8 = RT.var((String)"datomic.cluster", (String)"dbId");
        const__9 = RT.var((String)"datomic.domain", (String)"system-cache-olookup");
        const__10 = RT.var((String)"datomic.backup", (String)"create-backup-job");
        const__11 = RT.var((String)"clojure.core", (String)"seq?");
        const__12 = RT.var((String)"clojure.core", (String)"seq");
        const__14 = RT.keyword(null, (String)"index-top-node");
        const__15 = RT.keyword(null, (String)"log-root-node");
        const__16 = RT.keyword(null, (String)"db-id");
        const__17 = RT.keyword(null, (String)"t");
        const__18 = RT.var((String)"datomic.backup", (String)"create-value-backup");
        const__19 = RT.keyword(null, (String)"from-cluster");
        const__20 = RT.keyword(null, (String)"to-storage");
        const__21 = RT.keyword(null, (String)"progress");
        const__22 = RT.keyword(null, (String)"ids->nodes");
        const__23 = RT.var((String)"datomic.backup", (String)"create-ids->nodes");
        const__24 = RT.keyword(null, (String)"concurrency");
        const__25 = RT.keyword(null, (String)"incremental?");
        const__26 = RT.var((String)"clojure.core", (String)"future-call");
    }
}

