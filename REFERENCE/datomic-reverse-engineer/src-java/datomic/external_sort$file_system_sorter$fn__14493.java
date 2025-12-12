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
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.RT;
import clojure.lang.Util;
import clojure.lang.Var;
import datomic.external_sort$file_system_sorter$fn__14493$fn__14494;
import datomic.external_sort.IO;

public final class external_sort$file_system_sorter$fn__14493
extends AFunction {
    Object io;
    Object create_file_writer_fn;
    Object cmp;
    Object prog_fn;
    private static Class __cached_class__0;
    public static final Var const__0;
    public static final Var const__1;

    public external_sort$file_system_sorter$fn__14493(Object object, Object object2, Object object3, Object object4) {
        this.io = object;
        this.create_file_writer_fn = object2;
        this.cmp = object3;
        this.prog_fn = object4;
    }

    /*
     * Unable to fully structure code
     */
    public Object invoke(Object chunk) {
        v0 = this.io;
        if (Util.classOf((Object)v0) == external_sort$file_system_sorter$fn__14493.__cached_class__0) ** GOTO lbl6
        if (!(v0 instanceof IO)) {
            v0 = v0;
            external_sort$file_system_sorter$fn__14493.__cached_class__0 = Util.classOf((Object)v0);
lbl6:
            // 2 sources

            v1 = external_sort$file_system_sorter$fn__14493.const__0.getRawRoot().invoke(v0);
        } else {
            v1 = ((IO)v0).make_temp_file();
        }
        f = v1;
        v2 = os = ((IFn)external_sort$file_system_sorter$fn__14493.const__1.getRawRoot()).invoke(f);
        os = null;
        v3 = chunk;
        chunk = null;
        ((IFn)new external_sort$file_system_sorter$fn__14493$fn__14494(this.io, this.create_file_writer_fn, v2, f, this.cmp, v3, this.prog_fn)).invoke();
        var2_2 = null;
        return f;
    }

    static {
        const__0 = RT.var((String)"datomic.external-sort", (String)"make-temp-file");
        const__1 = RT.var((String)"clojure.java.io", (String)"output-stream");
    }
}

