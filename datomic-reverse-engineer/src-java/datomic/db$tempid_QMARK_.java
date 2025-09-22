/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.IFn$LO
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

public final class db$tempid_QMARK_
extends AFunction
implements IFn.LO {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"not");

    public static Object invokeStatic(long eid) {
        return ((IFn)const__0.getRawRoot()).invoke((Object)(Numbers.isZero((long)(0xC000000000000000L & eid)) ? Boolean.TRUE : Boolean.FALSE));
    }

    public Object invoke(Object object) {
        return db$tempid_QMARK_.invokeStatic(RT.uncheckedLongCast((Object)((Number)object)));
    }

    public final Object invokePrim(long l) {
        return db$tempid_QMARK_.invokeStatic(l);
    }
}

