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
 *  clojure.lang.PersistentArrayMap
 *  clojure.lang.RT
 *  clojure.lang.Symbol
 *  clojure.lang.Tuple
 *  clojure.lang.Util
 *  clojure.lang.Var
 *  cognitect.caster.Impl
 *  com.amazonaws.services.s3.model.ObjectMetadata
 *  datomic.java.io.impl.ByteBufferInputStream
 */
package datomic.core2.val_store.s3.sdkv1;

import clojure.lang.AFn;
import clojure.lang.IFn;
import clojure.lang.ILookupThunk;
import clojure.lang.IObj;
import clojure.lang.IPersistentVector;
import clojure.lang.IType;
import clojure.lang.Keyword;
import clojure.lang.KeywordLookupSite;
import clojure.lang.PersistentArrayMap;
import clojure.lang.RT;
import clojure.lang.Symbol;
import clojure.lang.Tuple;
import clojure.lang.Util;
import clojure.lang.Var;
import com.amazonaws.services.s3.model.ObjectMetadata;
import datomic.core2.val_store.s3.sdkv1.Impl;
import datomic.core2.val_store.s3.sdkv1.ValStore$fn__21764;
import datomic.core2.val_store.s3.sdkv1.ValStore$fn__21766;
import datomic.core2.val_store.s3.sdkv1.ValStore$fn__21768;
import datomic.core2.val_store.spi.Delete;
import datomic.core2.val_store.spi.Get;
import datomic.core2.val_store.spi.Put;
import datomic.java.io.impl.ByteBufferInputStream;
import java.nio.Buffer;
import java.nio.ByteBuffer;

public final class ValStore
implements Get,
Delete,
Impl,
Put,
IType {
    public final Object read_pool;
    public final Object write_pool;
    public final Object retry_fn;
    public final Object client;
    public final Object bucket;
    public final Object prefix;
    private static Class __cached_class__0;
    private static Class __cached_class__1;
    private static Class __cached_class__2;
    private static Class __cached_class__3;
    private static Class __cached_class__4;
    public static final Var const__0;
    public static final Var const__1;
    public static final Keyword const__2;
    public static final Keyword const__3;
    public static final Keyword const__4;
    public static final Var const__5;
    public static final Var const__6;
    public static final Keyword const__8;
    public static final AFn const__9;
    public static final Keyword const__10;
    public static final Var const__11;
    public static final Var const__12;
    public static final Keyword const__13;
    public static final Keyword const__14;
    public static final Keyword const__15;
    public static final Keyword const__17;
    public static final Keyword const__18;
    public static final Var const__19;
    public static final Var const__20;
    public static final Var const__21;
    public static final Var const__22;
    public static final Var const__23;
    public static final Var const__25;
    public static final Var const__26;
    public static final Keyword const__27;
    public static final AFn const__30;
    public static final Var const__31;
    public static final AFn const__33;
    public static final Var const__34;
    public static final Var const__35;
    public static final Keyword const__36;
    public static final Keyword const__37;
    public static final Var const__38;
    public static final Keyword const__39;
    public static final Keyword const__40;
    public static final Keyword const__41;
    static final KeywordLookupSite __site__0__;
    static ILookupThunk __thunk__0__;

    public ValStore(Object object, Object object2, Object object3, Object object4, Object object5, Object object6) {
        this.read_pool = object;
        this.write_pool = object2;
        this.retry_fn = object3;
        this.client = object4;
        this.bucket = object5;
        this.prefix = object6;
    }

    public static IPersistentVector getBasis() {
        return Tuple.create((Object)((IObj)Symbol.intern(null, (String)"read-pool")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), Symbol.intern(null, (String)"ExecutorService")})), (Object)((IObj)Symbol.intern(null, (String)"write-pool")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), Symbol.intern(null, (String)"ExecutorService")})), (Object)Symbol.intern(null, (String)"retry-fn"), (Object)((IObj)Symbol.intern(null, (String)"client")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), Symbol.intern(null, (String)"AmazonS3Client")})), (Object)Symbol.intern(null, (String)"bucket"), (Object)Symbol.intern(null, (String)"prefix"));
    }

    /*
     * Unable to fully structure code
     */
    @Override
    public Object _delete(Object k, Object opts) {
        v0 = (IFn)ValStore.const__34.getRawRoot();
        v1 = this;
        if (Util.classOf((Object)v1) == ValStore.__cached_class__4) ** GOTO lbl7
        if (!(v1 instanceof Impl)) {
            v1 = v1;
            ValStore.__cached_class__4 = Util.classOf((Object)v1);
lbl7:
            // 2 sources

            v2 = new ValStore$fn__21768(opts, this);
            v3 = k;
            k = null;
            v4 = opts;
            opts = null;
            v5 = ValStore.const__35.getRawRoot().invoke((Object)v1, (Object)v2, v3, (Object)ValStore.const__41, v4, (Object)PersistentArrayMap.EMPTY);
        } else {
            v6 = new ValStore$fn__21768(opts, this);
            v7 = k;
            k = null;
            v8 = opts;
            opts = null;
            v5 = ((Impl)v1)._wrap_op((Object)v6, v7, ValStore.const__41, v8, PersistentArrayMap.EMPTY);
        }
        this = null;
        return v0.invoke(v5, this.write_pool);
    }

    /*
     * Unable to fully structure code
     */
    @Override
    public Object _get(Object k, Object opts) {
        ((IFn)ValStore.const__38.getRawRoot()).invoke((Object)ValStore.const__39);
        v0 = (IFn)ValStore.const__34.getRawRoot();
        v1 = this;
        if (Util.classOf((Object)v1) == ValStore.__cached_class__3) ** GOTO lbl9
        if (!(v1 instanceof Impl)) {
            v1 = v1;
            ValStore.__cached_class__3 = Util.classOf((Object)v1);
lbl9:
            // 2 sources

            v2 = new ValStore$fn__21766(this, opts);
            v3 = k;
            k = null;
            v4 = opts;
            opts = null;
            v5 = ValStore.const__35.getRawRoot().invoke((Object)v1, (Object)v2, v3, (Object)ValStore.const__40, v4, (Object)PersistentArrayMap.EMPTY);
        } else {
            v6 = new ValStore$fn__21766(this, opts);
            v7 = k;
            k = null;
            v8 = opts;
            opts = null;
            v5 = ((Impl)v1)._wrap_op((Object)v6, v7, ValStore.const__40, v8, PersistentArrayMap.EMPTY);
        }
        this = null;
        return v0.invoke(v5, this.read_pool);
    }

    /*
     * Unable to fully structure code
     */
    @Override
    public Object _put(Object k, Object v, Object opts) {
        v0 = (IFn)ValStore.const__34.getRawRoot();
        v1 = this;
        if (Util.classOf((Object)v1) == ValStore.__cached_class__2) ** GOTO lbl7
        if (!(v1 instanceof Impl)) {
            v1 = v1;
            ValStore.__cached_class__2 = Util.classOf((Object)v1);
lbl7:
            // 2 sources

            v2 = new ValStore$fn__21764(v, this, opts);
            v3 = k;
            k = null;
            v4 = opts;
            opts = null;
            v5 = new Object[2];
            v5[0] = ValStore.const__37;
            v6 = ValStore.__thunk__0__;
            v7 = v;
            v = null;
            v8 = v6.get(v7);
            if (v6 == v8) {
                ValStore.__thunk__0__ = ValStore.__site__0__.fault(v7);
                v8 = ValStore.__thunk__0__.get(v7);
            }
            v5[1] = ((Buffer)v8).remaining();
            v9 = ValStore.const__35.getRawRoot().invoke((Object)v1, (Object)v2, v3, (Object)ValStore.const__36, v4, (Object)RT.mapUniqueKeys((Object[])v5));
        } else {
            v10 = v1;
            v11 = new ValStore$fn__21764(v, this, opts);
            v12 = k;
            k = null;
            v13 = opts;
            opts = null;
            v14 = new Object[2];
            v14[0] = ValStore.const__37;
            v15 = ValStore.__thunk__0__;
            v16 = v;
            v = null;
            v17 = v15.get(v16);
            if (v15 == v17) {
                ValStore.__thunk__0__ = ValStore.__site__0__.fault(v16);
                v17 = ValStore.__thunk__0__.get(v16);
            }
            v14[1] = ((Buffer)v17).remaining();
            v9 = v10._wrap_op((Object)v11, v12, ValStore.const__36, v13, RT.mapUniqueKeys((Object[])v14));
        }
        this = null;
        return v0.invoke(v9, this.write_pool);
    }

    @Override
    public Object _sync_delete(Object k, Object opts) {
        Object object = k;
        k = null;
        Object object2 = opts;
        opts = null;
        ((IFn)const__31.getRawRoot()).invoke(this.client, this.bucket, ((IFn)const__5.getRawRoot()).invoke(this.prefix, object, object2));
        return const__33;
    }

    /*
     * Unable to fully structure code
     */
    @Override
    public Object _sync_put(Object k, Object p__21760, Object opts) {
        block9: {
            block8: {
                v0 = p__21760;
                p__21760 = null;
                map__21762 = v0;
                v1 = ((IFn)ValStore.const__19.getRawRoot()).invoke(map__21762);
                if (v1 != null && v1 != Boolean.FALSE) {
                    v2 = ((IFn)ValStore.const__20.getRawRoot()).invoke(map__21762);
                    if (v2 != null && v2 != Boolean.FALSE) {
                        v3 = map__21762;
                        map__21762 = null;
                        v4 = PersistentArrayMap.createAsIfByAssoc((Object[])((Object[])((IFn)ValStore.const__21.getRawRoot()).invoke(v3)));
                    } else {
                        v5 = ((IFn)ValStore.const__22.getRawRoot()).invoke(map__21762);
                        if (v5 != null && v5 != Boolean.FALSE) {
                            v6 = map__21762;
                            map__21762 = null;
                            v4 = ((IFn)ValStore.const__23.getRawRoot()).invoke(v6);
                        } else {
                            v4 = PersistentArrayMap.EMPTY;
                        }
                    }
                } else {
                    v4 = map__21762;
                    map__21762 = null;
                }
                v = map__21762 = v4;
                v7 = map__21762;
                map__21762 = null;
                val = RT.get((Object)v7, (Object)ValStore.const__8);
                v8 = v;
                v = null;
                v9 = or__5581__auto__21771 = ((IFn)ValStore.const__25.getRawRoot()).invoke(k, v8);
                if (v9 == null || v9 == Boolean.FALSE) break block8;
                v10 = or__5581__auto__21771;
                or__5581__auto__21771 = null;
                break block9;
            }
            content_length = ((Buffer)val).remaining();
            v11 = (IFn)ValStore.const__26.getRawRoot();
            v12 = k;
            k = null;
            v13 = opts;
            opts = null;
            v14 = ((IFn)ValStore.const__5.getRawRoot()).invoke(this.prefix, v12, v13);
            v15 = val;
            val = null;
            v16 = new ByteBufferInputStream((ByteBuffer)v15);
            G__21763 = new ObjectMetadata();
            G__21763.setContentLength((long)content_length);
            v17 = G__21763;
            G__21763 = null;
            v11.invoke(this.client, this.bucket, v14, (Object)v16, (Object)v17);
            v18 = ValStore.const__12.getRawRoot();
            if (Util.classOf((Object)v18) == ValStore.__cached_class__1) ** GOTO lbl55
            if (!(v18 instanceof cognitect.caster.Impl)) {
                v18 = v18;
                ValStore.__cached_class__1 = Util.classOf((Object)v18);
lbl55:
                // 2 sources

                v19 = ValStore.const__11.getRawRoot().invoke(v18, (Object)RT.mapUniqueKeys((Object[])new Object[]{ValStore.const__13, ValStore.const__27, ValStore.const__15, content_length, ValStore.const__17, ValStore.const__18}));
            } else {
                v19 = ((cognitect.caster.Impl)v18).metric_STAR_((Object)RT.mapUniqueKeys((Object[])new Object[]{ValStore.const__13, ValStore.const__27, ValStore.const__15, content_length, ValStore.const__17, ValStore.const__18}));
            }
            v10 = ValStore.const__30;
        }
        return v10;
    }

    /*
     * Unable to fully structure code
     */
    @Override
    public Object _sync_get(Object k, Object opts) {
        block3: {
            block4: {
                block2: {
                    v0 = k;
                    k = null;
                    v1 = opts;
                    opts = null;
                    value = ((IFn)ValStore.const__6.getRawRoot()).invoke(this.client, this.bucket, ((IFn)ValStore.const__5.getRawRoot()).invoke(this.prefix, v0, v1));
                    if (!Util.identical((Object)value, null)) break block2;
                    v2 = ValStore.const__9;
                    break block3;
                }
                v3 = ValStore.const__10;
                if (v3 == null || v3 == Boolean.FALSE) break block4;
                v4 = ValStore.const__12.getRawRoot();
                if (Util.classOf((Object)v4) == ValStore.__cached_class__0) ** GOTO lbl17
                if (!(v4 instanceof cognitect.caster.Impl)) {
                    v4 = v4;
                    ValStore.__cached_class__0 = Util.classOf((Object)v4);
lbl17:
                    // 2 sources

                    v5 = ValStore.const__11.getRawRoot().invoke(v4, (Object)RT.mapUniqueKeys((Object[])new Object[]{ValStore.const__13, ValStore.const__14, ValStore.const__15, RT.count((Object)value), ValStore.const__17, ValStore.const__18}));
                } else {
                    v5 = ((cognitect.caster.Impl)v4).metric_STAR_((Object)RT.mapUniqueKeys((Object[])new Object[]{ValStore.const__13, ValStore.const__14, ValStore.const__15, RT.count((Object)value), ValStore.const__17, ValStore.const__18}));
                }
                v6 = new Object[2];
                v6[0] = ValStore.const__8;
                v7 = value;
                value = null;
                v6[1] = ByteBuffer.wrap((byte[])v7);
                v2 = RT.mapUniqueKeys((Object[])v6);
                break block3;
            }
            v2 = null;
        }
        return v2;
    }

    @Override
    public Object _wrap_op(Object f, Object k, Object op, Object opts, Object context) {
        Object object = f;
        f = null;
        Object[] objectArray = new Object[6];
        objectArray[0] = const__2;
        objectArray[1] = this_.bucket;
        objectArray[2] = const__3;
        objectArray[3] = this_.prefix;
        objectArray[4] = const__4;
        Object object2 = opts;
        opts = null;
        objectArray[5] = ((IFn)const__5.getRawRoot()).invoke(this_.prefix, k, object2);
        Object object3 = k;
        k = null;
        Object object4 = context;
        context = null;
        Object object5 = ((IFn)const__0.getRawRoot()).invoke(((IFn)const__1.getRawRoot()).invoke(object, (Object)RT.mapUniqueKeys((Object[])objectArray)), object3, op, object4);
        Object object6 = op;
        op = null;
        ValStore this_ = null;
        return ((IFn)this_.retry_fn).invoke(object5, object6);
    }

    static {
        const__0 = RT.var((String)"datomic.core2.val-store.s3", (String)"wrap-metric-handler");
        const__1 = RT.var((String)"datomic.core2.aws.s3.sdkv1", (String)"wrap-ex-handler");
        const__2 = RT.keyword((String)"datomic.core2.val-store.s3.sdkv1", (String)"bucket");
        const__3 = RT.keyword((String)"datomic.core2.val-store.s3.sdkv1", (String)"prefix");
        const__4 = RT.keyword((String)"datomic.core2.val-store.s3.sdkv1", (String)"k");
        const__5 = RT.var((String)"datomic.core2.val-store.s3", (String)"storage-key");
        const__6 = RT.var((String)"datomic.core2.aws.s3.sdkv1", (String)"get-bytes");
        const__8 = RT.keyword(null, (String)"val");
        const__9 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"val"), null});
        const__10 = RT.keyword(null, (String)"default");
        const__11 = RT.var((String)"cognitect.caster", (String)"metric*");
        const__12 = RT.var((String)"cognitect.caster", (String)"instance");
        const__13 = RT.keyword(null, (String)"name");
        const__14 = RT.keyword(null, (String)"s3.get.bytes");
        const__15 = RT.keyword(null, (String)"value");
        const__17 = RT.keyword(null, (String)"units");
        const__18 = RT.keyword(null, (String)"count");
        const__19 = RT.var((String)"clojure.core", (String)"seq?");
        const__20 = RT.var((String)"clojure.core", (String)"next");
        const__21 = RT.var((String)"clojure.core", (String)"to-array");
        const__22 = RT.var((String)"clojure.core", (String)"seq");
        const__23 = RT.var((String)"clojure.core", (String)"first");
        const__25 = RT.var((String)"datomic.core2.val-store.spi", (String)"no-val-error");
        const__26 = RT.var((String)"datomic.core2.aws.s3.sdkv1", (String)"put-object");
        const__27 = RT.keyword(null, (String)"s3.put.bytes");
        const__30 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"result"), RT.keyword(null, (String)"created")});
        const__31 = RT.var((String)"datomic.core2.aws.s3.sdkv1", (String)"delete-object");
        const__33 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"result"), RT.keyword(null, (String)"deleted")});
        const__34 = RT.var((String)"datomic.core2.thread", (String)"pfuture-ch");
        const__35 = RT.var((String)"datomic.core2.val-store.s3.sdkv1", (String)"-wrap-op");
        const__36 = RT.keyword(null, (String)"create");
        const__37 = RT.keyword(null, (String)"content-length");
        const__38 = RT.var((String)"datomic.measure.io-stats", (String)"inc!");
        const__39 = RT.keyword(null, (String)"s3");
        const__40 = RT.keyword(null, (String)"get");
        const__41 = RT.keyword(null, (String)"delete");
        __site__0__ = new KeywordLookupSite(RT.keyword(null, (String)"val"));
        __thunk__0__ = __site__0__;
    }
}

