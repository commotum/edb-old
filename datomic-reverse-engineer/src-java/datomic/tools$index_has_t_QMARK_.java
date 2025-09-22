/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.IFn$LO
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

public final class tools$index_has_t_QMARK_
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"assoc");
    public static final Keyword const__1 = RT.keyword(null, (String)"memidx");
    public static final Var const__2 = RT.var((String)"datomic.db", (String)"mem-index-set");
    public static final Var const__4 = RT.var((String)"clojure.core", (String)"seq");
    public static final Var const__5 = RT.var((String)"datomic.api", (String)"datoms");
    public static final Keyword const__6 = RT.keyword(null, (String)"eavt");
    public static final Var const__7 = RT.var((String)"datomic.api", (String)"t->tx");

    public static Object invokeStatic(Object db2, Object t) {
        Object idb;
        Object object = db2;
        db2 = null;
        Object object2 = idb = ((IFn)const__0.getRawRoot()).invoke(object, (Object)const__1, const__2.getRawRoot());
        idb = null;
        Object object3 = t;
        t = null;
        return RT.booleanCast((Object)((IFn)const__4.getRawRoot()).invoke(((IFn)const__5.getRawRoot()).invoke(object2, (Object)const__6, ((IFn.LO)const__7.getRawRoot()).invokePrim(RT.longCast((Object)((Number)object3)))))) ? Boolean.TRUE : Boolean.FALSE;
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return tools$index_has_t_QMARK_.invokeStatic(object3, object4);
    }
}

