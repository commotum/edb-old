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

public final class db$find_aevt$fn__12973
extends AFunction {
    Object e;
    Object attrid;
    Object v;
    public static final Var const__3 = RT.var((String)"datomic.common", (String)"compare");

    public db$find_aevt$fn__12973(Object object, Object object2, Object object3) {
        this.e = object;
        this.attrid = object2;
        this.v = object3;
    }

    public Object invoke(Object it) {
        Boolean bl;
        boolean and__5236__auto__12978 = Util.equiv((Object)this_.attrid, (long)((IDatum)it).getA());
        if (and__5236__auto__12978) {
            boolean and__5236__auto__12977;
            boolean or__5238__auto__12975 = Util.identical((Object)this_.e, null);
            boolean bl2 = and__5236__auto__12977 = or__5238__auto__12975 ? or__5238__auto__12975 : Util.equiv((Object)this_.e, (long)((IDatum)it).getE());
            if (and__5236__auto__12977) {
                boolean or__5238__auto__12976 = Util.identical((Object)this_.v, null);
                if (or__5238__auto__12976) {
                    bl = or__5238__auto__12976 ? Boolean.TRUE : Boolean.FALSE;
                } else {
                    Object object = it;
                    it = null;
                    db$find_aevt$fn__12973 this_ = null;
                    bl = Numbers.isZero((long)((IFn.OOL)const__3.getRawRoot()).invokePrim(this_.v, ((IDatum)object).getV())) ? Boolean.TRUE : Boolean.FALSE;
                }
            } else {
                bl = and__5236__auto__12977 ? Boolean.TRUE : Boolean.FALSE;
            }
        } else {
            bl = and__5236__auto__12978 ? Boolean.TRUE : Boolean.FALSE;
        }
        return bl;
    }
}

