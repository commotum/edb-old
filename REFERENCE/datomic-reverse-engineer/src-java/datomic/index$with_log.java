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

public final class index$with_log
extends RestFn {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"seq");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"concat");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"list");
    public static final AFn const__3 = (AFn)Symbol.intern(null, (String)"do");
    public static final AFn const__4 = (AFn)Symbol.intern((String)"datomic.common", (String)"log-and-print");
    public static final Var const__5 = RT.var((String)"clojure.core", (String)"apply");
    public static final Var const__6 = RT.var((String)"clojure.core", (String)"hash-map");
    public static final Keyword const__7 = RT.keyword(null, (String)"in");
    public static final AFn const__8 = (AFn)Symbol.intern((String)"clojure.core", (String)"let");
    public static final Var const__9 = RT.var((String)"clojure.core", (String)"vector");
    public static final AFn const__10 = (AFn)Symbol.intern(null, (String)"ret__15302__auto__");
    public static final AFn const__11 = (AFn)Symbol.intern(null, (String)"do");
    public static final AFn const__12 = (AFn)Symbol.intern((String)"datomic.common", (String)"log-and-print");
    public static final Keyword const__13 = RT.keyword(null, (String)"out");
    public static final AFn const__14 = (AFn)Symbol.intern(null, (String)"do");

    public static Object invokeStatic(Object _AMPERSAND_form, Object _AMPERSAND_env, Object name, ISeq body) {
        Object object;
        Object object2 = name;
        if (object2 != null && object2 != Boolean.FALSE) {
            Object object3 = ((IFn)const__2.getRawRoot()).invoke(((IFn)const__0.getRawRoot()).invoke(((IFn)const__1.getRawRoot()).invoke(((IFn)const__2.getRawRoot()).invoke((Object)const__4), ((IFn)const__2.getRawRoot()).invoke(((IFn)const__5.getRawRoot()).invoke(const__6.getRawRoot(), ((IFn)const__0.getRawRoot()).invoke(((IFn)const__1.getRawRoot()).invoke(((IFn)const__2.getRawRoot()).invoke((Object)const__7), ((IFn)const__2.getRawRoot()).invoke(name))))))));
            ISeq iSeq = body;
            body = null;
            Object object4 = name;
            name = null;
            object = ((IFn)const__0.getRawRoot()).invoke(((IFn)const__1.getRawRoot()).invoke(((IFn)const__2.getRawRoot()).invoke((Object)const__3), object3, ((IFn)const__2.getRawRoot()).invoke(((IFn)const__0.getRawRoot()).invoke(((IFn)const__1.getRawRoot()).invoke(((IFn)const__2.getRawRoot()).invoke((Object)const__8), ((IFn)const__2.getRawRoot()).invoke(((IFn)const__5.getRawRoot()).invoke(const__9.getRawRoot(), ((IFn)const__0.getRawRoot()).invoke(((IFn)const__1.getRawRoot()).invoke(((IFn)const__2.getRawRoot()).invoke((Object)const__10), ((IFn)const__2.getRawRoot()).invoke(((IFn)const__0.getRawRoot()).invoke(((IFn)const__1.getRawRoot()).invoke(((IFn)const__2.getRawRoot()).invoke((Object)const__11), (Object)iSeq))))))), ((IFn)const__2.getRawRoot()).invoke(((IFn)const__0.getRawRoot()).invoke(((IFn)const__1.getRawRoot()).invoke(((IFn)const__2.getRawRoot()).invoke((Object)const__12), ((IFn)const__2.getRawRoot()).invoke(((IFn)const__5.getRawRoot()).invoke(const__6.getRawRoot(), ((IFn)const__0.getRawRoot()).invoke(((IFn)const__1.getRawRoot()).invoke(((IFn)const__2.getRawRoot()).invoke((Object)const__13), ((IFn)const__2.getRawRoot()).invoke(object4)))))))), ((IFn)const__2.getRawRoot()).invoke((Object)const__10))))));
        } else {
            ISeq iSeq = body;
            body = null;
            object = ((IFn)const__0.getRawRoot()).invoke(((IFn)const__1.getRawRoot()).invoke(((IFn)const__2.getRawRoot()).invoke((Object)const__14), (Object)iSeq));
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
        return index$with_log.invokeStatic(object5, object6, object7, iSeq);
    }

    public int getRequiredArity() {
        return 3;
    }
}

