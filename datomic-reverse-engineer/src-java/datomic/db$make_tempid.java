/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn$LLL
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.RT;
import clojure.lang.Var;

public final class db$make_tempid
extends AFunction
implements IFn.LLL {
    public static final Var const__2 = RT.var((String)"datomic.db", (String)"make-eid");

    public static long invokeStatic(long part2, long l) {
        return Long.MIN_VALUE | ((IFn.LLL)const__2.getRawRoot()).invokePrim(part2, l);
    }

    public Object invoke(Object object, Object object2) {
        return new Long(db$make_tempid.invokeStatic(RT.uncheckedLongCast((Object)((Number)object)), RT.uncheckedLongCast((Object)((Number)object2))));
    }

    public final long invokePrim(long l, long l2) {
        return db$make_tempid.invokeStatic(l, l2);
    }
}

