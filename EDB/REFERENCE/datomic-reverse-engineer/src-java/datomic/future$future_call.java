/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFn
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.Keyword
 *  clojure.lang.RT
 *  clojure.lang.Symbol
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFn;
import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Keyword;
import clojure.lang.RT;
import clojure.lang.Symbol;
import clojure.lang.Var;

public final class future$future_call
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"assoc");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"meta");
    public static final Keyword const__2 = RT.keyword(null, (String)"file");
    public static final Var const__3 = RT.var((String)"clojure.core", (String)"*file*");
    public static final Var const__4 = RT.var((String)"clojure.core", (String)"seq");
    public static final Var const__5 = RT.var((String)"clojure.core", (String)"concat");
    public static final Var const__6 = RT.var((String)"clojure.core", (String)"list");
    public static final AFn const__7 = (AFn)Symbol.intern((String)"clojure.core", (String)"let");
    public static final Var const__8 = RT.var((String)"clojure.core", (String)"apply");
    public static final Var const__9 = RT.var((String)"clojure.core", (String)"vector");
    public static final AFn const__10 = (AFn)Symbol.intern(null, (String)"f__10264__auto__");
    public static final AFn const__11 = (AFn)Symbol.intern((String)"datomic.future", (String)"-future-with-channel-impl");
    public static final AFn const__12 = (AFn)Symbol.intern(null, (String)"ch__10265__auto__");
    public static final AFn const__13 = (AFn)Symbol.intern((String)"datomic.future", (String)"get-channel");
    public static final AFn const__14 = (AFn)Symbol.intern((String)"datomic.future", (String)"add-bounding-warning");
    public static final AFn const__15 = (AFn)Symbol.intern((String)"clojure.core", (String)"deref");
    public static final AFn const__16 = (AFn)Symbol.intern((String)"datomic.future", (String)"bounding-warn-seconds");

    public static Object invokeStatic(Object _AMPERSAND_form, Object _AMPERSAND_env, Object f) {
        Object object = _AMPERSAND_form;
        _AMPERSAND_form = null;
        Object context = ((IFn)const__0.getRawRoot()).invoke(((IFn)const__1.getRawRoot()).invoke(object), (Object)const__2, const__3.get());
        Object object2 = f;
        f = null;
        Object object3 = context;
        context = null;
        return ((IFn)const__4.getRawRoot()).invoke(((IFn)const__5.getRawRoot()).invoke(((IFn)const__6.getRawRoot()).invoke((Object)const__7), ((IFn)const__6.getRawRoot()).invoke(((IFn)const__8.getRawRoot()).invoke(const__9.getRawRoot(), ((IFn)const__4.getRawRoot()).invoke(((IFn)const__5.getRawRoot()).invoke(((IFn)const__6.getRawRoot()).invoke((Object)const__10), ((IFn)const__6.getRawRoot()).invoke(((IFn)const__4.getRawRoot()).invoke(((IFn)const__5.getRawRoot()).invoke(((IFn)const__6.getRawRoot()).invoke((Object)const__11), ((IFn)const__6.getRawRoot()).invoke(object2)))), ((IFn)const__6.getRawRoot()).invoke((Object)const__12), ((IFn)const__6.getRawRoot()).invoke(((IFn)const__4.getRawRoot()).invoke(((IFn)const__5.getRawRoot()).invoke(((IFn)const__6.getRawRoot()).invoke((Object)const__13), ((IFn)const__6.getRawRoot()).invoke((Object)const__10)))))))), ((IFn)const__6.getRawRoot()).invoke(((IFn)const__4.getRawRoot()).invoke(((IFn)const__5.getRawRoot()).invoke(((IFn)const__6.getRawRoot()).invoke((Object)const__14), ((IFn)const__6.getRawRoot()).invoke((Object)const__12), ((IFn)const__6.getRawRoot()).invoke(object3), ((IFn)const__6.getRawRoot()).invoke(((IFn)const__4.getRawRoot()).invoke(((IFn)const__5.getRawRoot()).invoke(((IFn)const__6.getRawRoot()).invoke((Object)const__15), ((IFn)const__6.getRawRoot()).invoke((Object)const__16))))))), ((IFn)const__6.getRawRoot()).invoke((Object)const__10)));
    }

    public Object invoke(Object object, Object object2, Object object3) {
        Object object4 = object;
        object = null;
        Object object5 = object2;
        object2 = null;
        Object object6 = object3;
        object3 = null;
        return future$future_call.invokeStatic(object4, object5, object6);
    }
}

