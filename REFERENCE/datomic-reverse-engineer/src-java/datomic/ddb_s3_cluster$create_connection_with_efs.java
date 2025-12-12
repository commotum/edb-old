/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.ISeq
 *  clojure.lang.Keyword
 *  clojure.lang.PersistentArrayMap
 *  clojure.lang.PersistentHashMap
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.ISeq;
import clojure.lang.Keyword;
import clojure.lang.PersistentArrayMap;
import clojure.lang.PersistentHashMap;
import clojure.lang.RT;
import clojure.lang.Var;
import java.util.concurrent.Semaphore;

public final class ddb_s3_cluster$create_connection_with_efs
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"seq?");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"seq");
    public static final Keyword const__3 = RT.keyword(null, (String)"near-store-get-timeout-msec");
    public static final Object const__4 = 20L;
    public static final Keyword const__5 = RT.keyword(null, (String)"aws-region");
    public static final Keyword const__6 = RT.keyword(null, (String)"aws-dynamodb-table");
    public static final Keyword const__7 = RT.keyword(null, (String)"system");
    public static final Var const__8 = RT.var((String)"clojure.core", (String)"partial");
    public static final Var const__9 = RT.var((String)"datomic.kv-cluster", (String)"retry-fn");
    public static final Var const__10 = RT.var((String)"datomic.config", (String)"property");
    public static final Keyword const__11 = RT.keyword(null, (String)"StorageDeleteBackoffMsec");
    public static final Var const__12 = RT.var((String)"clojure.core", (String)"atom");
    public static final Keyword const__13 = RT.keyword(null, (String)"linear");
    public static final Var const__14 = RT.var((String)"clojure.core", (String)"merge");
    public static final Keyword const__15 = RT.keyword(null, (String)"retrying-delete");
    public static final Keyword const__16 = RT.keyword(null, (String)"pod-garbage-handler");
    public static final Var const__17 = RT.var((String)"datomic.garbage.pod", (String)"schedule-gc");
    public static final Var const__18 = RT.var((String)"datomic.ddb", (String)"client");
    public static final Var const__19 = RT.var((String)"datomic.config", (String)"ddb-client-args");
    public static final Keyword const__20 = RT.keyword(null, (String)"region");
    public static final Var const__21 = RT.var((String)"datomic.kv-dynamo", (String)"kv-dynamo");
    public static final Var const__22 = RT.var((String)"datomic.ddb-s3-cluster", (String)"get-valid-config!");
    public static final Keyword const__23 = RT.keyword(null, (String)"fs-vals-path");
    public static final Var const__24 = RT.var((String)"datomic.ddb-s3-cluster", (String)"create-s3-store");
    public static final Var const__25 = RT.var((String)"datomic.core2.val-store.fs", (String)"create");
    public static final Keyword const__26 = RT.keyword(null, (String)"delete-pool");
    public static final Var const__27 = RT.var((String)"clojure.core", (String)"deref");
    public static final Var const__28 = RT.var((String)"datomic.ddb-s3-cluster", (String)"efs-delete-pool");
    public static final Keyword const__29 = RT.keyword(null, (String)"get-pool");
    public static final Var const__30 = RT.var((String)"datomic.ddb-s3-cluster", (String)"efs-read-pool");
    public static final Keyword const__31 = RT.keyword(null, (String)"path");
    public static final Var const__32 = RT.var((String)"clojure.core", (String)"str");
    public static final Var const__33 = RT.var((String)"datomic.ddb-s3-cluster", (String)"storage-path");
    public static final Keyword const__34 = RT.keyword(null, (String)"put-pool");
    public static final Var const__35 = RT.var((String)"datomic.ddb-s3-cluster", (String)"efs-write-pool");
    public static final Var const__36 = RT.var((String)"datomic.core2.val-store.double-store", (String)"create");
    public static final Keyword const__37 = RT.keyword(null, (String)"near-store");
    public static final Keyword const__38 = RT.keyword(null, (String)"far-store");
    public static final Var const__39 = RT.var((String)"datomic.kv-cluster", (String)"kv-cluster");
    public static final Var const__40 = RT.var((String)"datomic.val-cluster", (String)"val-cluster");
    public static final Var const__41 = RT.var((String)"datomic.combined-cluster", (String)"combined-cluster");

    public static Object invokeStatic(Object p__22794) {
        Object map__22796;
        Object object;
        Object ddb_client;
        Object map__22795;
        Object object2;
        Object object3 = p__22794;
        p__22794 = null;
        Object map__227952 = object3;
        Object object4 = ((IFn)const__0.getRawRoot()).invoke(map__227952);
        if (object4 != null && object4 != Boolean.FALSE) {
            Object object5 = map__227952;
            map__227952 = null;
            object2 = PersistentHashMap.create((ISeq)((ISeq)((IFn)const__1.getRawRoot()).invoke(object5)));
        } else {
            object2 = map__227952;
            map__227952 = null;
        }
        Object cluster_conf = map__22795 = object2;
        Object near_store_get_timeout_msec = RT.get((Object)map__22795, (Object)const__3, (Object)const__4);
        Object aws_region = RT.get((Object)map__22795, (Object)const__5);
        Object aws_dynamodb_table = RT.get((Object)map__22795, (Object)const__6);
        Object object6 = map__22795;
        map__22795 = null;
        Object system = RT.get((Object)object6, (Object)const__7);
        Object retrying_delete = ((IFn)const__8.getRawRoot()).invoke(const__9.getRawRoot(), (Object)new Semaphore(RT.intCast((Object)((Number)((IFn)const__10.getRawRoot()).invoke((Object)"datomic.deleteConcurrency"))), Boolean.TRUE), (Object)const__11, (Object)Boolean.FALSE, ((IFn)const__12.getRawRoot()).invoke((Object)PersistentArrayMap.EMPTY), (Object)const__13);
        Object object7 = cluster_conf;
        cluster_conf = null;
        Object[] objectArray = new Object[4];
        objectArray[0] = const__15;
        Object object8 = retrying_delete;
        retrying_delete = null;
        objectArray[1] = object8;
        objectArray[2] = const__16;
        objectArray[3] = const__17.getRawRoot();
        Object cluster_conf2 = ((IFn)const__14.getRawRoot()).invoke(object7, (Object)RT.mapUniqueKeys((Object[])objectArray));
        Object[] objectArray2 = new Object[2];
        objectArray2[0] = const__20;
        Object object9 = aws_region;
        aws_region = null;
        objectArray2[1] = object9;
        Object object10 = ddb_client = ((IFn)const__18.getRawRoot()).invoke(null, ((IFn)const__19.getRawRoot()).invoke((Object)RT.mapUniqueKeys((Object[])objectArray2)));
        ddb_client = null;
        Object object11 = aws_dynamodb_table;
        aws_dynamodb_table = null;
        Object object12 = system;
        system = null;
        Object kvs = ((IFn)const__21.getRawRoot()).invoke(object10, object11, object12);
        Object map__227962 = ((IFn)const__22.getRawRoot()).invoke(kvs, cluster_conf2);
        Object object13 = ((IFn)const__0.getRawRoot()).invoke(map__227962);
        if (object13 != null && object13 != Boolean.FALSE) {
            Object object14 = map__227962;
            map__227962 = null;
            object = PersistentHashMap.create((ISeq)((ISeq)((IFn)const__1.getRawRoot()).invoke(object14)));
        } else {
            object = map__227962;
            map__227962 = null;
        }
        Object store_config = map__22796 = object;
        Object object15 = map__22796;
        map__22796 = null;
        Object fs_vals_path = RT.get((Object)object15, (Object)const__23);
        Object object16 = store_config;
        store_config = null;
        Object s3_store = ((IFn)const__24.getRawRoot()).invoke(((IFn)const__14.getRawRoot()).invoke(cluster_conf2, object16));
        Object[] objectArray3 = new Object[8];
        objectArray3[0] = const__26;
        objectArray3[1] = ((IFn)const__27.getRawRoot()).invoke(const__28.getRawRoot());
        objectArray3[2] = const__29;
        objectArray3[3] = ((IFn)const__27.getRawRoot()).invoke(const__30.getRawRoot());
        objectArray3[4] = const__31;
        Object object17 = fs_vals_path;
        fs_vals_path = null;
        objectArray3[5] = ((IFn)const__32.getRawRoot()).invoke(object17, ((IFn)const__33.getRawRoot()).invoke(cluster_conf2));
        objectArray3[6] = const__34;
        objectArray3[7] = ((IFn)const__27.getRawRoot()).invoke(const__35.getRawRoot());
        Object fs_store = ((IFn)const__25.getRawRoot()).invoke((Object)RT.mapUniqueKeys((Object[])objectArray3));
        Object[] objectArray4 = new Object[6];
        objectArray4[0] = const__37;
        Object object18 = fs_store;
        fs_store = null;
        objectArray4[1] = object18;
        objectArray4[2] = const__38;
        Object object19 = s3_store;
        s3_store = null;
        objectArray4[3] = object19;
        objectArray4[4] = const__3;
        Object object20 = near_store_get_timeout_msec;
        near_store_get_timeout_msec = null;
        objectArray4[5] = object20;
        Object double_store2 = ((IFn)const__36.getRawRoot()).invoke((Object)RT.mapUniqueKeys((Object[])objectArray4));
        Object object21 = kvs;
        kvs = null;
        Object object22 = cluster_conf2;
        cluster_conf2 = null;
        Object ddb_cluster2 = ((IFn)const__39.getRawRoot()).invoke(object21, object22);
        Object object23 = double_store2;
        double_store2 = null;
        Object s3_PLUS_efs_cluster = ((IFn)const__40.getRawRoot()).invoke(object23);
        Object object24 = ddb_cluster2;
        ddb_cluster2 = null;
        Object object25 = s3_PLUS_efs_cluster;
        s3_PLUS_efs_cluster = null;
        return ((IFn)const__41.getRawRoot()).invoke(object24, object25);
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return ddb_s3_cluster$create_connection_with_efs.invokeStatic(object2);
    }
}

