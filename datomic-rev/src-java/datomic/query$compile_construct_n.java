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

public final class query$compile_construct_n
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"zipmap");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"range");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"seq");
    public static final Var const__3 = RT.var((String)"clojure.core", (String)"concat");
    public static final Var const__4 = RT.var((String)"clojure.core", (String)"list");
    public static final AFn const__5 = (AFn)Symbol.intern((String)"clojure.core", (String)"fn");
    public static final Var const__6 = RT.var((String)"clojure.core", (String)"apply");
    public static final Var const__7 = RT.var((String)"clojure.core", (String)"vector");
    public static final AFn const__8 = (AFn)Symbol.intern(null, (String)"tuple");
    public static final Var const__9 = RT.var((String)"clojure.core", (String)"mapv");
    public static final Var const__10 = RT.var((String)"clojure.core", (String)"partial");
    public static final Var const__11 = RT.var((String)"datomic.query", (String)"compile-construct-1");

    public static Object invokeStatic(Object find_clause, Object construct_clause) {
        Object smap;
        Object object = find_clause;
        find_clause = null;
        Object object2 = smap = ((IFn)const__0.getRawRoot()).invoke(object, ((IFn)const__1.getRawRoot()).invoke());
        smap = null;
        Object object3 = construct_clause;
        construct_clause = null;
        return ((IFn)const__2.getRawRoot()).invoke(((IFn)const__3.getRawRoot()).invoke(((IFn)const__4.getRawRoot()).invoke((Object)const__5), ((IFn)const__4.getRawRoot()).invoke(((IFn)const__6.getRawRoot()).invoke(const__7.getRawRoot(), ((IFn)const__2.getRawRoot()).invoke(((IFn)const__3.getRawRoot()).invoke(((IFn)const__4.getRawRoot()).invoke((Object)const__8))))), ((IFn)const__4.getRawRoot()).invoke(((IFn)const__6.getRawRoot()).invoke(const__7.getRawRoot(), ((IFn)const__2.getRawRoot()).invoke(((IFn)const__3.getRawRoot()).invoke(((IFn)const__9.getRawRoot()).invoke(((IFn)const__10.getRawRoot()).invoke(const__11.getRawRoot(), object2), object3)))))));
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return query$compile_construct_n.invokeStatic(object3, object4);
    }
}

