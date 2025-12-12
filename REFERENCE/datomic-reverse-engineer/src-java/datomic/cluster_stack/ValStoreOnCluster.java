/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.IFn
 *  clojure.lang.ILookupThunk
 *  clojure.lang.IPersistentVector
 *  clojure.lang.IType
 *  clojure.lang.KeywordLookupSite
 *  clojure.lang.RT
 *  clojure.lang.Symbol
 *  clojure.lang.Tuple
 *  clojure.lang.Util
 *  clojure.lang.Var
 */
package datomic.cluster_stack;

import clojure.lang.IFn;
import clojure.lang.ILookupThunk;
import clojure.lang.IPersistentVector;
import clojure.lang.IType;
import clojure.lang.KeywordLookupSite;
import clojure.lang.RT;
import clojure.lang.Symbol;
import clojure.lang.Tuple;
import clojure.lang.Util;
import clojure.lang.Var;
import datomic.cluster.ClusteredStore;
import datomic.cluster_stack.ValStoreOnCluster$fn__11298;
import datomic.cluster_stack.ValStoreOnCluster$fn__11349;
import datomic.cluster_stack.ValStoreOnCluster$fn__11398;
import datomic.core2.val_store.spi.Delete;
import datomic.core2.val_store.spi.Get;
import datomic.core2.val_store.spi.Put;
import datomic.future.GetChannel;

public final class ValStoreOnCluster
implements Get,
Delete,
Put,
IType {
    public final Object cluster;
    private static Class __cached_class__0;
    private static Class __cached_class__1;
    private static Class __cached_class__2;
    private static Class __cached_class__3;
    private static Class __cached_class__4;
    private static Class __cached_class__5;
    public static final Var const__0;
    public static final Var const__1;
    public static final Var const__3;
    public static final Object const__4;
    public static final Var const__5;
    public static final Var const__6;
    public static final Var const__7;
    static final KeywordLookupSite __site__0__;
    static ILookupThunk __thunk__0__;

    public ValStoreOnCluster(Object object) {
        this.cluster = object;
    }

    public static IPersistentVector getBasis() {
        return Tuple.create((Object)Symbol.intern(null, (String)"cluster"));
    }

    /*
     * Unable to fully structure code
     */
    public Object _get(Object k, Object opts) {
        v0 = this.cluster;
        if (Util.classOf((Object)v0) == ValStoreOnCluster.__cached_class__4) ** GOTO lbl6
        if (!(v0 instanceof ClusteredStore)) {
            v0 = v0;
            ValStoreOnCluster.__cached_class__4 = Util.classOf((Object)v0);
lbl6:
            // 2 sources

            v1 = ValStoreOnCluster.const__7.getRawRoot().invoke(v0, k);
        } else {
            v1 = v2 = ((ClusteredStore)v0).get_val(k);
        }
        if (Util.classOf((Object)v1) == ValStoreOnCluster.__cached_class__5) ** GOTO lbl13
        if (!(v2 instanceof GetChannel)) {
            v2 = v2;
            ValStoreOnCluster.__cached_class__5 = Util.classOf((Object)v2);
lbl13:
            // 2 sources

            v3 = ValStoreOnCluster.const__0.getRawRoot().invoke(v2);
        } else {
            v3 = ((GetChannel)v2).get_channel();
        }
        ch = v3;
        c__6597__auto__11426 = ((IFn)ValStoreOnCluster.const__3.getRawRoot()).invoke(ValStoreOnCluster.const__4);
        captured_bindings__6598__auto__11427 = Var.getThreadBindingFrame();
        v4 = opts;
        opts = null;
        v5 = k;
        k = null;
        v6 = captured_bindings__6598__auto__11427;
        captured_bindings__6598__auto__11427 = null;
        v7 = ch;
        ch = null;
        ((IFn)ValStoreOnCluster.const__5.getRawRoot()).invoke((Object)new ValStoreOnCluster$fn__11398(v4, v5, this, this.cluster, v6, c__6597__auto__11426, v7));
        v8 = c__6597__auto__11426;
        c__6597__auto__11426 = null;
        return v8;
    }

    /*
     * Unable to fully structure code
     */
    public Object _delete(Object k, Object opts) {
        v0 = this.cluster;
        if (Util.classOf((Object)v0) == ValStoreOnCluster.__cached_class__2) ** GOTO lbl6
        if (!(v0 instanceof ClusteredStore)) {
            v0 = v0;
            ValStoreOnCluster.__cached_class__2 = Util.classOf((Object)v0);
lbl6:
            // 2 sources

            v1 = ValStoreOnCluster.const__6.getRawRoot().invoke(v0, k);
        } else {
            v1 = v2 = ((ClusteredStore)v0).delete(k);
        }
        if (Util.classOf((Object)v1) == ValStoreOnCluster.__cached_class__3) ** GOTO lbl13
        if (!(v2 instanceof GetChannel)) {
            v2 = v2;
            ValStoreOnCluster.__cached_class__3 = Util.classOf((Object)v2);
lbl13:
            // 2 sources

            v3 = ValStoreOnCluster.const__0.getRawRoot().invoke(v2);
        } else {
            v3 = ((GetChannel)v2).get_channel();
        }
        ch = v3;
        c__6597__auto__11428 = ((IFn)ValStoreOnCluster.const__3.getRawRoot()).invoke(ValStoreOnCluster.const__4);
        v4 = captured_bindings__6598__auto__11429 = Var.getThreadBindingFrame();
        captured_bindings__6598__auto__11429 = null;
        v5 = opts;
        opts = null;
        v6 = k;
        k = null;
        v7 = ch;
        ch = null;
        ((IFn)ValStoreOnCluster.const__5.getRawRoot()).invoke((Object)new ValStoreOnCluster$fn__11349(v4, c__6597__auto__11428, v5, v6, this.cluster, this, v7));
        v8 = c__6597__auto__11428;
        c__6597__auto__11428 = null;
        return v8;
    }

    /*
     * Unable to fully structure code
     */
    public Object _put(Object k, Object v, Object opts) {
        v0 = this.cluster;
        if (Util.classOf((Object)v0) == ValStoreOnCluster.__cached_class__0) ** GOTO lbl6
        if (!(v0 instanceof ClusteredStore)) {
            v0 = v0;
            ValStoreOnCluster.__cached_class__0 = Util.classOf((Object)v0);
lbl6:
            // 2 sources

            v1 = ValStoreOnCluster.__thunk__0__;
            v2 = v;
            v3 = v1.get(v2);
            if (v1 == v3) {
                ValStoreOnCluster.__thunk__0__ = ValStoreOnCluster.__site__0__.fault(v2);
                v3 = ValStoreOnCluster.__thunk__0__.get(v2);
            }
            v4 = ValStoreOnCluster.const__1.getRawRoot().invoke(v0, k, v3);
        } else {
            v5 = (ClusteredStore)v0;
            v6 = ValStoreOnCluster.__thunk__0__;
            v7 = v;
            v8 = v6.get(v7);
            if (v6 == v8) {
                ValStoreOnCluster.__thunk__0__ = ValStoreOnCluster.__site__0__.fault(v7);
                v8 = ValStoreOnCluster.__thunk__0__.get(v7);
            }
            v4 = v9 = v5.create_val(k, v8);
        }
        if (Util.classOf((Object)v4) == ValStoreOnCluster.__cached_class__1) ** GOTO lbl28
        if (!(v9 instanceof GetChannel)) {
            v9 = v9;
            ValStoreOnCluster.__cached_class__1 = Util.classOf((Object)v9);
lbl28:
            // 2 sources

            v10 = ValStoreOnCluster.const__0.getRawRoot().invoke(v9);
        } else {
            v10 = ((GetChannel)v9).get_channel();
        }
        ch = v10;
        c__6597__auto__11430 = ((IFn)ValStoreOnCluster.const__3.getRawRoot()).invoke(ValStoreOnCluster.const__4);
        captured_bindings__6598__auto__11431 = Var.getThreadBindingFrame();
        v11 = ch;
        ch = null;
        v12 = captured_bindings__6598__auto__11431;
        captured_bindings__6598__auto__11431 = null;
        v13 = v;
        v = null;
        v14 = k;
        k = null;
        v15 = opts;
        opts = null;
        ((IFn)ValStoreOnCluster.const__5.getRawRoot()).invoke((Object)new ValStoreOnCluster$fn__11298(c__6597__auto__11430, v11, this.cluster, v12, v13, this, v14, v15));
        v16 = c__6597__auto__11430;
        c__6597__auto__11430 = null;
        return v16;
    }

    static {
        const__0 = RT.var((String)"datomic.future", (String)"get-channel");
        const__1 = RT.var((String)"datomic.cluster", (String)"create-val");
        const__3 = RT.var((String)"clojure.core.async", (String)"chan");
        const__4 = 1L;
        const__5 = RT.var((String)"clojure.core.async.impl.dispatch", (String)"run");
        const__6 = RT.var((String)"datomic.cluster", (String)"delete");
        const__7 = RT.var((String)"datomic.cluster", (String)"get-val");
        __site__0__ = new KeywordLookupSite(RT.keyword(null, (String)"val"));
        __thunk__0__ = __site__0__;
    }
}

