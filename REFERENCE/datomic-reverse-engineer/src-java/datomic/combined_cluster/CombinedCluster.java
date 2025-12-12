/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.IFn
 *  clojure.lang.IPersistentVector
 *  clojure.lang.IType
 *  clojure.lang.RT
 *  clojure.lang.Symbol
 *  clojure.lang.Tuple
 *  clojure.lang.Util
 *  clojure.lang.Var
 */
package datomic.combined_cluster;

import clojure.lang.IFn;
import clojure.lang.IPersistentVector;
import clojure.lang.IType;
import clojure.lang.RT;
import clojure.lang.Symbol;
import clojure.lang.Tuple;
import clojure.lang.Util;
import clojure.lang.Var;
import datomic.cluster.ClusteredStore;
import datomic.cluster.Dbid;
import datomic.cluster.Get2;
import datomic.cluster.RefClusterStore;
import java.io.Closeable;
import java.io.IOException;

public final class CombinedCluster
implements Dbid,
Get2,
ClusteredStore,
Closeable,
RefClusterStore,
IType {
    public final Object ref_cluster;
    public final Object val_cluster;
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
    public static final Var const__0;
    public static final Var const__1;
    public static final Var const__2;
    public static final Object const__3;
    public static final Var const__4;
    public static final Var const__5;
    public static final Var const__6;
    public static final Var const__7;
    public static final Var const__8;
    public static final Var const__9;
    public static final Var const__10;
    public static final Var const__11;
    public static final Var const__12;
    public static final Var const__13;

    public CombinedCluster(Object object, Object object2) {
        this.ref_cluster = object;
        this.val_cluster = object2;
    }

    public static IPersistentVector getBasis() {
        return Tuple.create((Object)Symbol.intern(null, (String)"ref-cluster"), (Object)Symbol.intern(null, (String)"val-cluster"));
    }

    public Object _get_ref_store() {
        CombinedCluster this_ = null;
        return ((IFn)const__13.getRawRoot()).invoke(this_.ref_cluster);
    }

    /*
     * Enabled aggressive block sorting
     */
    public Object get_val2(Object val_key, Object opts) {
        Object object;
        Object object2 = this_.val_cluster;
        if (Util.classOf((Object)object2) != __cached_class__11) {
            if (object2 instanceof Get2) {
                Object object3 = val_key;
                val_key = null;
                Object object4 = opts;
                opts = null;
                object = ((Get2)object2).get_val2(object3, object4);
                return object;
            }
            object2 = object2;
            __cached_class__11 = Util.classOf((Object)object2);
        }
        Object object5 = val_key;
        val_key = null;
        Object object6 = opts;
        opts = null;
        CombinedCluster this_ = null;
        object = const__12.getRawRoot().invoke(object2, object5, object6);
        return object;
    }

    /*
     * Enabled aggressive block sorting
     */
    public Object update_pod_STAR_(Object pod_key, Object rev, Object etag, Object buf, Object metamap) {
        Object object;
        Object object2 = this_.ref_cluster;
        if (Util.classOf((Object)object2) != __cached_class__10) {
            if (object2 instanceof ClusteredStore) {
                Object object3 = pod_key;
                pod_key = null;
                Object object4 = rev;
                rev = null;
                Object object5 = etag;
                etag = null;
                Object object6 = buf;
                buf = null;
                Object object7 = metamap;
                metamap = null;
                object = ((ClusteredStore)object2).update_pod_STAR_(object3, object4, object5, object6, object7);
                return object;
            }
            object2 = object2;
            __cached_class__10 = Util.classOf((Object)object2);
        }
        Object object8 = pod_key;
        pod_key = null;
        Object object9 = rev;
        rev = null;
        Object object10 = etag;
        etag = null;
        Object object11 = buf;
        buf = null;
        Object object12 = metamap;
        metamap = null;
        CombinedCluster this_ = null;
        object = const__11.getRawRoot().invoke(object2, object8, object9, object10, object11, object12);
        return object;
    }

    /*
     * Enabled aggressive block sorting
     */
    public Object get_pod(Object pod_key) {
        Object object;
        Object object2 = this_.ref_cluster;
        if (Util.classOf((Object)object2) != __cached_class__9) {
            if (object2 instanceof ClusteredStore) {
                Object object3 = pod_key;
                pod_key = null;
                object = ((ClusteredStore)object2).get_pod(object3);
                return object;
            }
            object2 = object2;
            __cached_class__9 = Util.classOf((Object)object2);
        }
        Object object4 = pod_key;
        pod_key = null;
        CombinedCluster this_ = null;
        object = const__10.getRawRoot().invoke(object2, object4);
        return object;
    }

    /*
     * Enabled aggressive block sorting
     */
    public Object get_pod_meta(Object pod_key) {
        Object object;
        Object object2 = this_.ref_cluster;
        if (Util.classOf((Object)object2) != __cached_class__8) {
            if (object2 instanceof ClusteredStore) {
                Object object3 = pod_key;
                pod_key = null;
                object = ((ClusteredStore)object2).get_pod_meta(object3);
                return object;
            }
            object2 = object2;
            __cached_class__8 = Util.classOf((Object)object2);
        }
        Object object4 = pod_key;
        pod_key = null;
        CombinedCluster this_ = null;
        object = const__9.getRawRoot().invoke(object2, object4);
        return object;
    }

    /*
     * Enabled aggressive block sorting
     */
    public Object set_ref(Object ref_key, Object rev, Object vkey) {
        Object object;
        Object object2 = this_.ref_cluster;
        if (Util.classOf((Object)object2) != __cached_class__7) {
            if (object2 instanceof ClusteredStore) {
                Object object3 = ref_key;
                ref_key = null;
                Object object4 = rev;
                rev = null;
                Object object5 = vkey;
                vkey = null;
                object = ((ClusteredStore)object2).set_ref(object3, object4, object5);
                return object;
            }
            object2 = object2;
            __cached_class__7 = Util.classOf((Object)object2);
        }
        Object object6 = ref_key;
        ref_key = null;
        Object object7 = rev;
        rev = null;
        Object object8 = vkey;
        vkey = null;
        CombinedCluster this_ = null;
        object = const__8.getRawRoot().invoke(object2, object6, object7, object8);
        return object;
    }

    /*
     * Enabled aggressive block sorting
     */
    public Object get_ref(Object ref_key) {
        Object object;
        Object object2 = this_.ref_cluster;
        if (Util.classOf((Object)object2) != __cached_class__6) {
            if (object2 instanceof ClusteredStore) {
                Object object3 = ref_key;
                ref_key = null;
                object = ((ClusteredStore)object2).get_ref(object3);
                return object;
            }
            object2 = object2;
            __cached_class__6 = Util.classOf((Object)object2);
        }
        Object object4 = ref_key;
        ref_key = null;
        CombinedCluster this_ = null;
        object = const__7.getRawRoot().invoke(object2, object4);
        return object;
    }

    /*
     * Enabled aggressive block sorting
     */
    public Object delete_reference(Object key) {
        Object object;
        Object object2 = this_.ref_cluster;
        if (Util.classOf((Object)object2) != __cached_class__5) {
            if (object2 instanceof ClusteredStore) {
                Object object3 = key;
                key = null;
                object = ((ClusteredStore)object2).delete_reference(object3);
                return object;
            }
            object2 = object2;
            __cached_class__5 = Util.classOf((Object)object2);
        }
        Object object4 = key;
        key = null;
        CombinedCluster this_ = null;
        object = const__6.getRawRoot().invoke(object2, object4);
        return object;
    }

    /*
     * Enabled aggressive block sorting
     */
    public Object delete(Object key) {
        Object object;
        Object object2 = this_.val_cluster;
        if (Util.classOf((Object)object2) != __cached_class__4) {
            if (object2 instanceof ClusteredStore) {
                Object object3 = key;
                key = null;
                object = ((ClusteredStore)object2).delete(object3);
                return object;
            }
            object2 = object2;
            __cached_class__4 = Util.classOf((Object)object2);
        }
        Object object4 = key;
        key = null;
        CombinedCluster this_ = null;
        object = const__5.getRawRoot().invoke(object2, object4);
        return object;
    }

    /*
     * Enabled aggressive block sorting
     */
    public Object get_val(Object val_key) {
        Object object;
        Object object2 = this_.val_cluster;
        if (Util.classOf((Object)object2) != __cached_class__3) {
            if (object2 instanceof ClusteredStore) {
                Object object3 = val_key;
                val_key = null;
                object = ((ClusteredStore)object2).get_val(object3);
                return object;
            }
            object2 = object2;
            __cached_class__3 = Util.classOf((Object)object2);
        }
        Object object4 = val_key;
        val_key = null;
        CombinedCluster this_ = null;
        object = const__4.getRawRoot().invoke(object2, object4);
        return object;
    }

    /*
     * Enabled aggressive block sorting
     */
    public Object create_val(Object val_key, Object buf) {
        Object object;
        CombinedCluster combinedCluster = this_;
        if (Util.classOf((Object)combinedCluster) != __cached_class__2) {
            if (combinedCluster instanceof ClusteredStore) {
                Object object2 = val_key;
                val_key = null;
                Object object3 = buf;
                buf = null;
                object = ((ClusteredStore)combinedCluster).create_val(const__3, object2, object3);
                return object;
            }
            combinedCluster = combinedCluster;
            __cached_class__2 = Util.classOf((Object)combinedCluster);
        }
        Object object4 = val_key;
        val_key = null;
        Object object5 = buf;
        buf = null;
        CombinedCluster this_ = null;
        object = const__2.getRawRoot().invoke((Object)combinedCluster, const__3, object4, object5);
        return object;
    }

    /*
     * Enabled aggressive block sorting
     */
    public Object create_val(Object priority, Object val_key, Object buf) {
        Object object;
        Object object2 = this_.val_cluster;
        if (Util.classOf((Object)object2) != __cached_class__1) {
            if (object2 instanceof ClusteredStore) {
                Object object3 = priority;
                priority = null;
                Object object4 = val_key;
                val_key = null;
                Object object5 = buf;
                buf = null;
                object = ((ClusteredStore)object2).create_val(object3, object4, object5);
                return object;
            }
            object2 = object2;
            __cached_class__1 = Util.classOf((Object)object2);
        }
        Object object6 = priority;
        priority = null;
        Object object7 = val_key;
        val_key = null;
        Object object8 = buf;
        buf = null;
        CombinedCluster this_ = null;
        object = const__2.getRawRoot().invoke(object2, object6, object7, object8);
        return object;
    }

    /*
     * Enabled aggressive block sorting
     */
    public Object dbId() {
        Object object;
        Object object2 = this_.ref_cluster;
        if (Util.classOf((Object)object2) != __cached_class__0) {
            if (object2 instanceof Dbid) {
                object = ((Dbid)object2).dbId();
                return object;
            }
            object2 = object2;
            __cached_class__0 = Util.classOf((Object)object2);
        }
        CombinedCluster this_ = null;
        object = const__1.getRawRoot().invoke(object2);
        return object;
    }

    public void close() throws IOException {
        ((IFn)const__0.getRawRoot()).invoke(this_.ref_cluster);
        CombinedCluster this_ = null;
        ((IFn)const__0.getRawRoot()).invoke(this_.val_cluster);
    }

    static {
        const__0 = RT.var((String)"datomic.cluster", (String)"close");
        const__1 = RT.var((String)"datomic.cluster", (String)"dbId");
        const__2 = RT.var((String)"datomic.cluster", (String)"create-val");
        const__3 = 3L;
        const__4 = RT.var((String)"datomic.cluster", (String)"get-val");
        const__5 = RT.var((String)"datomic.cluster", (String)"delete");
        const__6 = RT.var((String)"datomic.cluster", (String)"delete-reference");
        const__7 = RT.var((String)"datomic.cluster", (String)"get-ref");
        const__8 = RT.var((String)"datomic.cluster", (String)"set-ref");
        const__9 = RT.var((String)"datomic.cluster", (String)"get-pod-meta");
        const__10 = RT.var((String)"datomic.cluster", (String)"get-pod");
        const__11 = RT.var((String)"datomic.cluster", (String)"update-pod*");
        const__12 = RT.var((String)"datomic.cluster", (String)"get-val2");
        const__13 = RT.var((String)"datomic.cluster", (String)"get-ref-store");
    }
}

