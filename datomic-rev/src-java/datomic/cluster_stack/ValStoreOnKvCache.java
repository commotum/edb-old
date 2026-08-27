/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFn
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

import clojure.lang.AFn;
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
import datomic.cluster_stack.ValStoreOnKvCache$fn__11261;
import datomic.core2.val_store.spi.Delete;
import datomic.core2.val_store.spi.Get;
import datomic.core2.val_store.spi.Put;
import datomic.future.GetChannel;

public final class ValStoreOnKvCache
implements Get,
Delete,
Put,
IType {
    public final Object exec;
    public final Object kv_cache;
    private static Class __cached_class__0;
    private static Class __cached_class__1;
    public static final Var const__0;
    public static final Var const__2;
    public static final Var const__3;
    public static final AFn const__6;
    public static final Var const__7;
    public static final Var const__8;
    public static final Var const__9;
    public static final AFn const__15;
    public static final Var const__16;
    public static final Var const__17;
    public static final AFn const__19;
    static final KeywordLookupSite __site__0__;
    static ILookupThunk __thunk__0__;

    public ValStoreOnKvCache(Object object, Object object2) {
        this.exec = object;
        this.kv_cache = object2;
    }

    public static IPersistentVector getBasis() {
        return Tuple.create((Object)Symbol.intern(null, (String)"exec"), (Object)Symbol.intern(null, (String)"kv-cache"));
    }

    public Object _delete(Object k, Object opts) {
        Object G__11263 = ((IFn)const__2.getRawRoot()).invoke();
        ((IFn)const__3.getRawRoot()).invoke(G__11263, (Object)const__19);
        Object var3_3 = null;
        return G__11263;
    }

    /*
     * Unable to fully structure code
     */
    public Object _get(Object k, Object opts) {
        v0 = k;
        k = null;
        f__10267__auto__11265 = ((IFn)ValStoreOnKvCache.const__8.getRawRoot()).invoke(this.exec, (Object)new ValStoreOnKvCache$fn__11261(this.kv_cache, v0));
        v1 = f__10267__auto__11265;
        if (Util.classOf((Object)v1) == ValStoreOnKvCache.__cached_class__0) ** GOTO lbl9
        if (!(v1 instanceof GetChannel)) {
            v1 = v1;
            ValStoreOnKvCache.__cached_class__0 = Util.classOf((Object)v1);
lbl9:
            // 2 sources

            v2 = ValStoreOnKvCache.const__7.getRawRoot().invoke(v1);
        } else {
            v2 = ((GetChannel)v1).get_channel();
        }
        v3 = ch__10268__auto__11266 = v2;
        ch__10268__auto__11266 = null;
        ((IFn)ValStoreOnKvCache.const__9.getRawRoot()).invoke(v3, (Object)ValStoreOnKvCache.const__15, ((IFn)ValStoreOnKvCache.const__16.getRawRoot()).invoke(ValStoreOnKvCache.const__17.getRawRoot()));
        v4 = f__10267__auto__11265;
        var3_3 = null;
        v5 = v4;
        if (Util.classOf((Object)v4) == ValStoreOnKvCache.__cached_class__1) ** GOTO lbl23
        if (!(v5 instanceof GetChannel)) {
            v5 = v5;
            ValStoreOnKvCache.__cached_class__1 = Util.classOf((Object)v5);
lbl23:
            // 2 sources

            this = null;
            v6 = ValStoreOnKvCache.const__7.getRawRoot().invoke(v5);
        } else {
            v6 = ((GetChannel)v5).get_channel();
        }
        return v6;
    }

    public Object _put(Object k, Object v, Object opts) {
        IFn iFn = (IFn)const__0.getRawRoot();
        Object object = k;
        k = null;
        ILookupThunk iLookupThunk = __thunk__0__;
        Object object2 = v;
        v = null;
        Object object3 = iLookupThunk.get(object2);
        if (iLookupThunk == object3) {
            __thunk__0__ = __site__0__.fault(object2);
            object3 = __thunk__0__.get(object2);
        }
        iFn.invoke(this.kv_cache, object, object3);
        Object G__11260 = ((IFn)const__2.getRawRoot()).invoke();
        ((IFn)const__3.getRawRoot()).invoke(G__11260, (Object)const__6);
        Object object4 = G__11260;
        G__11260 = null;
        return object4;
    }

    static {
        const__0 = RT.var((String)"datomic.cache", (String)"put");
        const__2 = RT.var((String)"clojure.core.async", (String)"promise-chan");
        const__3 = RT.var((String)"clojure.core.async", (String)"put!");
        const__6 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"result"), RT.keyword(null, (String)"unknown")});
        const__7 = RT.var((String)"datomic.future", (String)"get-channel");
        const__8 = RT.var((String)"datomic.future", (String)"-future-with-channel-impl");
        const__9 = RT.var((String)"datomic.future", (String)"add-bounding-warning");
        const__15 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"line"), 40, RT.keyword(null, (String)"column"), 9, RT.keyword(null, (String)"file"), "datomic/cluster_stack.clj"});
        const__16 = RT.var((String)"clojure.core", (String)"deref");
        const__17 = RT.var((String)"datomic.future", (String)"bounding-warn-seconds");
        const__19 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"result"), RT.keyword(null, (String)"no-op")});
        __site__0__ = new KeywordLookupSite(RT.keyword(null, (String)"val"));
        __thunk__0__ = __site__0__;
    }
}

