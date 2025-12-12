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

public final class log$seek_seg_id
extends AFunction {
    private static Class __cached_class__0;
    public static final Var const__0;

    /*
     * Unable to fully structure code
     */
    public static Object invokeStatic(Object log, Object t) {
        v0 = log;
        log = null;
        v1 = v0;
        if (Util.classOf((Object)v0) == log$seek_seg_id.__cached_class__0) ** GOTO lbl8
        if (!(v1 instanceof LogSeek)) {
            v1 = v1;
            log$seek_seg_id.__cached_class__0 = Util.classOf((Object)v1);
lbl8:
            // 2 sources

            v2 = t;
            t = null;
            v3 = log$seek_seg_id.const__0.getRawRoot().invoke(v1, v2);
        } else {
            v4 = t;
            t = null;
            v3 = ((LogSeek)v1).seek_seg_path(v4);
        }
        vec__15974 = v3;
        RT.nth((Object)vec__15974, (int)RT.intCast((long)0L), null);
        v5 = vec__15974;
        vec__15974 = null;
        segid = RT.nth((Object)v5, (int)RT.intCast((long)1L), null);
        var3_3 = null;
        return segid;
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return log$seek_seg_id.invokeStatic(object3, object4);
    }

    static {
        const__0 = RT.var((String)"datomic.log", (String)"seek-seg-path");
    }
}

