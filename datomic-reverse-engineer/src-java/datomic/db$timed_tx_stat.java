/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFn
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.RT
 *  clojure.lang.Symbol
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFn;
import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.RT;
import clojure.lang.Symbol;
import clojure.lang.Var;

public final class db$timed_tx_stat
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"seq");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"concat");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"list");
    public static final AFn const__3 = (AFn)Symbol.intern((String)"clojure.core", (String)"let");
    public static final Var const__4 = RT.var((String)"clojure.core", (String)"apply");
    public static final Var const__5 = RT.var((String)"clojure.core", (String)"vector");
    public static final AFn const__6 = (AFn)Symbol.intern(null, (String)"start__13414__auto__");
    public static final AFn const__7 = (AFn)Symbol.intern((String)"java.lang.System", (String)"nanoTime");
    public static final AFn const__8 = (AFn)Symbol.intern(null, (String)"ret__13415__auto__");
    public static final AFn const__9 = (AFn)Symbol.intern((String)"datomic.db", (String)"long-add!");
    public static final AFn const__10 = (AFn)Symbol.intern((String)"clojure.core", (String)"unchecked-subtract");
    public static final AFn const__11 = (AFn)Symbol.intern((String)"java.lang.System", (String)"nanoTime");

    public static Object invokeStatic(Object _AMPERSAND_form, Object _AMPERSAND_env, Object adder, Object expr) {
        Object object = expr;
        expr = null;
        Object object2 = adder;
        adder = null;
        return ((IFn)const__0.getRawRoot()).invoke(((IFn)const__1.getRawRoot()).invoke(((IFn)const__2.getRawRoot()).invoke((Object)const__3), ((IFn)const__2.getRawRoot()).invoke(((IFn)const__4.getRawRoot()).invoke(const__5.getRawRoot(), ((IFn)const__0.getRawRoot()).invoke(((IFn)const__1.getRawRoot()).invoke(((IFn)const__2.getRawRoot()).invoke((Object)const__6), ((IFn)const__2.getRawRoot()).invoke(((IFn)const__0.getRawRoot()).invoke(((IFn)const__1.getRawRoot()).invoke(((IFn)const__2.getRawRoot()).invoke((Object)const__7)))), ((IFn)const__2.getRawRoot()).invoke((Object)const__8), ((IFn)const__2.getRawRoot()).invoke(object))))), ((IFn)const__2.getRawRoot()).invoke(((IFn)const__0.getRawRoot()).invoke(((IFn)const__1.getRawRoot()).invoke(((IFn)const__2.getRawRoot()).invoke((Object)const__9), ((IFn)const__2.getRawRoot()).invoke(object2), ((IFn)const__2.getRawRoot()).invoke(((IFn)const__0.getRawRoot()).invoke(((IFn)const__1.getRawRoot()).invoke(((IFn)const__2.getRawRoot()).invoke((Object)const__10), ((IFn)const__2.getRawRoot()).invoke(((IFn)const__0.getRawRoot()).invoke(((IFn)const__1.getRawRoot()).invoke(((IFn)const__2.getRawRoot()).invoke((Object)const__11)))), ((IFn)const__2.getRawRoot()).invoke((Object)const__6))))))), ((IFn)const__2.getRawRoot()).invoke((Object)const__8)));
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
        return db$timed_tx_stat.invokeStatic(object5, object6, object7, object8);
    }
}

