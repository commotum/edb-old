/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFn
 *  clojure.lang.IFn
 *  clojure.lang.ISeq
 *  clojure.lang.Keyword
 *  clojure.lang.RT
 *  clojure.lang.RestFn
 *  clojure.lang.Symbol
 *  clojure.lang.Tuple
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFn;
import clojure.lang.IFn;
import clojure.lang.ISeq;
import clojure.lang.Keyword;
import clojure.lang.RT;
import clojure.lang.RestFn;
import clojure.lang.Symbol;
import clojure.lang.Tuple;
import clojure.lang.Var;

public final class error$with_unwind_STAR_
extends RestFn {
    public static final Object const__2 = 0L;
    public static final Var const__3 = RT.var((String)"clojure.core", (String)"seq");
    public static final Var const__4 = RT.var((String)"clojure.core", (String)"concat");
    public static final Var const__5 = RT.var((String)"clojure.core", (String)"list");
    public static final AFn const__6 = (AFn)Symbol.intern(null, (String)"do");
    public static final Var const__7 = RT.var((String)"clojure.core", (String)"symbol?");
    public static final AFn const__8 = (AFn)Symbol.intern(null, (String)"try");
    public static final Object const__9 = 2L;
    public static final AFn const__10 = (AFn)Symbol.intern(null, (String)"catch");
    public static final AFn const__11 = (AFn)Symbol.intern(null, (String)"java.lang.Throwable");
    public static final AFn const__12 = (AFn)Symbol.intern(null, (String)"t__708__auto__");
    public static final AFn const__13 = (AFn)Symbol.intern((String)"datomic.error", (String)"report");
    public static final AFn const__14 = (AFn)Symbol.intern((String)"clojure.core", (String)"let");
    public static final Var const__15 = RT.var((String)"clojure.core", (String)"into");
    public static final Var const__16 = RT.var((String)"clojure.core", (String)"subvec");
    public static final AFn const__17 = (AFn)Symbol.intern((String)"datomic.error", (String)"runonce");
    public static final AFn const__18 = (AFn)Symbol.intern((String)"clojure.core", (String)"fn");
    public static final Var const__19 = RT.var((String)"clojure.core", (String)"apply");
    public static final Var const__20 = RT.var((String)"clojure.core", (String)"vector");
    public static final AFn const__21 = (AFn)Symbol.intern(null, (String)"try");
    public static final AFn const__22 = (AFn)Symbol.intern((String)"datomic.error", (String)"with-unwind*");
    public static final Object const__23 = 3L;
    public static final AFn const__24 = (AFn)Symbol.intern(null, (String)"catch");
    public static final AFn const__25 = (AFn)Symbol.intern(null, (String)"java.lang.Throwable");
    public static final AFn const__26 = (AFn)Symbol.intern(null, (String)"t__709__auto__");
    public static final AFn const__27 = (AFn)Symbol.intern(null, (String)"throw");
    public static final Keyword const__28 = RT.keyword(null, (String)"else");

    public static Object invokeStatic(Object _AMPERSAND_form, Object _AMPERSAND_env, Object unwind_prev, Object unwind_all, Object bindings, ISeq body) {
        Object object;
        if ((long)RT.count((Object)bindings) == 0L) {
            ISeq iSeq = body;
            body = null;
            object = ((IFn)const__3.getRawRoot()).invoke(((IFn)const__4.getRawRoot()).invoke(((IFn)const__5.getRawRoot()).invoke((Object)const__6), (Object)iSeq));
        } else {
            Object object2 = ((IFn)const__7.getRawRoot()).invoke(((IFn)bindings).invoke(const__2));
            if (object2 != null && object2 != Boolean.FALSE) {
                Object object3;
                Object unwind_one = ((IFn)const__3.getRawRoot()).invoke(((IFn)const__4.getRawRoot()).invoke(((IFn)const__5.getRawRoot()).invoke((Object)const__8), ((IFn)const__5.getRawRoot()).invoke(((IFn)const__3.getRawRoot()).invoke(((IFn)const__4.getRawRoot()).invoke(((IFn)const__5.getRawRoot()).invoke(((IFn)bindings).invoke(const__9)), ((IFn)const__5.getRawRoot()).invoke(((IFn)bindings).invoke(const__2))))), ((IFn)const__5.getRawRoot()).invoke(((IFn)const__3.getRawRoot()).invoke(((IFn)const__4.getRawRoot()).invoke(((IFn)const__5.getRawRoot()).invoke((Object)const__10), ((IFn)const__5.getRawRoot()).invoke((Object)const__11), ((IFn)const__5.getRawRoot()).invoke((Object)const__12), ((IFn)const__5.getRawRoot()).invoke(((IFn)const__3.getRawRoot()).invoke(((IFn)const__4.getRawRoot()).invoke(((IFn)const__5.getRawRoot()).invoke((Object)const__13), ((IFn)const__5.getRawRoot()).invoke((Object)const__12)))))))));
                IFn iFn = (IFn)const__3.getRawRoot();
                IFn iFn2 = (IFn)const__4.getRawRoot();
                Object object4 = ((IFn)const__5.getRawRoot()).invoke((Object)const__14);
                IFn iFn3 = (IFn)const__5.getRawRoot();
                IFn iFn4 = (IFn)const__15.getRawRoot();
                Object object5 = ((IFn)const__16.getRawRoot()).invoke(bindings, const__2, const__9);
                IFn iFn5 = (IFn)const__3.getRawRoot();
                IFn iFn6 = (IFn)const__4.getRawRoot();
                Object object6 = ((IFn)const__5.getRawRoot()).invoke((Object)const__17);
                IFn iFn7 = (IFn)const__5.getRawRoot();
                IFn iFn8 = (IFn)const__3.getRawRoot();
                IFn iFn9 = (IFn)const__4.getRawRoot();
                Object object7 = ((IFn)const__5.getRawRoot()).invoke((Object)const__18);
                Object object8 = ((IFn)const__5.getRawRoot()).invoke(((IFn)const__19.getRawRoot()).invoke(const__20.getRawRoot(), ((IFn)const__3.getRawRoot()).invoke(((IFn)const__4.getRawRoot()).invoke())));
                Object object9 = ((IFn)const__5.getRawRoot()).invoke(unwind_one);
                IFn iFn10 = (IFn)const__5.getRawRoot();
                Object object10 = unwind_prev;
                if (object10 != null && object10 != Boolean.FALSE) {
                    Object object11 = unwind_prev;
                    unwind_prev = null;
                    object3 = ((IFn)const__3.getRawRoot()).invoke(((IFn)const__4.getRawRoot()).invoke(((IFn)const__5.getRawRoot()).invoke(object11)));
                } else {
                    object3 = null;
                }
                Object object12 = iFn3.invoke(iFn4.invoke(object5, (Object)Tuple.create((Object)unwind_all, (Object)iFn5.invoke(iFn6.invoke(object6, iFn7.invoke(iFn8.invoke(iFn9.invoke(object7, object8, object9, iFn10.invoke(object3)))))))));
                Object object13 = ((IFn)const__5.getRawRoot()).invoke(unwind_all);
                Object object14 = unwind_all;
                unwind_all = null;
                Object object15 = bindings;
                bindings = null;
                ISeq iSeq = body;
                body = null;
                Object object16 = unwind_one;
                unwind_one = null;
                object = iFn.invoke(iFn2.invoke(object4, object12, ((IFn)const__5.getRawRoot()).invoke(((IFn)const__3.getRawRoot()).invoke(((IFn)const__4.getRawRoot()).invoke(((IFn)const__5.getRawRoot()).invoke((Object)const__21), ((IFn)const__5.getRawRoot()).invoke(((IFn)const__3.getRawRoot()).invoke(((IFn)const__4.getRawRoot()).invoke(((IFn)const__5.getRawRoot()).invoke((Object)const__22), object13, ((IFn)const__5.getRawRoot()).invoke(object14), ((IFn)const__5.getRawRoot()).invoke(((IFn)const__16.getRawRoot()).invoke(object15, const__23)), (Object)iSeq))), ((IFn)const__5.getRawRoot()).invoke(((IFn)const__3.getRawRoot()).invoke(((IFn)const__4.getRawRoot()).invoke(((IFn)const__5.getRawRoot()).invoke((Object)const__24), ((IFn)const__5.getRawRoot()).invoke((Object)const__25), ((IFn)const__5.getRawRoot()).invoke((Object)const__26), ((IFn)const__5.getRawRoot()).invoke(object16), ((IFn)const__5.getRawRoot()).invoke(((IFn)const__3.getRawRoot()).invoke(((IFn)const__4.getRawRoot()).invoke(((IFn)const__5.getRawRoot()).invoke((Object)const__27), ((IFn)const__5.getRawRoot()).invoke((Object)const__26))))))))))));
            } else {
                Keyword keyword = const__28;
                if (keyword != null && keyword != Boolean.FALSE) {
                    throw (Throwable)new IllegalArgumentException("with-unwind only allows symbols in binding names");
                }
                object = null;
            }
        }
        return object;
    }

    public Object doInvoke(Object object, Object object2, Object object3, Object object4, Object object5, Object object6) {
        Object object7 = object;
        object = null;
        Object object8 = object2;
        object2 = null;
        Object object9 = object3;
        object3 = null;
        Object object10 = object4;
        object4 = null;
        Object object11 = object5;
        object5 = null;
        ISeq iSeq = (ISeq)object6;
        object6 = null;
        return error$with_unwind_STAR_.invokeStatic(object7, object8, object9, object10, object11, iSeq);
    }

    public int getRequiredArity() {
        return 5;
    }
}

