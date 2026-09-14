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

public final class summary$print_method
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"seq");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"concat");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"list");
    public static final AFn const__3 = (AFn)Symbol.intern(null, (String)"do");
    public static final AFn const__4 = (AFn)Symbol.intern((String)"clojure.core", (String)"defmethod");
    public static final AFn const__5 = (AFn)Symbol.intern((String)"clojure.core", (String)"print-method");
    public static final Var const__6 = RT.var((String)"clojure.core", (String)"apply");
    public static final Var const__7 = RT.var((String)"clojure.core", (String)"vector");
    public static final AFn const__8 = (AFn)Symbol.intern(null, (String)"o");
    public static final AFn const__9 = (AFn)Symbol.intern(null, (String)"w");
    public static final AFn const__10 = (AFn)Symbol.intern((String)"datomic.summary", (String)"write");
    public static final AFn const__11 = (AFn)Symbol.intern(null, (String)"w");
    public static final AFn const__12 = (AFn)Symbol.intern((String)"clojure.core", (String)"str");
    public static final AFn const__13 = (AFn)Symbol.intern((String)"clojure.core", (String)"assoc");
    public static final AFn const__14 = (AFn)Symbol.intern((String)"datomic.summary", (String)"summary");
    public static final AFn const__15 = (AFn)Symbol.intern(null, (String)"o");
    public static final Keyword const__16 = RT.keyword(null, (String)"type");
    public static final AFn const__17 = (AFn)Symbol.intern((String)"clojure.core", (String)"defmethod");
    public static final AFn const__18 = (AFn)Symbol.intern((String)"clojure.core", (String)"print-dup");
    public static final AFn const__19 = (AFn)Symbol.intern(null, (String)"o");
    public static final AFn const__20 = (AFn)Symbol.intern(null, (String)"w");
    public static final AFn const__21 = (AFn)Symbol.intern((String)"clojure.core", (String)"print-method");
    public static final AFn const__22 = (AFn)Symbol.intern(null, (String)"o");
    public static final AFn const__23 = (AFn)Symbol.intern(null, (String)"w");

    public static Object invokeStatic(Object _AMPERSAND_form, Object _AMPERSAND_env, Object clazz) {
        Object object = ((IFn)const__2.getRawRoot()).invoke(((IFn)const__0.getRawRoot()).invoke(((IFn)const__1.getRawRoot()).invoke(((IFn)const__2.getRawRoot()).invoke((Object)const__4), ((IFn)const__2.getRawRoot()).invoke((Object)const__5), ((IFn)const__2.getRawRoot()).invoke(clazz), ((IFn)const__2.getRawRoot()).invoke(((IFn)const__6.getRawRoot()).invoke(const__7.getRawRoot(), ((IFn)const__0.getRawRoot()).invoke(((IFn)const__1.getRawRoot()).invoke(((IFn)const__2.getRawRoot()).invoke((Object)const__8), ((IFn)const__2.getRawRoot()).invoke((Object)const__9))))), ((IFn)const__2.getRawRoot()).invoke(((IFn)const__0.getRawRoot()).invoke(((IFn)const__1.getRawRoot()).invoke(((IFn)const__2.getRawRoot()).invoke((Object)const__10), ((IFn)const__2.getRawRoot()).invoke((Object)const__11), ((IFn)const__2.getRawRoot()).invoke(((IFn)const__0.getRawRoot()).invoke(((IFn)const__1.getRawRoot()).invoke(((IFn)const__2.getRawRoot()).invoke((Object)const__12), ((IFn)const__2.getRawRoot()).invoke(((IFn)const__0.getRawRoot()).invoke(((IFn)const__1.getRawRoot()).invoke(((IFn)const__2.getRawRoot()).invoke((Object)const__13), ((IFn)const__2.getRawRoot()).invoke(((IFn)const__0.getRawRoot()).invoke(((IFn)const__1.getRawRoot()).invoke(((IFn)const__2.getRawRoot()).invoke((Object)const__14), ((IFn)const__2.getRawRoot()).invoke((Object)const__15)))), ((IFn)const__2.getRawRoot()).invoke((Object)const__16), ((IFn)const__2.getRawRoot()).invoke(clazz)))))))))))));
        Object object2 = clazz;
        clazz = null;
        return ((IFn)const__0.getRawRoot()).invoke(((IFn)const__1.getRawRoot()).invoke(((IFn)const__2.getRawRoot()).invoke((Object)const__3), object, ((IFn)const__2.getRawRoot()).invoke(((IFn)const__0.getRawRoot()).invoke(((IFn)const__1.getRawRoot()).invoke(((IFn)const__2.getRawRoot()).invoke((Object)const__17), ((IFn)const__2.getRawRoot()).invoke((Object)const__18), ((IFn)const__2.getRawRoot()).invoke(object2), ((IFn)const__2.getRawRoot()).invoke(((IFn)const__6.getRawRoot()).invoke(const__7.getRawRoot(), ((IFn)const__0.getRawRoot()).invoke(((IFn)const__1.getRawRoot()).invoke(((IFn)const__2.getRawRoot()).invoke((Object)const__19), ((IFn)const__2.getRawRoot()).invoke((Object)const__20))))), ((IFn)const__2.getRawRoot()).invoke(((IFn)const__0.getRawRoot()).invoke(((IFn)const__1.getRawRoot()).invoke(((IFn)const__2.getRawRoot()).invoke((Object)const__21), ((IFn)const__2.getRawRoot()).invoke((Object)const__22), ((IFn)const__2.getRawRoot()).invoke((Object)const__23)))))))));
    }

    public Object invoke(Object object, Object object2, Object object3) {
        Object object4 = object;
        object = null;
        Object object5 = object2;
        object2 = null;
        Object object6 = object3;
        object3 = null;
        return summary$print_method.invokeStatic(object4, object5, object6);
    }
}

