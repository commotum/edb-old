/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.IObj
 *  clojure.lang.ISeq
 *  clojure.lang.Indexed
 *  clojure.lang.Keyword
 *  clojure.lang.Numbers
 *  clojure.lang.PersistentHashMap
 *  clojure.lang.PersistentList
 *  clojure.lang.RT
 *  clojure.lang.Symbol
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.IObj;
import clojure.lang.ISeq;
import clojure.lang.Indexed;
import clojure.lang.Keyword;
import clojure.lang.Numbers;
import clojure.lang.PersistentHashMap;
import clojure.lang.PersistentList;
import clojure.lang.RT;
import clojure.lang.Symbol;
import clojure.lang.Var;
import datomic.backup$missing_seg_ids$fn__20320;
import datomic.backup$missing_seg_ids$fn__20324;
import java.util.Arrays;

public final class backup$missing_seg_ids
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"set?");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"str");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"pr-str");
    public static final Object const__3 = ((IObj)PersistentList.create(Arrays.asList(Symbol.intern(null, (String)"set?"), Symbol.intern(null, (String)"seg-id-set")))).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"column"), 11}));
    public static final Var const__4 = RT.var((String)"datomic.backup", (String)"substorage");
    public static final Var const__5 = RT.var((String)"datomic.queue", (String)"queue-seq");
    public static final Var const__8 = RT.var((String)"datomic.backup", (String)"prefixes");
    public static final Var const__11 = RT.var((String)"clojure.core", (String)"seq?");
    public static final Var const__12 = RT.var((String)"clojure.core", (String)"seq");
    public static final Keyword const__14 = RT.keyword(null, (String)"fill");
    public static final Keyword const__15 = RT.keyword(null, (String)"done");
    public static final Keyword const__16 = RT.keyword(null, (String)"drain");
    public static final Var const__17 = RT.var((String)"clojure.core", (String)"future-call");
    public static final Var const__18 = RT.var((String)"clojure.core", (String)"mapv");
    public static final Var const__21 = RT.var((String)"clojure.core", (String)"deref");
    public static final Var const__23 = RT.var((String)"clojure.core", (String)"chunked-seq?");
    public static final Var const__24 = RT.var((String)"clojure.core", (String)"chunk-first");
    public static final Var const__25 = RT.var((String)"clojure.core", (String)"chunk-rest");
    public static final Var const__27 = RT.var((String)"clojure.core", (String)"first");
    public static final Var const__28 = RT.var((String)"clojure.core", (String)"next");

    public static Object invokeStatic(Object backup_storage, Object seg_id_set) {
        Object futs;
        Object drain;
        Object object;
        Object object2 = ((IFn)const__0.getRawRoot()).invoke(seg_id_set);
        if (object2 == null || object2 == Boolean.FALSE) {
            throw (Throwable)((Object)new AssertionError(((IFn)const__1.getRawRoot()).invoke((Object)"Assert failed: ", ((IFn)const__2.getRawRoot()).invoke(const__3))));
        }
        Object object3 = backup_storage;
        backup_storage = null;
        Object value_substorage = ((IFn)const__4.getRawRoot()).invoke(object3, (Object)"values");
        Object map__20319 = ((IFn)const__5.getRawRoot()).invoke((Object)Numbers.num((long)Numbers.multiply((long)Numbers.multiply((long)RT.count((Object)const__8.getRawRoot()), (long)1000L), (long)10L)));
        Object object4 = ((IFn)const__11.getRawRoot()).invoke(map__20319);
        if (object4 != null && object4 != Boolean.FALSE) {
            Object object5 = map__20319;
            map__20319 = null;
            object = PersistentHashMap.create((ISeq)((ISeq)((IFn)const__12.getRawRoot()).invoke(object5)));
        } else {
            object = map__20319;
            map__20319 = null;
        }
        Object map__203192 = object;
        Object fill = RT.get((Object)map__203192, (Object)const__14);
        Object done = RT.get((Object)map__203192, (Object)const__15);
        Object object6 = map__203192;
        map__203192 = null;
        Object object7 = drain = RT.get((Object)object6, (Object)const__16);
        drain = null;
        Object ms_fut = ((IFn)const__17.getRawRoot()).invoke((Object)new backup$missing_seg_ids$fn__20320(object7, seg_id_set));
        Object object8 = seg_id_set;
        seg_id_set = null;
        Object object9 = value_substorage;
        value_substorage = null;
        Object object10 = fill;
        fill = null;
        Object object11 = futs = ((IFn)const__18.getRawRoot()).invoke((Object)new backup$missing_seg_ids$fn__20324(object8, object9, object10), const__8.getRawRoot());
        futs = null;
        Object seq_20339 = ((IFn)const__12.getRawRoot()).invoke(object11);
        Object chunk_20340 = null;
        long count_20341 = 0L;
        long i_20342 = 0L;
        while (true) {
            Object fut;
            Object temp__5457__auto__20345;
            if (i_20342 < count_20341) {
                Object fut2;
                Object object12 = fut2 = ((Indexed)chunk_20340).nth(RT.intCast((long)i_20342));
                fut2 = null;
                ((IFn)const__21.getRawRoot()).invoke(object12);
                Object object13 = seq_20339;
                seq_20339 = null;
                Object object14 = chunk_20340;
                chunk_20340 = null;
                ++i_20342;
                chunk_20340 = object14;
                seq_20339 = object13;
                continue;
            }
            Object object15 = seq_20339;
            seq_20339 = null;
            Object object16 = temp__5457__auto__20345 = ((IFn)const__12.getRawRoot()).invoke(object15);
            if (object16 == null || object16 == Boolean.FALSE) break;
            Object object17 = temp__5457__auto__20345;
            temp__5457__auto__20345 = null;
            Object seq_203392 = object17;
            Object object18 = ((IFn)const__23.getRawRoot()).invoke(seq_203392);
            if (object18 != null && object18 != Boolean.FALSE) {
                Object c__5719__auto__20344 = ((IFn)const__24.getRawRoot()).invoke(seq_203392);
                Object object19 = seq_203392;
                seq_203392 = null;
                Object object20 = c__5719__auto__20344;
                Object object21 = c__5719__auto__20344;
                c__5719__auto__20344 = null;
                i_20342 = RT.intCast((long)0L);
                count_20341 = RT.intCast((int)RT.count((Object)object21));
                chunk_20340 = object20;
                seq_20339 = ((IFn)const__25.getRawRoot()).invoke(object19);
                continue;
            }
            Object object22 = fut = ((IFn)const__27.getRawRoot()).invoke(seq_203392);
            fut = null;
            ((IFn)const__21.getRawRoot()).invoke(object22);
            Object object23 = seq_203392;
            seq_203392 = null;
            i_20342 = 0L;
            count_20341 = 0L;
            chunk_20340 = null;
            seq_20339 = ((IFn)const__28.getRawRoot()).invoke(object23);
        }
        Object object24 = done;
        done = null;
        ((IFn)object24).invoke();
        Object object25 = ms_fut;
        ms_fut = null;
        return ((IFn)const__21.getRawRoot()).invoke(object25);
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return backup$missing_seg_ids.invokeStatic(object3, object4);
    }
}

