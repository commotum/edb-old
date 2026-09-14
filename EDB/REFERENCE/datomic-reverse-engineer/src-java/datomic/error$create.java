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

public final class error$create
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"seq");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"concat");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"list");
    public static final AFn const__3 = (AFn)Symbol.intern((String)"datomic.error", (String)"create");
    public static final AFn const__4 = (AFn)Symbol.intern((String)"datomic.error", (String)"create");
    public static final AFn const__5 = (AFn)Symbol.intern((String)"clojure.core", (String)"let");
    public static final Var const__6 = RT.var((String)"clojure.core", (String)"apply");
    public static final Var const__7 = RT.var((String)"clojure.core", (String)"vector");
    public static final AFn const__8 = (AFn)Symbol.intern(null, (String)"msg__662__auto__");
    public static final AFn const__9 = (AFn)Symbol.intern(null, (String)"new");
    public static final AFn const__10 = (AFn)Symbol.intern((String)"clojure.core", (String)"str");
    public static final AFn const__11 = (AFn)Symbol.intern((String)"datomic.error", (String)"anomalize");
    public static final AFn const__12 = (AFn)Symbol.intern((String)"clojure.core", (String)"assoc");
    public static final Keyword const__13 = RT.keyword((String)"db", (String)"error");
    public static final AFn const__14 = (AFn)Symbol.intern((String)"clojure.core", (String)"let");
    public static final AFn const__15 = (AFn)Symbol.intern(null, (String)"msg__663__auto__");
    public static final AFn const__16 = (AFn)Symbol.intern(null, (String)"new");
    public static final AFn const__17 = (AFn)Symbol.intern((String)"clojure.core", (String)"str");
    public static final AFn const__18 = (AFn)Symbol.intern((String)"datomic.error", (String)"anomalize");
    public static final AFn const__19 = (AFn)Symbol.intern((String)"clojure.core", (String)"assoc");

    public static Object invokeStatic(Object _AMPERSAND_form, Object _AMPERSAND_env, Object cls, Object code, Object msg, Object details, Object cause) {
        Object object = msg;
        msg = null;
        Object object2 = ((IFn)const__2.getRawRoot()).invoke(cls);
        Object object3 = ((IFn)const__2.getRawRoot()).invoke(((IFn)const__0.getRawRoot()).invoke(((IFn)const__1.getRawRoot()).invoke(((IFn)const__2.getRawRoot()).invoke((Object)const__17), ((IFn)const__2.getRawRoot()).invoke(code), ((IFn)const__2.getRawRoot()).invoke((Object)" "), ((IFn)const__2.getRawRoot()).invoke((Object)const__15))));
        Object object4 = details;
        details = null;
        Object object5 = code;
        code = null;
        Object object6 = cls;
        cls = null;
        Object object7 = cause;
        cause = null;
        return ((IFn)const__0.getRawRoot()).invoke(((IFn)const__1.getRawRoot()).invoke(((IFn)const__2.getRawRoot()).invoke((Object)const__14), ((IFn)const__2.getRawRoot()).invoke(((IFn)const__6.getRawRoot()).invoke(const__7.getRawRoot(), ((IFn)const__0.getRawRoot()).invoke(((IFn)const__1.getRawRoot()).invoke(((IFn)const__2.getRawRoot()).invoke((Object)const__15), ((IFn)const__2.getRawRoot()).invoke(object))))), ((IFn)const__2.getRawRoot()).invoke(((IFn)const__0.getRawRoot()).invoke(((IFn)const__1.getRawRoot()).invoke(((IFn)const__2.getRawRoot()).invoke((Object)const__16), object2, object3, ((IFn)const__2.getRawRoot()).invoke(((IFn)const__0.getRawRoot()).invoke(((IFn)const__1.getRawRoot()).invoke(((IFn)const__2.getRawRoot()).invoke((Object)const__18), ((IFn)const__2.getRawRoot()).invoke(((IFn)const__0.getRawRoot()).invoke(((IFn)const__1.getRawRoot()).invoke(((IFn)const__2.getRawRoot()).invoke((Object)const__19), ((IFn)const__2.getRawRoot()).invoke(object4), ((IFn)const__2.getRawRoot()).invoke((Object)const__13), ((IFn)const__2.getRawRoot()).invoke(object5)))), ((IFn)const__2.getRawRoot()).invoke(object6), ((IFn)const__2.getRawRoot()).invoke((Object)const__15)))), ((IFn)const__2.getRawRoot()).invoke(object7))))));
    }

    public Object invoke(Object object, Object object2, Object object3, Object object4, Object object5, Object object6, Object object7) {
        Object object8 = object;
        object = null;
        Object object9 = object2;
        object2 = null;
        Object object10 = object3;
        object3 = null;
        Object object11 = object4;
        object4 = null;
        Object object12 = object5;
        object5 = null;
        Object object13 = object6;
        object6 = null;
        Object object14 = object7;
        object7 = null;
        return error$create.invokeStatic(object8, object9, object10, object11, object12, object13, object14);
    }

    public static Object invokeStatic(Object _AMPERSAND_form, Object _AMPERSAND_env, Object cls, Object code, Object msg, Object details) {
        Object object = msg;
        msg = null;
        Object object2 = ((IFn)const__2.getRawRoot()).invoke(cls);
        Object object3 = ((IFn)const__2.getRawRoot()).invoke(((IFn)const__0.getRawRoot()).invoke(((IFn)const__1.getRawRoot()).invoke(((IFn)const__2.getRawRoot()).invoke((Object)const__10), ((IFn)const__2.getRawRoot()).invoke(code), ((IFn)const__2.getRawRoot()).invoke((Object)" "), ((IFn)const__2.getRawRoot()).invoke((Object)const__8))));
        Object object4 = details;
        details = null;
        Object object5 = code;
        code = null;
        Object object6 = cls;
        cls = null;
        return ((IFn)const__0.getRawRoot()).invoke(((IFn)const__1.getRawRoot()).invoke(((IFn)const__2.getRawRoot()).invoke((Object)const__5), ((IFn)const__2.getRawRoot()).invoke(((IFn)const__6.getRawRoot()).invoke(const__7.getRawRoot(), ((IFn)const__0.getRawRoot()).invoke(((IFn)const__1.getRawRoot()).invoke(((IFn)const__2.getRawRoot()).invoke((Object)const__8), ((IFn)const__2.getRawRoot()).invoke(object))))), ((IFn)const__2.getRawRoot()).invoke(((IFn)const__0.getRawRoot()).invoke(((IFn)const__1.getRawRoot()).invoke(((IFn)const__2.getRawRoot()).invoke((Object)const__9), object2, object3, ((IFn)const__2.getRawRoot()).invoke(((IFn)const__0.getRawRoot()).invoke(((IFn)const__1.getRawRoot()).invoke(((IFn)const__2.getRawRoot()).invoke((Object)const__11), ((IFn)const__2.getRawRoot()).invoke(((IFn)const__0.getRawRoot()).invoke(((IFn)const__1.getRawRoot()).invoke(((IFn)const__2.getRawRoot()).invoke((Object)const__12), ((IFn)const__2.getRawRoot()).invoke(object4), ((IFn)const__2.getRawRoot()).invoke((Object)const__13), ((IFn)const__2.getRawRoot()).invoke(object5)))), ((IFn)const__2.getRawRoot()).invoke(object6), ((IFn)const__2.getRawRoot()).invoke((Object)const__8)))))))));
    }

    public Object invoke(Object object, Object object2, Object object3, Object object4, Object object5, Object object6) {
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
        Object object12 = object6;
        object6 = null;
        return error$create.invokeStatic(object7, object8, object9, object10, object11, object12);
    }

    public static Object invokeStatic(Object _AMPERSAND_form, Object _AMPERSAND_env, Object cls, Object code, Object msg) {
        Object object = cls;
        cls = null;
        Object object2 = code;
        code = null;
        Object object3 = msg;
        msg = null;
        return ((IFn)const__0.getRawRoot()).invoke(((IFn)const__1.getRawRoot()).invoke(((IFn)const__2.getRawRoot()).invoke((Object)const__4), ((IFn)const__2.getRawRoot()).invoke(object), ((IFn)const__2.getRawRoot()).invoke(object2), ((IFn)const__2.getRawRoot()).invoke(object3), ((IFn)const__2.getRawRoot()).invoke(null)));
    }

    public Object invoke(Object object, Object object2, Object object3, Object object4, Object object5) {
        Object object6 = object;
        object = null;
        Object object7 = object2;
        object2 = null;
        Object object8 = object3;
        object3 = null;
        Object object9 = object4;
        object4 = null;
        Object object10 = object5;
        object5 = null;
        return error$create.invokeStatic(object6, object7, object8, object9, object10);
    }

    public static Object invokeStatic(Object _AMPERSAND_form, Object _AMPERSAND_env, Object cls, Object code) {
        Object object = cls;
        cls = null;
        Object object2 = code;
        code = null;
        return ((IFn)const__0.getRawRoot()).invoke(((IFn)const__1.getRawRoot()).invoke(((IFn)const__2.getRawRoot()).invoke((Object)const__3), ((IFn)const__2.getRawRoot()).invoke(object), ((IFn)const__2.getRawRoot()).invoke(object2), ((IFn)const__2.getRawRoot()).invoke((Object)""), ((IFn)const__2.getRawRoot()).invoke(null)));
    }

    public Object invoke(Object object, Object object2, Object object3, Object object4) {
        Object object5 = object;
        object = null;
        Object object6 = object2;
        object2 = null;
        Object object7 = object3;
        object3 = null;
        Object object8 = object4;
        object4 = null;
        return error$create.invokeStatic(object5, object6, object7, object8);
    }
}

