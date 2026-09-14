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

public final class query$has_self_unifications_QMARK_$fn__19416
extends AFunction {
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"filter");
    public static final Var const__3 = RT.var((String)"datomic.datalog", (String)"variable?");
    public static final Var const__4 = RT.var((String)"clojure.core", (String)"set");

    public Object invoke(Object clause) {
        long l = RT.count((Object)((IFn)const__2.getRawRoot()).invoke(const__3.getRawRoot(), clause));
        Object object = clause;
        clause = null;
        query$has_self_unifications_QMARK_$fn__19416 this_ = null;
        return Util.equiv((long)l, (long)RT.count((Object)((IFn)const__4.getRawRoot()).invoke(((IFn)const__2.getRawRoot()).invoke(const__3.getRawRoot(), object)))) ? Boolean.TRUE : Boolean.FALSE;
    }
}

