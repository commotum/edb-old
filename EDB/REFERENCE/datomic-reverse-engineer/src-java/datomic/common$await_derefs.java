/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.Indexed
 *  clojure.lang.Numbers
 *  clojure.lang.RT
 *  clojure.lang.Util
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Indexed;
import clojure.lang.Numbers;
import clojure.lang.RT;
import clojure.lang.Util;
import clojure.lang.Var;

public final class common$await_derefs
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"seq");
    public static final Var const__3 = RT.var((String)"clojure.core", (String)"deref");
    public static final Var const__5 = RT.var((String)"clojure.core", (String)"chunked-seq?");
    public static final Var const__6 = RT.var((String)"clojure.core", (String)"chunk-first");
    public static final Var const__7 = RT.var((String)"clojure.core", (String)"chunk-rest");
    public static final Var const__10 = RT.var((String)"clojure.core", (String)"first");
    public static final Var const__11 = RT.var((String)"clojure.core", (String)"next");

    public static Object invokeStatic(Object msec, Object coll) {
        Boolean bl;
        block2: {
            Object G__9097;
            Object vec__9098;
            Object timed_out = new Object();
            Object object = msec;
            msec = null;
            Number limit2 = Numbers.unchecked_add((Object)object, (long)System.currentTimeMillis());
            Object object2 = coll;
            coll = null;
            Object object3 = vec__9098 = (G__9097 = object2);
            vec__9098 = null;
            Object seq__9099 = ((IFn)const__0.getRawRoot()).invoke(object3);
            Object first__9100 = ((IFn)const__10.getRawRoot()).invoke(seq__9099);
            Object object4 = seq__9099;
            seq__9099 = null;
            Object seq__90992 = ((IFn)const__11.getRawRoot()).invoke(object4);
            first__9100 = null;
            seq__90992 = null;
            Object object5 = G__9097;
            G__9097 = null;
            Object G__90972 = object5;
            while (true) {
                Object vec__9101;
                Object object6 = G__90972;
                G__90972 = null;
                Object object7 = vec__9101 = object6;
                vec__9101 = null;
                Object seq__9102 = ((IFn)const__0.getRawRoot()).invoke(object7);
                Object first__9103 = ((IFn)const__10.getRawRoot()).invoke(seq__9102);
                Object object8 = seq__9102;
                seq__9102 = null;
                Object seq__91022 = ((IFn)const__11.getRawRoot()).invoke(object8);
                Object object9 = first__9103;
                first__9103 = null;
                Object item = object9;
                Object object10 = seq__91022;
                seq__91022 = null;
                Object more = object10;
                Object object11 = item;
                if (object11 == null || object11 == Boolean.FALSE) break;
                Object object12 = item;
                item = null;
                if (Util.equiv((Object)timed_out, (Object)((IFn)const__3.getRawRoot()).invoke(object12, (Object)Numbers.unchecked_minus((Object)limit2, (long)System.currentTimeMillis()), timed_out))) {
                    bl = Boolean.FALSE;
                    break block2;
                }
                Object object13 = more;
                more = null;
                G__90972 = object13;
            }
            bl = Boolean.TRUE;
        }
        return bl;
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return common$await_derefs.invokeStatic(object3, object4);
    }

    public static Object invokeStatic(Object coll) {
        Object object = coll;
        coll = null;
        Object seq_9090 = ((IFn)const__0.getRawRoot()).invoke(object);
        Object chunk_9091 = null;
        long count_9092 = 0L;
        long i_9093 = 0L;
        while (true) {
            Object c;
            Object temp__5457__auto__9106;
            if (i_9093 < count_9092) {
                Object c2;
                Object object2 = c2 = ((Indexed)chunk_9091).nth(RT.uncheckedIntCast((long)i_9093));
                c2 = null;
                ((IFn)const__3.getRawRoot()).invoke(object2);
                Object object3 = seq_9090;
                seq_9090 = null;
                Object object4 = chunk_9091;
                chunk_9091 = null;
                ++i_9093;
                chunk_9091 = object4;
                seq_9090 = object3;
                continue;
            }
            Object object5 = seq_9090;
            seq_9090 = null;
            Object object6 = temp__5457__auto__9106 = ((IFn)const__0.getRawRoot()).invoke(object5);
            if (object6 == null || object6 == Boolean.FALSE) break;
            Object object7 = temp__5457__auto__9106;
            temp__5457__auto__9106 = null;
            Object seq_90902 = object7;
            Object object8 = ((IFn)const__5.getRawRoot()).invoke(seq_90902);
            if (object8 != null && object8 != Boolean.FALSE) {
                Object c__5719__auto__9105 = ((IFn)const__6.getRawRoot()).invoke(seq_90902);
                Object object9 = seq_90902;
                seq_90902 = null;
                Object object10 = c__5719__auto__9105;
                Object object11 = c__5719__auto__9105;
                c__5719__auto__9105 = null;
                i_9093 = (int)0L;
                count_9092 = RT.count((Object)object11);
                chunk_9091 = object10;
                seq_9090 = ((IFn)const__7.getRawRoot()).invoke(object9);
                continue;
            }
            Object object12 = c = ((IFn)const__10.getRawRoot()).invoke(seq_90902);
            c = null;
            ((IFn)const__3.getRawRoot()).invoke(object12);
            Object object13 = seq_90902;
            seq_90902 = null;
            i_9093 = 0L;
            count_9092 = 0L;
            chunk_9091 = null;
            seq_9090 = ((IFn)const__11.getRawRoot()).invoke(object13);
        }
        return Boolean.TRUE;
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return common$await_derefs.invokeStatic(object2);
    }
}

