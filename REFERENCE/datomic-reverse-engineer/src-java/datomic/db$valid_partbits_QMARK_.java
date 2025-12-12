/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.IFn$OLO
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

public final class db$valid_partbits_QMARK_
extends AFunction
implements IFn.OLO {
    public static final Var const__2 = RT.var((String)"datomic.db", (String)"explicit-partition");

    public static Object invokeStatic(Object db2, long partbits2) {
        Object object;
        boolean or__5238__auto__12682 = Numbers.gte((long)partbits2, (long)524288L);
        if (or__5238__auto__12682) {
            object = or__5238__auto__12682 ? Boolean.TRUE : Boolean.FALSE;
        } else {
            Object object2 = db2;
            db2 = null;
            object = ((IFn)const__2.getRawRoot()).invoke(object2, (Object)Numbers.num((long)partbits2));
        }
        return object;
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        return db$valid_partbits_QMARK_.invokeStatic(object3, RT.uncheckedLongCast((Object)((Number)object2)));
    }

    public final Object invokePrim(Object object, long l) {
        Object object2 = object;
        object = null;
        return db$valid_partbits_QMARK_.invokeStatic(object2, l);
    }
}

