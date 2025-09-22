/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFn
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.Indexed
 *  clojure.lang.Keyword
 *  clojure.lang.RT
 *  clojure.lang.Tuple
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFn;
import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Indexed;
import clojure.lang.Keyword;
import clojure.lang.RT;
import clojure.lang.Tuple;
import clojure.lang.Var;

public final class integrity$validate_index_sorts
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.integrity", (String)"validate-index-sorts");
    public static final Keyword const__1 = RT.keyword(null, (String)"strict");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"seq");
    public static final AFn const__7 = (AFn)Tuple.create((Object)RT.keyword(null, (String)"eavt"), (Object)RT.keyword(null, (String)"aevt"), (Object)RT.keyword(null, (String)"avet"), (Object)RT.keyword(null, (String)"vaet"));
    public static final Var const__10 = RT.var((String)"clojure.core", (String)"println");
    public static final Var const__11 = RT.var((String)"datomic.integrity", (String)"unsorted-datoms");
    public static final Var const__12 = RT.var((String)"datomic.integrity", (String)"progress-dot-fn");
    public static final Object const__13 = 10000L;
    public static final Var const__14 = RT.var((String)"clojure.core", (String)"ex-info");
    public static final Var const__15 = RT.var((String)"clojure.core", (String)"str");
    public static final Var const__16 = RT.var((String)"clojure.core", (String)"first");
    public static final Keyword const__17 = RT.keyword(null, (String)"pairs");
    public static final Var const__19 = RT.var((String)"clojure.core", (String)"chunked-seq?");
    public static final Var const__20 = RT.var((String)"clojure.core", (String)"chunk-first");
    public static final Var const__21 = RT.var((String)"clojure.core", (String)"chunk-rest");
    public static final Var const__24 = RT.var((String)"clojure.core", (String)"next");

    public static Object invokeStatic(Object db2, Object order) {
        Object seq_22087 = ((IFn)const__2.getRawRoot()).invoke((Object)const__7);
        Object chunk_22088 = null;
        long count_22089 = 0L;
        long i_22090 = 0L;
        while (true) {
            Object temp__5457__auto__22094;
            Object temp__5457__auto__22095;
            if (i_22090 < count_22089) {
                Object temp__5457__auto__22092;
                Object sort = ((Indexed)chunk_22088).nth(RT.intCast((long)i_22090));
                ((IFn)const__10.getRawRoot()).invoke((Object)"Validating ", sort);
                Object object = sort;
                sort = null;
                Object object2 = temp__5457__auto__22092 = ((IFn)const__11.getRawRoot()).invoke(db2, object, order, ((IFn)const__12.getRawRoot()).invoke(const__13));
                if (object2 != null && object2 != Boolean.FALSE) {
                    Object object3 = temp__5457__auto__22092;
                    temp__5457__auto__22092 = null;
                    Object s = object3;
                    Object object4 = ((IFn)const__15.getRawRoot()).invoke((Object)"Disorderly datom pairs ", ((IFn)const__16.getRawRoot()).invoke(s));
                    Object[] objectArray = new Object[2];
                    objectArray[0] = const__17;
                    Object object5 = s;
                    s = null;
                    objectArray[1] = object5;
                    throw (Throwable)((IFn)const__14.getRawRoot()).invoke(object4, (Object)RT.mapUniqueKeys((Object[])objectArray));
                }
                ((IFn)const__10.getRawRoot()).invoke();
                Object object6 = seq_22087;
                seq_22087 = null;
                Object object7 = chunk_22088;
                chunk_22088 = null;
                ++i_22090;
                chunk_22088 = object7;
                seq_22087 = object6;
                continue;
            }
            Object object = seq_22087;
            seq_22087 = null;
            Object object8 = temp__5457__auto__22095 = ((IFn)const__2.getRawRoot()).invoke(object);
            if (object8 == null || object8 == Boolean.FALSE) break;
            Object object9 = temp__5457__auto__22095;
            temp__5457__auto__22095 = null;
            Object seq_220872 = object9;
            Object object10 = ((IFn)const__19.getRawRoot()).invoke(seq_220872);
            if (object10 != null && object10 != Boolean.FALSE) {
                Object c__5719__auto__22093 = ((IFn)const__20.getRawRoot()).invoke(seq_220872);
                Object object11 = seq_220872;
                seq_220872 = null;
                Object object12 = c__5719__auto__22093;
                Object object13 = c__5719__auto__22093;
                c__5719__auto__22093 = null;
                i_22090 = RT.intCast((long)0L);
                count_22089 = RT.intCast((int)RT.count((Object)object13));
                chunk_22088 = object12;
                seq_22087 = ((IFn)const__21.getRawRoot()).invoke(object11);
                continue;
            }
            Object sort = ((IFn)const__16.getRawRoot()).invoke(seq_220872);
            ((IFn)const__10.getRawRoot()).invoke((Object)"Validating ", sort);
            Object object14 = sort;
            sort = null;
            Object object15 = temp__5457__auto__22094 = ((IFn)const__11.getRawRoot()).invoke(db2, object14, order, ((IFn)const__12.getRawRoot()).invoke(const__13));
            if (object15 != null && object15 != Boolean.FALSE) {
                Object object16 = temp__5457__auto__22094;
                temp__5457__auto__22094 = null;
                Object s = object16;
                Object object17 = ((IFn)const__15.getRawRoot()).invoke((Object)"Disorderly datom pairs ", ((IFn)const__16.getRawRoot()).invoke(s));
                Object[] objectArray = new Object[2];
                objectArray[0] = const__17;
                Object object18 = s;
                s = null;
                objectArray[1] = object18;
                throw (Throwable)((IFn)const__14.getRawRoot()).invoke(object17, (Object)RT.mapUniqueKeys((Object[])objectArray));
            }
            ((IFn)const__10.getRawRoot()).invoke();
            Object object19 = seq_220872;
            seq_220872 = null;
            i_22090 = 0L;
            count_22089 = 0L;
            chunk_22088 = null;
            seq_22087 = ((IFn)const__24.getRawRoot()).invoke(object19);
        }
        return null;
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return integrity$validate_index_sorts.invokeStatic(object3, object4);
    }

    public static Object invokeStatic(Object db2) {
        Object object = db2;
        db2 = null;
        return ((IFn)const__0.getRawRoot()).invoke(object, (Object)const__1);
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return integrity$validate_index_sorts.invokeStatic(object2);
    }
}

