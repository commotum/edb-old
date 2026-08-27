/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn$LL
 *  clojure.lang.IFn$LLL
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.RT;
import clojure.lang.Var;

public final class db$t__GT_tx
extends AFunction
implements IFn.LL {
    public static final Var const__0 = RT.var((String)"datomic.db", (String)"make-eid");

    public static long invokeStatic(long t) {
        return ((IFn.LLL)const__0.getRawRoot()).invokePrim(3L, t);
    }

    public Object invoke(Object object) {
        return new Long(db$t__GT_tx.invokeStatic(RT.uncheckedLongCast((Object)((Number)object))));
    }

    public final long invokePrim(long l) {
        return db$t__GT_tx.invokeStatic(l);
    }
}

