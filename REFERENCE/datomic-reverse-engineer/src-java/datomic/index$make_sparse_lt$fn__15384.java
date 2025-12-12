/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn$OOL
 *  clojure.lang.Numbers
 *  clojure.lang.RT
 *  clojure.lang.Util
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Numbers;
import clojure.lang.RT;
import clojure.lang.Util;
import clojure.lang.Var;
import datomic.db.Datum;
import java.util.Comparator;

public final class index$make_sparse_lt$fn__15384
extends AFunction {
    Object cmp;
    public static final Var const__2 = RT.var((String)"datomic.common", (String)"compare");

    public index$make_sparse_lt$fn__15384(Object object) {
        this.cmp = object;
    }

    /*
     * WARNING - void declaration
     */
    public Object invoke(Object k1, Object k2) {
        Boolean bl;
        void var3_3;
        boolean and__5236__auto__15386;
        boolean and__5236__auto__15387 = Util.equiv((long)((Datum)k1).getE(), (long)((Datum)k2).getE());
        boolean bl2 = and__5236__auto__15387 ? ((and__5236__auto__15386 = Util.equiv((long)((Datum)k1).getA(), (long)((Datum)k2).getA())) ? Numbers.isZero((long)((IFn.OOL)const__2.getRawRoot()).invokePrim(((Datum)k1).getV(), ((Datum)k2).getV())) : and__5236__auto__15386) : var3_3;
        if (bl2) {
            bl = null;
        } else {
            Object object = k1;
            k1 = null;
            Object object2 = k2;
            k2 = null;
            index$make_sparse_lt$fn__15384 this_ = null;
            bl = Numbers.isNeg((long)((Comparator)this_.cmp).compare(object, object2)) ? Boolean.TRUE : Boolean.FALSE;
        }
        return bl;
    }
}

