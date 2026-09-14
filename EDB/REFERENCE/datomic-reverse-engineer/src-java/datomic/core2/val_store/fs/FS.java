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
 *  clojure.lang.Tuple
 *  clojure.lang.Util
 *  clojure.lang.Var
 */
package datomic.core2.val_store.fs;

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
import clojure.lang.Tuple;
import clojure.lang.Util;
import clojure.lang.Var;
import datomic.core2.val_store.fs.FS$doit__21325;
import datomic.core2.val_store.fs.FS$fn__21327;
import datomic.core2.val_store.fs.FS$fn__21329;
import datomic.core2.val_store.fs.FS$fn__21331;
import datomic.core2.val_store.fs.FS$fn__21333;
import datomic.core2.val_store.fs.Impl;
import datomic.core2.val_store.spi.Delete;
import datomic.core2.val_store.spi.Get;
import datomic.core2.val_store.spi.Put;
import java.io.File;
import java.nio.channels.FileChannel;
import java.nio.channels.spi.AbstractInterruptibleChannel;
import java.nio.file.OpenOption;
import java.util.concurrent.RejectedExecutionException;

public final class FS
implements Get,
Impl,
Delete,
Put,
IType {
    public final Object get_pool;
    public final Object put_pool;
    public final Object path;
    public final Object delete_pool;
    private static Class __cached_class__0;
    private static Class __cached_class__1;
    public static final Keyword const__1;
    public static final Var const__3;
    public static final Var const__4;
    public static final Var const__5;
    public static final Var const__6;
    public static final Var const__7;
    public static final Var const__9;
    public static final Keyword const__10;
    public static final Var const__11;
    public static final AFn const__12;
    public static final Var const__13;
    public static final AFn const__16;
    public static final Var const__17;
    public static final Var const__18;
    public static final Keyword const__19;
    public static final Var const__20;
    public static final Keyword const__21;
    public static final Keyword const__22;
    public static final Keyword const__23;
    public static final Var const__24;
    public static final Object const__25;
    public static final Var const__26;
    public static final AFn const__30;
    public static final Var const__31;
    static final KeywordLookupSite __site__0__;
    static ILookupThunk __thunk__0__;

    public FS(Object object, Object object2, Object object3, Object object4) {
        this.get_pool = object;
        this.put_pool = object2;
        this.path = object3;
        this.delete_pool = object4;
    }

    public static IPersistentVector getBasis() {
        return Tuple.create((Object)((IObj)Symbol.intern(null, (String)"get-pool")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), Symbol.intern(null, (String)"ExecutorService")})), (Object)((IObj)Symbol.intern(null, (String)"put-pool")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), Symbol.intern(null, (String)"ExecutorService")})), (Object)Symbol.intern(null, (String)"path"), (Object)((IObj)Symbol.intern(null, (String)"delete-pool")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), Symbol.intern(null, (String)"ExecutorService")})));
    }

    @Override
    public Object _put(Object k, Object v, Object opts) {
        Object object;
        try {
            Object object2 = opts;
            opts = null;
            Object object3 = v;
            v = null;
            Object object4 = k;
            k = null;
            object = ((IFn)const__17.getRawRoot()).invoke(((IFn)const__18.getRawRoot()).invoke((Object)new FS$fn__21333(this, object2, object3), object4, (Object)const__23), this.put_pool);
        }
        catch (RejectedExecutionException _) {
            Object G__21335 = ((IFn)const__24.getRawRoot()).invoke(const__25);
            ((IFn)const__26.getRawRoot()).invoke(G__21335, (Object)const__30);
            ((IFn)const__31.getRawRoot()).invoke(G__21335);
            Object object5 = G__21335;
            G__21335 = null;
            object = object5;
        }
        return object;
    }

    @Override
    public Object _get(Object k, Object opts) {
        ((IFn)const__20.getRawRoot()).invoke((Object)const__21);
        Object object = opts;
        opts = null;
        FS$fn__21331 fS$fn__21331 = new FS$fn__21331(this_, object);
        Object object2 = k;
        k = null;
        FS this_ = null;
        return ((IFn)const__17.getRawRoot()).invoke(((IFn)const__18.getRawRoot()).invoke((Object)fS$fn__21331, object2, (Object)const__22), this_.get_pool);
    }

    @Override
    public Object _delete(Object k, Object opts) {
        Object object = opts;
        opts = null;
        FS$fn__21329 fS$fn__21329 = new FS$fn__21329(this_, object);
        Object object2 = k;
        k = null;
        FS this_ = null;
        return ((IFn)const__17.getRawRoot()).invoke(((IFn)const__18.getRawRoot()).invoke((Object)fS$fn__21329, object2, (Object)const__19), this_.delete_pool);
    }

    /*
     * Unable to fully structure code
     */
    @Override
    public Object _sync_put(Object k, Object v, Object opts) {
        block3: {
            block2: {
                v0 = or__5581__auto__21337 = ((IFn)FS.const__13.getRawRoot()).invoke(k, v);
                if (v0 == null || v0 == Boolean.FALSE) break block2;
                v1 = or__5581__auto__21337;
                or__5581__auto__21337 = null;
                break block3;
            }
            v2 = this;
            if (Util.classOf((Object)v2) == FS.__cached_class__1) ** GOTO lbl12
            if (!(v2 instanceof Impl)) {
                v2 = v2;
                FS.__cached_class__1 = Util.classOf((Object)v2);
lbl12:
                // 2 sources

                v3 = k;
                k = null;
                v4 = opts;
                opts = null;
                v5 = FS.const__6.getRawRoot().invoke((Object)v2, v3, v4);
            } else {
                v6 = k;
                k = null;
                v7 = opts;
                opts = null;
                v5 = ((Impl)v2)._file_path(v6, v7);
            }
            new_file = ((File)v5).toPath();
            v8 = v;
            v = null;
            doit = new FS$doit__21325(new_file, v8);
            v9 = new_file;
            new_file = null;
            v10 = doit;
            doit = null;
            ((IFn)new FS$fn__21327(v9, (Object)v10)).invoke();
            v1 = FS.const__16;
        }
        return v1;
    }

    /*
     * Unable to fully structure code
     */
    @Override
    public Object _sync_get(Object k, Object opts) {
        v0 = this;
        if (Util.classOf((Object)v0) == FS.__cached_class__0) ** GOTO lbl6
        if (!(v0 instanceof Impl)) {
            v0 = v0;
            FS.__cached_class__0 = Util.classOf((Object)v0);
lbl6:
            // 2 sources

            v1 = k;
            k = null;
            v2 = opts;
            opts = null;
            v3 = FS.const__6.getRawRoot().invoke((Object)v0, v1, v2);
        } else {
            v4 = k;
            k = null;
            v5 = opts;
            opts = null;
            v3 = ((Impl)v0)._file_path(v4, v5);
        }
        f = v3;
        len = ((File)f).length();
        v6 = ((IFn)FS.const__7.getRawRoot()).invoke((Object)(Numbers.isZero((long)len) != false ? Boolean.TRUE : Boolean.FALSE));
        if (v6 != null && v6 != Boolean.FALSE) {
            v7 = f;
            f = null;
            fc = FileChannel.open(((File)v7).toPath(), (OpenOption[])FS.const__9.getRawRoot());
            try {
                var7_6 = RT.mapUniqueKeys((Object[])new Object[]{FS.const__10, ((IFn)FS.const__11.getRawRoot()).invoke((Object)Numbers.num((long)len), (Object)fc)});
            }
            finally {
                v8 = fc;
                fc = null;
                ((AbstractInterruptibleChannel)v8).close();
            }
            v9 = var7_6;
        } else {
            v9 = FS.const__12;
        }
        return v9;
    }

    @Override
    public Object _file_path(Object k, Object opts) {
        Object object;
        FS this_;
        ILookupThunk iLookupThunk = __thunk__0__;
        Object object2 = opts;
        opts = null;
        Object object3 = iLookupThunk.get(object2);
        if (iLookupThunk == object3) {
            __thunk__0__ = __site__0__.fault(object2);
            object3 = __thunk__0__.get(object2);
        }
        if (Util.equiv((Object)const__1, (Object)object3)) {
            Object object4 = k;
            k = null;
            this_ = null;
            object = ((IFn)const__3.getRawRoot()).invoke(this_.path, object4);
        } else {
            Object object5 = k;
            Object object6 = k;
            k = null;
            this_ = null;
            object = ((IFn)const__3.getRawRoot()).invoke(this_.path, ((IFn)const__4.getRawRoot()).invoke(object5, ((IFn)const__5.getRawRoot()).invoke(object6)));
        }
        return object;
    }

    static {
        const__1 = RT.keyword(null, (String)"skip");
        const__3 = RT.var((String)"clojure.java.io", (String)"file");
        const__4 = RT.var((String)"datomic.core2.val-store.spi", (String)"splice-partition-key");
        const__5 = RT.var((String)"datomic.core2.val-store.spi", (String)"partition-key");
        const__6 = RT.var((String)"datomic.core2.val-store.fs", (String)"-file-path");
        const__7 = RT.var((String)"clojure.core", (String)"not");
        const__9 = RT.var((String)"datomic.core2.val-store.fs", (String)"SYNC_GET_READ_OPTION");
        const__10 = RT.keyword(null, (String)"val");
        const__11 = RT.var((String)"datomic.java.io.bbuf", (String)"read-n-bytes");
        const__12 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"val"), null});
        const__13 = RT.var((String)"datomic.core2.val-store.spi", (String)"no-val-error");
        const__16 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"result"), RT.keyword(null, (String)"created")});
        const__17 = RT.var((String)"datomic.core2.thread", (String)"pfuture-ch");
        const__18 = RT.var((String)"datomic.core2.val-store.fs", (String)"wrap-op");
        const__19 = RT.keyword(null, (String)"delete");
        const__20 = RT.var((String)"datomic.measure.io-stats", (String)"inc!");
        const__21 = RT.keyword(null, (String)"fs");
        const__22 = RT.keyword(null, (String)"get");
        const__23 = RT.keyword(null, (String)"put");
        const__24 = RT.var((String)"clojure.core.async", (String)"chan");
        const__25 = 1L;
        const__26 = RT.var((String)"clojure.core.async", (String)"offer!");
        const__30 = (AFn)RT.map((Object[])new Object[]{RT.keyword((String)"cognitect.anomalies", (String)"category"), RT.keyword((String)"cognitect.anomalies", (String)"busy"), RT.keyword((String)"cognitect.anomalies", (String)"message"), "EFS Write Queue Full"});
        const__31 = RT.var((String)"clojure.core.async", (String)"close!");
        __site__0__ = new KeywordLookupSite(RT.keyword((String)"datomic.core2.val-store.opts", (String)"partition"));
        __thunk__0__ = __site__0__;
    }
}

