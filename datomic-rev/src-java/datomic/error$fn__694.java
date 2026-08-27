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
import datomic.error$fn__694$fn__695;

public final class error$fn__694
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.error", (String)"has-string-constructor?");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"eval");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"seq");
    public static final Var const__3 = RT.var((String)"clojure.core", (String)"concat");
    public static final Var const__4 = RT.var((String)"clojure.core", (String)"list");
    public static final AFn const__5 = (AFn)Symbol.intern((String)"clojure.core", (String)"fn");
    public static final Var const__6 = RT.var((String)"clojure.core", (String)"apply");
    public static final Var const__7 = RT.var((String)"clojure.core", (String)"vector");
    public static final AFn const__8 = (AFn)Symbol.intern(null, (String)"msg__693__auto__");
    public static final AFn const__9 = (AFn)Symbol.intern(null, (String)"new");
    public static final Var const__10 = RT.var((String)"clojure.core", (String)"symbol");

    public static Object invokeStatic(Object classname) {
        Object object;
        boolean and__5236__auto__698 = ((String)classname).startsWith("java");
        Object object2 = and__5236__auto__698 ? ((IFn)const__0.getRawRoot()).invoke(Class.forName((String)classname)) : (and__5236__auto__698 ? Boolean.TRUE : Boolean.FALSE);
        if (object2 != null && object2 != Boolean.FALSE) {
            Object object3 = classname;
            classname = null;
            object = ((IFn)const__1.getRawRoot()).invoke(((IFn)const__2.getRawRoot()).invoke(((IFn)const__3.getRawRoot()).invoke(((IFn)const__4.getRawRoot()).invoke((Object)const__5), ((IFn)const__4.getRawRoot()).invoke(((IFn)const__6.getRawRoot()).invoke(const__7.getRawRoot(), ((IFn)const__2.getRawRoot()).invoke(((IFn)const__3.getRawRoot()).invoke(((IFn)const__4.getRawRoot()).invoke((Object)const__8))))), ((IFn)const__4.getRawRoot()).invoke(((IFn)const__2.getRawRoot()).invoke(((IFn)const__3.getRawRoot()).invoke(((IFn)const__4.getRawRoot()).invoke((Object)const__9), ((IFn)const__4.getRawRoot()).invoke(((IFn)const__10.getRawRoot()).invoke(object3)), ((IFn)const__4.getRawRoot()).invoke((Object)const__8)))))));
        } else {
            object = new error$fn__694$fn__695();
        }
        return object;
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return error$fn__694.invokeStatic(object2);
    }
}

