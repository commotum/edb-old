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
import datomic.tools$log_size_STAR_$fn__21765;

public final class tools$log_size_STAR_
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.api", (String)"log");
    public static final Var const__1 = RT.var((String)"datomic.api", (String)"db");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"reduce");
    public static final Object const__3 = 0L;
    public static final Var const__4 = RT.var((String)"datomic.api", (String)"tx-range");

    public static Object invokeStatic(Object conn, Object t) {
        Object log2 = ((IFn)const__0.getRawRoot()).invoke(conn);
        Object object = conn;
        conn = null;
        ((IFn)const__1.getRawRoot()).invoke(object);
        Object object2 = log2;
        log2 = null;
        Object object3 = t;
        t = null;
        return ((IFn)const__2.getRawRoot()).invoke((Object)new tools$log_size_STAR_$fn__21765(), const__3, ((IFn)const__4.getRawRoot()).invoke(object2, object3, null));
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return tools$log_size_STAR_.invokeStatic(object3, object4);
    }
}

