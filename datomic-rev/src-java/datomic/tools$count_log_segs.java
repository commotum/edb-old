/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.ISeq
 *  clojure.lang.Keyword
 *  clojure.lang.Numbers
 *  clojure.lang.PersistentHashMap
 *  clojure.lang.RT
 *  clojure.lang.Util
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.ISeq;
import clojure.lang.Keyword;
import clojure.lang.Numbers;
import clojure.lang.PersistentHashMap;
import clojure.lang.RT;
import clojure.lang.Util;
import clojure.lang.Var;
import datomic.log.LogDirSeq;

public final class tools$count_log_segs
extends AFunction {
    private static Class __cached_class__0;
    private static Class __cached_class__1;
    public static final Var const__0;
    public static final Var const__1;
    public static final Keyword const__3;
    public static final Keyword const__4;
    public static final Var const__5;
    public static final Var const__6;
    public static final Object const__7;
    public static final Var const__8;
    public static final Var const__10;
    public static final Var const__11;
    public static final Var const__12;
    public static final Var const__13;

    /*
     * Unable to fully structure code
     */
    public static Object invokeStatic(Object p__21791) {
        block7: {
            block6: {
                v0 = p__21791;
                p__21791 = null;
                map__21792 = v0;
                v1 = ((IFn)tools$count_log_segs.const__0.getRawRoot()).invoke(map__21792);
                if (v1 != null && v1 != Boolean.FALSE) {
                    v2 = map__21792;
                    map__21792 = null;
                    v3 = PersistentHashMap.create((ISeq)((ISeq)((IFn)tools$count_log_segs.const__1.getRawRoot()).invoke(v2)));
                } else {
                    v3 = map__21792;
                    map__21792 = null;
                }
                map__21792 = v3;
                cluster = RT.get((Object)map__21792, (Object)tools$count_log_segs.const__3);
                v4 = map__21792;
                map__21792 = null;
                olookup = RT.get((Object)v4, (Object)tools$count_log_segs.const__4);
                v5 = cluster;
                cluster = null;
                v6 = olookup;
                olookup = null;
                v7 = temp__5457__auto__21794 = ((IFn)tools$count_log_segs.const__5.getRawRoot()).invoke(((IFn)tools$count_log_segs.const__6.getRawRoot()).invoke(v5, v6), tools$count_log_segs.const__7);
                if (v7 == null || v7 == Boolean.FALSE) break block6;
                v8 = temp__5457__auto__21794;
                temp__5457__auto__21794 = null;
                tree_iter = v8;
                v9 = tree_iter;
                if (Util.classOf((Object)v9) == tools$count_log_segs.__cached_class__0) ** GOTO lbl31
                if (!(v9 instanceof LogDirSeq)) {
                    v9 = v9;
                    tools$count_log_segs.__cached_class__0 = Util.classOf((Object)v9);
lbl31:
                    // 2 sources

                    v10 = tools$count_log_segs.const__11.getRawRoot().invoke(v9);
                } else {
                    v10 = ((LogDirSeq)v9).log_dir_seq();
                }
                v11 = Numbers.add((long)1L, (long)RT.count((Object)v10));
                v12 = (IFn)tools$count_log_segs.const__12.getRawRoot();
                v13 = tools$count_log_segs.const__8.getRawRoot();
                v14 = (IFn)tools$count_log_segs.const__13.getRawRoot();
                v15 = tools$count_log_segs.const__10.getRawRoot();
                v16 = tree_iter;
                tree_iter = null;
                v17 = v16;
                if (Util.classOf((Object)v16) == tools$count_log_segs.__cached_class__1) ** GOTO lbl46
                if (!(v17 instanceof LogDirSeq)) {
                    v17 = v17;
                    tools$count_log_segs.__cached_class__1 = Util.classOf((Object)v17);
lbl46:
                    // 2 sources

                    v18 = tools$count_log_segs.const__11.getRawRoot().invoke(v17);
                } else {
                    v18 = ((LogDirSeq)v17).log_dir_seq();
                }
                v19 = Numbers.add((long)v11, (Object)v12.invoke(v13, v14.invoke(v15, v18)));
                break block7;
            }
            v19 = null;
        }
        return v19;
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return tools$count_log_segs.invokeStatic(object2);
    }

    static {
        const__0 = RT.var((String)"clojure.core", (String)"seq?");
        const__1 = RT.var((String)"clojure.core", (String)"seq");
        const__3 = RT.keyword(null, (String)"cluster");
        const__4 = RT.keyword(null, (String)"olookup");
        const__5 = RT.var((String)"datomic.log", (String)"seek-tx");
        const__6 = RT.var((String)"datomic.log", (String)"find-log");
        const__7 = 0L;
        const__8 = RT.var((String)"clojure.core", (String)"+");
        const__10 = RT.var((String)"clojure.core", (String)"count");
        const__11 = RT.var((String)"datomic.log", (String)"log-dir-seq");
        const__12 = RT.var((String)"clojure.core", (String)"apply");
        const__13 = RT.var((String)"clojure.core", (String)"map");
    }
}

