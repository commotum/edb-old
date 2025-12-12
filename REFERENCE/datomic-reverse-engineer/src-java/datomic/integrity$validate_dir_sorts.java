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
import datomic.integrity$validate_dir_sorts$iter__22044__22050;

public final class integrity$validate_dir_sorts
extends AFunction {
    public static final AFn const__3 = (AFn)Tuple.create((Object)RT.keyword(null, (String)"mid-index"), (Object)RT.keyword(null, (String)"main"), (Object)RT.keyword(null, (String)"history"));
    public static final Var const__4 = RT.var((String)"clojure.core", (String)"print");
    public static final Var const__5 = RT.var((String)"clojure.core", (String)"seq");
    public static final Var const__10 = RT.var((String)"datomic.integrity", (String)"unsorted-dirs");
    public static final Var const__11 = RT.var((String)"datomic.integrity", (String)"progress-dot-fn");
    public static final Object const__12 = 10000L;
    public static final Var const__13 = RT.var((String)"clojure.core", (String)"println");
    public static final Var const__14 = RT.var((String)"clojure.core", (String)"ex-info");
    public static final Var const__15 = RT.var((String)"clojure.core", (String)"str");
    public static final Var const__16 = RT.var((String)"clojure.core", (String)"first");
    public static final Keyword const__17 = RT.keyword(null, (String)"pairs");
    public static final Keyword const__18 = RT.keyword(null, (String)"tier");
    public static final Keyword const__19 = RT.keyword(null, (String)"sort");
    public static final Var const__21 = RT.var((String)"clojure.core", (String)"chunked-seq?");
    public static final Var const__22 = RT.var((String)"clojure.core", (String)"chunk-first");
    public static final Var const__23 = RT.var((String)"clojure.core", (String)"chunk-rest");
    public static final Var const__26 = RT.var((String)"clojure.core", (String)"next");

    public static Object invokeStatic(Object db2) {
        integrity$validate_dir_sorts$iter__22044__22050 iter__6025__auto__22078;
        integrity$validate_dir_sorts$iter__22044__22050 integrity$validate_dir_sorts$iter__22044__22050 = iter__6025__auto__22078 = new integrity$validate_dir_sorts$iter__22044__22050();
        iter__6025__auto__22078 = null;
        Object tses = ((IFn)integrity$validate_dir_sorts$iter__22044__22050).invoke((Object)const__3);
        ((IFn)const__4.getRawRoot()).invoke((Object)"Validating dirs for ");
        Object object = tses;
        tses = null;
        Object seq_22067 = ((IFn)const__5.getRawRoot()).invoke(object);
        Object chunk_22068 = null;
        long count_22069 = 0L;
        long i_22070 = 0L;
        while (true) {
            Object temp__5457__auto__22081;
            Object temp__5457__auto__22082;
            if (i_22070 < count_22069) {
                Object temp__5457__auto__22079;
                Object vec__22071 = ((Indexed)chunk_22068).nth(RT.intCast((long)i_22070));
                Object tier = RT.nth((Object)vec__22071, (int)RT.intCast((long)0L), null);
                Object object2 = vec__22071;
                vec__22071 = null;
                Object sort = RT.nth((Object)object2, (int)RT.intCast((long)1L), null);
                ((IFn)const__4.getRawRoot()).invoke((Object)"[", tier, sort, (Object)"]");
                Object object3 = temp__5457__auto__22079 = ((IFn)const__10.getRawRoot()).invoke(db2, tier, sort, ((IFn)const__11.getRawRoot()).invoke(const__12));
                if (object3 != null && object3 != Boolean.FALSE) {
                    Object object4 = temp__5457__auto__22079;
                    temp__5457__auto__22079 = null;
                    Object s = object4;
                    ((IFn)const__13.getRawRoot()).invoke();
                    Object object5 = ((IFn)const__15.getRawRoot()).invoke((Object)"Disorderly dirs in ", tier, sort, ((IFn)const__16.getRawRoot()).invoke(s));
                    Object[] objectArray = new Object[6];
                    objectArray[0] = const__17;
                    Object object6 = s;
                    s = null;
                    objectArray[1] = object6;
                    objectArray[2] = const__18;
                    Object object7 = tier;
                    tier = null;
                    objectArray[3] = object7;
                    objectArray[4] = const__19;
                    Object object8 = sort;
                    sort = null;
                    objectArray[5] = object8;
                    throw (Throwable)((IFn)const__14.getRawRoot()).invoke(object5, (Object)RT.mapUniqueKeys((Object[])objectArray));
                }
                Object object9 = seq_22067;
                seq_22067 = null;
                Object object10 = chunk_22068;
                chunk_22068 = null;
                ++i_22070;
                chunk_22068 = object10;
                seq_22067 = object9;
                continue;
            }
            Object object11 = seq_22067;
            seq_22067 = null;
            Object object12 = temp__5457__auto__22082 = ((IFn)const__5.getRawRoot()).invoke(object11);
            if (object12 == null || object12 == Boolean.FALSE) break;
            Object object13 = temp__5457__auto__22082;
            temp__5457__auto__22082 = null;
            Object seq_220672 = object13;
            Object object14 = ((IFn)const__21.getRawRoot()).invoke(seq_220672);
            if (object14 != null && object14 != Boolean.FALSE) {
                Object c__5719__auto__22080 = ((IFn)const__22.getRawRoot()).invoke(seq_220672);
                Object object15 = seq_220672;
                seq_220672 = null;
                Object object16 = c__5719__auto__22080;
                Object object17 = c__5719__auto__22080;
                c__5719__auto__22080 = null;
                i_22070 = RT.intCast((long)0L);
                count_22069 = RT.intCast((int)RT.count((Object)object17));
                chunk_22068 = object16;
                seq_22067 = ((IFn)const__23.getRawRoot()).invoke(object15);
                continue;
            }
            Object vec__22074 = ((IFn)const__16.getRawRoot()).invoke(seq_220672);
            Object tier = RT.nth((Object)vec__22074, (int)RT.intCast((long)0L), null);
            Object object18 = vec__22074;
            vec__22074 = null;
            Object sort = RT.nth((Object)object18, (int)RT.intCast((long)1L), null);
            ((IFn)const__4.getRawRoot()).invoke((Object)"[", tier, sort, (Object)"]");
            Object object19 = temp__5457__auto__22081 = ((IFn)const__10.getRawRoot()).invoke(db2, tier, sort, ((IFn)const__11.getRawRoot()).invoke(const__12));
            if (object19 != null && object19 != Boolean.FALSE) {
                Object object20 = temp__5457__auto__22081;
                temp__5457__auto__22081 = null;
                Object s = object20;
                ((IFn)const__13.getRawRoot()).invoke();
                Object object21 = ((IFn)const__15.getRawRoot()).invoke((Object)"Disorderly dirs in ", tier, sort, ((IFn)const__16.getRawRoot()).invoke(s));
                Object[] objectArray = new Object[6];
                objectArray[0] = const__17;
                Object object22 = s;
                s = null;
                objectArray[1] = object22;
                objectArray[2] = const__18;
                Object object23 = tier;
                tier = null;
                objectArray[3] = object23;
                objectArray[4] = const__19;
                Object object24 = sort;
                sort = null;
                objectArray[5] = object24;
                throw (Throwable)((IFn)const__14.getRawRoot()).invoke(object21, (Object)RT.mapUniqueKeys((Object[])objectArray));
            }
            Object object25 = seq_220672;
            seq_220672 = null;
            i_22070 = 0L;
            count_22069 = 0L;
            chunk_22068 = null;
            seq_22067 = ((IFn)const__26.getRawRoot()).invoke(object25);
        }
        return ((IFn)const__13.getRawRoot()).invoke();
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return integrity$validate_dir_sorts.invokeStatic(object2);
    }
}

