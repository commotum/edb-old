/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.Keyword
 *  clojure.lang.RT
 *  clojure.lang.Util
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Keyword;
import clojure.lang.RT;
import clojure.lang.Util;
import clojure.lang.Var;
import datomic.external_sort$file_system_sorter$fn__14493$fn__14494$fn__14495;
import datomic.external_sort.IO;
import java.io.OutputStream;

public final class external_sort$file_system_sorter$fn__14493$fn__14494
extends AFunction {
    Object io;
    Object create_file_writer_fn;
    Object os;
    Object f;
    Object cmp;
    Object chunk;
    Object prog_fn;
    private static Class __cached_class__0;
    public static final Keyword const__0;
    public static final Keyword const__1;
    public static final Var const__3;
    public static final Var const__4;
    public static final Keyword const__5;
    public static final Keyword const__6;
    public static final Var const__7;

    public external_sort$file_system_sorter$fn__14493$fn__14494(Object object, Object object2, Object object3, Object object4, Object object5, Object object6, Object object7) {
        this.io = object;
        this.create_file_writer_fn = object2;
        this.os = object3;
        this.f = object4;
        this.cmp = object5;
        this.chunk = object6;
        this.prog_fn = object7;
    }

    /*
     * Unable to fully structure code
     */
    public Object invoke() {
        try {
            this.create_file_writer_fn = null;
            write = ((IFn)this.create_file_writer_fn).invoke(this.os);
            ((IFn)this.prog_fn).invoke((Object)RT.mapUniqueKeys((Object[])new Object[]{external_sort$file_system_sorter$fn__14493$fn__14494.const__0, this.f, external_sort$file_system_sorter$fn__14493$fn__14494.const__1, RT.count((Object)this.chunk)}));
            v0 = (IFn)external_sort$file_system_sorter$fn__14493$fn__14494.const__3.getRawRoot();
            v1 = write;
            write = null;
            v2 = new external_sort$file_system_sorter$fn__14493$fn__14494$fn__14495(v1);
            v3 = this.cmp;
            if (v3 != null && v3 != Boolean.FALSE) {
                this.cmp = null;
                v4 = ((IFn)external_sort$file_system_sorter$fn__14493$fn__14494.const__4.getRawRoot()).invoke(this.cmp, this.chunk);
            } else {
                v4 = ((IFn)external_sort$file_system_sorter$fn__14493$fn__14494.const__4.getRawRoot()).invoke(this.chunk);
            }
            v0.invoke((Object)v2, null, v4);
            this.prog_fn = null;
            v5 = (IFn)this.prog_fn;
            v6 = new Object[6];
            v6[0] = external_sort$file_system_sorter$fn__14493$fn__14494.const__5;
            v6[1] = this.f;
            v6[2] = external_sort$file_system_sorter$fn__14493$fn__14494.const__1;
            this.chunk = null;
            v6[3] = RT.count((Object)this.chunk);
            v6[4] = external_sort$file_system_sorter$fn__14493$fn__14494.const__6;
            v7 = this.io;
            this.io = null;
            v8 = v7;
            if (Util.classOf((Object)v7) == external_sort$file_system_sorter$fn__14493$fn__14494.__cached_class__0) ** GOTO lbl34
            if (!(v8 instanceof IO)) {
                v8 = v8;
                external_sort$file_system_sorter$fn__14493$fn__14494.__cached_class__0 = Util.classOf((Object)v8);
lbl34:
                // 2 sources

                this.f = null;
                v9 = external_sort$file_system_sorter$fn__14493$fn__14494.const__7.getRawRoot().invoke(v8, this.f);
            } else {
                this.f = null;
                v9 = ((IO)v8).temp_file_size(this.f);
            }
            v6[5] = v9;
            var2_2 = v5.invoke((Object)RT.mapUniqueKeys((Object[])v6));
        }
        finally {
            this.os = null;
            ((OutputStream)this.os).close();
        }
        return var2_2;
    }

    static {
        const__0 = RT.keyword(null, (String)"before");
        const__1 = RT.keyword(null, (String)"count");
        const__3 = RT.var((String)"clojure.core", (String)"reduce");
        const__4 = RT.var((String)"clojure.core", (String)"sort");
        const__5 = RT.keyword(null, (String)"after");
        const__6 = RT.keyword(null, (String)"size");
        const__7 = RT.var((String)"datomic.external-sort", (String)"temp-file-size");
    }
}

