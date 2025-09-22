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

public final class assert$assert
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"seq");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"concat");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"list");
    public static final AFn const__3 = (AFn)Symbol.intern((String)"datomic.assert", (String)"assert");
    public static final Var const__4 = RT.var((String)"clojure.core", (String)"*assert*");
    public static final Var const__5 = RT.var((String)"datomic.assert", (String)"local-bindings");
    public static final AFn const__6 = (AFn)Symbol.intern((String)"clojure.core", (String)"when-not");
    public static final AFn const__7 = (AFn)Symbol.intern((String)"clojure.core", (String)"let");
    public static final Var const__8 = RT.var((String)"clojure.core", (String)"apply");
    public static final Var const__9 = RT.var((String)"clojure.core", (String)"vector");
    public static final AFn const__10 = (AFn)Symbol.intern(null, (String)"form__20659__auto__");
    public static final AFn const__11 = (AFn)Symbol.intern(null, (String)"quote");
    public static final AFn const__12 = (AFn)Symbol.intern(null, (String)"error__20660__auto__");
    public static final AFn const__13 = (AFn)Symbol.intern((String)"clojure.core", (String)"ex-info");
    public static final Var const__14 = RT.var((String)"clojure.core", (String)"hash-map");
    public static final Keyword const__15 = RT.keyword(null, (String)"form");
    public static final Keyword const__16 = RT.keyword(null, (String)"bindings");
    public static final AFn const__17 = (AFn)Symbol.intern(null, (String)"if");
    public static final AFn const__18 = (AFn)Symbol.intern((String)"datomic.assert", (String)"*assert-handler*");
    public static final AFn const__19 = (AFn)Symbol.intern((String)"datomic.assert", (String)"*assert-handler*");
    public static final AFn const__20 = (AFn)Symbol.intern(null, (String)"throw");

    public static Object invokeStatic(Object _AMPERSAND_form, Object _AMPERSAND_env, Object x, Object msg) {
        Object object;
        Object object2 = const__4.get();
        if (object2 != null && object2 != Boolean.FALSE) {
            Object object3 = _AMPERSAND_env;
            _AMPERSAND_env = null;
            Object bindings = ((IFn)const__5.getRawRoot()).invoke(object3);
            Object object4 = ((IFn)const__2.getRawRoot()).invoke(x);
            Object object5 = x;
            x = null;
            Object object6 = msg;
            msg = null;
            Object object7 = bindings;
            bindings = null;
            object = ((IFn)const__0.getRawRoot()).invoke(((IFn)const__1.getRawRoot()).invoke(((IFn)const__2.getRawRoot()).invoke((Object)const__6), object4, ((IFn)const__2.getRawRoot()).invoke(((IFn)const__0.getRawRoot()).invoke(((IFn)const__1.getRawRoot()).invoke(((IFn)const__2.getRawRoot()).invoke((Object)const__7), ((IFn)const__2.getRawRoot()).invoke(((IFn)const__8.getRawRoot()).invoke(const__9.getRawRoot(), ((IFn)const__0.getRawRoot()).invoke(((IFn)const__1.getRawRoot()).invoke(((IFn)const__2.getRawRoot()).invoke((Object)const__10), ((IFn)const__2.getRawRoot()).invoke(((IFn)const__0.getRawRoot()).invoke(((IFn)const__1.getRawRoot()).invoke(((IFn)const__2.getRawRoot()).invoke((Object)const__11), ((IFn)const__2.getRawRoot()).invoke(object5)))), ((IFn)const__2.getRawRoot()).invoke((Object)const__12), ((IFn)const__2.getRawRoot()).invoke(((IFn)const__0.getRawRoot()).invoke(((IFn)const__1.getRawRoot()).invoke(((IFn)const__2.getRawRoot()).invoke((Object)const__13), ((IFn)const__2.getRawRoot()).invoke(object6), ((IFn)const__2.getRawRoot()).invoke(((IFn)const__8.getRawRoot()).invoke(const__14.getRawRoot(), ((IFn)const__0.getRawRoot()).invoke(((IFn)const__1.getRawRoot()).invoke(((IFn)const__2.getRawRoot()).invoke((Object)const__15), ((IFn)const__2.getRawRoot()).invoke((Object)const__10), ((IFn)const__2.getRawRoot()).invoke((Object)const__16), ((IFn)const__2.getRawRoot()).invoke(object7)))))))))))), ((IFn)const__2.getRawRoot()).invoke(((IFn)const__0.getRawRoot()).invoke(((IFn)const__1.getRawRoot()).invoke(((IFn)const__2.getRawRoot()).invoke((Object)const__17), ((IFn)const__2.getRawRoot()).invoke((Object)const__18), ((IFn)const__2.getRawRoot()).invoke(((IFn)const__0.getRawRoot()).invoke(((IFn)const__1.getRawRoot()).invoke(((IFn)const__2.getRawRoot()).invoke((Object)const__19), ((IFn)const__2.getRawRoot()).invoke((Object)const__12)))), ((IFn)const__2.getRawRoot()).invoke(((IFn)const__0.getRawRoot()).invoke(((IFn)const__1.getRawRoot()).invoke(((IFn)const__2.getRawRoot()).invoke((Object)const__20), ((IFn)const__2.getRawRoot()).invoke((Object)const__12))))))))))));
        } else {
            object = null;
        }
        return object;
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
        return assert$assert.invokeStatic(object5, object6, object7, object8);
    }

    public static Object invokeStatic(Object _AMPERSAND_form, Object _AMPERSAND_env, Object x) {
        Object object = x;
        x = null;
        return ((IFn)const__0.getRawRoot()).invoke(((IFn)const__1.getRawRoot()).invoke(((IFn)const__2.getRawRoot()).invoke((Object)const__3), ((IFn)const__2.getRawRoot()).invoke(object), ((IFn)const__2.getRawRoot()).invoke((Object)"Assertion failed, see ex-data for details")));
    }

    public Object invoke(Object object, Object object2, Object object3) {
        Object object4 = object;
        object = null;
        Object object5 = object2;
        object2 = null;
        Object object6 = object3;
        object3 = null;
        return assert$assert.invokeStatic(object4, object5, object6);
    }
}

