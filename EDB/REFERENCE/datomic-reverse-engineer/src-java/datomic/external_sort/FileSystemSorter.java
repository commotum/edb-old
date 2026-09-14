/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.IFn
 *  clojure.lang.IObj
 *  clojure.lang.IPersistentVector
 *  clojure.lang.IType
 *  clojure.lang.Indexed
 *  clojure.lang.RT
 *  clojure.lang.Symbol
 *  clojure.lang.Util
 *  clojure.lang.Var
 */
package datomic.external_sort;

import clojure.lang.IFn;
import clojure.lang.IObj;
import clojure.lang.IPersistentVector;
import clojure.lang.IType;
import clojure.lang.Indexed;
import clojure.lang.RT;
import clojure.lang.Symbol;
import clojure.lang.Util;
import clojure.lang.Var;
import datomic.external_sort.ExternalSort;
import datomic.external_sort.FileSystemSorter$fn__14469;
import datomic.external_sort.IO;
import java.io.InputStream;

public final class FileSystemSorter
implements ExternalSort,
IType {
    public final Object pool;
    public final Object cmp;
    public final Object io;
    public final Object file_iter_fn;
    public final Object create_file_writer_fn;
    public final Object prog_fn;
    Object files;
    private static Class __cached_class__0;
    private static Class __cached_class__1;
    private static Class __cached_class__2;
    private static Class __cached_class__3;
    public static final Var const__0;
    public static final Var const__1;
    public static final Object const__5;
    public static final Var const__6;
    public static final Var const__7;
    public static final Var const__8;
    public static final Var const__11;
    public static final Var const__12;
    public static final Var const__13;
    public static final Var const__15;
    public static final Var const__16;
    public static final Var const__17;
    public static final Object const__19;
    public static final Var const__20;
    public static final Var const__21;
    public static final Var const__22;
    public static final Var const__23;

    public FileSystemSorter(Object object, Object object2, Object object3, Object object4, Object object5, Object object6, Object object7) {
        this.pool = object;
        this.cmp = object2;
        this.io = object3;
        this.file_iter_fn = object4;
        this.create_file_writer_fn = object5;
        this.prog_fn = object6;
        this.files = object7;
    }

    public static IPersistentVector getBasis() {
        return RT.vector((Object[])new Object[]{Symbol.intern(null, (String)"pool"), Symbol.intern(null, (String)"cmp"), Symbol.intern(null, (String)"io"), Symbol.intern(null, (String)"file-iter-fn"), Symbol.intern(null, (String)"create-file-writer-fn"), Symbol.intern(null, (String)"prog-fn"), ((IObj)Symbol.intern(null, (String)"files")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"unsynchronized-mutable"), Boolean.TRUE}))});
    }

    public Object merge_step() {
        FileSystemSorter$fn__14469 fileSystemSorter$fn__14469 = new FileSystemSorter$fn__14469(this_.prog_fn, this_.create_file_writer_fn, this_.io, this_);
        FileSystemSorter this_ = null;
        return ((IFn)const__22.getRawRoot()).invoke(this_.pool, (Object)fileSystemSorter$fn__14469, ((IFn)const__23.getRawRoot()).invoke(const__19, this_.files));
    }

    /*
     * Unable to fully structure code
     */
    public Object consume_iter(Object handler) {
        while (true) {
            block6: {
                block8: {
                    block7: {
                        if ((long)RT.count((Object)this.files) > 4L) break block6;
                        v0 = this;
                        if (Util.classOf((Object)v0) == FileSystemSorter.__cached_class__2) break block7;
                        if (v0 instanceof ExternalSort) break block8;
                        v0 = v0;
                        FileSystemSorter.__cached_class__2 = Util.classOf((Object)v0);
                    }
                    this = null;
                    v1 = FileSystemSorter.const__20.getRawRoot().invoke((Object)v0, this.files, handler);
                    break;
                }
                v1 = ((ExternalSort)v0).consume_files_iter(this.files, handler);
                break;
            }
            v2 = this;
            if (Util.classOf((Object)v2) == FileSystemSorter.__cached_class__3) ** GOTO lbl21
            if (!(v2 instanceof ExternalSort)) {
                v2 = v2;
                FileSystemSorter.__cached_class__3 = Util.classOf((Object)v2);
lbl21:
                // 2 sources

                v3 = FileSystemSorter.const__21.getRawRoot().invoke((Object)v2);
            } else {
                v3 = ((ExternalSort)v2).merge_step();
            }
            this.files = v3;
        }
        return v1;
    }

    /*
     * Unable to fully structure code
     */
    public Object consume_files_iter(Object infiles, Object handler) {
        istreams = ((IFn)FileSystemSorter.const__0.getRawRoot()).invoke(FileSystemSorter.const__1.getRawRoot(), infiles);
        iters = ((IFn)FileSystemSorter.const__0.getRawRoot()).invoke(this.file_iter_fn, istreams);
        try {
            G__14460 = RT.count((Object)iters);
            switch (G__14460) {
                case 0: {
                    v0 = handler;
                    handler = null;
                    v1 = ((IFn)v0).invoke(null);
                    break;
                }
                case 1: {
                    v2 = handler;
                    handler = null;
                    v3 = iters;
                    iters = null;
                    v1 = ((IFn)v2).invoke(((IFn)v3).invoke(FileSystemSorter.const__5));
                    break;
                }
                default: {
                    v4 = handler;
                    handler = null;
                    v5 = iters;
                    iters = null;
                    v1 = ((IFn)v4).invoke(((IFn)FileSystemSorter.const__6.getRawRoot()).invoke(FileSystemSorter.const__7.getRawRoot(), this.cmp, v5));
                }
            }
            var6_8 = v1;
            v6 = istreams;
            istreams = null;
        }
        catch (Throwable var15_21) {
            v38 = istreams;
            istreams = null;
            seq_14461 = ((IFn)FileSystemSorter.const__8.getRawRoot()).invoke(v38);
            chunk_14462 = null;
            count_14463 = 0L;
            i_14464 = 0L;
            while (true) {
                if (i_14464 < count_14463) {
                    v39 = i = ((Indexed)chunk_14462).nth(RT.intCast((long)i_14464));
                    i = null;
                    ((InputStream)v39).close();
                    v40 = seq_14461;
                    seq_14461 = null;
                    v41 = chunk_14462;
                    chunk_14462 = null;
                    ++i_14464;
                    chunk_14462 = v41;
                    seq_14461 = v40;
                    continue;
                }
                v42 = seq_14461;
                seq_14461 = null;
                v43 = temp__5457__auto__14483 = ((IFn)FileSystemSorter.const__8.getRawRoot()).invoke(v42);
                if (v43 == null || v43 == Boolean.FALSE) break;
                v44 = temp__5457__auto__14483;
                temp__5457__auto__14483 = null;
                seq_14461 = v44;
                v45 = ((IFn)FileSystemSorter.const__11.getRawRoot()).invoke(seq_14461);
                if (v45 != null && v45 != Boolean.FALSE) {
                    c__5719__auto__14482 = ((IFn)FileSystemSorter.const__12.getRawRoot()).invoke(seq_14461);
                    v46 = seq_14461;
                    seq_14461 = null;
                    v47 = c__5719__auto__14482;
                    v48 = c__5719__auto__14482;
                    c__5719__auto__14482 = null;
                    i_14464 = RT.intCast((long)0L);
                    count_14463 = RT.intCast((int)RT.count((Object)v48));
                    chunk_14462 = v47;
                    seq_14461 = ((IFn)FileSystemSorter.const__13.getRawRoot()).invoke(v46);
                    continue;
                }
                v49 = i = ((IFn)FileSystemSorter.const__15.getRawRoot()).invoke(seq_14461);
                i = null;
                ((InputStream)v49).close();
                v50 = seq_14461;
                seq_14461 = null;
                i_14464 = 0L;
                count_14463 = 0L;
                chunk_14462 = null;
                seq_14461 = ((IFn)FileSystemSorter.const__16.getRawRoot()).invoke(v50);
            }
            v51 = infiles;
            infiles = null;
            seq_14465 = ((IFn)FileSystemSorter.const__8.getRawRoot()).invoke(v51);
            chunk_14466 = null;
            count_14467 = 0L;
            i_14468 = 0L;
            while (true) {
                block26: {
                    if (i_14468 >= count_14467) break block26;
                    f = ((Indexed)chunk_14466).nth(RT.intCast((long)i_14468));
                    v52 = this.io;
                    if (Util.classOf((Object)v52) == FileSystemSorter.__cached_class__0) ** GOTO lbl215
                    if (!(v52 instanceof IO)) {
                        v52 = v52;
                        FileSystemSorter.__cached_class__0 = Util.classOf((Object)v52);
lbl215:
                        // 2 sources

                        v53 = f;
                        f = null;
                        v54 = FileSystemSorter.const__17.getRawRoot().invoke(v52, v53);
                    } else {
                        v55 = f;
                        f = null;
                        v54 = ((IO)v52).delete_temp_file(v55);
                    }
                    v56 = seq_14465;
                    seq_14465 = null;
                    v57 = chunk_14466;
                    chunk_14466 = null;
                    ++i_14468;
                    chunk_14466 = v57;
                    seq_14465 = v56;
                    continue;
                }
                v58 = seq_14465;
                seq_14465 = null;
                v59 = temp__5457__auto__14485 = ((IFn)FileSystemSorter.const__8.getRawRoot()).invoke(v58);
                if (v59 == null || v59 == Boolean.FALSE) break;
                v60 = temp__5457__auto__14485;
                temp__5457__auto__14485 = null;
                seq_14465 = v60;
                v61 = ((IFn)FileSystemSorter.const__11.getRawRoot()).invoke(seq_14465);
                if (v61 != null && v61 != Boolean.FALSE) {
                    c__5719__auto__14484 = ((IFn)FileSystemSorter.const__12.getRawRoot()).invoke(seq_14465);
                    v62 = seq_14465;
                    seq_14465 = null;
                    v63 = c__5719__auto__14484;
                    v64 = c__5719__auto__14484;
                    c__5719__auto__14484 = null;
                    i_14468 = RT.intCast((long)0L);
                    count_14467 = RT.intCast((int)RT.count((Object)v64));
                    chunk_14466 = v63;
                    seq_14465 = ((IFn)FileSystemSorter.const__13.getRawRoot()).invoke(v62);
                    continue;
                }
                f = ((IFn)FileSystemSorter.const__15.getRawRoot()).invoke(seq_14465);
                v65 = this.io;
                if (Util.classOf((Object)v65) == FileSystemSorter.__cached_class__1) ** GOTO lbl257
                if (!(v65 instanceof IO)) {
                    v65 = v65;
                    FileSystemSorter.__cached_class__1 = Util.classOf((Object)v65);
lbl257:
                    // 2 sources

                    v66 = f;
                    f = null;
                    v67 = FileSystemSorter.const__17.getRawRoot().invoke(v65, v66);
                } else {
                    v68 = f;
                    f = null;
                    v67 = ((IO)v65).delete_temp_file(v68);
                }
                v69 = seq_14465;
                seq_14465 = null;
                i_14468 = 0L;
                count_14467 = 0L;
                chunk_14466 = null;
                seq_14465 = ((IFn)FileSystemSorter.const__16.getRawRoot()).invoke(v69);
            }
            throw var15_21;
        }
        seq_14461 = ((IFn)FileSystemSorter.const__8.getRawRoot()).invoke(v6);
        chunk_14462 = null;
        count_14463 = 0L;
        i_14464 = 0L;
        while (true) {
            if (i_14464 < count_14463) {
                v7 = i = ((Indexed)chunk_14462).nth(RT.intCast((long)i_14464));
                i = null;
                ((InputStream)v7).close();
                v8 = seq_14461;
                seq_14461 = null;
                v9 = chunk_14462;
                chunk_14462 = null;
                ++i_14464;
                chunk_14462 = v9;
                seq_14461 = v8;
                continue;
            }
            v10 = seq_14461;
            seq_14461 = null;
            v11 = temp__5457__auto__14479 = ((IFn)FileSystemSorter.const__8.getRawRoot()).invoke(v10);
            if (v11 == null || v11 == Boolean.FALSE) break;
            v12 = temp__5457__auto__14479;
            temp__5457__auto__14479 = null;
            seq_14461 = v12;
            v13 = ((IFn)FileSystemSorter.const__11.getRawRoot()).invoke(seq_14461);
            if (v13 != null && v13 != Boolean.FALSE) {
                c__5719__auto__14478 = ((IFn)FileSystemSorter.const__12.getRawRoot()).invoke(seq_14461);
                v14 = seq_14461;
                seq_14461 = null;
                v15 = c__5719__auto__14478;
                v16 = c__5719__auto__14478;
                c__5719__auto__14478 = null;
                i_14464 = RT.intCast((long)0L);
                count_14463 = RT.intCast((int)RT.count((Object)v16));
                chunk_14462 = v15;
                seq_14461 = ((IFn)FileSystemSorter.const__13.getRawRoot()).invoke(v14);
                continue;
            }
            v17 = i = ((IFn)FileSystemSorter.const__15.getRawRoot()).invoke(seq_14461);
            i = null;
            ((InputStream)v17).close();
            v18 = seq_14461;
            seq_14461 = null;
            i_14464 = 0L;
            count_14463 = 0L;
            chunk_14462 = null;
            seq_14461 = ((IFn)FileSystemSorter.const__16.getRawRoot()).invoke(v18);
        }
        v19 = infiles;
        infiles = null;
        seq_14465 = ((IFn)FileSystemSorter.const__8.getRawRoot()).invoke(v19);
        chunk_14466 = null;
        count_14467 = 0L;
        i_14468 = 0L;
        while (true) {
            block24: {
                if (i_14468 >= count_14467) break block24;
                f = ((Indexed)chunk_14466).nth(RT.intCast((long)i_14468));
                v20 = this.io;
                if (Util.classOf((Object)v20) == FileSystemSorter.__cached_class__0) ** GOTO lbl91
                if (!(v20 instanceof IO)) {
                    v20 = v20;
                    FileSystemSorter.__cached_class__0 = Util.classOf((Object)v20);
lbl91:
                    // 2 sources

                    v21 = f;
                    f = null;
                    v22 = FileSystemSorter.const__17.getRawRoot().invoke(v20, v21);
                } else {
                    v23 = f;
                    f = null;
                    v22 = ((IO)v20).delete_temp_file(v23);
                }
                v24 = seq_14465;
                seq_14465 = null;
                v25 = chunk_14466;
                chunk_14466 = null;
                ++i_14468;
                chunk_14466 = v25;
                seq_14465 = v24;
                continue;
            }
            v26 = seq_14465;
            seq_14465 = null;
            v27 = temp__5457__auto__14481 = ((IFn)FileSystemSorter.const__8.getRawRoot()).invoke(v26);
            if (v27 == null || v27 == Boolean.FALSE) break;
            v28 = temp__5457__auto__14481;
            temp__5457__auto__14481 = null;
            seq_14465 = v28;
            v29 = ((IFn)FileSystemSorter.const__11.getRawRoot()).invoke(seq_14465);
            if (v29 != null && v29 != Boolean.FALSE) {
                c__5719__auto__14480 = ((IFn)FileSystemSorter.const__12.getRawRoot()).invoke(seq_14465);
                v30 = seq_14465;
                seq_14465 = null;
                v31 = c__5719__auto__14480;
                v32 = c__5719__auto__14480;
                c__5719__auto__14480 = null;
                i_14468 = RT.intCast((long)0L);
                count_14467 = RT.intCast((int)RT.count((Object)v32));
                chunk_14466 = v31;
                seq_14465 = ((IFn)FileSystemSorter.const__13.getRawRoot()).invoke(v30);
                continue;
            }
            f = ((IFn)FileSystemSorter.const__15.getRawRoot()).invoke(seq_14465);
            v33 = this.io;
            if (Util.classOf((Object)v33) == FileSystemSorter.__cached_class__1) ** GOTO lbl133
            if (!(v33 instanceof IO)) {
                v33 = v33;
                FileSystemSorter.__cached_class__1 = Util.classOf((Object)v33);
lbl133:
                // 2 sources

                v34 = f;
                f = null;
                v35 = FileSystemSorter.const__17.getRawRoot().invoke(v33, v34);
            } else {
                v36 = f;
                f = null;
                v35 = ((IO)v33).delete_temp_file(v36);
            }
            v37 = seq_14465;
            seq_14465 = null;
            i_14468 = 0L;
            count_14467 = 0L;
            chunk_14466 = null;
            seq_14465 = ((IFn)FileSystemSorter.const__16.getRawRoot()).invoke(v37);
        }
        return var6_8;
    }

    static {
        const__0 = RT.var((String)"clojure.core", (String)"mapv");
        const__1 = RT.var((String)"clojure.java.io", (String)"input-stream");
        const__5 = 0L;
        const__6 = RT.var((String)"clojure.core", (String)"apply");
        const__7 = RT.var((String)"datomic.iter", (String)"merge-iters");
        const__8 = RT.var((String)"clojure.core", (String)"seq");
        const__11 = RT.var((String)"clojure.core", (String)"chunked-seq?");
        const__12 = RT.var((String)"clojure.core", (String)"chunk-first");
        const__13 = RT.var((String)"clojure.core", (String)"chunk-rest");
        const__15 = RT.var((String)"clojure.core", (String)"first");
        const__16 = RT.var((String)"clojure.core", (String)"next");
        const__17 = RT.var((String)"datomic.external-sort", (String)"delete-temp-file");
        const__19 = 4L;
        const__20 = RT.var((String)"datomic.external-sort", (String)"consume-files-iter");
        const__21 = RT.var((String)"datomic.external-sort", (String)"merge-step");
        const__22 = RT.var((String)"datomic.common", (String)"pooled-mapv");
        const__23 = RT.var((String)"clojure.core", (String)"partition-all");
    }
}

