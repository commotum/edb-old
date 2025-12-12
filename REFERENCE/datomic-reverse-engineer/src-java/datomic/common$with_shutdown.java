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
import clojure.lang.Var;

public final class common$with_shutdown
extends RestFn {
    public static final Object const__2 = 0L;
    public static final Var const__3 = RT.var((String)"clojure.core", (String)"seq");
    public static final Var const__4 = RT.var((String)"clojure.core", (String)"concat");
    public static final Var const__5 = RT.var((String)"clojure.core", (String)"list");
    public static final AFn const__6 = (AFn)Symbol.intern(null, (String)"do");
    public static final Var const__7 = RT.var((String)"clojure.core", (String)"symbol?");
    public static final AFn const__8 = (AFn)Symbol.intern((String)"clojure.core", (String)"let");
    public static final Var const__9 = RT.var((String)"clojure.core", (String)"subvec");
    public static final Object const__10 = 2L;
    public static final AFn const__11 = (AFn)Symbol.intern(null, (String)"try");
    public static final AFn const__12 = (AFn)Symbol.intern((String)"datomic.common", (String)"with-shutdown");
    public static final AFn const__13 = (AFn)Symbol.intern(null, (String)"finally");
    public static final AFn const__14 = (AFn)Symbol.intern((String)"clojure.core", (String)"deref");
    public static final AFn const__15 = (AFn)Symbol.intern((String)"datomic.common", (String)"async-shutdown");
    public static final Keyword const__16 = RT.keyword(null, (String)"else");

    public static Object invokeStatic(Object _AMPERSAND_form, Object _AMPERSAND_env, Object bindings, ISeq body) {
        Object object;
        if ((long)RT.count((Object)bindings) == 0L) {
            ISeq iSeq = body;
            body = null;
            object = ((IFn)const__3.getRawRoot()).invoke(((IFn)const__4.getRawRoot()).invoke(((IFn)const__5.getRawRoot()).invoke((Object)const__6), (Object)iSeq));
        } else {
            Object object2 = ((IFn)const__7.getRawRoot()).invoke(((IFn)bindings).invoke(const__2));
            if (object2 != null && object2 != Boolean.FALSE) {
                Object object3 = ((IFn)const__5.getRawRoot()).invoke(((IFn)const__9.getRawRoot()).invoke(bindings, const__2, const__10));
                ISeq iSeq = body;
                body = null;
                Object object4 = ((IFn)const__5.getRawRoot()).invoke(((IFn)const__3.getRawRoot()).invoke(((IFn)const__4.getRawRoot()).invoke(((IFn)const__5.getRawRoot()).invoke((Object)const__12), ((IFn)const__5.getRawRoot()).invoke(((IFn)const__9.getRawRoot()).invoke(bindings, const__10)), (Object)iSeq)));
                Object object5 = bindings;
                bindings = null;
                object = ((IFn)const__3.getRawRoot()).invoke(((IFn)const__4.getRawRoot()).invoke(((IFn)const__5.getRawRoot()).invoke((Object)const__8), object3, ((IFn)const__5.getRawRoot()).invoke(((IFn)const__3.getRawRoot()).invoke(((IFn)const__4.getRawRoot()).invoke(((IFn)const__5.getRawRoot()).invoke((Object)const__11), object4, ((IFn)const__5.getRawRoot()).invoke(((IFn)const__3.getRawRoot()).invoke(((IFn)const__4.getRawRoot()).invoke(((IFn)const__5.getRawRoot()).invoke((Object)const__13), ((IFn)const__5.getRawRoot()).invoke(((IFn)const__3.getRawRoot()).invoke(((IFn)const__4.getRawRoot()).invoke(((IFn)const__5.getRawRoot()).invoke((Object)const__14), ((IFn)const__5.getRawRoot()).invoke(((IFn)const__3.getRawRoot()).invoke(((IFn)const__4.getRawRoot()).invoke(((IFn)const__5.getRawRoot()).invoke((Object)const__15), ((IFn)const__5.getRawRoot()).invoke(((IFn)object5).invoke(const__2))))))))))))))));
            } else {
                Keyword keyword = const__16;
                if (keyword != null && keyword != Boolean.FALSE) {
                    throw (Throwable)new IllegalArgumentException("with-shutdown only allows Symbols in bindings");
                }
                object = null;
            }
        }
        return object;
    }

    public Object doInvoke(Object object, Object object2, Object object3, Object object4) {
        Object object5 = object;
        object = null;
        Object object6 = object2;
        object2 = null;
        Object object7 = object3;
        object3 = null;
        ISeq iSeq = (ISeq)object4;
        object4 = null;
        return common$with_shutdown.invokeStatic(object5, object6, object7, iSeq);
    }

    public int getRequiredArity() {
        return 3;
    }
}

