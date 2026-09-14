/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFn
 *  clojure.lang.AFunction
 *  clojure.lang.RT
 *  clojure.lang.Util
 *  clojure.lang.Var
 */
package datomic.core2.val_store.fs;

import clojure.lang.AFn;
import clojure.lang.AFunction;
import clojure.lang.RT;
import clojure.lang.Util;
import clojure.lang.Var;
import datomic.core2.val_store.fs.Impl;
import java.io.File;

/*
 * Illegal identifiers - consider using --renameillegalidents true
 */
public final class FS$fn__21329
extends AFunction {
    Object this;
    Object opts;
    private static Class __cached_class__0;
    public static final Var const__0;
    public static final AFn const__3;

    public FS$fn__21329(Object object, Object object2) {
        this.this = object;
        this.opts = object2;
    }

    /*
     * Unable to fully structure code
     */
    public Object invoke(Object p1__21321_SHARP_) {
        v0 = this.this;
        if (Util.classOf((Object)v0) == FS$fn__21329.__cached_class__0) ** GOTO lbl6
        if (!(v0 instanceof Impl)) {
            v0 = v0;
            FS$fn__21329.__cached_class__0 = Util.classOf((Object)v0);
lbl6:
            // 2 sources

            v1 = p1__21321_SHARP_;
            p1__21321_SHARP_ = null;
            v2 = FS$fn__21329.const__0.getRawRoot().invoke(v0, v1, this.opts);
        } else {
            v3 = p1__21321_SHARP_;
            p1__21321_SHARP_ = null;
            v2 = ((Impl)v0)._file_path(v3, this.opts);
        }
        v4 = f = v2;
        f = null;
        ((File)v4).delete();
        return FS$fn__21329.const__3;
    }

    static {
        const__0 = RT.var((String)"datomic.core2.val-store.fs", (String)"-file-path");
        const__3 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"result"), RT.keyword(null, (String)"deleted")});
    }
}

