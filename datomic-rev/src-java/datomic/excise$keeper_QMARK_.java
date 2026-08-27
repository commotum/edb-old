/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
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
import datomic.impl.db.IDatum;

public final class excise$keeper_QMARK_
extends AFunction {
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"contains?");
    public static final Var const__2 = RT.var((String)"datomic.excise", (String)"bootids");

    public static Object invokeStatic(Object d) {
        Object object;
        boolean or__5238__auto__14808 = Numbers.isZero((long)((IDatum)d).getP());
        if (or__5238__auto__14808) {
            object = or__5238__auto__14808 ? Boolean.TRUE : Boolean.FALSE;
        } else {
            Object object2 = d;
            d = null;
            object = ((IFn)const__1.getRawRoot()).invoke(const__2.getRawRoot(), (Object)((IDatum)object2).getA());
        }
        return object;
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return excise$keeper_QMARK_.invokeStatic(object2);
    }
}

