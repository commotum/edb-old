/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.RT;
import clojure.lang.Var;

public final class datafy$setter_name__GT_keyword
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"subs");
    public static final Object const__1 = 3L;
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"keyword");
    public static final Var const__3 = RT.var((String)"clojure.core", (String)"str");
    public static final Object const__4 = 0L;
    public static final Object const__5 = 1L;

    public static Object invokeStatic(Object n) {
        Object object = n;
        n = null;
        Object base = ((IFn)const__0.getRawRoot()).invoke(object, const__1);
        String string = ((String)((IFn)const__0.getRawRoot()).invoke(base, const__4, const__5)).toLowerCase();
        Object object2 = base;
        base = null;
        return ((IFn)const__2.getRawRoot()).invoke(((IFn)const__3.getRawRoot()).invoke((Object)string, ((IFn)const__0.getRawRoot()).invoke(object2, const__5)));
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return datafy$setter_name__GT_keyword.invokeStatic(object2);
    }
}

