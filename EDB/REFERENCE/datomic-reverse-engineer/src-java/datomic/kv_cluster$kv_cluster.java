/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.ISeq
 *  clojure.lang.Keyword
 *  clojure.lang.Numbers
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
import clojure.lang.Numbers;
import clojure.lang.PersistentArrayMap;
import clojure.lang.PersistentHashMap;
import clojure.lang.RT;
import clojure.lang.Var;
import datomic.kv_cluster.KVCluster;
import java.util.concurrent.Semaphore;

public final class kv_cluster$kv_cluster
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"seq?");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"seq");
    public static final Keyword const__3 = RT.keyword(null, (String)"tenant");
    public static final Keyword const__4 = RT.keyword(null, (String)"db-id");
    public static final Keyword const__5 = RT.keyword(null, (String)"write-concurrency");
    public static final Keyword const__6 = RT.keyword(null, (String)"read-concurrency");
    public static final Keyword const__7 = RT.keyword(null, (String)"shared-pool?");
    public static final Keyword const__8 = RT.keyword(null, (String)"protocol");
    public static final Keyword const__9 = RT.keyword(null, (String)"kvc");
    public static final Keyword const__10 = RT.keyword(null, (String)"pod-garbage-handler");
    public static final Var const__11 = RT.var((String)"datomic.kv-cluster", (String)"mark-pod-garbage");
    public static final Keyword const__12 = RT.keyword(null, (String)"retrying-delete");
    public static final Var const__13 = RT.var((String)"datomic.config", (String)"property");
    public static final Var const__14 = RT.var((String)"clojure.core", (String)"partial");
    public static final Var const__15 = RT.var((String)"datomic.kv-cluster", (String)"retry-fn");
    public static final Keyword const__16 = RT.keyword(null, (String)"StorageDeleteBackoffMsec");
    public static final Var const__17 = RT.var((String)"clojure.core", (String)"atom");
    public static final Keyword const__18 = RT.keyword(null, (String)"exponential");
    public static final Var const__19 = RT.var((String)"clojure.core", (String)"keyword");
    public static final Var const__20 = RT.var((String)"clojure.core", (String)"str");
    public static final Var const__21 = RT.var((String)"clojure.core", (String)"name");
    public static final Object const__22 = 0L;
    public static final Var const__23 = RT.var((String)"datomic.kv-cluster", (String)"shared-pool");
    public static final Var const__24 = RT.var((String)"datomic.common", (String)"thread-pool");
    public static final Keyword const__25 = RT.keyword(null, (String)"nthreads");
    public static final Keyword const__26 = RT.keyword(null, (String)"name");
    public static final Keyword const__27 = RT.keyword(null, (String)"db");
    public static final Keyword const__30 = RT.keyword(null, (String)"StoragePutBackoffMsec");
    public static final Keyword const__31 = RT.keyword(null, (String)"StorageGetBackoffMsec");

    public static Object invokeStatic(Object kvs, Object p__11061) {
        Object object;
        Object or__5238__auto__11066;
        Object object2;
        Object or__5238__auto__11065;
        Object object3;
        Object or__5238__auto__11064;
        Object object4;
        Object object5 = p__11061;
        p__11061 = null;
        Object map__11062 = object5;
        Object object6 = ((IFn)const__0.getRawRoot()).invoke(map__11062);
        if (object6 != null && object6 != Boolean.FALSE) {
            Object object7 = map__11062;
            map__11062 = null;
            object4 = PersistentHashMap.create((ISeq)((ISeq)((IFn)const__1.getRawRoot()).invoke(object7)));
        } else {
            object4 = map__11062;
            map__11062 = null;
        }
        Object map__110622 = object4;
        Object tenant = RT.get((Object)map__110622, (Object)const__3);
        Object db_id = RT.get((Object)map__110622, (Object)const__4);
        Object write_concurrency = RT.get((Object)map__110622, (Object)const__5);
        Object read_concurrency = RT.get((Object)map__110622, (Object)const__6);
        Object shared_pool_QMARK_ = RT.get((Object)map__110622, (Object)const__7, (Object)Boolean.TRUE);
        Object protocol = RT.get((Object)map__110622, (Object)const__8, (Object)const__9);
        Object pod_garbage_handler = RT.get((Object)map__110622, (Object)const__10, (Object)const__11.getRawRoot());
        Object object8 = map__110622;
        map__110622 = null;
        Object retrying_delete = RT.get((Object)object8, (Object)const__12);
        Object object9 = write_concurrency;
        write_concurrency = null;
        Object object10 = or__5238__auto__11064 = object9;
        if (object10 != null && object10 != Boolean.FALSE) {
            object3 = or__5238__auto__11064;
            or__5238__auto__11064 = null;
        } else {
            object3 = ((IFn)const__13.getRawRoot()).invoke((Object)"datomic.writeConcurrency");
        }
        Object write_concurrency2 = object3;
        Object object11 = read_concurrency;
        read_concurrency = null;
        Object object12 = or__5238__auto__11065 = object11;
        if (object12 != null && object12 != Boolean.FALSE) {
            object2 = or__5238__auto__11065;
            or__5238__auto__11065 = null;
        } else {
            object2 = ((IFn)const__13.getRawRoot()).invoke((Object)"datomic.readConcurrency");
        }
        Object read_concurrency2 = object2;
        Object object13 = retrying_delete;
        retrying_delete = null;
        Object object14 = or__5238__auto__11066 = object13;
        if (object14 != null && object14 != Boolean.FALSE) {
            object = or__5238__auto__11066;
            or__5238__auto__11066 = null;
        } else {
            object = ((IFn)const__14.getRawRoot()).invoke(const__15.getRawRoot(), (Object)new Semaphore(RT.intCast((Object)((Number)((IFn)const__13.getRawRoot()).invoke((Object)"datomic.deleteConcurrency"))), Boolean.TRUE), (Object)const__16, (Object)Boolean.FALSE, ((IFn)const__17.getRawRoot()).invoke((Object)PersistentArrayMap.EMPTY), (Object)const__18);
        }
        Object retrying_delete2 = object;
        Object protocol_nsec_k = ((IFn)const__19.getRawRoot()).invoke(((IFn)const__20.getRawRoot()).invoke(((IFn)const__21.getRawRoot()).invoke(protocol), (Object)"-ns"));
        ((IFn)const__17.getRawRoot()).invoke(const__22);
        Object object15 = shared_pool_QMARK_;
        shared_pool_QMARK_ = null;
        Object exec = object15 != null && object15 != Boolean.FALSE ? ((IFn)const__23.getRawRoot()).invoke() : ((IFn)const__24.getRawRoot()).invoke((Object)RT.mapUniqueKeys((Object[])new Object[]{const__25, write_concurrency2, const__26, "storage"}));
        Object object16 = kvs;
        kvs = null;
        Object[] objectArray = new Object[4];
        objectArray[0] = const__3;
        Object object17 = tenant;
        tenant = null;
        objectArray[1] = object17;
        objectArray[2] = const__27;
        Object object18 = db_id;
        db_id = null;
        objectArray[3] = object18;
        Object object19 = exec;
        exec = null;
        Object object20 = write_concurrency2;
        write_concurrency2 = null;
        Object object21 = read_concurrency2;
        read_concurrency2 = null;
        Object object22 = retrying_delete2;
        retrying_delete2 = null;
        Object object23 = protocol;
        protocol = null;
        Object object24 = protocol_nsec_k;
        protocol_nsec_k = null;
        Object object25 = pod_garbage_handler;
        pod_garbage_handler = null;
        return new KVCluster(object16, RT.mapUniqueKeys((Object[])objectArray), object19, ((IFn)const__14.getRawRoot()).invoke(const__15.getRawRoot(), (Object)new Semaphore(RT.intCast((Object)Numbers.add((Object)object20, (long)2L)), Boolean.TRUE), (Object)const__30, (Object)Boolean.FALSE, ((IFn)const__17.getRawRoot()).invoke((Object)PersistentArrayMap.EMPTY)), ((IFn)const__14.getRawRoot()).invoke(const__15.getRawRoot(), (Object)new Semaphore(RT.intCast((Object)((Number)object21)), Boolean.TRUE), (Object)const__31, (Object)Boolean.FALSE, ((IFn)const__17.getRawRoot()).invoke((Object)PersistentArrayMap.EMPTY)), object22, object23, object24, object25);
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return kv_cluster$kv_cluster.invokeStatic(object3, object4);
    }
}

