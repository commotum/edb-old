/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFn
 *  clojure.lang.IFn
 *  clojure.lang.ILookupThunk
 *  clojure.lang.IObj
 *  clojure.lang.IPersistentVector
 *  clojure.lang.IType
 *  clojure.lang.Keyword
 *  clojure.lang.KeywordLookupSite
 *  clojure.lang.Numbers
 *  clojure.lang.RT
 *  clojure.lang.Symbol
 *  clojure.lang.Util
 *  clojure.lang.Var
 */
package datomic.kv_cluster;

import clojure.lang.AFn;
import clojure.lang.IFn;
import clojure.lang.ILookupThunk;
import clojure.lang.IObj;
import clojure.lang.IPersistentVector;
import clojure.lang.IType;
import clojure.lang.Keyword;
import clojure.lang.KeywordLookupSite;
import clojure.lang.Numbers;
import clojure.lang.RT;
import clojure.lang.Symbol;
import clojure.lang.Util;
import clojure.lang.Var;
import datomic.cluster.ClusteredStore;
import datomic.cluster.Dbid;
import datomic.cluster.Get2;
import datomic.cluster.RefClusterStore;
import datomic.future.GetChannel;
import datomic.kv_cluster.KVCluster$doit__10886;
import datomic.kv_cluster.KVCluster$fn__10898;
import datomic.kv_cluster.KVCluster$fn__10913;
import datomic.kv_cluster.KVCluster$fn__10926;
import datomic.kv_cluster.KVCluster$fn__10939;
import datomic.kv_cluster.KVCluster$fn__10953;
import datomic.kv_cluster.KVCluster$fn__10968;
import datomic.kv_cluster.KVCluster$fn__10981;
import datomic.kv_cluster.KVCluster$fn__11004;
import java.io.Closeable;
import java.io.IOException;
import java.util.concurrent.ExecutorService;

public final class KVCluster
implements Dbid,
Get2,
ClusteredStore,
Closeable,
RefClusterStore,
IType {
    public final Object kvs;
    public final Object path_map;
    public final Object exec;
    public final Object retrying_write;
    public final Object retrying_read;
    public final Object retrying_delete;
    public final Object protocol;
    public final Object protocol_nsec_k;
    public final Object pod_garbage_handler;
    private static Class __cached_class__0;
    private static Class __cached_class__1;
    private static Class __cached_class__2;
    private static Class __cached_class__3;
    private static Class __cached_class__4;
    private static Class __cached_class__5;
    private static Class __cached_class__6;
    private static Class __cached_class__7;
    private static Class __cached_class__8;
    private static Class __cached_class__9;
    private static Class __cached_class__10;
    private static Class __cached_class__11;
    public static final Var const__1;
    public static final Var const__2;
    public static final Object const__5;
    public static final Keyword const__6;
    public static final Keyword const__7;
    public static final Var const__8;
    public static final Var const__9;
    public static final Var const__10;
    public static final AFn const__16;
    public static final Var const__17;
    public static final Var const__18;
    public static final AFn const__20;
    public static final Var const__21;
    public static final Var const__22;
    public static final AFn const__25;
    public static final Var const__26;
    public static final AFn const__29;
    public static final AFn const__32;
    public static final AFn const__34;
    public static final AFn const__36;
    public static final AFn const__38;
    public static final AFn const__40;
    public static final Var const__41;
    public static final Var const__42;
    public static final AFn const__45;
    public static final Var const__46;
    static final KeywordLookupSite __site__0__;
    static ILookupThunk __thunk__0__;

    public KVCluster(Object object, Object object2, Object object3, Object object4, Object object5, Object object6, Object object7, Object object8, Object object9) {
        this.kvs = object;
        this.path_map = object2;
        this.exec = object3;
        this.retrying_write = object4;
        this.retrying_read = object5;
        this.retrying_delete = object6;
        this.protocol = object7;
        this.protocol_nsec_k = object8;
        this.pod_garbage_handler = object9;
    }

    public static IPersistentVector getBasis() {
        return RT.vector((Object[])new Object[]{Symbol.intern(null, (String)"kvs"), Symbol.intern(null, (String)"path-map"), ((IObj)Symbol.intern(null, (String)"exec")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), Symbol.intern(null, (String)"ExecutorService")})), Symbol.intern(null, (String)"retrying-write"), Symbol.intern(null, (String)"retrying-read"), Symbol.intern(null, (String)"retrying-delete"), Symbol.intern(null, (String)"protocol"), Symbol.intern(null, (String)"protocol-nsec-k"), Symbol.intern(null, (String)"pod-garbage-handler")});
    }

    /*
     * Enabled aggressive block sorting
     */
    public Object get_val2(Object val_key, Object _) {
        Object object;
        KVCluster kVCluster = this_;
        if (Util.classOf((Object)kVCluster) != __cached_class__11) {
            if (kVCluster instanceof ClusteredStore) {
                Object object2 = val_key;
                val_key = null;
                object = ((ClusteredStore)kVCluster).get_val(object2);
                return object;
            }
            kVCluster = kVCluster;
            __cached_class__11 = Util.classOf((Object)kVCluster);
        }
        Object object3 = val_key;
        val_key = null;
        KVCluster this_ = null;
        object = const__46.getRawRoot().invoke((Object)kVCluster, object3);
        return object;
    }

    public Object _get_ref_store() {
        return this.kvs;
    }

    /*
     * Unable to fully structure code
     */
    public Object update_pod_STAR_(Object pod_key, Object rev, Object etag, Object buf, Object metamap) {
        v0 = buf;
        tailid = v0 != null && v0 != Boolean.FALSE ? ((IFn)KVCluster.const__41.getRawRoot()).invoke(((IFn)KVCluster.const__42.getRawRoot()).invoke()) : etag;
        v1 = pod_key;
        pod_key = null;
        v2 = metamap;
        metamap = null;
        v3 = etag;
        etag = null;
        v4 = rev;
        rev = null;
        v5 = tailid;
        tailid = null;
        v6 = buf;
        buf = null;
        f__10261__auto__11038 = ((IFn)KVCluster.const__8.getRawRoot()).invoke((Object)new KVCluster$fn__11004(this.pod_garbage_handler, this.kvs, this.retrying_read, this, this.retrying_write, v1, v2, v3, v4, v5, v6));
        v7 = f__10261__auto__11038;
        if (Util.classOf((Object)v7) == KVCluster.__cached_class__10) ** GOTO lbl21
        if (!(v7 instanceof GetChannel)) {
            v7 = v7;
            KVCluster.__cached_class__10 = Util.classOf((Object)v7);
lbl21:
            // 2 sources

            v8 = KVCluster.const__9.getRawRoot().invoke(v7);
        } else {
            v8 = ((GetChannel)v7).get_channel();
        }
        v9 = ch__10262__auto__11039 = v8;
        ch__10262__auto__11039 = null;
        ((IFn)KVCluster.const__10.getRawRoot()).invoke(v9, (Object)KVCluster.const__45, ((IFn)KVCluster.const__17.getRawRoot()).invoke(KVCluster.const__18.getRawRoot()));
        v10 = f__10261__auto__11038;
        f__10261__auto__11038 = null;
        return v10;
    }

    /*
     * Unable to fully structure code
     */
    public Object get_pod(Object pod_key) {
        v0 = pod_key;
        pod_key = null;
        f__10261__auto__11040 = ((IFn)KVCluster.const__8.getRawRoot()).invoke((Object)new KVCluster$fn__10981(this.kvs, this.retrying_read, v0));
        v1 = f__10261__auto__11040;
        if (Util.classOf((Object)v1) == KVCluster.__cached_class__9) ** GOTO lbl9
        if (!(v1 instanceof GetChannel)) {
            v1 = v1;
            KVCluster.__cached_class__9 = Util.classOf((Object)v1);
lbl9:
            // 2 sources

            v2 = KVCluster.const__9.getRawRoot().invoke(v1);
        } else {
            v2 = ((GetChannel)v1).get_channel();
        }
        v3 = ch__10262__auto__11041 = v2;
        ch__10262__auto__11041 = null;
        ((IFn)KVCluster.const__10.getRawRoot()).invoke(v3, (Object)KVCluster.const__40, ((IFn)KVCluster.const__17.getRawRoot()).invoke(KVCluster.const__18.getRawRoot()));
        var2_2 = null;
        return f__10261__auto__11040;
    }

    /*
     * Unable to fully structure code
     */
    public Object get_pod_meta(Object pod_key) {
        v0 = pod_key;
        pod_key = null;
        f__10261__auto__11042 = ((IFn)KVCluster.const__8.getRawRoot()).invoke((Object)new KVCluster$fn__10968(this.kvs, this.retrying_read, v0));
        v1 = f__10261__auto__11042;
        if (Util.classOf((Object)v1) == KVCluster.__cached_class__8) ** GOTO lbl9
        if (!(v1 instanceof GetChannel)) {
            v1 = v1;
            KVCluster.__cached_class__8 = Util.classOf((Object)v1);
lbl9:
            // 2 sources

            v2 = KVCluster.const__9.getRawRoot().invoke(v1);
        } else {
            v2 = ((GetChannel)v1).get_channel();
        }
        v3 = ch__10262__auto__11043 = v2;
        ch__10262__auto__11043 = null;
        ((IFn)KVCluster.const__10.getRawRoot()).invoke(v3, (Object)KVCluster.const__38, ((IFn)KVCluster.const__17.getRawRoot()).invoke(KVCluster.const__18.getRawRoot()));
        var2_2 = null;
        return f__10261__auto__11042;
    }

    /*
     * Unable to fully structure code
     */
    public Object set_ref(Object ref_key, Object rev, Object vkey) {
        v0 = vkey;
        vkey = null;
        v1 = ref_key;
        ref_key = null;
        v2 = rev;
        rev = null;
        f__10261__auto__11044 = ((IFn)KVCluster.const__8.getRawRoot()).invoke((Object)new KVCluster$fn__10953(v0, this.kvs, v1, this.retrying_read, this.retrying_write, v2));
        v3 = f__10261__auto__11044;
        if (Util.classOf((Object)v3) == KVCluster.__cached_class__7) ** GOTO lbl13
        if (!(v3 instanceof GetChannel)) {
            v3 = v3;
            KVCluster.__cached_class__7 = Util.classOf((Object)v3);
lbl13:
            // 2 sources

            v4 = KVCluster.const__9.getRawRoot().invoke(v3);
        } else {
            v4 = ((GetChannel)v3).get_channel();
        }
        v5 = ch__10262__auto__11045 = v4;
        ch__10262__auto__11045 = null;
        ((IFn)KVCluster.const__10.getRawRoot()).invoke(v5, (Object)KVCluster.const__36, ((IFn)KVCluster.const__17.getRawRoot()).invoke(KVCluster.const__18.getRawRoot()));
        v6 = f__10261__auto__11044;
        f__10261__auto__11044 = null;
        return v6;
    }

    /*
     * Unable to fully structure code
     */
    public Object get_ref(Object ref_key) {
        v0 = ref_key;
        ref_key = null;
        f__10261__auto__11046 = ((IFn)KVCluster.const__8.getRawRoot()).invoke((Object)new KVCluster$fn__10939(this.kvs, this.retrying_read, v0));
        v1 = f__10261__auto__11046;
        if (Util.classOf((Object)v1) == KVCluster.__cached_class__6) ** GOTO lbl9
        if (!(v1 instanceof GetChannel)) {
            v1 = v1;
            KVCluster.__cached_class__6 = Util.classOf((Object)v1);
lbl9:
            // 2 sources

            v2 = KVCluster.const__9.getRawRoot().invoke(v1);
        } else {
            v2 = ((GetChannel)v1).get_channel();
        }
        v3 = ch__10262__auto__11047 = v2;
        ch__10262__auto__11047 = null;
        ((IFn)KVCluster.const__10.getRawRoot()).invoke(v3, (Object)KVCluster.const__34, ((IFn)KVCluster.const__17.getRawRoot()).invoke(KVCluster.const__18.getRawRoot()));
        var2_2 = null;
        return f__10261__auto__11046;
    }

    /*
     * Unable to fully structure code
     */
    public Object delete_reference(Object key) {
        v0 = key;
        key = null;
        f__10261__auto__11048 = ((IFn)KVCluster.const__8.getRawRoot()).invoke((Object)new KVCluster$fn__10926(this.kvs, v0, this.retrying_delete));
        v1 = f__10261__auto__11048;
        if (Util.classOf((Object)v1) == KVCluster.__cached_class__5) ** GOTO lbl9
        if (!(v1 instanceof GetChannel)) {
            v1 = v1;
            KVCluster.__cached_class__5 = Util.classOf((Object)v1);
lbl9:
            // 2 sources

            v2 = KVCluster.const__9.getRawRoot().invoke(v1);
        } else {
            v2 = ((GetChannel)v1).get_channel();
        }
        v3 = ch__10262__auto__11049 = v2;
        ch__10262__auto__11049 = null;
        ((IFn)KVCluster.const__10.getRawRoot()).invoke(v3, (Object)KVCluster.const__32, ((IFn)KVCluster.const__17.getRawRoot()).invoke(KVCluster.const__18.getRawRoot()));
        var2_2 = null;
        return f__10261__auto__11048;
    }

    /*
     * Unable to fully structure code
     */
    public Object delete(Object key) {
        v0 = key;
        key = null;
        f__10267__auto__11050 = ((IFn)KVCluster.const__8.getRawRoot()).invoke(((IFn)KVCluster.const__17.getRawRoot()).invoke(KVCluster.const__26.getRawRoot()), (Object)new KVCluster$fn__10913(this.kvs, v0, this.retrying_delete));
        v1 = f__10267__auto__11050;
        if (Util.classOf((Object)v1) == KVCluster.__cached_class__4) ** GOTO lbl9
        if (!(v1 instanceof GetChannel)) {
            v1 = v1;
            KVCluster.__cached_class__4 = Util.classOf((Object)v1);
lbl9:
            // 2 sources

            v2 = KVCluster.const__9.getRawRoot().invoke(v1);
        } else {
            v2 = ((GetChannel)v1).get_channel();
        }
        v3 = ch__10268__auto__11051 = v2;
        ch__10268__auto__11051 = null;
        ((IFn)KVCluster.const__10.getRawRoot()).invoke(v3, (Object)KVCluster.const__29, ((IFn)KVCluster.const__17.getRawRoot()).invoke(KVCluster.const__18.getRawRoot()));
        var2_2 = null;
        return f__10267__auto__11050;
    }

    /*
     * Unable to fully structure code
     */
    public Object get_val(Object val_key) {
        start = System.nanoTime();
        ((IFn)KVCluster.const__22.getRawRoot()).invoke(this.protocol);
        v0 = val_key;
        val_key = null;
        f__10261__auto__11052 = ((IFn)KVCluster.const__8.getRawRoot()).invoke((Object)new KVCluster$fn__10898(this.protocol_nsec_k, this.kvs, v0, this.retrying_read, start));
        v1 = f__10261__auto__11052;
        if (Util.classOf((Object)v1) == KVCluster.__cached_class__3) ** GOTO lbl12
        if (!(v1 instanceof GetChannel)) {
            v1 = v1;
            KVCluster.__cached_class__3 = Util.classOf((Object)v1);
lbl12:
            // 2 sources

            v2 = KVCluster.const__9.getRawRoot().invoke(v1);
        } else {
            v2 = ((GetChannel)v1).get_channel();
        }
        v3 = ch__10262__auto__11053 = v2;
        ch__10262__auto__11053 = null;
        ((IFn)KVCluster.const__10.getRawRoot()).invoke(v3, (Object)KVCluster.const__25, ((IFn)KVCluster.const__17.getRawRoot()).invoke(KVCluster.const__18.getRawRoot()));
        v4 = f__10261__auto__11052;
        f__10261__auto__11052 = null;
        return v4;
    }

    /*
     * Enabled aggressive block sorting
     */
    public Object create_val(Object val_key, Object buf) {
        Object object;
        KVCluster kVCluster = this_;
        if (Util.classOf((Object)kVCluster) != __cached_class__2) {
            if (kVCluster instanceof ClusteredStore) {
                Object object2 = val_key;
                val_key = null;
                Object object3 = buf;
                buf = null;
                object = ((ClusteredStore)kVCluster).create_val(const__5, object2, object3);
                return object;
            }
            kVCluster = kVCluster;
            __cached_class__2 = Util.classOf((Object)kVCluster);
        }
        Object object4 = val_key;
        val_key = null;
        Object object5 = buf;
        buf = null;
        KVCluster this_ = null;
        object = const__21.getRawRoot().invoke((Object)kVCluster, const__5, object4, object5);
        return object;
    }

    /*
     * Unable to fully structure code
     */
    public Object create_val(Object priority, Object val_key, Object buf) {
        block5: {
            block4: {
                backoff = Numbers.lt((Object)priority, (long)3L) != false ? KVCluster.const__6 : KVCluster.const__7;
                v0 = buf;
                buf = null;
                v1 = backoff;
                backoff = null;
                v2 = val_key;
                val_key = null;
                doit = new KVCluster$doit__10886(v0, this.kvs, v1, this.retrying_write, v2);
                v3 = priority;
                priority = null;
                if (!Numbers.lt((Object)v3, (long)3L)) break block4;
                v4 = doit;
                doit = null;
                f__10264__auto__11054 = ((IFn)KVCluster.const__8.getRawRoot()).invoke((Object)v4);
                v5 = f__10264__auto__11054;
                if (Util.classOf((Object)v5) == KVCluster.__cached_class__0) ** GOTO lbl20
                if (!(v5 instanceof GetChannel)) {
                    v5 = v5;
                    KVCluster.__cached_class__0 = Util.classOf((Object)v5);
lbl20:
                    // 2 sources

                    v6 = KVCluster.const__9.getRawRoot().invoke(v5);
                } else {
                    v6 = ((GetChannel)v5).get_channel();
                }
                v7 = ch__10265__auto__11055 = v6;
                ch__10265__auto__11055 = null;
                ((IFn)KVCluster.const__10.getRawRoot()).invoke(v7, (Object)KVCluster.const__16, ((IFn)KVCluster.const__17.getRawRoot()).invoke(KVCluster.const__18.getRawRoot()));
                v8 = f__10264__auto__11054;
                f__10264__auto__11054 = null;
                break block5;
            }
            v9 = doit;
            doit = null;
            f__10270__auto__11056 = ((IFn)KVCluster.const__8.getRawRoot()).invoke(this.exec, (Object)v9);
            v10 = f__10270__auto__11056;
            if (Util.classOf((Object)v10) == KVCluster.__cached_class__1) ** GOTO lbl39
            if (!(v10 instanceof GetChannel)) {
                v10 = v10;
                KVCluster.__cached_class__1 = Util.classOf((Object)v10);
lbl39:
                // 2 sources

                v11 = KVCluster.const__9.getRawRoot().invoke(v10);
            } else {
                v11 = ((GetChannel)v10).get_channel();
            }
            v12 = ch__10271__auto__11057 = v11;
            ch__10271__auto__11057 = null;
            ((IFn)KVCluster.const__10.getRawRoot()).invoke(v12, (Object)KVCluster.const__20, ((IFn)KVCluster.const__17.getRawRoot()).invoke(KVCluster.const__18.getRawRoot()));
            v8 = f__10270__auto__11056;
            f__10270__auto__11056 = null;
        }
        return v8;
    }

    public Object dbId() {
        ILookupThunk iLookupThunk = __thunk__0__;
        Object object = this.path_map;
        Object object2 = iLookupThunk.get(object);
        if (iLookupThunk == object2) {
            __thunk__0__ = __site__0__.fault(object);
            object2 = __thunk__0__.get(object);
        }
        return object2;
    }

    public void close() throws IOException {
        if (Util.equiv((Object)((IFn)const__1.getRawRoot()).invoke(), (Object)this_.exec)) {
        } else {
            ((ExecutorService)this_.exec).shutdown();
        }
        KVCluster this_ = null;
        ((IFn)const__2.getRawRoot()).invoke(this_.kvs);
    }

    static {
        const__1 = RT.var((String)"datomic.kv-cluster", (String)"shared-pool");
        const__2 = RT.var((String)"datomic.cluster", (String)"close");
        const__5 = 3L;
        const__6 = RT.keyword(null, (String)"linear");
        const__7 = RT.keyword(null, (String)"exponential");
        const__8 = RT.var((String)"datomic.future", (String)"-future-with-channel-impl");
        const__9 = RT.var((String)"datomic.future", (String)"get-channel");
        const__10 = RT.var((String)"datomic.future", (String)"add-bounding-warning");
        const__16 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"line"), 157, RT.keyword(null, (String)"column"), 8, RT.keyword(null, (String)"file"), "datomic/kv_cluster.clj"});
        const__17 = RT.var((String)"clojure.core", (String)"deref");
        const__18 = RT.var((String)"datomic.future", (String)"bounding-warn-seconds");
        const__20 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"line"), 158, RT.keyword(null, (String)"column"), 8, RT.keyword(null, (String)"file"), "datomic/kv_cluster.clj"});
        const__21 = RT.var((String)"datomic.cluster", (String)"create-val");
        const__22 = RT.var((String)"datomic.measure.io-stats", (String)"inc!");
        const__25 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"line"), 168, RT.keyword(null, (String)"column"), 7, RT.keyword(null, (String)"file"), "datomic/kv_cluster.clj"});
        const__26 = RT.var((String)"datomic.kv-cluster", (String)"delete-pool-ref");
        const__29 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"line"), 180, RT.keyword(null, (String)"column"), 5, RT.keyword(null, (String)"file"), "datomic/kv_cluster.clj"});
        const__32 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"line"), 188, RT.keyword(null, (String)"column"), 4, RT.keyword(null, (String)"file"), "datomic/kv_cluster.clj"});
        const__34 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"line"), 195, RT.keyword(null, (String)"column"), 4, RT.keyword(null, (String)"file"), "datomic/kv_cluster.clj"});
        const__36 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"line"), 206, RT.keyword(null, (String)"column"), 4, RT.keyword(null, (String)"file"), "datomic/kv_cluster.clj"});
        const__38 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"line"), 230, RT.keyword(null, (String)"column"), 4, RT.keyword(null, (String)"file"), "datomic/kv_cluster.clj"});
        const__40 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"line"), 237, RT.keyword(null, (String)"column"), 4, RT.keyword(null, (String)"file"), "datomic/kv_cluster.clj"});
        const__41 = RT.var((String)"clojure.core", (String)"str");
        const__42 = RT.var((String)"datomic.common", (String)"rand-uuid");
        const__45 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"line"), 265, RT.keyword(null, (String)"column"), 6, RT.keyword(null, (String)"file"), "datomic/kv_cluster.clj"});
        const__46 = RT.var((String)"datomic.cluster", (String)"get-val");
        __site__0__ = new KeywordLookupSite(RT.keyword(null, (String)"db"));
        __thunk__0__ = __site__0__;
    }
}

