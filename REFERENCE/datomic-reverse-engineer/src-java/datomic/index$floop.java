/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFn
 *  clojure.lang.IFn
 *  clojure.lang.ISeq
 *  clojure.lang.RT
 *  clojure.lang.RestFn
 *  clojure.lang.Symbol
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFn;
import clojure.lang.IFn;
import clojure.lang.ISeq;
import clojure.lang.RT;
import clojure.lang.RestFn;
import clojure.lang.Symbol;
import clojure.lang.Var;

public final class index$floop
extends RestFn {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"partition");
    public static final Object const__1 = 2L;
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"map");
    public static final Var const__3 = RT.var((String)"clojure.core", (String)"first");
    public static final Var const__4 = RT.var((String)"clojure.core", (String)"second");
    public static final Var const__5 = RT.var((String)"clojure.core", (String)"seq");
    public static final Var const__6 = RT.var((String)"clojure.core", (String)"concat");
    public static final Var const__7 = RT.var((String)"clojure.core", (String)"list");
    public static final AFn const__8 = (AFn)Symbol.intern((String)"clojure.core", (String)"let");
    public static final Var const__9 = RT.var((String)"clojure.core", (String)"apply");
    public static final Var const__10 = RT.var((String)"clojure.core", (String)"vector");
    public static final AFn const__11 = (AFn)Symbol.intern(null, (String)"f__15304__auto__");
    public static final AFn const__12 = (AFn)Symbol.intern((String)"clojure.core", (String)"fn");

    public static Object invokeStatic(Object _AMPERSAND_form, Object _AMPERSAND_env, Object bindings, ISeq body) {
        Object object = bindings;
        bindings = null;
        Object bs = ((IFn)const__0.getRawRoot()).invoke(const__1, object);
        Object params = ((IFn)const__2.getRawRoot()).invoke(const__3.getRawRoot(), bs);
        Object object2 = bs;
        bs = null;
        Object args = ((IFn)const__2.getRawRoot()).invoke(const__4.getRawRoot(), object2);
        Object object3 = params;
        params = null;
        ISeq iSeq = body;
        body = null;
        Object object4 = args;
        args = null;
        return ((IFn)const__5.getRawRoot()).invoke(((IFn)const__6.getRawRoot()).invoke(((IFn)const__7.getRawRoot()).invoke((Object)const__8), ((IFn)const__7.getRawRoot()).invoke(((IFn)const__9.getRawRoot()).invoke(const__10.getRawRoot(), ((IFn)const__5.getRawRoot()).invoke(((IFn)const__6.getRawRoot()).invoke(((IFn)const__7.getRawRoot()).invoke((Object)const__11), ((IFn)const__7.getRawRoot()).invoke(((IFn)const__5.getRawRoot()).invoke(((IFn)const__6.getRawRoot()).invoke(((IFn)const__7.getRawRoot()).invoke((Object)const__12), ((IFn)const__7.getRawRoot()).invoke(((IFn)const__9.getRawRoot()).invoke(const__10.getRawRoot(), ((IFn)const__5.getRawRoot()).invoke(((IFn)const__6.getRawRoot()).invoke(object3)))), (Object)iSeq))))))), ((IFn)const__7.getRawRoot()).invoke(((IFn)const__5.getRawRoot()).invoke(((IFn)const__6.getRawRoot()).invoke(((IFn)const__7.getRawRoot()).invoke((Object)const__11), object4)))));
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
        return index$floop.invokeStatic(object5, object6, object7, iSeq);
    }

    public int getRequiredArity() {
        return 3;
    }
}

