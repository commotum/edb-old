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
import datomic.db$run_hooks$fn__13583;
import datomic.db$run_hooks$fn__13586;
import datomic.db$run_hooks$fn__13593;

public final class db$run_hooks
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"assoc");
    public static final Var const__1 = RT.var((String)"datomic.db", (String)"hooks");
    public static final Object const__2 = 19L;
    public static final Var const__3 = RT.var((String)"datomic.db", (String)"install-attribute-hook");
    public static final Object const__4 = 10L;
    public static final Var const__5 = RT.var((String)"clojure.core", (String)"reduce");
    public static final Var const__6 = RT.var((String)"datomic.db", (String)"ident-setting-datoms");
    public static final Var const__7 = RT.var((String)"datomic.db", (String)"update-schema-level");
    public static final Var const__8 = RT.var((String)"clojure.core", (String)"keep-indexed");

    public static Object invokeStatic(Object db2, Object index2) {
        Object ret;
        Object ret2;
        Object fhooks = ((IFn)const__0.getRawRoot()).invoke(((IFn)const__0.getRawRoot()).invoke(const__1.getRawRoot(), const__2, const__3.getRawRoot()), const__4, null);
        Object object = db2;
        Object object2 = db2;
        db2 = null;
        Object object3 = ret2 = ((IFn)const__5.getRawRoot()).invoke((Object)new db$run_hooks$fn__13583(), object, ((IFn)const__6.getRawRoot()).invoke(object2, index2));
        ret2 = null;
        Object ret3 = ((IFn)const__7.getRawRoot()).invoke(object3);
        Object object4 = index2;
        index2 = null;
        Object object5 = ret3;
        ret3 = null;
        Object object6 = fhooks;
        fhooks = null;
        Object object7 = ret = ((IFn)const__5.getRawRoot()).invoke((Object)new db$run_hooks$fn__13586(object4), object5, ((IFn)const__8.getRawRoot()).invoke((Object)new db$run_hooks$fn__13593(), object6));
        ret = null;
        return object7;
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return db$run_hooks.invokeStatic(object3, object4);
    }
}

