/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFn
 *  clojure.lang.IFn
 *  clojure.lang.IPersistentVector
 *  clojure.lang.IType
 *  clojure.lang.RT
 *  clojure.lang.Symbol
 *  clojure.lang.Tuple
 *  clojure.lang.Util
 *  clojure.lang.Var
 */
package datomic.val_cluster;

import clojure.lang.AFn;
import clojure.lang.IFn;
import clojure.lang.IPersistentVector;
import clojure.lang.IType;
import clojure.lang.RT;
import clojure.lang.Symbol;
import clojure.lang.Tuple;
import clojure.lang.Util;
import clojure.lang.Var;
import datomic.cluster.ClusteredStore;
import datomic.cluster.Get2;
import datomic.cluster.RefClusterStore;
import datomic.future.GetChannel;
import datomic.val_cluster.Impl;
import datomic.val_cluster.ValCluster$fn__11207;
import datomic.val_cluster.ValCluster$fn__11219;
import datomic.val_cluster.ValCluster$fn__11230;

public final class ValCluster
implements Get2,
ClusteredStore,
Impl,
RefClusterStore,
IType {
    public final Object val_store;
    private static Class __cached_class__0;
    private static Class __cached_class__1;
    private static Class __cached_class__2;
    private static Class __cached_class__3;
    private static Class __cached_class__4;
    private static Class __cached_class__5;
    public static final Var const__0;
    public static final Var const__1;
    public static final Var const__2;
    public static final AFn const__8;
    public static final Var const__9;
    public static final Var const__10;
    public static final AFn const__12;
    public static final Var const__13;
    public static final Object const__14;
    public static final Var const__15;
    public static final AFn const__17;

    public ValCluster(Object object) {
        this.val_store = object;
    }

    public static IPersistentVector getBasis() {
        return Tuple.create((Object)Symbol.intern(null, (String)"val-store"));
    }

    public Object _get_ref_store() {
        return null;
    }

    /*
     * Enabled aggressive block sorting
     */
    public Object get_val2(Object val_key, Object opts) {
        Object object;
        ValCluster valCluster = this_;
        if (Util.classOf((Object)valCluster) != __cached_class__5) {
            if (valCluster instanceof Impl) {
                Object object2 = val_key;
                val_key = null;
                Object object3 = opts;
                opts = null;
                object = ((Impl)valCluster)._get(object2, object3);
                return object;
            }
            valCluster = valCluster;
            __cached_class__5 = Util.classOf((Object)valCluster);
        }
        Object object4 = val_key;
        val_key = null;
        Object object5 = opts;
        opts = null;
        ValCluster this_ = null;
        object = const__15.getRawRoot().invoke((Object)valCluster, object4, object5);
        return object;
    }

    /*
     * Unable to fully structure code
     */
    public Object delete(Object key) {
        v0 = key;
        key = null;
        f__10261__auto__11242 = ((IFn)ValCluster.const__0.getRawRoot()).invoke((Object)new ValCluster$fn__11230(this.val_store, v0));
        v1 = f__10261__auto__11242;
        if (Util.classOf((Object)v1) == ValCluster.__cached_class__4) ** GOTO lbl9
        if (!(v1 instanceof GetChannel)) {
            v1 = v1;
            ValCluster.__cached_class__4 = Util.classOf((Object)v1);
lbl9:
            // 2 sources

            v2 = ValCluster.const__1.getRawRoot().invoke(v1);
        } else {
            v2 = ((GetChannel)v1).get_channel();
        }
        v3 = ch__10262__auto__11243 = v2;
        ch__10262__auto__11243 = null;
        ((IFn)ValCluster.const__2.getRawRoot()).invoke(v3, (Object)ValCluster.const__17, ((IFn)ValCluster.const__9.getRawRoot()).invoke(ValCluster.const__10.getRawRoot()));
        var2_2 = null;
        return f__10261__auto__11242;
    }

    /*
     * Enabled aggressive block sorting
     */
    public Object get_val(Object val_key) {
        Object object;
        ValCluster valCluster = this_;
        if (Util.classOf((Object)valCluster) != __cached_class__3) {
            if (valCluster instanceof Impl) {
                Object object2 = val_key;
                val_key = null;
                object = ((Impl)valCluster)._get(object2, null);
                return object;
            }
            valCluster = valCluster;
            __cached_class__3 = Util.classOf((Object)valCluster);
        }
        Object object3 = val_key;
        val_key = null;
        ValCluster this_ = null;
        object = const__15.getRawRoot().invoke((Object)valCluster, object3, null);
        return object;
    }

    /*
     * Enabled aggressive block sorting
     */
    public Object create_val(Object val_key, Object buf) {
        Object object;
        ValCluster valCluster = this_;
        if (Util.classOf((Object)valCluster) != __cached_class__2) {
            if (valCluster instanceof ClusteredStore) {
                Object object2 = val_key;
                val_key = null;
                Object object3 = buf;
                buf = null;
                object = ((ClusteredStore)valCluster).create_val(const__14, object2, object3);
                return object;
            }
            valCluster = valCluster;
            __cached_class__2 = Util.classOf((Object)valCluster);
        }
        Object object4 = val_key;
        val_key = null;
        Object object5 = buf;
        buf = null;
        ValCluster this_ = null;
        object = const__13.getRawRoot().invoke((Object)valCluster, const__14, object4, object5);
        return object;
    }

    /*
     * Unable to fully structure code
     */
    public Object create_val(Object _, Object val_key, Object buf) {
        v0 = val_key;
        val_key = null;
        v1 = buf;
        buf = null;
        f__10261__auto__11244 = ((IFn)ValCluster.const__0.getRawRoot()).invoke((Object)new ValCluster$fn__11219(v0, this.val_store, v1));
        v2 = f__10261__auto__11244;
        if (Util.classOf((Object)v2) == ValCluster.__cached_class__1) ** GOTO lbl11
        if (!(v2 instanceof GetChannel)) {
            v2 = v2;
            ValCluster.__cached_class__1 = Util.classOf((Object)v2);
lbl11:
            // 2 sources

            v3 = ValCluster.const__1.getRawRoot().invoke(v2);
        } else {
            v3 = ((GetChannel)v2).get_channel();
        }
        v4 = ch__10262__auto__11245 = v3;
        ch__10262__auto__11245 = null;
        ((IFn)ValCluster.const__2.getRawRoot()).invoke(v4, (Object)ValCluster.const__12, ((IFn)ValCluster.const__9.getRawRoot()).invoke(ValCluster.const__10.getRawRoot()));
        v5 = f__10261__auto__11244;
        f__10261__auto__11244 = null;
        return v5;
    }

    /*
     * Unable to fully structure code
     */
    public Object _get(Object val_key, Object opts) {
        v0 = val_key;
        val_key = null;
        v1 = opts;
        opts = null;
        f__10261__auto__11246 = ((IFn)ValCluster.const__0.getRawRoot()).invoke((Object)new ValCluster$fn__11207(v0, this.val_store, v1));
        v2 = f__10261__auto__11246;
        if (Util.classOf((Object)v2) == ValCluster.__cached_class__0) ** GOTO lbl11
        if (!(v2 instanceof GetChannel)) {
            v2 = v2;
            ValCluster.__cached_class__0 = Util.classOf((Object)v2);
lbl11:
            // 2 sources

            v3 = ValCluster.const__1.getRawRoot().invoke(v2);
        } else {
            v3 = ((GetChannel)v2).get_channel();
        }
        v4 = ch__10262__auto__11247 = v3;
        ch__10262__auto__11247 = null;
        ((IFn)ValCluster.const__2.getRawRoot()).invoke(v4, (Object)ValCluster.const__8, ((IFn)ValCluster.const__9.getRawRoot()).invoke(ValCluster.const__10.getRawRoot()));
        var3_3 = null;
        return f__10261__auto__11246;
    }

    static {
        const__0 = RT.var((String)"datomic.future", (String)"-future-with-channel-impl");
        const__1 = RT.var((String)"datomic.future", (String)"get-channel");
        const__2 = RT.var((String)"datomic.future", (String)"add-bounding-warning");
        const__8 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"line"), 25, RT.keyword(null, (String)"column"), 5, RT.keyword(null, (String)"file"), "datomic/val_cluster.clj"});
        const__9 = RT.var((String)"clojure.core", (String)"deref");
        const__10 = RT.var((String)"datomic.future", (String)"bounding-warn-seconds");
        const__12 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"line"), 43, RT.keyword(null, (String)"column"), 5, RT.keyword(null, (String)"file"), "datomic/val_cluster.clj"});
        const__13 = RT.var((String)"datomic.cluster", (String)"create-val");
        const__14 = 3L;
        const__15 = RT.var((String)"datomic.val-cluster", (String)"-get");
        const__17 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"line"), 68, RT.keyword(null, (String)"column"), 5, RT.keyword(null, (String)"file"), "datomic/val_cluster.clj"});
    }
}

