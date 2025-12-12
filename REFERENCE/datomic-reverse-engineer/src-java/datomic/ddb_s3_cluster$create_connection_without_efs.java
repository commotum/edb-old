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
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ddb_s3_cluster$create_connection_without_efs
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"seq?");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"seq");
    public static final Keyword const__3 = RT.keyword(null, (String)"aws-region");
    public static final Keyword const__4 = RT.keyword(null, (String)"aws-dynamodb-table");
    public static final Keyword const__5 = RT.keyword(null, (String)"system");
    public static final Var const__6 = RT.var((String)"datomic.slf4j", (String)"process");
    public static final Var const__7 = RT.var((String)"clojure.core", (String)"partial");
    public static final Var const__8 = RT.var((String)"datomic.kv-cluster", (String)"retry-fn");
    public static final Var const__9 = RT.var((String)"datomic.config", (String)"property");
    public static final Keyword const__10 = RT.keyword(null, (String)"StorageDeleteBackoffMsec");
    public static final Var const__11 = RT.var((String)"clojure.core", (String)"atom");
    public static final Keyword const__12 = RT.keyword(null, (String)"linear");
    public static final Var const__13 = RT.var((String)"clojure.core", (String)"merge");
    public static final Keyword const__14 = RT.keyword(null, (String)"retrying-delete");
    public static final Keyword const__15 = RT.keyword(null, (String)"pod-garbage-handler");
    public static final Var const__16 = RT.var((String)"datomic.garbage.pod", (String)"schedule-gc");
    public static final Var const__17 = RT.var((String)"datomic.ddb", (String)"client");
    public static final Var const__18 = RT.var((String)"datomic.config", (String)"ddb-client-args");
    public static final Keyword const__19 = RT.keyword(null, (String)"region");
    public static final Var const__20 = RT.var((String)"datomic.kv-dynamo", (String)"kv-dynamo");
    public static final Var const__21 = RT.var((String)"datomic.ddb-s3-cluster", (String)"get-valid-config!");
    public static final Var const__22 = RT.var((String)"datomic.ddb-s3-cluster", (String)"create-s3-store");
    public static final Var const__23 = RT.var((String)"datomic.kv-cluster", (String)"kv-cluster");
    public static final Var const__24 = RT.var((String)"datomic.val-cluster", (String)"val-cluster");
    public static final Var const__25 = RT.var((String)"datomic.combined-cluster", (String)"combined-cluster");

    public static Object invokeStatic(Object p__22798) {
        Object store_config;
        Object ddb_client;
        Object map__22799;
        Object object;
        Object object2 = p__22798;
        p__22798 = null;
        Object map__227992 = object2;
        Object object3 = ((IFn)const__0.getRawRoot()).invoke(map__227992);
        if (object3 != null && object3 != Boolean.FALSE) {
            Object object4 = map__227992;
            map__227992 = null;
            object = PersistentHashMap.create((ISeq)((ISeq)((IFn)const__1.getRawRoot()).invoke(object4)));
        } else {
            object = map__227992;
            map__227992 = null;
        }
        Object cluster_conf = map__22799 = object;
        Object aws_region = RT.get((Object)map__22799, (Object)const__3);
        Object aws_dynamodb_table = RT.get((Object)map__22799, (Object)const__4);
        Object object5 = map__22799;
        map__22799 = null;
        Object system = RT.get((Object)object5, (Object)const__5);
        Logger logger = LoggerFactory.getLogger((String)"datomic.ddb-s3-cluster");
        if (logger.isWarnEnabled()) {
            Logger logger2 = logger;
            logger = null;
            logger2.warn((String)((IFn)const__6.getRawRoot()).invoke((Object)"Running without EFS!"));
        }
        Object retrying_delete = ((IFn)const__7.getRawRoot()).invoke(const__8.getRawRoot(), (Object)new Semaphore(RT.intCast((Object)((Number)((IFn)const__9.getRawRoot()).invoke((Object)"datomic.deleteConcurrency"))), Boolean.TRUE), (Object)const__10, (Object)Boolean.FALSE, ((IFn)const__11.getRawRoot()).invoke((Object)PersistentArrayMap.EMPTY), (Object)const__12);
        Object object6 = cluster_conf;
        cluster_conf = null;
        Object[] objectArray = new Object[4];
        objectArray[0] = const__14;
        Object object7 = retrying_delete;
        retrying_delete = null;
        objectArray[1] = object7;
        objectArray[2] = const__15;
        objectArray[3] = const__16.getRawRoot();
        Object cluster_conf2 = ((IFn)const__13.getRawRoot()).invoke(object6, (Object)RT.mapUniqueKeys((Object[])objectArray));
        Object[] objectArray2 = new Object[2];
        objectArray2[0] = const__19;
        Object object8 = aws_region;
        aws_region = null;
        objectArray2[1] = object8;
        Object object9 = ddb_client = ((IFn)const__17.getRawRoot()).invoke(null, ((IFn)const__18.getRawRoot()).invoke((Object)RT.mapUniqueKeys((Object[])objectArray2)));
        ddb_client = null;
        Object object10 = aws_dynamodb_table;
        aws_dynamodb_table = null;
        Object object11 = system;
        system = null;
        Object kvs = ((IFn)const__20.getRawRoot()).invoke(object9, object10, object11);
        Object object12 = store_config = ((IFn)const__21.getRawRoot()).invoke(kvs, cluster_conf2);
        store_config = null;
        Object s3_store = ((IFn)const__22.getRawRoot()).invoke(((IFn)const__13.getRawRoot()).invoke(cluster_conf2, object12));
        Object object13 = kvs;
        kvs = null;
        Object object14 = cluster_conf2;
        cluster_conf2 = null;
        Object ddb_cluster2 = ((IFn)const__23.getRawRoot()).invoke(object13, object14);
        Object object15 = s3_store;
        s3_store = null;
        Object s3_cluster = ((IFn)const__24.getRawRoot()).invoke(object15);
        Object object16 = ddb_cluster2;
        ddb_cluster2 = null;
        Object object17 = s3_cluster;
        s3_cluster = null;
        return ((IFn)const__25.getRawRoot()).invoke(object16, object17);
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return ddb_s3_cluster$create_connection_without_efs.invokeStatic(object2);
    }
}

