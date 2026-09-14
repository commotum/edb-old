/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.RT
 *  clojure.lang.Util
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.RT;
import clojure.lang.Util;
import clojure.lang.Var;

public final class db$forward_attr
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"number?");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"name");
    public static final Object const__3 = Character.valueOf('_');
    public static final Var const__5 = RT.var((String)"clojure.core", (String)"keyword");
    public static final Var const__6 = RT.var((String)"clojure.core", (String)"namespace");
    public static final Var const__7 = RT.var((String)"clojure.core", (String)"subs");
    public static final Object const__8 = 1L;

    public static Object invokeStatic(Object x) {
        Object object;
        Object object2 = ((IFn)const__0.getRawRoot()).invoke(x);
        if (object2 != null && object2 != Boolean.FALSE) {
            object = x;
            x = null;
        } else {
            Object n = ((IFn)const__1.getRawRoot()).invoke(x);
            if (Util.equiv((char)((Character)const__3).charValue(), (char)((String)n).charAt(RT.uncheckedIntCast((long)0L)))) {
                Object object3 = x;
                x = null;
                Object object4 = n;
                n = null;
                object = ((IFn)const__5.getRawRoot()).invoke(((IFn)const__6.getRawRoot()).invoke(object3), ((IFn)const__7.getRawRoot()).invoke(object4, const__8));
            } else {
                object = x;
                Object object5 = null;
            }
        }
        return object;
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return db$forward_attr.invokeStatic(object2);
    }
}

