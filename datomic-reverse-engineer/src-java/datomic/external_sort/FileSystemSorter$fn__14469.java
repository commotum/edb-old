/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.RT
 *  clojure.lang.Util
 *  clojure.lang.Var
 */
package datomic.external_sort;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.RT;
import clojure.lang.Util;
import clojure.lang.Var;
import datomic.external_sort.FileSystemSorter$fn__14469$fn__14470;
import datomic.external_sort.IO;

/*
 * Illegal identifiers - consider using --renameillegalidents true
 */
public final class FileSystemSorter$fn__14469
extends AFunction {
    Object prog_fn;
    Object create_file_writer_fn;
    Object io;
    Object this;
    private static Class __cached_class__0;
    public static final Var const__0;
    public static final Var const__1;

    public FileSystemSorter$fn__14469(Object object, Object object2, Object object3, Object object4) {
        this.prog_fn = object;
        this.create_file_writer_fn = object2;
        this.io = object3;
        this.this = object4;
    }

    /*
     * Unable to fully structure code
     */
    public Object invoke(Object infiles) {
        v0 = this.io;
        if (Util.classOf((Object)v0) == FileSystemSorter$fn__14469.__cached_class__0) ** GOTO lbl6
        if (!(v0 instanceof IO)) {
            v0 = v0;
            FileSystemSorter$fn__14469.__cached_class__0 = Util.classOf((Object)v0);
lbl6:
            // 2 sources

            v1 = FileSystemSorter$fn__14469.const__0.getRawRoot().invoke(v0);
        } else {
            v1 = ((IO)v0).make_temp_file();
        }
        outfile = v1;
        os = ((IFn)FileSystemSorter$fn__14469.const__1.getRawRoot()).invoke(outfile);
        v2 = infiles;
        infiles = null;
        v3 = os;
        os = null;
        ((IFn)new FileSystemSorter$fn__14469$fn__14470(this.prog_fn, this.create_file_writer_fn, outfile, this.io, v2, this.this, v3)).invoke();
        var2_2 = null;
        return outfile;
    }

    static {
        const__0 = RT.var((String)"datomic.external-sort", (String)"make-temp-file");
        const__1 = RT.var((String)"clojure.java.io", (String)"output-stream");
    }
}

