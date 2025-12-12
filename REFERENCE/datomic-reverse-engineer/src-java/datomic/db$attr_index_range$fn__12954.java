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
import datomic.impl.db.IDatum;

public final class db$attr_index_range$fn__12954
extends AFunction {
    long attrid;
    Object end;
    public static final Var const__3 = RT.var((String)"datomic.common", (String)"compare");

    public db$attr_index_range$fn__12954(long l, Object object) {
        this.attrid = l;
        this.end = object;
    }

    public Object invoke(Object p1__12953_SHARP_) {
        Boolean bl;
        boolean and__5236__auto__12957 = Util.equiv((long)this_.attrid, (long)((IDatum)p1__12953_SHARP_).getA());
        if (and__5236__auto__12957) {
            boolean or__5238__auto__12956 = Util.identical((Object)this_.end, null);
            if (or__5238__auto__12956) {
                bl = or__5238__auto__12956 ? Boolean.TRUE : Boolean.FALSE;
            } else {
                Object object = p1__12953_SHARP_;
                p1__12953_SHARP_ = null;
                db$attr_index_range$fn__12954 this_ = null;
                bl = Numbers.isNeg((long)((IFn.OOL)const__3.getRawRoot()).invokePrim(((IDatum)object).getV(), this_.end)) ? Boolean.TRUE : Boolean.FALSE;
            }
        } else {
            bl = and__5236__auto__12957 ? Boolean.TRUE : Boolean.FALSE;
        }
        return bl;
    }
}

