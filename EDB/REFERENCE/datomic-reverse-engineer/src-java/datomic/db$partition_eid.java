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

public final class db$partition_eid
extends AFunction
implements IFn.LL {
    public static final Var const__0 = RT.var((String)"datomic.db", (String)"eid->part");
    public static final Var const__3 = RT.var((String)"datomic.db", (String)"make-eid");

    public static long invokeStatic(long eid) {
        long partbits2 = ((IFn.LL)const__0.getRawRoot()).invokePrim(eid);
        return partbits2 < 524288L ? partbits2 : ((IFn.LLL)const__3.getRawRoot()).invokePrim(partbits2, 0L);
    }

    public Object invoke(Object object) {
        return new Long(db$partition_eid.invokeStatic(RT.uncheckedLongCast((Object)((Number)object))));
    }

    public final long invokePrim(long l) {
        return db$partition_eid.invokeStatic(l);
    }
}

