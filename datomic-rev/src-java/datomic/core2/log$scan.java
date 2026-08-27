/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.Keyword
 *  clojure.lang.PersistentArrayMap
 *  clojure.lang.RT
 *  clojure.lang.Util
 *  clojure.lang.Var
 */
package datomic.core2;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Keyword;
import clojure.lang.PersistentArrayMap;
import clojure.lang.RT;
import clojure.lang.Util;
import clojure.lang.Var;
import datomic.core2.log.spi.Scan;

public final class log$scan
extends AFunction {
    private static Class __cached_class__0;
    public static final Var const__0;
    public static final Var const__1;
    public static final Var const__2;
    public static final Var const__3;
    public static final Var const__4;
    public static final Var const__5;
    public static final Keyword const__7;
    public static final Var const__8;

    /*
     * Unable to fully structure code
     */
    public static Object invokeStatic(Object log, Object opts) {
        v0 = opts;
        opts = null;
        map__20563 = ((IFn)log$scan.const__0.getRawRoot()).invoke(v0);
        v1 = ((IFn)log$scan.const__1.getRawRoot()).invoke(map__20563);
        if (v1 != null && v1 != Boolean.FALSE) {
            v2 = ((IFn)log$scan.const__2.getRawRoot()).invoke(map__20563);
            if (v2 != null && v2 != Boolean.FALSE) {
                v3 = map__20563;
                map__20563 = null;
                v4 = PersistentArrayMap.createAsIfByAssoc((Object[])((Object[])((IFn)log$scan.const__3.getRawRoot()).invoke(v3)));
            } else {
                v5 = ((IFn)log$scan.const__4.getRawRoot()).invoke(map__20563);
                if (v5 != null && v5 != Boolean.FALSE) {
                    v6 = map__20563;
                    map__20563 = null;
                    v4 = ((IFn)log$scan.const__5.getRawRoot()).invoke(v6);
                } else {
                    v4 = PersistentArrayMap.EMPTY;
                }
            }
        } else {
            v4 = map__20563;
            map__20563 = null;
        }
        nopts = map__20563 = v4;
        v7 = map__20563;
        map__20563 = null;
        ch = RT.get((Object)v7, (Object)log$scan.const__7);
        v8 = log;
        log = null;
        v9 = v8;
        if (Util.classOf((Object)v8) == log$scan.__cached_class__0) ** GOTO lbl33
        if (!(v9 instanceof Scan)) {
            v9 = v9;
            log$scan.__cached_class__0 = Util.classOf((Object)v9);
lbl33:
            // 2 sources

            v10 = nopts;
            nopts = null;
            v11 = log$scan.const__8.getRawRoot().invoke(v9, v10);
        } else {
            v12 = nopts;
            nopts = null;
            v11 = ((Scan)v9)._scan(v12);
        }
        v13 = ch;
        ch = null;
        return v13;
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return log$scan.invokeStatic(object3, object4);
    }

    static {
        const__0 = RT.var((String)"datomic.core2.log.spi", (String)"normalize-scan-opts");
        const__1 = RT.var((String)"clojure.core", (String)"seq?");
        const__2 = RT.var((String)"clojure.core", (String)"next");
        const__3 = RT.var((String)"clojure.core", (String)"to-array");
        const__4 = RT.var((String)"clojure.core", (String)"seq");
        const__5 = RT.var((String)"clojure.core", (String)"first");
        const__7 = RT.keyword(null, (String)"ch");
        const__8 = RT.var((String)"datomic.core2.log.spi", (String)"-scan");
    }
}

