/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.ISeq
 *  clojure.lang.Keyword
 *  clojure.lang.PersistentHashMap
 *  clojure.lang.PersistentHashSet
 *  clojure.lang.RT
 *  clojure.lang.Util
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.ISeq;
import clojure.lang.Keyword;
import clojure.lang.PersistentHashMap;
import clojure.lang.PersistentHashSet;
import clojure.lang.RT;
import clojure.lang.Util;
import clojure.lang.Var;
import datomic.cluster.AsyncWriter;
import datomic.log$excise_root$fn__16440;
import datomic.log$excise_root$fn__16447;
import datomic.log$excise_root$fn__16449;
import datomic.log$excise_root$patch_dir__16442;
import datomic.log.Log;

public final class log$excise_root
extends AFunction {
    private static Class __cached_class__0;
    private static Class __cached_class__1;
    private static Class __cached_class__2;
    public static final Var const__0;
    public static final Var const__1;
    public static final Keyword const__3;
    public static final Keyword const__4;
    public static final Var const__5;
    public static final Var const__6;
    public static final Var const__7;
    public static final Var const__8;
    public static final Var const__9;
    public static final Var const__10;
    public static final Var const__11;
    public static final Var const__12;
    public static final Var const__13;
    public static final Var const__14;
    public static final Var const__15;
    public static final Var const__16;
    public static final Var const__17;
    public static final Var const__18;
    public static final Var const__19;
    public static final Keyword const__20;
    public static final Var const__21;
    public static final Keyword const__22;
    public static final Var const__23;
    public static final Var const__24;

    /*
     * Unable to fully structure code
     */
    public static Object invokeStatic(Object cs, Object lookup, Object current_root_id, Object p__16438) {
        block10: {
            v0 = p__16438;
            p__16438 = null;
            map__16439 = v0;
            v1 = ((IFn)log$excise_root.const__0.getRawRoot()).invoke(map__16439);
            if (v1 != null && v1 != Boolean.FALSE) {
                v2 = map__16439;
                map__16439 = null;
                v3 = PersistentHashMap.create((ISeq)((ISeq)((IFn)log$excise_root.const__1.getRawRoot()).invoke(v2)));
            } else {
                v3 = map__16439;
                map__16439 = null;
            }
            map__16439 = v3;
            dir_map = RT.get((Object)map__16439, (Object)log$excise_root.const__3);
            v4 = map__16439;
            map__16439 = null;
            replacements = RT.get((Object)v4, (Object)log$excise_root.const__4);
            v5 = current_root_id;
            current_root_id = null;
            log = ((IFn)log$excise_root.const__5.getRawRoot()).invoke(lookup, v5);
            v6 = log;
            if (Util.classOf((Object)v6) == log$excise_root.__cached_class__0) ** GOTO lbl25
            if (!(v6 instanceof Log)) {
                v6 = v6;
                log$excise_root.__cached_class__0 = Util.classOf((Object)v6);
lbl25:
                // 2 sources

                v7 = log$excise_root.const__6.getRawRoot().invoke(v6);
            } else {
                v7 = ((Log)v6).get_root_val();
            }
            root = v7;
            dirs = ((IFn)log$excise_root.const__7.getRawRoot()).invoke((Object)PersistentHashSet.EMPTY, ((IFn)log$excise_root.const__8.getRawRoot()).invoke((Object)new log$excise_root$fn__16440(), root));
            patch_dir = new log$excise_root$patch_dir__16442();
            v8 = pario = ((IFn)log$excise_root.const__9.getRawRoot()).invoke((Object)"datomic.exciseIOParallelism");
            if (v8 != null && v8 != Boolean.FALSE) {
                v9 = cs;
                cs = null;
                v10 = ((IFn)log$excise_root.const__10.getRawRoot()).invoke(v9, pario, log$excise_root.const__11.getRawRoot(), (Object)new log$excise_root$fn__16447());
            } else {
                v10 = cs;
                cs = null;
            }
            cs = v10;
            v11 = lookup;
            lookup = null;
            v12 = new log$excise_root$fn__16449(replacements, (Object)patch_dir, log, dir_map, v11, cs);
            v13 = replacements;
            replacements = null;
            v14 = dirs;
            dirs = null;
            v15 = dir_map;
            dir_map = null;
            replacements = ((IFn)log$excise_root.const__12.getRawRoot()).invoke((Object)v12, v13, ((IFn)log$excise_root.const__13.getRawRoot()).invoke(v14, ((IFn)log$excise_root.const__14.getRawRoot()).invoke(v15)));
            v16 = patch_dir;
            patch_dir = null;
            v17 = root;
            root = null;
            newroot = ((IFn)v16).invoke(replacements, v17);
            newrid = ((IFn)log$excise_root.const__15.getRawRoot()).invoke();
            v18 = newroot;
            newroot = null;
            ((IFn)log$excise_root.const__16.getRawRoot()).invoke(cs, newrid, ((IFn)log$excise_root.const__17.getRawRoot()).invoke(v18));
            v19 = pario;
            pario = null;
            if (v19 == null || v19 == Boolean.FALSE) break block10;
            v20 = (IFn)log$excise_root.const__18.getRawRoot();
            v21 = cs;
            cs = null;
            v22 = v21;
            if (Util.classOf((Object)v21) == log$excise_root.__cached_class__1) ** GOTO lbl71
            if (!(v22 instanceof AsyncWriter)) {
                v22 = v22;
                log$excise_root.__cached_class__1 = Util.classOf((Object)v22);
lbl71:
                // 2 sources

                v23 = log$excise_root.const__19.getRawRoot().invoke(v22);
            } else {
                v23 = ((AsyncWriter)v22).finish_writer();
            }
            v20.invoke(v23, log$excise_root.const__11.getRawRoot());
        }
        v24 = new Object[4];
        v24[0] = log$excise_root.const__20;
        v25 = newrid;
        newrid = null;
        v24[1] = ((IFn)log$excise_root.const__21.getRawRoot()).invoke(v25);
        v24[2] = log$excise_root.const__22;
        v26 = (IFn)log$excise_root.const__8.getRawRoot();
        v27 = log$excise_root.const__21.getRawRoot();
        v28 = (IFn)log$excise_root.const__23.getRawRoot();
        v29 = log;
        log = null;
        v30 = v29;
        if (Util.classOf((Object)v29) == log$excise_root.__cached_class__2) ** GOTO lbl93
        if (!(v30 instanceof Log)) {
            v30 = v30;
            log$excise_root.__cached_class__2 = Util.classOf((Object)v30);
lbl93:
            // 2 sources

            v31 = log$excise_root.const__24.getRawRoot().invoke(v30);
        } else {
            v31 = ((Log)v30).get_root_id();
        }
        v32 = replacements;
        replacements = null;
        v24[3] = v26.invoke(v27, v28.invoke(v31, ((IFn)log$excise_root.const__14.getRawRoot()).invoke(v32)));
        return RT.mapUniqueKeys((Object[])v24);
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
        return log$excise_root.invokeStatic(object5, object6, object7, object8);
    }

    static {
        const__0 = RT.var((String)"clojure.core", (String)"seq?");
        const__1 = RT.var((String)"clojure.core", (String)"seq");
        const__3 = RT.keyword(null, (String)"dir-map");
        const__4 = RT.keyword(null, (String)"replacements");
        const__5 = RT.var((String)"datomic.log", (String)"log-tree");
        const__6 = RT.var((String)"datomic.log", (String)"get-root-val");
        const__7 = RT.var((String)"clojure.core", (String)"into");
        const__8 = RT.var((String)"clojure.core", (String)"map");
        const__9 = RT.var((String)"datomic.config", (String)"property");
        const__10 = RT.var((String)"datomic.cluster", (String)"queueing-writer");
        const__11 = RT.var((String)"datomic.cluster", (String)"BOUNDING_TIMEOUT_MSEC");
        const__12 = RT.var((String)"clojure.core", (String)"reduce");
        const__13 = RT.var((String)"clojure.core", (String)"remove");
        const__14 = RT.var((String)"clojure.core", (String)"keys");
        const__15 = RT.var((String)"datomic.common", (String)"rand-uuid");
        const__16 = RT.var((String)"datomic.log", (String)"write-excise-val");
        const__17 = RT.var((String)"datomic.log", (String)"fressianed-dir");
        const__18 = RT.var((String)"datomic.common", (String)"bounded-deref");
        const__19 = RT.var((String)"datomic.cluster", (String)"finish-writer");
        const__20 = RT.keyword(null, (String)"root-id");
        const__21 = RT.var((String)"datomic.cluster", (String)"uuid->val-key");
        const__22 = RT.keyword(null, (String)"garbage-ids");
        const__23 = RT.var((String)"clojure.core", (String)"cons");
        const__24 = RT.var((String)"datomic.log", (String)"get-root-id");
    }
}

