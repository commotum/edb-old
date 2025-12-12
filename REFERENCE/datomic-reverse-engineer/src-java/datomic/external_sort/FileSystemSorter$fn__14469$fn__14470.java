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
package datomic.external_sort;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Keyword;
import clojure.lang.RT;
import clojure.lang.Util;
import clojure.lang.Var;
import datomic.external_sort.ExternalSort;
import datomic.external_sort.FileSystemSorter$fn__14469$fn__14470$fn__14471;
import datomic.external_sort.IO;
import java.io.OutputStream;

/*
 * Illegal identifiers - consider using --renameillegalidents true
 */
public final class FileSystemSorter$fn__14469$fn__14470
extends AFunction {
    Object prog_fn;
    Object create_file_writer_fn;
    Object outfile;
    Object io;
    Object infiles;
    Object this;
    Object os;
    private static Class __cached_class__0;
    private static Class __cached_class__1;
    public static final Keyword const__0;
    public static final Var const__1;
    public static final Keyword const__2;
    public static final Keyword const__3;
    public static final Var const__4;

    public FileSystemSorter$fn__14469$fn__14470(Object object, Object object2, Object object3, Object object4, Object object5, Object object6, Object object7) {
        this.prog_fn = object;
        this.create_file_writer_fn = object2;
        this.outfile = object3;
        this.io = object4;
        this.infiles = object5;
        this.this = object6;
        this.os = object7;
    }

    /*
     * Unable to fully structure code
     */
    public Object invoke() {
        try {
            write = ((IFn)this.create_file_writer_fn).invoke(this.os);
            ((IFn)this.prog_fn).invoke((Object)RT.mapUniqueKeys((Object[])new Object[]{FileSystemSorter$fn__14469$fn__14470.const__0, this.outfile}));
            v0 = this.this;
            if (Util.classOf((Object)v0) == FileSystemSorter$fn__14469$fn__14470.__cached_class__0) ** GOTO lbl10
            if (!(v0 instanceof ExternalSort)) {
                v0 = v0;
                FileSystemSorter$fn__14469$fn__14470.__cached_class__0 = Util.classOf((Object)v0);
lbl10:
                // 2 sources

                this.infiles = null;
                v1 = write;
                write = null;
                v2 = FileSystemSorter$fn__14469$fn__14470.const__1.getRawRoot().invoke(v0, this.infiles, (Object)new FileSystemSorter$fn__14469$fn__14470$fn__14471(v1));
            } else {
                this.infiles = null;
                v3 = write;
                write = null;
                v2 = ((ExternalSort)v0).consume_files_iter(this.infiles, (Object)new FileSystemSorter$fn__14469$fn__14470$fn__14471(v3));
            }
            v4 = (IFn)this.prog_fn;
            v5 = new Object[4];
            v5[0] = FileSystemSorter$fn__14469$fn__14470.const__2;
            v5[1] = this.outfile;
            v5[2] = FileSystemSorter$fn__14469$fn__14470.const__3;
            v6 = this.io;
            if (Util.classOf((Object)v6) == FileSystemSorter$fn__14469$fn__14470.__cached_class__1) ** GOTO lbl29
            if (!(v6 instanceof IO)) {
                v6 = v6;
                FileSystemSorter$fn__14469$fn__14470.__cached_class__1 = Util.classOf((Object)v6);
lbl29:
                // 2 sources

                this.outfile = null;
                v7 = FileSystemSorter$fn__14469$fn__14470.const__4.getRawRoot().invoke(v6, this.outfile);
            } else {
                this.outfile = null;
                v7 = ((IO)v6).temp_file_size(this.outfile);
            }
            v5[3] = v7;
            var2_2 = v4.invoke((Object)RT.mapUniqueKeys((Object[])v5));
        }
        finally {
            this.os = null;
            ((OutputStream)this.os).close();
        }
        return var2_2;
    }

    static {
        const__0 = RT.keyword(null, (String)"before");
        const__1 = RT.var((String)"datomic.external-sort", (String)"consume-files-iter");
        const__2 = RT.keyword(null, (String)"after");
        const__3 = RT.keyword(null, (String)"size");
        const__4 = RT.var((String)"datomic.external-sort", (String)"temp-file-size");
    }
}

