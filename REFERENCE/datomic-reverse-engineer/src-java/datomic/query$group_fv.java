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
import datomic.query$group_fv$fn__19472;
import datomic.query$group_fv$list_QMARK___19470;

public final class query$group_fv
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"zipmap");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"range");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"find-ns");
    public static final AFn const__3 = (AFn)Symbol.intern(null, (String)"datomic.aggregation");
    public static final Var const__4 = RT.var((String)"clojure.core", (String)"mapv");

    public static Object invokeStatic(Object find_clause, Object group_clause) {
        Object object = find_clause;
        find_clause = null;
        Object smap = ((IFn)const__0.getRawRoot()).invoke(object, ((IFn)const__1.getRawRoot()).invoke());
        Object gns = ((IFn)const__2.getRawRoot()).invoke((Object)const__3);
        query$group_fv$list_QMARK___19470 list_QMARK_ = new query$group_fv$list_QMARK___19470();
        Object object2 = gns;
        gns = null;
        query$group_fv$list_QMARK___19470 query$group_fv$list_QMARK___19470 = list_QMARK_;
        list_QMARK_ = null;
        Object object3 = smap;
        smap = null;
        Object object4 = group_clause;
        group_clause = null;
        return ((IFn)const__4.getRawRoot()).invoke((Object)new query$group_fv$fn__19472(object2, (Object)query$group_fv$list_QMARK___19470, object3), object4);
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return query$group_fv.invokeStatic(object3, object4);
    }
}

