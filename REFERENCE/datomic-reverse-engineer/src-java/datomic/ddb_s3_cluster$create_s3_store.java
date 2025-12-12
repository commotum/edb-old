/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.IPersistentMap
 *  clojure.lang.ISeq
 *  clojure.lang.Keyword
 *  clojure.lang.PersistentHashMap
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.IPersistentMap;
import clojure.lang.ISeq;
import clojure.lang.Keyword;
import clojure.lang.PersistentHashMap;
import clojure.lang.RT;
import clojure.lang.Var;
import datomic.ddb_s3_cluster$create_s3_store$fn__22786;
import datomic.ddb_s3_cluster$create_s3_store$retry_fn__22791;

public final class ddb_s3_cluster$create_s3_store
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"seq?");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"seq");
    public static final Keyword const__3 = RT.keyword(null, (String)"aws-region");
    public static final Keyword const__4 = RT.keyword(null, (String)"s3-vals-bucket");
    public static final Keyword const__5 = RT.keyword(null, (String)"s3-vals-prefix");
    public static final Var const__6 = RT.var((String)"datomic.core2.aws.s3.sdkv1", (String)"s3-service");
    public static final Keyword const__7 = RT.keyword(null, (String)"region");
    public static final Keyword const__8 = RT.keyword(null, (String)"client-conf");
    public static final Var const__9 = RT.var((String)"datomic.aws", (String)"client-config");
    public static final Var const__10 = RT.var((String)"datomic.config", (String)"s3-client-args");
    public static final Var const__11 = RT.var((String)"datomic.config", (String)"property");
    public static final Keyword const__12 = RT.keyword(null, (String)"base");
    public static final Object const__13 = 2L;
    public static final Keyword const__14 = RT.keyword(null, (String)"backoff");
    public static final Keyword const__15 = RT.keyword(null, (String)"retriable?");
    public static final Var const__16 = RT.var((String)"datomic.core2.val-store.s3.sdkv1", (String)"create");
    public static final Keyword const__17 = RT.keyword(null, (String)"bucket");
    public static final Keyword const__18 = RT.keyword(null, (String)"prefix");
    public static final Var const__19 = RT.var((String)"clojure.core", (String)"str");
    public static final Var const__20 = RT.var((String)"datomic.ddb-s3-cluster", (String)"storage-path");
    public static final Keyword const__21 = RT.keyword(null, (String)"read-pool");
    public static final Var const__22 = RT.var((String)"clojure.core", (String)"deref");
    public static final Var const__23 = RT.var((String)"datomic.ddb-s3-cluster", (String)"s3-read-pool");
    public static final Keyword const__24 = RT.keyword(null, (String)"write-pool");
    public static final Var const__25 = RT.var((String)"datomic.ddb-s3-cluster", (String)"s3-write-pool");
    public static final Keyword const__26 = RT.keyword(null, (String)"client");
    public static final Keyword const__27 = RT.keyword(null, (String)"retry-fn");

    public static Object invokeStatic(Object p__22783) {
        IPersistentMap retry_opts;
        Object map__22784;
        Object object;
        Object object2 = p__22783;
        p__22783 = null;
        Object map__227842 = object2;
        Object object3 = ((IFn)const__0.getRawRoot()).invoke(map__227842);
        if (object3 != null && object3 != Boolean.FALSE) {
            Object object4 = map__227842;
            map__227842 = null;
            object = PersistentHashMap.create((ISeq)((ISeq)((IFn)const__1.getRawRoot()).invoke(object4)));
        } else {
            object = map__227842;
            map__227842 = null;
        }
        Object cluster_conf = map__22784 = object;
        Object aws_region = RT.get((Object)map__22784, (Object)const__3);
        Object s3_vals_bucket = RT.get((Object)map__22784, (Object)const__4);
        Object object5 = map__22784;
        map__22784 = null;
        Object s3_vals_prefix = RT.get((Object)object5, (Object)const__5);
        Object[] objectArray = new Object[4];
        objectArray[0] = const__7;
        Object object6 = aws_region;
        aws_region = null;
        objectArray[1] = object6;
        objectArray[2] = const__8;
        objectArray[3] = ((IFn)const__9.getRawRoot()).invoke(((IFn)const__10.getRawRoot()).invoke());
        Object s3_client = ((IFn)const__6.getRawRoot()).invoke((Object)RT.mapUniqueKeys((Object[])objectArray));
        Object max_retries = ((IFn)const__11.getRawRoot()).invoke((Object)"datomic.s3MaxRetries");
        Object[] objectArray2 = new Object[6];
        objectArray2[0] = const__12;
        objectArray2[1] = const__13;
        objectArray2[2] = const__14;
        objectArray2[3] = ((IFn)const__11.getRawRoot()).invoke((Object)"datomic.s3RetryBaseDelay");
        objectArray2[4] = const__15;
        Object object7 = max_retries;
        max_retries = null;
        objectArray2[5] = new ddb_s3_cluster$create_s3_store$fn__22786(object7);
        IPersistentMap iPersistentMap = retry_opts = RT.mapUniqueKeys((Object[])objectArray2);
        retry_opts = null;
        ddb_s3_cluster$create_s3_store$retry_fn__22791 retry_fn2 = new ddb_s3_cluster$create_s3_store$retry_fn__22791(iPersistentMap);
        Object[] objectArray3 = new Object[12];
        objectArray3[0] = const__17;
        Object object8 = s3_vals_bucket;
        s3_vals_bucket = null;
        objectArray3[1] = object8;
        objectArray3[2] = const__18;
        Object object9 = s3_vals_prefix;
        s3_vals_prefix = null;
        Object object10 = cluster_conf;
        cluster_conf = null;
        objectArray3[3] = ((IFn)const__19.getRawRoot()).invoke(object9, ((IFn)const__20.getRawRoot()).invoke(object10));
        objectArray3[4] = const__21;
        objectArray3[5] = ((IFn)const__22.getRawRoot()).invoke(const__23.getRawRoot());
        objectArray3[6] = const__24;
        objectArray3[7] = ((IFn)const__22.getRawRoot()).invoke(const__25.getRawRoot());
        objectArray3[8] = const__26;
        Object object11 = s3_client;
        s3_client = null;
        objectArray3[9] = object11;
        objectArray3[10] = const__27;
        ddb_s3_cluster$create_s3_store$retry_fn__22791 ddb_s3_cluster$create_s3_store$retry_fn__22791 = retry_fn2;
        retry_fn2 = null;
        objectArray3[11] = ddb_s3_cluster$create_s3_store$retry_fn__22791;
        return ((IFn)const__16.getRawRoot()).invoke((Object)RT.mapUniqueKeys((Object[])objectArray3));
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return ddb_s3_cluster$create_s3_store.invokeStatic(object2);
    }
}

