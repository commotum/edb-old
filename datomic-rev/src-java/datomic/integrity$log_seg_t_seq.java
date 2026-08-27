/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.ISeq
 *  clojure.lang.Keyword
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
import clojure.lang.PersistentHashMap;
import clojure.lang.RT;
import clojure.lang.Util;
import clojure.lang.Var;
import datomic.log.LogSegSeq;

public final class integrity$log_seg_t_seq
extends AFunction {
    private static Class __cached_class__0;
    public static final Var const__0;
    public static final Var const__1;
    public static final Keyword const__3;
    public static final Keyword const__4;
    public static final Var const__5;
    public static final Var const__6;
    public static final Var const__7;
    public static final Keyword const__8;
    public static final Var const__9;
    public static final Var const__10;
    public static final Var const__11;

    /*
     * Unable to fully structure code
     */
    public static Object invokeStatic(Object p__22272, Object t) {
        block5: {
            block4: {
                v0 = p__22272;
                p__22272 = null;
                map__22273 = v0;
                v1 = ((IFn)integrity$log_seg_t_seq.const__0.getRawRoot()).invoke(map__22273);
                if (v1 != null && v1 != Boolean.FALSE) {
                    v2 = map__22273;
                    map__22273 = null;
                    v3 = PersistentHashMap.create((ISeq)((ISeq)((IFn)integrity$log_seg_t_seq.const__1.getRawRoot()).invoke(v2)));
                } else {
                    v3 = map__22273;
                    map__22273 = null;
                }
                map__22273 = v3;
                cluster = RT.get((Object)map__22273, (Object)integrity$log_seg_t_seq.const__3);
                v4 = map__22273;
                map__22273 = null;
                olookup = RT.get((Object)v4, (Object)integrity$log_seg_t_seq.const__4);
                v5 = cluster;
                cluster = null;
                v6 = olookup;
                olookup = null;
                v7 = t;
                t = null;
                v8 = temp__5457__auto__22275 = ((IFn)integrity$log_seg_t_seq.const__5.getRawRoot()).invoke(((IFn)integrity$log_seg_t_seq.const__6.getRawRoot()).invoke(v5, v6), v7);
                if (v8 == null || v8 == Boolean.FALSE) break block4;
                v9 = temp__5457__auto__22275;
                temp__5457__auto__22275 = null;
                tree_iter = v9;
                v10 = (IFn)integrity$log_seg_t_seq.const__7.getRawRoot();
                v11 = (IFn)integrity$log_seg_t_seq.const__9.getRawRoot();
                v12 = integrity$log_seg_t_seq.const__10.getRawRoot();
                v13 = tree_iter;
                tree_iter = null;
                v14 = v13;
                if (Util.classOf((Object)v13) == integrity$log_seg_t_seq.__cached_class__0) ** GOTO lbl38
                if (!(v14 instanceof LogSegSeq)) {
                    v14 = v14;
                    integrity$log_seg_t_seq.__cached_class__0 = Util.classOf((Object)v14);
lbl38:
                    // 2 sources

                    v15 = integrity$log_seg_t_seq.const__11.getRawRoot().invoke(v14);
                } else {
                    v15 = ((LogSegSeq)v14).log_seg_seq();
                }
                v16 = v10.invoke((Object)integrity$log_seg_t_seq.const__8, v11.invoke(v12, v15));
                break block5;
            }
            v16 = null;
        }
        return v16;
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return integrity$log_seg_t_seq.invokeStatic(object3, object4);
    }

    static {
        const__0 = RT.var((String)"clojure.core", (String)"seq?");
        const__1 = RT.var((String)"clojure.core", (String)"seq");
        const__3 = RT.keyword(null, (String)"cluster");
        const__4 = RT.keyword(null, (String)"olookup");
        const__5 = RT.var((String)"datomic.log", (String)"seek-tx");
        const__6 = RT.var((String)"datomic.log", (String)"find-log");
        const__7 = RT.var((String)"clojure.core", (String)"map");
        const__8 = RT.keyword(null, (String)"t");
        const__9 = RT.var((String)"clojure.core", (String)"mapcat");
        const__10 = RT.var((String)"clojure.core", (String)"identity");
        const__11 = RT.var((String)"datomic.log", (String)"log-seg-seq");
    }
}

