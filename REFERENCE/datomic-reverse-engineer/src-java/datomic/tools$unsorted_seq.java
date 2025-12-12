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
import datomic.tools$unsorted_seq$fn__21869;

public final class tools$unsorted_seq
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"seq");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"map");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"vec");
    public static final Var const__3 = RT.var((String)"clojure.core", (String)"remove");
    public static final Var const__4 = RT.var((String)"clojure.core", (String)"partition");
    public static final Object const__5 = 2L;
    public static final Object const__6 = 1L;

    public static Object invokeStatic(Object pred2, Object s) {
        Object object = pred2;
        pred2 = null;
        Object object2 = s;
        s = null;
        return ((IFn)const__0.getRawRoot()).invoke(((IFn)const__1.getRawRoot()).invoke(const__2.getRawRoot(), ((IFn)const__3.getRawRoot()).invoke((Object)new tools$unsorted_seq$fn__21869(object), ((IFn)const__4.getRawRoot()).invoke(const__5, const__6, object2))));
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return tools$unsorted_seq.invokeStatic(object3, object4);
    }
}

