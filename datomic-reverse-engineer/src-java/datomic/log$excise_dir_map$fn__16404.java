/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.RT
 *  clojure.lang.Util
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.RT;
import clojure.lang.Util;
import clojure.lang.Var;
import datomic.log.LogSeek;

public final class log$excise_dir_map$fn__16404
extends AFunction {
    Object log;
    private static Class __cached_class__0;
    public static final Var const__0;

    public log$excise_dir_map$fn__16404(Object object) {
        this.log = object;
    }

    /*
     * Enabled aggressive block sorting
     */
    public Object invoke(Object p1__16397_SHARP_) {
        Object object;
        Object object2 = this_.log;
        if (Util.classOf((Object)object2) != __cached_class__0) {
            if (object2 instanceof LogSeek) {
                Object object3 = p1__16397_SHARP_;
                p1__16397_SHARP_ = null;
                object = ((LogSeek)object2).seek_seg_path(object3);
                return object;
            }
            object2 = object2;
            __cached_class__0 = Util.classOf((Object)object2);
        }
        Object object4 = p1__16397_SHARP_;
        p1__16397_SHARP_ = null;
        log$excise_dir_map$fn__16404 this_ = null;
        object = const__0.getRawRoot().invoke(object2, object4);
        return object;
    }

    static {
        const__0 = RT.var((String)"datomic.log", (String)"seek-seg-path");
    }
}

