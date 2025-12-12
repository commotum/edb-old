/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
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

public final class excise$a_target_QMARK_
extends AFunction {
    public static final Var const__1 = RT.var((String)"datomic.db", (String)"eid->part");

    public static Object invokeStatic(Object id) {
        Object object = id;
        id = null;
        return Numbers.isZero((long)((IFn.LL)const__1.getRawRoot()).invokePrim(RT.longCast((Object)((Number)object)))) ? Boolean.TRUE : Boolean.FALSE;
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return excise$a_target_QMARK_.invokeStatic(object2);
    }
}

