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

public final class slf4j$log_expr
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"seq");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"concat");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"list");
    public static final AFn const__3 = (AFn)Symbol.intern((String)"clojure.core", (String)"let");
    public static final Var const__4 = RT.var((String)"clojure.core", (String)"apply");
    public static final Var const__5 = RT.var((String)"clojure.core", (String)"vector");
    public static final AFn const__6 = (AFn)Symbol.intern(null, (String)"logger");
    public static final AFn const__7 = (AFn)Symbol.intern((String)"org.slf4j.LoggerFactory", (String)"getLogger");
    public static final Var const__8 = RT.var((String)"clojure.core", (String)"str");
    public static final Var const__9 = RT.var((String)"clojure.core", (String)"*ns*");
    public static final AFn const__10 = (AFn)Symbol.intern((String)"clojure.core", (String)"when");
    public static final Var const__11 = RT.var((String)"datomic.slf4j", (String)"enabled-method");
    public static final AFn const__12 = (AFn)Symbol.intern(null, (String)"logger");
    public static final AFn const__13 = (AFn)Symbol.intern(null, (String)".");
    public static final AFn const__14 = (AFn)Symbol.intern(null, (String)"logger");
    public static final AFn const__15 = (AFn)Symbol.intern((String)"datomic.slf4j", (String)"process");
    public static final AFn const__16 = (AFn)Symbol.intern((String)"clojure.core", (String)"let");
    public static final AFn const__17 = (AFn)Symbol.intern(null, (String)"logger");
    public static final AFn const__18 = (AFn)Symbol.intern((String)"org.slf4j.LoggerFactory", (String)"getLogger");
    public static final AFn const__19 = (AFn)Symbol.intern(null, (String)"ex");
    public static final AFn const__20 = (AFn)Symbol.intern((String)"clojure.core", (String)"when");
    public static final AFn const__21 = (AFn)Symbol.intern(null, (String)"logger");
    public static final AFn const__22 = (AFn)Symbol.intern(null, (String)".");
    public static final AFn const__23 = (AFn)Symbol.intern(null, (String)"logger");
    public static final AFn const__24 = (AFn)Symbol.intern((String)"datomic.slf4j", (String)"process");
    public static final AFn const__25 = (AFn)Symbol.intern(null, (String)"ex");
    public static final AFn const__26 = (AFn)Symbol.intern((String)"datomic.slf4j", (String)"caused-by");
    public static final AFn const__27 = (AFn)Symbol.intern(null, (String)"logger");
    public static final AFn const__28 = (AFn)Symbol.intern(null, (String)"ex");

    public static Object invokeStatic(Object level, Object msg, Object ex) {
        Object object = ex;
        ex = null;
        Object object2 = ((IFn)const__2.getRawRoot()).invoke(((IFn)const__0.getRawRoot()).invoke(((IFn)const__1.getRawRoot()).invoke(((IFn)const__2.getRawRoot()).invoke(((IFn)const__11.getRawRoot()).invoke(level)), ((IFn)const__2.getRawRoot()).invoke((Object)const__21))));
        Object object3 = level;
        level = null;
        Object object4 = msg;
        msg = null;
        return ((IFn)const__0.getRawRoot()).invoke(((IFn)const__1.getRawRoot()).invoke(((IFn)const__2.getRawRoot()).invoke((Object)const__16), ((IFn)const__2.getRawRoot()).invoke(((IFn)const__4.getRawRoot()).invoke(const__5.getRawRoot(), ((IFn)const__0.getRawRoot()).invoke(((IFn)const__1.getRawRoot()).invoke(((IFn)const__2.getRawRoot()).invoke((Object)const__17), ((IFn)const__2.getRawRoot()).invoke(((IFn)const__0.getRawRoot()).invoke(((IFn)const__1.getRawRoot()).invoke(((IFn)const__2.getRawRoot()).invoke((Object)const__18), ((IFn)const__2.getRawRoot()).invoke(((IFn)const__8.getRawRoot()).invoke(const__9.get()))))), ((IFn)const__2.getRawRoot()).invoke((Object)const__19), ((IFn)const__2.getRawRoot()).invoke(object))))), ((IFn)const__2.getRawRoot()).invoke(((IFn)const__0.getRawRoot()).invoke(((IFn)const__1.getRawRoot()).invoke(((IFn)const__2.getRawRoot()).invoke((Object)const__20), object2, ((IFn)const__2.getRawRoot()).invoke(((IFn)const__0.getRawRoot()).invoke(((IFn)const__1.getRawRoot()).invoke(((IFn)const__2.getRawRoot()).invoke((Object)const__22), ((IFn)const__2.getRawRoot()).invoke((Object)const__23), ((IFn)const__2.getRawRoot()).invoke(object3), ((IFn)const__2.getRawRoot()).invoke(((IFn)const__0.getRawRoot()).invoke(((IFn)const__1.getRawRoot()).invoke(((IFn)const__2.getRawRoot()).invoke((Object)const__24), ((IFn)const__2.getRawRoot()).invoke(object4)))), ((IFn)const__2.getRawRoot()).invoke((Object)const__25)))), ((IFn)const__2.getRawRoot()).invoke(((IFn)const__0.getRawRoot()).invoke(((IFn)const__1.getRawRoot()).invoke(((IFn)const__2.getRawRoot()).invoke((Object)const__26), ((IFn)const__2.getRawRoot()).invoke((Object)const__27), ((IFn)const__2.getRawRoot()).invoke((Object)const__28))))))), ((IFn)const__2.getRawRoot()).invoke(null)));
    }

    public Object invoke(Object object, Object object2, Object object3) {
        Object object4 = object;
        object = null;
        Object object5 = object2;
        object2 = null;
        Object object6 = object3;
        object3 = null;
        return slf4j$log_expr.invokeStatic(object4, object5, object6);
    }

    public static Object invokeStatic(Object level, Object msg) {
        Object object = ((IFn)const__2.getRawRoot()).invoke(((IFn)const__0.getRawRoot()).invoke(((IFn)const__1.getRawRoot()).invoke(((IFn)const__2.getRawRoot()).invoke(((IFn)const__11.getRawRoot()).invoke(level)), ((IFn)const__2.getRawRoot()).invoke((Object)const__12))));
        Object object2 = level;
        level = null;
        Object object3 = msg;
        msg = null;
        return ((IFn)const__0.getRawRoot()).invoke(((IFn)const__1.getRawRoot()).invoke(((IFn)const__2.getRawRoot()).invoke((Object)const__3), ((IFn)const__2.getRawRoot()).invoke(((IFn)const__4.getRawRoot()).invoke(const__5.getRawRoot(), ((IFn)const__0.getRawRoot()).invoke(((IFn)const__1.getRawRoot()).invoke(((IFn)const__2.getRawRoot()).invoke((Object)const__6), ((IFn)const__2.getRawRoot()).invoke(((IFn)const__0.getRawRoot()).invoke(((IFn)const__1.getRawRoot()).invoke(((IFn)const__2.getRawRoot()).invoke((Object)const__7), ((IFn)const__2.getRawRoot()).invoke(((IFn)const__8.getRawRoot()).invoke(const__9.get()))))))))), ((IFn)const__2.getRawRoot()).invoke(((IFn)const__0.getRawRoot()).invoke(((IFn)const__1.getRawRoot()).invoke(((IFn)const__2.getRawRoot()).invoke((Object)const__10), object, ((IFn)const__2.getRawRoot()).invoke(((IFn)const__0.getRawRoot()).invoke(((IFn)const__1.getRawRoot()).invoke(((IFn)const__2.getRawRoot()).invoke((Object)const__13), ((IFn)const__2.getRawRoot()).invoke((Object)const__14), ((IFn)const__2.getRawRoot()).invoke(object2), ((IFn)const__2.getRawRoot()).invoke(((IFn)const__0.getRawRoot()).invoke(((IFn)const__1.getRawRoot()).invoke(((IFn)const__2.getRawRoot()).invoke((Object)const__15), ((IFn)const__2.getRawRoot()).invoke(object3)))))))))), ((IFn)const__2.getRawRoot()).invoke(null)));
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return slf4j$log_expr.invokeStatic(object3, object4);
    }
}

