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

public final class db$new_ents_not_installed$needs_install_QMARK___13329
extends AFunction
implements IFn.LO {
    int p;
    public static final Var const__1 = RT.var((String)"datomic.db", (String)"eid->part");
    public static final Var const__3 = RT.var((String)"datomic.db", (String)"eid->eidx");

    public db$new_ents_not_installed$needs_install_QMARK___13329(int n) {
        this.p = n;
    }

    public final Object invokePrim(long e) {
        Boolean bl;
        boolean and__5236__auto__13331 = Numbers.isZero((long)((IFn.LL)const__1.getRawRoot()).invokePrim(e));
        if (and__5236__auto__13331) {
            db$new_ents_not_installed$needs_install_QMARK___13329 this_ = null;
            bl = Numbers.lte((long)this_.p, (long)((IFn.LL)const__3.getRawRoot()).invokePrim(e)) ? Boolean.TRUE : Boolean.FALSE;
        } else {
            bl = and__5236__auto__13331 ? Boolean.TRUE : Boolean.FALSE;
        }
        return bl;
    }

    public Object invoke(Object object) {
        return this.invokePrim(RT.uncheckedLongCast((Object)((Number)object)));
    }
}

