/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.IFn$LL
 *  clojure.lang.IFn$LLL
 *  clojure.lang.Keyword
 *  clojure.lang.Numbers
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Keyword;
import clojure.lang.Numbers;
import clojure.lang.RT;
import clojure.lang.Var;

public final class db$implicit_part
extends AFunction
implements IFn.LL {
    public static final Var const__4 = RT.var((String)"datomic.db", (String)"make-eid");
    public static final Var const__6 = RT.var((String)"datomic.error", (String)"arg");
    public static final Keyword const__7 = RT.keyword((String)"db.error", (String)"implicit-part-out-of-range");
    public static final Var const__8 = RT.var((String)"clojure.core", (String)"str");

    /*
     * WARNING - void declaration
     */
    public static long invokeStatic(long id) {
        void var2_1;
        boolean and__5236__auto__12404 = Numbers.lt((long)id, (long)524288L);
        return ((Number)((and__5236__auto__12404 ? Numbers.gte((long)id, (long)0L) : var2_1) ? Numbers.num((long)((IFn.LLL)const__4.getRawRoot()).invokePrim(id | 0x80000L, 0L)) : ((IFn)const__6.getRawRoot()).invoke((Object)const__7, ((IFn)const__8.getRawRoot()).invoke((Object)Numbers.num((long)id), (Object)" out of implicit part range")))).longValue();
    }

    public Object invoke(Object object) {
        return new Long(db$implicit_part.invokeStatic(RT.uncheckedLongCast((Object)((Number)object))));
    }

    public final long invokePrim(long l) {
        return db$implicit_part.invokeStatic(l);
    }
}

