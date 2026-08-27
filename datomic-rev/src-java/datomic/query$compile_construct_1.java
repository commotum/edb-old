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
import datomic.query$compile_construct_1$fn__19420;

public final class query$compile_construct_1
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"cons");
    public static final AFn const__1 = (AFn)Symbol.intern(null, (String)"vector");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"map");

    public static Object invokeStatic(Object smap, Object clause) {
        Object object = smap;
        smap = null;
        Object object2 = clause;
        clause = null;
        return ((IFn)const__0.getRawRoot()).invoke((Object)const__1, ((IFn)const__2.getRawRoot()).invoke((Object)new query$compile_construct_1$fn__19420(object), object2));
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return query$compile_construct_1.invokeStatic(object3, object4);
    }
}

