/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.Indexed
 *  clojure.lang.Keyword
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Indexed;
import clojure.lang.Keyword;
import clojure.lang.RT;
import clojure.lang.Var;

public final class garbage$mark_garbage
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.garbage", (String)"mark-garbage");
    public static final Object const__1 = 1000L;
    public static final Object const__2 = 800L;
    public static final Var const__3 = RT.var((String)"clojure.core", (String)"seq");
    public static final Var const__4 = RT.var((String)"clojure.core", (String)"partition-all");
    public static final Var const__7 = RT.var((String)"clojure.core", (String)"send-off");
    public static final Var const__8 = RT.var((String)"datomic.garbage", (String)"garbage-agent");
    public static final Var const__9 = RT.var((String)"datomic.garbage", (String)"do-mark-garbage");
    public static final Var const__11 = RT.var((String)"clojure.core", (String)"chunked-seq?");
    public static final Var const__12 = RT.var((String)"clojure.core", (String)"chunk-first");
    public static final Var const__13 = RT.var((String)"clojure.core", (String)"chunk-rest");
    public static final Var const__16 = RT.var((String)"clojure.core", (String)"first");
    public static final Var const__17 = RT.var((String)"clojure.core", (String)"next");
    public static final Keyword const__18 = RT.keyword(null, (String)"ok");

    public static Object invokeStatic(Object cluster2, Object lookup, Object ids, Object max_leaf_size, Object max_dir_size) {
        Object object = ids;
        ids = null;
        Object seq_19812 = ((IFn)const__3.getRawRoot()).invoke(((IFn)const__4.getRawRoot()).invoke(max_leaf_size, object));
        Object chunk_19813 = null;
        long count_19814 = 0L;
        long i_19815 = 0L;
        while (true) {
            Object chunks;
            Object temp__5457__auto__19818;
            if (i_19815 < count_19814) {
                Object chunks2;
                Object object2 = chunks2 = ((Indexed)chunk_19813).nth(RT.intCast((long)i_19815));
                chunks2 = null;
                ((IFn)const__7.getRawRoot()).invoke(const__8.getRawRoot(), const__9.getRawRoot(), cluster2, lookup, object2, max_leaf_size, max_dir_size);
                Object object3 = seq_19812;
                seq_19812 = null;
                Object object4 = chunk_19813;
                chunk_19813 = null;
                ++i_19815;
                chunk_19813 = object4;
                seq_19812 = object3;
                continue;
            }
            Object object5 = seq_19812;
            seq_19812 = null;
            Object object6 = temp__5457__auto__19818 = ((IFn)const__3.getRawRoot()).invoke(object5);
            if (object6 == null || object6 == Boolean.FALSE) break;
            Object object7 = temp__5457__auto__19818;
            temp__5457__auto__19818 = null;
            Object seq_198122 = object7;
            Object object8 = ((IFn)const__11.getRawRoot()).invoke(seq_198122);
            if (object8 != null && object8 != Boolean.FALSE) {
                Object c__5719__auto__19817 = ((IFn)const__12.getRawRoot()).invoke(seq_198122);
                Object object9 = seq_198122;
                seq_198122 = null;
                Object object10 = c__5719__auto__19817;
                Object object11 = c__5719__auto__19817;
                c__5719__auto__19817 = null;
                i_19815 = RT.intCast((long)0L);
                count_19814 = RT.intCast((int)RT.count((Object)object11));
                chunk_19813 = object10;
                seq_19812 = ((IFn)const__13.getRawRoot()).invoke(object9);
                continue;
            }
            Object object12 = chunks = ((IFn)const__16.getRawRoot()).invoke(seq_198122);
            chunks = null;
            ((IFn)const__7.getRawRoot()).invoke(const__8.getRawRoot(), const__9.getRawRoot(), cluster2, lookup, object12, max_leaf_size, max_dir_size);
            Object object13 = seq_198122;
            seq_198122 = null;
            i_19815 = 0L;
            count_19814 = 0L;
            chunk_19813 = null;
            seq_19812 = ((IFn)const__17.getRawRoot()).invoke(object13);
        }
        return const__18;
    }

    public Object invoke(Object object, Object object2, Object object3, Object object4, Object object5) {
        Object object6 = object;
        object = null;
        Object object7 = object2;
        object2 = null;
        Object object8 = object3;
        object3 = null;
        Object object9 = object4;
        object4 = null;
        Object object10 = object5;
        object5 = null;
        return garbage$mark_garbage.invokeStatic(object6, object7, object8, object9, object10);
    }

    public static Object invokeStatic(Object cluster2, Object lookup, Object ids) {
        Object object = cluster2;
        cluster2 = null;
        Object object2 = lookup;
        lookup = null;
        Object object3 = ids;
        ids = null;
        return ((IFn)const__0.getRawRoot()).invoke(object, object2, object3, const__1, const__2);
    }

    public Object invoke(Object object, Object object2, Object object3) {
        Object object4 = object;
        object = null;
        Object object5 = object2;
        object2 = null;
        Object object6 = object3;
        object3 = null;
        return garbage$mark_garbage.invokeStatic(object4, object5, object6);
    }
}

