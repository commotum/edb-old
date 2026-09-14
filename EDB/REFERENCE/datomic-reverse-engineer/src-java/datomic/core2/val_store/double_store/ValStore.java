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
 *  clojure.lang.Var
 */
package datomic.core2.val_store.double_store;

import clojure.lang.IFn;
import clojure.lang.IPersistentVector;
import clojure.lang.IType;
import clojure.lang.RT;
import clojure.lang.Symbol;
import clojure.lang.Tuple;
import clojure.lang.Var;
import datomic.core2.val_store.double_store.ValStore$fn__21186;
import datomic.core2.val_store.spi.Delete;
import datomic.core2.val_store.spi.Get;
import datomic.core2.val_store.spi.Put;

public final class ValStore
implements Get,
Delete,
Put,
IType {
    public final Object near_store;
    public final Object far_store;
    public final Object repair_metric;
    public final Object get_fallback_msec;
    public static final Var const__0 = RT.var((String)"datomic.core2.val-store", (String)"delete");
    public static final Var const__1 = RT.var((String)"clojure.core.async", (String)"chan");
    public static final Object const__2 = 1L;
    public static final Var const__3 = RT.var((String)"clojure.core.async.impl.dispatch", (String)"run");
    public static final Var const__4 = RT.var((String)"datomic.core2.val-store", (String)"put");

    public ValStore(Object object, Object object2, Object object3, Object object4) {
        this.near_store = object;
        this.far_store = object2;
        this.repair_metric = object3;
        this.get_fallback_msec = object4;
    }

    public static IPersistentVector getBasis() {
        return Tuple.create((Object)Symbol.intern(null, (String)"near-store"), (Object)Symbol.intern(null, (String)"far-store"), (Object)Symbol.intern(null, (String)"repair-metric"), (Object)Symbol.intern(null, (String)"get-fallback-msec"));
    }

    @Override
    public Object _put(Object k, Object v, Object opts) {
        ((IFn)const__4.getRawRoot()).invoke(this_.near_store, k, v, opts);
        Object object = k;
        k = null;
        Object object2 = v;
        v = null;
        Object object3 = opts;
        opts = null;
        ValStore this_ = null;
        return ((IFn)const__4.getRawRoot()).invoke(this_.far_store, object, object2, object3);
    }

    @Override
    public Object _get(Object k, Object opts) {
        Object captured_bindings__10231__auto__21247;
        Object c__10230__auto__21246 = ((IFn)const__1.getRawRoot()).invoke(const__2);
        Object object = captured_bindings__10231__auto__21247 = Var.getThreadBindingFrame();
        captured_bindings__10231__auto__21247 = null;
        Object object2 = opts;
        opts = null;
        Object object3 = k;
        k = null;
        ((IFn)const__3.getRawRoot()).invoke((Object)new ValStore$fn__21186(object, object2, object3, this, this.far_store, c__10230__auto__21246, this.get_fallback_msec, this.near_store, this.repair_metric));
        Object var3_3 = null;
        return c__10230__auto__21246;
    }

    @Override
    public Object _delete(Object k, Object opts) {
        ((IFn)const__0.getRawRoot()).invoke(this_.near_store, k, opts);
        Object object = k;
        k = null;
        Object object2 = opts;
        opts = null;
        ValStore this_ = null;
        return ((IFn)const__0.getRawRoot()).invoke(this_.far_store, object, object2);
    }
}

