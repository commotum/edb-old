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
import datomic.db$rseek_index$fn__12854;
import datomic.db$rseek_index$rseek__12851;

public final class db$rseek_index
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"seq?");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"seq");
    public static final Keyword const__3 = RT.keyword(null, (String)"raw");
    public static final Keyword const__4 = RT.keyword(null, (String)"asOfT");
    public static final Keyword const__5 = RT.keyword(null, (String)"indexBasisT");
    public static final Keyword const__6 = RT.keyword(null, (String)"memidx");
    public static final Keyword const__7 = RT.keyword(null, (String)"indexing");
    public static final Keyword const__8 = RT.keyword(null, (String)"mid-index");
    public static final Keyword const__9 = RT.keyword(null, (String)"index");
    public static final Keyword const__10 = RT.keyword(null, (String)"history");
    public static final Var const__11 = RT.var((String)"datomic.db", (String)"index-sort->cmp");
    public static final Keyword const__13 = RT.keyword(null, (String)"vaet");
    public static final Keyword const__14 = RT.keyword(null, (String)"raet");
    public static final Var const__15 = RT.var((String)"datomic.iter", (String)"drop-while");
    public static final Var const__16 = RT.var((String)"datomic.iter", (String)"merge-iters");
    public static final Var const__17 = RT.var((String)"datomic.db", (String)"reverse-comparator");

    public static Object invokeStatic(Object p__12849, Object index_sort, Object d) {
        Object object;
        Object object2;
        Object or__5238__auto__12861;
        Object object3;
        Object and__5236__auto__12859;
        Object object4;
        Object and__5236__auto__12858;
        Object object5;
        Object and__5236__auto__12857;
        Object object6;
        Object object7 = p__12849;
        p__12849 = null;
        Object map__12850 = object7;
        Object object8 = ((IFn)const__0.getRawRoot()).invoke(map__12850);
        if (object8 != null && object8 != Boolean.FALSE) {
            Object object9 = map__12850;
            map__12850 = null;
            object6 = PersistentHashMap.create((ISeq)((ISeq)((IFn)const__1.getRawRoot()).invoke(object9)));
        } else {
            object6 = map__12850;
            map__12850 = null;
        }
        Object map__128502 = object6;
        Object raw = RT.get((Object)map__128502, (Object)const__3);
        Object asOfT = RT.get((Object)map__128502, (Object)const__4);
        Object indexBasisT = RT.get((Object)map__128502, (Object)const__5);
        Object memidx = RT.get((Object)map__128502, (Object)const__6);
        Object indexing = RT.get((Object)map__128502, (Object)const__7);
        Object mid_index = RT.get((Object)map__128502, (Object)const__8);
        Object index2 = RT.get((Object)map__128502, (Object)const__9);
        Object object10 = map__128502;
        map__128502 = null;
        Object history2 = RT.get((Object)object10, (Object)const__10);
        Object cmp = ((IFn)const__11.getRawRoot()).invoke(index_sort);
        db$rseek_index$rseek__12851 rseek2 = new db$rseek_index$rseek__12851();
        Object idx_key2 = Util.equiv((Object)index_sort, (Object)const__13) ? const__14 : index_sort;
        IFn iFn = (IFn)const__15.getRawRoot();
        Object object11 = cmp;
        cmp = null;
        db$rseek_index$fn__12854 db$rseek_index$fn__12854 = new db$rseek_index$fn__12854(d, object11);
        IFn iFn2 = (IFn)const__16.getRawRoot();
        Object object12 = index_sort;
        index_sort = null;
        Object object13 = ((IFn)const__17.getRawRoot()).invoke(((IFn)const__11.getRawRoot()).invoke(object12));
        Object object14 = memidx;
        memidx = null;
        Object object15 = ((IFn)rseek2).invoke(RT.get((Object)object14, (Object)idx_key2), d);
        IFn iFn3 = (IFn)rseek2;
        Object object16 = and__5236__auto__12857 = indexing;
        if (object16 != null && object16 != Boolean.FALSE) {
            Object object17 = indexing;
            indexing = null;
            object5 = RT.get((Object)object17, (Object)idx_key2);
        } else {
            object5 = and__5236__auto__12857;
            and__5236__auto__12857 = null;
        }
        Object object18 = iFn3.invoke(object5, d);
        IFn iFn4 = (IFn)rseek2;
        Object object19 = and__5236__auto__12858 = mid_index;
        if (object19 != null && object19 != Boolean.FALSE) {
            Object object20 = mid_index;
            mid_index = null;
            object4 = RT.get((Object)object20, (Object)idx_key2);
        } else {
            object4 = and__5236__auto__12858;
            and__5236__auto__12858 = null;
        }
        Object object21 = iFn4.invoke(object4, d);
        IFn iFn5 = (IFn)rseek2;
        Object object22 = and__5236__auto__12859 = index2;
        if (object22 != null && object22 != Boolean.FALSE) {
            Object object23 = index2;
            index2 = null;
            object3 = RT.get((Object)object23, (Object)idx_key2);
        } else {
            object3 = and__5236__auto__12859;
            and__5236__auto__12859 = null;
        }
        Object object24 = iFn5.invoke(object3, d);
        Object object25 = raw;
        raw = null;
        Object object26 = or__5238__auto__12861 = object25;
        if (object26 != null && object26 != Boolean.FALSE) {
            object2 = or__5238__auto__12861;
            or__5238__auto__12861 = null;
        } else {
            Object and__5236__auto__12860;
            Object object27 = and__5236__auto__12860 = asOfT;
            if (object27 != null && object27 != Boolean.FALSE) {
                Object object28 = asOfT;
                asOfT = null;
                Object object29 = indexBasisT;
                indexBasisT = null;
                object2 = Numbers.lt((Object)object28, (Object)object29) ? Boolean.TRUE : Boolean.FALSE;
            } else {
                object2 = and__5236__auto__12860;
                and__5236__auto__12860 = null;
            }
        }
        if (object2 != null && object2 != Boolean.FALSE) {
            Object object30;
            Object and__5236__auto__12862;
            db$rseek_index$rseek__12851 db$rseek_index$rseek__12851 = rseek2;
            rseek2 = null;
            IFn iFn6 = (IFn)db$rseek_index$rseek__12851;
            Object object31 = and__5236__auto__12862 = history2;
            if (object31 != null && object31 != Boolean.FALSE) {
                Object object32 = history2;
                history2 = null;
                Object object33 = idx_key2;
                idx_key2 = null;
                object30 = RT.get((Object)object32, (Object)object33);
            } else {
                object30 = and__5236__auto__12862;
                and__5236__auto__12862 = null;
            }
            Object object34 = d;
            d = null;
            object = iFn6.invoke(object30, object34);
        } else {
            object = null;
        }
        return iFn.invoke((Object)db$rseek_index$fn__12854, iFn2.invoke(object13, object15, object18, object21, object24, object));
    }

    public Object invoke(Object object, Object object2, Object object3) {
        Object object4 = object;
        object = null;
        Object object5 = object2;
        object2 = null;
        Object object6 = object3;
        object3 = null;
        return db$rseek_index.invokeStatic(object4, object5, object6);
    }
}

