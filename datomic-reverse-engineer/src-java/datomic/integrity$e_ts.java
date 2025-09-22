/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.Keyword
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Keyword;
import clojure.lang.RT;
import clojure.lang.Var;
import datomic.integrity$e_ts$fn__22391;

public final class integrity$e_ts
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"sort");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"distinct");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"map");
    public static final Var const__3 = RT.var((String)"datomic.api", (String)"datoms");
    public static final Keyword const__4 = RT.keyword(null, (String)"eavt");

    public static Object invokeStatic(Object db2, Object e) {
        Object object = db2;
        db2 = null;
        Object object2 = e;
        e = null;
        return ((IFn)const__0.getRawRoot()).invoke(((IFn)const__1.getRawRoot()).invoke(((IFn)const__2.getRawRoot()).invoke((Object)new integrity$e_ts$fn__22391(), ((IFn)const__3.getRawRoot()).invoke(object, (Object)const__4, object2))));
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return integrity$e_ts.invokeStatic(object3, object4);
    }
}

