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
import datomic.index$get_avet_sorted_datoms_mem$fn__15527;
import datomic.index$get_avet_sorted_datoms_mem$fn__15529;
import datomic.index$get_avet_sorted_datoms_mem$fn__15531;
import datomic.index$get_avet_sorted_datoms_mem$fn__15533;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public final class index$get_avet_sorted_datoms_mem
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"seq");
    public static final Var const__3 = RT.var((String)"clojure.core", (String)"reduce");
    public static final Var const__4 = RT.var((String)"datomic.iter", (String)"iter-seq");
    public static final Var const__5 = RT.var((String)"datomic.iter", (String)"take-while");
    public static final Var const__6 = RT.var((String)"datomic.btset", (String)"seek");
    public static final Var const__7 = RT.var((String)"datomic.db", (String)"datum");
    public static final Keyword const__8 = RT.keyword(null, (String)"a");
    public static final Var const__10 = RT.var((String)"clojure.core", (String)"chunked-seq?");
    public static final Var const__11 = RT.var((String)"clojure.core", (String)"chunk-first");
    public static final Var const__12 = RT.var((String)"clojure.core", (String)"chunk-rest");
    public static final Var const__15 = RT.var((String)"clojure.core", (String)"first");
    public static final Var const__16 = RT.var((String)"clojure.core", (String)"next");
    public static final Var const__17 = RT.var((String)"datomic.db", (String)"avet-cmp");

    public static Object invokeStatic(Object db2, Object index2, Object attrids) {
        ArrayList ret = new ArrayList();
        Object object = index2;
        if (object != null && object != Boolean.FALSE) {
            Object object2 = attrids;
            attrids = null;
            Object seq_15523 = ((IFn)const__0.getRawRoot()).invoke(object2);
            Object chunk_15524 = null;
            long count_15525 = 0L;
            long i_15526 = 0L;
            while (true) {
                Object temp__5457__auto__15537;
                if (i_15526 < count_15525) {
                    Object attrid = ((Indexed)chunk_15524).nth(RT.uncheckedIntCast((long)i_15526));
                    index$get_avet_sorted_datoms_mem$fn__15529 index$get_avet_sorted_datoms_mem$fn__15529 = new index$get_avet_sorted_datoms_mem$fn__15529(attrid);
                    Object object3 = attrid;
                    attrid = null;
                    ((IFn)const__3.getRawRoot()).invoke((Object)new index$get_avet_sorted_datoms_mem$fn__15527(ret), null, ((IFn)const__4.getRawRoot()).invoke(((IFn)const__5.getRawRoot()).invoke((Object)index$get_avet_sorted_datoms_mem$fn__15529, ((IFn)const__6.getRawRoot()).invoke(index2, ((IFn)const__7.getRawRoot()).invoke(db2, (Object)const__8, object3)))));
                    Object object4 = seq_15523;
                    seq_15523 = null;
                    Object object5 = chunk_15524;
                    chunk_15524 = null;
                    ++i_15526;
                    chunk_15524 = object5;
                    seq_15523 = object4;
                    continue;
                }
                Object object6 = seq_15523;
                seq_15523 = null;
                Object object7 = temp__5457__auto__15537 = ((IFn)const__0.getRawRoot()).invoke(object6);
                if (object7 == null || object7 == Boolean.FALSE) break;
                Object object8 = temp__5457__auto__15537;
                temp__5457__auto__15537 = null;
                Object seq_155232 = object8;
                Object object9 = ((IFn)const__10.getRawRoot()).invoke(seq_155232);
                if (object9 != null && object9 != Boolean.FALSE) {
                    Object c__5719__auto__15536 = ((IFn)const__11.getRawRoot()).invoke(seq_155232);
                    Object object10 = seq_155232;
                    seq_155232 = null;
                    Object object11 = c__5719__auto__15536;
                    Object object12 = c__5719__auto__15536;
                    c__5719__auto__15536 = null;
                    i_15526 = (int)0L;
                    count_15525 = RT.count((Object)object12);
                    chunk_15524 = object11;
                    seq_15523 = ((IFn)const__12.getRawRoot()).invoke(object10);
                    continue;
                }
                Object attrid = ((IFn)const__15.getRawRoot()).invoke(seq_155232);
                index$get_avet_sorted_datoms_mem$fn__15533 index$get_avet_sorted_datoms_mem$fn__15533 = new index$get_avet_sorted_datoms_mem$fn__15533(attrid);
                Object object13 = attrid;
                attrid = null;
                ((IFn)const__3.getRawRoot()).invoke((Object)new index$get_avet_sorted_datoms_mem$fn__15531(ret), null, ((IFn)const__4.getRawRoot()).invoke(((IFn)const__5.getRawRoot()).invoke((Object)index$get_avet_sorted_datoms_mem$fn__15533, ((IFn)const__6.getRawRoot()).invoke(index2, ((IFn)const__7.getRawRoot()).invoke(db2, (Object)const__8, object13)))));
                Object object14 = seq_155232;
                seq_155232 = null;
                i_15526 = 0L;
                count_15525 = 0L;
                chunk_15524 = null;
                seq_15523 = ((IFn)const__16.getRawRoot()).invoke(object14);
            }
        }
        Collections.sort(ret, (Comparator)const__17.getRawRoot());
        Object var3_3 = null;
        return ret;
    }

    public Object invoke(Object object, Object object2, Object object3) {
        Object object4 = object;
        object = null;
        Object object5 = object2;
        object2 = null;
        Object object6 = object3;
        object3 = null;
        return index$get_avet_sorted_datoms_mem.invokeStatic(object4, object5, object6);
    }
}

