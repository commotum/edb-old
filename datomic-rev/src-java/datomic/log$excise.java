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
import datomic.cluster.AsyncWriter;
import datomic.log$excise$fn__16433;

public final class log$excise
extends AFunction {
    private static Class __cached_class__0;
    public static final Var const__0;
    public static final Var const__1;
    public static final Var const__2;
    public static final Var const__3;
    public static final Var const__4;
    public static final Var const__5;
    public static final Var const__6;
    public static final Var const__7;
    public static final Keyword const__8;
    public static final Keyword const__9;

    /*
     * Unable to fully structure code
     */
    public static Object invokeStatic(Object cs, Object lookup, Object log, Object xpreds) {
        block4: {
            ts = ((IFn)log$excise.const__0.getRawRoot()).invoke(log, xpreds);
            v0 = log;
            log = null;
            dir_map = ((IFn)log$excise.const__1.getRawRoot()).invoke(v0, ts);
            v1 = pario = ((IFn)log$excise.const__2.getRawRoot()).invoke((Object)"datomic.exciseIOParallelism");
            if (v1 != null && v1 != Boolean.FALSE) {
                v2 = cs;
                cs = null;
                v3 = ((IFn)log$excise.const__3.getRawRoot()).invoke(v2, pario, log$excise.const__4.getRawRoot(), (Object)new log$excise$fn__16433());
            } else {
                v3 = cs;
                cs = null;
            }
            cs = v3;
            v4 = lookup;
            lookup = null;
            v5 = xpreds;
            xpreds = null;
            v6 = ts;
            ts = null;
            replacements = ((IFn)log$excise.const__5.getRawRoot()).invoke(cs, v4, v5, v6, dir_map);
            v7 = pario;
            pario = null;
            if (v7 == null || v7 == Boolean.FALSE) break block4;
            v8 = (IFn)log$excise.const__6.getRawRoot();
            v9 = cs;
            cs = null;
            v10 = v9;
            if (Util.classOf((Object)v9) == log$excise.__cached_class__0) ** GOTO lbl32
            if (!(v10 instanceof AsyncWriter)) {
                v10 = v10;
                log$excise.__cached_class__0 = Util.classOf((Object)v10);
lbl32:
                // 2 sources

                v11 = log$excise.const__7.getRawRoot().invoke(v10);
            } else {
                v11 = ((AsyncWriter)v10).finish_writer();
            }
            v8.invoke(v11, log$excise.const__4.getRawRoot());
        }
        v12 = new Object[4];
        v12[0] = log$excise.const__8;
        v13 = dir_map;
        dir_map = null;
        v12[1] = v13;
        v12[2] = log$excise.const__9;
        v14 = replacements;
        replacements = null;
        v12[3] = v14;
        return RT.mapUniqueKeys((Object[])v12);
    }

    public Object invoke(Object object, Object object2, Object object3, Object object4) {
        Object object5 = object;
        object = null;
        Object object6 = object2;
        object2 = null;
        Object object7 = object3;
        object3 = null;
        Object object8 = object4;
        object4 = null;
        return log$excise.invokeStatic(object5, object6, object7, object8);
    }

    static {
        const__0 = RT.var((String)"datomic.log", (String)"excise-ts");
        const__1 = RT.var((String)"datomic.log", (String)"excise-dir-map");
        const__2 = RT.var((String)"datomic.config", (String)"property");
        const__3 = RT.var((String)"datomic.cluster", (String)"queueing-writer");
        const__4 = RT.var((String)"datomic.cluster", (String)"BOUNDING_TIMEOUT_MSEC");
        const__5 = RT.var((String)"datomic.log", (String)"write-excised-log");
        const__6 = RT.var((String)"datomic.common", (String)"bounded-deref");
        const__7 = RT.var((String)"datomic.cluster", (String)"finish-writer");
        const__8 = RT.keyword(null, (String)"dir-map");
        const__9 = RT.keyword(null, (String)"replacements");
    }
}

