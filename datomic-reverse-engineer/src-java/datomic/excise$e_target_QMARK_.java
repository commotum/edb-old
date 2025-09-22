/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.IFn$LL
 *  clojure.lang.Numbers
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Numbers;
import clojure.lang.RT;
import clojure.lang.Var;

public final class excise$e_target_QMARK_
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"not");
    public static final Var const__2 = RT.var((String)"datomic.db", (String)"eid->part");

    public static Object invokeStatic(Object id) {
        Object object = id;
        id = null;
        return ((IFn)const__0.getRawRoot()).invoke((Object)(Numbers.isZero((long)((IFn.LL)const__2.getRawRoot()).invokePrim(RT.longCast((Object)((Number)object)))) ? Boolean.TRUE : Boolean.FALSE));
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return excise$e_target_QMARK_.invokeStatic(object2);
    }
}

