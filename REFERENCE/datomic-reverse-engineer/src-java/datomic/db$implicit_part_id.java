/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn$LL
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

public final class db$implicit_part_id
extends AFunction
implements IFn.LO {
    public static final Var const__0 = RT.var((String)"datomic.db", (String)"eid->part");
    public static final Var const__4 = RT.var((String)"datomic.db", (String)"eid->eidx");

    public static Object invokeStatic(long part2) {
        long partbits2 = ((IFn.LL)const__0.getRawRoot()).invokePrim(part2);
        boolean and__5236__auto__12406 = Numbers.gte((long)partbits2, (long)524288L);
        return (and__5236__auto__12406 ? Numbers.isZero((long)((IFn.LL)const__4.getRawRoot()).invokePrim(part2)) : and__5236__auto__12406) ? (Number)Numbers.num((long)Numbers.xor((long)partbits2, (long)524288L)) : (Number)null;
    }

    public Object invoke(Object object) {
        return db$implicit_part_id.invokeStatic(RT.uncheckedLongCast((Object)((Number)object)));
    }

    public final Object invokePrim(long l) {
        return db$implicit_part_id.invokeStatic(l);
    }
}

