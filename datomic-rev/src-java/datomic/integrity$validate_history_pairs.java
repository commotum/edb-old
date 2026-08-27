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

public final class integrity$validate_history_pairs
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"seq");
    public static final AFn const__5 = (AFn)Tuple.create((Object)RT.keyword(null, (String)"eavt"), (Object)RT.keyword(null, (String)"aevt"), (Object)RT.keyword(null, (String)"avet"), (Object)RT.keyword(null, (String)"vaet"));
    public static final Var const__8 = RT.var((String)"clojure.core", (String)"println");
    public static final Var const__9 = RT.var((String)"datomic.integrity", (String)"unpaired-history-assertions");
    public static final Var const__10 = RT.var((String)"datomic.integrity", (String)"progress-dot-fn");
    public static final Object const__11 = 100000L;
    public static final Var const__12 = RT.var((String)"clojure.core", (String)"ex-info");
    public static final Var const__13 = RT.var((String)"clojure.core", (String)"str");
    public static final Var const__14 = RT.var((String)"clojure.core", (String)"first");
    public static final Keyword const__15 = RT.keyword(null, (String)"assertions");
    public static final Var const__17 = RT.var((String)"clojure.core", (String)"chunked-seq?");
    public static final Var const__18 = RT.var((String)"clojure.core", (String)"chunk-first");
    public static final Var const__19 = RT.var((String)"clojure.core", (String)"chunk-rest");
    public static final Var const__22 = RT.var((String)"clojure.core", (String)"next");

    public static Object invokeStatic(Object db2) {
        Object seq_22108 = ((IFn)const__0.getRawRoot()).invoke((Object)const__5);
        Object chunk_22109 = null;
        long count_22110 = 0L;
        long i_22111 = 0L;
        while (true) {
            Object temp__5457__auto__22115;
            Object temp__5457__auto__22116;
            if (i_22111 < count_22110) {
                Object temp__5457__auto__22113;
                Object sort = ((Indexed)chunk_22109).nth(RT.intCast((long)i_22111));
                ((IFn)const__8.getRawRoot()).invoke((Object)"Validating history pairs ", sort);
                Object object = sort;
                sort = null;
                Object object2 = temp__5457__auto__22113 = ((IFn)const__9.getRawRoot()).invoke(db2, object, ((IFn)const__10.getRawRoot()).invoke(const__11));
                if (object2 != null && object2 != Boolean.FALSE) {
                    Object object3 = temp__5457__auto__22113;
                    temp__5457__auto__22113 = null;
                    Object s = object3;
                    Object object4 = ((IFn)const__13.getRawRoot()).invoke((Object)"Unpaired history assertions ", ((IFn)const__14.getRawRoot()).invoke(s));
                    Object[] objectArray = new Object[2];
                    objectArray[0] = const__15;
                    Object object5 = s;
                    s = null;
                    objectArray[1] = object5;
                    throw (Throwable)((IFn)const__12.getRawRoot()).invoke(object4, (Object)RT.mapUniqueKeys((Object[])objectArray));
                }
                ((IFn)const__8.getRawRoot()).invoke();
                Object object6 = seq_22108;
                seq_22108 = null;
                Object object7 = chunk_22109;
                chunk_22109 = null;
                ++i_22111;
                chunk_22109 = object7;
                seq_22108 = object6;
                continue;
            }
            Object object = seq_22108;
            seq_22108 = null;
            Object object8 = temp__5457__auto__22116 = ((IFn)const__0.getRawRoot()).invoke(object);
            if (object8 == null || object8 == Boolean.FALSE) break;
            Object object9 = temp__5457__auto__22116;
            temp__5457__auto__22116 = null;
            Object seq_221082 = object9;
            Object object10 = ((IFn)const__17.getRawRoot()).invoke(seq_221082);
            if (object10 != null && object10 != Boolean.FALSE) {
                Object c__5719__auto__22114 = ((IFn)const__18.getRawRoot()).invoke(seq_221082);
                Object object11 = seq_221082;
                seq_221082 = null;
                Object object12 = c__5719__auto__22114;
                Object object13 = c__5719__auto__22114;
                c__5719__auto__22114 = null;
                i_22111 = RT.intCast((long)0L);
                count_22110 = RT.intCast((int)RT.count((Object)object13));
                chunk_22109 = object12;
                seq_22108 = ((IFn)const__19.getRawRoot()).invoke(object11);
                continue;
            }
            Object sort = ((IFn)const__14.getRawRoot()).invoke(seq_221082);
            ((IFn)const__8.getRawRoot()).invoke((Object)"Validating history pairs ", sort);
            Object object14 = sort;
            sort = null;
            Object object15 = temp__5457__auto__22115 = ((IFn)const__9.getRawRoot()).invoke(db2, object14, ((IFn)const__10.getRawRoot()).invoke(const__11));
            if (object15 != null && object15 != Boolean.FALSE) {
                Object object16 = temp__5457__auto__22115;
                temp__5457__auto__22115 = null;
                Object s = object16;
                Object object17 = ((IFn)const__13.getRawRoot()).invoke((Object)"Unpaired history assertions ", ((IFn)const__14.getRawRoot()).invoke(s));
                Object[] objectArray = new Object[2];
                objectArray[0] = const__15;
                Object object18 = s;
                s = null;
                objectArray[1] = object18;
                throw (Throwable)((IFn)const__12.getRawRoot()).invoke(object17, (Object)RT.mapUniqueKeys((Object[])objectArray));
            }
            ((IFn)const__8.getRawRoot()).invoke();
            Object object19 = seq_221082;
            seq_221082 = null;
            i_22111 = 0L;
            count_22110 = 0L;
            chunk_22109 = null;
            seq_22108 = ((IFn)const__22.getRawRoot()).invoke(object19);
        }
        return null;
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return integrity$validate_history_pairs.invokeStatic(object2);
    }
}

