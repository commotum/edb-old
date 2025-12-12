/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 */
package datomic;

import clojure.lang.AFunction;
import datomic.external_sort.FileSystemSorter;

public final class external_sort$fn__14459$__GT_FileSystemSorter__14486
extends AFunction {
    public Object invoke(Object pool, Object cmp, Object io2, Object file_iter_fn, Object create_file_writer_fn, Object prog_fn, Object files2) {
        Object object = pool;
        pool = null;
        Object object2 = cmp;
        cmp = null;
        Object object3 = io2;
        io2 = null;
        Object object4 = file_iter_fn;
        file_iter_fn = null;
        Object object5 = create_file_writer_fn;
        create_file_writer_fn = null;
        Object object6 = prog_fn;
        prog_fn = null;
        Object object7 = files2;
        files2 = null;
        return new FileSystemSorter(object, object2, object3, object4, object5, object6, object7);
    }
}

