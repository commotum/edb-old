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

public final class db$reverse_key
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"name");
    public static final Object const__2 = Character.valueOf('_');
    public static final Var const__4 = RT.var((String)"clojure.core", (String)"keyword");
    public static final Var const__5 = RT.var((String)"clojure.core", (String)"namespace");
    public static final Var const__6 = RT.var((String)"clojure.core", (String)"subs");
    public static final Object const__7 = 1L;
    public static final Var const__8 = RT.var((String)"clojure.core", (String)"str");

    public static Object invokeStatic(Object k) {
        Object object;
        Object n = ((IFn)const__0.getRawRoot()).invoke(k);
        if (Util.equiv((char)((Character)const__2).charValue(), (char)((String)n).charAt(RT.uncheckedIntCast((long)0L)))) {
            Object object2 = k;
            k = null;
            Object object3 = n;
            n = null;
            object = ((IFn)const__4.getRawRoot()).invoke(((IFn)const__5.getRawRoot()).invoke(object2), ((IFn)const__6.getRawRoot()).invoke(object3, const__7));
        } else {
            Object object4 = k;
            k = null;
            Object object5 = n;
            n = null;
            object = ((IFn)const__4.getRawRoot()).invoke(((IFn)const__5.getRawRoot()).invoke(object4), ((IFn)const__8.getRawRoot()).invoke((Object)"_", object5));
        }
        return object;
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return db$reverse_key.invokeStatic(object2);
    }
}

